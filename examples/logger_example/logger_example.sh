echo "Running LoggerExample:"

for i in ../../bin/*.jar
do
    _VELCP=$_VELCP:"$i"
done

java -cp $_VELCP:. LoggerExample 

