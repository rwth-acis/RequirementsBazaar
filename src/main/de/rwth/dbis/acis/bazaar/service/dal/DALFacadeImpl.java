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
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.*;
import de.rwth.dbis.acis.bazaar.service.dal.transform.PrivilegeEnumConverter;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/14/2014
 */
public class DALFacadeImpl implements DALFacade {

    private final DSLContext dslContext;

    private AttachmentRepository attachmentRepository;
    private CommentRepository commentRepository;
    private ComponentRepository componentRepository;
    private DeveloperRepository developerRepository;
    private RequirementFollowerRepository followerRepository;
    private ProjectRepository projectRepository;
    private RequirementRepository requirementRepository;
    private TagRepository tagRepository;
    private UserRepository userRepository;
    private VoteRepostitory voteRepostitory;
    private RoleRepostitory roleRepostitory;
    private PrivilegeRepostitory privilegeRepostitory;

    public DALFacadeImpl(DataSource dataSource, SQLDialect dialect) {
        dslContext = DSL.using(dataSource, dialect);
//        dslContext.configuration().set(new ExecuteListenerProvider() {
//            @Override
//            public ExecuteListener provide() {
//                return new DefaultExecuteListener() {
//                    @Override
//                    public void renderEnd(ExecuteContext ctx) {
//                        String sql = ctx.sql();
//                    }
//                };
//            }
//        });
    }

    public DSLContext getDslContext() {
        return dslContext;
    }

    @Override
    public void close() {
        dslContext.close();
    }

    @Override
    public User createUser(User user) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        User newUser = userRepository.add(user);
        return newUser;
    }

    @Override
    public User modifyUser(User modifiedUser) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.update(modifiedUser);
    }

    @Override
    public User getUserById(int userId) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findById(userId);
    }

    @Override
    public Integer getUserIdByLAS2PeerId(long las2PeerId) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.getIdByLas2PeerId(las2PeerId);
    }

    @Override
    public List<User> getRecipientListForProject(int projectId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.getEmailReceiverForProject(projectId);
    }

    @Override
    public List<User> getRecipientListForComponent(int componentId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.getEmailReceiverForComponent(componentId);
    }

    @Override
    public List<User> getRecipientListForRequirement(int requirementId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.getEmailReceiverForRequirement(requirementId);
    }

    @Override
    public PaginationResult<Project> listPublicProjects(Pageable pageable) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findAllPublic(pageable);
    }

    @Override
    public PaginationResult<Project> listPublicAndAuthorizedProjects(PageInfo pageable, long userId) throws BazaarException {
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
    public Project createProject(Project project) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        project.setDefaultComponentId(null);
        Project newProject = projectRepository.add(project);
        Component uncategorizedComponent = Component.getBuilder(Localization.getInstance().getResourceBundle().getString("component.uncategorized.Name"))
                .description(Localization.getInstance().getResourceBundle().getString("component.uncategorized.Description"))
                .leaderId(newProject.getLeaderId())
                .projectId(newProject.getId())
                .build();
        Component defaultComponent = createComponent(uncategorizedComponent);
        newProject.setDefaultComponentId(defaultComponent.getId());
        //TODO concurrency transaction
        return projectRepository.update(newProject);
    }

    @Override
    public Project modifyProject(Project modifiedProject) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.update(modifiedProject);
    }

    @Override
    public boolean isProjectPublic(int projectId) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.belongsToPublicProject(projectId);
    }

    @Override
    public List<Requirement> listRequirements(Pageable pageable) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAll(pageable);
    }

    @Override
    public PaginationResult<RequirementEx> listRequirementsByProject(int projectId, Pageable pageable, int userId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByProject(projectId, pageable, userId);
    }

    @Override
    public PaginationResult<RequirementEx> listRequirementsByComponent(int componentId, Pageable pageable, int userId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByComponent(componentId, pageable, userId);
    }

    @Override
    public List<Requirement> searchRequirements(String searchTerm, Pageable pageable) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.searchAll(searchTerm, pageable);
    }

    @Override
    public RequirementEx getRequirementById(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findById(requirementId, userId);
    }

    @Override
    public RequirementEx createRequirement(Requirement requirement, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        Requirement newRequirement = requirementRepository.add(requirement);
        for (Component component : requirement.getComponents()) {
            addComponentTag(newRequirement.getId(), component.getId());
        }
        return getRequirementById(newRequirement.getId(), userId);
    }

    @Override
    public RequirementEx modifyRequirement(Requirement modifiedRequirement, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.update(modifiedRequirement);

        if (modifiedRequirement.getComponents() != null) {
            PaginationResult<Component> oldComponents = listComponentsByRequirementId(modifiedRequirement.getId(), new PageInfo(0, 1000, ""));
            for (Component oldComponent : oldComponents.getElements()) {
                boolean containComponent = false;
                for (Component newComponent : modifiedRequirement.getComponents()) {
                    if (oldComponent.getId() == newComponent.getId()) {
                        containComponent = true;
                        break;
                    }
                }
                if (!containComponent) {
                    deleteComponentTag(modifiedRequirement.getId(), oldComponent.getId());
                }
            }
            for (Component newComponent : modifiedRequirement.getComponents()) {
                boolean containComponent = false;
                for (Component oldComponent : oldComponents.getElements()) {
                    if (oldComponent.getId() == newComponent.getId()) {
                        containComponent = true;
                        break;
                    }
                }
                if (!containComponent) {
                    addComponentTag(modifiedRequirement.getId(), newComponent.getId());
                }
            }
        }

        return getRequirementById(modifiedRequirement.getId(), userId);
    }

    @Override
    public RequirementEx deleteRequirementById(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);

        //TODO it's a very heavy call for very little
        RequirementEx requirement = requirementRepository.findById(requirementId, userId);
        requirementRepository.delete(requirementId);
        return requirement;
    }

    @Override
    public boolean isRequirementPublic(int requirementId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.belongsToPublicProject(requirementId);
    }

    @Override
    public PaginationResult<Component> listComponentsByProjectId(int projectId, Pageable pageable) throws BazaarException {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        return componentRepository.findByProjectId(projectId, pageable);
    }

    @Override
    public PaginationResult<Component> listComponentsByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        return componentRepository.findByRequirementId(requirementId, pageable);
    }

    @Override
    public Component createComponent(Component component) throws BazaarException {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        Component newComponent = componentRepository.add(component);
        return componentRepository.findById(newComponent.getId());
    }

    @Override
    public Component getComponentById(int componentId) throws Exception {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        return componentRepository.findById(componentId);
    }

    @Override
    public Component modifyComponent(Component component) throws Exception {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        return componentRepository.update(component);
    }

    @Override
    public Component deleteComponentById(int componentId, int userId) throws Exception {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);

        //Get requirements for the component in question
        PaginationResult<RequirementEx> requirements = listRequirementsByComponent(componentId, new PageInfo(0, Integer.MAX_VALUE, ""), 0);

        // Get default component
        Component componentById = getComponentById(componentId);
        Project projectById = getProjectById(componentById.getProjectId());

        // Move requirements from this component to the default if requirement has no more components
        for (RequirementEx requirement : requirements.getElements()) {
            deleteComponentTag(requirement.getId(), componentId);
            requirement = getRequirementById(requirement.getId(), userId);
            if (requirement.getComponents().isEmpty()) {
                addComponentTag(requirement.getId(), projectById.getDefaultComponentId());
            }
        }

        Component deletedComponent = componentRepository.delete(componentId);
        return deletedComponent;
    }

    @Override
    public boolean isComponentPublic(int componentId) throws BazaarException {
        componentRepository = (componentRepository != null) ? componentRepository : new ComponentRepositoryImpl(dslContext);
        return componentRepository.belongsToPublicProject(componentId);
    }

    @Override
    public Attachment getAttachmentById(int attachmentId) throws Exception {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        return attachmentRepository.findById(attachmentId);
    }

    @Override
    public PaginationResult<Attachment> listAttachmentsByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        return attachmentRepository.findAllByRequirementId(requirementId, pageable);
    }

    @Override
    public Attachment createAttachment(Attachment attachment) throws BazaarException {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        Attachment newAttachment = attachmentRepository.add(attachment);
        return newAttachment;
    }

    @Override
    public Attachment deleteAttachmentById(int attachmentId) throws Exception {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        Attachment attachment = attachmentRepository.findById(attachmentId);
        attachmentRepository.delete(attachmentId);
        return attachment;
    }

    @Override
    public PaginationResult<Comment> listCommentsByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        return commentRepository.findAllByRequirementId(requirementId, pageable);
    }

    @Override
    public Comment getCommentById(int commentId) throws Exception {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        return commentRepository.findById(commentId);
    }

    @Override
    public Comment createComment(Comment comment) throws BazaarException, Exception {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        Comment newComment = commentRepository.add(comment);
        return commentRepository.findById(newComment.getId());
    }

    @Override
    public Comment deleteCommentById(int commentId) throws Exception {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        Comment comment = commentRepository.findById(commentId);
        commentRepository.delete(commentId);
        return comment;
    }

    @Override
    public CreationStatus follow(int userId, int requirementId) throws BazaarException {
        followerRepository = (followerRepository != null) ? followerRepository : new RequirementFollowerRepositoryImpl(dslContext);
        return followerRepository.addOrUpdate(RequirementFollower.getBuilder()
                .requirementId(requirementId)
                .userId(userId)
                .build()
        );
    }

    @Override
    public void unFollow(int userId, int requirementId) throws BazaarException {
        followerRepository = (followerRepository != null) ? followerRepository : new RequirementFollowerRepositoryImpl(dslContext);
        followerRepository.delete(userId, requirementId);
    }

    @Override
    public CreationStatus wantToDevelop(int userId, int requirementId) throws BazaarException {
        developerRepository = (developerRepository != null) ? developerRepository : new DeveloperRepositoryImpl(dslContext);
        return developerRepository.addOrUpdate(Developer.getBuilder()
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
    public void addComponentTag(int requirementId, int componentId) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        tagRepository.add(Tag.getBuilder(componentId)
                .requirementId(requirementId)
                .build()
        );
    }

    @Override
    public void deleteComponentTag(int requirementId, int componentId) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        tagRepository.delete(requirementId, componentId);
    }

    @Override
    public CreationStatus vote(int userId, int requirementId, boolean isUpVote) throws BazaarException {
        voteRepostitory = (voteRepostitory != null) ? voteRepostitory : new VoteRepostitoryImpl(dslContext);
        return voteRepostitory.addOrUpdate(Vote.getBuilder()
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

    @Override
    public List<Role> getRolesByUserId(int userId, String context) throws BazaarException {
        roleRepostitory = (roleRepostitory != null) ? roleRepostitory : new RoleRepostitoryImpl(dslContext);
        return roleRepostitory.listRolesOfUser(userId, context);
    }

    @Override
    public List<Role> getParentsForRole(int roleId) throws BazaarException {
        roleRepostitory = (roleRepostitory != null) ? roleRepostitory : new RoleRepostitoryImpl(dslContext);
        return roleRepostitory.listParentsForRole(roleId);
    }

    @Override
    public void createPrivilegeIfNotExists(PrivilegeEnum privilege) throws BazaarException {
        privilegeRepostitory = (privilegeRepostitory != null) ? privilegeRepostitory : new PrivilegeRepostitoryImpl(dslContext);

        Privilege privilegeDb = privilegeRepostitory.findByName(new PrivilegeEnumConverter().to(privilege));
        if (privilegeDb == null) {
            privilegeRepostitory.add(Privilege.getBuilder(privilege).build());
        }

    }

    @Override
    public void addUserToRole(int userId, String roleName, String context) throws BazaarException {
        roleRepostitory = (roleRepostitory != null) ? roleRepostitory : new RoleRepostitoryImpl(dslContext);
        roleRepostitory.addUserToRole(userId, roleName, context);
    }
}
