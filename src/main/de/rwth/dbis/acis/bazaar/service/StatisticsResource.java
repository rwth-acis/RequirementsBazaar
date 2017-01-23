package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Context;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.util.*;

/**
 * Statistics Resource
 * Created by hugif on 26.12.2016.
 */
@ServicePath("/bazaar/statistics")
public class StatisticsResource extends RESTService {

    private BazaarService bazaarService;

    @Override
    protected void initResources() {
        getResourceConfig().register(StatisticsResource.Resource.class);
    }

    public StatisticsResource() throws Exception {
        bazaarService = new BazaarService();
    }

    @Api(value = "statistics", description = "Statistics resource")
    @SwaggerDefinition(
            info = @Info(
                    title = "Requirements Bazaar",
                    version = "0.5",
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
            host = "requirements-bazaar.org",
            basePath = "",
            schemes = SwaggerDefinition.Scheme.HTTPS
    )
    @Path("/")
    public static class Resource {

        private final StatisticsResource service = (StatisticsResource) Context.getCurrent().getService();

        /**
         * This method allows to retrieve statistics.
         *
         * @param since timestamp since filter
         * @param scopeField field for scopeId filter
         * @param scopeId id of scope filter
         * @return Response with statistics as a JSON object.
         */
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to retrieve statistics.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns statistics", response = Statistic.class),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getStatistics(
                @ApiParam(value = "Since timestamp", required = false) @QueryParam("since") String since,
                @ApiParam(value = "scope filter field", required = false, allowableValues = "project,component,requirement") @QueryParam("scopeField") String scopeField,
                @ApiParam(value = "scope filter id", required = false) @QueryParam("scopeId") int scopeId) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();

                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

                Statistic statisticsResult = null;

                Calendar sinceCal = since == null ? null : DatatypeConverter.parseDateTime(since);

                if (scopeField == null) {
                    statisticsResult = dalFacade.getStatisticsForAllProjects(internalUserId, sinceCal);
                } else if (scopeField.equals("project")) {
                    statisticsResult = dalFacade.getStatisticsForProject(internalUserId, scopeId, sinceCal);

                } else if (scopeField.equals("component")) {
                    statisticsResult = dalFacade.getStatisticsForComponent(internalUserId, scopeId, sinceCal);

                } else if (scopeField.equals("requirement")) {
                    statisticsResult = dalFacade.getStatisticsForRequirement(internalUserId, scopeId, sinceCal);
                }

                Gson gson = new Gson();
                return Response.ok(gson.toJson(statisticsResult)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                service.bazaarService.closeDBConnection(dalFacade);
            }
        }

    }
}
