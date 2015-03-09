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

package de.rwth.dbis.acis.bazaar.service.dal.entities;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/17/2015
 */
public enum PrivilegeEnum {
    Create_PROJECT,
    Read_PROJECT,
    Read_PUBLIC_PROJECT,
    Modify_PROJECT,

    Create_COMPONENT,
    Read_COMPONENT,
    Read_PUBLIC_COMPONENT,
    Modify_COMPONENT,

    Create_REQUIREMENT,
    Read_REQUIREMENT,
    Read_PUBLIC_REQUIREMENT,
    Modify_REQUIREMENT,

    Create_COMMENT,
    Read_COMMENT,
    Read_PUBLIC_COMMENT,
    Modify_COMMENT,

    Create_ATTACHMENT,
    Read_ATTACHMENT,
    Read_PUBLIC_ATTACHMENT,
    Modify_ATTACHMENT,

    Create_VOTE, Delete_VOTE,
    Create_FOLLOW, Delete_FOLLOW,

    Create_DEVELOP, Delete_DEVELOP,
}
