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

package de.rwth.dbis.acis.bazaar.service.dal.helpers;

import com.google.gson.Gson;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/5/2015
 */
public class DeleteResponse {
    private boolean success;
    private Integer deletedItemId;
    private String deletedItemText;

    public DeleteResponse(boolean success, Integer deletedItemId, String deletedItemText) {
        this.success = success;
        this.deletedItemId = deletedItemId;
        this.deletedItemText = deletedItemText;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getDeletedItemId() {
        return deletedItemId;
    }

    public void setDeletedItemId(Integer deletedItemId) {
        this.deletedItemId = deletedItemId;
    }

    public String getDeletedItemText() {
        return deletedItemText;
    }

    public void setDeletedItemText(String deletedItemText) {
        this.deletedItemText = deletedItemText;
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
