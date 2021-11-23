package org.apache.velocity.api;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import org.apache.velocity.context.Context;

public interface Template extends Resource, Node {

  void render(Context context, Writer out) throws IOException, ParseException;

  Object evaluate(Context context) throws ParseException;
}
