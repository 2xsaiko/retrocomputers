dvar TERMADDR,8,,,

dcode BINDTERM,8,F_HIDDEN,
    jsr bind_term
nxt

bind_term:
    lda var_TERMADDR
    mmu $00
rts

dword XY@,3,,GETXY ; ( -- x y )
    .wp BINDTERM
    .wp ONE
    .wp BUS_PEEK
    .wp SPLIT
.wp EXIT

dword XY!,3,,SETXY ; ( x y -- )
    .wp JOIN
    .wp BINDTERM
    .wp ONE
    .wp BUS_POKE
.wp EXIT

dword CURSOR,6,, ; ( cursortype -- )
    .wp BINDTERM
    .lit 3
    .wp BUS_POKEBYTE
.wp EXIT

dcode KEY?,4,,KEYQ ; ( -- key/0 )
    jsr bind_term
    mmu $81
    tax
    lda #$0000
    sep #$20
    lda $0004, x
    cmp $0005, x
    beq KEYQ_nokey
    lda $0006, x
    inc $0004, x
    rep #$20
    pha
nxt

KEYQ_nokey:
    rep #$20
    lda #$0000
    pha
nxt

dcode KEY,3,, ; ( -- key )
    jsr bind_term
    mmu $81
    tax
    lda #$0000
    sep #$20
    bra KEY_check
KEY_waitloop:
    wai
KEY_check:
    lda $0004, x
    cmp $0005, x
    beq KEY_waitloop
    lda $0006, x
    inc $0004, x
    rep #$20
    pha
nxt

;unnamed_word handle_backspace

;.wp EXIT

dword ACCEPT,6,,ACCEPT ; ( c-addr maxlength -- read )
    .wp TWOTOR

    .wp ZERO
RL_loop:
    .wp KEY

    .wp DUP
    .lit _F_RETURN
    .wp EQU
    .zbranch RL_a
    .wp DROP
    .wp RDROP
    .wp RDROP
    .wp SPACE
    .wp EXIT
RL_a:
    .wp DUP
    .lit _F_BACKSPACE
    .wp EQU
    .zbranch RL_b
    .wp DROP
    .wp DUP
    .zbranch RL_loop
    .wp GETXY
    .wp DROP
    .zbranch RL_loop
    .wp GETXY
    .wp SWAP
    .wp DECR
    .wp SWAP
    .wp TWODUP
    .wp SETXY
    .wp SPACE
    .wp SETXY
    .wp DECR
    .branch RL_loop
RL_b:
    .wp OVER
    .wp FROMR
    .wp FROMR
    .wp DUP
    .wp NROT
    .wp TOR
    .wp TOR
    .wp EQU
    .zbranch RL_c
    .wp DROP
    .branch RL_loop
RL_c:
    .wp DUP
    .wp EMIT
    .wp OVER
    .wp RFETCH
    .wp ADD
    .wp POKEBYTE
    .wp INCR
    .branch RL_loop
;.wp EXIT

dcode EMIT,4,, ; ( char -- )
    jsr bind_term
    mmu $81
    tay
    sep #$20
    lda $0002, y
    sta $0000, y
    lda $0001, y
    tax
    clc
    adc #$10
    sta $02
    stz $03
    pla ; get char
    sta ($0002), y
    pla ; throw high bytes away
    txa
    inc a
    cmp #$50
    beq CR
    sta $0001, y
    rep #$20
nxt

dcode CR,2,,
    rep #$20
    jsr bind_term
    mmu $81
    tax
    sep #$20
    stz $0001, x
    lda $0002, x
    inc a
    cmp #$32
    bne CR_continue
    rep #$20
    jsr SCROLL_begin
    sep #$20
    lda #$31
CR_continue:
    sta $0002, x
    rep #$20
nxt

dcode SCROLL,6,,
    jsr bind_term
    jsr SCROLL_begin
nxt

SCROLL_begin:
    mmu $81
    tax
    sep #$20
    lda $0002, x
    beq SCROLL_topline
    dec $0002, x
    bra SCROLL_cont
SCROLL_topline:
    lda #$31
    sta $0002, x
SCROLL_cont:
    stz $0008, x
    stz $000A, x
    stz $000B, x
    lda #$01
    sta $0009, x
    lda #$50
    sta $000C, x
    lda #$31
    sta $000D, x
    lda #$03
    sta $0007, x
    wai
    lda #$20
    sta $0008, x
    lda #$31
    sta $000B, x
    lda #$01
    sta $000D, x
    sta $0007, x
    wai
    rep #$20
rts

dword PAGE,4,,
    .wp BINDTERM

    .lit $20
    .lit $08
    .wp BUS_POKEBYTE
    .wp ZERO
    .lit $0A
    .wp BUS_POKEBYTE
    .wp ZERO
    .lit $0B
    .wp BUS_POKEBYTE
    .lit 80
    .lit $0C
    .wp BUS_POKEBYTE
    .lit 50
    .lit $0D
    .wp BUS_POKEBYTE
    .wp ONE
    .lit $07
    .wp BUS_POKEBYTE

    .wp ZERO
    .lit $01
    .wp BUS_POKE

    .wp TICK
.wp EXIT

dword TYPE,4,, ; ( address length -- )
    .wp DUP
    .zbranch TYPE_zerolength

    .wp ZERO
TYPE_loop:
    .wp TWOTOR
    .wp DUP
    .wp I
    .wp ADD
    .wp PEEK
    .wp EMIT
    .wp TWOFROMR
    .wp INCR
    .wp TWODUP
    .wp EQU
    .zbranch TYPE_loop
    .wp DROP
TYPE_zerolength:
    .wp TWODROP
.wp EXIT

dconst BL,2,,,_F_BL

dword SPACE,5,,
    .wp BL
    .wp EMIT
.wp EXIT

dword SPACES,6,, ; ( n -- )
    .wp TOR
SPACES_loop:
    .wp RFETCH
    .zbranch SPACES_end

    .wp SPACE

    .wp FROMR
    .wp DECR
    .wp TOR
    .branch SPACES_loop
SPACES_end:
    .wp RDROP
.wp EXIT