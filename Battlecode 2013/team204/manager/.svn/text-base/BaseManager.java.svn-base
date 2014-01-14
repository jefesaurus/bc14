package team204.manager;

import team204.util.ActionStatus;
import team204.util.RobotAction;
import team204.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public abstract class BaseManager {
    
    private static RobotController rc = null;
    
    public static void setRobotController(RobotController rc) {
        if (BaseManager.rc == null) {
            BaseManager.rc = rc;
        } else {
            throw new RuntimeException("rc already set");
        }
    }
    
    // public static boolean isRobotControllerSet() {
    // return rc != null;
    // }
    
    /**
     * @see RobotController#addMatchObservation(String)
     */
    public static void addMatchObservation(String observation) {
        rc.addMatchObservation(observation);
    }
    
    /**
     * @see RobotController#attackSquare(MapLocation)
     */
    @RobotAction
    public static ActionStatus attackSquare(MapLocation loc) throws GameActionException {
        if (isActive()) {
            if (canAttackSquare(loc)) {
                rc.attackSquare(loc);
                return ActionStatus.SUCCESS;
            } else {
                return ActionStatus.FAILURE;
            }
        } else {
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * @see RobotController#breakpoint()
     */
    public static void breakpoint() {
        rc.breakpoint();
    }
    
    /**
     * @see RobotController#broadcast(int, int)
     */
    @RobotAction
    public static ActionStatus broadcast(int channel, int data) throws GameActionException {
        // System.out.println("broadcast " + data + " on real channel " + channel);
        rc.broadcast(channel, data);
        return ActionStatus.SUCCESS;
        
    }
    
    /**
     * @see RobotController#canAttackSquare(MapLocation)
     */
    public static boolean canAttackSquare(MapLocation loc) {
        return rc.canAttackSquare(loc);
    }
    
    /**
     * @see RobotController#canMove(Direction)
     */
    public static boolean canMove(Direction dir) {
        return rc.canMove(dir);
    }
    
    /**
     * @see RobotController#canSenseObject(GameObject)
     */
    public static boolean canSenseRobot(Robot r) {
        return rc.canSenseObject(r);
    }
    
    /**
     * @see RobotController#canSenseSquare(MapLocation)
     */
    public static boolean canSenseSquare(MapLocation loc) {
        return rc.canSenseSquare(loc);
    }
    
    /**
     * @see RobotController#captureEncampment(RobotType)
     */
    @RobotAction
    public static ActionStatus captureEncampment(RobotType type) throws GameActionException {
        if (rc.isActive() && hasEnoughPower(senseCaptureCost())) {
            rc.captureEncampment(type);
            return ActionStatus.SUCCESS;
        } else {
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * @see RobotController#checkResearchProgress(Upgrade)
     */
    public static int checkResearchProgress(Upgrade upgrade) throws GameActionException {
        return rc.checkResearchProgress(upgrade);
    }
    
    /**
     * @see RobotController#defuseMine(MapLocation)
     */
    @RobotAction
    public static ActionStatus defuseMine(MapLocation loc) throws GameActionException {
        rc.defuseMine(loc);
        return ActionStatus.SUCCESS;
    }
    
    /**
     * @see RobotController#getControlBits()
     */
    public static long getControlBits() {
        return rc.getControlBits();
    }
    
    /**
     * @see RobotController#getEnergon()
     */
    public static double getEnergon() {
        return rc.getEnergon();
    }
    
    /**
     * @see RobotController#getLocation()
     */
    public static MapLocation getLocation() {
        return rc.getLocation();
    }
    
    /**
     * @see RobotController#getMapHeight()
     */
    public static int getMapHeight() {
        return rc.getMapHeight();
    }
    
    /**
     * @see RobotController#getMapWidth()
     */
    public static int getMapWidth() {
        return rc.getMapWidth();
    }
    
    /**
     * @see RobotController#getRobot()
     */
    public static Robot getRobot() {
        return rc.getRobot();
    }
    
    /**
     * @see RobotController#getShields()
     */
    public static double getShields() {
        return rc.getShields();
    }
    
    /**
     * @see RobotController#getTeam()
     */
    public static Team getTeam() {
        return rc.getTeam();
    }
    
    /**
     * @see RobotController#getTeamMemory()
     */
    public static long[] getTeamMemory() {
        return rc.getTeamMemory();
    }
    
    /**
     * @see RobotController#getTeamPower()
     */
    public static double getTeamPower() {
        return rc.getTeamPower();
    }
    
    /**
     * @see RobotController#getType()
     */
    public static RobotType getType() {
        return rc.getType();
    }
    
    /**
     * @see RobotController#hasUpgrade(Upgrade)
     */
    public static boolean hasUpgrade(Upgrade upgrade) {
        return rc.hasUpgrade(upgrade);
    }
    
    /**
     * @see RobotController#isActive()
     */
    public static boolean isActive() {
        return rc.isActive();
    }
    
    /**
     * @see RobotController#layMine()
     */
    @RobotAction
    public static ActionStatus layMine() throws GameActionException {
        if (isActive()) {
            rc.layMine();
            return ActionStatus.SUCCESS;
        } else {
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * @see RobotController#move(Direction)
     */
    @RobotAction
    public static ActionStatus move(Direction dir) throws GameActionException {
        if (isActive()) {
            if (canMove(dir)) {
                rc.move(dir);
                return ActionStatus.SUCCESS;
            } else {
                return ActionStatus.FAILURE;
            }
        } else {
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * @see RobotController#readBroadcast(int)
     */
    public static int readBroadcast(int channel) throws GameActionException {
        int data = rc.readBroadcast(channel);
        return data;
    }
    
    /**
     * @see RobotController#researchUpgrade(Upgrade)
     */
    @RobotAction
    public static ActionStatus researchUpgrade(Upgrade upgrade) throws GameActionException {
        if (hasUpgrade(upgrade)) {
            return ActionStatus.SUCCESS;
        } else {
            if (isActive()) {
                rc.researchUpgrade(upgrade);
            }
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * @see RobotController#resign()
     */
    public static void resign() {
        rc.resign();
    }
    
    /**
     * @see RobotController#roundsUntilActive()
     */
    public static int roundsUntilActive() {
        return rc.roundsUntilActive();
    }
    
    /**
     * @see RobotController#senseAllEncampmentSquares()
     */
    public static MapLocation[] senseAllEncampmentSquares() {
        return rc.senseAllEncampmentSquares();
    }
    
    /**
     * @see RobotController#senseAlliedEncampmentSquares()
     */
    public static MapLocation[] senseAlliedEncampmentSquares() {
        return rc.senseAlliedEncampmentSquares();
    }
    
    /**
     * @see RobotController#senseCaptureCost()
     */
    public static double senseCaptureCost() {
        return rc.senseCaptureCost();
    }
    
    /**
     * @see RobotController#senseEncampmentSquare(MapLocation)
     */
    public static boolean senseEncampmentSquare(MapLocation loc) {
        return rc.senseEncampmentSquare(loc);
    }
    
    /**
     * @see RobotController#senseEncampmentSquares(MapLocation, int, Team)
     */
    public static MapLocation[] senseEncampmentSquares(MapLocation center, int radiusSquared, Team team)
            throws GameActionException {
        return rc.senseEncampmentSquares(center, radiusSquared, team);
    }
    
    /**
     * @see RobotController#senseEnemyHQLocation()
     */
    public static MapLocation senseEnemyHQLocation() {
        return rc.senseEnemyHQLocation();
    }
    
    /**
     * @see RobotController#senseEnemyNukeHalfDone()
     */
    public static boolean senseEnemyNukeHalfDone() throws GameActionException {
        return rc.senseEnemyNukeHalfDone();
    }
    
    /**
     * @see RobotController#senseHQLocation()
     */
    public static MapLocation senseHQLocation() {
        return rc.senseHQLocation();
    }
    
    /**
     * @see RobotController#senseLocationOf(GameObject)
     */
    public static MapLocation senseLocationOf(GameObject o) throws GameActionException {
        return rc.senseLocationOf(o);
    }
    
    /**
     * @see RobotController#senseMine(MapLocation)
     */
    public static Team senseMine(MapLocation location) {
        return rc.senseMine(location);
    }
    
    /**
     * @see RobotController#senseMineLocations(MapLocation, int, Team)
     */
    public static MapLocation[] senseMineLocations(MapLocation center, int radiusSquared, Team team)
            throws GameActionException {
        return rc.senseMineLocations(center, radiusSquared, team);
    }
    
    /**
     * @see RobotController#senseNearbyGameObjects(Class)
     */
    public static Robot[] senseNearbyRobots() {
        return rc.senseNearbyGameObjects(Robot.class);
    }
    
    /**
     * @see RobotController#senseNearbyGameObjects(Class, int)
     */
    public static Robot[] senseAllOtherRobots(int radiusSquared) {
        return rc.senseNearbyGameObjects(Robot.class, radiusSquared);
    }
    
    /**
     * @see RobotController#senseNearbyGameObjects(Class, int, Team)
     */
    public static Robot[] senseAllOtherRobots(int radiusSquared, Team team) {
        return rc.senseNearbyGameObjects(Robot.class, radiusSquared, team);
    }
    
    /**
     * @see RobotController#senseNearbyGameObjects(Class, MapLocation, int, Team)
     */
    public static Robot[] senseAllOtherRobots(MapLocation center, int radiusSquared, Team team) {
        return rc.senseNearbyGameObjects(Robot.class, center, radiusSquared, team);
    }
    
    /**
     * @see RobotController#senseNonAlliedMineLocations(MapLocation, int)
     */
    public static MapLocation[] senseNonAlliedMineLocations(MapLocation center, int radiusSquared) {
        return rc.senseNonAlliedMineLocations(center, radiusSquared);
    }
    
    /**
     * @see RobotController#senseObjectAtLocation(MapLocation)
     */
    public static Robot senseRobotAtLocation(MapLocation loc) throws GameActionException {
        GameObject o = rc.senseObjectAtLocation(loc);
        if (o != null && o instanceof Robot) {
            return (Robot) o;
        } else {
            return null;
        }
    }
    
    private static final int ROBOT_CACHE_SIZE = 1000;
    
    private static final RobotInfo[] infoCache = new RobotInfo[ROBOT_CACHE_SIZE];
    private static final int[] infoCacheRound = new int[ROBOT_CACHE_SIZE];
    
    /**
     * @see RobotController#senseRobotInfo(Robot)
     */
    public static RobotInfo senseRobotInfo(Robot r) throws GameActionException {
        int index = r.getID() % ROBOT_CACHE_SIZE;
        if (infoCache[index] == null || infoCacheRound[index] < Clock.getRoundNum()) {
            infoCache[index] = rc.senseRobotInfo(r);
            typeCache[index] = infoCache[index].type;
            typeRobotCache[index] = r;
            infoCacheRound[index] = Clock.getRoundNum();
        }
        if (r.equals(infoCache[index].robot)) {
            return infoCache[index];
        } else {
            System.out.println("info cache collision: " + infoCache[index].robot.getID() + "|" + r.getID() + " -> "
                    + index);
            return rc.senseRobotInfo(r);
        }
    }
    
    /**
     * @see RobotController#setIndicatorString(int, String)
     */
    public static void setIndicatorString(int stringIndex, String newString) {
        rc.setIndicatorString(stringIndex, newString);
    }
    
    /**
     * @see RobotController#setTeamMemory(int, long)
     */
    public static void setTeamMemory(int index, long value) {
        rc.setTeamMemory(index, value);
    }
    
    /**
     * @see RobotController#setTeamMemory(int, long, long)
     */
    public static void setTeamMemory(int index, long value, long mask) {
        rc.setTeamMemory(index, value, mask);
    }
    
    /**
     * @see RobotController#spawn(Direction)
     */
    @RobotAction
    public static ActionStatus spawn(Direction dir) throws GameActionException {
        if (isActive()) {
            if (canMove(dir)) {
                rc.spawn(dir);
                return ActionStatus.SUCCESS;
            } else {
                return ActionStatus.FAILURE;
            }
        } else {
            return ActionStatus.RUNNING;
        }
    }
    
    /**
     * @see RobotController#suicide()
     */
    public static void suicide() {
        rc.suicide();
    }
    
    /**
     * @see RobotController#wearHat()
     */
    public static void wearHat() {
        rc.wearHat();
    }
    
    /**
     * @see RobotController#yield()
     */
    public static void yield() {
        rc.yield();
    }
    
    /**
     * @see Robot#getID()
     */
    public static int getRobotID() {
        return rc.getRobot().getID();
    }
    
    public static boolean isOnMap(MapLocation loc) {
        return loc.x >= 0 && loc.x < getMapWidth() && loc.y >= 0 && loc.y < getMapHeight();
    }
    
    /**
     * @see RobotController#senseNearbyGameObjects(Class, MapLocation, int, Team)
     */
    public static Robot[] senseAllOtherAlliedRobots() {
        return rc.senseNearbyGameObjects(Robot.class, getLocation(), Integer.MAX_VALUE, getTeam());
    }
    
    /**
     * @see RobotController#senseNearbyGameObjects(Class, MapLocation, int, Team)
     */
    public static Robot[] senseEnemyRobots() {
        return rc.senseNearbyGameObjects(Robot.class, getLocation(), Integer.MAX_VALUE, getTeam().opponent());
    }
    
    public static boolean hasEnoughPower(double cost) {
        return getTeamPower() >= cost + Util.UNIT_MAX_POWER_UPKEEP * senseAllOtherAlliedRobots().length;
    }
    
    public static double calculatePowerBuildupGeneration(int extraBuildings) {
        return (senseCaptureCost() + extraBuildings * GameConstants.CAPTURE_POWER_COST)
                * (1 - (hasUpgrade(Upgrade.FUSION) ? GameConstants.POWER_DECAY_RATE_FUSION : GameConstants.POWER_DECAY_RATE));
    }
    
    public static Team senseNonAlliedMine(MapLocation loc) {
        Team mine = senseMine(loc);
        if (mine == getTeam()) {
            return null;
        } else {
            return mine;
        }
    }
    
    /**
     * @see RobotController#senseEncampmentSquares(MapLocation, int, Team)
     */
    public static MapLocation[] senseNonAlliedEncampmentSquares() throws GameActionException {
        return rc.senseEncampmentSquares(getLocation(), Integer.MAX_VALUE, Team.NEUTRAL);
    }
    
    private static final RobotType[] typeCache = new RobotType[ROBOT_CACHE_SIZE];
    private static final Robot[] typeRobotCache = new Robot[ROBOT_CACHE_SIZE];
    
    public static RobotType senseRobotType(Robot r) throws GameActionException {
        int index = r.getID() % ROBOT_CACHE_SIZE;
        if (r.equals(typeRobotCache[index])) {
            return typeCache[index];
        } else if (typeRobotCache[index] == null) {
            typeRobotCache[index] = r;
            RobotInfo info = senseRobotInfo(r);
            return info.type;
        } else {
            System.out.println("type cache collision: " + typeRobotCache[index].getID() + "|" + r.getID() + " -> "
                    + index);
            RobotInfo info = senseRobotInfo(r);
            return info.type;
        }
    }
}
