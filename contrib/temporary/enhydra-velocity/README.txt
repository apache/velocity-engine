This is a simple example of how Velocity may be used
with Enhydra.

How to set-up:

1. Add the velocity jar and the velocity service classes ( see classes dir )
   to your classpath.  Optionally, you can add this info to the config.mk and start.in 
   files of your application

2. Add the following line to your Enhydra application conf file:
 
   velocity = "THE FULL PATH TO VELOCITY PROPERTIES FILE"
   Example: velocity = "/dave/enhydraApps/VelocityApplication/velocity.properties"

3. Set the path to your template directory in the velocity.properties file:

   file.resource.loader.path = "FULL PATH TO THE TEMPLATES DIRECTORY"
   Example: file.resource.loader.path = /dave/enhydraApps/VelocityApplication/templates


Have fun...

