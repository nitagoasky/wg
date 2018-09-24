package minimini.cc.wg;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import minimini.cc.wg.http.Answer;
import minimini.cc.wg.model.Result;


/**
 * Created by gaotian on 2018/9/23.
 */

public class MainService extends Service {

    //Log用的TAG
    private static final String TAG = "toucherlayout";

    //要引用的布局文件.
    ConstraintLayout toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;

    ImageButton imageButton1;

    Button submit;

    TextView textView;

    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    public static int mResultCode = 0;
    public static Intent mResultData = null;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    private SimpleDateFormat dateFormat = null;
    private String strDate = null;
    private String pathImage = null;
    private String nameImage = null;
    private String nameImage1 = null;

    private WindowManager mWindowManager1 = null;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private ImageReader mImageReader = null;
    private DisplayMetrics metrics = null;
    private int mScreenDensity = 0;

    private Aip aip = new Aip();

    private String model = android.os.Build.MODEL;

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            String txt = (String)msg.obj;
            textView.setText(txt);
            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        LLD-AL00
        System.out.println("111111111111111111111");
        System.out.println(model);

        Log.i(TAG,"MainService Created");
        //OnCreate中来生成悬浮窗.
        createToucher();

        createVirtualEnvironment();
    }

    private void createToucher()
    {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        if (Build.VERSION.SDK_INT >= 26) {//8.0新特性
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = 1080;
        params.height = 300;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.toucherlayout,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);

        textView = (TextView) toucherLayout.findViewById(R.id.text_id);
        submit = (Button) toucherLayout.findViewById(R.id.sub);

        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        imageButton1.setOnClickListener(new View.OnClickListener() {
            long[] hints = new long[2];
            @Override
            public void onClick(View v) {
                Log.i(TAG,"点击了");
                System.arraycopy(hints,1,hints,0,hints.length -1);
                hints[hints.length -1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - hints[0] >= 700)
                {
                    Log.i(TAG,"要执行");
                    Toast.makeText(MainService.this,"连续点击两次以退出",Toast.LENGTH_SHORT).show();
                }else
                {
                    Log.i(TAG,"即将关闭");
                    stopSelf();
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Message msg = new Message();
                msg.obj = "正在处理" ;
                mHandler.sendMessage(msg);

                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    public void run() {
                        startVirtual();
                    }
                }, 500);

                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        startCapture();
                    }
                }, 1000);
//                textView.setText("gdddddddddddddddddddddddddddt");
            }
        });
    }

    @Override
    public void onDestroy()
    {
        //用imageButton检查悬浮窗还在不在，这里可以不要。优化悬浮窗时要用到。
        if (imageButton1 != null)
        {
            windowManager.removeView(toucherLayout);
        }
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startVirtual(){
        if (mMediaProjection != null) {
            Log.i(TAG, "want to display virtual");
            virtualDisplay();
        } else {
            Log.i(TAG, "start screen capture intent");
            Log.i(TAG, "want to build mediaprojection and display virtual");
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpMediaProjection(){
        mResultData = ((ShotApplication)getApplication()).getIntent();
        mResultCode = ((ShotApplication)getApplication()).getResult();
        mMediaProjectionManager1 = ((ShotApplication)getApplication()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay(){
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
        Log.i(TAG, "virtual displayed");
    }

    private void createVirtualEnvironment(){
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        strDate = dateFormat.format(new java.util.Date());
        pathImage = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";
        nameImage = pathImage+strDate+".png";
        mMediaProjectionManager1 = (MediaProjectionManager)getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mWindowManager1 = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowWidth = mWindowManager1.getDefaultDisplay().getWidth();
        windowHeight = mWindowManager1.getDefaultDisplay().getHeight();
        metrics = new DisplayMetrics();
        mWindowManager1.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565

        Log.i(TAG, "prepared the virtual environment");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startCapture(){
        strDate = dateFormat.format(new java.util.Date());
        nameImage = pathImage+"screen"+".png";
        nameImage1 = pathImage+"screen1"+".png";

        Image image = mImageReader.acquireLatestImage();
        System.out.println(image);
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
        image.close();
        Log.i(TAG, "image data captured");

        if(bitmap != null) {
            try{
                File fileImage = new File(nameImage);
                File fileImage1 = new File(nameImage1);
                Log.i("GT", nameImage);
                if(!fileImage.exists()){
                    fileImage.createNewFile();
                    Log.i(TAG, "image file created");
                }
                if(!fileImage1.exists()){
                    fileImage1.createNewFile();
                    Log.i(TAG, "image1 file created");
                }
                FileOutputStream out = new FileOutputStream(fileImage);
                FileOutputStream out1 = new FileOutputStream(fileImage1);
                if(out != null){
                    Bitmap bitmap1 = null;
                    Bitmap bitmap2 = null;
                    switch (model){
                        case "LLD-AL00"://高天
                            bitmap1 = Bitmap.createBitmap(bitmap, 130, 550, 830, 150);
                            bitmap2 = Bitmap.createBitmap(bitmap, 130, 670, 830, 200);
                            break;
                        case "BLN-AL20"://妈妈
                            bitmap1 = Bitmap.createBitmap(bitmap, 130, 550, 830, 150);
                            bitmap2 = Bitmap.createBitmap(bitmap, 130, 670, 830, 200);
                            break;
                        default:
                            bitmap1 = Bitmap.createBitmap(bitmap, 130, 600, 830, 150);
                            bitmap2 = Bitmap.createBitmap(bitmap, 130, 720, 830, 200);
                    }
                    bitmap1.compress(Bitmap.CompressFormat.PNG, 100, out);
                    bitmap2.compress(Bitmap.CompressFormat.PNG, 100, out1);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(fileImage);
                    media.setData(contentUri);
                    this.sendBroadcast(media);
                    Log.i(TAG, "screen image saved");

                    new Thread(){
                        @Override
                        public void run(){
                            // 传入可选参数调用接口
                            HashMap<String, String> options = new HashMap<String, String>();
                            options.put("language_type", "CHN_ENG");
                            options.put("detect_direction", "true");
                            options.put("detect_language", "true");
                            options.put("probability", "true");
                            // 通用文字识别, 图片参数为远程url图片
                            JSONObject res = aip.getClient().basicGeneral(nameImage, options);
                            JSONObject res1 = aip.getClient().basicGeneral(nameImage1, options);
                            try {
                                String jsonString = res.get("words_result").toString();
                                List<Result> results = JSON.parseArray(jsonString, Result.class);
                                if(results.size() == 0) return;
                                String text = "";
                                for (Result result: results){
                                    text += result.getWords();
                                }

                                text = text.replaceAll("[(0-9)]", "");
                                text = text.replaceAll("\\.", "");
                                System.out.println(text);
                                Message msg = new Message();
                                msg.obj =text ;
                                mHandler.sendMessage(msg);

                                String jsonString1 = res1.get("words_result").toString();
                                System.out.println(jsonString1);
                                List<Result> results1 = JSON.parseArray(jsonString1, Result.class);
                                String text1 = "";
                                if(null != results1 && results1.size() != 0){
                                    text1 = results1.get(0).getWords();
                                }

                                String key = handleStr(text);
                                List<String> answer = Answer.get(key);
                                if(null == answer || answer.size() == 0){
                                    Log.i(TAG, "网页查不到");
                                    return;
                                }

                                if(!"".equals(text1)){
                                    Iterator<String> iterator = answer.iterator();
                                    while(iterator.hasNext()){
                                        String a = iterator.next();
                                        if(!Pattern.matches(".*" + text1 + ".*", a)){
                                            iterator.remove();
                                        }
                                    }
                                }

                                String cy = listToString(answer);
                                Message msg1 = new Message();
                                msg1.obj = cy;
                                mHandler.sendMessage(msg1);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }}.start();

                }
            }catch(FileNotFoundException e) {
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public String handleStr(String s){
        try {
            byte[] b=s.getBytes("GB2312");
            String S = bytesToHexFun1(b);
            StringBuffer s1 = new StringBuffer(S);

            int index;

            for(index=2;index<s1.length();index+=3){

                s1.insert(index - 2, '%');

            }
            s1.insert(s1.length() - 2, '%');
            return new String(s1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    //将byte数组转成16进制字符串
    public static String bytesToHexFun1(byte[] bytes) {
        char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        for(byte b : bytes) { // 使用除与取余进行转换
            if(b < 0) {
                a = 256 + b;
            } else {
                a = b;
            }
            buf[index++] = HEX_CHAR[a / 16];
            buf[index++] = HEX_CHAR[a % 16];
        }
        return new String(buf);
    }

    public String listToString(List list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }
}
