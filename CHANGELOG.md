# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Releases prior to v0.7.2 are only documented on
the [GitHub Release Page](https://github.com/rwth-acis/RequirementsBazaar/releases)

## [Unreleased]

## [0.17.1] - 2023-06-13

### Added

- Added possibility to edit / delete labels
  [#181](https://github.com/rwth-acis/RequirementsBazaar/pull/181)

## [0.17.0] - 2023-05-10

### Added

- Added gamification
  [d758be2b4a66bd943665c097eee963c65a7dc68e](https://github.com/rwth-acis/RequirementsBazaar/commit/d758be2b4a66bd943665c097eee963c65a7dc68e)
- Added priority labels
  [#179](https://github.com/rwth-acis/RequirementsBazaar/pull/179)

## [0.16.0] - 2022-10-05

### Changed

- Upgraded to las2peer 1.2.3 and Java 17
  [#175](https://github.com/rwth-acis/RequirementsBazaar/pull/175)
- Fixed bug which caused new users being unable to login
  [#173](https://github.com/rwth-acis/RequirementsBazaar/issues/173)

## [0.15.0] - 2022-08-07

### Added
- Add API for moving requirements to other project and category
  [#171](https://github.com/rwth-acis/RequirementsBazaar/pull/171)
- Return user authorization information in API responses (`isDeleteAllowed`, `isMoveAllowed`)
  [#172](https://github.com/rwth-acis/RequirementsBazaar/pull/172)


## [0.13.1] - 2022-06-05

### Changed
- Fixed SQL query bug error when loading last activity of a requirement
  [#170](https://github.com/rwth-acis/RequirementsBazaar/pull/170)


## [0.13.0] - 2022-05-11

### Added
- Add information about the user who last updated a requirement to API responses (in the `lastUpdatingUser` property)
  [#164](https://github.com/rwth-acis/RequirementsBazaar/pull/164)
- Add information about the user who performed the last activity on a requirement to API responses (in the `lastActivityUser` property).
  Editing, commenting, and adding attachments are examples for activities.
  [#165](https://github.com/rwth-acis/RequirementsBazaar/pull/165)

### Changed
- Fixed authorization error which caused members of a project not being able to start (or stop) developing a requirement
  [#167](https://github.com/rwth-acis/RequirementsBazaar/pull/167)
- Migrate to new OIDC domain (auth.las2peer.org)
  [#168](https://github.com/rwth-acis/RequirementsBazaar/pull/168)

## [0.12.4] - 2022-04-26

### Changed
- Creator of a requirement is always allowed to delete the requirement (independent of roles and privileges)
  [#159](https://github.com/rwth-acis/RequirementsBazaar/pull/159)
- Stop redirecting to requirement resource after DELETE on user vote (`/bazaar/requirements/{id}/votes`).
  This caused some clients which are strictly implementing HTTP spec to delete the requirement too when the request
  should only remove the vote of a user. See [#141](https://github.com/rwth-acis/RequirementsBazaar/pull/141)


## [0.12.3] - 2022-04-23

### Changed
- Stopped dispatching 'project update' notifications when GitHub webhook updates project
  [#154](https://github.com/rwth-acis/RequirementsBazaar/pull/154)
- Fixed not working pagination for `/categories/{id}/requirements` and `/requirements` resources. The `page` and
  `perPage` query params were ignored before which resulted in all requirements being returned with the first request.
  [#156](https://github.com/rwth-acis/RequirementsBazaar/pull/156)

## [0.12.1] - 2022-04-10

### Changed

- Fixed wrong requirement link in email notifications
  [#151](https://github.com/rwth-acis/RequirementsBazaar/pull/151)


## [0.12.0] - 2022-04-03

### Added

- Requirements Bazaar can have a 'linked Twitter account' which can be used for tweeting about new projects, etc. The
  acts as an OAuth 2.0 application for Twitter and a Twitter user can give the service access
  to an account. The internal `TweetDispatcher` refreshes the access token for the Twitter API automatically when it expires.
  [#143](https://github.com/rwth-acis/RequirementsBazaar/pull/143)
- If a Twitter account is linked, Requirements Bazaar publishes a weekly Tweet (on Sunday afternoon, 4 pm) about new projects
  [#146](https://github.com/rwth-acis/RequirementsBazaar/pull/146)

## [0.11.1] - 2022-02-10

### Changed

- Fix error response for /projects/{id}/members when project has no members
  [#136](https://github.com/rwth-acis/RequirementsBazaar/pull/139)


## [0.11.0] - 2022-01-29

### Added

- Add endpoint to get user statistics for a certain time period (active users, new users)
  [#136](https://github.com/rwth-acis/RequirementsBazaar/pull/136)

## [0.10.0] - 2022-01-22

### Added

- Add webhook resource to process events from GitHub repositories connected to a project
  [#132](https://github.com/rwth-acis/RequirementsBazaar/pull/132)
  - update a project when new GiHub release is available
  - links GitHub issues to requirements if they are referenced by a Requirements Bazaar URL
  - updates requirements when the linked issue changes its status
  - Users need to use the project name with whitespaces removed as secret in the webhook

## [0.9.3] - 2021-12-21

### Added

- Build generates a script to start the las2peer node in debug mode for easier debugging [a0ae55f](https://github.com/rwth-acis/RequirementsBazaar/commit/a0ae55f6e08c57a93ed2cf4533a47151e2a24ef4)

### Changed

- Fixed bug in project member API which allowed adding the same users multiple times to the same project.
  Also, fixed PUT request to update the role of members if they already exists
  [#130](https://github.com/rwth-acis/RequirementsBazaar/pull/130)

## [0.9.2] - 2021-11-16

### Changed

- Fixed project search API which did not return projects for incomplete words
  [#121](https://github.com/rwth-acis/RequirementsBazaar/pull/124)

## [0.9.1] - 2021-09-26

### Changed

- Fixed permission for realize and unrealize endpoints

## [0.9.0] - 2021-09-08

### Added

- Added new testcases [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
- Added endpoints to provide anonymous feedback which can be read by project
  admins [#85](https://github.com/rwth-acis/RequirementsBazaar/pull/85)
- Added endpoint to search for users by name or email [#90](https://github.com/rwth-acis/RequirementsBazaar/pull/90)
- Categories, projects and requirements now have a `lastActivity`
  attribute [#91](https://github.com/rwth-acis/RequirementsBazaar/pull/91).
- Categories, projects and requirements now have a `userContext` encapsuling the dynamic user related information (
  permissions, votes, contribution) [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94).
- If a user is a member of a project the respective role is now returned in the`usertRole` attribute of the
  new `userContext`
  attribute [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94) [#96](https://github.com/rwth-acis/RequirementsBazaar/pull/96)
  .
- Add a delete projects endpoint [#100](https://github.com/rwth-acis/RequirementsBazaar/pull/100).
- Add an update comment endpoint [#100](https://github.com/rwth-acis/RequirementsBazaar/pull/100).
- Redacted comments now have a deleted flag [#103](https://github.com/rwth-acis/RequirementsBazaar/pull/103).
- Added a user dashboard listing the last 10 most recent active followed projects, categories and
  requirements [#106](https://github.com/rwth-acis/RequirementsBazaar/pull/106).
- Requirements can now have arbitrary tags next to the categories. Tags are a project scoped
  entity [#108](https://github.com/rwth-acis/RequirementsBazaar/pull/108).
- Projects, categories and requirements now have an `additionalProperties` object which allows storing and providing
  additional context as plain json [#109](https://github.com/rwth-acis/RequirementsBazaar/pull/109).
- The projects endpoint now has a recursive flag which includes projects with requirements matching the search term into
  the results [#116](https://github.com/rwth-acis/RequirementsBazaar/pull/116)

### Changed

- Updated all dependencies, most notably las2peer 1.0.0 [#68](https://github.com/rwth-acis/RequirementsBazaar/pull/68)
- Updated las2peer to 1.1.0 and thereby requiring Java 14 [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
- Updated las2peer to 1.1.2 [#115](https://github.com/rwth-acis/RequirementsBazaar/pull/115)
- Changed buildsystem to use gradle [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
- Automatically generate jooq code from migration files at build
  time [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
- Replaced vtor with Java Bean Validation (this implies changes to the API documentation as these annotations included)
  [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
- Fixed a bug in the swagger documentation where `GET /categories/{categoryId/requirements` was incorrectly annotated to
  return a list of categories instead of a list of
  requirements [#78](https://github.com/rwth-acis/RequirementsBazaar/pull/78)
- Revised and automated release process [#78](https://github.com/rwth-acis/RequirementsBazaar/pull/78)
- Split leading `+`/`-`from sort parameter in favour of a `sortDirection` (ASC/DESC) parameter.
  [#82](https://github.com/rwth-acis/RequirementsBazaar/pull/82)
- Order by name now implies natural sorting [#82](https://github.com/rwth-acis/RequirementsBazaar/pull/82)
- Remove static code from data classes and generate getter/setters and builder with lombok. This renames the `category`
  attribute in `EntityContext` to `categories` [#83](https://github.com/rwth-acis/RequirementsBazaar/pull/82)
- Rework permission system as lined out
  in [Privileges](docs/Privileges.md) [#85](https://github.com/rwth-acis/RequirementsBazaar/pull/85)
- Category `leader` attribute has been renamed
  to `creator` [#85](https://github.com/rwth-acis/RequirementsBazaar/pull/85)
- Voting now returns a 303 response with reference to the modified
  object [#85](https://github.com/rwth-acis/RequirementsBazaar/pull/85)
- Restrict user attributes normally returned to id, username, profile image and
  las2peerid [#90](https://github.com/rwth-acis/RequirementsBazaar/pull/90)
- Requirements no longer return the category objects in the `categories` attribute but a list of category
  ids [#91](https://github.com/rwth-acis/RequirementsBazaar/pull/91).
- Vote direction can no longer be provided as a query
  parameter [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94) but instead as a direction object strictly
  defined by an enum [#96](https://github.com/rwth-acis/RequirementsBazaar/pull/96),
- Moved user related information in categories, requirements and projects (isFollower/Developer/Contributor, userVoted)
  into the new `userContext` [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94).
- Comments with existing responses will no longer be deleted but
  redacted [#103](https://github.com/rwth-acis/RequirementsBazaar/pull/103).
- Comments for a requirement are no longer paginated but instead return all
  comments [#103](https://github.com/rwth-acis/RequirementsBazaar/pull/103).
- Postgres is now the backend database [#112](https://github.com/rwth-acis/RequirementsBazaar/pull/112)
- Attachments are now updated via PUT towards the requirement
  endpoint [#114](https://github.com/rwth-acis/RequirementsBazaar/pull/114).
- All timestamps now contain timezone information [#115](https://github.com/rwth-acis/RequirementsBazaar/pull/115)

### Removed

- Personalisation endpoints [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94)
- Attachment endpoint [#114](https://github.com/rwth-acis/RequirementsBazaar/pull/114)

## [0.7.2] - 2017-10-25

See GH Releases
