package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.*;


@ServicePath("/bazaar/projects")
public class ProjectsResource extends RESTService {

    private BazaarService bazaarService;

    @Override
    protected void initResources() {
        getResourceConfig().register(Resource.class);
    }

    public ProjectsResource() throws Exception {
        bazaarService = new BazaarService();
    }

    @Path("/")
    public static class Resource {

        private final ProjectsResource service = (ProjectsResource) Context.getCurrent().getService();

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
        public Response getProjects(
                @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                @ApiParam(value = "Elements of project by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                @ApiParam(value = "Sort", required = false, allowableValues = "name,date,requirement,follower") @DefaultValue("name") @QueryParam("sort") List<String> sort) {

            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                Gson gson = new Gson();
                List<Pageable.SortField> sortList = new ArrayList<>();
                for (String sortOption : sort) {
                    Pageable.SortDirection direction = Pageable.SortDirection.DEFAULT;
                    if (sortOption.startsWith("+") || sortOption.startsWith(" ")) { // " " is needed because jersey does not pass "+"
                        direction = Pageable.SortDirection.ASC;
                        sortOption = sortOption.substring(1);

                    } else if (sortOption.startsWith("-")) {
                        direction = Pageable.SortDirection.DESC;
                        sortOption = sortOption.substring(1);
                    }
                    Pageable.SortField sortField = new Pageable.SortField(sortOption, direction);
                    sortList.add(sortField);
                }
                PageInfo pageInfo = new PageInfo(page, perPage, new HashMap<>(), sortList);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(pageInfo);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                PaginationResult<Project> projectsResult;
                if (agent.getLoginName().equals("anonymous")) {
                    // return only public projects
                    projectsResult = dalFacade.listPublicProjects(pageInfo);
                } else {
                    // return public projects and the ones the user belongs to
                    projectsResult = dalFacade.listPublicAndAuthorizedProjects(pageInfo, userId);
                }

                Map<String, List<String>> parameter = new HashMap<>();
                parameter.put("page", new ArrayList() {{
                    add(String.valueOf(page));
                }});
                parameter.put("per_page", new ArrayList() {{
                  add(String.valueOf(perPage));
                }});
                parameter.put("sort", sort);

                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder = responseBuilder.entity(gson.toJson(projectsResult.getElements()));
                responseBuilder = service.bazaarService.paginationLinks(responseBuilder, projectsResult, "projects", parameter);
                responseBuilder = service.bazaarService.xHeaderFields(responseBuilder, projectsResult);
                Response response = responseBuilder.build();

                return response;
            } catch (BazaarException bex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
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
        public Response getProject(@PathParam("projectId") int projectId) {
            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                dalFacade = service.bazaarService.getDBConnection();
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
                return Response.ok(gson.toJson(projectToReturn)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
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
        public Response createProject(@ApiParam(value = "Project entity as JSON", required = true) String project) {
            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                Gson gson = new Gson();
                Project projectToCreate = gson.fromJson(project, Project.class);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(projectToCreate);
                if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PROJECT, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.project.create"));
                }
                projectToCreate.setLeaderId(internalUserId);
                Project createdProject = dalFacade.createProject(projectToCreate);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, createdProject.getCreation_time(), Activity.ActivityAction.CREATE, createdProject.getId(),
                        Activity.DataType.PROJECT, 0, null, internalUserId);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(createdProject)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
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
        public Response updateProject(@PathParam("projectId") int projectId,
                                      @ApiParam(value = "Project entity as JSON", required = true) String project) {
            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                Gson gson = new Gson();
                Project projectToUpdate = gson.fromJson(project, Project.class);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(projectToUpdate);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_PROJECT, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.project.modify"));
                }
                if (projectToUpdate.getId() != 0 && projectId != projectToUpdate.getId()) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
                }
                Project updatedProject = dalFacade.modifyProject(projectToUpdate);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, updatedProject.getLastupdated_time(), Activity.ActivityAction.UPDATE, updatedProject.getId(),
                        Activity.DataType.PROJECT, 0, null, internalUserId);
                return Response.ok(gson.toJson(updatedProject)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
            }
        }

        /**
         * This method add the current user to the followers list of a given project.
         *
         * @param projectId id of the project
         * @return Response with project as a JSON object.
         */
        @POST
        @Path("/{projectId}/followers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method add the current user to the followers list of a given project.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the project"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response addUserToFollowers(@PathParam("projectId") int projectId) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.create"));
                }
                dalFacade.followProject(internalUserId, projectId);
                Project project = dalFacade.getProjectById(projectId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.FOLLOW, project.getId(),
                        Activity.DataType.PROJECT, 0, null, internalUserId);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(project)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
            }
        }

        /**
         * This method removes the current user from a followers list of a given project.
         *
         * @param projectId id of the project
         * @return Response with project as a JSON object.
         */
        @DELETE
        @Path("/{projectId}/followers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method removes the current user from a followers list of a given project.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the project"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response removeUserFromFollowers(@PathParam("projectId") int projectId) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.delete"));
                }
                dalFacade.unFollowProject(internalUserId, projectId);
                Project project = dalFacade.getProjectById(projectId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.UNFOLLOW, project.getId(),
                        Activity.DataType.PROJECT, 0, null, internalUserId);
                return Response.ok(gson.toJson(project)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
            }
        }

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
        public Response getComponentsByProject(
                @PathParam("projectId") int projectId,
                @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                @ApiParam(value = "Elements of components by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                @ApiParam(value = "Sort", required = false, allowableValues = "name,date,requirement,follower") @DefaultValue("name") @QueryParam("sort") List<String> sort) {

            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                Gson gson = new Gson();
                List<Pageable.SortField> sortList = new ArrayList<>();
                for (String sortOption : sort) {
                    Pageable.SortDirection direction = Pageable.SortDirection.DEFAULT;
                    if (sortOption.startsWith("+") || sortOption.startsWith(" ")) { // " " is needed because jersey does not pass "+"
                        direction = Pageable.SortDirection.ASC;
                        sortOption = sortOption.substring(1);

                    } else if (sortOption.startsWith("-")) {
                        direction = Pageable.SortDirection.DESC;
                        sortOption = sortOption.substring(1);
                    }
                    Pageable.SortField sortField = new Pageable.SortField(sortOption, direction);
                    sortList.add(sortField);
                }
                PageInfo pageInfo = new PageInfo(page, perPage, new HashMap<>(), sortList);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(pageInfo);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
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
                PaginationResult<Component> componentsResult = dalFacade.listComponentsByProjectId(projectId, pageInfo);

                Map<String, List<String>> parameter = new HashMap<>();
                parameter.put("page", new ArrayList() {{
                    add(String.valueOf(page));
                }});
                parameter.put("per_page", new ArrayList() {{
                    add(String.valueOf(perPage));
                }});
                parameter.put("sort", sort);

                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder = responseBuilder.entity(gson.toJson(componentsResult.getElements()));
                responseBuilder = service.bazaarService.paginationLinks(responseBuilder, componentsResult, "projects/" + String.valueOf(projectId) + "/components", parameter);
                responseBuilder = service.bazaarService.xHeaderFields(responseBuilder, componentsResult);
                Response response = responseBuilder.build();

                return response;
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
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
        public Response getRequirementsByProject(@PathParam("projectId") int projectId,
                                                 @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                                 @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                                                 @ApiParam(value = "State filter", required = false, allowableValues = "all,open,realized") @DefaultValue("all") @QueryParam("state") String stateFilter,
                                                 @ApiParam(value = "Sort", required = false, allowableValues = "date,title,vote,comment,follower,realized") @DefaultValue("date") @QueryParam("sort") List<String> sort) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                Gson gson = new Gson();
                HashMap<String, String> filters = new HashMap<>();
                if (stateFilter != "all") {
                    filters.put("realized", stateFilter);
                }
                List<Pageable.SortField> sortList = new ArrayList<>();
                for (String sortOption : sort) {
                    Pageable.SortDirection direction = Pageable.SortDirection.DEFAULT;
                    if (sortOption.startsWith("+") || sortOption.startsWith(" ")) { // " " is needed because jersey does not pass "+"
                        direction = Pageable.SortDirection.ASC;
                        sortOption = sortOption.substring(1);

                    } else if (sortOption.startsWith("-")) {
                        direction = Pageable.SortDirection.DESC;
                        sortOption = sortOption.substring(1);
                    }
                    Pageable.SortField sortField = new Pageable.SortField(sortOption, direction);
                    sortList.add(sortField);
                }
                PageInfo pageInfo = new PageInfo(page, perPage, filters, sortList);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(pageInfo);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
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
                PaginationResult<RequirementEx> requirementsResult = dalFacade.listRequirementsByProject(projectId, pageInfo, internalUserId);

                Map<String, List<String>> parameter = new HashMap<>();
                parameter.put("page", new ArrayList() {{
                    add(String.valueOf(page));
                }});
                parameter.put("per_page", new ArrayList() {{
                    add(String.valueOf(perPage));
                }});
                parameter.put("state", new ArrayList() {{
                    add(String.valueOf(stateFilter));
                }});
                parameter.put("sort", sort);

                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder = responseBuilder.entity(gson.toJson(requirementsResult.getElements()));
                responseBuilder = service.bazaarService.paginationLinks(responseBuilder, requirementsResult, "projects/" + String.valueOf(projectId) + "/requirements", parameter);
                responseBuilder = service.bazaarService.xHeaderFields(responseBuilder, requirementsResult);
                Response response = responseBuilder.build();

                return response;
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
            }
        }

    }

}
