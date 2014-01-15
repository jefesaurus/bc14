package ourBot_newComm.robots;

import ourBot_newComm.BasicPathing;
import ourBot_newComm.BreadthFirst;
import ourBot_newComm.Comms;
import ourBot_newComm.managers.InfoArray.Command;
import ourBot_newComm.util.VectorFunctions;
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
    int squadNum = 0;
    Command currentCommand;

    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);          
        squadNum = comms.getNewSpawnSquad();
        rc.setIndicatorString(1, "Squad: " + squadNum);

        BreadthFirst.rc = rc;
    }

    @Override
    public void run() throws GameActionException {
      //follow orders from HQ
        currentCommand = comms.getSquadCommand(squadNum);
        rc.setIndicatorString(2, currentCommand.toString());


        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        MapLocation[] robotLocations = VectorFunctions.robotsToLocationsRemoveHQ(enemyRobots, rc);
        
        if(robotLocations.length > 0){//SHOOT AT, OR RUN TOWARDS, ENEMIES
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
            MapLocation destination;
            switch (currentCommand.type) {
            case RALLY_POINT:
            case ATTACK_POINT:
            case PASTR_POINT:
            default:
                destination = currentCommand.loc;
            }
            Direction towardClosest = rc.getLocation().directionTo(destination);
            simpleMove(towardClosest, rc);
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
}