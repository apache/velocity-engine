package org.apache.velocity.runtime.configuration;

/*
 * Copyright (c) 1997-2000 The Java Apache Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine", "Turbine",
 *    "Apache Turbine", "Turbine Project", "Apache Turbine Project" and
 *    "Java Apache Project" must not be used to endorse or promote products
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

// Java stuff.
import java.io.*;
import java.util.*;

/**
 * This class extends normal Java properties by adding the possibility
 * to use the same key many times concatenating the value strings
 * instead of overwriting them.
 *
 * <p>The Extended Properties syntax is explained here:
 *
 * <ul>
 *  <li>
 *   Each property has the syntax <code>key = value</code>
 *  </li>
 *  <li>
 *   The <i>key</i> may use any character but the equal sign '='.
 *  </li>
 *  <li>
 *   <i>value</i> may be separated on different lines if a backslash
 *   is placed at the end of the line that continues below.
 *  </li>
 *  <li>
 *   If <i>value</i> is a list of strings, each token is separated
 *   by a comma ','.
 *  </li>
 *  <li>
 *   Commas in each token are escaped placing a backslash right before
 *   the comma.
 *  </li>
 *  <li>
 *   If a <i>key<i> is used more than once, the values are appended
 *   like if they were on the same line separated with commas.
 *  </li>
 *  <li>
 *   Blank lines and lines starting with character '#' are skipped.
 *  </li>
 *  <li>
 *   If a property is named "include" (or whatever is defined by
 *   setInclude() and getInclude() and the value of that property is
 *   the full path to a file on disk, that file will be included into
 *   the ConfigurationsRepository.  Duplicate name values will be
 *   replaced, so be careful.
 *  </li>
 * </ul>
 *
 * <p>Here is an example of a valid extended properties file:
 *
 * <p><pre>
 *      # lines starting with # are comments
 *
 *      # This is the simplest property
 *      key = value
 *
 *      # A long property may be separated on multiple lines
 *      longvalue = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
 *                  aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
 *
 *      # This is a property with many tokens
 *      tokens_on_a_line = first token, second token
 *
 *      # This sequence generates exactly the same result
 *      tokens_on_multiple_lines = first token
 *      tokens_on_multiple_lines = second token
 *
 *      # commas may be escaped in tokens
 *      commas.excaped = Hi\, what'up?
 * </pre>
 *
 * <p><b>NOTE</b>: this class has <b>not</b> been written for
 * performance nor low memory usage.  In fact, it's way slower than it
 * could be and generates too much memory garbage.  But since
 * performance is not an issue during intialization (and there is not
 * much time to improve it), I wrote it this way.  If you don't like
 * it, go ahead and tune it up!
 *
 * @see org.apache.velocity.runtime.configuration.Configurations
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id: ExtendedProperties.java,v 1.2 2001/01/06 20:57:03 geirm Exp $
 */
public class ExtendedProperties
    extends ConfigurationsRepository
{
    /**
     * This is the name of the property that can point to other
     * properties file for including other properties files.
     */
    private static String include = "include";

    /**
     * This class is used to read properties lines.  These lines do
     * not terminate with new-line chars but rather when there is no
     * backslash sign a the end of the line.  This is used to
     * concatenate multiple lines for readability.
     */
    class PropertiesReader
        extends LineNumberReader
    {
        /**
         * Constructor.
         *
         * @param reader A Reader.
         */
        public PropertiesReader(Reader reader)
        {
            super(reader);
        }

        /**
         * Read a property.
         *
         * @return A String.
         * @exception IOException.
         */
        public String readProperty()
            throws IOException
        {
            StringBuffer buffer = new StringBuffer();

            try
            {
                while (true)
                {
                    String line = readLine().trim();
                    if ((line.length() != 0) && (line.charAt(0) != '#'))
                    {
                        if (line.endsWith("\\"))
                        {
                            line = line.substring(0, line.length() - 1);
                            buffer.append(line);
                        }
                        else
                        {
                            buffer.append(line);
                            break;
                        }
                    }
                }
            }
            catch (NullPointerException e)
            {
                return null;
            }

            return buffer.toString();
        }
    }

    /**
     * This class divides into tokens a property value.  Token
     * separator is "," but commas into the property value are escaped
     * using the backslash in front.
     */
    class PropertiesTokenizer
        extends StringTokenizer
    {
        /**
         * Constructor.
         *
         * @param string A String.
         */
        public PropertiesTokenizer(String string)
        {
            super(string, ",");
        }

        /**
         * Check whether the object has more tokens.
         *
         * @return True if the object has more tokens.
         */
        public boolean hasMoreTokens()
        {
            return super.hasMoreTokens();
        }

        /**
         * Get next token.
         *
         * @return A String.
         */
        public String nextToken()
        {
            StringBuffer buffer = new StringBuffer();

            while (hasMoreTokens())
            {
                String token = super.nextToken();
                if (token.endsWith("\\"))
                {
                    buffer.append(token.substring(0, token.length() - 1));
                    buffer.append(",");
                }
                else
                {
                    buffer.append(token);
                    break;
                }
            }

            return buffer.toString().trim();
        }
    }

    /**
     * Creates an empty extended properties object.
     */
    public ExtendedProperties ()
    {
    }

    /**
     * Creates and loads the extended properties from the specified
     * file.
     *
     * @param file A String.
     * @exception IOException.
     */
    public ExtendedProperties (String file)
        throws IOException
    {
        this.load(new FileInputStream(file));
    }

    /**
     * Gets the property value for including other properties files.
     * By default it is "include".
     *
     * @return A String.
     */
    public String getInclude()
    {
        return this.include;
    }

    /**
     * Sets the property value for including other properties files.
     * By default it is "include".
     *
     * @param inc A String.
     */
    public void setInclude(String inc)
    {
        this.include = inc;
    }

    /**
     * Load the properties from the given input stream.
     *
     * @param input An InputStream.
     * @exception IOException.
     */
    public synchronized void load(InputStream input)
        throws IOException
    {
        PropertiesReader reader =
            new PropertiesReader(new InputStreamReader(input));

        try
        {
            while (true)
            {
                String line = reader.readProperty();
                int equalSign = line.indexOf('=');

                if (equalSign > 0)
                {
                    String key = line.substring(0, equalSign).trim();
                    String value = line.substring(equalSign + 1).trim();

                    // Configure produces lines like this ... just
                    // ignore them.
                    if ("".equals(value))
                        continue;

                    // Recursively load properties files.
                    File file = new File(value);
                    if (getInclude() != null &&
                        key.equalsIgnoreCase(getInclude()) &&
                        file != null &&
                        file.exists() &&
                        file.canRead())
                        load ( new FileInputStream(file) );

                    PropertiesTokenizer tokenizer =
                        new PropertiesTokenizer(value);
                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();
                        Object o = this.get(key);
                        if (o instanceof String)
                        {
                            Vector v = new Vector(2);
                            v.addElement(o);
                            v.addElement(token);
                            this.put(key, v);
                        }
                        else if (o instanceof Vector)
                        {
                            ((Vector) o).addElement(token);
                        }
                        else
                        {
                            this.put(key, token);
                        }
                    }
                }
            }
        }
        catch (NullPointerException e)
        {
            // Should happen only when EOF is reached.
            return;
        }
    }

    /**
     * Save the properties to the given outputstream.
     *
     * @param output An OutputStream.
     * @param header A String.
     * @exception IOException.
     */
    public synchronized void save(OutputStream output,
                                  String Header)
        throws IOException
    {
        if(output != null)
        {
            PrintWriter theWrtr = new PrintWriter(output);
            if(Header != null)
            {
                theWrtr.println(Header);
            }
            Enumeration theKeys = keys();
            while(theKeys.hasMoreElements())
            {
                String key = (String) theKeys.nextElement();
                Object value = get((Object) key);
                if(value != null)
                {
                    if(value instanceof String)
                    {
                        StringBuffer currentOutput = new StringBuffer();
                        currentOutput.append(key);
                        currentOutput.append("=");
                        currentOutput.append((String) value);
                        theWrtr.println(currentOutput.toString());
                    }
                    else if(value instanceof Vector)
                    {
                        Vector values = (Vector) value;
                        Enumeration valuesEnum = values.elements();
                        while(valuesEnum.hasMoreElements())
                        {
                            String currentElement = 
                                   (String) valuesEnum.nextElement();
                            StringBuffer currentOutput = new StringBuffer();
                            currentOutput.append(key);
                            currentOutput.append("=");
                            currentOutput.append(currentElement);
                            theWrtr.println(currentOutput.toString());
                        }
                    }
                }    
                theWrtr.println();
                theWrtr.flush();
            }    
        }        
    }
}
