package team169_baseline.managers.InfoArray;

public interface ArrayPackable {
    public static int packedSize = -1;
    
    public int[] toPacked();
    public void toUnpacked(int[] packed);
}
