package de.rwth.dbis.acis.bazaar.service;

import de.rwth.dbis.acis.bazaar.service.helpers.SetupData;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 */
public abstract class TestBase extends SetupData {

    static final String mainPath = "bazaar/";
    static final String testVersion = "1.0.0";
    private static final String testPass = "adamspass";
    private static final String adminPass = "evespass";
    private static LocalNode node;
    private static WebConnector connector;
    private static ByteArrayOutputStream logStream;
    private static UserAgentImpl testAgent;
    private static UserAgentImpl adminAgent;


    /**
     * Called before a test starts.
     * <p>
     * Sets up the node, initializes connector and adds user agent that can be used throughout the test.
     *
     * @throws Exception
     */
    @Before
    public void startServer() throws Exception {
        // start node
        node = new LocalNodeManager().newNode();
        node.launch();

        // add agent to node
        testAgent = MockAgentFactory.getAdam();
        testAgent.unlock(testPass); // agents must be unlocked in order to be stored
        node.storeAgent(testAgent);

        // add agent to node
        adminAgent = MockAgentFactory.getEve();
        adminAgent.unlock(adminPass); // agents must be unlocked in order to be stored
        node.storeAgent(adminAgent);


        // start service
        // during testing, the specified service version does not matter
        node.startService(new ServiceNameVersion(BazaarService.class.getName(), testVersion), "a pass");

        // start connector
        connector = new WebConnector(true, 0, false, 0); // port 0 means use system defined port
        logStream = new ByteArrayOutputStream();
        connector.setLogStream(new PrintStream(logStream));
        connector.start(node);
    }

    /**
     * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
     *
     * @throws Exception
     */
    @After
    public void shutDownServer() throws Exception {
        if (connector != null) {
            connector.stop();
            connector = null;
        }
        if (node != null) {
            node.shutDown();
            node = null;
        }
        if (logStream != null) {
            System.out.println("Connector-Log:");
            System.out.println("--------------");
            System.out.println(logStream.toString());
            logStream = null;
        }
    }

    MiniClient getClient() {
        MiniClient client = new MiniClient();
        client.setConnectorEndpoint(connector.getHttpEndpoint());

        client.setLogin(testAgent.getIdentifier(), testPass);
        return client;
    }

    MiniClient getAdminClient() {
        MiniClient client = new MiniClient();
        client.setConnectorEndpoint(connector.getHttpEndpoint());

        client.setLogin(adminAgent.getIdentifier(), adminPass);
        return client;
    }

    boolean isValidISO8601(String dateStr) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            dateFormatter.parse(dateStr);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
