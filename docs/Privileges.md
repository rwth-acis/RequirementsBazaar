# Privileges

With Version 0.9 the privilege model has been extended and partially redesigned.

## Roles
There are 6 roles, which inherit the privileges of the roles mentioned before. The `Project.*` roles are scoped to a specific project.

1. Anonymous
2. Logged in user
3. Project Member (scoped)
4. Project Manager (scoped)
5. Project Admin (scoped)
6. System Administrator

## Privilege Map
|name          |name                       |
|--------------|---------------------------|
|Anonymous     |Read_PUBLIC_ATTACHMENT     |
|Anonymous     |Read_PUBLIC_CATEGORY       |
|Anonymous     |Read_PUBLIC_COMMENT        |
|Anonymous     |Read_PUBLIC_PROJECT        |
|Anonymous     |Read_PUBLIC_REQUIREMENT    |
|LoggedInUser  |Create_ATTACHMENT          |
|LoggedInUser  |Create_COMMENT             |
|LoggedInUser  |Create_FOLLOW              |
|LoggedInUser  |Create_PERSONALISATION_DATA|
|LoggedInUser  |Create_PROJECT             |
|LoggedInUser  |Create_REQUIREMENT         |
|LoggedInUser  |Create_VOTE                |
|LoggedInUser  |Delete_FOLLOW              |
|LoggedInUser  |Delete_VOTE                |
|LoggedInUser  |Read_PERSONALISATION_DATA  |
|ProjectAdmin  |Modify_CATEGORY            |
|ProjectAdmin  |Modify_PROJECT             |
|ProjectManager|Create_CATEGORY            |
|ProjectManager|Modify_ATTACHMENT          |
|ProjectManager|Modify_COMMENT             |
|ProjectManager|Modify_REQUIREMENT         |
|ProjectManager|Promote_USER               |
|ProjectManager|Read_FEEDBACK              |
|ProjectMember |Create_DEVELOP             |
|ProjectMember |Delete_DEVELOP             |
|ProjectMember |Read_ATTACHMENT            |
|ProjectMember |Read_CATEGORY              |
|ProjectMember |Read_COMMENT               |
|ProjectMember |Read_PROJECT               |
|ProjectMember |Read_REQUIREMENT           |
|ProjectMember |Realize_REQUIREMENT        |
