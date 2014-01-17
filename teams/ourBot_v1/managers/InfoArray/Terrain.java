package ourBot_v1.managers.InfoArray;

import battlecode.common.*;

public class Terrain implements ArrayPackable {
    TerrainTile type;
    
    public Terrain() {
    }
    
    public Terrain(TerrainTile terrain) {
        this.type = terrain;
    }
    
    public int[] toPacked() {
        switch (this.type) {
        case NORMAL:
            int[] packed = {1};
            return packed;
        case ROAD:
            int[] packed2 = {2};
            return packed2;
        case VOID:
            int[] packed3 = {3};
            return packed3;
        case OFF_MAP:
            int[] packed4 = {4};
            return packed4;
        default:
            int[] packed5 = {-1};
            return packed5;
        }
    }
    
    public void toUnpacked(int[] packed) {
        switch (packed[0]) {
        case 1:
            this.type = TerrainTile.NORMAL;
        case 2:
            this.type = TerrainTile.ROAD;
        case 3:
            this.type = TerrainTile.VOID;
        case 4:
            this.type = TerrainTile.OFF_MAP;
        default:
            return;
        }
    }
}
