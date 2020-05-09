dword =,1,,EQU ; ( a b -- a==b )
    .wp SUB
    .wp ZEQU
.wp EXIT

dword <>,2,,NEQU ; ( a b -- a!=b )
    .wp EQU
    .wp INVERT
.wp EXIT

dword <,1,,LT ; ( a b -- a<b )
    .wp SUB
    .wp ZLT
.wp EXIT

dword >,1,,GT ; ( a b -- a>b )
    .wp SWAP
    .wp LT
.wp EXIT

dword <=,2,,LE ; ( a b -- a<=b )
    .wp GT
    .wp INVERT
.wp EXIT

dword >=,2,,GE ; ( a b -- a>=b )
    .wp LT
    .wp INVERT
.wp EXIT

dcode U>,2,,UGT ; ( a b -- a>b )
    ldx #_F_FALSE
    pla
    sec
    sbc $01, s
    bcs ULT_end
    ldx #_F_TRUE
ULT_end:
    ply
    phx
nxt

dword U<,2,,ULT ; ( a b -- a<b )
    .wp SWAP
    .wp UGT
.wp EXIT

dword U<=,3,,ULE ; ( a b -- a<=b )
    .wp UGT
    .wp INVERT
.wp EXIT

dword U>=,3,,UGE ; ( a b -- a>=b )
    .wp ULT
    .wp INVERT
.wp EXIT

dcode 0=,2,,ZEQU ; ( a -- a==0 )
    pla
    beq ZEQU_yes
    lda #_F_FALSE
    bra ZEQU_end
ZEQU_yes:
    lda #_F_TRUE
ZEQU_end:
    pha
nxt

dcode 0<>,3,,ZNEQU ; ( a -- a!=0 )
    pla
    bne ZNEQU_yes
    lda #_F_FALSE
    bra ZNEQU_end
ZNEQU_yes:
    lda #_F_TRUE
ZNEQU_end:
    pha
nxt

dcode 0<,2,,ZLT ; ( a -- a<0 )
    pla
    bmi ZLT_yes
    lda #_F_FALSE
    bra ZLT_end
ZLT_yes:
    lda #_F_TRUE
ZLT_end:
    pha
nxt

dword 0>,2,,ZGT ; ( a -- a>0 )
    .wp ZERO
    .wp GT
.wp EXIT

dword 0<=,3,,ZLE ; ( a -- a<=0 )
    .wp ZERO
    .wp LE
.wp EXIT

dword 0>=,3,,ZGE ; ( a -- a>=0 )
    .wp ZERO
    .wp GE
.wp EXIT

dword MIN,3,, ; ( a b -- {a|b} )
    .wp TWODUP ; a b a b
    .wp LT ; a b ?a<b
    .zbranch MIN_b
    .wp DROP
.wp EXIT
MIN_b:
    .wp NIP
.wp EXIT

dword MAX,3,, ; ( a b -- {a|b} )
    .wp TWODUP ; a b a b
    .wp LT ; a b ?a<b
    .zbranch MAX_b
    .wp NIP
.wp EXIT
MAX_b:
    .wp DROP
.wp EXIT

dcode AND,3,, ; ( a b -- a&b )
    pla
    and $01, s
    ply
    pha
nxt

dcode OR,2,, ; ( a b -- a|b )
    pla
    ora $01, s
    ply
    pha
nxt

dcode XOR,3,, ; ( a b -- a^b )
    pla
    eor $01, s
    ply
    pha
nxt

dcode INVERT,6,, ; ( a -- ~a )
    pla
    eor #$ffff
    pha
nxt

dword NEGATE,6,, ; ( a -- -a )
    .wp INVERT
    .wp INCR
.wp EXIT

dcode LSHIFT,6,, ; ( a b -- a<<b )
    ply ; get amount to rot left
    pla ; get number
LSHIFT_loop:
    clc
    rol a
    dey
    bne LSHIFT_loop
    pha
nxt

dcode RSHIFT,6,, ; ( a b -- a>>b )
    ply ; get amount to rot right
    pla ; get number
RSHIFT_loop:
    clc
    ror a
    dey
    bne RSHIFT_loop
    pha
nxt