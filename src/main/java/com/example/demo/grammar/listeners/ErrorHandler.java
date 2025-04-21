package com.example.demo.grammar.listeners;

import java.util.List;
import com.example.demo.grammar.antlr.JavaBaseListener;
import com.example.demo.grammar.antlr.JavaParser;
import com.example.demo.grammar.symboltable.SymbolTable;


public class ErrorHandler extends JavaBaseListener {
    private final SymbolTable symbolTable;
    private final List<String> errors;

    public ErrorHandler(SymbolTable symbolTable, List<String> errors) {
        this.symbolTable = symbolTable;
        this.errors = errors;
    }

    @Override
    public void exitVarDecl(JavaParser.VarDeclContext ctx) {
        if (ctx.expr() != null) {
            String declaredType = ctx.type().getText();
            String exprType = inferType(ctx.expr());
            if (!typeCompatible(declaredType, exprType)) {
                errors.add(String.format(
                    "Line %d: Type mismatch - cannot assign %s to %s",
                    ctx.start.getLine(),
                    exprType,
                    declaredType
                ));
            }
        }
    }

    @Override
    public void exitAssignmentExpression(JavaParser.AssignmentExpressionContext ctx) {
    	com.example.demo.grammar.symboltable.SymbolTable.Symbol symbol = symbolTable.resolve(ctx.ID().getText());

        if (symbol == null) {
            errors.add(String.format(
                "Line %d: Undefined variable '%s'",
                ctx.start.getLine(),
                ctx.ID().getText()
            ));
        }    
       }
    private String inferType(JavaParser.ExprContext expr) {
        if (expr == null) {
            return "void";
        }

        // Handle primary expressions
        if (expr instanceof JavaParser.PrimaryExpressionContext) {
            JavaParser.PrimaryContext primary = ((JavaParser.PrimaryExpressionContext)expr).primary();
            
        
        // Handle literals
        if (primary instanceof JavaParser.LiteralExpressionContext) {
            JavaParser.LiteralContext literal = ((JavaParser.LiteralExpressionContext)primary).literal();
            return inferTypeFromLiteral(literal);
        }
        
        
        // Handle variable references
        if (primary instanceof JavaParser.VariableReferenceContext) {
            String varName = ((JavaParser.VariableReferenceContext)primary).ID().getText();
            SymbolTable.Symbol symbol = symbolTable.resolve(varName);
            return symbol != null ? symbol.getType() : "unknown";
        }
        
     // Handle parentheses
        if (primary instanceof JavaParser.ParenthesizedExpressionContext) {
            return inferType(((JavaParser.ParenthesizedExpressionContext)primary).expr());
        }
        
        return inferTypeFromPrimary(primary);
        }
        
        if (isBinaryOperation(expr)) {
            return inferBinaryOperationType(expr);
        
        }
        
        // Handle logical operations
        if (expr instanceof JavaParser.LogicalAndOperationContext ||
            expr instanceof JavaParser.LogicalOrOperationContext) {
            return "boolean";
        }
        
        // Handle assignment
        if (expr instanceof JavaParser.AssignmentExpressionContext) {
            return inferType(((JavaParser.AssignmentExpressionContext)expr).expr());
        }
        
        
        
        // Default fallback
        return "unknown";
    }
    
    

    private String inferTypeFromPrimary(JavaParser.PrimaryContext primary) {
        if (primary instanceof JavaParser.LiteralExpressionContext) {
            return inferTypeFromLiteral(((JavaParser.LiteralExpressionContext)primary).literal());
        }
        if (primary instanceof JavaParser.VariableReferenceContext) {
            String varName = ((JavaParser.VariableReferenceContext)primary).ID().getText();
            SymbolTable.Symbol symbol = symbolTable.resolve(varName);
            return symbol != null ? symbol.getType() : "unknown";
        }
        if (primary instanceof JavaParser.ParenthesizedExpressionContext) {
            return inferType(((JavaParser.ParenthesizedExpressionContext)primary).expr());
        }
        return "unknown";
    }

    private String inferTypeFromLiteral(JavaParser.LiteralContext literal) {
        if (literal instanceof JavaParser.IntegerLiteralContext) {
            return "int";
        }
        if (literal instanceof JavaParser.FloatLiteralContext) {
            return "float";
        }
        if (literal instanceof JavaParser.BooleanLiteralContext) {
            return "boolean";
        }
        if (literal instanceof JavaParser.StringLiteralContext) {
            return "String";
        }
        if (literal instanceof JavaParser.NullLiteralContext) {
            return "null";
        }
        return "unknown";
    }

 // CHANGE: New helper method to check if expression is a binary operation
    private boolean isBinaryOperation(JavaParser.ExprContext expr) {
        return expr instanceof JavaParser.AdditionSubtractionContext || 
               expr instanceof JavaParser.MultiplicationDivisionContext ||
               expr instanceof JavaParser.ComparisonOperationContext ||
               expr instanceof JavaParser.EqualityOperationContext;
    }
    
    
 // CHANGE: Completely rewritten binary operation handling
    private String inferBinaryOperationType(JavaParser.ExprContext expr) {
        String leftType = inferType(getLeftOperand(expr));
        String rightType = inferType(getRightOperand(expr));
        int operatorTokenType = getOperatorTokenType(expr);
        
        if (isArithmeticOp(operatorTokenType)) {
            if (!isNumericType(leftType) || !isNumericType(rightType)) {
                errors.add("Arithmetic operation requires numeric operands");
                return "unknown";
            }
            return handleArithmeticOperation(leftType, rightType);
        }
        
        if (isComparisonOp(operatorTokenType)) {
            return "boolean";
        }
        
        return "unknown";
    }
        
 // CHANGE: New helper methods for binary operation handling
    private JavaParser.ExprContext getLeftOperand(JavaParser.ExprContext expr) {
        if (expr instanceof JavaParser.AdditionSubtractionContext) {
            return ((JavaParser.AdditionSubtractionContext)expr).expr(0);
        }
        if (expr instanceof JavaParser.MultiplicationDivisionContext) {
            return ((JavaParser.MultiplicationDivisionContext)expr).expr(0);
        }
        if (expr instanceof JavaParser.ComparisonOperationContext) {
            return ((JavaParser.ComparisonOperationContext)expr).expr(0);
        }
        if (expr instanceof JavaParser.EqualityOperationContext) {
            return ((JavaParser.EqualityOperationContext)expr).expr(0);
        }
        return null;
    }

    private JavaParser.ExprContext getRightOperand(JavaParser.ExprContext expr) {
        if (expr instanceof JavaParser.AdditionSubtractionContext) {
            return ((JavaParser.AdditionSubtractionContext)expr).expr(1);
        }
        if (expr instanceof JavaParser.MultiplicationDivisionContext) {
            return ((JavaParser.MultiplicationDivisionContext)expr).expr(1);
        }
        if (expr instanceof JavaParser.ComparisonOperationContext) {
            return ((JavaParser.ComparisonOperationContext)expr).expr(1);
        }
        if (expr instanceof JavaParser.EqualityOperationContext) {
            return ((JavaParser.EqualityOperationContext)expr).expr(1);
        }
        return null;
    }

    private int getOperatorTokenType(JavaParser.ExprContext expr) {
        if (expr instanceof JavaParser.AdditionSubtractionContext) {
            return ((JavaParser.AdditionSubtractionContext)expr).op.getType();
        }
        if (expr instanceof JavaParser.MultiplicationDivisionContext) {
            return ((JavaParser.MultiplicationDivisionContext)expr).op.getType();
        }
        if (expr instanceof JavaParser.ComparisonOperationContext) {
            return ((JavaParser.ComparisonOperationContext)expr).op.getType();
        }
        if (expr instanceof JavaParser.EqualityOperationContext) {
            return ((JavaParser.EqualityOperationContext)expr).op.getType();
        }
        return -1;
    }

    private String handleArithmeticOperation(String leftType, String rightType) {
        // Handle string concatenation
        if (leftType.equals("String") || rightType.equals("String")) {
            return "String";
        }
        
        // Numeric type promotion
        if (isNumericType(leftType) && isNumericType(rightType)) {
            return getWiderType(leftType, rightType);
        }
        
        return "unknown";
    }

    
    private boolean typeCompatible(String target, String source) {
        if (target.equals(source)) {
            return true;
        }
        
        // Handle primitive numeric conversions
        if (isNumericType(target) && isNumericType(source)) {
            return canConvertNumeric(source, target);
        }
        
        // Handle null assignment to objects
        if (target.endsWith("[]") || (!isPrimitiveType(target) && !target.equals("void"))) {
            return source.equals("null");
        }
        
        // Handle inheritance (simple case - in real implementation you'd need class hierarchy)
        if (target.equals("Object") && !isPrimitiveType(source)) {
            return true;
        }
        
        return false;
    }

    // Helper methods
    private boolean isArithmeticOp(int tokenType) {
        return tokenType == JavaParser.T__24 ||  // '+'
               tokenType == JavaParser.T__25 ||  // '-'
               tokenType == JavaParser.T__28 ||  // '*'
               tokenType == JavaParser.T__29 ||  // '/'
               tokenType == JavaParser.T__30;    // '%'
    }

    private boolean isComparisonOp(int tokenType) {
        return tokenType == JavaParser.T__31 ||  // '<'
               tokenType == JavaParser.T__32 ||  // '>'
               tokenType == JavaParser.T__33 ||  // '<='
               tokenType == JavaParser.T__34 ||  // '>='
               tokenType == JavaParser.T__35 ||  // '=='
               tokenType == JavaParser.T__36;    // '!='
    }

    
    
    

    private boolean isNumericType(String type) {
        return type.equals("byte") || type.equals("short") || 
               type.equals("int") || type.equals("long") ||
               type.equals("float") || type.equals("double");
    }

    private boolean isPrimitiveType(String type) {
        return type.equals("boolean") || isNumericType(type);
    }

    private boolean canConvertNumeric(String from, String to) {
        // Define implicit conversion hierarchy
        String[] numericHierarchy = {"byte", "short", "int", "long", "float", "double"};
        int fromIndex = -1, toIndex = -1;
        
        for (int i = 0; i < numericHierarchy.length; i++) {
            if (numericHierarchy[i].equals(from)) fromIndex = i;
            if (numericHierarchy[i].equals(to)) toIndex = i;
        }
        
        return fromIndex >= 0 && toIndex >= 0 && fromIndex <= toIndex;
    }

    private String getWiderType(String type1, String type2) {
        if (!isNumericType(type1) || !isNumericType(type2)) {
            return "unknown";
        }
        
        String[] hierarchy = {"byte", "short", "int", "long", "float", "double"};
        int index1 = -1, index2 = -1;
        
        for (int i = 0; i < hierarchy.length; i++) {
            if (hierarchy[i].equals(type1)) index1 = i;
            if (hierarchy[i].equals(type2)) index2 = i;
        }
        
        return index1 > index2 ? type1 : type2;
    }
    
}