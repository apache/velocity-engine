Contributed by Christoph Reck <Christoph.Reck@dlr.de> with the
following [edited] message to the user list :

For anyone interested, I've created a simple StderrLogSystem as a 
drop-in for Velocity - see attachment. Either it can be taken into
the velocity contribution section, or put into velocity as the
standard logger, or you can change the package name to whatever
you want and include it in your distribution.

The reason for this is that I do not want stray files created
(velocity.log) in my file system when running my Anakia commandline
tool.

I use it as follows:

within main()
    // The classname of the default logger to be used
    String loggerClass =
      "StderrLogSystem";

    Vector vargs = new Vector();
    for( int i = 0; i < args.length; ++i )
    {
      if( args[i].equals("-quiet") )
        loggerClass = "org.apache.velocity.runtime.log.NullLogSystem";
      else if( args[i].equals("-verbose") )
        loggerClass = "StderrLogSystem";
      else
        vargs.add( args[i] );
    }

and for the ve.init():
    templatePath = new File(templatePath).getCanonicalPath();
    velocity = new VelocityEngine();
    velocity.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatePath);
    velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, loggerClass);
    velocity.init();
NOTE: templatePath is "." for my current XmlTransformer batch tool.


[Note - Class was removed from package for ease of demonstration 
here in the contrib section - geir ]
