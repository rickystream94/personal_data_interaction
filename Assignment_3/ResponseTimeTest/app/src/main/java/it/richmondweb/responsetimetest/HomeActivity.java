package it.richmondweb.responsetimetest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void start(View view) {
        String nickname = ((EditText)findViewById(R.id.nickname)).getText().toString();
        if (nickname.length()<4) {
            displayDialog("Please insert a nickname of at least 4 characters!","Error!",android.R
                    .drawable.ic_dialog_alert);
            return;
        }
        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
        intent.putExtra("nickname",nickname);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        switch (item.getItemId()) {
            case R.id.export:
                checkWritePermissions();
                dbHelper.export();
                Toast.makeText(this, "Exported in Downloads/ResponseTimeTest as JSON", Toast
                        .LENGTH_LONG).show();
                return true;
            case R.id.delete:
                dbHelper.reset();
                Toast.makeText(this, "All data deleted!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.howto:
                String message = getString(R.string.howTo);
                displayDialog(message,"How to Play",android.R.drawable.ic_dialog_info);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayDialog(String message,String title, int icon) {
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(icon)
                .setCancelable(false)
                .show();
    }

    private void checkWritePermissions(){

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // We request the permission.
                Log.e("requesting", "write external permissions");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

}
