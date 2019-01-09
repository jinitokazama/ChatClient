package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import group8.tcss450.uw.edu.chatclient.model.Credentials;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

/**
 * Launcher activity.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class SignInActivity extends AppCompatActivity implements
        LoginFragment.OnLoginFragmentInteractionListener,
        RegisterFragment.OnRegisterFragmentInteractionListener,
        RegisterResultFragment.OnVerifyFragmentInteractionListener,
        ResetPasswordFragment.OnFragmentInteractionListener{

    private Credentials mCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get app color theme
        SharedPreferences themePrefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        int theme = themePrefs.getInt("colorTheme", 1);
        // apply app theme to activity
        if( theme == 1) {
            setTheme(R.style.BlueAndOragneAppTheme);
        } else if (theme == 2) {
            setTheme(R.style.GreenAndAmberAppTheme);
        } else if (theme == 3) {
            setTheme(R.style.RedAndBlueAppTheme);
        } else if (theme == 4) {
            setTheme(R.style.BrownAndPinkAppTheme);
        } else {
            Log.wtf("SignInActivity", "Why is the theme option set to " + Integer.toString(theme)+ "?!?!");
        }

        setContentView(R.layout.activity_sign_in);
        //setContentView(R.layout.activity_home);

        if(savedInstanceState == null) {
            if (findViewById(R.id.signinActivity) != null) {
                SharedPreferences prefs =
                        getSharedPreferences(
                                getString(R.string.keys_shared_prefs),
                                Context.MODE_PRIVATE);
                if (prefs.getBoolean(getString(R.string.keys_prefs_stay_logged_in),
                        false)) {
                    //checkStayLoggedIn();
                    loadHome();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.signinActivity, new LoginFragment(),
                                    getString(R.string.keys_fragment_login))
                            .commit();
                }
            }
        }
    }

    /**Builds JSON and starts new AsyncTask to send Login post.*/
    @Override
    public void onLoginAttempt(Credentials cred) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();
        //build the JSONObject
        JSONObject msg = cred.asJSONObject();
        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleLoginPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleLoginPre() {
        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().
                findFragmentByTag(getString(R.string.keys_fragment_login));
        loginFragment.loginClicked();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                checkStayLoggedIn();
                loadHome();
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().
                        findFragmentByTag(getString(R.string.keys_fragment_login));
                loginFragment.loginDone();
                TextView fail = (TextView) findViewById(R.id.loginFailMsg);
                fail.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**Builds JSON and starts new AsyncTask to send first reset_password post.*/
    @Override
    public void onEmailClicked(Credentials cred) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_reset_password_email))
                .build();
        //build the JSONObject
        JSONObject msg = cred.asJSONObject();
        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleResetPre)
                .onPostExecute(this::handleResetEmailPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleResetPre() {
        ResetPasswordFragment resetFragment = (ResetPasswordFragment) getSupportFragmentManager().
                findFragmentByTag(getString(R.string.reset_pass_tag));
        resetFragment.handleProgressStart();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleResetEmailPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            ResetPasswordFragment resetFragment = (ResetPasswordFragment) getSupportFragmentManager().
                    findFragmentByTag(getString(R.string.reset_pass_tag));

            if (success) {
                resetFragment.handleEmailSuccess();
            } else {
                resetFragment.handleEmailFail();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**Builds JSON and starts new AsyncTask to send second reset_password post.*/
    @Override
    public void onCodeClicked(Credentials cred) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_reset_password_code))
                .build();
        //build the JSONObject
        JSONObject msg = cred.asJSONObject();
        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleResetPre)
                .onPostExecute(this::handleResetCodePost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleResetCodePost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            ResetPasswordFragment resetFragment = (ResetPasswordFragment) getSupportFragmentManager().
                    findFragmentByTag(getString(R.string.reset_pass_tag));
            if (success) {
                resetFragment.handleCodeSuccess();
            } else {
                Log.d("Password Reset Error: ", result);
                resetFragment.handleCodeFail();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**Transitions to the registerFragment.*/
    @Override
    public void onRegisterClicked() {
        RegisterFragment registerFragment = new RegisterFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.signinActivity, registerFragment)
                .addToBackStack("registerFrag");
        transaction.commit();
    }

    /**Transitions to the ResetPasswordFragment.*/
    @Override
    public void onResetPasswordClicked() {
        ResetPasswordFragment resetFragment = new ResetPasswordFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.signinActivity, resetFragment, getString(R.string.reset_pass_tag))
                .addToBackStack(null);
        transaction.commit();
    }

    /**Builds JSON and starts new AsyncTask to send Registration post.*/
    @Override
    public void onRegisterAttempt(Credentials cred) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .build();
        //build the JSONObject
        JSONObject msg = cred.asJSONObject();
        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleRegisterOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**Checks if Stay Logged In is selected and saves to SharedPreferences if needed.*/
    private void checkStayLoggedIn() {
        if (((CheckBox) findViewById(R.id.logCheckBox)).isChecked()) {
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //save the username for later usage
            prefs.edit().putString(
                    getString(R.string.keys_prefs_username),
                    mCredentials.getUsername())
                    .apply();
            //save the users “want” to stay logged in
            prefs.edit().putBoolean(
                    getString(R.string.keys_prefs_stay_logged_in),
                    true)
                    .apply();
        }
    }

    /**Transitions to the HomeActivity.*/
    public void loadHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        Bundle b = new Bundle();
        EditText t = (EditText) findViewById(R.id.logUsernnameText);
        String s;
        if (t == null) {
            SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            s = prefs.getString("username", "");
        } else {
            s = t.getText().toString();
        }
        b.putString("username", s);
        intent.putExtras(b);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            loadRegisterResult(success);
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**Loads the RegisterResultFragment.*/
    public void loadRegisterResult(boolean success) {
        //getSupportFragmentManager().popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        RegisterResultFragment resultFragment = new RegisterResultFragment();
        Bundle args = new Bundle();
        args.putBoolean("result", success);
        args.putString("username", mCredentials.getUsername());
        args.putString("email", mCredentials.getEmail());
        resultFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.signinActivity, resultFragment).addToBackStack(null);
        transaction.commit();
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }

    @Override
    public void onVerifyAttempt(String userName, String userEmail, String code) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_verify))
                .build();
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", userName);
            msg.put("email", userEmail);
            msg.put("code", code);
        } catch (JSONException e) {
            Log.wtf("VERIFICATION", "Error creating JSON: " + e.getMessage());
        }
//        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleVerifyOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    @Override
    public void onResendCode(String userName, String userEmail) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_resend_code))
                .build();
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", userName);
            msg.put("email", userEmail);
        } catch (JSONException e) {
            Log.wtf("RESEND VERIFICATION CODE", "Error creating JSON: " + e.getMessage());
        }

        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleResendOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleResendOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                if(findViewById(R.id.resendVerificationButton) != null) {
                    Button b = (Button) findViewById(R.id.resendVerificationButton);
                    b.setText(getString(R.string.signup_email_sent));
                }
            } else {
                TextView t = (TextView) findViewById(R.id.resultDisplayMsg);
                t.setText(resultsJSON.getString("message"));
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    private void handleVerifyOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                loadVerificationResult();
            } else {
                TextView t = (TextView) findViewById(R.id.resultDisplayMsg);
                t.setText(resultsJSON.getString("message"));
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    private void loadVerificationResult() {
        if(findViewById(R.id.verifyButton) != null
                && findViewById(R.id.verificationCodeInput) != null
                && findViewById(R.id.resendVerificationButton) != null) {
            EditText verificationInput = (EditText) findViewById(R.id.verificationCodeInput);
            Button verifyButton = (Button) findViewById(R.id.verifyButton);
            Button resendButton = (Button) findViewById(R.id.resendVerificationButton);
            verificationInput.setEnabled(false);
            verifyButton.setText(getString(R.string.signin_verify));
            verifyButton.setEnabled(false);
            resendButton.setText(getString(R.string.signin_resend));
            resendButton.setOnClickListener(this::goBackToLogin);
        }
    }

    private void goBackToLogin(View view) {
        getSupportFragmentManager().popBackStack ("registerFrag", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        LoginFragment login = new LoginFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.signinActivity, login)
                .addToBackStack(null);
        transaction.commit();
    }
}