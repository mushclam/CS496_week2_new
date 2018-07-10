package com.example.q.cs496_week2_new.tabs.Gallery;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    ArrayList<SharedItem> mSharedImages = new ArrayList<>();
    ArrayList<GalleryItem> mImages = new ArrayList<>();
    Hashtable<String, Integer> imageStatus = new Hashtable<>();

    TextView textShared;
    TextView textLocal;

    GalleryState galleryState;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    RecyclerView sharedView;
    RecyclerView.Adapter sharedAdapter;
    RecyclerView.LayoutManager sharedManager;

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

        textShared = (TextView) view.findViewById(R.id.title_shared);
        textLocal = (TextView) view.findViewById(R.id.title_local);

        recyclerView = view.findViewById(R.id.recyclerView_gallery);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(0);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        sharedView = view.findViewById(R.id.recyclerView_shared);
        sharedView.setHasFixedSize(true);

        sharedManager = new GridLayoutManager(getActivity(), 3);
        sharedView.setLayoutManager(sharedManager);
        sharedView.scrollToPosition(0);
        sharedView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.galleryRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetGalleryTask().execute();
                new GetSharedTask().execute();
                adapter.notifyDataSetChanged();
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
            new GetSharedTask().execute();
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

    public class GetSharedTask extends AsyncTask<String, String, ArrayList<SharedItem>> {

        public ArrayList<SharedItem> sharedItemList = new ArrayList<>();

        public GetSharedTask() { }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                Gson gson = new Gson();
                File file = new File(getActivity().getFilesDir() + "/shared.json");
                if (file.exists()) {
                    StringBuilder data = new StringBuilder();
                    FileInputStream fis = getActivity().openFileInput("shared.json");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String str = br.readLine();
                    while (str != null) {
                        data.append(str).append("\n");
                        str = br.readLine();
                    }

                    Log.e("PRE_RESULT", data.toString());
                    mSharedImages = gson.fromJson(data.toString(), new TypeToken<ArrayList<SharedItem>>(){}.getType());
                    for (SharedItem sharedItem : mSharedImages) {
                        imageStatus.put(sharedItem.get_id(), 1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            sharedAdapter = new SharedAdapter(getActivity(), GalleryFragment.this, mSharedImages);
            sharedView.setAdapter(sharedAdapter);
        }

        @Override
        protected ArrayList<SharedItem> doInBackground(String... strings) {
            Gson gson = new Gson();
            try {
                File galleryCache = new File(getActivity().getFilesDir() + "/shared.json");
                if (!galleryCache.exists()) {
                    Log.e("GET_SHARED_TASK", "IN IF");
                    sharedItemList = getSharedList();
                } else {
                    List<String> requestIds = getState();
                    List<SharedItem> updateList = getUpdate(requestIds);
                    sharedItemList.addAll(updateList);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<SharedItem> sharedItems) {
            super.onPostExecute(sharedItems);
            Gson gson = new Gson();
            mSharedImages.addAll(sharedItemList);
            try {
                String json = gson.toJson(mSharedImages);
                Log.e("Json", json);

                FileOutputStream fos = getActivity().openFileOutput("shared.json", Context.MODE_PRIVATE);
                fos.write(json.getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sharedAdapter = new SharedAdapter(getActivity(), GalleryFragment.this, mSharedImages);
            sharedView.setAdapter(sharedAdapter);
        }
    }

    private List<String> getState() {

        List<String> requestIds = new ArrayList<>();

        GalleryClient client = ServiceGenerator.createService(GalleryClient.class);
        Call<GalleryState> call = client.getStateList();

        Gson gson = new Gson();
        try {
            galleryState = call.execute().body();

            if(galleryState != null){
                String[] imageIds = galleryState.getImages();
                String[] openIds = galleryState.getOpenImages();

                if (imageIds.length != 0) {
                    for (String id : imageIds) {
                        if (imageStatus.get(id) == null) {
                            requestIds.add(id);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = gson.toJson(requestIds);
        Log.e("STATE", json);

        return requestIds;
    }

    private ArrayList<SharedItem> getUpdate(List<String> state) {
        ArrayList<SharedItem> sharedItemList = new ArrayList<>();
        Gson gson = new Gson();

        GalleryClient client = ServiceGenerator.createService(GalleryClient.class);
        Call<List<BaseItem>> call = client.getUpdateList(state);


        try {

            List<BaseItem> updateList = call.execute().body();
            if (!updateList.isEmpty()) {
                for (BaseItem item : updateList) {
                    String _id = item.get_id();
                    String encodedBase = item.getBase64();
                    byte[] decodedBase = Base64.decode(encodedBase, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBase, 0, decodedBase.length);

                    String path = saveCacheImage(_id, decodedBitmap);

                    SharedItem sharedItem = new SharedItem(_id, path);
                    sharedItemList.add(sharedItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json = gson.toJson(sharedItemList);
        Log.e("UPDATE", json);
        return sharedItemList;
    }

    private ArrayList<SharedItem> getSharedList() {

        Hashtable<String, Integer> imageState = new Hashtable<>();
        ArrayList<SharedItem> sharedItemList = new ArrayList<>();

        GalleryClient client = ServiceGenerator.createService(GalleryClient.class);
        Call<List<BaseItem>> call = client.getSharedList();

        try {
            List<BaseItem> baseItemList = call.execute().body();

            for (BaseItem item : baseItemList) {
                Gson gson = new Gson();
                String _id = item.get_id();
                String encodedBase = item.getBase64();
                byte[] decodedBase = Base64.decode(encodedBase, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBase, 0, decodedBase.length);

                String path = saveCacheImage(_id, decodedBitmap);

                SharedItem sharedItem = new SharedItem(_id, path);
                sharedItemList.add(sharedItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sharedItemList;
    }

    public String saveCacheImage(String _id, Bitmap bitmap) {
        String imagePath = null;
        try {
            String filename = _id + ".png";
            Log.e("CACHE_NAME", filename);
            FileOutputStream fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            imagePath = getActivity().getFileStreamPath(filename).getPath();
            Log.e("CACHE_PATH", imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return imagePath;
        }
    }
}
