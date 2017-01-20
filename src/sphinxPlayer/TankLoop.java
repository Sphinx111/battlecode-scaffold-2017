package sphinxPlayer;

import battlecode.common.*;

/**
 * Created by Celeron on 1/10/2017.
 */
public class TankLoop extends Globals {

    private static MapLocation destination;
    private static MapLocation target;


    public static void loop() throws GameActionException {
        runBehaviour();
    }

    private static void runBehaviour() throws GameActionException {
        processSignals();
        commonFunctions();

        TreeInfo[] nearbyTrees = rc.senseNearbyTrees(strideLength, Team.NEUTRAL);
        tryShakingTrees(nearbyTrees);

        if (visibleEnemies.length > 0) {
            MapLocation enemyLoc = visibleEnemies[0].getLocation();
            MapLocation nearDest = chooseSafeLocation(enemyLoc,1);
            if (!tryMove(nearDest, 30,2)) {
                Direction random = randomDirection();
                MapLocation nextMove = here.add(random, 1);
                tryMove(nextMove, 45, 1);
            }
            if (here.distanceTo(enemyLoc) < 6 && rc.canFireSingleShot()) {
                rc.fireSingleShot(here.directionTo(enemyLoc));
            }
        } else {
            MapLocation toArchons = theirArchonStartCoM;
            MapLocation farDest = chooseSafeLocation(toArchons,1);
            if (rc.canMove(farDest)) {
                rc.move(farDest);
            } else {
                Direction random = randomDirection();
                MapLocation nextMove = here.add(random, 1);
                tryMove(nextMove, 45, 2);
            }
        }
    }
}
