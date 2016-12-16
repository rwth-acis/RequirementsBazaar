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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
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
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;
import org.apache.commons.dbcp2.*;
import org.apache.http.client.utils.URIBuilder;
import org.jooq.SQLDialect;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.*;


/**
 * Requirements Bazaar LAS2peer Service
 * <p>
 * This is the main service class of the Requirements Bazaar
 *
 * @author István Koren
 */
//TODO Service from BasaarService, here is no Endpoint
@ServicePath("/bazaar/main")
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.3",
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
    protected String smtpServer;
    protected String emailFromAddress;

    private Vtor vtor;
    private List<BazaarFunctionRegistrator> functionRegistrators;
    private NotificationDispatcher notificationDispatcher;
    private DataSource dataSource;

    @Override
    protected void initResources() {
        getResourceConfig().register(Resource.class);
    }

    public BazaarService() throws Exception {

        setFieldValues();
        Locale locale = new Locale(lang, country);
        Localization.getInstance().setResourceBundle(ResourceBundle.getBundle("i18n.Translation", locale));

        Class.forName("com.mysql.jdbc.Driver").newInstance();

        dataSource = setupDataSource(dbUrl, dbUserName, dbPassword);

        functionRegistrators = new ArrayList<>();
        functionRegistrators.add(new BazaarFunctionRegistrator() {
            @Override
            public void registerFunction(EnumSet<BazaarFunction> functions) throws BazaarException {
                DALFacade dalFacade = null;
                try {
                    dalFacade = getDBConnection();
                    AuthorizationManager.SyncPrivileges(dalFacade);
                } catch (CommunicationsException commEx) {
                    ExceptionHandler.getInstance().convertAndThrowException(commEx, ExceptionLocation.BAZAARSERVICE, ErrorCode.DB_COMM, Localization.getInstance().getResourceBundle().getString("error.db_comm"));
                } catch (Exception ex) {
                    ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.privilige_sync"));
                } finally {
                    closeDBConnection(dalFacade);
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

        notificationDispatcher = new NotificationDispatcherImp();
        if (!activityTrackerService.isEmpty()) {
            notificationDispatcher.setActivityDispatcher(new ActivityDispatcher(this, activityTrackerService, baseURL, frontendBaseURL));
        }
        if (!smtpServer.isEmpty()) {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpServer);
            notificationDispatcher.setEmailDispatcher(new EmailDispatcher(this, smtpServer, emailFromAddress, frontendBaseURL));
        }
    }

    public String notifyRegistrators(EnumSet<BazaarFunction> functions) {
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
            dalFacade = getDBConnection();
            Integer userIdByLAS2PeerId = dalFacade.getUserIdByLAS2PeerId(agent.getId());
            if (userIdByLAS2PeerId == null) {
                User.Builder userBuilder = User.geBuilder(agent.getEmail());
                if (givenName != null)
                    userBuilder = userBuilder.firstName(givenName);
                if (familyName != null)
                    userBuilder = userBuilder.lastName(familyName);
                User user = userBuilder.admin(false).las2peerId(agent.getId()).userName(agent.getLoginName()).profileImage(profileImage)
                        .emailLeadItems(true).emailFollowItems(true).build();
                int userId = dalFacade.createUser(user).getId();
                dalFacade.addUserToRole(userId, "SystemAdmin", null);
            }
        } catch (Exception ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, Localization.getInstance().getResourceBundle().getString("error.first_login"));
        } finally {
            closeDBConnection(dalFacade);
        }
    }

    public static DataSource setupDataSource(String dbUrl, String dbUserName, String dbPassword) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(dbUrl);
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
