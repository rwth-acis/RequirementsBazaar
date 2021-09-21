# Requirements Bazaar

## Idea

In years of research at RWTH Aachen we have developed and actually operated an **Open Innovation Platform for gathering requirements** for prototypes in large international academic projects. The last version of the current product is available under http://requirements-bazaar.org . End-users can enter their requirements by providing short descriptions including user stories, screenshots and other images. The requirements can then be shared amongst its users. On the other side of the chain, developers may take up ideas and transfer the accepted requirements to an issue system like JIRA.

Over the last years it turned out that people want more lightweight and mobile-friendly tools; we found the old monolithic system to be very hard to maintain to add new features. Therefore we are currently redeveloping it from scratch integrating many ideas from our users and incorporating new research findings. We are further driven by a mobile-first approach to support a wide variety of devices. We additionally want to support various social features like sharing on social networks or blogs and allowing discussions and rating amongst end-users and developers.

So briefly what is Requirement Bazaar? It is a web application that provides a user-friendly, yet developer usable way, how end-users and developers can share ideas/requirement/problems about a certain app.

We also provide a modern webcomponent frontend for this service which you can find also on **[<i class="icon-link "></i>Github](https://github.com/rwth-acis/RequirementsBazaar-WebFrontend)**.

The service is under development. You can participate by creating pull requests or by discussing ideas and requirements inside **[<i class="icon-link "></i>Requirements-Bazaar](https://requirements-bazaar.org/projects/2/categories/143)**.

More information about our microservice ecosystem is explained in our  **[<i class="icon-link "></i>Requirements-Bazaar wiki on Github](https://github.com/rwth-acis/RequirementsBazaar/wiki)**.

---

## Service Documentation

We use **[<i class="icon-link "></i>Swagger](http://swagger.io/specification/)** for documenting this microservice. You can use **[<i class="icon-link "></i>Swagger UI](http://swagger.io/swagger-ui/)** to inspect the API.
Please use our deployed **[<i class="icon-link "></i>Swagger UI instance](https://requirements-bazaar.org/docs)** to inspect our API. You can authorize yourself via Oauth 2.0 by clicking at the `Authorize` button at the top. After you are authorized you can try out all the API calls directly from Swagger UI.
To try out one API call open one method, fill the parameters and click `Try it out!`. The Swagger UI instance is also connected to our development environment where we test nex features. You can select this in the dropdown menu at the top. Feel free to play around there.

API documentation endpoints:

 - `baseURL/bazaar/swagger.json`

---

## Technology

Requirements bazaar itself is a web application, which is separated to a client-only side and a back-end side. This GitHub repo holds the codebase of the back-end side only. If you are looking for the front-end, take a look at this GitHub project: **[<i class="icon-link "></i>Requirement Bazaar Web Frontend](https://github.com/rwth-acis/RequirementsBazaar-WebFrontend)**

The backend is built on Java technologies. As a service framework we use our in-house developed **[<i class="icon-link "></i>las2peer](https://github.com/rwth-acis/LAS2peer)** project. For persisting our data we use a PostgreSQL database and jOOQ to access it and for serializing our data into JSON format, we use the Jackson library.

## Dependencies

In order to be able to run this service project the following components should be installed on your system:

 - JDK 14
 - PostgreSQL 12 or newer
 - Gradle to build

## How to set up the database (non developer guide)

 1. Download a release bundle from [GitHub](https://github.com/rwth-acis/RequirementsBazaar/releases)
 2. Create a new database called `reqbaz`, possibly with UTF-8 collation
 3. To configure your database access look at the [Configuration](#configuration) section
 4. Use liquibase to migrate the database

## Configuration

You need to configure this service to work with your own specific environment. Here is the list of configuration variables:

`\etc\de.rwth.dbis.acis.bazaar.service.BazaarService.properties`:
 - `dbUserName`:	    A database user's name, which will be used to access the database
 - `dbPassword`:	    The database user's password
 - `dbUrl`:			    JDBC Connection string to access the database. Modify it if you have changed the name of your database
 - `lang`:              Default language setting
 - `country`:           Default country setting
 - `baseURL`:           Base URL this service runs on
 - `frontendBaseURL`:    Base URL for the frontend which uses Requirements-Bazaar service
 - `activityTrackerService`: p2p microservice name of the las2peer activity tracker. Leave this field empty if no las2peer activity tracker is used. (example: `de.rwth.dbis.acis.activitytracker.service.ActivityTrackerService@0.2`)
 - `smtpServer`:         SMTP server to send email notifications to users. Leave this field empty if no email notification is used.
 - `emailFromAddress`:   Email from address which is used for email notifications to users. Leave this field empty if no email notification is used.
 - `monitor`:            Boolean value for logging calls to MobSOS data processing

For other configuration settings, check the **[<i class="icon-link "></i>las2peer](https://github.com/rwth-acis/LAS2peer)** project.

### How to run
 1. First please make sure you have already [set up the database](#how-to-set-up-the-database).
 2. Make sure your [config settings](#configuration) are properly set.
 3. When not using a release asset, check [Building](#building).
 4. Open a console/terminal window and navigate to the project directory.
 5. Run the `bin\start_network.bat` or `bin/start_network.sh` script.

---

## Developer guide

### Prerequisites

Building and testing requires a database which will be cleaned on every build.

Set the credentials for this database in the gradle.properties and in the `reqbaz/etc` folder.
This configuration folder is relevant for any testing related settings.

In the `etc` folder on top level you may configure a different database if you want to distinguish between local testing and automated testing.
However you'll have to apply the migrations with liquibase manually to this database.

### Building

The build and test process is done with gradle. A simple `gradle build` should be sufficient to build the project and run the test cases.

You may have to adjust either your global java environment or the one in your IDE to use Java 14 or the built asset won't start.

### Debugging

Add the debugger parameters below to your startscript and set up your IDE to connect to a remote debugging port.

`-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5433`.

Now you should be able to set breakpoints and inspect the runtime.

## Troubleshooting & FAQ

 - I can not run the start script: Check if you have OS permission to run the file.
 - The service does not start: Check if all jars in the lib and service folder are readable.
 - The start script seems broken: Check if the start script has the correct encoding. If you ran the service on Unix use `dos2unix` to change the encoding.
