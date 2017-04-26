package com.bobbytables.phrasebook;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bobbytables.phrasebook.utils.AlertDialogManager;
import com.bobbytables.phrasebook.utils.SettingsManager;

import java.util.HashMap;
import java.util.Map;

public class NewUserActivity extends AppCompatActivity {

    private AlertDialogManager alertDialogManager;
    private static final int NICKNAME_MIN_LENGTH = 4;
    private static final int LANG_MIN_LENGTH = 3;
    private RequestQueue requestQueue;
    private static final String SERVER_URL = "http://www.richmondweb.it/phrasebook/new_user.php";
    private boolean gamification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        alertDialogManager = new AlertDialogManager();
        //Initialize request queue for Volley
        requestQueue = Volley.newRequestQueue(this);
    }

    public void createUser(View view) {
        String nickname = ((TextView) findViewById(R.id.nicknameText)).getText()
                .toString();
        String motherLanguage = ((TextView) findViewById(R.id.motherLanguage)).getText()
                .toString();
        String foreignLanguage = ((TextView) findViewById(R.id.foreignLanguage)).getText()
                .toString();
        String errorMessage = "";
        if (nickname.length() < NICKNAME_MIN_LENGTH)
            errorMessage += "Nickname too short, it must be long at least " + NICKNAME_MIN_LENGTH + " " +
                    "characters\n";
        if (motherLanguage.length() < LANG_MIN_LENGTH)
            errorMessage += "Mother language name too short, it must be long at least " +
                    "" + LANG_MIN_LENGTH + " characters\n";
        if (foreignLanguage.length() < LANG_MIN_LENGTH)
            errorMessage += "Foreign language name too short, it must be long at least " +
                    "" + LANG_MIN_LENGTH + " characters\n";
        if (!isConnected())
            errorMessage += "You're currently not connected to any network! Please create your " +
                    "user while having an active internet connection!";
        if (errorMessage.length() > 0) {
            alertDialogManager.showAlertDialog(NewUserActivity.this, "Error!", errorMessage, false);
            return;
        }
        //Otherwise, if everything is fine, proceed
        requestVersionType(nickname, motherLanguage, foreignLanguage);
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void requestVersionType(final String nickname, final String motherLanguage, final
    String foreignLanguage) {
        Request request = new StringRequest(Request.Method.POST, SERVER_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Request Response", response);
                int value = Integer.parseInt(response); //Server always replies either with 1 or 0
                gamification = value == 1;
                SettingsManager settingsManager = SettingsManager.getInstance(getApplicationContext());
                settingsManager.createUser(nickname, motherLanguage, foreignLanguage, gamification);
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR RESPONSE", error.toString());
                Toast.makeText(NewUserActivity.this, "An error occurred! Please try again...", Toast
                        .LENGTH_SHORT)
                        .show();
            }
        }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nickname", nickname);
                return params;
            }
        };

        // Adding request to request queue
        requestQueue.add(request);
    }
}
