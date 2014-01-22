package c_bot;

import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Constants {
    public static final int MAX_PATH_SIZE = 100;
    
    public static final int MIN_SQUAD_SIZE = 3;
    public static final int MAX_SQUAD_SIZE = 8;
    
    public static final int SOLDIER_SIGHT_RANGE = RobotType.SOLDIER.sensorRadiusSquared;
    public static final int SOLDIER_ATTACK_RANGE = RobotType.SOLDIER.attackRadiusMaxSquared;
    
    public static final Direction allDirections[] = Direction.values();
    public static final int directionalLooks[] = {1,-1,2,-2,3,-3,4};

}
