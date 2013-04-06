default: 
	javac -g sensorserver/RunServer.java

clean: 
	find . -type f -iname \*.class
	find . -type f -iname \*.class -delete
	find . -type f -iname \*.log
	find . -type f -iname \*.log -delete