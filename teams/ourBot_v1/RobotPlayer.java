package ourBot_v1;

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