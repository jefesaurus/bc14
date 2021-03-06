package b_bot.robots;

import b_bot.Constants;
import b_bot.managers.InfoArray.Command;
import b_bot.navigation.NavigationMode;
import b_bot.util.CowGrowth;
import b_bot.util.Util;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public class SoldierRobot extends BaseRobot {

    public enum BehaviorState {
        BUILD_PASTR,
        BUILD_NOISE_TOWER,
        SQUADING,
        ATTACKING_PASTR,
        DEFENDING_PASTR
    }

    public enum ConstructionState {
        INIT, 
        MOVE_TO_COARSE_LOC, 
        MOVE_TO_EXACT_LOC
    }


    static int pathCreatedRound = -1;
    int squadNum = 0;
    Command currentCommand;

    //PASTR and NOISETOWER vars
    public final int FUZZY_BUILDING_PLACEMENT = 20;
    public ConstructionState cstate;
    public MapLocation adjacent;
    public MapLocation tower_loc;
    public MapLocation pastr_loc;


    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);          
        squadNum = comms.getNewSpawnSquad();
        currentCommand = comms.getSquadCommand(squadNum);
        rc.setIndicatorString(1, "Squad: " + squadNum);
        nav.setNavigationMode(NavigationMode.BUG);
        cstate = ConstructionState.INIT;
    }

    @Override
    public void run() throws GameActionException {
        currentCommand = comms.getSquadCommand(squadNum);
        rc.setIndicatorString(2, currentCommand.toString());
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
        MapLocation destination = currentCommand.loc;


        switch (currentCommand.type) {
        case RALLY_POINT:
        case ATTACK_POINT:
            if(nearbyEnemies.length > 0) {
                if (rc.isActive()) {
                    offensiveMicro(nearbyEnemies);
                }
            } else {
                simpleBug(destination);
            }
            break;

        case ATTACK_PASTR:
            if(nearbyEnemies.length > 0) {
                if (rc.isActive()) {
                    offensiveMicro(nearbyEnemies);
                }
            } else {
                simpleBug(destination);
            }
            break;

        case DEFEND_PASTR:
        case BUILD_PASTR:
            switch (this.cstate) {
            case INIT: 
                while (comms.getSearchCoordinates()[3] == 0) {
                  rc.yield();
                }
                int[] sc = comms.getSearchCoordinates();
                int[] bestLoc = new CowGrowth(this.rc, this).getBestLocation(sc[0], sc[1], sc[2], sc[3]);
                int HQBestScore = comms.wait_P_PASTR_SCORE_1();
                if (HQBestScore > bestLoc[2]) {
                    System.out.println("HQ BETTER");
                    int[] HQBestLoc = comms.wait_P_PASTR_LOC_1();
                    nav.setDestination(new MapLocation(HQBestLoc[0], HQBestLoc[1]));
                    comms.setP_PASTR_LOC2(HQBestLoc);
                } else {
                    System.out.println("HQ NOT BETTER");
                    nav.setDestination(new MapLocation(bestLoc[0], bestLoc[1]));
                    comms.setP_PASTR_LOC2(bestLoc);
                }
                System.out.println("done with msgs");
                rc.setIndicatorString(2, nav.getDestination().toString());
                this.cstate = ConstructionState.MOVE_TO_COARSE_LOC;
                break;

            case MOVE_TO_COARSE_LOC:
                System.out.println("coarse loc");
                if (this.curLoc.distanceSquaredTo(nav.getDestination()) > FUZZY_BUILDING_PLACEMENT) {
                    Direction toMove = nav.navigateToDestination();
                    if (toMove != null) {
                        simpleMove(toMove);
                    }
                } else {
                    comms.setPastrLoc(this.curLoc);
                    this.cstate = ConstructionState.MOVE_TO_EXACT_LOC;
                }
                break;

            case MOVE_TO_EXACT_LOC: 
                if (rc.isActive()) {
                    System.out.println("constructing pastr now!");
                    rc.construct(RobotType.PASTR);
                    
                    break;
                }
            }
            break;
        case BUILD_NOISE_TOWER:
            int[] msg = comms.wait_P_PASTR_LOC_2();
            MapLocation possiblePastrLoc = new MapLocation(msg[0], msg[1]);
            

            switch (this.cstate) {
            case INIT:
                destination = possiblePastrLoc;
                nav.setDestination(possiblePastrLoc);
                this.cstate = ConstructionState.MOVE_TO_COARSE_LOC;
                break;  
            case MOVE_TO_COARSE_LOC:
                System.out.println("coarse loc");
                if (this.curLoc.distanceSquaredTo(nav.getDestination()) > FUZZY_BUILDING_PLACEMENT) {
                    simpleBug(nav.getDestination());
                } else {
                    this.cstate = ConstructionState.MOVE_TO_EXACT_LOC;
                }
                break;
            case MOVE_TO_EXACT_LOC:
                System.out.println("exact loc");
                MapLocation exactLoc = comms.wait_PASTR_LOC_FINAL();
                adjacent = findAdjacentSquare(exactLoc);
                nav.setDestination(adjacent);
                if (!this.curLoc.equals(adjacent)) {
                    simpleBug(adjacent);
                } else {
                    if (rc.isActive()) {
                        rc.construct(RobotType.NOISETOWER);
                    }
                }
                break;
            default:
                break;
            }
        }
        rc.yield();

    }

    private MapLocation findAdjacentSquare(MapLocation loc) {
        Direction dir = Direction.values()[(int) (Util.randDouble() * 8)];
        MapLocation curLoc = loc.add(dir);
        while (rc.senseTerrainTile(curLoc) == TerrainTile.VOID || rc.senseTerrainTile(curLoc) == TerrainTile.OFF_MAP) {
            dir = dir.rotateRight();
            curLoc = loc.add(dir);
        }
        return curLoc;
    }


    public void simpleBug(MapLocation destination) throws GameActionException {
        nav.setDestination(destination);
        Direction toMove = nav.navigateToDestination();
        if (toMove != null) {
            simpleMove(toMove);
        }

    }

    protected void simpleMove(Direction chosenDirection) throws GameActionException{
        if(chosenDirection == Direction.OMNI || chosenDirection == Direction.NONE){
            rc.yield();
            return;
        }
        if(rc.isActive()){
            if(rc.canMove(chosenDirection)){
                rc.move(chosenDirection);
                return;
            }
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:Constants.directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = Constants.allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.move(trialDir);
                    break;
                }
            }
        }
    }

    protected void simpleMoveVeerOff(Direction chosenDirection) throws GameActionException{
        if(rc.isActive()){
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:Constants.directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = Constants.allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    rc.move(trialDir);
                    break;
                }
            }
        }
    } 



    /*
     * BATTLE MICRO
     */

    public void offensiveMicro(Robot[] nearbyEnemies) throws GameActionException {
        // Enemy unittype counters
        boolean enemyHQInSight = false;
        boolean enemyHQInRange = false;

        int numEnemySoldiers = 0;


        // Useful metrics
        double enemyNumHits = 0;

        int enemyCentroidX = 0;
        int enemyCentroidY = 0;

        int potentialDamage = 0; // Theoretically this is the most damage the robot could take.

        // Tally up counters & metrics
        RobotInfo lowestHealthAttackableSoldier = null;
        double lowestSoldierHealth = Integer.MAX_VALUE;
        MapLocation pastrLoc = null;
        MapLocation towerLoc;

        for (Robot b : nearbyEnemies) {
            RobotInfo info = rc.senseRobotInfo(b);

            switch(info.type) {
            case SOLDIER:
                numEnemySoldiers++;
                enemyCentroidX += info.location.x;
                enemyCentroidY += info.location.y;
                if (rc.canAttackSquare(info.location)) {
                    potentialDamage += RobotType.SOLDIER.attackPower;
                    enemyNumHits += 1 + (int)info.health;
                    if (info.health < lowestSoldierHealth) {
                        lowestHealthAttackableSoldier = info;
                        lowestSoldierHealth = info.health;
                    }
                }
                break;

            case PASTR:
                pastrLoc = info.location;
                break;

            case NOISETOWER:
                towerLoc = info.location;
                break;

            case HQ:
                enemyHQInSight = true;
                if (this.curLoc.distanceSquaredTo(info.location) <= RobotType.HQ.attackRadiusMaxSquared) {
                    enemyHQInRange = true;
                    potentialDamage += RobotType.HQ.attackPower;
                }
                break;

            default:
                break;
            }
        }
        MapLocation enemyCentroid;

        if (numEnemySoldiers > 0) {
            // Finalize the averaged metrics
            enemyCentroid = new MapLocation(enemyCentroidX/numEnemySoldiers, enemyCentroidY/numEnemySoldiers);
        } else {
            enemyCentroid = this.enemyHQ;
        }


        Robot[] allies = rc.senseNearbyGameObjects(Robot.class, Constants.SOLDIER_SIGHT_RANGE, rc.getTeam());

        // Enemy unittype counters
        int numAllySoldiers = 0;

        // Useful metrics
        double allyAverageSoldierHealth = 0;
        double allyNumHits = 0;

        int allyCentroidX = 0;
        int allyCentroidY = 0;

        for (Robot b : allies) {
            RobotInfo info = rc.senseRobotInfo(b);

            if (info.type == RobotType.SOLDIER) {
                numAllySoldiers++;
                allyAverageSoldierHealth += info.health;
                allyCentroidX += info.location.x;
                allyCentroidY += info.location.y;
                allyNumHits += 1 + (int)(info.health/RobotType.SOLDIER.attackPower);
            } else if(info.type == RobotType.HQ) {
                numAllySoldiers++;
            }
        }

        MapLocation allyCentroid;
        // Finalize the averaged metrics
        if (numAllySoldiers > 0) {
            allyCentroid = new MapLocation(allyCentroidX/numAllySoldiers, allyCentroidY/numAllySoldiers);
            allyAverageSoldierHealth /= (numAllySoldiers);
        } else {
            allyCentroid = this.curLoc;
        }

        boolean enemyHQPastrCombo = enemyHQInSight && (pastrLoc != null) && (this.enemyHQ.distanceSquaredTo(pastrLoc) < 9);
        int unitDisadvantage = numEnemySoldiers - numAllySoldiers;
        boolean healthDisadvantage = rc.getHealth() < allyAverageSoldierHealth;



        // Retreat logic:
        // Only attack HQ pastr combo if we have unit parity/advantage
        if (enemyHQPastrCombo) {
            if (unitDisadvantage < -1) {
                // Attack PASTR
                if (rc.canAttackSquare(pastrLoc)) {
                    rc.attackSquare(pastrLoc);
                } else {
                    simpleBug(pastrLoc);
                }    
            } else {
                simpleBug(this.myHQ);
            }

            // If we are in HQ range but there is no pastr, or we have a unit disdvantage, we need to bounce
        } else if(enemyHQInRange || unitDisadvantage > 0) {
            // Retreat away or home
            Direction toMove = this.curLoc.directionTo(this.myHQ);
            rc.setIndicatorString(0, "retreating because enemy hq or unit disad");
            simpleBug(this.myHQ);
        } else if(healthDisadvantage) {
            // Retreat to center of allies
            Direction toMove = this.curLoc.directionTo(allyCentroid);
            rc.setIndicatorString(0, "retreating because health disad");
            simpleBug(this.myHQ);
        }

        if (lowestHealthAttackableSoldier != null) {
            // If we can attack, we do
            rc.setIndicatorString(0, "attacking");
            if(rc.isActive()){ 
                rc.attackSquare(lowestHealthAttackableSoldier.location);
            }

            // TODO tune what we consider a good enough advantage
        } else if (unitDisadvantage < 0 && !enemyHQInSight) {
            //Strong enough support, lets advance            
            if(pastrLoc != null) {
                if (rc.isActive() && rc.canAttackSquare(pastrLoc)) {
                    rc.attackSquare(pastrLoc);
                } else {
                    rc.setIndicatorString(0, "Advancing to enemy pastr");

                    simpleBug(pastrLoc);
                }
            } else {
                rc.setIndicatorString(0, "Advancing to enemy");

                simpleBug(enemyCentroid);
            }

        } else {
            rc.setIndicatorString(0, "Yielding");

            // Hold ground, don't waste movement
            rc.yield();
        }
    }
}