package org.apache.velocity.runtime.parser;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import org.apache.velocity.runtime.parser.node.ASTAddNode;
import org.apache.velocity.runtime.parser.node.ASTAndNode;
import org.apache.velocity.runtime.parser.node.ASTAssignment;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.ASTComment;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTDivNode;
import org.apache.velocity.runtime.parser.node.ASTEQNode;
import org.apache.velocity.runtime.parser.node.ASTElseIfStatement;
import org.apache.velocity.runtime.parser.node.ASTElseStatement;
import org.apache.velocity.runtime.parser.node.ASTEscape;
import org.apache.velocity.runtime.parser.node.ASTEscapedDirective;
import org.apache.velocity.runtime.parser.node.ASTExpression;
import org.apache.velocity.runtime.parser.node.ASTFalse;
import org.apache.velocity.runtime.parser.node.ASTFloatingPointLiteral;
import org.apache.velocity.runtime.parser.node.ASTGENode;
import org.apache.velocity.runtime.parser.node.ASTGTNode;
import org.apache.velocity.runtime.parser.node.ASTIdentifier;
import org.apache.velocity.runtime.parser.node.ASTIfStatement;
import org.apache.velocity.runtime.parser.node.ASTIntegerLiteral;
import org.apache.velocity.runtime.parser.node.ASTIntegerRange;
import org.apache.velocity.runtime.parser.node.ASTLENode;
import org.apache.velocity.runtime.parser.node.ASTLTNode;
import org.apache.velocity.runtime.parser.node.ASTMap;
import org.apache.velocity.runtime.parser.node.ASTMethod;
import org.apache.velocity.runtime.parser.node.ASTModNode;
import org.apache.velocity.runtime.parser.node.ASTMulNode;
import org.apache.velocity.runtime.parser.node.ASTNENode;
import org.apache.velocity.runtime.parser.node.ASTNotNode;
import org.apache.velocity.runtime.parser.node.ASTObjectArray;
import org.apache.velocity.runtime.parser.node.ASTOrNode;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ASTSetDirective;
import org.apache.velocity.runtime.parser.node.ASTStop;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;
import org.apache.velocity.runtime.parser.node.ASTSubtractNode;
import org.apache.velocity.runtime.parser.node.ASTText;
import org.apache.velocity.runtime.parser.node.ASTTrue;
import org.apache.velocity.runtime.parser.node.ASTWord;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 *
 */
public interface ParserVisitor
{
  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(SimpleNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTprocess node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTEscapedDirective node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTEscape node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTComment node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTFloatingPointLiteral node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTIntegerLiteral node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTStringLiteral node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTIdentifier node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTWord node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTDirective node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTBlock node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTMap node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTObjectArray node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTIntegerRange node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTMethod node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTReference node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTTrue node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTFalse node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTText node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTIfStatement node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTElseStatement node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTElseIfStatement node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTSetDirective node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTStop node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTExpression node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTAssignment node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTOrNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTAndNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTEQNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTNENode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTLTNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTGTNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTLENode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTGENode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTAddNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTSubtractNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTMulNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTDivNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTModNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
  public Object visit(ASTNotNode node, Object data);

  /**
   * @param node
   * @param data
   * @return The object rendered by this node.
   */
}
