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

package de.rwth.dbis.acis.bazaar.service.dal.helpers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @since 6/12/2014
 */
public interface Pageable {
    int getOffset();

    int getPageNumber();

    int getPageSize();

    Map<String, String> getFilters();

    List<SortField> getSorts();

    String getSearch();

    List<Integer> getIds();

    List<String> getEmbed();

    Map<String, Boolean> getOptions();

    enum SortDirection {
        DEFAULT, ASC, DESC
    }

    class SortField {
        String field;
        SortDirection sortDirection;

        public SortField(String field, String sortDirection) {
            this.field = field;

            // Use Object.equals here for no extra null check (else should cover this)
            if (Objects.equals(sortDirection, "ASC")) {
                this.sortDirection = SortDirection.ASC;
            } else if (Objects.equals(sortDirection, "DESC")) {
                this.sortDirection = SortDirection.DESC;
            } else {
                this.sortDirection = SortDirection.DEFAULT;
            }
        }

        public String getField() {
            return field;
        }

        public SortDirection getSortDirection() {
            return sortDirection;
        }
    }
}
