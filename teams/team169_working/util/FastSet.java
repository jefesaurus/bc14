package team169_working.util;

import battlecode.common.MapLocation;

/**
 * Fast set for storing unsigned shorts
 */
public class FastSet {
    private boolean[] vals;
    public int length;
    
    public FastSet() {
        vals = new boolean[10001];
    }
    
    public void clear() {
        vals = new boolean[10001];
    }

    public void add(int key) {
        vals[key] = true;
        length ++;
    }
    
    public void addMapLocation(MapLocation loc) {
        vals[loc.x*100 + loc.y] = true;
        length ++;
    }
    
    public void remove(int key) {
        vals[key] = false;
        length --;
    }
    
    public void removeMapLocation(MapLocation loc) {
        vals[loc.x*100 + loc.y] = false;
        length --;
    }

    public boolean contains(int i) {
        return vals[i];
    }
    
    public boolean containsMapLocation(MapLocation loc) {
        return vals[loc.x*100 + loc.y];
    }
}