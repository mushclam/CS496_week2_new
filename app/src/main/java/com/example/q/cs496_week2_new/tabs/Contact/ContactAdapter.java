package com.example.q.cs496_week2_new.tabs.Contact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.q.cs496_week2_new.BitmapString;
import com.example.q.cs496_week2_new.R;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context mContext;
    private List<ContactItem> mContacts;
    private ContactFragment mFragment;

    public ContactAdapter(Context mContext, List<ContactItem> mContacts, ContactFragment mFragment) {
        this.mContext = mContext;
        this.mContacts = mContacts;
        this.mFragment = mFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mContacts != null) {
            final ContactItem mContact = mContacts.get(position);
            holder.image.setImageBitmap(BitmapString.base64ToBitmap(mContact.getProfile()));
            holder.name.setText(mContact.getNickname());
            holder.phoneNumber.setText(mContact.getPhoneNumber());
        }
    }

    @Override
    public int getItemCount() {
        return mContacts != null ? mContacts.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView image;
        public TextView name;
        public TextView phoneNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
        }

        @Override
        public void onClick(View view) {
        }
    }
}
