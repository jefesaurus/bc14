package a_Bot.robots;

import java.util.ArrayList;
import java.util.Random;

import a_Bot.managers.InfoCache;
import a_Bot.managers.MapCacheSystem;
import a_Bot.managers.InfoArray.InfoArrayManager;
import a_Bot.navigation.NavigationSystem;
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
    public final MapLocation myHome;

    // Robot Statistics - updated per turn
    public MapLocation curLoc;
    public int curRound;

    // Robot Flags - toggle important behavior changes
    public int gameEndTime = GameConstants.ROUND_MAX_LIMIT;


    protected static Direction allDirections[] = Direction.values();
    static Random randall = new Random();
    
    protected static int directionalLooks[] = new int[]{1,-1,2,-2,3,-3,4};
    protected static ArrayList<MapLocation> path = new ArrayList<MapLocation>();
    protected static int bigBoxSize = 5;
    public static int myBand = 100;


    public BaseRobot(RobotController myRC) throws GameActionException {
        rc = myRC;
        ic = new InfoCache(rc);

        myType = rc.getType();
        myTeam = rc.getTeam();
        myID = rc.getRobot().getID();
        myHome = rc.senseHQLocation();
        
        comms = new InfoArrayManager(rc);
        nav = new NavigationSystem(this);
        mc = new MapCacheSystem(this);
        
        updateRoundVariables();
        bigBoxSize = rc.getMapHeight() * rc.getMapWidth() / 400;
        
        
    }

    public abstract void run() throws GameActionException;

    public void loop() {
        while(true) {

            // Begin New Turn
            updateRoundVariables();

            try {

                // Main Run Call
                run();

                // Check if we've already run out of bytecodes
                int bcUsed = Clock.getBytecodeNum();
                if (bcUsed < GameConstants.FREE_BYTECODES) {
                    // We need to use these.
                } else if (bcUsed < GameConstants.BYTECODE_LIMIT) {
                    // We *can* use these
                } else {
                    // We went over?
                }
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