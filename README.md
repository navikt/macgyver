[![Deploy to dev and prod](https://github.com/navikt/macgyver/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/navikt/macgyver/actions/workflows/deploy.yml)

# macgyver

Application that fixes stuff like Macgyver for team sykmelding

## Technologies used

* Kotlin
* Ktor
* Gradle
* Junit

#### Requirements

* JDK 17

## FlowChart
This the high level flow of the application

```mermaid
  graph LR;
      macgyver --- macgyver-frontend;
      macgyver --- azure-ad;
      macgyver --- id1[(syfosmregister)];
      macgyver --- PDL;
      macgyver --- oppgave;
      macgyver --- narmesteleder;
```

## Getting started

### Getting github-package-registry packages NAV-IT

Some packages used in this repo is uploaded to the GitHub Package Registry which requires authentication. It can, for
example, be solved like this in Gradle:

```
val githubUser: String by project
val githubPassword: String by project

repositories {
    maven {
        credentials {
            username = githubUser
            password = githubPassword
        }
        setUrl("https://maven.pkg.github.com/navikt/helse-sykepenger-beregning")
    }
}
```

`githubUser` and `githubPassword` can be put into a separate file `~/.gradle/gradle.properties` with the following
content:

```                                                     
githubUser=x-access-token
githubPassword=[token]
```

Replace `[token]` with a personal access token with scope `read:packages`.
See githubs guide [creating-a-personal-access-token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) on
how to create a personal access token.

Alternatively, the variables can be configured via environment variables:

* `ORG_GRADLE_PROJECT_githubUser`
* `ORG_GRADLE_PROJECT_githubPassword`

or the command line:

``` bash
./gradlew -PgithubUser=x-access-token -PgithubPassword=[token]
```

#### Build and run tests

To build locally and run the integration tests you can simply run
``` bash
./gradlew shadowJar
```
or on windows
`gradlew.bat shadowJar`

#### Creating a docker image

Creating a docker image should be as simple as
``` bash
docker build -t macgyver .
```

#### Running a docker image

``` bash 
docker run --rm -it -p 8080:8080 macgyver
```

### Upgrading the gradle wrapper

Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

``` bash 
./gradlew wrapper --gradle-version $gradleVersjon
```

### Contact

This project is maintained by [navikt/teamsykmelding](CODEOWNERS)

Questions and/or feature requests? Please create an
[issue](https://github.com/navikt/macgyver/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997)
