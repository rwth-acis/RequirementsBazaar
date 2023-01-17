package de.rwth.dbis.acis.bazaar.service.resources;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PersonalisationData;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PrivilegeEnum;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;


@Api(value = "personalisation", description = "Personalisation Data resource")
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
@Path("/personalisation")
public class PersonalisationDataResource {

    private final L2pLogger logger = L2pLogger.getInstance(PersonalisationDataResource.class.getName());
    private final BazaarService bazaarService;

    private final ResourceHelper resourceHelper;

    public PersonalisationDataResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }


    /**
     * This method allows to retrieve a certain stored personalisationData value.
     *
     * @param key     The plugins identifier
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PERSONALISATION_DATA, dalFacade), "error.authorization.personalisationData.read", true);
            PersonalisationData data = dalFacade.getPersonalisationData(internalUserId, key, version);
            return Response.ok(data.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get personalisationData " + key + " version:" + version, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get personalisationData " + key + " version:" + version, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_PERSONALISATION_DATA, dalFacade), "error.authorization.comment.create", true);
            PersonalisationData fullData = PersonalisationData.builder().key(key).userId(internalUserId).version(version).value(data.getValue()).build();
            resourceHelper.handleGenericError(bazaarService.validateCreate(fullData));
            dalFacade.setPersonalisationData(fullData);

            return Response.ok(fullData.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Set personalisationData", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Set personalisationData", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }


}
