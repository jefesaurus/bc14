package team169_working;

import team169_working.robots.BaseRobot;
import team169_working.robots.HQRobot;
import team169_working.robots.NoiseTower;
import team169_working.robots.Pastr;
import team169_working.robots.SoldierRobot;
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
                break;
            case PASTR:
                br = new Pastr(rc);
                break;
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