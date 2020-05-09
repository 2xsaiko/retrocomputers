dcode 1+,2,,INCR ; ( a -- a+1 )
    pla
    inc a
    pha
nxt

dcode 1-,2,,DECR ; ( a -- a-1 )
    pla
    dec a
    pha
nxt

dcode 2+,2,,INCRTWO ; ( a -- a+2 )
    pla
    inc a
    inc a
    pha
nxt

dcode 2-,2,,DECRTWO ; ( a -- a-2 )
    pla
    dec a
    dec a
    pha
nxt

dcode +,1,,ADD ; ( a b -- a+b )
    pla
    clc
    adc $01, s
    ply
    pha
nxt

dword -,1,,SUB ; ( a b -- a-b )
    .wp NEGATE
    .wp ADD
.wp EXIT

dcode UM*,3,,UMMUL ; ( a b -- d[a*b] )
    pla
    tsx
    sec
    mul $0001, x
    ply
    pha
    phd
nxt

dword U*,2,,UMUL ; ( a b -- a*b )
    .wp UMMUL
    .wp DROP
.wp EXIT

dcode M*,2,,MMUL ; (a b -- d[a*b] )
    pla
    tsx
    clc
    mul $0001, x
    ply
    pha
    phd
nxt

dword *,1,,MUL ; ( a b -- a*b )
    .wp MMUL
    .wp DROP
.wp EXIT

dcode 2*,2,,TWOMUL ; ( a -- 2*a )
    pla
    clc
    rol a
    pha
nxt

dword UM/MOD,6,,UMDIVMOD ; ( d b -- d/b d%b )
    .wp ROT
    .wp _DM
nxt

dword U/MOD,5,,UDIVMOD ; ( a b -- a/b a%b )
    .wp SWAP
    .wp ZERO
    .wp _DM
.wp EXIT

dword /MOD,4,,DIVMOD ; ( a b -- a/b a%b )
    .wp SWAP
    .wp NUM_TODOUBLE
    .wp _DM
.wp EXIT

dcode (UM/MOD),7,F_HIDDEN,_DM
    pld
    pla
    tsx
    sec
    div $0001, x
    ply
    pha
    phd
nxt

dword U/,2,,UDIV ; ( a b -- a/b )
    .wp UDIVMOD
    .wp DROP
.wp EXIT

dword /,1,,DIV ; ( a b -- a/b )
    .wp DIVMOD
    .wp DROP
.wp EXIT

dword MOD,3,, ; ( a b -- a%b )
    .wp UDIVMOD
    .wp NIP
.wp EXIT

dword 2/,2,,TWODIV ; ( a -- b/2 )
    .wp ONE
    .wp RSHIFT
.wp EXIT

dword ABS,3,, ; ( a -- |a| )
    .wp DUP
    .wp ZLT
    .zbranch ABS_noaction
    .wp NEGATE
ABS_noaction:
.wp EXIT

dcode N>D,3,,NUM_TODOUBLE ; ( value -- d )
    pla
    sea
    pha
    phd
nxt

.ifcflag dbl_size
    dword DNEGATE,7,,
        .wp INVERT
        .wp SWAP
        .wp INVERT
        .wp SWAP
        .wp ONE
        .wp ZERO
        .wp DADD
    .wp EXIT

    dcode D+,2,,DADD ; ( a b -- a+b )
        plx
        pla
        clc
        adc $03, s
        tay
        txa
        adc $01, s
        plx
        plx
        phy
        pha
    nxt

    dword D-,2,,DSUB ; ( a b -- a-b )
        .wp DNEGATE
        .wp DADD
    .wp EXIT

    signed_prep:
        ; if the numbers are negative make them positive

        stz $48 ; set $48 to -1, gets increased when a number is negative,
        dec $48 ; is used to determine if the result is negative or not

        lda $40
        bit #$8000
        beq sp_next_a
        ; number is negative, make it positive
        inc $48
        lda #$ffff
        pha
        eor $40
        sta $40
        pla
        eor $42
        inc a
        sta $42
        bne sp_next_a
        inc $40 ; overflow, carry into next bits
    sp_next_a:

        lda $44
        bit #$8000
        beq sp_next_b
        ; number is negative, make it positive
        inc $48
        lda #$ffff
        pha
        eor $44
        sta $44
        pla
        eor $46
        inc a
        sta $46
        bne sp_next_b
        inc $44 ; overflow, carry into next bits
    sp_next_b:
        rts

    load_double:
        pla
        sta $40 ; MSB A
        pla
        sta $42 ; LSB A
        pla
        sta $44 ; MSB B
        pla
        sta $46 ; LSB B
        rts

    double_mul:
        ; multiply LSB A with LSB B
        sec ; we want unsigned multiplication
        lda $42
        mul $46
        rha
        phd

        ; MSB A * LSB B
        lda $40
        mul $46
        pha ; we only need the lower bits

        ; MSB B * LSB A
        lda $44
        mul $42

        clc ; we want to ignore overflow here as this is the upper part of the 32 bits
        adc $01, s
        clc
        adc $03, s
        ply
        ply
        tay ; LSB in y
        rla
        tax ; MSB in x
        rts

    double_div:
        ; TODO Implement division routine
        lda #$0000
        tax
        tay

    setsb:
        lda $48
        bne setsb_positive
        ; if the result should be negative, negate the value
        txa
        eor #$ffff
        tax
        tya
        eor #$ffff
        inc a
        tay
        bcc setsb_positive
        txa
        inc a
        tax
    setsb_positive:
        rts

    dcode D*,2,,DMUL
        jsr load_double
        jsr signed_prep
        jsr double_mul
        jsr setsb
        phx
        phy
    nxt

    dcode D/,2,,DDIV
        jsr load_double
        jsr signed_prep
        jsr double_div
        jsr setsb
        phx
        phy
    nxt

    dcode UD*,3,,UDMUL
        jsr load_double
        jsr double_mul
        phx
        phy
    nxt

    dcode UD/,3,,UDDIV
        jsr load_double
        jsr double_div
        phx
        phy
    nxt
.endif

dcode SPLIT,5,, ; ( $1234 -- $34 $12 )
    pla
    sep #$30
    ldx #$00
    phx
    pha
    xba
    phx
    pha
    rep #$30
nxt

dcode JOIN,4,, ; ( $34 $12 -- $1234 )
    sep #$30
    plx
    ply
    ply
    pla
    phx
    phy
    rep #$30
nxt

.ifcflag math_ext
    dword UNDER+,6,,UNDERADD ; ( a b c -- a+c b )
        .wp ROT
        .wp ADD
        .wp SWAP
    .wp EXIT
.endif