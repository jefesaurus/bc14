package a_Bot.robots;

import a_Bot.Constants;
import a_Bot.managers.InfoCache;
import a_Bot.managers.InfoArray.Command;
import a_Bot.managers.InfoArray.CommandType;
import a_Bot.util.FastSet;
import a_Bot.util.VectorFunctions;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class HQRobot extends BaseRobot {
    static MapLocation rallyPoint;
    static FastSet enemyPastrsDetected = new FastSet();
    
    static Command[] squadCommands = new Command[999];

    
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
            MapLocation closestPastr = null;
            double closestDist = Integer.MAX_VALUE;
            
            double currDist;
            for (int i = 0; i < enemyPastrs.length; i ++) {
                enemyPastrsDetected.addMapLocation(enemyPastrs[i]);
                currDist = enemyPastrs[i].distanceSquaredTo(InfoCache.HQLocation);
                if (currDist < closestDist) {
                    closestPastr = enemyPastrs[i];
                    closestDist = currDist;
                }
            }
            
            for (int i = 0; i < currentSquadNum; i ++) {
                if (squadCommands[i].type == CommandType.ATTACK_POINT &&
                        !enemyPastrsDetected.containsMapLocation(squadCommands[i].loc)) {
                    // Send old squad to next pastr
                    Command toSend = new Command(CommandType.ATTACK_POINT, closestPastr);
                    comms.sendSquadCommand(i, toSend);
                    squadCommands[currentSquadNum] = toSend;
                }
            } 
            
            if (currentSquadSize >= Constants.MIN_SQUAD_SIZE) {
                Command toSend = new Command(CommandType.ATTACK_POINT, closestPastr);
                comms.sendSquadCommand(currentSquadNum, toSend);
                squadCommands[currentSquadNum] = toSend;
                System.out.println("Sent Loc: " + closestPastr.toString());
                
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
