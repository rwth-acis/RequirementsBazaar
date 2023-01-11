package de.rwth.dbis.acis.bazaar.service.resources;

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
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(value = "comments", description = "Comments resource")
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
@Path("/comments")
public class CommentsResource {

    private final L2pLogger logger = L2pLogger.getInstance(CommentsResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    public CommentsResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }


    /**
     * This method returns the list of comments on the server.
     *
     * @param page         page number
     * @param perPage      number of comments by page
     * @param embedParents embed context/parents of comment (project, requirement)
     * @param search       search string
     * @param sort         sort order
     * @return Response with list of all requirements
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of comments on the server.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of comments", response = Comment.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getAllComments(
            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
            @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
            @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
            @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "name,date") @DefaultValue("name") @QueryParam("sort") List<String> sort,
            @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection,
            @ApiParam(value = "Filter", required = true, allowMultiple = false, allowableValues = "created, following, replies") @QueryParam("filters") List<String> filters,
            @ApiParam(value = "Embed parents", required = true, allowMultiple = true, allowableValues = "project, requirement") @QueryParam("embedParents") List<String> embedParents) {

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
            PageInfo pageInfo = new PageInfo(page, perPage, filterMap, sortList, search, null, embedParents);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            PaginationResult<Comment> commentResult = null;

            //Might want to change this to allow anonymous agents to get all public requirements?
            if (agent instanceof AnonymousAgent) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comments.read"));
            } else {
                commentResult = dalFacade.listAllComments(pageInfo);
            }

            //TODO Results in "No CommentRecord found with id: 0"
            //bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3,
            //        0, Activity.DataType.COMMENT, internalUserId);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, search, sort);
            parameter.put("embedParents", embedParents);
            parameter.put("sort", sort);
            parameter.put("filters", filters);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(commentResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, commentResult, "comments", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, commentResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get all comments", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get all comments", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }


    /**
     * This method returns the list of comments for a specific requirement.
     *
     * @param requirementId id of the requirement
     * @return Response with comments as a JSON array.
     */
    public Response getCommentsForRequirement(int requirementId) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            resourceHelper.checkRegistrarErrors();

            dalFacade = bazaarService.getDBConnection();
            //Todo use requirement's projectId for security context, not the one sent from client
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Project project = dalFacade.getProjectById(requirement.getProjectId(), internalUserId);
            checkIsPublic(dalFacade.isRequirementPublic(requirementId), internalUserId, project.getId(), dalFacade);
            List<Comment> commentsResult = dalFacade.listCommentsByRequirementId(requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_43,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(bazaarService.getMapper().writeValueAsString(commentsResult));

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get comments for requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get comments for requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private void checkIsPublic(boolean dalFacade, Integer internalUserId, int project, DALFacade dalFacade1) throws BazaarException {
        if (dalFacade) {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT, project, dalFacade1), "error.authorization.anonymous");
        } else {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Read_COMMENT, project, dalFacade1), "error.authorization.comment.read");
        }
    }

    /**
     * This method allows to retrieve a certain comment.
     *
     * @param commentId id of the comment
     * @return Response with comment as a JSON object.
     */
    @GET
    @Path("/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain comment")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain comment", response = Comment.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getComment(@PathParam("commentId") int commentId) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Comment comment = dalFacade.getCommentById(commentId);
            Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_45,
                    commentId, Activity.DataType.COMMENT, internalUserId);
            checkIsPublic(dalFacade.isProjectPublic(requirement.getProjectId()), internalUserId, requirement.getProjectId(), dalFacade);
            return Response.ok(comment.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get comment " + commentId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get comment " + commentId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }


    /**
     * This method allows to create a new comment.
     *
     * @param commentToCreate comment as JSON object
     * @return Response with the created comment as JSON object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new comment.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created comment", response = Comment.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response createComment(@ApiParam(value = "Comment entity", required = true) Comment commentToCreate) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            resourceHelper.checkRegistrarErrors();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(commentToCreate.getRequirementId(), internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Create_COMMENT, requirement.getProjectId(), dalFacade), "error.authorization.comment.create");
            commentToCreate.setCreator(dalFacade.getUserById(internalUserId));
            resourceHelper.handleGenericError(bazaarService.validateCreate(commentToCreate));

            dalFacade.followRequirement(internalUserId, requirement.getId());
            Comment createdComment = dalFacade.createComment(commentToCreate);
            bazaarService.getNotificationDispatcher().dispatchNotification(createdComment.getCreationDate(), Activity.ActivityAction.CREATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_46,
                    createdComment.getId(), Activity.DataType.COMMENT, internalUserId);
            return Response.status(Response.Status.CREATED).entity(createdComment.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Create comment", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Create comment", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method updates a specific comment.
     *
     * @return Response with the updated comment as a JSON object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method modifies a specific comment.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated comment", response = Comment.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateComment(@ApiParam(value = "Comment entity", required = true) Comment commentToUpdate) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            Comment internalComment = dalFacade.getCommentById(commentToUpdate.getId());
            Requirement requirement = dalFacade.getRequirementById(internalComment.getRequirementId(), internalUserId);

            boolean authorized = new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_COMMENT, requirement.getProjectId(), dalFacade);
            if (!authorized && !internalComment.isOwner(internalUserId)) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.modify"));
            }
            internalComment.setMessage(commentToUpdate.getMessage());

            Comment updatedComment = dalFacade.updateComment(internalComment);

            bazaarService.getNotificationDispatcher().dispatchNotification(internalComment.getCreationDate(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48,
                    internalComment.getId(), Activity.DataType.COMMENT, internalUserId);
            return Response.ok(updatedComment.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Delete comment", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Delete comment", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method deletes a specific comment.
     *
     * @param commentId id of the comment, which should be deleted
     * @return Response with the deleted comment as a JSON object.
     */
    @DELETE
    @Path("/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific comment.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted comment", response = Comment.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response deleteComment(@PathParam("commentId") int commentId) {
        DALFacade dalFacade = null;
        try {
            // TODO: check if the user may delete this requirement.
            String userId = resourceHelper.getUserId();

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Comment commentToDelete = dalFacade.getCommentById(commentId);
            Requirement requirement = dalFacade.getRequirementById(commentToDelete.getRequirementId(), internalUserId);

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_COMMENT, requirement.getProjectId(), dalFacade), "error.authorization.comment.modify");
            Comment deletedComment = dalFacade.deleteCommentById(commentId);
            bazaarService.getNotificationDispatcher().dispatchNotification(deletedComment.getCreationDate(), Activity.ActivityAction.DELETE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48,
                    deletedComment.getId(), Activity.DataType.COMMENT, internalUserId);
            return Response.ok(deletedComment.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Delete comment " + commentId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Delete comment " + commentId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }
}
