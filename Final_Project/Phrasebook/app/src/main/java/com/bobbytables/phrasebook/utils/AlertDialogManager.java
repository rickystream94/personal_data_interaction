package com.bobbytables.phrasebook.utils;

/**
 * Created by ricky on 15/03/2017.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogManager {
    /**
     * Function to display simple Alert Dialog
     *
     * @param context - application context
     * @param title   - alert dialog title
     * @param message - alert message
     * @param status  - success/failure (used to set icon)
     *                - pass null if you don't want icon
     */
    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        //Set dismiss button
        alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (status != null)
            // Setting alert dialog icon
            alertDialog.setIcon((status) ? android.R.drawable.star_big_on : android.R.drawable
                    .ic_dialog_alert);

        // Showing Alert Message
        alertDialog.show();
    }
}
