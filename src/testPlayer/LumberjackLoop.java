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
        // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
        RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS, enemyTeam);
        TreeInfo[] trees = rc.senseNearbyTrees();
        BulletInfo[] nearbyBullets = rc.senseNearbyBullets(6);
        MapLocation dest;
        TreeInfo[] nearbyTrees = rc.senseNearbyTrees(strideLength, Team.NEUTRAL);

        tryShakingTrees(nearbyTrees);

        if (trees.length > 0) {
            for (int i = 0; i < trees.length; i++) {
                if (trees[i].getTeam() != myTeam) {
                    TreeInfo target = trees[i];
                    dest = target.getLocation();
                    rc.setIndicatorLine(here, dest, 255, 0, 0);

                    if (here.distanceTo(dest) > (strideLength + target.radius + myType.bodyRadius)) {
                        dest = chooseSafeLocation(nearbyBullets, dest, 1);
                        if (tryMove(here.directionTo(dest), 45, 3)) {
                            break;
                        }

                    } else {
                        if (rc.canChop(target.getID())) {
                            rc.chop(target.getID());
                            break;
                        }
                    }
                }
            }
        }
        if (!rc.hasAttacked()) {
            if (robots.length > 0) {
                // Use strike() to hit all nearby robots!
                rc.strike();
            } else {
                // No trees or close robots, so search for robots within sight radius
                robots = rc.senseNearbyRobots(-1, enemyTeam);

                // If there is a robot, move towards it
                if (robots.length > 0) {
                    MapLocation enemyLocation = robots[0].getLocation();
                    Direction toEnemy = here.directionTo(enemyLocation);
                    if (rc.canMove(toEnemy) && !rc.hasMoved()) {
                        rc.move(toEnemy);
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
    }
}
