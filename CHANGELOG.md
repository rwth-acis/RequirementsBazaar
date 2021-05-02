# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Releases prior to v0.7.2 are only documented on
the [GitHub Release Page](https://github.com/rwth-acis/RequirementsBazaar/releases)

## [Unreleased]

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
  new `userContext` attribute [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94) [#96](https://github.com/rwth-acis/RequirementsBazaar/pull/96).
- Add a delete projects endpoint [#100](https://github.com/rwth-acis/RequirementsBazaar/pull/100).
- Add an update comment endpoint [#100](https://github.com/rwth-acis/RequirementsBazaar/pull/100).

### Changed

- Updated all dependencies, most notably las2peer 1.0.0 [#68](https://github.com/rwth-acis/RequirementsBazaar/pull/68)
- Updated las2peer to 1.1.0 and thereby requiring Java 14 [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
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
  parameter [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94) but instead as a direction object strictly defined by an enum [#96](https://github.com/rwth-acis/RequirementsBazaar/pull/96),
- Moved user related information in categories, requirements and projects (isFollower/Developer/Contributor, userVoted)
  into the new `userContext` [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94).

### Removed

- Personalisation endpoints [#94](https://github.com/rwth-acis/RequirementsBazaar/pull/94)

## [0.7.2] - 2017-10-25

See GH Releases
