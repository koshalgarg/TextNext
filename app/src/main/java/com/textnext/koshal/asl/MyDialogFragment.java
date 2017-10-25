package com.textnext.koshal.asl;

/**
 * Created by me on 10/21/2017.
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by me on 7/3/2017.
 */

public class MyDialogFragment extends DialogFragment {

    Bundle data;

    static MyDialogFragment newInstance(Bundle b) {
        MyDialogFragment f = new MyDialogFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle b = getArguments();
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);
        ImageView i = (ImageView) v.findViewById(R.id.image);
        final ProgressBar progressBar= (ProgressBar) v.findViewById(R.id.progress);

        String uri = b.getString("image");
        Log.i("showing", uri);

        Picasso.with(getActivity())
                .load(uri)
                .into(i, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        return v;
    }


}
