package com.textnext.koshal.asl;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by me on 10/26/2017.
 */

class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder> {

    Context mContext;
    ArrayList<String> mSuggestions;

    public SuggestionsAdapter(Context context, ArrayList<String> suggestions) {
        this.mContext=context;
        this.mSuggestions=suggestions;
    }


    @Override
    public SuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v=LayoutInflater.from(mContext).inflate(R.layout.suggestion_layout, parent, false);
        return  new SuggestionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SuggestionViewHolder holder, int position) {

        holder.mSuggested.setText(mSuggestions.get(position));
        holder.mLLSug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mContext instanceof  ChatActivity)
                {
                    ((ChatActivity)(mContext)).sendMessage(holder.mSuggested.getText().toString());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder {

        TextView mSuggested;
        LinearLayout mLLSug;

        public SuggestionViewHolder(View itemView) {
            super(itemView);

            mSuggested= (TextView) itemView.findViewById(R.id.suggestion);
            mLLSug= (LinearLayout) itemView.findViewById(R.id.llSug);

        }
    }
}
