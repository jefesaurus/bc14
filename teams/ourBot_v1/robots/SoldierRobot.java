package ourBot_v1.robots;

import ourBot_v1.BasicPathing;
import ourBot_v1.NavigationMode;
import ourBot_v1.NavigationSystem;
import ourBot_v1.BreadthFirst;
import ourBot_v1.Comms;
import ourBot_v1.util.VectorFunctions;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class SoldierRobot extends BaseRobot {
    private enum BehaviorState {
        /** No enemies to deal with, swarming. */
        SWARM,
        /** Heard of an enemy spotted call, but no enemy info calls yet. */
        SEEK,
        /** Far from target. Use bug to navigate. */
        LOST,
        /** Tracking closest enemy, even follow them for 12 turns. */
        ENEMY_DETECTED,
    }

    static int pathCreatedRound = -1;
    public final NavigationSystem nav;
    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);
        nav = new NavigationSystem(this);
        nav.setNavigationMode(NavigationMode.BUG);
        BreadthFirst.rc = rc;
    }

    @Override
    public void run() throws GameActionException {
      //follow orders from HQ
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        MapLocation[] robotLocations = VectorFunctions.robotsToLocationsRemoveHQ(enemyRobots, rc);
        
        if(robotLocations.length>0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
            MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
            if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){//close enough to shoot
                if(rc.isActive()){
                    rc.attackSquare(closestEnemyLoc);
                }
            }else{//not close enough to shoot, so try to go shoot
                Direction towardClosest = rc.getLocation().directionTo(closestEnemyLoc);
                simpleMove(towardClosest, rc);
            }
        } else {//NAVIGATION BY DOWNLOADED PATH
            rc.setIndicatorString(0, "team "+myBand+", path length "+path.size());
            if(path.size()<=1){
                //check if a new path is available
                int broadcastCreatedRound = rc.readBroadcast(myBand);
                if(pathCreatedRound<broadcastCreatedRound){
                    rc.setIndicatorString(1, "downloading path");
                    pathCreatedRound = broadcastCreatedRound;
                    path = Comms.downloadPath(rc);
                }
            }
            if(path.size() > 0){
                //follow breadthFirst path
                Direction bdir = BreadthFirst.getNextDirection(path, bigBoxSize);
                BasicPathing.tryToMove(bdir, true, rc, directionalLooks, allDirections);
            }
        }
        //Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        //BasicPathing.tryToMove(towardEnemy, true, rc, directionalLooks, allDirections);//was Direction.SOUTH_EAST

        //Direction towardEnemy = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        //simpleMove(towardEnemy);    
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
}