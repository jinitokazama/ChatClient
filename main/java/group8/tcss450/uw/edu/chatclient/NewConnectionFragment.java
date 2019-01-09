package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import group8.tcss450.uw.edu.chatclient.utils.ListenManager;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * make a new connection from the home screen navigation drawer
 *
 * @author Jin Byoun - jinito@uw.edu
 */
public class NewConnectionFragment extends Fragment {

    private String mUsername;
    private String mSendUrl;
    private TextView mOutputTextView;
    private ListenManager mListenManager;

    public NewConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_new_connection, container, false);
        v.findViewById(R.id.newConnectionRequestButton).setOnClickListener(this::sendRequest);
        return v;
    }


    // sends the connection request
    private void sendRequest(final View theButton) {
        Log.e("test1", "gets to sendRequest");
        JSONObject messageJson = new JSONObject();
        String newConnection = ((EditText) getView().findViewById(R.id.newConnectionUsernameInputEditText))
                .getText().toString();
        try {
            messageJson.put(getString(R.string.keys_json_current_username), mUsername);
            messageJson.put(getString(R.string.keys_json_connection_username), newConnection);
            //messageJson.put(getString(R.string.keys_json_connection_verification), 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendRequestTask)
                .onCancelled(this::handleError)
                .build().execute();
    }
    private void handleError(final String msg) {
        Log.e("new Connections ERROR!!!", msg.toString());
    }

    private void endOfSendRequestTask(final String result) {
        Log.e("test2", "gets to endofSendRequestTask");
        try {
            JSONObject res = new JSONObject(result);
            Log.e("test", "gets to try part of endofSendRequestTask");
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
                ((EditText) getView().findViewById(R.id.newConnectionUsernameInputEditText))
                        .setText("");
                Log.e("test3", "gets to success, should make toast");
                Toast.makeText(getActivity(),"Connection Request Sent!",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e("test4", "does not get to success");
            e.printStackTrace();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs for new connection!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_request))
                .build()
                .toString();
    }
}
