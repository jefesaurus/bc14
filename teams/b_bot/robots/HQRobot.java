package b_bot.robots;

import b_bot.Constants;
import b_bot.managers.InfoCache;
import b_bot.managers.InfoArray.Command;
import b_bot.managers.InfoArray.CommandType;
import b_bot.managers.InfoArray.InfoArrayManager;
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
    
    public enum HQLocation {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        BOTTOM,
        TOP,
        LEFT,
        RIGHT
    };
    
    static MapLocation rallyPoint;    
    static Command[] squadCommands = new Command[999];
    Direction directionToEnemyHQ;
    public HQLocation HQ_LOCATION;
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
        comms.sendSquadCommand(currentSquadNum, new Command(CommandType.BUILD_PASTR, myHQ));
        if (tryToSpawn(directionToEnemyHQ)) {
            System.out.println("Did spawnwith squad num: " + currentSquadNum);
            currentSquadNum ++;            
        }
        
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();
        
        if (Math.abs(this.curLoc.x - (width / 2)) < 5) {
            if (this.curLoc.y < (height / 2)) {
                this.HQ_LOCATION = HQLocation.TOP;
            } else {
                this.HQ_LOCATION = HQLocation.BOTTOM;
            }
        } else if (Math.abs(this.curLoc.y - (width / 2)) < 5) {
            if (this.curLoc.x > (width / 2)) {
                this.HQ_LOCATION = HQLocation.RIGHT;
            } else {
                this.HQ_LOCATION = HQLocation.LEFT;
            }
        } else if (this.curLoc.x < width / 2) {
            if (this.curLoc.y < (height / 2)) {
                this.HQ_LOCATION = HQLocation.TOP_LEFT;
            } else {
                this.HQ_LOCATION = HQLocation.BOTTOM_LEFT;
            }
        } else {
            if (this.curLoc.y < (height / 2)) {
                this.HQ_LOCATION = HQLocation.TOP_RIGHT;
            } else {
                this.HQ_LOCATION = HQLocation.BOTTOM_RIGHT;
            }
        }
        
    }
    
    
    //MapLocation pastrLoc = new MapLocation(40, 32);

    @Override
    public void run() throws GameActionException {   
        
        if (bestPastrLoc == null) {
            int MIDX =  (rc.getMapWidth() / 2);
            int MIDY = (rc.getMapHeight() / 2);
            int[] info;
            switch (this.HQ_LOCATION) {
            case TOP:
                comms.sendSearchCoordinates(MIDX - CowGrowth.bigBoxSize, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(0, 0, MIDX  + CowGrowth.bigBoxSize, MIDY  + CowGrowth.bigBoxSize));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case BOTTOM:
                comms.sendSearchCoordinates(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight());
                info = (new CowGrowth(this.rc, this).getBestLocation(0, MIDY - CowGrowth.bigBoxSize, MIDX + CowGrowth.bigBoxSize,  rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case RIGHT:
                comms.sendSearchCoordinates(MIDX - CowGrowth.bigBoxSize, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(),  rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case LEFT:
                comms.sendSearchCoordinates(0, 0, MIDX + CowGrowth.bigBoxSize, MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(0, MIDY - CowGrowth.bigBoxSize, MIDX + CowGrowth.bigBoxSize, rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case TOP_RIGHT:
                comms.sendSearchCoordinates(0, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case TOP_LEFT:
                comms.sendSearchCoordinates(0, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(0, MIDY - CowGrowth.bigBoxSize, MIDX - CowGrowth.bigBoxSize, rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case BOTTOM_RIGHT:
                comms.sendSearchCoordinates(0, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight());
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            case BOTTOM_LEFT:
                comms.sendSearchCoordinates(0, 0, MIDX + CowGrowth.bigBoxSize, rc.getMapHeight());
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(info);
                break;
            }
            int[] bestLoc = comms.wait_P_PASTR_LOC_2();
            bestPastrLoc = new MapLocation(bestLoc[0], bestLoc[1]);
            comms.sendSquadCommand(0, new Command(CommandType.BUILD_PASTR, bestPastrLoc));
            
        } else if (currentSquadNum == 1) {
            if (tryToSpawn(myHQ.directionTo(bestPastrLoc))) {
                System.out.println("Did spawnwith squad num: " + currentSquadNum);
                comms.setNewSpawnSquad(currentSquadNum);
                comms.sendSquadCommand(currentSquadNum++, new Command(CommandType.BUILD_NOISE_TOWER, bestPastrLoc));
                rc.yield();
                comms.sendSquadCommand(currentSquadNum, new Command(CommandType.RALLY_POINT, rallyPoint));
                comms.setNewSpawnSquad(currentSquadNum);


            }
            
        // If there is one HQ and one soldier
        } else {
            //after telling them where to go, consider spawning
            if (tryToSpawn(directionToEnemyHQ)) {
                //System.out.println("Did spawnwith squad num: " + currentSquadNum);
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
