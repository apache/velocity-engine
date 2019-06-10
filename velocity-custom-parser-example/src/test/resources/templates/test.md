# Test markdown template for the custom parser

@* this is a comment *@

@set ($subtitle = 'Custom parser is needed')

## $subtitle

some $some @@ should print 'some value'

@if ($some == 'value')
  all seems fine
@else
  there is a problem
@end

@macro(block $foo)
  block macro called with foo=$foo and bodyContent=$bodyContent
@end

@%block($some)
here
@end
