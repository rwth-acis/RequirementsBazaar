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
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 10/6/2014
 */
public enum ExceptionLocation {
    REPOSITORY("01", "Repository"),
    TRANSFORMATOR("02", "Transformators"),
    DALFACADE("03", "DAL facade implementation"),
    BAZAARSERVICE("04", "Bazaar service"),
    NETWORK("5", "Network");

    private final String code;
    private final String message;

    public String asCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ExceptionLocation(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
