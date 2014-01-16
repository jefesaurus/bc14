package ourBot_newComm;

import ourBot_newComm.robots.BaseRobot;
import ourBot_newComm.robots.HQRobot;
import ourBot_newComm.robots.NoiseTower;
import ourBot_newComm.robots.SoldierRobot;
import ourBot_newComm.robots.Pastr;
import battlecode.common.RobotController;

public class RobotPlayer {
    public static void run(RobotController rc) {
        BaseRobot br = null;

        try {
            switch (rc.getType()) {
            case SOLDIER:
                br = new SoldierRobot(rc);
                break;
            case HQ:
                br = new HQRobot(rc);
                break;
            case NOISETOWER:
                br = new NoiseTower(rc);
            case PASTR:
                br = new Pastr(rc);
            default:
                break;
            }
        } catch (Exception e) {
            System.out.println("Robot constructor failed");
        }


        // Main loop should never terminate
        while (true) {
            try {
                br.loop();
            } catch (Exception e) {
                System.out.println("Main loop terminated unexpectedly");
                e.printStackTrace();
            }
        }
    }
}