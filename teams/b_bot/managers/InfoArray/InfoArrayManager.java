package b_bot.managers.InfoArray;

import b_bot.util.VectorFunctions;
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
    
    static final int OUR_HQ_LOC_SLOT = 0;
    static final int ENEMY_HQ_LOC_SLOT = OUR_HQ_LOC_SLOT + 1;
    static final int NEW_SPAWN_SQUAD_SLOT = ENEMY_HQ_LOC_SLOT + 1;
    static final int GLOBAL_COMMAND_SLOT = ENEMY_HQ_LOC_SLOT + 1;
    static final int SQUAD_COMMAND_SLOTS = GLOBAL_COMMAND_SLOT + Command.packedSize;
    static final int PASTR_LOC_SLOT = 1000 ;


    public InfoArrayManager(RobotController rc) throws GameActionException {
        this.rc = rc;
    }

    public void setOurHQLocation(MapLocation loc) throws GameActionException {
        rc.broadcast(OUR_HQ_LOC_SLOT, VectorFunctions.locToInt(loc));
    }
    
    public void setPastrLoc(MapLocation loc) throws GameActionException {
        rc.broadcast(PASTR_LOC_SLOT, loc.x * 100 + loc.y);
    }
    
    public MapLocation getPastrLoc() throws GameActionException {
        return new MapLocation(rc.readBroadcast(PASTR_LOC_SLOT) / 100, rc.readBroadcast(PASTR_LOC_SLOT) % 100);
    }
    
    public void setEnemyHQLocation(MapLocation loc) throws GameActionException {
        rc.broadcast(ENEMY_HQ_LOC_SLOT, VectorFunctions.locToInt(loc));
    }
    
    public MapLocation getOurHQLocation(MapLocation loc) throws GameActionException {
        return  VectorFunctions.intToLoc(rc.readBroadcast(OUR_HQ_LOC_SLOT));
    }   
    
    public MapLocation getEnemyHQLocation(MapLocation loc) throws GameActionException {
        return VectorFunctions.intToLoc(rc.readBroadcast(ENEMY_HQ_LOC_SLOT));
    }
    
    /*
     * New Soldiers read this first command to get their squad assignment
     */
    public void setNewSpawnSquad(int squadNum) throws GameActionException {
        rc.broadcast(NEW_SPAWN_SQUAD_SLOT, squadNum);
    }
    
    public int getNewSpawnSquad() throws GameActionException {
        return rc.readBroadcast(NEW_SPAWN_SQUAD_SLOT);
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
}