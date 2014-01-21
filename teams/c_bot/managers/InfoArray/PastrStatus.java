package c_bot.managers.InfoArray;

public enum PastrStatus {
    IN_CONSTRUCTION,
    NEEDS_TOWER, // Approach point in offensive mode, no emphasis on pastrs
    ALL_GOOD, // Approach point with primary objective of destroying pastr
    UNDER_ATTACK
}