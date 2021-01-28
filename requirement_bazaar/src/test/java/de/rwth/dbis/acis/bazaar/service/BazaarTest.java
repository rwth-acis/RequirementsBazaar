package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;

public class BazaarTest extends TestBase {

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
            Assert.assertTrue(response.isJsonObject());
            Assert.assertEquals(response.get("version").getAsString(), BazaarService.class.getName() + "@" + testVersion);

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

            Assert.assertEquals(200, result.getHttpCode());
            JsonElement response = JsonParser.parseString(result.getResponse());
            System.out.println(response.toString());
            Assert.assertTrue(response.isJsonArray());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
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
            Assert.assertEquals(201, result.getHttpCode());

            JsonObject response = JsonParser.parseString(result.getResponse()).getAsJsonObject();
            Assert.assertTrue(response.isJsonObject());
            System.out.println(response.get("creationDate").toString());
            Assert.assertTrue(isValidISO8601(response.get("creationDate").toString()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

}
