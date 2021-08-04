.EXPORT_ALL_VARIABLES:
.PHONY: test

SHELL = bash

repl:
	echo "Use clojure jack-in instead"
	clj -M:nrepl:test:build -m nrepl.cmdline --middleware [cider.nrepl/cider-middleware]

jar:
	clojure -X:depstar jar :jar target/zen.jar

pom:
	clj -X:deps mvn-pom

test:
	clj -A:test:kaocha

pub:
	clj -A:deploy
