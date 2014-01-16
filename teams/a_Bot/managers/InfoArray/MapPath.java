package a_Bot.managers.InfoArray;

import a_Bot.Constants;
import a_Bot.robots.BaseRobot;
import a_Bot.util.VectorFunctions;
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
