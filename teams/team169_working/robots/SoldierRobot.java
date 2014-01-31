package team169_working.robots;

import team169_working.Constants;
import team169_working.managers.InfoArray.BattleFront;
import team169_working.managers.InfoArray.BuildingInfo;
import team169_working.managers.InfoArray.BuildingStatus;
import team169_working.managers.InfoArray.BuildingType;
import team169_working.managers.InfoArray.Command;
import team169_working.navigation.NavigationMode;
import team169_working.robots.SoldierRobot.ConstructionState;
import team169_working.util.CowGrowth;
import team169_working.util.Util;
import team169_working.util.VectorFunctions;
import battlecode.common.Clock;
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
    public final int FUZZY_BUILDING_PLACEMENT = 9;
    public ConstructionState cstate;
    public MapLocation adjacent;
    public MapLocation tower_loc;
    public MapLocation pastr_loc;
    
    public MapLocation pastrToDefend = null;
    public Command lastCommand = null;
   
    //Battle Constants
    //Number of extra units we need over the opponent to force a battle
    public int[] ADVANTAGE_THRESHOLD = {0,
                                        1,1,1,1,1,1,1,
                                        2,2,2,2,2,2,2,
                                        3,3,3,3,3,3,3,
                                        4,4,4,4};
    //Number of units the enemy can have over us and still not retreat
    public int[] DEFENDERS_ADVANTAGE = {0,
                                        0,0,0,0,0,0,0,
                                        1,1,1,1,1,1,1,
                                        2,2,2,2,2,2,2,
                                        3,3,3,3};
    
    public SoldierRobot(RobotController rc) throws GameActionException {
        super(rc);          
        squadNum = comms.getNewSpawnSquad();
        currentCommand = comms.getSquadCommand(squadNum);
        rc.setIndicatorString(1, "Squad: " + squadNum);
        nav.setNavigationMode(NavigationMode.BUG);
        cstate = ConstructionState.INIT;
    }
    
    public final int DEFENSE_RADIUS_SQUARED = 9;

    @Override
    public void run() throws GameActionException {
        
        currentCommand = comms.getSquadCommand(squadNum);
        rc.setIndicatorString(2, "Squad num: " + squadNum + ", " + currentCommand.toString());
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent());
        MapLocation destination = currentCommand.loc;
        


        switch (currentCommand.type) {
        case RALLY_POINT:
        case ATTACK_POINT:
            if(nearbyEnemies.length > 0) {
                if (rc.isActive()) {
                    offensiveMicro(nearbyEnemies, null);
                }
            } else {
                simpleBug(destination, false, false);
            }
            break;

        case ATTACK_PASTR:
            if(nearbyEnemies.length > 0) {
                if (rc.isActive()) {
                    offensiveMicro(nearbyEnemies, destination);
                }
            } else {
                simpleBug(destination, false, false);
            }
            break;

        case DEFEND_PASTR:
            MapLocation possiblePastrLoc = comms.getPastrLoc();
            if(possiblePastrLoc.x > 0) {
                pastrToDefend = possiblePastrLoc;
            } else {
                pastrToDefend = destination;
            }
            
            if(curLoc.distanceSquaredTo(pastrToDefend) <= DEFENSE_RADIUS_SQUARED && nearbyEnemies.length > 0) {
                rc.setIndicatorString(0, "Attempting to defense micro. Round: " + curRound);
                if (rc.isActive()) {
                    defensiveMicro(nearbyEnemies, pastrToDefend);
                }
            } else {
                rc.setIndicatorString(0, "Bugging to defense pastr. Round: " + curRound);
                simpleBugToRadius(pastrToDefend, DEFENSE_RADIUS_SQUARED, false, false);
            } 
            
            break;
            
        case BUILD_PASTR:
            comms.setBuildingStatus(BuildingType.PASTR, new BuildingInfo(curRound, BuildingStatus.MOVING_TO, curLoc));
            BuildingInfo towerInfo = comms.getBuildingStatus(BuildingType.TOWER);
            boolean isExact = false;
            MapLocation moveTowards;
            if ((towerInfo.roundNum - curRound) < 2 && towerInfo.status == BuildingStatus.ALL_GOOD || towerInfo.status == BuildingStatus.IN_CONSTRUCTION) {
                moveTowards = towerInfo.loc;
                isExact = true;
            } else {
                moveTowards = comms.getPastrLoc();
                if (moveTowards.x == 0 && moveTowards.y == 0) {
                    rc.setIndicatorString(1, "Location invalid");
                    return;
                }
            }

            if (!isExact || this.curLoc.distanceSquaredTo(moveTowards) > FUZZY_BUILDING_PLACEMENT) {
                rc.setIndicatorString(1, "Bugging to inexact location");

                simpleBug(moveTowards, false, false);
            } else if(isExact && curLoc.isAdjacentTo(moveTowards) && rc.isActive()) {
                comms.setBuildingStatus(BuildingType.PASTR, new BuildingInfo(curRound, BuildingStatus.IN_CONSTRUCTION, curLoc));
                rc.construct(RobotType.PASTR);
            } else {
                rc.setIndicatorString(1, "Finding adjacent location");
                adjacent = findAdjacentSquare(moveTowards);
                simpleBug(adjacent, false, false);
            }

            break;
        case BUILD_NOISE_TOWER:
            
            // Get the existing ideal pastr location.
            MapLocation pastrLoc = comms.getPastrLoc();
            
            // If it isn't valid, begin calculation
            if (pastrLoc.equals(new MapLocation(0,0))) {
                comms.setBuildingStatus(BuildingType.TOWER, new BuildingInfo(curRound, BuildingStatus.IS_COMPUTING, curLoc));
                while (comms.getSearchCoordinates()[3] == 0) {
                    return; // effectively yield
                }
                int[] sc = comms.getSearchCoordinates();
                int[] bestLoc = new CowGrowth(this.rc, this).getBestLocation(sc[0], sc[1], sc[2], sc[3]);
                int HQBestScore = comms.wait_P_PASTR_SCORE_1();
                if (HQBestScore > bestLoc[2]) {
                    pastrLoc = comms.wait_P_PASTR_LOC_1();
                } else {
                    pastrLoc = new MapLocation(bestLoc[0], bestLoc[1]);
                }
                // When finished, set the ideal pastr location
                comms.setPastrLoc(pastrLoc);
            }
            
            // Navigate towards it
            if ( this.curLoc.distanceSquaredTo(pastrLoc) > FUZZY_BUILDING_PLACEMENT) {
                comms.setBuildingStatus(BuildingType.TOWER, new BuildingInfo(curRound, BuildingStatus.MOVING_TO, curLoc));
                rc.setIndicatorString(1, "Bugging to inexact location");
                simpleBug(pastrLoc, false, false);
            } else if(rc.isActive()){
                comms.setBuildingStatus(BuildingType.TOWER, new BuildingInfo(curRound, BuildingStatus.IN_CONSTRUCTION, curLoc));
                rc.construct(RobotType.NOISETOWER);  
            }
            break;
        }
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


    public void simpleBug(MapLocation destination, boolean sneak, boolean EnterHQAttackZone) throws GameActionException {
        nav.setDestination(destination);
        Direction toMove = nav.navigateToDestination(EnterHQAttackZone);
        if (toMove != null) {
            simpleMove(toMove, sneak);
        }
    }
    
    public void simpleBugToRadius(MapLocation destination, int radius, boolean sneak, boolean EnterHQAttackZone) throws GameActionException {
        int distance = curLoc.distanceSquaredTo(destination) - radius;
        if (distance > 0) {
            rc.setIndicatorString(1, "Attempting to bug: round: " + curRound);
            nav.setDestination(destination);
            Direction toMove = nav.navigateToDestination(EnterHQAttackZone);
            if (toMove != null) {
                simpleMove(toMove, sneak);
            }
        } else {
            rc.setIndicatorString(1, "Within range. No need to bug. Round: " + curRound);
        }
    }

    protected void simpleMove(Direction chosenDirection, boolean sneak) throws GameActionException{
        if(chosenDirection == Direction.OMNI || chosenDirection == Direction.NONE){
            return;
        }
        if(rc.isActive()){
            if(rc.canMove(chosenDirection)){
                if (sneak) {
                    rc.sneak(chosenDirection);
                } else {
                    rc.move(chosenDirection);
                }
                return;
            }
            int forwardInt;
            Direction trialDir;
            for(int directionalOffset:Constants.directionalLooks){
                forwardInt = chosenDirection.ordinal();
                trialDir = Constants.allDirections[(forwardInt+directionalOffset+8)%8];
                if(rc.canMove(trialDir)){
                    if (sneak) {
                        rc.sneak(trialDir);
                    } else {
                        rc.move(trialDir);
                    }
                    return;
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

    public void offensiveMicro(Robot[] nearbyEnemies, MapLocation pointToAttack) throws GameActionException {
        // Enemy unittype counters
        boolean enemyHQInSight = false;
        boolean enemyHQInRange = false;

        int numEnemySoldiers = 0;

        int squaredDistanceToClosestEnemy = Integer.MAX_VALUE;
        RobotInfo closestEnemy = null;
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
                if (squaredDistanceToClosestEnemy < this.curLoc.distanceSquaredTo(info.location)) {
                  closestEnemy = info;
                  squaredDistanceToClosestEnemy = this.curLoc.distanceSquaredTo(info.location);
                }
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

        MapLocation enemyCentroid = null;

        if (numEnemySoldiers > 0) {
            // Finalize the averaged metrics
            enemyCentroid = new MapLocation(enemyCentroidX/numEnemySoldiers, enemyCentroidY/numEnemySoldiers);
            BattleFront existing = comms.getBattle();
            if (curRound - existing.roundNum > 3 || existing.numEnemies < numEnemySoldiers) {
                comms.setBattle(new BattleFront(curRound, numEnemySoldiers, enemyCentroid));
            }
        } else if (pastrLoc == null){
            simpleBug(pointToAttack, false, false);
            return;
        } else {
            if (rc.isActive() && rc.canAttackSquare(pastrLoc)) {
                rc.attackSquare(pastrLoc);
            } else {
                simpleBug(pastrLoc, false, false);
            }
            return;
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
            rc.setIndicatorString(0, "Enemy + pastr");

            if (unitDisadvantage < -1) {
                // Attack PASTR
                if (rc.canAttackSquare(pastrLoc)) {
                    rc.attackSquare(pastrLoc);
                } else {
                    simpleBug(pastrLoc, false, true);
                }    
            } else {
                simpleBug(this.myHQ, false, false);
            }

            // If we are in HQ range but there is no pastr, or we have a unit disdvantage, we need to bounce
        } else if(enemyHQInRange || unitDisadvantage > DEFENDERS_ADVANTAGE[numEnemySoldiers]) {
            // Retreat away or home
            rc.setIndicatorString(1, "retreating because enemy hq or unit disad");
            simpleBug(this.curLoc.add(enemyCentroid.directionTo(this.curLoc), 3), false, false);
        } /*else if(healthDisadvantage) {
            // Retreat to center of allies
            rc.setIndicatorString(0, "retreating because health disadvantage");
            simpleBug(allyCentroid, false);
        }*/

        if (lowestHealthAttackableSoldier != null) {
            // If we can attack, we do
            if(rc.isActive()){ 
                rc.setIndicatorString(1, "attacking lowest health");
                if (lowestHealthAttackableSoldier.health <= RobotType.SOLDIER.attackPower) {
                    comms.updateKillCount();
                }
                rc.attackSquare(lowestHealthAttackableSoldier.location);
            } else {
                rc.setIndicatorString(1, "would attack but not active");
            }

            // TODO tune what we consider a good enough advantage
        } else if (unitDisadvantage >= -ADVANTAGE_THRESHOLD[numEnemySoldiers] && unitDisadvantage < 0 && !enemyHQInSight) {
            //Strong enough support, lets advance            
            if(pastrLoc != null) {
                if (rc.isActive() && rc.canAttackSquare(pastrLoc)) {
                    rc.attackSquare(pastrLoc);
                } else {
                    rc.setIndicatorString(0, "Advancing to enemy pastr");

                    simpleBug(pastrLoc, false, false);
                }
            } else {
                rc.setIndicatorString(0, "Advancing to enemy");
                if (closestEnemy != null) {
                    if (roundsUntilInRange(closestEnemy) <= 3) {
                //if (squaredDistanceToClosestEnemy < 13) {
                        rc.setIndicatorString(0, "wait for enemey to come attack us");
                    } else {
                        simpleBug(enemyCentroid, false, false);
                    }
                }
            }

        } else if (unitDisadvantage < -ADVANTAGE_THRESHOLD[numEnemySoldiers] && !enemyHQInSight) {
            if(pastrLoc != null) {
                if (rc.isActive() && rc.canAttackSquare(pastrLoc)) {
                    rc.attackSquare(pastrLoc);
                } else {
                    rc.setIndicatorString(0, "Advancing to enemy pastr");

                    simpleBug(pastrLoc, false, false);
                }
            } else {
                rc.setIndicatorString(0, "Advancing to enemy");
                simpleBug(enemyCentroid, false, false);    
            }
            
        } else {
            rc.setIndicatorString(1, "Yielding");

            // Hold ground, don't waste movement
            //rc.yield();
        }
    }
    
    /*
     * BATTLE MICRO
     */

    public void defensiveMicro(Robot[] nearbyEnemies, MapLocation toDefend) throws GameActionException {
        // Enemy unittype counters
        int numEnemySoldiers = 0;
        int numEnemiesAlmostInRange = 0;

        /*
        // Useful metrics
        int enemyCentroidX = 0;
        int enemyCentroidY = 0;
         */

        // Tally up counters & metrics
        RobotInfo lowestHealthAttackableSoldier = null;
        double lowestSoldierHealth = Integer.MAX_VALUE;
        
        for (Robot b : nearbyEnemies) {
            RobotInfo info = rc.senseRobotInfo(b);

            if(info.type == RobotType.SOLDIER) {
                numEnemySoldiers++;
                /*
                enemyCentroidX += info.location.x;
                enemyCentroidY += info.location.y;
                */
                if (rc.canAttackSquare(info.location)) {

                    if (info.health < lowestSoldierHealth) {
                        lowestHealthAttackableSoldier = info;
                        lowestSoldierHealth = info.health;
                    }
                } else {
                    if (roundsUntilInRange(info) < 3.0) {
                        // This thing will be in range before we have another turn, lets camp.
                        numEnemiesAlmostInRange++;
                    }
                }
            }
        }
        
        // If there is no attackable soldier, we yield
        if (lowestHealthAttackableSoldier == null) {
            if (numEnemySoldiers > 0) {
                if (numEnemiesAlmostInRange > 0) {
                    return;
                } else {
                    if (curLoc.distanceSquaredTo(toDefend) > 25) {
                        rc.setIndicatorString(1, "Bugging: Because no one to attack and not in range of base.");
                        simpleBug(toDefend, false, false);
                    } else {
                        return;
                    }
                }   
            }
        } else {
            rc.setIndicatorString(2, "lowest health attackable soldier: " + lowestHealthAttackableSoldier.location.toString());
            if (rc.isActive() && rc.canAttackSquare(lowestHealthAttackableSoldier.location)) {
                rc.attackSquare(lowestHealthAttackableSoldier.location);
            }
        }
    }
    
    public int roundsUntilInRange(RobotInfo target) {
        return (int)(target.actionDelay + 1
                + ((int)Math.sqrt(target.location.distanceSquaredTo(this.curLoc))
                        - (int)Math.sqrt(RobotType.SOLDIER.attackRadiusMaxSquared))*2);
    }
}