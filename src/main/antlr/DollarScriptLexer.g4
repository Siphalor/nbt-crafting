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

lexer grammar DollarScriptLexer;

@header {
	package de.siphalor.nbtcrafting3.dollar.antlr;
}

WHITESPACE: [\p{Zs}\n]+
  -> channel(HIDDEN)
  ;
HASH: '#'
  ;
COLON: ':'
  ;
LPAREN: '('
  ;
RPAREN: ')'
  ;
LBRACK: '['
  ;
RBRACK: ']'
  ;
LBRACE: '{'
  ;
RBRACE: '}'
  ;
QUEST: '?'
  ;
COMMA: ','
  ;
SEMICOLON: ';'
  ;
DOT: '.'
  ;
PLUS: '+'
  ;
MINUS: '-'
  ;
DIVIDE: '/'
  ;
MULTIPLY: '*'
  ;
fragment LT: '<'
  ;
fragment GT: '>'
  ;
fragment EQ: '='
  ;
fragment EXCL: '!'
  ;
ASSIGN: EQ
  ;
BANG: EXCL
  ;
LESS: LT
  ;
GREATER: GT
  ;
LESS_EQUAL: LT EQ
  ;
GREATER_EQUAL: GT EQ
  ;
EQUAL: EQ EQ
  ;
NOT_EQUAL: EXCL EQ
  ;
LOG_OR: '||'
  ;
LOG_AND: '&&'
  ;
COALESCING_DOT: QUEST DOT
  ;
ARROW: MINUS GT
  ;
fragment CHAR_ESCAPE: '\\\''
  ;
EMPTY_CHAR: '\'\''
  ;
CHAR_LITERAL:
 '\''
  ( ~[']
  | CHAR_ESCAPE
  )*
  '\''
  ;
fragment STRING_ESCAPE: '\\"'
  ;
EMPTY_STRING: '""'
  ;
STRING_LITERAL:
  '"'
  ( ~["]
  | STRING_ESCAPE
  )*
  '"'
  ;
TRUE:
  'true'
  ;
FALSE:
  'false'
  ;
NULL:
  'null'
  ;
fragment ID_START: [a-zA-Z_$]
  ;
fragment ID_CONTINUE: [a-zA-Z0-9_]
  ;
ID: ID_START ID_CONTINUE*
  ;
fragment POLARITY:
  PLUS | MINUS
  ;
fragment DIGIT: [0-9]
  ;
fragment LEADING_DIGIT: [1-9]
  ;
fragment SCIENT_EXP:
  [eE] POLARITY? DIGIT+
  ;
fragment FLOAT_TYPE_INDICATOR: [fFdD]
  ;
fragment INTEGER_TYPE_INDICATOR: [bBcsSiIlL]
  ;
fragment NUMBER_TYPE_INDICATOR:
  FLOAT_TYPE_INDICATOR | INTEGER_TYPE_INDICATOR
  ;
fragment OTHER_INDICATOR: [ao]
  ;
fragment TYPE_INDICATOR:
  FLOAT_TYPE_INDICATOR | INTEGER_TYPE_INDICATOR | OTHER_INDICATOR
  ;
HASH_CAST:
  HASH TYPE_INDICATOR
  ;
INTEGER_LITERAL:
  ('0' | LEADING_DIGIT DIGIT*) INTEGER_TYPE_INDICATOR?
  ;
FLOAT_LITERAL:
  ('0' | LEADING_DIGIT DIGIT*) (DOT DIGIT+ SCIENT_EXP? | SCIENT_EXP) FLOAT_TYPE_INDICATOR?
  ;
