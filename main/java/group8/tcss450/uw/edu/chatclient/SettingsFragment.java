package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Fragment for app settings.
 * including color theme selection.
 *
 * @author LLoyd Brooks
 * @version 5/2/2018
 */
public class SettingsFragment extends Fragment{


    private OnSettingsInteractionListener mListener;
    private View mView;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        RadioGroup group = (RadioGroup) mView.findViewById(R.id.colorThemeRadioGroup);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mListener != null) {

                    // Check which radio button was clicked
                    switch (checkedId) {
                        case R.id.blueOrangeRadio:
                            // tell listener to set app theme to blue and orange
                            mListener.onSettingsInteraction(1);
                            break;
                        case R.id.greenAmberRadio:
                            // tell listenr to set theme to green and amber
                            mListener.onSettingsInteraction(2);
                            break;
                        case R.id.redBlueRadio:
                            // tell listener to set theme to red and blue
                            mListener.onSettingsInteraction(3);
                            break;
                        case R.id.brownPinkRadio:
                            //tell listener to set theme to brown and pink.
                            mListener.onSettingsInteraction(4);
                            break;
                        default:
                            Log.wtf("settingsFragment","How'd this happen?");
                    }
                } else {
                    Log.wtf("settingsFragment", "not where i thought.");
                }
            }
        });


        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsInteractionListener) {
            mListener = (OnSettingsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsInteractionListener");
        }

    }


    //Call back interface to be used by the implementing activity to apply the selected color theme to the app.
    public interface OnSettingsInteractionListener {
        void onSettingsInteraction(int theme);
    }
}
