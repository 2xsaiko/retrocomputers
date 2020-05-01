# Disk Drive memory map

| Address       | Purpose                                                                                                                |
|---------------|------------------------------------------------------------------------------------------------------------------------|
| `$00` - `$7F` | general purpose buffer                                                                                                 |
| `$80` - `$81` | sector index                                                                                                           |
| `$82`         | drive command (1: get disk name, 2: set disk name, 3: get disk uuid, 4: read sector, 5: write sector, 6: clear buffer) |
| `$83` - `$FF` | _unused_                                                                                                               |