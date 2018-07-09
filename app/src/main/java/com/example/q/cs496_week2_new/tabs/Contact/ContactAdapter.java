package com.example.q.cs496_week2_new.tabs.Contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context mContext;
    private List<ContactItem> mContact;
    private ContactFragment mFragment;

    public ContactAdapter(Context mContext, List<ContactItem> mContact, ContactFragment mFragment) {
        this.mContext = mContext;
        this.mContact = mContact;
        this.mFragment = mFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


        }

        @Override
        public void onClick(View view) {

        }
    }
}
