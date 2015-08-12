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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Path("/bazaar/users")
@Api(value = "/users", description = "Users resource")
public class UsersResource extends Service {

    //CONFIG PROPERTIES
    protected String dbUserName;
    protected String dbPassword;
    protected String dbUrl;
    protected String lang;
    protected String country;

    private Vtor vtor;
    private List<BazaarFunctionRegistrator> functionRegistrators;

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

        functionRegistrators = new ArrayList<BazaarFunctionRegistrator>();
        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws BazaarException {
                DALFacade dalFacade = null;
                try {
                    dalFacade = createConnection();
                    AuthorizationManager.SyncPrivileges(dalFacade);
                } catch (CommunicationsException commEx) {
                    ExceptionHandler.getInstance().convertAndThrowException(commEx, ExceptionLocation.BAZAARSERVICE, ErrorCode.DB_COMM, Localization.getInstance().getResourceBundle().getString("error.db_comm"));
                } catch (Exception ex) {
                    ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.privilige_sync"));
                } finally {
                    closeConnection(dalFacade);
                }
            }
        });

        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) {
                if (functions.contains(BazaarFunction.VALIDATION)) {
                    createValidators();
                }
            }
        });

        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws Exception {
                if (functions.contains(BazaarFunction.USER_FIRST_LOGIN_HANDLING)) {
                    registerUserAtFirstLogin();
                }
            }
        });
    }

    private String notifyRegistrators(EnumSet<BazaarFunction> functions) {
        String resultJSON = null;
        try {
            for (BazaarFunctionRegistrator functionRegistrator : functionRegistrators) {
                functionRegistrator.registerFunction(functions);
            }
        } catch (BazaarException bazaarEx) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarEx);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.registrators"));
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);
        }
        return resultJSON;
    }

    private void createValidators() {
        vtor = new Vtor();
    }

    private void registerUserAtFirstLogin() throws Exception {
        UserAgent agent = (UserAgent) getActiveAgent();

        if (agent.getEmail() == null) agent.setEmail("NO.EMAIL@WARNING.COM");

        String profileImage = "https://api.learning-layers.eu/profile.png";
        String givenName = null;
        String familyName = null;

        //TODO how to check if the user is anonymous?
        if (agent.getLoginName().equals("anonymous")) {
            agent.setEmail("anonymous@requirements-bazaar.org");
        } else if (agent.getUserData() != null) {
            JsonObject userDataJson = new JsonParser().parse(agent.getUserData().toString()).getAsJsonObject();
            JsonPrimitive pictureJson = userDataJson.getAsJsonPrimitive("picture");
            String agentPicture;

            if (pictureJson == null)
                agentPicture = profileImage;
            else
                agentPicture = pictureJson.getAsString();

            if (agentPicture != null && !agentPicture.isEmpty())
                profileImage = agentPicture;
            String givenNameData = userDataJson.getAsJsonPrimitive("given_name").getAsString();
            if (givenNameData != null && !givenNameData.isEmpty())
                givenName = givenNameData;
            String familyNameData = userDataJson.getAsJsonPrimitive("family_name").getAsString();
            if (familyNameData != null && !familyNameData.isEmpty())
                familyName = familyNameData;
        }

        DALFacade dalFacade = null;
        try {
            dalFacade = createConnection();
            Integer userIdByLAS2PeerId = dalFacade.getUserIdByLAS2PeerId(agent.getId());
            if (userIdByLAS2PeerId == null) {
                User.Builder userBuilder = User.geBuilder(agent.getEmail());
                if (givenName != null)
                    userBuilder = userBuilder.firstName(givenName);
                if (familyName != null)
                    userBuilder = userBuilder.lastName(familyName);
                User user = userBuilder.admin(false).las2peerId(agent.getId()).userName(agent.getLoginName()).profileImage(profileImage).build();
                int userId = dalFacade.createUser(user);
                dalFacade.addUserToRole(userId, "SystemAdmin", null);
            }
        } catch (Exception ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.first_login"));
        } finally {
            closeConnection(dalFacade);
        }

    }

    private DALFacade createConnection() throws Exception {
        Connection dbConnection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        return new DALFacadeImpl(dbConnection, SQLDialect.MYSQL);
    }

    private void closeConnection(DALFacade dalFacade) {
        if (dalFacade == null) return;
        Connection dbConnection = dalFacade.getConnection();
        if (dbConnection != null) {
            try {
                dbConnection.close();
                System.out.println("Database connection closed!");
            } catch (SQLException ignore) {
                System.out.println("Could not close db connection!");
            }
        }
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
            @ApiResponse(code = 200, message = "Returns a certain user"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getUser(@PathParam("userId") int userId) {
        DALFacade dalFacade = null;
        try {
            // TODO: check whether the current user may request this project
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            User user = dalFacade.getUserById(userId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(user), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
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
            @ApiResponse(code = 200, message = "Returns the current user"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse getCurrentUser() {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            User user = dalFacade.getUserById(internalUserId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(user), 200);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 401);
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 404);
            } else {
                return new HttpResponse(ExceptionHandler.getInstance().toJSON(bex), 500);
            }
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "");
            return new HttpResponse(ExceptionHandler.getInstance().toJSON(bazaarException), 500);
        } finally {
            closeConnection(dalFacade);
        }
    }


}
