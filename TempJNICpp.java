public class TempJNICpp {
static {
System.loadLibrary("tempjnicpp");
}

private native int[] shuffle(String arg1, int length1, String arg2, int length2, String arg3, int length3);
public static void main(String[] args){
int[] a = new TempJNICpp().shuffle("10", 2, "20", 2, "40", 2); 
 for(int i = 0; i < 3; i++) { System.out.println(a[i]); } 

a = new int[]{};
}
}

