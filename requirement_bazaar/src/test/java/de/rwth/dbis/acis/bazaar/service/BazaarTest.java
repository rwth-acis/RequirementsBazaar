package de.rwth.dbis.acis.bazaar.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import org.junit.Assert;
import org.junit.Test;

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

            Assert.assertTrue(response.isJsonObject());
            Assert.assertEquals(response.get("version").getAsString(), BazaarService.class.getName() + "@" + testVersion);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

}
