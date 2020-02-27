package de.rwth.dbis.acis.bazaar.service;

import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PersonalisationData;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PrivilegeEnum;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.*;


@Api(value = "personalisation", description = "Personalisation Data resource")
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.6",
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
@Path("/personalisation")
public class PersonalisationDataResource {

    private final L2pLogger logger = L2pLogger.getInstance(PersonalisationDataResource.class.getName());
    private BazaarService bazaarService;

    public PersonalisationDataResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
    }



    /**
     * This method allows to retrieve a certain stored personalisationData value.
     *
     * @param key The plugins identifier
     * @param version The plugins identifier
     * @return Response with attachment as a JSON object.
     */
    @GET
    @Path("/{key}-{version}/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain personalisationData value")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain personalisationData", response = PersonalisationData.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NO_CONTENT, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getPersonalisationData(@PathParam("key") String key, @PathParam("version") int version) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PERSONALISATION_DATA, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.personalisationData.read"));
            }
            PersonalisationData data = dalFacade.getPersonalisationData(internalUserId, key, version);
            bazaarService.getNotificationDispatcher().dispatchNotification(new Date(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_59,
                    0, Activity.DataType.PERSONALISATION, internalUserId, new Activity.AdditionalObject(data));

            return Response.ok(data.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NO_CONTENT).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get personalisationData " + key+" version:"+version );
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get personalisationData " + key+" version:"+version );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to save a personalisationData
     *
     * @param data as JSON object
     * @return Response with the created attachment as JSON object.
     */
    @PUT
    @Path("/{key}-{version}/")
    @Consumes(MediaType.APPLICATION_JSON)
    //@Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to save a personalisationData item")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Success", response = PersonalisationData.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response setPersonalisationData(@PathParam("key") String key, @PathParam("version") int version, @ApiParam(value = "PersonalisationData as JSON", required = true) PersonalisationData data) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();

            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);


            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PERSONALISATION_DATA, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.create"));
            }

            PersonalisationData fullData = PersonalisationData.getBuilder().key(key).userId(internalUserId).version(version).value(data.getValue()).build();


            Vtor vtor = bazaarService.getValidators();
            vtor.useProfiles("create");
            vtor.validate(fullData);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade.setPersonalisationData(fullData);

            bazaarService.getNotificationDispatcher().dispatchNotification(new Date(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_58,
                    0, Activity.DataType.PERSONALISATION, internalUserId, new Activity.AdditionalObject(fullData));


            return Response.ok(fullData.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Set personalisationData");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Set personalisationData");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }


}