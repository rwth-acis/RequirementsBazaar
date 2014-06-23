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
import de.rwth.dbis.acis.bazaar.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.dal.repositories.*;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/14/2014
 */
public class DALFacadeImpl implements DALFacade {

    private final DSLContext dslContext;

    private AttachmentRepository attachmentRepository;
    private AuthorizationRepository authorizationRepository;
    private CommentRepository commentRepository;
    private ComponentRepository componentRepository;
    private DeveloperRepository developerRepository;
    private FollowerRepository followerRepository;
    private ProjectRepository projectRepository;
    private RequirementRepository requirementRepository;
    private TagRepository tagRepository;
    private UserRepository userRepository;
    private VoteRepostitory voteRepostitory;

    public DALFacadeImpl() throws Exception {
        Connection conn = null;

        String userName = "root";
        String password = "";
        String url = "jdbc:mysql://localhost:3306/library";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        conn = DriverManager.getConnection(url, userName, password);
        dslContext = DSL.using(conn, SQLDialect.MYSQL);

        attachmentRepository = new AttachmentRepositoryImpl(dslContext);
        authorizationRepository = new AuthorizationRepositoryImpl(dslContext);
        commentRepository = new CommentRepositoryImpl(dslContext);
        componentRepository = new ComponentRepositoryImpl(dslContext);
        developerRepository = new DeveloperRepositoryImpl(dslContext);
        followerRepository = new FollowerRepositoryImpl(dslContext);
        projectRepository = new ProjectRepositoryImpl(dslContext);
        requirementRepository = new RequirementRepositoryImpl(dslContext);
        tagRepository = new TagRepositoryImpl(dslContext);
        userRepository = new UserRepositoryImpl(dslContext);
        voteRepostitory = new VoteRepostitoryImpl(dslContext);

    }


    @Override
    public void createUser(User user) {
        userRepository.add(user);
    }

    @Override
    public void modifyUser(User modifiedUser) throws Exception {
        userRepository.update(modifiedUser);
    }

    @Override
    public User getUserById(int userId) throws Exception {
        return userRepository.findById(userId);
    }

    @Override
    public List<Project> listProjects(Pageable pageable) {
        return null;
    }

    @Override
    public List<Project> searchProjects(String searchTerm, Pageable pageable) {
        return null;
    }

    @Override
    public Project getProjectById(int projectId) {
        return null;
    }

    @Override
    public void createProject(Project project) {

    }

    @Override
    public void modifyProject(Project modifiedProject) {

    }

    @Override
    public List<Requirement> listRequirements(Pageable pageable) {
        return null;
    }

    @Override
    public List<Requirement> listRequirementsByProject(int projectId, Pageable pageable) {
        return null;
    }

    @Override
    public List<Requirement> listRequirementsByComponent(int componentId, Pageable pageable) {
        return null;
    }

    @Override
    public List<Requirement> searchRequirements(String searchTerm, Pageable pageable) {
        return null;
    }

    @Override
    public RequirementEx getRequirementById(int requirementId) {
        return null;
    }

    @Override
    public void createRequirement(Requirement requirement) {

    }

    @Override
    public void modifyRequirement(Requirement modifiedRequirement) {

    }

    @Override
    public void deleteRequirementById(int requirementId) {

    }

    @Override
    public List<Component> listComponentsByProjectId(int projectId, Pageable pageable) {
        return null;
    }

    @Override
    public void createComponent(Component component) {

    }

    @Override
    public void modifyComponent(Component component) {

    }

    @Override
    public void deleteComponentById(int componentId) {

    }

    @Override
    public void createAttachment(Attachment attachment) {

    }

    @Override
    public void deleteAttachmentById(int attachmentId) {

    }

    @Override
    public List<Comment> listCommentsByRequirementId(int requirementId, Pageable pageable) {
        return null;
    }

    @Override
    public void createComment(Comment comment) {

    }

    @Override
    public void deleteCommentById(int commentId) {

    }

    @Override
    public void follow(int userId, int requirementId) {

    }

    @Override
    public void unFollow(int userId, int requirementId) {

    }

    @Override
    public void wantToDevelop(int userId, int requirementId) {

    }

    @Override
    public void notWantToDevelop(int userId, int requirementId) {

    }

    @Override
    public void giveAuthorization(int userId, int projectId) {

    }

    @Override
    public void removeAuthorization(int userId, int projectId) {

    }

    @Override
    public boolean isAuthorized(int userId, int projectId) {
        return false;
    }

    @Override
    public void addComponentTag(int requirementId, int componentId) {

    }

    @Override
    public void removeComponentTag(int requirementId, int componentId) {

    }

    @Override
    public void vote(int userId, int requirementId, boolean isUpVote) {

    }

    @Override
    public void unVote(int userId, int requirementId) {

    }

    @Override
    public boolean hasUserVotedForRequirement(int userId, int requirementId) {
        return false;
    }
}
