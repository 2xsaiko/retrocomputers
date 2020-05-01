# Terminal memory map

| Address       | Purpose                                                  |
|---------------|----------------------------------------------------------|
| `$00`         | current screen row                                       |
| `$01`         | cursor x                                                 |
| `$02`         | cursor y                                                 |
| `$03`         | cursor mode (0: hidden, 1: solid, 2: blink)              |
| `$04`         | key buffer start (16 byte buffer)                        |
| `$05`         | key buffer position                                      |
| `$06`         | key value at buffer start                                |
| `$07`         | command (1: fill, 2: invert, 3: shift, 4: reset charset) |
| `$08`         | blit x start / fill value                                |
| `$09`         | blit y start                                             |
| `$0A`         | blit x offset                                            |
| `$0B`         | blit y offset                                            |
| `$0C`         | blit width                                               |
| `$0D`         | blit height                                              |
| `$0E`         | editable char                                            |
| `$0F`         | _unused_                                                 |
| `$10` - `$5F` | screen line                                              |
| `$60` - `$67` | character bitmap data                                    |
| `$68` - `$FF` | _unused_                                                 |