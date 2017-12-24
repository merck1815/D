package merck.org.drawroto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import android.view.View.OnTouchListener;
import android.graphics.Matrix;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class ViewImgRevise extends AppCompatActivity implements OnTouchListener{

    private int detail = 1;
    private int smooth = 1;
    private int lastX=0;    //拖动事件 x y坐标
    private int lastY=0;

    private int rim_A_X = 0;  // 画板边框坐标
    private int rim_A_Y = 0;
    private int rim_B_X = 0;
    private int rim_B_Y = 0;
    private int distance_Top = 0;     // 画板与图片的距离 (px)
    private int distance_Bottom = 0;
    private int distance_Left = 0;
    private int distance_Right = 0;
    private ScreenInfo screenInfo ;
    private ImageView imageView ;
    private Bitmap fitBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();                  // 隐藏ActionBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//remove notification bar  即全屏

        setContentView(R.layout.view_img_revise);

        //接受传递来的数据
        Intent intent = getIntent();
        String imgpath = intent.getStringExtra("imgpath");
        imageView = (ImageView)this.findViewById(R.id.imageView);

        imageView.setOnTouchListener((OnTouchListener) this);

        //屏幕信息
        screenInfo = new ScreenInfo(this);
        fitBitmap = drawRim(photoZoom(imgpath));
        setSketchpadRim(fitBitmap);  // 设置画板边框
        imageView.setImageBitmap(drawRim(fitBitmap));  // 显示带有画板边框的图片

        Button returnButt = (Button)findViewById(R.id.returnButt);  //返回按钮
        Button resetButt = (Button)findViewById(R.id.resetButt);    //重置按钮
        Button completeButt = (Button)findViewById(R.id.completeButt);  //完成按钮

    }

    // 加载本地图片
    public static  Bitmap getLoacalBitmap(String path){
        try {
            FileInputStream fis = new FileInputStream(path);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void returnClick(View v){ // 返回按钮
        this.finish();//销毁当前活动
    }
    public void resetClick(View v){ // 重置按钮

    }
    public void completeClick(View v){ // 完成按钮
        // 构建返还数据
        Intent intent = new Intent();
        // 裁剪画板图片
        Bitmap drawImage = Bitmap.createBitmap(fitBitmap, rim_A_X, rim_A_Y, rim_B_X-rim_A_X, rim_B_Y-rim_A_Y, null, false);

        // 传递Bitmap对象,bitmap默认实现Parcelable接口,直接传递即可,但如果图片过大易引起程序崩溃
        // 所以使用字节流来传递
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //int src_len = drawImage.getByteCount(); // 裁剪后的图片大小
        drawImage.compress(Bitmap.CompressFormat.JPEG,90,baos);
        byte[] image_Byte = baos.toByteArray();
        int baos_len = image_Byte.length;

        System.out.println("图片字节大小:"+String.valueOf(baos_len/1024)+"KB");

        //Bundle bundle = new Bundle();
        //bundle.putByteArray("bitmap", image_Byte);
        intent.putExtra("bitmap",image_Byte);
        this.setResult(1,intent);
        this.finish(); // 销毁当前View
    }

    // 将图片缩放到屏幕大小
    public Bitmap photoZoom(String photoPath){
        ScreenInfo screenInfo = new ScreenInfo(this);
        int screen_width_px = screenInfo.width_pix;  // 屏幕宽像素
        int screen_high_px  = screenInfo.height_pix; // 屏幕高像素

        // 预加载图片以计算图片宽与高度
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath,options);
        // 原图大小（像素）
        final int src_width_px  = options.outWidth;
        final int src_height_px =options.outHeight;


        //大图片缩小处理
        int inSampleSize = 1;
        if ((src_width_px/inSampleSize) > screen_width_px && (src_height_px/inSampleSize) > screen_high_px){
            inSampleSize *= 2;
        }
        inSampleSize = inSampleSize/2;
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

        int src_w_px = bitmap.getWidth();
        int src_h_px = bitmap.getHeight();

        float zoom_factor = 1.00f; //缩放因子
        if(src_h_px > 0 && src_w_px > 0){
            float zoom_w = (float)screen_width_px/(float)src_w_px;
            float zoom_h = (float)screen_high_px/(float)src_h_px;
            //如果需要放大就取最小的
            if(zoom_w > 1 || zoom_h > 1){
                zoom_factor = zoom_w > zoom_h ? zoom_h : zoom_w;
            }
            //如果是缩小就取最小的
            if(zoom_w < 1 || zoom_h < 1){
                zoom_factor = zoom_w < zoom_h ? zoom_w : zoom_h;
            }
        }
        Log.i("-------zoom_factor", String.valueOf(zoom_factor));
        //图片缩放
        Matrix matrix = new Matrix();
        matrix.postScale(zoom_factor,zoom_factor);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,src_w_px,src_h_px,matrix,true);
        bitmap.recycle(); // 释放资源

        return resizeBmp;
    }

    // 设置纸张(画板)边框
    public void setSketchpadRim(Bitmap bitmap){
        //获取画板物理大小
        float w_mm = ((Configuration.PAPER_WIDTH_MAXI - Configuration.MARGIN * 2)/25.4000000f) ;
        float h_mm = (Configuration.PAPER_HIGH_MAXI - Configuration.MARGIN * 2)/25.4000000f;
        //转换为像素
        int draw_width_px = (int)(w_mm * screenInfo.x_DPI);
        int draw_heig_px = (int)(h_mm * screenInfo.y_DPI);
        int src_w_px = bitmap.getWidth();
        int src_h_px = bitmap.getHeight();
        // 换算成可绘制尺寸
        // 宽高比例：
        float drw_proportion = (float)draw_width_px/(float)draw_heig_px; // 画纸正比 0.714
        float photo_proportion = (float)src_w_px/(float)src_h_px; //照片正比
        int drw_w = 0;
        int drw_h = 0;
        if(drw_proportion > photo_proportion){
            // 取照片的宽度为调整基准
            drw_w = src_w_px;
            drw_h = (int)((float)drw_w/drw_proportion);
        }else {
            // 取照片的高度为调整基准
            drw_h = src_h_px;
            drw_w = (int)((float)src_h_px * drw_proportion);
        }

        rim_A_X = (src_w_px - drw_w)/2;
        rim_A_Y = (src_h_px - drw_h)/2;
        rim_B_X = rim_A_X + drw_w;
        rim_B_Y = rim_A_Y + drw_h;

        //bitmap.recycle();
    }

    // 显示带画板的图片
    public void showImage(Bitmap bitmap){
        if(fitBitmap != null){
            this.imageView.setImageBitmap(bitmap);
        }
    }

    // 在图片上绘制 纸张边框（A4）
    public Bitmap drawRim(Bitmap bitmap){

        Bitmap bitmapDuplicate = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmapDuplicate);
        //图像上画矩形
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(10);  //线的宽度
        canvas.drawRect(rim_A_X, rim_A_Y, rim_B_X, rim_B_Y, paint);

        return bitmapDuplicate;
    }

    // 绘制选中框拖动
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:  // 单指事件
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE: // 单指滑动事件
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;

                System.out.println("dx:" + dx);
                System.out.println("dy:" + dy);

                // 设置可拖动距离
                if (dx != 0) { //左右移动
                    if(dx + this.rim_A_X >=0 && dx + this.rim_B_X <= fitBitmap.getWidth()){
                        this.rim_A_X = this.rim_A_X + dx;
                        this.rim_B_X = this.rim_B_X + dx;

                    }else{

                    }
                }

                if (dy != 0) {
                    if(dy + this.rim_A_Y >=0 && dy + this.rim_B_Y <= fitBitmap.getHeight()){
                        this.rim_A_Y = this.rim_A_Y + dy;
                        this.rim_B_Y = this.rim_B_Y + dy;
                    }else{

                    }
                }

                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();

                break;
            case MotionEvent.ACTION_UP:
                //Toast.makeText(FingerActivity.this, "Up...", Toast.LENGTH_SHORT).show();
                break;
        }
        //重新绘制图片画板
        showImage(drawRim(fitBitmap));
        return true;
    }
}





