#!/bin/bash

# this script starts a las2peer node providing the example service of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory\(\'etc/startup\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.BazaarService\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.UsersResource\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ProjectsResource\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ComponentsResource\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.RequirementsResource\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.CommentsResource\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.AttachmentsResource\',\'SampleServicePass\'\) startWebConnector interactive
