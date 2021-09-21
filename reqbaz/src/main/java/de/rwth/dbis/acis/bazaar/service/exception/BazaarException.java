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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;

/**
 * @since 10/6/2014
 */
public class BazaarException extends Exception {

    private final ExceptionLocation location;
    private String message;
    private ErrorCode errorCode;

    protected BazaarException(ExceptionLocation location) {
        this.location = location;
        message = "";
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public int getExceptionCode() {
        return Integer.valueOf(location.asCode() + errorCode.asCode());
    }

    public String getExceptionMessage() {
        return String.format(Localization.getInstance().getResourceBundle().getString("error.unknown_exception"), message, location.getMessage(), errorCode.getMessage(), getExceptionCode());
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

}
