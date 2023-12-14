package org.apache.velocity.api;

import org.apache.velocity.runtime.visitor.BaseVisitor;

public interface Node {

  void accept(BaseVisitor visitor);
}
