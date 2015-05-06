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

package de.rwth.dbis.acis.bazaar.service.update;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rwth.dbis.acis.bazaar.service.BazaarRequestParams;
import de.rwth.dbis.acis.bazaar.service.TestBase;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/13/2015
 */
public class UpdateTest extends TestBase {
    @BeforeClass
    public static void startServer() throws Exception {
        //testPass = "evespass";
        testAgent = MockAgentFactory.getAnonymous();
        TestBase.startServer();
    }

    @Override
    protected void login(MiniClient c) throws UnsupportedEncodingException {
        //super.login(c);
    }

    @Test
    public void test_modifyProjects() {
        ClientResponse response = super.test_getProjects(new BazaarRequestParams());
        Gson gson = new Gson();
        List<Project> projectList = gson.fromJson(response.getResponse(), new TypeToken<List<Project>>() {
        }.getType());
        assertThat(projectList, not(empty()));
        final Project project = projectList.get(0);

        String mod_title = "AAAAAAAAA";
        int mod_defaultComponentId = 99;
        String mod_Desc = "MOD_DESC";
        Project.ProjectVisibility publicVisibility = Project.ProjectVisibility.PUBLIC;
        int mod_leaderId = 88;
        final Project modifiedProject = Project.getBuilder(mod_title)
                                         .id(project.getId())
                                         .defaultComponentId(mod_defaultComponentId)
                                         .description(mod_Desc)
                                         .visibility(publicVisibility)
                                         .leaderId(mod_leaderId)
                                         .build();
        BazaarRequestParams mod_params = new BazaarRequestParams();
        mod_params.setContentParam(gson.toJson(modifiedProject));
        mod_params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(modifiedProject.getId()));
        }});

        ClientResponse modifyResponse = super.test_modifyProject(mod_params);

        BazaarRequestParams mod_test_params = new BazaarRequestParams();
        mod_test_params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(project.getId()));
        }});
        ClientResponse clientResponse = super.test_getProject(mod_test_params);
        Project projectAfterModification = new Gson().fromJson(clientResponse.getResponse(), Project.class);
        assertThat(projectAfterModification.getId(), is(project.getId()));
        assertThat(projectAfterModification.getName(), is(mod_title));
        assertThat(projectAfterModification.getDefaultComponentId(), is(mod_defaultComponentId));
        assertThat(projectAfterModification.getDescription(), is(mod_Desc));
        assertThat(projectAfterModification.getVisibility(), is(publicVisibility));
        assertThat(projectAfterModification.getLeaderId(), is(mod_leaderId));

        mod_params.setContentParam(gson.toJson(project));
        super.test_modifyProject(mod_params);
    }
}
