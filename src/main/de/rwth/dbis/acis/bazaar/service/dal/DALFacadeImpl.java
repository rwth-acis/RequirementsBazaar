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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/14/2014
 */
public class DALFacadeImpl implements DALFacade {

    private final DSLContext dslContext;

    private AttachmentRepository attachmentRepository;
    private CommentRepository commentRepository;
    private CategoryRepository categoryRepository;
    private RequirementDeveloperRepository developerRepository;
    private ProjectFollowerRepository projectFollowerRepository;
    private CategoryFollowerRepository categoryFollowerRepository;
    private RequirementFollowerRepository requirementFollowerRepository;
    private ProjectRepository projectRepository;
    private RequirementRepository requirementRepository;
    private RequirementCategoryRepository tagRepository;
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
    public List<User> getRecipientListForCategory(int categoryId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.getEmailReceiverForCategory(categoryId);
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
        project.setDefaultCategoryId(null);
        Project newProject = projectRepository.add(project);
        Category uncategorizedCategory = Category.getBuilder(Localization.getInstance().getResourceBundle().getString("category.uncategorized.Name"))
                .description(Localization.getInstance().getResourceBundle().getString("category.uncategorized.Description"))
                .leaderId(newProject.getLeaderId())
                .projectId(newProject.getId())
                .build();
        Category defaultCategory = createCategory(uncategorizedCategory);
        newProject.setDefaultCategoryId(defaultCategory.getId());
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
    public Statistic getStatisticsForAllProjects(int userId, Calendar since) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        Timestamp timestamp  = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return projectRepository.getStatisticsForVisibleProjects(userId, timestamp);
    }

    @Override
    public Statistic getStatisticsForProject(int userId, int projectId, Calendar since) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        Timestamp timestamp  = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return projectRepository.getStatisticsForProject(userId, projectId, timestamp);
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
    public PaginationResult<RequirementEx> listRequirementsByCategory(int categoryId, Pageable pageable, int userId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByCategory(categoryId, pageable, userId);
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
        for (Category category : requirement.getCategories()) {
            addCategoryTag(newRequirement.getId(), category.getId());
        }
        return getRequirementById(newRequirement.getId(), userId);
    }

    @Override
    public RequirementEx modifyRequirement(Requirement modifiedRequirement, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.update(modifiedRequirement);

        if (modifiedRequirement.getCategories() != null) {
            PaginationResult<Category> oldCategorys = listCategorysByRequirementId(modifiedRequirement.getId(), new PageInfo(0, 1000, new HashMap<>()));
            for (Category oldCategory : oldCategorys.getElements()) {
                boolean containCategory = false;
                for (Category newCategory : modifiedRequirement.getCategories()) {
                    if (oldCategory.getId() == newCategory.getId()) {
                        containCategory = true;
                        break;
                    }
                }
                if (!containCategory) {
                    deleteCategoryTag(modifiedRequirement.getId(), oldCategory.getId());
                }
            }
            for (Category newCategory : modifiedRequirement.getCategories()) {
                boolean containCategory = false;
                for (Category oldCategory : oldCategorys.getElements()) {
                    if (oldCategory.getId() == newCategory.getId()) {
                        containCategory = true;
                        break;
                    }
                }
                if (!containCategory) {
                    addCategoryTag(modifiedRequirement.getId(), newCategory.getId());
                }
            }
        }

        return getRequirementById(modifiedRequirement.getId(), userId);
    }

    @Override
    public RequirementEx deleteRequirementById(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        RequirementEx requirement = requirementRepository.findById(requirementId, userId);
        requirementRepository.delete(requirementId);
        return requirement;
    }

    @Override
    public RequirementEx setRequirementToRealized(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setRealized(requirementId, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        return getRequirementById(requirementId, userId);
    }

    @Override
    public RequirementEx setRequirementToUnRealized(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setRealized(requirementId, null);
        return getRequirementById(requirementId, userId);
    }

    @Override
    public RequirementEx setUserAsLeadDeveloper(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setLeadDeveloper(requirementId, userId);
        return getRequirementById(requirementId, userId);
    }

    @Override
    public RequirementEx deleteUserAsLeadDeveloper(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setLeadDeveloper(requirementId, null);
        return getRequirementById(requirementId, userId);
    }

    @Override
    public boolean isRequirementPublic(int requirementId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.belongsToPublicProject(requirementId);
    }

    @Override
    public Statistic getStatisticsForRequirement(int userId, int requirementId, Calendar since) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        Timestamp timestamp  = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return requirementRepository.getStatisticsForRequirement(userId, requirementId, timestamp);
    }

    @Override
    public PaginationResult<Category> listCategorysByProjectId(int projectId, Pageable pageable) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.findByProjectId(projectId, pageable);
    }

    @Override
    public PaginationResult<Category> listCategorysByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.findByRequirementId(requirementId, pageable);
    }

    @Override
    public Category createCategory(Category category) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        Category newCategory = categoryRepository.add(category);
        return categoryRepository.findById(newCategory.getId());
    }

    @Override
    public Category getCategoryById(int categoryId) throws Exception {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.findById(categoryId);
    }

    @Override
    public Category modifyCategory(Category category) throws Exception {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.update(category);
    }

    @Override
    public Category deleteCategoryById(int categoryId, int userId) throws Exception {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);

        //Get requirements for the category in question
        PaginationResult<RequirementEx> requirements = listRequirementsByCategory(categoryId, new PageInfo(0, Integer.MAX_VALUE, new HashMap<>()), 0);

        // Get default category
        Category categoryById = getCategoryById(categoryId);
        Project projectById = getProjectById(categoryById.getProjectId());

        // Move requirements from this category to the default if requirement has no more categories
        for (RequirementEx requirement : requirements.getElements()) {
            deleteCategoryTag(requirement.getId(), categoryId);
            requirement = getRequirementById(requirement.getId(), userId);
            if (requirement.getCategories().isEmpty()) {
                addCategoryTag(requirement.getId(), projectById.getDefaultCategoryId());
            }
        }

        Category deletedCategory = categoryRepository.delete(categoryId);
        return deletedCategory;
    }

    @Override
    public boolean isCategoryPublic(int categoryId) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.belongsToPublicProject(categoryId);
    }

    @Override
    public Statistic getStatisticsForCategory(int userId, int categoryId, Calendar since) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        Timestamp timestamp  = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return categoryRepository.getStatisticsForCategory(userId, categoryId, timestamp);
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
    public Attachment createAttachment(Attachment attachment) throws Exception {
        attachmentRepository = (attachmentRepository != null) ? attachmentRepository : new AttachmentRepositoryImpl(dslContext);
        Attachment newAttachment = attachmentRepository.add(attachment);
        return attachmentRepository.findById(newAttachment.getId());
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
    public Comment createComment(Comment comment) throws Exception {
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
    public CreationStatus followProject(int userId, int projectId) throws BazaarException {
        projectFollowerRepository = (projectFollowerRepository != null) ? projectFollowerRepository : new ProjectFollowerRepositoryImpl(dslContext);
        return projectFollowerRepository.addOrUpdate(ProjectFollower.getBuilder()
                .projectId(projectId)
                .userId(userId)
                .build()
        );
    }

    @Override
    public void unFollowProject(int userId, int projectId) throws BazaarException {
        projectFollowerRepository = (projectFollowerRepository != null) ? projectFollowerRepository : new ProjectFollowerRepositoryImpl(dslContext);
        projectFollowerRepository.delete(userId, projectId);
    }

    @Override
    public CreationStatus followCategory(int userId, int categoryId) throws BazaarException {
        categoryFollowerRepository = (categoryFollowerRepository != null) ? categoryFollowerRepository : new CategoryFollowerRepositoryImpl(dslContext);
        return categoryFollowerRepository.addOrUpdate(CategoryFollower.getBuilder()
                .categoryId(categoryId)
                .userId(userId)
                .build()
        );
    }

    @Override
    public void unFollowCategory(int userId, int categoryId) throws BazaarException {
        categoryFollowerRepository = (categoryFollowerRepository != null) ? categoryFollowerRepository : new CategoryFollowerRepositoryImpl(dslContext);
        categoryFollowerRepository.delete(userId, categoryId);
    }

    @Override
    public CreationStatus followRequirement(int userId, int requirementId) throws BazaarException {
        requirementFollowerRepository = (requirementFollowerRepository != null) ? requirementFollowerRepository : new RequirementFollowerRepositoryImpl(dslContext);
        return requirementFollowerRepository.addOrUpdate(RequirementFollower.getBuilder()
                .requirementId(requirementId)
                .userId(userId)
                .build()
        );
    }

    @Override
    public void unFollowRequirement(int userId, int requirementId) throws BazaarException {
        requirementFollowerRepository = (requirementFollowerRepository != null) ? requirementFollowerRepository : new RequirementFollowerRepositoryImpl(dslContext);
        requirementFollowerRepository.delete(userId, requirementId);
    }

    @Override
    public CreationStatus wantToDevelop(int userId, int requirementId) throws BazaarException {
        developerRepository = (developerRepository != null) ? developerRepository : new RequirementDeveloperRepositoryImpl(dslContext);
        return developerRepository.addOrUpdate(RequirementDeveloper.getBuilder()
                .requirementId(requirementId)
                .userId(userId)
                .build()
        );
    }

    @Override
    public void notWantToDevelop(int userId, int requirementId) throws BazaarException {
        developerRepository = (developerRepository != null) ? developerRepository : new RequirementDeveloperRepositoryImpl(dslContext);
        developerRepository.delete(userId, requirementId);
    }


    @Override
    public void addCategoryTag(int requirementId, int categoryId) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new RequirementCategoryRepositoryImpl(dslContext);
        tagRepository.add(RequirementCategory.getBuilder(categoryId)
                .requirementId(requirementId)
                .build()
        );
    }

    @Override
    public void deleteCategoryTag(int requirementId, int categoryId) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new RequirementCategoryRepositoryImpl(dslContext);
        tagRepository.delete(requirementId, categoryId);
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
