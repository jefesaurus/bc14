package team204.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import team204.manager.RobotManager;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;

public class Util {
    
    public static final Direction DIRECTION_BETWEEN_HQS = RobotManager.senseHQLocation().directionTo(
            RobotManager.senseEnemyHQLocation());
    public static final double ANGLE_BETWEEN_HQS = Math.atan2(
            RobotManager.senseEnemyHQLocation().y - RobotManager.senseHQLocation().y,
            RobotManager.senseEnemyHQLocation().x - RobotManager.senseHQLocation().x);
    public static final int DISTANCE_SQUARED_BETWEEN_HQS = RobotManager.senseHQLocation().distanceSquaredTo(
            RobotManager.senseEnemyHQLocation());
    public static final double ANGLE_SCORE_WEIGHT = .15;
    
    private static Iterator<Object> iterator;
    private static Iterable<Object> iterable = new Iterable<Object>() {
        @Override
        public Iterator<Object> iterator() {
            return iterator;
        }
    };
    
    public static double angleBetweenLocations(MapLocation l1, MapLocation l2) {
        return Math.atan2(l1.y - l2.y, l1.x - l2.x);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> Iterable<E> iterable(Iterator<E> iter) {
        iterator = (Iterator<Object>) iter;
        return (Iterable<E>) iterable;
    }
    
    public static int packLocation(MapLocation loc) {
        return loc.x + loc.y * RobotManager.getMapWidth();
    }
    
    public static MapLocation unpackLocation(int v) {
        return new MapLocation(v % RobotManager.getMapWidth(), v / RobotManager.getMapWidth());
    }
    
    public static double angleDifference(double ang0, double ang1) {
        double a = Math.abs(ang0 - ang1);
        if (a > Math.PI) {
            return 2 * Math.PI - a;
        } else {
            return a;
        }
    }
    
    private static double calculateEncampmentPriorityAngleScore(MapLocation loc) {
        double a = Math.atan2(loc.y - RobotManager.senseHQLocation().y, loc.x - RobotManager.senseHQLocation().x);
        double s = angleDifference(ANGLE_BETWEEN_HQS, a) / Math.PI;
        return 1 - s * s;
    }
    
    private static double calculateEncampmentPriorityDistanceScore(MapLocation loc) {
        double max = RobotManager.getMapWidth() * RobotManager.getMapWidth() + RobotManager.getMapHeight()
                * RobotManager.getMapHeight();
        return 1 - loc.distanceSquaredTo(RobotManager.senseHQLocation()) / max;
    }
    
    public static double calculateEncampmentPriorityScore(MapLocation loc) {
        return ANGLE_SCORE_WEIGHT * calculateEncampmentPriorityAngleScore(loc) + (1 - ANGLE_SCORE_WEIGHT)
                * calculateEncampmentPriorityDistanceScore(loc);
    }
    
    private static final Comparator<MapLocation> encampmentPriorityComparator = new Comparator<MapLocation>() {
        @Override
        public int compare(MapLocation eLoc0, MapLocation eLoc1) {
            return -Double.compare(calculateEncampmentPriorityScore(eLoc0), calculateEncampmentPriorityScore(eLoc1));
            
        }
    };
    
    public static MapLocation[] sortByEncampmentPriority(MapLocation[] locs) {
        Arrays.sort(locs, encampmentPriorityComparator);
        return locs;
    }
    
    public static MapLocation getArmyCoM(MapLocation[] robotLocs) throws GameActionException {
        double xbin = 0;
        double ybin = 0;
        for (MapLocation loc : robotLocs) {
            xbin += loc.x;
            ybin += loc.y;
        }
        MapLocation CoM = new MapLocation((int) (xbin / robotLocs.length), (int) (ybin / robotLocs.length));
        return CoM;
    }
    
    public static MapLocation getArmyCoM(Robot[] robots) throws GameActionException {
        MapLocation[] robotLocs = new MapLocation[robots.length];
        for (int i = 0; i < robots.length; i++) {
            robotLocs[i] = RobotManager.senseRobotInfo(robots[i]).location;
        }
        return getArmyCoM(robotLocs);
    }
    
    public static MapLocation getArmyCom() throws GameActionException {
        return getArmyCoM(RobotManager.senseAllOtherAlliedRobots());
    }
    
    public static final Random random = new Random(RobotManager.getRobotID() * RobotManager.getMapWidth()
            * RobotManager.getMapHeight());
    
    public static int mapDotProduct(MapLocation loc, Direction dir) {
        return loc.x * dir.dx + loc.y * dir.dy;
    }
    
    public static int compareOnAxis(MapLocation a, MapLocation b, Direction dir) {
        return Integer.signum(mapDotProduct(a, dir) - mapDotProduct(b, dir));
    }
    
    public static final Direction[] REGULAR_DIRECTIONS = new Direction[] { Direction.NORTH, Direction.NORTH_EAST,
            Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST,
            Direction.NORTH_WEST };
    
    // Get directions without None or OMNI
    public static final Direction[] REGUALR_DIRECTIONS_WITH_NONE = new Direction[] { Direction.NORTH,
            Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST,
            Direction.WEST, Direction.NORTH_WEST, Direction.NONE };
    
    // public static boolean isCardinal(Direction dir) {
    // if (dir.equals(Direction.NORTH) || dir.equals(Direction.EAST) || dir.equals(Direction.SOUTH)
    // || dir.equals(Direction.WEST)) {
    // return true;
    // }
    // return false;
    // }
    
    public static final double UNIT_MAX_POWER_UPKEEP = GameConstants.UNIT_POWER_UPKEEP
            + GameConstants.POWER_COST_PER_BYTECODE * GameConstants.BYTECODE_LIMIT;
}
