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
