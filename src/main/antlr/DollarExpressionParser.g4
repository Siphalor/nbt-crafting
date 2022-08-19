/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

parser grammar DollarExpressionParser;

options {
  tokenVocab = DollarExpressionLexer;
}
@header {
	package de.siphalor.nbtcrafting.dollar.antlr;
}

number
  : INTEGER_LITERAL
  | FLOAT_LITERAL
  ;
stringLiteral
  : EMPTY_STRING
  | STRING_LITERAL
  | EMPTY_CHAR
  | CHAR_LITERAL
  ;
identifier: ID
  ;
namedConstant
  : TRUE
  | FALSE
  | NULL
  ;
nesting
  : LPAREN expr RPAREN
  | LPAREN assignmentExpr RPAREN
  ;
listConstruct
  : LBRACK RBRACK
  | LBRACK expr (COMMA expr)* COMMA? RBRACK
  ;
objectConstructProperty
  : identifier COLON expr
  | stringLiteral COLON expr
  ;
objectConstruct
  : LBRACE RBRACE
  | LBRACE objectConstructProperty (COMMA objectConstructProperty)* COMMA? RBRACE
  ;
lambda
  : LPAREN RPAREN ARROW inline=expr
  | identifier ARROW inline=expr
  | LPAREN identifier (COMMA identifier)* RPAREN ARROW inline=expr
  | LPAREN RPAREN ARROW LBRACE body=statementList RBRACE
  | identifier ARROW LBRACE body=statementList RBRACE
  | LPAREN identifier (COMMA identifier)* RPAREN ARROW LBRACE body=statementList RBRACE
  ;
functionCall
  : target=identifier (LPAREN RPAREN | LPAREN expr (COMMA expr)* RPAREN)
  ;
constant
  : stringLiteral
  | number
  | identifier
  | namedConstant
  | listConstruct
  | objectConstruct
  | lambda
  ;
lexpr
  : identifier
  | lexpr op=DOT identifier
  | lexpr op=LBRACK RBRACK
  | lexpr op=LBRACK expr RBRACK
  ;
assignmentExpr
  : lexpr ASSIGN expr
  ;
expr
  : expr op=DOT identifier
  | expr op=LBRACK expr RBRACK
  | expr op=HASH_CAST
  | functionCall
  | op=MINUS expr
  | op=PLUS expr
  | op=BANG expr
  | expr op=DIVIDE expr
  | expr op=MULTIPLY expr
  | expr op=MINUS expr
  | expr op=PLUS expr
  | expr op=EQUAL expr
  | expr op=NOT_EQUAL expr
  | expr op=LESS expr
  | expr op=LESS_EQUAL expr
  | expr op=GREATER expr
  | expr op=GREATER_EQUAL expr
  | expr op=LOG_AND expr
  | expr op=LOG_OR expr
  | nesting
  |<assoc=right> expr op=QUEST expr COLON expr
  | constant
  ;
statement
  : assignmentExpr
  | expr
  ;
statementList
  : statement (SEMICOLON statement)* SEMICOLON?
  ;
script
  : statementList EOF
  ;
