package com.example.demo.grammar.symboltable;

public class Symbol {
    private final String type;
    private final boolean isFunction;
    public final boolean isArray;
    private final int declarationLine;

    public Symbol(String type, boolean isFunction, boolean isArray, int declarationLine) {
        this.type = type;
        this.isFunction = isFunction;
        this.isArray = isArray;
        this.declarationLine = declarationLine;
    }

    // Getters
    public String getType() { return type; }
    public boolean isFunction() { return isFunction; }
    public boolean isArray() { return isArray; }
    public int getDeclarationLine() { return declarationLine; }

    @Override
    public String toString() {
        return (isFunction ? "Function" : "Variable") + 
               " [type=" + type + 
               ", array=" + isArray + 
               ", declaredAt=" + declarationLine + "]";
    }
}