pushd .

cd bin
java -cp .:../lib/mysql-connector-java-5.1.24-bin.jar sensorserver.RunServer "$@"

popd