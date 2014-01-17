package a_Bot.managers.InfoArray;

import a_Bot.util.VectorFunctions;
import battlecode.common.MapLocation;


public class Command implements ArrayPackable {
    public CommandType type;
    public MapLocation loc;
    static int packedSize = 2;
    
    public Command() {
    }
    
    public Command(CommandType type, MapLocation loc) {
        this.type = type;
        this.loc = loc;
    }

    @Override
    public int[] toPacked() {
        int[] info = {this.type.ordinal(), VectorFunctions.locToInt(this.loc)};
        return info;
    }

    @Override
    public void toUnpacked(int[] packed) {
        this.type = CommandType.values()[packed[0]];
        this.loc = VectorFunctions.intToLoc(packed[1]);
    }
    
    public String toString() {
        String mapLoc = this.loc.toString();
        switch (this.type) {
        case RALLY_POINT: return "RALLY POINT: " + mapLoc;
        case ATTACK_POINT:  return "ATTACK POINT: " + mapLoc;
        case PASTR_POINT:  return "PASTR POINT: " + mapLoc;
        }
        return null;
    }
}