package b_bot.robots;

import b_bot.util.Util;
import b_bot.Constants;
import b_bot.managers.InfoCache;
import b_bot.managers.InfoArray.Command;
import b_bot.navigation.NavigationMode;
import b_bot.util.*;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.GameConstants;
import battlecode.common.TerrainTile;

public class SoldierRobot extends BaseRobot {
    
    public enum BehaviorState {
        BUILD_PASTR,
        BUILD_NOISE_TOWER,
        SQUADING
    }
    
    public enum ConstructionState {
        INIT, 
        MOVE_TO_COARSE_LOC, 
        MOVE_TO_EXACT_LOC
    }
    
    
    static int pathCreatedRound = -1;
    int squadNum = 0;
    Command currentCommand;
    public final BehaviorState state;
    
    //PASTR and NOISETOWER vars
    public final int FUZZY_BUILDING_PLACEMENT = 5;
    public ConstructionState cstate;
    public MapLocation adjacent;
    public MapLocation tower_loc;
    public MapLocation pastr_loc;
    
    
    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);          
        squadNum = comms.getNewSpawnSquad();
        rc.setIndicatorString(1, "Squad: " + squadNum);
        nav.setNavigationMode(NavigationMode.BUG);
        
        if (Clock.getRoundNum() < GameConstants.HQ_SPAWN_DELAY_CONSTANT_1) {
            state = BehaviorState.BUILD_PASTR;
            cstate = ConstructionState.INIT;
        } 
        else if (Clock.getRoundNum() < 2*GameConstants.HQ_SPAWN_DELAY_CONSTANT_1) {
            state = BehaviorState.BUILD_NOISE_TOWER;
            cstate = ConstructionState.INIT;
        }
        else {
            state = BehaviorState.SQUADING;
        }
    }

    @Override
    public void run() throws GameActionException {
      //follow orders from HQ
        switch(this.state) {
        case SQUADING:
            currentCommand = comms.getSquadCommand(squadNum);
            rc.setIndicatorString(2, currentCommand.toString());
    
            Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
                    
            if(nearbyEnemies.length > 0){
                if (rc.isActive()) {
                    offensiveMicro(nearbyEnemies);
                }
            } else {
                MapLocation destination;
                switch (currentCommand.type) {
                case RALLY_POINT:
                case ATTACK_POINT:
                case PASTR_POINT:
                default:
                    destination = currentCommand.loc;
                }
                nav.setDestination(destination);
                Direction toMove = nav.navigateToDestination();
                if (toMove != null) {
                    simpleMove(toMove);
                }
            }
            break;
        case BUILD_PASTR:
            switch (this.cstate) {
            case INIT: 
                rc.setIndicatorString(2, "COWGROWTH COMPUTATION");
                this.pastr_loc = new CowGrowth(rc, this).getBestLocation();
                //this.pastr_loc = rc.getLocation();
                //this.pastr_loc = new MapLocation(40,32);
                rc.setIndicatorString(2, "DONE WITH COWGROWTH COMPUTATION");
                comms.setPastrLoc(pastr_loc);
                nav.setDestination(pastr_loc);
                this.cstate = ConstructionState.MOVE_TO_COARSE_LOC;
                break;
                
            case MOVE_TO_COARSE_LOC:
                
                if (rc.getLocation().distanceSquaredTo(pastr_loc) > FUZZY_BUILDING_PLACEMENT) {
                    Direction toMove = nav.navigateToDestination();
                    if (toMove != null) {
                        simpleMove(toMove);
                    }
                } else {
                    comms.setPastrLoc(rc.getLocation());
                    this.cstate = ConstructionState.MOVE_TO_EXACT_LOC;
                }
                break;
                
            case MOVE_TO_EXACT_LOC: 
                
                if (rc.isActive()) {
                    rc.construct(RobotType.PASTR);
                    break;
                }  
                
            }
            break;
        case BUILD_NOISE_TOWER:
            switch (this.cstate) {
            case INIT:
                tower_loc = comms.getPastrLoc();
                nav.setDestination(tower_loc); 
                rc.setIndicatorString(2, "NOISE TOWA: " + tower_loc.toString());
                this.cstate = ConstructionState.MOVE_TO_COARSE_LOC;
                break;  
            case MOVE_TO_COARSE_LOC:
                if (rc.getLocation().distanceSquaredTo(tower_loc) > FUZZY_BUILDING_PLACEMENT) {
                    Direction toMove1 = nav.navigateToDestination();
                    if (toMove1 != null) {
                        simpleMove(toMove1);
                    }
                } else {
                    rc.setIndicatorString(2, "found adjacent square");
                    adjacent = comms.getPastrLoc();
                    adjacent = findAdjacentSquare(adjacent);
                    nav.setDestination(adjacent);
                    this.cstate = ConstructionState.MOVE_TO_EXACT_LOC;
                }
                break;
            case MOVE_TO_EXACT_LOC:
                if (!rc.getLocation().equals(adjacent)) {
                    rc.setIndicatorString(2, "curloc: " + rc.getLocation().toString() + "adjacent: " + adjacent.toString());
                    Direction toMove2 = nav.navigateToDestination();
                    if (toMove2 != null) {
                        simpleMove(toMove2);
                    }
                } else {
                    if (rc.isActive()) {
                        rc.setIndicatorString(2, "building noisetower");
                        rc.construct(RobotType.NOISETOWER);
                    }
                }
                break;
            }
        }
    }
    
    private MapLocation findAdjacentSquare(MapLocation loc) {
        Direction dir = Direction.values()[(int) (Util.randDouble() * 8)];
        MapLocation curLoc = loc.add(dir);
        while (rc.senseTerrainTile(curLoc) == TerrainTile.VOID || rc.senseTerrainTile(curLoc) == TerrainTile.OFF_MAP) {
            dir = dir.rotateRight();
            curLoc = loc.add(dir);
        }
        return curLoc;
    }

    
    public void simpleBug(MapLocation destination) throws GameActionException {
        nav.setDestination(destination);
        Direction toMove = nav.navigateToDestination();
        if (toMove != null) {
            simpleMove(toMove);
        }
        
    }
    
    protected void simpleMove(Direction chosenDirection) throws GameActionException{
        if(chosenDirection == Direction.OMNI || chosenDirection == Direction.NONE){
            rc.yield();
            return;
        }
        if(rc.isActive()){
            if(rc.canMove(chosenDirection)){
                rc.move(chosenDirection);
                return;
            }
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:Constants.directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = Constants.allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.move(trialDir);
                    break;
                }
            }
        }
    }
    
    protected void simpleMoveVeerOff(Direction chosenDirection) throws GameActionException{
        if(rc.isActive()){
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:Constants.directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = Constants.allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.move(trialDir);
                    break;
                }
            }
        }
    } 
    
    
    
    /*
     * BATTLE MICRO
     */
    
    public void offensiveMicro(Robot[] nearbyEnemies) throws GameActionException {
        // Enemy unittype counters
        int numEnemies = nearbyEnemies.length;
        boolean enemyHQInSight = false;
        boolean enemyHQInRange = false;

        int numEnemySoldiers = 0;
        int numEnemyPastrs = 0;
        int numEnemyNoiseTowers = 0;

        // Useful metrics
        double enemyNumHits = 0;

        int enemyCentroidX = 0;
        int enemyCentroidY = 0;

        int potentialDamage = 0; // Theoretically this is the most damage the robot could take.

        // Tally up counters & metrics
        RobotInfo lowestHealthAttackableSoldier = null;
        double lowestSoldierHealth = Integer.MAX_VALUE;
        MapLocation pastrLoc = null;
        MapLocation towerLoc;

        for (Robot b : nearbyEnemies) {
            RobotInfo info = rc.senseRobotInfo(b);

            switch(info.type) {
            case SOLDIER:
                numEnemySoldiers++;
                enemyCentroidX += info.location.x;
                enemyCentroidY += info.location.y;
                if (rc.canAttackSquare(info.location)) {
                    potentialDamage += RobotType.SOLDIER.attackPower;
                    enemyNumHits += 1 + (int)info.health;
                    if (info.health < lowestSoldierHealth) {
                        lowestHealthAttackableSoldier = info;
                        lowestSoldierHealth = info.health;
                    }
                }
                break;

            case PASTR:
                numEnemyPastrs++;
                pastrLoc = info.location;
                break;

            case NOISETOWER:
                numEnemyNoiseTowers++;
                towerLoc = info.location;
                break;

            case HQ:
                enemyHQInSight = true;
                if (this.curLoc.distanceSquaredTo(info.location) <= RobotType.HQ.attackRadiusMaxSquared) {
                    enemyHQInRange = true;
                    potentialDamage += RobotType.HQ.attackPower;
                }
                break;

            default:
                break;
            }
        }
        MapLocation enemyCentroid;
        
        if (numEnemySoldiers > 0) {
            // Finalize the averaged metrics
            enemyCentroid = new MapLocation(enemyCentroidX/numEnemySoldiers, enemyCentroidY/numEnemySoldiers);
        } else {
            enemyCentroid = this.enemyHQ;
        }





        Robot[] allies = rc.senseNearbyGameObjects(Robot.class, Constants.SOLDIER_SIGHT_RANGE, rc.getTeam());

        // Enemy unittype counters
        int numAllySoldiers = 0;

        // Useful metrics
        double allyAverageSoldierHealth = 0;
        double allyNumHits = 0;

        int allyCentroidX = 0;
        int allyCentroidY = 0;

        for (Robot b : allies) {
            RobotInfo info = rc.senseRobotInfo(b);

            if (info.type == RobotType.SOLDIER) {
                numAllySoldiers++;
                allyAverageSoldierHealth += info.health;
                allyCentroidX += info.location.x;
                allyCentroidY += info.location.y;
                allyNumHits += 1 + (int)(info.health/RobotType.SOLDIER.attackPower);
            } else if(info.type == RobotType.HQ) {
                numAllySoldiers++;
                //allyCentroidX += 3*info.location.x; // HQ counts as 3 allied soldiers
                //allyCentroidY += 3*info.location.y;
            }
        }

        MapLocation allyCentroid;
        // Finalize the averaged metrics
        if (numAllySoldiers > 0) {
            allyCentroid = new MapLocation(allyCentroidX/numAllySoldiers, allyCentroidY/numAllySoldiers);
            allyAverageSoldierHealth /= (numAllySoldiers);
        } else {
            allyCentroid = this.curLoc;
        }
        
        boolean enemyHQPastrCombo = enemyHQInSight && (pastrLoc != null) && (this.enemyHQ.distanceSquaredTo(pastrLoc) < 9);
        int unitDisadvantage = numEnemySoldiers - numAllySoldiers;
        boolean healthDisadvantage = rc.getHealth() < allyAverageSoldierHealth;
        
        
        
        // Retreat logic:
        // Only attack HQ pastr combo if we have unit parity/advantage
        if (enemyHQPastrCombo) {
            if (unitDisadvantage < -1) {
                // Attack PASTR
                if (rc.canAttackSquare(pastrLoc)) {
                    rc.attackSquare(pastrLoc);
                } else {
                    simpleBug(pastrLoc);
                }    
            } else {
                simpleBug(this.myHQ);
            }
            
        // If we are in HQ range but there is no pastr, or we have a unit disdvantage, we need to bounce
        } else if(enemyHQInRange || unitDisadvantage > 0) {
            // Retreat away or home
            Direction toMove = this.curLoc.directionTo(this.myHQ);
            rc.setIndicatorString(0, "retreating because enemy hq or unit disad");
            simpleMove(toMove);
        } else if(healthDisadvantage) {
            // Retreat to center of allies
            Direction toMove = this.curLoc.directionTo(allyCentroid);
            rc.setIndicatorString(0, "retreating because health disad");
            simpleMove(toMove);
        }

        if (lowestHealthAttackableSoldier != null) {
            // If we can attack, we do
            rc.setIndicatorString(0, "attacking");
            if(rc.isActive()){ 
                rc.attackSquare(lowestHealthAttackableSoldier.location);
            }

            // TODO tune what we consider a good enough advantage
        } else if (unitDisadvantage < 0 && !enemyHQInSight) {
            //Strong enough support, lets advance
            rc.setIndicatorString(0, "Advancing");

            Direction toEnemy = this.curLoc.directionTo(enemyCentroid);//.rotateLeft();

            // If we are nearer to the enemy than the rest of our squad, then veer off sideways
            // To allow them to catch up and to aid concavity
            if (this.curLoc.distanceSquaredTo(enemyCentroid) < allyCentroid.distanceSquaredTo(enemyCentroid)) {
                // TODO make sure this is actually functioning like it is supposed to
                simpleMoveVeerOff(toEnemy);
            } else {
                simpleBug(enemyCentroid);
            }
            
        } else {
            rc.setIndicatorString(0, "Yielding");

            // Hold ground, don't waste movement
            rc.yield();
        }
    }
}