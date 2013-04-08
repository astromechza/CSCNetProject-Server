default: 
	javac -g sensorserver/RunServer.java

tests:
	javac -g sensorserver/tests/*.java

clean: 
	find . -type f -iname \*.class
	find . -type f -iname \*.class -delete
	find . -type f -iname \*.log
	find . -type f -iname \*.log -delete