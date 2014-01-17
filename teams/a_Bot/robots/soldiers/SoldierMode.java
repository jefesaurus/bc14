package a_Bot.robots.soldiers;

import a_Bot.Constants;
import a_Bot.robots.SoldierRobot;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class SoldierMode {
    protected SoldierRobot thisBot;
    protected final RobotController rc;
    public SoldierMode(SoldierRobot thisBot) throws GameActionException {
        this.thisBot = thisBot;
        this.rc = thisBot.rc;
    }

    
    public void step() throws GameActionException {
        rc.yield();
    }
    
    public void simpleBug(MapLocation destination) throws GameActionException {
        thisBot.nav.setDestination(destination);
        Direction toMove = thisBot.nav.navigateToDestination();
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
}
