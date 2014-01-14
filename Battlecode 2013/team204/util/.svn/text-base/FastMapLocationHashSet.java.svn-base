package team204.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import team204.manager.RobotManager;
import battlecode.common.MapLocation;

/**
 * A lightweight hashset abstract superclass which supports adding, removing, and membership testing, as well as fast
 * constant-time clearing.
 */
public class FastMapLocationHashSet implements Set<MapLocation> {
    
    private final int[] addedAtCount = new int[RobotManager.getMapWidth() * RobotManager.getMapHeight()];
    
    private int count;
    {
        this.clear();
    }
    
    public int hash(MapLocation e) {
        return e.x + e.y * RobotManager.getMapWidth();
    }
    
    @Override
    public boolean add(MapLocation e) {
        int h = this.hash(e);
        if (this.addedAtCount[h] == this.count) {
            return false;
        } else {
            this.addedAtCount[h] = this.count;
            return true;
        }
    }
    
    @Override
    public boolean contains(Object o) {
        return this.addedAtCount[this.hash((MapLocation) o)] == this.count;
    }
    
    @Override
    public boolean remove(Object o) {
        int h = this.hash((MapLocation) o);
        if (this.addedAtCount[h] == this.count) {
            this.addedAtCount[h] = this.count - 1;
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void clear() {
        this.count++;
    }
    
    @Override
    public boolean addAll(Collection<? extends MapLocation> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Iterator<MapLocation> iterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
    
}
