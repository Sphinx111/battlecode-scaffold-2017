package testPlayer;
import battlecode.common.*;
/**
 * Created by Celeron on 1/10/2017.
 */
public class TankLoop extends Globals {

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
