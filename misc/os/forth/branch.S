dcode BRANCH,6,F_COMPILEONLY,
    nxa
    tax
    txi
nxt

dcode 0BRANCH,7,F_COMPILEONLY,ZBRANCH
    nxa
    ply
    bne ZBRANCH_false
    tax
    txi
ZBRANCH_false:
nxt

dword RECURSE,7,F_IMMED+F_COMPILEONLY,
    .wp LATEST
    .wp PEEK
    .wp TCFA
    .wp COMMA
.wp EXIT

dword IF,2,F_IMMED+F_COMPILEONLY,
    .comp ZBRANCH
    .wp HERE
    .comp 0
.wp EXIT

dword THEN,4,F_IMMED+F_COMPILEONLY,
    .wp HERE
    .wp SWAP
    .wp POKE
.wp EXIT

dword ELSE,4,F_IMMED+F_COMPILEONLY,
    .comp BRANCH
    .wp HERE
    .comp 0
    .wp SWAP
    .wp THEN
.wp EXIT

dword BEGIN,5,F_IMMED+F_COMPILEONLY,
    .wp HERE
.wp EXIT

dword UNTIL,5,F_IMMED+F_COMPILEONLY,
    .comp ZBRANCH
    .wp COMMA
.wp EXIT

dword AGAIN,5,F_IMMED+F_COMPILEONLY,
    .comp BRANCH
    .wp COMMA
.wp EXIT

dword UNLESS,6,F_IMMED+F_COMPILEONLY,
    .comp ZEQU
    .wp IF
.wp EXIT

dword WHILE,5,F_IMMED+F_COMPILEONLY,
    .comp ZBRANCH
    .wp HERE
    .comp 0
.wp EXIT

dword REPEAT,6,F_IMMED+F_COMPILEONLY,
    .comp BRANCH
    .wp SWAP
    .wp COMMA
    .wp HERE
    .wp SWAP
    .wp POKE
.wp EXIT

dword DO,2,F_IMMED+F_COMPILEONLY,
    .wp ZERO
    .wp HERE
    .comp TWOTOR
.wp EXIT

dword ?2DUP,5,F_HIDDEN,QTWODUP
    .wp TWODUP
    .wp NEQU
    .zbranch QTWODUP_eq
    .wp TWODUP
QTWODUP_eq:
.wp EXIT

dword ?DO,3,F_IMMED+F_COMPILEONLY,QDO
    .comp QTWODUP
    .comp NEQU
    .comp ZBRANCH
    .wp HERE
    .comp 0
    .wp HERE
    .comp TWOTOR
.wp EXIT

dword +LOOP,5,F_IMMED+F_COMPILEONLY,PLUSLOOP
    .comp TWOFROMR
    .comp ROT
    .comp ADD
    .comp TWODUP
    .comp EQU
    .comp ZBRANCH
    .wp COMMA
    .comp TWODROP
    .wp DUP
    .wp ZBRANCH
    .wp LOOP_noqdo
    .wp HERE
    .wp SWAP
    .wp POKE
.wp EXIT
LOOP_noqdo:
    .wp DROP
.wp EXIT

dword LOOP,4,F_IMMED+F_COMPILEONLY,
    .clt 1
    .wp PLUSLOOP
.wp EXIT

dcode UNLOOP,6,, ; R: ( a b -- )
    rla
    rla
nxt

dcode I,1,,
    lda $03, r
    pha
nxt

dcode J,1,,
    lda $07, r
    pha
nxt

dcode K,1,,
    lda $0b, r
    pha
nxt