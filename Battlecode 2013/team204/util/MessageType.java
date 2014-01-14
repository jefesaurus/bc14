package team204.util;

public enum MessageType {
    ENCAMPMENT_TO_CAPTURE(false), NEXT_SUPPORT_ENCAMPMENT_TYPE(true), ENEMY_NUKE_HALF_DONE(true);
    
    public final int streamCount;
    public final boolean overwrite;
    private int streamBaseIndex;
    
    public static final int streamCountTotal;
    static {
        int count = 0;
        for (MessageType type : MessageType.values()) {
            type.streamBaseIndex = count;
            count += type.streamCount;
        }
        streamCountTotal = count;
    }
    
    private static final int DEFAULT_STREAMS_PER_TYPE = 4;
    
    private MessageType(int streamCount, boolean overwrite) {
        this.streamCount = streamCount;
        this.overwrite = overwrite;
    }
    
    private MessageType(boolean overwrite) {
        this.streamCount = DEFAULT_STREAMS_PER_TYPE;
        this.overwrite = overwrite;
    }
    
    public int getStreamBaseIndex() {
        return this.streamBaseIndex;
    }
}
