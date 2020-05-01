# Retinal Scanner memory map

| Address       | Purpose                                              |
|---------------|------------------------------------------------------|
| `$00` - `$0F` | last player name                                     |
| `$10` - `$13` | last player id                                       |
| `$14`         | last player health                                   |
| `$15`         | state (0: clear, 1: scanning, 2: success, 3: failure |
| `$16` - `$FF` | _unused_                                             |