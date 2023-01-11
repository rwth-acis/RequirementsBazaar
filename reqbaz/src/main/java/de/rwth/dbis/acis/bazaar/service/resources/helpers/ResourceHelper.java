package de.rwth.dbis.acis.bazaar.service.resources.helpers;

import de.rwth.dbis.acis.bazaar.service.BazaarFunction;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.logging.L2pLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.util.*;

public class ResourceHelper {
    private final BazaarService bazaarService;

    public ResourceHelper(BazaarService service) {
        this.bazaarService = service;
    }

    public void checkRegistrarErrors() throws BazaarException {
        String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if (registrarErrors != null) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
        }
    }

    @NotNull
    public Map<String, List<String>> getSortResponseMap(int page, int perPage, String search, List<String> sort) {
        Map<String, List<String>> parameter = new HashMap<>();

        ArrayList<String> pageList = new ArrayList<>();
        pageList.add(String.valueOf(page));
        ArrayList<String> perPageList = new ArrayList<>();
        perPageList.add(String.valueOf(perPage));

        parameter.put("page", pageList);
        parameter.put("per_page", perPageList);
        if (search != null) {
            ArrayList<String> searchList = new ArrayList<>();
            searchList.add(search);
            parameter.put("search", searchList);
        }

        if (sort != null) {
            parameter.put("sort", sort);
        }
        return parameter;
    }

    @NotNull
    public List<Pageable.SortField> getSortFieldList(List<String> sort, String sortDirection) {
        List<Pageable.SortField> sortList = new ArrayList<>();
        for (String sortOption : sort) {
            Pageable.SortField sortField = new Pageable.SortField(sortOption, sortDirection);
            sortList.add(sortField);
        }
        return sortList;
    }

    public void handleGenericError(Set<ConstraintViolation<Object>> violations) throws BazaarException {
        Set<ConstraintViolation<Object>> violationSet = violations;
        if (violationSet.size() > 0) {
            ExceptionHandler.getInstance().handleViolations(violations);
        }
    }

    public void checkViolations(PageInfo pageInfo) throws BazaarException {
        Set<ConstraintViolation<Object>> violations = bazaarService.validate(pageInfo);
        if (violations.size() > 0) {
            ExceptionHandler.getInstance().handleViolations(violations);
        }
    }

    public void checkAuthorization(boolean authorized, String key) throws BazaarException {
        if (!authorized) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.AUTHORIZATION, Localization.getInstance().getResourceBundle().getString(key));
        }
    }

    public String getUserId() throws BazaarException {
        Agent agent = Context.getCurrent().getMainAgent();
        String userId = agent.getIdentifier();
        checkRegistrarErrors();
        return userId;
    }

    @NotNull
    public HashMap<String, String> getFilterMap(List<String> filters, Integer internalUserId) {
        HashMap<String, String> filterMap = new HashMap<>();
        for (String filterOption : filters) {
            filterMap.put(filterOption, internalUserId.toString());
        }
        return filterMap;
    }

    public Response handleException(Exception ex, String key, L2pLogger logger) {
        BazaarException bex = ExceptionHandler.getInstance().convert(ex, ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, ex.getMessage());
        logger.warning(bex.getMessage());
        Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, key);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
    }

    public Response handleBazaarException(BazaarException bex, String key, L2pLogger logger) {
        if (bex.getErrorCode() == ErrorCode.AUTHORIZATION) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } else if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
            return Response.status(Response.Status.NOT_FOUND).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        } else {
            logger.warning(bex.getMessage());
            Context.get().monitorEvent(MonitoringEvent.SERVICE_ERROR, key);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionHandler.getInstance().toJSON(bex)).build();
        }
    }

    @Nullable
    public Calendar getSinceCal(String since) {
        Calendar sinceCal = since == null ? null : DatatypeConverter.parseDateTime(since);
        return sinceCal;
    }

}