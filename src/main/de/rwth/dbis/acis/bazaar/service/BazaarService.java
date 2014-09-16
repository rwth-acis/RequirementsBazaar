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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import de.rwth.dbis.acis.bazaar.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.dal.repositories.ProjectRepository;
import de.rwth.dbis.acis.bazaar.dal.repositories.ProjectRepositoryImpl;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Consumes;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.DELETE;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.PUT;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.QueryParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.security.UserAgent;

/**
 * 
 * Requirements Bazaar LAS2peer Service
 * 
 * This is the main service class of the Requirements Bazaar
 * 
 * @author István Koren
 *
 */
@Path("bazaar")
@Version("0.1")
public class BazaarService extends Service {

    //CONFIG PROPERTIES
    public static final String DEFAULT_DB_USERNAME = "root";
    protected String dbUserName = DEFAULT_DB_USERNAME;

    public static final String DEFAULT_DB_PASSWORD = "";
    protected String dbPassword = DEFAULT_DB_PASSWORD;

    public static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/reqbaz";
    protected String dbUrl = DEFAULT_DB_URL;

    private Connection dbConnection;
    private DSLContext context;

	/**
	 * This method is needed for every RESTful application in LAS2peer.
	 * 
	 * @return the mapping to the REST interface.
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}

    public BazaarService() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dbConnection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);

            context = DSL.using(dbConnection, SQLDialect.MYSQL);
        } catch (Exception e) {
            // For the sake of this tutorial, let's keep exception handling simple
            e.printStackTrace();
        } finally {
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

	/**********************************
	 * PROJECTS
	 **********************************/

	/**
	 * This method returns the list of projects on the server.
	 * 
	 * @return a list of projects.
	 */
	@GET
	@Path("projects")
	public String getProjects(
			@QueryParam(name = "page", defaultValue = "1") int page,
			@QueryParam(name = "per_page", defaultValue = "10") int perPage) {
		// TODO: if the user is not logged in, return all the public projects.
        ProjectRepository repo = new ProjectRepositoryImpl(context);
        List<Project> projects = repo.findAll();

		// Otherwise return all the user can see.
		return "[]";
	}

	/**
	 * This method allows to create a new project.
	 * 
	 * @return
	 */
	@POST
	@Path("projects")
	@Consumes("application/json")
	@Produces("application/json")
	public String createProject(@ContentParam String project) {
		long userId = ((UserAgent) getActiveAgent()).getId();
		// TODO: check whether the current user may create a new project
		// TODO: check whether all required parameters are entered

		return ("{success=false}");
	}

	/**
	 * This method allows to retrieve a certain project.
	 * 
	 * @param projectId
	 *            the id of the project to retrieve
	 * @return the details of a certain project.
	 */
	@GET
	@Path("projects/{projectId}")
	@Produces("application/json")
	public String getProject(@PathParam("projectId") int projectId) {
		// TODO: check whether the current user may request this project
		return "{}";
	}

	/**
	 * Allows to update a certain project.
	 * 
	 * @param projectId
	 *            the id of the project to update.
	 * @return a JSON string containing whether the operation was successful or
	 *         not.
	 */
	@PUT
	@Path("projects/{projectId}")
	@Produces("application/json")
	public String updateProject(@PathParam("projectId") int projectId) {
		// TODO: check if user can change this project
		return "{success=false}";
	}

	@DELETE
	@Path("projects/{projectId}")
	@Produces("application/json")
	public String deleteProject(@PathParam("projectId") int projectId) {
		// TODO: check if user can delete this project
		return "{success=false}";
	}

    /**********************************
     * COMPONENTS
     **********************************/

    /**
     * This method returns the list of components under a given project.
     *
     * @return a list of components.
     */
    @GET
    @Path("projects/{projectId}/components")
    public String getComponents(
            @PathParam("projectId") int projectId,
            @QueryParam(name = "page", defaultValue = "1") int page,
            @QueryParam(name = "per_page", defaultValue = "10") int perPage) {
        // TODO: if the user is not logged in, return all the public projects.
        // Otherwise return all the user can see.
        return "[]";
    }

    /**
     * This method allows to create a new component under a given a project.
     *
     * @return
     */
    @POST
    @Path("projects/{projectId}/components")
    @Consumes("application/json")
    @Produces("application/json")
    public String createComponent(@PathParam("projectId") int projectId, @ContentParam String component) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new project
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method allows to retrieve a certain component under a project.
     *
     * @param projectId
     *            the id of the project
     * @param componentId
     *            the id of the component under a given project
     * @return the details of a certain component.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}")
    @Produces("application/json")
    public String getComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
        // TODO: check whether the current user may request this project
        return "{}";
    }

    /**
     * Allows to update a certain component under a project.
     *
     * @param projectId
     *            the id of the project
     * @param componentId
     *            the id of the component under a given project
     * @return a JSON string containing whether the operation was successful or
     *         not.
     */
    @PUT
    @Path("projects/{projectId}/components/{componentId}")
    @Produces("application/json")
    public String updateComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
        // TODO: check if user can change this project
        return "{success=false}";
    }

    @DELETE
    @Path("projects/{projectId}/components/{componentId}")
    @Produces("application/json")
    public String deleteComponent(@PathParam("projectId") int projectId, @PathParam("componentId") int componentId) {
        // TODO: check if user can delete this project
        return "{success=false}";
    }

	/**********************************
	 * REQUIREMENTS
	 **********************************/

	/**
	 * This method returns the list of requirements for a specific project and component.
	 * 
	 * @param projectId
	 *            the ID of the project to retrieve requirements for.
     * @param componentId
     *            the id of the component under a given project
	 * @return a list of requirements
	 */
	@GET
	@Path("projects/{projectId}/components/{componentId}/requirements")
	@Produces("application/json")
	public String getRequirements(@PathParam("projectId") int projectId,@PathParam("componentId") int componentId,
			@QueryParam(name = "page", defaultValue = "1") int page,
			@QueryParam(name = "per_page", defaultValue = "10") int perPage) {
		return "The requirements for project " + projectId + ".";
	}

	/**
	 * This method allows to create a new requirement.
	 * 
	 * @param projectId
	 *            the ID of the project to create the requirement in.
     * @param componentId
     *            the id of the component under a given project
	 * @return
     *            true if the creation was successful, otherwise false
	 */
	@POST
	@Path("projects/{projectId}/components/{componentId}/requirements")
	@Consumes("application/json")
	@Produces("application/json")
	public String createRequirement(@PathParam("projectId") int projectId,@PathParam("componentId") int componentId,
			@ContentParam String requirement) {
		long userId = ((UserAgent) getActiveAgent()).getId();
		// TODO: check whether the current user may create a new requirement
		// TODO: check whether all required parameters are entered

		return ("{success=false}");
	}

	/**
	 * This method returns a specific requirement within a project.
	 * 
	 * @param projectId
	 *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
	 * @param requirementId
	 *            the ID of the requirement to retrieve.
	 * @return a specific requirement.
	 */
	@GET
	@Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}")
	public String getRequirement(@PathParam("projectId") int projectId,@PathParam("componentId") int componentId,
			@PathParam("requirementId") int requirementId) {
		return "[]";
	}

	/**
	 * This method updates a specific requirement within a project.
	 * 
	 * @param projectId
	 *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
	 * @param requirementId
	 *            the ID of the requirement to update.
	 * @return the updated requirement.
	 */
	@PUT
	@Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}")
	public String updateRequirement(@PathParam("projectId") int projectId,@PathParam("componentId") int componentId,
			@PathParam("requirementId") int requirementId) {
		return "[]";
	}

	/**
	 * This method deletes a specific requirement within a project.
	 * 
	 * @param projectId
	 *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
	 * @param requirementId
	 *            the ID of the requirement to delete.
	 * @return the updated requirement.
	 */
	@DELETE
	@Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}")
	public String deleteRequirement(@PathParam("projectId") int projectId,@PathParam("componentId") int componentId,
			@PathParam("requirementId") int requirementId) {
		// TODO: check if the user may delete this requirement.
		return "[]";
	}


    /**
     * This method set the current user as a lead developer for a given requirement
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/lead_developer")
    public String setLeadDeveloper(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method remove the current user as a lead developer for a given requirement
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/lead_developer")
    @Consumes("application/json")
    @Produces("application/json")
    public String removeLeadDeveloper(@PathParam("projectId") int projectId,
                                   @PathParam("componentId") int componentId,
                                   @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method returns the developers list of a given requirement.
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement to retrieve.
     * @return a list of the developers for the given requirement.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers")
    public String getDevelopers(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId) {
        return "[]";
    }

    /**
     * This method add the current user to the developers list of a given requirement
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @POST
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers")
    public String addUserToDevelopers(@PathParam("projectId") int projectId,
                                   @PathParam("componentId") int componentId,
                                   @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method remove the current user from a developers list of a given requirement
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/developers")
    @Consumes("application/json")
    @Produces("application/json")
    public String removeUserFromDevelopers(@PathParam("projectId") int projectId,
                                      @PathParam("componentId") int componentId,
                                      @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method returns the followers list of a given requirement.
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement to retrieve.
     * @return a list of the followers for the given requirement.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers")
    public String getFollowers(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId) {
        return "[]";
    }

    /**
     * This method add the current user to the followers list of a given requirement
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @POST
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers")
    public String addUserToFollowers(@PathParam("projectId") int projectId,
                                      @PathParam("componentId") int componentId,
                                      @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method removes the current user from a followers list of a given requirement
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/followers")
    @Consumes("application/json")
    @Produces("application/json")
    public String removeUserFromFollowers(@PathParam("projectId") int projectId,
                                           @PathParam("componentId") int componentId,
                                           @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method creates a vote for the given requirement in the name of the current user.
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements")
    @Produces("application/json")
    @Consumes("application/json")
    public String addVote(@PathParam("projectId") int projectId,
                                  @PathParam("componentId") int componentId,
                                  @PathParam("requirementId") int requirementId,
                                  @QueryParam(name = "direction", defaultValue = "up") String direction) {
        return "The requirements for project " + projectId + ".";
    }


    /**
     * This method removes the vote of the given requirement made by the current user
     *
     * @param projectId
     *            the ID of the project of the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement.
     * @return
     */
    @DELETE
    @Path("/projects/{projectId}/components/{componentId}/requirements/{requirementId}/vote")
    @Consumes("application/json")
    @Produces("application/json")
    public String removeVote(@PathParam("projectId") int projectId,
                             @PathParam("componentId") int componentId,
                             @PathParam("requirementId") int requirementId) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**********************************
     * COMMENTS
     **********************************/

    /**
     * This method returns the list of comments for a specific requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @return a list of comments
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments")
    @Produces("application/json")
    public String getComments(@PathParam("projectId") int projectId,
                                  @PathParam("componentId") int componentId,
                                  @PathParam("requirementId") int requirementId,
                                  @QueryParam(name = "page", defaultValue = "1") int page,
                                  @QueryParam(name = "per_page", defaultValue = "10") int perPage) {
        return "The comments for requirement " + projectId + ".";
    }

    /**
     * This method allows to create a new comment fro a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @return
     *            true if the creation was successful, otherwise false
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments")
    @Consumes("application/json")
    @Produces("application/json")
    public String createComment(@PathParam("projectId") int projectId,
                                    @PathParam("componentId") int componentId,
                                    @PathParam("requirementId") int requirementId,
                                    @ContentParam String comment) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method returns a specific comment within a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @param commentId
     *            the ID of the comment, which should be returned.
     * @return a specific comment.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
    public String getComment(@PathParam("projectId") int projectId,
                                 @PathParam("componentId") int componentId,
                                 @PathParam("requirementId") int requirementId,
                                 @PathParam("commentId") int commentId) {
        return "[]";
    }

//    /**
//     * This method updates a specific comment within a requirement.
//     *
//     * @param projectId
//     *            the ID of the project for the requirement.
//     * @param componentId
//     *            the id of the component under a given project
//     * @param requirementId
//     *            the ID of the requirement, which was commented.
//     * @param commentId
//     *            the ID of the comment, which should be returned.
//     * @return the updated requirement.
//     */
//    @PUT
//    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
//    public String updateComment(@PathParam("projectId") int projectId,
//                                @PathParam("componentId") int componentId,
//                                @PathParam("requirementId") int requirementId,
//                                @PathParam("commentId") int commentId) {
//        return "[]";
//    }

    /**
     * This method deletes a specific comment within a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @param commentId
     *            the ID of the comment, which should be deleted.
     * @return the updated requirement.
     */
    @DELETE
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/comments/{commentId}")
    public String deleteRequirement(@PathParam("projectId") int projectId,
                                    @PathParam("componentId") int componentId,
                                    @PathParam("requirementId") int requirementId,
                                    @PathParam("commentId") int commentId) {
        // TODO: check if the user may delete this requirement.
        return "[]";
    }

    /**********************************
     * ATTACHMENTS
     **********************************/
    /**
     * This method returns the list of attachments for a specific requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, whose attachments should be returned.
     * @return a list of attachments
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments")
    @Produces("application/json")
    public String getAttachments(@PathParam("projectId") int projectId,
                              @PathParam("componentId") int componentId,
                              @PathParam("requirementId") int requirementId,
                              @QueryParam(name = "page", defaultValue = "1") int page,
                              @QueryParam(name = "per_page", defaultValue = "10") int perPage) {
        return "The attachments for requirement " + projectId + ".";
    }

    /**
     * This method allows to create a new attachment for a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which is extended by the attachment.
     * @return
     *            true if the creation was successful, otherwise false
     */
    @POST
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments")
    @Consumes("application/json")
    @Produces("application/json")
    public String createAttachment(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId,
                                @ContentParam String attachment) {
        long userId = ((UserAgent) getActiveAgent()).getId();
        // TODO: check whether the current user may create a new requirement
        // TODO: check whether all required parameters are entered

        return ("{success=false}");
    }

    /**
     * This method returns a specific attachment within a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @param attachmentId
     *            the ID of the attachment, which should be returned.
     * @return a specific attachment.
     */
    @GET
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
    public String getAttachment(@PathParam("projectId") int projectId,
                             @PathParam("componentId") int componentId,
                             @PathParam("requirementId") int requirementId,
                             @PathParam("attachmentId") int attachmentId) {
        return "[]";
    }

    /**
     * This method updates a specific attachment within a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @param attachmentId
     *            the ID of the attachment, which should be returned.
     * @return ??.
     */
    @PUT
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
    public String updateAttachment(@PathParam("projectId") int projectId,
                                @PathParam("componentId") int componentId,
                                @PathParam("requirementId") int requirementId,
                                @PathParam("attachmentId") int attachmentId) {
        return "[]";
    }

    /**
     * This method deletes a specific attachment within a requirement.
     *
     * @param projectId
     *            the ID of the project for the requirement.
     * @param componentId
     *            the id of the component under a given project
     * @param requirementId
     *            the ID of the requirement, which was commented.
     * @param attachmentId
     *            the ID of the attachment, which should be deleted.
     * @return the updated requirement.
     */
    @DELETE
    @Path("projects/{projectId}/components/{componentId}/requirements/{requirementId}/attachments/{attachmentId}")
    public String deleteAttachment(@PathParam("projectId") int projectId,
                                    @PathParam("componentId") int componentId,
                                    @PathParam("requirementId") int requirementId,
                                    @PathParam("attachmentId") int attachmentId) {
        // TODO: check if the user may delete this requirement.
        return "[]";
    }

	/**********************************
	 * USERS
	 **********************************/

	/**
	 * Retrieves a list of all users.
	 * 
	 * @return a JSON encoded list of all users.
	 */
	@GET
	@Path("users")
	@Produces("application/json")
	public String getUsers() {
		// TODO: check if the admin user wants to retrieve all users.
		return "[]";
	}

    /**
     * Allows to update a certain project.
     *
     * @param userId
     *            the id of the user to be returned.
     * @return a JSON string of the user data specified by the id.
     */
    @GET
    @Path("users/{userId}")
    @Produces("application/json")
    public String getUser(@PathParam("userId") int userId) {
        // TODO: check whether the current user may request this project
        return "{}";
    }

    /**
     * Allows to update a certain project.
     *
     * @param userId
     *            the id of the user to update.
     * @return a JSON string containing whether the operation was successful or
     *         not.
     */
    @PUT
    @Path("users/{userId}")
    @Produces("application/json")
    public String updateUser(@PathParam("userId") int userId) {
        // TODO: check if user can change this project
        return "{success=false}";
    }
}
