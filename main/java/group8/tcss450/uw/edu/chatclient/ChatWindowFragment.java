package group8.tcss450.uw.edu.chatclient;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.ListenManager;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * contains the chat session with messages
 *
 * @author Jin Byoun - jinito@uw.edu
 * @author Eric Harty - hartye@uw.edu
 * @author Phu Lam Pham
 *
 */
public class ChatWindowFragment extends Fragment {

    /* Adapter for populating the chat messages. */
    private ChatWindowFragment.ChatArrayAdapter chatArrayAdapter;
    /* The list of chat messages. */
    private ListView listView;
    /* User's input text message */
    private EditText chatText;
    /* The send message button */
    private Button buttonSend;

    /* MIGHT CHANGE LATER
       For now use to find left or right display messages.
    */
    private boolean side = false;

    private String mUsername;
    private String mSendUrl;

    ScrollView mScrollView;

    private int mChatId;

    private TextView mOutputTextView;
    private ListenManager mListenManager;

    private Bundle bundle;

    public ChatWindowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_window, container, false);
        setHasOptionsMenu(true);

        v.findViewById(R.id.chatOptionsImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatInnerFragment nextFrag= new ChatInnerFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)(getView().getParent())).getId(), nextFrag)
                        .addToBackStack(null)
                        .commit();
            }
        });

        v.findViewById(R.id.chatSendButton).setOnClickListener(this::sendMessage);
        mOutputTextView = (TextView) v.findViewById(R.id.chatOutputTextView);

        HomeActivity homeAcivity = (HomeActivity) getActivity();
        mUsername = homeAcivity.mUsername;

        System.out.println("username in ChatWindow Fragment is: " + mUsername);

        mScrollView = (ScrollView) v.findViewById(R.id.chatOutputScrollView);



        return v;
    }


    // sends messages to the chat
    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatWindowFragment.ChatMessage(side, chatText.getText().toString()));
        chatText.setText("");
        side = !side;
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        bundle = this.getActivity().getIntent().getExtras();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
//            throw new IllegalStateException("No username in prefs!");
            mUsername = bundle.getString("username", "");
        } else {
            mUsername = prefs.getString("username", "");
        }

//        bundle = this.getActivity().getIntent().getExtras();
//        int chatId = bundle.getInt("chatId");
        HomeActivity homeAcivity = (HomeActivity) getActivity();
        mChatId = homeAcivity.getCurrentChatId();
        String stringChatId = Integer.toString(mChatId);

        System.out.println("bundle chatid in ChatWindowsFragment is: " + mChatId);

        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_message))
                .build()
                .toString();

        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_message))
                .appendQueryParameter("chatId", stringChatId)
                .build();







        mListenManager = new ListenManager.Builder(retrieve.toString(),
                this::publishProgress)
                .setExceptionHandler(this::handleError)
                .setDelay(1000)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        mOutputTextView.setText("");
        mListenManager.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        String latestMessage = mListenManager.stopListening();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Save the most recent message timestamp
        prefs.edit().putString(
                getString(R.string.keys_prefs_time_stamp),
                latestMessage)
                .apply();
    }




    private void sendMessage(final View theButton) {
        JSONObject messageJson = new JSONObject();
        String msg = ((EditText) getView().findViewById(R.id.chatInputEditText))
                .getText().toString();




        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), msg);


            messageJson.put(getString(R.string.keys_chatId), mChatId);

            // make it so that it sends messages to the chat that you are in, not always global id
            //messageJson.put(getString(R.string.keys_json_chat_id), 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(this::handleSendMsgError)
                .build().execute();
        //set scroll view to show most recent message.
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void handleSendMsgError(final String msg) {
        Log.e("CHAT ERROR!!!", msg.toString());
    }

    private void endOfSendMsgTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
                ((EditText) getView().findViewById(R.id.chatInputEditText))
                        .setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    private void publishProgress(JSONObject messages) {
        final String[] msgs;
        if(messages.has(getString(R.string.keys_json_messages))) {
            try {
                JSONArray jMessages =
                        messages.getJSONArray(getString(R.string.keys_json_messages));
                msgs = new String[jMessages.length()];
                for (int i = 0; i < jMessages.length(); i++) {
                    JSONObject msg = jMessages.getJSONObject(i);
                    String username =
                            msg.get(getString(R.string.keys_json_username)).toString();
                    String userMessage =
                            msg.get(getString(R.string.keys_json_message)).toString();
                    msgs[i] = username + ":" + userMessage;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            getActivity().runOnUiThread(() -> {
                for (String msg : msgs) {
                    mOutputTextView.append(msg);
                    mOutputTextView.append(System.lineSeparator());
                }

            });
        }
    }

    public class ChatMessage {
        public boolean left;
        public String message;

        public ChatMessage(boolean left, String message) {
            super();
            this.left = left;
            this.message = message;
        }
    }

    class ChatArrayAdapter extends ArrayAdapter<ChatWindowFragment.ChatMessage> {

        private TextView chatText;
        private List<ChatWindowFragment.ChatMessage> chatMessageList = new ArrayList<ChatWindowFragment.ChatMessage>();
        private Context context;

        @Override
        public void add(ChatWindowFragment.ChatMessage object) {
            chatMessageList.add(object);
            super.add(object);
        }

        public ChatArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.context = context;
        }

        public int getCount() {
            return this.chatMessageList.size();
        }

        public ChatWindowFragment.ChatMessage getItem(int index) {
            return this.chatMessageList.get(index);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ChatWindowFragment.ChatMessage chatMessageObj = getItem(position);
            View row = convertView;
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatMessageObj.left) {
                row = inflater.inflate(R.layout.chat_right, parent, false);
            }else{
                row = inflater.inflate(R.layout.chat_left, parent, false);
            }

            // For now display time, use it later to display user name and time
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();

            chatText = (TextView) row.findViewById(R.id.senderInfo);
            chatText.setText(formatter.format(date));
            chatText = (TextView) row.findViewById(R.id.message);
            chatText.setText(chatMessageObj.message);
            return row;
        }
    }
}
