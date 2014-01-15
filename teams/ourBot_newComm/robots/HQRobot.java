package ourBot_newComm.robots;

import ourBot_newComm.BreadthFirst;
import ourBot_newComm.Comms;
import ourBot_newComm.Constants;
import ourBot_newComm.managers.InfoArray.Command;
import ourBot_newComm.managers.InfoArray.CommandType;
import ourBot_newComm.util.VectorFunctions;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class HQRobot extends BaseRobot {
    static MapLocation rallyPoint;
    
    int currentSquadNum = 0;
    int currentSquadSize = 0;

    public HQRobot(RobotController rc) throws GameActionException {
        super(rc);
   
        rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
        
        // Set the new spawn squad
        comms.setNewSpawnSquad(currentSquadNum);
        // Tell this squad to rally at rallyPoint
        comms.sendSquadCommand(currentSquadNum, new Command(CommandType.RALLY_POINT, rallyPoint));
        if (tryToSpawn(rc)) {
            currentSquadSize ++;
        }
    }

    @Override
    public void run() throws GameActionException {
        
        newCommsSystem();
    }
    
    public void newCommsSystem() throws GameActionException {
        //tell them to go to the rally point
        comms.sendSquadCommand(currentSquadNum, new Command(CommandType.RALLY_POINT, rallyPoint));
        
        //if the enemy builds a pastr, tell squad 2 to go there.
        MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
        if(enemyPastrs.length > 0){
            if (currentSquadSize >= Constants.MIN_SQUAD_SIZE) {
                comms.sendSquadCommand(currentSquadNum, new Command(CommandType.ATTACK_POINT, enemyPastrs[0]));
                currentSquadNum++;
                currentSquadSize = 0;
                comms.setNewSpawnSquad(currentSquadNum);
            }
        }
        
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
        
        if(robotLocations.length>0){
            MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
            if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){//close enough to shoot
                if(rc.isActive()&&rc.canAttackSquare(closestEnemyLoc)){
                    rc.attackSquare(closestEnemyLoc);
                }
            }
        }

        //after telling them where to go, consider spawning
        if (tryToSpawn(rc)) {
            currentSquadSize ++;
            if (currentSquadSize > Constants.MAX_SQUAD_SIZE) {
                MapLocation attackPoint;
                if(enemyPastrs.length > 0){
                    attackPoint = enemyPastrs[0];
                } else {
                    attackPoint = rc.senseEnemyHQLocation();
                }

                comms.sendSquadCommand(currentSquadNum, new Command(CommandType.ATTACK_POINT, attackPoint));
                currentSquadNum++;
                currentSquadSize = 0;
                comms.setNewSpawnSquad(currentSquadNum);
            }
        }
    }


    public static boolean tryToSpawn(RobotController rc) throws GameActionException {
        if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            for(int i=0;i<8;i++){
                Direction trialDir = allDirections[i];
                if(rc.canMove(trialDir)){
                    // Post squad assignment to this new spawn
                    
                    rc.spawn(trialDir);
                    return true;
                }
            }
        }
        return false;
    }
}
