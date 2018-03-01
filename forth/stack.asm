dconst RP0,3,,RPORIG,_F_RPORIG

dcode RP@,3,,RPGET ; ( -- RP )
    trx
    phx
nxt

dcode RP!,3,,RPSTORE ; ( RP -- )
    plx
    txr
nxt

dcode 0RP,3,,RPRST ; ( -- )
    ldx #_F_RPORIG
    txr
nxt

dconst SP0,3,,SPORIG,_F_SPORIG

dcode SP@,3,,SPGET ; ( -- SP )
    tsx
    phx
nxt

dcode SP!,3,,SPSET ; ( SP -- )
    plx
    txs
nxt

dcode 0SP,3,,SPRST ; ( -- )
    ldx #_F_SPORIG
    txs
nxt

dword DEPTH,5,, ; ( -- stack-depth )
    .wp SPORIG
    .wp SPGET
    .wp SUB
    .lit 2
    .wp SUB
    .wp CELL
    .wp DIV
.wp EXIT

dcode DROP,4,, ; ( a -- )
    pla
nxt

dcode SWAP,4,, ; ( a b -- b a )
    plx
    ply
    phx
    phy
nxt

dcode DUP,3,, ; ( a -- a a )
    lda $01, s
    pha
nxt

dcode OVER,4,, ; ( a b -- a b a )
    lda $03, s
    pha
nxt

dword NIP,3,, ; ( a b -- b )
    .wp SWAP
    .wp DROP
.wp EXIT

dword TUCK,4,, ; ( a b -- b a b )
    .wp SWAP
    .wp OVER
.wp EXIT

dcode ROT,3,, ; ( a b c -- b c a )
    pla
    plx
    ply
    phx
    pha
    phy
nxt

dcode -ROT,4,,NROT ; ( a b c -- c a b )
    pla
    plx
    ply
    pha
    phy
    phx
nxt

dcode 2DROP,5,,TWODROP ; ( a b -- )
    pla
    pla
nxt

dword 2DUP,4,,TWODUP ; ( a b -- a b a b )
    .wp OVER
    .wp OVER
.wp EXIT

dcode 2SWAP,5,,TWOSWAP ; ( a b c d -- c d a b )
    plx
    ply
    rhx
    rhy
    plx
    ply
    rla
    pha
    rla
    pha
    phy
    phx
nxt

dcode ?DUP,4,,QDUP ; ( a -- a a )
    pla
    pha
    beq QDUP_zero
    pha
QDUP_zero:
nxt

dcode >R,2,,TOR ; ( a -- ) R: ( -- a )
    pla
    rha
nxt

dcode R>,2,,FROMR ; ( -- a ) R: ( a -- )
    rla
    pha
nxt

dcode 2>R,3,,TWOTOR ; ( a b -- ) R: ( -- b a )
    pla
    rha
    pla
    rha
nxt

dcode 2R>,3,,TWOFROMR ; ( -- a b ) R: ( b a -- )
    rla
    pha
    rla
    pha
nxt

dcode R@,2,,RFETCH ; ( -- a ) R: ( a -- a )
    lda $01, r
    pha
nxt

dcode RDROP,5,,RDROP ; R: ( a -- )
    rla
nxt