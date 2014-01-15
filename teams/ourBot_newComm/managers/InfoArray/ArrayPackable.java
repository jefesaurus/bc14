package ourBot_newComm.managers.InfoArray;

public interface ArrayPackable {
    public static int packedSize = -1;
    
    public int[] toPacked();
    public void toUnpacked(int[] packed);
}
