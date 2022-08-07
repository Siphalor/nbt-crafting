lexer grammar DollarLexer;

@header {
	package de.siphalor.nbtcrafting.dollar.antlr;
}

WHITESPACE: [\p{Zs}]+
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
QUEST: '?'
  ;
COMMA: ','
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
BANG: EXCL
  ;
LESS_THEN: LT
  ;
GREATER_THEN: GT
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
EMPTY_EXP: LPAREN RPAREN
  ;
fragment ID_START: [a-z_]
  ;
fragment ID_CONTINUE: [a-z0-9_]
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
FLOAT_TYPE_INDICATOR: [fd]
  ;
INTEGER_TYPE_INDICATOR: [bcsil]
  ;
NUMBER_TYPE_INDICATOR:
  FLOAT_TYPE_INDICATOR | INTEGER_TYPE_INDICATOR
  ;
OTHER_INDICATOR: [asB]
  ;
TYPE_INDICATOR:
  FLOAT_TYPE_INDICATOR | INTEGER_TYPE_INDICATOR | OTHER_INDICATOR
  ;
INTEGER_LITERAL:
  POLARITY? LEADING_DIGIT DIGIT*
  ;
FLOAT_LITERAL:
  POLARITY? LEADING_DIGIT DIGIT* (DOT DIGIT+ SCIENT_EXP? | SCIENT_EXP)
  ;
