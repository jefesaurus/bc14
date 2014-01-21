package c_bot.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import c_bot.Constants;
import c_bot.managers.InfoCache;
import c_bot.managers.InfoArray.BuildingInfo;
import c_bot.managers.InfoArray.BuildingType;
import c_bot.managers.InfoArray.Command;
import c_bot.managers.InfoArray.CommandType;
import c_bot.util.CowGrowth;
import c_bot.util.VectorFunctions;

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
    
 
    
    public HQLocation HQ_LOCATION;
    static MapLocation rallyPoint;    
    static MapLocation defenseRallyPoint = null;    

    Direction directionToEnemyHQ;
    
    // Our favored pastr location
    MapLocation bestPastrLoc = null;

    public enum Strategy {
        GREEDY, SAFE_MACRO, DEFENSE_MACRO, RUSH 
    }
    
    Strategy strat;
    int NumUnitsProduced = 0;

    public HQRobot(RobotController rc) throws GameActionException {
        super(rc);
        rallyPoint = VectorFunctions.mldivide(VectorFunctions.mladd(this.myHQ, this.enemyHQ), 2);
        directionToEnemyHQ = this.myHQ.directionTo(this.enemyHQ);
        
        
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();
        
        int mapSize = width*height;
        if (mapSize > 2500) {
            strat = Strategy.RUSH;
        } else if (mapSize <= 900) {
            strat = Strategy.RUSH;
        } else {
            strat = Strategy.RUSH;
        }
        
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
    
    MapLocation currentPastrTarget = null;

    boolean safeMacroInitDone = false;
    public int SWARM_SIZE = 4;
    @Override
    public void run() throws GameActionException {
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        tryToAttack(enemyRobots);
        
        switch(strat) {
        case GREEDY:
            if (bestPastrLoc == null) {
                trySpawnPastr();
                calcBestPastrLocation();
            }
            if (!isPastrAlive()) {
                trySpawnPastr();
            } else if (!isTowerAlive()) {
                trySpawnTower();
            } else {
                currentPastrTarget = getPastrTarget(currentPastrTarget);
                if (currentPastrTarget != null) {
                    Command attackPastr = new Command(CommandType.ATTACK_PASTR, currentPastrTarget);
                    if (!trySpawnSquadMember(SWARM_SIZE, rallyPoint, attackPastr)){
                        for (int i = 2; i < squadNumber; i ++) {
                            comms.sendSquadCommand(i, attackPastr);
                        }
                    }
                } else {
                    trySpawnSquadMember(SWARM_SIZE, rallyPoint, new Command(CommandType.RALLY_POINT, rallyPoint));
                }
            }
            break;
            
        case RUSH:
            currentPastrTarget = getPastrTarget(currentPastrTarget);
            if (currentPastrTarget != null) {
                Command attackPastr = new Command(CommandType.ATTACK_PASTR, currentPastrTarget);
                if (!trySpawnSquadMember(SWARM_SIZE, rallyPoint, attackPastr)){
                    for (int i = 2; i < squadNumber; i ++) {
                        comms.sendSquadCommand(i, attackPastr);
                    }
                }
            } else {
                trySpawnSquadMember(SWARM_SIZE, rallyPoint, new Command(CommandType.RALLY_POINT, rallyPoint));
            }
            break;
        case SAFE_MACRO:
            if (!safeMacroInitDone) {
                if (NumUnitsProduced < 6) {
                    trySpawnSquadMember(6, rallyPoint, new Command(CommandType.RALLY_POINT, rallyPoint));
                } else {
                    currentPastrTarget = getPastrTarget(currentPastrTarget);
                    if (currentPastrTarget != null) {
                        Command attackPastr = new Command(CommandType.ATTACK_PASTR, currentPastrTarget);
                        for (int i = 2; i < squadNumber; i ++) {
                            comms.sendSquadCommand(i, attackPastr);
                        }
                    }
                    safeMacroInitDone = true;
                    
                }
            } else {
                
                //System.out.println("now we buildin pastr");
                if (bestPastrLoc == null) {

                    trySpawnPastr();
                    System.out.println("try to spawn");

                    calcBestPastrLocation();
                    System.out.println("now we buildin pastr");

                }
                if (!isPastrAlive()) {
                    trySpawnPastr();
                } else if (!isTowerAlive()) {
                    trySpawnTower();
                } else {
                    System.out.println("should be making more soldiers");

                    currentPastrTarget = getPastrTarget(currentPastrTarget);
                    if (currentPastrTarget != null) {
                        Command attackPastr = new Command(CommandType.ATTACK_PASTR, currentPastrTarget);
                        for (int i = 2; i < squadNumber; i ++) {
                            comms.sendSquadCommand(i, attackPastr);
                        }
                        trySpawnSquadMember(3, rallyPoint, new Command(CommandType.ATTACK_PASTR, currentPastrTarget));
                    } else {
                        trySpawnSquadMember(3, rallyPoint, new Command(CommandType.RALLY_POINT, rallyPoint));
                    }
                }
                
            }
            break;
        case DEFENSE_MACRO:
            if (bestPastrLoc == null) {
                trySpawnPastr();
                calcBestPastrLocation();
            }
            if (!isPastrAlive()) {
                trySpawnPastr();
            } else if (!isTowerAlive()) {
                trySpawnTower();
            } else {
                currentPastrTarget = getPastrTarget(currentPastrTarget);
                Command defPastr = new Command(CommandType.DEFEND_PASTR, bestPastrLoc);
                if (currentPastrTarget != null) {
                    Command attackPastr = new Command(CommandType.ATTACK_PASTR, currentPastrTarget);
                    trySpawnSquadMember(SWARM_SIZE, rallyPoint, attackPastr);
                    
                } else {
                    if (!trySpawnSquadMember(1, rallyPoint, defPastr)) {
                        for (int i = 2; i <= squadNumber; i ++) {
                            comms.sendSquadCommand(i, defPastr);
                        }
                    } else {
                        for (int i = 2; i < squadNumber; i ++) {
                            comms.sendSquadCommand(i, defPastr);
                        }
                    }
                }
            }
            break;
        }
    }
    
    public void tryToAttack(Robot[] enemyRobots) throws GameActionException {
        MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
        
        if(robotLocations.length>0){
            MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
            if(rc.isActive() && rc.canAttackSquare(closestEnemyLoc)){
                rc.attackSquare(closestEnemyLoc);
            }
        }
    }
    
    public void calcBestPastrLocation() throws GameActionException {
        if (bestPastrLoc == null) {
            int MIDX =  (rc.getMapWidth() / 2);
            int MIDY = (rc.getMapHeight() / 2);
            int[] info;
            switch (this.HQ_LOCATION) {
            case TOP:
                comms.sendSearchCoordinates(MIDX - CowGrowth.bigBoxSize, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(0, 0, MIDX  + CowGrowth.bigBoxSize, MIDY  + CowGrowth.bigBoxSize));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case BOTTOM:
                comms.sendSearchCoordinates(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight());
                info = (new CowGrowth(this.rc, this).getBestLocation(0, MIDY - CowGrowth.bigBoxSize, MIDX + CowGrowth.bigBoxSize,  rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case RIGHT:
                comms.sendSearchCoordinates(MIDX - CowGrowth.bigBoxSize, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(),  rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case LEFT:
                comms.sendSearchCoordinates(0, 0, MIDX + CowGrowth.bigBoxSize, MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(0, MIDY - CowGrowth.bigBoxSize, MIDX + CowGrowth.bigBoxSize, rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case TOP_RIGHT:
                comms.sendSearchCoordinates(0, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case TOP_LEFT:
                comms.sendSearchCoordinates(0, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize);
                info = (new CowGrowth(this.rc, this).getBestLocation(0, MIDY - CowGrowth.bigBoxSize, MIDX - CowGrowth.bigBoxSize, rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case BOTTOM_RIGHT:
                comms.sendSearchCoordinates(0, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight());
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, 0, rc.getMapWidth(), MIDY + CowGrowth.bigBoxSize));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            case BOTTOM_LEFT:
                comms.sendSearchCoordinates(0, 0, MIDX + CowGrowth.bigBoxSize, rc.getMapHeight());
                info = (new CowGrowth(this.rc, this).getBestLocation(MIDX - CowGrowth.bigBoxSize, MIDY - CowGrowth.bigBoxSize, rc.getMapWidth(), rc.getMapHeight()));
                comms.setP_PASTR_SCORE1(info[2]);
                comms.setP_PASTR_LOC1(new MapLocation(info[0], info[1]));
                break;
            }
            bestPastrLoc = comms.wait_P_PASTR_LOC_2();
        }
    }


    public boolean isPastrAlive() throws GameActionException {
        comms.getPastrLoc();
        BuildingInfo info = comms.getBuildingStatus(BuildingType.PASTR);
        
        switch (info.status) {
        case IS_COMPUTING:
        case IN_CONSTRUCTION:
        case ALL_GOOD:
        case UNDER_ATTACK:
            return ((curRound - info.roundNum) < 2); // If the round number hasn't been updated in 2 rounds, it is dead
        case NOTHING:
        default:
            return false;
        }
    }
    
    public boolean isTowerAlive() throws GameActionException {
        comms.getPastrLoc();
        BuildingInfo info = comms.getBuildingStatus(BuildingType.TOWER);
        
        switch (info.status) {
        case IS_COMPUTING:
        case IN_CONSTRUCTION:
        case ALL_GOOD:
        case UNDER_ATTACK:
            return ((curRound - info.roundNum) < 2); // If the round number hasn't been updated in 2 rounds, it is dead
        case NOTHING:
        default:
            return false;
        }
    }
    
    public void trySpawnPastr() throws GameActionException {
        if (bestPastrLoc != null) {
            if (tryToSpawn(myHQ.directionTo(bestPastrLoc))) {
                comms.setNewSpawnSquad(0);
                comms.sendSquadCommand(0, new Command(CommandType.BUILD_PASTR, myHQ));
            }
        } else {
            if (tryToSpawn(directionToEnemyHQ)) {
                comms.setNewSpawnSquad(0);
                comms.sendSquadCommand(0, new Command(CommandType.BUILD_PASTR, myHQ));
            }
        }
    }
    
    public void trySpawnTower() throws GameActionException {
        if (bestPastrLoc != null) {
            if (tryToSpawn(myHQ.directionTo(bestPastrLoc))) {
                comms.setNewSpawnSquad(1);
                comms.sendSquadCommand(1, new Command(CommandType.BUILD_NOISE_TOWER, myHQ));
            }
        } else {
            if (tryToSpawn(directionToEnemyHQ)) {
                comms.setNewSpawnSquad(1);        
                comms.sendSquadCommand(1, new Command(CommandType.BUILD_NOISE_TOWER, myHQ));
            }
        }
    }
    
    
    int squadSize = 0;
    int squadNumber = 2; // Start at 2 because the slots 0 and 1 are reserved for the buildings
    
    public boolean trySpawnSquadMember(int numBots, MapLocation rallyPoint, Command command) throws GameActionException {
        if (tryToSpawn(myHQ.directionTo(rallyPoint))) {
            comms.setNewSpawnSquad(squadNumber);
            NumUnitsProduced++;
            squadSize ++;
            if (squadSize >= numBots) {
                comms.sendSquadCommand(squadNumber++, command);
                squadSize = 0;
            } else {
                comms.sendSquadCommand(squadNumber, new Command(CommandType.RALLY_POINT, rallyPoint));
            }
            return true;
        } else {
            return false;
        }
    }

    
    public MapLocation getPastrTarget(MapLocation currentTarget) {        
        MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
        if(enemyPastrs.length > 0) {
            return getPastrTarget(enemyPastrs, currentTarget);
        } else {
            return null;
        }
    }

    
    /*
     * Returns the closest pastr or the currentTarget if it still exists.
     */
    public MapLocation getPastrTarget(MapLocation[] enemyPastrs, MapLocation currentTarget) {
        MapLocation closestPastr = null;
        double closestDist = Integer.MAX_VALUE;
        double currDist;
        
        // Determine if our current pastr target has been killed while simultaneously finding the next closest one.
        for (int i = 0; i < enemyPastrs.length; i ++) {
            if (currentTarget != null && enemyPastrs[i].equals(currentTarget)) {
                // We are already heading towards this one, so lets not do anything else
                return currentTarget;
            } else {
                currDist = enemyPastrs[i].distanceSquaredTo(InfoCache.HQLocation);
                if (currDist < closestDist) {
                    closestPastr = enemyPastrs[i];
                    closestDist = currDist;
                }
            }
        }
        return closestPastr;
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
