:: this script starts a las2peer node providing the example service of this project
:: pls execute it from the bin folder of your deployment by double-clicking on it

%~d0
cd %~p0
cd ..
set BASE=%CD%
set CLASSPATH="%BASE%/lib/*;"

java -cp %CLASSPATH% i5.las2peer.tools.L2pNodeLauncher -p 9011 uploadStartupDirectory startService('de.rwth.dbis.acis.bazaar.service.BazaarService@0.2','SampleServicePass') startService('de.rwth.dbis.acis.bazaar.service.UsersResource@0.2','SampleServicePass') startService('de.rwth.dbis.acis.bazaar.service.ProjectsResource@0.2','SampleServicePass') startService('de.rwth.dbis.acis.bazaar.service.ComponentsResource@0.2','SampleServicePass') startService('de.rwth.dbis.acis.bazaar.service.RequirementsResource@0.2','SampleServicePass') startService('de.rwth.dbis.acis.bazaar.service.CommentsResource@0.2','SampleServicePass') startService('de.rwth.dbis.acis.bazaar.service.AttachmentsResource@0.2','SampleServicePass') startWebConnector interactive
pause
