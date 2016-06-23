#!/bin/bash

# this script starts a las2peer node providing the example service of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory startService\(\'de.rwth.dbis.acis.bazaar.service.BazaarService@0.2\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.UsersResource@0.2\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ProjectsService@0.2\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ComponentsService@0.2\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.RequirementsService@0.2\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.CommentsResource@0.2\',\'SampleServicePass\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.AttachmentsService@0.2\',\'SampleServicePass\'\) startWebConnector interactive
