package in.ureport.managers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import in.ureport.R;
import in.ureport.activities.LoginActivity;
import in.ureport.models.CountryProgram;

/**
 * Created by johncordeiro on 21/07/15.
 */
public class UserManager {

    public static boolean userLoggedIn = false;
    public static String countryCode = "";

    public static boolean validateKeyAction(Context context) {
        if(!UserManager.userLoggedIn) {
            showLoginAlertValidation(context);
            return false;
        } else if(isUserCountryProgram()) {
            showCountryProgramAlert(context);
            return false;
        }
        return true;
    }

    private static boolean isUserCountryProgram() {
        return countryCode == null || countryCode.length() == 0
        || !CountryProgramManager.getCurrentCountryProgram().equals(CountryProgramManager.getCountryProgramForCode(countryCode));
    }

    private static void showCountryProgramAlert(Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.country_program_validation_error))
                .setPositiveButton(R.string.confirm_neutral_dialog_button, null)
                .create();
        alertDialog.show();
    }

    private static void showLoginAlertValidation(final Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(R.string.login_validation_error_message)
                .setNegativeButton(R.string.cancel_dialog_button, null)
                .setPositiveButton(R.string.confirm_neutral_dialog_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent loginIntent = new Intent(context, LoginActivity.class);
                        context.startActivity(loginIntent);
                    }
                }).create();
        alertDialog.show();
    }

}
