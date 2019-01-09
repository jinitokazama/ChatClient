package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import group8.tcss450.uw.edu.chatclient.model.Credentials;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

/**
 * Fragment for the Map and Weather features.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class WeatherMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, AdapterView.OnItemSelectedListener{

    private static final String TAG = "WeatherMapFragment ERROR->";
    private GoogleMap mGoogleMap;
    private double mLat, mLng;
    private Marker mMarker;
    private String mWhenChoice = "Now";
    private String mWhereChoice = "Here";
    private Location mCurrentLocation;
    private EditText mZIPView;
    private TextView mResultView;
    private ProgressBar mProgressBar;
    private Button mSubmitButton;
    private View mView;

    public WeatherMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_weather_map, container, false);

        mZIPView = (EditText) mView.findViewById(R.id.weatherZIPEditText);
        mZIPView.setVisibility(View.GONE);

        HomeActivity home = (HomeActivity) getActivity();
        mCurrentLocation = home.mCurrentLocation;
        mLat = home.mLat;
        mLng = home.mLng;

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        Spinner whereSpinner = (Spinner) mView.findViewById(R.id.weatherWhereSpinner);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.weatherSWhereArray, android.R.layout.simple_spinner_item);
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whereSpinner.setAdapter(whereAdapter);
        whereSpinner.setOnItemSelectedListener(this);

        Spinner whenSpinner = (Spinner) mView.findViewById(R.id.weatherWhenSpinner);
        ArrayAdapter<CharSequence> whenAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.weatherSWhenArray, android.R.layout.simple_spinner_item);
        whenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whenSpinner.setAdapter(whenAdapter);
        whenSpinner.setOnItemSelectedListener(this);

        mProgressBar = (ProgressBar) mView.findViewById(R.id.weatherMapProgressBar);
        mProgressBar.setVisibility(View.GONE);

        mResultView = (TextView) mView.findViewById(R.id.weatherResultView);

        mSubmitButton = (Button) mView.findViewById(R.id.weatherSubmitButton);
        mSubmitButton.setOnClickListener(this::onSubmitClick);

        return mView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // Hardcode - Add a marker in Tacoma, WA, and move the camera.
        //LatLng latLng = new LatLng(47.2529, -122.4443);
        LatLng latLng = new LatLng(mLat, mLng);
        mGoogleMap.addMarker(new MarkerOptions().
                position(latLng).
                title("You are here"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        mGoogleMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("LAT/LONG", latLng.toString());
        if (mMarker != null) {
            mMarker.setPosition(latLng);
        } else {
            mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));
        }
        //This feels weird to me
        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
    }

    /**Used by both spinners, determines which was selected and updates field*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) parent.getAdapter().getItem(position);
        //Log.d("--->","The item is " + item + " from: " + parent);
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.weatherWhenSpinner) {
            mWhenChoice = item;
        } else if(spinner.getId() == R.id.weatherWhereSpinner) {
            mWhereChoice = item;
            if (item.equals("ZIP")){
                mZIPView.setVisibility(View.VISIBLE);
            } else{
                mZIPView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.weatherWhereSpinner) {
            mZIPView.setVisibility(View.GONE);
        }
    }

    /**Determines what is selected in the spinner set and sends the appropriate AsyncTask*/
    public void onSubmitClick(View view) {
        getLocation();
        CheckBox save = (CheckBox) mView.findViewById(R.id.weatherCheckBox);
        String lat;
        String lon;
        //There's probably a better way to do this with less cyclomatic complexity
        if(mWhereChoice.equals("Here")){
            if(mCurrentLocation != null){
                lat = Double.toString(mCurrentLocation.getLatitude());
                lon = Double.toString(mCurrentLocation.getLongitude());
            } else {
                lat = Double.toString(mLat);
                lon = Double.toString(mLng);
            }
            String loc = lat + "," + lon;
            if(save.isChecked()) saveLocation(loc);
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(loc);
            }
        } else if(mWhereChoice.equals("Pin")){
            if(mMarker != null){
                lat = Double.toString(mMarker.getPosition().latitude);
                lon = Double.toString(mMarker.getPosition().longitude);
            } else {
                lat = Double.toString(mLat);
                lon = Double.toString(mLng);
            }
            String loc = lat + "," + lon;
            if(save.isChecked()) saveLocation(loc);
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(loc);
            }
        } else if(mWhereChoice.equals("ZIP")){
            checkZIP();
        } else if(mWhereChoice.equals("Saved")){
            String loc;
            SharedPreferences prefs = getContext().getSharedPreferences(getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            loc = prefs.getString(getString(R.string.location_key), "98403");
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(loc);
            }
        }else {
            System.out.println("Error with Spinner!");
        }
    }

    /**Confirms the ZIP is 5 digits before sending.*/
    public void checkZIP() {
        CheckBox save = (CheckBox) mView.findViewById(R.id.weatherCheckBox);
        String zip = (mZIPView.getText().toString());
        if (zip.length() != 5){
            mZIPView.setError("5-Digit ZIP");
        } else{
            if(save.isChecked()) saveLocation(zip);
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(zip);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(zip);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(zip);
            }
        }
    }

    /**Saves the location currently selected for use by Saved choice.*/
    public void saveLocation(String key) {
        SharedPreferences prefs =
                getContext().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        prefs.edit().putString(
                getString(R.string.location_key), key)
                .apply();
    }

    /**Callback to check HomeActivities current location.*/
    public void getLocation() {
        HomeActivity home = (HomeActivity) getActivity();
        mCurrentLocation = home.mCurrentLocation;
    }

    private void handleWeatherPre() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressBar.setProgress(0);
        mSubmitButton.setEnabled(false);
    }

    /**Builds JSON and starts new AsyncTask to get current weather.*/
    public void getCurrentWeather(String location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_current))
                .build();
        //build the JSONObject
        Credentials cred = new Credentials.Builder(location, null)
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleWeatherPre)
                .onPostExecute(this::handleCurrentWeatherPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**Builds JSON and starts new AsyncTask to get tomorrow's weather.*/
    public void getNextWeather(String location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_forecast))
                .build();
        //build the JSONObject
        Credentials cred = new Credentials.Builder(location, null)
                .addEmail("1")
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleWeatherPre)
                .onPostExecute(this::handleNextWeatherPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**Builds JSON and starts new AsyncTask to get the next week's weather.*/
    public void getFiveWeather(String location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_forecast))
                .build();
        //build the JSONObject
        Credentials cred = new Credentials.Builder(location, null)
                .addEmail("7")
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleWeatherPre)
                .onPostExecute(this::handleFiveWeatherPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handleCurrentWeatherPost(final String jsonResult) {
        String description = "";
        double temp = -99;
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("current")) {
                JSONObject current = json.getJSONObject("current");
                if (current.has("temp_f")) {
                    temp = current.getDouble("temp_f");
                }
                if (current.has("condition")) {
                    JSONObject cond = current.getJSONObject("condition");
                    if (cond.has("text")) {
                        description = cond.getString("text");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp != -99){
            //Truncate the temp
            DecimalFormat df = new DecimalFormat("#.#");
            String output = String.format(getString(R.string.weather_single_msg),
                    description, df.format(temp));
            mResultView.setText(output);
        }
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handleNextWeatherPost(final String jsonResult) {
        String description = "";
        double temp = -99;
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("forecast")) {
                JSONObject result = json.getJSONObject("forecast");
                JSONArray days = result.getJSONArray("forecastday");
                JSONObject forecast = days.getJSONObject(0).getJSONObject("day");
                if (forecast.has("maxtemp_f")) {
                    temp = forecast.getDouble("maxtemp_f");
                }
                if (forecast.has("condition")) {
                    JSONObject cond = forecast.getJSONObject("condition");
                    if (cond.has("text")) {
                        description = cond.getString("text");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp != -99){
            //Truncate the temp
            DecimalFormat df = new DecimalFormat("#.#");
            String output = String.format(getString(R.string.weather_single_msg),
                    description, df.format(temp));
            mResultView.setText(output);
        }
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handleFiveWeatherPost(final String jsonResult) {
        String description = "";
        double temp[] = {-99, -99, -99, -99, -99, -99, -99};
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("forecast")) {
                JSONObject result = json.getJSONObject("forecast");
                JSONArray days = result.getJSONArray("forecastday");
                for(int i =0; i < 7; i++){
                    JSONObject forecast = days.getJSONObject(i).getJSONObject("day");
                    if (forecast.has("maxtemp_f")) {
                        temp[i] = forecast.getDouble("maxtemp_f");
                    }
                    if (forecast.has("condition")) {
                        JSONObject cond = forecast.getJSONObject("condition");
                        if (cond.has("text")) {
                            description = cond.getString("text");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp[0] != -99){
            //Truncate the temp
            DecimalFormat df = new DecimalFormat("#.#");
            String output = String.format(getString(R.string.weather_five_msg), description,
                    df.format(temp[0]), df.format(temp[1]), df.format(temp[2]), df.format(temp[3]),
                    df.format(temp[4]), df.format(temp[5]), df.format(temp[6]));
            mResultView.setText(output);
        }
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }

}
