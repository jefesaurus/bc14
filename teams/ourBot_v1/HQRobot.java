package ourBot_v1;

import ourBot_v1.Comms;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class HQRobot extends BaseRobot {
    static MapLocation rallyPoint;

    public HQRobot(RobotController rc) throws GameActionException {
        super(rc);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run() throws GameActionException {
        //TODO consider updating the rally point to an allied pastr 

        //tell them to go to the rally point
        Comms.findPathAndBroadcast(1,rc.getLocation(), rallyPoint, bigBoxSize, 2);

        //if the enemy builds a pastr, tell squad 2 to go there.
        MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
        if(enemyPastrs.length>0){
            Comms.findPathAndBroadcast(2,rallyPoint,enemyPastrs[0],bigBoxSize,2);//for some reason, they are not getting this message
        }

        //after telling them where to go, consider spawning
        tryToSpawn(rc);
    }


    public static void tryToSpawn(RobotController rc) throws GameActionException {
        if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            for(int i=0;i<8;i++){
                Direction trialDir = allDirections[i];
                if(rc.canMove(trialDir)){
                    rc.spawn(trialDir);
                    break;
                }
            }
        }
    }
}
