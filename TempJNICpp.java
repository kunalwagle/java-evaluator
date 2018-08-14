public class TempJNICpp {
static {
System.loadLibrary("jniexample");
}

private native int[] answer(int arg1, int arg2, int arg3);
public static void main(String[] args){
int[] a = new TempJNICpp().answer(10, 20, 90); 
 for(int i = 0; i < 3; i++) { System.out.println(a[i]); } 

a = new int[]{};
}
}

