dword INTERPRET,9,, ; ( address length -- )
    .lit $20
    .wp PEEK
    .wp TOR
    .lit $22
    .wp PEEK
    .wp TOR

    .wp OVER
    .lit $20
    .wp POKE ; source address is at $20

    .wp ADD
    .lit $22
    .wp POKE ; end address is at $22

INTERPRET_loop:

    .wp WORD
    .wp SHOULDSKIP
    .zbranch INTERPRET_end

    .wp TWODUP ; w-a w-l w-a w-l

    .wp FIND ; w-a w-l addr
    .wp DUP ; w-a w-l addr addr
    .zbranch INTERPRET_wordnotfound

    .wp DUP ; w-a w-l addr addr
    .wp ISCOMPONLY ; w-a w-l addr cond
    .wp STATE
    .wp PEEK ; w-a w-l addr cond state
    .wp ZEQU
    .wp AND ; w-a w-l addr cond
    .wp INVERT
    .zbranch INTERPRET_throwic

    .wp NIP ; w-a addr
    .wp NIP ; addr

    .wp DUP
    .wp ISIMMEDIATE
    .wp STATE
    .wp PEEK
    .wp ZEQU
    .wp OR
    .zbranch INTERPRET_cword

    .wp TCFA ; addr
    .wp EXECUTE

    .branch INTERPRET_loop

INTERPRET_wordnotfound:

    .wp DROP ; w-a w-l
    .wp TWODUP ; w-a w-l w-a w-l
    .wp NUMBER ; w-a w-l result upcc
    .wp ZEQU ; w-a w-l result cond
    .zbranch INTERPRET_notanumber ; w-a w-l result

    .wp NIP ; w-a result
    .wp NIP ; result

    .wp STATE
    .wp PEEK
    .zbranch INTERPRET_loop
    .branch INTERPRET_compilenum

INTERPRET_notanumber:
    .wp DROP
    .lit INTERPRET_texta
    .lit 15
    .wp TYPE
    .wp TYPE
    .wp ABORT

INTERPRET_end:
    .wp TWODROP

    .wp DEPTH
    .wp ZERO
    .wp LT
    .zbranch INTERPRET_checka
    .lit INTERPRET_textb
    .lit 11
    .wp TYPE
    .wp ABORT

INTERPRET_checka:

    .wp DEPTH
    .lit 127
    .wp GT
    .zbranch INTERPRET_checkb
    .lit INTERPRET_textc
    .lit 14
    .wp TYPE
    .wp ABORT

INTERPRET_checkb: ; normal exit
    .wp FROMR
    .lit $22
    .wp POKE
    .wp FROMR
    .lit $20
    .wp POKE
.wp EXIT

INTERPRET_cword:
    .wp TCFA
    .wp COMMA
    .branch INTERPRET_loop

INTERPRET_compilenum:
    .comp LIT
    .wp COMMA
    .branch INTERPRET_loop

INTERPRET_throwic:
    .wp DROP
    .lit INTERPRET_textd
    .lit 32
    .wp TYPE
    .wp TYPE
    .wp ABORT

dword (GCL),5,F_HIDDEN,GCL
    .lit $20
    .wp PEEKINCR
.wp EXIT

dword WORD,4,, ; ( -- word-address word-length )
    .wp BL
    .wp PARSE
.wp EXIT

dword PARSE,5,, ; ( delimiter-char -- word-address word-length )
    .lit $28
    .wp POKEBYTE
WORD_loop_a:
    .wp GCL
    .wp DUP
    .wp PEEKBYTE
    .wp BL
    .wp EQU
    .zbranch WORD_noblank
    .wp DROP
    .branch WORD_loop_a
WORD_noblank:

    .wp ZERO

WORD_loop_b:

    .wp TWODUP
    .wp ADD
    .lit $22
    .wp PEEK
    .wp GE
    .zbranch WORD_continue
    .wp EXIT
WORD_continue:

    .wp INCR

    .wp GCL
    .wp PEEKBYTE
    .lit $28
    .wp PEEKBYTE
    .wp EQU
    .zbranch WORD_loop_b
.wp EXIT

dword KEY>DIGIT,9,,KEYTODIGIT ; ( char(a) -- a )
    .lit $FF
    .wp AND
    .lit $30
    .wp SUB
    .wp DUP
    .lit $10
    .wp GT
    .zbranch KD_nooffset
    .lit 7
    .wp SUB
KD_nooffset:
.wp EXIT

dword DIGIT>KEY,9,,DIGITTOKEY ; ( a -- char(a) )
    .wp DUP
    .lit 9
    .wp GT
    .zbranch DK_nooffset
    .lit 7
    .wp ADD
DK_nooffset:
    .lit $30
    .wp ADD
.wp EXIT

dword NUMBER,6,, ; ( word-address word-length -- result upcc )
    .wp ZERO
    .lit $26
    .wp POKE ; set result to 0

    .wp OVER
    .lit $24
    .wp POKE

    .wp DUP ; w-a w-l w-l
    .zbranch NUMBER_zerolength

    .wp BASE
    .wp PEEK ; w-a w-l base

    .wp ROT ; w-l base w-a
    .wp DUP ; w-l base w-a w-a
    .wp PEEKBYTE ; w-l base w-a first-char
    .lit $2d
    .wp EQU

    .wp DUP ; w-l base w-a cond cond
    .zbranch NUMBER_notneg
    .wp GDL
    .wp DROP ; skip char from parsing
    .wp TWOTOR
    .wp TOR
    .wp DECR
    .wp FROMR
    .wp TWOFROMR
NUMBER_notneg:

    .wp TOR ; save whether the number should be negative for the end
            ; w-l base w-a

    .wp ROT ; base w-a w-l

    .lit 0 ; base w-a w-l 0

NUMBER_loop:
    .wp TOR ; base w-a w-l
    .wp TOR ; base w-a

    .wp SWAP ; w-a base
    .lit $26
    .wp PEEK ; w-a base value
    .wp OVER ; w-a base value base
    .wp UMUL ; w-a base newv
    .wp OVER ; w-a base newv base

    .wp GDL
    .wp PEEK ; w-a base newv base char
    .wp KEYTODIGIT ; w-a base newv base digit
    .wp DUP ; w-a base newv base digit digit
    .wp ZGE
    .zbranch NUMBER_invalidchara ; w-a base newv base digit
    .wp DUP ; w-a base newv base digit digit
    .wp NROT ; w-a base newv digit base digit
    .wp GT
    .zbranch NUMBER_invalidcharb ; w-a base newv digit

    .wp ADD ; w-a base newv+digit
    .lit $26
    .wp POKE ; w-a base
    .wp SWAP ; base w-a

    .wp FROMR ; base w-a w-l
    .wp FROMR ; base w-a w-l i
    .wp INCR ; base w-a w-l i+1
    .wp TWODUP ; base w-a w-l i+1 w-l i+1
    .wp EQU ; base w-a w-l i+1 cond
    .zbranch NUMBER_loop ; base w-a w-l i+1
    .wp TWODROP ; base w-a
    .wp TWODROP ;

    .lit $26
    .wp PEEK ; value
    .wp FROMR ; value cond
    .zbranch NUMBER_notneg_b ; value
    .wp NEGATE ; -value
NUMBER_notneg_b:
    .lit 0 ; value 0

.wp EXIT

NUMBER_zerolength:
    .wp TWODROP
    .wp ZERO
    .wp ZERO
.wp EXIT

NUMBER_invalidchara:
    .wp NIP
NUMBER_invalidcharb:
    .wp TWODROP ; w-a base
    .wp TWODROP ;
    .wp FROMR ; w-l
    .wp FROMR ; w-l i
    .wp RDROP
    .wp SUB ; r-c
    .lit $26
    .wp PEEK ; r-c result
    .wp SWAP ; result r-c
.wp EXIT

dword (GDL),5,F_HIDDEN,GDL
    .lit $24
    .wp PEEKINCR
.wp EXIT

dword CHAR,4,,
    .wp WORD
    .wp DROP
    .wp PEEKBYTE
.wp EXIT

dword (?SKIP),7,F_HIDDEN,SHOULDSKIP
    .wp DUP
    .zbranch SKIP_yes

    .wp OVER
    .lit $22
    .wp PEEK
    .wp LT
    .zbranch SKIP_yes
    .branch SKIP_no
SKIP_yes:
    .wp FALSE
    .wp EXIT
SKIP_no:
    .wp TRUE
.wp EXIT