package com.example.q.cs496_week2_new.tabs.Canvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.example.q.cs496_week2_new.UserProfile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class PaintView extends View {

    public static int BRUSH_SIZE = 20;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private SparseArray<Pair<FingerPath, ArrayList<Pair<Float, Float>>>> multi_paths = new SparseArray<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    // String : token (dangerous) or phoneNumber (maybe unique)
    //public HashMap<String, ArrayList<FingerPath>> paths_map = new HashMap<>();

    public Handler mHandler;
    public Socket socket;
    public BufferedReader networkReader;
    public BufferedWriter networkWriter;

    public String ip = "52.231.66.99";
    public int port = 8888;

    public Gson gson = new Gson();

    public void setSocket(String ip, int port) throws IOException {

        try {
            socket = new Socket(ip, port);
            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            networkReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public void networkInit() {
        mHandler = new Handler();

        try {
            setSocket(ip, port);
        } catch (IOException e){
            e.printStackTrace();
        }

        // Send header info
        PrintWriter out = new PrintWriter(networkWriter, true);
        JsonObject header = new JsonObject();
        header.addProperty("token", UserProfile.id);
        // TODO: change image_id
        header.addProperty("image_id", "temp");

        String msg = gson.toJson(header);
        out.print(msg);

        checkUpdate.start();
    }

    public void sendDrawing () {
        PrintWriter out = new PrintWriter(networkWriter, true);
        String msg = "Asdf";
        out.print(msg);
    }
    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String json_str;
                Log.w("checkUpdate", "Open Canvas started");
                while (true) {
                    Log.w("checkUpdate-loop", "Receiving other's drawing...");
                    json_str = networkReader.readLine();
                    JSONObject obj = new JSONObject(json_str);

                    JSONArray action_downs = obj.getJSONArray("d");
                    MyMotionEvent action_moves = new MyMotionEvent(obj.getJSONArray("m"));
                    JSONArray action_ups = obj.getJSONArray("u");

                    int i;
                    for (i = 0; i < action_downs.length(); ++i){
                        JSONObject action = (JSONObject) action_downs.get(i);
                        touchStart(BigDecimal.valueOf(action.optDouble("x")).floatValue(),
                                BigDecimal.valueOf(action.optDouble("y")).floatValue(),
                                action.optInt("id"));
                    }

                    touchMove(action_moves);

                    for (i = 0; i < action_ups.length(); ++i){
                        JSONObject action = (JSONObject) action_ups.get(i);
                        touchUp(action.optInt("id"));
                    }
                    //mHandler.post(showUpdate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable showUpdate = new Runnable() {

        public void run() {
            //Toast.makeText(NewClient.this, "Coming word: " + html, Toast.LENGTH_SHORT).show();
        }

    };

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[] {1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }

    public void init(DisplayMetrics metrics) {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;

        networkInit();
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    public void emboss() {
        emboss = true;
        blur = false;
    }

    public void blur() {
        emboss = false;
        blur = true;
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        normal();
        invalidate();
    }

    protected Bitmap getmBitmap()
    {
        return mBitmap;
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.save(); // 이 순간의 canvas 상태를 기록
        //mCanvas.drawColor(backgroundColor);

        /*for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(fp.path, mPaint);

        }*/
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        /*
        if (paths.size() > 0)
        {
            FingerPath fp = paths.get(paths.size() - 1);
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            mCanvas.drawPath(fp.path, mPaint);
        }
        */

        // simul-draw version
        for (int i=0; i<multi_paths.size(); i++) {
            FingerPath fp = multi_paths.valueAt(i).first;
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);

            canvas.drawPath(fp.path, mPaint);
        }

//        canvas.restore(); // canvas를 가장 최근의 canvas.save 시점으로 복구 (그 사이의 변화를 삭제)
    }

    /*private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);

        Gson gson = new Gson();
        String temp = gson.toJson(mPath);
        Log.d("Path json test", "temp : "+temp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }*/
    // id : user 식별자
    public void touchStart(float x, float y, int id) {
        Path path = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, path);
        ArrayList<Pair<Float, Float>> xys = new ArrayList<>();
        xys.add(new Pair(x,y));

        multi_paths.put(id, new Pair(fp, xys));
        path.reset();
        path.moveTo(x,y);
    }

    public void remote_touchStart(float x, float y) {

    }

    /*private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }*/
    public void touchMove(MyMotionEvent event) {
        Path path;
        FingerPath fp;
        ArrayList<Pair<Float, Float>> xys;
        float x, y, mX, mY, dx, dy;
        for (int i=0; i<event.getPointerCount(); i++) {
            int id = event.getPointerId(i);
            if (multi_paths.get(id) != null) {
                x = event.getX(i);
                y = event.getY(i);

                fp = multi_paths.get(id).first;
                path = fp.path;
                xys = multi_paths.get(id).second;
                assert xys.size() > 0;
                mX = xys.get(xys.size() - 1).first;
                mY = xys.get(xys.size() - 1).second;
                dx = Math.abs(x - mX);
                dy = Math.abs(y - mY);

                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    xys.add(new Pair(x, y));
                }
            }
        }
    }

    /*private void touchUp() {
        mPath.lineTo(mX, mY);
    }*/

    public void touchUp(int id){
        FingerPath fp;
        ArrayList<Pair<Float, Float>> xys;
        float mX, mY;
        if (multi_paths.get(id) != null) {
            fp = multi_paths.get(id).first;
            xys = multi_paths.get(id).second;
            mX = xys.get(xys.size() - 1).first;
            mY = xys.get(xys.size() - 1).second;
            fp.path.lineTo(mX, mY);

            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);

            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);
            mCanvas.drawPath(fp.path, mPaint);
            multi_paths.remove(id);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        JsonObject action;
        PrintWriter out;
        String msg;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                //touchStart(x, y);
                action = new JsonObject();
                action.addProperty("type", 0);
                action.addProperty("x", x);
                action.addProperty("y", y);

                out = new PrintWriter(networkWriter, true);
                msg = gson.toJson(action);
                out.print(msg);
                break;
            case MotionEvent.ACTION_MOVE :
                //touchMove(x, y);
                action = new JsonObject();
                action.addProperty("type", 1);
                action.addProperty("x", x);
                action.addProperty("y", y);

                out = new PrintWriter(networkWriter, true);
                msg = gson.toJson(action);
                out.print(msg);
                break;
            case MotionEvent.ACTION_UP :
                //touchUp();
                action = new JsonObject();
                action.addProperty("type", 2);
                
                out = new PrintWriter(networkWriter, true);
                msg = gson.toJson(action);
                out.print(msg);
                break;
        }
        invalidate();
        return true;
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int id = event.getPointerId(index);

        Path path;
        FingerPath fp;
        ArrayList<Pair<Float, Float>> xys;
        float x, y, mX, mY, dx, dy;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("draw test", "action down");
                //touchStart(event.getX(index), event.getY(index), id);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d("draw test", "action move");
                //touchMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("draw test", "action up");
                //touchUp(id);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }*/

    public void setColor(int color) {
        currentColor = color;
    }
}