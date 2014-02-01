package team169_baseline.managers.InfoArray;

public class PastrAlarm implements ArrayPackable {
    
    public int roundNum;
    public int status;
    
    public PastrAlarm() {
    }
    
    public PastrAlarm(int status, int roundNum) {
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
