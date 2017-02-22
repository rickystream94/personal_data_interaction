package it.richmondweb.responsetimetest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button yesButton;
    private Button noButton;
    private TextView question;
    private TypedArray images;
    private Random random = new Random();
    private LinearLayout mainPanel;
    private ImageView imageView;
    private Button clickButton;
    private int currentAttempts = 0;
    private final static int checkpoint = 5;
    private int currentImageResource;
    private Handler delayHandler;
    private int maxMillisDelay = 500;
    private int acceptable = 0;
    private int notAcceptable = 0;
    private int currentDelay;
    private String nickname;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(getApplicationContext());
        nickname = getIntent().getExtras().getString("nickname");
        imageView = (ImageView) findViewById(R.id.image);
        mainPanel = (LinearLayout) findViewById(R.id.mainPanel);
        clickButton = (Button) findViewById(R.id.button);
        images = getResources().obtainTypedArray(R.array.random_imgs);
        currentImageResource = images.getResourceId(random.nextInt(images.length()), -1);

        //Hide buttons the first time
        toggleButtons(false);
    }

    public void toggleButtons(boolean value) {
        int visibility = value ? View.VISIBLE : View.INVISIBLE;
        yesButton = (Button) findViewById(R.id.yes);
        yesButton.setVisibility(visibility);
        noButton = (Button) findViewById(R.id.no);
        noButton.setVisibility(visibility);
        question = (TextView) findViewById(R.id.question);
        question.setVisibility(visibility);
        //Click button always opposed to the others
        clickButton.setVisibility(!value ? View.VISIBLE : View.INVISIBLE);
    }

    public void click(View view) {
        delayHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                currentDelay = message.getData().getInt("delay");
                //Toast.makeText(MainActivity.this, "Delay from thread: " + currentDelay + "ms",
                 //       Toast
                  //      .LENGTH_SHORT)
                   //     .show();
                refreshImage();
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    int delay = random.nextInt(maxMillisDelay);
                    Thread.sleep(delay);
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("delay", delay);
                    message.setData(bundle);
                    message.setTarget(delayHandler);
                    message.sendToTarget();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    public void refreshImage() {
        if (mainPanel.getChildAt(0) instanceof ImageView)
            mainPanel.removeViewAt(0);
        imageView.setImageResource(currentImageResource);
        mainPanel.addView(imageView, 0);

        //Get random image and set is as a resource for the ImageView
        //Avoid two times in a row the same image (get ready for next round)
        int newImageResource;
        do {
            newImageResource = images.getResourceId(random.nextInt(images.length()), -1);
        } while (currentImageResource == newImageResource);
        currentImageResource = newImageResource;
        toggleButtons(true);
    }

    public void choice(View view) {
        String choice = ((Button) view).getText().toString();
        boolean isAcceptable = choice.equals("yes");
        if (isAcceptable) {
            acceptable++;
        } else {
            notAcceptable++;
        }
        db.insertResponseTimeTest(currentDelay,isAcceptable,nickname);
        currentAttempts++;
        toggleButtons(false);

        if (currentAttempts % checkpoint == 0) {
            String message = "Are you tired? Please, go on as much as you can't stand it anymore, " +
                    "it would be great if you answer the question 20 times!" + "\nCurrent No. of " +
                    "answers: " + currentAttempts;
            displayDialog(message, "Checkpoint reached!", android.R.drawable.ic_dialog_info);
        }
    }

    private void displayDialog(String message, String title, int icon) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Go On", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Thank you so much " + nickname + " for your patience and" +
                                        " contribution!",
                                Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                })
                .setIcon(icon)
                .setCancelable(false)
                .show();
    }
}
