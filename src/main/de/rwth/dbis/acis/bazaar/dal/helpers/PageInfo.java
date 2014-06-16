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

package de.rwth.dbis.acis.bazaar.dal.helpers;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/15/2014
 */
public class PageInfo implements Pageable {
    private final int offset;
    private final int pageNumber;
    private final int pageSize;
    private final SortDirection sortDirection;

    public PageInfo(int offset, int pageNumber, int pageSize) {
        this(offset,pageNumber,pageSize,SortDirection.DEFAULT);
    }

    public PageInfo(int offset, int pageNumber, int pageSize, SortDirection sortDirection) {
        this.offset = offset;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sortDirection = sortDirection;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }
}
