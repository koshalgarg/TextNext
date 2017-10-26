package com.textnext.koshal.asl;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.textnext.koshal.asl.DataBase.ASLContract;
import com.textnext.koshal.asl.DataObjects.Message;

/**
 * Created by me on 6/27/2017.
 */

class ChatsAdapter extends CursorRecyclerViewAdapter {

    private Context mContext;
    private Cursor mCursor;


    public ChatsAdapter(Context context, Cursor cursor) {
        super(context, cursor);

        this.mContext = context;
        this.mCursor = cursor;

    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chat_bubble_sent_, parent, false);
            return new CustomViewHolder(v);

        }
        if (viewType == 1) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chat_bubble_received, parent, false);
            return new CustomViewHolder(v);

        }
        if (viewType == 2) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.image_layout_mine, parent, false);
            return new CustomImageViewHolder(v);

        }
        if (viewType == 3) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.image_layout, parent, false);
            return new CustomImageViewHolder(v);

        }
       /* if (viewType == 5) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.nartive_add, parent, false);
            return new AddViewHolder(v);

        }*/
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {

        final Message message = Message.fromCursor(cursor);
        int type = getItemViewType(cursor.getPosition());
        switch (type) {
            case 0://my message
            {
                bindMyMessages(viewHolder, message);
                break;
            }
            case 1://received message
            {

                bindReceivedMessages(viewHolder, message);
                break;
            }
            case 2://myImage
            {
                bindMyImage(viewHolder, message);
                break;
            }
            case 3: {
                bindReceivedImage(viewHolder, message);
                break;
            }

        }


    }


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {

            if (cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_TYPE)).equals("ADD")) {
                return 5;
            } else if (cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_USER_ID)).equals(ASL.mDeviceId)) {
                if (cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_TYPE)).toUpperCase().equals("TEXT")) {
                    return 0;  //text self
                } else
                    return 2; //image self

            } else {

                if (cursor.getString(cursor.getColumnIndex(ASLContract.MessagesEntry.COL_TYPE)).toUpperCase().equals("TEXT")) {
                    return 1;  //text
                } else
                    return 3; //
            }


        }
        return -1;
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView time;
        ImageView status;


        public CustomViewHolder(View v) {
            super(v);
            message = (TextView) v.findViewById(R.id.chat);
            time = (TextView) v.findViewById(R.id.time);
            status = (ImageView) v.findViewById(R.id.status);
        }
    }


    private void bindMyMessages(RecyclerView.ViewHolder viewHolder, final Message message) {

        CustomViewHolder holder = (CustomViewHolder) viewHolder;

        holder.message.setText(message.getMsg());
        holder.time.setText(ASLUtils.time(Long.parseLong(message.getTs())));

        holder.status.setVisibility(View.VISIBLE);

        if (message.getStatus().equals("1")) {
            holder.status.setImageResource(R.drawable.ic_done_all_white_24dp);
        } else {
                holder.status.setImageResource(R.drawable.ic_done_white_24dp);
        }
    }

    private void bindReceivedMessages(RecyclerView.ViewHolder viewHolder, Message message) {
        CustomViewHolder holder = (CustomViewHolder) viewHolder;

        holder.message.setText(message.getMsg());

        holder.time.setText(ASLUtils.time(Long.parseLong(message.getTs())));

        holder.status.setVisibility(View.GONE);
    }

    private void bindMyImage(final RecyclerView.ViewHolder viewHolder, final Message message) {

        final CustomImageViewHolder holder = (CustomImageViewHolder) viewHolder;

        holder.time.setText(ASLUtils.time(Long.parseLong(message.getTs())));



        Picasso.with(mContext).load(message.getUri()).resize(240, 240).networkPolicy(NetworkPolicy.OFFLINE).centerCrop().into(holder.image, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(mContext)
                        .load(message.getMsg())
                        .resize(240, 240)
                        .centerCrop()
                        .into(holder.image);

            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentTransaction ft = ((Activity) mContext).getFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("url", message.getMsg());
                bundle.putString("uri", message.getUri());

                MyDialogFragment newFragment = MyDialogFragment.newInstance(bundle);
                newFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_NoActionBar);
                newFragment.show(ft, "dialog");
            }
        });

        if (message.getStatus().equals("1")) {
            holder.status.setImageResource(R.drawable.ic_done_all_white_24dp);
            holder.status.setVisibility(View.VISIBLE);
        }
        else if(!message.getMsg().contains("http"))
        {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setImageResource(R.drawable.ic_query_builder_white_24dp);
            holder.progressBar.setVisibility(View.VISIBLE);
        }

        else {
                holder.status.setVisibility(View.VISIBLE);
                holder.status.setImageResource(R.drawable.ic_done_white_24dp);
        }
    }


    private class CustomImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView time;
        ImageView status;
        LinearLayout rv;
        ProgressBar progressBar;

        public CustomImageViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            time = (TextView) v.findViewById(R.id.time);
            status = (ImageView) v.findViewById(R.id.status);
            rv = (LinearLayout) v.findViewById(R.id.chatBubble);
            progressBar = (ProgressBar) v.findViewById(R.id.progress);

        }
    }

    private void bindReceivedImage(RecyclerView.ViewHolder viewHolder, Message message) {

        final CustomImageViewHolder holder = (CustomImageViewHolder) viewHolder;
        holder.time.setText(ASLUtils.time(Long.parseLong(message.getTs())));
        final String url = message.getMsg();


        Picasso.with(mContext).load(url).resize(240, 240).networkPolicy(NetworkPolicy.OFFLINE).centerCrop().into(holder.image, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(mContext)
                        .load(url)
                        .resize(240, 240)
                        .centerCrop()
                        .into(holder.image);

            }
        });


        Picasso.with(mContext)
                .load(url)
                .resize(240, 240)
                .centerCrop()
                .into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentTransaction ft = ((Activity) mContext).getFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putString("image", url);
                android.app.DialogFragment newFragment = MyDialogFragment.newInstance(bundle);
                newFragment.setStyle(android.app.DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_NoActionBar);
                newFragment.show(ft, "dialog");
            }
        });
    }


}
