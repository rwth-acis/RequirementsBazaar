/*
 *
 *  Copyright (c) 2014, RWTH Aachen University.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.*;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.security.UserAgent;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.*;
import io.swagger.jaxrs.config.DefaultReaderConfig;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import javax.ws.rs.*;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

import jodd.vtor.Violation;
import jodd.vtor.Vtor;
import org.jooq.SQLDialect;


import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;


/**
 * Requirements Bazaar LAS2peer Service
 * <p/>
 * This is the main service class of the Requirements Bazaar
 *
 * @author István Koren
 */
@Path("/bazaar")
@Version("0.2")
@Api
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.2",
                description = "Requirements Bazaar project",
                termsOfService = "http://requirements-bazaar.org",
                contact = @Contact(
                        name = "Requirements Bazaar Dev Team",
                        url = "http://requirements-bazaar.org",
                        email = "info@requirements-bazaar.org"
                ),
                license = @License(
                        name = "Apache2",
                        url = "http://requirements-bazaar.org/license"
                )
        ))
public class BazaarService extends Service {

    //CONFIG PROPERTIES
    protected String dbUserName;
    protected String dbPassword;
    protected String dbUrl;
    protected String lang;
    protected String country;

    private Vtor vtor;
    private List<BazaarFunctionRegistrator> functionRegistrators;

    /**
     * This method is needed for every RESTful application in LAS2peer.
     *
     * @return the mapping to the REST interface.
     */
    public String getRESTMapping() {
        String result = "";
        try {
            result = RESTMapper.getMethodsAsXML(this.getClass());
        } catch (Exception e) {

            e.printStackTrace();
        }
        return result;
    }

    public BazaarService() throws Exception {

        setFieldValues();
        Locale locale = new Locale(lang, country);
        Localization.getInstance().setResourceBundle(ResourceBundle.getBundle("i18n.Translation", locale));

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        functionRegistrators = new ArrayList<BazaarFunctionRegistrator>();
        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws BazaarException {
                DALFacade dalFacade = null;
                try {
                    dalFacade = createConnection();
                    AuthorizationManager.SyncPrivileges(dalFacade);
                } catch (CommunicationsException commEx) {
                    ExceptionHandler.getInstance().convertAndThrowException(commEx, ExceptionLocation.BAZAARSERVICE, ErrorCode.DB_COMM, Localization.getInstance().getResourceBundle().getString("error.db_comm"));
                } catch (Exception ex) {
                    ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.privilige_sync"));
                } finally {
                    closeConnection(dalFacade);
                }
            }
        });

        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) {
                if (functions.contains(BazaarFunction.VALIDATION)) {
                    createValidators();
                }
            }
        });

        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws Exception {
                if (functions.contains(BazaarFunction.USER_FIRST_LOGIN_HANDLING)) {
                    registerUserAtFirstLogin();
                }
            }
        });
    }

    private String notifyRegistrators(EnumSet<BazaarFunction> functions) {
        String resultJSON = null;
        try {
            for (BazaarFunctionRegistrator functionRegistrator : functionRegistrators) {
                functionRegistrator.registerFunction(functions);
            }
        } catch (BazaarException bazaarEx) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarEx);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.registrators"));
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);
        }
        return resultJSON;
    }

    private void createValidators() {
        vtor = new Vtor();
    }

    private void registerUserAtFirstLogin() throws Exception {
        UserAgent agent = (UserAgent) getActiveAgent();

        if (agent.getEmail() == null) agent.setEmail("NO.EMAIL@WARNING.COM");

        String profileImage = "https://api.learning-layers.eu/profile.png";
        String givenName = null;
        String familyName = null;

        //TODO how to check if the user is anonymous?
        if (agent.getLoginName().equals("anonymous")) {
            agent.setEmail("anonymous@requirements-bazaar.org");
        } else if (agent.getUserData() != null) {
            JsonObject userDataJson = new JsonParser().parse(agent.getUserData().toString()).getAsJsonObject();
            JsonPrimitive pictureJson = userDataJson.getAsJsonPrimitive("picture");
            String agentPicture;

            if (pictureJson == null)
                agentPicture = profileImage;
            else
                agentPicture = pictureJson.getAsString();

            if (agentPicture != null && !agentPicture.isEmpty())
                profileImage = agentPicture;
            String givenNameData = userDataJson.getAsJsonPrimitive("given_name").getAsString();
            if (givenNameData != null && !givenNameData.isEmpty())
                givenName = givenNameData;
            String familyNameData = userDataJson.getAsJsonPrimitive("family_name").getAsString();
            if (familyNameData != null && !familyNameData.isEmpty())
                familyName = familyNameData;
        }

        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer userIdByLAS2PeerId = dalFacade.getUserIdByLAS2PeerId(agent.getId());
            if (userIdByLAS2PeerId == null) {
                User.Builder userBuilder = User.geBuilder(agent.getEmail());
                if (givenName != null)
                    userBuilder = userBuilder.firstName(givenName);
                if (familyName != null)
                    userBuilder = userBuilder.lastName(familyName);
                User user = userBuilder.admin(false).las2peerId(agent.getId()).userName(agent.getLoginName()).profileImage(profileImage).build();
                int userId = dalFacade.createUser(user);
                dalFacade.addUserToRole(userId, "SystemAdmin", null);
            }
        } catch (Exception ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.first_login"));
        } finally {
            closeConnection(dalFacade);
        }

    }

    private DALFacade createConnection() throws Exception {
        Connection dbConnection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        return new DALFacadeImpl(dbConnection, SQLDialect.MYSQL);
    }

    private void closeConnection(DALFacade dalFacade) {
        if (dalFacade == null) return;
        Connection dbConnection = dalFacade.getConnection();
        if (dbConnection != null) {
            try {
                dbConnection.close();
                System.out.println("Database connection closed!");
            } catch (SQLException ignore) {
                System.out.println("Could not close db connection!");
            }
        }
    }

     /********************************
     * SWAGGER
     * ******************************/

    /**
     * Returns the API documentation of all annotated resources
     * for purposes of Swagger documentation.
     *
     * @return The resource's documentation.
     */
    @GET
    @Path("/swagger.json")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerJSON() {
        // TODO scan all resource classes
        //DefaultReaderConfig readerConfig = new DefaultReaderConfig();
        //readerConfig.setScanAllResources(true);
        //Swagger swagger = new Reader(new Swagger(), readerConfig).read(this.getClass());
        Swagger swagger = new Reader(new Swagger()).read(this.getClass());
        if (swagger == null) {
            return new HttpResponse("Swagger API declaration not available!", HttpURLConnection.HTTP_NOT_FOUND);
        }
        swagger.getDefinitions().clear();
        try {
            return new HttpResponse(Json.mapper().writeValueAsString(swagger), HttpURLConnection.HTTP_OK);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }


    /**********************************
     * PROJECTS
     **********************************/

    /**
     * This method returns the list of projects on the server.
     *
     * @param page    page number
     * @param perPage number of projects by page
     * @return Response with list of all projects
     */
    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of projects on the server.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of projects"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getProjects(
            @ApiParam(value = "Page number", required = false) @DefaultValue("1") @QueryParam("page") int page,
            @ApiParam(value = "Elements of project by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            UserAgent agent = (UserAgent) getActiveAgent();
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            List<Project> projects;
            if (agent.getLoginName().equals("anonymous")) {
                // return only public projects
                projects = dalFacade.listPublicProjects(pageInfo);
            } else {
                // return public projects and the ones the user belongs to
                long userId = agent.getId();
                projects = dalFacade.listPublicAndAuthorizedProjects(pageInfo, userId);
            }
            return new HttpResponse(gson.toJson(projects), 200);
        } catch (BazaarException bex) {
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve a certain project.
     *
     * @param projectId id of the project to retrieve
     * @return Response with a project as a JSON object.
     */
    @GET
    @Path("/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a certain project"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getProject(@PathParam("projectId") int projectId) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isProjectPublic(projectId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_PROJECT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PROJECT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                }
            }
            Project projectToReturn = dalFacade.getProjectById(projectId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(projectToReturn), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new project.
     *
     * @param project project as a JSON object
     * @return Response with the created project as a JSON object.
     */
    @POST
    @Path("/projects")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new project")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the created project"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse createProject(@ApiParam(value = "Project entity as JSON", required = true) String project) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            Gson gson = new Gson();
            Project projectToCreate = gson.fromJson(project, Project.class);
            vtor.validate(projectToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PROJECT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.project.create"));
            }
            projectToCreate.setLeaderId(internalUserId);
            Project createdProject = dalFacade.createProject(projectToCreate);
            return new HttpResponse(gson.toJson(createdProject), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * Allows to update a certain project.
     *
     * @param projectId id of the project to update
     * @param project   updated project as a JSON object
     * @return Response with the updated project as a JSON object.
     */
    @PUT
    @Path("/projects/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update a certain project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the updated project"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse updateProject(@PathParam("projectId") int projectId,
                                      @ApiParam(value = "Project entity as JSON", required = true) String project) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            Gson gson = new Gson();
            Project projectToUpdate = gson.fromJson(project, Project.class);
            vtor.validate(projectToUpdate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_PROJECT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.project.modify"));
            }
            if (projectToUpdate.getId() != 0 && projectId != projectToUpdate.getId()) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
            }
            Project updatedProject = dalFacade.modifyProject(projectToUpdate);
            return new HttpResponse(gson.toJson(updatedProject), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    //TODO DELETE PROJECT, DID WE WANT IT
//    @DELETE
//    @Path("/projects/{projectId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String deleteProject(@PathParam("projectId") int projectId) {
//        // TODO: check if user can delete this project
//        String resultJSON = "{\"success\" : \"true\"}";
//        try {
//            createConnection();
//            dalFacade.delePR(projectToCreate);
//        } catch (BazaarException bex) {
//            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
//        } catch (Exception ex) {
//            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
//            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);
//
//        } finally {
//            closeConnection();
//        }
//
//        return resultJSON;
//    }

    /**********************************
     * COMPONENTS
     **********************************/

    /**
     * This method returns the list of components under a given project.
     *
     * @param page    page number
     * @param perPage number of projects by page
     * @return Response with components as a JSON array.
     */
    @GET
    @Path("/projects/{projectId}/components")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of components under a given project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of components for a given project"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getComponentsByProject(
            @PathParam("projectId") int projectId,
            @ApiParam(value = "Page number", required = false) @DefaultValue("1") @QueryParam("page") int page,
            @ApiParam(value = "Elements of components by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.getProjectById(projectId) == null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "component"));
            }
            if (dalFacade.isProjectPublic(projectId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMPONENT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMPONENT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                }
            }
            List<Component> components = dalFacade.listComponentsByProjectId(projectId, pageInfo);
            return new HttpResponse(gson.toJson(components), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve a certain component.
     *
     * @param componentId id of the component under a given project
     * @return Response with a component as a JSON object.
     */
    @GET
    @Path("/components/{componentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain component.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a certain component"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getComponent(@PathParam("componentId") int componentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Component componentToReturn = dalFacade.getComponentById(componentId);
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isComponentPublic(componentId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMPONENT, String.valueOf(componentId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMPONENT, String.valueOf(componentId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                }
            }
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(componentToReturn), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new component.
     *
     * @param component component as a JSON object
     * @return Response with the created project as a JSON object.
     */
    @POST
    @Path("/components")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new component under a given a project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the created component"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse createComponent(@ApiParam(value = "Component entity as JSON", required = true) String component) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new project
            // TODO: check whether all required parameters are entered
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            Component componentToCreate = gson.fromJson(component, Component.class);
            vtor.validate(componentToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_COMPONENT, String.valueOf(componentToCreate.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.create"));
            }
            componentToCreate.setLeaderId(internalUserId);
            Component createdComponent = dalFacade.createComponent(componentToCreate);
            return new HttpResponse(gson.toJson(createdComponent), 201);
        } catch (BazaarException bex) {
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * Allows to update a certain component.
     *
     * @param componentId id of the component under a given project
     * @param component   updated component as a JSON object
     * @return Response with the updated component as a JSON object.
     */
    @PUT
    @Path("/components/{componentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update a certain component.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the updated component"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse updateComponent(@PathParam("componentId") int componentId,
                                        @ApiParam(value = "Tag entity as JSON", required = true) String component) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            Gson gson = new Gson();
            Component updatedComponent = gson.fromJson(component, Component.class);
            vtor.validate(updatedComponent);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMPONENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.modify"));
            }
            if (updatedComponent.getId() != 0 && componentId != updatedComponent.getId()) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
            }
            updatedComponent = dalFacade.modifyComponent(updatedComponent);
            return new HttpResponse(gson.toJson(updatedComponent), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * Allows to delete a component.
     *
     * @param componentId id of the component to delete
     * @return Response with deleted component as a JSON object.
     */
    @DELETE
    @Path("/components/{componentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific component.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the deleted component"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse deleteComponent(@PathParam("componentId") int componentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Component componentToDelete = dalFacade.getComponentById(componentId);
            Project project = dalFacade.getProjectById(componentToDelete.getProjectId());
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMPONENT, String.valueOf(project.getId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.modify"));
            }
            if (project.getDefaultComponentId() != null && project.getDefaultComponentId() == componentId) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception(),
                        ExceptionLocation.BAZAARSERVICE,
                        ErrorCode.CANNOTDELETE,
                        MessageFormat.format(Localization.getInstance().getResourceBundle().getString("error.authorization.component.delete"), componentId)
                );
            }
            Gson gson = new Gson();
            Component deletedComponent = dalFacade.deleteComponentById(componentId);
            return new HttpResponse(gson.toJson(deletedComponent), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**********************************
     * REQUIREMENTS
     **********************************/

    /**
     * This method returns the list of requirements for a specific project.
     *
     * @param projectId id of the project to retrieve requirements for
     * @param page      page number
     * @param perPage   number of projects by page
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/projects/{projectId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of requirements for a given project"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getRequirementsByProject(@PathParam("projectId") int projectId,
                                                 @ApiParam(value = "Page number", required = false) @DefaultValue("1") @QueryParam("page") int page,
                                                 @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.getProjectById(projectId) == null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "ressource"));
            }
            if (dalFacade.isProjectPublic(projectId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                }
            }
            List<Requirement> requirements = dalFacade.listRequirementsByProject(projectId, pageInfo, internalUserId);
            return new HttpResponse(gson.toJson(requirements), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of requirements for a specific component.
     *
     * @param componentId id of the component under a given project
     * @param page        page number
     * @param perPage     number of projects by page
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/components/{componentId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements for a specific component.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of requirements for a given project"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getRequirementsByComponent(@PathParam("componentId") int componentId,
                                                   @ApiParam(value = "Page number", required = false) @DefaultValue("1") @QueryParam("page") int page,
                                                   @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.getComponentById(componentId) == null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "component"));
            }
            Component component = dalFacade.getComponentById(componentId);
            Project project = dalFacade.getProjectById(component.getProjectId());
            if (dalFacade.isComponentPublic(componentId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                }
            }
            List<Requirement> requirements = dalFacade.listRequirementsByComponent(componentId, pageInfo, internalUserId);
            return new HttpResponse(gson.toJson(requirements), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method returns a specific requirement.
     *
     * @param requirementId id of the requirement to retrieve
     * @return Response with requirement as a JSON object.
     */
    @GET
    @Path("/requirements/{requirementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a certain requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            RequirementEx requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            if (dalFacade.isRequirementPublic(requirementId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                }
            }
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new requirement.
     *
     * @param requirement requirement as a JSON object
     * @return Response with the created requirement as a JSON object.
     */
    @POST
    @Path("/requirements")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the created requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse createRequirement(@ApiParam(value = "Requirement entity as JSON", required = true) String requirement) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            dalFacade = createConnection();
            Gson gson = new Gson();
            Requirement requirementToCreate = gson.fromJson(requirement, Requirement.class);
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            requirementToCreate.setCreatorId(internalUserId);
            if (requirementToCreate.getLeadDeveloperId() == 0) {
                requirementToCreate.setLeadDeveloperId(1);
            }
            vtor.useProfiles("create");
            vtor.validate(requirementToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            vtor.resetProfiles();
            // check if all components are in the same project
            for (Component component : requirementToCreate.getComponents()) {
                component = dalFacade.getComponentById(component.getId());
                if (requirementToCreate.getProjectId() != component.getProjectId()) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.VALIDATION, "Component does not fit with project");
                }
            }
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_REQUIREMENT, String.valueOf(requirementToCreate.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.create"));
            }
            Requirement createdRequirement = dalFacade.createRequirement(requirementToCreate, internalUserId);
            return new HttpResponse(gson.toJson(createdRequirement), 201);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method updates a specific requirement within a project and component.
     *
     * @param requirementId id of the requirement to update
     * @param requirement   requirement as a JSON object
     * @return Response with updated requirement as a JSON object.
     */
    @PUT
    @Path("/requirements/{requirementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method updates a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the updated requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse updateRequirement(@PathParam("requirementId") int requirementId,
                                          @ApiParam(value = "Requirement entity as JSON", required = true) String requirement) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            Gson gson = new Gson();
            Requirement requirementToUpdate = gson.fromJson(requirement, Requirement.class);
            vtor.validate(requirementToUpdate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.modify"));
            }
            if (requirementToUpdate.getId() != 0 && requirementId != requirementToUpdate.getId()) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
            }
            RequirementEx updatedRequirement = dalFacade.modifyRequirement(requirementToUpdate, internalUserId);
            return new HttpResponse(gson.toJson(updatedRequirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method deletes a specific requirement.
     *
     * @param requirementId id of the requirement to delete
     * @return Response with the deleted requirement as a JSON object.
     */
    @DELETE
    @Path("/requirements/{requirementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the deleted requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse deleteRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            RequirementEx requirementToDelete = dalFacade.getRequirementById(requirementId, internalUserId);
            Project project = dalFacade.getProjectById(requirementToDelete.getProjectId());
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, Arrays.asList(String.valueOf(project.getId()), String.valueOf(requirementId)), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.delete"));
            }
            Gson gson = new Gson();
            RequirementEx deletedRequirement = dalFacade.deleteRequirementById(requirementId, internalUserId);
            return new HttpResponse(gson.toJson(deletedRequirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method add the current user to the developers list of a given requirement.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @POST
    @Path("/requirements/{requirementId}/developers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method add the current user to the developers list of a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse addUserToDevelopers(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_DEVELOP, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.develop.create"));
            }
            dalFacade.wantToDevelop(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method remove the current user from a developers list of a given requirement.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @DELETE
    @Path("/requirements/{requirementId}/developers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method remove the current user from a developers list of a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse removeUserFromDevelopers(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_DEVELOP, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.develop.delete"));
            }
            dalFacade.notWantToDevelop(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method add the current user to the followers list of a given requirement.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @POST
    @Path("/requirements/{requirementId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method add the current user to the followers list of a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse addUserToFollowers(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.create"));
            }
            dalFacade.follow(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method removes the current user from a followers list of a given requirement.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @DELETE
    @Path("/requirements/{requirementId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the current user from a followers list of a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse removeUserFromFollowers(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.delete"));
            }
            dalFacade.unFollow(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method creates a vote for the given requirement in the name of the current user.
     *
     * @param requirementId id of the requirement
     * @param direction     "up" or "down" vote direction
     * @return Response with requirement as a JSON object.
     */
    @POST
    @Path("/requirements/{requirementId}/vote")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method creates a vote for the given requirement in the name of the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse addVote(@PathParam("requirementId") int requirementId,
                                @ApiParam(value = "Vote direction", allowableValues = "up, down") @DefaultValue("up") @QueryParam("direction") String direction) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            if (!(direction.equals("up") || direction.equals("down"))) {
                vtor.addViolation(new Violation("Direction can only be \"up\" or \"down\"", direction, direction));
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_VOTE, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.create"));
            }
            dalFacade.vote(internalUserId, requirementId, direction.equals("up"));
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method removes the vote of the given requirement made by the current user.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @DELETE
    @Path("/requirements/{requirementId}/vote")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the vote of the given requirement made by the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse removeVote(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_VOTE, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.delete"));
            }
            dalFacade.unVote(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(requirement), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**********************************
     * COMMENTS
     **********************************/

    /**
     * This method returns the list of comments for a specific requirement.
     *
     * @param requirementId id of the requirement, which was commented
     * @param page          page number
     * @param perPage       number of projects by page
     * @return Response with comments as a JSON array.
     */
    @GET
    @Path("/requirements/{requirementId}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of comments for a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of comments for a given requirement"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getComments(@PathParam("requirementId") int requirementId,
                                    @ApiParam(value = "Page number", required = false) @DefaultValue("1") @QueryParam("page") int page,
                                    @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();
            //Todo use requirement's projectId for serurity context, not the one sent from client
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Project project = dalFacade.getProjectById(requirement.getProjectId());
            if (dalFacade.isRequirementPublic(requirementId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMMENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.read"));
                }
            }
            List<Comment> comments = dalFacade.listCommentsByRequirementId(requirementId, pageInfo);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(comments), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve a certain comment.
     *
     * @param commentId id of the comment
     * @return Response with comment as a JSON object.
     */
    @GET
    @Path("/comments/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain comment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a certain comment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getComment(@PathParam("commentId") int commentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Comment comment = dalFacade.getCommentById(commentId);
            Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), internalUserId);
            if (dalFacade.isProjectPublic(requirement.getProjectId())) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.read"));
                }
            }
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(comment), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }


    /**
     * This method allows to create a new comment.
     *
     * @param comment comment as JSON object
     * @return Response with the created comment as JSON object.
     */
    @POST
    @Path("/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new comment.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns the created comment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse createComment(@ApiParam(value = "Comment entity as JSON", required = true) String comment) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            Comment commentToCreate = gson.fromJson(comment, Comment.class);
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(commentToCreate.getRequirementId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_COMMENT, String.valueOf(requirement.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.create"));
            }
            commentToCreate.setCreatorId(internalUserId);
            vtor.validate(commentToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            Comment createdComment = dalFacade.createComment(commentToCreate);
            return new HttpResponse(gson.toJson(createdComment), 201);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    //TODO Should exist?
//    /**
//     * This method updates a specific comment within a requirement.
//     *
//     * @param projectId
//     *            the ID of the project for the requirement.
//     * @param componentId
//     *            the id of the component under a given project
//     * @param requirementId
//     *            the ID of the requirement, which was commented.
//     * @param commentId
//     *            the ID of the comment, which should be returned.
//     * @return the updated requirement.
//     */
//    @PUT
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
//    public String updateComment(@PathParam("projectId") int projectId,
//                                @PathParam("componentId") int componentId,
//                                @PathParam("requirementId") int requirementId,
//                                @PathParam("commentId") int commentId,
//                                @ContentParam String comment) {
//    }

    /**
     * This method deletes a specific comment.
     *
     * @param commentId id of the comment, which should be deleted
     * @return Response with the deleted comment as a JSON object.
     */
    @DELETE
    @Path("/comments/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific comment.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the deleted comment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse deleteComment(@PathParam("commentId") int commentId) {
        DALFacade dalFacade = null;
        try {
            // TODO: check if the user may delete this requirement.
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Comment commentToDelete = dalFacade.getCommentById(commentId);
            Requirement requirement = dalFacade.getRequirementById(commentToDelete.getRequirementId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMMENT, Arrays.asList(String.valueOf(commentId), String.valueOf(requirement.getProjectId())), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.modify"));
            }
            Gson gson = new Gson();
            Comment deletedComment = dalFacade.deleteCommentById(commentId);
            return new HttpResponse(gson.toJson(deletedComment), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**********************************
     * ATTACHMENTS
     **********************************/

    // TODO INCLUDED IN REQUIREMENT?
//    /**
//     * This method returns the list of attachments for a specific requirement.
//     *
//     * @param projectId     the ID of the project for the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement, whose attachments should be returned.
//     * @return a list of attachments
//     */
//    @GET
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getAttachments(@PathParam("projectId") int projectId,
//                                 @PathParam("componentId") int componentId,
//                                 @PathParam("requirementId") int requirementId,
//                                 @QueryParam(name = "page", defaultValue = "0")  int page,
//                                 @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
//
//    }

    /**
     * This method allows to create a new attachment.
     *
     * @param attachmentType type of attachment
     * @param attachment     attachment as JSON object
     * @return Response with the created attachment as JSON object.
     */
    @POST
    @Path("/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns the created comment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse createAttachment(@ApiParam(value = "Attachment type", allowableValues = "U") @DefaultValue("U") @QueryParam("attachmentType") String attachmentType,
                                           @ApiParam(value = "Attachment entity as JSON", required = true) String attachment) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            //TODO??? HOW DOES IT KNOW THE TYPE
            Attachment attachmentToCreate = gson.fromJson(attachment, Attachment.class);
            vtor.validate(attachmentToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(attachmentToCreate.getRequirementId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.create"));
            }
            Attachment createdAttachment = dalFacade.createAttachment(attachmentToCreate);
            return new HttpResponse(gson.toJson(createdAttachment), 201);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    // TODO INCLUDED IN REQUIREMENT? NEEDED?
//    /**
//     * This method returns a specific attachment within a requirement.
//     *
//     * @param projectId     the ID of the project for the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement, which was commented.
//     * @param attachmentId  the ID of the attachment, which should be returned.
//     * @return a specific attachment.
//     */
//    @GET
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
//    public String getAttachment(@PathParam("projectId") int projectId,
//                                @PathParam("componentId") int componentId,
//                                @PathParam("requirementId") int requirementId,
//                                @PathParam("attachmentId") int attachmentId) {
//        return "[]";
//    }

    //TODO UPDATE?
//    /**
//     * This method updates a specific attachment within a requirement.
//     *
//     * @param projectId     the ID of the project for the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement, which was commented.
//     * @param attachmentId  the ID of the attachment, which should be returned.
//     * @return ??.
//     */
//    @PUT
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
//    public String updateAttachment(@PathParam("projectId") int projectId,
//                                   @PathParam("componentId") int componentId,
//                                   @PathParam("requirementId") int requirementId,
//                                   @PathParam("attachmentId") int attachmentId) {
//        return "[]";
//    }

    /**
     * This method deletes a specific attachment.
     *
     * @param attachmentId id of the attachment, which should be deleted
     * @return Response with the deleted attachment as a JSON object.
     */
    @DELETE
    @Path("/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the deleted attachment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse deleteAttachment(@PathParam("attachmentId") int attachmentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            // TODO check requirement
            Requirement requirement = dalFacade.getRequirementById(attachmentId, internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_ATTACHMENT, Arrays.asList(String.valueOf(attachmentId), String.valueOf(requirement.getProjectId())), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.modify"));
            }
            Attachment deletedAttachment = dalFacade.deleteAttachmentById(attachmentId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(deletedAttachment), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    /**********************************
     * USERS
     **********************************/

    /**
     * This method allows to retrieve a certain user.
     *
     * @param userId the id of the user to be returned
     * @return Response with user as a JSON object.
     */
    @GET
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a certain user"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getUser(@PathParam("userId") int userId) {
        DALFacade dalFacade = null;
        try {
            // TODO: check whether the current user may request this project
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            User user = dalFacade.getUserById(userId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(user), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

    //TODO UPDATE?
//    /**
//     * Allows to update a certain project.
//     *
//     * @param userId the id of the user to update.
//     * @return a JSON string containing whether the operation was successful or
//     * not.
//     */
//    @PUT
//    @Path("/users/{userId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String updateUser(@PathParam("userId") int userId) {
//        // TODO: check if user can change this project
//        return "{success=false}";
//    }

    /**
     * This method allows to retrieve the current user.
     *
     * @return Response with user as a JSON object.
     */
    @GET
    @Path("/users/current")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the current user"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getCurrentUser() {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            User user = dalFacade.getUserById(internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(user), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }

}
