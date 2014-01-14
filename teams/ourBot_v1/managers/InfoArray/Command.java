package ourBot_v1.managers.InfoArray;

import battlecode.common.MapLocation;
import ourBot_v1.util.VectorFunctions;


public class Command implements ArrayPackable {
    CommandType type;
    MapLocation loc;
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
}
