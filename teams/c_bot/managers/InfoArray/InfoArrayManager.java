package c_bot.managers.InfoArray;

import c_bot.util.VectorFunctions;
import c_bot.managers.InfoArray.Command;
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
    
    // Status of the buildings (tower and pastr)
    static final int PASTR_STATUS_SLOTS = SQUAD_COMMAND_SLOTS + Command.packedSize*NUM_SQUADS;
    static final int TOWER_STATUS_SLOTS = PASTR_STATUS_SLOTS + BuildingInfo.packedSize*1;

    

    static final int PASTR_LOC_SLOT = 1000;
    static final int P_PASTR_LOC1 = PASTR_LOC_SLOT + 1;
    static final int P_PASTR_SCORE1 = P_PASTR_LOC1 + 1;
    static final int P_PASTR_LOC2 = P_PASTR_SCORE1 + 1;
    static final int P_PASTR_SCORE2 = P_PASTR_LOC2 + 1;

    static final int P_SEARCH_COORDINATES = P_PASTR_SCORE2 + 2;

    public InfoArrayManager(RobotController rc) throws GameActionException {
        this.rc = rc;
    }
    
    
    public MapLocation wait_P_PASTR_LOC_1() throws GameActionException {
        int msg = rc.readBroadcast(P_PASTR_LOC1);
        while (msg == 0) {
            msg = rc.readBroadcast(P_PASTR_LOC1);
            rc.yield();
        }
        return new MapLocation(msg / 100, msg % 100);
    }
    
    public MapLocation wait_P_PASTR_LOC_2() throws GameActionException {
        int msg = rc.readBroadcast(P_PASTR_LOC2);
        while (msg == 0) {
            msg = rc.readBroadcast(P_PASTR_LOC2);
            rc.yield();
        }
        return new MapLocation(msg / 100, msg % 100);
    }
    
    public int wait_P_PASTR_SCORE_1() throws GameActionException {
        int msg = rc.readBroadcast(P_PASTR_SCORE1);
        while (msg == 0) {
            msg = rc.readBroadcast(P_PASTR_SCORE1);
            rc.yield();
        }
        return msg;
    }
    
    public int wait_P_PASTR_SCORE_2() throws GameActionException {
        int msg = rc.readBroadcast(P_PASTR_SCORE2);
        while (msg == 0) {
            msg = rc.readBroadcast(P_PASTR_SCORE2);
            rc.yield();
        }
        return msg;
    }
    
    
    public MapLocation wait_PASTR_LOC_FINAL() throws GameActionException {
        int msg = rc.readBroadcast(PASTR_LOC_SLOT);
        while (msg == 0) {
            msg = rc.readBroadcast(PASTR_LOC_SLOT);
            rc.yield();
        }
        return new MapLocation(msg / 100, msg % 100);
    }
    public void sendSearchCoordinates(int sx, int sy, int fx, int fy) throws GameActionException {
        rc.broadcast(P_SEARCH_COORDINATES-1, sx*100 + sy);
        rc.broadcast(P_SEARCH_COORDINATES, fx*100 + fy);
    }
       
    public int[] getSearchCoordinates() throws GameActionException {
        int sc = rc.readBroadcast(P_SEARCH_COORDINATES-1);
        int fc = rc.readBroadcast(P_SEARCH_COORDINATES);
        int[] msg_decode = {sc/100, sc%100, fc/100, fc%100};
        return msg_decode;
    }  
    
    public void setP_PASTR_LOC1(MapLocation loc) throws GameActionException{
        rc.broadcast(P_PASTR_LOC1, loc.x * 100 + loc.y);
    }
    
    public void setP_PASTR_SCORE1(int score) throws GameActionException{
        rc.broadcast(P_PASTR_SCORE1, score);
    }
    
    public void setP_PASTR_LOC2(MapLocation loc) throws GameActionException {
        rc.broadcast(P_PASTR_LOC2, loc.x * 100 + loc.y);
    }
    
    public void setP_PASTR_SCORE2(int score) throws GameActionException{
        rc.broadcast(P_PASTR_SCORE2, score);
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

    public void setBuildingStatus(BuildingType buildingType, BuildingInfo info) throws GameActionException {
        int[] packets = info.toPacked();
        int firstSlot = PASTR_STATUS_SLOTS;
        switch(buildingType) {
        case TOWER:
            firstSlot = TOWER_STATUS_SLOTS;
        default:
            break;
        }        for (int i = 0; i < packets.length; i ++) {
            rc.broadcast(firstSlot + i, packets[i]);
        }
    }

    public BuildingInfo getBuildingStatus(BuildingType buildingType) throws GameActionException {
        BuildingInfo info = new BuildingInfo();
        int[] packets = new int[BuildingInfo.packedSize];
        int firstSlot = PASTR_STATUS_SLOTS;
        switch(buildingType) {
        case TOWER:
            firstSlot = TOWER_STATUS_SLOTS;
        default:
            break;
        }
        for (int i = 0; i < packets.length; i ++) {
            packets[i] = rc.readBroadcast(firstSlot + i);

        }

        info.toUnpacked(packets);
        return info;
    }
}