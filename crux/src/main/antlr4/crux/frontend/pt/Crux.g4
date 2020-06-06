grammar Crux;

program
 : declarationList EOF
 ;


statementBlock
 : Open_Brace statementList Close_Brace
 ;

statementList
 : statement*
 ;

statement
 : variableDeclaration
 | callStatement
 | assignmentStatement
 | ifStatement
 | whileStatement
 | returnStatement
 ;

returnStatement: Return expression0 SemiColon;
whileStatement: While expression0 statementBlock;
ifStatement: If expression0 statementBlock (Else statementBlock)*;
callStatement: callExpression SemiColon;
assignmentStatement: Let designator Assign expression0 SemiColon;


declarationList
 : declaration*
 ;

declaration
 : variableDeclaration
 | arrayDeclaration
 | functionDefinition
 ;

functionDefinition
 : Func Identifier Open_Paren parameterList Close_Paren Colon type statementBlock
 ;

arrayDeclaration
 : Array Identifier Colon type Open_Bracket Integer Close_Bracket SemiColon
 ;

variableDeclaration: Var Identifier Colon type SemiColon;


parameterList: | parameter (Comma parameter)*;
parameter: Identifier Colon type;


expressionList: | expression0 (Comma expression0)*;
callExpression: Call Identifier Open_Paren expressionList Close_Paren;

expression0: expression1 (op0 expression1)*;
expression1: expression2 (op1 expression2)*;
expression2: expression3 (op2 expression3)*;
expression3
 : Not expression3
 | Open_Paren expression0 Close_Paren
 | designator
 | callExpression
 | literal
 ;

op0: Greater_Equal | Lesser_Equal | Not_Equal | Equal | Greater_Than | Less_Than;
op1: Add | Sub | Or;
op2: Mul | Div | And;

type
 : Identifier
 ;

designator: Identifier (Open_Bracket expression0 Close_Bracket)*;

literal
 : Integer
 | True
 | False
 ;

Open_Paren: '(';
Close_Paren: ')';
Open_Brace: '{';
Close_Brace: '}';
Open_Bracket: '[';
Close_Bracket: ']';

Add: '+';
Sub: '-';
Mul: '*';
Div: '/';

Greater_Equal: '>=';
Lesser_Equal: '<=';
Not_Equal: '!=';
Equal: '==';
Greater_Than: '>';
Less_Than: '<';

Assign: '=';
Comma: ',';
SemiColon: ';';
Colon: ':';
Call: '::';

Integer
 : '0'
 | [1-9] [0-9]*
 ;

And: 'and';
Or: 'or';
Not: 'not';
Let: 'let';
Var: 'var';
Array: 'array';
Func: 'func';
If: 'if';
Else: 'else';
While: 'while';
True: 'true';
False: 'false';
Return: 'return';

Identifier
 : 'void'
 | 'bool'
 | 'int'
 | [a-zA-Z] [a-zA-Z0-9_]*
 ;

WhiteSpaces
 : [ \t\r\n]+ -> skip
 ;

Comment
 : '//' ~[\r\n]* -> skip
 ;

