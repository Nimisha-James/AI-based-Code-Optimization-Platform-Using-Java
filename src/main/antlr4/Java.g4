grammar Java;

@lexer::members {
    private JavaParser parser;
    
    public void setParser(JavaParser parser) {
        this.parser = parser;
    }
    
    private void reportLexerError(String msg) {
        // Option 1: Use _factory (recommended)
        Token token = _factory.create(_token.getType(), _token.getText());
        ((CommonToken)token).setLine(getLine());
        ((CommonToken)token).setCharPositionInLine(getCharPositionInLine());

        // Option 2: Wrap _token directly (alternative)
        // CommonToken token = new CommonToken(_token);
        // token.setLine(getLine());
        // token.setCharPositionInLine(getCharPositionInLine());

        if (parser != null) {
            parser.reportParserError("Lexer " + msg, (CommonToken)token);
        }
        skip();
    }
}

@parser::members {
    public List<String> errors = new ArrayList<>();
    private SymbolTable symbolTable = new SymbolTable();
    
    

    // For parser-originated errors
    public void reportParserError(String msg, Token token) {
        errors.add(String.format("Line %d:%d - %s (Token: '%s')",
            token.getLine(),
            token.getCharPositionInLine(),
            msg,
            token.getText()));
    }
    
    // For lexer-originated errors (accepts pre-formatted)
    public void reportError(String msg) {
        errors.add(msg);
    }
    
    
}

// Parser Rules
compilationUnit: (classDecl | functionDecl | statement)* EOF;

// CLASSES
classDecl: 
     (PUBLIC | PRIVATE | PROTECTED)? 'class' ID 
    {
        if ($ID.text.equals("System") || $ID.text.equals("String")) {
            reportParserError("Reserved class name", $ID);
        } else {
            symbolTable.addClass($ID.text);
        }
    }
    '{' (memberDecl)* '}';

memberDecl: 
    varDecl ';' | functionDecl;

// FUNCTIONS
functionDecl: 
    (PUBLIC | PRIVATE | PROTECTED)? (STATIC)? type ID '(' (parameter (',' parameter)*)? ')' 

    {
        symbolTable.enterScope();
        symbolTable.addFunction($ID.text, $type.ctx);
    }
    '{' statement* '}' 
    {
        symbolTable.exitScope();
    };

parameter: type ID { symbolTable.addVariable($ID.text, $type.ctx); };

type: 
    ('int' | 'float' | 'boolean' | 'void' | 'String' | ID) ('[' ']')*;

// STATEMENTS (all remain unchanged)
statement:
    varDecl ';'                          # VarDeclaration
    | expr ';'                           # ExpressionStatement
    | ifStmt                             # IfStatement
    | forStmt                            # ForStatement
    | whileStmt                          # WhileStatement
    | returnStmt ';'                     # ReturnStatement
    | block                              # BlockStatement
    | ';'                                # EmptyStatement
    ;

ifStmt: 'if' '(' expr ')' statement ('else' statement)?;
forStmt: 'for' '(' (varDecl | expr)? ';' expr? ';' expr? ')' statement;
whileStmt: 'while' '(' expr ')' statement;
returnStmt: 'return' expr?;
varDecl: type ID ('=' expr { 
    Symbol varSymbol = new Symbol($type.ctx.getText(), false, $type.ctx.getText().contains("["), $ID.getLine());
    Symbol exprSymbol = symbolTable.resolve($expr.ctx.getText());

    symbolTable.addVariable($ID.text, $type.ctx);

    if (exprSymbol != null) {
        if (varSymbol.isArray && !exprSymbol.isArray) {
            reportParserError("Type mismatch - cannot assign " + exprSymbol.type + " to " + varSymbol.type, $ID);
        } else {
            symbolTable.validateType(varSymbol, exprSymbol);
        }
    }
})?;


block: '{' statement* '}';



// EXPRESSIONS (all remain unchanged)
expr:
    primary                               # PrimaryExpression
    | expr '[' expr ']'                   # ArrayAccessExpression
    | 'new' type '[' expr ']'             # ArrayCreationExpression
    | 'new' ID '(' ')'                    # ObjectCreationExpression
    | expr '.' ID                          # MemberAccessExpression
    | expr '(' (expr (',' expr)*)? ')'     # FunctionCallExpression
    | op=('++'|'--') expr                 # PrefixOperation
    | expr '[' expr ']' '=' expr 	#Arrayelementassignment
    | expr op=('++'|'--')                 # PostfixOperation
    | op=('+'|'-'|'!'|'~') expr           # UnaryOperation
    | expr op=('*'|'/'|'%') expr          # MultiplicationDivision
    | expr op=('+'|'-') expr              # AdditionSubtraction
    | expr op=('<'|'>'|'<='|'>=') expr    # ComparisonOperation
    | expr op=('=='|'!=') expr            # EqualityOperation
    | expr '&&' expr                      # LogicalAndOperation
    | expr '||' expr                      # LogicalOrOperation
    | ID '=' expr  # AssignmentExpression
    ;

primary:
    '(' expr ')'                          # ParenthesizedExpression
    | literal                             # LiteralExpression
    | ID                                  # VariableReference
    ;

literal:
    INT                                   # IntegerLiteral
    | FLOAT                               # FloatLiteral
    | BOOLEAN                             # BooleanLiteral
    | STRING                              # StringLiteral
    | 'null'                              # NullLiteral
    ;

// LEXER RULES

// ** Change 4: Added proper keyword tokens to prevent merging issues **
PUBLIC : 'public';
STATIC : 'static';
VOID : 'void';
PROTECTED: 'protected';
PRIVATE : 'private';

// Add other keywords here...

// ** Change 5: Fixed identifier rule to avoid merging issues like 'publicstaticvoid' **
fragment LETTER : [a-zA-Z_];
fragment DIGIT  : [0-9];

ID: LETTER (LETTER | DIGIT)*;


INT: [0-9]+;
FLOAT: [0-9]+ '.' [0-9]+;
BOOLEAN: 'true' | 'false';
STRING: '"' .*? '"';
WS: [ \t\r\n]+ -> skip;
COMMENT: '/*' .*? '*/' -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;

// ERROR TOKENS
UNEXPECTED_CHAR: . 
    { 
        reportLexerError("illegal character");
        skip(); 
    };