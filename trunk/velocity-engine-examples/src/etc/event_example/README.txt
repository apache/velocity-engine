Welcome to Velocity!

As always, the if you have any questions :

1) Make sure you followed any directions :)  (did you build everything?)
2) Review documentation included in this package, or online at
      http://jakarta.apache.org/velocity/
3) Ask on the velocity-user list.  This is a great source of support information.
   To join, read http://jakarta.apache.org/site/mail.html and then follow the
   link at the bottom to join the lists.

event_example
-------------

This simple example demonstrates the event handling features of
the Velocity Template Engine.

Note that this is considered a more advanced topic - you don't need
to know this to use 99% of the features of Velocity, so if this
seems like too much at first - ignore it!

This is a standalone program.  It is compiled by the 'examples' build
target.

To run, and assuming you have the Velocity and Commons-Collections jar
in your CLASSPATH, simply do :

java EventExample

if the .jar's are not in your classpath, do :

java -cp \
../../bin/velocity-[VERSION].jar:../../build/lib/commons-collections.jar:. \
org.apache.velocity.example.EventExample

(NOTE: make sure that the jarname is correct - this document might get
out of synch with the release version numbers.)
