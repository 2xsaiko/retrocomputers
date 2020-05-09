dword ],1,F_IMMED,RBRAC
    .wp ONE
    .wp STATE
    .wp POKE
.wp EXIT

dword [,1,F_IMMED,LBRAC
    .wp ZERO
    .wp STATE
    .wp POKE
.wp EXIT

dword \;,1,F_IMMED+F_COMPILEONLY,SEMICOLON
    .comp EXIT
    .wp REVEAL
    .wp LBRAC
.wp EXIT

dword \:,1,,COLON
    .wp HEADER
    .lit $22
    .wp CCOMMA
    .comp DOCOL
    .wp RBRAC
.wp EXIT

.ifncflag min
    dword },1,F_IMMED+F_COMPILEONLY,END_QCOMP
        .comp EXIT
        .wp LBRAC
        .wp LATEST
        .wp PEEK
        .wp TCFA
        .wp EXECUTE
        .wp LATEST
        .wp PEEK
        .wp IFORGET
    .wp EXIT

    dword {,1,,BEGIN_QCOMP
        .wp ZERO
        .wp DUP
        .wp IHEADER
        .lit $22
        .wp CCOMMA
        .comp DOCOL
        .wp RBRAC
    .wp EXIT
.endif

dword LITERAL,7,F_IMMED+F_COMPILEONLY,
    .comp LIT
    .wp COMMA
.wp EXIT

dword [COMPILE],9,F_IMMED+F_COMPILEONLY,COMPILEB
    .wp _TICK
    .wp COMMA
.wp EXIT

dword (DOES>),7,,NDOES
    ; replace ent DOVAR with jsr
    .wp LATEST
    .wp PEEK
    .wp TCFA
    .wp DUP
    .lit $20 ; jsr abs
    .wp SWAP
    .wp POKEBYTE
    .wp INCR
    .wp RFETCH
    .wp INCRTWO ; skip 'EXIT'
    .wp SWAP
    .wp POKE
.wp EXIT

.macro .does
    .wp NDOES
    .wp EXIT
    ent DOCOL
.endm

dword DOES>,5,F_IMMED+F_COMPILEONLY,DOES
    .comp NDOES
    .comp EXIT
    .compb $22 ; ent WORD
    .comp DOCOL
.wp EXIT

dword POSTPONE,8,F_IMMED+F_COMPILEONLY,
    .wp _HTICK
    .wp DUP
    .wp ISIMMEDIATE
    .zbranch POSTPONE_notimmed
    .wp TCFA
    .wp COMMA
.wp EXIT
POSTPONE_notimmed:
    .wp TCFA
    .comp LIT
    .wp COMMA
    .comp COMMA
.wp EXIT