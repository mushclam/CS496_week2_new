package com.example.q.cs496_week2_new.tabs.Gallery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.q.cs496_week2_new.R;

import java.util.ArrayList;
import java.util.Collections;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class GalleryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ArrayList<GalleryItem> mSharedImages = new ArrayList<>();
    ArrayList<GalleryItem> mImages = new ArrayList<>();

    TextView textShared;
    TextView textLocal;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    SwipeRefreshLayout swipeRefreshLayout;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance(String param1, String param2) {
        GalleryFragment fragment = new GalleryFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        GalleryClient client = GalleryServiceGenerator.createService(GalleryClient.class);
        Call<List<>> call = client.getGalleryList();

        textShared = (TextView) view.findViewById(R.id.title_shared);
        textLocal = (TextView) view.findViewById(R.id.title_local);
        if (mSharedImages.isEmpty()) {
            textShared.setVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.recyclerView_gallery);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(0);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.galleryRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetGalleryTask().execute();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermission()) {
            new GetGalleryTask().execute();
        }
    }

    private boolean checkPermission() {
        int resultW = ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE);
        return resultW == PackageManager.PERMISSION_GRANTED;
    }

    public class GetGalleryTask extends AsyncTask<String, String, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(String... strings) {
            return fetchAllImages();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            super.onPostExecute(galleryItems);
            mImages = galleryItems;
            adapter = new GalleryAdapter(getActivity(), GalleryFragment.this, mImages);
            recyclerView.setAdapter(adapter);
        }

        private ArrayList<GalleryItem> fetchAllImages() {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.LATITUDE};

            Cursor imageCursor = getActivity().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection,
                    null,       // 모든 개체 출력
                    null,
                    null);      // 정렬 안 함

            if(imageCursor != null) {
                ArrayList<GalleryItem> result = new ArrayList<>(imageCursor.getCount());

                if (imageCursor.moveToFirst()) {
                    do {
                        String filePath = imageCursor.getString(imageCursor.getColumnIndex(projection[0]));
                        long taken = imageCursor.getLong(imageCursor.getColumnIndex(projection[1]));
                        float longitude = imageCursor.getFloat(imageCursor.getColumnIndex(projection[2]));
                        float latitude = imageCursor.getFloat(imageCursor.getColumnIndex(projection[3]));

//                if(new File(filePath).exists())
                        result.add(new GalleryItem(filePath, taken, longitude, latitude));
                    } while (imageCursor.moveToNext());
                } else {
                    // imageCursor가 비었습니다.
                    Log.e("Fetch All Images", "ImageCursor is empty");
                }
                imageCursor.close();

                Collections.sort(result, Collections.reverseOrder());
                return result;
            }
            return null;
        }
    }

}
