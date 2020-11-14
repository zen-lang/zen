.EXPORT_ALL_VARIABLES:
.PHONY: test

SHELL = bash

repl:
	clj -A:test:nrepl -m nrepl.cmdline

jar:
	clojure -X:depstar jar :jar target/zen.jar

test:
	clj -A:test:kaocha
