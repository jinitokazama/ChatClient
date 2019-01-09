package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * information fragment from the navigation drawer
 *
 * @author Jin Byoun - jinito@uw.edu
 * @author Eric Harty - hartye@uw.edu
 */
public class HomeInformationFragment extends Fragment implements View.OnClickListener{

    private View mView;
    private Button mWeatherButton;
    private TextView mWeatherView;
    private HomeInformationFragment.OnHomeFragmentInteractionListener mListener;

    public HomeInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_home_information, container, false);
        mWeatherButton = (Button) mView.findViewById(R.id.moreWeatherButton);
        mWeatherButton.setOnClickListener(this);
        mWeatherButton.setEnabled(false);
        mWeatherView = (TextView) mView.findViewById(R.id.homeWeatherView);

        return mView;
    }

    /**Called by HomeActivity to set the weather after it's retrieved*/
    public void setWeather(String weather) {
        if(mView!= null){
            mWeatherView.setText(weather);
            mWeatherButton.setEnabled(true);
        }
    }

    /**Called by HomeActivity to set the location after it's retrieved*/
    public void setLocation(Location location) {
        //If this fragment isn't inflated when the location changed then don't crash
        if(mView!= null){
            TextView text = (TextView) mView.findViewById(R.id.homeInfoLocation);
            String loc = String.format(getString(R.string.home_info_location_msg),
                    location.getLatitude(), location.getLongitude());
            text.setText(loc);
        }
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            mListener.onMoreWeatherClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeInformationFragment.OnHomeFragmentInteractionListener) {
            mListener = (HomeInformationFragment.OnHomeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " OnHomeFragmentInteractionListener");
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
    public interface OnHomeFragmentInteractionListener {
        void onMoreWeatherClicked();
    }

}
