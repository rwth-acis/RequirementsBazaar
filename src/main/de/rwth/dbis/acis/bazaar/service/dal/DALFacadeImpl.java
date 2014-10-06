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

import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.*;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.Timestamp;
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

    public DALFacadeImpl(Connection connection, SQLDialect dialect) throws Exception {
        dslContext = DSL.using(connection, dialect);
    }

    public DSLContext getDslContext() {
        return dslContext;
    }

    @Override
    public void createUser(User user) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        userRepository.add(user);
    }

    @Override
    public void modifyUser(User modifiedUser) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        userRepository.update(modifiedUser);
    }

    @Override
    public User getUserById(int userId) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findById(userId);
    }

    @Override
    public List<Project> listPublicProjects(Pageable pageable) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findAllPublic(pageable);
    }

    @Override
    public List<Project> listPublicAndAuthorizedProjects(PageInfo pageable, int userId) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findAllPublicAndAuthorized(pageable, userId);
    }

    @Override
    public List<Project> searchProjects(String searchTerm, Pageable pageable) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.searchAll(searchTerm, pageable);
    }

    @Override
    public Project getProjectById(int projectId) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findById(projectId);
    }

    @Override
    public void createProject(Project project) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        projectRepository.add(project);
    }

    @Override
    public void modifyProject(Project modifiedProject) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        projectRepository.update(modifiedProject);
    }

    @Override
    public List<Requirement> listRequirements(Pageable pageable) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAll(pageable);
    }

    @Override
    public List<Requirement> listRequirementsByProject(int projectId, Pageable pageable) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByProject(projectId, pageable);
    }

    @Override
    public List<Requirement> listRequirementsByComponent(int componentId, Pageable pageable) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByComponent(componentId, pageable);
    }

    @Override
    public List<Requirement> searchRequirements(String searchTerm, Pageable pageable) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.searchAll(searchTerm, pageable);
    }

    @Override
    public RequirementEx getRequirementById(int requirementId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findById(requirementId);
    }

    @Override
    public void createRequirement(Requirement requirement) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.add(requirement);
    }

    @Override
    public void modifyRequirement(Requirement modifiedRequirement) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.update(modifiedRequirement);
    }

    @Override
    public void deleteRequirementById(int requirementId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.delete(requirementId);
    }

    @Override
    public List<Component> listComponentsByProjectId(int projectId, Pageable pageable) throws BazaarException {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        return componentRepository.findByProjectId(projectId, pageable);
    }

    @Override
    public void createComponent(Component component) throws BazaarException {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        componentRepository.add(component);
    }

    @Override
    public void modifyComponent(Component component) throws Exception {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        componentRepository.update(component);
    }

    @Override
    public void deleteComponentById(int componentId) throws Exception {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        componentRepository.delete(componentId);
    }

    @Override
    public void createAttachment(Attachment attachment) throws BazaarException {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        attachmentRepository.add(attachment);
    }

    @Override
    public void deleteAttachmentById(int attachmentId) throws Exception {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        attachmentRepository.delete(attachmentId);
    }

    @Override
    public List<Comment> listCommentsByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        return commentRepository.findAllByRequirementId(requirementId, pageable);
    }

    @Override
    public void createComment(Comment comment) throws BazaarException {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        commentRepository.add(comment);
    }

    @Override
    public void deleteCommentById(int commentId) throws Exception {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        commentRepository.delete(commentId);
    }

    @Override
    public void follow(int userId, int requirementId) throws BazaarException {
        followerRepository = (followerRepository != null) ? followerRepository : new FollowerRepositoryImpl(dslContext);
        followerRepository.add(Follower.getBuilder()
                        .requirementId(requirementId)
                        .userId(userId)
                        .build()
        );
    }

    @Override
    public void unFollow(int userId, int requirementId) throws BazaarException {
        followerRepository = (followerRepository != null) ? followerRepository : new FollowerRepositoryImpl(dslContext);
        followerRepository.delete(userId, requirementId);
    }

    @Override
    public void wantToDevelop(int userId, int requirementId) throws BazaarException {
        developerRepository = (developerRepository != null) ? developerRepository : new DeveloperRepositoryImpl(dslContext);
        developerRepository.add(Developer.getBuilder()
                        .requirementId(requirementId)
                        .userId(userId)
                        .build()
        );
    }

    @Override
    public void notWantToDevelop(int userId, int requirementId) throws BazaarException {
        developerRepository = (developerRepository != null) ? developerRepository : new DeveloperRepositoryImpl(dslContext);
        developerRepository.delete(userId, requirementId);
    }

    @Override
    public void giveAuthorization(int userId, int projectId) throws BazaarException {
        authorizationRepository = (authorizationRepository != null) ? authorizationRepository : new AuthorizationRepositoryImpl(dslContext);
        authorizationRepository.add(Authorization.getBuilder()
                        .projectId(projectId)
                        .userId(userId)
                        .build()
        );
    }

    @Override
    public void removeAuthorization(int userId, int projectId) throws BazaarException {
        authorizationRepository = (authorizationRepository != null) ? authorizationRepository : new AuthorizationRepositoryImpl(dslContext);
        authorizationRepository.delete(userId, projectId);
    }

    @Override
    public boolean isAuthorized(int userId, int projectId) throws BazaarException {
        authorizationRepository = (authorizationRepository != null) ? authorizationRepository : new AuthorizationRepositoryImpl(dslContext);
        return authorizationRepository.isAuthorized(userId, projectId);
    }

    @Override
    public void addComponentTag(int requirementId, int componentId) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        tagRepository.add(Tag.getBuilder(componentId)
                        .requirementId(requirementId)
                        .build()
        );
    }

    @Override
    public void removeComponentTag(int requirementId, int componentId) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        tagRepository.delete(requirementId, componentId);
    }

    @Override
    public void vote(int userId, int requirementId, boolean isUpVote) throws BazaarException {
        voteRepostitory = (voteRepostitory != null) ? voteRepostitory : new VoteRepostitoryImpl(dslContext);
        voteRepostitory.add(Vote.getBuilder()
                        .requirementId(requirementId)
                        .userId(userId)
                        .isUpvote(isUpVote)
                        .build()
        );
    }

    @Override
    public void unVote(int userId, int requirementId) throws BazaarException {
        voteRepostitory = (voteRepostitory != null) ? voteRepostitory : new VoteRepostitoryImpl(dslContext);
        voteRepostitory.delete(userId, requirementId);
    }

    @Override
    public boolean hasUserVotedForRequirement(int userId, int requirementId) throws BazaarException {
        voteRepostitory = (voteRepostitory != null) ? voteRepostitory : new VoteRepostitoryImpl(dslContext);
        return voteRepostitory.hasUserVotedForRequirement(userId, requirementId);
    }
}
