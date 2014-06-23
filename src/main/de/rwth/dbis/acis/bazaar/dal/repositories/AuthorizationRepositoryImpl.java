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

package de.rwth.dbis.acis.bazaar.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.entities.Authorization;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.AuthorizationsRecord;
import de.rwth.dbis.acis.bazaar.dal.transform.AuthorizationTransformator;
import de.rwth.dbis.acis.bazaar.dal.transform.Transformator;
import org.jooq.DSLContext;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class AuthorizationRepositoryImpl extends RepositoryImpl<Authorization,AuthorizationsRecord> implements AuthorizationRepository {
    /**
     * @param jooq          DSLContext for JOOQ connection
     */
    public AuthorizationRepositoryImpl(DSLContext jooq) {
        super(jooq, new AuthorizationTransformator());
    }
}
