package sphinxPlayer;

import battlecode.common.*;

import static java.lang.Math.PI;

/**
 * Created by Celeron on 1/10/2017.
 */
public class Globals {
    public static int archonStartCount;
    public static MapLocation[] ourArchonStartLocs;
    public static MapLocation[] theirArchonStartLocs;
    public static MapLocation ourArchonStartCoM;
    public static MapLocation theirArchonStartCoM;
    public static MapLocation mapStartCoM;

    public static RobotController rc;
    public static MapLocation here;
    public static Team myTeam;
    public static Team enemyTeam;
    public static int myID;
    public static RobotType myType;
    public static boolean iAmLeader = false;
    public static int myUnitCount = 1;

    public static MapLocation enemyTarget;
    public static int enemyTargetAge;

    public static float sensorRadius;
    public static float strideLength;

    public static int roundNum;
    public static int buildCount;
    public static RobotInfo[] visibleEnemies;
    public static RobotInfo[] visibleAllies;
    public static BulletInfo[] nearbyBullets;
    public static TreeInfo[] adjacentTrees;
    public static TreeInfo[] neutralTrees;

    public static int SOLDIER_VALUE = 2;
    public static int TANK_VALUE = 4;
    public static int SCOUT_VALUE = 1;
    public static int ARCHON_VALUE = 0;
    public static int GARDENER_VALUE = 0;
    public static int LUMBERJACK_VALUE = 1;
    public static int TREE_VALUE = 0;

    public static void init(RobotController myRC) {
        rc = myRC;
        myID = rc.getID();
        myType = rc.getType();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        here = myRC.getLocation();
        roundNum = 0;

        sensorRadius = myType.sensorRadius;
        strideLength = myType.strideRadius;

        ourArchonStartLocs = rc.getInitialArchonLocations(myTeam);
        theirArchonStartLocs = rc.getInitialArchonLocations(enemyTeam);
        archonStartCount = ourArchonStartLocs.length;
        ourArchonStartCoM = new MapLocation(0,0);
        theirArchonStartCoM = new MapLocation(0,0);
        mapStartCoM = new MapLocation (0,0);
        for (MapLocation i : ourArchonStartLocs) {
            ourArchonStartCoM = FastMath.addVec(ourArchonStartCoM, i);
        }
        for (MapLocation i : theirArchonStartLocs) {
            theirArchonStartCoM = FastMath.addVec(theirArchonStartCoM, i);
        }
        mapStartCoM = FastMath.addVec(ourArchonStartCoM, theirArchonStartCoM);
        ourArchonStartCoM = FastMath.multiplyVec( 1.0 / (double)archonStartCount, ourArchonStartCoM);
        theirArchonStartCoM = FastMath.multiplyVec (1.0 / (double) archonStartCount, theirArchonStartCoM);
        mapStartCoM = FastMath.multiplyVec ( 0.5 / (double) archonStartCount, mapStartCoM);
    }

    public static void update() {
        roundNum = rc.getRoundNum();
        here = rc.getLocation();
    }

    //Carries out common functions used by all unit types.
    //For example, reporting their existence for unit counting
    public static void commonFunctions() throws GameActionException {
        Messaging.raiseHandForUnitCounts();
        Messaging.leaderElection();
        if (iAmLeader) {
            leaderOnlyBehaviours();
        }
        visibleEnemies = rc.senseNearbyRobots(sensorRadius, enemyTeam);
        visibleAllies = rc.senseNearbyRobots(sensorRadius, myTeam);
        nearbyBullets = rc.senseNearbyBullets(6);
        adjacentTrees = rc.senseNearbyTrees(4);
    }

    public static Direction randomDirection() {
        return new Direction((float)FastMath.rand256() / 256 * 2 * (float) PI);
    }

    public static boolean tryMove(MapLocation target, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(target)) {
            rc.move(target);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;
        Direction startDir = here.directionTo(target);
        float targetDist = here.distanceTo(target);
        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            for (int i = 1; i < 4; i++) {
                if (rc.canMove(here.add(startDir.rotateLeftDegrees(degreeOffset), targetDist / i))) {
                    rc.move(here.add(startDir.rotateLeftDegrees(degreeOffset), targetDist / i));
                    return true;
                }
            }
            // Try the offset on the right side
            for (int i = 1; i < 4; i++) {
                if (rc.canMove(here.add(startDir.rotateRightDegrees(degreeOffset), targetDist / i))) {
                    rc.move(here.add(startDir.rotateRightDegrees(degreeOffset), targetDist / i));
                    return true;
                }
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    /*
        Vector Field sums of forces applied by all bullets approaching the player.
        "impetus" argument allows robot to act less safely if set to a large value
    */
    public static MapLocation chooseSafeLocation(MapLocation dest, float impetus) throws GameActionException {
        MapLocation combinedBulletForce = new MapLocation(0,0);
        if (nearbyBullets.length > 0) {
            MapLocation botEscapeVector;
            for (BulletInfo bullet : nearbyBullets) {
                if (here.distanceTo(bullet.location) > (myType.bodyRadius + GameConstants.BULLET_SPAWN_OFFSET)) {
                    Direction propDir = bullet.dir;
                    MapLocation bulletLoc = bullet.location;
                    float dx = propDir.getDeltaX(bullet.speed);
                    float dy = propDir.getDeltaY(bullet.speed);
                    float bulletX = bulletLoc.x + dx;
                    float bulletY = bulletLoc.y + dy;
                    bulletLoc = new MapLocation(bulletX, bulletY);
                    botEscapeVector = FastMath.minusVec(here, bulletLoc);
                    float bulletDistance = here.distanceSquaredTo(bulletLoc);
                    botEscapeVector = FastMath.multiplyVec(3 / bulletDistance, botEscapeVector);
                    combinedBulletForce = FastMath.addVec(combinedBulletForce, botEscapeVector);
                }
            }
            //rc.setIndicatorLine(here,FastMath.minusVec(here,combinedBulletForce), 0,155,0);
        }
        MapLocation combinedRobotForce = new MapLocation(0,0);
        if (visibleAllies.length > 0) {
            MapLocation botEscapeVector;
            int botCount = 0;
            for (RobotInfo ally : visibleAllies) {
                if (ally.location.distanceTo(here) < 4) {
                    botEscapeVector = FastMath.minusVec(here,ally.location);
                    botEscapeVector = FastMath.multiplyVec(1/here.distanceSquaredTo(ally.location), botEscapeVector);
                    combinedRobotForce = FastMath.addVec(combinedRobotForce, botEscapeVector);
                }
                if (botCount > 10) {
                    break;
                }
            }
            //rc.setIndicatorLine(here,FastMath.minusVec(here,combinedRobotForce), 0,0,155);
        }
        MapLocation combinedEnemyForce = new MapLocation(0,0);
        if (visibleEnemies.length > 0) {
            MapLocation botEscapeVector;
            int botCount = 0;
            for (RobotInfo enemy : visibleEnemies) {
                botCount++;
                float botFearRating = 4;
                if (enemy.type == RobotType.LUMBERJACK) {
                    botFearRating = 6;
                }
                if (enemy.location.distanceTo(here) < 6 && enemy.type != RobotType.GARDENER && enemy.type!= RobotType.ARCHON) {
                    botEscapeVector = FastMath.minusVec(here,enemy.location);
                    botEscapeVector = FastMath.multiplyVec(botFearRating / here.distanceSquaredTo(enemy.location), botEscapeVector);
                    combinedEnemyForce = FastMath.addVec(combinedEnemyForce, botEscapeVector);
                }
                if (botCount > 10) {
                    break;
                }
            }
            //rc.setIndicatorLine(here,FastMath.minusVec(here,combinedEnemyForce), 155,0,0);
        }
        MapLocation combinedTreeForce = new MapLocation(0,0);
        if (adjacentTrees.length > 0 && (myType != RobotType.SCOUT && myType != RobotType.LUMBERJACK)) {
            MapLocation botEscapeVector;
            int treeCount = 0;
            for (TreeInfo tree : adjacentTrees) {
                botEscapeVector = FastMath.minusVec(here, tree.location);
                botEscapeVector = FastMath.multiplyVec(0.25f / here.distanceSquaredTo(tree.location), botEscapeVector);
                // if the bot is trying to get somewhere, rotate tree force vector at right angles to path of travel
                /*if (dest != null && dest != here) {
                    //get inverse vector from destination to here
                    Direction destToHere = here.directionTo(dest);
                    Direction treeToHere = tree.location.directionTo(here);
                    float angleBetween = treeToHere.degreesBetween(destToHere);
                    treeToHere.rotateLeftDegrees(90 + angleBetween);
                    MapLocation origin = new MapLocation(0,0);
                    float escapeVectorUnit = origin.distanceSquaredTo(botEscapeVector);
                    botEscapeVector = origin.add(treeToHere, escapeVectorUnit);
                    System.out.println("pathing past a tree");
                }*/
                combinedTreeForce = FastMath.addVec(combinedTreeForce, botEscapeVector);
                if (treeCount > 4) {
                    break;
                }
            }
            rc.setIndicatorLine(here, FastMath.addVec(combinedTreeForce, here), 0,190,10);
            //rc.setIndicatorLine(here,FastMath.minusVec(here,combinedRobotForce), 0,0,155);
        }
        //We now have 3 vectors summing up the total forces applied by bullets, enemies and allies from "here".
        //Find vector to destination:
        MapLocation vectorToDest = FastMath.minusVec(dest,here);
        //If destination is not "here", Normalise it, then multiply by the impetus value
        if (dest != null && dest != here) {
            vectorToDest = FastMath.multiplyVec(impetus / here.distanceTo(dest), vectorToDest);
        }
        //Then add up all of our resultant vectors
        MapLocation vectorResult = FastMath.addVec(combinedEnemyForce, combinedBulletForce);
        vectorResult = FastMath.addVec(vectorResult, combinedRobotForce);
        vectorResult = FastMath.addVec(vectorResult, combinedTreeForce);
        vectorResult = FastMath.addVec(vectorResult, vectorToDest);

        //"normalise" the final resulting vector up to the unit's stridelength value.
        float vectorDistancesSummed = 3 + impetus;
        MapLocation origin = new MapLocation(0,0);
        vectorResult = FastMath.multiplyVec((strideLength/vectorResult.distanceTo(origin)), vectorResult);
        vectorResult = FastMath.negateVec(vectorResult);
        //convert Vector to absolute mapLocation
        vectorResult = FastMath.minusVec(here,vectorResult);
        //draw a line and return the resultant movement choice.
        //rc.setIndicatorLine(here,vectorResult, 255,255,255);
        return vectorResult;
    }

    public static void getTargetPoint(RobotInfo target) {

    }

    public static void tryShakingTrees(TreeInfo[] nearbyTrees) throws GameActionException{
        for (TreeInfo tree : nearbyTrees) {
            if (tree.getContainedBullets() > 0) {
                int treeID = tree.getID();
                if (rc.canShake(treeID)) {
                    rc.shake(treeID);
                    break;
                }
            }
        }
    }

    public static void processSignals() throws GameActionException {
        if (myType == RobotType.ARCHON || myType == RobotType.GARDENER) {
            buildCount = rc.readBroadcast(Messaging.indexBuildCount);
        }
        myUnitCount = Messaging.getMyUnitCount();
        int enemyLocationData = rc.readBroadcast(Messaging.indexEnemyLocation);
        enemyTarget = Messaging.mapLocFromInt(enemyLocationData);
        enemyTargetAge = Messaging.mapLocAgeFromInt(enemyLocationData);
    }


    public static void leaderOnlyBehaviours() throws GameActionException {
        if (iAmLeader && roundNum % 20 == 0) {
            Navigation.updateVectorMap();
        }
        //Navigation.checkUpdateConditions();
        //Navigation.gatherMapInfo();
        //Navigation.calculateGrids();
        //Navigation.RunPathfinding();
        //Navigation.postGrids();
    }

}

