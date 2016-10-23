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
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManagerImpl;
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
     * This method allows to retrieve a certain attachment.
     *
     * @param attachmentId id of the attachment
     * @return Response with attachment as a JSON object.
     */
    @GET
    @Path("/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain attachment")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain attachment"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getAttachment(@PathParam("attachmentId") int attachmentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Attachment attachment = dalFacade.getAttachmentById(attachmentId);
            Requirement requirement = dalFacade.getRequirementById(attachment.getRequirementId(), internalUserId);
            if (dalFacade.isProjectPublic(requirement.getProjectId())) {
                boolean authorized = new AuthorizationManagerImpl().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManagerImpl().isAuthorized(internalUserId, PrivilegeEnum.Read_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.read"));
                }
            }
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(attachment), HttpURLConnection.HTTP_OK);
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
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new attachment.
     *
     * @param attachment as JSON object
     * @return Response with the created attachment as JSON object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created attachement"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse createAttachment(@ApiParam(value = "Attachment entity as JSON", required = true) @ContentParam String attachment) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            Attachment attachmentToCreate = gson.fromJson(attachment, Attachment.class);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(attachmentToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(attachmentToCreate.getRequirementId(), internalUserId);
            boolean authorized = new AuthorizationManagerImpl().isAuthorized(internalUserId, PrivilegeEnum.Create_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.create"));
            }
            attachmentToCreate.setCreatorId(internalUserId);
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
            bazaarService.closeDBConnection(dalFacade);
        }
    }

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
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(attachmentId, internalUserId);
            boolean authorized = new AuthorizationManagerImpl().isAuthorized(internalUserId, PrivilegeEnum.Modify_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
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
            bazaarService.closeDBConnection(dalFacade);
        }
    }

}
