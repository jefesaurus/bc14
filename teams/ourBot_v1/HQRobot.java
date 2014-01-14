package ourBot_v1;

import ourBot_v1.BreadthFirst;
import ourBot_v1.VectorFunctions;
import ourBot_v1.Comms;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class HQRobot extends BaseRobot {
    static MapLocation rallyPoint;

    public HQRobot(RobotController rc) throws GameActionException {
        super(rc);
        rc.broadcast(101,VectorFunctions.locToInt(VectorFunctions.mldivide(rc.senseHQLocation(),bigBoxSize)));//this tells soldiers to stay near HQ to start
        rc.broadcast(102,-1);//and to remain in squad 1
        tryToSpawn(rc);
        BreadthFirst.init(rc, bigBoxSize);
        rallyPoint = VectorFunctions.mladd(VectorFunctions.mldivide(VectorFunctions.mlsubtract(rc.senseEnemyHQLocation(),rc.senseHQLocation()),3),rc.senseHQLocation());
    }

    @Override
    public void run() throws GameActionException {
        //TODO consider updating the rally point to an allied pastr 

        //tell them to go to the rally point
        Comms.findPathAndBroadcast(rc, 1, rc.getLocation(), rallyPoint, bigBoxSize, 2);

        //if the enemy builds a pastr, tell squad 2 to go there.
        MapLocation[] enemyPastrs = rc.sensePastrLocations(rc.getTeam().opponent());
        if(enemyPastrs.length>0){
            Comms.findPathAndBroadcast(rc, 2, rallyPoint,enemyPastrs[0],bigBoxSize,2);//for some reason, they are not getting this message
        }
        
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,10000,rc.getTeam().opponent());
        MapLocation[] robotLocations = VectorFunctions.robotsToLocations(enemyRobots, rc);
        
        if(robotLocations.length>0){
            MapLocation closestEnemyLoc = VectorFunctions.findClosest(robotLocations, rc.getLocation());
            if(closestEnemyLoc.distanceSquaredTo(rc.getLocation())<rc.getType().attackRadiusMaxSquared){//close enough to shoot
                if(rc.isActive()&&rc.canAttackSquare(closestEnemyLoc)){
                    rc.attackSquare(closestEnemyLoc);
                }
            }
        }

        //after telling them where to go, consider spawning
        tryToSpawn(rc);
    }


    public static void tryToSpawn(RobotController rc) throws GameActionException {
        if(rc.isActive()&&rc.senseRobotCount()<GameConstants.MAX_ROBOTS){
            for(int i=0;i<8;i++){
                Direction trialDir = allDirections[i];
                if(rc.canMove(trialDir)){
                    rc.spawn(trialDir);
                    break;
                }
            }
        }
    }
}
