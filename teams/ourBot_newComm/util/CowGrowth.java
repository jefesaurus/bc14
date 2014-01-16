package ourBot_newComm.util;

import battlecode.common.*;

public class CowGrowth {
    
    private static RobotController rc;
    public static double[][] cowGrowth;
    public static int bigBoxSize=5; //Magic Number
    public static double[][] coarseCowGrowth;
    public static double[][] finalLocations;
    
    public static void init(RobotController rci){
        rc = rci;
        cowGrowth = rc.senseCowGrowth();
    }
    
    public static void assessCowGrowth() {
        int width = rc.getMapWidth()/bigBoxSize;
        int height = rc.getMapHeight()/bigBoxSize;
        coarseCowGrowth = new double[width][height];
        
        for(int x=width*bigBoxSize;--x>=0;x++){
            for(int y=height*bigBoxSize;--y>=0;){
                coarseCowGrowth[x/bigBoxSize][y/bigBoxSize]+=cowGrowth[x][y];
            }
        }
        
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
