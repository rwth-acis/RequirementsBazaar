package de.rwth.dbis.acis.bazaar.service;


import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Attachment;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PrivilegeEnum;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
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
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.EnumSet;

@Path("/bazaar/attachments")
@Api(value = "/attachments", description = "Attachments resource")
public class AttachmentsResource extends Service {

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

    public AttachmentsResource() throws Exception {
        bazaarService = new BazaarService();
    }

    /**
     * This method allows to create a new attachment.
     *
     * @param attachmentType type of attachment
     * @param attachment     attachment as JSON object
     * @return Response with the created attachment as JSON object.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created comment"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse createAttachment(@ApiParam(value = "Attachment type", allowableValues = "U") @DefaultValue("U") @QueryParam("attachmentType") String attachmentType,
                                         @ApiParam(value = "Attachment entity as JSON", required = true) @ContentParam String attachment) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            //TODO??? HOW DOES IT KNOW THE TYPE
            Attachment attachmentToCreate = gson.fromJson(attachment, Attachment.class);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(attachmentToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(attachmentToCreate.getRequirementId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.create"));
            }
            Attachment createdAttachment = dalFacade.createAttachment(attachmentToCreate);
            return new HttpResponse(gson.toJson(createdAttachment), HttpURLConnection.HTTP_CREATED);
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
//    @Path("/{attachmentId}")
//    public String updateAttachment(@PathParam("projectId") int projectId,
//                                   @PathParam("componentId") int componentId,
//                                   @PathParam("requirementId") int requirementId,
//                                   @PathParam("attachmentId") int attachmentId) {
//        return "[]";
//    }

    /**
     * This method deletes a specific attachment.
     *
     * @param attachmentId id of the attachment, which should be deleted
     * @return Response with the deleted attachment as a JSON object.
     */
    @DELETE
    @Path("/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted attachment"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse deleteAttachment(@PathParam("attachmentId") int attachmentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = bazaarService.createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            // TODO check requirement
            Requirement requirement = dalFacade.getRequirementById(attachmentId, internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_ATTACHMENT, Arrays.asList(String.valueOf(attachmentId), String.valueOf(requirement.getProjectId())), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.modify"));
            }
            Attachment deletedAttachment = dalFacade.deleteAttachmentById(attachmentId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(deletedAttachment), HttpURLConnection.HTTP_OK);
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
