package org.apache.velocity.runtime.parser.node;

import org.apache.velocity.runtime.directive.Directive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to fix indentation in structured mode.
 */

public class IndentationFixer implements ParserVisitor
{
    protected String parentIndentation = null;
    protected String extraIndentation = null;
    protected Pattern fix = null;

    protected void fillExtraIndentation(String prefix)
    {
        Pattern captureExtraIndentation = Pattern.compile("^" + parentIndentation + "(\\s+)");
        Matcher matcher = captureExtraIndentation.matcher(prefix);
        if (matcher.find())
        {
            extraIndentation = matcher.group(1);
            fix = Pattern.compile("^" + parentIndentation + extraIndentation, Pattern.MULTILINE);
        }
        else
        {
            extraIndentation = "";
        }
    }

    public IndentationFixer(String parentIndentation)
    {
        this.parentIndentation = parentIndentation;
    }

    @Override
    public Object visit(SimpleNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTprocess node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTText node, Object data)
    {
        String text = node.getCtext();
        if (extraIndentation == null)
        {
            fillExtraIndentation(text);
        }
        if (extraIndentation.length() > 0)
        {
            Matcher matcher = fix.matcher(text);
            node.setCtext(matcher.replaceAll(parentIndentation));
        }
        return null;
    }

    @Override
    public Object visit(ASTEscapedDirective node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTEscape node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTComment node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTTextblock node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTFloatingPointLiteral node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTIntegerLiteral node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTStringLiteral node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTWord node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTDirectiveAssign node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTDirective node, Object data)
    {
        String prefix = node.getPrefix();
        if (prefix.length() > 0)
        {
            if (extraIndentation == null)
            {
                fillExtraIndentation(prefix);
            }
            if (extraIndentation.length() > 0)
            {
                Matcher matcher = fix.matcher(prefix);
                node.setPrefix(matcher.replaceAll(parentIndentation));
                if (node.getDirectiveType() == Directive.BLOCK)
                {
                    node.childrenAccept(this, null);
                }
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data)
    {
        String prefix = node.getPrefix();
        if (prefix.length() > 0)
        {
            node.childrenAccept(this, null);
        }
        return null;
    }

    @Override
    public Object visit(ASTMap node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTObjectArray node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTIntegerRange node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTMethod node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTIndex node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTReference node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTTrue node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTFalse node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTIfStatement node, Object data)
    {
        String prefix = node.getPrefix();
        if (prefix.length() > 0)
        {
            if (extraIndentation == null)
            {
                fillExtraIndentation(prefix);
            }
            if (extraIndentation.length() > 0)
            {
                Matcher matcher = fix.matcher(prefix);
                node.setPrefix(matcher.replaceAll(parentIndentation));
                node.childrenAccept(this, null);
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTElseStatement node, Object data)
    {
        if (extraIndentation != null && extraIndentation.length() > 0)
        {
            node.childrenAccept(this, null);
        }
        return null;
    }

    @Override
    public Object visit(ASTElseIfStatement node, Object data)
    {
        if (extraIndentation != null && extraIndentation.length() > 0)
        {
            node.childrenAccept(this, null);
        }
        return null;
    }

    @Override
    public Object visit(ASTSetDirective node, Object data)
    {
        String prefix = node.getPrefix();
        if (prefix.length() > 0)
        {
            if (extraIndentation == null)
            {
                fillExtraIndentation(prefix);
            }
            if (extraIndentation.length() > 0)
            {
                Matcher matcher = fix.matcher(prefix);
                node.setPrefix(matcher.replaceAll(parentIndentation));
            }
        }
        return null;
    }

    @Override
    public Object visit(ASTExpression node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTAssignment node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTOrNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTAndNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTEQNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTNENode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTLTNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTGTNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTLENode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTGENode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTAddNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTSubtractNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTMulNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTDivNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTModNode node, Object data)
    {
        return null;
    }

    @Override
    public Object visit(ASTNotNode node, Object data)
    {
        return null;
    }
}
