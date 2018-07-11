package com.example.q.cs496_week2_new.tabs.Contact;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.example.q.cs496_week2_new.BitmapString;
import com.example.q.cs496_week2_new.R;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context mContext;
    private List<ContactItem> mContacts;
    private ArrayList<ProfilePath> ProfilePathList;
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
        ProfilePathList = new ArrayList<>();
        for (ContactItem contactItem : mContacts) {
            Bitmap decodedBitmap = BitmapString.base64ToBitmap(contactItem.getProfile());
            String profilePath = saveProfileImage(contactItem.get_id(), decodedBitmap);
            ProfilePathList.add(new ProfilePath(contactItem.get_id(), profilePath));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mContacts != null) {
            final ContactItem mContact = mContacts.get(position);
            Glide.with(mContext)
                    .load(ProfilePathList.get(position).getProfilePath())
                    .thumbnail(0.5f)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
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

    public String saveProfileImage(String _id, Bitmap bitmap) {
        String imagePath = null;
        try {
            String filename = _id + ".jpeg";
            Log.e("PROFILE_NAME", filename);
            FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            imagePath = mContext.getFileStreamPath(filename).getPath();
            Log.e("PROFILE_PATH", imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return imagePath;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        Glide.get(mContext).clearMemory();
    }
}
