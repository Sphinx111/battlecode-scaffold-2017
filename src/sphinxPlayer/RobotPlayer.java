package sphinxPlayer;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * Created by Celeron on 1/10/2017.
 */
public class RobotPlayer extends Globals {

    public static void run(RobotController owner) throws Exception {
        Globals.init(owner);
        FastMath.initRand(owner);
        while (true) {
            try {
                Globals.update();
                commonFunctions();

                int startTurn = roundNum;
                if (myType == RobotType.ARCHON) {
                    ArchonLoop.loop();
                } else if (myType == RobotType.GARDENER) {
                    GardenerLoop.loop();
                } else if (myType == RobotType.SCOUT) {
                    ScoutLoop.loop();
                } else if (myType == RobotType.SOLDIER) {
                    SoldierLoop.loop();
                } else if (myType == RobotType.TANK) {
                    TankLoop.loop();
                } else if (myType == RobotType.LUMBERJACK) {
                    LumberjackLoop.loop();
                } else {
                    System.out.println("Robot Type Not Detected, leaving bot in holding mode.");
                    while (true) {
                        Clock.yield();
                    }
                }
                int endTurn = rc.getRoundNum();
                if (startTurn != endTurn) {
                    System.out.println(myID + " OVER BYTECODE LIMIT!");
                    rc.setIndicatorDot(here, 0, 0, 0);
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
