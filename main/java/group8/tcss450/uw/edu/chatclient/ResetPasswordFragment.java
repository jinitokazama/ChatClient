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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import group8.tcss450.uw.edu.chatclient.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResetPasswordFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class ResetPasswordFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View mView;
    private String mEmail;
    private ProgressBar mProgressBar;
    private EditText mCodeView;
    private EditText mPassView;
    private EditText mCopyView;
    private Button mSubmitButton;
    private Button mSubmitCodeButton;
    private TextView mResultMsg;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_reset_password, container, false);

        mProgressBar = (ProgressBar) mView.findViewById(R.id.resetPasswordProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mCodeView = (EditText) mView.findViewById(R.id.resetCodeEditText);
        mCodeView.setVisibility(View.GONE);
        mPassView = (EditText) mView.findViewById(R.id.resetPasswordEditText);
        mPassView.setVisibility(View.GONE);
        mCopyView = (EditText) mView.findViewById(R.id.resetCopyEditText);
        mCopyView.setVisibility(View.GONE);
        mResultMsg= (TextView) mView.findViewById(R.id.resetMessageTextView);

        mSubmitButton = (Button) mView.findViewById(R.id.resetAttemptButton);
        mSubmitButton.setOnClickListener(this::onEmailClick);

        mSubmitCodeButton = (Button) mView.findViewById(R.id.resetSubmitCodeButton);
        mSubmitCodeButton.setOnClickListener(this::onCodeClick);
        mSubmitCodeButton.setVisibility(View.GONE);

        return mView;
    }

    /**onClick for the first/email button that submits the email to be sent to*/
    public void onEmailClick(View view) {
        if (mListener != null) {
            EditText emailText = (EditText) mView.findViewById(R.id.resetEmailEditText);
            mEmail = emailText.getText().toString();
            boolean good = true;

            Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
            Matcher mat = pattern.matcher(mEmail);
            if(!mat.matches()){
                emailText.setError("Must have valid email form");
                good = false;
            }
            if(good){
                Credentials cred = new Credentials.Builder(null, null)
                        .addEmail(mEmail)
                        .build();
                mListener.onEmailClicked(cred);
            }
        }
    }

    /**onClick for the second/code button that submits the code and password*/
    public void onCodeClick(View view) {
        if (mListener != null) {
            boolean good = true;

            EditText passText = (EditText) mView.findViewById(R.id.resetPasswordEditText);
            Editable password = passText.getText();
            EditText copyText = (EditText) mView.findViewById(R.id.resetCopyEditText);
            Editable passcopy = copyText.getText();

            EditText codeText = (EditText) mView.findViewById(R.id.resetCodeEditText);
            String code= codeText.getText().toString();

            if(code.length() < 4|| password.length() == 0 || passcopy.length() == 0 ){
                codeText.setError("All fields must be filled");
                good = false;
            }else{
                if(password.length() < 6 || passcopy.length() < 6){
                    passText.setError("Password must be more than 5 chars in length");
                    good = false;
                }
                if(password.length() == passcopy.length()){
                    for(int i = 0; i < password.length(); i++){
                        if(password.charAt(i) != passcopy.charAt(i)){
                            passText.setError("Passwords must match");
                            good = false;
                            break;
                        }
                    }
                }
            }
            if(good){
                Credentials cred = new Credentials.Builder(code, password)
                        .addEmail(mEmail)
                        .build();
                mListener.onCodeClicked(cred);
            }
        }
    }

    /**Starts the progress bar and disables buttons*/
    public void handleProgressStart() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressBar.setProgress(0);
        mSubmitButton.setEnabled(false);
        mSubmitCodeButton.setEnabled(false);
    }

    /**Handle failure on the first post*/
    public void handleEmailFail() {
        mProgressBar.setVisibility(ProgressBar.GONE);
        mResultMsg.setText(R.string.reset_message_failed);
        mResultMsg.setVisibility(TextView.VISIBLE);
        mSubmitButton.setEnabled(true);
    }

    /**Handle success on the first post*/
    public void handleEmailSuccess() {
        mProgressBar.setVisibility(ProgressBar.GONE);
        mResultMsg.setText(R.string.reset_message_code);
        mResultMsg.setVisibility(View.VISIBLE);
        mSubmitButton.setEnabled(false);
        mSubmitButton.setVisibility(View.GONE);
        mSubmitCodeButton.setEnabled(true);
        mSubmitCodeButton.setVisibility(View.VISIBLE);
        mCopyView.setVisibility(View.VISIBLE);
        mPassView.setVisibility(View.VISIBLE);
        mCodeView.setVisibility(View.VISIBLE);
    }

    /**Handle failure on the second post*/
    public void handleCodeFail() {
        mProgressBar.setVisibility(ProgressBar.GONE);
        mResultMsg.setText(R.string.reset_message_bad_code);
        mResultMsg.setVisibility(View.VISIBLE);
    }

    /**Handle success on the second post*/
    public void handleCodeSuccess() {
        mProgressBar.setVisibility(ProgressBar.GONE);
        mResultMsg.setText(R.string.reset_message_success);
        mResultMsg.setVisibility(View.VISIBLE);
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        void onEmailClicked(Credentials cred);
        void onCodeClicked(Credentials cred);
    }
}
