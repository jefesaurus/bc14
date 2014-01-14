package team204.brains;

import team204.manager.RobotManager;
import battlecode.common.GameActionException;

public abstract class RobotBrain extends RobotManager {
    
    public abstract void step() throws GameActionException;
}
