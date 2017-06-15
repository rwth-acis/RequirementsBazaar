package de.rwth.dbis.acis.bazaar.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver;
import i5.las2peer.security.UserAgent;
import io.swagger.annotations.*;
import jodd.vtor.Violation;
import jodd.vtor.Vtor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.util.*;


@Api(value = "requirements", description = "Requirements resource")
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
@Path("/requirements")
public class RequirementsResource {

    private BazaarService bazaarService;

    private final L2pLogger logger = L2pLogger.getInstance(RequirementsResource.class.getName());
    private ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public RequirementsResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
    }

    /**
     * This method returns the list of requirements for a specific project.
     *
     * @param projectId   id of the project to retrieve requirements for
     * @param page        page number
     * @param perPage     number of requirements by page
     * @param search      search string
     * @param stateFilter requirement state
     * @param sort        sort order
     * @return Response with requirements as a JSON array.
     */
    public Response getRequirementsForProject(int projectId, int page, int perPage, String search, String stateFilter, List<String> sort) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
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
            PageInfo pageInfo = new PageInfo(page, perPage, filters, sortList, search);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.getProjectById(projectId, internalUserId) == null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "resource"));
            }
            if (dalFacade.isProjectPublic(projectId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(projectId), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.read"));
                }
            }
            PaginationResult<Requirement> requirementsResult = dalFacade.listRequirementsByProject(projectId, pageInfo, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_14, Context.getCurrent().getMainAgent(), "Get requirements for project " + projectId);

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
            parameter.put("state", new ArrayList() {{
                add(String.valueOf(stateFilter));
            }});
            parameter.put("sort", sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(mapper.writeValueAsString(requirementsResult.getElements()));
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "projects/" + String.valueOf(projectId) + "/requirements", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);
            Response response = responseBuilder.build();

            return response;
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get requirements for project " + projectId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get requirements for project " + projectId);
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
    public Response getRequirementsForCategory(int categoryId, int page, int perPage, String search, String stateFilter, List<String> sort) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
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
            PageInfo pageInfo = new PageInfo(page, perPage, filters, sortList, search);
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(pageInfo);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            if (dalFacade.getCategoryById(categoryId, internalUserId) == null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "category"));
            }
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            Project project = dalFacade.getProjectById(category.getProjectId(), internalUserId);
            if (dalFacade.isCategoryPublic(categoryId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(project.getId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.read"));
                }
            }
            PaginationResult<Requirement> requirementsResult = dalFacade.listRequirementsByCategory(categoryId, pageInfo, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_24, Context.getCurrent().getMainAgent(), "Get requirements for category " + categoryId);

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
            parameter.put("state", new ArrayList() {{
                add(String.valueOf(stateFilter));
            }});
            parameter.put("sort", sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(mapper.writeValueAsString(requirementsResult.getElements()));
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "categories/" + String.valueOf(categoryId) + "/requirements", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);
            Response response = responseBuilder.build();

            return response;
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get requirements for category " + categoryId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get requirements for category " + categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a certain requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_25, Context.getCurrent().getMainAgent(), "Get requirement " + requirementId);
            if (dalFacade.isRequirementPublic(requirementId)) {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.anonymous"));
                }
            } else {
                boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Read_REQUIREMENT, String.valueOf(requirement.getProjectId()), dalFacade);
                if (!authorized) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.category.read"));
                }
            }
            return Response.ok(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to create a new requirement.
     *
     * @param requirementToCreate requirement as a JSON object
     * @return Response with the created requirement as a JSON object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to create a new requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the created requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response createRequirement(@ApiParam(value = "Requirement entity", required = true) Requirement requirementToCreate) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            // TODO: check whether the current user may create a new requirement
            dalFacade = bazaarService.getDBConnection();
            
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            requirementToCreate.setCreator(dalFacade.getUserById(internalUserId));
            Vtor vtor = bazaarService.getValidators();
            vtor.useProfiles("create");
            vtor.validate(requirementToCreate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            vtor.resetProfiles();

            // check if all categories are in the same project
            for (Category category : requirementToCreate.getCategories()) {
                category = dalFacade.getCategoryById(category.getId(), internalUserId);
                if (requirementToCreate.getProjectId() != category.getProjectId()) {
                    ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.VALIDATION, "Category does not fit with project");
                }
            }
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_REQUIREMENT, String.valueOf(requirementToCreate.getProjectId()), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.create"));
            }
            Requirement createdRequirement = dalFacade.createRequirement(requirementToCreate, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_26, Context.getCurrent().getMainAgent(), "Create requirement " + createdRequirement.getId());

            // check if attachments are given
            if (requirementToCreate.getAttachments() != null && !requirementToCreate.getAttachments().isEmpty()) {
                for (Attachment attachment : requirementToCreate.getAttachments()) {
                    attachment.setCreator(dalFacade.getUserById(internalUserId));
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
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, createdRequirement.getCreationDate(), Activity.ActivityAction.CREATE, createdRequirement.getId(),
                    Activity.DataType.REQUIREMENT, createdRequirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(mapper.writeValueAsString(createdRequirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Create requirement");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Create requirement" );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method updates a specific requirement within a project and category.
     *
     * @param requirementId       id of the requirement to update
     * @param requirementToUpdate requirement as a JSON object
     * @return Response with updated requirement as a JSON object.
     */
    @PUT
    @Path("/{requirementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method updates a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateRequirement(@PathParam("requirementId") int requirementId,
                                      @ApiParam(value = "Requirement entity", required = true) Requirement requirementToUpdate) {
        DALFacade dalFacade = null;
        try {
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            Vtor vtor = bazaarService.getValidators();
            vtor.validate(requirementToUpdate);
            if (vtor.hasViolations()) {
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.modify"));
            }
            if (requirementToUpdate.getId() != 0 && requirementId != requirementToUpdate.getId()) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, "Id does not match");
            }
            dalFacade.followRequirement(internalUserId, requirementToUpdate.getId());
            Requirement updatedRequirement = dalFacade.modifyRequirement(requirementToUpdate, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_27, Context.getCurrent().getMainAgent(), "Update requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, updatedRequirement.getLastUpdatedDate(), Activity.ActivityAction.UPDATE, updatedRequirement.getId(),
                    Activity.DataType.REQUIREMENT, updatedRequirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(updatedRequirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Update requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Update requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the deleted requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response deleteRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirementToDelete = dalFacade.getRequirementById(requirementId, internalUserId);
            Project project = dalFacade.getProjectById(requirementToDelete.getProjectId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, Arrays.asList(String.valueOf(project.getId()), String.valueOf(requirementId)), dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.delete"));
            }
            Requirement deletedRequirement = dalFacade.deleteRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_28, Context.getCurrent().getMainAgent(), "Delete requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, deletedRequirement.getLastUpdatedDate(), Activity.ActivityAction.DELETE, deletedRequirement.getId(),
                    Activity.DataType.REQUIREMENT, deletedRequirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(deletedRequirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Delete requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Delete requirement " + requirementId);
            logger.warning(bex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method set the current user as lead developer for a given requirement
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @POST
    @Path("/{requirementId}/leaddevelopers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method set the current user as lead developer for a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response leaddevelopRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.create"));
            }
            Requirement requirement = dalFacade.setUserAsLeadDeveloper(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_29, Context.getCurrent().getMainAgent(), "Leaddevelop requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.LEADDEVELOP, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Leaddevelop requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Leaddevelop requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method removes the current user as lead developer for a given requirement
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @DELETE
    @Path("/{requirementId}/leaddevelopers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the current user as lead developer for a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unleaddevelopRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.delete"));
            }
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            if (requirement.getLeadDeveloper().getId() != internalUserId) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, "You are not lead developer.");
            }
            requirement = dalFacade.deleteUserAsLeadDeveloper(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_30, Context.getCurrent().getMainAgent(), "Unleaddevelop requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.UNLEADDEVELOP, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unleaddevelop requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unleaddevelop requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response developRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_DEVELOP, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.develop.create"));
            }
            dalFacade.wantToDevelop(internalUserId, requirementId);
            dalFacade.followRequirement(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_31, Context.getCurrent().getMainAgent(), "Develop requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.DEVELOP, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Develop requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Develop requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response undevelopRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_DEVELOP, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.develop.delete"));
            }
            dalFacade.notWantToDevelop(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_32, Context.getCurrent().getMainAgent(), "Undevelop requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.UNDEVELOP, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Undevelop requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Undevelop requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response followRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
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
            dalFacade.followRequirement(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_33, Context.getCurrent().getMainAgent(), "Follow requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.FOLLOW, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Follow requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Follow requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unfollowRequirement(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
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
            dalFacade.unFollowRequirement(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_34, Context.getCurrent().getMainAgent(), "Unfollow requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.UNFOLLOW, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unfollow requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unfollow requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
    @Path("/{requirementId}/votes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method creates a vote for the given requirement in the name of the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response vote(@PathParam("requirementId") int requirementId,
                         @ApiParam(value = "Vote direction", allowableValues = "up, down") @DefaultValue("up") @QueryParam("direction") String direction) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            if (!(direction.equals("up") || direction.equals("down"))) {
                Vtor vtor = bazaarService.getValidators();
                vtor.addViolation(new Violation("Direction can only be \"up\" or \"down\"", direction, direction));
                ExceptionHandler.getInstance().handleViolations(vtor.getViolations());
            }
            dalFacade = bazaarService.getDBConnection();
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
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_35, Context.getCurrent().getMainAgent(), "Vote requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.VOTE, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unfollow requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unfollow requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method removes the vote of the given requirement made by the current user.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @DELETE
    @Path("/{requirementId}/votes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the vote of the given requirement made by the current user.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unvote(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_VOTE, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.delete"));
            }
            dalFacade.unVote(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_36, Context.getCurrent().getMainAgent(), "Unvote requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.UNVOTE, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unvote requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unvote requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method set the realized field to now for the given requirement
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @POST
    @Path("/{requirementId}/realized")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method set the realized field to now for a given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response realize(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.create"));
            }
            Requirement requirement = dalFacade.setRequirementToRealized(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_37, Context.getCurrent().getMainAgent(), "Realize requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.REALIZE, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.status(Response.Status.CREATED).entity(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Realize requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Realize requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method removes the realized information for the given requirement
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @DELETE
    @Path("/{requirementId}/realized")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method removes the realized information for the given requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response unrealize(@PathParam("requirementId") int requirementId) {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            boolean authorized = new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade);
            if (!authorized) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.vote.delete"));
            }
            Requirement requirement = dalFacade.setRequirementToUnRealized(requirementId, internalUserId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_38, Context.getCurrent().getMainAgent(), "Unrealize requirement " + requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(bazaarService, new Date(), Activity.ActivityAction.UNREALIZE, requirement.getId(),
                    Activity.DataType.REQUIREMENT, requirement.getCategories().get(0).getId(), Activity.DataType.CATEGORY, internalUserId);
            return Response.ok(mapper.writeValueAsString(requirement)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unrealize requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Unrealize requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve statistics for one requirement.
     *
     * @param requirementId
     * @param since         timestamp since filter
     * @return Response with statistics as a JSON object.
     */
    @GET
    @Path("/{requirementId}/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method allows to retrieve statistics for one requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns statistics", response = Statistic.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getStatisticsForRequirement(
            @PathParam("requirementId") int requirementId,
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
            Statistic statisticsResult = dalFacade.getStatisticsForRequirement(internalUserId, requirementId, sinceCal);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_39, Context.getCurrent().getMainAgent(), "Get statistics for requirement " + requirementId);
            return Response.ok(mapper.writeValueAsString(statisticsResult)).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get statistics for requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get statistics for requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of developers for a specific requirement.
     *
     * @param requirementId id of the requirement
     * @param page          page number
     * @param perPage       number of projects by page
     * @return Response with developers as a JSON array.
     */
    @GET
    @Path("/{requirementId}/developers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of developers for a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of developers for a given requirement", response = User.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getDevelopersForRequirement(@PathParam("requirementId") int requirementId,
                                                @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                                @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
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
            PaginationResult<User> requirementsResult = dalFacade.listDevelopersForRequirement(requirementId, pageInfo);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_40, Context.getCurrent().getMainAgent(), "Get developers for requirement " + requirementId);

            Map<String, List<String>> parameter = new HashMap<>();
            parameter.put("page", new ArrayList() {{
                add(String.valueOf(page));
            }});
            parameter.put("per_page", new ArrayList() {{
                add(String.valueOf(perPage));
            }});

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(mapper.writeValueAsString(requirementsResult.getElements()));
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "requirements/" + String.valueOf(requirementId) + "/developers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);
            Response response = responseBuilder.build();

            return response;
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get developers for requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get developers for requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of contributors for a specific requirement.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement contributors
     */
    @GET
    @Path("/{requirementId}/contributors")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of contributors for a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of contributors for a given requirement", response = RequirementContributors.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getContributorsForRequirement(@PathParam("requirementId") int requirementId) throws Exception {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if (registrarErrors != null) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }
            dalFacade = bazaarService.getDBConnection();
            RequirementContributors requirementContributors = dalFacade.listContributorsForRequirement(requirementId);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_41, Context.getCurrent().getMainAgent(), "Get contributors for requirement " + requirementId);
            return Response.ok(requirementContributors.toJSON()).build();
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get contributors for requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get contributors for requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of followers for a specific requirement.
     *
     * @param requirementId id of the requirement
     * @param page          page number
     * @param perPage       number of projects by page
     * @return Response with followers as a JSON array.
     */
    @GET
    @Path("/{requirementId}/followers")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of followers for a specific requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of followers for a given requirement", response = User.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getFollowersForRequirement(@PathParam("requirementId") int requirementId,
                                                @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                                @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        DALFacade dalFacade = null;
        try {
            UserAgent agent = (UserAgent) Context.getCurrent().getMainAgent();
            long userId = agent.getId();
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
            PaginationResult<User> requirementsResult = dalFacade.listFollowersForRequirement(requirementId, pageInfo);
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_CUSTOM_MESSAGE_42, Context.getCurrent().getMainAgent(), "Get followers for requirement " + requirementId);

            Map<String, List<String>> parameter = new HashMap<>();
            parameter.put("page", new ArrayList() {{
                add(String.valueOf(page));
            }});
            parameter.put("per_page", new ArrayList() {{
                add(String.valueOf(perPage));
            }});
            
            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(mapper.writeValueAsString(requirementsResult.getElements()));
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "requirements/" + String.valueOf(requirementId) + "/followers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);
            Response response = responseBuilder.build();

            return response;
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            } else {
                logger.warning(bex.getMessage());
                L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get followers for requirement " + requirementId);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
            }
        } catch (Exception ex) {
            BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
            logger.warning(bex.getMessage());
            L2pLogger.logEvent(NodeObserver.Event.SERVICE_ERROR, Context.getCurrent().getMainAgent(), "Get followers for requirement " + requirementId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } finally {
            bazaarService.closeDBConnection(dalFacade);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of comments for a given requirement", response = Comment.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getCommentsForRequirement(@PathParam("requirementId") int requirementId,
                                              @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                              @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        CommentsResource commentsResource = new CommentsResource();
        return commentsResource.getCommentsForRequirement(requirementId, page, perPage);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns a list of attachments for a given requirement", response = Attachment.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getAttachmentsForRequirement(@PathParam("requirementId") int requirementId,
                                                 @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
                                                 @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) throws Exception {
        AttachmentsResource attachmentsResource = new AttachmentsResource();
        return attachmentsResource.getAttachmentsForRequirement(requirementId, page, perPage);
    }
}