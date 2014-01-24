package c_bot.managers.InfoArray;

import c_bot.util.VectorFunctions;
import battlecode.common.MapLocation;

public class BattleFront implements ArrayPackable {
    public int numEnemies;
    public int roundNum;
    public MapLocation enemyCentroid;
    static int packedSize = 3;
    
    public BattleFront() {
        enemyCentroid = new MapLocation(-1, -1);
        roundNum = -1;
        numEnemies = -1;
    }
    
    public BattleFront(int roundNum, int numEnemies, MapLocation enemyCentroid) {
        this.roundNum = roundNum;
        this.numEnemies = numEnemies;
        this.enemyCentroid = enemyCentroid;
    }
    
    @Override
    public int[] toPacked() {
        int[] info = {this.roundNum, this.numEnemies, VectorFunctions.locToInt(this.enemyCentroid)};
        return info;
    }

    @Override
    public void toUnpacked(int[] packed) {
        this.roundNum = packed[0];
        this.numEnemies = packed[1];
        this.enemyCentroid = VectorFunctions.intToLoc(packed[2]);
    }
}
