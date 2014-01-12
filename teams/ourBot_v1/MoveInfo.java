package ourBot_v1;

import battlecode.common.Direction;
import battlecode.common.RobotType;

/** A data structure containing a command to be processed by the movement state machine. */
public class MoveInfo {
    public RobotType robotType;
    public Direction dir;
    public boolean move;

    /** Move in a direction. 
     * @param moonwalk true if we want to move backwards, false if we want to move forwards */
    public MoveInfo(Direction dirToMove) {
        this.dir = dirToMove;
        move = true;
    }
    /** Spawn a robot in a given direction. */
    public MoveInfo(RobotType robotType, Direction dirToSpawn) {
        this.robotType = robotType;
        this.dir = dirToSpawn;
    }

    @Override
    public String toString() {
        if(dir==null || dir == Direction.NONE || dir==Direction.OMNI) return "Do nothing";
        if(robotType!=null) return "Spawn "+robotType+" to the "+dir;
        if(move) return "Move forward to the "+dir;
        return "Do nothing";
    }
}