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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Consumes;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.DELETE;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.PUT;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.QueryParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.security.Agent;
import i5.las2peer.security.UserAgent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import jodd.vtor.Violation;
import jodd.vtor.Vtor;
import jodd.vtor.constraint.*;
import org.jooq.SQLDialect;

import com.google.gson.Gson;

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
@Path("bazaar")
@Version("0.1")
public class BazaarService extends Service {

    //CONFIG PROPERTIES
    public static final String DEFAULT_DB_USERNAME = "root";
    protected String dbUserName = DEFAULT_DB_USERNAME;

    public static final String DEFAULT_DB_PASSWORD = "";
    protected String dbPassword = DEFAULT_DB_PASSWORD;

    public static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/reqbaz";
    protected String dbUrl = DEFAULT_DB_URL;

    private Vtor vtor;

    private DALFacade dalFacade;
    private Connection dbConnection;


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

    }

    private void createValidators() {
        vtor = new Vtor();
    }

    private void createConnection() throws Exception {
        dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/reqbaz", "root", "");
        dalFacade = new DALFacadeImpl(dbConnection, SQLDialect.MYSQL);
    }

    private void closeConnection() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
                System.out.println("Database connection closed!");
            } catch (SQLException ignore) {
                System.out.println("Could not close db connection!");
            }
        }
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
    public String getProjects(
            @QueryParam(name = "page", defaultValue = "0") int page,
            @QueryParam(name = "per_page", defaultValue = "10") int perPage) {
        createValidators();
        // if the user is not logged in, return all the public projects.
        UserAgent agent = (UserAgent) getActiveAgent();
        String resultJSON = "[]";
        Gson gson = new Gson();
        try {
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());

            createConnection();
            List<Project> projects = null;
            if (agent.getLoginName().equals("anonymous")) {
                projects = dalFacade.listPublicProjects(pageInfo);
            } else {
                // return public projects and the ones the user belongs to
                long userId = agent.getId();
                projects = dalFacade.listPublicAndAuthorizedProjects(pageInfo, (int) userId);
            }

            resultJSON = gson.toJson(projects);// return only public projects
        } catch (BazaarException bex) {
            return ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Consumes("application/json")
    @Produces("application/json")
    public String createProject(@ContentParam String project) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new project
        // TODO: check whether all required parameters are entered
        String resultJSON = "{success = true}";
        try {
            Gson gson = new Gson();
            Project projectToCreate = gson.fromJson(project, Project.class);
            vtor.validate(projectToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            dalFacade.createProject(projectToCreate);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Produces("application/json")
    public String getProject(@PathParam("projectId")  int projectId) {
        // TODO: check whether the current user may request this project
        String resultJSON = "{}";
        try {
            createConnection();
            Project projectToReturn = dalFacade.getProjectById(projectId);
            resultJSON = projectToReturn.toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
//    @Produces("application/json")
//    public String updateProject(@PathParam("projectId") int projectId) {
//
//    }

    //TODO DELETE PROJECT, DID WE WANTED IT
//    @DELETE
//    @Path("projects/{projectId}")
//    @Produces("application/json")
//    public String deleteProject(@PathParam("projectId") int projectId) {
//        // TODO: check if user can delete this project
//        String resultJSON = "{success = true}";
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
    public String getComponents(
            @PathParam("projectId")  int projectId,
            @QueryParam(name = "page", defaultValue = "0")  int page,
            @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
        // TODO: if the user is not logged in, return all the public projects.
        // Otherwise return all the user can see.
        String resultJSON = "[]";
        try {
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            List<Component> components = dalFacade.listComponentsByProjectId(projectId, pageInfo);
            resultJSON = gson.toJson(components);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Consumes("application/json")
    @Produces("application/json")
    public String createComponent(@PathParam("projectId")  int projectId, @ContentParam String component) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new project
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            Gson gson = new Gson();
            Component componentToCreate = gson.fromJson(component, Component.class);
            vtor.validate(componentToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            dalFacade.createComponent(componentToCreate);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Produces("application/json")
    public String getComponent(@PathParam("projectId")  int projectId, @PathParam("componentId")  int componentId) {
        // TODO: check whether the current user may request this project
        String resultJSON = "{}";
        try {
            createConnection();
            resultJSON = dalFacade.getComponentById(componentId).toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
//    @Produces("application/json")
//    public String updateComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
//        // TODO: check if user can change this project
//        return "{success=false}";
//    }

    @DELETE
    @Path("projects/{projectId}/components/{componentId}")
    @Produces("application/json")
    public String deleteComponent(@PathParam("projectId")  int projectId, @PathParam("componentId")  int componentId) {
        // TODO: check if user can delete this project
        String resultJSON = "{success = true}";
        try {
            createConnection();
            dalFacade.deleteComponentById(componentId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
        }

        return resultJSON;
    }

    /**********************************
     * REQUIREMENTS
     **********************************/

    /**
     * This method returns the list of requirements for a specific project.
     *
     * @param projectId   the ID of the project to retrieve requirements for.
     * @param componentId the id of the component under a given project
     * @return a list of requirements
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements")
    @Produces("application/json")
    public String getRequirementsByProject(@PathParam("projectId")  int projectId, @PathParam("componentId")  int componentId,
                                  @QueryParam(name = "page", defaultValue = "0")  int page,
                                  @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
        String resultJSON = "[]";
        try {
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            List<Requirement> requirements = dalFacade.listRequirementsByProject(projectId, pageInfo);
            resultJSON = gson.toJson(requirements);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Produces("application/json")
    public String getRequirementsByComponent(@PathParam("projectId")  int projectId, @PathParam("componentId")  int componentId,
                                           @QueryParam(name = "page", defaultValue = "0")  int page,
                                           @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
        String resultJSON = "[]";
        try {
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            List<Requirement> requirements = dalFacade.listRequirementsByComponent(componentId, pageInfo);
            resultJSON = gson.toJson(requirements);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Consumes("application/json")
    @Produces("application/json")
    public String createRequirement(@PathParam("projectId")  int projectId, @PathParam("componentId") int componentId,
                                    @ContentParam String requirement) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            Gson gson = new Gson();
            Requirement requirementToCreate = gson.fromJson(requirement, Requirement.class);
            vtor.validate(requirementToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            dalFacade.createRequirement(requirementToCreate);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    public String getRequirement(@PathParam("projectId")  int projectId, @PathParam("componentId")  int componentId,
                                 @PathParam("requirementId")  int requirementId) {
        String resultJSON = "{}";
        try {
            createConnection();
            resultJSON = dalFacade.getRequirementById(requirementId).toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    public String deleteRequirement(@PathParam("projectId")  int projectId, @PathParam("componentId")  int componentId,
                                    @PathParam("requirementId")  int requirementId) {
        // TODO: check if the user may delete this requirement.
        String resultJSON = "{success = true}";
        try {
            createConnection();
            dalFacade.deleteRequirementById(requirementId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
//    @Consumes("application/json")
//    @Produces("application/json")
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
    public String addUserToDevelopers(@PathParam("projectId")  int projectId,
                                      @PathParam("componentId")  int componentId,
                                      @PathParam("requirementId")  int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);
            if (internalUserId == null) {
                resultJSON = "{success = false}";
            } else {
                dalFacade.wantToDevelop(internalUserId,requirementId);
            }
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Consumes("application/json")
    @Produces("application/json")
    public String removeUserFromDevelopers(@PathParam("projectId")  int projectId,
                                           @PathParam("componentId")  int componentId,
                                           @PathParam("requirementId")  int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);
            if (internalUserId == null) {
                resultJSON = "{success = false}";
            } else {
                dalFacade.notWantToDevelop(internalUserId,requirementId);
            }
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    public String addUserToFollowers(@PathParam("projectId")  int projectId,
                                     @PathParam("componentId")  int componentId,
                                     @PathParam("requirementId")  int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);
            if (internalUserId == null) {
                resultJSON = "{success = false}";
            } else {
                dalFacade.follow(internalUserId,requirementId);
            }
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Consumes("application/json")
    @Produces("application/json")
    public String removeUserFromFollowers(@PathParam("projectId")  int projectId,
                                          @PathParam("componentId")  int componentId,
                                          @PathParam("requirementId")  int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);
            if (internalUserId == null) {
                resultJSON = "{success = false}";
            } else {
                dalFacade.unFollow(internalUserId,requirementId);
            }
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Produces("application/json")
    @Consumes("application/json")
    public String addVote(@PathParam("projectId")  int projectId,
                          @PathParam("componentId")  int componentId,
                          @PathParam("requirementId")  int requirementId,
                          @QueryParam(name = "direction", defaultValue = "up") String direction) {

        long userId = ((UserAgent) getActiveAgent()).getId();

        String resultJSON = "{success = true}";
        try {
            if (!(direction.equals("up") || direction.equals("down"))){
                vtor.addViolation(new Violation("Direction can only be \"up\" or \"down\"",direction,direction));
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }

            createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);
            if (internalUserId == null) {
                resultJSON = "{success = false}";
            } else {
                dalFacade.vote(internalUserId, requirementId, direction.equals("up"));
            }
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Consumes("application/json")
    @Produces("application/json")
    public String removeVote(@PathParam("projectId")  int projectId,
                             @PathParam("componentId")  int componentId,
                             @PathParam("requirementId")  int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId((int) userId);
            if (internalUserId == null) {
                resultJSON = "{success = false}";
            } else {
                dalFacade.unVote(internalUserId,requirementId);
            }
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    @Produces("application/json")
    public String getComments(@PathParam("projectId")  int projectId,
                              @PathParam("componentId")  int componentId,
                              @PathParam("requirementId")  int requirementId,
                              @QueryParam(name = "page", defaultValue = "0")  int page,
                              @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
        long userId = ((UserAgent) getActiveAgent()).getId();

        String resultJSON = "[]";
        try {
            PageInfo pageInfo = new PageInfo(page, perPage);
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            List<Comment> comments = dalFacade.listCommentsByRequirementId(requirementId, pageInfo);
            Gson gson = new Gson();
            resultJSON = gson.toJson(comments);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
        }

        return resultJSON;
    }

    /**
     * This method allows to create a new comment fro a requirement.
     *
     * @param projectId     the ID of the project for the requirement.
     * @param componentId   the id of the component under a given project
     * @param requirementId the ID of the requirement, which was commented.
     * @return true if the creation was successful, otherwise false
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments")
    @Consumes("application/json")
    @Produces("application/json")
    public String createComment(@PathParam("projectId")  int projectId,
                                @PathParam("componentId")  int componentId,
                                @PathParam("requirementId")  int requirementId,
                                @ContentParam String comment) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            Gson gson = new Gson();
            Comment commentToCreate = gson.fromJson(comment,Comment.class);
            vtor.validate(commentToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            dalFacade.createComment(commentToCreate);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    public String deleteRequirement(@PathParam("projectId")  int projectId,
                                    @PathParam("componentId")  int componentId,
                                    @PathParam("requirementId")  int requirementId,
                                    @PathParam("commentId")  int commentId) {
        // TODO: check if the user may delete this requirement.
        long userId = ((UserAgent) getActiveAgent()).getId();
        String resultJSON = "{success = true}";
        try {
            createConnection();
            dalFacade.deleteCommentById(commentId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
//    @Produces("application/json")
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
    @Consumes("application/json")
    @Produces("application/json")
    public String createAttachment(@PathParam("projectId")  int projectId,
                                   @PathParam("componentId")  int componentId,
                                   @PathParam("requirementId")  int requirementId,
                                   @ContentParam String attachment) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        String resultJSON = "{success = true}";
        try {
            Gson gson = new Gson();
            //TODO??? HOW DOES IT KNOW THE TYPE
            Attachment attachmentToCreate = gson.fromJson(attachment,Attachment.class);
            vtor.validate(attachmentToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            createConnection();
            dalFacade.createAttachment(attachmentToCreate);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
    public String deleteAttachment(@PathParam("projectId")  int projectId,
                                   @PathParam("componentId")  int componentId,
                                   @PathParam("requirementId")  int requirementId,
                                   @PathParam("attachmentId")  int attachmentId) {
        // TODO: check if the user may delete this requirement.
        String resultJSON = "{success = true}";
        try {
            createConnection();
            dalFacade.deleteAttachmentById(attachmentId);
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
//    @Produces("application/json")
//    public String getUsers() {
//        // TODO: check if the admin user wants to retrieve all users.
//
//    }

    /**
     * Allows to update a certain project.
     *
     * @param userId the id of the user to be returned.
     * @return a JSON string of the user data specified by the id.
     */
    @GET
    @Path("users/{userId}")
    @Produces("application/json")
    public String getUser(@PathParam("userId")  int userId) {
        // TODO: check whether the current user may request this project
        String resultJSON = "{}";
        try {
            createConnection();
            resultJSON = dalFacade.getUserById(userId).toJSON();
        } catch (BazaarException bex) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bex);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);

        } finally {
            closeConnection();
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
//    @Produces("application/json")
//    public String updateUser(@PathParam("userId") int userId) {
//        // TODO: check if user can change this project
//        return "{success=false}";
//    }
}
