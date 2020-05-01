#!/bin/sh

function die() {
  tput bold
  tput setaf 1
  echo "$@"
  exit 1
}

./assembler bootldr.asm build/bootldr.bin '$0400' || die "Failed to compile bootldr.bin."
./assembler forth.asm build/forth.bin '$0500' || die "Failed to compile forth.bin."
./assembler forth.asm build/extforth.bin '$0500' -Cdisk_ext -Cmath_ext -Cdefer || die "Failed to compile extforth.bin."
./assembler forth.asm build/minforth.bin '$0500' -Cmin || die "Failed to compile minforth.bin."
