package team204.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import team204.manager.RobotManager;
import battlecode.common.MapLocation;

/**
 * A lightweight hashset abstract superclass which supports adding, removing, and membership testing, as well as fast
 * constant-time clearing.
 */
public class FastMapLocationHashMap<T> implements Map<MapLocation, T> {
    private final int mapWidth = RobotManager.getMapWidth();
    private final int mapHeight = RobotManager.getMapHeight();
    private final int[] addedAtCount = new int[this.mapWidth * this.mapHeight];
    private final Object[] values = new Object[this.mapWidth * this.mapHeight];
    
    private int size = 0;
    
    private int count = 1;
    
    @Override
    public String toString() {
        String repr = "{";
        for (int i = 0; i < this.mapWidth * this.mapHeight; i++) {
            if (this.addedAtCount[i] == this.count) {
                repr += "(" + this.reverseHashX(i) + "," + this.reverseHashY(i) + "):" + this.values[i].toString()
                        + ",";
            }
        }
        return repr + "}";
    }
    
    public int hash(MapLocation e) {
        return e.x + e.y * this.mapWidth;
    }
    
    public int reverseHashX(int hash) {
        return hash % this.mapWidth;
    }
    
    public int reverseHashY(int hash) {
        return (int) Math.floor(hash / this.mapWidth);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T remove(Object o) {
        int h = this.hash((MapLocation) o);
        if (this.addedAtCount[h] == this.count) {
            this.size--;
            this.addedAtCount[h] = this.count - 1;
            return (T) this.values[h];
        } else {
            return null;
        }
    }
    
    @Override
    public void clear() {
        this.count++;
        this.size = 0;
    }
    
    @Override
    public boolean containsKey(Object key) {
        int hash = this.hash((MapLocation) key);
        return (hash > 0 && hash < this.addedAtCount.length && this.addedAtCount[hash] == this.count);
    }
    
    @Override
    public boolean containsValue(Object value) {
        for (Object stored : this.values) {
            if (stored.equals(value)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Set<java.util.Map.Entry<MapLocation, T>> entrySet() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Set<MapLocation> keySet() {
        throw new UnsupportedOperationException();
    }
    
    // Careful, this will die if used on a location outside of the map.
    @SuppressWarnings("unchecked")
    @Override
    public T put(MapLocation key, T value) {
        int h = this.hash(key);
        if (this.addedAtCount[h] == this.count) {
            T oldValue = (T) this.values[h];
            this.values[h] = value;
            return oldValue;
        } else {
            this.size++;
            this.addedAtCount[h] = this.count;
            this.values[h] = value;
            return null;
        }
    }
    
    @Override
    public void putAll(Map<? extends MapLocation, ? extends T> m) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Collection<T> values() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isEmpty() {
        return this.size < 1;
    }
    
    @Override
    public int size() {
        return this.size;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T get(Object key) {
        int hash = this.hash((MapLocation) key);
        if (this.addedAtCount[hash] == this.count) {
            return (T) this.values[hash];
        }
        throw new UnsupportedOperationException();
    }
}