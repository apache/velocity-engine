package org.apache.velocity.util.introspection;

/*
 * Copyright 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.velocity.runtime.RuntimeLogger;

/**
 *  Marker interface to let an uberspector indicate it can and wants to
 *  log
 *
 *  Thanks to Paulo for the suggestion
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @version $Id: UberspectLoggable.java,v 1.1.4.1 2004/03/03 23:23:08 geirm Exp $
 *
 */
public interface UberspectLoggable
{
    /**
     *  Sets the logger.  This will be called before any calls to the
     *  uberspector
     */
    public void setRuntimeLogger(RuntimeLogger logger);
}
