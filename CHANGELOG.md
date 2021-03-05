# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Releases prior to v0.7.2 are only documented on
the [GitHub Release Page](https://github.com/rwth-acis/RequirementsBazaar/releases)

## [Unreleased]

### Added

- Added new testcases [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)

### Changed

- Updated all dependencies, most notably las2peer 1.0.0 [#68](https://github.com/rwth-acis/RequirementsBazaar/pull/68)
- Updated las2peer to 1.1.0 and thereby requiring Java
  14) [#73](https://github.com/rwth-acis/RequirementsBazaar/pull/73)
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

## [0.7.2] - 2017-10-25

See GH Releases
