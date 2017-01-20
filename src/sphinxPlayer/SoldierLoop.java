package sphinxPlayer;

import battlecode.common.*;

/**
 * Created by Celeron on 1/10/2017.
 */
public class SoldierLoop extends Globals {

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
            MapLocation nearDest = chooseSafeLocation(enemyLoc,2);
            if (!tryMove(nearDest, 30, 1)) {
                Direction random = randomDirection();
                MapLocation nextMove = here.add(random, 1);
                tryMove(nextMove, 45, 1);
            }
            if (visibleEnemies[0].type == RobotType.SCOUT) {
                if (here.distanceTo(enemyLoc) <= sensorRadius && rc.canFireSingleShot()) {
                    here = rc.getLocation();
                    rc.fireSingleShot(here.directionTo(enemyLoc));
                }
            } else if (visibleEnemies.length - visibleAllies.length < 3) {
                if (here.distanceTo(enemyLoc) <= sensorRadius && rc.canFireTriadShot()) {
                    here = rc.getLocation();
                    rc.fireTriadShot(here.directionTo(enemyLoc));
                }
            } else {
                if (here.distanceTo(enemyLoc) <= sensorRadius && rc.canFirePentadShot()) {
                    here = rc.getLocation();
                    rc.firePentadShot(here.directionTo(enemyLoc));
                }
            }
        } else {
            MapLocation toArchons = here;
            if (rc.readBroadcast(Messaging.indexEnemyLocation) != 0){
                toArchons = Messaging.mapLocFromInt(rc.readBroadcast(Messaging.indexEnemyLocation));
                rc.setIndicatorLine(here,toArchons, 200,0,0);
            } else {
                toArchons = theirArchonStartLocs[(int)(Math.random()*archonStartCount)];
            }
            MapLocation farDest = chooseSafeLocation(toArchons,0.5f);
            if (tryMove(farDest, 35,4)) {
                Direction random = randomDirection();
                MapLocation nextMove = here.add(random, 1);
                tryMove(nextMove, 45, 2);
            }
        }
    }
}
