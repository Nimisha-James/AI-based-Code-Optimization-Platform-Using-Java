package com.example.demo.grammar.listeners;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import com.example.demo.grammar.antlr.JavaLexer;

public class SyntaxErrorListener extends BaseErrorListener {
    public static final SyntaxErrorListener INSTANCE = new SyntaxErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                          Object offendingSymbol,
                          int line,
                          int charPositionInLine,
                          String msg,
                          RecognitionException e) {
        
        String errorType = "Syntax Error";
        Token token = (Token) offendingSymbol;

        // Classify error types
        if (msg.contains("missing")) {
            errorType = "Missing Token";
        } else if (msg.contains("extraneous")) {
            errorType = "Unexpected Token";
        } else if (msg.contains("no viable alternative")) {
            errorType = "Invalid Construct";
        }

        String errorDetail = String.format(
            "[%s] Line %d:%d - %s\n" +
            "Token: '%s' (%s)",
            errorType,
            line,
            charPositionInLine,
            msg.replace("input", "code"),
            token.getText(),
            JavaLexer.VOCABULARY.getSymbolicName(token.getType())
        );

        throw new ParseCancellationException(errorDetail);
    }
}