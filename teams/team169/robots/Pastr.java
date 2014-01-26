package team169.robots;

import team169.managers.InfoArray.BuildingInfo;
import team169.managers.InfoArray.BuildingStatus;
import team169.managers.InfoArray.BuildingType;
import team169.managers.InfoArray.PastrAlarm;
import battlecode.common.*;

public class Pastr extends BaseRobot {
    
    public PastrAlarm alarm;
    
    public Pastr(RobotController rc) throws GameActionException {
        super(rc);
        alarm = new PastrAlarm(0, Clock.getRoundNum());
    }

    public void alertIfEnemySighted() throws GameActionException {
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            alarm = new PastrAlarm(1, Clock.getRoundNum());
        }
        comms.updatePastrAlarm(RobotType.PASTR, alarm);
    }
    
    @Override
    public void run() throws GameActionException {
        alertIfEnemySighted();
        comms.setBuildingStatus(BuildingType.PASTR, new BuildingInfo(curRound, BuildingStatus.ALL_GOOD, curLoc));       
    }
}
