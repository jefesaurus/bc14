package team169_baseline.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class BotAction {
    public enum ActionType {
        YIELD, CONSTRUCT_PASTR, CONSTRUCT_NOISE_TOWER, MOVE, SNEAK, ATTACK, SPAWN
    }
    ActionType action;
    MapLocation loc;
    Direction dir;
    public String explanation = "";
    
    public BotAction(ActionType type, MapLocation loc, Direction dir) {
        this.action = type;
        this.loc = loc;
        this.dir = dir;
    }
    public BotAction(ActionType type, MapLocation loc) {
        this.action = type;
        this.loc = loc;
        this.dir = null;
    }
    public BotAction(ActionType type, Direction dir) {
        this.action = type;
        this.loc = null;
        this.dir = dir;
    }
    public BotAction(ActionType type) {
        this.action = type;
        this.loc = null;
        this.dir = null;
    }
    
    public BotAction(ActionType type, MapLocation loc, Direction dir, String explanation) {
        this.action = type;
        this.loc = loc;
        this.dir = dir;
        this.explanation = explanation;
    }
    
    public BotAction(ActionType type, MapLocation loc, String explanation) {
        this.action = type;
        this.loc = loc;
        this.dir = null;
        this.explanation = explanation;

    }
    
    public BotAction(ActionType type, Direction dir, String explanation) {
        this.action = type;
        this.loc = null;
        this.dir = dir;
        this.explanation = explanation;
    }
    
    public BotAction(ActionType type, String explanation) {
        this.action = type;
        this.loc = null;
        this.dir = null;
        this.explanation = explanation;
    }
    
    
    public void execute(RobotController rc) throws GameActionException {
        switch(this.action) {
        case CONSTRUCT_PASTR:
            rc.construct(RobotType.PASTR);
            break;
        case CONSTRUCT_NOISE_TOWER:
            rc.construct(RobotType.NOISETOWER);
            break;
        case MOVE:
            rc.move(this.dir);
            break;
        case SNEAK:
            rc.sneak(this.dir);
            break;
        case ATTACK:
            rc.attackSquare(this.loc);
            break;
        case SPAWN:
            rc.spawn(this.dir); 
        case YIELD:
        default:
            //rc.yield();
            break;
        }
    }
}
