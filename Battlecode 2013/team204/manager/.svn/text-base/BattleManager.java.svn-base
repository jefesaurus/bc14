package team204.manager;

import team204.util.ActionStatus;
import team204.util.FastMapLocationHashMap;
import team204.util.RobotAction;
import team204.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class BattleManager extends BugTraceManager {
    
    public static final int DANGER_ZONE_DISTANCE_SQUARED = 8; // 2sqrt2 squared
    public static final int ATTACK_READY_DISTANCE_SQUARED = 18; // 3sqrt2 squared
    public static final int SAFE_BUDDY_COUNT = 3;
    
    private static final FastMapLocationHashMap<RobotInfo> botMap = new FastMapLocationHashMap<RobotInfo>();
    
    @RobotAction
    public static ActionStatus moveDuringBattle() throws GameActionException {
        int c = Clock.getBytecodeNum();
        botMap.clear();
        
        for (Robot b : senseAllOtherRobots(DANGER_ZONE_DISTANCE_SQUARED)) {
            RobotInfo info = senseRobotInfo(b);
            botMap.put(info.location, info);
        }
        MapLocation center = getLocation();
        
        // int bestNumKilled = 0;
        // double bestOvershoot = 0;
        // double bestLowestHealth = 40.0;
        
        int weightLowestHealth = 1; // weight per health missing on lowest guy
        int weightOvershoot = 41; // 1 more than highest possible score from lowesthealthWeight
        int weightNumKilled = 247; // 1 more than highest possible score from overshootWeight (41*6)+1
        
        int weightPerAlly = 241;
        int weightPerAdjacentAlly = 723;
        
        double bestScore = 0;
        Direction bestDirection = null;
        
        for (Direction dir : Util.REGUALR_DIRECTIONS_WITH_NONE) {
            MapLocation currentMove = center.add(dir);
            Team mineTeam = senseNonAlliedMine(currentMove);
            
            if (!botMap.containsKey(currentMove) && mineTeam == null) {
                // This is a possible move, so compute a score for this location by looking at the surroundings
                int numEnemies = 0;
                int numAllies = 0;
                int numAdjacentAllies = 0;
                double[] energonLevels = { 40.0, 40.0, 40.0, 40.0, 40.0, 40.0, 40.0, 40.0 };
                
                for (Direction dir2 : Util.REGULAR_DIRECTIONS) {
                    if (dir2 != dir.opposite()) {
                        MapLocation spot = currentMove.add(dir2);
                        if (botMap.containsKey(spot)) {
                            RobotInfo currentBot = botMap.get(spot);
                            if (currentBot.team != getTeam()) {
                                numEnemies++;
                                energonLevels[dir2.ordinal()] = currentBot.energon;
                            } else {
                                if (!dir2.isDiagonal()) {
                                    numAllies++;
                                } else {
                                    numAdjacentAllies++;
                                }
                            }
                        }
                    }
                }
                
                if (numEnemies > 0) {
                    int numKilled = 0;
                    double lowestHealth = energonLevels[0];
                    double overshoot = 0;
                    for (double health : energonLevels) {
                        double newHealth = health - (6.0 / numEnemies);
                        if (newHealth <= 0.0) {
                            numKilled++;
                            overshoot -= newHealth;
                        } else if (newHealth < lowestHealth) {
                            lowestHealth = newHealth;
                        }
                    }
                    double score = numKilled * weightNumKilled + (40.0 - lowestHealth) * weightLowestHealth
                            + (6.0 - overshoot) * weightOvershoot + weightPerAlly * numAllies + weightPerAdjacentAlly
                            * numAdjacentAllies;
                    if (score > bestScore) {
                        bestDirection = dir;
                        bestScore = score;
                    }
                }
            }
        }
        // int bestNumKilled = (int) Math.floor(bestScore / weightNumKilled);
        // int bestOvershoot = (int) Math.floor((bestScore % weightNumKilled) / weightOvershoot);
        // int bestLowestHealth = 40 - (int) Math.floor(((bestScore % weightNumKilled) % weightOvershoot)
        // / weightLowestHealth);
        // System.out.println("Finished optimization! Best score: " + bestScore + " Best direction: " + bestDirection
        // + " Bots killed: " + bestNumKilled + " Overshoot: " + bestOvershoot + " Lowest Health: "
        // + bestLowestHealth + " Bytecodes used: " + Clock.getBytecodeNum());
        // System.out.println("Best score: " + bestScore + " Best direction: " + bestDirection);
        System.out.println(Clock.getBytecodeNum() - c);
        if (bestDirection == null) {
            return ActionStatus.FAILURE;
            
        } else if (bestDirection == Direction.NONE) {
            return ActionStatus.SUCCESS;
        }
        return move(bestDirection);
    }
}
