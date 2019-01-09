package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.model.Credentials;
import group8.tcss450.uw.edu.chatclient.utils.ChatListenManager;
import group8.tcss450.uw.edu.chatclient.utils.MessagesIntentService;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private static final String TAG = "ChatListFragment";

    private ArrayList<ChatListItem> mData = new ArrayList<ChatListItem>();
    private ListView mChatList;
    private String mUserName;
    private ChatSessionAdapter mAdapter;
    private ChatListenManager mListenManager;

    private ChatListFragmentInteractionListener mListener;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle b = this.getActivity().getIntent().getExtras();
        if(b != null) {
            mUserName = b.getString("username");
        }

        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mChatList = (ListView) v.findViewById(R.id.chatList);
        mAdapter = new ChatSessionAdapter(v.getContext(), mData);
        mChatList.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ChatListFragmentInteractionListener) {
            mListener = (ChatListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "must implement ChatListFragmentInteractionListener");
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        mData.clear();
        getPopulateList();
        updateTimestamp();
        MessagesIntentService.stopServiceAlarm(getContext());

    }

    @Override
    public void onStop(){
        super.onStop();

        MessagesIntentService.startServiceAlarm(getContext(), true);
//        String lastMessageTime=mListenManager.stopListening();
//        SharedPreferences prefs = getActivity().getSharedPreferences(
//                getString(R.string.keys_shared_prefs),
//                Context.MODE_PRIVATE);
//        prefs.edit().putString(getString(R.string.keys_prefs_messages_time_stamp),
//                lastMessageTime)
//                .apply();

    }

//    public void findChatSessions(){
//        //build the web service URL
//        Uri uri = new Uri.Builder()
//                .scheme("https")
//                .appendPath(getString(R.string.ep_base_url))
//                .appendPath(getString(R.string.ep_my_chats) + "Two")
//                .appendQueryParameter("username", mUserName)
//                .build();
//
//        //open shared preferences
//        SharedPreferences prefs = getActivity().getSharedPreferences(
//                getString(R.string.keys_shared_prefs),
//                Context.MODE_PRIVATE);
//
//        //check shared preferences for chatListTimestamp
//        if(prefs.contains(getString(R.string.keys_prefs_chat_time_stamp))) {
//            //create listen manager to ignore seen messages.
//            Log.d(TAG, "Creating ChatListenManager with timestamp: " );
//            mListenManager = new ChatListenManager.Builder(uri.toString(),
//                    this::populateChatList)
//                    .setExceptionHandler(this::handleExceptionsInListener)
//                    .setTimeStamp(prefs.getString(getString(R.string.keys_prefs_chat_time_stamp), "0"))
//                    .setDelay(1000)
//                    .build();
//
//        } else {
//            //No time stamp in setting. Must be a first time login
//            //The ChatListenManager will assign itself the default timestamp 1970 to get all results.
//            Log.d(TAG, "Creating ChatListenManager without timestamp");
//            mListenManager = new ChatListenManager.Builder(uri.toString(),
//                    this::populateChatList)
//                    .setExceptionHandler(this::handleExceptionsInListener)
//                    .setDelay(1000)
//                    .build();
//        }
//    }

    //get the current time from the server to update last checked time
    public void updateTimestamp(){
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("getTime")
                .build();
        Credentials cred = new Credentials.Builder(mUserName, null).build();
        JSONObject msg = cred.asJSONObject();
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleUpdateTimeStamp)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    public void handleUpdateTimeStamp(String s) {
        try{
            JSONObject results = new JSONObject(s);
            String timestamp = results.getString("time");
            SharedPreferences prefs = getActivity().getSharedPreferences(
                    getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            prefs.edit().putString(getString(R.string.keys_prefs_messages_time_stamp),
                    timestamp)
                    .apply();

        } catch (Exception e){
            Log.d(TAG,"error parsing JSON response from getTime");
        }

    }


    public void handleExceptionsInListener(Exception e) {
        Log.e(TAG + "LISTEN ERROR!!", e.getMessage());
    }

    private void populateChatList(JSONObject resultsJSON) {

        if (getActivity().findViewById(R.id.loadChatListProgressBar) != null) {
            ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.loadChatListProgressBar);
            progressBar.setVisibility(View.GONE);
        }
        try {
            JSONArray array =resultsJSON.getJSONArray("chats");

                for (int i =0; i < array.length(); i++) {
                    JSONObject aChatSession = array.getJSONObject(i);
                    // PARSE JSON RESULTS HERE
                    String chatName = aChatSession.getString("name");
                    Log.d(TAG, "chat name = " + chatName);
                    int chatId = aChatSession.getInt("chatid");

                    mData.add(new ChatListItem(chatName, chatId));
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {

                Log.e("JSON_PARSE_ERROR", "Error when populating Chat List in " + TAG);
            }

    }

    /**
     * Builds JSON and starts new AsyncTask to populate the list
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getPopulateList() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_my_chats))
                .build();
        Credentials cred = new Credentials.Builder(mUserName, null).build();
        JSONObject msg = cred.asJSONObject();
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handlePopulatePost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handlePopulatePost(String jsonResult) {
        try{
            populateChatList(new JSONObject(jsonResult));
        } catch (Exception e){
            Log.d("populate","error casting response String");
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }


    //*******************************************************Inner Classes *************************************

    public class ChatListItem {
        private String mName;
        private int mChatId;

        public ChatListItem(String name, int chatId) {
            this.mName = name;
            this.mChatId = chatId;
        }

        public String getName() {
            return mName;
        }

        public int getId() {
            return mChatId;
        }

    }

    public class ChatSessionAdapter extends ArrayAdapter<ChatListItem> {
        private Context mContext;
        private List<ChatListItem> mList;
        private ArrayList<ChatListItem> mStringFilterList;

        public ChatSessionAdapter(Context context, ArrayList<ChatListItem> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
            mStringFilterList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item, parent, false);
            }

            ChatListItem currentItem = mList.get(position);

            Button button = (Button) listItem.findViewById(R.id.chatSessionButton);
            button.setText(currentItem.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onChatSelected(currentItem.getName(),
                            currentItem.getId());
                    System.out.println("GOes into OnCHatSelected?");
                }
            });

            return listItem;
        }
    }

    public interface ChatListFragmentInteractionListener {
        /**
         * Used to handle a chat being selected from the list
         * Should create an Intent to start the ChatSessionActivity for the specified chat session.
         *
         * @param chatName the name of the chosen chat session.
         * @param chatId the chatId of the chosen chat session.
         */
        void onChatSelected(String chatName, int chatId);
    }
}
