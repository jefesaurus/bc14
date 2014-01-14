package team204.manager;

import team204.util.ActionStatus;
import team204.util.RobotAction;
import team204.util.Rotation;
import team204.util.Util;
import team204.util.WallSet;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;

public class BugTraceManager extends BaseManager {
    
    /**
     * The robot's current location, stored separately to detect changed between calls
     */
    private static MapLocation robotLocation;
    /**
     * The direction the robot should move, as determined by the algorithm
     */
    private static Direction actionDirection;
    
    /**
     * The blocked adjacent location closest in rotation to the robot's previous direction
     */
    private static MapLocation obstacle;
    
    /**
     * The rotation with which the robot turned to start the tracing
     */
    private static Rotation tracingRotation;
    /**
     * whether the current tracingRotation is the second one being tried
     */
    private static boolean tracingRotationHasSwitched;
    
    /**
     * the location at which the robot has been so far closest to the goal
     */
    private static MapLocation bestRobotLocation;
    
    /**
     * the set of obstacles at which the robot has encountered while tracing with the current tracingRotation
     */
    private static final WallSet alreadyTraced = new WallSet();
    
    private static enum State {
        TRACING, GOING, FAILED, ;
    }
    
    /**
     * whether the robot is in defusing mode
     */
    private static boolean isInDefusingMode;
    
    /**
     * whether the robot is currently about to defuse a mine
     */
    private static boolean isDefusing;
    
    /**
     * which state of movement the robot is in
     */
    private static State state = null;
    
    // movement parameters; stored in fields, rather than method arguments, for
    // convenience
    
    /**
     * The target location of the tracing
     */
    private static MapLocation target;
    /**
     * the boundary distance squared from the target
     */
    private static int goalDistanceSquared;
    /**
     * whether the goal is to be within the goal distance, or beyond it, from the target
     */
    private static boolean goalWithinDistance;
    /**
     * whether the goal is to be at the boundary of the goal distance, or just within/beyond it
     */
    private static boolean goalAtBoundary;
    
    // common calculation values; re-evaluated each step
    
    private static Direction goalDirection;
    
    // occupancy determination
    
    private static enum OccupancyStatus {
        /**
         * The location is off the map
         */
        OFF_MAP,
        /**
         * The location currently contains a non-allied mine
         */
        MINE,
        /**
         * The location is currently occupied by a robot (and contains no non-allied mine)
         */
        ROBOT,
        /**
         * The location is currently safely open for occupation
         */
        OPEN, ;
    }
    
    /**
     * determine the OccupancyStatus of the adjacent location in a given direction
     * 
     * @param dir
     * @return
     */
    private static OccupancyStatus senseOccupancyStatus(Direction dir) {
        MapLocation loc = getLocation().add(dir);
        if (isOnMap(loc)) {
            if (senseNonAlliedMine(loc) != null) {
                return OccupancyStatus.MINE;
            } else {
                Robot r;
                try {
                    r = senseRobotAtLocation(loc);
                } catch (GameActionException e) {
                    throw new IllegalStateException();
                }
                if (r != null) {
                    return OccupancyStatus.ROBOT;
                } else {
                    return OccupancyStatus.OPEN;
                }
            }
        } else {
            return OccupancyStatus.OFF_MAP;
        }
    }
    
    // going
    
    /**
     * set the movement state to GOING and perform state initialization
     */
    private static void resetGoingIfNecessary() {
        state = State.GOING;
    }
    
    /**
     * calculate the movementDirection during the GOING state, or switch to the TRACING state
     */
    private static ActionStatus calculateStepGoing() {
        resetGoingIfNecessary();
        switch (senseOccupancyStatus(goalDirection)) {
        case OFF_MAP:
            return ActionStatus.FAILURE;
        case MINE:
            if (isInDefusingMode) {
                actionDirection = goalDirection;
                isDefusing = true;
                return ActionStatus.RUNNING;
            }
        case ROBOT:
            return calculateStepTracing();
        case OPEN:
            actionDirection = goalDirection;
            return ActionStatus.RUNNING;
        default:
            throw new IllegalStateException();
        }
    }
    
    // tracing
    
    /**
     * set the movement state to TRACING and perform state initialization
     */
    private static void resetTracingIfNecessary() {
        if (state != State.TRACING) {
            state = State.TRACING;
            setBestRobotLocation();
            obstacle = robotLocation.add(goalDirection);
            alreadyTraced.add(robotLocation, goalDirection);
            tracingRotation = Util.random.nextBoolean() ? Rotation.LEFT : Rotation.RIGHT;
            tracingRotationHasSwitched = false;
            alreadyTraced.clear();
        }
    }
    
    private static void setBestRobotLocation() {
        bestRobotLocation = robotLocation;
    }
    
    /**
     * evaluate the goal metric for the given location against the given goal location
     */
    public static int evaluateAgainstGoal(MapLocation loc) {
        return Util.mapDotProduct(loc, goalDirection);
        // return loc.distanceSquaredTo(target) * (goalTowards ? -1 : 1);
    }
    
    /**
     * calculate the movementDirection during the TRACING state, or switch to the GOING state
     */
    private static ActionStatus calculateStepTracing() {
        resetTracingIfNecessary();
        Direction toObstacle = robotLocation.directionTo(obstacle);
        OccupancyStatus occupancy = senseOccupancyStatus(toObstacle);
        if (occupancy == OccupancyStatus.OPEN || (isInDefusingMode && occupancy == OccupancyStatus.MINE)) {
            // the previous obstacle is no longer an obstacle
            return calculateStepGoing();
        }
        if (evaluateAgainstGoal(robotLocation) >= evaluateAgainstGoal(bestRobotLocation)) {
            // this is as close as the robot has gotten to the goal during this
            // trace
            setBestRobotLocation();
        }
        return calculateStepTracingRotation(toObstacle);
    }
    
    /**
     * rotate the movementDirection around adjacent blocked locations
     */
    private static ActionStatus calculateStepTracingRotation(Direction toFarthestObstacle) {
        // start with the blocked location that is in the robot's way
        boolean toGoalInObstacle = toFarthestObstacle == goalDirection;
        for (Direction dir = tracingRotation.rotate(toFarthestObstacle); dir != toFarthestObstacle; dir = tracingRotation
                .rotate(dir)) {
            switch (senseOccupancyStatus(dir)) {
            case OPEN:
                if (!toGoalInObstacle && robotLocation.equals(bestRobotLocation)
                        && tracingRotation.difference(dir, goalDirection) > 1) {
                    return calculateStepGoing();
                }
                
                actionDirection = dir;
                return ActionStatus.RUNNING;
            case MINE:
                if (isInDefusingMode) {
                    if (!toGoalInObstacle && robotLocation.equals(bestRobotLocation)
                            && tracingRotation.difference(dir, goalDirection) > 1) {
                        return calculateStepGoing();
                    }
                    
                    actionDirection = dir;
                    isDefusing = true;
                    return ActionStatus.RUNNING;
                }
            case ROBOT:
                obstacle = robotLocation.add(dir); // TODO: only calculate once per step?
                if (!alreadyTraced.add(robotLocation, dir)) {
                    return calculateStepGoing();
                }
                toGoalInObstacle |= dir == goalDirection;
                break;
            case OFF_MAP:
                if (tracingRotationHasSwitched) {
                    return ActionStatus.FAILURE;
                } else {
                    tracingRotation = tracingRotation.opposite();
                    tracingRotationHasSwitched = true;
                    obstacle = robotLocation.add(dir);
                    alreadyTraced.clear();
                    return calculateStepTracingRotation(dir);
                }
            default:
                throw new IllegalStateException();
            }
        }
        return ActionStatus.FAILURE;
    }
    
    // main
    
    /**
     * if necessary, reset the mover and perform initialization
     */
    private static void resetIfNecessary() {
        if (state == null || state == State.FAILED || !robotLocation.equals(getLocation())) {
            robotLocation = getLocation();
            resetGoingIfNecessary();
        }
    }
    
    private static int compareToBoundary(MapLocation loc1, MapLocation loc2, MapLocation center, int distanceSquared) {
        if (loc1.distanceSquaredTo(center) > distanceSquared) {
            return -1;
        } else {
            if (loc2.distanceSquaredTo(center) > distanceSquared) {
                return 0;
            } else {
                return 1;
            }
        }
    }
    
    /**
     * calculate the next movementDirection
     */
    public static ActionStatus calculateStep() {
        resetIfNecessary();
        actionDirection = null;
        isDefusing = false;
        goalDirection = robotLocation.directionTo(target);
        if (!goalWithinDistance) {
            goalDirection = goalDirection == Direction.OMNI ? Direction.NORTH : goalDirection.opposite();
        }
        int c;
        if (goalWithinDistance) {
            c = compareToBoundary(robotLocation, robotLocation.subtract(goalDirection), target, goalDistanceSquared);
        } else {
            c = -compareToBoundary(robotLocation.subtract(goalDirection), robotLocation, target, goalDistanceSquared);
        }
        if (c == 0) {
            return ActionStatus.SUCCESS;
        } else if (c == 1) {
            if (goalAtBoundary) {
                goalDirection = goalDirection.opposite();
            } else {
                return ActionStatus.SUCCESS;
            }
        }
        if (isActive()) {
            ActionStatus status;
            switch (state) {
            case GOING:
                status = calculateStepGoing();
                break;
            case TRACING:
                status = calculateStepTracing();
                break;
            default:
                throw new IllegalStateException();
            }
            if (status.failure()) {
                state = State.FAILED;
            }
            return status;
        } else {
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * update the mover when the robot moves in movementDirection
     */
    private static void updateStepMove() {
        robotLocation = robotLocation.add(actionDirection);
    }
    
    /**
     * calculate movementDirection and then go in that direction
     */
    @RobotAction
    private static ActionStatus step() throws GameActionException {
        ActionStatus status = calculateStep();
        if (actionDirection != null) {
            if (!isDefusing) {
                ActionStatus status2 = move(actionDirection);
                if (status2.success()) {
                    updateStepMove();
                }
            } else {
                defuseMine(robotLocation.add(actionDirection));
            }
        }
        return status;
    }
    
    @RobotAction
    public static ActionStatus moveWithinDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = true;
        goalAtBoundary = false;
        isInDefusingMode = false;
        return step();
    }
    
    @RobotAction
    public static ActionStatus moveBeyondDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = false;
        goalAtBoundary = false;
        isInDefusingMode = false;
        return step();
    }
    
    @RobotAction
    public static ActionStatus moveJustWithinDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = true;
        goalAtBoundary = true;
        isInDefusingMode = false;
        return step();
    }
    
    @RobotAction
    public static ActionStatus moveJustBeyondDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = false;
        goalAtBoundary = true;
        isInDefusingMode = false;
        return step();
    }
    
    @RobotAction
    public static ActionStatus moveWithinAdjacentToLocation(MapLocation loc) throws GameActionException {
        return moveWithinDistanceFromLocation(loc, 2);
    }
    
    @RobotAction
    public static ActionStatus moveToLocation(MapLocation loc) throws GameActionException {
        return moveWithinDistanceFromLocation(loc, 0);
    }
    
    @RobotAction
    public static ActionStatus defuseMoveWithinDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = true;
        goalAtBoundary = false;
        isInDefusingMode = true;
        return step();
    }
    
    @RobotAction
    public static ActionStatus defuseMoveBeyondDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = false;
        goalAtBoundary = false;
        isInDefusingMode = true;
        return step();
    }
    
    @RobotAction
    public static ActionStatus defuseMoveJustWithinDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = true;
        goalAtBoundary = true;
        isInDefusingMode = true;
        return step();
    }
    
    @RobotAction
    public static ActionStatus defuseMoveJustBeyondDistanceFromLocation(MapLocation loc, int distanceSquared)
            throws GameActionException {
        target = loc;
        goalDistanceSquared = distanceSquared;
        goalWithinDistance = false;
        goalAtBoundary = true;
        isInDefusingMode = true;
        return step();
    }
    
    @RobotAction
    public static ActionStatus defuseMoveToLocation(MapLocation loc) throws GameActionException {
        return defuseMoveWithinDistanceFromLocation(loc, 0);
    }
    
    @RobotAction
    public static ActionStatus defuseMoveWithinAdjacentToLocation(MapLocation loc) throws GameActionException {
        return defuseMoveWithinDistanceFromLocation(loc, 2);
    }
}
