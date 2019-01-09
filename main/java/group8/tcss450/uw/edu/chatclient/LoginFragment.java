package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import group8.tcss450.uw.edu.chatclient.model.Credentials;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLoginFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private OnLoginFragmentInteractionListener mListener;
    private View mView;
    private ProgressBar mProgressBar;
    private Button mLoginButton;
    private Button mResetButton;
    private Button mRegisterButton;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_login, container, false);

        mLoginButton = (Button) mView.findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(this);

        mRegisterButton = (Button) mView.findViewById(R.id.registerButton);
        mRegisterButton.setOnClickListener(this::onRegisterClick);

        mResetButton = (Button) mView.findViewById(R.id.resetPasswordButton);
        mResetButton.setOnClickListener(this::onResetPasswordClick);

        mProgressBar = (ProgressBar) mView.findViewById(R.id.loginProgressBar);
        mProgressBar.setVisibility(View.GONE);

        return mView;
    }

    /**Performs client side checks on login information, if they pass fires onLoginAttempt.*/
    @Override
    public void onClick(View view) {
        if (mListener != null) {
            EditText usernameText = (EditText) mView.findViewById(R.id.logUsernnameText);
            String username = usernameText.getText().toString();
            EditText passText = (EditText) mView.findViewById(R.id.logPasswordText);
            Editable password = passText.getText();
            boolean good = true;

            //Client side checks here
            if(username.length() == 0 || password.length() == 0){
                usernameText.setError("Both fields must be filled");
                good = false;
            }else{

                if(username.length() < 3){
                    usernameText.setError("Username must be at least 3 characters.");
                    good = false;

                }
                if(password.length() < 4){
                    passText.setError("Password must be more than 3 chars in length");
                    good = false;
                }
            }

            if(good){
                Credentials cred = new Credentials.Builder(username, password)
                        .build();
                mListener.onLoginAttempt(cred);
            }
        }
    }

    /**Handles the preExecute view disabling*/
    public void loginClicked() {
        mProgressBar.setVisibility(View.VISIBLE);
        mLoginButton.setEnabled(false);
        mResetButton.setEnabled(false);
        mRegisterButton.setEnabled(false);
    }

    /**Handles the postExecute view enabling*/
    public void loginDone() {
        mProgressBar.setVisibility(View.GONE);
        mLoginButton.setEnabled(true);
        mResetButton.setEnabled(true);
        mRegisterButton.setEnabled(true);
    }

    public void onRegisterClick(View view) {
        if (mListener != null) {
            mListener.onRegisterClicked();
        }
    }

    public void onResetPasswordClick(View view) {
        if (mListener != null) {
            mListener.onResetPasswordClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Allows an external source to set an error message on this fragment. This may
     * be needed if an Activity includes processing that could cause login to fail.
     * @param err the error message to display.
     */
    public void setError(String err) {
        //Log in unsuccessful for reason: err. Try again.
        //you may want to add error stuffs for the user here.
        ((TextView) getView().findViewById(R.id.logUsernnameText))
                .setError("Login Unsuccessful");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoginFragmentInteractionListener {
        void onLoginAttempt(Credentials cred);
        void onRegisterClicked();
        void onResetPasswordClicked();
    }
}