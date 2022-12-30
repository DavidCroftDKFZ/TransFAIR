echo Sleep until Blazectl is finished
sleep 170
echo Starting Transfair
#java -jar transFAIR.jar
java -agentlib:jdwp=transport=dt_socket,server=y,address=*:8086,suspend=n -jar transFAIR.jar
echo Ended Transfair
