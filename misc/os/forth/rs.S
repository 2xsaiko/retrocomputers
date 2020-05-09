dvar IOXADDR,7,,,3

dword BINDIOX,7,F_HIDDEN,
    .wp IOXADDR
    .wp PEEK
    .wp BUS_SETADDR
.wp EXIT

dword IOX!,4,,IOXPOKE
    .wp BINDIOX
    .lit 2
    .wp BUS_POKE
.wp EXIT

dword IOXO@,5,F_HIDDEN,IOXOPEEK
    .wp BINDIOX
    .lit 2
    .wp BUS_PEEK
.wp EXIT

dword IOX@,4,,IOXPEEK
    .wp BINDIOX
    .wp ZERO
    .wp BUS_PEEK
.wp EXIT

dword IOXSET,6,,
    .wp IOXOPEEK
    .wp OR
    .wp IOXPOKE
.wp EXIT

dword IOXRST,6,,
    .wp INVERT
    .wp IOXOPEEK
    .wp AND
    .wp IOXPOKE
.wp EXIT