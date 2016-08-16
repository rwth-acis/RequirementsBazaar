///*
// *
// *  Copyright (c) 2014, RWTH Aachen University.
// *  For a list of contributors see the AUTHORS file at the top-level directory
// *  of this distribution.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// * /
// */
//
//package de.rwth.dbis.acis.bazaar.service.dal;
//
//import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
//import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
//import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.*;
//import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.*;
//import junit.framework.TestCase;
//import org.jooq.*;
//import org.jooq.impl.DefaultExecuteListener;
//import org.junit.BeforeClass;
//
//import java.net.URL;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.Timestamp;
//import java.util.List;
//
//import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users.USERS;
//import static org.jooq.impl.DSL.count;
//
////TODO Pagination testing
//public class DALFacadeTest extends TestCase {
//
//    public static final PageInfo ALL_IN_ONE_PAGE = new PageInfo(0, 100);
//
//    DALFacade facade;
//    private DALFacadeImpl dalImpl;
//    DSLContext jooq;
//
//    @BeforeClass
//    public void setUp() throws Exception {
//        Connection conn = null;
//
//        String userName = "root";
//        String password = "";
//        URL db = DALFacadeTest.class.getResource("reqbaz.db");
//        String url = "jdbc:sqlite:" + db.getPath();
//
//
//        Class.forName("org.sqlite.JDBC").newInstance();
//        conn = DriverManager.getConnection(url, userName, password);
//
//
//
//        dalImpl = new DALFacadeImpl(conn, SQLDialect.SQLITE);
//        facade = dalImpl;
//        jooq = dalImpl.getDslContext();
//        jooq.configuration().set(new ExecuteListenerProvider() {
//            @Override
//            public ExecuteListener provide() {
//                return new DefaultExecuteListener() {
//                    @Override
//                    public void renderEnd(ExecuteContext ctx) {
//                        String sql = ctx.sql();
//                        String replace = sql.replace("reqbaz.", "main.");
//                        ctx.sql(replace);
//                    }
//                };
//            }
//        });
//
//        jooq.execute("DELETE FROM main.Attachments");
//        jooq.execute("DELETE FROM main.Authorizations");
//        jooq.execute("DELETE FROM main.Comments");
//        jooq.execute("DELETE FROM main.Developers");
//        jooq.execute("DELETE FROM main.Followers");
//        jooq.execute("DELETE FROM main.Votes");
//        jooq.execute("DELETE FROM main.Tags");
//        jooq.execute("DELETE FROM main.Components");
//        jooq.execute("DELETE FROM main.Requirements");
//        jooq.execute("DELETE FROM main.Projects");
//        jooq.execute("DELETE FROM main.Users");
//
//        Field<Integer> f = count();
//
//        User initUser = getInitUser();
//        if (jooq.selectCount().from(USERS).where(USERS.ID.equal(initUser.getId())).fetchOne(f) != 1)
//            jooq.insertInto(USERS).set(new UsersRecord(initUser.getId(), initUser.getFirstName(), initUser.getLastName(), initUser.geteMail(), (byte) (initUser.getAdmin() ? 1 : 0), initUser.getLas2peerId(), initUser.getUserName())).execute();
//
//        if (jooq.selectCount().from(USERS).where(USERS.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(USERS).set(new UsersRecord(2, "Alma", "Barack", "test@test.hu", (byte) 1, 2222, "AlmBar")).execute();
//
//        if (jooq.selectCount().from(USERS).where(USERS.ID.equal(3)).fetchOne(f) != 1)
//            jooq.insertInto(USERS).set(new UsersRecord(3, "Citrom", "Datolya", "test@test.hu", (byte) 0, 3333, "CitrDat")).execute();
//
//        if (jooq.selectCount().from(Projects.PROJECTS).where(Projects.PROJECTS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Projects.PROJECTS).set(new ProjectsRecord(1, "Project1", "ProjDesc1", "-", 1)).execute();
//
//        if (jooq.selectCount().from(Projects.PROJECTS).where(Projects.PROJECTS.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(Projects.PROJECTS).set(new ProjectsRecord(2, "Project2", "ProjDesc2", "+", 1)).execute();
//
//        if (jooq.selectCount().from(Requirements.REQUIREMENTS).where(Requirements.REQUIREMENTS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Requirements.REQUIREMENTS).set(new RequirementsRecord(1, "Req1", "ReqDesc1", Timestamp.valueOf("2005-04-06 09:01:10"), 1, 1, 1)).execute();
//
//        if (jooq.selectCount().from(Requirements.REQUIREMENTS).where(Requirements.REQUIREMENTS.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(Requirements.REQUIREMENTS).set(new RequirementsRecord(2, "Req2", "ReqDesc2", Timestamp.valueOf("2005-05-06 09:01:10"), 1, 1, 2)).execute();
//
//        if (jooq.selectCount().from(Requirements.REQUIREMENTS).where(Requirements.REQUIREMENTS.ID.equal(3)).fetchOne(f) != 1)
//            jooq.insertInto(Requirements.REQUIREMENTS).set(new RequirementsRecord(3, "Req3", "ReqDesc3", Timestamp.valueOf("2005-06-06 09:01:10"), 1, 1, 1)).execute();
//
//        if (jooq.selectCount().from(Components.COMPONENTS).where(Components.COMPONENTS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Components.COMPONENTS).set(new ComponentsRecord(1, "Comp1", "CompDesc1", 2, 1)).execute();
//
//        if (jooq.selectCount().from(Tags.TAGS).where(Tags.TAGS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Tags.TAGS).set(new TagsRecord(1, 1, 2)).execute();
//
//        if (jooq.selectCount().from(Developers.DEVELOPERS).where(Developers.DEVELOPERS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Developers.DEVELOPERS).set(new DevelopersRecord(1, 2, 2)).execute();
//
//        if (jooq.selectCount().from(Developers.DEVELOPERS).where(Developers.DEVELOPERS.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(Developers.DEVELOPERS).set(new DevelopersRecord(2, 2, 3)).execute();
//
//        if (jooq.selectCount().from(Followers.FOLLOWERS).where(Followers.FOLLOWERS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Followers.FOLLOWERS).set(new FollowersRecord(1, 2, 2)).execute();
//        if (jooq.selectCount().from(Followers.FOLLOWERS).where(Followers.FOLLOWERS.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(Followers.FOLLOWERS).set(new FollowersRecord(2, 2, 3)).execute();
//
//        if (jooq.selectCount().from(Votes.VOTES).where(Votes.VOTES.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Votes.VOTES).set(new VotesRecord(1, (byte) 1, 2, 1)).execute();
//        if (jooq.selectCount().from(Votes.VOTES).where(Votes.VOTES.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(Votes.VOTES).set(new VotesRecord(2, (byte) 0, 2, 3)).execute();
//
//        if (jooq.selectCount().from(Attachments.ATTACHMENTS).where(Attachments.ATTACHMENTS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Attachments.ATTACHMENTS).set(new AttachmentsRecord(1, Timestamp.valueOf("2005-04-06 09:01:10"), 2, 2, "StoryAttachment", "S", null, null, "RequirementStory", null, null, null)).execute();
//
//
//        if (jooq.selectCount().from(Comments.COMMENTS).where(Comments.COMMENTS.ID.equal(1)).fetchOne(f) != 1)
//            jooq.insertInto(Comments.COMMENTS).set(new CommentsRecord(1, "Lorem ipsum dolor sit amet", Timestamp.valueOf("2005-04-06 09:01:10"), 2, 2)).execute();
//        if (jooq.selectCount().from(Comments.COMMENTS).where(Comments.COMMENTS.ID.equal(2)).fetchOne(f) != 1)
//            jooq.insertInto(Comments.COMMENTS).set(new CommentsRecord(2, "Test message payload", Timestamp.valueOf("2005-04-06 10:01:10"), 2, 3)).execute();
//
//
//    }
//
//    private User getInitUser() {
//        return User.geBuilder("test@test.hu")
//                .id(1)
//                .firstName("Elek")
//                .lastName("Test")
//                .userName("TestElek")
//                .admin(true)
//                .las2peerId(1111)
//                .build();
//    }
//
//    public void testCreateUser() throws Exception {
//        facade.createUser(User.geBuilder("unittest@test.hu").id(9).firstName("Max").lastName("Zimmermann").admin(false).las2peerId(9999).userName("MaxZim").build());
//
//        User user = facade.getUserById(9);
//
//        assertEquals(9, user.getId());
//        assertEquals("unittest@test.hu", user.geteMail());
//        assertEquals("Max", user.getFirstName());
//        assertEquals("Zimmermann", user.getLastName());
//        assertEquals(false, user.isAdmin());
//        assertEquals(9999, user.getLas2peerId());
//
//
//        //Clean
//        jooq.delete(USERS).where(USERS.ID.equal(9)).execute();
//    }
//
//    public void testModifyUser() throws Exception {
//        //TODO
//    }
//
//    public void testGetUserById() throws Exception {
//        User user = facade.getUserById(1);
//
//        assertEquals(1, user.getId());
//        assertEquals("test@test.hu", user.geteMail());
//        assertEquals("Elek", user.getFirstName());
//        assertEquals("Test", user.getLastName());
//        assertEquals(true, user.isAdmin());
//        assertEquals(1111, user.getLas2peerId());
//    }
//
//    public void testListPublicProjects() throws Exception {
//        List<Project> projects = facade.listPublicProjects(new PageInfo(0, 1));
//
//        assertNotNull(projects);
//        assertEquals(1,projects.size());
//
//        for (Project project : projects) {
//            assertEquals(Project.ProjectVisibility.PUBLIC,project.getVisibility());
//        }
//
//        Project project = projects.get(0);
//
//        assertEquals(2, project.getId());
//        assertEquals("Project2",project.getName());
//        assertEquals("ProjDesc2",project.getDescription());
//        assertEquals(1,project.getLeaderId());
//        assertEquals(Project.ProjectVisibility.PUBLIC, project.getVisibility());
//    }
//
//
//    public void testSearchProjects() throws Exception {
//        List<Project> projects = facade.searchProjects("2", ALL_IN_ONE_PAGE);
//
//        assertNotNull(projects);
//        assertEquals(1,projects.size());
//
//        Project project = projects.get(0);
//        assertEquals(2, project.getId());
//        assertEquals("Project2",project.getName());
//        assertEquals("ProjDesc2",project.getDescription());
//        assertEquals(1,project.getLeaderId());
//        assertEquals(Project.ProjectVisibility.PUBLIC, project.getVisibility());
//
//        projects = facade.searchProjects("ProjD", new PageInfo(0, 100));
//
//        assertNotNull(projects);
//        assertEquals(2,projects.size());
//
//        assertEquals(1, projects.get(0).getId());
//        assertEquals("Project1",projects.get(0).getName());
//        assertEquals("ProjDesc1",projects.get(0).getDescription());
//        assertEquals(1,projects.get(0).getLeaderId());
//        assertEquals(Project.ProjectVisibility.PRIVATE, projects.get(0).getVisibility());
//
//        assertEquals(2, projects.get(1).getId());
//        assertEquals("Project2",projects.get(1).getName());
//        assertEquals("ProjDesc2",projects.get(1).getDescription());
//        assertEquals(1,projects.get(1).getLeaderId());
//        assertEquals(Project.ProjectVisibility.PRIVATE, projects.get(1).getVisibility());
//    }
//
//    public void testGetProjectById() throws Exception {
//        Project project = facade.getProjectById(1);
//
//        assertEquals(1, project.getId());
//        assertEquals("Project1",project.getName());
//        assertEquals("ProjDesc1",project.getDescription());
//        assertEquals(1,project.getLeaderId());
//        assertEquals(Project.ProjectVisibility.PRIVATE, project.getVisibility());
//    }
//
//    public void testCreateProject() throws Exception {
//        int createdProjId = 3;
//        Project project = Project.getBuilder("Project3").description("ProjDesc3").leaderId(1).id(createdProjId).visibility(Project.ProjectVisibility.PUBLIC).build();
//
//        facade.createProject(project);
//
//        Project projectById = facade.getProjectById(createdProjId);
//
//        assertEquals(project.getId(),         projectById.getId()             );
//        assertEquals(project.getName(),       projectById.getName()           );
//        assertEquals(project.getDescription(),projectById.getDescription()      );
//        assertEquals(project.getLeaderId(),   projectById.getLeaderId()      );
//        assertEquals(project.getVisibility(), projectById.getVisibility()      );
//
//        //Clean
//        jooq.delete(Projects.PROJECTS).where(Projects.PROJECTS.ID.equal(createdProjId)).execute();
//    }
//
//    public void testModifyProject() throws Exception {
//        //TODO
//    }
//
//    public void testListRequirements() throws Exception {
//        List<Requirement> requirements = facade.listRequirements(ALL_IN_ONE_PAGE);
//
//        assertNotNull(requirements);
//        assertEquals(3, requirements.size());
//
//        Requirement requirement = requirements.get(2);
//
//        assertEquals(1,requirement.getId());
//        assertEquals(1,requirement.getCreatorId());
//        assertEquals(1,requirement.getLeadDeveloperId());
//        assertEquals(1,requirement.getProjectId());
//        assertEquals("Req1",requirement.getTitle());
//        assertEquals("ReqDesc1",requirement.getDescription());
//
//        requirements = facade.listRequirements(new PageInfo(1, 2));
//
//        assertNotNull(requirements);
//        assertEquals(1, requirements.size());
//        assertEquals(1,requirements.get(0).getId());
//
//        requirements = facade.listRequirements(new PageInfo(0, 1));
//
//        assertNotNull(requirements);
//        assertEquals(1, requirements.size());
//        assertEquals(3,requirements.get(0).getId());
//
//    }
//
//    public void testListRequirementsByProject() throws Exception {
//        List<Requirement> requirements = facade.listRequirementsByProject(2, ALL_IN_ONE_PAGE);
//
//        assertNotNull(requirements);
//        assertEquals(1, requirements.size());
//
//        Requirement requirement2 = requirements.get(0);
//
//        assertEquals(2,requirement2.getId());
//        assertEquals(1,requirement2.getCreatorId());
//        assertEquals(1,requirement2.getLeadDeveloperId());
//        assertEquals(2,requirement2.getProjectId());
//        assertEquals("Req2",requirement2.getTitle());
//        assertEquals("ReqDesc2",requirement2.getDescription());
//
//
//    }
//
//    public void testListRequirementsByComponent() throws Exception {
//        List<Requirement> requirements = facade.listRequirementsByComponent(1, ALL_IN_ONE_PAGE);
//
//        assertNotNull(requirements);
//        assertEquals(1, requirements.size());
//
//        Requirement requirement2 = requirements.get(0);
//
//        assertEquals(2,requirement2.getId());
//        assertEquals(1,requirement2.getCreatorId());
//        assertEquals(1,requirement2.getLeadDeveloperId());
//        assertEquals(2,requirement2.getProjectId());
//        assertEquals("Req2",requirement2.getTitle());
//        assertEquals("ReqDesc2",requirement2.getDescription());
//
//    }
//
//    public void testSearchRequirements() throws Exception {
//        List<Requirement> requirements = facade.searchRequirements("desc2", ALL_IN_ONE_PAGE);
//
//        assertNotNull(requirements);
//        assertEquals(1, requirements.size());
//
//        Requirement requirement2 = requirements.get(0);
//
//        assertEquals(2,requirement2.getId());
//        assertEquals(1,requirement2.getCreatorId());
//        assertEquals(1,requirement2.getLeadDeveloperId());
//        assertEquals(2,requirement2.getProjectId());
//        assertEquals("Req2",requirement2.getTitle());
//        assertEquals("ReqDesc2",requirement2.getDescription());
//    }
//
//    public void testGetRequirementById() throws Exception {
//        RequirementEx requirement = facade.getRequirementById(2);
//
//        assertEquals(2, requirement.getId());
//
//        assertEquals(1, requirement.getCreatorId());
//        assertEquals(1,requirement.getCreator().getId());
//        assertEquals("Elek",requirement.getCreator().getFirstName());
//
//        assertEquals(1,requirement.getLeadDeveloperId());
//        assertEquals(1,requirement.getLeadDeveloper().getId());
//        assertEquals("Elek",requirement.getLeadDeveloper().getFirstName());
//
//        assertEquals(2,requirement.getProjectId());
//        assertEquals("Req2",requirement.getTitle());
//        assertEquals("ReqDesc2",requirement.getDescription());
//
//        List<Attachment> attachments = requirement.getAttachments();
//        assertNotNull(attachments);
//        assertEquals(1, attachments.size());
//        assertEquals(2, attachments.get(0).getCreatorId());
//
//        List<Component> components = requirement.getComponents();
//        assertNotNull(components);
//        assertEquals(1,components.size());
//        assertEquals(1, components.get(0).getId());
//        assertEquals("Comp1",components.get(0).getName());
//
//        List<User> contributors = requirement.getContributors();
//        assertNotNull(contributors);
//        assertEquals(1,contributors.size());
//        assertEquals(2,contributors.get(0).getId());
//
//        List<User> developers = requirement.getDevelopers();
//        assertNotNull(developers);
//        assertEquals(2,developers.size());
//        assertEquals(2,developers.get(0).getId());
//        assertEquals(3, developers.get(1).getId());
//
//        List<User> followers = requirement.getFollowers();
//        assertNotNull(followers);
//        assertEquals(2,followers.size());
//        assertEquals(2,followers.get(0).getId());
//        assertEquals(3,followers.get(1).getId());
//
//    }
//
//    public void testCreateRequirement() throws Exception {
//        int createdRequirementId = 9;
//        try {
//            Requirement requirement = Requirement.getBuilder("AddedReq1").id(createdRequirementId).description("Test addition").creatorId(2).leadDeveloperId(2).projectId(3).creationTime(Timestamp.valueOf("2005-04-06 09:01:10")).build();
//
//            facade.createRequirement(requirement, componentId);
//
//            RequirementEx requirementById = facade.getRequirementById(createdRequirementId);
//
//            assertEquals(requirement.getId(), requirementById.getId());
//            assertEquals(requirement.getTitle(), requirementById.getTitle());
//            assertEquals(requirement.getDescription(), requirementById.getDescription());
//            assertEquals(requirement.getCreatorId(), requirementById.getCreatorId());
//            assertEquals(requirement.getLeadDeveloperId(), requirementById.getLeadDeveloperId());
//            assertEquals(requirement.getProjectId(), requirementById.getProjectId());
//        }
//        finally {
//            jooq.delete(Requirements.REQUIREMENTS).where(Requirements.REQUIREMENTS.ID.equal(createdRequirementId)).execute();
//        }
//
//    }
//
//    public void testModifyRequirement() throws Exception {
//        //TODO
//    }
//
//    public void testDeleteRequirementById() throws Exception {
//        Requirement requirement = Requirement.getBuilder("AddedReq1").id(9).description("Test addition").creatorId(2).leadDeveloperId(2).projectId(3).build();
//
//        facade.createRequirement(requirement, componentId);
//
//        facade.deleteRequirementById(9);
//
//        try {
//            RequirementEx requirementById = facade.getRequirementById(9);
//        }
//        catch (Exception ex){
//            assertEquals("No class de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord found with id: 9", ex.getMessage());
//        }
//    }
//
//    public void testListComponentsByProjectId() throws Exception {
//        List<Component> components = facade.listComponentsByProjectId(2, ALL_IN_ONE_PAGE);
//
//        assertNotNull(components);
//        assertEquals(1,components.size());
//        assertEquals(1,components.get(0).getId());
//    }
//
//    public void testCreateComponent() throws Exception {
//        int createdComponentId = 9;
//        Component testComp9 = Component.getBuilder("TestComp9").description("Very testing").id(createdComponentId).projectId(1).leaderId(1).build();
//
//        facade.createComponent(testComp9);
//
//        List<Component> components = facade.listComponentsByProjectId(1, ALL_IN_ONE_PAGE);
//
//        assertNotNull(components);
//        assertEquals(1,components.size());
//        assertEquals(createdComponentId,components.get(0).getId());
//
//        //Clean
//        jooq.delete(Components.COMPONENTS).where(Components.COMPONENTS.ID.equal(createdComponentId)).execute();
//
//    }
//
//    public void testModifyComponent() throws Exception {
//        //TODO
//    }
//
//    public void testDeleteComponentById() throws Exception {
//        //TODO
//    }
//
//    public void testListCommentsByRequirementId() throws Exception {
//        List<Comment> comments = facade.listCommentsByRequirementId(2, ALL_IN_ONE_PAGE);
//
//        assertNotNull(comments);
//        assertEquals(2,comments.size());
//        assertTrue(comments.get(0).getId() == 1 || comments.get(0).getId() == 2);
//        assertTrue(comments.get(1).getId() == 1 || comments.get(1).getId() == 2);
//    }
//
//    public void testCreateComment() throws Exception {
//        int createdCommentId = 9;
//        Comment testComment = Comment.getBuilder("TestComment").id(createdCommentId).creatorId(1).requirementId(1).build();
//
//        facade.createComment(testComment);
//
//        List<Comment> comments = facade.listCommentsByRequirementId(1, ALL_IN_ONE_PAGE);
//
//        assertNotNull(comments);
//        assertEquals(1,comments.size());
//        assertEquals(createdCommentId,comments.get(0).getId());
//
//        //CLEAN
//        jooq.delete(Comments.COMMENTS).where(Comments.COMMENTS.ID.equal(createdCommentId)).execute();
//    }
//
//    public void testDeleteCommentById() throws Exception {
//        Comment testComment = Comment.getBuilder("TestComment").id(9).creatorId(1).requirementId(1).build();
//
//        facade.createComment(testComment);
//
//        facade.deleteCommentById(9);
//
//        List<Comment> comments = facade.listCommentsByRequirementId(1, ALL_IN_ONE_PAGE);
//        assertNotNull(comments);
//        assertEquals(0, comments.size());
//    }
//
//    public void testFollowUnFollow() throws Exception {
//        facade.follow(2, 1);
//
//        RequirementEx requirementById = facade.getRequirementById(1);
//        List<User> followers = requirementById.getFollowers();
//
//        assertNotNull(followers);
//        assertEquals(1, followers.size());
//        assertEquals(2, followers.get(0).getId());
//
//        facade.unFollow(2, 1);
//
//        requirementById = facade.getRequirementById(1);
//        followers = requirementById.getFollowers();
//
//        assertEquals(0, followers.size());
//    }
//
//
//    public void testWantToDevelopNotWant() throws Exception {
//        facade.wantToDevelop(2, 1);
//
//        RequirementEx requirementById = facade.getRequirementById(1);
//        List<User> developers = requirementById.getDevelopers();
//
//        assertNotNull(developers);
//        assertEquals(1, developers.size());
//        assertEquals(2, developers.get(0).getId());
//
//        facade.notWantToDevelop(2, 1);
//
//        requirementById = facade.getRequirementById(1);
//        developers = requirementById.getDevelopers();
//
//        assertEquals(0, developers.size());
//    }
//
//
//    public void testGiveAuthorizationAndRemove() throws Exception {
//
//        boolean isAuthorized = facade.isAuthorized(2, 1);
//
//        assertEquals(false, isAuthorized);
//
//        facade.giveAuthorization(2,1);
//        isAuthorized = facade.isAuthorized(2,1);
//
//        assertEquals(true, isAuthorized);
//
//        facade.removeAuthorization(2, 1);
//        isAuthorized = facade.isAuthorized(2,1);
//
//        assertEquals(false, isAuthorized);
//    }
//
//
//    public void testAddComponentTagAndRemove() throws Exception {
//        facade.addComponentTag(1,1);
//
//        List<Component> components = facade.getRequirementById(1).getComponents();
//        assertNotNull(components);
//        assertEquals(1,components.size());
//        assertEquals(1, components.get(0).getId());
//
//        facade.deleteComponentTag(1,1);
//
//        components = facade.getRequirementById(1).getComponents();
//        assertEquals(0,components.size());
//    }
//
//
//
//    public void testVoteandUnvote() throws Exception {
//        boolean hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);
//
//        assertEquals(false, hasUserVotedForRequirement);
//
//        facade.vote(3,1,true);
//        hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);
//
//        assertEquals(true, hasUserVotedForRequirement);
//
//        facade.unVote(3,1);
//        hasUserVotedForRequirement = facade.hasUserVotedForRequirement(3, 1);
//
//        assertEquals(false, hasUserVotedForRequirement);
//    }
//
//}