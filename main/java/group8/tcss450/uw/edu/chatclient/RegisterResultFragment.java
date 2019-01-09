package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 *
 *  @author Eric Harty - hartye@uw.edu
 *  @author Phu Lam
 */
public class RegisterResultFragment extends Fragment {
    // Listener for the fragment's interaction
    private OnVerifyFragmentInteractionListener mListener;
    // The verify button
    private Button verifyButton;
    // The resend button
    private Button resendButton;
    // The view that holds the user's input verification code
    private EditText codeInput;
    // The user's username
    private String userName;
    // The user's email
    private String email;

    public RegisterResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register_result, container, false);

        verifyButton = (Button) v.findViewById(R.id.verifyButton);
        resendButton = (Button) v.findViewById(R.id.resendVerificationButton);
        codeInput = (EditText) v.findViewById(R.id.verificationCodeInput);

        // Retrieve the user's information from previous fragment, which can only be register fragment
        if(getArguments() != null){
            boolean success = getArguments().getBoolean("result");

            userName = getArguments().getString("username");
            email = getArguments().getString("email");

            TextView resultMsg = (TextView) v.findViewById(R.id.resultDisplayMsg);
            TextView enterCodeMessage = (TextView) v.findViewById(R.id.enterCodeMessage);


            if(success){
                resultMsg.setText(String.format(getString(R.string.register_verify), email));
                verifyButton.setVisibility(View.VISIBLE);
                resendButton.setVisibility(View.VISIBLE);
                codeInput.setVisibility(View.VISIBLE);
                enterCodeMessage.setVisibility(View.VISIBLE);
            } else{
                resultMsg.setText(getString(R.string.register_fail_msg));
                verifyButton.setVisibility(View.INVISIBLE);
                resendButton.setVisibility(View.INVISIBLE);
                codeInput.setVisibility(View.INVISIBLE);
                enterCodeMessage.setVisibility(View.INVISIBLE);
            }
        }

        verifyButton.setOnClickListener(this::onClick);
        resendButton.setOnClickListener(this::onClickResend);

        return v;
    }

    /**
     * Handles actions when resend button is clicked.
     */
    private void onClickResend(View view) {
        if (mListener != null) {
            mListener.onResendCode(userName, email);
        }
    }

    /**
     * Handle actions when verify button is clicked.
     */
    private void onClick(View view) {
        if (mListener != null) {

            String code = codeInput.getText().toString();
            boolean good = true;

            //Client side checks here
            if(code.length() == 0 ){
                codeInput.setError("Please input your code");
                good = false;
            }else{
                //Uses regex to check for <>@<>.XXX email addresses
                Pattern pattern = Pattern.compile("[0-9][0-9][0-9][0-9]");
                Matcher mat = pattern.matcher(code);
                if(!mat.matches()){
                    codeInput.setError("Verification code has to be consisted of 4 numbers");
                    good = false;
                }
            }
            if(good){
                mListener.onVerifyAttempt(userName, email, code);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegisterFragment.OnRegisterFragmentInteractionListener) {
            mListener = (RegisterResultFragment.OnVerifyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface to implement appropriate action when getting contacts for RegisterResultFragment.
     */
    public interface OnVerifyFragmentInteractionListener {
        void onVerifyAttempt(String userName, String userEmail, String code);
        void onResendCode(String userName, String userEmail);
    }

}
