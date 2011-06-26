#
# Plot results from flow_sweep script.
#
# USAGE:
# source('flow_sweep_plot.R')
# flow.sweep.plot.for.speed.limit(subset(flow.sweep, rampFlow %in% c(0,400)))
#

library(ggplot2)

# load results
if (!exists('flow.sweep')) {
  flow.sweep <- transform(read.csv(file='flow_sweep_3h_init.csv', row.names=1),
    qOut=numCarsOut / statsDuration * 3600)
}

flow.sweep.plot.for.ramp.flow <- function(sweep) {
  ggplot(sweep, aes(qIn, qOut)) +
    #geom_point(aes(group=as.factor(rampFlow), color=as.factor(rampFlow))) +
    geom_smooth(aes(group=as.factor(rampFlow)), se=TRUE) +
    facet_wrap(~ speedLimit)
}

flow.sweep.plot.for.speed.limit <- function(sweep) {
  ggplot(sweep, aes(qIn, qOut)) +
    geom_point(aes(group=as.factor(speedLimit), color=as.factor(speedLimit)),
      size=1) +
    facet_wrap(~ rampFlow)
}
