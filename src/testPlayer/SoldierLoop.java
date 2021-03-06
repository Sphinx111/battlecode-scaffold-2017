package testPlayer;
import battlecode.common.*;
/**
 * Created by Celeron on 1/10/2017.
 */
public class SoldierLoop extends Globals {

    private static MapLocation destination;
    private static MapLocation target;


    public static void loop() {
        while (true) {
            int startTurn = rc.getRoundNum();
            try {
                Globals.update();
                runBehaviour();
            } catch (Exception e) {
                e.printStackTrace();
            }
            int endTurn = rc.getRoundNum();
            if (startTurn != endTurn) {
                System.out.println(myID + " OVER BYTECODE LIMIT!");
            }
            Clock.yield();
        }

    }

    private static void runBehaviour() throws GameActionException {
        // processSignals();
        //FastMath.initRand(rc);
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


    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

}
