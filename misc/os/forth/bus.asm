dcode BA!,3,,BUS_SETADDR ; ( addr -- )
    pla
    mmu $00
nxt

dcode BA@,3,,BUS_GETADDR ; ( -- addr )
    mmu $80
    pha
nxt

dcode BW!,3,,BUS_SETWIN ; ( win -- )
    pla
    mmu $01
nxt

dcode BW@,3,,BUS_GETWIN ; ( win -- )
    mmu $81
    pha
nxt

dword BUS!,4,,BUS_POKE ; ( value addr -- )
    .wp BUS_GETWIN
    .wp ADD
    .wp POKE
.wp EXIT

dword BUS@,4,,BUS_PEEK ; ( addr -- value )
    .wp BUS_GETWIN
    .wp ADD
    .wp PEEK
.wp EXIT

dword BUSC!,5,,BUS_POKEBYTE ; ( value addr -- )
    .wp BUS_GETWIN
    .wp ADD
    .wp POKEBYTE
.wp EXIT

dword BUSC@,5,,BUS_PEEKBYTE ; ( addr -- value )
    .wp BUS_GETWIN
    .wp ADD
    .wp PEEKBYTE
.wp EXIT