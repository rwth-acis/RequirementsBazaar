/*
 *
 *  Copyright (c) 2014, RWTH Aachen University.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package de.rwth.dbis.acis.bazaar.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.notification.ActivityDispatcher;
import de.rwth.dbis.acis.bazaar.service.notification.EmailDispatcher;
import de.rwth.dbis.acis.bazaar.service.notification.NotificationDispatcher;
import de.rwth.dbis.acis.bazaar.service.notification.NotificationDispatcherImp;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;
import org.apache.commons.dbcp2.*;
import org.apache.http.client.utils.URIBuilder;
import org.jooq.SQLDialect;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.*;


/**
 * Requirements Bazaar LAS2peer Service
 * <p>
 * This is the main service class of the Requirements Bazaar
 *
 * @author István Koren
 */
@ServicePath("/bazaar")
public class BazaarService extends RESTService {

    //CONFIG PROPERTIES
    protected String dbUserName;
    protected String dbPassword;
    protected String dbUrl;
    protected String lang;
    protected String country;
    protected String baseURL;
    protected String frontendBaseURL;
    protected String activityTrackerService;
    protected String activityOrigin;
    protected String smtpServer;
    protected String emailFromAddress;

    private Vtor vtor;
    private List<BazaarFunctionRegistrar> functionRegistrar;
    private NotificationDispatcher notificationDispatcher;
    private DataSource dataSource;

    private final L2pLogger logger = L2pLogger.getInstance(BazaarService.class.getName());
    private static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    protected void initResources() {
        getResourceConfig().register(Resource.class);
        getResourceConfig().register(ProjectsResource.class);
        getResourceConfig().register(CategoryResource.class);
        getResourceConfig().register(RequirementsResource.class);
        getResourceConfig().register(CommentsResource.class);
        getResourceConfig().register(AttachmentsResource.class);
        getResourceConfig().register(UsersResource.class);
    }

    public BazaarService() throws Exception {

        setFieldValues();
        Locale locale = new Locale(lang, country);
        Localization.getInstance().setResourceBundle(ResourceBundle.getBundle("i18n.Translation", locale));

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        dataSource = setupDataSource(dbUrl, dbUserName, dbPassword);

        functionRegistrar = new ArrayList<>();
        functionRegistrar.add(functions -> {
            DALFacade dalFacade = null;
            try {
                dalFacade = getDBConnection();
                AuthorizationManager.SyncPrivileges(dalFacade);
            } catch (BazaarException ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.privilege_sync"));
            } catch (Exception e) {
                ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.privilege_sync"));
            } finally {
                closeDBConnection(dalFacade);
            }
        });

        functionRegistrar.add(functions -> {
            if (functions.contains(BazaarFunction.VALIDATION)) {
                createValidators();
            }
        });

        functionRegistrar.add(functions -> {
            if (functions.contains(BazaarFunction.USER_FIRST_LOGIN_HANDLING)) {
                registerUserAtFirstLogin();
            }
        });

        notificationDispatcher = new NotificationDispatcherImp();
        if (!activityTrackerService.isEmpty()) {
            notificationDispatcher.setActivityDispatcher(new ActivityDispatcher(this, activityTrackerService, activityOrigin, baseURL, frontendBaseURL));
        }
        if (!smtpServer.isEmpty()) {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpServer);
            notificationDispatcher.setEmailDispatcher(new EmailDispatcher(this, smtpServer, emailFromAddress, frontendBaseURL));
        }
        notificationDispatcher.setBazaarService(this);
    }

    @Api(value = "/", description = "Bazaar service")
    @SwaggerDefinition(
            info = @Info(
                    title = "Requirements Bazaar",
                    version = "0.7",
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
    @Path("/")
    public static class Resource {

        private final BazaarService bazaarService = (BazaarService) Context.getCurrent().getService();

        /**
         * This method allows to retrieve statistics over all projects.
         *
         * @param since      timestamp since filter
         * @return Response with statistics as a JSON object.
         */
        @GET
        @Path("/statistics")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to retrieve statistics over all projects.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns statistics", response = Statistic.class),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getStatistics(
                @ApiParam(value = "Since timestamp", required = false) @QueryParam("since") String since) {
            DALFacade dalFacade = null;
            try {
                String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registrarErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                dalFacade = bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                Calendar sinceCal = since == null ? null : DatatypeConverter.parseDateTime(since);
                Statistic platformStatistics = dalFacade.getStatisticsForAllProjects(internalUserId, sinceCal);
                bazaarService.getNotificationDispatcher().dispatchNotification(new Date(), Activity.ActivityAction.RETRIEVE, NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_2,
                        0, Activity.DataType.STATISTIC, internalUserId);
                return Response.ok(platformStatistics.toJSON()).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                    return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                } else {
                    bazaarService.logger.warning(bex.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
                }
            } catch (Exception ex) {
                BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get statistics failed");
                bazaarService.logger.warning(bex.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } finally {
                bazaarService.closeDBConnection(dalFacade);
            }
        }
    }

    public String notifyRegistrars(EnumSet<BazaarFunction> functions) {
        String resultJSON = null;
        try {
            for (BazaarFunctionRegistrar functionRegistrar : functionRegistrar) {
                functionRegistrar.registerFunction(functions);
            }
        } catch (BazaarException bazaarEx) {
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarEx);
        } catch (Exception ex) {
            BazaarException bazaarException = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.registrars"));
            resultJSON = ExceptionHandler.getInstance().toJSON(bazaarException);
        }
        return resultJSON;
    }

    private void createValidators() {
        vtor = new Vtor();
    }

    public Vtor getValidators() {
        return vtor;
    }

    public NotificationDispatcher getNotificationDispatcher() {
        return notificationDispatcher;
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
            JsonNode userDataJson = mapper.readTree(agent.getUserData().toString());
            JsonNode pictureJson = userDataJson.get("picture");
            String agentPicture;

            if (pictureJson == null)
                agentPicture = profileImage;
            else
                agentPicture = pictureJson.textValue();

            if (agentPicture != null && !agentPicture.isEmpty())
                profileImage = agentPicture;
            String givenNameData = userDataJson.get("given_name").textValue();
            if (givenNameData != null && !givenNameData.isEmpty())
                givenName = givenNameData;
            String familyNameData = userDataJson.get("family_name").textValue();
            if (familyNameData != null && !familyNameData.isEmpty())
                familyName = familyNameData;
        }

        DALFacade dalFacade = null;
        try {
            dalFacade = getDBConnection();
            Integer userIdByLAS2PeerId = dalFacade.getUserIdByLAS2PeerId(agent.getId());
            if (userIdByLAS2PeerId == null) {
                // create user
                User.Builder userBuilder = User.geBuilder(agent.getEmail());
                if (givenName != null)
                    userBuilder = userBuilder.firstName(givenName);
                if (familyName != null)
                    userBuilder = userBuilder.lastName(familyName);
                User user = userBuilder.admin(false).las2peerId(agent.getId()).userName(agent.getLoginName()).profileImage(profileImage)
                        .emailLeadSubscription(true).emailFollowSubscription(true).build();
                int userId = dalFacade.createUser(user).getId();
                this.getNotificationDispatcher().dispatchNotification(user.getCreationDate(), Activity.ActivityAction.CREATE, NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_55,
                        userId, Activity.DataType.USER, userId);
                dalFacade.addUserToRole(userId, "SystemAdmin", null);
            } else {
                // update lastLoginDate
                dalFacade.updateLastLoginDate(userIdByLAS2PeerId);
            }
        } catch (Exception ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.first_login"));
            logger.warning(ex.getMessage());
        } finally {
            closeDBConnection(dalFacade);
        }
    }

    public static DataSource setupDataSource(String dbUrl, String dbUserName, String dbPassword) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(dbUrl + "?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername(dbUserName);
        dataSource.setPassword(dbPassword);
        dataSource.setValidationQuery("SELECT 1;");
        dataSource.setTestOnBorrow(true); // test each connection when borrowing from the pool with the validation query
        dataSource.setMaxConnLifetimeMillis(1000 * 60 * 60); // max connection life time 1h. mysql drops connection after 8h.
        return dataSource;
    }

    public DALFacade getDBConnection() throws Exception {
        return new DALFacadeImpl(dataSource, SQLDialect.MYSQL);
    }

    public void closeDBConnection(DALFacade dalFacade) {
        if (dalFacade == null) return;
        dalFacade.close();
    }

    public Response.ResponseBuilder paginationLinks(Response.ResponseBuilder responseBuilder, PaginationResult paginationResult,
                                                    String path, Map<String, List<String>> httpParameter) throws URISyntaxException {
        List<Link> links = new ArrayList<>();
        URIBuilder uriBuilder = new URIBuilder(baseURL + path);
        for (Map.Entry<String, List<String>> entry : httpParameter.entrySet()) {
            for (String parameter : entry.getValue()) {
                uriBuilder.addParameter(entry.getKey(), parameter);
            }
        }
        if (paginationResult.getPrevPage() != -1) {
            links.add(Link.fromUri(uriBuilder.setParameter("page", String.valueOf(paginationResult.getPrevPage())).build()).rel("prev").build());
        }
        if (paginationResult.getNextPage() != -1) {
            links.add(Link.fromUri(uriBuilder.setParameter("page", String.valueOf(paginationResult.getNextPage())).build()).rel("next").build());
        }
        links.add(Link.fromUri(uriBuilder.setParameter("page", "0").build()).rel("first").build());
        links.add(Link.fromUri(uriBuilder.setParameter("page", String.valueOf(paginationResult.getTotalPages())).build()).rel("last").build());
        responseBuilder = responseBuilder.links(links.toArray(new Link[links.size()]));
        return responseBuilder;
    }

    public Response.ResponseBuilder xHeaderFields(Response.ResponseBuilder responseBuilder, PaginationResult paginationResult) {
        responseBuilder = responseBuilder.header("X-Page", String.valueOf(paginationResult.getPageable().getPageNumber()));
        responseBuilder = responseBuilder.header("X-Per-Page", String.valueOf(paginationResult.getPageable().getPageSize()));
        if (paginationResult.getPrevPage() != -1) {
            responseBuilder = responseBuilder.header("X-Prev-Page", String.valueOf(paginationResult.getPrevPage()));
        }
        if (paginationResult.getNextPage() != -1) {
            responseBuilder = responseBuilder.header("X-Next-Page", String.valueOf(paginationResult.getNextPage()));
        }
        responseBuilder = responseBuilder.header("X-Total-Pages", String.valueOf(paginationResult.getTotalPages()));
        responseBuilder = responseBuilder.header("X-Total", String.valueOf(paginationResult.getTotal()));
        return responseBuilder;
    }

}
