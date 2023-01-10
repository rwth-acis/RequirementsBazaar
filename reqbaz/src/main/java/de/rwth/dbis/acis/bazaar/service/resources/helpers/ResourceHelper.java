package de.rwth.dbis.acis.bazaar.service.resources.helpers;

import de.rwth.dbis.acis.bazaar.service.BazaarFunction;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;

import java.util.EnumSet;

public class ResourceHelper {
    private final BazaarService bazaarService;

    public ResourceHelper(BazaarService service){
        this.bazaarService = service;
    }

    public void checkRegistrarErrors() throws BazaarException {
        String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION, BazaarFunction.USER_FIRST_LOGIN_HANDLING));
        if (registrarErrors != null) {
            ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
        }
    }
}
