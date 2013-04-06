
CLASSPATH = "src/:src/sensorserver/:src/sensorserver/log/:src/sensorserver/database/"

default: 
	javac -g -d bin/ -cp $(CLASSPATH) src/sensorserver/RunServer.java

clean: 
	find bin/ -type f -iname \*.class
	find bin/ -type f -iname \*.class -delete
