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

package de.rwth.dbis.acis.bazaar.service.dal;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ProjectRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.helpers.SetupData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.*;

//TODO Pagination testing
public class DALFacadeTest extends SetupData {

    public static final PageInfo ALL_IN_ONE_PAGE = new PageInfo(0, 100);

    @Test
    public void testCreateUser() {
        try {
            facade.createUser(User.builder().eMail("unittest@test.hu").firstName("Max").lastName("Zimmermann").las2peerId("9999").userName("MaxZim").personalizationEnabled(false).emailFollowSubscription(false).emailLeadSubscription(false).build());

            Integer userId = facade.getUserIdByLAS2PeerId("9999");
            User user = facade.getUserById(userId);

            assertEquals("unittest@test.hu", user.getEMail());
            assertEquals("Max", user.getFirstName());
            assertEquals("Zimmermann", user.getLastName());
            assertEquals("9999", user.getLas2peerId());

            jooq.delete(de.rwth.dbis.acis.bazaar.dal.jooq.tables.User.USER).where(de.rwth.dbis.acis.bazaar.dal.jooq.tables.User.USER.ID.eq(userId)).execute();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testGetUserById() throws Exception {
        Integer userId = facade.getUserIdByLAS2PeerId(eveId);
        User user = facade.getUserById(userId);

        assertEquals("test@test.hu", user.getEMail());
        assertEquals("Elek", user.getFirstName());
        assertEquals("Test", user.getLastName());
        assertEquals(eveId, user.getLas2peerId());
    }

    @Test
    public void testCreateGetProject() throws Exception {
        Project project = Project.builder().name("Project3  \uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66").description("An \uD83D\uDE00awesome \uD83D\uDE03string with a few \uD83D\uDE09emojis!").id(1).leader(initUser).visibility(true).isFollower(false).build();

        facade.createProject(project, initUser.getId());

        // This can't work reliably without fetching by name
        // Id is set by the database but never returned
        ProjectRecord projectRecord = jooq.selectFrom(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT).where(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT.NAME.equal("Project3  \uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66")).fetchOne();
        assertNotNull(projectRecord);
        Integer projectID = projectRecord.getId(); // .get(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT.ID);

        Project projectById = facade.getProjectById(projectID, initUser.getId());

        assertEquals(project.getName(), projectById.getName());
        assertEquals(project.getDescription(), projectById.getDescription());
        assertEquals("An 😀awesome 😃string with a few 😉emojis!", projectById.getDescription());
        assertEquals(project.getLeader().getId(), projectById.getLeader().getId());
        assertEquals(project.getVisibility(), projectById.getVisibility());
        assertTrue(projectById.isOwner(initUser));
        assertEquals(ProjectRole.ProjectAdmin, projectById.getProjectRole());

        // Now check if this can also be found as a public project
        PaginationResult<Project> projectsPage = facade.listPublicProjects(new PageInfo(0, 10), initUser.getId());

        List<Project> projects = projectsPage.getElements();

        assertNotNull(projects);

        Project proj = null;
        for (Project pr : projects) {
            assertTrue(pr.getVisibility());

            if (pr.getId() == projectById.getId()) proj = pr;
        }

        assertNotNull(proj);

        assertEquals(projectById.getId(), proj.getId());
        assertEquals(projectById.getName(), proj.getName());
        assertEquals(projectById.getDescription(), proj.getDescription());
        assertEquals(projectById.getLeader().getId(), proj.getLeader().getId());
        assertEquals(projectById.getVisibility(), proj.getVisibility());


        jooq.delete(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT).where(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT.ID.equal(project.getId())).execute();
    }

    @Test
    public void testGetProjectMembers() throws Exception {
        PaginationResult<ProjectMember> membersPage = facade.getProjectMembers(testProject.getId(), new PageInfo(0, 100));

        List<ProjectMember> members = membersPage.getElements();

        assertNotNull(members);
        assertEquals(1, members.size());
    }

    @Test
    public void testListPublicProjects() throws Exception {

    }

    @Test
    public void testFeedback() {
        try {
            Feedback createdFeedback = facade.createFeedback(Feedback.builder().projectId(testProject.getId()).feedback("Crashes all the time.").build());

            assertNull(createdFeedback.getEMail());
            assertNotNull(createdFeedback.getCreationDate());
            assertEquals("Crashes all the time.", createdFeedback.getFeedback());
            assertEquals(testProject.getId(), createdFeedback.getProjectId());

            Feedback createdFeedbackWithMail = facade.createFeedback(Feedback.builder().projectId(testProject.getId()).feedback("Crashes all the time.").eMail(initUser.getEMail()).build());
            assertEquals(initUser.getEMail(), createdFeedbackWithMail.getEMail());

            PaginationResult<Feedback> feedbackPage = facade.getFeedbackByProject(testProject.getId(), new PageInfo(0, 20));

            List<Feedback> feedbackByProject = feedbackPage.getElements();

            assertNotNull(feedbackByProject);
            assertEquals(2, feedbackByProject.size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }

    }

    /* Doesn't work, searching breaks
    public void testModifyProject() throws Exception {
        //TODO
    }

    public void testListRequirements() throws Exception {
        List<Requirement> requirements = facade.listRequirements(ALL_IN_ONE_PAGE);

        assertNotNull(requirements);
        assertEquals(3, requirements.size());

        Requirement requirement = requirements.get(2);

        assertEquals(1,requirement.getId());
        assertEquals(1,requirement.getCreator().getId());
        assertEquals(1,requirement.getLeadDeveloper().getId());
        assertEquals(1,requirement.getProjectId());
        assertEquals("Req1",requirement.getName());
        assertEquals("ReqDesc1",requirement.getDescription());

        requirements = facade.listRequirements(new PageInfo(1, 2));

        assertNotNull(requirements);
        assertEquals(1, requirements.size());
        assertEquals(1,requirements.get(0).getId());

        requirements = facade.listRequirements(new PageInfo(0, 1));

        assertNotNull(requirements);
        assertEquals(1, requirements.size());
        assertEquals(3,requirements.get(0).getId());
    }

    public void testListRequirementsByProject() throws Exception {
        List<Requirement> requirements = (List<Requirement>) facade.listRequirementsByProject(2, ALL_IN_ONE_PAGE, 1);

        assertNotNull(requirements);
        assertEquals(1, requirements.size());

        Requirement requirement2 = requirements.get(0);

        assertEquals(2,requirement2.getId());
        assertEquals(1,requirement2.getCreator().getId());
        assertEquals(1,requirement2.getLeadDeveloper().getId());
        assertEquals(2,requirement2.getProjectId());
        assertEquals("Req2",requirement2.getName());
        assertEquals("ReqDesc2",requirement2.getDescription());


    }

    public void testGetRequirementById() throws Exception {
        Requirement requirement = facade.getRequirementById(2, 1);

        assertEquals(2, requirement.getId());

        assertEquals(initUser, requirement.getCreator());
        assertEquals(initUser,requirement.getCreator());
        assertEquals(initUser.getFirstName(),requirement.getCreator().getFirstName());

        assertEquals(initUser,requirement.getLeadDeveloper());
        assertEquals(1,requirement.getLeadDeveloper().getId());
        assertEquals("Elek",requirement.getLeadDeveloper().getFirstName());

        assertEquals(2,requirement.getProjectId());
        assertEquals("Req2",requirement.getName());
        assertEquals("ReqDesc2",requirement.getDescription());

        List<Attachment> attachments = requirement.getAttachments();
        assertNotNull(attachments);
        assertEquals(1, attachments.size());
        assertEquals(2, attachments.get(0).getCreator().getId());

        List<Category> components = requirement.getCategories();
        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(1, components.get(0).getId());
        assertEquals("Comp1",components.get(0).getName());
    }

    public void testCreateRequirement() throws Exception {
        int createdRequirementId = 9;
        try {
            Requirement requirement = Requirement.getBuilder("AddedReq1").id(createdRequirementId).description("Test addition").creator(initUser).leadDeveloper(initUser).projectId(3).build();

            facade.createRequirement(requirement, initUser.getId());

            Requirement requirementById = facade.getRequirementById(createdRequirementId, 1);

            assertEquals(requirement.getId(), requirementById.getId());
            assertEquals(requirement.getName(), requirementById.getName());
            assertEquals(requirement.getDescription(), requirementById.getDescription());
            assertEquals(requirement.getCreator(), requirementById.getCreator());
            assertEquals(requirement.getLeadDeveloper(), requirementById.getLeadDeveloper());
            assertEquals(requirement.getProjectId(), requirementById.getProjectId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }
    }

    public void testModifyRequirement() throws Exception {
        //TODO
    }

    public void testDeleteRequirementById() throws Exception {
        Requirement requirement = Requirement.getBuilder("AddedReq1").id(9).description("Test addition").creator(initUser).leadDeveloper(initUser).projectId(3).build();

        facade.createRequirement(requirement, initUser.getId());

        facade.deleteRequirementById(9, initUser.getId());

        try {
            Requirement requirementById = facade.getRequirementById(9, 1);
        }
        catch (Exception ex){
            assertEquals("No class de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord found with id: 9", ex.getMessage());
        }
    }

    public void testListComponentsByProjectId() throws Exception {
        List<Category> components = (List<Category>) facade.listCategoriesByProjectId(2, ALL_IN_ONE_PAGE, 1);

        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(1,components.get(0).getId());
    }

    public void testCreateCategory() throws Exception {
        int createdComponentId = 9;
        Category testComp9 = Category.getBuilder("TestComp9").description("Very testing").id(createdComponentId).projectId(1).leader(initUser).build();

        facade.createCategory(testComp9, initUser.getId());

        List<Category> components = (List<Category>) facade.listCategoriesByProjectId(1, ALL_IN_ONE_PAGE, 1);

        assertNotNull(components);
        assertEquals(1,components.size());
        assertEquals(createdComponentId,components.get(0).getId());

        jooq.delete(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Category.CATEGORY).where(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Category.CATEGORY.ID.equal(createdComponentId)).execute();
    }

    public void testModifyComponent() throws Exception {
        //TODO
    }

    public void testDeleteComponentById() throws Exception {
        //TODO
    }

    public void testListCommentsByRequirementId() throws Exception {
        List<Comment> comments = facade.listCommentsByRequirementId(2, ALL_IN_ONE_PAGE).getElements();

        assertNotNull(comments);
        assertEquals(2,comments.size());
        assertTrue(comments.get(0).getId() == 1 || comments.get(0).getId() == 2);
        assertTrue(comments.get(1).getId() == 1 || comments.get(1).getId() == 2);
    }

    public void testCreateComment() throws Exception {
        int createdCommentId = 9;
        Comment testComment = Comment.getBuilder("TestComment").id(createdCommentId).creator(initUser).requirementId(1).build();

        facade.createComment(testComment);

        List<Comment> comments = facade.listCommentsByRequirementId(1, ALL_IN_ONE_PAGE).getElements();

        assertNotNull(comments);
        assertEquals(1,comments.size());
        assertEquals(createdCommentId,comments.get(0).getId());

        jooq.delete(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment.COMMENT).where(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment.COMMENT.ID.equal(createdCommentId)).execute();
    }

    public void testDeleteCommentById() throws Exception {
        Comment testComment = Comment.getBuilder("TestComment").id(9).creator(initUser).requirementId(1).build();

        facade.createComment(testComment);

        facade.deleteCommentById(9);

        List<Comment> comments = facade.listCommentsByRequirementId(1, ALL_IN_ONE_PAGE).getElements();
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    public void testVoteAndUnvote() throws Exception {
        boolean hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);

        assertFalse(hasUserVotedForRequirement);

        facade.vote(3,1,true);
        hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);

        assertTrue(hasUserVotedForRequirement);

        facade.unVote(3,1);
        hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);

        assertFalse(hasUserVotedForRequirement);
    }
    */
}
