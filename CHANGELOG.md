# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Releases prior to v0.7.2 are only documented on
the [GitHub Release Page](https://github.com/rwth-acis/RequirementsBazaar/releases)

## [Unreleased]

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
