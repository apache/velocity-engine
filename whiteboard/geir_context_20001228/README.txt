Here is my impl of all the context stuff we have been 
talking about for a while.

Note that this code won't drop in and run as it requires 
small non-functional mods to a few AST nodes to decouple
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
Context.java : basic interface defintion.  It looks exactly like 
the current Context, for the most part, except that put optionally
returns the Object it replaces, and the InternalContext crap is 
gotten as an object by the nodes rather than have the methods 
implemented by the Context.

AbstractContext.java : abstract class for a usable app-level Context.
Handles the wrapping/chaining support as well as interfacing with the
maybe upcoming 'toolsmith', the global context tool manager/dispenser.

VelocityContext.java : replaces the current Context.java implementation.
This is a working hashtable-based concrete implementation of 
AbstractContext.java.   You would use it wherever you use Context now.
(and indeed, it is used in the included jar)

HashMapContext.java : example of creating another kind of Context, it uses
a HashMap for storage.

TreeMapContext.java : another example of creating a new kind of Context, it uses
a TreeMap for storage.

[yes, the two above aren't that interesting.  But show how one goes about
doing it. I'll try for a DBContext later when I have more time]

MultiContextTest.java : the Test program but now uses the three above contexts
together, wrapped/chained in a chain to demonstrate that it indeed works.

velocity-0.71.jar : working jar to work with the above test program. 
(remember, there are some small  nonfunctional tweaks to everything to get 
this to work.  I will check all in if we decide this is the way to go...)
You can also use this against the testbed - chaining is used within the 
TemplateTestCase.


geir


