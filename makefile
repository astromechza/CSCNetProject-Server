
CLASSPATH = "src/:src/sensorserver/:src/sensorserver/log/:src/sensorserver/database/"

default: 
	javac -g -d bin/ -cp $(CLASSPATH) src/sensorserver/RunServer.java

clean: 
	rm -f bin/*.class
	rm -f bin/org/json/*.class
	rm -f bin/sensorserver/*.class
	rm -f bin/sensorserver/database/*.class
	rm -f bin/sensorserver/log/*.class	
	rm -f bin/sensorserver/tests/*.class
	rm -f src/*.class
	rm -f src/org/json/*.class
	rm -f src/sensorserver/*.class
	rm -f src/sensorserver/database/*.class
	rm -f src/sensorserver/log/*.class	
	rm -f src/sensorserver/tests/*.class