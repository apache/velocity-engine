package org.apache.velocity.runtime.configuration;

/*
 * Copyright (c) 2001 The Java Apache Project.  All rights reserved.
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
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:daveb@miceda-data">Dave Bryson</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Configuration.java,v 1.11 2001/03/12 04:23:10 geirm Exp $
 */
public class Configuration extends Hashtable
{
    /**
     * Default configurations repository.
     */
    private Configuration defaults;

    /**
     * The file connected to this repository (holding comments and
     * such).
     *
     * @serial
     */
    protected String file;

    private boolean isInitialized = false;

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
    class PropertiesReader extends LineNumberReader
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
    class PropertiesTokenizer extends StringTokenizer
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
    public Configuration ()
    {
        super();
    }

    /**
     * Creates and loads the extended properties from the specified
     * file.
     *
     * @param file A String.
     * @exception IOException.
     */
    public Configuration (String file) throws IOException
    {
        this(file,null);
    }

    /**
     * Creates and loads the extended properties from the specified
     * file.
     *
     * @param file A String.
     * @exception IOException.
     */
    public Configuration (String file, String defaultFile)
        throws IOException
    {
        this.file = file;
        this.load(new FileInputStream(file));
        
        if (defaultFile != null)
        {
            defaults = new Configuration(defaultFile);
        }            
    }

    /**
     * Load Configuration from a properties file. 
     *
     * @param propertiesFileName The file name.
     * @exception IOException, if there was an I/O problem.
     */
    public void setPropertiesFileName(String propertiesFileName)
        throws IOException
    {
        file = propertiesFileName;
        
        if ( file == null )
        {
            throw new IOException ( "VelocityResources: fileName must not be null!" );
        }            

        init( new Configuration(file) );
    }

    /**
     * Load configuration from the an InputStream.  
     *
     * @param properties A Properties object.
     */
    public void setPropertiesInputStream( InputStream is ) 
        throws IOException
    {
        load(is);
        init(null);
    }

    /**
     * Private initializer method that sets up the generic
     * resources.
     *
     * @exception IOException, if there was an I/O problem.
     */
    private void init( Configuration exp ) throws IOException
    {
        isInitialized = true;
    }
    
    /**
     * Indicate to client code whether property
     * resources have been initialized or not.
     */
    public boolean isInitialized()
    {
        return isInitialized;
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
                        setProperty(key,token);
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
     * Set a property taking into consideration
     * duplicate keys.
     *
     * @param String key
     * @param String token
     */
    public void setProperty(String key, Object token)
    {
        Object o = this.get(key);
        
        if (o instanceof String)
        {
            Vector v = new Vector(2);
            v.addElement(o);
            v.addElement(token);
            put(key, v);
        }
        else if (o instanceof Vector)
        {
            ((Vector) o).addElement(token);
        }
        else
        {
            put(key, token);
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

    /**
     * Combines an existing Hashtable with this Hashtable.
     *
     * Warning: It will overwrite previous entries without warning.
     *
     * @param hash A Hashtable.
     */
    public void combine (Hashtable hash)
    {
        for (Enumeration e = hash.keys() ; e.hasMoreElements() ;)
        {
            String key = (String) e.nextElement();
            this.put ( key, hash.get(key) );
        }
    }

    /**
     * Set a property making sure that the property
     * is overriden. We use this in the case where
     * there is a default property all ready specified
     * and we don't want a Vector created with the default
     * and the new value which setProperty(k,v) above
     * will do. We want to replace the value.
     *
     * @param String key
     * @param String value
     */
    public void setOverridingProperty(String key, Object token)
    {
        this.put(key, token);
    }

    /// methods from Configurations

    /**
     * Get the list of the keys contained in the configuration
     * repository.
     *
     * @return An Enumeration.
     */
    public Enumeration getKeys()
    {
        return keys();
    }

    /**
     * Get the list of the keys contained in the configuration
     * repository that match the specified prefix.
     *
     * @param prefix The prefix to test against.
     * @return An Enumeration of keys that match the prefix.
     */
    public Enumeration getKeys(String prefix)
    {
        Enumeration keys = keys();
        Vector matchingKeys = new Vector();
        while( keys.hasMoreElements() )
        {
            Object key = keys.nextElement();
            if( key instanceof String &&
                ((String) key).startsWith(prefix) )
            {
                matchingKeys.addElement(key);
            }
        }
        return matchingKeys.elements();
    }

    /**
     * Create a Configurations object that is a subset
     * of this one. Take into account duplicate keys
     * by using the setProperty() in Configuration.
     *
     * @param String prefix
     */
    public Configuration subset(String prefix)
    {
        Configuration c = new Configuration();
        Enumeration keys = keys();
        boolean validSubset = false;
        
        while( keys.hasMoreElements() )
        {
            Object key = keys.nextElement();
            
            if( key instanceof String &&
                ((String) key).startsWith(prefix) )
            {
                if (!validSubset)
                {
                    validSubset = true;
                }
                
                String newKey = ((String)key).substring(prefix.length() + 1);
                //c.setProperty(newKey, get(key));
                c.put(newKey, get(key));
            }
        }
        
        if (validSubset)
        {
            return c;
        }
        else
        {
            return null;
        }
    }

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated string.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String.
     */
    public String getString(String key)
    {
        return getString(key, null);
    }

    /**
     * Get a string associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated string if key is found,
     * default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String.
     */
    public String getString(String key,
                            String defaultValue)
    {
        Object value = get(key);

        if (value instanceof String)
        {
            return (String) value;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getString(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                " doesn't map to a String object");
        }
    }

    /**
     * Get a list of properties associated with the given
     * configuration key.
     *
     * @param key The configuration key.
     * @return The associated properties if key is found.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String/Vector.
     * @exception IllegalArgumentException if one of the tokens is
     * malformed (does not contain an equals sign).
     */
    public Properties getProperties(String key)
    {
        return getProperties(key, new Properties());
    }

    /**
     * Get a list of properties associated with the given
     * configuration key.
     *
     * @param key The configuration key.
     * @return The associated properties if key is found.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String/Vector.
     * @exception IllegalArgumentException if one of the tokens is
     * malformed (does not contain an equals sign).
     */
    public Properties getProperties(String key,
                                    Properties defaults)
    {
        /*
         * Grab an array of the tokens for this key.
         */
        String[] tokens = getStringArray(key);

        /* 
         * Each token is of the form 'key=value'.
         */
        Properties props = new Properties(defaults);
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i];
            int equalSign = token.indexOf('=');
            if (equalSign > 0)
            {
                String pkey = token.substring(0, equalSign).trim();
                String pvalue = token.substring(equalSign + 1).trim();
                props.put(pkey, pvalue);
            }
            else
            {
                throw new IllegalArgumentException("'" +
                                                   token +
                                                   "' does not contain " +
                                                   "an equals sign");
            }
        }
        return props;
    }

    /**
     * Get an array of strings associated with the given configuration
     * key.
     *
     * @param key The configuration key.
     * @return The associated string array if key is found.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a String/Vector.
     */
    public String[] getStringArray(String key)
    {
        Object value = get(key);

        // What's your vector, Victor?
        Vector vector;
        if (value instanceof String)
        {
            vector = new Vector(1);
            vector.addElement(value);
        }
        else if (value instanceof Vector)
        {
            vector = (Vector)value;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getStringArray(key);
            }
            else
            {
                return new String[0];
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a" +
                                         " String/Vector object");
        }

        String[] tokens = new String[vector.size()];
        for (int i = 0; i < tokens.length; i++)
        tokens[i] = (String)vector.elementAt(i);

        return tokens;
    }

    /**
     * Get a list of strings associated with the given configuration
     * key.
     *
     * @param key The configuration key.
     * @return The associated Enumeration.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Vector.
     */
    public Enumeration getList(String key)
    {
        return getVector(key, null).elements();
    }

    /**
     * Get a Vector of strings associated with the given configuration
     * key.
     *
     * @param key The configuration key.
     * @return The associated Vector.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Vector.
     */
    public Vector getVector(String key)
    {
        return getVector(key, null);
    }

    /**
     * Get a Vector of strings associated with the given configuration
     * key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated Vector.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Vector.
     */
    public Vector getVector(String key,
                            Vector defaultValue)
    {
        Object value = get(key);

        if (value instanceof Vector)
        {
            return (Vector) value;
        }
        else if (value instanceof String)
        {
            Vector v = new Vector(1);
            v.addElement((String) value);
            put(key, v);
            return v;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getVector(key, defaultValue);
            }
            else
            {
                return ((defaultValue == null) ?
                        new Vector() : defaultValue);
            }
        }
        else
        {
            throw new ClassCastException(key +
                " doesn't map to a Vector object");
        }
    }

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated boolean.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public boolean getBoolean(String key)
    {
        Boolean b = getBoolean(key, (Boolean) null);
        if (b != null)
        {
            return b.booleanValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public boolean getBoolean(String key,
                              boolean defaultValue)
    {
        return getBoolean(key, new Boolean(defaultValue)).booleanValue();
    }

    /**
     * Get a boolean associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated boolean if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Boolean.
     */
    public Boolean getBoolean(String key,
                              Boolean defaultValue)
    {
    
        Object value = get(key);

        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof String)
        {
            Boolean b = new Boolean((String) value);
            put(key, b);
            return b;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getBoolean(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                " doesn't map to a Boolean object");
        }
    }

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated byte.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public byte getByte(String key)
    {
        Byte b = getByte(key, null);
        if (b != null)
        {
            return b.byteValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public byte getByte(String key,
                        byte defaultValue)
    {
        return getByte(key, new Byte(defaultValue)).byteValue();
    }

    /**
     * Get a byte associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated byte if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Byte.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Byte getByte(String key,
                        Byte defaultValue)
    {
        Object value = get(key);

        if (value instanceof Byte)
        {
            return (Byte) value;
        }
        else if (value instanceof String)
        {
            Byte b = new Byte((String) value);
            put(key, b);
            return b;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getByte(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a Byte object");
        }
    }

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated short.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public short getShort(String key)
    {
        Short s = getShort(key, null);
        if (s != null)
        {
            return s.shortValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public short getShort(String key,
                          short defaultValue)
    {
        return getShort(key, new Short(defaultValue)).shortValue();
    }

    /**
     * Get a short associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated short if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Short.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Short getShort(String key,
                          Short defaultValue)
    {
        Object value = get(key);

        if (value instanceof Short)
        {
            return (Short) value;
        }
        else if (value instanceof String)
        {
            Short s = new Short((String) value);
            put(key, s);
            return s;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getShort(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a Short object");
        }
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as an integer.
     *
     * @param name The resource name.
     * @return The value of the resource as an integer.
     */
    public int getInt(String name)
    {
        return getInteger(name);
    }

    /**
     * The purpose of this method is to get the configuration resource
     * with the given name as an integer, or a default value.
     *
     * @param name The resource name
     * @param def The default value of the resource.
     * @return The value of the resource as an integer.
     */
    public int getInt(String name,
                      int def)
    {
        return getInteger(name, def);
    }

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated int.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public int getInteger(String key)
    {
        Integer i = getInteger(key, null);
        if (i != null)
        {
            return i.intValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public int getInteger(String key,
                          int defaultValue)
    {
        return getInteger(key, new Integer(defaultValue)).intValue();
    }

    /**
     * Get a int associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated int if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Integer.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Integer getInteger(String key,
                              Integer defaultValue)
    {
        Object value = get(key);

        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        else if (value instanceof String)
        {
            Integer i = new Integer((String) value);
            put(key, i);
            return i;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getInteger(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a Integer object");
        }
    }

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated long.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public long getLong(String key)
    {
        Long l = getLong(key, null);
        if (l != null)
        {
            return l.longValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public long getLong(String key,
                        long defaultValue)
    {
        return getLong(key, new Long(defaultValue)).longValue();
    }

    /**
     * Get a long associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated long if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Long.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Long getLong(String key,
                        Long defaultValue)
    {
        Object value = get(key);

        if (value instanceof Long)
        {
            return (Long) value;
        }
        else if (value instanceof String)
        {
            Long l = new Long((String) value);
            put(key, l);
            return l;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getLong(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a Long object");
        }
    }

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated float.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public float getFloat(String key)
    {
        Float f = getFloat(key, null);
        if (f != null)
        {
            return f.floatValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public float getFloat(String key,
                          float defaultValue)
    {
        return getFloat(key, new Float(defaultValue)).floatValue();
    }

    /**
     * Get a float associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated float if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Float.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Float getFloat(String key,
                          Float defaultValue)
    {
        Object value = get(key);

        if (value instanceof Float)
        {
            return (Float) value;
        }
        else if (value instanceof String)
        {
            Float f = new Float((String) value);
            put(key, f);
            return f;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getFloat(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a Float object");
        }
    }

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @return The associated double.
     * @exception NoSuchElementException is thrown if the key doesn't
     * map to an existing object.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public double getDouble(String key)
    {
        Double d = getDouble(key, null);
        if (d != null)
        {
            return d.doubleValue();
        }
        else
        {
            throw new NoSuchElementException(key +
                                             " doesn't map to an" +
                                             " existing object");
        }
    }

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public double getDouble(String key,
                            double defaultValue)
    {
        return getDouble(key, new Double(defaultValue)).doubleValue();
    }

    /**
     * Get a double associated with the given configuration key.
     *
     * @param key The configuration key.
     * @param defaultValue The default value.
     * @return The associated double if key is found and has valid
     * format, default value otherwise.
     * @exception ClassCastException is thrown if the key maps to an
     * object that is not a Double.
     * @exception NumberFormatException is thrown if the value mapped
     * by the key has not a valid number format.
     */
    public Double getDouble(String key,
                            Double defaultValue)
    {
        Object value = get(key);

        if (value instanceof Double)
        {
            return (Double) value;
        }
        else if (value instanceof String)
        {
            Double d = new Double((String) value);
            put(key, d);
            return d;
        }
        else if (value == null)
        {
            if (defaults != null)
            {
                return defaults.getDouble(key, defaultValue);
            }
            else
            {
                return defaultValue;
            }
        }
        else
        {
            throw new ClassCastException(key +
                                         " doesn't map to a Double object");
        }
    }
}
