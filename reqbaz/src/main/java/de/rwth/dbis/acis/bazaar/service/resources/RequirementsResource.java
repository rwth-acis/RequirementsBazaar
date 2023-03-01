package de.rwth.dbis.acis.bazaar.service.resources;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Tag;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import de.rwth.dbis.acis.bazaar.service.resources.helpers.ResourceHelper;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.util.*;

@Api(value = "requirements", description = "Requirements resource")
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.9.0",
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

    private final L2pLogger logger = L2pLogger.getInstance(RequirementsResource.class.getName());
    private final BazaarService bazaarService;
    private final ResourceHelper resourceHelper;

    public RequirementsResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
        resourceHelper = new ResourceHelper(bazaarService);
    }


    /**
     * This method returns the list of requirements on the server.
     *
     * @param page    page number
     * @param perPage number of requirements by page
     * @param search  search string
     * @param sort    sort order
     * @return Response with list of all requirements
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method returns the list of requirements on the server.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of requirements", response = Requirement.class, responseContainer = "List"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response getRequirements(
            @ApiParam(value = "Page number", required = false) @DefaultValue("0") @QueryParam("page") int page,
            @ApiParam(value = "Elements of requirements by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage,
            @ApiParam(value = "Search filter", required = false) @QueryParam("search") String search,
            @ApiParam(value = "Sort", required = false, allowMultiple = true, allowableValues = "name,date,last_activity,requirement,follower") @DefaultValue("name") @QueryParam("sort") List<String> sort,
            @ApiParam(value = "SortDirection", allowableValues = "ASC,DESC") @QueryParam("sortDirection") String sortDirection,
            @ApiParam(value = "Filter", required = true, allowMultiple = true, allowableValues = "created, following") @QueryParam("filters") List<String> filters,
            @ApiParam(value = "Embed parents", required = true, allowMultiple = true, allowableValues = "project") @QueryParam("embedParents") List<String> embedParents) {

        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            Agent agent = Context.getCurrent().getMainAgent();
            List<Pageable.SortField> sortList = resourceHelper.getSortFieldList(sort, sortDirection);

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            HashMap<String, String> filterMap = resourceHelper.getFilterMap(filters, internalUserId);
            PageInfo pageInfo = new PageInfo(page, perPage, filterMap, sortList, search, null, embedParents);

            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            PaginationResult<Requirement> requirementsResult = null;

            //Might want to change this to allow anonymous agents to get all public requirements?
            if (agent instanceof AnonymousAgent) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirements.read"));
            } else {
                requirementsResult = dalFacade.listAllRequirements(pageInfo, internalUserId);
            }
            //TODO NotificationDispatcher tries to find Requirement with id 0 as additional Object, need to implement logic for multiple
            //bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3,
            //       0, Activity.DataType.REQUIREMENT, internalUserId);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, search, sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(requirementsResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "requirements", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get all requirements", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get all requirements", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
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
    public Response getRequirementsForProject(int projectId, int page, int perPage, String search, String stateFilter, List<String> sort, String sortDirection) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            HashMap<String, String> filters = buildFilters(stateFilter);
            List<Pageable.SortField> sortList = resourceHelper.getSortFieldList(sort, sortDirection);
            PageInfo pageInfo = new PageInfo(page, perPage, filters, sortList, search);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            ensureCatIsInProject(dalFacade.getProjectById(projectId, internalUserId) == null, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "resource"));
            isPublicCheck(dalFacade.isProjectPublic(projectId), internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, projectId, dalFacade, PrivilegeEnum.Read_REQUIREMENT, "error.authorization.category.read");
            PaginationResult<Requirement> requirementsResult = dalFacade.listRequirementsByProject(projectId, pageInfo, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14,
                    projectId, Activity.DataType.PROJECT, internalUserId);

            Map<String, List<String>> parameter = getParameters(page, perPage, search, stateFilter, sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(requirementsResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "projects/" + String.valueOf(projectId) + "/requirements", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get requirements for project " + projectId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get requirements for project " + projectId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of requirements for a specific category.
     *
     * @param categoryId    id of the category under a given project
     * @param page          page number
     * @param perPage       number of projects by page
     * @param search        search string
     * @param stateFilter   requirement state
     * @param sort          parameter to sort by
     * @param sortDirection orientation to sort by
     * @return Response with requirements as a JSON array.
     */
    public Response getRequirementsForCategory(int categoryId, int page, int perPage, String search, String stateFilter, List<String> sort, String sortDirection) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            HashMap<String, String> filters = buildFilters(stateFilter);
            List<Pageable.SortField> sortList = resourceHelper.getSortFieldList(sort, sortDirection);
            PageInfo pageInfo = new PageInfo(page, perPage, filters, sortList, search);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            ensureCatIsInProject(dalFacade.getCategoryById(categoryId, internalUserId) == null, ErrorCode.NOT_FOUND, String.format(Localization.getInstance().getResourceBundle().getString("error.resource.notfound"), "category"));
            Category category = dalFacade.getCategoryById(categoryId, internalUserId);
            Project project = dalFacade.getProjectById(category.getProjectId(), internalUserId);
            isPublicCheck(dalFacade.isCategoryPublic(categoryId), internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, project.getId(), dalFacade, PrivilegeEnum.Read_REQUIREMENT, "error.authorization.category.read");
            PaginationResult<Requirement> requirementsResult = dalFacade.listRequirementsByCategory(categoryId, pageInfo, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24,
                    categoryId, Activity.DataType.CATEGORY, internalUserId);

            Map<String, List<String>> parameter = getParameters(page, perPage, search, stateFilter, sort);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(requirementsResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementsResult, "categories/" + String.valueOf(categoryId) + "/requirements", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementsResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get requirements for category " + categoryId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get requirements for category " + categoryId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    @NotNull
    private Map<String, List<String>> getParameters(int page, int perPage, String search, String stateFilter, List<String> sort) {
        Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, search, sort);
        parameter.put("state", new ArrayList() {{
            add(String.valueOf(stateFilter));
        }});
        return parameter;
    }

    @NotNull
    private static HashMap<String, String> buildFilters(String stateFilter) {
        HashMap<String, String> filters = new HashMap<>();
        if (!Objects.equals(stateFilter, "all")) {
            filters.put("realized", stateFilter);
        }
        return filters;
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            isPublicCheck(dalFacade.isRequirementPublic(requirementId), internalUserId, PrivilegeEnum.Read_PUBLIC_REQUIREMENT, requirement.getProjectId(), dalFacade, PrivilegeEnum.Read_REQUIREMENT, "error.authorization.category.read");
            // add notifications to requirement
            requirement.setGamificationNotifications(bazaarService.getGamificationManager().getUserNotifications(internalUserId));
            return Response.ok(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            resourceHelper.checkRegistrarErrors();
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Create_REQUIREMENT, requirementToCreate.getProjectId(), dalFacade), "error.authorization.requirement.create", true);

            // TODO Refactor this! Such logic should be moved to the constructor
            requirementToCreate.setCreator(dalFacade.getUserById(internalUserId));
            requirementToCreate.setLastUpdatingUser(requirementToCreate.getCreator());
            requirementToCreate.setLastUpdatedDate(OffsetDateTime.now());
            resourceHelper.handleGenericError(bazaarService.validateCreate(requirementToCreate));

            isCategorySameProject(requirementToCreate, dalFacade, internalUserId);

            setTagsForRequirement(requirementToCreate, dalFacade);

            Requirement createdRequirement = dalFacade.createRequirement(requirementToCreate, internalUserId);

            buildAttachments(requirementToCreate, dalFacade, internalUserId, createdRequirement);
            createdRequirement = dalFacade.getRequirementById(createdRequirement.getId(), internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.CREATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26,
                    createdRequirement.getId(), Activity.DataType.REQUIREMENT, internalUserId);

            // trigger Gamification Framework
            bazaarService.getGamificationManager().triggerCreateRequirementAction(internalUserId);
            // add notifications to response
            createdRequirement.setGamificationNotifications(bazaarService.getGamificationManager().getUserNotifications(internalUserId));

            return Response.status(Response.Status.CREATED).entity(createdRequirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Create requirement", logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Create requirement", logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private void buildAttachments(Requirement requirementToCreate, DALFacade dalFacade, Integer internalUserId, Requirement createdRequirement) throws Exception {
        if (requirementToCreate.getAttachments() != null && !requirementToCreate.getAttachments().isEmpty()) {
            for (Attachment attachment : requirementToCreate.getAttachments()) {
                attachment.setCreator(dalFacade.getUserById(internalUserId));
                attachment.setRequirementId(createdRequirement.getId());
                resourceHelper.handleGenericError(bazaarService.validate(attachment));
                dalFacade.createAttachment(attachment);
            }
        }
    }

    private static void setTagsForRequirement(Requirement requirementToCreate, DALFacade dalFacade) throws Exception {
        List<Tag> validatedTags = new ArrayList<>();
        for (Tag tag : requirementToCreate.getTags()) {
            Tag internalTag = dalFacade.getTagById(tag.getId());
            if (internalTag == null) {
                tag.setProjectId(requirementToCreate.getProjectId());
                internalTag = dalFacade.createTag(tag);
            } else
                ensureCatIsInProject(requirementToCreate.getProjectId() != tag.getProjectId(), ErrorCode.VALIDATION, "Tag does not fit with project");
            validatedTags.add(internalTag);
        }
        requirementToCreate.setTags(validatedTags);
    }

    private static void isCategorySameProject(Requirement requirementToCreate, DALFacade dalFacade, Integer internalUserId) throws Exception {
        for (Integer catId : requirementToCreate.getCategories()) {
            Category category = dalFacade.getCategoryById(catId, internalUserId);
            ensureCatIsInProject(requirementToCreate.getProjectId() != category.getProjectId(), ErrorCode.VALIDATION, "Category does not fit with project");
        }
    }

    /**
     * This method updates a specific requirement within a project and category.
     *
     * @param requirementToUpdate requirement as a JSON object
     * @return Response with updated requirement as a JSON object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method updates a requirement.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the updated requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response updateRequirement(@ApiParam(value = "Requirement entity", required = true) Requirement requirementToUpdate) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            resourceHelper.handleGenericError(bazaarService.validate(requirementToUpdate));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);

            // Get internal requirement, so a malicious actor can't provide another project id
            Requirement internalRequirement = dalFacade.getRequirementById(requirementToUpdate.getId(), internalUserId);
            boolean authorized = new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, internalRequirement.getProjectId(), dalFacade);
            ensureCatIsInProject(!authorized && !internalRequirement.isOwner(internalUserId), ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.modify"));
            dalFacade.followRequirement(internalUserId, requirementToUpdate.getId());
            Requirement updatedRequirement = dalFacade.modifyRequirement(requirementToUpdate, internalUserId);

            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UPDATE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27,
                    updatedRequirement.getId(), Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(updatedRequirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Update requirement " + requirementToUpdate.getId(), logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Update requirement " + requirementToUpdate.getId(), logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirementToDelete = dalFacade.getRequirementById(requirementId, internalUserId);
            Project project = dalFacade.getProjectById(requirementToDelete.getProjectId(), internalUserId);
            resourceHelper.checkAuthorization(isUserAuthorizedToDeleteRequirement(dalFacade, internalUserId, project, requirementToDelete), "error.authorization.requirement.delete", true);
            Requirement deletedRequirement = dalFacade.deleteRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.DELETE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(deletedRequirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Delete requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Delete requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * Returns whether the given user is authorized to delete a certain requirement.
     *
     * @param dalFacade            facade for database access
     * @param userId               the user to test authorization for
     * @param projectOfRequirement the project of which the requirement is part of
     * @param requirement          the requirement to delete
     * @return
     * @throws BazaarException
     */
    private boolean isUserAuthorizedToDeleteRequirement(DALFacade dalFacade, Integer userId, Project projectOfRequirement, Requirement requirement) throws BazaarException {
        // If user is author they can delete it (independent of privilege)
        if (requirement.isOwner(userId)) {
            return true;
        }
        // Check whether the suer has the required privilege
        return new AuthorizationManager()
                .isAuthorizedInContext(userId, PrivilegeEnum.Modify_REQUIREMENT, projectOfRequirement.getId(), dalFacade);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade), "error.authorization.vote.create", true);
            Requirement requirement = dalFacade.setUserAsLeadDeveloper(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.LEADDEVELOP, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.status(Response.Status.CREATED).entity(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Leaddevelop requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Leaddevelop requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, dalFacade), "error.authorization.vote.delete", true);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            ensureCatIsInProject(requirement.getLeadDeveloper().getId() != internalUserId, ErrorCode.AUTHORIZATION, "You are not lead developer.");
            requirement = dalFacade.deleteUserAsLeadDeveloper(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UNLEADDEVELOP, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unleaddevelop requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unleaddevelop requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Create_DEVELOP, requirement.getProjectId(), dalFacade), "error.authorization.develop.create", true);
            dalFacade.wantToDevelop(internalUserId, requirementId);
            dalFacade.followRequirement(internalUserId, requirementId);

            // refresh requirement object after modification
            requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.DEVELOP, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.status(Response.Status.CREATED).entity(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Develop requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Develop requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Delete_DEVELOP, requirement.getProjectId(), dalFacade), "error.authorization.develop.delete", true);
            dalFacade.notWantToDevelop(internalUserId, requirementId);

            // refresh requirement object
            requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UNDEVELOP, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_32,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Undevelop requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Undevelop requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method moves the requirement to another category or even another project.
     *
     * @param requirementId id of the requirement
     * @return Response with requirement as a JSON object.
     */
    @POST
    @Path("/{requirementId}/move")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "This method moves the requirement to another category or even another project.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Returns the requirement", response = Requirement.class),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response moveRequirement(@PathParam("requirementId") int requirementId, NewRequirementLocation requirementLocation) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            // ensure requirement exists
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            // ensure target project exists
            Project targetProject = dalFacade.getProjectById(requirementLocation.getProjectId(), internalUserId);
            // ensure target category exists
            Category targetCategory = dalFacade.getCategoryById(requirementLocation.getCategoryId(), internalUserId);
            ensureCatIsInProject(targetCategory.getProjectId() != targetProject.getId(), ErrorCode.VALIDATION, Localization.getInstance().getResourceBundle().getString("error.validation.categoryNotInTargetProject"));
            boolean authorizedToModifyRequirement = new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Modify_REQUIREMENT, requirement.getProjectId(), dalFacade);
            boolean authorizedToCreateRequirementInTargetProject = new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Create_REQUIREMENT, targetProject.getId(), dalFacade);
            ensureCatIsInProject(isTargetCategory(authorizedToModifyRequirement, authorizedToCreateRequirementInTargetProject), ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString("error.authorization.requirement.move"));

            requirement.setProjectId(targetProject.getId());
            requirement.setCategories(Arrays.asList(targetCategory.getId()));
            dalFacade.modifyRequirement(requirement, internalUserId);

            // refresh requirement object
            requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.MOVE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Move requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Move requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private static boolean isTargetCategory(boolean authorizedToModifyRequirement, boolean authorizedToCreateRequirementInTargetProject) {
        return !(authorizedToModifyRequirement && authorizedToCreateRequirementInTargetProject);
    }

    private static void ensureCatIsInProject(boolean targetCategory, ErrorCode validation, String String) throws BazaarException {
        if (targetCategory) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, validation, String);
        }
    }

    @Getter
    public static class NewRequirementLocation {
        private int projectId;
        private int categoryId;
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_FOLLOW, dalFacade), "error.authorization.follow.create", true);
            dalFacade.followRequirement(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.FOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_33,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.status(Response.Status.CREATED).entity(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Follow requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Follow requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_FOLLOW, dalFacade), "error.authorization.follow.delete", true);
            dalFacade.unFollowRequirement(internalUserId, requirementId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UNFOLLOW, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_34,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unfollow requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unfollow requirement " + requirementId, logger);
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
            @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Ok"),
            @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response vote(@PathParam("requirementId") int requirementId,
                         @ApiParam(value = "Vote direction", required = true) Direction direction) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Create_VOTE, dalFacade), "error.authorization.vote.create", true);
            dalFacade.vote(internalUserId, requirementId, direction.isUpVote());
            followUpvote(requirementId, direction, dalFacade, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.VOTE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_35,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            // trigger Gamification Framework
            bazaarService.getGamificationManager().triggerVoteAction(internalUserId);
            return Response.ok().build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unfollow requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unfollow requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private static void followUpvote(int requirementId, Direction direction, DALFacade dalFacade, Integer internalUserId) throws BazaarException {
        if (direction.isUpVote()) {
            dalFacade.followRequirement(internalUserId, requirementId);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorized(internalUserId, PrivilegeEnum.Delete_VOTE, dalFacade), "error.authorization.vote.delete", true);
            dalFacade.unVote(internalUserId, requirementId);
            return Response.noContent().build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unvote requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unvote requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Realize_REQUIREMENT, requirement.getProjectId(), dalFacade), "error.authorization.requirement.realize", true);
            requirement = dalFacade.setRequirementToRealized(requirementId, internalUserId);
            // trigger Gamification Framework
            bazaarService.getGamificationManager().triggerComplete_RequirementAction(internalUserId);
            // add notifications to response
            requirement.setGamificationNotifications(bazaarService.getGamificationManager().getUserNotifications(internalUserId));
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.REALIZE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_37,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.status(Response.Status.CREATED).entity(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Realize requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Realize requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);

            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, PrivilegeEnum.Realize_REQUIREMENT, requirement.getProjectId(), dalFacade), "error.authorization.requirement.realize", true);

            requirement = dalFacade.setRequirementToUnRealized(requirementId, internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.UNREALIZE, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_38,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirement.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Unrealize requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Unrealize requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method allows to retrieve statistics for one requirement.
     *
     * @param requirementId
     * @param since         timestamp since filter, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z
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
            @ApiParam(value = "Since timestamp, ISO-8601 e.g. 2017-12-30 or 2017-12-30T18:30:00Z", required = false) @QueryParam("since") String since) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Calendar sinceCal = resourceHelper.getSinceCal(since);
            Statistic requirementStatistics = dalFacade.getStatisticsForRequirement(internalUserId, requirementId, sinceCal);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_39,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirementStatistics.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get statistics for requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get statistics for requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            PageInfo pageInfo = new PageInfo(page, perPage);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            PaginationResult<User> requirementDevelopers = dalFacade.listDevelopersForRequirement(requirementId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_40,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, null, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(requirementDevelopers.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementDevelopers, "requirements/" + String.valueOf(requirementId) + "/developers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementDevelopers);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get developers for requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get developers for requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            RequirementContributors requirementContributors = dalFacade.listContributorsForRequirement(requirementId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_41,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);
            return Response.ok(requirementContributors.toJSON()).build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get contributors for requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get contributors for requirement " + requirementId, logger);
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
            String userId = resourceHelper.getUserId();
            PageInfo pageInfo = new PageInfo(page, perPage);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            PaginationResult<User> requirementFollowers = dalFacade.listFollowersForRequirement(requirementId, pageInfo);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_42,
                    requirementId, Activity.DataType.REQUIREMENT, internalUserId);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, null, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(requirementFollowers.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, requirementFollowers, "requirements/" + String.valueOf(requirementId) + "/followers", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, requirementFollowers);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get followers for requirement " + requirementId, logger
            );
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get followers for requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    /**
     * This method returns the list of comments for a specific requirement.
     *
     * @param requirementId id of the requirement
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
    public Response getCommentsForRequirement(@PathParam("requirementId") int requirementId) throws Exception {
        CommentsResource commentsResource = new CommentsResource();
        return commentsResource.getCommentsForRequirement(requirementId);
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
                                                 @ApiParam(value = "Elements of comments by page", required = false) @DefaultValue("10") @QueryParam("per_page") int perPage) {
        DALFacade dalFacade = null;
        try {
            String userId = resourceHelper.getUserId();
            PageInfo pageInfo = new PageInfo(page, perPage);
            resourceHelper.handleGenericError(bazaarService.validate(pageInfo));

            dalFacade = bazaarService.getDBConnection();
            //Todo use requirement's projectId for security context, not the one sent from client
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Requirement requirement = dalFacade.getRequirementById(requirementId, internalUserId);
            Project project = dalFacade.getProjectById(requirement.getProjectId(), internalUserId);
            bazaarService.getNotificationDispatcher().dispatchNotification(OffsetDateTime.now(), Activity.ActivityAction.RETRIEVE_CHILD, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_44,
                    requirement.getId(), Activity.DataType.REQUIREMENT, internalUserId);
            isPublicCheck(dalFacade.isRequirementPublic(requirementId), internalUserId, PrivilegeEnum.Read_PUBLIC_COMMENT, project.getId(), dalFacade, PrivilegeEnum.Read_COMMENT, "error.authorization.comment.read");
            PaginationResult<Attachment> attachmentsResult = dalFacade.listAttachmentsByRequirementId(requirementId, pageInfo);

            Map<String, List<String>> parameter = resourceHelper.getSortResponseMap(page, perPage, null, null);

            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder = responseBuilder.entity(attachmentsResult.toJSON());
            responseBuilder = bazaarService.paginationLinks(responseBuilder, attachmentsResult, "requirements/" + String.valueOf(requirementId) + "/attachments", parameter);
            responseBuilder = bazaarService.xHeaderFields(responseBuilder, attachmentsResult);

            return responseBuilder.build();
        } catch (BazaarException bex) {
            return resourceHelper.handleBazaarException(bex, "Get attachments for requirement " + requirementId, logger);
        } catch (Exception ex) {
            return resourceHelper.handleException(ex, "Get attachments for requirement " + requirementId, logger);
        } finally {
            bazaarService.closeDBConnection(dalFacade);
        }
    }

    private void isPublicCheck(boolean dalFacade, Integer internalUserId, PrivilegeEnum read_PUBLIC_COMMENT, int project, DALFacade dalFacade1, PrivilegeEnum read_COMMENT, String key) throws BazaarException {
        if (dalFacade) {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, read_PUBLIC_COMMENT, project, dalFacade1), ResourceHelper.ERROR_ANONYMUS, true);
        } else {
            resourceHelper.checkAuthorization(new AuthorizationManager().isAuthorizedInContext(internalUserId, read_COMMENT, project, dalFacade1), key, true);
        }
    }
}
