package com.bobbytables.phrasebook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bobbytables.phrasebook.utils.SettingsManager;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    private CircleImageView profileImage;
    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        settingsManager = SettingsManager.getInstance(getApplicationContext());
        profileImage = (CircleImageView) findViewById(R.id.profileImage);

        //Set nickname text
        TextView nicknameText = (TextView) findViewById(R.id.nicknameText);
        nicknameText.setText(settingsManager.getPrefStringValue(SettingsManager.KEY_NICKNAME));

        //Load profile picture
        String path = settingsManager.getPrefStringValue(SettingsManager
                .KEY_PROFILE_PIC);
        if (!path.equals("DEFAULT")) {
            loadProfilePic(path);
        } else
            profileImage.setImageResource(R.drawable.camera);
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
            Uri uri = data.getData();
            String path = getRealPathFromURI(uri);
            settingsManager.updatePrefValue(SettingsManager.KEY_PROFILE_PIC, path);
            Log.d("Profile Pic", "Correctly updated! " + path);
            loadProfilePic(path);
        }
    }

    /**
     * Extracts full path from URI, extremely important since it's impossible to get resources
     * with URI
     *
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Uri contentUri) {
        String wholeID = DocumentsContract.getDocumentId(contentUri);
        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = null;
        try {
            String[] column = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);
            cursor.moveToFirst();
            return cursor.getString(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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
}
