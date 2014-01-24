package d_bot.robots;

import d_bot.managers.InfoArray.BuildingInfo;
import d_bot.managers.InfoArray.BuildingStatus;
import d_bot.managers.InfoArray.BuildingType;
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
