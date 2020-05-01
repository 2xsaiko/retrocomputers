dcode DOCOL,5,,
nxt

dcode DOVAR,5,,
    tix
    phx
    rli
nxt

dcode DOCON,5,,
    nxa
    pha
    rli
nxt

dcode EXIT,4,,
    rli
nxt

dcode LIT,3,,
    nxa
    pha
nxt

dconst 0,1,,ZERO,0
dconst 1,1,,ONE,1

dconst TRUE,4,,,_F_TRUE
dconst FALSE,5,,,_F_FALSE

dcode TICK,4,,
    wai
nxt

dcode TICKS,5,,
    pla
TICKS_loop:
    beq TICKS_end
    wai
    dec a
    bra TICKS_loop
TICKS_end:
nxt

.ifcflag defer
    dword SYSBM,5,,ISTARTUP
        .lit COLD_linea
        .lit 9
        .wp TYPE
        .wp CR
        .wp FREE
        .wp PRINT_UNUM
        .lit COLD_lineb
        .lit 11
        .wp TYPE
        .wp CR
    .wp EXIT

    defer BOOTMESSAGE,11,,,ISTARTUP
.endif

dword COLD,4,,
    .wp DECIMAL
    .wp GETXY
    .wp DROP
    .zbranch COLD_a
    .wp CR
COLD_a:
    .wp CR
    .ifcflag defer
        .wp BOOTMESSAGE
    .endif
    .ifncflag defer
        .lit COLD_linea
        .lit 9
        .wp TYPE
        .wp CR
        .wp FREE
        .wp PRINT_UNUM
        .lit COLD_lineb
        .lit 11
        .wp TYPE
        .wp CR
    .endif

    .wp ABORT

dword ABORT,5,,
    .lit 2
    .wp CURSOR
    .wp SPRST
    .wp QUIT

dword ABORT",6,F_IMMED+F_COMPILEONLY,
    .wp PRTSTR
    .comp ABORT
.wp EXIT

dword QUIT,4,,
    .wp RPRST
    .wp ZERO
    .wp STATE
    .wp POKE
QUIT_loop:
    .wp CR
    .wp STATE
    .wp PEEK
    .zbranch QUIT_pis
    .branch QUIT_pcs
QUIT_cont:

    .lit $80
    .wp DUP
    .wp DUP
    .wp ACCEPT ; read into address $80, max length $80
    .wp INTERPRET

    .lit QUIT_ok
    .lit 3
    .wp TYPE

    .branch QUIT_loop ; loop infinitely

QUIT_pis:
    .lit QUIT_prompta
    .lit 2
    .wp TYPE
    .branch QUIT_cont
QUIT_pcs:
    .lit QUIT_promptb
    .lit 9
    .wp TYPE
    .branch QUIT_cont