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
literal
  : EMPTY_STRING
  | STRING_LITERAL
  | EMPTY_CHAR
  | CHAR_LITERAL
  ;
identifier: ID
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
  : expr EOF
  ;
