package org.apache.velocity.test;

import junit.framework.Assert;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RenderVelocityTemplateTest
{

    static class RenderVelocityTemplate
    {
        static
        {
            try
            {
                Properties p = new Properties();
                p.put("velocimacro.permissions.allow.inline.local.scope", "true");
                Velocity.init(p);
            }
            catch (Exception e)
            {
                throw new AssertionError("Failed to init Velocity");
            }
        }

        private final VelocityContext velocityContext = new VelocityContext();

        private final String template;

        public RenderVelocityTemplate(String template)
        {
            this.template = template;
        }

        public String getContent()
            throws Exception
        {
            StringWriter stringWriter = new StringWriter();
            Velocity.evaluate(velocityContext, stringWriter, "", template);
            return stringWriter.toString();
        }
    }
    
    
    private static final String templateString = "" + //
        "#macro (showhelloworld $foo)\n" + //
        "Hello, World\n" + //
        "#end\n" + //
        "\n" + //
        "<html>\n" + //
        "<head>\n" + //
        "<title>page title</title>\n" + //
        "</head>\n" + //
        "<body>\n" + //
        "<p>This is a test</p>\n" + //
        "<p>#showhelloworld ($foo)</p>\n" + //
        "</body>\n" + //
        "</html>";
    
    public void testMultipleEvals()
        throws Exception
    {
        RenderVelocityTemplate template = new RenderVelocityTemplate(templateString);

        String result = null;
        for (int i = 0; i < 1000; ++i)
        {
            result = template.getContent();
            
            // Verify that the original macro invocation has been replaced with its result.
            int index = result.indexOf("#showhelloworld");
            if (index != -1)
            {
                throw new AssertionError("Failed to substitute macro:\n" + result);
            }
            
            // Verify that the macro did indeed expand.
            int indexHW = result.indexOf("<p>Hello, World");
            Assert.assertTrue(indexHW >= 0);
            
            // Assert.assertEquals("", result); // enable to show what we really get
        }
    }

    /** Helper class for testMultiThreadMultipleEvals(). */
    static class ExceptionHandler
        implements Thread.UncaughtExceptionHandler
    {
        List<Throwable> errors = new ArrayList<Throwable>();

        public void uncaughtException(Thread t, Throwable e)
        {
            errors.add(e);
        }
    }

    /** Helper class for testMultiThreadMultipleEvals(). */
    class RunMultipleEvals
        extends Thread
    {
        @Override
        public void run()
        {
            try
                {
                    testMultipleEvals();
                }
            catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
        }
    }

    /**
     * Spawn multiple threads that concurrently call testMultipleEvals.
     */
    public void testMultiThreadMultipleEvals()
        throws Throwable
    {
        int nthreads = 4;
        ExceptionHandler eh = new ExceptionHandler();

        List<Thread> threads = new ArrayList<Thread>(nthreads);
        for (int i = 0; i < nthreads; ++i)
        {
            Thread t = new RunMultipleEvals();
            t.setUncaughtExceptionHandler(eh);
            threads.add(t);
        }

        for (Thread t : threads)
        {
            t.start();
        }
        
        for (Thread t : threads)
        {
            t.join();
        }

        if (eh.errors.size() > 0)
        {
            // Rethrow the first failing exception.
            System.out.println("Failed " + eh.errors.size() + " out of " + nthreads + " template evaluations");
            Throwable t = eh.errors.get(0);
            throw t;
        }
    }
}
