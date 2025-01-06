# Note
This project was developed as part of the SENG302 course at the University of Canterbury.

I was a member of a team of 8 students that worked on this project.

For more details, refer to the course information: 
https://courseinfo.canterbury.ac.nz/GetCourseDetails.aspx?course=SENG302

# Project Overview
GreenThumbs is a one-stop shop for gardeners who want to document, record, and check on their own gardens and plants, make friends and get inspirations from other gardeners.
Users are also able to request services and help from other gardeners in their area, and/or find & apply for nearby gardening-related jobs.

## User accounts

| Account Type     | Email                   | Password  | First Name | Last Name      | Purpose                                              |
| ---------------- | ----------------------- | --------- | ---------- | -------------- | ---------------------------------------------------- |
| Unverified User  | unverifieduser@gmail.com | Testp4$$  | Unverified | User           | User to test registration email with                 |
| Verified Contractor User   | verifieduser@gmail.com   | Testp4$$  | Verified   | User           | Main user for testing. Has access to all features of the app                                |
| Verified User    | userwithgarden@gmail.com | Testp4$$  | Garden     | User           | Friended with the main user, has multiple gardens and multiple service requests (enough for pagination)   |
| Verified User    | searchuser@gmail.com     | Testp4$$  | Search     | User           | User to test searching up on Add Friend              |
| Verified User    | pending@gmail.com        | Testp4$$  | Pending    | Request User   | User who the main user has sent a pending invite to  |
| Verified User    | declined@gmail.com       | Testp4$$  | Declined   | Request User   | User who the main user has sent a declined invite to |
| Verified User    | requesting@gmail.com     | Testp4$$  | Requesting | User           | User who has sent a friend request to the main user  |
| Verified Contractor User    | contractor@gmail.com     | Testp4$$  | Contractor | | User who is registered as a contractor |


## Dependencies
This project requires Java version >= 21, [click here to get the latest stable OpenJDK release (as of time of writing)](https://jdk.java.net/21/)


## Technologies
This project makes use of several technologies that you will have to work with. Here are some helpful links to documentation/resources for the big one:

- [Spring Boot](https://spring.io/projects/spring-boot) - Used to provide http server functionality
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - Used to implement JPA (Java Persistence API) repositories
- [h2](https://www.h2database.com/html/main.html) - Used as an SQL JDBC and embedded database
- [Thymeleaf](https://www.thymeleaf.org/) - A templating engine to render HTML on the server, as opposed to a separate client-side application (such as React)
- [Gradle](https://gradle.org/) - A build tool that greatly simplifies getting application up and running, even managing our dependencies (for those who did SENG202, you can think of Gradle as a Maven replacement)
- [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/3.0.2/gradle-plugin/reference/html/) - Allows us to more easily integrate our Spring Boot application with Gradle
- [GeoApify](https://www.geoapify.com/geocoding-api?gad_source=1&gclid=CjwKCAjwgdayBhBQEiwAXhMxtqCOfyIqAuHZnyYCZiPwQLrvK5UyeuC1izotOWqjyDrgnc7-vC2mRxoCGAIQAvD_BwE) Used for garden location autocomplete.
- [Azure Content Moderation](https://learn.microsoft.com/en-us/azure/ai-services/content-moderator/) - API for handling content moderation.
- [OpenWeatherMap](https://openweathermap.org/api) - API for getting the weather history, forecast and current weather.
- [SpringBoot Starter Mail](https://docs.spring.io/spring-boot/reference/io/email.html) - Used with Gmail to handle email services within the application.
- [Cucumber](https://cucumber.io/docs/cucumber/) - Used to verify the code written matches the acceptance criteria expected by the product owner
- [Flying Saucer](https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html) - Pdf generation for invoices
## Quickstart Guide

### Building and running the project with Gradle
We'll give some steps here for building and running via the commandline, though IDEs such as IntelliJ will typically 
have a 'gradle' tab somewhere that you can use to perform the same actions with as well. 

#### 1 - Running the project
From the root directory ...

On Linux:
```
./gradlew bootRun
```

On Windows:
```
gradlew bootRun
```

By default, the application will run on local port 8080 ('http://localhost:8080')

### Accessing the production instance of the application
You can load up your preferred browser and connect to the application at
('https://csse-seng302-team200.canterbury.ac.nz/prod/').

## How to run tests
on Linux:
- Unit test suite
>  ./gradlew test
- Integration test suite
>  ./gradlew integration
- Cucumber test suite
>  ./gradlew cucumber
- Full suite
>  ./gradlew check
> 
on Windows:
- Unit test suite
>  gradlew test
- Integration test suite
>  gradlew integration
- Cucumber test suite
>  gradlew cucumber
- Full suite
>  gradlew check
> 

## Coverage and Code Smells
We use SonarQube for code coverage and code smells.
It is hosted at http://sonarqube.csse.canterbury.ac.nz/.

If you want the coverage stats for only unit, integration, or cucumber tests (for eg.) you can run the respective tasks with IntelliJ's coverage tool.


## Setting up environment variables
### Here is an example of the application.properties file that is required to run locally.
```


# Application name
spring.application.name=gardeners-grove

# DataSource configuration
# spring.datasource.url=jdbc:h2:file:./data/gardenersgrove;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA database platform
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.defer-datasource-initialization=true

# H2 console configuration
spring.h2.console.enabled=true
spring.h2.console.path=/h2

# SQL initialization mode
spring.sql.init.mode=embedded

# Enable or disable Spring Security
security.enabled=true

server.servlet.session.persistent=false

spring.servlet.multipart.max-file-size=-1
spring.servlet.multipart.max-request-size=-1

# Mail
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.port=587
spring.mail.host=smtp.gmail.com
spring.mail.properties.mail.transport.protocol=smtp
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# enable bean overriding, needed for localeResolver and addInterceptor
spring.main.allow-bean-definition-overriding=true

# Allows accents etc to show up properly
spring.messages.encoding=UTF-8
```
### Here are the exports of the environment variables
> export MAIL_PASSWORD='{gmail_app_password}'

> export MAIL_USERNAME='{gmail_username}'

> export WEATHER_KEY='{your_OpenWeather_api_key}'

> export LOCATION_KEY='{your_GeoApify_api_key}'

> export AZURE_KEY='{your_azure_api_key}'

> export AZURE_ENDPOINT='{your_azure_endpoint}'



## Contributors
- SENG302 Teaching Team
- Team Software Sprouts

## Information about included build files/scripts
Whilst these scripts and files will not be of use until later in the course when you set up continuous integration (CI) we have included files with default behaviour, and some for reference.
- `deployment-files-fyi/nginx/sites-available.conf`
  - Reference file showing the VMs NGINX config.
- `deployment-files-fyi/systemd-service/production.service`
  - Reference file showing the production environment service configuration. [See here for more information about .service files](https://www.shellhacks.com/systemd-service-file-example/)
- `deployment-files-fyi/systemd-service/staging.service`
  - Reference file showing the staging environment service configuration.
- `runner-scripts/production.sh`
  - Deployment shell script for running the production environment on a VM.
- `runner-scripts/staging.sh`
  - Deployment shell script for running the staging environment on a VM.
- `.gitlab-ci.yml`
  - The (GitLab specific) CI script for the project. This will cause your pipelines to fail/timeout until gitlab-runners are set up so feel free to ignore these for now. [For more information refer to GitLab's Documentation Here](https://docs.gitlab.com/ee/ci/yaml/gitlab_ci_yaml.html)