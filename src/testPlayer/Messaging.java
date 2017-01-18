package testPlayer;
import battlecode.common.*;

public class Messaging extends Globals{
    //1000 channels available for messaging.
    public static int indexDefendArchon = 1;
    public static int indexAttackHere = 2;
    public static int indexRallyHere = 3;
    public static int indexBuildCount = 4;
    private static int indexLastLeaderSignal = 0;

    public static int indexLastUpdateToUnitCounts = 24;
    public static int indexUnitCounts = 25;
    public static int indexTreeCount = 26;

    public static int indexEnemyLocation = 27;
    public static int indexEnemyUnitCounts = 28;
    public static int indexEnemyTreeCount = 29;
    public static int indexEnemyTotalUnitCount = 30;

    public static int indexArchonCount = 31;
    public static int indexGardenerCount = 32;
    public static int indexSoldierCount = 33;
    public static int indexTankCount = 34;
    public static int indexScoutCount = 35;
    public static int indexLumberjackCount = 36;



    public static int indexNeutralTreeCount = 40;
    public static int indexEconomyStrength = 41;

    public static int indexMapEdges = 42;


    public static void leaderElection() throws GameActionException {
        if (rc.readBroadcast(indexLastLeaderSignal) < roundNum) {
            iAmLeader = true;
            rc.broadcast(indexLastLeaderSignal, roundNum);
        }
    }

    public static void raiseHandForUnitCounts() throws GameActionException {
        if (rc.readBroadcast(indexLastUpdateToUnitCounts) < roundNum) {
            rc.broadcast(indexLastUpdateToUnitCounts, roundNum);
            rc.broadcast(indexArchonCount, 1);
            rc.broadcast(indexGardenerCount, 1);
            rc.broadcast(indexSoldierCount, 1);
            rc.broadcast(indexTankCount, 1);
            rc.broadcast(indexScoutCount, 1);
            rc.broadcast(indexLumberjackCount, 1);
        } else {
            if (myType == RobotType.ARCHON) {
                rc.broadcast(indexArchonCount, rc.readBroadcast(indexArchonCount) + 1);
            } else if (myType == RobotType.GARDENER) {
                rc.broadcast(indexGardenerCount, rc.readBroadcast(indexGardenerCount) + 1);
            } else if (myType == RobotType.SOLDIER) {
                rc.broadcast(indexSoldierCount, rc.readBroadcast(indexSoldierCount) + 1);
            } else if (myType == RobotType.TANK) {
                rc.broadcast(indexTankCount, rc.readBroadcast(indexTankCount) + 1);
            } else if (myType == RobotType.SCOUT) {
                rc.broadcast(indexScoutCount, rc.readBroadcast(indexScoutCount) + 1);
            } else if (myType == RobotType.LUMBERJACK) {
                rc.broadcast(indexLumberjackCount, rc.readBroadcast(indexLumberjackCount) + 1);
            }
        }
    }

    public static int getMyUnitCount()throws GameActionException {
        int unitCount = 1;
        if (myType == RobotType.ARCHON) {
            unitCount = rc.readBroadcast(Messaging.indexArchonCount);
        } else if (myType == RobotType.GARDENER) {
            unitCount = rc.readBroadcast(Messaging.indexGardenerCount);
        } else if (myType == RobotType.SOLDIER) {
            unitCount = rc.readBroadcast(Messaging.indexSoldierCount);
        } else if (myType == RobotType.TANK) {
            unitCount = rc.readBroadcast(Messaging.indexTankCount);
        } else if (myType == RobotType.SCOUT) {
            unitCount = rc.readBroadcast(Messaging.indexScoutCount);
        } else if (myType == RobotType.LUMBERJACK) {
            unitCount = rc.readBroadcast(Messaging.indexLumberjackCount);
        }

        return unitCount;
    }

    //encode unit counts
    //Counts friendly Archons up to 15, Gardeners up to 127
    //Counts soldiers (<255), tanks(<255), scouts (<127)
    //lumberjacks(<255)
    public static int intFromUnitCounts(int[] unitsCounted) {
        //AAAGGGGGGScScScScScScSSSSSSSTTTTTTLLLLLL
        int archons = unitsCounted[0];
        int gardeners = unitsCounted[1];
        int scouts = unitsCounted[2];
        int soldiers = unitsCounted[3];
        int tanks = unitsCounted[4];
        int lumberjacks = unitsCounted[5];
        if (archons > 15) {archons = 15;}
        if (gardeners > 255) {gardeners = 255;}
        if (scouts > 127) {scouts = 127;}
        if (soldiers > 255) {soldiers = 255;}
        if (tanks > 255) {tanks = 255;}
        if (lumberjacks > 255) {lumberjacks = 255;}
        return (archons << 29) | (gardeners << 26) | (scouts << 21) | (soldiers << 14) | (tanks << 7) | (lumberjacks);
    }

    public static int[] unitCountsFromInt(int data) {
        int[] unitCounts = new int[6];
        unitCounts[5] = (data & 0x07F); //lumberjacks
        unitCounts[4] = ((data & 0x07F) >>> 7); //tanks
        unitCounts[3] = ((data & 0x07F) >>> 7); //soldiers
        unitCounts[2] = ((data & 0x40) >>> 7); //scouts
        unitCounts[1] = ((data & 0x40) >>> 6); //Gardeners
        unitCounts[0] = ((data & 0x8) >>> 6); //archons
        return unitCounts;
    }


    //posts unit counts to messageboard, costs 20Bytecode.
    public static void postEncodedUnitCounts(Team team, int unitsMaskToSend) throws GameActionException{
        int channelToUse = 999;
        int unitsToPost = unitsMaskToSend;
        if (team == myTeam) {
            channelToUse = indexUnitCounts;
        } else if (team == enemyTeam) {
            channelToUse = indexEnemyUnitCounts;
        }
        rc.broadcast(channelToUse, unitsToPost);
    }

    public static void postIntelAge() throws GameActionException{
        rc.broadcast(indexLastUpdateToUnitCounts, roundNum);
    }

    public static int[] getUnitCounts(Team team) throws GameActionException{
        int[] unitsReceived = new int[6];
        if (team == myTeam) {
            int unitsMask = rc.readBroadcast(indexUnitCounts);
            unitsReceived = unitCountsFromInt(unitsMask);
        } else if (team == enemyTeam) {
            int unitsMask = rc.readBroadcast(indexEnemyUnitCounts);
            unitsReceived = unitCountsFromInt(unitsMask);
        }
        return unitsReceived;
    }
    //compressionStart
    private static int compressMapEdges(int original) {
        if (original == Radar.UNKNOWN) {
            return 127;
        } else {
            // max map size is 100 < 110
            return (original + 33000) % 110;
        }
    }

    private static float parseMapEdgesMax(int compressed, int reference) {
        if (compressed == 127) return Radar.UNKNOWN;
        int original = (reference / 110 + 1) * 110 + compressed;
        while (original >= reference) {
            original -= 110;
        }
        return original + 110;
    }

    private static int parseMapEdgesMin(int compressed, int reference) {
        if (compressed == 127) return (int)Radar.UNKNOWN;
        int original = (reference / 110 + 1) * 110 + compressed;
        while (original > reference) {
            original -= 110;
        }
        return original;
    }

    public static void sendKnownMapEdges() throws GameActionException {
        int minX = compressMapEdges((int)Radar.minX);
        int maxX = compressMapEdges((int)Radar.maxX);
        int minY = compressMapEdges((int)Radar.minY);
        int maxY = compressMapEdges((int)Radar.maxY);
        int value = minX << 24 | maxX << 16 | minY << 8 | maxY;
        rc.broadcast(indexMapEdges, value);
//		Debug.indicate("edges", 1, "send: value=" + Integer.toHexString(value) + " MinX=" + MapEdges.minX + " MaxX=" + MapEdges.maxX + " MinY=" + MapEdges.minY + " MaxY=" + MapEdges.maxY);
//		Debug.indicate("msg", msgDILN(), "sendKnownMapEdges " + radiusSq);
    }

    public static int encodeMapLocation(MapLocation loc) {
        int xVal = (int)(Math.floor(loc.x));
        int yVal = (int)(Math.floor(loc.y));
        //max x/y values are (500+500) = 1,000
        return yVal << 10 | xVal;
    }

    public static MapLocation mapLocFromInt(int data) {
        int xVal = 0x03FF & data;
        data >>>= 10;
        int yVal = 0x03FF & data;
        MapLocation newLoc = new MapLocation(xVal,yVal);
        return newLoc;
    }

    private static int parseInt(int[] data) {
        return data[1];
    }

    public static void processMapEdges(int data) {
        int value = data;
        int maxY = value & 0x000000ff;
        value >>>= 8;
        int minY = value & 0x000000ff;
        value >>>= 8;
        int maxX = value & 0x000000ff;
        value >>>= 8;
        int minX = value & 0x000000ff;
        if (Radar.minX == Radar.UNKNOWN) Radar.minX = parseMapEdgesMin(minX, (int)here.x);
        if (Radar.maxX == Radar.UNKNOWN) Radar.maxX = parseMapEdgesMax(maxX, (int)here.x);
        if (Radar.minY == Radar.UNKNOWN) Radar.minY = parseMapEdgesMin(minY, (int)here.y);
        if (Radar.maxY == Radar.UNKNOWN) Radar.maxY = parseMapEdgesMax(maxY, (int)here.y);
//		Debug.indicate("edges", 2, "receive: value=" + Integer.toHexString(parseInt(data)) + " MinX=" + MapEdges.minX + " MaxX=" + MapEdges.maxX + " MinY=" + MapEdges.minY + " MaxY=" + MapEdges.maxY);
    }

}
