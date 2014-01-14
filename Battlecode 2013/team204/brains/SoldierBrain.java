package team204.brains;

import team204.manager.BattleManager;
import team204.util.ActionStatus;
import team204.util.MessageType;
import team204.util.RobotAction;
import team204.util.Util;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class SoldierBrain extends RobotBrain {
    
    private MapLocation encampment = null;
    
    private final boolean isFirstDude = Clock.getRoundNum() == 0;
    
    // enum AttackState {
    // INIT, FORMUP, ENGAGE, RETREAT, SUICIDE
    // }
    //
    // private AttackState aState = AttackState.INIT;
    
    @Override
    public void step() throws GameActionException {
        setIndicatorString(1, Boolean.toString(this.isFirstDude));
        if (!this.isFirstDude) {
            this.updateAssignedEncampment();
        }
        RobotInfo nearestEnemy = this.senseNearestEnemyRobotInfo();
        // TODO: figure out arbitrary bound
        if (nearestEnemy == null) {
            if (this.encampment == null || readBroadcastMessage(MessageType.ENEMY_NUKE_HALF_DONE) != null) {
                this.stepSeekEnemyHQ();
            } else {
                this.stepCaptureEncampment();
            }
        } else {
            this.stepNearEnemy(nearestEnemy);
        }
    }
    
    private void updateAssignedEncampment() throws GameActionException {
        if (this.encampment == null) {
            int[] message = readBroadcastMessage(MessageType.ENCAMPMENT_TO_CAPTURE);
            if (message != null) {
                this.encampment = Util.unpackLocation(message[0]);
            }
        }
    }
    
    private void stepSeekEnemyHQ() throws GameActionException {
        setIndicatorString(0, "seek");
        defuseMoveWithinAdjacentToLocation(senseEnemyHQLocation());
    }
    
    private void stepCaptureEncampment() throws GameActionException {
        setIndicatorString(0, this.encampment.toString());
        if (getLocation().equals(this.encampment)) {
            if (isActive()) {
                int[] m = readBroadcastMessage(MessageType.NEXT_SUPPORT_ENCAMPMENT_TYPE);
                if (m != null) {
                    captureEncampment(RobotType.values()[m[0]]);
                }
            }
        } else {
            defuseMoveToLocation(this.encampment);
        }
    }
    
    private void stepNearEnemy(RobotInfo nearestEnemy) throws GameActionException {
        if (this.isWithinEnemyDangerZone(nearestEnemy)) {
            if (this.isEnemyVulnerable(nearestEnemy)) {
                this.microAttackEnemy(nearestEnemy);
            } else {
                this.microFleeEnemy(nearestEnemy);
            }
        } else {
            if (this.isEnemyVulnerable(nearestEnemy)) {
                this.approachEnemy(nearestEnemy);
            } else {
                this.approachJustBeyondEnemy(nearestEnemy);
            }
        }
    }
    
    private boolean isEnemyVulnerable(RobotInfo enemy) {
        return enemy.type != RobotType.SOLDIER
                || enemy.roundsUntilMovementIdle > 0
                || senseAllOtherRobots(enemy.location, BattleManager.ATTACK_READY_DISTANCE_SQUARED, getTeam()).length >= BattleManager.SAFE_BUDDY_COUNT;
    }
    
    private boolean isWithinEnemyDangerZone(RobotInfo enemy) {
        return getLocation().distanceSquaredTo(enemy.location) <= BattleManager.DANGER_ZONE_DISTANCE_SQUARED;
    }
    
    private void microAttackEnemy(RobotInfo nearestEnemy) throws GameActionException {
        ActionStatus status = moveDuringBattle();
        if (status.failure()) {
            defuseMoveWithinAdjacentToLocation(nearestEnemy.location);
        }
    }
    
    private void microFleeEnemy(RobotInfo nearestEnemy) throws GameActionException {
        ActionStatus status = this.fleeBeyond(nearestEnemy.location, BattleManager.DANGER_ZONE_DISTANCE_SQUARED);
        if (status.failure()) {
            this.microAttackEnemy(nearestEnemy);
        }
    }
    
    private void approachEnemy(RobotInfo nearestEnemy) throws GameActionException {
        defuseMoveWithinAdjacentToLocation(nearestEnemy.location);
    }
    
    private void approachJustBeyondEnemy(RobotInfo nearestEnemy) throws GameActionException {
        defuseMoveJustBeyondDistanceFromLocation(nearestEnemy.location, BattleManager.DANGER_ZONE_DISTANCE_SQUARED);
    }
    
    @RobotAction
    private ActionStatus fleeBeyond(MapLocation loc, int distanceSquared) throws GameActionException {
        if (!isActive()) {
            return ActionStatus.FAILURE;
        }
        if (getLocation().distanceSquaredTo(loc) > distanceSquared) {
            return ActionStatus.SUCCESS;
        } else {
            Direction dir = getLocation().directionTo(loc).opposite();
            Direction dirLeft = dir, dirRight = dir.rotateRight();
            for (int i = 0; i < 8; i++) {
                dir = i % 2 == 0 ? dirLeft : dirRight;
                MapLocation inDir = getLocation().add(dir);
                if (canMove(dir) && senseNonAlliedMine(inDir) == null) {
                    if (inDir.distanceSquaredTo(loc) > distanceSquared) {
                        return move(dir).failureIfRunning();
                    }
                }
                if (i % 2 == 0) {
                    dirLeft = dirLeft.rotateLeft();
                } else {
                    dirRight = dirRight.rotateRight();
                }
            }
            return ActionStatus.FAILURE;
        }
    }
    
    private RobotInfo senseNearestEnemyRobotInfo() throws GameActionException {
        RobotInfo nearest = null;
        int nearestDistanceSquared = Integer.MAX_VALUE;
        for (Robot r : senseEnemyRobots()) {
            RobotInfo info = senseRobotInfo(r);
            int distanceSquared = getLocation().distanceSquaredTo(info.location);
            if (nearest == null || distanceSquared < nearestDistanceSquared) {
                nearest = info;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return nearest;
    }
    
    // public void attack() throws GameActionException {
    // switch (this.aState) {
    // case INIT:
    // // TODO: decide what state you're in.
    // break;
    // case FORMUP:
    // // TODO: create a concave around enemy, stay more than 2sqrt2 away from the enemy, and decide when to move
    // // in
    // break;
    // case ENGAGE:
    // // TODO: attack, move past enemies while fighting, surround/focus, and switch to retreat or out of attack if
    // // necessary
    // break;
    // case SUICIDE:
    // // TODO: run past enemy to attempt to distract them.
    // break;
    // case RETREAT:
    // // TODO: run away from the enemy making sure there's people distracting too. Be ready to re-engage
    // break;
    // }
    // }
}
