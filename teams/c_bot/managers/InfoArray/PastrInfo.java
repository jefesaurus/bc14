package c_bot.managers.InfoArray;

import c_bot.util.VectorFunctions;
import battlecode.common.MapLocation;

public class PastrInfo implements ArrayPackable {
    
    int roundNum;
    MapLocation loc;
    PastrStatus status;
    
    int packedSize = 3;

    
    
    public PastrInfo() {
    }
    
    public PastrInfo(int roundNum, PastrStatus status, MapLocation loc) {
        this.roundNum = roundNum;
        this.status = status;
        this.loc = loc;
    }

    @Override
    public int[] toPacked() {
        int[] info = {this.roundNum, VectorFunctions.locToInt(this.loc), this.status.ordinal()};
        return info;
    }

    @Override
    public void toUnpacked(int[] packed) {
        this.roundNum = packed[0];
        this.loc = VectorFunctions.intToLoc(packed[1]);
        this.status = PastrStatus.values()[packed[2]];
    }
    
    public String toString() {
        String mapLoc = this.loc.toString();
        switch (this.status) {
        case IN_CONSTRUCTION: return "ROUND: " + roundNum + ", IN_CONSTRUCTION: " + mapLoc;
        case NEEDS_TOWER: return "ROUND: " + roundNum + ", NEEDS_TOWER: " + mapLoc;
        case ALL_GOOD: return "ROUND: " + roundNum + ", ALL_GOOD: " + mapLoc;
        case UNDER_ATTACK: return "ROUND: " + roundNum + ", UNDER_ATTACK: " + mapLoc;
        }
        return null;
    }

}
