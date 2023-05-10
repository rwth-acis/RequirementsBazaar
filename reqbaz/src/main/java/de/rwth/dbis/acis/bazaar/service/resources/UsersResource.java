package de.rwth.dbis.acis.bazaar.service.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(value = "users", description = "Users resource")
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
@Path("/users")
public class UsersResource {

    private final L2pLogger logger = L2pLogger.getInstance(UsersResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    public UsersResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }

    /**
     * This method allows to search for users.
     *
     * @return Response with user as a JSON object.
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to search for users.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of matching users", response = User.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response searchUser(@ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
                               @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                               @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_USERS, dalFacade), ResourceHelper.ERROR_ANONYMUS, true);

            PageInfo pageInfo = new PageInfo(page, perPage, new HashMap<>(), new ArrayList<>(), search);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            PaginationResult<User> users = dalFacade.searchUsers(pageInfo);

            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_53,
                    internalUserId, Activity.DataType.USER, internalUserId);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, search, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(users.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, users, "users", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, users);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Search users", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Search users", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve a certain user.
     *
     * @param userId the id of the user to be returned
     * @return Response with user as a JSON object.
     */
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain user.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain user", response = User.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getUser(@PathParam("userId") int userId) {
        DALFacade dalFacade = null;
        try {
            // TODO: check whether the current user may request this project
            resourceHelper.checkRegistrarErrors();
            dalFacade = bazaarService.getDBConnection();
            User user = dalFacade.getUserById(userId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_53,
                    userId, Activity.DataType.USER, userId);

            return Response.ok(user.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get user " + userId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get user " + userId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve the active user.
     *
     * @return Response with active user as a JSON object.
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve the active user.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the active user", response = User.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getActiveUser() {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();

            // Block anonymous user
            resourceHelper.checkAuthorization(!userId.equals("anonymous"), "error.authorization.user.read", true);

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            User user = dalFacade.getUserById(internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_54,
                    internalUserId, Activity.DataType.USER, internalUserId);

            return Response.ok(user.toPrivateJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get active user", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get active user", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve the current users individual dashboard.
     *
     * @return Response with active user as a JSON object.
     */
    @GET
    @Path("/me/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve the current users individual dashboard.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns user dashboard data", response = Dashboard.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getUserDashboard() {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean isGamified = bazaarService.getGamificationManager().isAvailable();

            // Block anonymous user
            resourceHelper.checkAuthorization(!userId.equals("anonymous"), "error.authorization.user.read", true);
            // make sure logged-in user is added to gamification if not anonymous
            if (!userId.equals("anonymous") && isGamified) {
                bazaarService.getGamificationManager().initializeUser(internalUserId);
            }
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_54,
                    internalUserId, Activity.DataType.USER, internalUserId);
            Dashboard data = dalFacade.getDashboardData(internalUserId, 10);
            if (isGamified) {
                data.setBadges(bazaarService.getGamificationManager().getUserBadges(internalUserId));
                data.setStatus(bazaarService.getGamificationManager().getUserStatus(internalUserId));
                data.setGamificationNotifications(bazaarService.getGamificationManager().getUserNotifications(internalUserId));
            }
            String dashboardResponse = getDashboardResponse(isGamified, data);

            return Response.ok(dashboardResponse).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get active user", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get active user", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private static String getDashboardResponse(boolean isGamified, Dashboard data) throws JsonProcessingException {
        String dashboardResponse = data.toJSON();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = (Map<String, String>) mapper.readValue(dashboardResponse, Map.class);
        map.put("isGamified", String.valueOf(isGamified));
        dashboardResponse = mapper.writeValueAsString(map);
        return dashboardResponse;
    }

    /**
     * Allows to update a certain user.
     *
     * @param userId       id of the user to update
     * @param userToUpdate updated user as a JSON object
     * @return Response with the updated user as a JSON object.
     */
    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update the user profile.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated user", response = User.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateUser(@PathParam("userId") int userId,
                               @ApiParam(value = "User entity as JSON", required = true) User userToUpdate) {
        DALFacade dalFacade = null;
        try {
            resourceHelper.checkRegistrarErrors();
            Agent agent = Context.getCurrent().getMainAgent();
            resourceHelper.handleGenericError(bazaarService.validate(userToUpdate));
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(agent.getIdentifier());
            isUserIdMatching(userId, internalUserId);
            User updatedUser = dalFacade.modifyUser(userToUpdate);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_56,
                    userId, Activity.DataType.USER, internalUserId);
            return Response.ok(updatedUser.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Update user " + userId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Update user " + userId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private static void isUserIdMatching(int userId, Integer internalUserId) throws BazaarException {
        if (!internalUserId.equals(userId)) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION,
                    "UserId is not identical with user sending this request.");
        }
    }

    /**
     * This method returns an entityOverview for the logged in user
     *
     * @param search  search string
     * @param include which entities to include in the overview             //TODO Add Comments/Attachments
     * @param sort    sort order
     * @param filters set of entities that should be returned
     * @return Response as EntityOverview including the entities selected in include
     */
    @GET
    @Path("/me/entities")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to receive an overview of entities related to the user")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated user", response = EntityOverview.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getEntityOverview(
            @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
            @ApiParam(value = "Types of entities to include", required = true, allowMultiple = true, allowableValues = "projects,categories,requirements") @QueryParam("include") List<String> include,
            @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "name,date,last_activity,requirement,follower") @DefaultValue("date") @QueryParam("sort") List<String> sort,
            @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection,
            @ApiParam(value = "Filter", required = false, allowMultiple = true, allowableValues = "created, following, developing") @DefaultValue("created") @QueryParam("filters") List<String> filters) {
        //Possibly allow filtertype "all"?
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            List<Pageable.SortField> sortList = resourceHelper.getSortFieldList(sort, sortDirection);
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            HashMap<String, String> filterMap = new HashMap<>();
            for (String filterOption : filters) {
                filterMap.put(filterOption, internalUserId.toString());
            }
            PageInfo pageInfo = new PageInfo(0, 0, filterMap, sortList, search);

            EntityOverview result = dalFacade.getEntitiesForUser(include, pageInfo, internalUserId);
            // Wrong SERVICE_CUSTOM_MESSAGE_3 ?
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3,
                    0, Activity.DataType.USER, internalUserId);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(result.toJSON());

            return responseBuilder.build();
        } catch (BazaarException bex) {
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get entityOverview failed");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get entityOverview failed", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }


}
