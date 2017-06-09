Requirements Bazaar
===================

Idea
-------------------
In years of research at RWTH Aachen we have developed and actually operated an **Open Innovation Platform for gathering requirements** for prototypes in large international academic projects. The last version of the current product is available under http://requirements-bazaar.org . End-users can enter their requirements by providing short descriptions including user stories, screenshots and other images. The requirements can then be shared amongst its users. On the other side of the chain, developers may take up ideas and transfer the accepted requirements to an issue system like JIRA.

Over the last years it turned out that people want more lightweight and mobile-friendly tools; we found the old monolithic system to be very hard to maintain to add new features. Therefore we are currently redeveloping it from scratch integrating many ideas from our users and incorporating new research findings. We are further driven by a mobile-first approach to support a wide variety of devices. We additionally want to support various social features like sharing on social networks or blogs and allowing discussions and rating amongst end-users and developers.

So briefly what is Requirement Bazaar? It is a web application that provides a user-friendly, yet developer usable way, how end-users and developers can share ideas/requirement/problems about a certain app.

We also provide a modern webcomponent frontend for this service which you can find also on **[<i class="icon-link "></i>Github](https://github.com/rwth-acis/RequirementsBazaar-WebFrontend)**.

The service is under development. You can participate by creating pull requests or by discussing ideas and requirements inside **[<i class="icon-link "></i>Requirements-Bazaar](https://requirements-bazaar.org/projects/2/categories/143)**.

More information about our microservice ecosystem is explained in our  **[<i class="icon-link "></i>Requirements-Bazaar wiki on Github](https://github.com/rwth-acis/RequirementsBazaar/wiki)**.

----------

Service Documentation
-------------------
We use **[<i class="icon-link "></i>Swagger](http://swagger.io/specification/)** for documenting this microservice. You can use **[<i class="icon-link "></i>Swagger UI](http://swagger.io/swagger-ui/)** to inspect the API.
Please use our deployed **[<i class="icon-link "></i>Swagger UI instance](https://requirements-bazaar.org/docs)** to inspect our API. You can authorize yourself via Oauth 2.0 by clicking at the `Authorize` button at the top. After you are authorized you can try out all the API calls directly from Swagger UI.
To try out one API call open one method, fill the parameters and click `Try it out!`. The Swagger UI instance is connected to our development environment where we test nex features. So feel free to play around.

API documentation endpoints:

 - `baseURL/bazaar/swagger.json`

----------

Technology
-------------------
Requirements bazaar itself is a web application, which is separated to a client-only side and a back-end side. This GitHub repo holds the codebase of the back-end side only. If you are looking for the front-end, take a look at this GitHub project: **[<i class="icon-link "></i>Requirement Bazaar Web Frontend](https://github.com/rwth-acis/RequirementsBazaar-WebFrontend)**

The backend is built on Java technologies. As a service framework we use our in-house developed **[<i class="icon-link "></i>las2peer](https://github.com/rwth-acis/LAS2peer)** project. For persisting our data we use MySQL database and jOOQ to access it. User input validation is done using Jodd Vtor library and for serializing our data into JSON format, we use the Jackson library.

Dependencies
-------------------
In order to be able to run this service project the following components should be installed on your system:

 - JDK (min v1.8) + Java Cryptography Extension (JCE)
 - MySQL 5.7
 - Apache Ant to build

How to set up the database
-------------------
 1. `git clone` this repo
 2. To configure your database access look at the [Configuration](#configuration) section
 3. Compile the project with `ant`
 4. Create a new database called `reqbaz`, possibly with UTF-8 collation
 5. Run `ant migrate-db` to create your db schema
 6. If you need sample data run the file `\etc\add_reqbaz_demo_data.sql` or `\etc\add_reqbaz_demo_data_full.sql`

Configuration
-------------------
You need to configure this service to work with your own specific environment. Here is the list of configuration variables:

`\etc\de.rwth.dbis.acis.bazaar.service.BazaarService.properties`:
 - `dbUserName`:	    A database user's name, which will be used to access the database
 - `dbPassword`:	    The database user's password
 - `dbUrl`:			    JDBC Connection string to access the database. Modify it if you have changed the name of your database
 - `land`:              Default language setting
 - `country`:           Default country setting
 - `baseURL`:           Base URL this service runs on
 - `frontendBaseURL`    Base URL for the frontend which uses Requirements-Bazaar service
 - `activityTrackerService` p2p microservice name of the las2peer activity tracker. Leave this field empty if no las2peer activity tracker is used. (example: `de.rwth.dbis.acis.activitytracker.service.ActivityTrackerService@0.2`)
 - `smtpServer`         SMTP server to send email notifications to users. Leave this field empty if no email notification is used.
 - `emailFromAddress`   Email from address which is used for email notifications to users. Leave this field empty if no email notification is used.

For other configuration settings, check the **[<i class="icon-link "></i>las2peer](https://github.com/rwth-acis/LAS2peer)** project.

Build
-------------------
For build management we use Ant. To build the cloned code, please using a console/terminal navigate to the home directory, where the build.xml file is located and run the following commands:

 - `ant`
 
You can also generate a bundled jar with all the dependencies with the command
 
  - `ant jar-big`

How to run
-------------------
 1. First please make sure you have already [set up the database](#how-to-set-up-the-database)
 2. Make sure your [config settings](#configuration) are properly set.
 3. [Build](#build)
 4. Open a console/terminal window and navigate to the `\bin` directory
 5. Run the `start_network.bat` or `start_network.sh` script

 How to run using Docker
 -------------------
 Docker is providing the simplest way to run the Requirement Bazaar back-end. Just follow the following steps if Docker is already installed on your system:

  1. `git clone` this repo
  2. `docker build -t rwthacis/reqbaz-service`
  3. `docker run -i -t --rm -v "pwd":/build rwthacis/reqbaz-service`

----------

Troubleshooting & FAQ
-------------------
 - I get Java encryption errors: Did you install Java Cryptography Extension?
 - I can not run the start script: Check if you have OS permission to run the file.
 - The service does not start: Check if all jars in the lib and service folder are readable.
 - The start script seems broken: Check if the start script has the correct encoding. If you ran the service on Unix use `dos2unix` to change the encoding.