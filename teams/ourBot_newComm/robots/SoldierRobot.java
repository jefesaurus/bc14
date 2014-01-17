package ourBot_newComm.robots;

import ourBot_newComm.BreadthFirst;
import ourBot_newComm.util.*;
import ourBot_newComm.managers.InfoCache;
import ourBot_newComm.managers.InfoArray.Command;
import ourBot_newComm.navigation.NavigationMode;
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
    
    static int pathCreatedRound = -1;
    int squadNum = 0;
    Command currentCommand;
    public final BehaviorState state;
    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);          
        squadNum = comms.getNewSpawnSquad();
        rc.setIndicatorString(1, "Squad: " + squadNum);
        nav.setNavigationMode(NavigationMode.BUG);
        
        if (Clock.getRoundNum() < GameConstants.HQ_SPAWN_DELAY_CONSTANT_1) {
            System.out.println("PASTR " + rc.getRobot().getID());
            state = BehaviorState.BUILD_PASTR;
        } 
        else if (Clock.getRoundNum() < 2*GameConstants.HQ_SPAWN_DELAY_CONSTANT_1) {
            state = BehaviorState.BUILD_NOISE_TOWER;
            System.out.println("NOISE TOWER " + rc.getRobot().getID());
        }
        else {
            state = BehaviorState.SQUADING;
            System.out.println("SQUADING " + rc.getRobot().getID());
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
                    
            if(nearbyEnemies.length > 0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
                if (rc.isActive()) {
                    moveDuringBattle(nearbyEnemies);
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
                nav.setDestination(destination);
                Direction toMove = nav.navigateToDestination();
                if (toMove != null) {
                    simpleMove(toMove, rc);
                }
            }
            break;
        case BUILD_PASTR:
            rc.setIndicatorString(2, "COWGROWTH COMPUTATION");
            MapLocation pastr_loc = rc.getLocation();//new CowGrowth(rc).getBestLocation();
            rc.setIndicatorString(2, "DONE WITH COWGROWTH COMPUTATION");
            comms.setPastrLoc(pastr_loc);
            nav.setDestination(pastr_loc);
            
            while (rc.getLocation().distanceSquaredTo(pastr_loc) > 5) {
                Direction toMove = nav.navigateToDestination();
                if (toMove != null) {
                    simpleMove(toMove, rc);
                }
                rc.yield();
            }
            
            comms.setPastrLoc(rc.getLocation());
            
            while (true) {
                if (rc.isActive()) {
                    rc.construct(RobotType.PASTR);
                    break;
                }
                rc.yield();
            }
            break;
        case BUILD_NOISE_TOWER:
            rc.setIndicatorString(2, "NOISE TOWA");
            MapLocation tower_loc = comms.getPastrLoc();
            System.out.println("tower_loc: " + tower_loc);
            nav.setDestination(tower_loc); 
            rc.setIndicatorString(2, "NOISE TOWA: " + tower_loc.toString());
            
            while (rc.getLocation().distanceSquaredTo(tower_loc) > 5) {
                Direction toMove1 = nav.navigateToDestination();
                if (toMove1 != null) {
                    simpleMove(toMove1, rc);
                }
                rc.yield();
            }
            
            MapLocation adjacent = comms.getPastrLoc();
            adjacent = findAdjacentSquare(adjacent);
            nav.setDestination(adjacent);
            rc.setIndicatorString(2, "NOISE TOWA Adjacent square: " + adjacent.toString() + "nav destination: " + nav.getDestination().toString());
            while (rc.getLocation() != adjacent) {
                Direction toMove2 = nav.navigateToDestination();
                if (toMove2 != null) {
                    simpleMove(toMove2, rc);
                }
                rc.yield();
            }
            
            while (true) {
                if (rc.isActive()) {
                    rc.construct(RobotType.NOISETOWER);
                    break;
                }
                rc.yield();
            }
            break;
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

    private static void simpleMove(Direction chosenDirection, RobotController rc) throws GameActionException{
        if(rc.isActive()){
            for(int directionalOffset:directionalLooks){
                int forwardInt = chosenDirection.ordinal();
                Direction trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.move(trialDir);
                    break;
                }
            }
        }
    }
    
    
    /*
     * BATTLE MICRO...
     * 
     */
    
    static final int SOLDIER_SIGHT_RANGE = RobotType.SOLDIER.sensorRadiusSquared;
    static final int SOLDIER_ATTACK_RANGE = RobotType.SOLDIER.attackRadiusMaxSquared;
    
    public void moveDuringBattle(Robot[] enemies) throws GameActionException {
        //int c_start = Clock.getBytecodeNum();
        Robot[] allies = rc.senseNearbyGameObjects(Robot.class, SOLDIER_SIGHT_RANGE, rc.getTeam());
        
        //botMap.clear();
        
        MapLocation center = rc.getLocation();
        
        int numEnemies = enemies.length;
        //int enemyHealth = 0;
        int enemyCentroidX = 0;
        int enemyCentroidY = 0;
        
        int numAllies = allies.length;
        //int allyHealth = 0;
        //int allyCentroidX = 0;
        //int allyCentroidY = 0;
              
        /*
        RobotInfo[] attackable = new RobotInfo[500];
        int numAttackableRobots = 0;
        */
        RobotInfo lowestHealthAttackable = null;
        double lowestHealth = 10000000;
        
        for (Robot b : enemies) {
            RobotInfo info = rc.senseRobotInfo(b);
            enemyCentroidX += info.location.x;
            enemyCentroidY += info.location.y;
            if (rc.canAttackSquare(info.location)) {

                //attackable[numAttackableRobots++] = info;
                if (info.health < lowestHealth) {
                    lowestHealthAttackable = info;
                    lowestHealth = info.health;
                }
            }   
        }
        
        MapLocation enemyCentroid = new MapLocation(enemyCentroidX/numEnemies, enemyCentroidY/numEnemies);
        //MapLocation allyCentroid = new MapLocation(allyCentroidX/numEnemies, allyCentroidY/numEnemies);
        rc.setIndicatorString(0, "Allies: " + numAllies + " Enemies: " + numEnemies);

        if (numAllies > numEnemies) {
            // Offense
            if (lowestHealthAttackable != null && lowestHealthAttackable.type != RobotType.HQ) {
                rc.attackSquare(lowestHealthAttackable.location);
            } else {
                simpleMove(center.directionTo(enemyCentroid), rc);
            }
        } else {
            nav.setDestination(ic.HQLocation);
            Direction toMove = nav.navigateToDestination();
            if (toMove != null) {
                simpleMove(toMove, rc);
            }
        }
    }
}