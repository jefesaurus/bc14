package team169.robots;

import team169.Constants;
import team169.managers.InfoCache;
import team169.managers.InfoArray.BattleFront;
import team169.managers.InfoArray.BuildingInfo;
import team169.managers.InfoArray.BuildingType;
import team169.managers.InfoArray.Command;
import team169.managers.InfoArray.CommandType;
import team169.util.CowGrowth;
import team169.util.VectorFunctions;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
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
    
 
    
    public HQLocation HQ_LOCATION;
    static MapLocation rallyPoint;    
    static MapLocation defenseRallyPoint = null;    
    public boolean WINNING = false;
    Direction directionToEnemyHQ;
    
    // Our favored pastr location
    MapLocation bestPastrLoc = null;

    double NumUnitsProduced = 0;
    double DECAY_RATE = .9997;

    public HQRobot(RobotController rc) throws GameActionException {
        super(rc);
        //rallyPoint = VectorFunctions.mldivide(VectorFunctions.mladd(this.myHQ, this.enemyHQ), 3);//, 2);
        rallyPoint = VectorFunctions.mldivide(VectorFunctions.mladd(this.myHQ, this.enemyHQ), 2);
        //rallyPoint = enemyHQ;
        directionToEnemyHQ = this.myHQ.directionTo(this.enemyHQ);
        
        
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
    
    MapLocation currentPastrTarget = null;
    public boolean MILK_ADVANTAGE = true;
    public int SWARM_SIZE = 4;
    public int WINNING_ADVANTAGE = 6;
    @Override
    public void run() throws GameActionException {
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        tryToAttack(enemyRobots);
        
        Direction spawnDir = null;
        if (enemyRobots.length > 0) {
            int enemyCentroidX = 0;
            int enemyCentroidY = 0;
            int numEnemySoldiers = 0;
            for (Robot r : enemyRobots) {
                RobotInfo info = rc.senseRobotInfo(r);
                if (info.type == RobotType.SOLDIER) {
                    enemyCentroidX += info.location.x;
                    enemyCentroidY += info.location.y;
                    numEnemySoldiers++;
                }
            }
            if (numEnemySoldiers > 0) {
                spawnDir = new MapLocation(enemyCentroidX / numEnemySoldiers, enemyCentroidY / numEnemySoldiers).directionTo(this.myHQ);
            }
        }
        
        NumUnitsProduced *= DECAY_RATE;
        currentPastrTarget = getPastrTarget(currentPastrTarget);

        double ourTotalDeaths = NumUnitsProduced - rc.senseRobotCount();

        
        if (ourTotalDeaths + WINNING_ADVANTAGE < comms.readKillCount() && MILK_ADVANTAGE) {
            WINNING = true;
        } else {
            WINNING = false;
        }
        
        if (currentPastrTarget != null && currentPastrTarget.distanceSquaredTo(this.enemyHQ) <= 9 && Clock.getRoundNum() <= 300) {
            WINNING = true;
        }
        
        boolean PASTR_UNDER_ATTACK = comms.checkPastrAlarm();
        
        //System.out.println("we are winning: " + WINNING);
        if (!WINNING) {
            if (currentPastrTarget != null) {
                Command attackPastr = new Command(CommandType.ATTACK_PASTR, currentPastrTarget);

                trySpawnSquadMember(1000000, rallyPoint, attackPastr, spawnDir);
                for (int i = 2; i <= squadNumber; i ++) {
                    comms.sendSquadCommand(i, attackPastr);
                }
                //System.out.println("Detected pastr at: " + currentPastrTarget.toString());
            } else {
                MILK_ADVANTAGE = true;
                MapLocation enemyCentroid;
                BattleFront existing = comms.getBattle();
                if (curRound - existing.roundNum < 5) {
                    enemyCentroid = existing.enemyCentroid;
                } else {
                    enemyCentroid = rallyPoint;
                }
                Command attackGeneral = new Command(CommandType.ATTACK_PASTR, enemyCentroid);
                trySpawnSquadMember(10000, rallyPoint, attackGeneral, spawnDir);
                for (int i = 2; i <= squadNumber; i ++) {
                    comms.sendSquadCommand(i, attackGeneral);
                }
            }
        } else {
            if (rc.senseTeamMilkQuantity(rc.getTeam()) < rc.senseTeamMilkQuantity(rc.getTeam().opponent()) && currentPastrTarget != null) {
                MILK_ADVANTAGE = false;
            } else {
                MILK_ADVANTAGE = true;
            }

            if (PASTR_UNDER_ATTACK) {
                Command attackEnemiesAtOurPastr = new Command(CommandType.ATTACK_POINT, bestPastrLoc);
                for (int i = 2; i <= squadNumber; i ++) {
                    comms.sendSquadCommand(i, attackEnemiesAtOurPastr);
                }
            }
            if (bestPastrLoc == null) {
                //System.out.println("Trying to spawn initial pastr");

                if (!trySpawnTower()) {
                    return;
                }
                //System.out.println("Starting position calc");

                calcBestPastrLocation();
                //System.out.println("Finished position calc");

            }
            
            // Spawn Pastr if we need to
            if (!isTowerAlive()) {
                trySpawnTower();
                
            // Spawn Tower if we need to
            } else if (!isPastrAlive()) {
                trySpawnPastr();
                
            // Defend.
            } else {
                Command defendPastr = new Command(CommandType.DEFEND_PASTR, bestPastrLoc);
                trySpawnSquadMember(10000, rallyPoint, defendPastr, spawnDir);
                for (int i = 2; i <= squadNumber; i ++) {
                    comms.sendSquadCommand(i, defendPastr);
                }
            }
        }
    }
    
    public void tryToAttack(Robot[] enemyRobots) throws GameActionException {
        if (!rc.isActive()) {
            return;
        }
        boolean canDirectAttack = false;
        MapLocation lowestHealthLoc = null;
        double lowestSoldierHealth = Integer.MAX_VALUE;
        for (Robot b : enemyRobots) {
            RobotInfo info = rc.senseRobotInfo(b);
            if (info.type == RobotType.SOLDIER) {
                int distTo = myHQ.distanceSquaredTo(info.location);
                if (canDirectAttack) {
                    if (distTo <= 15 && info.health < lowestSoldierHealth) {
                        lowestHealthLoc = info.location;
                        lowestSoldierHealth = info.health;        
                    }
                } else {
                    if (distTo <= 15) {
                        canDirectAttack = true;
                        lowestHealthLoc = info.location;
                        lowestSoldierHealth = info.health;        
                    } else if (info.health < lowestSoldierHealth) {
                        if(distTo <= 24 || (distTo == 25 && info.location.x != myHQ.x && info.location.y != myHQ.y)) {
                            lowestHealthLoc = info.location;
                            lowestSoldierHealth = info.health; 
                        }      
                    }
                }
            }
        }
        if (lowestHealthLoc != null && rc.isActive()) {
            if (canDirectAttack) {
                rc.attackSquare(lowestHealthLoc);
            } else {
                MapLocation toAttack = lowestHealthLoc.add(lowestHealthLoc.directionTo(curLoc));

                if (!rc.canAttackSquare(toAttack)) {
                    toAttack = toAttack.add(toAttack.directionTo(curLoc));
                }
                rc.attackSquare(lowestHealthLoc.add(lowestHealthLoc.directionTo(curLoc)));
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
            bestPastrLoc = comms.wait_PASTR_LOC_FINAL();
        }
    }


    public boolean isPastrAlive() throws GameActionException {
        BuildingInfo info = comms.getBuildingStatus(BuildingType.PASTR);
        
        switch (info.status) {
        case IS_COMPUTING:
        case MOVING_TO:
        case ALL_GOOD:
        case UNDER_ATTACK:
            return ((curRound - info.roundNum) < 2); // If the round number hasn't been updated in 2 rounds, it is dead
        case IN_CONSTRUCTION:
            return ((curRound - info.roundNum) < 101);
        case NOTHING:
        default:
            return false;
        }
    }
    
    public boolean isTowerAlive() throws GameActionException {
        BuildingInfo info = comms.getBuildingStatus(BuildingType.TOWER);
        
        switch (info.status) {
        case IS_COMPUTING:
        case MOVING_TO:
        case ALL_GOOD:
        case UNDER_ATTACK:
            return ((curRound - info.roundNum) < 5); // If the round number hasn't been updated in 2 rounds, it is dead
        case IN_CONSTRUCTION:
            return ((curRound - info.roundNum) < 201);
        case NOTHING:
        default:
            return false;
        }
    }
    
    public boolean trySpawnPastr() throws GameActionException {
        if (bestPastrLoc != null) {
            if (tryToSpawn(myHQ.directionTo(bestPastrLoc))) {
                comms.setNewSpawnSquad(0);
                comms.sendSquadCommand(0, new Command(CommandType.BUILD_PASTR, myHQ));
                return true;
            }
        } else {
            if (tryToSpawn(directionToEnemyHQ)) {
                comms.setNewSpawnSquad(0);
                comms.sendSquadCommand(0, new Command(CommandType.BUILD_PASTR, myHQ));
                return true;
            }
        }
        return false;
    }
    
    public boolean trySpawnTower() throws GameActionException {
        if (bestPastrLoc != null) {
            if (tryToSpawn(myHQ.directionTo(bestPastrLoc))) {
                comms.setNewSpawnSquad(1);
                comms.sendSquadCommand(1, new Command(CommandType.BUILD_NOISE_TOWER, myHQ));
                return true;
            }
        } else {
            if (tryToSpawn(directionToEnemyHQ)) {
                comms.setNewSpawnSquad(1);        
                comms.sendSquadCommand(1, new Command(CommandType.BUILD_NOISE_TOWER, myHQ));
                return true;
            }
        }
        return false;
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
    
    public boolean trySpawnSquadMember(int numBots, MapLocation rallyPoint, Command command, Direction preferred) throws GameActionException {
        if (preferred == null) {
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
        } else {
            if (tryToSpawn(preferred)) {
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
