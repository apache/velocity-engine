package org.apache.velocity.runtime.compiler;

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

import de.fub.bytecode.generic.*;
import de.fub.bytecode.Constants;

/**
 * The start of a velocity template compiler. Incomplete.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @version $Id: Compiler.java,v 1.4 2001/03/20 01:11:19 jon Exp $
 */
public class Compiler implements InstructionConstants
{
    public static void main(String[] args)
    {
        String template = args[0].substring(0, args[0].indexOf("."));
        ClassGen cg =
                new ClassGen(template, "java.lang.Object", "<generated>",
                Constants.ACC_PUBLIC | Constants.ACC_SUPER, null);
        
        ConstantPoolGen cp = cg.getConstantPool(); // cg creates constant pool
        InstructionList il = new InstructionList();
        MethodGen mg = new MethodGen(Constants.ACC_STATIC |
                Constants.ACC_PUBLIC, // access flags
                Type.VOID, // return type
                new Type[]{ // argument types
                new ArrayType(Type.STRING, 1)},
                new String[]{ "argv" }, // arg names
                "main", template, // method, class
                il, cp);

        //Add often needed constants to constant pool.

        int br_index = cp.addClass("java.io.BufferedReader");
        int ir_index = cp.addClass("java.io.InputStreamReader");
        int system_out = cp.addFieldref("java.lang.System", "out", // System.out
                "Ljava/io/PrintStream;");
        int system_in = cp.addFieldref("java.lang.System", "in", // System.in
                "Ljava/io/InputStream;");

        // Create BufferedReader object and store it in local variable `in'.

        il.append(new NEW(br_index));
        il.append(DUP);
        il.append(new NEW(ir_index));
        il.append(DUP);
        il.append(new GETSTATIC(system_in));

        // Call constructors, i.e. BufferedReader(InputStreamReader())

        il.append( new INVOKESPECIAL(
                cp.addMethodref("java.io.InputStreamReader", "<init>",
                "(Ljava/io/InputStream;)V")));
        il.append( new INVOKESPECIAL(
                cp.addMethodref("java.io.BufferedReader", "<init>", "(Ljava/io/Reader;)V")));
        
        // Create local variable `in'

        LocalVariableGen lg = mg.addLocalVariable("in",
                new ObjectType("java.io.BufferedReader"), null, null);
        int in = lg.getIndex();
        lg.setStart(il.append(new ASTORE(in))); // `i' valid from here

        // Create local variable `name'

        lg = mg.addLocalVariable("name", Type.STRING, null, null);
        int name = lg.getIndex();
        il.append(ACONST_NULL);
        lg.setStart(il.append(new ASTORE(name))); // `name' valid from here

        InstructionHandle try_start = il.append(new GETSTATIC(system_out));
        il.append(new PUSH(cp, "I will be a template compiler!"));
        il.append( new INVOKEVIRTUAL(
                cp.addMethodref("java.io.PrintStream", "println", "(Ljava/lang/String;)V")));
        
        // Upon normal execution we jump behind exception handler,
        // the target address is not known yet.

        GOTO g = new GOTO(null);
        InstructionHandle try_end = il.append(g);

        InstructionHandle handler = il.append(RETURN);
        mg.addExceptionHandler(try_start, try_end, handler,
                new ObjectType("java.io.IOException"));

        // Normal code continues, now we can set the branch target of the GOTO.

        InstructionHandle ih = il.append(new GETSTATIC(system_out));
        g.setTarget(ih);

        // String concatenation compiles to StringBuffer operations.
        
        il.append(new NEW(cp.addClass("java.lang.StringBuffer")));
        il.append(DUP);
        il.append(new PUSH(cp, " "));
        il.append( new INVOKESPECIAL(
                cp.addMethodref("java.lang.StringBuffer", "<init>", "(Ljava/lang/String;)V")));
        
        il.append(new ALOAD(name));

        // One can also abstract from using the ugly signature syntax by using
        // the getMethodSignature() method. For example:

        String sig = Type.getMethodSignature(Type.STRINGBUFFER,
                new Type[]{ Type.STRING });
        il.append( new INVOKEVIRTUAL(
                cp.addMethodref("java.lang.StringBuffer", "append", sig)));

        il.append( new INVOKEVIRTUAL(
                cp.addMethodref("java.lang.StringBuffer", "toString", "()Ljava/lang/String;")));

        il.append(RETURN);
        
        mg.setMaxStack(5); // Needed stack size
        cg.addMethod(mg.getMethod());

        // Add public <init> method, i.e. empty constructor
        cg.addEmptyConstructor(Constants.ACC_PUBLIC);

        // Get JavaClass object and dump it to file.
        try
        {
            cg.getJavaClass().dump(template + ".class");
        }
        catch (java.io.IOException e)
        {
            System.err.println(e);
        }
    }
}
