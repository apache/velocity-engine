Welcome to Velocity!

The files in this directory demonstrate how the Velocity template engine
can be used.

1a) Example.java:  This simple example shows how to use the Velocity Template Engine
in a standalone program.  It should be pre-compiled for you. Run it using the example
template provided (example.vm):

	./test.sh

the output will be in the file 'output'. 

If you wish to recompile the example, remember to include the velocity jar in your classpath, or 
just specify it on the command line :

	javac -classpath .:../bin/velocity-0.71.jar Example.java

(If there is a problem, make sure that the filename of the velocity jar hasn't changed. We can 
be forgetful at times :)

1b) Example2.java : Another simple example showing how to use Velocity in a standalone
program.  This examples uses the org.apache.velocity.util.Velocity application utility
class, which provides a few convenient methods for application programmers.  It also
should be precompiled for you.  To run :

  ./example2.sh

 
2) SimpleServlet.java:  This is a simple example of how Velocity can be used with your favorite servlet
engine. <need to review the code - gmj>


3) DBContext.java : simple demonstration of a context implementation that uses a database as the 
storage.  Use the DBContextTest program to test.  See the DBContextTest.java file for the db table
info.  Unsupported demonstration code.  You may need to futz with it to get it to work in your
environment.  To run, use the dbcontexttest.sh script.

Thanks for using Velocity!

- the Velocity team

 	
