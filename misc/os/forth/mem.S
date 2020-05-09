dconst CELL,4,,,$02

dcode !,1,,POKE ; ( value addr -- )
    lda $03, s
    ldy #$0000
    sta ($01, s), y
    ply
    ply
nxt

dcode @,1,,PEEK ; ( addr -- value )
    ldy #$0000
    lda ($01, s), y
    ply
    pha
nxt

dword ?,1,,PRTADDR ; ( addr -- )
    .wp PEEK
    .wp PRINT_NUM
.wp EXIT

dcode C!,2,,POKEBYTE ; ( value addr -- )
    lda $03, s
    ldy #$0000
    sep #$20
    sta ($01, s), y
    rep #$20
    ply
    ply
nxt

dword C@,2,,PEEKBYTE ; ( addr -- value )
    .wp PEEK
    .lit $FF
    .wp AND
.wp EXIT

.ifncflag min
    dword C?,2,,PRTBADDR ; ( addr -- )
        .wp PEEKBYTE
        .wp PRINT_NUM
    .wp EXIT
.endif

dword (MEMTEST),9,F_HIDDEN,IMEMTEST
    .lit $FF
    .wp SWAP
    .wp TWODUP ; $FF addr $FF addr
    .wp POKEBYTE ; $FF addr
    .wp PEEKBYTE ; $FF value
    .wp EQU ; cond
.wp EXIT

dword MEMTEST,7,, ; ( addr -- writeable )
    .wp DUP
    .wp DUP ; addr addr addr
    .wp PEEKBYTE ; addr addr value
    .wp TOR ; addr addr
    .wp IMEMTEST ; addr cond
    .wp SWAP ; cond addr
    .wp FROMR ; cond addr value
    .wp SWAP ; cond value addr
    .wp POKEBYTE
.wp EXIT

dword FREE,4,, ; ( -- free-mem )
    .lit $1FFF

    .lit $2000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $3FFF

    .lit $4000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $5FFF

    .lit $6000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $7FFF

    .lit $8000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $9FFF

    .lit $a000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $BFFF

    .lit $c000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $DFFF

    .lit $e000
    .wp MEMTEST
    .zbranch FREE_end
    .wp DROP
    .lit $FFFF

FREE_end:
    .wp HERE
    .wp SUB
.wp EXIT

.ifncflag min
    dword +!,2,,ADDSTORE ; ( value addr -- )
        .wp DUP
        .wp PEEK
        .wp ROT
        .wp ADD
        .wp POKE
    .wp EXIT
.endif

dword @1+!,4,,PEEKINCR ; ( addr -- value )
    .wp DUP
    .wp PEEK
    .wp DUP
    .wp INCR
    .wp SWAP
    .wp NROT
    .wp SWAP
    .wp POKE
.wp EXIT

dword ALLOT,5,, ; ( length -- addr )
    .wp DUP
    .wp DUP
    .wp ZGE
    .zbranch ALLOT_deallot
    .branch ALLOT_cont
ALLOT_deallot:
    .wp NEGATE
ALLOT_cont:
    .wp FREE
    .wp ULE
    .zbranch ALLOT_outofmemory
    .wp HERE
    .wp TUCK
    .wp ADD
    .wp DP
    .wp POKE
.wp EXIT

ALLOT_outofmemory:
    .wp DROP
    .lit ALLOT_str
    .lit 13
    .wp TYPE
.wp ABORT ; reset execution

dcode MEMCPY,6,, ; ( source target length -- )
    ply
    pla
    plx
    jsr memcpy
nxt

memcpy: ; (A: target X: source Y: length)
    stx memcpy_src
    sta memcpy_dest
    sep #$20
memcpy_loop:
    cpy #$0000
    beq memcpy_end
    dey
    lda (memcpy_src), y
    sta (memcpy_dest), y
    bra memcpy_loop
memcpy_end:
    rep #$20
rts

memcpy_src: db 0,0
memcpy_dest: db 0,0

dcode FILL,4,, ; ( byte target length -- )
        ply
        pla
        plx
        jsr fill
    nxt

    fill: ; (A: target X: fill Y: length)
        stx memcpy_src
        sta memcpy_dest
        sep #$20
    fill_loop:
        cpy #$0000
        beq fill_end
        dey
        lda memcpy_src
        sta (memcpy_dest), y
        bra fill_loop
    fill_end:
        rep #$20
    rts

dword ERASE,5,, ; ( target length -- )
    .wp ZERO
    .wp NROT
    .wp FILL
.wp EXIT

dword STRCMP,6,, ; ( addr1 addr2 -- matching-length )
    .wp TWODUP
    .wp NEQU
    .zbranch STRCMP_equaddr

    .wp ZERO
    .wp TOR

STRCMP_loop:
    .wp TWODUP ; addr1 addr2 addr1 addr2
    .wp RFETCH ; addr1 addr2 addr1 addr2 index
    .wp ADD ; addr1 addr2 addr1 loc2
    .wp PEEKBYTE ; addr1 addr2 addr1 value2
    .wp SWAP ; addr1 addr2 value2 addr1
    .wp RFETCH ; addr1 addr2 value2 addr1 index
    .wp ADD ; addr1 addr2 value2 loc1
    .wp PEEKBYTE ; addr1 addr2 value2 value1
    .wp EQU
    .zbranch STRCMP_noequ
    .wp FROMR
    .wp INCR
    .wp TOR
    .branch STRCMP_loop

STRCMP_noequ:
    .wp TWODROP
    .wp FROMR
.wp EXIT

STRCMP_equaddr:
    .wp TWODROP
    .lit $ffff
.wp EXIT