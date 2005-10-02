TemplateTool

This is the start of a little tool for debug-ish / validation-ish
template activities.

Currently, it is able to return a list of the references used 
in a template, as well as a frequency map of those references.

To test, simply compile (need Velocity jar, of course), and then

  $ java TemplateTool  <template>

where <template> is your template name in the current directory,
such as 'mytemplate.vm'

It will print a list of all references used in the order they 
are used, and also the frequency in which they are used...

Currently, different method invocations on the same reference
are treated as different :

  $foo.bar()
  $foo.baz()

are different, there is one enhancement that can be done...

-geir


