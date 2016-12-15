package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Context;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.EnumSet;

@ServicePath("/bazaar/users")
public class UsersResource extends RESTService {

    private BazaarService bazaarService;

    @Override
    protected void initResources() {
        getResourceConfig().register(UsersResource.Resource.class);
    }

    public UsersResource() throws Exception {
        bazaarService = new BazaarService();
    }

    @Api(value = "users", description = "Users resource")
    @Path("/")
    public static class Resource {

        private final UsersResource service = (UsersResource) Context.getCurrent().getService();

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
        public Response getUser(@PathParam("userId") int userId) {
            DALFacade dalFacade = null;
            try {
                // TODO: check whether the current user may request this project
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                dalFacade = service.bazaarService.getDBConnection();
                User user = dalFacade.getUserById(userId);
                Gson gson = new Gson();
                return Response.ok(gson.toJson(user)).build();
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

        /**
         * Allows to update a certain user.
         *
         * @param userId id of the user to update
         * @param user   updated user as a JSON object
         * @return Response with the updated user as a JSON object.
         */
        @PUT
        @Path("/{userId}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to update the user profile.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated user"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response updateUser(@PathParam("userId") int userId,
                                   @ApiParam(value = "User entity as JSON", required = true) String user) {
            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                Gson gson = new Gson();
                User userToUpdate = gson.fromJson(user, User.class);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(userToUpdate);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(agent.getId());
                if (!internalUserId.equals(userId)) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION,
                            "UserId is not identical with user sending this request.");
                }
                User updatedUser = dalFacade.modifyUser(userToUpdate);
                return Response.ok(gson.toJson(updatedUser)).build();
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
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the active user"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getActiveUser() {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                User user = dalFacade.getUserById(internalUserId);
                Gson gson = new Gson();
                return Response.ok(gson.toJson(user)).build();
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
