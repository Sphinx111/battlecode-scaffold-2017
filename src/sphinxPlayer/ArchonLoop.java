package sphinxPlayer;

import battlecode.common.*;

/**
 * Created by Celeron on 1/10/2017.
 */
public class ArchonLoop extends Globals {

    private static MapLocation currentDest;

    public static void loop () throws GameActionException{

        runBehaviour();

    }

    private static void runBehaviour() throws GameActionException {
        processSignals();

        neutralTrees = rc.senseNearbyTrees(sensorRadius, Team.NEUTRAL);

        //If unit counts on messageboard are more than 1 turn out of date, update them.
        /*if (rc.readBroadcast(Messaging.indexLastUpdateToUnitCounts) < rc.getRoundNum() -1) {
            Radar.countBuiltUnits(visibleAllies);
            int[] unitsKnown = Radar.getUnitCountsFromRadar();
            int unitsMask = Messaging.intFromUnitCounts(unitsKnown);
            Messaging.postUnitCounts(myTeam, unitsMask);
            Messaging.postIntelAge();
        }*/

        tryShakingTrees(neutralTrees);

        tryBuild();

        chooseDest();

    }

    //If game conditions are right (constants set in MutableGameData), build a Gardener
    private static void tryBuild() throws GameActionException {
        if ((buildCount % 8 == 0 || rc.readBroadcast(Messaging.indexGardenerCount) < 1) && rc.hasRobotBuildRequirements(RobotType.GARDENER)) {
            Direction awayEnemy = theirArchonStartCoM.directionTo(ourArchonStartCoM);
            for (int x = 0; x < 6; x++) {
                Direction newDir = awayEnemy.rotateLeftDegrees(x * 60);
                if (rc.canHireGardener(newDir)) {
                    rc.hireGardener(newDir);
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
        if (currentDest == null ) {
            int rallyInt = rc.readBroadcast(Messaging.indexRallyHere);
            if (rallyInt != 0) {
                if (roundNum - Messaging.mapLocAgeFromInt(rallyInt) < 50) {
                    currentDest = Messaging.mapLocFromInt(rallyInt);
                }
            } else {
                currentDest = ourArchonStartCoM;
            }
        }
        MapLocation nextMove = chooseSafeLocation(currentDest, 1);
        tryMove(nextMove, 35,3);
    }

}

