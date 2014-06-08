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
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Version;


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
        String result="";
        try {
            result= RESTMapper.getMethodsAsXML(this.getClass());
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
    public String getProjects() {
    	return "[]";
    }
    
    
    /**********************************
     * REQUIREMENTS
     **********************************/
    
    /**
     * This method returns the list of requirements for a specific project.
     * 
     * @param projectId the ID of the project to retrieve requirements for.
     * @return a list of requirements
     */
    @GET
    @Path("projects/{projectId}/requirements")
    public String getRequirements(@PathParam("projectId") String projectId) {
		return "The requirements for project " + projectId + ".";
	}
}
