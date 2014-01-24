package d_bot;

import d_bot.robots.BaseRobot;
import d_bot.robots.HQRobot;
import d_bot.robots.NoiseTower;
import d_bot.robots.Pastr;
import d_bot.robots.SoldierRobot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

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