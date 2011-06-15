# 
# Very simple Makefile for building and releasing.
#

JAR := de.trafficsimulation.game.jar

# default target: build jar and run the app with terminal output
.PHONY: run
run: $(JAR)
	java -jar $<

#
# parameter sweep on qIn for the flow game; to plot in R:
#
# dat <- read.table('flow_game_scores.txt', header=TRUE)
# pdf('flow_game_scores.pdf');
# with(dat, plot(qIn, 3600 * numCarsOut / statsDuration,
#   ylab="vehicles out per hour", xlab="vehicles in per hour",
#   pch=42, col='gray'));
# lines(aggregate(3600 * numCarsOut / statsDuration ~ qIn, mean, data=dat)); 
# dev.off()
# 
flow_game_scores.txt: $(JAR)
	java -cp $< de.trafficsimulation.game.NoGUITest >$@

# compile all source
# omit tests to avoid having to deal with JUnit dependency
build:
	mkdir -p bin
	find src -name *.java | grep -v TestUtility | xargs javac -d bin

# build executable jar file for distribution
$(JAR): build
	cp -r src/de/trafficsimulation/game/res bin/de/trafficsimulation/game/res
	jar cfe $@ de.trafficsimulation.game.MainFrame -C bin .

clean:
	rm -rf bin

