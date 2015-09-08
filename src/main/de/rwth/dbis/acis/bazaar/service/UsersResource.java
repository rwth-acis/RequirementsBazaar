package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
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
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jodd.vtor.Vtor;
import org.jooq.SQLDialect;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Path("/bazaar/users")
@Api(value = "/users", description = "Users resource")
public class UsersResource extends Service {

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

    public UsersResource() throws Exception {
        bazaarService = new BazaarService();
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain user"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getUser(@PathParam("userId") int userId) {
        DALFacade dalFacade = null;
        try {
            // TODO: check whether the current user may request this project
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = bazaarService.createConnection();
            User user = dalFacade.getUserById(userId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(user), HttpURLConnection.HTTP_OK);
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
//     * Allows to update a certain project.
//     *
//     * @param userId the id of the user to update.
//     * @return a JSON string containing whether the operation was successful or
//     * not.
//     */
//    @PUT
//    @Path("/users/{userId}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String updateUser(@PathParam("userId") int userId) {
//        // TODO: check if user can change this project
//        return "{success=false}";
//    }

    /**
     * This method allows to retrieve the current user.
     *
     * @return Response with user as a JSON object.
     */
    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the current user"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public HttpResponse getCurrentUser() {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = bazaarService.createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            User user = dalFacade.getUserById(internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(user), HttpURLConnection.HTTP_OK);
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
