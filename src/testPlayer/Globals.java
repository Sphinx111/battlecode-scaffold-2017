package testPlayer;
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

    public static Direction[] dirs = {Direction.getNorth(),Direction.getEast(),Direction.getSouth(),Direction.getWest()};

    public static RobotController rc;
    public static MapLocation here;
    public static Team myTeam;
    public static Team enemyTeam;
    public static int myID;
    public static RobotType myType;

    public static float sensorRadius;
    public static float strideLength;

    public static int roundNum;
    public static RobotInfo[] visibleEnemies;
    public static RobotInfo[] visibleAllies;
    public static TreeInfo[] ourTrees;
    public static TreeInfo[] theirTrees;
    public static TreeInfo[] neutralTrees;

    public static int soldierValue = 2;
    public static int tankValue = 4;
    public static int scoutValue = 1;
    public static int archonValue = 0;
    public static int gardenerValue = 0;
    public static int lumberjackValue = 1;
    public static int treeValue = 1;

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

    public static Direction randomDirection() {
        return new Direction((float)FastMath.rand256() / 256 * 2 * (float) PI);
    }

    public static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
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
    public static MapLocation chooseSafeLocation(BulletInfo[] nearbyBullets, MapLocation dest, float impetus) throws GameActionException {
        MapLocation startDirection = FastMath.minusVec(dest,here);
        startDirection = FastMath.multiplyVec(impetus,startDirection);
        MapLocation combinedBulletForce = here;
        if (nearbyBullets.length > 0) {
            Direction botEscapeAngle = new Direction(0);
            float force = 0;
            for (BulletInfo bullet : nearbyBullets) {
                Direction propDir = bullet.dir;
                MapLocation bulletLoc = bullet.location;
                float dx = propDir.getDeltaX(bullet.speed);
                float dy = propDir.getDeltaY(bullet.speed);
                float bulletX = bulletLoc.x + dx;
                float bulletY = bulletLoc.y + dy;
                bulletLoc = new MapLocation(bulletX, bulletY);
                botEscapeAngle = new Direction(-dx,dy);
                float bulletDistance = here.distanceTo(bulletLoc);
                force = 6 / bulletDistance + 0.01f;
            }
            combinedBulletForce = combinedBulletForce.add(botEscapeAngle, force);
        }
        MapLocation vectorResult = FastMath.addVec(startDirection, combinedBulletForce);
        //vectorResult = FastMath.multiplyVec(0.99f,vectorResult);
        //rc.setIndicatorLine(here,vectorResult, 0,0,255);
        return vectorResult;
    }

    public static void tryShakingTrees(TreeInfo[] nearbyTrees) throws GameActionException{
        for (TreeInfo tree : nearbyTrees) {
            if (tree.getContainedBullets() > 0) {
                int treeID = tree.getID();
                if (rc.canShake(treeID)) {
                    rc.shake(treeID);
                    rc.setIndicatorDot(tree.location, 0,180,180);
                    break;
                }
            }
        }
    }
}

