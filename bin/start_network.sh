#!/bin/bash

            # this script is autogenerated by 'ant startscripts'
            # it starts a LAS2peer node providing the service '${service.name}.${service.class}' of this project
            # pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

            java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory startService\(\'de.rwth.dbis.acis.bazaar.service.BazaarService@0.2\',\'Passphrase\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ProjectsResource@0.2\',\'Passphrase\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.ComponentsResource@0.2\',\'Passphrase\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.RequirementsResource@0.2\',\'Passphrase\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.CommentsResource@0.2\',\'Passphrase\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.AttachmentsResource@0.2\',\'Passphrase\'\) startService\(\'de.rwth.dbis.acis.bazaar.service.UsersResource@0.2\',\'Passphrase\'\) startWebConnector interactive
        