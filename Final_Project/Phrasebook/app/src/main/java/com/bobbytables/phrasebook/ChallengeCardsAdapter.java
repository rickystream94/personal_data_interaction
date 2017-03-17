package com.bobbytables.phrasebook;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ricky on 16/03/2017.
 */

class ChallengeCardsAdapter extends RecyclerView.Adapter<ChallengeCardsAdapter.ViewHolder> {
    private ChallengeCard challengeCard;
    private static final int NUMBER_OF_CARDS = 1;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        CardView cardView;
        Button refreshButton;
        ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView.findViewById(R.id.cardText);
            cardView = (CardView)itemView.findViewById(R.id.challengeCardView);
            refreshButton = (Button)itemView.findViewById(R.id.refreshCard);
        }
    }

    ChallengeCardsAdapter() {
        challengeCard = new ChallengeCard();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChallengeCardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.challenge_card, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(challengeCard.getRandomText());
        holder.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                challengeCard = new ChallengeCard();
                notifyItemChanged(0);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return NUMBER_OF_CARDS; //we will always display just one card! Otherwise, change it accordingly
    }
}
