package ourBot_newComm.managers;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class InfoCache {
    public static RobotController rc;
    public static int mapWidth;
    public static int mapHeight;
    public static MapLocation HQLocation;
    public static MapLocation enemyHQLocation;

    
    public InfoCache(RobotController rc) {
        InfoCache.rc = rc;
        InfoCache.mapWidth = rc.getMapWidth();
        InfoCache.mapWidth = rc.getMapHeight();
        InfoCache.HQLocation = rc.senseHQLocation();
        InfoCache.enemyHQLocation = rc.senseEnemyHQLocation();
    }
}
