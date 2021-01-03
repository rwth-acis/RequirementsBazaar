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

package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Privilege;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.PrivilegeRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.PrivilegeTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.PRIVILEGE;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/18/2015
 */
public class PrivilegeRepositoryImpl extends RepositoryImpl<Privilege, PrivilegeRecord> implements PrivilegeRepository {
    public PrivilegeRepositoryImpl(DSLContext jooq) {
        super(jooq, new PrivilegeTransformer());
    }

    @Override
    public Privilege findByName(String privilegeName) throws BazaarException {
        PrivilegeRecord privilege = null;
        try {
            privilege = jooq.selectFrom(PRIVILEGE).where(PRIVILEGE.NAME.equal(privilegeName)).fetchOne();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return privilege == null ? null : new PrivilegeTransformer().getEntityFromTableRecord(privilege);
    }
}
