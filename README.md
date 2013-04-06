# CSCNetProject-Server

A Server designed to accept sensor data from multiple connected clients over TCP sockets.

Clients may also request statistics, logs, and other data from the server which will be presented as graphs on the client side.

## How to Build
```
$ make
```

## How to Run:
```
$ source run.sh <arguments>
```
or:
```
$ java -cp .:lib/* sensorserver.RunServer <arguments>
```

## Configuration
The server relies on a configuration file ```server.properties```. Use ```server.properties.example``` as a guide. Custom file names/locations are supported via the ```--file``` argument.

## Arguments
```
--file/-f       : Path to the file containing server properties. View example.server.properties for more information.  default: 'server.properties' 
--debug/-d      : Force the server into debug mode. 
--rebuild/-r    : Drop and recreate the database tables. 
--help/-h       : Print this usage message. 
```

