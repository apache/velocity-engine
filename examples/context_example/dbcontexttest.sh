echo "DBContextTest : please ensure MySQL is set up and jdbc drivers are in classpath. See DBContextTest.java for clues"
echo "This is an unsupported demo."

java -cp .:../bin/velocity-0.71.jar:$CLASSPATH DBContextTest dbtest.vm

