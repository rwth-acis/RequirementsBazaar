package de.rwth.dbis.acis.bazaar.service.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.SystemRole;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import de.rwth.dbis.acis.bazaar.service.twitter.WeeklyNewProjectsTweetTask;
import i5.las2peer.api.Context;
import i5.las2peer.api.security.Agent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import lombok.Builder;
import lombok.Getter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Parent endpoint for global, administrative operations and queries
 */
@Api(value = "webhook")
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
@Path("/admin")
public class AdminResource {

    private final L2pLogger logger = L2pLogger.getInstance(AdminResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    @javax.ws.rs.core.Context
    UriInfo uriInfo;

    public AdminResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }

    @POST
    @Path("/twitter/test-tweet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Post a test Tweet on twitter")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns OK"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response postTestTweet() {
        return handleAuthenticatedRequest(
                SystemRole.SystemAdmin.name(),
                "Only Administrators can manually trigger a tweet",
                ((dalFacade, internalUserId) -> {
                    //// actual operation - start

                    int randomNumber = new Random().nextInt(4242);

                    bazaarService.getTweetDispatcher().publishTweet(
                            "Hello World! (from ReqBaz). Here's some random number: " + randomNumber);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    String json = mapper.writeValueAsString(response);
                    return Response.ok(json).build();

                    //// actual operation - end
                }),
                "Posting a test Tweet failed"
        );
    }

    @POST
    @Path("/twitter/trigger-new-projects-tweet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Manually trigger the Tweet about new projects")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns OK"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response triggerNewProjectsTweet() {
        return handleAuthenticatedRequest(
                SystemRole.SystemAdmin.name(),
                "Only Administrators can manually trigger a the tweet",
                ((dalFacade, internalUserId) -> {
                    //// actual operation - start

                    int randomNumber = new Random().nextInt(4242);

                    // Manually call task that is usually called by scheduler
                    WeeklyNewProjectsTweetTask task = new WeeklyNewProjectsTweetTask(bazaarService);
                    task.tweetNewProjects();

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    String json = mapper.writeValueAsString(response);
                    return Response.ok(json).build();

                    //// actual operation - end
                }),
                "Posting 'new projects Tweet' failed"
        );
    }

    @GET
    @Path("/twitter/authorize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Authorize ReqBaz to control a certain Twitter account.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns OK", response = TwitterAuthRedirect.class),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response authorizeTwitterAccount() {
        return handleAuthenticatedRequest(
                SystemRole.SystemAdmin.name(),
                "SystemAdmin role is required to link ReqBaz Twitter account",
                (dalFacade, internalUserId) -> {
                    String redirectUri = buildTwitterAuthRedirectUri();
                    logger.info("redirectUri: " + redirectUri);
                    String authorizationUrl = bazaarService.getTweetDispatcher().getAuthorizationUrl(redirectUri);

                    TwitterAuthRedirect redirect = TwitterAuthRedirect.builder()
                            .redirectUrl(authorizationUrl)
                            .build();
                    return Response.ok(redirect.toJSON()).build();
                },
                "Failed to init Twitter authentication process");
    }

    @GET
    @Path("/twitter/auth-cb")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Redirect callback after Twitter account authorized RqBaz for account control")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns OK"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response twitterAuthCallback(@QueryParam("code") String code) throws Exception {
        /*
         * No authentication here, because this callback is called by Twitter during authentication.
         */
        bazaarService.getTweetDispatcher().handleAuthCallback(buildTwitterAuthRedirectUri(), code);

        return Response.ok("You can close this tab now.").build();
    }

    private String buildTwitterAuthRedirectUri() {
        return uriInfo.getBaseUriBuilder()
                .path(AdminResource.class)
                .path(AdminResource.class, "twitterAuthCallback")
                .build().toString();
    }

    /**
     * Helper function to reduce duplicated code in every request handler that does the following:
     *
     * 1. Register a new agent if requesting user has not one (first login handler)
     * 2. Translate agentId into a ReqBaz internal user ID
     * 3. Ensure the authenticated user has the role which is required for the operation
     * 4. [ call actual request handler ]
     * 5. Catch all exceptions and translate them to appropriate HTTP responses (handle authentication/authorization
     *     exceptions with special HTTP status code)
     *
     * @param requiredRole the role required for the request
     * @param authorizationErrorMessage error message in case of an authorization error
     * @param handler the actual request handler that is called after authorization
     * @param errorMessage error message in other case (uncaught exception)
     * @return the resposne to the request
     */
    private Response handleAuthenticatedRequest(
            String requiredRole,
            String authorizationErrorMessage,
            BazaarRequestHandler handler,
            String errorMessage) {
        DALFacade dalFacade;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            resourceHelper.checkRegistrarErrors();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, dalFacade.getRoleByName(requiredRole), dalFacade), authorizationErrorMessage, true);

            //// actual operation -start
            return handler.handle(dalFacade, internalUserId);

        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, errorMessage, logger);

        } catch (Exception ex) {
            return resourceHelper.handleException(ex, errorMessage, logger);
        }
    }

    @FunctionalInterface
    private interface BazaarRequestHandler {

        /**
         *
         * @param dalFacade facade for the DAL
         * @param internalUserId user ID of the authenticated user
         * @return response for the request
         */
        Response handle(DALFacade dalFacade, Integer internalUserId) throws Exception;
    }

    @Getter
    @Builder
    public static class TwitterAuthRedirect {

        /** URL where the user should be redirected to */
        private String redirectUrl;

        public String toJSON() throws JsonProcessingException {
            // TODO Refactor (multiple classes have this helper method!)
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .writeValueAsString(this);
        }
    }
}
