package group8.tcss450.uw.edu.chatclient.utils;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.InputMismatchException;

import group8.tcss450.uw.edu.chatclient.HomeActivity;
import group8.tcss450.uw.edu.chatclient.R;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class ContactsIntentService extends IntentService {

    private static final String TAG = "ContactsIntentService";
    private static final int POLL_INTERVAL = 60_000;
    public static final  String RECEIVED_UPDATE = "New Contact Request";

    private String mUsername;
    private String mIncomingTimestamp;

    public ContactsIntentService() {
        super("ContactsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "performing the contacts listen service");
            SharedPreferences prefs =  getSharedPreferences(getString(R.string.keys_shared_prefs)
                    , Context.MODE_PRIVATE);
            mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
            mIncomingTimestamp = prefs.getString(getString(R.string.keys_prefs_incoming_request_time_stamp), "0");

            checkWebService(intent.getBooleanExtra(getString(R.string.keys_is_foreground), false));

        }
    }

    //used to start the service
    public static void startServiceAlarm(Context context, boolean isInForeground) {
        Intent i = new Intent(context, ContactsIntentService.class);
        i.putExtra(context.getString(R.string.keys_is_foreground), isInForeground);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,i,0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int startAfeter = isInForeground ? POLL_INTERVAL : POLL_INTERVAL *2;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startAfeter
                ,POLL_INTERVAL, pendingIntent);
    }

    // used to stop the service.
    public static void stopServiceAlarm(Context context) {
        Intent i = new Intent(context, ContactsIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    //This method checks the webserice to see if there are any new connection requests.
    private boolean checkWebService(boolean isInForeground) {
        boolean isEmptyReply = false;
        //check webservice in background
        Uri retrieveIncoming = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_pending))
                .appendPath(getString(R.string.ep_pending_incoming))
                .appendQueryParameter("username", mUsername)
                .build();

        HttpURLConnection urlConnection = null;

        StringBuilder response = new StringBuilder();
        try{
            URL urlObject = new URL(retrieveIncoming.toString() + "&after=" + mIncomingTimestamp);
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            InputStream content = urlConnection.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s;
            while ((s = buffer.readLine()) != null) {
                response.append(s);
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        //check for empty reply
        if(response.charAt(27) == ']') isEmptyReply = true;

        //if there are new incoming requests then set a notification or notify HomeActivity.
        if (!isEmptyReply) {
            if (isInForeground) {
                Intent i = new Intent(RECEIVED_UPDATE);
                i.putExtra(getString(R.string.keys_extra_results), response.toString());
                sendBroadcast(i);
            } else {
                buildNotification(response.toString());
            }
        }


        return true;
    }

    /*
        This method puts a notification in the notification bar of the device which will load the
        pendingConnectionsFragment when pressed.
     */

    private void buildNotification (String s) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_menu_new_connection)
                .setContentTitle("New Connection")
                .setContentText("You have a new connection request!");

        //Create an Intent for the Activity
        Intent notifyIntent = new Intent(this, HomeActivity.class);
        notifyIntent.putExtra("username", mUsername);
        notifyIntent.putExtra(getString(R.string.keys_extra_results), s);

        //Sets the activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Creates the Pending Intent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0
                , notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Puts the PendingIntent into the notification builder
        mBuilder.setContentIntent(notifyPendingIntent);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

}
