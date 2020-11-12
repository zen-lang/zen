.EXPORT_ALL_VARIABLES:
.PHONY: test

SHELL = bash

repl:
	clj -A:test:nrepl -m nrepl.cmdline

test:
	clj -A:test:kaocha
