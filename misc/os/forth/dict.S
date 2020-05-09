dvar STATE,5,,,
dvar DP,2,,,here_pos

dcode HERE,4,, ; ( -- dp@ )
    lda var_DP
    pha
nxt

dvar LATEST,6,,,name_marker

dword >CFA,4,,TCFA
    .lit 2
    .wp ADD
    .wp DUP
    .wp PEEK
    .lit F_LENMASK
    .wp AND
    .wp ADD
    .wp INCR
.wp EXIT

dword >DFA,4,,TDFA
    .wp TCFA
    .lit 3
    .wp ADD
.wp EXIT

dword FIND,4,, ; ( word-address word-length -- address )
    .wp LATEST
    .wp PEEK ; w-a w-l addr

FIND_loop:

    .wp DUP
    .zbranch FIND_notfound

    .wp DUP ; w-a w-l addr addr
    .wp INCRTWO
    .wp PEEKBYTE ; w-a w-l addr flags
    .lit F_LENMASK
    .lit F_HIDDEN
    .wp OR ; w-a w-l addr flags mask
    .wp AND ; w-a w-l addr length
    .wp ROT ; w-a addr length w-l
    .wp DUP ; w-a addr length w-l w-l
    .wp ROT ; w-a addr w-l w-l length
    .wp EQU
    .zbranch FIND_notequal ; w-a addr w-l

    .wp TOR ; w-a addr
    .wp TWODUP ; w-a addr w-a addr
    .lit 3
    .wp ADD
    .wp STRCMP ; w-a addr len
    .wp FROMR ; w-a addr len w-l
    .wp DUP ; w-a addr len w-l w-l
    .wp ROT ; w-a addr w-l w-l len
    .wp LE
    .zbranch FIND_notequal ; w-a addr w-l

    ; Found

    .wp DROP ; w-a addr
    .wp NIP ; addr
.wp EXIT

FIND_notequal:
    .wp SWAP
    .wp PEEK
    .branch FIND_loop
.wp EXIT

FIND_notfound:
    .wp DROP ; w-a w-l
    .wp TWODROP ;
    .wp ZERO
.wp EXIT

dword ('),3,,_HTICK
    .wp WORD
    .wp TWODUP
    .wp FIND
    .wp DUP
    .zbranch TICK_notfound
    .wp NIP
    .wp NIP
.wp EXIT

dword ',1,,_TICK
    .wp _HTICK
    .wp TCFA
.wp EXIT

TICK_notfound:
    .wp DROP
    .lit INTERPRET_texta
    .lit 15
    .wp TYPE
    .wp TYPE
    .wp ABORT
.wp EXIT

dcode EXECUTE,7,,
    rts

dword HIDE,4,,
    .wp _HTICK
    .wp INCRTWO
    .wp DUP
    .wp PEEKBYTE
    .lit F_HIDDEN
    .wp OR
    .wp SWAP
    .wp POKEBYTE
.wp EXIT

dword REVEAL,6,,
    .wp LATEST
    .wp PEEK
    .wp INCRTWO
    .wp DUP
    .wp PEEKBYTE
    .lit F_HIDDEN
    .wp INVERT
    .wp AND
    .wp SWAP
    .wp POKEBYTE
.wp EXIT

dword IMMEDIATE,9,F_IMMED,
    .wp LATEST
    .wp PEEK
    .wp INCRTWO
    .wp DUP
    .wp PEEKBYTE
    .lit F_IMMED
    .wp OR
    .wp SWAP
    .wp POKEBYTE
.wp EXIT

dword COMPILE-ONLY,12,F_IMMED,COMPILEONLY
    .wp LATEST
    .wp PEEK
    .wp INCRTWO
    .wp DUP
    .wp PEEKBYTE
    .lit F_COMPILEONLY
    .wp OR
    .wp SWAP
    .wp POKEBYTE
.wp EXIT

dword (HEADER),8,,IHEADER ; ( word-addr word-length -- )
    .lit F_LENMASK
    .wp AND
    .wp HERE ; w-a w-l here
    .wp LATEST
    .wp PEEK ; w-a w-l here latest
    .wp COMMA ; w-a w-l here
    .wp LATEST
    .wp POKE ; w-a w-l
    .wp DUP ; w-a w-l w-l
    .lit F_HIDDEN
    .wp OR
    .wp CCOMMA ; w-a w-l
    .wp DUP ; w-a w-l w-l
    .wp ALLOT ; w-a w-l here
    .wp SWAP ; w-a here w-l
    .wp MEMCPY
.wp EXIT

dword HEADER,6,,
    .wp WORD ; w-a w-l
    .wp IHEADER
.wp EXIT

dword \,,1,,COMMA ; ( a -- )
    .wp HERE
    .wp POKE
    .wp CELL
    .wp ALLOT
    .wp DROP
.wp EXIT

dword C\,,2,,CCOMMA ; ( a -- )
    .wp HERE
    .wp POKEBYTE
    .wp ONE
    .wp ALLOT
    .wp DROP
.wp EXIT

dword CREATE,6,,
    .wp HEADER
    .wp REVEAL
    .lit $22
    .wp CCOMMA
    .comp DOVAR
.wp EXIT

dword VARIABLE,8,,
    .wp CREATE
    .comp 0
.wp EXIT

dword CONSTANT,8,, ; ( a -- )
    .wp HEADER
    .wp REVEAL
    .compb $22
    .comp DOCON
    .wp COMMA
.wp EXIT

dword (FORGET),8,,IFORGET ; ( word -- )
    .wp DUP
    .wp PEEK
    .wp LATEST
    .wp POKE
    .wp DP
    .wp POKE
.wp EXIT

dword FORGET,6,,
    .wp _HTICK
    .wp IFORGET
.wp EXIT

dword ['],3,F_IMMED+F_COMPILEONLY,CLIT
    .comp LIT
.wp EXIT

dword WORDS,5,,
    .wp LATEST
    .wp PEEK ; addr
WORDS_loop:

    .wp DUP ; addr addr
    .wp ZNEQU ; addr cond
    .zbranch WORDS_end ; addr

    .wp DUP ; addr addr
    .wp INCRTWO ; addr flagaddr
    .wp PEEKBYTE ; addr flags
    .wp DUP ; addr flags flags
    .lit F_HIDDEN
    .wp AND ; addr flags hidden
    .wp ZEQU
    .zbranch WORDS_hidden
    .lit F_LENMASK
    .wp AND ; addr length
    .wp OVER ; addr length addr
    .lit 3
    .wp ADD ; addr length straddr
    .wp SWAP ; addr straddr length
    .wp TYPE
    .wp SPACE
WORDS_hidden_return:
    .wp PEEK ; newaddr
    .branch WORDS_loop
WORDS_end:
    .wp DROP
.wp EXIT

WORDS_hidden:
    .wp DROP ; addr
    .branch WORDS_hidden_return

dword ?COMPILE-ONLY,13,,ISCOMPONLY ; ( address -- cond )
    .wp INCRTWO
    .wp PEEK
    .lit F_COMPILEONLY
    .wp AND
    .wp ZNEQU
.wp EXIT

dword ?IMMEDIATE,10,,ISIMMEDIATE ; ( address -- cond )
    .wp INCRTWO
    .wp PEEK
    .lit F_IMMED
    .wp AND
    .wp ZNEQU
.wp EXIT

.ifcflag defer
    .macro defer [name],namelen,flags=0,[label]=${name},pointer=EXIT
        dcode ${name},${namelen},${flags},${label}
        jsr defer_does
        .wp ${pointer}
    .endm

    dword DEFER,5,,
        .wp CREATE
        .lit EXIT
        .wp COMMA
        .wp NDOES
        .wp EXIT
    defer_does:
        ent DOCOL
        .wp PEEK
        .wp EXECUTE
    .wp EXIT

    dword DEFER!,6,,DEFER_SET
        .lit 3
        .wp ADD
        .wp POKE
    .wp EXIT

    dword IS,2,,
        .wp _TICK
        .wp DEFER_SET
    .wp EXIT

    dword DEFER@,6,,DEFER_GET
        .lit 3
        .wp ADD
        .wp PEEK
    .wp EXIT
.endif