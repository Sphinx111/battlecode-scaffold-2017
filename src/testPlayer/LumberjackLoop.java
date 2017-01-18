package testPlayer;
import battlecode.common.*;
/**
 * Created by Celeron on 1/10/2017.
 */
public class LumberjackLoop extends Globals{

    public static void loop() throws GameActionException {

        while(true) {
            //Code copy pasted from exampleFuncsPlayer
            try {
                Globals.update();
                runBehaviour();
            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static void runBehaviour() throws GameActionException {
        commonFunctions();
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, enemyTeam);
        TreeInfo[] trees = rc.senseNearbyTrees();
        MapLocation dest;
        TreeInfo[] nearbyTrees = rc.senseNearbyTrees(strideLength, Team.NEUTRAL);

        tryShakingTrees(nearbyTrees);

        if (!rc.hasAttacked() && visibleEnemies.length > 0) {
            if (robots.length > 0) {
                // Use strike() to hit all nearby robots!
                rc.strike();
            } else {
                // If there is a robot, move towards it
                if (visibleEnemies.length > 0) {
                    MapLocation enemyLocation = visibleEnemies[0].getLocation();
                    MapLocation nextMove = chooseSafeLocation(enemyLocation, 1);
                    tryMove(nextMove, 25,7);
                    if (!rc.hasAttacked() && visibleEnemies[0].location.distanceTo(rc.getLocation()) < GameConstants.LUMBERJACK_STRIKE_RADIUS) {
                        rc.strike();
                    }
                } else {
                    // Move Randomly
                    Direction k = randomDirection();
                    if (rc.canMove(k) && !rc.hasMoved()) {
                        rc.move(k);
                    }
                }
            }
        }
        if (trees.length > 0 && !rc.hasAttacked()) {
            for (int i = 0; i < trees.length; i++) {
                if (trees[i].getTeam() != myTeam) {
                    TreeInfo target = trees[i];
                    dest = target.getLocation();
                    rc.setIndicatorLine(here, dest, 255, 0, 0);

                    if (here.distanceTo(dest) > (strideLength + target.radius + myType.bodyRadius)) {
                        MapLocation nextMove = chooseSafeLocation(dest, 1);
                        tryMove(nextMove, 35, 4);
                    }
                    if (here.distanceTo(dest) <= (strideLength + target.radius)){
                        if (rc.canChop(target.getID())) {
                            rc.chop(target.getID());
                            break;
                        }
                    }
                }
            }
        }
    }
}
