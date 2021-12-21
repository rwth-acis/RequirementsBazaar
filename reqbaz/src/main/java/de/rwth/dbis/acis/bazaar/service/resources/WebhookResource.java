package de.rwth.dbis.acis.bazaar.service.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rwth.dbis.acis.bazaar.service.BazaarFunction;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.logging.L2pLogger;
import io.swagger.annotations.*;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.http.HttpStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Api(value = "webhook")
@SwaggerDefinition(
        info = @Info(
                title = "Requirements Bazaar",
                version = "0.9.0",
                description = "Requirements Bazaar project",
                termsOfService = "http://requirements-bazaar.org",
                contact = @Contact(
                        name = "Requirements Bazaar Dev Team",
                        url = "http://requirements-bazaar.org",
                        email = "info@requirements-bazaar.org"
                ),
                license = @License(
                        name = "Apache2",
                        url = "http://requirements-bazaar.org/license"
                )
        ),
        schemes = SwaggerDefinition.Scheme.HTTPS
)
@Path("/webhook/{projectId}/github")
public class WebhookResource {

    private L2pLogger logger = L2pLogger.getInstance(WebhookResource.class.getName());
    private BazaarService bazaarService;

    private static final char[] HEX = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public WebhookResource() throws Exception {
        bazaarService = (BazaarService) Context.getCurrent().getService();
    }

    /**
     * This method processes the payload sent from GitHub
     *
     * @param projectId  projectId
     * @param requestBody  payload body
     * @param eventHeader   github event header
     * @param signatureHeader github signature header
     * @return Response
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Webhook Endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "Returns OK"),
            @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Not found"),
            @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server problems")
    })
    public Response handleWebhook(@PathParam("projectId") int projectId, String requestBody,
                                  @HeaderParam("X-GitHub-Event")String eventHeader,
                                  @HeaderParam("X-Hub-Signature-256")String signatureHeader
    ){

        String issueHtml = "";
        String issueBody = "";
        String issueNumber = "";
        String issueBodyMsgResult = null;
        String hookId = "";
        String pullRequestHtml = "";
        String releaseHtmlUrl = "";
        String action = "";

        try {
            // parse the payload
            JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
            JSONObject j = (JSONObject) p.parse(requestBody);

            // DB Operations
            DALFacade dalFacade = null;

            String registrarErrors = bazaarService.notifyRegistrars(EnumSet.of(BazaarFunction.VALIDATION,BazaarFunction.USER_FIRST_LOGIN_HANDLING));
            if(registrarErrors != null){
                ExceptionHandler.getInstance().throwException(ExceptionLocation.BAZAARSERVICE, ErrorCode.UNKNOWN, registrarErrors);
            }

            Agent agent = Context.getCurrent().getMainAgent();
            String userId = agent.getIdentifier();

            dalFacade = bazaarService.getDBConnection();
            Integer internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            Project projectToReturn = dalFacade.getProjectById(projectId, internalUserId);

            // Validate the x-hub-signature-256
            if(signatureHeader != null) {
                // get the project secret
                String projectSignatureKey = projectToReturn.getName().replaceAll("\\s","");
                Boolean isSignatureValid = false;
                String HMAC_SHA256_ALGORITHM = "HmacSHA256";
                Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
                SecretKeySpec signingKey = new SecretKeySpec(projectSignatureKey.getBytes(), HMAC_SHA256_ALGORITHM);
                mac.init(signingKey);
                byte[] requestBodyByteArray = mac.doFinal(requestBody.getBytes());

                // excludes the prefix "sha256="
                String expectedHeader = signatureHeader.substring(7);
                String actualRequestedBody = new String(encode(requestBodyByteArray));

                isSignatureValid = expectedHeader.equals(actualRequestedBody);

                if(isSignatureValid) {
                    // get the additionalProperties
                    JsonNode projectAdditionalProperties = projectToReturn.getAdditionalProperties();

                    // transform into ObjectNode for manipulation
                    ObjectNode objectNode = (ObjectNode) projectAdditionalProperties;

                    /*** get the necessary fields from payload and update the additionalProperties ***/

                    // get the event actions
                    action = j.getAsString("action");

                    if (eventHeader.equals("ping")) {
                        hookId = j.getAsString("hook_id");
                        objectNode.put("hook_id", hookId);
                    }

                    if (eventHeader.equals("pull_request")) {
                        JSONObject pullRequestUrl = (JSONObject) j.get("pull_request");
                        pullRequestHtml = pullRequestUrl.getAsString("html_url");

                        objectNode.put("pull_request", action);
                        objectNode.put("pull_request_url", pullRequestHtml);
                    }

                    if (eventHeader.equals("issues")) {
                        JSONObject issueField = (JSONObject) j.get("issue");
                        issueHtml = issueField.getAsString("html_url");
                        issueNumber = issueField.getAsString("number");
                        issueBody = issueField.getAsString("body");
                        String issueBodyMsg = issueBody;
                        issueBodyMsgResult = issueBodyMsg.replaceAll(".*[^\\d](\\d+$)", "$1");

                        int requirementId = Integer.parseInt(issueBodyMsgResult);
                        Requirement requirementToReturn = dalFacade.getRequirementById(requirementId,internalUserId);

                        JsonNode requirementAdditionalProperties = requirementToReturn.getAdditionalProperties();

                        if(requirementAdditionalProperties != null){
                            ObjectNode objectNodeRequirement = (ObjectNode) requirementAdditionalProperties;

                            objectNodeRequirement.put("issue_number", issueNumber);
                            objectNodeRequirement.put("issue_url", issueHtml);
                            objectNodeRequirement.put("issue_status", action);

                            requirementToReturn.setAdditionalProperties(objectNodeRequirement);
                            Requirement updatedRequirement = dalFacade.modifyRequirement(requirementToReturn,internalUserId);

                        }else{
                            ObjectNode createObjectNodeRequirement = JsonNodeFactory.instance.objectNode();
                            createObjectNodeRequirement.put("issue_number", issueNumber);
                            createObjectNodeRequirement.put("issue_url", issueHtml);
                            createObjectNodeRequirement.put("issue_status", action);

                            requirementToReturn.setAdditionalProperties(createObjectNodeRequirement);
                            Requirement updatedRequirement = dalFacade.modifyRequirement(requirementToReturn,internalUserId);
                        }

                    }

                    if (eventHeader.equals("release")) {
                        JSONObject releaseUrl = (JSONObject) j.get("release");
                        releaseHtmlUrl = releaseUrl.getAsString("html_url");

                        objectNode.put("release", releaseHtmlUrl);
                    }

                    // save the updated Project additionalProperties
                    projectToReturn.setAdditionalProperties(objectNode);
                    Project updatedProject = dalFacade.modifyProject(projectToReturn);

                    bazaarService.getNotificationDispatcher().dispatchNotification(LocalDateTime.now(), Activity.ActivityAction.UPDATE,
                            MonitoringEvent.SERVICE_CUSTOM_ERROR_6,updatedProject.getId(), Activity.DataType.PROJECT, internalUserId);

                    // close db connection
                    bazaarService.closeDBConnection(dalFacade);
                }
            }

        } catch (ParseException | BazaarException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().entity(HttpStatus.SC_OK).build();
    }

    /**
     * This method encodes a given input
     *
     * @param bytes input to encode
     * @return encoded result
     */
    private static char[] encode(byte[] bytes) {
        final int amount = bytes.length;
        char[] result = new char[2 * amount];

        int j = 0;
        for (int i = 0; i < amount; i++) {
            result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
            result[j++] = HEX[(0x0F & bytes[i])];
        }
        return result;
    }
}

