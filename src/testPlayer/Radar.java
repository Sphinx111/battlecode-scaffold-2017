package testPlayer;
import battlecode.common.*;
/**
 * Created by Celeron on 1/10/2017.
 */
public class Radar extends Globals{
    private static int archons = 0;
    private static int gardeners = 0;
    private static int trees = 0;
    private static int soldiers = 0;
    private static int tanks = 0;
    private static int scouts = 0;
    private static int lumberjacks = 0;
    private static int totalUnitsCount = 0;
    private static int armyStrength = 0;
    private static int economyStrength = 0;

    private static int enemyArchons = 0;
    private static int enemyGardeners = 0;
    private static int enemyTrees = 0;
    private static int enemySoldiers = 0;
    private static int enemyTanks = 0;
    private static int enemyScouts = 0;
    private static int enemyLumberjacks = 0;
    private static int totalEnemyUnitsCount = 0;

    public static float UNKNOWN = -99999;
    public static float minX = UNKNOWN;
    public static float maxX = UNKNOWN;
    public static float minY = UNKNOWN;
    public static float maxY = UNKNOWN;

    private static int neutralTrees = 0;

    // countBuiltUnits counts up type of units in argument, then writes totals to the messages array
    public static void countBuiltUnits(RobotInfo[] sensedUnits) throws GameActionException {
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
        totalUnitsCount = archons + gardeners + soldiers + tanks + scouts + lumberjacks;
        armyStrength = (archons * archonValue) + (gardeners * gardenerValue) + (soldiers * soldierValue) + (tanks * tankValue) + (scouts * scoutValue) + (lumberjacks * lumberjackValue);
        economyStrength = (trees * treeValue) + 20;
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

    public static void detectAndBroadcastMapEdges(int visionRange) throws GameActionException {
        boolean shouldSend = false;
        if (minX == UNKNOWN) {
            Direction west = Direction.getWest();
            if (!rc.onTheMap(here.add(west, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(west, r))) {
                        minX = here.x - r + 1;
                        shouldSend = true;
                        break;
                    }
                }
            }
        }

        if (maxX == UNKNOWN) {
            Direction east = Direction.getEast();
            if (!rc.onTheMap(here.add(east, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(east, r))) {
                        maxX = here.x + r - 1;
                        shouldSend = true;
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
                        break;
                    }
                }
            }
        }

        if (maxY == UNKNOWN) {
            Direction north = Direction.getNorth();
            if (!rc.onTheMap(here.add(north, visionRange))) {
                for (int r = 1; r <= visionRange; ++r) {
                    if (!rc.onTheMap(here.add(north, r))) {
                        maxY = here.y + r - 1;
                        shouldSend = true;
                        break;
                    }
                }
            }
        }
        if (shouldSend) {
            Messaging.sendKnownMapEdges();
        };
    }

}
