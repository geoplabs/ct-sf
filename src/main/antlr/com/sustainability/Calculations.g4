grammar Calculations;

/**
 * Main program structure - accepts multiple expressions separated by newlines
 */
prog
    : SPACE* expr (NL SPACE* expr)* NL?
    ;

/**
 * Expression rules defining all possible operations and functions
 */
expr
    : // Basic arithmetic and comparison operations
      MINUS SPACE* expr                                                                # SignedExpr
    | left=expr SPACE* op=POW SPACE* right=expr                                        # PowerExpr
    | left=expr SPACE* op=( TIMES | DIV ) SPACE* right=expr                            # MulDivExpr
    | left=expr SPACE* op=( PLUS | MINUS ) SPACE* right=expr                           # AddSubExpr
    | left=expr SPACE* op=( GT | GTE | LT | LTE | DEQ | NEQ ) SPACE* right=expr        # PredicateExpr

    // General utility functions
    | AS_TIMESTAMP LPAREN value=expr COMMA pattern=expr (optionalAsTimestampParams)* RPAREN                 # AsTimestampFunctionExpr
    | GET_VALUE LPAREN json=expr COMMA query=expr RPAREN                                                    # GetValueFunctionExpr
    | COALESCE LPAREN exprList RPAREN                                                                       # CoalesceFunctionExpr
    | CONCAT LPAREN exprList RPAREN                                                                         # ConcatFunctionExpr
    | IF LPAREN predicate=expr COMMA true=expr COMMA false=expr RPAREN                                      # IfFunctionExpr
    | SEARCH LPAREN text=expr COMMA match=expr (optionalSearchParams)* RPAREN                               # SearchFunctionExpr
    | SWITCH LPAREN value=expr COMMA exprList (optionalSwitchParams)* RPAREN                                # SwitchFunctionExpr
    
    // String manipulation functions
    | CAML LPAREN value=expr RPAREN                                                                         # CamlFunctionExpr
    | LOWERCASE LPAREN value=expr RPAREN                                                                    # LowercaseFunctionExpr
    | UPPERCASE LPAREN value=expr RPAREN                                                                    # UppercaseFunctionExpr
    | SPLIT LPAREN text=expr COMMA regex=expr (optionalSplitParams)* RPAREN index=optionalArrayIndexParam?  # SplitFunctionExpr
    
    // Reference and variable functions
    | REF LPAREN columnName=expr RPAREN                                                                     # RefFunctionExpr
    | SET SPACE* name=TOKEN SPACE* EQ SPACE* value=expr                                                     # SetVariableExpr
    
    // Sustainability specific functions
    | IMPACT LPAREN activity=expr COMMA impact=expr COMMA component=expr (optionalImpactParams)* RPAREN     # ImpactFunctionExpr
    | CONVERT LPAREN value=expr COMMA fromUnit=expr COMMA toUnit=expr (optionalConvertParams)* RPAREN       # ConvertFunctionExpr
    | LOOKUP LPAREN value=expr COMMA name=expr COMMA keyColumn=expr COMMA outputColumn=expr 
        (optionalLookupParams)* RPAREN                                                                      # LookupFunctionExpr
    | ASSIGN_TO_GROUP LPAREN groupId=expr RPAREN                                                            # AssignToGroupFunctionExpr
    
    // Custom function support
    | function=CUSTOM_FUNCTION LPAREN exprList (optionalCustomParams)* RPAREN                               # CustomFunctionExpr
    
    // Basic atom expressions (literals, variables, etc)
    | atom                                                                                                  # AtomsExpr
    ;

/**
 * Optional parameter definitions for different functions
 */
// Impact function optional parameters
optionalImpactParams
    : (COMMA optionalCommonParam)
    ;

// Lookup function optional parameters
optionalLookupParams
    : (COMMA optionalCommonParam)
    ;

// Custom function optional parameters
optionalCustomParams
    : (COMMA optionalCommonParam)
    ;

// AS_TIMESTAMP function optional parameters
optionalAsTimestampParams
    : (COMMA timezone=optionalTimezoneParam)
    | (COMMA locale=optionalLocaleParam)
    | (COMMA roundDownTo=optionalRoundDownToParam)
    ;

// Convert function optional parameters
optionalConvertParams
    : (COMMA qualityKind=optionalQualityKindParam)
    ;

// Switch function optional parameters
optionalSwitchParams
    : (COMMA defaut=optionalDefaultParam)
    | (COMMA ignoreCase=optionalIgnoreCaseParam)
    ;

// Split function optional parameters
optionalSplitParams
    : (COMMA limit=optionalLimitParam)
    ;

// Common parameters that can be used across multiple functions
optionalCommonParam
    : group=optionalGroupParam
    | tenant=optionalTenantParam
    | version=optionalVersionParam
    | versionAsAt=optionalVersionAsAtParam
    ;

// Search function optional parameters
optionalSearchParams
    : (COMMA ignoreCase=optionalIgnoreCaseParam)
    ;

/**
 * Expression list for functions that take multiple parameters
 */
exprList
    : expr (COMMA expr)*
    ;

/**
 * Basic atoms (literals, variables, parenthesized expressions)
 */
atom
    : LPAREN expr RPAREN          # BracesAtom
    | NULL                        # Null
    | BOOLEAN                     # Boolean
    | SCIENTIFIC_NUMBER           # ScientificAtom
    | NUMBER                      # NumberAtom
    | TOKEN                       # TokenAtom
    | QUOTED_STRING               # QuotedStringAtom
    ;

/**
 * Optional parameter definitions
 */
optionalVersionParam
    : VERSION EQ expr
    ;

optionalTenantParam
    : TENANT EQ expr
    ;

optionalVersionAsAtParam
    : VERSIONASAT EQ expr
    ;

optionalGroupParam
    : GROUP EQ expr
    ;

optionalDefaultParam
    : DEFAULT EQ expr
    ;

optionalIgnoreCaseParam
    : IGNORECASE EQ expr
    ;

optionalQualityKindParam
    : QUALITYKIND EQ expr
    ;

optionalLimitParam
    : LIMIT EQ expr
    ;

optionalLocaleParam
    : LOCALE EQ expr
    ;

optionalTimezoneParam
    : TIMEZONE EQ expr
    ;

optionalRoundDownToParam
    : ROUNDDOWNTO EQ expr
    ;

optionalArrayIndexParam
    : LSQUARE expr RSQUARE
    ;

/**
 * Function name declarations
 */
AS_TIMESTAMP    : A S US T I M E S T A M P ;
ASSIGN_TO_GROUP : A S S I G N US T O US G R O U P ;
GET_VALUE       : G E T US V A L U E ;
COALESCE        : C O A L E S C E ;
CONCAT          : C O N C A T ;
CONVERT         : C O N V E R T ;
IF              : I F ;
IMPACT          : I M P A C T ;
LOOKUP          : L O O K U P ;
LOWERCASE       : L O W E R C A S E ;
REF             : R E F ;
CAML            : C A M L ;
SET             : S E T ;
SOURCE          : S O U R C E ;
SPLIT           : S P L I T ;
SWITCH          : S W I T C H ;
UPPERCASE       : U P P E R C A S E ;
SEARCH          : S E A R C H ;

/**
 * Parameter name declarations
 */
BOOLEAN  : T R U E | F A L S E ;
DEFAULT  : D E F A U L T ;
GROUP    : G R O U P ;
IGNORECASE : I G N O R E C A S E ;
LOCALE   : L O C A L E ;
NULL     : N U L L ;
QUALITYKIND : Q U A L I T Y K I N D ;
TENANT   : T E N A N T ;
TIMEZONE : T I M E Z O N E ;
VERSION  : V E R S I O N ;
VERSIONASAT : V E R S I O N A S A T;
ROUNDDOWNTO: R O U N D D O W N T O;
LIMIT: L I M I T;

/**
 * Custom function naming pattern 
 */
CUSTOM_FUNCTION : HASH VALID_CUSTOM_FUNCTION_START VALID_CUSTOM_FUNCTION_CHAR*;
fragment VALID_CUSTOM_FUNCTION_START : ([a-z]) | ([A-Z]) | US ;
fragment VALID_CUSTOM_FUNCTION_CHAR  : VALID_CUSTOM_FUNCTION_START | ([0-9]);

/**
 * Token pattern for variable references 
 */
TOKEN  : COLON VALID_TOKEN_START (VALID_TOKEN_CHAR* VALID_TOKEN_END)? ;
fragment VALID_TOKEN_START : (DIGIT | [a-z] | [A-Z] | US ) ;
fragment VALID_TOKEN_END  : VALID_TOKEN_START | COLON | HASH;
fragment VALID_TOKEN_CHAR  : VALID_TOKEN_END | SPACE;

/**
 * String literals
 */
QUOTED_STRING : SQ (~[\\'] | '\\' [\\'()])* SQ ;

/**
 * Numeric literals
 */
NUMBER: UNSIGNED_INTEGER ('.' (DIGIT) +)?;
SCIENTIFIC_NUMBER: NUMBER (E SIGN? UNSIGNED_INTEGER)?;

fragment SIGN: ('+' | '-');
fragment UNSIGNED_INTEGER: (DIGIT)+;
fragment DIGIT: [0-9];

/**
 * Case-insensitive letter fragments
 */
fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

/**
 * Operators and symbols
 */
LPAREN   : '(';
RPAREN   : ')';
PLUS     : '+';
MINUS    : '-';
TIMES    : '*';
DIV      : '/';
POW      : '^';

GT       : '>';
GTE      : '>=';
LT       : '<';
LTE      : '<=';
DEQ      : '==';
NEQ      : '!=';

EQ       : '=';
LSQUARE  : '[';
RSQUARE  : ']';

HASH     : '#';
SQ       : '\'';
COMMA    : SPACE* ',' SPACE* ;
POINT    : '.';
COLON    : ':';
US       : '_';
SPACE    : ' ';

/**
 * Newline handling
 */
NL
   : SPACE* '\n'
   | SPACE* '\r' '\n'?
   ;

/**
 * Whitespace skipping
 */
WS       : [ \t]+ -> skip ;

/**
 * Catch-all for any other character
 */
ANY      : . ; 