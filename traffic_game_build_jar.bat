@REM 
@REM NB: must build current in eclipse before building the jar
@REM NB: eclipse puts the resource folder into the bin path for us, so we don't
@REM     have to copy it in here, like we do in the manual build
@REM

jar cfe de.trafficsimulation.game.jar de.trafficsimulation.game.MainFrame -C bin .

