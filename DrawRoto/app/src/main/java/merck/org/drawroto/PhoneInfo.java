package merck.org.drawroto;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by merck on 2017/12/19.
 */

public class PhoneInfo {
}

class ScreenInfo{
    /*
      获取屏幕高度的像素数量！
      注意 - 因为这里会自动减去32dp的像素数量，根据分辨率不同的设备，减去的像素数量也不同，但是可以根据公式推算完整（px = dp x 基准比例）。
      为啥不用dm.densityDpi / 160 得到基准比例？
      因为那个会随着build.prop文件代码变更而更改，算出来的不一定准确
      dm.heightPixels + 32 * dm.ydpi / 160;
    */

    public int width_pix = 0;     // 屏幕宽度（像素）
    public int height_pix = 0;    // 屏幕高度（像素）
    public float density = 0.00f; // 屏幕密度（0.75 / 1.0 / 1.5）
    public int densityDpi = 0;    // 屏幕密度dpi（120 / 160 / 240）获取系统dpi，随着 build.prop 文件中的代码而改变。
    public float x_DPI = 0.0000f;         // 得到物理屏幕上 X 轴方向每英寸的像素
    public float y_DPI = 0.0000f;         // 得到物理屏幕上 Y 轴方向每英寸的像素


    // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
    public int screenWidth = 0;  // 屏幕宽度(dp)
    public int screenHeight = 0; // 屏幕高度(dp)

    public ScreenInfo(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        this.width_pix = dm.widthPixels;      // 屏幕宽度（像素）
        this.height_pix = dm.heightPixels;    // 屏幕高度（像素）
        this.density = dm.density;           // 屏幕密度（0.75 / 1.0 / 1.5）当前设备的基准比例
        this.densityDpi = dm.densityDpi;     // 屏幕像素密度密度dpi（120 / 160 / 240）

        this.x_DPI = dm.xdpi;
        this.y_DPI = dm.ydpi;

        this.screenWidth = (int) (width_pix / density);  // 屏幕宽度(dp)
        this.screenHeight = (int) (height_pix / density);// 屏幕高度(dp)
    }

}
