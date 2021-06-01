package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;

import static org.junit.Assert.*;

public class BazaarTest extends TestBase {

    private int adamId;

    @Before
    public void adamSetup() {
        MiniClient client = getClient();

        ClientResponse result = client.sendRequest("GET", mainPath + "users/me", "");
        assertEquals(200, result.getHttpCode());

        JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
        System.out.println(response.toString());
        assertTrue(response.isJsonObject());
        adamId = response.get("id").getAsInt();
    }

    /**
     * Test to get the version from the version endpoint
     */
    @Test
    public void testGetVersion() {
        try {
            MiniClient client = getClient();

            ClientResponse result = client.sendRequest("GET", mainPath + "version", "");
            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            System.out.println(response.toString());
            assertTrue(response.isJsonObject());
            assertEquals(response.get("version").getAsString(), BazaarService.class.getName() + "@" + testVersion);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to get the statistics
     */
    @Test
    public void testStatistics() {
        try {
            MiniClient client = getClient();

            ClientResponse result = client.sendRequest("GET", mainPath + "statistics", "");
            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            System.out.println(response.toString());
            assertTrue(response.isJsonObject());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to get a list of projects
     */
    @Test
    public void testCreateProject() {
        try {
            MiniClient client = getClient();

            String testProject = "{\"name\": \"Test Project\",  \"description\": \"A test Project\"}";
            ClientResponse result = client.sendRequest("POST", mainPath + "projects", testProject,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            System.out.println(result.toString());
            System.out.println("Result of 'testPost': " + result.getResponse().trim());
            assertEquals(201, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());

            // gson doesn't remove the quotes
            assertTrue(isValidISO8601(response.get("creationDate").toString().replace("\"", "")));
            assertTrue(response.has("userContext"));

            JsonObject userContext = response.getAsJsonObject("userContext");
            assertTrue(userContext.has("userRole"));
            assertTrue(userContext.has("isFollower"));

            result = client.sendRequest("DELETE", mainPath + "projects/" + response.get("id").getAsString(), "");
            assertEquals(204, result.getHttpCode());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    /**
     * Test to get a list of projects
     */
    @Test
    public void testGetProjects() {
        try {
            MiniClient client = getClient();
            MiniClient adminClient = getAdminClient();

            ClientResponse result = client.sendRequest("GET", mainPath + "projects", "");

            assertEquals(200, result.getHttpCode());
            JsonElement response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonArray());

            // Now for a specific project
            result = client.sendRequest("GET", mainPath + "projects/" + testProject.getId(), "");

            assertEquals(200, result.getHttpCode());
            response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonObject());

            JsonObject jsonObject = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(isValidISO8601(jsonObject.get("creationDate").toString().replace("\"", "")));
            // Normal user has no project role
            assertTrue(jsonObject.has("userContext"));
            JsonObject userContext = jsonObject.getAsJsonObject("userContext");
            assertFalse(userContext.has("userRole"));
            assertTrue(userContext.has("isFollower"));

            // Test with admin
            // Now for a specific project
            result = adminClient.sendRequest("GET", mainPath + "projects/" + testProject.getId(), "");
            assertEquals(200, result.getHttpCode());

            response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonObject());

            jsonObject = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            userContext = jsonObject.getAsJsonObject("userContext");
            assertTrue(userContext.has("userRole"));
            assertTrue(userContext.has("isFollower"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test create a new category
     */
    @Test
    public void testCategories() {
        try {
            MiniClient client = getClient();
            MiniClient adminClient = getAdminClient();

            String testCategory = "{\"name\": \"Test Category\",  \"description\": \"A test category\", \"projectId\":" + testProject.getId() + "}";
            String path = mainPath + "categories";

            // Plebs --> no permission
            ClientResponse result = client.sendRequest("POST", path, testCategory,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            assertEquals(401, result.getHttpCode());

            // Admin and owner --> permission
            result = adminClient.sendRequest("POST", path, testCategory,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            assertEquals(201, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());
            System.out.println(response);

            result = client.sendRequest("GET", mainPath + "projects/" + testProject.getId() + "/categories", "");
            assertEquals(200, result.getHttpCode());

            JsonElement resp = JsonParser.parseString(result.getResponse());
            System.out.println(resp);
            assertTrue(resp.isJsonArray());
            assertEquals(2, resp.getAsJsonArray().size());

            JsonObject createdRequirement = resp.getAsJsonArray().get(1).getAsJsonObject();

            assertTrue(createdRequirement.has("lastActivity"));
            assertTrue(isValidISO8601(createdRequirement.get("creationDate").toString().replace("\"", "")));
            assertTrue(isValidISO8601(createdRequirement.get("lastActivity").toString().replace("\"", "")));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test create a new requirement
     */
    @Test
    public void testRequirements() {
        try {
            MiniClient client = getClient();

            String testRequirement = String.format("{\"name\": \"Test Requirements\",  \"description\": \"A test requirement\", \"categories\": [%s], \"projectId\": \"%s\", \"tags\": [{\"name\": \"CreateTest\", \"colour\": \"#FFFFFF\"}]}", testProject.getDefaultCategoryId(), testProject.getId());
            ClientResponse result = client.sendRequest("POST", mainPath + "requirements", testRequirement,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            assertEquals(201, result.getHttpCode());
            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());
            System.out.println(response);

            result = client.sendRequest("GET", mainPath + "requirements", "");
            assertEquals(200, result.getHttpCode());

            JsonElement resp = JsonParser.parseString(result.getResponse());
            System.out.println(resp);
            assertTrue(resp.isJsonArray());
            assertEquals(2, resp.getAsJsonArray().size());

            JsonObject createdRequirement = resp.getAsJsonArray().get(1).getAsJsonObject();

            assertTrue(createdRequirement.has("lastActivity"));
            assertTrue(isValidISO8601(createdRequirement.get("creationDate").toString().replace("\"", "")));
            assertTrue(isValidISO8601(createdRequirement.get("lastActivity").toString().replace("\"", "")));


            // Test by category
            result = client.sendRequest("GET", mainPath + "categories/" + testProject.getDefaultCategoryId() + "/requirements", "");
            assertEquals(200, result.getHttpCode());

            resp = JsonParser.parseString(result.getResponse());
            System.out.println(resp);
            assertTrue(resp.isJsonArray());
            assertEquals(2, resp.getAsJsonArray().size());

            createdRequirement = resp.getAsJsonArray().get(1).getAsJsonObject();

            assertTrue(createdRequirement.has("lastActivity"));
            assertTrue(isValidISO8601(createdRequirement.get("creationDate").toString().replace("\"", "")));
            assertTrue(isValidISO8601(createdRequirement.get("lastActivity").toString().replace("\"", "")));

            // Test update
            createdRequirement.addProperty("description", "Updated Description");
            result = client.sendRequest("PUT", mainPath + "requirements", createdRequirement.toString(),
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            assertEquals(200, result.getHttpCode());
            response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());
            System.out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to get a list of projects
     */
    @Test
    public void testMembers() {
        try {
            MiniClient client = getClient();
            MiniClient adminClient = getAdminClient();

            String path = mainPath + "projects/" + testProject.getId() + "/members";
            ClientResponse result = client.sendRequest("GET", path, "");
            assertEquals(200, result.getHttpCode());

            JsonElement response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonArray());

            // Now add user role
            String testRequest = String.format("[{\"userId\": %s,  \"role\": \"ProjectMember\"}]", adamId);
            result = adminClient.sendRequest("PUT", path, testRequest, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            assertEquals(204, result.getHttpCode());

            result = client.sendRequest("GET", path, "");
            assertEquals(200, result.getHttpCode());

            response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonArray());

            assertEquals(2, response.getAsJsonArray().size());

            // And now delete again
            String delPath = mainPath + "projects/" + testProject.getId() + "/members/" + adamId;
            result = client.sendRequest("DELETE", delPath, "");
            assertEquals(401, result.getHttpCode());
            result = adminClient.sendRequest("DELETE", delPath, "");
            assertEquals(204, result.getHttpCode());

            result = client.sendRequest("GET", path, "");
            assertEquals(200, result.getHttpCode());

            response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonArray());

            assertEquals(1, response.getAsJsonArray().size());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to create feedback for the reference project
     */
    @Test
    public void testCreateFeedback() {
        try {
            MiniClient client = getClient();

            String testFeedback = String.format("{\"feedback\": \"Crashes all the time\",  \"projectId\": %s}", testProject.getId());
            ClientResponse result = client.sendRequest("POST", mainPath + "feedback", testFeedback,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            System.out.println(result.toString());
            System.out.println("Result of 'testPost': " + result.getResponse().trim());
            assertEquals(201, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());

            // gson doesn't remove the quotes
            assertTrue(isValidISO8601(response.get("creationDate").toString().replace("\"", "")));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to create feedback for the reference project
     * but include an email in the request
     */
    @Test
    public void testCreateFeedbackWithMail() {
        try {
            MiniClient client = getClient();

            String testFeedback = String.format("{\"feedback\": \"Crashes all the time\",  \"projectId\": %s, \"email\": \"%s\"}", testProject.getId(), initUser.getEMail());
            ClientResponse result = client.sendRequest("POST", mainPath + "feedback", testFeedback,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            System.out.println(result.toString());
            System.out.println("Result of 'testPost': " + result.getResponse().trim());
            assertEquals(201, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            Assert.assertTrue(response.isJsonObject());

            // gson doesn't remove the quotes
            Assert.assertTrue(isValidISO8601(response.get("creationDate").toString().replace("\"", "")));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    /**
     * Test to not authorized on feedbacks
     */
    @Test
    public void testGetFeedbacks() {
        try {
            MiniClient client = getClient();
            MiniClient adminClient = getAdminClient();

            String path = mainPath + "projects/" + testProject.getId() + "/feedbacks";
            ClientResponse result = client.sendRequest("GET", path, "");

            System.out.println(result.getResponse());

            assertEquals(401, result.getHttpCode());

            result = adminClient.sendRequest("GET", path, "");

            System.out.println(result.getResponse());

            assertEquals(200, result.getHttpCode());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    /**
     * Test to search for a user
     */
    @Test
    public void testSearchUser() {
        try {
            MiniClient client = getClient();

            ClientResponse result = client.sendRequest("GET", mainPath + "users?search=elek", "");
            JsonElement response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertEquals(200, result.getHttpCode());

            assertTrue(response.isJsonArray());
            assertEquals(1, response.getAsJsonArray().size());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to search for a user
     */
    @Test
    public void testUserJsonView() {
        try {
            MiniClient client = getClient();

            ClientResponse result = client.sendRequest("GET", mainPath + "users/me", "");
            System.out.println(result.toString());
            assertEquals(200, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());
            assertTrue(response.has("email"));
            assertTrue(response.has("emailFollowSubscription"));

            result = client.sendRequest("GET", mainPath + "users/" + initUser.getId(), "");
            System.out.println(result.toString());
            assertEquals(200, result.getHttpCode());

            response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());
            assertFalse(response.has("email"));
            assertFalse(response.has("emailFollowSubscription"));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test to get user dashboard
     */
    @Test
    public void testDashboard() {
        try {
            MiniClient client = getAdminClient();

            ClientResponse result = client.sendRequest("GET", mainPath + "users/me/dashboard", "");
            System.out.println(result.toString());
            assertEquals(200, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            assertTrue(response.isJsonObject());
            assertTrue(response.has("projects"));
            assertTrue(response.has("categories"));
            assertTrue(response.has("requirements"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test creating and updating comments
     */
    @Test
    public void testComments() {
        MiniClient client = getClient();
        String testComment = String.format("{\"message\": \"Crashes all the time\",  \"requirementId\": %s}", testRequirement.getId());
        ClientResponse result = client.sendRequest("POST", mainPath + "comments", testComment,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
        System.out.println(result.toString());
        System.out.println("Result of 'testPost': " + result.getResponse().trim());
        assertEquals(201, result.getHttpCode());

        JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
        Assert.assertTrue(response.isJsonObject());

        assertTrue(response.has("id"));
        assertTrue(isValidISO8601(response.get("creationDate").toString().replace("\"", "")));

        // Test update
        response.addProperty("message", "Updated message");
        result = client.sendRequest("PUT", mainPath + "comments", response.toString(),
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
        assertEquals(200, result.getHttpCode());
    }

    /**
     * Test to get a list of tags on a project
     */
    @Test
    public void testTags() {
        try {
            MiniClient client = getClient();
            MiniClient adminClient = getAdminClient();

            String path = mainPath + "projects/" + testProject.getId() + "/tags";
            ClientResponse result = client.sendRequest("GET", path, "");
            assertEquals(200, result.getHttpCode());

            JsonElement response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonArray());

            // Now create
            String testRequest = "{\"name\": \"Feature\",  \"colour\": \"#00FF00\"}";
            ClientResponse newResult = adminClient.sendRequest("POST", path, testRequest, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            assertEquals(201, newResult.getHttpCode());
            Integer tagId = JsonParser.parseString(newResult.getResponse()).getAsJsonObject().get("id").getAsInt();

            result = client.sendRequest("GET", path, "");
            assertEquals(200, result.getHttpCode());

            response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            assertTrue(response.isJsonArray());
            assertEquals(2, response.getAsJsonArray().size());

            // Now try to add one of these tags and an entirely new one to a requirement
            result = client.sendRequest("GET", mainPath + "requirements/" + testRequirement.getId(), "");
            assertEquals(200, result.getHttpCode());

            JsonElement resp = JsonParser.parseString(result.getResponse());
            JsonObject requirement = resp.getAsJsonObject();

            // Create Tag array and add it to the JsonObject
            JsonArray newTags = JsonParser.parseString(String.format("[{\"id\": %s, \"name\": \"Feature\",  \"colour\": \"#00FF00\"}, {\"name\": \"Test\", \"colour\": \"#0000FF\"}]", tagId)).getAsJsonArray();
            requirement.add("tags", newTags);


            result = adminClient.sendRequest("PUT", mainPath + "requirements", requirement.toString(),
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, new HashMap<>());
            System.out.println(result.getResponse());
            assertEquals(200, result.getHttpCode());

            response = JsonParser.parseString(result.getResponse());
            assertTrue(response.isJsonObject());
            assertEquals(2, response.getAsJsonObject().get("tags").getAsJsonArray().size());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
