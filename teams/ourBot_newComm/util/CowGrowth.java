package ourBot_newComm.util;

import battlecode.common.*;

public class CowGrowth {
    
    private static RobotController rc;
    public static double[][] cowGrowth;
    public static int bigBoxSize=50; //Magic Number
    public static double[][] coarseCowGrowth;
    public static double[][] finalLocations;
    public static double[][] test = new double[100][100];
    
    public CowGrowth(RobotController rci){
        rc = rci;
        cowGrowth = rc.senseCowGrowth();
    }
    
    public static void printCoarseMap(double[][] coarseMap){
        System.out.println("Coarse map:");
        for(int x=0;x<coarseMap[0].length;x++){
            for(int y=0;y<coarseMap.length;y++){
                double numberOfObstacles = coarseMap[x][y];
                System.out.print(Math.min(numberOfObstacles, 999) + " ");
            }
            System.out.println();
        }
    }

    
    public static void assessCowGrowth() {
        int width = rc.getMapWidth()/bigBoxSize;
        int height = rc.getMapHeight()/bigBoxSize;
        System.out.println("width " + width + " height " + height);
        coarseCowGrowth = new double[width][height];
        System.out.println("assessing cow growth");
        for(int x=width*bigBoxSize;--x>=0;){
            for(int y=height*bigBoxSize;--y>=0;){
                System.out.println("Beg: " + Clock.getBytecodeNum());
                test[x][y] = 1.0;
                //coarseCowGrowth[x/bigBoxSize][y/bigBoxSize]+=cowGrowth[x][y];
                System.out.println("End: " + Clock.getBytecodeNum());
            }
        }
        System.out.println("done assessing cow growth");
        System.out.println("rolling hash");
        finalLocations = new double[width][height];
        for (int x=width;--x>=0;) {
            for (int y=height;--y>=0;) {
                //Add all adjacent squares
                for (int j=-1;j<=1;j++) {
                    for (int k=-1; k<=1; k++) {
                        if (isValid(x+j,y+k)) {
                            finalLocations[x][y] += coarseCowGrowth[x+j][y+k];
                        }
                    }
                }
            }
        }
        
        printCoarseMap(finalLocations);
        
    }
    
    private static boolean isValid(int x, int y) {
        return (x >= 0 && x < rc.getMapWidth() && y >=0 && y < rc.getMapHeight());
    }
    
    //returns the center of the square that has the best location
    public static MapLocation getBestLocation() {
        
        assessCowGrowth();
        
        int finalx =-1, finaly=-1;
        double maxGrowth = -1.0;
        
        for (int x=rc.getMapWidth()/bigBoxSize; --x>=0;) {
            for (int y=rc.getMapHeight()/bigBoxSize; --y>=0;) {
                if (maxGrowth < finalLocations[x][y]) {
                    maxGrowth = finalLocations[x][y];
                    finalx = x;
                    finaly = y;
                }
            }
        }
        return new MapLocation(finalx*bigBoxSize + bigBoxSize/2, finaly*bigBoxSize + bigBoxSize/2);
    }
    
}
