package de.rwth.dbis.acis.bazaar.service.resources;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Feedback;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PrivilegeEnum;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Api(value = "feedback")
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
@Path("/feedback")
public class FeedbackResource {

    private final L2pLogger logger = L2pLogger.getInstance(CommentsResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    public FeedbackResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }

    /**
     * This method returns the list of feedback for a given project.
     *
     * @param projectId id of the project
     * @param page      page number
     * @param perPage   number of projects by page
     * @return Response with comments as a JSON array.
     */
    public Response getFeedbackForProject(int projectId, int page, int perPage) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            PageInfo pageInfo = new PageInfo(page, perPage);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            //Todo use requirement's projectId for security context, not the one sent from client
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            Project project = dalFacade.getProjectById(projectId, internalUserId);

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_FEEDBACK, project.getId(), dalFacade), "error.authorization.feedback.read");

            PaginationResult<Feedback> feedbackResult = dalFacade.getFeedbackByProject(projectId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_43,
                    projectId, Activity.DataType.FEEDBACK, internalUserId);
            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, null, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(feedbackResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, feedbackResult, "project/" + String.valueOf(projectId) + "/feedbacks", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, feedbackResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get feedback for project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get feedback for project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }


    /**
     * This method allows to create a new comment.
     *
     * @param givenFeedback feedback as JSON object
     * @return Response with the created comment as JSON object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to anonymously submit feedback.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created comment", response = Feedback.class),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response sendFeedback(@ApiParam(value = "Feedback entity", required = true) Feedback givenFeedback) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            resourceHelper.handleGenericError(bazaarService.validateCreate(givenFeedback));
            Feedback createdFeedback = dalFacade.createFeedback(givenFeedback);
            return Response.status(Response.Status.CREATED).entity(createdFeedback.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Create feedback", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Create feedback", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }
}
