package ourBot_v1.managers.InfoArray;

public interface ArrayPackable {
    public static int packedSize = -1;
    
    public int[] toPacked();
    public void toUnpacked(int[] packed);
}
