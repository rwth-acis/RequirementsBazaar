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

package de.rwth.dbis.acis.bazaar.service.security;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.rwth.dbis.acis.bazaar.service.BazaarRequestParams;
import de.rwth.dbis.acis.bazaar.service.TestBase;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import org.hamcrest.Matchers;
import org.hamcrest.Matchers.*;
import org.junit.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/22/2015
 */
public class AnonymUserRightsTest extends TestBase {

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
    public void test_getProjects() {
        ClientResponse response = super.test_getProjects(new BazaarRequestParams());
        assertThat(response, is(notNullValue()));
        List<Project> projectList = new Gson().fromJson(response.getResponse(), new TypeToken<List<Project>>() {
        }.getType());
        assertThat(projectList, hasItem(Matchers.<Project>hasProperty("id", equalTo(1))));
        assertThat(projectList, not(hasItem(Matchers.<Project>hasProperty("id", equalTo(2)))));
    }

    @Test
    public void test_getPublicProject() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
        }});
        ClientResponse response = super.test_getProject(params);
        assertThat(response,is(notNullValue()));
        Project project = new Gson().fromJson(response.getResponse(), Project.class);
        assertThat(project.getId(), is(1));
        assertThat(project.getName(), is("PublicTestProject"));
    }

    @Test
    public void test_getPrivateProject(){
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
        }});
        ClientResponse response = super.test_getProject(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_createComponents( ){
        BazaarRequestParams params = new BazaarRequestParams();
        Component component = Component.getBuilder("TestCreateComponent").id(901).description("hello").projectId(1).build();
        params.setContentParam(new Gson().toJson(component));
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
        }});
        ClientResponse response = super.test_createComponent(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_getPublicProjectComponents(){
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
        }});
        ClientResponse response = super.test_getComponents(params);
        assertThat(response, is(notNullValue()));
        List<Component> projectList = new Gson().fromJson(response.getResponse(), new TypeToken<List<Component>>() {}.getType());
        assertThat(projectList, hasItem(Matchers.<Component>hasProperty("id", equalTo(1))));
        assertThat(projectList, not(hasItem(Matchers.<Component>hasProperty("id", equalTo(2)))));
    }

    @Test
    public void test_getPrivateProjectComponents() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
        }});
        ClientResponse response = super.test_getComponents(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_deleteComponent() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
            put("componentId", String.valueOf(2));
        }});
        ClientResponse response = super.test_deleteComponent(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_createRequirementPublicProject( ){
        BazaarRequestParams params = new BazaarRequestParams();
        Requirement requirement = Requirement.getBuilder("TestCreateReq").id(901).description("hello").projectId(1).build();
        params.setContentParam(new Gson().toJson(requirement));
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
            put("componentId", String.valueOf(1));
        }});
        ClientResponse response = super.test_createRequirement(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_getPublicRequirements() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
        }});
        ClientResponse response = super.test_getRequirementsByProject(params);
        assertThat(response, is(notNullValue()));
        List<Component> projectList = new Gson().fromJson(response.getResponse(), new TypeToken<List<Component>>() {}.getType());
        assertThat(projectList, hasItem(Matchers.<Component>hasProperty("id", equalTo(1))));
    }

    @Test
    public void test_getPrivateRequirements() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
        }});
        ClientResponse response = super.test_getRequirementsByProject(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_getPublicRequirementsByComponent() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
            put("componentId", String.valueOf(1));
        }});
        ClientResponse response = super.test_getRequirementsByComponent(params);
        assertThat(response, is(notNullValue()));
        List<Component> projectList = new Gson().fromJson(response.getResponse(), new TypeToken<List<Component>>() {}.getType());
        assertThat(projectList, hasItem(Matchers.<Component>hasProperty("id", equalTo(1))));
    }

    @Test
    public void test_getPrivateRequirementsByComponent() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
            put("componentId", String.valueOf(2));
        }});
        ClientResponse response = super.test_getRequirementsByComponent(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_getPublicRequirement() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
            put("componentId", String.valueOf(1));
            put("requirementId", String.valueOf(1));
        }});
        ClientResponse response = super.test_getRequirement(params);
        assertThat(response,is(notNullValue()));
        RequirementEx requirementEx = new Gson().fromJson(response.getResponse(), RequirementEx.class);
        assertThat(requirementEx.getId(), is(1));
        assertThat(requirementEx.getName(), is("PublicRequirement"));
    }

    @Test
    public void test_getPrivateRequirement() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
            put("componentId", String.valueOf(2));
            put("requirementId", String.valueOf(2));
        }});
        ClientResponse response = super.test_getRequirement(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_deleteRequirement() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
            put("componentId", String.valueOf(1));
            put("requirementId", String.valueOf(1));
        }});
        ClientResponse response = super.test_deleteRequirement(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_createComment( ){
        BazaarRequestParams params = new BazaarRequestParams();
        Comment comment = Comment.getBuilder("TestCommentText").id(901).requirementId(1).build();
        params.setContentParam(new Gson().toJson(comment));
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
            put("componentId", String.valueOf(1));
            put("requirementId", String.valueOf(1));
        }});
        ClientResponse response = super.test_createComment(params);
        assertAccessDenied(response);
    }

    @Test
    public void test_getPublicComments() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(1));
            put("componentId", String.valueOf(1));
            put("requirementId", String.valueOf(1));
        }});
        ClientResponse response = super.test_getComments(params);
        assertThat(response, is(notNullValue()));
        List<Comment> comments = new Gson().fromJson(response.getResponse(), new TypeToken<List<Comment>>() {}.getType());
        assertThat(comments, hasItem(Matchers.<Comment>hasProperty("id", equalTo(1))));
    }

    @Test
    public void test_getPrivateComments() {
        BazaarRequestParams params = new BazaarRequestParams();
        params.setQueryParams(new HashMap<String, String>() {{
            put("projectId", String.valueOf(2));
            put("componentId", String.valueOf(2));
            put("requirementId", String.valueOf(2));
        }});
        ClientResponse response = super.test_getComments(params);
        assertAccessDenied(response);
    }
}
