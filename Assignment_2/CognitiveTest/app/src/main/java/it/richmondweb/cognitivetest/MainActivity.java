package it.richmondweb.cognitivetest;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.*;

import it.richmondweb.cognitivetest.Models.EriksenFlanker;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_ROWS = 3;
    private static final int GRID_COLUMNS = 5;
    private static final int testDuration = 10;

    private Random random = new Random();
    private String arrowsHead;
    private String centralArrowDirection;
    private String centralArrowHead;
    private TableLayout arrowsGridLayout;
    private Typeface iconFont;
    private TextView timerCounter;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);

        //Assigning correct font to control panel buttons
        TextView upButton = (TextView) findViewById(R.id.upButton);
        TextView downButton = (TextView) findViewById(R.id.downButton);
        TextView rightButton = (TextView) findViewById(R.id.rightButton);
        TextView leftButton = (TextView) findViewById(R.id.leftButton);
        upButton.setTypeface(iconFont);
        downButton.setTypeface(iconFont);
        rightButton.setTypeface(iconFont);
        leftButton.setTypeface(iconFont);

        //Getting the arrows grid layout
        arrowsGridLayout = (TableLayout) findViewById(R.id.arrowsGrid);
        arrowsGridLayout.setWeightSum(1.0f);
        drawGrid(null);

        //Launch timer thread and start playing the test
        GameState.startPlaying();
        startTimerThread();
    }

    public void drawGrid(View view) {
        /*if (view != null) {
            if (counter == numberOfTests) {
                displayDialog();
                return;
            }
            counter++;
            TextView counterText = (TextView) findViewById(R.id.counter);
            counterText.setText("Counter: " + counter);
        }*/
        arrowsGridLayout.removeAllViews();

        for (int i = 0; i < GRID_ROWS; i++) {
            TableRow row = new TableRow(getApplicationContext());
            for (int j = 0; j < GRID_COLUMNS; j++) {
                TextView arrow = new TextView(getApplicationContext());
                arrow.setTypeface(iconFont);
                arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);

                //Check if it's the center table cell (must always be visible)
                //Set all the other cells temporarily invisible
                String text = getRandomArrow();
                if (i == GRID_ROWS / 2 && j == GRID_COLUMNS / 2) {
                    arrow.setVisibility(View.VISIBLE);
                    if (text.equals(getString(R.string.fa_arrow_left)) || text.equals(getString(R.string
                            .fa_arrow_right)))
                        centralArrowDirection = "HORIZONTAL";
                    else
                        centralArrowDirection = "VERTICAL";
                } else {
                    arrow.setVisibility(View.INVISIBLE);
                }
                arrow.setTextColor(Color.BLACK);
                arrow.setText(text);

                row.addView(arrow, j);
                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams
                        .MATCH_PARENT, 0, (float) (1 * 1.0 / GRID_ROWS)));
                row.setGravity(Gravity.CENTER);
            }
            arrowsGridLayout.addView(row);
        }

        //Stretch all columns to fit the screen width
        for (int i = 0; i < GRID_COLUMNS; i++)
            arrowsGridLayout.setColumnStretchable(i, true);

        //Display the other arrows according to the attribute centralArrowDirection
        setArrowsHead(centralArrowDirection);
        fillTable();
    }

    private void completeTest() {
        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
        db.insertEriksenFlankerTest(correctAnswers,wrongAnswers);
        ArrayList<EriksenFlanker> testsEriksenFlanker = db.getAllEriksenFlankerTests();
        for (EriksenFlanker test : testsEriksenFlanker) {
            Log.d("EriksenFlankerTest", String.format("%d - %s. Correct: %d. Incorrect: %d", test.getId(), test.getCreated(), test.getCorrect(), test.getIncorrect()));
        }

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Test Completed!")
                .setMessage("Contratulations, your result:\nCorrect Answers: " +
                        ""+correctAnswers+"\nWrong Answers: "+wrongAnswers)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }

    public String getRandomArrow() {
        String[] arrowValues = getResources().getStringArray(R.array.arrows);
        return arrowValues[random.nextInt(arrowValues.length)];
    }

    public void setArrowsHead(String centralArrowDirection) {
        String[] leftRight = new String[]{"LEFT", "RIGHT"};
        String[] upDown = new String[]{"UP", "DOWN"};
        arrowsHead = (centralArrowDirection.equals("HORIZONTAL") ? leftRight[random.nextInt
                (leftRight.length)] : upDown[random.nextInt(upDown.length)]);
    }

    //It will make all the other arrows visible and with arrowsHead value
    private void fillTable() {
        String text;
        switch (arrowsHead) {
            case "LEFT":
                text = getString(R.string.fa_arrow_left);
                break;
            case "RIGHT":
                text = getString(R.string.fa_arrow_right);
                break;
            case "UP":
                text = getString(R.string.fa_arrow_up);
                break;
            case "DOWN":
                text = getString(R.string.fa_arrow_down);
                break;
            //Won't happen for sure
            default:
                text = null;
        }

        for (int i = 0; i < GRID_ROWS; i++) {
            TableRow row = (TableRow) arrowsGridLayout.getChildAt(i);
            for (int j = 0; j < GRID_COLUMNS; j++) {
                TextView textView = (TextView) row.getChildAt(j);
                textView.setText(text);
                //Central cell
                if (i == GRID_ROWS / 2 && j == GRID_COLUMNS / 2) {
                    textView.setBackground(getDrawable(R.drawable.back));
                    if (centralArrowDirection.equals("HORIZONTAL")) {
                        if(random.nextFloat()<.5) {
                            textView.setText(getString(R.string.fa_arrow_right));
                            centralArrowHead = "RIGHT";
                        } else {
                            textView.setText(getString(R.string.fa_arrow_left));
                            centralArrowHead = "LEFT";
                        }
                    }
                    else {
                        if(random.nextFloat()<.5) {
                            textView.setText(getString(R.string.fa_arrow_down));
                            centralArrowHead = "DOWN";
                        } else {
                            textView.setText(getString(R.string.fa_arrow_up));
                            centralArrowHead = "UP";
                        }
                    }
                }
                if (centralArrowDirection.equals("HORIZONTAL") && i == GRID_ROWS / 2) {
                    //Edges of the screen
                    if (j == 0 || j == GRID_COLUMNS - 1) {
                        textView.setVisibility(random.nextFloat() <= .5 ? View.VISIBLE : View.INVISIBLE);
                    } else {
                        textView.setVisibility(View.VISIBLE);
                    }
                } else if (centralArrowDirection.equals("VERTICAL") && j == GRID_COLUMNS / 2) {
                    textView.setVisibility(View.VISIBLE);
                } else {
                    //All other cases must be randomly visible
                    textView.setVisibility(random.nextFloat() <= .5 ? View.VISIBLE : View.INVISIBLE);
                }
            }
        }
    }

    private void startTimerThread() {
        final Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message message) {
                int seconds = message.getData().getInt("seconds");
                timerCounter.setText("" + seconds);
                //If the timer is over
                if(seconds<=0) {
                    GameState.stopPlaying();
                    completeTest();
                }
            }
        };
        timerCounter = (TextView) findViewById(R.id.timerCounter);
        Runnable runnable = new Runnable() {
            public void run() {
                int currentSeconds = 0;
                while (currentSeconds <= testDuration) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putInt("seconds",testDuration-currentSeconds);
                    message.setData(bundle);
                    message.setTarget(handler);
                    message.sendToTarget();
                    currentSeconds++;
                }
            }
        };
        new Thread(runnable).start();
    }

    public void userButtonChoice(View view) {
        if(!GameState.isPlayMode())
            return;
        TextView textView = (TextView)view;
        int transitionColor;
        if(textView.getContentDescription().toString().equals(centralArrowHead)) {
            correctAnswers++;
            transitionColor = Color.GREEN;
        }
        else {
            wrongAnswers++;
            transitionColor = Color.RED;
        }
        ValueAnimator animator = ObjectAnimator.ofInt(textView,"textColor",transitionColor,getColor(R.color
                .panelButtons));
        animator.setDuration(300);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();

        //Refreshes the grid
        drawGrid(null);
    }

    //Might be used for menu button to restart the test
    /*public void restart(View view) {
        counter = 0;
        TextView counterText = (TextView) findViewById(R.id.counter);
        counterText.setText("Counter: " + counter);
        drawGrid(null);
    }*/
}
