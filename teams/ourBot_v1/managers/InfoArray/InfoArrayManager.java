package ourBot_v1.managers.InfoArray;

import battlecode.common.*;
import ourBot_v1.util.VectorFunctions;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

/*
 * Organization:
 * 0: HQ location
 * 1: Enemy HQ location
 * 2+: Squad commands
 * ...
 */

public class InfoArrayManager {
    RobotController rc;
    static final int NUM_SQUADS = 25;
    
    static int OUR_HQ_LOC_SLOT;
    static int ENEMY_HQ_LOC_SLOT;
    static int GLOBAL_COMMAND_SLOT;
    static int SQUAD_COMMAND_SLOTS;
    static int MAP_CACHE_END;


    public InfoArrayManager(RobotController rc) throws GameActionException {
        this.rc = rc;
        OUR_HQ_LOC_SLOT = rc.getMapWidth()*rc.getMapHeight();
        MAP_CACHE_END = OUR_HQ_LOC_SLOT-1;
        ENEMY_HQ_LOC_SLOT = OUR_HQ_LOC_SLOT + 1;
        GLOBAL_COMMAND_SLOT = ENEMY_HQ_LOC_SLOT + 1;
        SQUAD_COMMAND_SLOTS = GLOBAL_COMMAND_SLOT + Command.packedSize;
        
    }

    public void setOurHQLocation(MapLocation loc) throws GameActionException {
        rc.broadcast(OUR_HQ_LOC_SLOT, VectorFunctions.locToInt(loc));
    }
    
    public void setEnemyHQLocation(MapLocation loc) throws GameActionException {
        rc.broadcast(ENEMY_HQ_LOC_SLOT, VectorFunctions.locToInt(loc));
    }
    
    public void getOurHQLocation(MapLocation loc) throws GameActionException {
        VectorFunctions.intToLoc(rc.readBroadcast(OUR_HQ_LOC_SLOT));
    }   
    
    public void getEnemyHQLocation(MapLocation loc) throws GameActionException {
        VectorFunctions.intToLoc(rc.readBroadcast(ENEMY_HQ_LOC_SLOT));
    }
    
    /*
     * This takes the desired squad target number and the command object
     * turns to command into the packeted array of ints and then broadcasts it
     */
    public void sendSquadCommand(int squadNum, Command command) throws GameActionException {
        int[] packets = command.toPacked();
        for (int i = 0; i < packets.length; i ++) {
            rc.broadcast(SQUAD_COMMAND_SLOTS + squadNum*Command.packedSize + i, packets[i]);
        }
    }
    
    /*
     * This is the "inverse" of the sendCommand method above. It reads the array and returns the
     * unpacked command
     */
    public Command getSquadCommand(int squadNum) throws GameActionException {
        Command squadCommand = new Command();
        int[] packets = new int[Command.packedSize];
        
        for (int i = 0; i < packets.length; i ++) {
            packets[i] = rc.readBroadcast(SQUAD_COMMAND_SLOTS + squadNum*Command.packedSize + i);
        }
        squadCommand.toUnpacked(packets);
        return squadCommand;
    }
    
    /**
     * Unsensed = 0
     * NORMAL = 1
     * ROAD = 2
     * VOID = 3
     * OFF_MAP = 4
     * @param loc
     * @return
     */
    public TerrainTile getTerrainTile(MapLocation loc) throws GameActionException {
      int terrain = rc.readBroadcast(VectorFunctions.locToCoordinates(loc, rc.getMapWidth()));
      switch (terrain) {
      case 0:
          TerrainTile sensed = rc.senseTerrainTile(loc);
          Terrain message = new Terrain(sensed);
          rc.broadcast(VectorFunctions.locToCoordinates(loc, rc.getMapWidth()), message.toPacked()[0]);
          return sensed;
      case 1:
          return TerrainTile.NORMAL;
      case 2:
          return TerrainTile.ROAD;
      case 3:
          return TerrainTile.VOID;
      case 4:
          return TerrainTile.OFF_MAP;
      default:
          return TerrainTile.OFF_MAP;
      }
    }
}