package com.example.q.cs496_week2_new.tabs.Contact;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.q.cs496_week2_new.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    public ContactFragment() {
        // Required empty public constructor
    }

    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
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
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        ContactClient client = ContactServiceGenerator.createService(ContactClient.class);
        Call<List<ContactItem>> call = client.getContactLIst();

        call.enqueue(new Callback<List<ContactItem>>() {
            @Override
            public void onResponse(Call<List<ContactItem>> call, Response<List<ContactItem>> response) {
                List<ContactItem> contactItemList = response.body();
                recyclerView.setAdapter(new ContactAdapter(getActivity(), contactItemList, ContactFragment.this));
            }

            @Override
            public void onFailure(Call<List<ContactItem>> call, Throwable t) {
                Toast.makeText(getActivity(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView_contact);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
