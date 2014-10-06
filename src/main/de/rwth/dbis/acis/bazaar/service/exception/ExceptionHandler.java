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

package de.rwth.dbis.acis.bazaar.service.exception;

import com.google.gson.Gson;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 10/6/2014
 */
public enum ExceptionHandler {
    INSTANCE;

    // Static getter
    public static ExceptionHandler getInstance() {
        return INSTANCE;
    }

    public BazaarException convert(Exception ex, ExceptionLocation location, ErrorCode errorCode, String message) {
        BazaarException bazaarException = new BazaarException(location);
        bazaarException.setErrorCode(errorCode);
        bazaarException.setMessage(ex.getMessage() + message);
        return bazaarException;
    }

    public void convertAndThrowException(Exception exception, ExceptionLocation location, ErrorCode errorCode) throws BazaarException {
        throw convert(exception, location, errorCode, "");
    }

    public void convertAndThrowException(Exception exception, ExceptionLocation location, ErrorCode errorCode, String message) throws BazaarException {
        throw convert(exception, location, errorCode, message);
    }

    public void convertAndThrowException(BazaarException bazaarEx) throws BazaarException {
        throw bazaarEx;
    }

    public String toJSON(BazaarException exception) {
        return new Gson().toJson(exception);
    }
}
