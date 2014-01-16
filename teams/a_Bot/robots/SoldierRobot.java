package a_Bot.robots;

import a_Bot.BreadthFirst;
import a_Bot.managers.InfoCache;
import a_Bot.managers.InfoArray.Command;
import a_Bot.navigation.NavigationMode;
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
            for(int directionalOffset:directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.move(trialDir);
                    break;
                }
            }
        }
    }
    
    private static void simpleMoveVeerOff(Direction chosenDirection, RobotController rc) throws GameActionException{
        if(rc.isActive()){
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = allDirections[(forwardInt+directionalOffset+8)%8];
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
        double enemyHealth = 0;
        int enemyCentroidX = 0;
        int enemyCentroidY = 0;
        
        // Apparently the current robot isn't counted, so we manually add its values
        int numAllies = allies.length + 1;
        double allyHealth = rc.getHealth();
        int allyCentroidX = rc.getLocation().x;
        int allyCentroidY = rc.getLocation().x;
              
        /*
        RobotInfo[] attackable = new RobotInfo[500];
        int numAttackableRobots = 0;
        */
        RobotInfo lowestHealthAttackable = null;
        double lowestHealth = 10000000;
        
        int numActiveEnemies = 0;
        int numActiveAllies = 0;
        
        for (Robot b : enemies) {
            RobotInfo info = rc.senseRobotInfo(b);
            enemyCentroidX += info.location.x;
            enemyCentroidY += info.location.y;
            enemyHealth += info.health;
            if (rc.canAttackSquare(info.location)) {
                //attackable[numAttackableRobots++] = info;
                if (info.health < lowestHealth) {
                    lowestHealthAttackable = info;
                    lowestHealth = info.health;
                }
            }
            if (info.actionDelay < 1.0) {
                numActiveEnemies++;
            }
        }
        
        for (Robot b : allies) {
            RobotInfo info = rc.senseRobotInfo(b);
            allyCentroidX += info.location.x;
            allyCentroidY += info.location.y;
            if (info.type == RobotType.SOLDIER) {
                allyHealth += info.health;
            }
            if (info.actionDelay < 1.0) {
                numActiveAllies++;
            }
        }
        
        MapLocation enemyCentroid = new MapLocation(enemyCentroidX/numEnemies, enemyCentroidY/numEnemies);
        MapLocation allyCentroid = new MapLocation(allyCentroidX/numAllies, allyCentroidY/numAllies);

        enemyHealth /= (numEnemies + .001);
        allyHealth /= (numAllies + .001);
        //MapLocation allyCentroid = new MapLocation(allyCentroidX/numEnemies, allyCentroidY/numEnemies);
        rc.setIndicatorString(0, "Allies: " + numAllies + " Enemies: " + numEnemies);

        /*
        boolean retreat = numActiveEnemies > numActiveAllies ||
                rc.getHealth() < allyHealth ||
                allyHealth*numAllies < enemyHealth*numEnemies;
        */
        String retreatReason = "";
        MapLocation retreatLoc = null;
        if (numActiveEnemies > numActiveAllies){
            retreatLoc = InfoCache.HQLocation;
            retreatReason = ", More active enemies";
        } else if (rc.getHealth() < allyHealth) {
            retreatLoc = allyCentroid;
            retreatReason = ", Average ally health is higher";
        } else if (allyHealth*numAllies < enemyHealth*numEnemies) {
            retreatLoc = InfoCache.HQLocation;
            retreatReason = ", Total enemy health is higher";
        }
        
        if (retreatLoc != null) {
            Direction awayDir = center.directionTo(enemyCentroid).opposite();

            if (rc.getHealth() < RobotType.SOLDIER.attackPower*numEnemies) {
                rc.setIndicatorString(0, "Retreat away: " + awayDir.toString() +retreatReason);
                simpleMove(awayDir, rc);

            } else {
                Direction toMove = center.directionTo(retreatLoc);
                rc.setIndicatorString(0, "Retreat to allies: " + toMove.toString() +retreatReason);
                simpleMove(toMove, rc);
            }

        } else {
            if (lowestHealthAttackable != null && lowestHealthAttackable.type != RobotType.HQ) {
                // If we can attack, we do
                rc.attackSquare(lowestHealthAttackable.location);
                
                // TODO tune what we consider a good enough advantage
            } else if ((numActiveAllies - numActiveEnemies) >= 2 ){
                //Strong enough support, lets advance
                Direction toEnemy = center.directionTo(enemyCentroid).rotateLeft();

                // If we are nearer to the enemy than the rest of our squad, then veer off sideways
                // To allow them to catch up and to aid concavity
                if (center.distanceSquaredTo(enemyCentroid) < allyCentroid.distanceSquaredTo(enemyCentroid)) {
                    // TODO make sure this is actually functioning like it is supposed to
                    simpleMoveVeerOff(toEnemy, rc);
                } else {
                    simpleMove(toEnemy, rc);
                }
            } else {
                // Hold ground, don't waste movement
                rc.yield();
            }
        }
    }
}