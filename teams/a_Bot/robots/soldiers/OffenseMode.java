package a_Bot.robots.soldiers;

import a_Bot.Constants;
import a_Bot.managers.InfoCache;
import a_Bot.robots.SoldierRobot;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;



public class OffenseMode extends SoldierMode {
    public OffenseMode(SoldierRobot thisBot) throws GameActionException {
        super(thisBot);
    }


    @Override
    public void step() throws GameActionException {
        // Enemy unittype counters
        int numEnemies = thisBot.nearbyEnemies.length;
        boolean enemyHQInSight = false;
        boolean enemyHQInRange = false;

        int numEnemySoldiers = 0;
        int numEnemyPastrs = 0;
        int numEnemyNoiseTowers = 0;

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

        for (Robot b : thisBot.nearbyEnemies) {
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
                numEnemyPastrs++;
                pastrLoc = info.location;
                break;

            case NOISETOWER:
                numEnemyNoiseTowers++;
                towerLoc = info.location;
                break;

            case HQ:
                enemyHQInSight = true;
                if (thisBot.curLoc.distanceSquaredTo(info.location) <= RobotType.HQ.attackRadiusMaxSquared) {
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
            enemyCentroid = thisBot.enemyHQ;
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
                //allyCentroidX += 3*info.location.x; // HQ counts as 3 allied soldiers
                //allyCentroidY += 3*info.location.y;
            }
        }

        MapLocation allyCentroid;
        // Finalize the averaged metrics
        if (numAllySoldiers > 0) {
            allyCentroid = new MapLocation(allyCentroidX/numAllySoldiers, allyCentroidY/numAllySoldiers);
            allyAverageSoldierHealth /= (numAllySoldiers);
        } else {
            allyCentroid = thisBot.curLoc;
        }
        
        boolean enemyHQPastrCombo = enemyHQInSight && (pastrLoc != null) && (thisBot.enemyHQ.distanceSquaredTo(pastrLoc) < 9);
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
                simpleBug(thisBot.myHQ);
            }
            
        // If we are in HQ range but there is no pastr, or we have a unit disdvantage, we need to bounce
        } else if(enemyHQInRange || unitDisadvantage > 0) {
            // Retreat away or home
            Direction toMove = thisBot.curLoc.directionTo(thisBot.myHQ);
            rc.setIndicatorString(0, "retreating because enemy hq or unit disad");
            simpleMove(toMove);
        } else if(healthDisadvantage) {
            // Retreat to center of allies
            Direction toMove = thisBot.curLoc.directionTo(allyCentroid);
            rc.setIndicatorString(0, "retreating because health disad");
            simpleMove(toMove);
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
            rc.setIndicatorString(0, "Advancing");

            Direction toEnemy = thisBot.curLoc.directionTo(enemyCentroid).rotateLeft();

            // If we are nearer to the enemy than the rest of our squad, then veer off sideways
            // To allow them to catch up and to aid concavity
            if (thisBot.curLoc.distanceSquaredTo(enemyCentroid) < allyCentroid.distanceSquaredTo(enemyCentroid)) {
                // TODO make sure this is actually functioning like it is supposed to
                simpleMoveVeerOff(toEnemy);
            } else {
                simpleMove(toEnemy);
            }
        } else {
            rc.setIndicatorString(0, "Yielding");

            // Hold ground, don't waste movement
            rc.yield();
        }
        
    }
}

