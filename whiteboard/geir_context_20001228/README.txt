Here is my impl of all the context stuff we have been 
talking about for a while.

New approach :

The code now reflects a new approach to dealing with the whole context issue.  
The big issue was that we have an internal 'context' that is used for a few 
purposes (and more in the future), and the first rev or so of this code showed
that.  This latest rev hides completely the internal context, allows us to add
more kinds of 'stuff' to the internal context in the future.  It places no 
requirements upon users, and app level code is unable to access the internal context
methods.

The big difference is that Context is now in it's own package.  VelocityContext is 
still at org.apache.velocity level, but all the guts are safe in their own package.

Note that this code won't drop in and run as it requires 
lots of non-functional mods to a few AST nodes to decouple
 the internal context stuff from the app-level context stuff,
which is a Good Thing.  There are also changes to 
everything from VelocityServlet to Anakia to Texen etc to
use the replacement for Context, namely VelocityContext.

If we agree that this is ok I will check the whole pile in
with whatever mods we want.  If not, I'll chuck it!

The important changes from the current way things are :

1) Fedor's wrapping/chaining context stuff is supported.

2) Jason's quest for a tool manager in Velocity is supported. 
This supports the idea of a global tool manager (specify tools 
in the vel.props file) for ease of use along with the ability 
for the app to not use the globals and just specify their own 
through a separate props file or a Properties.  Note that
there is just support for a tool manager, not one in here.
Figure jason has one already....

3) Everyone's desire for a flexible way to make new kinds of 
contexts that do strange and different things, like LDAP, 
a DB, a bunny rabbit, whatever.

4) Geir's desire that less major machinery is put into the 
Vel runtime core if it can be avoided.

Included
---------

Context.java :  interface defintion for app-level data context. This is 
what the application writer thinks of the context to be.  The app
writer has several choices for using this :

1) Use the provided VelocityContext() implementation, providing the current
facilities of the current Context class. It looks exactly like 
the current Context, for the most part, except that put() optionally
returns the Object it replaces.

2) Create their own class to use as a context by extending AbstractContext.
This would automatically support context chaining/wrapping, tool support if
we decide to do that, and fully caches node-specific cache info across merges.
This would be the most efficient and easy way to make a Context-compatible 
object.  It could be stored in a servlet session and reused with any applicable
cache information preserved. The included HashMapContext, TreeMapContext, 
and DBContext are examples of how this is done.

3) Create their own class to use as a context by simply implementing the
Context interface.  They would be responsible for supporting chaining if desired.
This seems to be more appropriate for specialized applications, frameworks, 
whatever.  The disadvange to this is that node-specific cache info is lost
after use.  This is fine for codeing patterns where the context is created, used
and destroyed. (Our current VelocityServlet does this...)

AbstractContext.java : abstract class for a usable app-level Context.
Handles the wrapping/chaining support as well as interfacing with the
possibly upcoming tool management. To make a usable Context object, a user
could simply subclass this and implement the storage mechanism and the 5 required 
methods.

VelocityContext.java : replaces the current Context.java implementation.
This is a working hashtable-based concrete implementation of 
AbstractContext.java.   You would use it wherever you use Context now.
(and indeed, it is used in the included jar).  The name isn't important...
we can also change to hashmap or whatever.  The point is to have a default
concrete context for people to use easily.

InternalContextBase.java : internal cache object, used for node-specific caching of 
introspection information, carrying temlpate names, etc.  Will need to extend
or add another in the future, I bet.  The functionality is inaccessable to any
app-layer code.

InternalContextAdapter.java : used by Template to carry all context and internalContext
information down into the AST.  Decouples the app-level context from the 'context'
expected by the AST internals.  Further, provides the InternalContext support for 
app-level Contexts that don't subclass from AbstractContext, so performance is 
preserved.  And further, we can add more kinds of information to be carried into 
the AST free of app-level code changes.  Used by Template.java as well as all AST nodes.
Fundamentally, all AST nodes would no longer care about the 'Context' interface, but expect
InternalContextAdapter to support it.

HashMapContext.java : example of creating another kind of Context, it uses
a HashMap for storage.

TreeMapContext.java : another example of creating a new kind of Context, it uses
a TreeMap for storage.

DBContext.jata : another [silly] example of a Context implementation that
serializes objects to and from a database (MySQL in my case...) Flaky, but works.

MultiContextTest.java : the Test program but now uses the three above contexts
together, wrapped/chained in a chain to demonstrate that it indeed works.

velocity-0.71.jar : working jar to work with the above test program. 
(remember, there are some small  nonfunctional tweaks to everything to get 
this to work.  I will check all in if we decide this is the way to go...)
You can also use this against the testbed - chaining is used within the 
TemplateTestCase.

Notes
-----
12/30/00
--------
1) It's snowing!
2) Jason had a great point - that the internalContext stuff should
be completely hidden from app level if possible - needed to remove 
the getInternalContext() accessor method from the Context interface and
the AbstractContext abstract base class.  So that's done.

12/30/00 pm
-----------
not done enough....  See above notes and new code if you care...

geir


