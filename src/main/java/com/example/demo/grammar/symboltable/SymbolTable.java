package com.example.demo.grammar.symboltable;

import java.util.*;
import org.antlr.v4.runtime.ParserRuleContext;

public class SymbolTable {
    private final Stack<Map<String, Symbol>> scopes = new Stack<>();
    private final Map<String, ClassSymbol> classes = new HashMap<>();
    private final Map<String, FunctionSymbol> functions = new HashMap<>();
    private final Map<String, VariableSymbol> variables = new HashMap<>();

    public static class Symbol {
        public final String type;
        public final boolean isFunction;
        public final boolean isArray;
        public final int declarationLine;

        public Symbol(String type, boolean isFunction, boolean isArray, int line) {
            this.type = type;
            this.isFunction = isFunction;
            this.isArray = isArray;
            this.declarationLine = line;
        }
        public String getType() {
        	return type;
        }
    }

    public static class ClassSymbol {
        private final Map<String, Symbol> members = new HashMap<>();

        public void addMember(String name, Symbol symbol) {
            members.put(name, symbol);
        }

        public Symbol getMember(String name) {
            return members.get(name);
        }
        
    }

    public static class FunctionSymbol extends Symbol {
        public final List<Parameter> parameters;
        public final ParserRuleContext context;

        public FunctionSymbol(String returnType, ParserRuleContext ctx) {
            super(returnType, true, false, ctx.start.getLine());
            this.parameters = new ArrayList<>();
            this.context = ctx;
        }
    }

    public static class VariableSymbol extends Symbol {
        public final ParserRuleContext context;

        public VariableSymbol(String type,boolean isArray, ParserRuleContext ctx) {
            super(type, false, isArray, ctx.start.getLine());
            this.context = ctx;
        }
    }

    public static class Parameter {
        public final String type;
        public final String name;

        public Parameter(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    public SymbolTable() {
        enterScope(); // Global scope
        initializeBuiltins();
    }

    private void initializeBuiltins() {
        // Add System.out.println()
        ClassSymbol systemClass = new ClassSymbol();
        systemClass.addMember("out", new Symbol("PrintStream", false, false, -1));
        classes.put("System", systemClass);

        // Add String class
        classes.put("String", new ClassSymbol());
    }

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        scopes.pop();
    }

    // Add a class to the symbol table
    public void addClass(String className) {
        if (classes.containsKey(className)) {
            throw new RuntimeException("Duplicate class name: " + className);
        }
        classes.put(className, new ClassSymbol());
    }

    // Add a function to the symbol table
    public void addFunction(String functionName, ParserRuleContext typeContext) {
        String returnType = typeContext.getText();
        if (functions.containsKey(functionName)) {
            throw new RuntimeException("Duplicate function name: " + functionName);
        }
        FunctionSymbol func = new FunctionSymbol(returnType, typeContext);
        functions.put(functionName, func);
        scopes.peek().put(functionName, func);
    }

    // Add a variable to the symbol table
    public void addVariable(String varName, ParserRuleContext typeContext) {
        String type = typeContext.getText();
        boolean isArray = type.contains("[]") || type.equals("int[]") || type.equals("float[]"); // Ensure arrays are detected
        
        if (scopes.peek().containsKey(varName)) {
            throw new RuntimeException("Duplicate variable name: " + varName);
        }
        VariableSymbol var = new VariableSymbol(type, isArray, typeContext);
        variables.put(varName, var);
        scopes.peek().put(varName, var);
        Symbol resolvedExpr = resolve(type);
        if (resolvedExpr != null) {
            if (var.isArray && !resolvedExpr.isArray) {
                reportTypeError(var, resolvedExpr); 
            } else {
                validateType(var, resolvedExpr);
            } 
        }
    }

    public Symbol resolve(String name) {
    	
    	// If name is an array allocation like "new int[5]", return a matching Symbol
        if (name.matches("new\\s+int\\s*\\[.*\\]")) {
            return new Symbol("int[]", false, true, -1);
        } else if (name.matches("new\\s+float\\s*\\[.*\\]")) {
            return new Symbol("float[]", false, true, -1);
        }
    	
        // Check current scope first
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return scopes.get(i).get(name);
            }
        }

        // Check class members
        for (ClassSymbol cls : classes.values()) {
            Symbol member = cls.getMember(name);
            if (member != null) return member;
        }

        return null; // Not found
    }

    public ClassSymbol getClass(String className) {
        return classes.get(className);
    }

    public FunctionSymbol getFunction(String functionName) {
        return functions.get(functionName);
    }

    public VariableSymbol getVariable(String varName) {
        return variables.get(varName);
    }
    
    public void validateType(Symbol expected, Symbol actual) {  
        if (expected == null || actual == null) {  
            return; // Skip validation if any type is missing  
        }  

        // Check if both are arrays, and compare their base types  
         
            if (!expected.type.equals(actual.type)) {  
                reportTypeError(expected, actual);  
                return;
            }  
          
        // If expected is an array but actual is not, it's a mismatch  
        else if (expected.isArray && !actual.isArray) {  
            reportTypeError(expected, actual);  
        }  
    }
    private void reportTypeError(Symbol expected, Symbol actual) {  
        System.err.println("Type mismatch - cannot assign " + actual.type + " to " + expected.type);  
    }
}