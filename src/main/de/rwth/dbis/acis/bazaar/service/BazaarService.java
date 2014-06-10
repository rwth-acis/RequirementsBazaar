/*
 * Copyright (c) 2014, RWTH Aachen University.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.rwth.dbis.acis.bazaar.service;

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
		// Otherwise return all the user can see.
		return "[]";
	}

	/**
	 * This method allows to create a new project.
	 * 
	 * @param String
	 *            project a JSON string containing all the parameters.
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
	 * REQUIREMENTS
	 **********************************/

	/**
	 * This method returns the list of requirements for a specific project.
	 * 
	 * @param projectId
	 *            the ID of the project to retrieve requirements for.
	 * @return a list of requirements
	 */
	@GET
	@Path("projects/{projectId}/requirements")
	public String getRequirements(@PathParam("projectId") int projectId,
			@QueryParam(name = "page", defaultValue = "1") int page,
			@QueryParam(name = "per_page", defaultValue = "10") int perPage) {
		return "The requirements for project " + projectId + ".";
	}

	/**
	 * This method allows to create a new requirement.
	 * 
	 * @param projectId
	 *            the ID of the project to create the requirement in.
	 * @param String
	 *            requirement a JSON string containing all the parameters.
	 * @return
	 */
	@POST
	@Path("projects/{projectId}/requirements")
	@Consumes("application/json")
	@Produces("application/json")
	public String createRequirement(@PathParam("projectId") int projectId,
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
	 *            the ID of the project to retrieve requirements for.
	 * @param requirementId
	 *            the ID of the requirement to retrieve.
	 * @return a list of requirements
	 */
	@GET
	@Path("projects/{projectId}/requirements/{requirementId}")
	public String getRequirements(@PathParam("projectId") int projectId,
			@PathParam("requirementId") int requirementId) {
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
}
