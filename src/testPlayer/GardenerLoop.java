package testPlayer;
import battlecode.common.*;

import static java.lang.Math.PI;
import static jdk.nashorn.internal.objects.NativeMath.random;

/**
 * Created by Celeron on 1/10/2017.
 */
public class GardenerLoop extends Globals {

    private static int buildCount = 0;
    private static int treeCount = 0;
    private static boolean reachedBuildPoint = false;
    private static MapLocation buildDest;
    private static int turnsFailedMove = 0;

    public static void loop() {
        while (true) {
            try {
                Globals.update();
                runBehaviour();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Clock.yield();
        }
    }

    private static void runBehaviour() throws GameActionException {
        buildCount = rc.readBroadcast(Messaging.indexBuildCount);
        BulletInfo[] nearbyBullets = rc.senseNearbyBullets(8);

        if (!reachedBuildPoint) {
            MapLocation buildLoc = chooseBuildSpot();
            buildDest = buildLoc;
            MapLocation nextMove = chooseSafeLocation(nearbyBullets, buildDest, 1);
            if (!tryMove(here.directionTo(nextMove), 20, 6)) {
                turnsFailedMove++;
            }
            if (here.distanceTo(buildDest) < strideLength * 2 || turnsFailedMove > 5) {
                reachedBuildPoint = true;
            }
            if (turnsFailedMove >= 5) {
                reachedBuildPoint = true;
            }
        }

        if (reachedBuildPoint) {
            tryBuild();
            tryWater();
        }


    }

    private static void tryWater() throws GameActionException {

        TreeInfo treeWaterTarget;
        TreeInfo[] visibleTrees = rc.senseNearbyTrees(strideLength + 1, myTeam);
        if (visibleTrees.length > 0) {
            for (int i = 0; i < visibleTrees.length; i++) {
                if (visibleTrees[i].getHealth() < 44) {
                    treeWaterTarget = visibleTrees[i];
                    if (rc.canWater(treeWaterTarget.getID())) {
                        rc.water(treeWaterTarget.getID());
                        break;
                    }
                }
            }
        }



    }

    private static void tryBuild() throws GameActionException {

        if (treeCount < 5 && (buildCount % 8 == 2 || buildCount % 8 == 4 || buildCount % 8 == 7)) {
            for (double x = 0; x < (2 * PI / 6 * 5); x+= PI/6) {
                Direction buildDir = new Direction((float)x);
                if (rc.canPlantTree(buildDir)) {
                    rc.plantTree(buildDir);
                    buildCount++;
                    treeCount++;
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        } else if (buildCount % 8 == 5) {
            for (double x = (2 * PI); x >= 0; x -= PI / 6) {
                Direction buildDir = new Direction((float) x);
                if (rc.canBuildRobot(RobotType.LUMBERJACK, buildDir)) {
                    rc.buildRobot(RobotType.LUMBERJACK, buildDir);
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        } else if (buildCount % 8 == 1 || buildCount % 8 == 6){
            for (double x = (2 * PI); x >= 0; x-= PI / 6) {
                Direction buildDir = new Direction((float)x);
                if (rc.canBuildRobot(RobotType.SOLDIER, buildDir)) {
                    rc.buildRobot(RobotType.SOLDIER, buildDir);
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        } else if (buildCount % 8 == 3 || buildCount % 8 == 0) {
            for (double x = (2 * PI); x >= 0; x-= PI / 6) {
                Direction buildDir = new Direction((float)x);
                if (rc.canBuildRobot(RobotType.SCOUT, buildDir)) {
                    rc.buildRobot(RobotType.SCOUT, buildDir);
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        }
    }

    private static MapLocation chooseBuildSpot() throws GameActionException{
        MapLocation buildTarget = here;
        Direction toBuildLoc;
        if (buildDest == null) {
            Direction hereToMid = here.directionTo(mapStartCoM);
            if (Math.random() < 0.5) {
                toBuildLoc = hereToMid.rotateLeftDegrees(45);
            } else {
                toBuildLoc = hereToMid.rotateRightDegrees(45);
            }

            float distance = (float)((Math.random()) * 15) + 5;
            buildTarget = here.add(toBuildLoc, distance);
        } else {
            buildTarget = buildDest;
            rc.setIndicatorLine(here,buildTarget,150,0,150);
        }
        return buildTarget;
    }

}
