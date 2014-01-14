package team204.manager;

import team204.util.ActionStatus;
import team204.util.MessageType;
import team204.util.RobotAction;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.Team;

public class MessagingManager extends BattleManager {
    
    private static final int SECRET_KEY = (getTeam() == Team.A ? "yo ho ho" : "and a bottle of rum").hashCode();
    
    private static final long RELATIVE_PRIME_BASE = 2276L;
    private static final int RELATIVE_PRIME_MOD = 9973;
    private static final int PERMUTE_OFFSET = 6037;
    
    private static final int STREAM_SIZE = 7;
    
    public static final int MAX_CHANNELS = RELATIVE_PRIME_MOD - 1;
    public static final int MAX_MESSAGE_SIZE = STREAM_SIZE - 2;
    
    private static final int[] permutationBase = new int[RELATIVE_PRIME_MOD - 1];
    
    private static int permuteBase(int channel) {
        if (permutationBase[channel] == 0) {
            if (channel == 0) {
                permutationBase[channel] = 1;
            } else {
                permutationBase[channel] = (int) ((permuteBase(channel - 1) * RELATIVE_PRIME_BASE) % RELATIVE_PRIME_MOD);
            }
        }
        return permutationBase[channel];
    }
    
    private static int permute(int channel) {
        return (permuteBase(channel) + PERMUTE_OFFSET) % GameConstants.BROADCAST_MAX_CHANNELS;
    }
    
    private static int encrypt(int data) {
        return data ^ SECRET_KEY;
    }
    
    private static int decrypt(int data) {
        return data ^ SECRET_KEY;
    }
    
    @RobotAction
    private static ActionStatus broadcastPermuted(int channel, int data) throws GameActionException {
        return broadcast(permute(channel), data);
    }
    
    private static final int[][][] broadcastMessages = new int[MessageType.values().length][][];
    private static final int[][] broadcastMessageRounds = new int[MessageType.values().length][];
    static {
        for (MessageType type : MessageType.values()) {
            broadcastMessages[type.ordinal()] = new int[type.streamCount][];
            broadcastMessageRounds[type.ordinal()] = new int[type.streamCount];
        }
    }
    
    @RobotAction
    private static ActionStatus broadcastStream(MessageType type, int stream, int[] message, boolean metaOnly)
            throws GameActionException {
        int streamBaseChannel = (type.getStreamBaseIndex() + stream) * STREAM_SIZE;
        int checksum = 0;
        for (int i = 0; i < STREAM_SIZE; i++) {
            int data;
            boolean broadcast = true;
            if (i == STREAM_SIZE - 1) {
                data = checksum;
            } else {
                if (i == 0) {
                    data = Clock.getRoundNum();
                } else if (message == null || i - 1 >= message.length) {
                    data = decrypt(readBroadcastPermuted(streamBaseChannel + i));
                    broadcast = false;
                } else {
                    data = message[i - 1];
                }
                checksum ^= data;
            }
            if (broadcast && (!metaOnly || i == 0 || i == STREAM_SIZE - 1)) {
                ActionStatus status = broadcastPermuted(streamBaseChannel + i, encrypt(data));
                if (status.failure()) {
                    return status;
                }
            }
        }
        broadcastMessages[type.ordinal()][stream] = message;
        broadcastMessageRounds[type.ordinal()][stream] = Clock.getRoundNum();
        return ActionStatus.SUCCESS;
    }
    
    private static final int[] startStream = new int[MessageType.values().length];
    
    @RobotAction
    private static final ActionStatus broadcastMessageAttempt(MessageType type, int[] message, boolean continuous)
            throws GameActionException {
        int[] storedMessage = broadcastMessageRounds[type.ordinal()][startStream[type.ordinal()]] < Clock.getRoundNum() - 1 ? null : broadcastMessages[type
                .ordinal()][startStream[type.ordinal()]];
        if (storedMessage == null) {
            return broadcastStream(type, startStream[type.ordinal()], message, false).runningIfSuccess();
        }
        int[] streamMessage = readBroadcastStream(type, startStream[type.ordinal()]);
        if (streamMessage != null) {
            int len = Math.min(storedMessage.length, streamMessage.length);
            for (int j = 0; j < len; j++) {
                if (storedMessage[j] != streamMessage[j]) {
                    streamMessage = null;
                    break;
                }
            }
        }
        if (streamMessage != null) {
            if (continuous) {
                broadcastStream(type, startStream[type.ordinal()], message, true);
            }
            return ActionStatus.SUCCESS;
        }
        return ActionStatus.FAILURE;
    }
    
    @RobotAction
    public static ActionStatus broadcastMessage(MessageType type, int[] message, boolean continuous)
            throws GameActionException {
        if (!hasEnoughPower(GameConstants.BROADCAST_SEND_COST * STREAM_SIZE + GameConstants.BROADCAST_READ_COST
                * (STREAM_SIZE + (STREAM_SIZE - 2 - message.length)))) {
            return ActionStatus.RUNNING;
        }
        for (int i = 0; i < type.streamCount; i++) {
            ActionStatus status = broadcastMessageAttempt(type, message, continuous);
            if (status.failure()) {
                startStream[type.ordinal()]++;
                startStream[type.ordinal()] %= type.streamCount;
            } else {
                return status;
            }
            
        }
        System.out.println(type + " is blocked on all of its allocated streams");
        return ActionStatus.RUNNING;
    }
    
    private static int readBroadcastPermuted(int channel) throws GameActionException {
        return readBroadcast(permute(channel));
    }
    
    @RobotAction
    private static int[] readBroadcastStream(MessageType type, int stream) throws GameActionException {
        int streamBaseChannel = (type.getStreamBaseIndex() + stream) * STREAM_SIZE;
        int checksum = 0;
        int[] message = new int[STREAM_SIZE - 2];
        if (getTeamPower() < GameConstants.BROADCAST_READ_COST * STREAM_SIZE) {
            return null;
        }
        for (int i = 0; i < STREAM_SIZE; i++) {
            int d = decrypt(readBroadcastPermuted(streamBaseChannel + i));
            checksum ^= d;
            if (i == STREAM_SIZE - 1) {
                if (checksum != 0) {
                    return null;
                }
            } else {
                if (i == 0) {
                    if (d > Clock.getRoundNum() || d < Clock.getRoundNum() - 1) {
                        return null;
                    }
                } else {
                    message[i - 1] = d;
                }
            }
        }
        return message;
    }
    
    @RobotAction
    public static int[] readBroadcastMessage(MessageType type) throws GameActionException {
        for (int i = 0; i < type.streamCount; i++) {
            int[] message = readBroadcastStream(type, i); // TODO: start at startStream?
            if (message != null) {
                return message;
            }
        }
        return null;
    }
    
    @RobotAction
    public static int[][] readBroadcastMessages(MessageType type) throws GameActionException {
        int[][] messages = new int[type.streamCount][];
        for (int i = 0; i < type.streamCount; i++) {
            messages[i] = readBroadcastStream(type, i); // TODO: start at startStream?
        }
        return messages;
    }
}
