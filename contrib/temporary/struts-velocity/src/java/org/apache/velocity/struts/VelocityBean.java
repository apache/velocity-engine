package org.apache.velocity.struts;

/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;


/**
 *  Baseclass for Strut's Tag -> Bean work
 *
 *  Nicht sehr gut.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * $Id: VelocityBean.java,v 1.1.12.1 2004/03/04 00:18:28 geirm Exp $
 */
public class VelocityBean
{
    protected ServletContext context;
    protected HttpSession session;
    protected ServletRequest request;

    public VelocityBean( ServletContext context, HttpSession sess, ServletRequest request )
    {
        this.context = context;
        this.session = sess;
        this.request = request;
    }
}
