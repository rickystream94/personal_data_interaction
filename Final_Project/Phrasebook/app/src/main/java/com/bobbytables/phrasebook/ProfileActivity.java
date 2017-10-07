package com.bobbytables.phrasebook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.bobbytables.phrasebook.database.DatabaseHelper;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;
import com.hanks.htextview.HTextView;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profileImage;
    private SettingsManager settingsManager;
    private XPManager xpManager;
    private DatabaseHelper databaseHelper;
    private AlertDialogManager alertDialogManager = new AlertDialogManager();
    private static final String DEFAULT_PIC = "DEFAULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settingsManager = SettingsManager.getInstance(getApplicationContext());
        xpManager = XPManager.getInstance(getApplicationContext());
        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        //Set nickname text
        HTextView nicknameText = (HTextView) findViewById(R.id.nicknameText);
        nicknameText.animateText(settingsManager.getPrefStringValue(SettingsManager.KEY_NICKNAME));

        //Set language text
        TextView myLanguage = (TextView) findViewById(R.id.myLanguage);
        myLanguage.setText(settingsManager.getCurrentLanguagesNames().getAsString(SettingsManager
                .KEY_CURRENT_LANG2_STRING));

        //Load profile picture
        profileImage = (CircleImageView) findViewById(R.id.profileImage);
        String path = settingsManager.getPrefStringValue(SettingsManager
                .KEY_PROFILE_PIC);
        if (!path.equals(DEFAULT_PIC)) {
            loadProfilePic(path);
        } else
            profileImage.setImageResource(R.drawable.camera);

        //Load experience progress bar and level bar
        loadProgressBars();

        //Load badges grid
        loadBadgesGrid();
    }

    private void loadBadgesGrid() {
        ExpandableHeightGridView badgesGridView = (ExpandableHeightGridView) findViewById(R.id.badgesGridView);
        badgesGridView.setOnItemClickListener(this);

        Cursor cursor = databaseHelper.getAllBadges();
        BadgeAdapter badgeAdapter = new BadgeAdapter(ProfileActivity.this, cursor);
        badgesGridView.setAdapter(badgeAdapter);
        badgesGridView.setExpanded(true);
    }

    private void loadProgressBars() {
        RoundCornerProgressBar xpPointsBar = (RoundCornerProgressBar) findViewById(R.id.xpPointsBar);
        RoundCornerProgressBar levelBar = (RoundCornerProgressBar) findViewById(R.id.levelsBar);
        TextView currentLevelLabel = (TextView) findViewById(R.id.currentLevelLabel);
        TextView currentXpLabel = (TextView) findViewById(R.id.currentXpLabel);
        int currentLevel = xpManager.getCurrentLevel();
        int currentXp = xpManager.getCurrentXp();
        int currentLevelMinExp = xpManager.getXpPerLevel(currentLevel);
        int nextLevelXp = xpManager.getXpPerLevel(currentLevel + 1);
        currentLevelLabel.setText("Level " + currentLevel + "/" + XPManager.MAX_LEVEL);
        currentXpLabel.setText(currentXp + "/" + nextLevelXp + " XP");
        if (xpManager.getCurrentLevel() == XPManager.MAX_LEVEL) {
            int XPmax = xpManager.getXpPerLevel(currentLevel);
            xpPointsBar.setMax(XPmax);
            xpPointsBar.setProgress(XPmax);
        } else {
            xpPointsBar.setMax(nextLevelXp - currentLevelMinExp);
            xpPointsBar.setProgress(currentXp - currentLevelMinExp);
        }
        levelBar.setMax(XPManager.MAX_LEVEL);
        levelBar.setProgress(currentLevel);
        xpPointsBar.invalidate();
        levelBar.invalidate();
    }

    /**
     * Invokes the intent to pick a picture from gallery
     */
    public void changeProfilePicture() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Invoked when user chooses the picture from the gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri originalUri = data.getData();
            String path = getRealPathFromURI(originalUri);
            if (path.equals(DEFAULT_PIC))
                return; //Don't update if can't find correct pic path
            settingsManager.updatePrefValue(SettingsManager.KEY_PROFILE_PIC, path);
            Log.d("Profile Pic", "Correctly updated! " + path);
            loadProfilePic(path);
        }
    }

    /**
     * Extracts full path from URI, extremely important since it's impossible to get resources
     * with URI
     *
     * @param originalUri
     * @return
     */
    public String getRealPathFromURI(Uri originalUri) {
        //String wholeID = DocumentsContract.getDocumentId(uri);
        // Split at colon, use second item in the array
        //String id = wholeID.split(":")[1];
        String id = originalUri.getLastPathSegment().split(":")[1];
        // where id is equal to
        String selection = MediaStore.Images.Media._ID + "=?";
        Cursor imageCursor = null;
        try {
            Uri uri = getUri();
            String[] column = {MediaStore.Images.Media.DATA};
            CursorLoader cursorLoader = new CursorLoader(this, uri, column, selection, new String[]{id},
                    null);
            imageCursor = cursorLoader.loadInBackground();
            if (imageCursor.moveToFirst())
                return imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            else return DEFAULT_PIC;
        } finally {
            if (imageCursor != null) {
                imageCursor.close();
            }
        }
    }

    /**
     * By using this method get the Uri of Internal/External Storage for Media
     *
     * @return
     */
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    /**
     * Sets a profile pic given the absolute path of the image file
     *
     * @param path image path
     */
    private void loadProfilePic(String path) {
        File file = new File(path);
        if (file.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImage.setImageBitmap(myBitmap);
        }
    }

    public void checkReadPermissions(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // We request the permission.
            Log.e("requesting", "read external permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else
            changeProfilePicture();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeProfilePicture();
                } else {
                    Toast.makeText(this, "Error, permission not granted!", Toast
                            .LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Cursor cursor = (Cursor) adapterView.getAdapter().getItem(position);
        cursor.moveToPosition(position);
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_BADGE_DESCRIPTION));
        String badgeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_BADGE_NAME));
        String createdOn = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper
                .KEY_CREATED_ON));
        if (createdOn != null)
            description += "\n\nAchieved on: " + createdOn.split("\\s")[0];
        alertDialogManager.showAlertDialog(ProfileActivity.this, badgeName, description, true);
    }
}
