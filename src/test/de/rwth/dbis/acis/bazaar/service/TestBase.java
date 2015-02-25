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

package de.rwth.dbis.acis.bazaar.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.persistency.MalformedXMLException;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.*;



public abstract class TestBase {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	protected static UserAgent testAgent;
	protected static String testPass;

	private static final String testServiceClass = "de.rwth.dbis.acis.bazaar.service.BazaarService";

	private static final String mainPath = "bazaar/";

	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used
	 * throughout the tests.
	 * 
	 * @throws Exception
	 */
	public static void startServer() throws Exception {

		// start node
		node = LocalNode.newNode();
		node.storeAgent(testAgent);
		node.launch();

		ServiceAgent testService = ServiceAgent.generateNewAgent(
				testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");

		node.registerReceiver(testService);

		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready
		connector.updateServiceList();
		// avoid timing errors: wait for the repository manager to get all
		// services before continuing
		try {
			System.out.println("waiting..");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

    protected static UserAgent getAgent() throws MalformedXMLException, IOException
    {
        testPass = "adamspass";
        return MockAgentFactory.getAdam();
    }

    /**
	 * Called after the tests have finished. Shuts down the server and prints
	 * out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer() throws Exception {

		connector.stop();
		node.shutDown();

		connector = null;
		node = null;

		LocalNode.reset();

		System.out.println("Connector-Log:");
		System.out.println("--------------");

		System.out.println(logStream.toString());

	}

    protected void login(MiniClient c) throws UnsupportedEncodingException {
        c.setLogin(Long.toString(testAgent.getId()), testPass);
    }

    private MiniClient getClient() {
        MiniClient c = new MiniClient();
        c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
        return c;
    }


    protected void assertAccessDenied(ClientResponse response) {
        assertThat(response,is(notNullValue()));
        BazaarException bazaarException = new Gson().fromJson(response.getResponse(), BazaarException.class);
        assertThat(bazaarException.getErrorCode(),is(ErrorCode.AUTHORIZATION));
    }

    public ClientResponse test_addUserToDevelopers(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_addUserToFollowers(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_addVote(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/vote"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_createAttachment(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_createComment(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments"), params.getContentParam(), "application/json", "*/*", new Pair[]{});
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_createComponent(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components"), params.getContentParam(), "application/json", "*/*", new Pair[]{});
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_createProject(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects"), params.getContentParam(), "application/json", "*/*", new Pair[]{});
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_createRequirement(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("POST", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements"), params.getContentParam(), "application/json", "*/*", new Pair[]{});
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_deleteAttachment(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_deleteComment(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_deleteComponent(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_deleteRequirement(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getComments(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getComponent(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getComponents(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}/components"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getProject(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getProjects(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getRequirement(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getRequirementsByComponent(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_getRequirementsByProject(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("projects/{projectId}/requirements"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};


    public ClientResponse test_getUser(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("GET", mainPath + substitutor.replace("users/{userId}"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_removeUserFromDevelopers(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_removeUserFromFollowers(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);

            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

    public ClientResponse test_removeVote(BazaarRequestParams params) {
         MiniClient c = getClient();

        try {
            login(c);
            StrSubstitutor substitutor = new StrSubstitutor(params.getQueryParams(), "{", "}");
            ClientResponse result = c.sendRequest("DELETE", mainPath + substitutor.replace("projects/{projectId}/components/{componentId}/requirements/{requirementId}/vote"), params.getContentParam());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e);
        }
		return null;
};

}
