.EXPORT_ALL_VARIABLES:
.PHONY: test

SHELL = bash

repl:
	clj -A:test -M:nrepl

jar:
	clojure -X:depstar jar :jar target/zen.jar

pom:
	clj -X:deps mvn-pom

test:
  ## TODO rewrite dev test to always finish correctly
	clj -A:test:kaocha --skip zen.dev-test

test-ci:
	clojure -A:test:kaocha --skip zen.dev-test --skip zen.package-test

pub:
	clj -A:deploy

build:
	clj -T:build-pm uber
