package d_bot.managers.InfoArray;

public enum CommandType {
    RALLY_POINT,  // Rally here
    ATTACK_POINT, // Approach point in offensive mode, no emphasis on pastrs
    ATTACK_PASTR, // Approach point with primary objective of destroying pastr
    DEFEND_PASTR, // Approach point with primary objective of defending pastr
    BUILD_PASTR,  // Build a pastr at the supplied location
    BUILD_NOISE_TOWER // Build a noise tower at this location
}