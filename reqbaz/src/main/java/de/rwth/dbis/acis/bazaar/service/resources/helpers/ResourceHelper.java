package de.rwth.dbis.acis.bazaar.service.resources.helpers;

import de.rwth.dbis.acis.bazaar.service.BazaarFunction;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jetbrains.annotations.NotNull;

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
        if (search != "") {
            ArrayList<String> searchList = new ArrayList<>();
            searchList.add(search);
            parameter.put("search", searchList);
        }

        if (sort != null) {
            parameter.put("sort", sort);
        }
        return parameter;
    }
}