package team169.robots;

/**
 * int x1 = rc.getLocation().x;
            int y1 = rc.getLocation().y;
            for (int i=8; --i>=0;) {
                int x=linearLocs[i].x;
                int y=linearLocs[i].y;
                switch (i) {
                case 0:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        x -= CARDINAL_INCREMENT;
                    }
                case 1:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        x += CARDINAL_INCREMENT;
                    }
                case 2:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        y -= CARDINAL_INCREMENT;
                    }
                case 3:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        y += CARDINAL_INCREMENT;
                    }
                case 4:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        x -= DIAGONAL_INCREMENT;
                        y -= DIAGONAL_INCREMENT;
                    }
                case 5:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        x -= DIAGONAL_INCREMENT;
                        y += DIAGONAL_INCREMENT;
                    }
                case 6:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        x += DIAGONAL_INCREMENT;
                        y -= DIAGONAL_INCREMENT;
                    }
                case 7:
                    while (new MapLocation(x,y).distanceSquaredTo(new MapLocation(x1,y1)) > FUZZY_BORDER) {
                        while (true) {
                            if (rc.isActive()) {
                                rc.attackSquare(new MapLocation(x,y));
                                break;
                            }
                            rc.yield();
                        }
                        x += DIAGONAL_INCREMENT;
                        y += DIAGONAL_INCREMENT;
                    }
                }
            }
 */

import team169.managers.InfoArray.BuildingInfo;
import team169.managers.InfoArray.BuildingStatus;
import team169.managers.InfoArray.BuildingType;
import team169.managers.InfoArray.PastrAlarm;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class NoiseTower extends BaseRobot {
    
    public enum HerdingMethod {
        //Sweep out in a circle of decreasing radius
        RADIAL, 
        //Scoop in cows
        LINEAR, 
        //for maze-like maps, herd cows along paths
        PATHING
    }
    
    public PastrAlarm alarm;
    public HerdingMethod method;
    public int rangeSquared;
    public int range;
    MapLocation[] linearLocs;
    public final int FUZZY_BORDER = 10;
    public final int NOISE_THRESHOLD_BORDER = 20;
    public NoiseTower(RobotController rc) throws GameActionException {
        super(rc);
        comms.setBuildingStatus(BuildingType.TOWER, new BuildingInfo(curRound, BuildingStatus.ALL_GOOD, curLoc));
        rangeSquared = rc.getType().attackRadiusMaxSquared;
        range = (int) Math.sqrt(rangeSquared);
        setHerdingMethod(HerdingMethod.LINEAR);
        alarm = new PastrAlarm(0, Clock.getRoundNum());
    }
    
    public void alertIfEnemySighted() throws GameActionException {
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
        if (nearbyEnemies.length > 0) {
            alarm = new PastrAlarm(1, Clock.getRoundNum());
        }
        comms.updatePastrAlarm(RobotType.NOISETOWER, alarm);
    }
    
    @Override
    public void run() throws GameActionException {
        
        alertIfEnemySighted();

        switch (this.method) {
        case RADIAL:
            break;
        case LINEAR:
            int x1 = rc.getLocation().x;
            int y1 = rc.getLocation().y;
            for (int i=8; --i>=0;) {
                //Bresenham Line Calculation
                int x=linearLocs[i].x;
                int y=linearLocs[i].y;
                int dx = Math.abs(x1 - x);
                int dy = Math.abs(y1 - y);
                int x_step = (x > x1) ? -1 : 1;
                int y_step = (y > y1) ? -1 : 1;
                if (dx > dy) {
                    int err = dx;
                    while (new MapLocation(x,y).distanceSquaredTo(rc.getLocation()) > FUZZY_BORDER) {
                        while (true) {
                            comms.setBuildingStatus(BuildingType.TOWER, new BuildingInfo(Clock.getRoundNum(), BuildingStatus.ALL_GOOD, curLoc));

                            if (rc.isActive()) {
                                if (new MapLocation(x,y).distanceSquaredTo(rc.getLocation()) < NOISE_THRESHOLD_BORDER) {
                                    rc.attackSquareLight(new MapLocation(x,y));             
                                } else {
                                    rc.attackSquare(new MapLocation(x,y));
                                }
                                break;
                            }
                            rc.yield();
                        }
                    err -= 2*dy;
                    if (err < 0) {
                        y += y_step;
                        err += 2*dx;
                    }
                    x += x_step;
                    rc.yield();
                  }
                } else {
                    int err = dy;
                    while (new MapLocation(x,y).distanceSquaredTo(rc.getLocation()) > FUZZY_BORDER) {
                        while (true) {
                            comms.setBuildingStatus(BuildingType.TOWER, new BuildingInfo(Clock.getRoundNum(), BuildingStatus.ALL_GOOD, curLoc));

                            if (rc.isActive()) {
                                if (new MapLocation(x,y).distanceSquaredTo(rc.getLocation()) < NOISE_THRESHOLD_BORDER) {
                                    rc.attackSquareLight(new MapLocation(x,y));             
                                } else {
                                    rc.attackSquare(new MapLocation(x,y));
                                }
                                break;
                            }
                            rc.yield();
                        }
                    err -= 2*dx;
                    if (err < 0) {
                        x += x_step;
                        err += 2*dy;
                    }
                    y += y_step;
                    rc.yield();
                  }
                }
              }
            break;
        case PATHING:
            break;
        } 
    }

    
    public void setHerdingMethod(HerdingMethod method) {
        this.method = method;
        switch (method) {
        case RADIAL:
            break;
        case LINEAR:
            linearInit();
            break;
        case PATHING:
            break;
        }
    }
    
    private void linearInit() {
        MapLocation[] linearLocs = { 
                      rc.getLocation().add(range,0),
                      rc.getLocation().add(0,range),
                      rc.getLocation().add(-range,0),
                      rc.getLocation().add(0,-range),
                      rc.getLocation().add((int) (range / 1.414),(int) -(range / 1.414)),
                      rc.getLocation().add((int) (range / 1.414),(int) (range / 1.414)),
                      rc.getLocation().add((int) -(range / 1.414), (int) (range / 1.414)),
                      rc.getLocation().add((int) -(range / 1.414), (int) -(range / 1.414)),
        };
        
        this.linearLocs = linearLocs;
        
        cleanLinearLocs();
    }
    
    private void cleanLinearLocs() {

        for (int i=8; --i>=0;) {
            if (this.linearLocs[i].x < 0) {
                this.linearLocs[i] = new MapLocation(0, this.linearLocs[i].y);
            }
            
            else if (this.linearLocs[i].y < 0) {
                this.linearLocs[i] = new MapLocation(this.linearLocs[i].x, 0);
            }
            
            else if (this.linearLocs[i].y >= rc.getMapHeight()) {
                this.linearLocs[i] = new MapLocation(this.linearLocs[i].x, rc.getMapHeight()-1);
            }
            
            else if (this.linearLocs[i].x >= rc.getMapHeight()) {
                this.linearLocs[i] = new MapLocation(rc.getMapWidth()-1, this.linearLocs[i].y);
            }
            
            while (!(this.linearLocs[i].distanceSquaredTo(rc.getLocation()) < rangeSquared)) {
                switch(i){
                case 0:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x - 1, this.linearLocs[i].y);
                case 2:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x + 1, this.linearLocs[i].y);
                case 1:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x, this.linearLocs[i].y - 1);
                case 3:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x, this.linearLocs[i].y + 1);
                case 5:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x - 1, this.linearLocs[i].y - 1);
                case 4:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x - 1, this.linearLocs[i].y + 1);
                case 6:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x + 1, this.linearLocs[i].y - 1);
                case 7:
                    this.linearLocs[i] = new MapLocation(this.linearLocs[i].x + 1, this.linearLocs[i].y + 1);
                }
            }
        }  
    }
}
