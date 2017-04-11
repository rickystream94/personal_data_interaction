package com.bobbytables.phrasebook;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.Toast;

import com.bobbytables.phrasebook.database.ChallengeModel;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.DateUtil;
import com.bobbytables.phrasebook.utils.SettingsManager;
import com.hanks.htextview.HTextView;
import com.hanks.htextview.HTextViewType;

import java.util.List;

/**
 * Created by ricky on 16/03/2017.
 */

class ChallengeCardsAdapter extends RecyclerView.Adapter<ChallengeCardsAdapter.ViewHolder>
        implements View.OnClickListener {
    private final Context context;
    private String motherLanguage;
    private String foreignLanguage;
    private ChallengeCard challengeCard;
    private static final int NUMBER_OF_CARDS = 1;
    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager;
    private XPManager xpManager;
    private BadgeManager badgeManager;
    private ViewHolder holder;
    private SettingsManager settingsManager;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foreignLanguageText;
        TextView motherLanguageText;
        TextView correctTranslation;
        HTextView newLevelText;
        EditText translation;
        CardView cardView;
        Button checkButton;
        Button nextChallenge;
        HTextView xpText;

        ViewHolder(View itemView) {
            super(itemView);
            foreignLanguageText = (TextView) itemView.findViewById(R.id.foreignText);
            motherLanguageText = (TextView) itemView.findViewById(R.id.motherLanguageText);
            translation = (EditText) itemView.findViewById(R.id.translation);
            correctTranslation = (TextView) itemView.findViewById(R.id.correctTranslation);
            newLevelText = (HTextView) itemView.findViewById(R.id.newLevel);
            cardView = (CardView) itemView.findViewById(R.id.challengeCardView);
            checkButton = (Button) itemView.findViewById(R.id.checkTranslation);
            nextChallenge = (Button) itemView.findViewById(R.id.nextChallenge);
            xpText = (HTextView) itemView.findViewById(R.id.xpText);
        }
    }

    ChallengeCardsAdapter(Context context) {
        //In our case, each time a new (random) card is created!
        //This is our "dataset" of 1 element (if you want more items, just create a list)
        this.context = context;
        this.settingsManager = SettingsManager.getInstance(context);
        motherLanguage = settingsManager.getPrefStringValue(SettingsManager
                .KEY_MOTHER_LANGUAGE);
        foreignLanguage = settingsManager.getPrefStringValue(SettingsManager
                .KEY_FOREIGN_LANGUAGE);
        challengeCard = new ChallengeCard(motherLanguage, foreignLanguage);
        databaseHelper = DatabaseHelper.getInstance(context);
        alertDialogManager = new AlertDialogManager();
        this.xpManager = XPManager.getInstance(context);
        this.badgeManager = BadgeManager.getInstance(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChallengeCardsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        int layout;
        if (databaseHelper.isDatabaseEmpty())
            layout = R.layout.empty_database;
        else
            layout = R.layout.challenge_card;

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        if (databaseHelper.isDatabaseEmpty())
            return;

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        this.holder = viewHolder;
        holder.setIsRecyclable(false); //Must be specified since we're using a custom animator
        holder.foreignLanguageText.setText(challengeCard.getForeignLanguage());
        holder.motherLanguageText.setText(databaseHelper.getRandomChallenge());
        holder.checkButton.setOnClickListener(this);
        holder.nextChallenge.setOnClickListener(this);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return NUMBER_OF_CARDS; //we will always display just one card! Otherwise, change it accordingly
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.checkTranslation:
                checkTranslation(view);
                break;
            case R.id.nextChallenge:
                nextChallenge(view);
                break;
        }
    }

    private void checkTranslation(View view) {
        //Perform check in the DB
        String translation = holder.translation.getText().toString().trim().toLowerCase();
        String correctTranslation = databaseHelper.getTranslation(holder.motherLanguageText.getText
                ().toString());
        holder.correctTranslation.setText(correctTranslation);
        boolean result = databaseHelper.checkIfCorrect(holder.motherLanguageText.getText
                        ().toString(),
                translation, correctTranslation);
        boolean isArchived = databaseHelper.updateCorrectCount(holder.motherLanguageText.getText
                ().toString(), correctTranslation, result);

        //Insert new record in DB
        int correct = result ? 1 : 0;
        int phraseId = databaseHelper.getPhraseId(holder.motherLanguageText.getText
                ().toString(), correctTranslation);
        String currentTimeString = DateUtil.getCurrentTimestamp();
        try {
            databaseHelper.insertRecord(new ChallengeModel(phraseId, currentTimeString,
                    DatabaseHelper.TABLE_CHALLENGES, correct));
        } catch (Exception e) {
            alertDialogManager.showAlertDialog(context, "Error!", e.getMessage(), false);
        }

        //TODO: to be removed after experiment. Gamification will be permanent
        if (settingsManager.getPrefBoolValue(SettingsManager.KEY_GAMIFICATION)) {
            //Check XP and Level (gain XP only if not archived!)
            boolean currentlyArchived = databaseHelper.getArchivedStatus(holder.motherLanguageText.getText
                    ().toString(), correctTranslation);
            if (result && !currentlyArchived) {
                int xp = XPManager.XP_CHALLENGE_WON; //Standard XP amount to add
                //Add experience only if max XP hasn't been reached
                if (!(xpManager.getCurrentXp() == xpManager.getXpPerLevel(XPManager.MAX_LEVEL))) {
                    xpManager.addExperience(xp);
                }
                if (xpManager.checkLevelUp()) {
                    int newLevel = xpManager.levelUp();
                    holder.newLevelText.setVisibility(View.VISIBLE);
                    holder.newLevelText.setAnimateType(HTextViewType.SCALE);
                    holder.newLevelText.animateText("Level " + newLevel + " reached!");
                    xpManager.addExperience(XPManager.XP_BONUS_LEVEL_UP);
                    xp += XPManager.XP_BONUS_LEVEL_UP;
                }
                holder.xpText.setVisibility(View.VISIBLE);
                holder.xpText.setAnimateType(HTextViewType.ANVIL);
                holder.xpText.animateText("+" + xp + "XP!");
                Log.d("XP DEBUG", "Added XP points, new XP: " + xpManager.getCurrentXp());
            }

            //Check achieved badges
            List<String> achievedBadges = badgeManager.checkNewBadges(BadgeManager.TABLE_CHALLENGES);
            if (achievedBadges.size() > 0) {
                badgeManager.showDialogAchievedBadges(context, achievedBadges);
            }
        }

        //Update UI user feedback
        int editTextBackgroundColor = result ? ContextCompat.getColor(context, R.color
                .correctAnswer) : ContextCompat.getColor(context, R.color.wrongAnser);
        if (!result) {
            holder.correctTranslation.setVisibility(View.VISIBLE);
        }
        if (isArchived) {
            Toast.makeText(context, "Great! New word just stored in long term " +
                    "memory.", Toast.LENGTH_SHORT).show();
            Log.d("DEBUG", "Word correctly archived!");
        }
        holder.translation.setBackgroundColor(editTextBackgroundColor);
        view.setVisibility(View.INVISIBLE);
        holder.nextChallenge.setVisibility(View.VISIBLE);
    }

    private void nextChallenge(View view) {
        view.setVisibility(View.INVISIBLE);
        holder.translation.setBackgroundColor(Color.parseColor("#00000000"));
        holder.correctTranslation.setVisibility(View.INVISIBLE);
        holder.checkButton.setVisibility(View.VISIBLE);
        holder.newLevelText.setVisibility(View.INVISIBLE);
        holder.xpText.setVisibility(View.INVISIBLE);
        holder.xpText.setText("");
        holder.translation.setText("");
        challengeCard = new ChallengeCard(motherLanguage, foreignLanguage);
        //notifyItemInserted(getItemCount());
        notifyItemChanged(0);
    }
}