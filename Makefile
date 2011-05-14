# 
# Very simple Makefile for building and releasing.
#

JAR := de.trafficsimulation.game.jar

# default target: build jar and run the app with terminal output
.PHONY: run
run: $(JAR)
	java -jar $<

# compile all source
# omit tests to avoid having to deal with JUnit dependency
build:
	mkdir -p bin
	find src -name *.java | grep -v TestUtility | xargs javac -d bin

# build executable jar file for distribution
$(JAR): build
	jar cfe $@ de.trafficsimulation.game.MainFrame -C bin .

clean:
	rm -rf bin

