package org.apache.velocity.runtime.parser;

public interface ParserTokenManager
{
    void clearStateVars();
    void switchTo(int lexState);
    int getCurrentLexicalState();
    boolean stateStackPop();
    boolean stateStackPush();
    public Token getNextToken();
    void setDebugStream(java.io.PrintStream ds);
    boolean isInSet();
    void setInSet(boolean value);
    void ReInit(CharStream charStream);
}
