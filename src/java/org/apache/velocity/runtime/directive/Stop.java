package org.apache.velocity.runtime.directive;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.Writer;
import java.util.ArrayList;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * This class implements the #stop directive which allows
 * a user to stop rendering the current template.  the #stop directive
 * with no arguments will immediately stop rendering the current template
 * If the stop directive is called with the 'parse' argument, e.g.; #stop(parse),
 * then rendering will end within the current parsing unit, but resume at 
 * the #parse directive call.  If not within a parsing unit, then #stop will
 * behave as if called with no parameters.
 */
public class Stop extends Directive
{  
  
    /**
     * Set to true if called like #stop(parse)
     */
    private boolean parseStop = false;

    /**
     * Return name of this directive.
     * @return The name of this directive.
     */
    public String getName()
    {
        return "stop";
    }
  
    /**
     * Return type of this directive.
     * @return The type of this directive.
     */
    public int getType()
    {
        return LINE;
    }
  
    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context,
        Node node) throws TemplateInitException
    {
        super.init(rs, context, node);
        if (node.jjtGetNumChildren() == 1)  // first child is "parse"
        {  
            String keyword = node.jjtGetChild(0).getFirstToken().image;
            if (!keyword.equals("parse"))
            {
              throw new VelocityException("The #stop directive only accepts the 'parse' keyword at "
                 + Log.formatFileString(this));
            }
            
            parseStop = true;
        }
    }
    
    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
    {
        if (parseStop)
        {
            // if called like #stop(parse) then throw this throwable that will be
            // caught by the Parse.java directive.
            throw new StopParseThrowable();            
        }
        else
        {
            // This stop directive was called without arguments, so throw a throwable
            // that will be caught the Template.
            throw new StopThrowable();          
        }
    }
        
    /**
     * We select to overide Error here intead of RuntimeInstance because there are
     * certain nodes that catch RuntimeException when rendering their children, and log
     * the event to error.  But of course in the case that the template renders a Stop
     * node we don't want this to happen.
     */
    public static class StopThrowable extends Error
    {      
    }
  
    /**
     * Exception caught by <code>Parse.java</code> parse directive so that rendering
     * will stop within a given parsing unit.  Extend StopThrowable so that if the
     * This is not caught by <code>Parse.java</code> then it will be caught at the 
     * templat level.
     */
    public static class StopParseThrowable extends StopThrowable
    {      
    }
    
    /**
     * Called by the parser to validate the argument types
     */
    public void checkArgs(ArrayList<Integer> argtypes,  Token t, String templateName)
      throws ParseException
    {
        if (argtypes.size() > 1 ||
            (argtypes.size() == 1 && argtypes.get(0) != ParserTreeConstants.JJTWORD))
        {
            throw new MacroParseException("Only the 'parse' keyword allowed as an argument",
               templateName, t);
        }
    }    
}

