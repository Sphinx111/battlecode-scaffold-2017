package testPlayer;
import battlecode.common.*;

import static testPlayer.Globals.myType;
import static testPlayer.Globals.rc;

/**
 * Created by Celeron on 1/10/2017.
 */
public class RobotPlayer extends Globals {

    public static void run(RobotController owner) throws Exception {
        Globals.init(owner);

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

    }
}
