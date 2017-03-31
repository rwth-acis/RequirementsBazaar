Requirements Bazaar
===================

Table of contents
-------------

- [Requirements Bazaar](#)
	- [Table of contents](#)
	- [Idea](#)
	- [Technology](#)
	- [Dependencies](#)
	- [How to run using Docker](#)
	- [How to set up the database](#)
	- [Configuration](#)
	- [Build](#)
	- [How to run](#)

----------

Idea
-------------

In years of research at RWTH Aachen we have developed and actually operated an **Open Innovation Platform for gathering requirements** for prototypes in large international academic projects. The last version of the current product is available under http://requirements-bazaar.org . End-users can enter their requirements by providing short descriptions including user stories, screenshots and other images. The requirements can then be shared amongst its users. On the other side of the chain, developers may take up ideas and transfer the accepted requirements to an issue system like JIRA.

Over the last years it turned out that people want more lightweight and mobile-friendly tools; we found the old monolithic system to be very hard to maintain to add new features. Therefore we are currently redeveloping it from scratch integrating many ideas from our users and incorporating new research findings. We are further driven by a mobile-first approach to support a wide variety of devices. We additionally want to support various social features like sharing on social networks or blogs and allowing discussions and rating amongst end-users and developers.

So briefly what is Requirement Bazaar? It is a web application that provides a user-friendly, yet developer usable way, how end-users and developers can share ideas/requirement/problems about a certain app.


----------


Technology
-------------------
Requirements bazaar itself is a web application, which is separated to a client-only side and a back-end side. This GitHub repo holds the codebase of the back-end side only. If you are looking for the front-end, take a look at this GitHub project: **[<i class="icon-link "></i>Requirement Bazaar Web Frontend](https://github.com/rwth-acis/RequirementsBazaar-WebFrontend)**

The backend is built on Java technologies. As a service framework we use our in-house developed **[<i class="icon-link "></i>LAS2Peer](https://github.com/rwth-acis/LAS2peer)** project. For persisting our data we use MySQL database and JOOQ to access it. User input validation is done using Jodd Vtor library and for serializing our data into JSON format, we use GSON library.

----------


Dependencies
-------------------

In order to be able to run this service project the following categories should be installed on your system:

 - JDK (min v1.7) + Java Cryptography Extension (JCE) 
 - MySQL 5 
 - (Apache Ant)



How to run using Docker
-------------------
Docker is providing the simplest way to run the Requirement Bazaar back-end. Just follow the following steps if Docker is already installed on your system:

 1. `git clone this repo` 
 2. `docker build -t rwthacis/reqbaz-service .` 
 3. ``docker run -i -t --rm -v `pwd`:/build rwthacis/reqbaz-service``

How to set up the database
-------------------

 1. `git clone this repo`
 2. create a new database called `reqbaz`, possibly with UTF-8 collation
 3. Run the SQL commands in the file `\etc\ReqBaz_create.sql`
     This will create the tables and relations between them.
 4.  If you need sample data run the file `\etc\ReqBaz_demo_data.sql`
 5. To configure your database access look at the [Configuration](#configuration) section

Configuration
-------------------
Of course there are way to configure this service to your own specific environment. Here are the list of configuration files and their short description:

\etc\de.rwth.dbis.acis.bazaar.service.BazaarService.properties
:   *dbUserName*:	A database user's name, which will be used to access the database
:   *dbPassword*:		The database user's password
:   *dbUrl*:			JDBC Connection string to access the database. Modify it if you have changed the name of your database

For other configuration settings, check the **[<i class="icon-link "></i>LAS2Peer](https://github.com/rwth-acis/LAS2peer)** project.

Build
-------------------
For build management we use Ant. To build the cloned code, please using a console/terminal navigate to the home directory, where the build.xml file is located and run the following commands:

 1. `ant install-ivy`
 2. `ant clean_all`
 3. `ant get_deps`
 4. `ant generate_configs`
 5. `ant jar`

How to run
-------------------

 1. First please make sure you have already [set up the database](#how-to-set-up-the-database)
 2. Make sure your [config settings](#configuration) are properly set.
 3. [Build](#build)
 4. Open a console/terminal window and navigate to the `\bin` directory
 5. Run the `start_network.bat` or `start_network.sh`


