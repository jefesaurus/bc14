package b_bot.robots;

import b_bot.Constants;
import b_bot.managers.InfoCache;
import b_bot.managers.InfoArray.Command;
import b_bot.managers.InfoArray.CommandType;
import b_bot.util.CowGrowth;
import b_bot.util.FastSet;
import b_bot.util.VectorFunctions;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HQRobot extends BaseRobot {
    static MapLocation rallyPoint;    
    static Command[] squadCommands = new Command[999];
    Direction directionToEnemyHQ;
    
    // Our favored pastr location
    MapLocation bestPastrLoc = null;
 
    // Our target in the enemies pastrs
    MapLocation currPastrTarget = null;

    int currentSquadNum = 0;
    

    public HQRobot(RobotController rc) throws GameActionException {
        super(rc);
        rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
        directionToEnemyHQ = this.myHQ.directionTo(this.enemyHQ);
        
        // Set the new spawn squad
        comms.setNewSpawnSquad(currentSquadNum);        
        // Tell this squad to rally at rallyPoint
        comms.sendSquadCommand(currentSquadNum, new Command(CommandType.RALLY_POINT, myHQ));
        if (tryToSpawn(directionToEnemyHQ)) {
            System.out.println("Did spawnwith squad num: " + currentSquadNum);
            currentSquadNum ++;            
        }
        rc.yield();
    }
    
    
    MapLocation pastrLoc = new MapLocation(40, 32);

    @Override
    public void run() throws GameActionException {   
        
        if (bestPastrLoc == null) {
            bestPastrLoc = new CowGrowth(rc, this).getBestLocation();
            comms.sendSquadCommand(0, new Command(CommandType.BUILD_PASTR, bestPastrLoc));
        } else if (currentSquadNum == 1) {
            if (tryToSpawn(myHQ.directionTo(bestPastrLoc))) {
                System.out.println("Did spawnwith squad num: " + currentSquadNum);
                comms.setNewSpawnSquad(currentSquadNum);
                comms.sendSquadCommand(currentSquadNum++, new Command(CommandType.BUILD_NOISE_TOWER, bestPastrLoc));
                rc.yield();
            }
            
        // If there is one HQ and one soldier
        } else {
            //after telling them where to go, consider spawning
            if (tryToSpawn(directionToEnemyHQ)) {
                System.out.println("Did spawnwith squad num: " + currentSquadNum);
                comms.setNewSpawnSquad(currentSquadNum);
                comms.sendSquadCommand(currentSquadNum, new Command(CommandType.RALLY_POINT, bestPastrLoc));
                rc.yield();
            }
        }
        

        
        //if the enemy builds a pastr, tell squad 2 to go there.
        MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
        if(enemyPastrs.length > 0) {
            MapLocation closestPastr = null;
            double closestDist = Integer.MAX_VALUE;
            double currDist;
            
            
            // Determine if our current pastr target has been killed while simultaneously finding the next closest one.
            boolean hasKilledTarget = true;
            for (int i = 0; i < enemyPastrs.length; i ++) {
                if (currPastrTarget != null && enemyPastrs[i].equals(currPastrTarget)) {
                    // We are already heading towards this one, so lets not so anything else
                    hasKilledTarget = false;
                    break;
                } else {
                    currDist = enemyPastrs[i].distanceSquaredTo(InfoCache.HQLocation);
                    if (currDist < closestDist) {
                        closestPastr = enemyPastrs[i];
                        closestDist = currDist;
                    }
                }
            }
            
            if (hasKilledTarget && closestPastr != null) {
                Command toSend = new Command(CommandType.ATTACK_POINT, closestPastr);
                currPastrTarget = closestPastr;
                comms.sendSquadCommand(currentSquadNum, toSend);
                squadCommands[currentSquadNum] = toSend;
            }
        }
        


        

        
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
        
        if(robotLocations.length>0){
            MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
            if(rc.isActive() && rc.canAttackSquare(closestEnemyLoc)){
                rc.attackSquare(closestEnemyLoc);
            }
        }
        rc.yield();
    }




    protected boolean tryToSpawn(Direction chosenDirection) throws GameActionException{
        if(rc.isActive() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS){
            if(rc.canMove(chosenDirection)){
                rc.spawn(chosenDirection);
                return true;
            }
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:Constants.directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = Constants.allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.spawn(trialDir);
                    return true;
                }
            }
        }
        return false;
    }
}
