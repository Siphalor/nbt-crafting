parser grammar DollarParser;

options {
  tokenVocab = DollarLexer;
}
@header {
	package de.siphalor.nbtcrafting.dollar.antlr;
}

number
  : INTEGER_LITERAL
  | FLOAT_LITERAL
  ;
literal
  : EMPTY_STRING
  | STRING_LITERAL
  | EMPTY_CHAR
  | CHAR_LITERAL
  ;
identifier: ID
  ;
comparison_operator
  : op=LESS_EQUAL
  | op=LESS_THEN
  | op=GREATER_EQUAL
  | op=GREATER_THEN
  | op=EQUAL
  | op=NOT_EQUAL
  ;
nesting: LPAREN expr RPAREN
  ;
function_args
  : LPAREN expr (COMMA expr)* RPAREN
  ;
function_call
  : target=identifier (EMPTY_EXP | function_args)
  ;
constant
  : literal
  | number
  | identifier
  ;
expr
  : expr op=DOT identifier
  | expr op=LBRACK expr RBRACK
  | expr op=HASH TYPE_INDICATOR
  | function_call
  | op=MINUS expr
  | op=PLUS expr
  | op=BANG expr
  | expr op=DIVIDE expr
  | expr op=MULTIPLY expr
  | expr op=MINUS expr
  | expr op=PLUS expr
  | expr comparison_operator expr
  | expr LOG_AND expr
  | expr LOG_OR expr
  | nesting
  |<assoc=right> expr op=QUEST expr COLON expr
  | constant
  ;
