package team169.managers.InfoArray;

import team169.Constants;
import team169.robots.BaseRobot;
import team169.util.VectorFunctions;
import battlecode.common.MapLocation;

public class MapPath implements ArrayPackable  {
    int packedSize = Constants.MAX_PATH_SIZE;
    MapLocation[] steps = new MapLocation[Constants.MAX_PATH_SIZE];
    
    @Override
    public int[] toPacked() {
        // TODO Auto-generated method stub
        
        return null;
    }

    @Override
    public void toUnpacked(int[] packed) {
        int i = 0;
        while (packed[i] >= 0) {
            this.steps[i] = VectorFunctions.intToLoc(packed[i]);
            i++;
        }
    }
}
