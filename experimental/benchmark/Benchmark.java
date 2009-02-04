//package org.apache.velocity.benchmark;

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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;


public class Benchmark
{
  int threadCnt = 10;
  int runCnt = 1000;
  
  public static final void main(String[] argv) throws Exception
  {
    Benchmark benchmark = new Benchmark();
    benchmark.go();
  }

  public void log(String str)
  {
    System.out.println(str);
  }
  
  public void go() throws Exception
  {
    VelocityEngine vengine = new VelocityEngine();
    vengine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
    //vengine.setProperty(RuntimeConstants.VM_ARGUMENTS_PASSBYVALUE, "true");
    vengine.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, "false");
    vengine.setProperty(RuntimeConstants.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, "0");
    vengine.setProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, "true");
    vengine.setProperty("file.resource.loader.modificationCheckInterval", "0");
    vengine.setProperty(RuntimeConstants.VM_LIBRARY, "vmlib1.vm,vmlib2.vm");
    log("Starting " + threadCnt + " threads which will run " + runCnt + " times");
    ArrayList list = new ArrayList(threadCnt);    
    for (int i = 0; i < threadCnt; i++)
    {
      VelocityThread vt = new VelocityThread(vengine, runCnt);
      list.add(vt);
      vt.start();
    }
  }  
}


/**
 * Worker thread which calls the template.
 */
class VelocityThread extends Thread
{
  VelocityEngine vengine = null;
  int runCnt = 1000;
  boolean runError = false;

  // Stuff for the context
  List innerList = null;
  List outerList = null;
  
  public void initContextObjs()
  {
    outerList = new ArrayList();
    innerList = new ArrayList();
    for (int i=0; i < 10; i++)
    {
      outerList.add(new Integer(i));
      innerList.add(new Integer(i));
    }    
  }

    
  public VelocityThread(VelocityEngine vengine, int runCnt)
  {
    this.vengine = vengine;
    this.runCnt = runCnt;
    initContextObjs();
  }

  public void run()
  {
    for (int i = 0; i < runCnt; i++)
    {
      Writer writer = new FastWriter();
      
      // We do the setup inside the loop so we can be a little realistic
      // Since this type of setup would be done in a real application,
      // And filling the context is appropriate for Velocity performance.      
      VelocityContext context = new VelocityContext();
      context.put("blue", "green");
      context.put("innerList", innerList);
      context.put("outerList", outerList);
      
      for (int j=0; j<10; j++)
      {  
        Test test = new Test();
        test.name = "test" + j;
        context.put(test.name, test);
      }

      // Render template
      try
      {
        vengine.mergeTemplate("main.vm", "utf-8", context, writer);
      }
      catch(Exception e)
      {
        System.out.println(e);
        e.printStackTrace();
        runError = true;
        System.out.println("Errors during run");
        System.exit(1);
      }      
    }
  }
  
  
  /**
   * Test Object to be referenced from the template
   */
  public static class Test
  {
    String name = null;    
    public String getName()
    {
      return name;
    }
    
    public void setName(String name)
    {
      this.name = name;
    } 
  }  
  
}



class FastWriter extends Writer
{
  char[] buffer = new char[20000];

  public FastWriter()
  {
    super();
  }

  public void write(char[] buf, int off, int len)
  {
    for (int i=0; i < len; i++)
    {
      buffer[i] = buf[i+off];
    }    
  }  

  public void close() {}
  public void flush() {}
  
}




