package testPlayer;
import battlecode.common.*;
/**
 * Created by Celeron on 1/10/2017.
 */
public class ScoutLoop extends Globals{

    public static void loop() {
        while(true) {
            try {
                Globals.update();
                runBehaviour();
                Clock.yield();
            } catch (Exception e) {
                try {rc.setIndicatorDot(here,50,50,50);}
                catch (Exception E) {
                    E.printStackTrace();
                }
                Clock.yield();
            }
        }
    }

    public static String roleAssigned;
    private static boolean[] treeIDArray = new boolean[32000];
    private static MapLocation[] treeLocArray = new MapLocation[150];
    private static int treesCounted = 0;
    private static MapLocation currentTree;
    private static int index = 0; //Index of currentTree target in treeLocArray
    private static boolean mapEdgesKnown = false;


    private static void runBehaviour() throws GameActionException {
        commonFunctions();
        MapLocation destination = theirArchonStartCoM;
        if (roleAssigned == null) {
            chooseScoutRole();
        }

        if (roleAssigned == "mapEdgeFinder") {
            Messaging.processMapEdges(Messaging.indexMapEdges);
            destination = chooseSafeLocation(Radar.chooseExploreDirection(), 1);
            tryMove(destination, 30,4);
            Radar.detectAndBroadcastMapEdges((int)sensorRadius);
        } else if (roleAssigned == "patrolAndReport") {
            TreeInfo[] addTrees = rc.senseNearbyTrees(sensorRadius, Team.NEUTRAL);
            addTreesToMemory(addTrees);
            shakeAllTrees();
        } else if (roleAssigned == "gardenerHunter") {
            MapLocation target = findGardeners();
            if (target != here) {
                MapLocation safeSpot = findTreeAdjToGardener(target);
                if (here.distanceSquaredTo(safeSpot) > strideLength) {
                    safeSpot = chooseSafeLocation(safeSpot, 20);
                }
                if (rc.canMove(safeSpot)) {
                    rc.move(safeSpot);
                }
                here = rc.getLocation();
                if (rc.canFireSingleShot() && here.distanceTo(target) <= (RobotType.GARDENER.bodyRadius + RobotType.SCOUT.bodyRadius + 0.01f)) {
                    rc.fireSingleShot(here.directionTo(target));
                }
            } else {
                TreeInfo[] addTrees = rc.senseNearbyTrees(sensorRadius, Team.NEUTRAL);
                addTreesToMemory(addTrees);
                shakeAllTrees();
            }
        }
        if (!rc.hasMoved()) {
            MapLocation nextMove = chooseSafeLocation(destination, 0.5f);
            tryMove(nextMove, 30, 4);
            if (here.distanceTo(theirArchonStartCoM) < 3) {
                roleAssigned = "gardenerHunter";
            }
        }
        if (!rc.hasAttacked() && visibleEnemies.length > 0) {
            here = rc.getLocation();
            for (RobotInfo enemy : visibleEnemies) {
                if (enemy.type != RobotType.ARCHON || roundNum > (rc.getRoundLimit() - 500)) {
                    if (enemy.location.distanceTo(here) < 5 && rc.canFireSingleShot()) {
                        rc.fireSingleShot(here.directionTo(enemy.location));
                        break;
                    }
                }
            }
        }
        if (visibleEnemies.length > 0) {
            RobotInfo archonSeen;
            for (RobotInfo enemy : visibleEnemies) {
                if (enemy.type == RobotType.ARCHON) {
                    int encodedLoc = Messaging.encodeMapLocation(enemy.location);
                    rc.broadcast(Messaging.indexEnemyLocation, encodedLoc);
                    break;
                } else if (enemy.type == RobotType.GARDENER) {
                    int encodedLoc = Messaging.encodeMapLocation(enemy.location);
                    rc.broadcast(Messaging.indexEnemyLocation, encodedLoc);
                }
            }
        }

    }

    private static MapLocation findGardeners() {
        for (RobotInfo robot : visibleEnemies){
            if (robot.type == RobotType.GARDENER) {
                return robot.location;
            }
        }
        return here;
    }

    private static MapLocation findTreeAdjToGardener(MapLocation gardenerLoc) {
        TreeInfo[] nearbyTrees = rc.senseNearbyTrees(sensorRadius, enemyTeam);
        float nearestDist = 9999;
        MapLocation nearestTree = gardenerLoc;
        for (TreeInfo tree : nearbyTrees) {
            if (gardenerLoc.distanceTo(tree.location) < nearestDist) {
                nearestDist = gardenerLoc.distanceTo(tree.location);
                nearestTree = tree.location;
            }
        }
        return nearestTree;
    }

    private static void addTreesToMemory (TreeInfo[] checkTrees) {

        for (TreeInfo tree : checkTrees) {
            int treeID = tree.getID();
            if (!treeIDArray[treeID] && tree.getContainedBullets() > 0){
                treeIDArray[treeID] = true;
                treeLocArray[treesCounted] = tree.location;
                ++treesCounted;
            }
        }
    }

    private static void shakeAllTrees () throws GameActionException {
        if (currentTree == null) {
            for (index = 0; index < treeLocArray.length; index++) {
                if (treeLocArray[index] != null) {
                    currentTree = treeLocArray[index];
                    rc.setIndicatorLine(here, currentTree, 0,50,200);
                    break;
                }
            }
        }

        if (currentTree != null) {
            if (here.distanceTo(currentTree) > (strideLength + 1)) {
                MapLocation nextMove = chooseSafeLocation(currentTree, 1);
                tryMove(nextMove, 90, 1);
            } else {
                if (rc.canShake(currentTree)) {
                    rc.shake(currentTree);
                }
                currentTree = null;
                treeLocArray[index] = null;
            }
        }
    }

    private static void chooseScoutRole() throws GameActionException {
        if (Radar.minX == Radar.UNKNOWN || Radar.minY == Radar.UNKNOWN || Radar.maxX == Radar.UNKNOWN || Radar.maxY == Radar.UNKNOWN){
            roleAssigned = "mapEdgeFinder";
            return;
        } else {
            mapEdgesKnown = true;
        }
        if (roundNum < 250 || myUnitCount % 3 == 0) {
            roleAssigned = "gardenerHunter";
            return;
        } else if (myUnitCount % 3 == 1 || myUnitCount %3 ==2){
            roleAssigned = "patrolAndReport";
            return;
        }

    }

}


