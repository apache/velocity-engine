package org.apache.velocity.runtime.directive;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.BufferedReader;

import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.exception.MethodInvocationException;

/**
 *  The function of this class is to proxy for the calling parameter to the VM.
 *
 *  This class is designed to be used in conjunction with the VMContext class
 *  which knows how to get and set values via it, rather than a simple get()
 *  or put() from a hashtable-like object.
 *
 *  There is probably a lot of undocumented subtlty here, so step lightly.
 *
 *  We rely on the observation that an instance of this object has a constant
 *  state throughout its lifetime as it's bound to the use-instance of a VM.
 *  In other words, it's created by the VelocimacroProxy class, to represent
 *  one of the arguments to a VM in a specific template.  Since the template
 *  is fixed (it's a file...), we don't have to worry that the args to the VM
 *  will change.  Yes, the VM will be called in other templates, or in other 
 *  places on the same template, bit those are different use-instances.
 *
 *  These arguments can be, in the lingo of
 *  the parser, one of :
 *   <ul>
 *   <li> Reference() : anything that starts with '$'
 *   <li> StringLiteral() : something like "$foo" or "hello geir"
 *   <li> NumberLiteral() : 1, 2 etc
 *   <li> IntegerRange() : [ 1..2] or [$foo .. $bar]
 *   <li> ObjectArray() : [ "a", "b", "c"]
 *   <li> True() : true
 *   <li> False() : false
 *    <li>Word() : not likely - this is simply allowed by the parser so we can have
 *             syntactical sugar like #foreach($a in $b)  where 'in' is the Word  
 *    </ul>
 *  Now, Reference(), StringLit, NumberLit, IntRange, ObjArr are all dynamic things, so 
 *  their value is gotten with the use of a context.  The others are constants.  The trick
 *  we rely on is that the context rather than this class really represents the 
 *  state of the argument. We are simply proxying for the thing, returning the proper value 
 *  when asked, and storing the proper value in the appropriate context when asked.
 *
 *  So, the hope here, so an instance of this can be shared across threads, is to
 *  keep any dynamic stuff out of it, relying on trick of having the appropriate 
 *  context handed to us, and when a constant argument, letting VMContext punch that 
 *  into a local context.
 *  
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @version $Id: VMProxyArg.java,v 1.10 2001/09/10 08:56:47 geirm Exp $ 
 */
public class VMProxyArg
{
    /**  type of arg I will have */
    private int type = 0;

    /**  the AST if the type is such that it's dynamic (ex. JJTREFERENCE ) */
    private SimpleNode nodeTree = null;

    /**  reference for the object if we proxy for a static arg like an NumberLiteral */
    private Object staticObject = null;

    /** not used in this impl : carries the appropriate user context */
    private InternalContextAdapter usercontext = null;

    /** number of children in our tree if a reference */
    private int numTreeChildren = 0;

    /** our identity in the current context */
    private String contextReference = null;
    
    /** the reference we are proxying for  */
    private String callerReference = null;

    /** the 'de-dollared' reference if we are a ref but don't have a method attached */
    private String singleLevelRef = null;

    /** by default, we are dynamic.  safest */
    private boolean constant = false;

    /** in the event our type is switched - we don't care really what it is */
    private final int GENERALSTATIC = -1;

    private RuntimeServices rsvc = null;

    /**
     *  ctor for current impl 
     *
     *  takes the reference literal we are proxying for, the literal 
     *  the VM we are for is called with...
     *
     *  @param contextRef reference arg in the definition of the VM, used in the VM
     *  @param callerRef  reference used by the caller as an arg to the VM
     *  @param t  type of arg : JJTREFERENCE, JJTTRUE, etc
     */
    public VMProxyArg( RuntimeServices rs, String contextRef, String callerRef, int t )
    {
        rsvc = rs;

        contextReference = contextRef;
        callerReference = callerRef;
        type = t;
        
        /*
         *  make our AST if necessary
         */
        setup();

        /*
         *  if we are multi-node tree, then save the size to 
         *  avoid fn call overhead 
         */
        if( nodeTree != null)
            numTreeChildren = nodeTree.jjtGetNumChildren();

        /*
         *  if we are a reference, and 'scalar' (i.e. $foo )
         *  then get the de-dollared ref so we can
         *  hit our context directly, avoiding the AST
         */
        if ( type == ParserTreeConstants.JJTREFERENCE )
        {
            if ( numTreeChildren == 0)
            {
                /*
                 * do this properly and use the Reference node
                 */
                 singleLevelRef = ((ASTReference) nodeTree).getRootString();
            }
        }
    }

    /**
     *  tells if arg we are poxying for is
     *  dynamic or constant.
     *
     *  @return true of constant, false otherwise
     */
    public boolean isConstant()
    {
        return constant;
    }

    /**
     *  Invoked by VMContext when Context.put() is called for a proxied reference.
     *
     *  @param context context to modify via direct placement, or AST.setValue()
     *  @param o  new value of reference
     *  @return Object currently null
     */
    public Object setObject(  InternalContextAdapter context,  Object o )
    {  
        /*
         *  if we are a reference, we could be updating a property
         */

        if( type == ParserTreeConstants.JJTREFERENCE )
        {
            if( numTreeChildren > 0)
            {
                /*
                 *  we are a property, and being updated such as
                 *  #foo( $bar.BangStart) 
                 */

                try
                {
                    ( (ASTReference) nodeTree).setValue( context, o );
                }
                catch( MethodInvocationException mie )
                {
                    rsvc.error("VMProxyArg.getObject() : method invocation error setting value : " + mie );                    
                }
           }
            else
            {
                /*
                 *  we are a 'single level' reference like $foo, so we can set
                 *  out context directly
                 */

                context.put( singleLevelRef, o);
               
                // alternate impl : usercontext.put( singleLevelRef, o);
             }
        }
        else
        {
            /*
             *  if we aren't a reference, then we simply switch type, 
             *  get a new value, and it doesn't go into the context
             *
             *  in current impl, this shouldn't happen.
             */

            type = GENERALSTATIC;
            staticObject = o;

            rsvc.error("VMProxyArg.setObject() : Programmer error : I am a constant!  No setting! : "
                               + contextReference + " / " + callerReference);
        }

        return null;
    }

  
    /**
     *  returns the value of the reference.  Generally, this is only
     *  called for dynamic proxies, as the static ones should have
     *  been stored in the VMContext's localcontext store
     *
     *  @param context Context to use for getting current value
     *  @return Object value
     *
     */
    public Object getObject( InternalContextAdapter context )
    {        
        try
        {

            /*
             *  we need to output based on our type
             */

            Object retObject = null;
            
            if ( type == ParserTreeConstants.JJTREFERENCE ) 
            {                
                /*
                 *  two cases :  scalar reference ($foo) or multi-level ($foo.bar....)
                 */
                
                if ( numTreeChildren == 0)
                {      
                    /*
                     *  if I am a single-level reference, can I not get get it out of my context?
                     */
                
                    retObject = context.get( singleLevelRef );
                }
                else
                {
                    /*
                     *  I need to let the AST produce it for me.
                     */

                    retObject = nodeTree.execute( null, context);
                }
            }
            else if( type == ParserTreeConstants.JJTOBJECTARRAY )
            {
                retObject = nodeTree.value( context );
            }
            else if ( type == ParserTreeConstants.JJTINTEGERRANGE)
            {
                retObject = nodeTree.value( context );    
            }
            else if( type == ParserTreeConstants.JJTTRUE )
            {
                retObject = staticObject;
            }
            else if ( type == ParserTreeConstants.JJTFALSE )
            {
                retObject = staticObject;
            }
            else if ( type == ParserTreeConstants.JJTSTRINGLITERAL )
            {
                retObject =  nodeTree.value( context );
            }
            else if ( type == ParserTreeConstants.JJTNUMBERLITERAL )
            {
                retObject = staticObject;
            }
            else if ( type == ParserTreeConstants.JJTTEXT )
            {
                /*
                 *  this really shouldn't happen.  text is just a thowaway arg for #foreach()
                 */
                           
                try 
                {
                    StringWriter writer =new StringWriter();
                    nodeTree.render( context, writer );
                    
                    retObject = writer;
                }
                catch (Exception e )
                {
                    rsvc.error("VMProxyArg.getObject() : error rendering reference : " + e );
                }
            }
            else if( type ==  GENERALSTATIC )
            {
                retObject = staticObject;
            }
            else
            {
                rsvc.error("Unsupported VM arg type : VM arg = " + callerReference +" type = " + type + "( VMProxyArg.getObject() )");
            }
            
            return retObject;
        }
        catch( MethodInvocationException mie )
        {
            /*
             *  not ideal, but otherwise we propogate out to the 
             *  VMContext, and the Context interface's put/get 
             *  don't throw. So this is a the best compromise
             *  I can think of
             */
            
            rsvc.error("VMProxyArg.getObject() : method invocation error getting value : " + mie );
            
            return null;
        }
    }

    /**
     *  does the housekeeping upon creationg.  If a dynamic type
     *  it needs to make an AST for further get()/set() operations
     *  Anything else is constant.
     */
    private void setup()
    {
        switch( type ) {

        case ParserTreeConstants.JJTINTEGERRANGE :
        case ParserTreeConstants.JJTREFERENCE :
        case ParserTreeConstants.JJTOBJECTARRAY :
        case ParserTreeConstants.JJTSTRINGLITERAL :
        case ParserTreeConstants.JJTTEXT :
            {
                /*
                 *  dynamic types, just render
                 */
                
                constant = false;

                try 
                {
                    /*
                     *  fakie : wrap in  directive to get the parser to treat our args as args
                     *   it doesn't matter that #include() can't take all these types, because we 
                     *   just want the parser to consider our arg as a Directive/VM arg rather than
                     *   as if inline in schmoo
                     */

                    String buff ="#include(" + callerReference + " ) ";

                    //ByteArrayInputStream inStream = new ByteArrayInputStream( buff.getBytes() );

                    BufferedReader br = new BufferedReader( new StringReader( buff ) );

                    nodeTree = rsvc.parse( br, "VMProxyArg:" + callerReference );

                    /*
                     *  now, our tree really is the first DirectiveArg(), and only one
                     */

                    nodeTree = (SimpleNode) nodeTree.jjtGetChild(0).jjtGetChild(0);

                    /*
                     * sanity check
                     */

                    if ( nodeTree != null && nodeTree.getType() != type )
                    {
                        rsvc.error( "VMProxyArg.setup() : programmer error : type doesn't match node type.");
                    }

                    /*
                     *  init.  We can do this as they are only references
                     */

                    nodeTree.init(null, rsvc);
                } 
                catch ( Exception e ) 
                {
                    rsvc.error("VMProxyArg.setup() : exception " + callerReference + 
                                  " : "  + StringUtils.stackTrace(e));
                }

                break;
            }
            
        case ParserTreeConstants.JJTTRUE :
            {
                constant = true;
                staticObject = new  Boolean(true);
                break;
            }

        case ParserTreeConstants.JJTFALSE :
            {
                constant = true;
                staticObject =  new Boolean(false);
                break;
            }

        case ParserTreeConstants.JJTNUMBERLITERAL :
            {
                constant = true;
                staticObject = new Integer(callerReference);
                break;
            }

      case ParserTreeConstants.JJTWORD :
          {
              /*
               *  this is technically an error...
               */

              rsvc.error("Unsupported arg type : " + callerReference
                            + "  You most likely intended to call a VM with a string literal, so enclose with ' or \" characters. (VMProxyArg.setup())");
              constant = true;
              staticObject = new String( callerReference );

              break;
          }
 
        default :
            {
                 rsvc.error(" VMProxyArg.setup() : unsupported type : " 
                                    + callerReference  );
            }
        }
    }

    /*
     * CODE FOR ALTERNATE IMPL : please ignore.  I will remove when confortable with current.
     */

    /**
     *  not used in current impl
     *
     *  Constructor for alternate impl where VelProxy class would make new
     *  VMProxyArg objects, and use this contructor to avoid reparsing the 
     *  reference args
     *
     *  that impl also had the VMProxyArg carry it's context
     */
    public VMProxyArg( VMProxyArg model, InternalContextAdapter c )
    {
        usercontext = c;
        contextReference = model.getContextReference();
        callerReference = model.getCallerReference();
        nodeTree = model.getNodeTree();
        staticObject = model.getStaticObject();
        type = model.getType();

       if( nodeTree != null)
            numTreeChildren = nodeTree.jjtGetNumChildren();

        if ( type == ParserTreeConstants.JJTREFERENCE )
        {
            if ( numTreeChildren == 0)
            {
                /*
                 *  use the reference node to do this...
                 */
                singleLevelRef = ((ASTReference) nodeTree).getRootString();
            }
        }
    }
  
    public String getCallerReference()
    {
        return callerReference;
    }

    public String getContextReference()
    {
        return contextReference;
    }

    public SimpleNode getNodeTree()
    {
        return nodeTree;
    }

    public Object getStaticObject()
    {
        return staticObject;
    }

    public int getType()
    {
        return type;
    }
}
