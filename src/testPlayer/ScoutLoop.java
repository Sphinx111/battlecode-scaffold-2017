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


    private static void runBehaviour() throws GameActionException {
        commonFunctions();

        MapLocation destination = theirArchonStartCoM;

        TreeInfo[] addTrees = rc.senseNearbyTrees(sensorRadius, Team.NEUTRAL);
        addTreesToMemory(addTrees);
        shakeAllTrees();

        if (!rc.hasMoved()) {
            MapLocation safeSpot = chooseSafeLocation(destination, 1);
            tryMove(safeSpot, 45,2);
        }

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

}


