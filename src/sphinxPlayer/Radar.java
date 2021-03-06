package sphinxPlayer;

import battlecode.common.Direction;
import battlecode.common.*;
import sphinxPlayer.Globals;
import sphinxPlayer.Messaging;
import testPlayer.ScoutLoop;

import static battlecode.common.Direction.*;

/**
 * Created by Celeron on 1/10/2017.
 */
public class Radar extends Globals {
    private static int archons = 0;
    private static int gardeners = 0;
    private static int trees = 0;
    private static int soldiers = 0;
    private static int tanks = 0;
    private static int scouts = 0;
    private static int lumberjacks = 0;
    private static int armyStrength = 0;
    private static int economyStrength = 0;

    private static int enemyArchons = 0;
    private static int enemyGardeners = 0;
    private static int enemyTrees = 0;
    private static int enemySoldiers = 0;
    private static int enemyTanks = 0;
    private static int enemyScouts = 0;
    private static int enemyLumberjacks = 0;

    public static float UNKNOWN = -99999;
    public static float minX = UNKNOWN;
    public static float maxX = UNKNOWN;
    public static float minY = UNKNOWN;
    public static float maxY = UNKNOWN;

    private static int neutralTrees = 0;

    // countBuiltUnits counts up type of units in argument, then writes totals to the messages array
    public static void countSensedUnits(RobotInfo[] sensedUnits) throws GameActionException {
        for (RobotInfo info : sensedUnits) {
            if (info.type == RobotType.ARCHON) {
                archons++;
            } else if (info.type == RobotType.GARDENER) {
                gardeners++;
            } else if (info.type == RobotType.SOLDIER) {
                soldiers++;
            } else if (info.type == RobotType.TANK) {
                tanks++;
            } else if (info.type == RobotType.SCOUT) {
                scouts++;
            } else if (info.type == RobotType.LUMBERJACK) {
                lumberjacks++;
            }
        }
        armyStrength = (archons * ARCHON_VALUE) + (gardeners * GARDENER_VALUE) + (soldiers * SOLDIER_VALUE) + (tanks * TANK_VALUE) + (scouts * SCOUT_VALUE) + (lumberjacks * LUMBERJACK_VALUE);
    }


    public static int[] getUnitCountsFromRadar() {
        int[] unitCounts = new int[6];
        unitCounts[0] = archons;
        unitCounts[1] = gardeners;
        unitCounts[2] = scouts;
        unitCounts[3] = soldiers;
        unitCounts[4] = tanks;
        unitCounts[5] = lumberjacks;
        return unitCounts;
    }



    public static void addUnitsToRadar() {

    }

    public static MapLocation chooseExploreDirection() {
        MapLocation moveThisWay = here;
        if (minX == UNKNOWN) {
            moveThisWay = new MapLocation(0,here.y);
        } else if (minY == UNKNOWN) {
            moveThisWay = new MapLocation(here.x, 0);
        } else if (maxX == UNKNOWN) {
            moveThisWay = new MapLocation(3000,here.y);
        } else if (maxY == UNKNOWN) {
            moveThisWay = new MapLocation(here.x, 3000);
        } else {
            ScoutLoop.roleAssigned = "";
        }
        return moveThisWay;
    }

    public static void detectAndBroadcastMapEdges(int visionRange) throws GameActionException {
        boolean shouldSend = false;
        if (minX == UNKNOWN) {
            Direction west = getWest();
            if (!rc.onTheMap(here.add(west, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(west, r))) {
                        minX = here.x - r + 1;
                        shouldSend = true;
                        rc.setIndicatorLine(new MapLocation(minX,0), new MapLocation(minX, 800),150,150,150);
                        break;
                    }
                }
            }
        }

        if (maxX == UNKNOWN) {
            Direction east = getEast();
            if (!rc.onTheMap(here.add(east, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(east, r))) {
                        maxX = here.x + r - 1;
                        shouldSend = true;
                        rc.setIndicatorLine(new MapLocation(maxX,0), new MapLocation(maxX, 800),150,150,150);
                        break;
                    }
                }
            }
        }

        if (minY == UNKNOWN) {
            Direction south = Direction.getSouth();
            if (!rc.onTheMap(here.add(south, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(south, r))) {
                        minY = here.y - r + 1;
                        shouldSend = true;
                        rc.setIndicatorLine(new MapLocation(0,minY), new MapLocation(800, minY),150,150,150);
                        break;
                    }
                }
            }
        }

        if (maxY == UNKNOWN) {
            Direction north = getNorth();
            if (!rc.onTheMap(here.add(north, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(north, r))) {
                        maxY = here.y + r - 1;
                        shouldSend = true;
                        rc.setIndicatorLine(new MapLocation(maxY,0), new MapLocation(maxY, 800),150,150,150);
                        break;
                    }
                }
            }
        }
        if (shouldSend) {
            Messaging.sendKnownMapEdges();
        }
    }

}
