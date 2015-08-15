#!/bin/bash

# this script starts a las2peer node providing the example service of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5432 i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory\(\'etc/startup\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.BazaarService\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.UsersResource\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ProjectsResource\',\'SampleServicePass\'\) startWebConnector interactive
