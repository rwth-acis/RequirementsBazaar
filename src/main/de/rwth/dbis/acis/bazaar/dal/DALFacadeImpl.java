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
        return projectRepository.findAll(pageable);
    }

    @Override
    public List<Project> searchProjects(String searchTerm, Pageable pageable) throws Exception {
        return projectRepository.searchAll(searchTerm,pageable);
    }

    @Override
    public Project getProjectById(int projectId) throws Exception {
        return projectRepository.findById(projectId);
    }

    @Override
    public void createProject(Project project) {
        projectRepository.add(project);
    }

    @Override
    public void modifyProject(Project modifiedProject) throws Exception {
        projectRepository.update(modifiedProject);
    }

    @Override
    public List<Requirement> listRequirements(Pageable pageable) {

        return requirementRepository.findAll(pageable);
    }

    @Override
    public List<Requirement> listRequirementsByProject(int projectId, Pageable pageable) {
        return requirementRepository.findAllByProject(projectId, pageable);
    }

    @Override
    public List<Requirement> listRequirementsByComponent(int componentId, Pageable pageable) {
        return requirementRepository.findAllByComponent(componentId,pageable);
    }

    @Override
    public List<Requirement> searchRequirements(String searchTerm, Pageable pageable) throws Exception {
        return requirementRepository.searchAll(searchTerm,pageable);
    }

    @Override
    public RequirementEx getRequirementById(int requirementId) throws Exception {
       return requirementRepository.findById(requirementId);
    }

    @Override
    public void createRequirement(Requirement requirement) {
        requirementRepository.add(requirement);
    }

    @Override
    public void modifyRequirement(Requirement modifiedRequirement) throws Exception {
        requirementRepository.update(modifiedRequirement);
    }

    @Override
    public void deleteRequirementById(int requirementId) throws Exception {
        requirementRepository.delete(requirementId);
    }

    @Override
    public List<Component> listComponentsByProjectId(int projectId, Pageable pageable) {
        return componentRepository.findByProjectId(projectId,pageable);
    }

    @Override
    public void createComponent(Component component) {
        componentRepository.add(component);
    }

    @Override
    public void modifyComponent(Component component) throws Exception {
        componentRepository.update(component);
    }

    @Override
    public void deleteComponentById(int componentId) throws Exception {
        componentRepository.delete(componentId);
    }

    @Override
    public void createAttachment(Attachment attachment) {
        attachmentRepository.add(attachment);
    }

    @Override
    public void deleteAttachmentById(int attachmentId) throws Exception {
        attachmentRepository.delete(attachmentId);
    }

    @Override
    public List<Comment> listCommentsByRequirementId(int requirementId, Pageable pageable) {
        return commentRepository.findAllByRequirementId(requirementId,pageable);
    }

    @Override
    public void createComment(Comment comment) {
        commentRepository.add(comment);
    }

    @Override
    public void deleteCommentById(int commentId) throws Exception {
        commentRepository.delete(commentId);
    }

    @Override
    public void follow(int userId, int requirementId) {
        followerRepository.add(Follower.getBuilder()
                                .requirementId(requirementId)
                                .userId(userId)
                                .build()
        );
    }

    @Override
    public void unFollow(int userId, int requirementId) {
        followerRepository.delete(userId,requirementId);
    }

    @Override
    public void wantToDevelop(int userId, int requirementId) {
        developerRepository.add(Developer.getBuilder()
                        .requirementId(requirementId)
                        .userId(userId)
                        .build()
        );
    }

    @Override
    public void notWantToDevelop(int userId, int requirementId) {
        developerRepository.delete(userId,requirementId);
    }

    @Override
    public void giveAuthorization(int userId, int projectId) {
        authorizationRepository.add(Authorization.getBuilder()
                        .projectId(projectId)
                        .userId(userId)
                        .build()
        );
    }

    @Override
    public void removeAuthorization(int userId, int projectId) {
        authorizationRepository.delete(userId,projectId);
    }

    @Override
    public boolean isAuthorized(int userId, int projectId) {
        return authorizationRepository.isAuthorized(userId, projectId);
    }

    @Override
    public void addComponentTag(int requirementId, int componentId) {
        tagRepository.add(Tag.getBuilder(componentId)
                        .requirementId(requirementId)
                        .build()
        );
    }

    @Override
    public void removeComponentTag(int requirementId, int componentId) {
        tagRepository.delete(requirementId, componentId);
    }

    @Override
    public void vote(int userId, int requirementId, boolean isUpVote) {
        voteRepostitory.add(Vote.getBuilder()
                        .requirementId(requirementId)
                        .userId(userId)
                        .isUpvote(isUpVote)
                        .build()
        );
    }

    @Override
    public void unVote(int userId, int requirementId) {
        voteRepostitory.delete(userId,requirementId);
    }

    @Override
    public boolean hasUserVotedForRequirement(int userId, int requirementId) {
        return voteRepostitory.hasUserVotedForRequirement(userId, requirementId);
    }
}
