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
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.*;
import i5.las2peer.restMapper.annotations.swagger.*;
import i5.las2peer.security.Agent;
import i5.las2peer.security.UserAgent;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import jodd.vtor.Violation;
import jodd.vtor.Vtor;
import jodd.vtor.constraint.*;
import org.jooq.SQLDialect;



import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import org.jooq.tools.json.JSONObject;


/**
 * Requirements Bazaar LAS2peer Service
 * <p/>
 * This is the main service class of the Requirements Bazaar
 *
 * @author István Koren
 */
@Path("bazaar")
@Version("0.1")
@ApiInfo(
        title = "Requirements Bazaar",
        description = "Requirements Bazaar project",
        termsOfServiceUrl = "http://requirements-bazaar.org",
        contact = "info@requirements-bazaar.org",
        license = "Apache2",
        licenseUrl = "http://requirements-bazaar.org/license"
)
public class BazaarService extends Service {

    //CONFIG PROPERTIES
    public static final String DEFAULT_DB_USERNAME = "root";
    protected String dbUserName = DEFAULT_DB_USERNAME;

    public static final String DEFAULT_DB_PASSWORD = "";
    protected String dbPassword = DEFAULT_DB_PASSWORD;

    public static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/reqbaz";
    protected String dbUrl = DEFAULT_DB_URL;

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

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        functionRegistrators = new ArrayList<BazaarFunctionRegistrator>();
        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws BazaarException {
                DALFacade dalFacade = null;
                try {
                    dalFacade = createConnection();
                    AuthorizationManager.SyncPrivileges(dalFacade);
                } catch (Exception ex) {
                   ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Error during synching privileges");
                }
                finally {
                    closeConnection(dalFacade);
                }
            }
        });

        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) {
                if (functions.contains(BazaarFunction.VALIDATION)){
                    createValidators();
                }
            }
        });

        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws Exception {
                if (functions.contains(BazaarFunction.USER_FIRST_LOGIN_HANDLING)){
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
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Unknown error in registrators");
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

        //TODO how to check if the user is anonymous?
        if(agent.getLoginName().equals("anonymous")) {
            agent.setEmail("anonymous@requirements-bazaar.org");
        }
        else if (agent.getUserData() != null){
            JsonObject userDataJson = new JsonParser().parse(agent.getUserData().toString()).getAsJsonObject();
            String agentPicture= userDataJson.getAsJsonPrimitive("picture").getAsString();
            if (agentPicture != null && !agentPicture.isEmpty())
                profileImage = agentPicture;
        }

        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer userIdByLAS2PeerId = dalFacade.getUserIdByLAS2PeerId(agent.getId());
            if (userIdByLAS2PeerId == null) {
                int userId = dalFacade.createUser(User.geBuilder(agent.getEmail()).admin(false).las2peerId(agent.getId()).userName(agent.getLoginName()).profileImage(profileImage).build());
                dalFacade.addUserToRole(userId,"SystemAdmin",null);
            }
        } catch (Exception ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Error during registering users at first login.");
        }
        finally {
            closeConnection(dalFacade);
        }

    }
    private DALFacade createConnection() throws Exception {
        Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/reqbaz", "root", "");
        return new DALFacadeImpl(dbConnection, SQLDialect.MYSQL);
    }

    private void closeConnection(DALFacade dalFacade) {
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

    /**
     * *******************************
     * SWAGGER
     * ********************************
     */

    @GET
    @Path("api-docs")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerResourceListing() {
        return RESTMapper.getSwaggerResourceListing(this.getClass());
    }

    @GET
    @Path("api-docs/{tlr}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerApiDeclaration(@PathParam("tlr") String tlr) {
        return RESTMapper.getSwaggerApiDeclaration(this.getClass(), tlr, "http://localhost:8080/bazaar/");
    }


    /**********************************
     * PROJECTS
     **********************************/

    /**
     * This method returns the list of projects on the server.
     *
     * @return a list of projects.
     */
    @GET
    @Path("projects")
    @ResourceListApi(description = "Requirement Bazaar API")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method returns the list of projects on the server.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of projects")
////            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getProjects(
            @QueryParam(name = "page", defaultValue = "0") int page,
            @QueryParam(name = "per_page", defaultValue = "10") int perPage) {

        Serializable userData = ((UserAgent) getActiveAgent()).getUserData();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        // if the user is not logged in, return all the public projects.
        UserAgent agent = (UserAgent) getActiveAgent();

        String resultJSON = "[]";
        Gson gson = new Gson();
        DALFacade dalFacade = null;
        try {
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());

            dalFacade = createConnection();
            List<Project> projects = null;
            if (agent.getLoginName().equals("anonymous")) {
                projects = dalFacade.listPublicProjects(pageInfo);
            } else {
                // return public projects and the ones the user belongs to
                long userId = agent.getId();
                projects = dalFacade.listPublicAndAuthorizedProjects(pageInfo,userId);
            }

            resultJSON = gson.toJson(projects);// return only public projects
        } catch (BazaarException bex) {
            return ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }




    /**
     * This method allows to create a new project.
     *
     * @return
     */
    @POST
    @Path("projects")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to create a new project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the id if creation was successful")
////            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String createProject(@ContentParam String project) {
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new project
        // TODO: check whether all required parameters are entered
        String resultJSON = "{\"success\" : \"true\"}";
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            Project projectToCreate = gson.fromJson(project, Project.class);
            vtor.validate(projectToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PROJECT, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Minimum logged-in users can create projects.");


            int projectId = dalFacade.createProject(projectToCreate);
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", projectId);
            resultJSON = new Gson().toJson(idJson);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method allows to retrieve a certain project.
     *
     * @param projectId the id of the project to retrieve
     * @return the details of a certain project.
     */
    @GET
    @Path("projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to retrieve a certain project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the detailed view of a certain project.")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getProject(@PathParam("projectId") int projectId) {
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        long userId = ((UserAgent) getActiveAgent()).getId();
        String resultJSON = "{}";
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isProjectPublic(projectId)) {

                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_PROJECT, String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PROJECT, String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see components.");
            }

            Project projectToReturn = dalFacade.getProjectById(projectId);
            resultJSON = projectToReturn.toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * Allows to update a certain project.
     *
     * @param projectId the id of the project to update.
     * @return a JSON string containing whether the operation was successful or
     * not.
     */
    //TODO CORRECT PARAMETER, ID IS NOT ENOUGH TO UPDATE
//    @PUT
//    @Path("projects/{projectId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String updateProject(@PathParam("projectId") int projectId) {
//
//    }

    //TODO DELETE PROJECT, DID WE WANTED IT
//    @DELETE
//    @Path("projects/{projectId}")
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
     * @return a list of components.
     */
    @GET
    @Path("projects/{projectId}/components")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method returns the list of components under a given project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of components for a given project")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getComponents(
            @PathParam("projectId") int projectId,
            @QueryParam(name = "page", defaultValue = "0") int page,
            @QueryParam(name = "per_page", defaultValue = "10") int perPage) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        // Otherwise return all the user can see.
        String resultJSON = "[]";
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isProjectPublic(projectId)) {

                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMPONENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMPONENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see components.");
            }

            List<Component> components = dalFacade.listComponentsByProjectId(projectId, pageInfo);
            resultJSON = gson.toJson(components);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method allows to create a new component under a given a project.
     *
     * @return
     */
    @POST
    @Path("projects/{projectId}/components")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to create a new component under a given a project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the id if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String createComponent(@PathParam("projectId") int projectId, @ContentParam String component) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new project
        // TODO: check whether all required parameters are entered
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        String resultJSON = "{\"success\" : \"true\"}";
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            Component componentToCreate = gson.fromJson(component, Component.class);
            vtor.validate(componentToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_COMPONENT,String.valueOf(componentToCreate.getProjectId()), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only admins can create components.");


            int componentId = dalFacade.createComponent(componentToCreate);
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", componentId);
            resultJSON = new Gson().toJson(idJson);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method allows to retrieve a certain component under a project.
     *
     * @param projectId   the id of the project
     * @param componentId the id of the component under a given project
     * @return the details of a certain component.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to retrieve a certain component under a project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the details of a certain component.")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        String resultJSON = "{}";
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Component componentById = dalFacade.getComponentById(componentId);
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isComponentPublic(componentId)) {



                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMPONENT,String.valueOf(componentById.getProjectId()), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMPONENT,String.valueOf(componentById.getProjectId()), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see components.");
            }


            resultJSON = componentById.toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

//    /**
//     * Allows to update a certain component under a project.
//     *
//     * @param projectId   the id of the project
//     * @param componentId the id of the component under a given project
//     * @return a JSON string containing whether the operation was successful or
//     * not.
//     */
//    @PUT
//    @Path("projects/{projectId}/components/{componentId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String updateComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
//        // TODO: check if user can change this project
//        return "{success=false}";
//    }

    @DELETE
    @Path("projects/{projectId}/components/{componentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("Deletes a component under a project by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String deleteComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
        long userId = ((UserAgent) getActiveAgent()).getId();

        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        String resultJSON = "{\"success\" : \"true\"}";
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMPONENT,String.valueOf(projectId), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only admins can modify components.");


            Project projectById = dalFacade.getProjectById(projectId);
            if (projectById.getDefaultComponentId() != componentId) {
                dalFacade.deleteComponentById(componentId);
            } else {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception(),
                        ExceptionLocation.BAZAARSERVICE,
                        ErrorCode.CANNOTDELETE,
                        "This component item with id " +componentId + " cannot be deleted, because it is the defaut component for the project!"
                        );
            }

        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**********************************
     * REQUIREMENTS
     **********************************/

    /**
     * This method returns the list of requirements for a specific project.
     *
     * @param projectId the ID of the project to retrieve requirements for.
     * @return a list of requirements
     */
    @GET
    @Path("projects/{projectId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method returns the list of requirements for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of requirements for a given project")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getRequirementsByProject(@PathParam("projectId") int projectId,
                                           @QueryParam(name = "page", defaultValue = "0") int page,
                                           @QueryParam(name = "per_page", defaultValue = "10") int perPage) {

        long userId = ((UserAgent) getActiveAgent()).getId();
        String resultJSON = "[]";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isProjectPublic(projectId)) {

                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see components.");
            }

            List<Requirement> requirements = dalFacade.listRequirementsByProject(projectId, pageInfo, internalUserId);
            resultJSON = gson.toJson(requirements);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method returns the list of requirements for a specific component.
     *
     * @param projectId   the ID of the project to retrieve requirements for.
     * @param componentId the id of the component under a given project
     * @return a list of requirements
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method returns the list of requirements for a specific component.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a list of requirements for a given component")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getRequirementsByComponent(@PathParam("projectId") int projectId,
                                             @PathParam("componentId") int componentId,
                                             @QueryParam(name = "page", defaultValue = "0") int page,
                                             @QueryParam(name = "per_page", defaultValue = "10") int perPage) {

        long userId = ((UserAgent) getActiveAgent()).getId();
        String resultJSON = "[]";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            //TODO use components requirementId not the one it is sent for security context info
            if (dalFacade.isComponentPublic(componentId)) {

                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see components.");
            }

            List<Requirement> requirements = dalFacade.listRequirementsByComponent(componentId, pageInfo, internalUserId);
            resultJSON = gson.toJson(requirements);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method allows to create a new requirement.
     *
     * @param projectId   the ID of the project to create the requirement in.
     * @param componentId the id of the component under a given project
     * @return true if the creation was successful, otherwise false
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to create a new requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns id if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String createRequirement(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId,
                                    @ContentParam String requirement) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{\"success\" : \"true\"}";
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            Requirement requirementToCreate = gson.fromJson(requirement, Requirement.class);
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            requirementToCreate.setCreatorId(internalUserId);

            vtor.validate(requirementToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            vtor.validate(componentId);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_REQUIREMENT, String.valueOf(requirementToCreate.getProjectId()), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Minimum project members can create requirements.");


            int requirementId = dalFacade.createRequirement(requirementToCreate, componentId);
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", requirementId);
            resultJSON = new Gson().toJson(idJson);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method returns a specific requirement within a project.
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement to retrieve.
     * @return a specific requirement.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method returns a specific requirement within a project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns a requirement")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getRequirement(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId,
                                 @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();

        String resultJSON = "{}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            RequirementEx requirementById = dalFacade.getRequirementById(requirementId);
            if (dalFacade.isRequirementPublic(requirementId)) {

                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT,String.valueOf(requirementById.getProjectId()), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT,String.valueOf(requirementById.getProjectId()), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see components.");
            }


            resultJSON = requirementById.toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    //TODO
//    /**
//     * This method updates a specific requirement within a project.
//     *
//     * @param projectId     the ID of the project of the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement to update.
//     * @return the updated requirement.
//     */
//    @PUT
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}")
//    public String updateRequirement(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId,
//                                    @PathParam("requirementId") int requirementId) {
//        return "[]";
//    }

    /**
     * This method deletes a specific requirement within a project.
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement to delete.
     * @return the updated requirement.
     */
    @DELETE
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method deletes a specific requirement within a project.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String deleteRequirement(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId,
                                    @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            //Todo use requirement's projectId for serurity context, not the one sent from client
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT,Arrays.asList(String.valueOf(projectId), String.valueOf(requirementId)), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only the creator and admins can modify attachments.");


            dalFacade.deleteRequirementById(requirementId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }


    //TODO IS IT NEEDED??? NOT A SIMPLE UPDATE?
//    /**
//     * This method set the current user as a lead developer for a given requirement
//     *
//     * @param projectId     the ID of the project of the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement.
//     * @return
//     */
//    @POST
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/lead_developer")
//    public String setLeadDeveloper(@PathParam("projectId") int projectId,
//                                   @PathParam("componentId") int componentId,
//                                   @PathParam("requirementId") int requirementId) {
//        long userId = ((UserAgent) getActiveAgent()).getId();
//        // TODO: check whether the current user may create a new requirement
//        // TODO: check whether all required parameters are entered
//
//
//        return resultJSON;
//    }

    // TODO NOT SIMPLY UPDATE??
//    /**
//     * This method remove the current user as a lead developer for a given requirement
//     *
//     * @param projectId     the ID of the project of the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement.
//     * @return
//     */
//    @DELETE
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/lead_developer")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public String removeLeadDeveloper(@PathParam("projectId") int projectId,
//                                      @PathParam("componentId") int componentId,
//                                      @PathParam("requirementId") int requirementId) {
//        long userId = ((UserAgent) getActiveAgent()).getId();
//        // TODO: check whether the current user may create a new requirement
//        // TODO: check whether all required parameters are entered
//
//        return ("{success=false}");
//    }

    // TODO included in requirement.
//    /**
//     * This method returns the developers list of a given requirement.
//     *
//     * @param projectId     the ID of the project of the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement to retrieve.
//     * @return a list of the developers for the given requirement.
//     */
//    @GET
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers")
//    public String getDevelopers(@PathParam("projectId") int projectId,
//                                @PathParam("componentId") int componentId,
//                                @PathParam("requirementId") int requirementId) {
//        String resultJSON = "[]";
//        try {
//            createConnection();
//            dalFacade.list(requirementId);
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

    /**
     * This method add the current user to the developers list of a given requirement
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement.
     * @return
     */
    @POST
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method add the current user to the developers list of a given requirement")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String addUserToDevelopers(@PathParam("projectId") int projectId,
                                      @PathParam("componentId") int componentId,
                                      @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{}";
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_DEVELOP, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Project members can develop");

            CreationStatus creationStatus = dalFacade.wantToDevelop(internalUserId, requirementId);
            JsonObject resultJsonObject = new JsonObject();
            resultJsonObject.addProperty("status", String.valueOf(creationStatus));
            resultJSON = resultJsonObject.toString();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method remove the current user from a developers list of a given requirement
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method remove the current user from a developers list of a given requirement")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String removeUserFromDevelopers(@PathParam("projectId") int projectId,
                                           @PathParam("componentId") int componentId,
                                           @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{\"success\" : \"true\"}";
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_DEVELOP, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Project members can cancel to develop");

            dalFacade.notWantToDevelop(internalUserId, requirementId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    // TODO included in requirement
//    /**
//     * This method returns the followers list of a given requirement.
//     *
//     * @param projectId     the ID of the project of the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement to retrieve.
//     * @return a list of the followers for the given requirement.
//     */
//    @GET
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers")
//    public String getFollowers(@PathParam("projectId") int projectId,
//                               @PathParam("componentId") int componentId,
//                               @PathParam("requirementId") int requirementId) {
//        return "[]";
//    }

    /**
     * This method add the current user to the followers list of a given requirement
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement.
     * @return
     */
    @POST
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers")
    //@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method add the current user to the followers list of a given requirement")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String addUserToFollowers(@PathParam("projectId") int projectId,
                                     @PathParam("componentId") int componentId,
                                     @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{}";
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Project members can follow");

            CreationStatus creationStatus = dalFacade.follow(internalUserId, requirementId);
            JsonObject resultJsonObject = new JsonObject();
            resultJsonObject.addProperty("status", String.valueOf(creationStatus));
            resultJSON = resultJsonObject.toString();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method removes the current user from a followers list of a given requirement
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method removes the current user from a followers list of a given requirement")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String removeUserFromFollowers(@PathParam("projectId") int projectId,
                                          @PathParam("componentId") int componentId,
                                          @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Project members can cancel following");

            dalFacade.unFollow(internalUserId, requirementId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method creates a vote for the given requirement in the name of the current user.
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement.
     * @return
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/vote")
    //@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method creates a vote for the given requirement in the name of the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String addVote(@PathParam("projectId") int projectId,
                          @PathParam("componentId") int componentId,
                          @PathParam("requirementId") int requirementId,
                          @QueryParam(name = "direction", defaultValue = "up") String direction) {

        long userId = ((UserAgent) getActiveAgent()).getId();

        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        String resultJSON = "";
        try {
            if (!(direction.equals("up") || direction.equals("down"))) {
                vtor.addViolation(new Violation("Direction can only be \"up\" or \"down\"", direction, direction));
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }

            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_VOTE, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Project members can vote");

            CreationStatus creationStatus = dalFacade.vote(internalUserId, requirementId, direction.equals("up"));
            JsonObject resultJsonObject = new JsonObject();
            resultJsonObject.addProperty("status", String.valueOf(creationStatus));
            resultJSON = resultJsonObject.toString();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }


    /**
     * This method removes the vote of the given requirement made by the current user
     *
     * @param projectId     the ID of the project of the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/vote")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method removes the vote of the given requirement made by the current user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String removeVote(@PathParam("projectId") int projectId,
                             @PathParam("componentId") int componentId,
                             @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_VOTE, dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Project members can delete vote");

            dalFacade.unVote(internalUserId, requirementId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**********************************
     * COMMENTS
     **********************************/

    /**
     * This method returns the list of comments for a specific requirement.
     *
     * @param projectId     the ID of the project for the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement, which was commented.
     * @return a list of comments
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method returns the list of comments for a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns list of comments for the given requirement")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getComments(@PathParam("projectId") int projectId,
                              @PathParam("componentId") int componentId,
                              @PathParam("requirementId") int requirementId,
                              @QueryParam(name = "page", defaultValue = "0") int page,
                              @QueryParam(name = "per_page", defaultValue = "10") int perPage) {
        long userId = ((UserAgent) getActiveAgent()).getId();

        String resultJSON = "[]";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            //Todo use requirement's projectId for serurity context, not the one sent from client
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.isRequirementPublic(requirementId)) {

                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Even anonymous can watch this. Inform maintainers.");

            }
            else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMMENT,String.valueOf(projectId), dalFacade);
                if (!authorized)
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only project members can see comments.");
            }

            List<Comment> comments = dalFacade.listCommentsByRequirementId(requirementId, pageInfo);
            Gson gson = new Gson();
            resultJSON = gson.toJson(comments);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**
     * This method allows to create a new comment for a requirement.
     *
     * @param projectId     the ID of the project for the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement, which was commented.
     * @return true if the creation was successful, otherwise false
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to create a new comment for a requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the id if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String createComment(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId,
                                @ContentParam String comment) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            Comment commentToCreate = gson.fromJson(comment, Comment.class);
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            //Todo use requirement's projectId for serurity context, not the one sent from client
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_COMMENT,String.valueOf(projectId), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Minimum project members can create comments.");

            commentToCreate.setCreatorId(internalUserId);
            vtor.validate(commentToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            int commentId = dalFacade.createComment(commentToCreate);
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", commentId);
            resultJSON = new Gson().toJson(idJson);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    // TODO IS IT NEEDED?
//    /**
//     * This method returns a specific comment within a requirement.
//     *
//     * @param projectId     the ID of the project for the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement, which was commented.
//     * @param commentId     the ID of the comment, which should be returned.
//     * @return a specific comment.
//     */
//    @GET
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
//    public String getComment(@PathParam("projectId") int projectId,
//                             @PathParam("componentId") int componentId,
//                             @PathParam("requirementId") int requirementId,
//                             @PathParam("commentId") int commentId) {
//        return "[]";
//    }

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
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
//    public String updateComment(@PathParam("projectId") int projectId,
//                                @PathParam("componentId") int componentId,
//                                @PathParam("requirementId") int requirementId,
//                                @PathParam("commentId") int commentId) {
//        return "[]";
//    }

    /**
     * This method deletes a specific comment within a requirement.
     *
     * @param projectId     the ID of the project for the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement, which was commented.
     * @param commentId     the ID of the comment, which should be deleted.
     * @return the updated requirement.
     */
    @DELETE
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method deletes a specific comment within a requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String deleteComment(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId,
                                @PathParam("commentId") int commentId) {
        // TODO: check if the user may delete this requirement.
        long userId = ((UserAgent) getActiveAgent()).getId();
        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            //Todo use requirement's projectId for serurity context, not the one sent from client
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMMENT,Arrays.asList(String.valueOf(commentId),String.valueOf(projectId)), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only the creator and admins can modify comments.");


            dalFacade.deleteCommentById(commentId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
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
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getAttachments(@PathParam("projectId") int projectId,
//                                 @PathParam("componentId") int componentId,
//                                 @PathParam("requirementId") int requirementId,
//                                 @QueryParam(name = "page", defaultValue = "0")  int page,
//                                 @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
//
//    }

    /**
     * This method allows to create a new attachment for a requirement.
     *
     * @param projectId     the ID of the project for the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement, which is extended by the attachment.
     * @return true if the creation was successful, otherwise false
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method allows to create a new attachment for a requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the id if creation was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String createAttachment(@PathParam("projectId") int projectId,
                                   @PathParam("componentId") int componentId,
                                   @PathParam("requirementId") int requirementId,
                                   @QueryParam(name = "attachmentType", defaultValue = "U") String attachmentType,
                                   @ContentParam String attachment) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            Gson gson = new Gson();
            //TODO??? HOW DOES IT KNOW THE TYPE
            Attachment attachmentToCreate = gson.fromJson(attachment, Attachment.class);
            vtor.validate(attachmentToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            //Todo use requirement's projectId for serurity context, not the one sent from client
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_ATTACHMENT, String.valueOf(projectId), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Minimum project members can create attachments.");

            int attachmentId = dalFacade.createAttachment(attachmentToCreate);
            JsonObject idJson = new JsonObject();
            idJson.addProperty("id", attachmentId);
            resultJSON = new Gson().toJson(idJson);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
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
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
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
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
//    public String updateAttachment(@PathParam("projectId") int projectId,
//                                   @PathParam("componentId") int componentId,
//                                   @PathParam("requirementId") int requirementId,
//                                   @PathParam("attachmentId") int attachmentId) {
//        return "[]";
//    }

    /**
     * This method deletes a specific attachment within a requirement.
     *
     * @param projectId     the ID of the project for the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement, which was commented.
     * @param attachmentId  the ID of the attachment, which should be deleted.
     * @return the updated requirement.
     */
    @DELETE
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("This method deletes a specific attachment within a requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns {success : true} if deletion was successful")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String deleteAttachment(@PathParam("projectId") int projectId,
                                   @PathParam("componentId") int componentId,
                                   @PathParam("requirementId") int requirementId,
                                   @PathParam("attachmentId") int attachmentId) {
        long userId = ((UserAgent) getActiveAgent()).getId();

        String resultJSON = "{\"success\" : \"true\"}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();

            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            //Todo use requirement's projectId for serurity context, not the one sent from client
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_ATTACHMENT,Arrays.asList(String.valueOf(attachmentId),String.valueOf(projectId)), dalFacade);
            if (!authorized)
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "Only the creator and admins can modify attachments.");


            dalFacade.deleteAttachmentById(attachmentId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
    }

    /**********************************
     * USERS
     **********************************/

    // TODO SHOULD IT EXISTS?
//    /**
//     * Retrieves a list of all users.
//     *
//     * @return a JSON encoded list of all users.
//     */
//    @GET
//    @Path("users")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getUsers() {
//        // TODO: check if the admin user wants to retrieve all users.
//
//    }

    /**
     * Returns user data by ID
     *
     * @param userId the id of the user to be returned.
     * @return a JSON string of the user data specified by the id.
     */
    @GET
    @Path("users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Summary("Returns user data by ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns user data by ID")
//            @ApiResponse(code = 200, message = "Returns error handling JSON if error occurred")
    })
    public String getUser(@PathParam("userId") int userId) {
        // TODO: check whether the current user may request this project
        String resultJSON = "{}";
        String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if(registratorErrors != null) return registratorErrors;
        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            resultJSON = dalFacade.getUserById(userId).toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection(dalFacade);
        }

        return resultJSON;
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
//    @Path("users/{userId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String updateUser(@PathParam("userId") int userId) {
//        // TODO: check if user can change this project
//        return "{success=false}";
//    }


}
