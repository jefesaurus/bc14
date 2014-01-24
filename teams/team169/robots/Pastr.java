package team169.robots;

import team169.managers.InfoArray.BuildingInfo;
import team169.managers.InfoArray.BuildingStatus;
import team169.managers.InfoArray.BuildingType;
import battlecode.common.*;

public class Pastr extends BaseRobot {
    
    public Pastr(RobotController rc) throws GameActionException {
        super(rc);
    }
    
    @Override
    public void run() throws GameActionException {
        comms.setBuildingStatus(BuildingType.PASTR, new BuildingInfo(curRound, BuildingStatus.ALL_GOOD, curLoc));
    }
}
