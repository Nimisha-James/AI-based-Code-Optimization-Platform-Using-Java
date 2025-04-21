package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Service;

import com.example.demo.grammar.antlr.JavaLexer;
import com.example.demo.grammar.antlr.JavaParser;
import com.example.demo.grammar.symboltable.SymbolTable;
import com.example.demo.grammar.listeners.ErrorHandler;

@Service
public class CodeParser {
    
    public List<String> parse(String code) {
        List<String> errors = new ArrayList<>();
        
        // 1. LEXER PHASE
        JavaLexer lexer = new JavaLexer(CharStreams.fromString(code));
        lexer.removeErrorListeners(); // Remove default listeners
        lexer.addErrorListener(new LexerErrorListener(errors)); // Add our listener
        
        // 2. TOKEN STREAM
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // 3. PARSER PHASE
        JavaParser parser = new JavaParser(tokens);
        parser.removeErrorListeners(); // Remove default listeners
        parser.addErrorListener(new ParserErrorListener(errors)); // Add our listener
        
        // 4. SYMBOL TABLE
        SymbolTable symbolTable = new SymbolTable();
        
        try {
            // 5. PARSE TREE CONSTRUCTION
            JavaParser.CompilationUnitContext tree = parser.compilationUnit();
            
            // 6. SEMANTIC ANALYSIS
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new ErrorHandler(symbolTable, errors), tree);
            
        } catch (Exception e) {
            errors.add("Parsing failed: " + e.getMessage());
        }
        
        return errors;
    }

    // Custom Lexer Error Listener
    private static class LexerErrorListener extends BaseErrorListener {
        private final List<String> errors;
        
        public LexerErrorListener(List<String> errors) {
            this.errors = errors;
        }
        
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                              int line, int charPositionInLine,
                              String msg, RecognitionException e) {
            errors.add(String.format("Lexical error at line %d:%d - %s", 
                                  line, charPositionInLine, msg));
        }
    }

    // Custom Parser Error Listener
    private static class ParserErrorListener extends BaseErrorListener {
        private final List<String> errors;
        
        public ParserErrorListener(List<String> errors) {
            this.errors = errors;
        }
        
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                              int line, int charPositionInLine,
                              String msg, RecognitionException e) {
            Token token = (Token)offendingSymbol;
            errors.add(String.format("Syntax error at line %d:%d - %s (near '%s')", 
                                  line, charPositionInLine, 
                                  cleanErrorMessage(msg), 
                                  token.getText()));
        }
        
        private String cleanErrorMessage(String msg) {
            return msg.replace("input", "code")
                     .replace("mismatched input", "unexpected token")
                     .replace("extraneous input", "unexpected token");
        }
    }
}