package team204.util;

import team204.manager.RobotManager;
import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class WallSet {
    private final int[] addedAtCount = new int[RobotManager.getMapWidth() * RobotManager.getMapHeight() * 8];
    
    private int count;
    {
        this.clear();
    }
    
    public int hash(MapLocation loc, Direction dir) {
        return dir.ordinal() + (loc.x + loc.y * RobotManager.getMapWidth()) * 8;
    }
    
    public boolean add(MapLocation loc, Direction dir) {
        int h = this.hash(loc, dir);
        if (this.addedAtCount[h] == this.count) {
            return false;
        } else {
            this.addedAtCount[h] = this.count;
            return true;
        }
    }
    
    public boolean contains(MapLocation loc, Direction dir) {
        return this.addedAtCount[this.hash(loc, dir)] == this.count;
    }
    
    public boolean remove(MapLocation loc, Direction dir) {
        int h = this.hash(loc, dir);
        if (this.addedAtCount[h] == this.count) {
            this.addedAtCount[h] = this.count - 1;
            return true;
        } else {
            return false;
        }
    }
    
    public void clear() {
        this.count++;
    }
}
