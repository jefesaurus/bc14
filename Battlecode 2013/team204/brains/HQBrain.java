package team204.brains;

import team204.util.ActionStatus;
import team204.util.MessageType;
import team204.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotType;
import battlecode.common.Upgrade;

public class HQBrain extends RobotBrain {
    
    private MapLocation[] encampmentsToCapture = senseAllEncampmentSquares();
    private final double enemyHQPriorityScore = Util.calculateEncampmentPriorityScore(senseEnemyHQLocation());
    
    boolean spawning = true;
    MapLocation currentEncampmentToCapture = null;
    
    @Override
    public void step() throws GameActionException {
        if (senseEnemyNukeHalfDone()) {
            broadcastMessage(MessageType.ENEMY_NUKE_HALF_DONE, null, true);
        }
        if (this.spawning) {
            this.stepSpawn();
        } else {
            this.stepBroadcastEncampment();
        }
        
        RobotType type = this.hasEnoughPowerGeneration(Util.UNIT_MAX_POWER_UPKEEP, 1) ? RobotType.SUPPLIER : RobotType.GENERATOR;
        broadcastMessage(MessageType.NEXT_SUPPORT_ENCAMPMENT_TYPE, new int[] { type.ordinal() }, true);
        
        this.researchNextUpgrade();
    }
    
    private Direction findSafeSpawnDirection(Direction idealDirection) throws GameActionException {
        Direction dirLeft = idealDirection, dirRight = idealDirection.rotateRight();
        for (int i = 0; i < 8; i++) {
            Direction dir = i % 2 == 0 ? dirLeft : dirRight;
            if (canMove(dir) && senseNonAlliedMine(getLocation().add(dir)) == null) {
                return dir;
            }
            if (i % 2 == 0) {
                dirLeft = dirLeft.rotateLeft();
            } else {
                dirRight = dirRight.rotateRight();
            }
        }
        return null;
    }
    
    private void stepSpawn() throws GameActionException {
        if (this.currentEncampmentToCapture == null && Clock.getRoundNum() > 0) {
            this.currentEncampmentToCapture = this.popSafestEncampment();
        }
        if (this.hasEnoughPowerGeneration(Util.UNIT_MAX_POWER_UPKEEP, 0)) {
            if (this.currentEncampmentToCapture == null || senseEnemyNukeHalfDone()) {
                Direction spawnDir = this.findSafeSpawnDirection(getLocation().directionTo(senseEnemyHQLocation()));
                if (spawnDir != null) {
                    spawn(spawnDir);
                }
            } else {
                Direction spawnDir = this.findSafeSpawnDirection(getLocation().directionTo(
                        this.currentEncampmentToCapture));
                if (spawnDir != null) {
                    ActionStatus status = spawn(spawnDir);
                    if (status.success()) {
                        this.spawning = false;
                        this.stepBroadcastEncampment();
                    }
                }
            }
        }
    }
    
    private void stepBroadcastEncampment() throws GameActionException {
        ActionStatus status = broadcastMessage(MessageType.ENCAMPMENT_TO_CAPTURE,
                new int[] { Util.packLocation(this.currentEncampmentToCapture) }, false);
        if (status.success()) {
            this.spawning = true;
            this.currentEncampmentToCapture = null;
            this.stepSpawn();
        }
    }
    
    private static final Upgrade[] UPGRADE_ORDER = { Upgrade.FUSION, Upgrade.VISION, Upgrade.DEFUSION, Upgrade.NUKE,
            Upgrade.PICKAXE };
    private int upgradeIndex = 0;
    
    public void researchNextUpgrade() throws GameActionException {
        while (true) {
            if (this.upgradeIndex == UPGRADE_ORDER.length) {
                break;
            }
            Upgrade upgrade = UPGRADE_ORDER[this.upgradeIndex];
            ActionStatus status = researchUpgrade(upgrade);
            if (status.success()) {
                System.out.println("HQ has completed the upgrade " + upgrade + " before round " + Clock.getRoundNum());
                this.upgradeIndex++;
            } else {
                break;
            }
        }
    }
    
    public MapLocation popSafestEncampment() throws GameActionException {
        if (this.encampmentsToCapture == null) {
            return null;
        }
        int bestEncampmentIndex = -1;
        double bestScore = 0;
        for (int i = 0; i < this.encampmentsToCapture.length; i++) {
            if (this.encampmentsToCapture[i] == null) {
                continue;
            }
            double score = Util.calculateEncampmentPriorityScore(this.encampmentsToCapture[i]);
            if (score < this.enemyHQPriorityScore) {
                continue;
            }
            if (bestEncampmentIndex == -1 || score > bestScore) {
                bestEncampmentIndex = i;
                bestScore = score;
            }
        }
        if (bestEncampmentIndex == -1) {
            this.encampmentsToCapture = null;
            return null;
        } else {
            MapLocation bestEncampment = this.encampmentsToCapture[bestEncampmentIndex];
            this.encampmentsToCapture[bestEncampmentIndex] = null;
            return bestEncampment;
        }
        
        // if (this.encampmentSafety == null) {
        // this.encampmentSafety = Util.sortBySafety(senseAllEncampmentSquares());
        // }
        // for (int i = 0; i < this.encampmentSafety.length; i++) {
        // if (this.encampmentSafety[i] != null) {
        // MapLocation safestEncampment = this.encampmentSafety[i];
        // this.encampmentSafety[i] = null;
        // return safestEncampment;
        // }
        // }
        // return null;
    }
    
    // private int numGenerators;
    
    // public void updateNumGenerators() throws GameActionException {
    // int c = Clock.getBytecodeNum();
    // this.numGenerators = 0;
    // for (Robot r : senseAlliedRobots()) {
    // RobotInfo info = senseRobotInfo(r);
    // if (info.type == RobotType.GENERATOR) {
    // this.numGenerators++;
    // }
    // }
    // System.out.println(Clock.getBytecodeNum() - c);
    // }
    
    // public double getPowerGeneration() {
    // return GameConstants.HQ_POWER_PRODUCTION + this.numGenerators * GameConstants.GENERATOR_POWER_PRODUCTION;
    // }
    
    public boolean hasEnoughPowerGeneration(double upkeep, int extraBuildings) throws GameActionException {
        // int[] m = readBroadcastMessage(MessageType.ALLIED_GENERATOR_COUNT);
        // if (m != null) {
        // this.numGenerators = m[0];
        // }
        // broadcastMessage(MessageType.ALLIED_GENERATOR_COUNT, new int[] { 0 });
        Robot[] alliedRobots = senseAllOtherAlliedRobots();
        double genNecessary = upkeep + Util.UNIT_MAX_POWER_UPKEEP * alliedRobots.length
                + calculatePowerBuildupGeneration(extraBuildings) - GameConstants.HQ_POWER_PRODUCTION;
        if (genNecessary < 0) {
            return true;
        }
        for (Robot r : alliedRobots) {
            RobotType type = senseRobotType(r);
            if (type == RobotType.GENERATOR) {
                genNecessary -= GameConstants.GENERATOR_POWER_PRODUCTION;
                if (genNecessary < 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public int senseHQSpawnDelay() throws GameActionException {
        int numSuppliers = 0;
        for (Robot r : senseAllOtherAlliedRobots()) {
            RobotType type = senseRobotType(r);
            if (type == RobotType.SUPPLIER) {
                numSuppliers++;
            }
        }
        
        return (int) Math.round(GameConstants.HQ_SPAWN_DELAY * GameConstants.HQ_SPAWN_DELAY_CONSTANT
                / (GameConstants.HQ_SPAWN_DELAY_CONSTANT + numSuppliers));
    }
}
