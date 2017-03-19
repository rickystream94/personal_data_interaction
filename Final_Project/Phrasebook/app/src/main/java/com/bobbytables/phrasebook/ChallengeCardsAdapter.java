package com.bobbytables.phrasebook;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.SettingsManager;

import java.io.Console;

import static com.bobbytables.phrasebook.R.id.foreignLanguage;
import static com.bobbytables.phrasebook.R.id.motherLanguage;
import static com.bobbytables.phrasebook.R.id.motherLanguageText;
import static com.bobbytables.phrasebook.R.id.nextChallenge;

/**
 * Created by ricky on 16/03/2017.
 */

class ChallengeCardsAdapter extends RecyclerView.Adapter<ChallengeCardsAdapter.ViewHolder> {
    private final Context context;
    private String motherLanguage;
    private String foreignLanguage;
    private ChallengeCard challengeCard;
    private static final int NUMBER_OF_CARDS = 1;
    private DatabaseHelper databaseHelper;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foreignLanguageText;
        TextView motherLanguageText;
        TextView correctTranslation;
        EditText translation;
        CardView cardView;
        Button checkButton;
        Button nextChallenge;

        ViewHolder(View itemView) {
            super(itemView);
            foreignLanguageText = (TextView) itemView.findViewById(R.id.foreignText);
            motherLanguageText = (TextView) itemView.findViewById(R.id.motherLanguageText);
            translation = (EditText) itemView.findViewById(R.id.translation);
            correctTranslation = (TextView) itemView.findViewById(R.id.correctTranslation);
            cardView = (CardView) itemView.findViewById(R.id.challengeCardView);
            checkButton = (Button) itemView.findViewById(R.id.checkTranslation);
            nextChallenge = (Button) itemView.findViewById(R.id.nextChallenge);
        }
    }

    ChallengeCardsAdapter(Context context) {
        //In our case, each time a new (random) card is created!
        //This is our "dataset" of 1 element (if you want more items, just create a list)
        this.context = context;
        motherLanguage = SettingsManager.getInstance(context).getPrefValue(SettingsManager
                .KEY_MOTHER_LANGUAGE);
        foreignLanguage = SettingsManager.getInstance(context).getPrefValue(SettingsManager
                .KEY_FOREIGN_LANGUAGE);
        challengeCard = new ChallengeCard(motherLanguage, foreignLanguage);
        databaseHelper = DatabaseHelper.getInstance(context);
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.foreignLanguageText.setText(challengeCard.getForeignLanguage());
        String motherLanguageText;
        if (!databaseHelper.isDatabaseEmpty()) {
            motherLanguageText = databaseHelper.getRandomChallenge();
        } else motherLanguageText = "EMPTY DB"; //to be changed
        holder.motherLanguageText.setText(motherLanguageText);
        holder.checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String translation = holder.translation.getText().toString().toLowerCase();
                String correctTranslation = databaseHelper.getTranslation(holder.motherLanguageText.getText
                        ().toString());
                holder.correctTranslation.setText(correctTranslation);
                boolean result = databaseHelper.checkIfCorrect(holder.motherLanguageText.getText
                                ().toString(),
                        translation, correctTranslation);
                int editTextBackgroundColor = result ? ContextCompat.getColor(context, R.color
                        .correctAnswer) : ContextCompat.getColor(context, R.color.wrongAnser);
                if (!result) {
                    holder.correctTranslation.setVisibility(View.VISIBLE);
                }
                holder.translation.setBackgroundColor(editTextBackgroundColor);
                view.setVisibility(View.INVISIBLE);
                holder.nextChallenge.setVisibility(View.VISIBLE);
            }
        });
        holder.nextChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.INVISIBLE);
                holder.translation.setBackgroundColor(Color.parseColor("#00000000"));
                holder.correctTranslation.setVisibility(View.INVISIBLE);
                holder.checkButton.setVisibility(View.VISIBLE);
                holder.translation.setText("");
                challengeCard = new ChallengeCard(motherLanguage, foreignLanguage);
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
