package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
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
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.security.Context;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

@Path("/bazaar/projects")
@Api(value = "/projects", description = "Projects resource")
public class ProjectsResource extends Service {

    private BazaarService bazaarService;

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

    public ProjectsResource() throws Exception {
        bazaarService = new BazaarService();
    }

    /**
     * This method returns the list of projects on the server.
     *
     * @param page    page number
     * @param perPage number of projects by page
     * @return Response with list of all projects
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of projects on the server.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of projects"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getProjects(
            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
            @ApiParam(value = "Elements of project by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            UserAgent agent = (UserAgent) getActiveAgent();
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.createConnection();
            List<Project> projects;
            if (agent.getLoginName().equals("anonymous")) {
                // return only public projects
                projects = dalFacade.listPublicProjects(pageInfo);
            } else {
                // return public projects and the ones the user belongs to
                long userId = agent.getId();
                projects = dalFacade.listPublicAndAuthorizedProjects(pageInfo, userId);
            }
            return new HttpResponse(gson.toJson(projects), HttpURLConnection.HTTP_OK);
        } catch (BazaarException bex) {
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            bazaarService.closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve a certain project.
     *
     * @param projectId id of the project to retrieve
     * @return Response with a project as a JSON object.
     */
    @GET
    @Path("/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain project"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getProject(@PathParam("projectId") int projectId) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            dalFacade = bazaarService.createConnection();
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
            return new HttpResponse(gson.toJson(projectToReturn), HttpURLConnection.HTTP_OK);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            bazaarService.closeConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new project.
     *
     * @param project project as a JSON object
     * @return Response with the created project as a JSON object.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new project")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created project"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse createProject(@ApiParam(value = "Project entity as JSON", required = true) @ContentParam String project) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            Gson gson = new Gson();
            Project projectToCreate = gson.fromJson(project, Project.class);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(projectToCreate);
            if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            dalFacade = bazaarService.createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PROJECT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.project.create"));
            }
            projectToCreate.setLeaderId(internalUserId);
            Project createdProject = dalFacade.createProject(projectToCreate);
            bazaarService.sendActivityOverRMI(this, createdProject.getCreation_time(), Activity.ActivityAction.CREATE, createdProject.getId(),
                    Activity.DataType.PROJECT, internalUserId);
            return new HttpResponse(gson.toJson(createdProject), HttpURLConnection.HTTP_CREATED);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_UNAUTHORIZED);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            bazaarService.closeConnection(dalFacade);
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
    @Path("/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update a certain project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated project"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse updateProject(@PathParam("projectId") int projectId,
                                      @ApiParam(value = "Project entity as JSON", required = true) @ContentParam String project) {
        DALFacade dalFacade = null;
        try {
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            long userId = ((UserAgent) getActiveAgent()).getId();
            Gson gson = new Gson();
            Project projectToUpdate = gson.fromJson(project, Project.class);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(projectToUpdate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_PROJECT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.project.modify"));
            }
            if (projectToUpdate.getId() != 0 && projectId != projectToUpdate.getId()) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
            }
            Project updatedProject = dalFacade.modifyProject(projectToUpdate);
            bazaarService.sendActivityOverRMI(this, updatedProject.getLastupdated_time(), Activity.ActivityAction.UPDATE, updatedProject.getId(),
                    Activity.DataType.PROJECT, internalUserId);
            return new HttpResponse(gson.toJson(updatedProject), HttpURLConnection.HTTP_OK);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            bazaarService.closeConnection(dalFacade);
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

    /**
     * This method returns the list of components under a given project.
     *
     * @param projectId id of the project
     * @param page      page number
     * @param perPage   number of projects by page
     * @return Response with components as a JSON array.
     */
    @GET
    @Path("/{projectId}/components")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of components under a given project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of components for a given project"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getComponentsByProject(
            @PathParam("projectId") int projectId,
            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
            @ApiParam(value = "Elements of components by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.createConnection();
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
            return new HttpResponse(gson.toJson(components), HttpURLConnection.HTTP_OK);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            bazaarService.closeConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of requirements for a specific project.
     *
     * @param projectId id of the project to retrieve requirements for
     * @param page      page number
     * @param perPage   number of projects by page
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/{projectId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of requirements for a given project"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getRequirementsByProject(@PathParam("projectId") int projectId,
                                                 @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                                 @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            PageInfo pageInfo = new PageInfo(page, perPage);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.createConnection();
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
            return new HttpResponse(gson.toJson(requirements), HttpURLConnection.HTTP_OK);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_NOT_FOUND);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            bazaarService.closeConnection(dalFacade);
        }
    }

}
