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
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.*;


@ServicePath("/bazaar/components")
public class ComponentsResource extends RESTService {

    private BazaarService bazaarService;

    @Override
    protected void initResources() {
        getResourceConfig().register(Resource.class);
    }

    public ComponentsResource() throws Exception {
        bazaarService = new BazaarService();
    }

    @Path("/")
    public static class Resource {

        private final ComponentsResource service = (ComponentsResource) Context.getCurrent().getService();

        /**
         * This method allows to retrieve a certain component.
         *
         * @param componentId id of the component under a given project
         * @return Response with a component as a JSON object.
         */
        @GET
        @Path("/{componentId}")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to retrieve a certain component.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain component"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getComponent(@PathParam("componentId") int componentId) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                dalFacade = service.bazaarService.getDBConnection();
                Component componentToReturn = dalFacade.getComponentById(componentId);
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                if (dalFacade.isComponentPublic(componentId)) {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_COMPONENT, String.valueOf(componentId), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                    }
                } else {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_COMPONENT, String.valueOf(componentId), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                    }
                }
                Gson gson = new Gson();
                return Response.ok(gson.toJson(componentToReturn)).build();
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
         * This method allows to create a new component.
         *
         * @param component component as a JSON object
         * @return Response with the created project as a JSON object.
         */
        @POST
        @Path("/")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to create a new component under a given a project.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created component"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response createComponent(@ApiParam(value = "Component entity as JSON", required = true) String component) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                // TODO: check whether the current user may create a new project
                // TODO: check whether all required parameters are entered
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                Gson gson = new Gson();
                Component componentToCreate = gson.fromJson(component, Component.class);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(componentToCreate);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_COMPONENT, String.valueOf(componentToCreate.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.create"));
                }
                componentToCreate.setLeaderId(internalUserId);
                Component createdComponent = dalFacade.createComponent(componentToCreate);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, createdComponent.getCreation_time(), Activity.ActivityAction.CREATE, createdComponent.getId(),
                        Activity.DataType.COMPONENT, createdComponent.getProjectId(), Activity.DataType.PROJECT, internalUserId);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(createdComponent)).build();
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
         * Allows to update a certain component.
         *
         * @param componentId id of the component under a given project
         * @param component   updated component as a JSON object
         * @return Response with the updated component as a JSON object.
         */
        @PUT
        @Path("/{componentId}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method allows to update a certain component.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated component"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response updateComponent(@PathParam("componentId") int componentId,
                                        @ApiParam(value = "Tag entity as JSON", required = true) String component) {
            DALFacade dalFacade = null;
            try {
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                Gson gson = new Gson();
                Component updatedComponent = gson.fromJson(component, Component.class);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(updatedComponent);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMPONENT, dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.modify"));
                }
                if (updatedComponent.getId() != 0 && componentId != updatedComponent.getId()) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
                }
                updatedComponent = dalFacade.modifyComponent(updatedComponent);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, updatedComponent.getLastupdated_time(), Activity.ActivityAction.UPDATE, updatedComponent.getId(),
                        Activity.DataType.COMPONENT, updatedComponent.getProjectId(), Activity.DataType.PROJECT, internalUserId);
                return Response.ok(gson.toJson(updatedComponent)).build();
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
         * Allows to delete a component.
         *
         * @param componentId id of the component to delete
         * @return Response with deleted component as a JSON object.
         */
        @DELETE
        @Path("/{componentId}")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method deletes a specific component.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted component"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response deleteComponent(@PathParam("componentId") int componentId) {
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
                Component componentToDelete = dalFacade.getComponentById(componentId);
                Project project = dalFacade.getProjectById(componentToDelete.getProjectId());
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_COMPONENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.modify"));
                }
                if (project.getDefaultComponentId() != null && project.getDefaultComponentId() == componentId) {
                    ExceptionHandler.getInstance().convertAndThrowException(
                            new Exception(),
                            ExceptionLocation.BAZAARSERVICE,
                            ErrorCode.CANNOTDELETE,
                            MessageFormat.format(Localization.getInstance().getResourceBundle().getString("error.authorization.component.delete"), componentId)
                    );
                }
                Gson gson = new Gson();
                Component deletedComponent = dalFacade.deleteComponentById(componentId, internalUserId);
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, deletedComponent.getLastupdated_time(), Activity.ActivityAction.DELETE, deletedComponent.getId(),
                        Activity.DataType.COMPONENT, deletedComponent.getProjectId(), Activity.DataType.PROJECT, internalUserId);
                return Response.ok(gson.toJson(deletedComponent)).build();
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
         * This method add the current user to the followers list of a given component.
         *
         * @param componentId id of the component
         * @return Response with component as a JSON object.
         */
        @POST
        @Path("/{componentId}/followers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method add the current user to the followers list of a given component.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the component"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response addUserToFollowers(@PathParam("componentId") int componentId) {
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
                dalFacade.followComponent(internalUserId, componentId);
                Component component = dalFacade.getComponentById(componentId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.FOLLOW, component.getId(),
                        Activity.DataType.COMPONENT, component.getProjectId(), Activity.DataType.PROJECT, internalUserId);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(component)).build();
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
         * This method removes the current user from a followers list of a given component.
         *
         * @param componentId id of the component
         * @return Response with component as a JSON object.
         */
        @DELETE
        @Path("/{componentId}/followers")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method removes the current user from a followers list of a given component.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the component"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response removeUserFromFollowers(@PathParam("componentId") int componentId) {
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
                dalFacade.unFollowComponent(internalUserId, componentId);
                Component component = dalFacade.getComponentById(componentId);
                Gson gson = new Gson();
                service.bazaarService.getNotificationDispatcher().dispatchNotification(service, new Date(), Activity.ActivityAction.UNFOLLOW, component.getId(),
                        Activity.DataType.COMPONENT, component.getProjectId(), Activity.DataType.PROJECT, internalUserId);
                return Response.ok(gson.toJson(component)).build();
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
         * This method returns the list of requirements for a specific component.
         *
         * @param componentId id of the component under a given project
         * @param page        page number
         * @param perPage     number of projects by page
         * @return Response with requirements as a JSON array.
         */
        @GET
        @Path("/{componentId}/requirements")
        @Produces(MediaType.APPLICATION_JSON)
        @ApiOperation(value = "This method returns the list of requirements for a specific component.")
        @ApiResponses(value = {
                @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of requirements for a given project"),
                @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
                @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
                @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
        })
        public Response getRequirementsByComponent(@PathParam("componentId") int componentId,
                                                   @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                                   @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                                                   @ApiParam(value = "State filter", required = false, allowableValues = "all,open,realized") @DefaultValue("all") @QueryParam("state") String stateFilter,
                                                   @ApiParam(value = "Sort", required = false, allowableValues = "date,title,vote,comment,follower,realized") @DefaultValue("date") @QueryParam("sort") List<String> sort) {
            DALFacade dalFacade = null;
            try {
                UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
                long userId = agent.getId();
                String registratorErrors = service.bazaarService.notifyRegistrators(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
                if (registratorErrors != null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registratorErrors);
                }
                Gson gson = new Gson();
                HashMap<String, String> filters = new HashMap<>();
                if (stateFilter != "all") {
                    filters.put("realized", stateFilter);
                }
                List<Pageable.SortField> sortList = new ArrayList<>();
                for (String sortOption : sort) {
                    Pageable.SortDirection direction = Pageable.SortDirection.DEFAULT;
                    if (sortOption.startsWith("+") || sortOption.startsWith(" ")) { // " " is needed because jersey does not pass "+"
                        direction = Pageable.SortDirection.ASC;
                        sortOption = sortOption.substring(1);

                    } else if (sortOption.startsWith("-")) {
                        direction = Pageable.SortDirection.DESC;
                        sortOption = sortOption.substring(1);
                    }
                    Pageable.SortField sortField = new Pageable.SortField(sortOption, direction);
                    sortList.add(sortField);
                }
                PageInfo pageInfo = new PageInfo(page, perPage, filters, sortList);
                Vtor vtor = service.bazaarService.getValidators();
                vtor.validate(pageInfo);
                if (vtor.hasViolations()) {
                    ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
                }
                dalFacade = service.bazaarService.getDBConnection();
                Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
                if (dalFacade.getComponentById(componentId) == null) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "component"));
                }
                Component component = dalFacade.getComponentById(componentId);
                Project project = dalFacade.getProjectById(component.getProjectId());
                if (dalFacade.isComponentPublic(componentId)) {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(project.getId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                    }
                } else {
                    boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(project.getId()), dalFacade);
                    if (!authorized) {
                        ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.component.read"));
                    }
                }
                PaginationResult<RequirementEx> requirementsResult = dalFacade.listRequirementsByComponent(componentId, pageInfo, internalUserId);

                Map<String, String> parameter = new HashMap<>();
                parameter.put("page", String.valueOf(page));
                parameter.put("per_page", String.valueOf(perPage));

                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder = responseBuilder.entity(gson.toJson(requirementsResult.getElements()));
                responseBuilder = service.bazaarService.paginationLinks(responseBuilder, requirementsResult, "components/" + String.valueOf(componentId) + "/requirements", parameter);
                responseBuilder = service.bazaarService.xHeaderFields(responseBuilder, requirementsResult);
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
