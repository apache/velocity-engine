echo "Running Example with input file 'example.vm'"

_VELCP=.

for i in ../../bin/*.jar
do
    _VELCP=$_VELCP:"$i"
done
 
for i in ../../build/lib/*.jar
do
    _VELCP=$_VELCP:"$i"
done

# convert the unix path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
    _VELCP=`cygpath --path --windows "$_VELCP"`
fi

java -cp $_VELCP Example example.vm  
 
