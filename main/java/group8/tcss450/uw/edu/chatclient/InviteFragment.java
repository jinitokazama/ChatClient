package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import group8.tcss450.uw.edu.chatclient.R;

/**
 * Fragment for inviting and sending email to friend.
 *
 * @author Phu Lam
 */
public class InviteFragment extends Fragment {
    // The user's username
    private String mUserName;
    // The text input of the friend's name
    private EditText mFriendName;
    // The text input of the friend's email
    private EditText mFriendEmail;
    // The send button
    private Button sendButton;
    // Listener for the fragment's interaction
    private InviteFragmentInteractionListener mListener;
    // The fragment's name
    private static final String TAG = "InviteFragment";

    public InviteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invite, container, false);
        SharedPreferences prefs = getActivity().getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        mUserName = prefs.getString(getString(R.string.keys_prefs_username), "Problem! No Username in " + TAG + "!");

        mFriendName = (EditText) v.findViewById(R.id.friendName);
        mFriendEmail = (EditText) v.findViewById(R.id.friendEmail);
        sendButton = (Button) v.findViewById(R.id.inviteSendButton);
        sendButton.setOnClickListener(this::onClick);


        return v;
    }

    /**
     * Handle actions when send button is clicked
     */
    private void onClick(View view) {
        String friendName = mFriendName.getText().toString();
        String friendEmail = mFriendEmail.getText().toString();
        boolean good = true;
        if (friendName.length() == 0) {
            mFriendName.setError("Name cannot be empty.");
            good = false;
        }
        if (friendEmail.length() == 0) {
            mFriendEmail.setError("Email cannot be empty.");
            good = false;
        }

        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
        Matcher mat = pattern.matcher(friendEmail);
        if(!mat.matches()){
            mFriendEmail.setError("Must have valid email form");
            good = false;
        }

        if(good && mListener != null) {
            mListener.onInviteAttempt(mUserName, friendName, friendEmail);
        }
    }

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InviteFragmentInteractionListener) {
            mListener = (InviteFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InviteFragmentInteractionListener");
        }
    }

    /**
     * Interface to implement appropriate action when getting contacts for InviteFragment.
     */
    public interface InviteFragmentInteractionListener {
        void onInviteAttempt(String userName, String friendName, String friendEmail);
    }

}
