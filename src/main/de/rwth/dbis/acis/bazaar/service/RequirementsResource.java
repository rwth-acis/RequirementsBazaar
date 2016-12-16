package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Violation;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.*;


@ServicePath("/bazaar/requirements")
public class RequirementsResource extends RESTService {

    private BazaarService bazaarService;

    @Override
    protected void initResources() {
        getResourceConfig().register(RequirementsResource.Resource.class);
    }

    public RequirementsResource() throws Exception {
        bazaarService = new BazaarService();
    }

    @Api(value = "requirements", description = "Requirements resource")
    @Path("/")
    public static class Resource {

        private final RequirementsResource service = (RequirementsResource) Context.getCurrent().getService();

        /**
         * This method returns a specific requirement.
         *
         * @param requirementId id of the requirement to retrieve
         * @return Response with requirement as a JSON object.
         */
        @GET
        @Path("/{requirementId}")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method returns a specific requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getRequirement(@PathParam("requirementId") int requirementId) {
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
                RequirementEx requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                if (dalFacade.isRequirementPublic(requirementId)) {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                    }
                } else {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                    }
                }
                Gson gson = new Gson();
                return Response.ok(gson.toJson(requirement)).build();
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
         * This method allows to create a new requirement.
         *
         * @param requirement requirement as a JSON object
         * @return Response with the created requirement as a JSON object.
         */
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to create a new requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response createRequirement(@ApiParam(value = "Requirement entity as JSON", required = true) String requirement) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors =service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                // TODO: check whether the current user may create a new requirement
                dalFacade = service.bazaarService.getDBConnection();
                Gson gson = new Gson();
                Requirement requirementToCreate = gson.fromJson(requirement, Requirement.class);
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                requirementToCreate.setCreatorId(internalUserId);
                if (requirementToCreate.getLeadDeveloperId() == 0) {
                    requirementToCreate.setLeadDeveloperId(1);
                }
                Vtor vtor = service.bazaarService.getValidators();
                vtor.useProfiles("create");
                vtor.validate(requirementToCreate);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                vtor.resetProfiles();

                // check if all components are in the same project
                for (Component component : requirementToCreate.getComponents()) {
                    component = dalFacade.getComponentById(component.getId());
                    if (requirementToCreate.getProjectId() != component.getProjectId()) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.VALIDATION, "Component does not fit with project");
                    }
                }
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_REQUIREMENT, String.valueOf(requirementToCreate.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.create"));
                }
                Requirement createdRequirement = dalFacade.createRequirement(requirementToCreate, internalUserId);
                dalFacade.followRequirement(internalUserId, createdRequirement.getId());

                // check if attachments are given
                if (requirementToCreate.getAttachments() != null && !requirementToCreate.getAttachments().isEmpty()) {
                    for (Attachment attachment : requirementToCreate.getAttachments()) {
                        attachment.setCreatorId(internalUserId);
                        attachment.setRequirementId(createdRequirement.getId());
                        vtor.validate(attachment);
                        if (vtor.hasViolations()) {
                            ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                        }
                        vtor.resetProfiles();
                        dalFacade.createAttachment(attachment);
                    }
                }

                createdRequirement = dalFacade.getRequirementById(createdRequirement.getId(), internalUserId);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, createdRequirement.getCreation_time(), Activity.ActivityAction.CREATE, createdRequirement.getId(),
                        Activity.DataType.REQUIREMENT, createdRequirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(createdRequirement)).build();
            } catch (BazaarException bex) {
                if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
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
         * This method updates a specific requirement within a project and component.
         *
         * @param requirementId id of the requirement to update
         * @param requirement   requirement as a JSON object
         * @return Response with updated requirement as a JSON object.
         */
        @PUT
        @Path("/{requirementId}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method updates a specific requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response updateRequirement(@PathParam("requirementId") int requirementId,
                                          @ApiParam(value = "Requirement entity as JSON", required = true) String requirement) {
            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                Gson gson = new Gson();
                Requirement requirementToUpdate = gson.fromJson(requirement, Requirement.class);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(requirementToUpdate);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.modify"));
                }
                if (requirementToUpdate.getId() != 0 && requirementId != requirementToUpdate.getId()) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
                }
                dalFacade.followRequirement(internalUserId, requirementToUpdate.getId());
                RequirementEx updatedRequirement = dalFacade.modifyRequirement(requirementToUpdate, internalUserId);
                if (requirementToUpdate.getRealized() == null) {
                    service.bazaarService.getNotificationDispatcher().dispatchNotification(service, updatedRequirement.getLastupdated_time(), Activity.ActivityAction.UPDATE, updatedRequirement.getId(),
                            Activity.DataType.REQUIREMENT, updatedRequirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                } else {
                    service.bazaarService.getNotificationDispatcher().dispatchNotification(service, updatedRequirement.getLastupdated_time(), Activity.ActivityAction.REALIZE, updatedRequirement.getId(),
                            Activity.DataType.REQUIREMENT, updatedRequirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                }
                return Response.ok(gson.toJson(updatedRequirement)).build();
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
         * This method deletes a specific requirement.
         *
         * @param requirementId id of the requirement to delete
         * @return Response with the deleted requirement as a JSON object.
         */
        @DELETE
        @Path("/{requirementId}")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method deletes a specific requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response deleteRequirement(@PathParam("requirementId") int requirementId) {
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
                RequirementEx requirementToDelete = dalFacade.getRequirementById(requirementId, internalUserId);
                Project project = dalFacade.getProjectById(requirementToDelete.getProjectId());
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, Arrays.asList(String.valueOf(project.getId()), String.valueOf(requirementId)), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.delete"));
                }
                Gson gson = new Gson();
                RequirementEx deletedRequirement = dalFacade.deleteRequirementById(requirementId, internalUserId);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, deletedRequirement.getLastupdated_time(), Activity.ActivityAction.DELETE, deletedRequirement.getId(),
                        Activity.DataType.REQUIREMENT, deletedRequirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                return Response.ok(gson.toJson(deletedRequirement)).build();
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
         * This method add the current user to the developers list of a given requirement.
         *
         * @param requirementId id of the requirement
         * @return Response with requirement as a JSON object.
         */
        @POST
        @Path("/{requirementId}/developers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method add the current user to the developers list of a given requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response addUserToDevelopers(@PathParam("requirementId") int requirementId) {
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
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_DEVELOP, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.develop.create"));
                }
                dalFacade.wantToDevelop(internalUserId, requirementId);
                dalFacade.followRequirement(internalUserId, requirementId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.DEVELOP, requirement.getId(),
                        Activity.DataType.REQUIREMENT, requirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                Gson gson = new Gson();
                return Response.status(Response.Status.CREATED).entity(gson.toJson(requirement)).build();
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
         * This method remove the current user from a developers list of a given requirement.
         *
         * @param requirementId id of the requirement
         * @return Response with requirement as a JSON object.
         */
        @DELETE
        @Path("/{requirementId}/developers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method remove the current user from a developers list of a given requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response removeUserFromDevelopers(@PathParam("requirementId") int requirementId) {
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
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_DEVELOP, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.develop.delete"));
                }
                dalFacade.notWantToDevelop(internalUserId, requirementId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.UNDEVELOP, requirement.getId(),
                        Activity.DataType.REQUIREMENT, requirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                return Response.ok(gson.toJson(requirement)).build();
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
         * This method add the current user to the followers list of a given requirement.
         *
         * @param requirementId id of the requirement
         * @return Response with requirement as a JSON object.
         */
        @POST
        @Path("/{requirementId}/followers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method add the current user to the followers list of a given requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response addUserToFollowers(@PathParam("requirementId") int requirementId) {
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
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.create"));
                }
                dalFacade.followRequirement(internalUserId, requirementId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.FOLLOW, requirement.getId(),
                        Activity.DataType.REQUIREMENT, requirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(requirement)).build();
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
         * This method removes the current user from a followers list of a given requirement.
         *
         * @param requirementId id of the requirement
         * @return Response with requirement as a JSON object.
         */
        @DELETE
        @Path("/{requirementId}/followers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method removes the current user from a followers list of a given requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response removeUserFromFollowers(@PathParam("requirementId") int requirementId) {
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
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.delete"));
                }
                dalFacade.unFollowRequirement(internalUserId, requirementId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.UNFOLLOW, requirement.getId(),
                        Activity.DataType.REQUIREMENT, requirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                return Response.ok(gson.toJson(requirement)).build();
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
         * This method creates a vote for the given requirement in the name of the current user.
         *
         * @param requirementId id of the requirement
         * @param direction     "up" or "down" vote direction
         * @return Response with requirement as a JSON object.
         */
        @POST
        @Path("/{requirementId}/vote")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method creates a vote for the given requirement in the name of the current user.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response addVote(@PathParam("requirementId") int requirementId,
                                @ApiParam(value = "Vote direction", allowableValues = "up, down") @DefaultValue("up") @QueryParam("direction") String direction) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                if (!(direction.equals("up") || direction.equals("down"))) {
                    Vtor vtor = service.bazaarService.getValidators();
                    vtor.addViolation(new Violation("Direction can only be \"up\" or \"down\"", direction, direction));
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_VOTE, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.create"));
                }
                dalFacade.vote(internalUserId, requirementId, direction.equals("up"));
                if (direction.equals("up")) {
                    dalFacade.followRequirement(internalUserId, requirementId);
                }
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.VOTE, requirement.getId(),
                        Activity.DataType.REQUIREMENT, requirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                Gson gson = new Gson();
                return Response.status(Response.Status.CREATED).entity(gson.toJson(requirement)).build();
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
         * This method removes the vote of the given requirement made by the current user.
         *
         * @param requirementId id of the requirement
         * @return Response with requirement as a JSON object.
         */
        @DELETE
        @Path("/{requirementId}/vote")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method removes the vote of the given requirement made by the current user.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response removeVote(@PathParam("requirementId") int requirementId) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                // TODO: check whether the current user may create a new requirement
                // TODO: check whether all required parameters are entered
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_VOTE, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.delete"));
                }
                dalFacade.unVote(internalUserId, requirementId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.UNVOTE, requirement.getId(),
                        Activity.DataType.REQUIREMENT, requirement.getComponents().get(0).getId(), Activity.DataType.COMPONENT, internalUserId);
                return Response.ok(gson.toJson(requirement)).build();
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
         * This method returns the list of comments for a specific requirement.
         *
         * @param requirementId id of the requirement
         * @param page          page number
         * @param perPage       number of projects by page
         * @return Response with comments as a JSON array.
         */
        @GET
        @Path("/{requirementId}/comments")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method returns the list of comments for a specific requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of comments for a given requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getComments(@PathParam("requirementId") int requirementId,
                                    @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                    @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                PageInfo pageInfo = new PageInfo(page, perPage);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(pageInfo);
                if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                dalFacade = service.bazaarService.getDBConnection();
                //Todo use requirement's projectId for serurity context, not the one sent from client
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                Project project = dalFacade.getProjectById(requirement.getProjectId());
                if (dalFacade.isRequirementPublic(requirementId)) {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT, String.valueOf(project.getId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                    }
                } else {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMMENT, String.valueOf(project.getId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.read"));
                    }
                }
                PaginationResult<Comment> commentsResult = dalFacade.listCommentsByRequirementId(requirementId, pageInfo);
                Gson gson = new Gson();

                Map<String, List<String>> parameter = new HashMap<>();
                parameter.put("page", new ArrayList() {{
                    add(String.valueOf(page));
                }});
                parameter.put("per_page", new ArrayList() {{
                    add(String.valueOf(perPage));
                }});

                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder = responseBuilder.entity(gson.toJson(commentsResult.getElements()));
                responseBuilder = service.bazaarService.paginationLinks(responseBuilder, commentsResult, "requirements/" + String.valueOf(requirementId) + "/comments", parameter);
                responseBuilder = service.bazaarService.xHeaderFields(responseBuilder, commentsResult);
                Response response = responseBuilder.build();

                return response;
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
         * This method returns the list of attachments for a specific requirement.
         *
         * @param requirementId id of the requirement
         * @param page          page number
         * @param perPage       number of projects by page
         * @return Response with comments as a JSON array.
         */
        @GET
        @Path("/{requirementId}/attachments")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method returns the list of attachments for a specific requirement.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of attachments for a given requirement"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getAttachments(@PathParam("requirementId") int requirementId,
                                       @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                       @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                PageInfo pageInfo = new PageInfo(page, perPage);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(pageInfo);
                if (vtor.hasViolations()) ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                dalFacade = service.bazaarService.getDBConnection();
                //Todo use requirement's projectId for serurity context, not the one sent from client
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
                Project project = dalFacade.getProjectById(requirement.getProjectId());
                if (dalFacade.isRequirementPublic(requirementId)) {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT, String.valueOf(project.getId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                    }
                } else {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMMENT, String.valueOf(project.getId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.comment.read"));
                    }
                }
                PaginationResult<Attachment> attachmentsResult = dalFacade.listAttachmentsByRequirementId(requirementId, pageInfo);
                Gson gson = new Gson();

                Map<String, List<String>> parameter = new HashMap<>();
                parameter.put("page", new ArrayList() {{
                    add(String.valueOf(page));
                }});
                parameter.put("per_page", new ArrayList() {{
                    add(String.valueOf(perPage));
                }});

                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder = responseBuilder.entity(gson.toJson(attachmentsResult.getElements()));
                responseBuilder = service.bazaarService.paginationLinks(responseBuilder, attachmentsResult, "requirements/" + String.valueOf(requirementId) + "/attachments", parameter);
                responseBuilder = service.bazaarService.xHeaderFields(responseBuilder, attachmentsResult);
                Response response = responseBuilder.build();

                return response;
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
