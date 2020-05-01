; Disk drive controls

; Disk commands:
; 1: Read disk name
; 2: Write disk name
; 3: Read disk serial
; 4: Read disk sector
; 5: Write disk sector
; 6: Clear sector buffer

dvar DISKADDR,8,,,

bind_disk:
    lda var_DISKADDR
    mmu $00
rts

dcode BINDDISK,8,F_HIDDEN,
    jsr bind_disk
nxt

dword DISKCMD,7,,
    .wp BINDDISK
    .lit $82
    .wp BUS_POKEBYTE
    .wp TICK
.wp EXIT

dword SAVE,4,,
    .wp BINDDISK
    .wp ZERO
SAVE_loop:
    .wp TOR

    .wp RFETCH ; i
    .lit $80
    .wp BUS_POKE
    .lit $0500
    .wp RFETCH
    .lit $80
    .wp UMUL
    .wp ADD
    .wp BUS_GETWIN
    .lit $80
    .wp MEMCPY
    .lit 5
    .wp DISKCMD

    .wp FROMR ; i
    .wp INCR ; i+1
    .wp DUP ; i i
    .wp HERE
    .lit $0500
    .wp SUB
    .lit $80
    .wp UDIV ; i i max
    .wp LE ; i cond
    .zbranch SAVE_end ; i
    .branch SAVE_loop
SAVE_end:
    .wp DROP
.wp EXIT

.ifcflag disk_ext
    BLKBUF_INT: db 0,0
    dword BLKBUF,6,,
        .lit BLKBUF_INT
        .wp PEEK
        .zbranch BLKBUF_allot
        .lit BLKBUF_INT
        .wp PEEK
    .wp EXIT
    BLKBUF_allot:
        .lit 1024
        .wp ALLOT
        .wp DUP
        .lit BLKBUF_INT
        .wp POKE
    .wp EXIT

    dvar BLKNO,5,,,
    dvar BLKINT,6,F_HIDDEN,,_F_TRUE

    dword CLDRBUF,7,F_HIDDEN,
        .lit 6
        .wp DISKCMD
    .wp EXIT

    dword DISKNAME",9,,SET_DISKNAME
        .wp CLDRBUF
        .lit $22
        .wp PARSE ; addr len
        .wp BUS_GETWIN
        .wp SWAP
        .wp MEMCPY
        .lit 2
        .wp DISKCMD
    .wp EXIT

    dword BLOCK,5,,
        .wp BLKNO
        .wp POKE
        .wp REVERT
    .wp EXIT

    dword REVERT,6,, ; get 8 sectors (128 * 8 = 1024) into the buffer
            .wp BINDDISK
            .wp BLKNO
            .wp PEEK
            .lit 8
            .wp MUL ; origin

            .lit 0
    REVERT_loop:
            .wp TOR
            .wp RFETCH ; origin i
            .wp OVER ; origin i origin
            .wp ADD ; origin current-sector
            .lit $80
            .wp BUS_POKE ; origin
            .lit 4
            .wp DISKCMD

            .wp BUS_GETWIN ; origin c-origin
            .wp RFETCH
            .lit 128
            .wp UMUL
            .wp BLKBUF
            .wp ADD
            .lit 128 ; origin c-origin c-target c-length
            .wp MEMCPY ; origin

            .wp FROMR ; origin i
            .wp INCR ; origin i+1
            .wp DUP ; origin i i
            .lit 8
            .wp LT ; origin i cond
            .zbranch REVERT_end ; origin i
            .branch REVERT_loop
    REVERT_end:
            .wp TWODROP
    .wp EXIT

    dword FLUSH,5,,
        .wp BINDDISK
        .wp BLKNO
        .wp PEEK
        .lit 8
        .wp MUL ; origin

        .wp ZERO
    FLUSH_loop:
        .wp TOR
        .wp RFETCH ; origin i
        .wp OVER ; origin i origin
        .wp ADD ; origin current-sector
        .lit $80
        .wp BUS_POKE ; origin

        .wp BUS_GETWIN ; origin c-origin
        .wp RFETCH
        .lit 128
        .wp UMUL
        .wp BLKBUF
        .wp ADD
        .wp SWAP
        .lit 128 ; origin c-origin c-target c-length
        .wp MEMCPY ; origin

        .lit 5
        .wp DISKCMD

        .wp FROMR ; origin i
        .wp INCR ; origin i+1
        .wp DUP ; origin i i
        .lit 8
        .wp LT ; origin i cond
        .zbranch FLUSH_end ; origin i
        .branch FLUSH_loop
    FLUSH_end:
        .wp TWODROP
    .wp EXIT

    dword LIST,4,,
        .wp CR
        .lit 0
    LIST_loop:
        .wp TOR
        .wp RFETCH
        .lit 2
        .wp PRINT_UNUM_ALIGN
        .wp SPACE

        .wp BLKBUF
        .wp RFETCH
        .lit 64
        .wp MUL
        .wp ADD
        .lit 64
        .wp TYPE

        .wp CR

        .wp FROMR
        .wp INCR
        .wp DUP
        .lit 16
        .wp EQU
        .zbranch LIST_loop
        .wp DROP
    .wp EXIT

    dword WIPE,4,,
        .wp BL
        .wp BLKBUF
        .lit 1024
        .wp FILL
    .wp EXIT

    dconst BLKSIZE,7,F_HIDDEN,,128
    dword BLKCEIL,7,F_HIDDEN,
        .wp DECR
        .wp BLKSIZE
        .wp UDIV
        .wp INCR
        .wp BLKSIZE
        .wp UMUL
    .wp EXIT

    dword PP,2,,
        .wp BL ; ln bl
        .wp BLKBUF ; ln bl blkbuf
        .wp ROT ; bl blkbuf ln
        .lit 64
        .wp UMUL ; bl blkbuf offset
        .wp ADD ; bl pos
        .wp TUCK ; pos bl pos
        .lit 64
        .wp FILL ; pos
        .lit $FF
        .wp PARSE ; pos addr len
        .wp ROT ; addr len pos
        .wp SWAP ; addr pos len
        .lit 64
        .wp MIN
        .wp MEMCPY
    .wp EXIT

    dword LOAD,4,, ; ( blockid -- )
        .wp BLKNO
        .wp PEEK
        .wp TOR
        .wp BLOCK
        .lit 0
    LOAD_loop:
        .wp TOR

        .wp RFETCH
        .lit 64
        .wp UMUL
        .wp BLKBUF
        .wp ADD
        .lit 64
        .wp INTERPRET

        .wp FROMR
        .wp INCR
        .wp DUP
        .lit 16
        .wp LT
        .zbranch LOAD_end
        .branch LOAD_loop
    LOAD_end:
        .wp DROP
        .wp FROMR
        .wp BLOCK
    .wp EXIT
.endif