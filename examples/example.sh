echo "Running Example with input file 'example.vm'"

for i in ../bin/*.jar
do
    _VELCP=$VELCP:"$i"
done

java -cp $_VELCP:. Example example.vm  

