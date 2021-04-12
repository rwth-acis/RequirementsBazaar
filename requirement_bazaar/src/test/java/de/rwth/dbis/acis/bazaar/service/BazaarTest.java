package de.rwth.dbis.acis.bazaar.service;

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
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * Test create a new requirement
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

            String testRequirement = "{\"name\": \"Test Requirements\",  \"description\": \"A test requirement\", \"categories\": [" + testProject.getDefaultCategoryId() + "], \"projectId\":" + testProject.getId() + "}";
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
            assertEquals(1, resp.getAsJsonArray().size());

            JsonObject createdRequirement = resp.getAsJsonArray().get(0).getAsJsonObject();

            assertTrue(createdRequirement.has("lastActivity"));
            assertTrue(isValidISO8601(createdRequirement.get("creationDate").toString().replace("\"", "")));
            assertTrue(isValidISO8601(createdRequirement.get("lastActivity").toString().replace("\"", "")));

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
}
