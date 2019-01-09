package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * Fragment that displays the user's contact list.
 *
 * @author Phu Lam
 */
public class ConnectionsFragment extends Fragment {

    // The view of list of connections/contacts
    private ListView connectionList;
    // The adapter for connection list
    private ConnectionsAdapter connectionAdapter;
    // The connection list data structure
    private ArrayList<Connection> connectionListData = new ArrayList<>();
    // The view of the search box
    private SearchView searchView;
    // The user's username
    private String userName;
    // Listener for the fragment's interaction
    private ConnectionsFragmentInteractionListener mListener;
    // The list of currently selected contacts
    private HashSet<Connection> currentSelectedConnections = new HashSet<>();
    // The URL of end point for getting contacts
    private String mCreateChatUrl;

    public ConnectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        // Grab the user's username passed from SignIn Activity
        Bundle b = this.getActivity().getIntent().getExtras();
        if(b != null) {
            userName = b.getString("username");
        }

        v.findViewById(R.id.createMultichatButton).setOnClickListener(this::createChat);

        mCreateChatUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_create_multichat))
                .build()
                .toString();

        // Initialize views of the fragment and their respective onClick/adapter functions.
        connectionList = (ListView) v.findViewById(R.id.connectionList);
        connectionAdapter = new ConnectionsAdapter(v.getContext(), connectionListData);
        connectionList.setAdapter(connectionAdapter);
        searchView=(SearchView) v.findViewById(R.id.searchBox);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                connectionAdapter.getFilter().filter(text);
//                connectionAdapter.notifyDataSetChanged();
                return false;
            }
        });


        if (mListener != null) {
            mListener.onGetContactsAttempt(userName, connectionListData, connectionAdapter);
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Create an asynchronous call to webservice to create a new chat.
     */
    private void createChat(final View theButton) {
        JSONObject messageJson = new JSONObject();
        JSONArray arrayJson = new JSONArray();
        String chatName = ((EditText) getView().findViewById(R.id.inputChatName))
                .getText().toString();

        if (chatName.length() == 0) {
            System.out.println("Create Toast");
            Toast.makeText(getActivity(),"Please enter valid chat name", Toast.LENGTH_SHORT).show();
        } else {
            System.out.println("Create Chat");
            android.content.SharedPreferences prefs =
                    getActivity().getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);

            String mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");

            try {

                for (Connection s : currentSelectedConnections) {
                    arrayJson.put(s.userName);
                }
                //System.out.println("Array of usernames is: " + arrayJson.toString());
                messageJson.put(getString(R.string.keys_json_current_username), mUsername);
                messageJson.put(getString(R.string.keys_json_checkbox_contacts_array), arrayJson);
                messageJson.put(getString(R.string.keys_json_chat_name), chatName);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(messageJson.toString());



            new SendPostAsyncTask.Builder(mCreateChatUrl, messageJson)
                    .onPostExecute(this::endOfCreateChatTask)
                    .onCancelled(this::handleError)
                    .build().execute();
        }


    }

    /**
     * Handle error when asynchronous call failed.
     * @param msg the error message
     */
    private void handleError(final String msg) {
        Log.e("new chat creation from checks ERROR!!!", msg.toString());
    }

    /**
     * Handle the end of the asynchronous call and update UI accordingly.
     * @param result
     */
    private void endOfCreateChatTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            int chatId = res.getInt("chatId");

            ((EditText) getView().findViewById(R.id.inputChatName))
                        .setText("");

            HomeActivity homeActivity = (HomeActivity) getActivity();
            homeActivity.mCurrentChatId = chatId;
            homeActivity.loadFragment(new ChatWindowFragment());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectionsFragmentInteractionListener) {
            mListener = (ConnectionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConnectionsFragmentInteractionListener");
        }
    }

    /**
     * Data structure class representing a contact/connection.
     *
     * @author Phu Lam
     */
    public static class Connection {
        // The connection's userId
        private String userId;
        // The connection's first name
        private String firstName;
        // The connection's last name
        private String lastName;
        // The connection's email
        private String email;
        // The connection's username
        public String userName;

        public Connection(String userId, String firstName, String lastName, String email, String userName) {
            this.userName = userName;
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }

    /**
     * Custom adapter that handles the creating of each connection's UI, their data,
     * and filtering functionality.
     *
     * @author Phu Lam
     */
    public class ConnectionsAdapter extends ArrayAdapter<Connection> {
        private Context mContext;
        private List<Connection> mList = new ArrayList<Connection>();
        private List<Connection> mFilterList = new ArrayList<Connection>();

        public ConnectionsAdapter(Context context, ArrayList<Connection> list) {
            super(context, 0, list);

            System.out.println("Input list is: " + list.toString());
            mContext = context;
            mList = list;
            mFilterList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.connection_list_item, parent, false);
            }

            Connection currentItem = mFilterList.get(position);

            CheckedTextView itemName = (CheckedTextView) listItem.findViewById(R.id.connectionListItemName);
            itemName.setText(currentItem.firstName + " " + currentItem.lastName);
            CheckBox itemCheckBox = (CheckBox) listItem.findViewById(R.id.connectionListItemCheckBox);
            itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        currentSelectedConnections.add(currentItem);
                    } else {
                        if (currentSelectedConnections.contains(currentItem)) {
                            currentSelectedConnections.remove(currentItem);
                        }
                    }
                    for (Connection s : currentSelectedConnections) {
                        System.out.println(s.email);
                    }
                }
            });

            return listItem;
        }

        /**
         * Return the size of current connection list (dynamically with filtering)
         */
        public int getCount() {
            return mFilterList.size();
        }

        /**
         * Return an item in the connection list (dynamically with filtering)
         * @param position the index of the item
         * @return the item
         */
        public Connection getItem(int position) {
            return mFilterList.get(position);
        }

        @Override
        /**
         * Custom getFilter() method that to help better filter the list that works
         * with our Connection data structure.
         */
        public Filter getFilter() {
            return new Filter() {
                protected FilterResults performFiltering(CharSequence constraint) {
                    String filterString = constraint.toString().toLowerCase();
                    FilterResults results = new FilterResults();
                    final List<Connection> list = mList;
                    int count = list.size();
                    final List<Connection> nlist = new ArrayList<Connection>(count);
                    Connection filterableConnection;
                    for (int i = 0; i < count; i++) {
                        filterableConnection = list.get(i);
                        if ((filterableConnection.firstName + " " + filterableConnection.lastName).toLowerCase().contains(filterString)) {
                            nlist.add(filterableConnection);
                        }
                    }
                    results.values = nlist;
                    results.count = nlist.size();
                    System.out.println(results.values.toString() + "\n" + results.count + " " + nlist.size());
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mFilterList = (List<Connection>) results.values;
                    connectionAdapter.notifyDataSetChanged();
                }
            };
        }

    }

    /**
     * Interface to implement appropriate action when getting contacts for ConnectionFragment.
     */
    public interface ConnectionsFragmentInteractionListener {
        void onGetContactsAttempt(String userName, ArrayList<Connection> data, ConnectionsAdapter adapter);
    }
}
