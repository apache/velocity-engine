IntelliJ-Live-Template.xml is a set of templates for 
the IntelliJ IDEA IDE (http://www.intellij.com)

Contributed by Rickard Öberg <rickard@xpedio.com>, he wrote :

I've written a bunch of Velocity Live Templates for the IntelliJ IDE.
They're simply the directives but which expands to the full syntax.

E.g. "#foreach" followed by space expands into:
#foreach ($| in |)
#end
where | are markers for entering parameters.

They make template editing much faster and avoids silly errors such as
forgetting #end.



