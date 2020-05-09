RAX = ./bin/rax

all: bootldr.bin forth.bin extforth.bin minforth.bin

bootldr.bin:
	$(RAX) -o build/bootldr.bin bootldr.S

forth.bin:
	$(RAX) -o build/forth.bin forth.S

extforth.bin:
	$(RAX) -o build/extforth.bin -Ddisk_ext -Dmath_ext -Ddefer forth.S

minforth.bin:
	$(RAX) -o build/minforth.bin -Dmin forth.S

clean:
	$(RM) *.bin
