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
//import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
//import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
//
//import java.sql.Connection;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Random;
//
///**
// * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
// * @since 6/14/2014
// */
//public class DALFacadeMockImpl implements DALFacade {
//
//    //region Fields
//
//    List<User> userList = new ArrayList<User>();
//    List<Project> projectList = new ArrayList<Project>();
//    List<Component> componentList = new ArrayList<Component>();
//    List<Requirement> requirementList = new ArrayList<Requirement>();
//    List<Comment> commentList = new ArrayList<Comment>();
//    List<Attachment> attachmentList = new ArrayList<Attachment>();
//    List<Vote> voteList = new ArrayList<Vote>();
//    List<RequirementComponent> tagList = new ArrayList<RequirementComponent>();
//    List<RequirementDeveloper> developerList = new ArrayList<RequirementDeveloper>();
//    List<RequirementFollower> followerList = new ArrayList<RequirementFollower>();
//
//    //endregion
//
//    //region Private helper methods
//
//    private int calcPaginationFrom(Pageable pageable){
//
//        return Math.max(0,pageable.getOffset() + pageable.getPageNumber()* pageable.getPageSize());
//
//    }
//    private int calcPaginationTo(Pageable pageable, int listSize){
//        return Math.min(listSize,(pageable.getPageNumber()+1)*pageable.getPageSize());
//    }
//
//    private List<Component> getComponents(int requirementId) {
//        List<Component> components = new ArrayList<Component>();
//
//        for (RequirementComponent tag : tagList) {
//            if (tag.getRequirementId() == requirementId) {
//                for (Component component : componentList) {
//                    if (component.getId() == tag.getComponentId())
//                        components.add(component);
//                }
//
//            }
//        }
//        return components;
//    }
//
//    private List<User> getContributors(List<Attachment> attachments) {
//        List<User> users = new ArrayList<User>();
//        List<Integer> userIdList = new ArrayList<Integer>();
//
//        for (Attachment attachment : attachments) {
//            userIdList.add(attachment.getCreatorId());
//        }
//
//        for (User user : userList) {
//            if (userIdList.contains(user.getId()))
//                users.add(user);
//        }
//
//        return users;
//    }
//
//    private List<User> getFollowers(int requirementId) {
//        List<User> users = new ArrayList<User>();
//
//        for (RequirementFollower follower : followerList) {
//            if (follower.getRequirementId() == requirementId) {
//                for (User user : userList) {
//                    if (user.getId() == follower.getUserId())
//                        users.add(user);
//                }
//
//            }
//        }
//        return users;
//
//    }
//
//    private List<User> getDevelopers(int requirementId) {
//        List<User> users = new ArrayList<User>();
//
//        for (RequirementDeveloper developer : developerList) {
//            if (developer.getRequirementId() == requirementId) {
//                for (User user : userList) {
//                    if (user.getId() == developer.getUserId())
//                        users.add(user);
//                }
//
//            }
//        }
//        return users;
//
//    }
//
//    private List<Attachment> getAttachments(int requirementId) {
//        List<Attachment> attachments = new ArrayList<Attachment>();
//
//        for (Attachment attachment : attachmentList) {
//            if (attachment.getRequirementId() == requirementId)
//                attachments.add(attachment);
//        }
//
//        return attachments;
//    }
//    //endregion
//
//    @Override
//    public Connection getConnection() {
//        return null;
//    }
//
//    @Override
//    public int createUser(User user) {
//        userList.add(user);
//        return 0;
//    }
//
//    @Override
//    public void modifyUser(User modifiedUser) {
//
//    }
//
//    @Override
//    public User getUserById(int userId) {
////        return User.geBuilder("test@test.de")
////                .admin(false)
////                .firstName("Elek")
////                .lastName("Test")
////                .id(userId)
////                .userId(2222)
////                .build();
//
//        for (User user : userList) {
//            if (user.getId() == userId)
//                return user;
//        }
//        return null;
//    }
//
//    @Override
//    public Integer getUserIdByLAS2PeerId(long las2PeerId) throws Exception {
//        return null;
//    }
//
//    @Override
//    public List<Project> listPublicProjects(Pageable pageable) {
//        return projectList.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,projectList.size()));
////        return Arrays.asList(
////                Project.getBuilder("Proj1").description("Test project 1").id(1).visibility(Project.ProjectVisibility.PRIVATE).leaderId(1).build(),
////                Project.getBuilder("Proj2").description("Test project 2 + SEARCHSTR").visibility(Project.ProjectVisibility.PRIVATE).leaderId(1).id(2).build(),
////                Project.getBuilder("Proj3").description("Test project 3 + SEARCHSTR").visibility(Project.ProjectVisibility.PRIVATE).leaderId(1).id(2).build()
////        );
//    }
//
//    @Override
//    public List<Project> listPublicAndAuthorizedProjects(PageInfo pageable, long userId) throws BazaarException {
//        return null;
//    }
//
//    @Override
//    public List<Project> searchProjects(String searchTerm, Pageable pageable) {
//        List<Project> toReturn = new ArrayList<Project>();
//        for (Project project : projectList) {
//            if (project.getDescription().toUpperCase().contains(searchTerm.toUpperCase()))
//                toReturn.add(project);
//        }
//        return toReturn.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,toReturn.size()));
//    }
//
//    @Override
//    public Project getProjectById(int projectId) {
//
//        for (Project project : projectList) {
//            if (project.getId() == projectId)
//                return project;
//        }
//        return null;
////        return Project.getBuilder("ProjById").description("Test project").id(projectId).visibility(Project.ProjectVisibility.PRIVATE).leaderId(1).build();
//    }
//
//    @Override
//    public int createProject(Project project) {
//        projectList.add(project);
//        return 0;
//    }
//
//    @Override
//    public void modifyProject(Project modifiedProject) {
//
//    }
//
//    @Override
//    public boolean isProjectPublic(int projectId) throws Exception {
//        return false;
//    }
//
//    @Override
//    public List<Requirement> listRequirements(Pageable pageable) {
//        return requirementList.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,requirementList.size()));
//
////        return Arrays.asList(
////                Requirement.getBuilder("Req1").id(1).description("Requirement details 1").projectId(1).leadDeveloperId(1).creatorId(2).build(),
////                Requirement.getBuilder("Req2").id(2).description("Requirement details 2").projectId(1).leadDeveloperId(1).creatorId(2).build(),
////                Requirement.getBuilder("Req3").id(3).description("Requirement details 3").projectId(2).leadDeveloperId(1).creatorId(2).build()
////        );
//    }
//
//    @Override
//    public List<Requirement> listRequirementsByProject(int projectId, Pageable pageable) {
//        List<Requirement> toReturn = new ArrayList<Requirement>();
//        for (Requirement req : requirementList) {
//            if (req.getProjectId() == projectId)
//                toReturn.add(req);
//        }
//        return toReturn.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,toReturn.size()));
//
////        return Arrays.asList(
////                Requirement.getBuilder("Req1").id(1).description("Requirement details 1").projectId(projectId).build(),
////                Requirement.getBuilder("Req2").id(2).description("Requirement details 2").projectId(projectId).build()
////        );
//    }
//
//    @Override
//    public List<Requirement> listRequirementsByComponent(int componentId, Pageable pageable) {
//        List<Integer> requirementIdList = new ArrayList<Integer>();
//        for (RequirementComponent tag : tagList) {
//            if (tag.getComponentId() == componentId)
//                requirementIdList.add(tag.getRequirementId());
//        }
//
//        List<Requirement> toReturn = new ArrayList<Requirement>();
//        for (Requirement req : requirementList) {
//            if (requirementIdList.contains(req.getId()))
//                toReturn.add(req);
//        }
//        return toReturn.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,toReturn.size()));
//    }
//
//    @Override
//    public List<Requirement> searchRequirements(String searchTerm, Pageable pageable) {
//        List<Requirement> toReturn = new ArrayList<Requirement>();
//        for (Requirement req : requirementList) {
//            if (req.getDescription().toUpperCase().contains(searchTerm.toUpperCase()))
//                toReturn.add(req);
//        }
//        return toReturn.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,toReturn.size()));
//    }
//
//    @Override
//    public RequirementEx getRequirementById(int requirementId) {
//        Requirement requirement = null;
//
//        for (Requirement req : requirementList) {
//            if (req.getId() == requirementId)
//                requirement = req;
//        }
//
//        if (requirement != null) {
//            List<Attachment> attachments = getAttachments(requirementId);
//            List<User> developers = getDevelopers(requirementId);
//            List<User> followers = getFollowers(requirementId);
//            List<User> contributors = getContributors(attachments);
//            List<Component> components = getComponents(requirementId);
//
//            return RequirementEx.getBuilder(requirement.getName())
//                    .id(requirement.getId())
//                    .description(requirement.getDescription())
//                    .projectId(requirement.getProjectId())
//                    .leadDeveloperId(requirement.getLeadDeveloperId())
//                    .creatorId(requirement.getCreatorId())
//                    .creator(getUserById(requirement.getCreatorId()))
//                    .leadDeveloper(getUserById(requirement.getLeadDeveloperId()))
//                    .developers(developers)
//                    .followers(followers)
//                    .contributors(contributors)
//                    .attachments(attachments)
//                    .components(components)
//                    .build();
//        }
//
//        return null;
//    }
//
//
//    @Override
//    public int createRequirement(Requirement requirement, int componentId) {
//        requirementList.add(requirement);
//        return 0;
//    }
//
//    @Override
//    public void modifyRequirement(Requirement modifiedRequirement) {
//
//    }
//
//    @Override
//    public void deleteRequirementById(int requirementId) {
//        //Delete component tags
//        Iterator<RequirementComponent> tagIterator = tagList.iterator();
//        while (tagIterator.hasNext()) {
//            RequirementComponent tag = tagIterator.next();
//
//            if (tag.getRequirementId() == requirementId)
//                tagIterator.remove();
//        }
//
//        //Delete followers
//        Iterator<RequirementFollower> followerIterator = followerList.iterator();
//        while (followerIterator.hasNext()) {
//            RequirementFollower follower = followerIterator.next();
//
//            if (follower.getRequirementId() == requirementId)
//                followerIterator.remove();
//        }
//
//        //Delete developers
//        Iterator<RequirementDeveloper> developerIterator = developerList.iterator();
//        while (developerIterator.hasNext()) {
//            RequirementDeveloper developer = developerIterator.next();
//
//            if (developer.getRequirementId() == requirementId)
//                developerIterator.remove();
//        }
//        //Delete attachments
//        Iterator<Attachment> attachmentIterator = attachmentList.iterator();
//        while (attachmentIterator.hasNext()) {
//            Attachment attachment = attachmentIterator.next();
//
//            if (attachment.getRequirementId() == requirementId)
//                attachmentIterator.remove();
//        }
//
//        //Delete comments
//        Iterator<Comment> commentIterator = commentList.iterator();
//        while (commentIterator.hasNext()) {
//            Comment comment = commentIterator.next();
//
//            if (comment.getRequirementId() == requirementId)
//                commentIterator.remove();
//        }
//
//        //Delete votes
//        Iterator<Vote> voteIterator = voteList.iterator();
//        while (voteIterator.hasNext()) {
//            Vote vote = voteIterator.next();
//
//            if (vote.getRequirementId() == requirementId)
//                voteIterator.remove();
//        }
//
//        //Delete requirement itself
//        Iterator<Requirement> itr = requirementList.iterator();
//        while (itr.hasNext()) {
//            Requirement requirement = itr.next();
//
//            if (requirement.getId() == requirementId)
//                itr.remove();
//        }
//    }
//
//    @Override
//    public List<Component> listComponentsByProjectId(int projectId, Pageable pageable) {
//        Component.getBuilder("dd").projectId(2);
//        List<Component> toReturn = new ArrayList<Component>();
//        for (Component component : componentList) {
//            if (component.getProjectId() == projectId)
//                toReturn.add(component);
//        }
//        return toReturn.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,toReturn.size()));
//    }
//
//    @Override
//    public int createComponent(Component component) {
//        componentList.add(component);
//        return 0;
//    }
//
//    @Override
//    public void modifyComponent(Component component) {
//
//    }
//
//    @Override
//    public void deleteComponentById(int componentId) {
//        Integer index = null;
//        for (Component comp : componentList) {
//            if (comp.getId() == componentId)
//                index = componentList.indexOf(comp);
//        }
//
//        componentList.remove(index);
//    }
//
//    @Override
//    public int createAttachment(Attachment attachment) {
//        attachmentList.add(attachment);
//        return 0;
//    }
//
//    @Override
//    public void deleteAttachmentById(int attachmentId) {
//        Integer index = null;
//        for (Attachment attachment : attachmentList) {
//            if (attachment.getId() == attachmentId)
//                index = attachmentList.indexOf(attachment);
//        }
//
//        attachmentList.remove(index);
//    }
//
//    @Override
//    public List<Comment> listCommentsByRequirementId(int requirementId, Pageable pageable) {
//        List<Comment> toReturn = new ArrayList<Comment>();
//        for (Comment comment : commentList) {
//            if (comment.getRequirementId() == requirementId)
//                toReturn.add(comment);
//        }
//        return toReturn.subList(calcPaginationFrom(pageable),calcPaginationTo(pageable,toReturn.size()));
//    }
//
//    @Override
//    public int createComment(Comment comment) {
//        commentList.add(comment);
//        return 0;
//    }
//
//    @Override
//    public void deleteCommentById(int commentId) {
//        Iterator<Comment> itr = commentList.iterator();
//        while (itr.hasNext()) {
//            Comment comment = itr.next();
//
//            if (comment.getId() == commentId)
//                itr.remove();
//        }
//    }
//
//    @Override
//    public void follow(int userId, int requirementId) {
//        RequirementFollower follower = RequirementFollower.getBuilder().id(new Random().nextInt()).userId(userId).requirementId(requirementId).build();
//        followerList.add(follower);
//    }
//
//    @Override
//    public void unFollowRequirement(int userId, int requirementId) {
//        Iterator<RequirementFollower> itr = followerList.iterator();
//        while (itr.hasNext()) {
//            RequirementFollower follower = itr.next();
//
//            if (follower.getUserId() == userId && follower.getRequirementId() == requirementId)
//                itr.remove();
//        }
//    }
//
//    @Override
//    public void wantToDevelop(int userId, int requirementId) {
//        RequirementDeveloper developer = RequirementDeveloper.getBuilder().id(new Random().nextInt()).userId(userId).requirementId(requirementId).build();
//
//        developerList.add(developer);
//    }
//
//    @Override
//    public void notWantToDevelop(int userId, int requirementId) {
//        Iterator<RequirementDeveloper> itr = developerList.iterator();
//        while (itr.hasNext()) {
//            RequirementDeveloper developer = itr.next();
//
//            if (developer.getUserId() == userId && developer.getRequirementId() == requirementId)
//                itr.remove();
//        }
//    }
//
//
//
//    @Override
//    public void addComponentTag(int requirementId, int componentId) {
//        RequirementComponent tag = RequirementComponent.getBuilder(componentId).requirementId(requirementId).id(new Random().nextInt()).build();
//
//        tagList.add(tag);
//    }
//
//    @Override
//    public void deleteComponentTag(int requirementId, int componentId) {
//        Iterator<RequirementComponent> itr = tagList.iterator();
//        while (itr.hasNext()) {
//            RequirementComponent tag = itr.next();
//
//            if (tag.getRequirementId() == requirementId && tag.getComponentId() == componentId)
//                itr.remove();
//        }
//    }
//
//    @Override
//    public void vote(int userId, int requirementId, boolean isUpVote) {
//        Vote vote = Vote.getBuilder().id(new Random().nextInt()).requirementId(requirementId).userId(userId).isUpvote(isUpVote).build();
//
//       voteList.add(vote);
//    }
//
//    @Override
//    public void unVote(int userId, int requirementId) {
//        Iterator<Vote> itr = voteList.iterator();
//        while (itr.hasNext()) {
//            Vote vote = itr.next();
//
//            if (vote.getRequirementId() == requirementId && vote.getUserId() == userId)
//                itr.remove();
//        }
//    }
//
//    @Override
//    public boolean hasUserVotedForRequirement(int userId, int requirementId) {
//        for (Vote vote : voteList) {
//            if (vote.getRequirementId() == requirementId && vote.getUserId() == userId)
//                return true;
//        }
//        return false;
//    }
//
//    @Override
//    public List<Role> getRolesByUserId(int userId) throws BazaarException {
//        return null;
//    }
//
//    @Override
//    public List<Role> getParentsForRole(int roleId) throws BazaarException {
//        return null;
//    }
//
//    @Override
//    public void createPrivilegeIfNotExists(PrivilegeEnum privilege) throws BazaarException {
//
//    }
//
//	@Override
//	public Component getComponentById(int componentId) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}
