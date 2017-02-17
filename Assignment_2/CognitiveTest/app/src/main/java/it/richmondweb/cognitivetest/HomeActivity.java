package it.richmondweb.cognitivetest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.richmondweb.cognitivetest.Models.EriksenFlanker;

public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        checkWritePermissions();
        showResults(dbHelper);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showResults(dbHelper);
    }

    private void showResults(DatabaseHelper dbHelper) {
        ListView listView = (ListView) findViewById(R.id.results);

        //Formatting the results
        ArrayList<EriksenFlanker> results = dbHelper.getAllEriksenFlankerTests();
        Log.d("test","Number of results to display in home: "+results.size());
        List<String> strings = formatResults(results);

        //Setting the list view adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.results_row, strings);
        listView.setAdapter(adapter);
    }

    private List<String> formatResults(ArrayList<EriksenFlanker> results) {
        List<String> strings = new ArrayList<>();
        for(EriksenFlanker result : results) {
            int correct = result.getCorrect();
            int incorrect = result.getIncorrect();
            String date = result.getCreated();
            String string = "Date: "+date+"\nCorrect: "+correct+"\nWrong: "+incorrect;
            strings.add(string);
        }
        return strings;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_delete_data:
                Log.d("Menu clicked", "delete_data");
                dbHelper.reset();
                showResults(dbHelper);
                Toast.makeText(this, "All data deleted!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.menu_action_export_data:
                Log.d("Menu clicked", "export_data");
                dbHelper.export();
                Toast.makeText(this, "Exported in Downloads/CognitiveTest as JSON", Toast
                        .LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startNewTest(View view) {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
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
