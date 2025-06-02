# Volleyball Referee API

[![Java CI with Gradle](https://github.com/GuillaumeVT/volleyball-referee-api/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/GuillaumeVT/volleyball-referee-api/actions/workflows/gradle.yml)

Volleyball Referee API is a Spring Boot application, part of the [Volleyball Referee system][vbr], and providing REST endpoints.

### Manage your data

- For 6x6, 4x4, beach and snow volleyball
- Leagues
- Matches (scheduled, live and completed)
- Teams
- Rules
- Account and friends

### Search matches

- By date
- By team, league or referee name
- Being refereed in real-time by another user

### View matches

- Being refereed in real-time by another user
- Download score sheets
- Download league rankings

### Build

Build the API Docker image from this directory with `./.scripts/build.sh`

### Associated applications

- [Volleyball Referee Android app][play-store]
- [Volleyball Referee Angular app][web]

[vbr]: https://www.facebook.com/VolleyballReferee/

[web]: https://volleyball-referee.com

[play-store]: https://play.google.com/store/apps/details?id=com.tonkar.volleyballreferee&hl=en_GB
