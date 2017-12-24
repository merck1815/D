package merck.org.drawroto;

/**
 * Created by merck on 2017/12/19.
 */

public class NDKUtils {

    public static native void getEdge(Object bitmap);

    //加载库 Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

}
