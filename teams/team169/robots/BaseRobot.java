package team169.robots;

import java.util.ArrayList;
import java.util.Random;

import team169.managers.InfoCache;
import team169.managers.MapCacheSystem;
import team169.managers.InfoArray.InfoArrayManager;
import team169.navigation.NavigationSystem;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public abstract class BaseRobot {

    // Core Subsystems
    public final RobotController rc;
    public final InfoArrayManager comms;
    public final NavigationSystem nav;
    public final MapCacheSystem mc;
    public final InfoCache ic;

    // Robot Statistics - permanent variables
    public final RobotType myType;
    public final Team myTeam;
    public final int myID;
    public final MapLocation myHQ;
    public final MapLocation enemyHQ;

    // Robot Statistics - updated per turn
    public MapLocation curLoc;
    public int curRound;

    // Robot Flags - toggle important behavior changes
    public int gameEndTime = GameConstants.ROUND_MAX_LIMIT;


    protected static Direction allDirections[] = Direction.values();
    static Random randall = new Random();
    
    protected static int directionalLooks[] = new int[]{0,1,-1,2,-2,3,-3,4};
    protected static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
    protected static int bigBoxSize = 5;
    public static int myBand = 100;
    
    public static int executeStartRound = 0;
    public static int executeStartByte = 0;



    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
        ic = new InfoCache(rc);

        myType = rc.getType();
        myTeam = rc.getTeam();
        myID = rc.getRobot().getID();
        myHQ = rc.senseHQLocation();
        enemyHQ = rc.senseEnemyHQLocation();

        
        comms = new InfoArrayManager(rc);
        nav = new NavigationSystem(this);
        mc = new MapCacheSystem(this);
        updateRoundVariables();
    }

    public abstract void run() throws GameActionException;

    public void loop() {
        while(true) {

            // Begin New Turn
            updateRoundVariables();

            try {

                // Main Run Call
                run();

            } catch (Exception e) {
                e.printStackTrace();
            }

            rc.yield();
        }
    }

    /** Resets the current round variables of the robot. */
    public void updateRoundVariables() {
        curRound = Clock.getRoundNum();
        curLoc = rc.getLocation();
    }

    /** Should be overridden by any robot that wants to do movements. 
     * @return a new MoveInfo structure that either represents a spawn, a move, or a turn
     */
    public void computeNextMove() throws GameActionException {
    }

    public String locationToVectorString(MapLocation loc) {
        if(loc==null) return "<null>";
        return "<"+(loc.x-curLoc.x)+","+(loc.y-curLoc.y)+">";
    }
}