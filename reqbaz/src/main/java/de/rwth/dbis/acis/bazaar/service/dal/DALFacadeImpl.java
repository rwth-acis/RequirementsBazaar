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
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import i5.las2peer.api.Context;
import i5.las2peer.security.PassphraseAgentImpl;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
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
    private RequirementCategoryRepository requirementCategoryRepository;
    private UserRepository userRepository;
    private VoteRepository voteRepository;
    private RoleRepository roleRepository;
    private PrivilegeRepository privilegeRepository;
    private PersonalisationDataRepository personalisationDataRepository;
    private FeedbackRepository feedbackRepository;
    private TagRepository tagRepository;
    private RequirementTagRepository requirementTagRepository;

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
        // No longer necessary, jooq claims gc will take care of it
        // dslContext.close();
    }

    @Override
    public User createUser(User user) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.add(user);
    }

    @Override
    public User modifyUser(User modifiedUser) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.update(modifiedUser);
    }

    @Override
    public void updateLastLoginDate(int userId) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        userRepository.updateLastLoginDate(userId);
    }

    @Override
    public User getUserById(int userId) throws Exception {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findById(userId);
    }

    @Override
    public Integer getUserIdByLAS2PeerId(String las2PeerId) throws Exception {
        Integer userId;

        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        userId = userRepository.getIdByLas2PeerId(las2PeerId);

        if (userId == null) {
            PassphraseAgentImpl agent = (PassphraseAgentImpl) Context.getCurrent().getMainAgent();
            String oldLas2peerId = String.valueOf(userRepository.hashAgentSub(agent));
            userId = userRepository.getIdByLas2PeerId(oldLas2peerId);
            if (userId != null) {
                userRepository.updateLas2peerId(userId, las2PeerId);
            }
        }
        return userId;
    }

    @Override
    public RequirementContributors listContributorsForRequirement(int requirementId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findRequirementContributors(requirementId);
    }

    @Override
    public CategoryContributors listContributorsForCategory(int categoryId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findCategoryContributors(categoryId);
    }

    @Override
    public ProjectContributors listContributorsForProject(int projectId) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findProjectContributors(projectId);
    }

    @Override
    public PaginationResult<User> listDevelopersForRequirement(int requirementId, Pageable pageable) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findAllByDeveloping(requirementId, pageable);
    }

    @Override
    public PaginationResult<User> listFollowersForRequirement(int requirementId, Pageable pageable) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findAllByFollowing(0, 0, requirementId, pageable);
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
    public PaginationResult<User> searchUsers(PageInfo pageInfo) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        List<User> users = userRepository.search(pageInfo);
        return new PaginationResult<>(users.size(), pageInfo, users);
    }

    @Override
    public PaginationResult<Project> listPublicProjects(Pageable pageable, int userId) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findAllPublic(pageable, userId);
    }

    @Override
    public PaginationResult<Project> listPublicAndAuthorizedProjects(PageInfo pageable, int userId) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findAllPublicAndAuthorized(pageable, userId);
    }

    @Override
    public Project getProjectById(int projectId, int userId) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.findById(projectId, userId);
    }

    @Override
    public Project createProject(Project project, int userId) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        project.setDefaultCategoryId(null);
        Project newProject = projectRepository.add(project);

        String categoryName = Localization.getInstance().getResourceBundle().getString("category.uncategorized.Name");
        String categoryDescription = Localization.getInstance().getResourceBundle().getString("category.uncategorized.Description");
        Category uncategorizedCategory = Category.builder()
                .name(categoryName)
                .description(categoryDescription)
                .projectId(newProject.getId())
                .build();
        uncategorizedCategory.setCreator(project.getLeader());
        Category defaultCategory = createCategory(uncategorizedCategory, userId);
        newProject.setDefaultCategoryId(defaultCategory.getId());
        // TODO: concurrency transaction -> https://www.jooq.org/doc/3.9/manual/sql-execution/transaction-management/
        addUserToRole(userId, "ProjectAdmin", newProject.getId());

        // This is stupid, but the return value of update is inclomplete (dependent objects won't be resolved, since only the top level get by id method is called.
        // Call repository get separately
        projectRepository.update(newProject);
        return projectRepository.findById(newProject.getId(), userId);
    }

    @Override
    public Project modifyProject(Project modifiedProject) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.update(modifiedProject);
    }

    @Override
    public Project deleteProjectById(int projectId, Integer userId) throws Exception {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        Project project = projectRepository.findById(projectId, userId);
        projectRepository.delete(projectId);
        return project;
    }

    @Override
    public boolean isProjectPublic(int projectId) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.belongsToPublicProject(projectId);
    }

    @Override
    public Statistic getStatisticsForAllProjects(int userId, Calendar since) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        Timestamp timestamp = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return projectRepository.getStatisticsForVisibleProjects(userId, timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC));
    }

    @Override
    public Statistic getStatisticsForProject(int userId, int projectId, Calendar since) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        Timestamp timestamp = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return projectRepository.getStatisticsForProject(userId, projectId, timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC));
    }

    @Override
    public PaginationResult<User> listFollowersForProject(int projectId, Pageable pageable) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findAllByFollowing(projectId, 0, 0, pageable);
    }

    @Override
    public List<Project> getFollowedProjects(int userId, int count) throws BazaarException {
        projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
        return projectRepository.getFollowedProjects(userId, count);
    }


    @Override
    public List<Requirement> listRequirements(Pageable pageable) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAll(pageable);
    }

    @Override
    public PaginationResult<Requirement> listRequirementsByProject(int projectId, Pageable pageable, int userId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByProject(projectId, pageable, userId);
    }

    @Override
    public PaginationResult<Requirement> listRequirementsByCategory(int categoryId, Pageable pageable, int userId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAllByCategory(categoryId, pageable, userId);
    }

    @Override
    public PaginationResult<Requirement> listAllRequirements(Pageable pageable, int userId) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findAll(pageable, userId);
    }

    @Override
    public Requirement getRequirementById(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.findById(requirementId, userId);
    }

    @Override
    public Requirement createRequirement(Requirement requirement, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        Requirement newRequirement = requirementRepository.add(requirement);
        for (Integer category : requirement.getCategories()) {
            addCategoryTag(newRequirement.getId(), category);
        }
        for (Tag tag : requirement.getTags()) {
            tagRequirement(tag.getId(), newRequirement.getId());
        }
        return getRequirementById(newRequirement.getId(), userId);
    }

    @Override
    public Requirement modifyRequirement(Requirement modifiedRequirement, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        Requirement oldRequirement = getRequirementById(modifiedRequirement.getId(), userId);
        requirementRepository.update(modifiedRequirement);

        if (modifiedRequirement.getCategories() != null) {
            List<Category> oldCategories = listCategoriesByRequirementId(modifiedRequirement.getId(), userId);
            for (Category oldCategory : oldCategories) {
                boolean containCategory = false;
                for (Integer newCategory : modifiedRequirement.getCategories()) {
                    if (oldCategory.getId() == newCategory) {
                        containCategory = true;
                        break;
                    }
                }
                if (!containCategory) {
                    deleteCategoryTag(modifiedRequirement.getId(), oldCategory.getId());
                }
            }
            for (Integer newCategory : modifiedRequirement.getCategories()) {
                boolean containCategory = false;
                for (Category oldCategory : oldCategories) {
                    if (oldCategory.getId() == newCategory) {
                        containCategory = true;
                        break;
                    }
                }
                if (!containCategory) {
                    addCategoryTag(modifiedRequirement.getId(), newCategory);
                }
            }
        }

        // Synchronize tags
        if (modifiedRequirement.getTags() != null) {
            // Check if tags have changed
            for (Tag tag : modifiedRequirement.getTags()) {
                try {
                    Tag internalTag = getTagById(tag.getId());

                    // Check if tag exists (in project)
                    if (internalTag == null || modifiedRequirement.getProjectId() != internalTag.getProjectId()) {
                        tag.setProjectId(modifiedRequirement.getProjectId());
                        tag = createTag(tag);
                    }
                    tagRequirement(tag.getId(), modifiedRequirement.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Remove tags no longer present
            oldRequirement.getTags().stream().filter(tag -> modifiedRequirement.getTags().contains(tag)).forEach(tag -> {
                try {
                    untagRequirement(tag.getId(), oldRequirement.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Synchronize attachments
        if (modifiedRequirement.getAttachments() != null) {
            // Check if tags have changed
            for (Attachment attachment : modifiedRequirement.getAttachments()) {
                try {
                    Attachment internalAttachment = null;
                    if (attachment.getId() != 0) {
                        internalAttachment = getAttachmentById(attachment.getId());
                    }

                    // Check if attachment exists, otherwise create
                    if (internalAttachment == null) {
                        attachment.setRequirementId(modifiedRequirement.getId());
                        attachment.setCreator(getUserById(userId));
                        createAttachment(attachment);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Remove tags no longer present
            oldRequirement.getAttachments().stream().filter(attachment -> modifiedRequirement.getAttachments().contains(attachment)).forEach(attachment -> {
                try {
                    deleteAttachmentById(attachment.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return getRequirementById(modifiedRequirement.getId(), userId);
    }

    @Override
    public Requirement deleteRequirementById(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        Requirement requirement = requirementRepository.findById(requirementId, userId);
        requirementRepository.delete(requirementId);
        return requirement;
    }

    @Override
    public Requirement setRequirementToRealized(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setRealized(requirementId, OffsetDateTime.now());
        return getRequirementById(requirementId, userId);
    }

    @Override
    public Requirement setRequirementToUnRealized(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setRealized(requirementId, null);
        return getRequirementById(requirementId, userId);
    }

    @Override
    public Requirement setUserAsLeadDeveloper(int requirementId, int userId) throws Exception {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        requirementRepository.setLeadDeveloper(requirementId, userId);
        return getRequirementById(requirementId, userId);
    }

    @Override
    public Requirement deleteUserAsLeadDeveloper(int requirementId, int userId) throws Exception {
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
        Timestamp timestamp = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return requirementRepository.getStatisticsForRequirement(userId, requirementId, timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC));
    }

    @Override
    public List<Requirement> getFollowedRequirements(int userId, int count) throws BazaarException {
        requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
        return requirementRepository.getFollowedRequirements(userId, count);
    }

    @Override
    public PaginationResult<Category> listCategoriesByProjectId(int projectId, Pageable pageable, int userId) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.findByProjectId(projectId, pageable, userId);
    }

    @Override
    public List<Category> listCategoriesByRequirementId(int requirementId, int userId) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.findByRequirementId(requirementId, userId);
    }

    @Override
    public Category createCategory(Category category, int userId) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        Category newCategory = categoryRepository.add(category);
        return categoryRepository.findById(newCategory.getId(), userId);
    }

    @Override
    public Category getCategoryById(int categoryId, int userId) throws Exception {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.findById(categoryId, userId);
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
        PaginationResult<Requirement> requirements = listRequirementsByCategory(categoryId, new PageInfo(0, Integer.MAX_VALUE, new HashMap<>()), 0);

        // Get default category
        Category categoryById = getCategoryById(categoryId, userId);
        Project projectById = getProjectById(categoryById.getProjectId(), userId);

        // Move requirements from this category to the default if requirement has no more categories
        for (Requirement requirement : requirements.getElements()) {
            deleteCategoryTag(requirement.getId(), categoryId);
            requirement = getRequirementById(requirement.getId(), userId);
            if (requirement.getCategories().isEmpty()) {
                addCategoryTag(requirement.getId(), projectById.getDefaultCategoryId());
            }
        }

        return categoryRepository.delete(categoryId);
    }

    @Override
    public boolean isCategoryPublic(int categoryId) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.belongsToPublicProject(categoryId);
    }

    @Override
    public Statistic getStatisticsForCategory(int userId, int categoryId, Calendar since) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        Timestamp timestamp = since == null ? new java.sql.Timestamp(0) : new java.sql.Timestamp(since.getTimeInMillis());
        return categoryRepository.getStatisticsForCategory(userId, categoryId, timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC));
    }

    @Override
    public List<Category> getFollowedCategories(int userId, int count) throws BazaarException {
        categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        return categoryRepository.getFollowedCategories(userId, count);
    }

    @Override
    public PaginationResult<User> listFollowersForCategory(int categoryId, Pageable pageable) throws BazaarException {
        userRepository = (userRepository != null) ? userRepository : new UserRepositoryImpl(dslContext);
        return userRepository.findAllByFollowing(0, categoryId, 0, pageable);
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
    public List<Comment> listCommentsByRequirementId(int requirementId) throws BazaarException {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        return commentRepository.findAllByRequirementId(requirementId);
    }

    @Override
    public PaginationResult<Comment> listAllComments(Pageable pageable) throws BazaarException {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        return commentRepository.findAllComments(pageable);
    }

    @Override
    public PaginationResult<Comment> listAllAnswers(Pageable pageable, int userId) throws BazaarException {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        return commentRepository.findAllAnswers(pageable, userId);
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
    public Comment updateComment(Comment comment) throws Exception {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        commentRepository.update(comment);
        return commentRepository.findById(comment.getId());
    }

    @Override
    public Comment deleteCommentById(int commentId) throws Exception {
        commentRepository = (commentRepository != null) ? commentRepository : new CommentRepositoryImpl(dslContext);
        Comment comment = commentRepository.findById(commentId);
        if (commentRepository.hasAnswers(commentId)) {
            comment.setDeleted(true);
            comment.setMessage("[This message has been deleted]");
            commentRepository.update(comment);
        } else {
            commentRepository.delete(commentId);
        }
        return comment;
    }

    @Override
    public CreationStatus followProject(int userId, int projectId) throws BazaarException {
        projectFollowerRepository = (projectFollowerRepository != null) ? projectFollowerRepository : new ProjectFollowerRepositoryImpl(dslContext);
        return projectFollowerRepository.addOrUpdate(ProjectFollower.builder()
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
        return categoryFollowerRepository.addOrUpdate(CategoryFollower.builder()
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
        return requirementFollowerRepository.addOrUpdate(RequirementFollower.builder()
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
        return developerRepository.addOrUpdate(RequirementDeveloper.builder()
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
        requirementCategoryRepository = (requirementCategoryRepository != null) ? requirementCategoryRepository : new RequirementCategoryRepositoryImpl(dslContext);
        requirementCategoryRepository.add(RequirementCategory.builder()
                .categoryId(categoryId)
                .requirementId(requirementId)
                .build()
        );
    }

    @Override
    public void deleteCategoryTag(int requirementId, int categoryId) throws BazaarException {
        requirementCategoryRepository = (requirementCategoryRepository != null) ? requirementCategoryRepository : new RequirementCategoryRepositoryImpl(dslContext);
        requirementCategoryRepository.delete(requirementId, categoryId);
    }

    @Override
    public CreationStatus vote(int userId, int requirementId, boolean isUpVote) throws BazaarException {
        voteRepository = (voteRepository != null) ? voteRepository : new VoteRepositoryImpl(dslContext);
        return voteRepository.addOrUpdate(Vote.builder()
                .requirementId(requirementId)
                .userId(userId)
                .isUpvote(isUpVote)
                .build()
        );
    }

    @Override
    public void unVote(int userId, int requirementId) throws BazaarException {
        voteRepository = (voteRepository != null) ? voteRepository : new VoteRepositoryImpl(dslContext);
        voteRepository.delete(userId, requirementId);
    }

    @Override
    public boolean hasUserVotedForRequirement(int userId, int requirementId) throws BazaarException {
        voteRepository = (voteRepository != null) ? voteRepository : new VoteRepositoryImpl(dslContext);
        return voteRepository.hasUserVotedForRequirement(userId, requirementId);
    }

    @Override
    public List<Role> getRolesByUserId(int userId, Integer context) throws BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        return roleRepository.listRolesOfUser(userId, context);
    }

    @Override
    public List<Role> getParentsForRole(int roleId) throws BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        return roleRepository.listParentsForRole(roleId);
    }

    @Override
    public void createPrivilegeIfNotExists(PrivilegeEnum privilege) throws BazaarException {
        privilegeRepository = (privilegeRepository != null) ? privilegeRepository : new PrivilegeRepositoryImpl(dslContext);

        Privilege privilegeDb = privilegeRepository.findByName(new PrivilegeEnumConverter().to(privilege));
        if (privilegeDb == null) {
            privilegeRepository.add(Privilege.builder().name(privilege).build());
        }

    }

    @Override
    public void addUserToRole(int userId, String roleName, Integer context) throws BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        roleRepository.addUserToRole(userId, roleName, context);
    }

    @Override
    public void updateUserRole(int recordId, int userId, String roleName, Integer context) throws BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        roleRepository.updateUserRole(recordId, userId, roleName, context);
    }

    @Override
    public Role getRoleByName(String role) throws BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        return roleRepository.findByRoleName(role);
    }

    @Override
    public void removeUserFromProject(int userId, Integer context) throws BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        roleRepository.removeUserFromRole(userId, context);
    }

    @Override
    public PersonalisationData getPersonalisationData(int userId, String key, int version) throws BazaarException {
        personalisationDataRepository = (personalisationDataRepository != null) ? personalisationDataRepository : new PersonalisationDataRepositoryImpl(dslContext);
        return personalisationDataRepository.findByKey(userId, version, key);
    }

    @Override
    public void setPersonalisationData(PersonalisationData personalisationData) throws BazaarException {
        personalisationDataRepository = (personalisationDataRepository != null) ? personalisationDataRepository : new PersonalisationDataRepositoryImpl(dslContext);
        personalisationDataRepository.insertOrUpdate(personalisationData);

    }

    @Override
    public EntityOverview getEntitiesForUser(List<String> includes, Pageable pageable, int userId) throws BazaarException {
        //categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
        EntityOverview.Builder result = EntityOverview.builder();
        for (String include : includes) {
            switch (include) {
                case "projects" -> {
                    projectRepository = (projectRepository != null) ? projectRepository : new ProjectRepositoryImpl(dslContext);
                    result.projects(projectRepository.listAllProjectIds(pageable, userId));
                }
                case "requirements" -> {
                    requirementRepository = (requirementRepository != null) ? requirementRepository : new RequirementRepositoryImpl(dslContext);
                    result.requirements(requirementRepository.listAllRequirementIds(pageable, userId));
                }
                case "categories" -> {
                    categoryRepository = (categoryRepository != null) ? categoryRepository : new CategoryRepositoryImpl(dslContext);
                    result.categories(categoryRepository.listAllCategoryIds(pageable, userId));
                }
            }
            //TODO Add Comments/Attachments
        }
        return result.build();
    }

    @Override
    public Feedback createFeedback(Feedback feedback) throws Exception {
        feedbackRepository = (feedbackRepository != null) ? feedbackRepository : new FeedbackRepositoryImpl(dslContext);
        Feedback newFeedback = feedbackRepository.add(feedback);
        return feedbackRepository.findById(newFeedback.getId());
    }

    @Override
    public PaginationResult<Feedback> getFeedbackByProject(int projectId, Pageable pageable) throws BazaarException {
        feedbackRepository = (feedbackRepository != null) ? feedbackRepository : new FeedbackRepositoryImpl(dslContext);
        return feedbackRepository.findAllByProject(projectId, pageable);
    }

    @Override
    public Feedback getFeedbackById(int feedbackId) throws Exception {
        feedbackRepository = (feedbackRepository != null) ? feedbackRepository : new FeedbackRepositoryImpl(dslContext);
        return feedbackRepository.findById(feedbackId);
    }

    @Override
    public Dashboard getDashboardData(int userId, int count) throws BazaarException {
        return Dashboard.builder()
                .projects(getFollowedProjects(userId, count))
                .categories(getFollowedCategories(userId, count))
                .requirements(getFollowedRequirements(userId, count))
                .build();
    }

    @Override
    public Tag getTagById(int id) throws Exception {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        try {
            return tagRepository.findById(id);
        } catch (BazaarException bex) {
            if (bex.getErrorCode() == ErrorCode.NOT_FOUND) {
                return null;
            }
            ExceptionHandler.getInstance().convertAndThrowException(bex);
        }
        return null;
    }

    @Override
    public List<Tag> getTagsByProjectId(int projectId) throws Exception {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        return tagRepository.findByProjectId(projectId);
    }

    @Override
    public Tag createTag(Tag tag) throws BazaarException {
        tagRepository = (tagRepository != null) ? tagRepository : new TagRepositoryImpl(dslContext);
        return tagRepository.add(tag);
    }

    @Override
    public CreationStatus tagRequirement(int tagId, int requirementId) throws BazaarException {
        requirementTagRepository = (requirementTagRepository != null) ? requirementTagRepository : new RequirementTagRepositoryImpl(dslContext);
        return requirementTagRepository.addOrUpdate(RequirementTag.builder()
                .requirementId(requirementId)
                .tagId(tagId)
                .build()
        );
    }

    @Override
    public void untagRequirement(int tagId, int requirementId) throws Exception {
        requirementTagRepository = (requirementTagRepository != null) ? requirementTagRepository : new RequirementTagRepositoryImpl(dslContext);
        requirementTagRepository.delete(tagId, requirementId);
    }

    @Override
    public PaginationResult<ProjectMember> getProjectMembers(int projectId, Pageable pageable) throws
            BazaarException {
        roleRepository = (roleRepository != null) ? roleRepository : new RoleRepositoryImpl(dslContext);
        return roleRepository.listProjectMembers(projectId, pageable);
    }
}
