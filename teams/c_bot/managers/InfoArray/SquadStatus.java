package c_bot.managers.InfoArray;

public class SquadStatus implements ArrayPackable {
    public int status;
    public int roundNum;
    static int packedSize = 2;
    
    public SquadStatus() {
    }
    
    public SquadStatus(int status, int roundNum) {
        this.roundNum = roundNum;
        this.status = status;
    }

    @Override
    public int[] toPacked() {
        int[] info = {this.roundNum, this.status};
        return info;
    }

    @Override
    public void toUnpacked(int[] packed) {
        this.roundNum = packed[0];
        this.status = packed[1];
    }
    
    public String toString() {
        return "RoundNum: " + roundNum + " Status: " + status;
    }
}
