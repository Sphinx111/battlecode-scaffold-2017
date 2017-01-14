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
                Clock.yield();
            }
        }
    }

    private static boolean[] treeIDArray = new boolean[32000];
    private static MapLocation[] treeLocArray = new MapLocation[150];
    private static int treesCounted = 0;
    private static MapLocation currentTree;
    private static int index = 0; //Index of currentTree target in treeLocArray
    private static BulletInfo[] nearbyBullets;


    private static void runBehaviour() throws GameActionException {

        MapLocation destination = theirArchonStartCoM;
        nearbyBullets = rc.senseNearbyBullets(9);

        shakeAllTrees();

        if (!rc.hasMoved()) {
            MapLocation safeSpot = chooseSafeLocation(nearbyBullets, destination, 1);
            tryMove(here.directionTo(safeSpot), 45,1);
        }

    }

    private static void addTreesToMemory (TreeInfo[] scannedTrees) {
        TreeInfo[] checkTrees = rc.senseNearbyTrees();
        for (TreeInfo tree : checkTrees) {
            int treeID = tree.getID();
            if (!treeIDArray[treeID]){
                treeIDArray[treeID] = true;
                treeLocArray[treesCounted] = tree.location;
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

        if (here.distanceTo(currentTree) > (strideLength + rc.senseTreeAtLocation(currentTree).radius)) {
            MapLocation nextMove = chooseSafeLocation(nearbyBullets, currentTree, (float)0.5);
            tryMove(here.directionTo(nextMove), 90,1);
        } else {
            if (rc.canShake(currentTree)) {
                rc.shake(currentTree);
                treeLocArray[index] = null;
            }
        }
    }

}


