package team169_baseline.robots;

import java.util.ArrayList;
import java.util.Random;

import team169_baseline.managers.InfoCache;
import team169_baseline.managers.MapCacheSystem;
import team169_baseline.managers.InfoArray.InfoArrayManager;
import team169_baseline.navigation.NavigationSystem;
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
        rc.setIndicatorString(0,"Round: " + curRound);
        rc.setIndicatorString(1,"Round: " + curRound);
        rc.setIndicatorString(2,"Round: " + curRound);
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
                    System.out.println("Hit bytecode limit");
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
    public void resetClock() {
        executeStartRound = Clock.getRoundNum();
        executeStartByte = Clock.getBytecodeNum();
    }

    private boolean checkClock() {
        if(executeStartRound==Clock.getRoundNum())
            return false;
        int currRound = Clock.getRoundNum();
        //int byteCount = (GameConstants.BYTECODE_LIMIT-executeStartByte) + (currRound-executeStartRound-1) * GameConstants.BYTECODE_LIMIT + Clock.getBytecodeNum();
//        dbg.println('e', "Warning: Over Bytecode @"+executeStartTime+"-"+currRound +":"+ byteCount);
        return true;
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