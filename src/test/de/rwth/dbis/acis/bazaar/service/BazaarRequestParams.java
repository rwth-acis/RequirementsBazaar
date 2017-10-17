/*
 *
 *  Copyright (c) 2015, RWTH Aachen University.
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

package de.rwth.dbis.acis.bazaar.service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/22/2015
 */
public class BazaarRequestParams {
    private Map<String, String> queryParams;
    private String contentParam;

    public Map<String, String> getQueryParams() {
        if (queryParams == null)
            queryParams = new HashMap<>();
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public String getContentParam() {
        if (contentParam == null)
            return "";
        return contentParam;
    }

    public void setContentParam(String contentParam) {
        this.contentParam = contentParam;
    }
}
