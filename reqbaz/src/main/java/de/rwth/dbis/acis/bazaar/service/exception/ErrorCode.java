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

/**
 * @since 10/6/2014
 */
public enum ErrorCode {
    UNKNOWN("000", "Unknown, unexpected exception occurred"),
    VALIDATION("001", "Constraint validation failed."),
    CANNOTDELETE("002", "The item cannot be deleted"),
    AUTHORIZATION("003", "This user is not authorized to use this method."),
    DB_COMM("004", "Error during communicating to database. Possibly wrong connection parameters."),
    NOT_FOUND("005", "The item was not found"),
    RMI_ERROR("006", "RMI call did not work");

    private final String code;
    private final String message;

    public String asCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
