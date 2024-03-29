package de.rwth.dbis.acis.bazaar.service.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
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
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(value = "categories", description = "Categories resource")
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
@Path("/categories")
public class CategoryResource {

    private final L2pLogger logger = L2pLogger.getInstance(CategoryResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    public CategoryResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }

    /**
     * This method returns the list of categories under a given project.
     *
     * @param projectId     id of the project
     * @param page          page number
     * @param perPage       number of projects by page
     * @param search        search string
     * @param sort          item to sort by
     * @param sortDirection sort order
     * @return Response with categories as a JSON array.
     */
    public Response getCategoriesForProject(int projectId, int page, int perPage, String search, List<String> sort, String sortDirection) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            List<Pageable.SortField> sortList = resourceHelper.getSortFieldList(sort, sortDirection);
            PageInfo pageInfo = new PageInfo(page, perPage, new HashMap<>(), sortList, search);
            resourceHelper.checkViolations(pageInfo);
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = getInternalUserId(projectId, dalFacade, userId);
            PaginationResult<Category> categoriesResult = dalFacade.listCategoriesByProjectId(projectId, pageInfo, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_13,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, search, sort);

            Response.ResponseBuilder responseBuilder = buildCategoryForProjectResponse(projectId, categoriesResult, parameter);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get categories for project ", logger);

        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get categories for project ", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private Response.ResponseBuilder buildCategoryForProjectResponse(int projectId, PaginationResult<Category> categoriesResult, Map<String, List<String>> parameter) throws JsonProcessingException, URISyntaxException {
        Response.ResponseBuilder responseBuilder = Response.ok();
        responseBuilder = responseBuilder.entity(categoriesResult.toJSON());
        responseBuilder = bazaarService.paginationLinks(responseBuilder, categoriesResult, "projects/" + String.valueOf(projectId) + "/categories", parameter);
        responseBuilder = bazaarService.xHeaderFields(responseBuilder, categoriesResult);
        return responseBuilder;
    }

    @NotNull
    public Integer getInternalUserId(int projectId, DALFacade dalFacade, String userId) throws Exception {
        Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
        if (dalFacade.getProjectById(projectId, internalUserId) == null) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "category"));
        }
        if (dalFacade.isProjectPublic(projectId)) {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_PUBLIC_CATEGORY, projectId, dalFacade), ResourceHelper.ERROR_ANONYMUS, true);
        } else {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_CATEGORY, projectId, dalFacade), "error.authorization.category.read", true);
        }
        return internalUserId;
    }

    /**
     * This method allows to retrieve a certain category.
     *
     * @param categoryId id of the category under a given project
     * @return Response with a category as a JSON object.
     */
    @GET
    @Path("/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category categoryToReturn = dalFacade.getCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            if (dalFacade.isCategoryPublic(categoryId)) {
                resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_PUBLIC_CATEGORY, categoryToReturn.getProjectId(), dalFacade), ResourceHelper.ERROR_ANONYMUS, true);
            } else {
                resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_CATEGORY, categoryToReturn.getProjectId(), dalFacade), "error.authorization.category.read", true);
            }
            return Response.ok(categoryToReturn.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get projects ", logger);

        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get projects ", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new category.
     *
     * @param categoryToCreate category as a JSON object
     * @return Response with the created project as a JSON object.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new category under a given a project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response createCategory(@ApiParam(value = "Category entity", required = true) Category categoryToCreate) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            resourceHelper.handleGenericError(bazaarService.validateCreate(categoryToCreate));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Create_CATEGORY, categoryToCreate.getProjectId(), dalFacade), "error.authorization.category.create", true);
            categoryToCreate.setCreator(dalFacade.getUserById(internalUserId));
            Category createdCategory = dalFacade.createCategory(categoryToCreate, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(createdCategory.getCreationDate(), Activity.ActivityAction.CREATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16,
                    createdCategory.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(createdCategory.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Create category ", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Create category ", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Allows to update a certain category.
     *
     * @param categoryToUpdate updated category as a JSON object
     * @return Response with the updated category as a JSON object.
     */
    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update a certain category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateCategory(@ApiParam(value = "Category entity", required = true) Category categoryToUpdate) {

        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            resourceHelper.handleGenericError(bazaarService.validate(categoryToUpdate));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            // Prevent forget project ids
            Category internalCategory = dalFacade.getCategoryById(categoryToUpdate.getId(), internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_CATEGORY, internalCategory.getProjectId(), dalFacade), "error.authorization.category.modify", true);

            Category updatedCategory = dalFacade.modifyCategory(categoryToUpdate);
            bazaarService.getNotificationDispatcher().dispatchNotification(updatedCategory.getLastUpdatedDate(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17,
                    updatedCategory.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(updatedCategory.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Update category " + categoryToUpdate.getId(), logger);

        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Update category " + categoryToUpdate.getId(), logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Allows to delete a category.
     *
     * @param categoryId id of the category to delete
     * @return Response with deleted category as a JSON object.
     */
    @DELETE
    @Path("/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response deleteCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category categoryToDelete = dalFacade.getCategoryById(categoryId, internalUserId);
            Project project = dalFacade.getProjectById(categoryToDelete.getProjectId(), internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_CATEGORY, project.getId(), dalFacade), "error.authorization.category.modify", true);
            isDefaultCategory(categoryId, project);
            Category deletedCategory = dalFacade.deleteCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(deletedCategory.getLastUpdatedDate(), Activity.ActivityAction.DELETE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18,
                    deletedCategory.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(deletedCategory.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Delete category " + categoryId, logger);

        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Delete category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private static void isDefaultCategory(int categoryId, Project project) throws BazaarException {
        if (project.getDefaultCategoryId() != null && project.getDefaultCategoryId() == categoryId) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception(),
                    ExceptionLocation.BAZAARSERVICE,
                    ErrorCode.CANNOTDELETE,
                    MessageFormat.format(Localization.getInstance().getResourceBundle().getString("error.authorization.category.delete"), categoryId)
            );
        }
    }

    /**
     * This method add the current user to the followers list of a given category.
     *
     * @param categoryId id of the category
     * @return Response with category as a JSON object.
     */
    @POST
    @Path("/{categoryId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method add the current user to the followers list of a given category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response followCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade), "error.authorization.follow.create", true);
            dalFacade.followCategory(internalUserId, categoryId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.FOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19,
                    category.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(category.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Follow category " + categoryId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Follow category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method removes the current user from a followers list of a given category.
     *
     * @param categoryId id of the category
     * @return Response with category as a JSON object.
     */
    @DELETE
    @Path("/{categoryId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the current user from a followers list of a given category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unfollowCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade), "error.authorization.follow.delete", true);
            dalFacade.unFollowCategory(internalUserId, categoryId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UNFOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20,
                    category.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(category.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unfollow category " + categoryId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unfollow category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve statistics for one category.
     *
     * @param categoryId
     * @param since      timestamp since filter, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z
     * @return Response with statistics as a JSON object.
     */
    @GET
    @Path("/{categoryId}/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve statistics for one category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns statistics", response = Statistic.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getStatisticsForCategory(
            @PathParam("categoryId") int categoryId,
            @ApiParam(value = "Since timestamp, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z", required = false) @QueryParam("since") String since) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            Calendar sinceCal = resourceHelper.getSinceCal(since);
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Statistic categoryStatistics = dalFacade.getStatisticsForCategory(internalUserId, categoryId, sinceCal);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(categoryStatistics.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get statistics for category " + categoryId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get statistics for category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of contributors for a specific category.
     *
     * @param categoryId id of the category
     * @return Response with category contributors
     */
    @GET
    @Path("/{categoryId}/contributors")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of contributors for a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of contributors for a given category", response = CategoryContributors.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getContributorsForCategory(@PathParam("categoryId") int categoryId) throws Exception {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            CategoryContributors categoryContributors = dalFacade.listContributorsForCategory(categoryId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_22,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(categoryContributors.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get contributors for category " + categoryId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get contributors for category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of followers for a specific category.
     *
     * @param categoryId id of the category
     * @param page       page number
     * @param perPage    number of projects by page
     * @return Response with followers as a JSON array.
     */
    @GET
    @Path("/{categoryId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of followers for a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of followers for a given category", response = User.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getFollowersForCategory(@PathParam("categoryId") int categoryId,
                                            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                            @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            PageInfo pageInfo = new PageInfo(page, perPage);
            resourceHelper.checkViolations(pageInfo);

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            PaginationResult<User> categoryFollowers = dalFacade.listFollowersForCategory(categoryId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_23,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, null, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(categoryFollowers.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, categoryFollowers, "categories/" + String.valueOf(categoryId) + "/followers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, categoryFollowers);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get followers for category " + categoryId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get followers for category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of requirements for a specific category.
     *
     * @param categoryId  id of the category under a given project
     * @param page        page number
     * @param perPage     number of projects by page
     * @param search      search string
     * @param stateFilter requirement state
     * @param sort        sort order
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/{categoryId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements for a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of requirements for a given project", response = Requirement.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getRequirementsForCategory(@PathParam("categoryId") int categoryId,
                                               @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                               @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                                               @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
                                               @ApiParam(value = "State filter", required = false, allowableValues = "all,open,realized") @DefaultValue("all") @QueryParam("state") String stateFilter,
                                               @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "date,last_activity,name,vote,comment,follower") @DefaultValue("date") @QueryParam("sort") List<String> sort,
                                               @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @DefaultValue("DESC") @QueryParam("sortDirection") String sortDirection
    ) throws Exception {
        RequirementsResource requirementsResource = new RequirementsResource();
        return requirementsResource.getRequirementsForCategory(categoryId, page, perPage, search, stateFilter, sort, sortDirection);
    }
}
