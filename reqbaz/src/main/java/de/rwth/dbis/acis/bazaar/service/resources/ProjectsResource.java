package de.rwth.dbis.acis.bazaar.service.resources;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Tag;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Api(value = "projects", description = "Projects resource")
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.9.0",
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
        ),
        schemes = SwaggerDefinition.Scheme.HTTPS
)
@Path("/projects")
public class ProjectsResource {

    private final L2pLogger logger = L2pLogger.getInstance(ProjectsResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    public ProjectsResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }

    /**
     * This method returns the list of projects on the server.
     *
     * @param page    page number
     * @param perPage number of projects by page
     * @param search  search string
     * @param sort    sort order
     * @return Response with list of all projects
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of projects on the server.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of projects", response = Project.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getProjects(
            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
            @ApiParam(value = "Elements of project by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
            @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
            @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "name,date,last_activity,requirement,follower") @DefaultValue("name") @QueryParam("sort") List<String> sort,
            @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection,
            @ApiParam(value = "Filter", required = false, allowMultiple = true, allowableValues = "all, created, following") @QueryParam("filters") List<String> filters,
            @ApiParam(value = "Ids", required = false, allowMultiple = true) @QueryParam("ids") List<Integer> ids,
            @ApiParam(value = "Also search in projects requirements", required = false) @DefaultValue("false") @QueryParam("recursive") boolean recursive) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = resourceHelper.getUserId();
            List<Pageable.SortField> sortList = resourceHelper.getSortFieldList(sort, sortDirection);

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            HashMap<String, String> filterMap = new HashMap<>();
            for (String filterOption : filters) {
                filterMap.put(filterOption, internalUserId.toString());
            }

            HashMap<String, Boolean> options = new HashMap<>();
            options.put("recursive", recursive);

            PageInfo pageInfo = new PageInfo(page, perPage, filterMap, sortList, search, ids, null, options);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            PaginationResult<Project> projectsResult;
            if (agent instanceof AnonymousAgent) {
                // return only public projects
                projectsResult = dalFacade.listPublicProjects(pageInfo, 0);
            } else {
                // return public projects and the ones the user belongs to
                projectsResult = dalFacade.listPublicAndAuthorizedProjects(pageInfo, internalUserId);
            }
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3,
                    0, Activity.DataType.PROJECT, internalUserId);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, search, sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(projectsResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, projectsResult, "projects", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, projectsResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get projects failed");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get projects failed", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain project", response = Project.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getProject(@PathParam("projectId") int projectId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Project projectToReturn = getProjectToReturn(projectId, dalFacade, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_4,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            return Response.ok(projectToReturn.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get project " + projectId + " failed", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get project " + projectId + " failed", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    @NotNull
    private Project getProjectToReturn(int projectId, DALFacade dalFacade, Integer internalUserId) throws Exception {
        Project projectToReturn = dalFacade.getProjectById(projectId, internalUserId);

        if (projectToReturn == null) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "project"));
        }

        if (projectToReturn.getVisibility()) {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_PUBLIC_PROJECT, projectId, dalFacade), ResourceHelper.ERROR_ANONYMUS, true);
        } else {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_PROJECT, projectId, dalFacade), "error.authorization.project.read", true);
        }
        return projectToReturn;
    }

    /**
     * This method allows to create a new project.
     *
     * @param projectToCreate project
     * @return Response with the created project as a JSON object.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created project", response = Project.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response createProject(@ApiParam(value = "Project entity", required = true) Project projectToCreate) {
        DALFacade dalFacade = null;
        try {
            resourceHelper.checkRegistrarErrors();
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            resourceHelper.handleGenericError(bazaarService.validateCreate(projectToCreate));
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PROJECT, dalFacade), "error.authorization.project.create", true);
            projectToCreate.setLeader(dalFacade.getUserById(internalUserId));
            Project createdProject = dalFacade.createProject(projectToCreate, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.CREATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_5,
                    createdProject.getId(), Activity.DataType.PROJECT, internalUserId);
            // trigger Gamification Framework
            bazaarService.getGamificationManager().triggerCreateProjectAction(internalUserId);
            // add notifications to response
            createdProject.setGamificationNotifications(bazaarService.getGamificationManager().getUserNotifications(internalUserId));
            return Response.status(Response.Status.CREATED).entity(createdProject.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Create project", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Create project", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Allows to update a certain project.
     *
     * @param projectToUpdate updated project
     * @return Response with the updated project as a JSON object.
     */
    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update a certain project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated project", response = Project.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateProject(@ApiParam(value = "Project entity", required = true) Project projectToUpdate) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            resourceHelper.handleGenericError(bazaarService.validate(projectToUpdate));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_PROJECT, projectToUpdate.getId(), dalFacade), "error.authorization.project.modify", true);

            Project updatedProject = dalFacade.modifyProject(projectToUpdate);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_6,
                    updatedProject.getId(), Activity.DataType.PROJECT, internalUserId);
            return Response.ok(updatedProject.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Update project", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Update project", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method deletes a specific project.
     *
     * @param projectId id of the project to delete
     * @return Empty Response.
     */
    @DELETE
    @Path("/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Successfully deleted"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response deleteProject(@PathParam("projectId") int projectId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            Project projectToDelete = dalFacade.getProjectById(projectId, internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Delete_PROJECT, projectId, dalFacade), "error.authorization.project.delete", !projectToDelete.isOwner(internalUserId));
            dalFacade.deleteProjectById(projectId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.DELETE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            return Response.noContent().build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Delete project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Delete project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the project", response = Project.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response followProject(@PathParam("projectId") int projectId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade), "error.authorization.follow.create", true);
            dalFacade.followProject(internalUserId, projectId);
            Project project = dalFacade.getProjectById(projectId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.FOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_8,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            return Response.status(Response.Status.CREATED).entity(project.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Follow project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Follow project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the project", response = Project.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unfollowProject(@PathParam("projectId") int projectId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade), "error.authorization.follow.delete", true);
            dalFacade.unFollowProject(internalUserId, projectId);
            Project project = dalFacade.getProjectById(projectId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UNFOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_9,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            return Response.ok(project.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unfollow project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unfollow project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve statistics for one project.
     *
     * @param projectId
     * @param since     timestamp since filter, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z
     * @return Response with statistics as a JSON object.
     */
    @GET
    @Path("/{projectId}/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve statistics for one project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns statistics", response = Statistic.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getStatisticsForProject(
            @PathParam("projectId") int projectId,
            @ApiParam(value = "Since timestamp, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z", required = false) @QueryParam("since") String since) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Calendar sinceCal = resourceHelper.getSinceCal(since);
            Statistic projectStatistics = dalFacade.getStatisticsForProject(internalUserId, projectId, sinceCal);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_10,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            return Response.ok(projectStatistics.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get statistics for project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get statistics for project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of contributors for a specific project.
     *
     * @param projectId id of the project
     * @return Response with project contributors
     */
    @GET
    @Path("/{projectId}/contributors")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of contributors for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of contributors for a given project", response = ProjectContributors.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getContributorsForProject(@PathParam("projectId") int projectId) throws Exception {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            ProjectContributors projectContributors = dalFacade.listContributorsForProject(projectId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_11,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            return Response.ok(projectContributors.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get contributors for project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get contributors for project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of followers for a specific project.
     *
     * @param projectId id of the project
     * @param page      page number
     * @param perPage   number of projects by page
     * @return Response with followers as a JSON array.
     */
    @GET
    @Path("/{projectId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of followers for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of followers for a given project", response = User.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getFollowersForProject(@PathParam("projectId") int projectId,
                                           @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                           @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            PageInfo pageInfo = new PageInfo(page, perPage);

            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            PaginationResult<User> projectFollowers = dalFacade.listFollowersForProject(projectId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_12,
                    projectId, Activity.DataType.PROJECT, internalUserId);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, null, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(projectFollowers.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, projectFollowers, "projects/" + String.valueOf(projectId) + "/followers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, projectFollowers);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get followers for project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get followers for project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of categories under a given project.
     *
     * @param projectId id of the project
     * @param page      page number
     * @param perPage   number of projects by page
     * @param search    search string
     * @param sort      sort order
     * @return Response with categories as a JSON array.
     */
    @GET
    @Path("/{projectId}/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of categories under a given project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of categories for a given project", response = Category.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getCategoriesForProject(
            @PathParam("projectId") int projectId,
            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
            @ApiParam(value = "Elements of categories by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
            @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
            @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "name,date,last_activity,requirement,follower") @DefaultValue("name") @QueryParam("sort") List<String> sort,
            @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection
    ) throws Exception {
        CategoryResource categoryResource = new CategoryResource();
        return categoryResource.getCategoriesForProject(projectId, page, perPage, search, sort, sortDirection);
    }

    /**
     * This method returns the list of requirements for a specific project.
     *
     * @param projectId   id of the project to retrieve requirements for
     * @param page        page number
     * @param perPage     number of requirements by page
     * @param search      search string
     * @param stateFilter requirement state
     * @param sort        sort order
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/{projectId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of requirements for a given project", response = Requirement.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getRequirementsForProject(@PathParam("projectId") int projectId,
                                              @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                              @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                                              @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
                                              @ApiParam(value = "State filter", required = false, allowableValues = "all,open,realized") @DefaultValue("all") @QueryParam("state") String stateFilter,
                                              @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "date,last_activity,name,vote,comment,follower") @DefaultValue("date") @QueryParam("sort") List<String> sort,
                                              @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection
    ) throws Exception {
        RequirementsResource requirementsResource = new RequirementsResource();
        return requirementsResource.getRequirementsForProject(projectId, page, perPage, search, stateFilter, sort, sortDirection);
    }

    /**
     * This method returns the list of feedbacks for a specific project.
     *
     * @param projectId   id of the project to retrieve requirements for
     * @param page        page number
     * @param perPage     number of requirements by page
     * @param search      search string
     * @param stateFilter requirement state
     * @param sort        sort order
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/{projectId}/feedbacks")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of given feedbacks for a specific project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of feedbacks for a given project", response = Feedback.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getFeedbacksForProject(@PathParam("projectId") int projectId,
                                           @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                           @ApiParam(value = "Elements of feedbacks by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                                           @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
                                           @ApiParam(value = "State filter", required = false, allowableValues = "all,open") @DefaultValue("all") @QueryParam("state") String stateFilter,
                                           @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "date") @DefaultValue("date") @QueryParam("sort") List<String> sort,
                                           @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection
    ) throws Exception {
        FeedbackResource feedbackResource = new FeedbackResource();
        return feedbackResource.getFeedbackForProject(projectId, page, perPage);
    }

    @GET
    @Path("/{projectId}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of tags under a given project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of tags for a given project", response = Tag.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getTagsForProject(
            @PathParam("projectId") int projectId
    ) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            getProjectToReturn(projectId, dalFacade, internalUserId);
            List<Tag> tags = dalFacade.getTagsByProjectId(projectId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_12,
                    projectId, Activity.DataType.PROJECT, internalUserId);

            Response.ResponseBuilder responseBuilder = Response.ok()
                    .entity(bazaarService.getMapper().writeValueAsString(tags));

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get tags for project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get tags for project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method add the current user to the followers list of a given project.
     *
     * @param projectId id of the project
     * @param tag       Tag to be created
     * @return Response with project as a JSON object.
     */
    @POST
    @Path("/{projectId}/tags")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method adds a new tag to a given project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Path to parent project", response = Tag.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response createTag(@PathParam("projectId") int projectId,
                              @ApiParam(value = "New Tag Representation", required = true) Tag tag) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_CATEGORY, dalFacade), "error.authorization.category.create", true);

            // Ensure no cross-injection happens
            tag.setProjectId(projectId);
            Tag createdTag = dalFacade.createTag(tag);

            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.CREATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_8,
                    projectId, Activity.DataType.TAG, internalUserId);
            return Response.status(Response.Status.CREATED).entity(createdTag.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Follow project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Follow project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Add a member to the project
     *
     * @param projectId     id of the project to update
     * @param projectMember The new project member
     * @return
     */
    @POST
    @Path("/{projectId}/members")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to add a project member.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Member added"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = "User is already member"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response addMember(@PathParam("projectId") int projectId,
                              @ApiParam(value = "New project member", required = true) ProjectMember projectMember) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            resourceHelper.handleGenericError(bazaarService.validate(projectMember));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            PrivilegeEnum privilege = PrivilegeEnum.Modify_MEMBERS;
            // Only Admins should be able to create new admins.
            // Differentiate here
            privilege = getPrivilege(projectMember, privilege);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, privilege, projectId, dalFacade), "error.authorization.project.modify", true);

            // ensure the given user exists
            dalFacade.getUserById(projectMember.getUserId());

            // we want to *add* a member so throw error if user is already member
            if (dalFacade.isUserProjectMember(projectId, projectMember.getUserId())) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.validation.project_member.already_exists"));
            }

            dalFacade.addUserToRole(projectMember.getUserId(), projectMember.getRole().name(), projectId);

            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_6, projectId, Activity.DataType.PROJECT, internalUserId);

            // TODO Return 'location' header to conform to HTTP specification
            return Response.status(Response.Status.CREATED).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Update project", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Update project", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private static PrivilegeEnum getPrivilege(ProjectMember projectMember, PrivilegeEnum privilege) {
        if (projectMember.getRole() == ProjectRole.ProjectAdmin) {
            privilege = PrivilegeEnum.Modify_ADMIN_MEMBERS;
        }
        return privilege;
    }

    /**
     * Allows to update a certain project.
     *
     * @param projectId      id of the project to update
     * @param projectMembers One or more modified project members
     * @return Response with the updated project as a JSON object.
     */
    @PUT
    @Path("/{projectId}/members")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to modify the project members.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Member modified"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateMember(@PathParam("projectId") int projectId,
                                 @ApiParam(value = "New or updated project member", required = true) List<ProjectMember> projectMembers) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            resourceHelper.handleGenericError(bazaarService.validate(projectMembers));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            AtomicReference<PrivilegeEnum> privilege = setPrivileges(projectMembers);

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, privilege.get(), projectId, dalFacade), "error.authorization.project.modify", true);

            for (ProjectMember projectMember : projectMembers) {
                // ensure the given user exists
                dalFacade.getUserById(projectMember.getUserId());

                dalFacade.updateProjectMemberRole(projectId, projectMember.getUserId(), projectMember.getRole().name());

                bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_6, projectId, Activity.DataType.PROJECT, internalUserId);
            }
            return Response.noContent().build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Update member", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Update member", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    @NotNull
    private static AtomicReference<PrivilegeEnum> setPrivileges(List<ProjectMember> projectMembers) {
        AtomicReference<PrivilegeEnum> privilege = new AtomicReference<>(PrivilegeEnum.Modify_MEMBERS);
        projectMembers.forEach(projectMember -> {
            // Only Admins should be able to create new admins.
            // Differentiate here
            if (projectMember.getRole() == ProjectRole.ProjectAdmin) {
                privilege.set(PrivilegeEnum.Modify_ADMIN_MEMBERS);
            }
        });
        return privilege;
    }

    /**
     * Allows to update a certain project.
     *
     * @param projectId id of the project
     * @return Response with a list of project members as a JSON object.
     */
    @GET
    @Path("/{projectId}/members")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve the project members.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Members", responseContainer = "List", response = ProjectMember.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getMembers(@PathParam("projectId") int projectId,
                               @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                               @ApiParam(value = "Elements of memebers by page", required = false) @DefaultValue("20") @QueryParam("per_page") int perPage) throws Exception {

        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();

            PageInfo pageInfo = new PageInfo(page, perPage);

            // Take Object for generic error handling
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            PrivilegeEnum privilege = PrivilegeEnum.Read_PUBLIC_PROJECT;

            if (!dalFacade.isProjectPublic(projectId)) {
                privilege = PrivilegeEnum.Read_PROJECT;
            }

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, privilege, projectId, dalFacade), "error.authorization.project.modify", true);

            PaginationResult<ProjectMember> members = dalFacade.getProjectMembers(projectId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_12,
                    projectId, Activity.DataType.PROJECT, internalUserId);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(members.toJSON());
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, members);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get member", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get member", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    @DELETE
    @Path("/{projectId}/members/{memberUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to remove a project member.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Member removed"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response removeMember(
            @ApiParam(value = "Project to remove the user from") @PathParam("projectId") int projectId,
            @ApiParam(value = "User ID of the member to remove") @PathParam("memberUserId") int memberUserId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            // Get roles of the member to modify to prevent admins being removed by managers
            List<Role> modifiedMemberRoles = dalFacade.getRolesByUserId(memberUserId, projectId);

            // Only Admins should be able to remove admins.
            // Differentiate here by checking if a user is a project admin
            PrivilegeEnum privilege = PrivilegeEnum.Modify_MEMBERS;
            if (modifiedMemberRoles.stream().anyMatch(role -> role.getName().equals(ProjectRole.ProjectAdmin.name()))) {
                privilege = PrivilegeEnum.Modify_ADMIN_MEMBERS;
            }
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, privilege, projectId, dalFacade), "error.authorization.project.modify", true);
            dalFacade.removeUserFromProject(memberUserId, projectId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_6, projectId, Activity.DataType.PROJECT, internalUserId);

            return Response.noContent().build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Remove member", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Remove member", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }
}
