Welcome to the Struts-Velocity Experiment!
==========================================

NOTE : This is unsupported, experimental software, suitable
for *no* real use whatsoever.  It is intended as an exploration
of the incorporation of Velocity templates in the Struts 
framework. 

--------------------------------------------------------

This package is an exploration of using the Velocity template 
engine in the Struts framework.

This package allows you to :

1) Use velocity templates named XXXXX.vm anywhere you would
use a JSP in your Struts application.

2) You may intermingle - having some pages in Velocity, and
some in JSP

Currently, framework support is limited to :
- the <bean:message tag via $message.get( key ) for the message
  resource manager
- the <html:errors  tag via $errors.getErrors() for the
  ActionErrors

The current implementation is more or less a hack, and they
will be changed.  More will be converted if interest warrants.

Example
-------
Currently, the struts-example has been converted to work
with Velocity templates rather than JSP, and the example
works for the most part. (There may be a bug or two...)

This example, as a WAR, has been tested with Tomcat 4.0 and
Tomcat 3.2.1.  It can be found here :

http://jakarta.apache.org/builds/jakarta-velocity/contrib/struts/

To use :

1) drop the war into your webapps directory
2) restart tomcat
3) aim your browser at

   http://localhost:8080/struts-velocity-example/

That's all there is to it.


Building the Jar
----------------

Building the code is easy :

1) You must have ant installed, in your path, and ANT_HOME set correctly.
 This keeps this distro small.

2) Change the properties in build.xml to reflect the locations of your
   servlet.jar, struts.jar and velocity.jar.  You should also be able
   to use a build.properties file to override w/o having to change
   build.xml.  Your call...

3) type "ant jar" at the prompt, and it should build.


Configuration of a Struts webapp
--------------------------------
My experience with Struts is limited - the following may be completely wrong .

1) in WEB-INF/web.xml, add the VelServlet template handling servlet :

  <servlet>
    <servlet-name>vel</servlet-name>
    <servlet-class>org.apache.velocity.struts.VelServlet</servlet-class>
    <load-on-startup>4</load-on-startup>
  </servlet>

  the load-on-startup can be whatever you need it to be.  There are no loading
  dependencies.

2) in the same file, WEB-INF/web.xml, map all Velocity templates to be 
   handled by the VelServlet

  <servlet-mapping>
    <servlet-name>vel</servlet-name>
    <url-pattern>*.vm</url-pattern>
  </servlet-mapping>


