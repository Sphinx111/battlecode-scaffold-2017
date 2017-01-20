package sphinxPlayer;

import battlecode.common.*;
import static java.lang.Math.PI;

/**
 * Created by Celeron on 1/10/2017.
 */
public class GardenerLoop extends Globals {

    private static int buildCount = 0;
    private static int treeCount = 0;
    private static boolean reachedBuildPoint = false;
    private static MapLocation buildTarget;
    private static float turnsFailedMove = 0;
    private static boolean buildLocFound = false;
    private static MapLocation spawnLoc;
    private static MapLocation dest;

    public static void loop() throws GameActionException {
        runBehaviour();
    }

    private static void runBehaviour() throws GameActionException {
        buildCount = rc.readBroadcast(Messaging.indexBuildCount);
        commonFunctions();

        if (!reachedBuildPoint) {
            chooseAndMoveToBuildSpot();
            tryMove(chooseSafeLocation(dest, 1),25,5);
        }

        if (reachedBuildPoint) {
            rc.setIndicatorDot(here,25,150,25);
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
        double buildTowardsAngle = here.directionTo(theirArchonStartCoM).radians + (PI/6);
        if (treeCount < 5 && (buildCount % 8 == 2 || buildCount % 8 == 4 || buildCount % 8 == 7)) {
            for (double x = buildTowardsAngle; x < buildTowardsAngle + (2 * PI / 6 * 5); x+= PI/6) {
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
            for (double x = buildTowardsAngle - PI/6; x < buildTowardsAngle + (2 * PI / 6 * 5); x+= PI/6) {
                Direction buildDir = new Direction((float) x);
                if (rc.canBuildRobot(RobotType.LUMBERJACK, buildDir)) {
                    rc.buildRobot(RobotType.LUMBERJACK, buildDir);
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        } else if (buildCount % 8 == 3 || buildCount % 8 == 6){
            for (double x = buildTowardsAngle; x < buildTowardsAngle + (2 * PI / 6 * 5); x+= PI/6) {
                Direction buildDir = new Direction((float)x);
                if (rc.canBuildRobot(RobotType.SOLDIER, buildDir)) {
                    rc.buildRobot(RobotType.SOLDIER, buildDir);
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        } else if (buildCount % 8 == 1 || buildCount %8 == 0) {
            for (double x = buildTowardsAngle; x < buildTowardsAngle + (2 * PI / 6 * 5); x+= PI/6) {
                Direction buildDir = new Direction((float)x);
                if (rc.canBuildRobot(RobotType.SCOUT, buildDir)) {
                    rc.buildRobot(RobotType.SCOUT, buildDir);
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        }
    }

    //tryMove away or roughly at right angles from enemy line of attack, pick a spot if it appears clear of trees, etc.
    //if robot does not find buildLocation after 20 rounds, set up wherever it is already.
    private static void chooseAndMoveToBuildSpot() throws GameActionException{
        if (spawnLoc == null) {
            spawnLoc = here;
        }
        if (buildTarget == null) {
            Direction awayFromEnemy = theirArchonStartCoM.directionTo(ourArchonStartCoM);
            for (int j = 0; j <= 6; j++) {
                Direction checkDir = awayFromEnemy.rotateLeftDegrees(j * 30);
                for (float i = 20; i >= 0; i--) {
                    MapLocation nextCheck = here.add(checkDir, i);
                    if (rc.canSenseAllOfCircle(nextCheck, 2.1f)) {
                            if (rc.onTheMap(nextCheck, 2) && !rc.isCircleOccupiedExceptByThisRobot(nextCheck, 2)) {
                                buildTarget = nextCheck;
                                buildLocFound = true;
                                rc.setIndicatorLine(here, buildTarget, 0, 200, 200);
                                break;
                            }
                    } else {
                        dest = nextCheck;
                        break;
                    }
                }
                if (buildLocFound) {break;}
                Direction checkDir2 = awayFromEnemy.rotateRightDegrees(j * 30);
                for (float i = 20; i >= 0; i--) {
                    MapLocation nextCheck = here.add(checkDir2, i);
                    if (rc.canSenseAllOfCircle(nextCheck, 2)) {
                            if (rc.onTheMap(nextCheck,2) && !rc.isCircleOccupiedExceptByThisRobot(nextCheck, 2)) {
                                buildTarget = nextCheck;
                                buildLocFound = true;
                                rc.setIndicatorLine(here, buildTarget, 0, 200, 200);
                                break;
                            }
                    } else {
                        dest = nextCheck;
                        break;
                    }
                }
                if (buildLocFound) {break;}
            }
            if (buildTarget == null) {
                if (dest != null) {
                    MapLocation nextMove = chooseSafeLocation(dest, 1);
                    if(!tryMove(nextMove, 30,4)){
                        turnsFailedMove += 0.1;
                    }
                }
            }
        } else {
            MapLocation nextMove = chooseSafeLocation(buildTarget, 1);
            if (tryMove(nextMove, 35,3)) {
                if (here.distanceTo(buildTarget) < strideLength) {
                    buildTarget = here;
                    reachedBuildPoint = true;
                }
            }
        }

        if (turnsFailedMove > 5) {
            buildTarget = here;
            reachedBuildPoint = true;
        } else {
            turnsFailedMove += 0.5;
        }

    }

}
