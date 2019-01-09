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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * Fragment for searching for new contacts.
 *
 * @author Phu Lam
 */
public class SearchNewConnectionFragment extends Fragment {

    // The list of searched contacts
    public ArrayList<SearchConnectionListItem> data = new ArrayList<SearchConnectionListItem>();
    // The input containing search keyword
    private EditText searchContactTextView;
    // The search button
    private Button searchContactButton;
    // The view for searched contacts
    private ListView searchContactList;
    // Listener for the fragment's interaction
    private SearchContactFragmentInteractionListener mListener;
    // The user's username
    private String userName;
    // The adapter for searched contact list.
    protected SearchConnectionAdapter adapter;

    public SearchNewConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = this.getActivity().getIntent().getExtras();
        if(b != null) {
            userName = b.getString("username");
        }
        System.out.println("Username at onCreateView: " + userName);
        View v = inflater.inflate(R.layout.fragment_search_new_connection, container, false);
        searchContactTextView = (EditText) v.findViewById(R.id.searchContactTextView);
        searchContactButton = (Button) v.findViewById(R.id.searchContactButton);
        searchContactButton.setOnClickListener(this::onClick);
        searchContactList = (ListView) v.findViewById(R.id.searchContactList);

        ProgressBar searchConnectionProgrsesBar = (ProgressBar) v.findViewById(R.id.searchConnectionProgressBar);
        searchConnectionProgrsesBar.setVisibility(View.GONE);

        adapter = new SearchConnectionAdapter(v.getContext(), data);

        searchContactList.setAdapter(adapter);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SearchNewConnectionFragment.SearchContactFragmentInteractionListener) {
            mListener = (SearchNewConnectionFragment.SearchContactFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SearchContactFragmentInteractionListener");
        }
    }

    /**
     * Handle actions when search button is clicked.
     */
    private void onClick(View view) {
        if (mListener != null) {
            String keyword = searchContactTextView.getText().toString();
            if (keyword.length() == 0) {
                searchContactTextView.setError("Keyword must not be empty");
            } else {
                mListener.onSearchAttempt(userName, keyword, data, adapter);
            }
        }
    }

    /**
     * Data structure representing a connection found in searched contact list
     */
    public static class SearchConnectionListItem{
        // The connection's name (first + last)
        private String name;
        // The connection's email
        private String email;
        // The connection's username
        private String username;

        public SearchConnectionListItem(String first, String last, String username, String email) {
            this.name = first + " " + last;
            this.email = email;
            this.username = username;
        }
    }

    /**
     * Custom adapter for searched contact list.
     */
    public class SearchConnectionAdapter extends ArrayAdapter<SearchConnectionListItem> {
        // The context where the adapter is at.
        private Context mContext;
        // The list of searched connections
        private List<SearchConnectionListItem> mList = new ArrayList<>();

        public SearchConnectionAdapter(Context context, ArrayList<SearchConnectionListItem> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.contact_list_item, parent, false);
            }

            SearchConnectionListItem currentItem = mList.get(position);

            TextView itemName = (TextView) listItem.findViewById(R.id.contactListItemName);
            itemName.setText(currentItem.name);

            TextView itemUsername = (TextView) listItem.findViewById(R.id.contactListItemUsername);
            itemUsername.setText(currentItem.username);

            TextView itemEmail = (TextView) listItem.findViewById(R.id.contactListItemEmail);
            itemEmail.setText(currentItem.email);

            Button itemButton = (Button) listItem.findViewById(R.id.contactListItemAddButton);
            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                /**
                 * Create an asynchronous call to webservice to add this connection
                 */
                public void onClick(View v) {
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath(getString(R.string.ep_send_request))
                            .build();
                    //build the JSONObject
                    JSONObject msg = new JSONObject();
                    try {
                        msg.put("username", userName);
                        msg.put("connection", currentItem.username);
                        System.out.println(msg);
                    } catch (JSONException e) {
                        Log.wtf("SEND REQUEST", "Error creating JSON: " + e.getMessage());
                    }

                    new SendPostAsyncTask.Builder(uri.toString(), msg)
                            .onPostExecute(this::handleAddContact)
                            .onCancelled(this::handleErrorsInTask)
                            .build().execute();
                }

                private void handleErrorsInTask(String result) {
                    Log.e("ASYNCT_TASK_ERROR", result);
                }

                /**
                 * Handles UI updates after receiving result from webservice.
                 * @param result
                 */
                private void handleAddContact(String result) {
                    try {
                        JSONObject resultsJSON = new JSONObject(result);
                        boolean success = resultsJSON.getBoolean("success");
                        if (success) {
                            itemButton.setEnabled(false);
                            itemButton.setText("Sent");
                        }
                    } catch (JSONException e) {
                        //It appears that the web service didn’t return a JSON formatted String
                        //or it didn’t have what we expected in it.
                        Log.e("JSON_PARSE_ERROR", result
                                + System.lineSeparator()
                                + e.getMessage());
                    }
                }
            });

            return listItem;
        }
    }

    /**
     * Interface to implement appropriate action when getting contacts for SearchNewConnectionFragment.
     */
    public interface SearchContactFragmentInteractionListener {
        void onSearchAttempt(String userName, String keyword, ArrayList<SearchConnectionListItem> data, SearchConnectionAdapter adapter);
    }
}
