package com.example.q.cs496_week2_new.tabs.Gallery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.q.cs496_week2_new.MainActivity;
import com.example.q.cs496_week2_new.R;
import com.example.q.cs496_week2_new.ServiceGenerator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class GalleryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ArrayList<GalleryItem> mSharedImages = new ArrayList<>();
    ArrayList<GalleryItem> mImages = new ArrayList<>();
    Hashtable<String, Integer> imageStatus = new Hashtable<>();

    TextView textShared;
    TextView textLocal;

    GalleryState galleryState;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    ViewPager sharedPager;

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

        sharedPager = view.findViewById(R.id.sharedPager);
        sharedPager.setAdapter(new SharedAdapter(getChildFragmentManager()));

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.sharedPager, new SharedFragment()).commit();

        checkState();

        textShared = (TextView) view.findViewById(R.id.title_shared);
        textLocal = (TextView) view.findViewById(R.id.title_local);

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
                adapter.notifyDataSetChanged();
                checkState();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    public static class SharedAdapter extends FragmentPagerAdapter {
        public SharedAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new SharedFragment();
        }

        @Override
        public int getCount() {
            return 1;
        }
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
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                Gson gson = new Gson();
                File file = new File(getActivity().getFilesDir() + "/gallery.json");
                if (file.exists()) {
                    StringBuilder data = new StringBuilder();
                    FileInputStream fis = getActivity().openFileInput("gallery.json");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String str = br.readLine();
                    while (str != null) {
                        data.append(str).append("\n");
                        str = br.readLine();
                    }

                    Log.d("PRE_RESULT", data.toString());
                    mImages = gson.fromJson(data.toString(), new TypeToken<ArrayList<GalleryItem>>(){}.getType());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            adapter = new GalleryAdapter(getActivity(), GalleryFragment.this, mImages);
            recyclerView.setAdapter(adapter);
        }

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
            Gson gson = new Gson();
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
                String json = gson.toJson(result);

                try {
                    FileOutputStream fos = getActivity().openFileOutput("gallery.json", Context.MODE_PRIVATE);
                    fos.write(json.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }
            return null;
        }
    }

    private void getState() {

        GalleryClient client = ServiceGenerator.createService(GalleryClient.class);
        Call<GalleryState> call = client.getStateList();

        call.enqueue(new Callback<GalleryState>() {
            @Override
            public void onResponse(Call<GalleryState> call, Response<GalleryState> response) {
                galleryState = response.body();
                if(galleryState != null){
                    String[] imageIds = galleryState.getImages();
                    String[] openIds = galleryState.getOpenImages();

                    if (imageIds.length != 0) {
                        for (String id : imageIds) {
                            if (imageStatus.get(id) == null) {
                                Log.e("STATUS", "NO EXIST IN LOCAL");
//                                getImage();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GalleryState> call, Throwable t) {
                Toast.makeText(getActivity(), "ERROR: Check State", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkState() {
        getState();
    }
}
