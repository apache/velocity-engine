This is an implementation of a #local() directive.

Use :

  #local($arg $arg1...)


  #end

and the values for $arg etc will be 'protected' - the will be the same
after the block as before.  Currently this is limited to references $<foo>

