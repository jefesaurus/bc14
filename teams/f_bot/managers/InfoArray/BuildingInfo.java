package d_bot.managers.InfoArray;

import d_bot.util.VectorFunctions;
import battlecode.common.MapLocation;

public class BuildingInfo implements ArrayPackable {
    
    public int roundNum;
    MapLocation loc;
    public BuildingStatus status;
    
    static int packedSize = 3;

    
    
    public BuildingInfo() {
        roundNum = -1;
        loc = new MapLocation(-1, -1);
        status = BuildingStatus.NOTHING;
    }
    
    public BuildingInfo(int roundNum, BuildingStatus status, MapLocation loc) {
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
        this.status = BuildingStatus.values()[packed[2]];
    }
    
    public String toString() {
        String mapLoc = this.loc.toString();
        switch (this.status) {
        case NOTHING: return "ROUND: " + roundNum + ", NOTHING: " + mapLoc;
        case IS_COMPUTING: return "ROUND: " + roundNum + ", IS_COMPUTING: " + mapLoc;
        case IN_CONSTRUCTION: return "ROUND: " + roundNum + ", IN_CONSTRUCTION: " + mapLoc;
        case ALL_GOOD: return "ROUND: " + roundNum + ", ALL_GOOD: " + mapLoc;
        case UNDER_ATTACK: return "ROUND: " + roundNum + ", UNDER_ATTACK: " + mapLoc;
        }
        return null;
    }

}
