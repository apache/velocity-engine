Welcome to Velocity!

As always, the if you have any questions :

1) Make sure you followed any directions :)  (did you build everything?)
2) Review documentation included in this package, or online at
      http://jakarta.apache.org/velocity
3) Ask on the velocity-user list.  This is a great source of support information.
   To join, read http://jakarta.apache.org/site/mail.html and then follow the 
   link at the bottom to join the lists.


servlet_example2
----------------
This is a slightly more complicated example of how Velocity can be used with
 your favorite servlet engine. To use this, you should have a basic understanding
how your servlet engine works, and how you deploy and access servlets to try this.
While we cannot supply this, we know of a good one :)
   http://jakarta.apache.org/tomcat/

The difference is that instead of self-configuring, the servlet SampleServlet2 will
use an external velocity.properties file specified in the web.xml deployment 
descriptor.

The example will demonstrate how to use either the FileResourceLoader to load a 
template from a subdirectory of your webapp, or the ClasspathResourceLoader to
load a template from a jar file placed in WEB-INF/lib.

This example assumes you have played with servlet_example1 and understand the basics.

1) Get a servlet engine.  Tomcat, from the Jakarta project, is an 
excellent implementation. (In fact, it's the official reference
implementation...)

2) Install said servlet engine.


Setting up to use the ClasspathResourceLoader
---------------------------------------------

3) Build this servlet example : in build - ant examples

4) Setup a webapp under tomcat :

 - make a new 'webapp' : in tomcat's webapps directory, 
   make a new directory structure as follows :
   
      velexample
      velexample/WEB-INF
      velexample/WEB-INF/lib
      velexample/WEB-INF/classes


 - put the velocity-1.1-rc1.jar (or newer) into the velexample/WEB-INF/lib directory
 
 - put the SampleServlet2.class into the velexample/WEB-INF/classes directory

 - put the templates.jar into the velexample/WEB-INF/lib directory.

 - put the web.xml into the velexample/WEB-INF directory

 - place a copy of velocity.properties.classloader, but rename to be velocity.properties

 - at this point, you should be able to restart tomcat and access the servlet.  

 - so bounce tomcat, go into your web brower and go to : 

    http://<your computer's ip address>:8080/velexample/servlet/SampleServlet

 - you should see the sample output.  

At this point, you are accessing the template via the ClasspathResourceLoader.  Note also
that the velocity.log will be placed in the velexample directory. (root of your webapp.)


Setting up to use the FileResourceLoader
----------------------------------------

3) Build this servlet example : in build - ant examples

4) Setup a webapp under tomcat :

 - make a new 'webapp' : in tomcat's webapps directory, 
   make a new directory structure as follows :
   
      velexample
      velexample/templates
      velexample/WEB-INF
      velexample/WEB-INF/lib
      velexample/WEB-INF/classes


 - put the velocity-1.1-rc1.jar (or newer) into the velexample/WEB-INF/lib directory
 
 - put the SampleServlet2.class into the velexample/WEB-INF/classes directory

 - put the sample.vm into the velexample/templates directory.

 - put the web.xml into the velexample/WEB-INF directory

 - place a copy of velocity.properties.fileloader, but rename to be velocity.properties

 - at this point, you should be able to restart tomcat and access the servlet.  

 - so bounce tomcat, go into your web brower and go to : 

    http://<your computer's ip address>:8080/velexample/servlet/SampleServlet

 - you should see the sample output.  

At this point, you are accessing the template via the FileResourceLoader.  Note also
that the velocity.log will be placed in the velexample directory. (root of your webapp.)


