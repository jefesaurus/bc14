package ourBot_v1;

import battlecode.common.*;


public class MapAssessment{

    public static int[][] coarseMap;
    public static int bigBoxSize;
    public static int NORMAL_COST = 3;
    public static int ROAD_COST = 0;
    public static int VOID_COST = 4;
    public static int OFF_MAP_COST = 2;

    public static void assessMap(int bigBoxSizeIn,RobotController rc){
        bigBoxSize=bigBoxSizeIn;
        int coarseWidth = rc.getMapWidth()/bigBoxSize;
        int coarseHeight = rc.getMapHeight()/bigBoxSize;
        coarseMap = new int[coarseWidth][coarseHeight];
        for(int x=0;x<coarseWidth*bigBoxSize;x++){
            for(int y=0;y<coarseHeight*bigBoxSize;y++){
                coarseMap[x/bigBoxSize][y/bigBoxSize]+=countObstacles(x,y,rc);
            }
        }
        //printCoarseMap();
        //printBigCoarseMap(rc);
        //System.out.println("Big Box Size: " + bigBoxSizeIn);
    }

    public static int countObstacles(int x, int y,RobotController rc){//returns a 0 or a 1
        int terrainOrdinal = rc.senseTerrainTile(new MapLocation(x,y)).ordinal();//0 NORMAL, 1 ROAD, 2 VOID, 3 OFF_MAP
        switch(terrainOrdinal) {
        case 0:
            return NORMAL_COST;
        case 1:
            return ROAD_COST;
        case 2:
            return VOID_COST;
        case 3:
            return OFF_MAP_COST;
        default:
            return 0;
        }
    }

    public static void printCoarseMap(){
        System.out.println("Coarse map:");
        for(int x=0;x<coarseMap[0].length;x++){
            for(int y=0;y<coarseMap.length;y++){
                int numberOfObstacles = coarseMap[x][y];
                System.out.print(Math.min(numberOfObstacles, 999) + " ");
            }
            System.out.println();
        }
    }
    public static void printBigCoarseMap(RobotController rc){
        System.out.println("Fine map:");
        for(int x=0;x<coarseMap[0].length*bigBoxSize;x++){
            for(int y=0;y<coarseMap.length*bigBoxSize;y++){
                if(countObstacles(x,y,rc)==NORMAL_COST || countObstacles(x,y,rc) == ROAD_COST){//there's no obstacle, so print the box's obstacle count
                    int numberOfObstacles = coarseMap[x/bigBoxSize][y/bigBoxSize];
                    System.out.print(Math.min(numberOfObstacles, 999));
                }else{//there's an obstacle, so print an X
                    System.out.print("X");
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}