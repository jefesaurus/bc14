package a_Bot.robots;

import a_Bot.managers.InfoCache;
import a_Bot.managers.InfoArray.Command;
import a_Bot.navigation.NavigationMode;
import a_Bot.robots.soldiers.OffenseMode;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class SoldierRobot extends BaseRobot {
    int squadNum = 0;
    Command currentCommand;
    public Robot[] nearbyEnemies;
    
    // Modes
    OffenseMode om;
    
    public enum BehaviorMode {
        OFFENSE, DEFENSE
    }
    BehaviorMode botMode;
    

    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);
        om = new OffenseMode(this);
        
        squadNum = comms.getNewSpawnSquad();
        rc.setIndicatorString(1, "Squad: " + squadNum);
        nav.setNavigationMode(NavigationMode.BUG);
        botMode = BehaviorMode.OFFENSE;
        
        
    }

    @Override
    public void run() throws GameActionException {
      //follow orders from HQ
        currentCommand = comms.getSquadCommand(squadNum);
        rc.setIndicatorString(2, currentCommand.toString());

        nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());

        
        if(nearbyEnemies.length > 0){
            if (rc.isActive() && botMode == BehaviorMode.OFFENSE) {
                om.step();
            }
        } else {//NAVIGATION BY DOWNLOADED PATH
            MapLocation destination;
            switch (currentCommand.type) {
            case RALLY_POINT:
            case ATTACK_POINT:
            case PASTR_POINT:
            default:
                destination = currentCommand.loc;
            }
            om.simpleBug(destination);
        }
        rc.yield();
    }

}