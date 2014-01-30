package team169_working.util;

import java.util.ArrayList;

import battlecode.common.*;

public class VectorFunctions {
    public static MapLocation findClosest(MapLocation[] manyLocs, MapLocation point){
        int closestDist = 10000000;
        int challengerDist = closestDist;
        MapLocation closestLoc = null;
        for(MapLocation m:manyLocs){
            challengerDist = point.distanceSquaredTo(m);
            if(challengerDist<closestDist){
                closestDist = challengerDist;
                closestLoc = m;
            }
        }
        return closestLoc;
    }
    public static MapLocation mladd(MapLocation m1, MapLocation m2){
        return new MapLocation(m1.x+m2.x,m1.y+m2.y);
    }

    public static MapLocation mlsubtract(MapLocation m1, MapLocation m2){
        return new MapLocation(m1.x-m2.x,m1.y-m2.y);
    }

    public static MapLocation mldivide(MapLocation bigM, int divisor){
        return new MapLocation(bigM.x/divisor, bigM.y/divisor);
    }

    public static MapLocation mlmultiply(MapLocation bigM, int factor){
        return new MapLocation(bigM.x*factor, bigM.y*factor);
    }

    public static int locToInt(MapLocation m){
        return (m.x*100 + m.y);
    }

    public static MapLocation intToLoc(int i){
        return new MapLocation(i/100,i%100);
    }

    public static void printPath(ArrayList<MapLocation> path, int bigBoxSize){
        for(MapLocation m:path){
            MapLocation actualLoc = bigBoxCenter(m,bigBoxSize);
            System.out.println("("+actualLoc.x+","+actualLoc.y+")");
        }
    }
    public static MapLocation bigBoxCenter(MapLocation bigBoxLoc, int bigBoxSize){
        return mladd(mlmultiply(bigBoxLoc,bigBoxSize), new MapLocation(bigBoxSize/2,bigBoxSize/2));
    }
    
    public static MapLocation[] robotsToLocations(Robot[] robotList,RobotController rc) throws GameActionException{
        MapLocation[] robotLocations = new MapLocation[robotList.length];
        for(int i=0;i<robotList.length;i++){
            Robot anEnemy = robotList[i];
            RobotInfo anEnemyInfo = rc.senseRobotInfo(anEnemy);
            robotLocations[i] = anEnemyInfo.location;
        }
        return robotLocations;
    }
    
    public static MapLocation[] robotsToLocationsRemoveHQ(Robot[] robotList,RobotController rc) throws GameActionException{
        boolean hq = false;
        MapLocation enemyhq = rc.senseEnemyHQLocation();
        MapLocation[] robotLocations = new MapLocation[robotList.length];
        for(int i=0;i<robotList.length;i++){
            Robot anEnemy = robotList[i];
            RobotInfo anEnemyInfo = rc.senseRobotInfo(anEnemy);
            if (anEnemyInfo.location == enemyhq) {
               hq = true;
               break;
            } else {
              robotLocations[i] = anEnemyInfo.location;
            }
        }
        
        boolean hit_hq = false;
        if (hq == true) {
          robotLocations = new MapLocation[robotList.length -1];
          for (int i=0;i<robotList.length;i++) {
            Robot anEnemy = robotList[i];
            RobotInfo anEnemyInfo = rc.senseRobotInfo(anEnemy);
            if (anEnemyInfo.location == enemyhq) {
               hit_hq = true;
               continue;
            } else {
              if (hit_hq == true) {
                robotLocations[i-1] = anEnemyInfo.location;
              } else {
                robotLocations[i] = anEnemyInfo.location;
              }
            }
          }
        }
        return robotLocations;
    }
    
    public static MapLocation compoundMapAdd(MapLocation p1, MapLocation p2, int nSquares ){
        MapLocation current = p1;
        for (int i = 0; i < nSquares; i ++) {
            current = current.add(current.directionTo(p2));
        }
        return current;
    }
}

