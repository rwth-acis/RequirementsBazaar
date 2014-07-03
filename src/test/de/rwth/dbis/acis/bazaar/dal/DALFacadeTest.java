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

package de.rwth.dbis.acis.bazaar.dal;

import de.rwth.dbis.acis.bazaar.dal.entities.*;
import de.rwth.dbis.acis.bazaar.dal.helpers.PageInfo;
import junit.framework.TestCase;
import org.junit.BeforeClass;

import java.util.List;

//TODO Pagination testing
public class DALFacadeTest extends TestCase {

    public static final PageInfo ALL_IN_ONE_PAGE = new PageInfo(0, 100);
    DALFacadeMockImpl mock = new DALFacadeMockImpl();

    //TODO Change mock to implementation
    DALFacade facade = mock;

    @BeforeClass
    public void setUp() {
        //TODO change mock
        //Init DB directly
        mock.userList.add(getInitUser());
        mock.userList.add(User.geBuilder("test@test.hu").id(2).firstName("Alma").lastName("Barack").admin(true).userId(2222).build());
        mock.userList.add(User.geBuilder("test@test.hu").id(3).firstName("Citrom").lastName("Datolya").admin(false).userId(3333).build());

        mock.projectList.add(Project.getBuilder("Project1").description("ProjDesc1").leaderId(1).id(1).visibility(Project.ProjectVisibility.PRIVATE).build());
        mock.projectList.add(Project.getBuilder("Project2").description("ProjDesc2").leaderId(1).id(2).visibility(Project.ProjectVisibility.PRIVATE).build());

        mock.requirementList.add(Requirement.getBuilder("Req1").id(1).description("ReqDesc1").creatorId(1).leadDeveloperId(1).projectId(1).build());
        mock.requirementList.add(Requirement.getBuilder("Req2").id(2).description("ReqDesc2").creatorId(1).leadDeveloperId(1).projectId(2).build());
        mock.requirementList.add(Requirement.getBuilder("Req3").id(3).description("ReqDesc3").creatorId(1).leadDeveloperId(1).projectId(1).build());

        mock.componentList.add(Component.getBuilder("Comp1").id(1).description("CompDesc1").projectId(2).build());

        mock.tagList.add(Tag.getBuilder(1).id(1).requirementId(2).build());

        mock.developerList.add(Developer.getBuilder().id(1).requirementId(2).userId(2).build());
        mock.developerList.add(Developer.getBuilder().id(2).requirementId(2).userId(3).build());

        mock.followerList.add(Follower.getBuilder().id(1).requirementId(2).userId(2).build());
        mock.followerList.add(Follower.getBuilder().id(2).requirementId(2).userId(3).build());

        mock.voteList.add(Vote.getBuilder().id(1).requirementId(2).userId(1).isUpvote(true).build());
        mock.voteList.add(Vote.getBuilder().requirementId(2).userId(3).isUpvote(false).build());

        mock.attachmentList.add(FreeStory.getBuilder().story("RequirementStory").id(1).requirementId(2).creator(2).build());

        mock.commentList.add(Comment.getBuilder("Lorem ipsum dolor sit amet").id(1).requirementId(2).creatorId(2).build());
        mock.commentList.add(Comment.getBuilder("Test message payload").id(2).requirementId(2).creatorId(3).build());


    }

    private User getInitUser() {
        return User.geBuilder("test@test.hu").id(1).firstName("Elek").lastName("Test").admin(true).userId(1111).build();
    }

    public void testCreateUser() throws Exception {
        facade.createUser(User.geBuilder("unittest@test.hu").id(9).firstName("Max").lastName("Zimmermann").admin(false).userId(9999).build());

        User user = facade.getUserById(9);

        assertEquals(9, user.getId());
        assertEquals("unittest@test.hu", user.geteMail());
        assertEquals("Max", user.getFirstName());
        assertEquals("Zimmermann", user.getLastName());
        assertEquals(false, user.isAdmin());
        assertEquals(9999, user.getUserId());


        //TODO How to do this with not mocked stuff??
        mock.userList.remove(user);
    }

    public void testModifyUser() throws Exception {
        //TODO
    }

    public void testGetUserById() throws Exception {
        User user = facade.getUserById(1);

        assertEquals(1, user.getId());
        assertEquals("test@test.hu", user.geteMail());
        assertEquals("Elek", user.getFirstName());
        assertEquals("Test", user.getLastName());
        assertEquals(true, user.isAdmin());
        assertEquals(1111, user.getUserId());
    }

    public void testListProjects() throws Exception {
        List<Project> projects = facade.listProjects(new PageInfo(0, 1));

        assertNotNull(projects);
        assertEquals(1,projects.size());

        Project project = projects.get(0);

        assertEquals(1, project.getId());
        assertEquals("Project1",project.getName());
        assertEquals("ProjDesc1",project.getDescription());
        assertEquals(1,project.getLeaderId());
        assertEquals(Project.ProjectVisibility.PRIVATE, project.getVisibility());
    }


    public void testSearchProjects() throws Exception {
        List<Project> projects = facade.searchProjects("2", ALL_IN_ONE_PAGE);

        assertNotNull(projects);
        assertEquals(1,projects.size());

        Project project = projects.get(0);
        assertEquals(2, project.getId());
        assertEquals("Project2",project.getName());
        assertEquals("ProjDesc2",project.getDescription());
        assertEquals(1,project.getLeaderId());
        assertEquals(Project.ProjectVisibility.PRIVATE, project.getVisibility());

        projects = facade.searchProjects("ProjD", new PageInfo(0, 100));

        assertNotNull(projects);
        assertEquals(2,projects.size());

        assertEquals(1, projects.get(0).getId());
        assertEquals("Project1",projects.get(0).getName());
        assertEquals("ProjDesc1",projects.get(0).getDescription());
        assertEquals(1,projects.get(0).getLeaderId());
        assertEquals(Project.ProjectVisibility.PRIVATE, projects.get(0).getVisibility());

        assertEquals(2, projects.get(1).getId());
        assertEquals("Project2",projects.get(1).getName());
        assertEquals("ProjDesc2",projects.get(1).getDescription());
        assertEquals(1,projects.get(1).getLeaderId());
        assertEquals(Project.ProjectVisibility.PRIVATE, projects.get(1).getVisibility());
    }

    public void testGetProjectById() throws Exception {
        Project project = facade.getProjectById(1);

        assertEquals(1, project.getId());
        assertEquals("Project1",project.getName());
        assertEquals("ProjDesc1",project.getDescription());
        assertEquals(1,project.getLeaderId());
        assertEquals(Project.ProjectVisibility.PRIVATE, project.getVisibility());
    }

    public void testCreateProject() throws Exception {
        Project project = Project.getBuilder("Project3").description("ProjDesc3").leaderId(1).id(3).visibility(Project.ProjectVisibility.PUBLIC).build();

        facade.createProject(project);

        Project projectById = facade.getProjectById(3);

        assertEquals(project.getId(),         projectById.getId()             );
        assertEquals(project.getName(),       projectById.getName()           );
        assertEquals(project.getDescription(),projectById.getDescription()      );
        assertEquals(project.getLeaderId(),   projectById.getLeaderId()      );
        assertEquals(project.getVisibility(), projectById.getVisibility()      );

        //TODO NO DELETE PROJECTS ON FACADE?
        mock.userList.remove(project);
    }

    public void testModifyProject() throws Exception {
        //TODO
    }

    public void testListRequirements() throws Exception {
        List<Requirement> requirements = facade.listRequirements(ALL_IN_ONE_PAGE);

        assertNotNull(requirements);
        assertEquals(3, requirements.size());

        Requirement requirement = requirements.get(2);

        assertEquals(3,requirement.getId());
        assertEquals(1,requirement.getCreatorId());
        assertEquals(1,requirement.getLeadDeveloperId());
        assertEquals(1,requirement.getProjectId());
        assertEquals("Req3",requirement.getTitle());
        assertEquals("ReqDesc3",requirement.getDescription());

        requirements = facade.listRequirements(new PageInfo(1, 2));

        assertNotNull(requirements);
        assertEquals(1, requirements.size());
        assertEquals(3,requirements.get(0).getId());

        requirements = facade.listRequirements(new PageInfo( 0, 1));

        assertNotNull(requirements);
        assertEquals(1, requirements.size());
        assertEquals(3,requirements.get(0).getId());

    }

    public void testListRequirementsByProject() throws Exception {
        List<Requirement> requirements = facade.listRequirementsByProject(2, ALL_IN_ONE_PAGE);

        assertNotNull(requirements);
        assertEquals(1, requirements.size());

        Requirement requirement2 = requirements.get(0);

        assertEquals(2,requirement2.getId());
        assertEquals(1,requirement2.getCreatorId());
        assertEquals(1,requirement2.getLeadDeveloperId());
        assertEquals(2,requirement2.getProjectId());
        assertEquals("Req2",requirement2.getTitle());
        assertEquals("ReqDesc2",requirement2.getDescription());


    }

    public void testListRequirementsByComponent() throws Exception {
        List<Requirement> requirements = facade.listRequirementsByComponent(1, ALL_IN_ONE_PAGE);

        assertNotNull(requirements);
        assertEquals(1, requirements.size());

        Requirement requirement2 = requirements.get(0);

        assertEquals(2,requirement2.getId());
        assertEquals(1,requirement2.getCreatorId());
        assertEquals(1,requirement2.getLeadDeveloperId());
        assertEquals(2,requirement2.getProjectId());
        assertEquals("Req2",requirement2.getTitle());
        assertEquals("ReqDesc2",requirement2.getDescription());

    }

    public void testSearchRequirements() throws Exception {
        List<Requirement> requirements = facade.searchRequirements("desc2", ALL_IN_ONE_PAGE);

        assertNotNull(requirements);
        assertEquals(1, requirements.size());

        Requirement requirement2 = requirements.get(0);

        assertEquals(2,requirement2.getId());
        assertEquals(1,requirement2.getCreatorId());
        assertEquals(1,requirement2.getLeadDeveloperId());
        assertEquals(2,requirement2.getProjectId());
        assertEquals("Req2",requirement2.getTitle());
        assertEquals("ReqDesc2",requirement2.getDescription());
    }

    public void testGetRequirementById() throws Exception {
        RequirementEx requirement = facade.getRequirementById(2);

        assertEquals(2, requirement.getId());

        assertEquals(1, requirement.getCreatorId());
        assertEquals(1,requirement.getCreator().getId());
        assertEquals("Elek",requirement.getCreator().getFirstName());

        assertEquals(1,requirement.getLeadDeveloperId());
        assertEquals(1,requirement.getLeadDeveloper().getId());
        assertEquals("Elek",requirement.getLeadDeveloper().getFirstName());

        assertEquals(2,requirement.getProjectId());
        assertEquals("Req2",requirement.getTitle());
        assertEquals("ReqDesc2",requirement.getDescription());

        List<Attachment> attachments = requirement.getAttachments();
        assertNotNull(attachments);
        assertEquals(1, attachments.size());
        assertEquals(2, attachments.get(0).getCreatorId());

        List<Component> components = requirement.getComponents();
        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(1, components.get(0).getId());
        assertEquals("Comp1",components.get(0).getName());

        List<User> contributors = requirement.getContributors();
        assertNotNull(contributors);
        assertEquals(1,contributors.size());
        assertEquals(2,contributors.get(0).getId());

        List<User> developers = requirement.getDevelopers();
        assertNotNull(developers);
        assertEquals(2,developers.size());
        assertEquals(2,developers.get(0).getId());
        assertEquals(3, developers.get(1).getId());

        List<User> followers = requirement.getFollowers();
        assertNotNull(followers);
        assertEquals(2,followers.size());
        assertEquals(2,followers.get(0).getId());
        assertEquals(3,followers.get(1).getId());

    }

    public void testCreateRequirement() throws Exception {
        Requirement requirement = Requirement.getBuilder("AddedReq1").id(9).description("Test addition").creatorId(2).leadDeveloperId(2).projectId(3).build();

        facade.createRequirement(requirement);

        RequirementEx requirementById = facade.getRequirementById(9);

        assertEquals(requirement.getId(),requirementById.getId());
        assertEquals(requirement.getTitle(),requirementById.getTitle());
        assertEquals(requirement.getDescription(),requirementById.getDescription());
        assertEquals(requirement.getCreatorId(),requirementById.getCreatorId());
        assertEquals(requirement.getLeadDeveloperId(),requirementById.getLeadDeveloperId());
        assertEquals(requirement.getProjectId(),requirementById.getProjectId());

        //TODO
        mock.requirementList.remove(requirement);
    }

    public void testModifyRequirement() throws Exception {
        //TODO
    }

    public void testDeleteRequirementById() throws Exception {
        Requirement requirement = Requirement.getBuilder("AddedReq1").id(9).description("Test addition").creatorId(2).leadDeveloperId(2).projectId(3).build();

        facade.createRequirement(requirement);

        facade.deleteRequirementById(9);

        RequirementEx requirementById = facade.getRequirementById(9);
        assertNull(requirementById);
    }

    public void testListComponentsByProjectId() throws Exception {
        List<Component> components = facade.listComponentsByProjectId(2, ALL_IN_ONE_PAGE);

        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(1,components.get(0).getId());
    }

    public void testCreateComponent() throws Exception {
        Component testComp9 = Component.getBuilder("TestComp9").description("Very testing").id(9).projectId(1).build();

        facade.createComponent(testComp9);

        List<Component> components = facade.listComponentsByProjectId(1, ALL_IN_ONE_PAGE);

        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(9,components.get(0).getId());

        //TODO
        mock.componentList.remove(testComp9);
    }

    public void testModifyComponent() throws Exception {
        //TODO
    }

    public void testDeleteComponentById() throws Exception {
        //TODO
    }

    public void testListCommentsByRequirementId() throws Exception {
        List<Comment> comments = facade.listCommentsByRequirementId(2, ALL_IN_ONE_PAGE);

        assertNotNull(comments);
        assertEquals(2,comments.size());
        assertEquals(1,comments.get(0).getId());
        assertEquals(2,comments.get(1).getId());
    }

    public void testCreateComment() throws Exception {
        Comment testComment = Comment.getBuilder("TestComment").id(9).creatorId(1).requirementId(1).build();

        facade.createComment(testComment);

        List<Comment> comments = facade.listCommentsByRequirementId(1, ALL_IN_ONE_PAGE);

        assertNotNull(comments);
        assertEquals(1,comments.size());
        assertEquals(9,comments.get(0).getId());

        //TODO
        mock.commentList.remove(testComment);
    }

    public void testDeleteCommentById() throws Exception {
        Comment testComment = Comment.getBuilder("TestComment").id(9).creatorId(1).requirementId(1).build();

        facade.createComment(testComment);

        facade.deleteCommentById(9);

        List<Comment> comments = facade.listCommentsByRequirementId(1, ALL_IN_ONE_PAGE);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    public void testFollowUnFollow() throws Exception {
        facade.follow(2, 1);

        RequirementEx requirementById = facade.getRequirementById(1);
        List<User> followers = requirementById.getFollowers();

        assertNotNull(followers);
        assertEquals(1, followers.size());
        assertEquals(2, followers.get(0).getId());

        facade.unFollow(2, 1);

        requirementById = facade.getRequirementById(1);
        followers = requirementById.getFollowers();

        assertEquals(0, followers.size());
    }


    public void testWantToDevelopNotWant() throws Exception {
        facade.wantToDevelop(2, 1);

        RequirementEx requirementById = facade.getRequirementById(1);
        List<User> developers = requirementById.getDevelopers();

        assertNotNull(developers);
        assertEquals(1, developers.size());
        assertEquals(2, developers.get(0).getId());

        facade.notWantToDevelop(2, 1);

        requirementById = facade.getRequirementById(1);
        developers = requirementById.getDevelopers();

        assertEquals(0, developers.size());
    }


    public void testGiveAuthorizationAndRemove() throws Exception {

        boolean isAuthorized = facade.isAuthorized(2, 1);

        assertEquals(false, isAuthorized);

        facade.giveAuthorization(2,1);
        isAuthorized = facade.isAuthorized(2,1);

        assertEquals(true, isAuthorized);

        facade.removeAuthorization(2, 1);
        isAuthorized = facade.isAuthorized(2,1);

        assertEquals(false, isAuthorized);
    }


    public void testAddComponentTagAndRemove() throws Exception {
        facade.addComponentTag(1,1);

        List<Component> components = facade.getRequirementById(1).getComponents();
        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(1, components.get(0).getId());

        facade.removeComponentTag(1,1);

        components = facade.getRequirementById(1).getComponents();
        assertEquals(0,components.size());
    }



    public void testVoteandUnvote() throws Exception {
        boolean hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);

        assertEquals(false, hasUserVotedForRequirement);

        facade.vote(3,1,true);
        hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);

        assertEquals(true, hasUserVotedForRequirement);

        facade.unVote(3,1);
        hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);

        assertEquals(false, hasUserVotedForRequirement);
    }

}