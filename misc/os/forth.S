; Forth OS (RCOS)

; Known bugs:
; TIMES breaks when stack is modified (ex. 10 TIMES DUP)
;
;
;
;

; Compile flags:
; min      : Strip down to minimal level
; disk_ext : Enable extended disk drive words
; math_ext : Enable additional math words
; dbl_size : Enable support for 32-bit integers [beta]
; defer    : Enable support for deferred words

    .macro .wp num
        db ${num}, ^${num}
    .endm

    .macro .lit num
        .wp LIT
        .wp ${num}
    .endm
    
    .macro .compb num
        .lit ${num}
        .wp CCOMMA
    .endm
    
    .macro .comp num
        .lit ${num}
        .wp COMMA
    .endm
    
    .macro .clt num
        .comp LIT
        .comp ${num}
    .endm

    .macro dcode [name],namelen,flags=0,[label]=${name}
        name_${label}:
            db wptr, ^wptr
            .set wptr, name_${label}
            db ${flags}+${namelen}, '${name}'
        ${label}:
    .endm

    .macro dword [name],namelen,flags=0,[label]=${name}
        dcode ${name},${namelen},${flags},${label}
        ent DOCOL
    .endm

    .macro unnamed_word [label]
        ${label}:
            ent DOCOL
    .endm

    .macro dvar [name],namelen,flags=0,[label]=${name},value=0
            dcode ${name},${namelen},${flags},${label}
            ent DOVAR
        var_${label}:
            db ${value}, ^${value}
    .endm

    .macro dconst [name],namelen,flags=0,[label]=${name},value
            dcode ${name},${namelen},${flags},${label}
            ent DOCON
        const_${label}:
            db ${value}, ^${value}
    .endm

    .macro .marker
        name_marker:
            db wptr, ^wptr
            .set wptr, name_marker
            db F_HIDDEN
        marker:
    .endm

    .macro .branch location
        .wp BRANCH
        .wp ${location}
    .endm

    .macro .zbranch location
        .wp ZBRANCH
        .wp ${location}
    .endm

;   .macro callf word
;           lda #${word}
;           jsr callf_do
;   .endm

section 0_strings

    ALLOT_str: db 'Out of memory'
    INTERPRET_texta: db 'Unknown Token\: '
    INTERPRET_textb: db 'Stack Empty'
    INTERPRET_textc: db 'Stack Overflow'
    INTERPRET_textd: db 'Interpreting compile-only word\: '
    QUIT_ok: db ' ok'
    QUIT_prompta: db '> '
    QUIT_promptb: db 'compile\: '
    COLD_linea: db 'RCOS v1.0'
    COLD_lineb: db 'bytes free.'
    
section 1_dict

    .set F_IMMED, $80
    .set F_HIDDEN, $40
    .set F_COMPILEONLY, $20
    .set F_LENMASK, $1F

    .set wptr, $00
    
    ; FORTH Constants
    
    .set _F_RPORIG,$02FF
    .set _F_SPORIG,$01FF
    .set _F_BL,$20
    .set _F_BACKSPACE,$08
    .set _F_RETURN,$0D
    .set _F_TRUE,$FFFF
    .set _F_FALSE,$0000

    .include forth/base.asm

    .include forth/stack.asm
    .include forth/math.asm
    .include forth/logic.asm
    .include forth/mem.asm
    .include forth/dict.asm
    .include forth/io.asm
    .include forth/compile.asm
    .include forth/branch.asm
    
    .ifncflag min
        dword TIMES,5,,
            .wp _TICK
            .wp SWAP
        TIMES_loop:
            .wp DUP
            .wp ZBRANCH
            .wp TIMES_end
            .wp OVER
            .wp EXECUTE
            .wp DECR
            .wp BRANCH
            .wp TIMES_loop
        TIMES_end:
            .wp TWODROP
        .wp EXIT

        dword \\,1,F_IMMED,COMMENT
           .lit $FF
            .wp PARSE
            .wp TWODROP
        .wp EXIT

        dword (,1,F_IMMED,PAREN
            .lit $29
            .wp PARSE
            .wp TWODROP
        .wp EXIT
    .endif
    
    .include forth/string.asm

    .include forth/bus.asm
    .include forth/term.asm
    .include forth/rs.asm
    .include forth/disk.asm

    .marker
    here_pos:
    
section .text

    sep #$20
    lda $00
    sta var_DISKADDR
    lda $01
    sta var_TERMADDR
    rep #$20
start:
    clc
    rep #$30
    lda #$0300
    mmu $01
    mmu $02
    lda #$0400
    mmu $03
    mmu $04
    lda #start
    mmu $05
    mmu $06
    ent COLD

    ; currently unused, here for when it's required

;   callf_do: ; subroutine to call forth words from native code, use the macro 'callf'
;       ply
;       rhy
;       sta callf_ptr
;       rhi
;       ldx callf_a
;       txi
;
;       ent
;   callf_ptr:
;       db $00, $00
;
;   callf_a:
;       .wp callf_b
;   callf_b:
;       rli
;       rli
;       rly
;       phy
;   rts
