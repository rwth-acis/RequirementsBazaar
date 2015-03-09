:: this script starts a las2peer node providing the example service of this project
:: pls execute it from the bin folder of your deployment by double-clicking on it

cd ..
set BASE=%CD%
set CLASSPATH="%BASE%/lib/*;"

java -cp %CLASSPATH% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5432 i5.las2peer.tools.L2pNodeLauncher -w -p 9011 uploadStartupDirectory('etc/startup') startService('de.rwth.dbis.acis.bazaar.service.BazaarService','SampleServicePass') startWebConnector interactive

pause
