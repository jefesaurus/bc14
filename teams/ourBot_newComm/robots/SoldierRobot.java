package ourBot_newComm.robots;

import ourBot_newComm.BreadthFirst;
import ourBot_newComm.managers.InfoCache;
import ourBot_newComm.managers.InfoArray.Command;
import ourBot_newComm.navigation.NavigationMode;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class SoldierRobot extends BaseRobot {
    static int pathCreatedRound = -1;
    int squadNum = 0;
    Command currentCommand;

    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);          
        squadNum = comms.getNewSpawnSquad();
        rc.setIndicatorString(1, "Squad: " + squadNum);
        nav.setNavigationMode(NavigationMode.BUG);
        
        BreadthFirst.rc = rc;
    }

    @Override
    public void run() throws GameActionException {
      //follow orders from HQ
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
        Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam());
        
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
        
        if (numAllies > numEnemies) {
            // Offense
            if (lowestHealthAttackable != null && lowestHealthAttackable.type != RobotType.HQ) {
                rc.attackSquare(lowestHealthAttackable.location);
            } else {
                simpleMove(center.directionTo(enemyCentroid), rc);
            }
        } else {
            simpleMove(center.directionTo(rc.senseHQLocation()), rc); 
        }
    }
}