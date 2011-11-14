#
# Plot results from flow_sweep script.
#
# USAGE:
# source('flow_sweep_plot.R')
# flow.sweep.plot.for.speed.limit(subset(flow.sweep, qIn > 1000 & rampFlow %in% c(0,400)))
# flow.sweep.plot.for.ramp.flow(subset(flow.sweep, qIn > 1000 & speedLimit %in% c(50,70)))

library(ggplot2)

# load results
if (!exists('flow.sweep')) {
  flow.sweep.1 <- read.csv(file='flow_sweep_3h.csv', row.names=1)
  flow.sweep.2 <- read.csv(file='flow_sweep_3h_2.csv', row.names=1)
  flow.sweep <- transform(rbind(flow.sweep.1, flow.sweep.2),
    qOut=numCarsOut / statsDuration * 3600)
}

flow.sweep.plot.for.ramp.flow <- function(sweep) {
  ggplot(sweep, aes(qIn, qOut)) +
    geom_point(aes(group=as.factor(rampFlow), color=as.factor(rampFlow)),
      size=1) +
    stat_summary(aes(group=as.factor(rampFlow), color=as.factor(rampFlow)),
      fun.y=median, geom='line') +
    facet_wrap(~ speedLimit)
}

flow.sweep.plot.for.speed.limit <- function(sweep) {
  ggplot(sweep, aes(qIn, qOut)) +
    geom_point(aes(group=as.factor(speedLimit), color=as.factor(speedLimit)),
      size=1) +
    stat_summary(aes(group=as.factor(speedLimit), color=as.factor(speedLimit)),
      fun.y=median, geom='line') +
    facet_wrap(~ rampFlow)
}

#
# some extra data obtained around the peak for varying speed limits:
#
#flow.sweep.peak <- transform(read.csv(file='flow_sweep_peak.csv', row.names=1), qOut=numCarsOut / statsDuration * 3600)
#flow.sweep.peak.orig <- transform(read.csv(file='flow_sweep_peak_orig.csv', row.names=1), qOut=numCarsOut / statsTime * 3600)
#flow.sweep.peak$src <- 'new'
#flow.sweep.peak.orig$src <- 'old'
#dat.names <- c('qIn', 'rampFlow', 'speedLimit', 'qOut', 'src')
#dat <- rbind(flow.sweep.peak[,dat.names], flow.sweep.peak.orig[,dat.names])
#library(ggplot2)
# ggplot(dat, aes(qIn, qOut)) +
#    geom_point(aes(group=as.factor(speedLimit), color=as.factor(speedLimit)),
#      size=1) +
#    stat_summary(aes(group=as.factor(speedLimit), color=as.factor(speedLimit)),
#      fun.y=median, geom='line') +
#    facet_wrap(~ src)

