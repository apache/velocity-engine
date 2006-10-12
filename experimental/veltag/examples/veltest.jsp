<%--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.    
--%>
<%@ taglib uri="/WEB-INF/veltag.tld" prefix="vel" %>

<html>
<head>
  <title> Velocity! </title>
</head>

<jsp:useBean id="mybean"  class="SimpleBean" />
<jsp:useBean id="mybeanreq" class="SimpleBean" scope="request" />

<body>
    <vel:velocity strictaccess="true">

         #set($mybean = $scopetool.getPageScope("mybean"))
         #set($mybeanreq = $scopetool.getRequestScope("mybeanreq"))

         #if(true)
            this is true!
         #end

         <br>

         $mybean.string

         <br>

        #foreach($item in $mybean.array)
            $item <br>
        #end

       request : $mybeanreq.string

    </vel:velocity>
</body>
</html>
