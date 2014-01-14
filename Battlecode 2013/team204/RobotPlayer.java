package team204;

import team204.brains.GeneratorBrain;
import team204.brains.HQBrain;
import team204.brains.NoBrain;
import team204.brains.RobotBrain;
import team204.brains.SoldierBrain;
import team204.manager.BaseManager;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    
    public static void run(RobotController rc) {
        BaseManager.setRobotController(rc);
        RobotBrain brain;
        switch (BaseManager.getType()) {
        case HQ:
            brain = new HQBrain();
            break;
        case SOLDIER:
            brain = new SoldierBrain();
            break;
        case GENERATOR:
            brain = new GeneratorBrain();
            break;
        default:
            brain = new NoBrain();
            break;
        }
        while (true) {
            int startRound = Clock.getRoundNum();
            try {
                brain.step();
            } catch (GameActionException e) { // change to Exception
                e.printStackTrace();
            }
            if (Clock.getRoundNum() > startRound) {
                System.out.println("Round " + startRound + " bytecode limit exceeded");
            }
            BaseManager.yield();
        }
    }
    
}
