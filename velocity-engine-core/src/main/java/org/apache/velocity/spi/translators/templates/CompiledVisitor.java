package org.apache.velocity.spi.translators.templates;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.api.Resource;
import org.apache.velocity.api.Template;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.ASTAddNode;
import org.apache.velocity.runtime.parser.node.ASTAndNode;
import org.apache.velocity.runtime.parser.node.ASTAssignment;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.ASTComment;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTDirectiveAssign;
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
import org.apache.velocity.runtime.parser.node.ASTIncludeStatement;
import org.apache.velocity.runtime.parser.node.ASTIndex;
import org.apache.velocity.runtime.parser.node.ASTIntegerLiteral;
import org.apache.velocity.runtime.parser.node.ASTIntegerRange;
import org.apache.velocity.runtime.parser.node.ASTLENode;
import org.apache.velocity.runtime.parser.node.ASTLTNode;
import org.apache.velocity.runtime.parser.node.ASTMap;
import org.apache.velocity.runtime.parser.node.ASTMethod;
import org.apache.velocity.runtime.parser.node.ASTModNode;
import org.apache.velocity.runtime.parser.node.ASTMulNode;
import org.apache.velocity.runtime.parser.node.ASTNENode;
import org.apache.velocity.runtime.parser.node.ASTNegateNode;
import org.apache.velocity.runtime.parser.node.ASTNotNode;
import org.apache.velocity.runtime.parser.node.ASTObjectArray;
import org.apache.velocity.runtime.parser.node.ASTOrNode;
import org.apache.velocity.runtime.parser.node.ASTParameters;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ASTSetDirective;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;
import org.apache.velocity.runtime.parser.node.ASTSubtractNode;
import org.apache.velocity.runtime.parser.node.ASTText;
import org.apache.velocity.runtime.parser.node.ASTTextblock;
import org.apache.velocity.runtime.parser.node.ASTTrue;
import org.apache.velocity.runtime.parser.node.ASTVariable;
import org.apache.velocity.runtime.parser.node.ASTWord;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.NodeUtils;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompiledVisitor extends BaseVisitor {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final StringBuilder codeBuilder = new StringBuilder(1024 * 8);

  private String[] staticImportPackages = new String[]{"org.apache.velocity.spi.methods.LangMethod"};
  private String[] importPackages;
  private int ident = 2;

  @Override
  public Object visit(ASTAssignment node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTComment node, Object data) {
    return "";
  }

  @Override
  public Object visit(ASTDirective node, Object data) {
    // TODO: 2021/11/23
    return "";
  }

  @Override
  public Object visit(ASTDirectiveAssign node, Object data) {
    return null;
  }

  @Override
  public Object visit(ASTSetDirective node, Object data) {
    // https://velocity.apache.org/engine/1.7/user-guide.html#set
    // lhs:
    //  1. a variable reference
    //  2. a property reference
    // rhs:
    //  1. Variable reference
    //  2. String literal
    //  3. Property reference
    //  4. Method reference
    //  5. Number literal
    //  6. ArrayList
    //  7. Map
    //  #set( $monkey = $bill )                                         ## variable reference
    //  #set( $monkey.Friend = "monica" )                               ## string literal
    //  #set( $monkey.Blame = $hello.Leak )                             ## property reference
    //  #set( $monkey.Plan = $hello.weave($web) )                       ## method reference
    //  #set( $monkey.Number = 123 )                                    ## number literal
    //  #set( $monkey.Say = ["Not", $my, "fault"] )                     ## ArrayList
    //  #set( $monkey.Map = {"banana" : "good", "roast beef" : "bad"})  ## Map

    String key = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    Object val = dispatch(node.jjtGetChild(1), (Resource) data);
    return "$context.put(\"" + key + "\"," + val + ");";
  }

  @Override
  public String visit(ASTExpression node, Object data) {
    return (String) dispatch(node.jjtGetChild(0), (Resource) data);
  }

  @Override
  public String visit(ASTReference node, Object data) {
    // There are three types of references in the VTL: variables, properties and methods.
    // https://velocity.apache.org/engine/1.7/user-guide.html#references
    // 1. Variables
    // 2. Properties
    // 3. Methods
    return null;
  }

  @Override
  public Object visit(ASTBlock node, Object data) {
    int k = node.jjtGetNumChildren();
    Resource resource = (Resource) data;
    StringBuilder block = new StringBuilder();
    for (int i = 0; i < k; i++) {
      block.append(dispatch(node.jjtGetChild(i), resource)).append("\r\n");
    }
    return block.toString().trim();
  }

  // region if/elseif/else

  @Override
  public Object visit(ASTIfStatement node, Object data) {
    /*
     *  if () {         // child:0
     *    xx            // child:1
     *  } else if () {  // child:2
     *    yy            //
     *  } else {
     *    zz            // child:3
     *  }
     */
    int k = node.jjtGetNumChildren();
    Resource resource = (Resource) data;
    String cond = (String) dispatch(node.jjtGetChild(0), resource);
    String then = (String) dispatch(node.jjtGetChild(1), resource);
    StringBuilder ifStmt = new StringBuilder();
    ifStmt.append(append(ident, "if (")).append(cond).append(") {\r\n");
    ifStmt.append(append(ident + 2, then)).append("\r\n");
    ifStmt.append(append(ident, "}"));
    if (k > 2) {
      // else if
      for (int i = 2; i < k; i++) {
        ifStmt.append(dispatch(node.jjtGetChild(i), resource));
      }
    }
    return ifStmt.toString();
  }

  @Override
  public Object visit(ASTElseIfStatement node, Object data) {
    String cond = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String then = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return append(1, "else if (") + cond + ") {\n"
        + append(ident + 2, then) + "\r\n"
        + append(ident, "}");
  }

  @Override
  public Object visit(ASTElseStatement node, Object data) {
    String then = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    return append(1, "else {\r\n")
        + append(ident + 2, then) + "\r\n"
        + append(ident, "}");
  }

  // endregion if/elseif/else

  // region binaryOperator

  // region comparison

  @Override
  public String visit(ASTNENode node, Object data) {
    Object lhs = dispatch(node.jjtGetChild(0), (Resource) data);
    Object rhs = dispatch(node.jjtGetChild(1), (Resource) data);
    return "!java.util.Objects.equals(" + lhs + ", " + rhs + ")";
  }

  @Override
  public String visit(ASTEQNode node, Object data) {
    Object lhs = dispatch(node.jjtGetChild(0), (Resource) data);
    Object rhs = dispatch(node.jjtGetChild(1), (Resource) data);
    return "java.util.Objects.equals(" + lhs + ", " + rhs + ")";
  }

  @Override
  public String visit(ASTGENode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " >= " + rhs;
  }

  @Override
  public String visit(ASTGTNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " > " + rhs;
  }

  @Override
  public String visit(ASTLENode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " <= " + rhs;
  }

  @Override
  public String visit(ASTLTNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " < " + rhs;
  }

  // endregion comparison

  // region logical

  @Override
  public Object visit(ASTAndNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " && " + rhs;
  }

  @Override
  public Object visit(ASTOrNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " || " + rhs;
  }

  @Override
  public Object visit(ASTNotNode node, Object data) {
    String mhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    return "!" + mhs;
  }

  // endregion logical

  // region math

  @Override
  public Object visit(ASTAddNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " + " + rhs;
  }

  @Override
  public Object visit(ASTDivNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " / " + rhs;
  }

  @Override
  public Object visit(ASTModNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " % " + rhs;
  }

  @Override
  public Object visit(ASTMulNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " * " + rhs;
  }

  @Override
  public Object visit(ASTSubtractNode node, Object data) {
    String lhs = (String) dispatch(node.jjtGetChild(0), (Resource) data);
    String rhs = (String) dispatch(node.jjtGetChild(1), (Resource) data);
    return lhs + " - " + rhs;
  }

  // endregion math

  // endregion binaryOperator

  // region literal

  @Override
  public String visit(ASTStringLiteral node, Object data) {
    return "\"" + node.getFirstToken().image.substring(1, node.getFirstToken().image.length() - 1) + "\"";
  }

  @Override
  public BigInteger visit(ASTIntegerLiteral node, Object data) {
    return new BigInteger(node.getFirstToken().image);
  }

  @Override
  public BigDecimal visit(ASTFloatingPointLiteral node, Object data) {
    return new BigDecimal(node.getFirstToken().image);
  }

  // endregion literal

  @Override
  public String visit(ASTText node, Object data) {
    Token t = node.getFirstToken();
    List<String> text = new ArrayList<>();
    for (; t != node.getLastToken(); t = t.next) {
      text.add(StringUtils.trim(NodeUtils.tokenLiteral(node.getParser(), t)));
    }
    text.add(StringUtils.trim(NodeUtils.tokenLiteral(node.getParser(), t)));
    return text.stream()
        .filter(StringUtils::isNotBlank)
        .map(s -> "    $output.write(\"" + s + "\");")
        .collect(Collectors.joining("\r\n"));
  }

  /**
   * <pre>
   * {@code
   *    package xxx;
   *
   *    import yyy.y.y.y;
   *    import zzz.z.z.z;
   *
   *    public final class Template_$name_$locale_$encoding extends Template {
   *
   *      private final VelocityEngine engine;
   *
   *      @Override
   *      public boolean render(Context $context, Writer $writer) throws Exception {
   *        writer.write(xx)
   *        return true;
   *      }
   *    }
   *
   * }
   * </pre>
   *
   * @return Template
   */
  @Override
  public String visit(SimpleNode node, Object data) {
    int i, k = node.jjtGetNumChildren();
    Resource resource = (Resource) data;
    StringBuilder sourceCode = new StringBuilder();
    StringBuilder methodCode = new StringBuilder();

    for (i = 0; i < k; i++) {
      methodCode.append(dispatch(node.jjtGetChild(i), resource)).append("\r\n");
    }

    String pkgName = CompiledTemplate.class.getPackage().getName();
    String clsName = getTemplateClassName(resource);

    methodCode.append(this.codeBuilder);
    sourceCode.append("package ").append(pkgName).append(";\r\n\r\n");
    Arrays.stream(staticImportPackages).map(s -> "import static " + s + ".*;\r\n").forEach(sourceCode::append);
    sourceCode.append("\r\n");
    sourceCode.append("public final class ").append(clsName).append(" extends CompiledTemplate {\r\n\r\n");
    sourceCode.append("  @Override\r\n");
    sourceCode.append("  protected void doRender(Context $context, Writer $output) throws Exception {\r\n");
    sourceCode.append("    ").append("").append("\r\n");
    sourceCode.append("  }\r\n");
    sourceCode.append("}\r\n");
    String clazz = sourceCode.toString();
    logger.debug(clazz);
    return clazz;
  }

  private Object dispatch(Node node, Resource resource) {
    if (node instanceof ASTAssignment) {
      return visit((ASTAssignment) node, resource);
    }
    // region BinaryOperator Comparison
    else if (node instanceof ASTNENode) {
      return visit((ASTNENode) node, resource);
    } else if (node instanceof ASTEQNode) {
      return visit((ASTEQNode) node, resource);
    } else if (node instanceof ASTGENode) {
      return visit((ASTGENode) node, resource);
    } else if (node instanceof ASTGTNode) {
      return visit((ASTGTNode) node, resource);
    } else if (node instanceof ASTLENode) {
      return visit((ASTLENode) node, resource);
    } else if (node instanceof ASTLTNode) {
      return visit((ASTLTNode) node, resource);
    }
    // endregion BinaryOperator Comparison

    // region BinaryOperator Logical
    else if (node instanceof ASTAndNode) {
      return visit((ASTAndNode) node, resource);
    } else if (node instanceof ASTOrNode) {
      return visit((ASTOrNode) node, resource);
    }
    // endregion BinaryOperator Logical

    // region BinaryOperator Math
    else if (node instanceof ASTAddNode) {
      return visit((ASTAddNode) node, resource);
    } else if (node instanceof ASTDivNode) {
      return visit((ASTDivNode) node, resource);
    } else if (node instanceof ASTModNode) {
      return visit((ASTModNode) node, resource);
    } else if (node instanceof ASTMulNode) {
      return visit((ASTMulNode) node, resource);
    } else if (node instanceof ASTSubtractNode) {
      return visit((ASTSubtractNode) node, resource);
    }
    // endregion BinaryOperator Math

    else if (node instanceof ASTBlock) {
      return visit((ASTBlock) node, resource);
    } else if (node instanceof ASTComment) {
      return visit((ASTComment) node, resource);
    } else if (node instanceof ASTDirective) {
      return visit((ASTDirective) node, resource);
    } else if (node instanceof ASTDirectiveAssign) {
      return visit((ASTDirectiveAssign) node, resource);
    } else if (node instanceof ASTElseIfStatement) {
      return visit((ASTElseIfStatement) node, resource);
    } else if (node instanceof ASTElseStatement) {
      return visit((ASTElseStatement) node, resource);
    } else if (node instanceof ASTEscape) {
      return visit((ASTEscape) node, resource);
    } else if (node instanceof ASTEscapedDirective) {
      return visit((ASTEscapedDirective) node, resource);
    } else if (node instanceof ASTExpression) {
      return visit((ASTExpression) node, resource);
    } else if (node instanceof ASTFalse) {
      return visit((ASTFalse) node, resource);
    } else if (node instanceof ASTFloatingPointLiteral) {
      return visit((ASTFloatingPointLiteral) node, resource);
    } else if (node instanceof ASTIntegerLiteral) {
      return visit((ASTIntegerLiteral) node, resource);
    } else if (node instanceof ASTStringLiteral) {
      return visit((ASTStringLiteral) node, resource);
    } else if (node instanceof ASTIdentifier) {
      return visit((ASTIdentifier) node, resource);
    } else if (node instanceof ASTIfStatement) {
      return visit((ASTIfStatement) node, resource);
    } else if (node instanceof ASTIncludeStatement) {
      return visit((ASTIncludeStatement) node, resource);
    } else if (node instanceof ASTIndex) {
      return visit((ASTIndex) node, resource);
    } else if (node instanceof ASTIntegerRange) {
      return visit((ASTIntegerRange) node, resource);
    } else if (node instanceof ASTMap) {
      return visit((ASTMap) node, resource);
    } else if (node instanceof ASTMethod) {
      return visit((ASTMethod) node, resource);
    } else if (node instanceof ASTNegateNode) {
      return visit((ASTNegateNode) node, resource);
    } else if (node instanceof ASTNotNode) {
      return visit((ASTNotNode) node, resource);
    } else if (node instanceof ASTObjectArray) {
      return visit((ASTObjectArray) node, resource);
    } else if (node instanceof ASTParameters) {
      return visit((ASTParameters) node, resource);
    } else if (node instanceof ASTprocess) {
      return visit((ASTprocess) node, resource);
    } else if (node instanceof ASTReference) {
      return visit((ASTReference) node, resource);
    } else if (node instanceof ASTSetDirective) {
      return visit((ASTSetDirective) node, resource);
    } else if (node instanceof ASTText) {
      return visit((ASTText) node, resource);
    } else if (node instanceof ASTTextblock) {
      return visit((ASTTextblock) node, resource);
    } else if (node instanceof ASTTrue) {
      return visit((ASTTrue) node, resource);
    } else if (node instanceof ASTVariable) {
      return visit((ASTVariable) node, resource);
    } else if (node instanceof ASTWord) {
      return visit((ASTWord) node, resource);
    } else {
      throw new IllegalArgumentException("unknown node:" + node);
    }
  }

  private String getTemplateClassName(Resource resource) {
    // Template_name_locale_encoding
    StringBuilder name = new StringBuilder();
    name.append(Template.class.getSimpleName());
    name.append("_");
    name.append(resource.getName());
    if (resource.getLocale() != null) {
      name.append("_");
      name.append(resource.getLocale());
    }
    if (StringUtils.isNotBlank(resource.getEncoding())) {
      name.append("_");
      name.append(resource.getEncoding());
    }
    return name.toString().replaceAll("[-.]", "_");
  }

  private String append(int ident, String s) {
    return StringUtils.repeat(' ', ident) + s;
  }
}
