package de.rwth.dbis.acis.bazaar.service;

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
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;


@Api(value = "categories", description = "Categories resource")
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.6",
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
@Path("/categories")
public class CategoryResource {

    private BazaarService bazaarService;

    private final L2pLogger logger = L2pLogger.getInstance(CategoryResource.class.getName());

    public CategoryResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
    }

    /**
     * This method returns the list of categories under a given project.
     *
     * @param projectId id of the project
     * @param page      page number
     * @param perPage   number of projects by page
     * @param search    search string
     * @param sort      sort order
     * @return Response with categories as a JSON array.
     */
    public Response getCategoriesForProject(int projectId, int page, int perPage, String search, List<String> sort) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
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
            PageInfo pageInfo = new PageInfo(page, perPage, new HashMap<>(), sortList, search);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.getProjectById(projectId, internalUserId) == null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "category"));
            }
            if (dalFacade.isProjectPublic(projectId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_CATEGORY, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_CATEGORY, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.read"));
                }
            }
            PaginationResult<Category> categoriesResult = dalFacade.listCategoriesByProjectId(projectId, pageInfo, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_13,
                    projectId, Activity.DataType.PROJECT, internalUserId);
            Map<String, List<String>> parameter = new HashMap<>();
            parameter.put("page", new ArrayList() {{
                add(String.valueOf(page));
            }});
            parameter.put("per_page", new ArrayList() {{
                add(String.valueOf(perPage));
            }});
            if (search != null) {
                parameter.put("search", new ArrayList() {{
                    add(String.valueOf(search));
                }});
            }
            parameter.put("sort", sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(categoriesResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, categoriesResult, "projects/" + String.valueOf(projectId) + "/categories", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, categoriesResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get categories for project " + projectId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get categories for project " + projectId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve a certain category.
     *
     * @param categoryId id of the category under a given project
     * @return Response with a category as a JSON object.
     */
    @GET
    @Path("/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve a certain category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category categoryToReturn = dalFacade.getCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            if (dalFacade.isCategoryPublic(categoryId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_CATEGORY, String.valueOf(categoryId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_CATEGORY, String.valueOf(categoryId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.read"));
                }
            }
            return Response.ok(categoryToReturn.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get projects");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get projects");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new category.
     *
     * @param categoryToCreate category as a JSON object
     * @return Response with the created project as a JSON object.
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new category under a given a project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response createCategory(@ApiParam(value = "Category entity", required = true) Category categoryToCreate) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            // TODO: check whether the current user may create a new project
            // TODO: check whether all required parameters are entered
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            Vtor vtor = bazaarService.getValidators();
            vtor.useProfiles("create");
            vtor.validate(categoryToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_CATEGORY, String.valueOf(categoryToCreate.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.create"));
            }
            categoryToCreate.setLeader(dalFacade.getUserById(internalUserId));
            Category createdCategory = dalFacade.createCategory(categoryToCreate, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(createdCategory.getCreationDate(), Activity.ActivityAction.CREATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16,
                    createdCategory.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(createdCategory.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Create category");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Create category");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Allows to update a certain category.
     *
     * @param categoryId       id of the category under a given project
     * @param categoryToUpdate updated category as a JSON object
     * @return Response with the updated category as a JSON object.
     */
    @PUT
    @Path("/{categoryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to update a certain category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateCategory(@PathParam("categoryId") int categoryId,
                                   @ApiParam(value = "Category entity", required = true) Category categoryToUpdate) {

        DALFacade dalFacade = null;
        try {
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(categoryToUpdate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_CATEGORY, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.modify"));
            }
            if (categoryToUpdate.getId() != 0 && categoryId != categoryToUpdate.getId()) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
            }
            Category updatedCategory = dalFacade.modifyCategory(categoryToUpdate);
            bazaarService.getNotificationDispatcher().dispatchNotification(updatedCategory.getLastUpdatedDate(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17,
                    updatedCategory.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(updatedCategory.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Update category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Update category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Allows to delete a category.
     *
     * @param categoryId id of the category to delete
     * @return Response with deleted category as a JSON object.
     */
    @DELETE
    @Path("/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method deletes a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response deleteCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category categoryToDelete = dalFacade.getCategoryById(categoryId, internalUserId);
            Project project = dalFacade.getProjectById(categoryToDelete.getProjectId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_CATEGORY, String.valueOf(project.getId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.modify"));
            }
            if (project.getDefaultCategoryId() != null && project.getDefaultCategoryId() == categoryId) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception(),
                        ExceptionLocation.BAZAARSERVICE,
                        ErrorCode.CANNOTDELETE,
                        MessageFormat.format(Localization.getInstance().getResourceBundle().getString("error.authorization.category.delete"), categoryId)
                );
            }
            Category deletedCategory = dalFacade.deleteCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(deletedCategory.getLastUpdatedDate(), Activity.ActivityAction.DELETE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18,
                    deletedCategory.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(deletedCategory.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Delete category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Delete category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method add the current user to the followers list of a given category.
     *
     * @param categoryId id of the category
     * @return Response with category as a JSON object.
     */
    @POST
    @Path("/{categoryId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method add the current user to the followers list of a given category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response followCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.create"));
            }
            dalFacade.followCategory(internalUserId, categoryId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.FOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19,
                    category.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(category.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Follow category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Follow category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method removes the current user from a followers list of a given category.
     *
     * @param categoryId id of the category
     * @return Response with category as a JSON object.
     */
    @DELETE
    @Path("/{categoryId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the current user from a followers list of a given category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the category", response = Category.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unfollowCategory(@PathParam("categoryId") int categoryId) {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.follow.delete"));
            }
            dalFacade.unFollowCategory(internalUserId, categoryId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.UNFOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20,
                    category.getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(category.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Unfollow category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Unfollow category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve statistics for one category.
     *
     * @param categoryId
     * @param since      timestamp since filter, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z
     * @return Response with statistics as a JSON object.
     */
    @GET
    @Path("/{categoryId}/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve statistics for one category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns statistics", response = Statistic.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getStatisticsForCategory(
            @PathParam("categoryId") int categoryId,
            @ApiParam(value = "Since timestamp, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z", required = false) @QueryParam("since") String since) {
        DALFacade dalFacade = null;
        try {
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            Calendar sinceCal = since == null ? null : DatatypeConverter.parseDateTime(since);
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            Statistic categoryStatistics = dalFacade.getStatisticsForCategory(internalUserId, categoryId, sinceCal);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(categoryStatistics.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get statistics for category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get statistics for category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of contributors for a specific category.
     *
     * @param categoryId id of the category
     * @return Response with category contributors
     */
    @GET
    @Path("/{categoryId}/contributors")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of contributors for a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of contributors for a given category", response = CategoryContributors.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getContributorsForCategory(@PathParam("categoryId") int categoryId) throws Exception {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            CategoryContributors categoryContributors = dalFacade.listContributorsForCategory(categoryId);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_22,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(categoryContributors.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get contributors for category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get contributors for category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of followers for a specific category.
     *
     * @param categoryId id of the category
     * @param page       page number
     * @param perPage    number of projects by page
     * @return Response with followers as a JSON array.
     */
    @GET
    @Path("/{categoryId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of followers for a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of followers for a given category", response = User.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getFollowersForCategory(@PathParam("categoryId") int categoryId,
                                            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                            @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        DALFacade dalFacade = null;
        try {
            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            PageInfo pageInfo = new PageInfo(page, perPage);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            PaginationResult<User> categoryFollowers = dalFacade.listFollowersForCategory(categoryId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_23,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);
            Map<String, List<String>> parameter = new HashMap<>();
            parameter.put("page", new ArrayList() {{
                add(String.valueOf(page));
            }});
            parameter.put("per_page", new ArrayList() {{
                add(String.valueOf(perPage));
            }});

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(categoryFollowers.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, categoryFollowers, "categories/" + String.valueOf(categoryId) + "/followers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, categoryFollowers);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get followers for category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, "Get followers for category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of requirements for a specific category.
     *
     * @param categoryId  id of the category under a given project
     * @param page        page number
     * @param perPage     number of projects by page
     * @param search      search string
     * @param stateFilter requirement state
     * @param sort        sort order
     * @return Response with requirements as a JSON array.
     */
    @GET
    @Path("/{categoryId}/requirements")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements for a specific category.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of requirements for a given project", response = Category.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getRequirementsForCategory(@PathParam("categoryId") int categoryId,
                                               @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                               @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
                                               @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
                                               @ApiParam(value = "State filter", required = false, allowableValues = "all,open,realized") @DefaultValue("all") @QueryParam("state") String stateFilter,
                                               @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "date,last_activity,name,vote,comment,follower,realized") @DefaultValue("date") @QueryParam("sort") List<String> sort) throws Exception {
        RequirementsResource requirementsResource = new RequirementsResource();
        return requirementsResource.getRequirementsForCategory(categoryId, page, perPage, search, stateFilter, sort);
    }
}
