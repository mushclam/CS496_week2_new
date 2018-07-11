package com.example.q.cs496_week2_new.tabs.Canvas;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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

public class CanvasFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private PaintView paintView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        ((MainActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_canvas, container, false);

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

}
