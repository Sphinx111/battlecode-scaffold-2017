package testPlayer;
import battlecode.common.*;

import static java.lang.Math.PI;

/**
 * Created by Celeron on 1/10/2017.
 */
public class ArchonLoop extends Globals {

    private static MapLocation startLocation;
    private static MapLocation currentDest;
    private static Direction currentHeading;
    private static boolean isLeader;
    private static BulletInfo[] nearbyBullets;

    private static TreeInfo[] knownTreesHoldingArchons;
    private static int buildCount = 0;


    public static void loop () {
        //Initialise optimised random function
        FastMath.initRand(rc);

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
        //TODO: processSignals();
        buildCount = rc.readBroadcast(Messaging.indexBuildCount);

        //TODO: Check map edges here if game is underway (20-50 turns)

        visibleEnemies = rc.senseNearbyRobots(sensorRadius, enemyTeam);
        visibleAllies = rc.senseNearbyRobots (sensorRadius, myTeam);
        neutralTrees = rc.senseNearbyTrees(sensorRadius, Team.NEUTRAL);
        nearbyBullets = rc.senseNearbyBullets(9);

        //If unit counts on messageboard are more than 1 turn out of date, update them.
        /*if (rc.readBroadcast(Messaging.indexLastUpdateToUnitCounts) < rc.getRoundNum() -1) {
            Radar.countBuiltUnits(visibleAllies);
            int[] unitsKnown = Radar.getUnitCountsFromRadar();
            int unitsMask = Messaging.intFromUnitCounts(unitsKnown);
            Messaging.postUnitCounts(myTeam, unitsMask);
            Messaging.postIntelAge();
        }*/

        tryShakingTrees(neutralTrees);

        //TODO: send radarInfo to messaging arrays
        tryBuild();

        startLocation = here;
        chooseDest();

        if (rc.getTeamBullets() > 10000) {
            rc.donate(10000);
        }

    }

    //If game conditions are right (constants set in MutableGameData), build a Gardener
    private static void tryBuild() throws GameActionException {
        if (buildCount % 8 == 0 && rc.hasRobotBuildRequirements(RobotType.GARDENER)) {
            for (int x = 0; x < 4; x++) {
                if (rc.canHireGardener(dirs[x])) {
                    rc.hireGardener(dirs[x]);
                    ++buildCount;
                    rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
                    break;
                }
            }
        }
        if (roundNum > 100 && rc.getTeamBullets() > 150) {
            ++buildCount;
            rc.broadcast(Messaging.indexBuildCount, 1 + rc.readBroadcast(Messaging.indexBuildCount));
        }
    }

    private static void chooseDest() throws GameActionException {
        if (currentDest != null ) {
            if (currentDest == here) {
                currentDest = null;
                currentHeading = null;
            }
        } else {
            currentDest = ourArchonStartCoM;
        }


        MapLocation nextMove = chooseSafeLocation(nearbyBullets, currentDest, 1);
        tryMove(here.directionTo(nextMove),10,1);


    }

}

