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

import com.google.gson.*;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
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
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.security.UserAgent;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.*;

import javax.ws.rs.*;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

import jodd.vtor.Violation;
import jodd.vtor.Vtor;
import org.jooq.SQLDialect;


import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;


/**
 * Requirements Bazaar LAS2peer Service
 * <p>
 * This is the main service class of the Requirements Bazaar
 *
 * @author István Koren
 */
@Path("/bazaar")
@Version("0.2")
@Api
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.2",
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
        ))
public class BazaarService extends Service {

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

    public BazaarService() throws Exception {

        setFieldValues();
        Locale locale = new Locale(lang, country);
        Localization.getInstance().setResourceBundle(ResourceBundle.getBundle("i18n.Translation", locale));

        Class.forName("com.mysql.jdbc.Driver").newInstance();

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

    public DALFacade createConnection() throws Exception {
        Connection dbConnection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        return new DALFacadeImpl(dbConnection, SQLDialect.MYSQL);
    }

    public void closeConnection(DALFacade dalFacade) {
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

    /**********************************
     * ATTACHMENTS
     **********************************/

    // TODO INCLUDED IN REQUIREMENT?
//    /**
//     * This method returns the list of attachments for a specific requirement.
//     *
//     * @param projectId     the ID of the project for the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement, whose attachments should be returned.
//     * @return a list of attachments
//     */
//    @GET
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String getAttachments(@PathParam("projectId") int projectId,
//                                 @PathParam("componentId") int componentId,
//                                 @PathParam("requirementId") int requirementId,
//                                 @QueryParam(name = "page", defaultValue = "0")  int page,
//                                 @QueryParam(name = "per_page", defaultValue = "10")  int perPage) {
//
//    }

    /**
     * This method allows to create a new attachment.
     *
     * @param attachmentType type of attachment
     * @param attachment     attachment as JSON object
     * @return Response with the created attachment as JSON object.
     */
    @POST
    @Path("/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Returns the created comment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse createAttachment(@ApiParam(value = "Attachment type", allowableValues = "U") @DefaultValue("U") @QueryParam("attachmentType") String attachmentType,
                                         @ApiParam(value = "Attachment entity as JSON", required = true) String attachment) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            // TODO: check whether the current user may create a new requirement
            // TODO: check whether all required parameters are entered
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            Gson gson = new Gson();
            //TODO??? HOW DOES IT KNOW THE TYPE
            Attachment attachmentToCreate = gson.fromJson(attachment, Attachment.class);
            vtor.validate(attachmentToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(attachmentToCreate.getRequirementId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_ATTACHMENT, String.valueOf(requirement.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.create"));
            }
            Attachment createdAttachment = dalFacade.createAttachment(attachmentToCreate);
            return new HttpResponse(gson.toJson(createdAttachment), 201);
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

    // TODO INCLUDED IN REQUIREMENT? NEEDED?
//    /**
//     * This method returns a specific attachment within a requirement.
//     *
//     * @param projectId     the ID of the project for the requirement.
//     * @param componentId   the id of the component under a given project
//     * @param requirementId the ID of the requirement, which was commented.
//     * @param attachmentId  the ID of the attachment, which should be returned.
//     * @return a specific attachment.
//     */
//    @GET
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
//    public String getAttachment(@PathParam("projectId") int projectId,
//                                @PathParam("componentId") int componentId,
//                                @PathParam("requirementId") int requirementId,
//                                @PathParam("attachmentId") int attachmentId) {
//        return "[]";
//    }

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
//    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
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
    @Path("/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific attachment.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Returns the deleted attachment"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 500, message = "Internal server problems")
    })
    public HttpResponse deleteAttachment(@PathParam("attachmentId") int attachmentId) {
        DALFacade dalFacade = null;
        try {
            long userId = ((UserAgent) getActiveAgent()).getId();
            String registratorErrors = notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registratorErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
            }
            dalFacade = createConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            // TODO check requirement
            Requirement requirement = dalFacade.getRequirementById(attachmentId, internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_ATTACHMENT, Arrays.asList(String.valueOf(attachmentId), String.valueOf(requirement.getProjectId())), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.attachment.modify"));
            }
            Attachment deletedAttachment = dalFacade.deleteAttachmentById(attachmentId);
            Gson gson = new Gson();
            return new HttpResponse(gson.toJson(deletedAttachment), 200);
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
