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

package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementCategory;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementCategoryMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementCategoryTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_CATEGORY_MAP;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class RequirementCategoryRepositoryImpl extends RepositoryImpl<RequirementCategory, RequirementCategoryMapRecord> implements RequirementCategoryRepository {
    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public RequirementCategoryRepositoryImpl(DSLContext jooq) {
        super(jooq, new RequirementCategoryTransformer());
    }

    @Override
    public void delete(int requirementId, int categoryId) throws BazaarException {
        try {
            jooq.delete(REQUIREMENT_CATEGORY_MAP)
                    .where(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(requirementId).and(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(categoryId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }
}
