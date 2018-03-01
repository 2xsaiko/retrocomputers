; Bootloader

section .data
booting: db 'Booting', $00
no_disk: db 'No disk?', $00
retry: db 'Retry', $00

section .text
    xce
start:
    rep #$30
    ldx #$01FF
    txs
    lda #start
    mmu $05
    mmu $06
    mmu $02
    lda #$0300
    mmu $01
    ldy #$0500
    sty $20
    ldy #$0000

    ; clear screen
    jsr rb_term
    sep #$20
    lda #$20
    sta $0308 ; set fill value to space
    stz $030A ; set fill x origin to 0
    stz $030B ; set fill y origin to 0
    lda #$50 ; (width: 80)
    sta $030C ; set fill width to 80
    lda #$32 ; (height: 50)
    sta $030D ; set fill height to 50
    lda #$01
    sta $0307 ; enable fill mode
    inc a
    sta $0303 ; set cursor mode to blink
    stz $0301 ; set cursor x position to 0
    stz $0302 ; set cursor y position to 0
    wai ; wait for fill operation
    lda #$04
    sta $0307 ; reset charset
    wai
    rep #$20

    lda #booting
    sta $10
    jsr print
    ldy #$0000
load_disk: ; load sector
    sep #$20
    jsr rb_disk
    sty $0380
    lda #$04
    sta $0382
load_loop:
    wai
    lda $0382
    cmp #$04
    beq load_loop
    pha
    phy
    ldy #$0080
memcpy_loop:
    cpy #$0000
    beq memcpy_end
    dey
    lda $0300, y
    sta ($0020), y
    bra memcpy_loop
memcpy_end:
    rep #$20
    lda #$0080
    clc
    adc $0020
    sta $0020
    sep #$20
    ply
    pla
    bne ls_eof
    iny
    bra load_disk
ls_eof:
    jsr rb_term
    rep #$30
    cpy #$0000
    beq ls_err
    mmu $82
    jmp $0500
ls_err:
    lda #no_disk
    sta $10
    jsr print
    lda #retry
    sta $10
    jsr print
    ldx #$0028
sleep: ; wait x ticks
    wai
    dex
    bne sleep
    brk

rb_term: ; setup terminal r/w
    lda $01
    mmu $00
    rts

rb_disk: ; setup disk r/w
    lda $00
    mmu $00
    rts

print: ; print string at address $10 to terminal
    jsr rb_term
    sep #$30
    lda $0302
    sta $0300 ; set screen row to cursor y position
    ldy #$00
print_loop:
    lda ($0010), y
    beq print_end
    sta $0310, y
    iny
    bra print_loop
print_end:
    inc $0302 ; move cursor 1 down
    rep #$30
    rts