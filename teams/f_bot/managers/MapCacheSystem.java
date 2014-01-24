package d_bot.managers;

import d_bot.robots.BaseRobot;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;

public class MapCacheSystem {
    final BaseRobot br;
    final int mapWidth;
    final int mapHeight;
    TerrainTile[][] terrain;

    

    public MapCacheSystem(BaseRobot baseRobot) {
        this.br = baseRobot;
        this.mapHeight = br.rc.getMapHeight();
        this.mapWidth = br.rc.getMapWidth();
        terrain = new TerrainTile[mapWidth][mapHeight];

        switch(baseRobot.myType) {
            case SOLDIER: break;
            case HQ: break;
            default: break;
        }
    }
    
    /*
     * Check if we have already grabbed this tile, if so return it
     * Otherwise, sense it then cache it then return it.
     */
    public TerrainTile getTerrainTile(MapLocation loc) {
        TerrainTile cached = this.terrain[loc.x][loc.y];
        if (cached != null) {
            return cached;
        } else {
            TerrainTile sensed = br.rc.senseTerrainTile(loc);
            this.terrain[loc.x][loc.y] = sensed;
            return sensed;
        }
    }
    
    /** Is the given map location an off map tile? <br>
     *  Will return false if the robot does not know. 
     */
    public boolean isOffMap(MapLocation loc) {
        return loc.x > this.mapWidth || loc.y > this.mapHeight;
    }    
    
    public boolean isVoid(MapLocation loc) {
          return getTerrainTile(loc) == TerrainTile.VOID;
    }
}