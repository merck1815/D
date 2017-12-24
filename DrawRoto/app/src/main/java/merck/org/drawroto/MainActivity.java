package merck.org.drawroto;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int PHOTOZOOM = 2;
    private static final int PHOTOGATE = 1;
    private final static int REQUESTCODE = 1; // 返回的结果码

    private ImageView imageShow;
    private String photoPath;
    //private NDKUtils ndk = new NDKUtils();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageShow = (ImageView)this.findViewById(R.id.preView);

        //检查权限
        checkPermission();

    }

    // Activity 返回的数据，该方法对应于 startActivityForResult(intent, 0)方法
    // requestCode：请求码
    // resultCode：返回数据时的处理结果
    // data：返回数据的Intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PHOTOZOOM) {
            if (data == null) {return;}
            photoPath = selectImage(getApplicationContext(), data);
            //mHead_imgv.setImageBitmap(nativeBitmap(picPath));
            //Log.i("photo path",photoPath);
            Intent intent = new Intent("merck.org.drawroto.ViewImgRevise");
            intent.putExtra("imgpath", photoPath); // 向子窗口传递参数
            //startActivity(intent);
            startActivityForResult(intent, 0);
        }else if(resultCode == PHOTOGATE){
            if(data == null){return;}
            // 接收图片 Bitmap

            //Bundle b= intent.getExtras();
            byte[] bytes=data.getByteArrayExtra("bitmap");
            Bitmap bmp= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            System.out.println("图片接收完成，大小："+String.valueOf(bytes.length));
            //imageProcess(bmp);
            //NDKUtils.getEdge(bmp);
            //System.out.println(stringFromJNI());
            stringFromJNI(bmp);
            imageShow.setImageBitmap(bmp);


        }

    }

    public static String selectImage(Context context, Intent data) {
        Uri selectedImage = data.getData();
        if (selectedImage != null) {
            String uriStr = selectedImage.toString();
            String path = uriStr.substring(10, uriStr.length());
            if (path.startsWith("com.sec.android.gallery3d")) {

                return null;
            }
        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    //打开选择图片
    public void openfileCheck(View v) {
        Log.i("open photo", "open photo");
        //打开本地相册

        Intent openAlbumIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        openAlbumIntent.setType("image/*");
        openAlbumIntent.putExtra("crop", true);
        openAlbumIntent.putExtra("return-data", true);
        startActivityForResult(openAlbumIntent, PHOTOZOOM);

    }

    //检查权限
    public void checkPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void stringFromJNI(Object bitmap);
}
