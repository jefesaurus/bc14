package team204.util;

/**
 * An enum indicating the running state of of a robot activity. Such activities include, but are not limited to, ones
 * which cause the robot to perform one or more actions in the game world.
 */
public enum ActionStatus {
    /**
     * The activity is still in the process of being performed.
     */
    RUNNING,
    /**
     * The activity has been successfully completed.
     */
    SUCCESS,
    /**
     * The activity has terminated without being completed.
     */
    FAILURE, ;
    
    public boolean running() {
        return this == RUNNING;
    }
    
    public boolean success() {
        return this == SUCCESS;
    }
    
    public boolean failure() {
        return this == FAILURE;
    }
    
    public boolean done() {
        return !this.running();
    }
    
    public ActionStatus runningIfSuccess() {
        return this.success() ? ActionStatus.RUNNING : this;
    }
    
    public ActionStatus failureIfRunning() {
        return this.running() ? ActionStatus.FAILURE : this;
    }
}
