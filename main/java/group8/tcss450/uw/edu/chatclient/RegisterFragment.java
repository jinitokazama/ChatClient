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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import group8.tcss450.uw.edu.chatclient.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnRegisterFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    private OnRegisterFragmentInteractionListener mListener;
    private View mView;

    public RegisterFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_registration, container, false);

        Button b = (Button) mView.findViewById(R.id.registerButton2);
        b.setOnClickListener(this);

        return mView;
    }

    /**Performs client side checks on registration information.*/
    @Override
    public void onClick(View view) {
        if (mListener != null) {

            EditText emailText = (EditText) mView.findViewById(R.id.regEmailText);
            String email = emailText.getText().toString();
            EditText firstText = (EditText) mView.findViewById(R.id.regFirstNameText);
            String first = firstText.getText().toString();
            EditText lastText = (EditText) mView.findViewById(R.id.regLastNameText);
            String last = lastText.getText().toString();
            EditText userText = (EditText) mView.findViewById(R.id.regUsernameText);
            String username = userText.getText().toString();
            EditText passText = (EditText) mView.findViewById(R.id.regPassText);
            Editable password = passText.getText();
            EditText copyText = (EditText) mView.findViewById(R.id.regPassText2);
            Editable passcopy = copyText.getText();
            boolean good = true;

            //Client side checks here
            if(email.length() == 0 || first.length() == 0 || last.length() == 0 ||
                    username.length() == 0 || password.length() == 0 || passcopy.length() == 0 ){
                userText.setError("All fields must be filled");
                good = false;
            }else{
                //Uses regex to check for <>@<>.XXX email addresses
                Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
                Matcher mat = pattern.matcher(email);
                if(!mat.matches()){
                    emailText.setError("Must have valid email form");
                    good = false;
                }
                if(username.length() < 3){
                    userText.setError("Username must be more than 2 chars in length");
                    good = false;
                }
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

                Credentials cred = new Credentials.Builder(username, password)
                        .addEmail(email).addFirstName(first).addLastName(last)
                        .build();
                mListener.onRegisterAttempt(cred);
            }

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentInteractionListener) {
            mListener = (OnRegisterFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRegisterFragmentInteractionListener");
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
    public interface OnRegisterFragmentInteractionListener {
        void onRegisterAttempt(Credentials cred);
    }
}
