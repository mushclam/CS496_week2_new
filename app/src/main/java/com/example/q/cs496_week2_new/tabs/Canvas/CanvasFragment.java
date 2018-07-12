package com.example.q.cs496_week2_new.tabs.Canvas;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.q.cs496_week2_new.MainActivity;
import com.example.q.cs496_week2_new.R;
import com.example.q.cs496_week2_new.UserProfile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class CanvasFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private PaintView paintView;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public static final String OPEN_CANVAS_SERVER_URL = "http://52.231.66.99:8080";

    public static Socket mSocket;
    public boolean isConnected = false;

    public Gson gson = new Gson();

    public void networkInit(){
        try {
            mSocket = IO.socket(OPEN_CANVAS_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public CanvasFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CanvasFragment newInstance(String param1, String param2) {
        CanvasFragment fragment = new CanvasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkInit();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_canvas, container, false);



        mSocket.on("loadAction", onLoadAction);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.connect();
        isConnected = true;


        /* buttons */
        Button black_but = (Button) view.findViewById(R.id.button_black);
        Button red_but = (Button) view.findViewById(R.id.button_red);
        Button green_but = (Button) view.findViewById(R.id.button_green);
        Button blue_but = (Button) view.findViewById(R.id.button_blue);
        ImageButton eraser_but = (ImageButton) view.findViewById(R.id.eraser_activate);
        ImageButton pencil_but = (ImageButton) view.findViewById(R.id.pencil_activate);

        black_but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paintView.setColor(-16777216);
                networkInit();
            }
        });

        red_but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paintView.setColor(-65536);
            }
        });

        green_but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paintView.setColor(-16711936);
            }
        });

        blue_but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paintView.setColor(-16776961);
            }
        });

        eraser_but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paintView.setColor(-1);
            }
        });

        pencil_but.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "pencil activated", Toast.LENGTH_LONG).show(); // todo
            }
        });

        /* buttons end */

        paintView = (PaintView) view.findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mSocket.disconnect();
        mSocket.off("loadAction", onLoadAction);

    }
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("onConnect called", gson.toJson(args[0]));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray action_downs = new JSONArray();
                        MyMotionEvent action_moves = new MyMotionEvent(new JSONArray());
                        JSONArray action_ups = new JSONArray();
                        try {
                            action_downs = obj.getJSONArray("d");
                            action_moves = new MyMotionEvent(obj.getJSONArray("m"));
                            action_ups = obj.getJSONArray("u");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int i;
                        for (i = 0; i < action_downs.length(); ++i){
                            JSONObject action = new JSONObject();
                            try {
                                action = (JSONObject) action_downs.get(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            paintView.touchStart(BigDecimal.valueOf(action.optDouble("x")).floatValue(),
                                    BigDecimal.valueOf(action.optDouble("y")).floatValue(),
                                    action.optInt("id"));
                        }

                        paintView.touchMove(action_moves);

                        for (i = 0; i < action_ups.length(); ++i){
                            JSONObject action = new JSONObject();
                            try {
                                action = (JSONObject) action_ups.get(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            paintView.touchUp(action.optInt("id"));
                        }
                        paintView.invalidate();
                    }
                }
            });
        }
    };

    private Emitter.Listener onLoadAction = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("onGreeting called", gson.toJson(args[0]));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        JSONObject obj = (JSONObject) args[0];
                        JSONArray action_downs = new JSONArray();
                        MyMotionEvent action_moves = new MyMotionEvent(new JSONArray());
                        JSONArray action_ups = new JSONArray();
                        try {
                            action_downs = obj.getJSONArray("d");
                            action_moves = new MyMotionEvent(obj.getJSONArray("m"));
                            action_ups = obj.getJSONArray("u");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int i;
                        for (i = 0; i < action_downs.length(); ++i){
                            JSONObject action = new JSONObject();
                            try {
                                action = (JSONObject) action_downs.get(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            paintView.touchStart(BigDecimal.valueOf(action.optDouble("x")).floatValue(),
                                    BigDecimal.valueOf(action.optDouble("y")).floatValue(),
                                    action.optInt("id"));
                        }

                        paintView.touchMove(action_moves);

                        for (i = 0; i < action_ups.length(); ++i){
                            JSONObject action = new JSONObject();
                            try {
                                action = (JSONObject) action_ups.get(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            paintView.touchUp(action.optInt("id"));
                        }
                        paintView.invalidate();
                    }
                }
            });
        }
    };

    private Emitter.Listener onGreeting = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("onGreeting called", gson.toJson(args[0]));
        }
    };




}
