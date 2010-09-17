Welcome to Velocity!

This directory contains a few examples to help get you started.

As always, the if you have any questions :

1) Make sure you followed any directions :-) (did you build
   everything?)

2) Review documentation included in this package, or online at
   http://velocity.apache.org/

3) Ask on the velocity-user list.  This is a great source of support
   information.  To join, read http://velocity.apache.org/contact.html
   and then follow the links to join the lists.


logger_example
--------------

This 'toy' example shows how to use the Velocity logger interface
to have any class function as a logging facility.  

To run :

./logger_example.sh

or 

logger_example.bat


Log4jLoggerExample
------------------
This class demonstrates how to configure Velocity to use an 
 existing log4j logger for logging.

To test this example, change "LoggerExample" to "Log4jLoggerExample"
in the batch file and run.

You will see that the log4j output will contain the output from
velocity's initialization.
