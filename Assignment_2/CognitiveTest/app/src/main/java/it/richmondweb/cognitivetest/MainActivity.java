package it.richmondweb.cognitivetest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_ROWS = 3;
    private static final int GRID_COLUMNS = 5;
    private static final int numberOfTests = 10;
    private Random random = new Random();
    private String arrowsDirection;
    private String centralArrowDirection;
    private TableLayout arrowsGridLayout;
    private Typeface iconFont;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);

        //Assigning correct font to control panel buttons
        TextView upButton = (TextView)findViewById(R.id.upButton);
        TextView downButton = (TextView)findViewById(R.id.downButton);
        TextView rightButton = (TextView)findViewById(R.id.rightButton);
        TextView leftButton = (TextView)findViewById(R.id.leftButton);
        upButton.setTypeface(iconFont);
        downButton.setTypeface(iconFont);
        rightButton.setTypeface(iconFont);
        leftButton.setTypeface(iconFont);

        //Getting the arrows grid layout
        arrowsGridLayout = (TableLayout) findViewById(R.id.arrowsGrid);
        arrowsGridLayout.setBackgroundColor(Color.YELLOW);
        arrowsGridLayout.setWeightSum(1.0f);
        drawGrid(null);
    }

    public void drawGrid(View view) {
        if (view != null) {
            if (counter == numberOfTests) {
                displayDialog();
                return;
            }
            counter++;
            /*TextView counterText = (TextView) findViewById(R.id.counter);
            counterText.setText("Counter: " + counter);*/
        }
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
                    arrow.setTextColor(Color.BLUE);
                    if (text.equals(getString(R.string.fa_arrow_left)) || text.equals(getString(R.string
                            .fa_arrow_right)))
                        centralArrowDirection = "HORIZONTAL";
                    else
                        centralArrowDirection = "VERTICAL";
                } else {
                    arrow.setTextColor(Color.BLACK);
                    arrow.setVisibility(View.INVISIBLE);
                }
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
        setArrowsDirection(centralArrowDirection);
        fillTable();
    }

    private void displayDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Test Completed!")
                .setMessage("Contratulations, your result: XXX")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public String getRandomArrow() {
        String[] arrowValues = getResources().getStringArray(R.array.arrows);
        return arrowValues[random.nextInt(arrowValues.length)];
    }

    public void setArrowsDirection(String centralArrowDirection) {
        String[] leftRight = new String[]{"LEFT", "RIGHT"};
        String[] upDown = new String[]{"UP", "DOWN"};
        arrowsDirection = (centralArrowDirection.equals("HORIZONTAL") ? leftRight[random.nextInt
                (leftRight.length)] : upDown[random.nextInt(upDown.length)]);
    }

    //It will make all the other arrows visible and with arrowsDirection value
    private void fillTable() {
        String text;
        switch (arrowsDirection) {
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
                //Central cell must not be touched
                if (i == GRID_ROWS / 2 && j == GRID_COLUMNS / 2) {
                    if (centralArrowDirection.equals("HORIZONTAL"))
                        textView.setText(random.nextFloat() < .5 ? getString(R.string
                                .fa_arrow_right) :
                                getString(R
                                        .string
                                        .fa_arrow_left));
                    else
                        textView.setText(random.nextFloat() < .5 ? getString(R.string
                                .fa_arrow_up) :
                                getString(R
                                        .string
                                        .fa_arrow_down));
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

    /*public void restart(View view) {
        counter = 0;
        TextView counterText = (TextView) findViewById(R.id.counter);
        counterText.setText("Counter: " + counter);
        drawGrid(null);
    }*/
}
