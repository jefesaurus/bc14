package b_bot.util;

import b_bot.robots.BaseRobot;
import battlecode.common.RobotController;
import battlecode.common.MapLocation;

public class CowGrowth {
    
    private RobotController rc;
    public double[][] cowGrowth;
    public static int bigBoxSize = 5;
    public double[][] coarseCowGrowth;
    public double[][] finalLocations;
    public final BaseRobot myBot;
    
    public CowGrowth(RobotController rci, BaseRobot myBot){
        rc = rci;
        this.myBot = myBot;
        cowGrowth = rc.senseCowGrowth();
    }
    
    public  void printCoarseMap(double[][] coarseMap){
        System.out.println("Coarse map:");
        for(int x=0;x<coarseMap[0].length;x++){
            for(int y=0;y<coarseMap.length;y++){
                int numberOfObstacles = (int) coarseMap[x][y];
                System.out.print(Math.min(numberOfObstacles, 999) + " ");
            }
            System.out.println();
        }
    }
    
    public void assessCowGrowth(int sx, int sy, int fx, int fy) {
        int width = (fx - sx + 1)/bigBoxSize;
        int height = (fy - sy + 1)/bigBoxSize;
        coarseCowGrowth = new double[width][height];

        for(int x=width*bigBoxSize;--x>=0;){
            int x_bigBox = x/bigBoxSize;
            for(int y=height*bigBoxSize;--y>=0;){
                //System.out.println("Beg: " + Clock.getBytecodeNum());
                //test[x][y] = 1.0;
                coarseCowGrowth[x_bigBox][y/bigBoxSize] += (cowGrowth[x][y] / (100 + myBot.myHQ.distanceSquaredTo(new MapLocation(x,y)))) * (100 + myBot.enemyHQ.distanceSquaredTo(new MapLocation(x,y)));
                //System.out.println("End: " + Clock.getBytecodeNum());
            }
        }

        finalLocations = new double[width][height];
        for (int x=width;--x>=0;) {
            for (int y=height;--y>=0;) {
                //Add all adjacent squares
                for (int j=-1;j<=1;j++) {
                    for (int k=-1; k<=1; k++) {
                        if (isValid(x+j,y+k, width, height)) {
                            finalLocations[x][y] += coarseCowGrowth[x+j][y+k];
                        }
                    }
                }
            }
        }
        //System.out.println("search coordinates: (" + sx + "," + sy + "), (" + fx + "," + fy + ")");
        //printCoarseMap(finalLocations);
    }
    
    private static boolean isValid(int x, int y, int width, int height) {
        return (x >= 0 && x < width && y >=0 && y < height);
    }
    
    //returns the center of the square that has the best location
    public int[] getBestLocation(int sx, int sy, int fx, int fy) {
        
        assessCowGrowth(sx, sy, fx, fy);
        
        int finalx =-1, finaly=-1;
        double maxGrowth = -1.0;
        
        for (int x=(fx - sx + 1)/bigBoxSize; --x>=0;) {
            for (int y=(fy - sy + 1)/bigBoxSize; --y>=0;) {
                if (maxGrowth < finalLocations[x][y]) {
                    maxGrowth = finalLocations[x][y];
                    finalx = x;
                    finaly = y;
                }
            }
        }
        int[] best = { fx - finalx*bigBoxSize + bigBoxSize/2, sy + finaly*bigBoxSize + bigBoxSize/2, (int) maxGrowth };
        //System.out.println("best loc: " + "(" + best[0] + "," + best[1] + ")" + " score: " + best[2]);
        return best;
    }
    
}
