# Radio memory map

| Address       | Purpose                                                         |
|---------------|-----------------------------------------------------------------|
| `$00` - `$7F` | data buffer                                                     |
| `$80` - `$81` | sender id                                                       |
| `$82`         | command (1: listen for message, 2: targeted send, 3: broadcast) |
| `$83` - `$84` | target id / receive id                                          |
| `$85`         | response count                                                  |
| `$86`         | selected response (changes data buffer read and distance)       |
| `$87` - `$8A` | distance (32-bit float)                                         |
| `$8B` - `$FF` | _unused_                                                        |