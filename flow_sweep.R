#
# Run parameter sweeps for the URoad games.
#

# define the sweep
params <- expand.grid(
  statsStart=    60*60,                  # seconds
  statsDuration= 180*60,                 # seconds
  numPoints=     100,                    # for qIn
  rampFlow=      c(0,100,200,300,400),   # vehicles/hour
  speedLimit=    c(40,50,60,70,80),      # mph
  trial=         1:10)

# use a random seed for each row (trial)
params$seed <- sample(2e9,nrow(params),replace=TRUE)

# build commands to run
params$cmd <- with(params, 
  paste('java -cp de.trafficsimulation.game.jar',
    'de.trafficsimulation.game.NoGUITest',
    seed, statsStart, statsDuration,
    1, # one trial per row
    numPoints, rampFlow, speedLimit))

# run commands in parallel; requires CRAN package SNOW
# save intermediate results in case things go wrong; use CRAN package digest
# to get (probably) unique names for the intermediate results files
library(snow)
cl <- makeCluster(4)
clusterEvalQ(cl, library(digest))
res <- do.call(rbind, clusterApplyLB(cl, params$cmd, function(cmd) {
  out.str <- system(cmd, intern=TRUE)
  save(out.str, file=paste('flow_sweep_', digest(cmd), '_tmp.rda', sep=''))
  out.tc <- textConnection(out.str)
  out <- read.table(out.tc, header=TRUE)
  close(out.tc)
  out
}))
stopCluster(cl)

# save results
write.csv(res, file='flow_sweep.csv')
