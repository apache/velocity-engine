Welcome to Velocity!
--------------------

As always, the if you have any questions :

1) Make sure you followed any directions :)  (did you build everything?)
2) Review documentation included in this package, or online at
      http://jakarta.apache.org/velocity/
3) Ask on the velocity-user list.  This is a great source of support
   information. To join, read http://jakarta.apache.org/site/mail.html
   and then follow the link at the bottom to join the lists.


servlet_example1
----------------
This is a simple example of how Velocity can be used with your favorite
servlet engine. To use this, you should have a basic understanding how
your servlet engine works, and how you deploy and access servlets to try
this. While we cannot supply this, we know of a good one :)

   http://jakarta.apache.org/tomcat/


Unfamiliar with Servlets?
-------------------------

This documentation should be found elswhere, but putting here just in
case.

Using Velocity on the web is best done using Java Servlets.  While we
don't want to give a tutorial here, the basic idea is straightforward.

0) Install Ant. http://jakarta.apache.org/ant/

1) Get a servlet engine.  Tomcat, from the Jakarta project, is an
   excellent implementation. (In fact, it's the official reference
   implementation...)

2) Install your servlet engine.

3) Build this servlet example : cd jakarta-velocity/build; ant examples

4) Setup a webapp under Tomcat :

 - make a new 'webapp' : in tomcat's webapps directory, 
   make a new directory structure as follows :
   
      velexample
      velexample/WEB-INF
      velexample/WEB-INF/lib
      velexample/WEB-INF/classes

 - put the velocity.jar into the velexample/WEB-INF/lib directory
 
 - put the SampleServlet.class into the velexample/WEB-INF/classes directory

 - put the sample.vm template into the velexample directory.

 - at this point, you should be able to restart tomcat and access the servlet.  

 - so bounce tomcat, go into your web brower and go to : 

    http://<your computer's ip address>:8080/velexample/servlet/SampleServlet

 - you should see the sample output.  

At this point, you are using Velocity!
