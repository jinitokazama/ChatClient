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
public class MessagesIntentService extends IntentService {

    private static final String TAG = "MessagesIntentService";
    private static final int POLL_INTERVAL = 60_000;
    public static final  String RECEIVED_UPDATE = "New Message(s)";

    private String mUsername;
    private String mTimestamp;

    public MessagesIntentService() {
        super("MessagesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "performing the new messages listen service");
            SharedPreferences prefs =  getSharedPreferences(getString(R.string.keys_shared_prefs)
                    , Context.MODE_PRIVATE);
            mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
            mTimestamp = prefs.getString(getString(R.string.keys_prefs_messages_time_stamp), "1970-01-01 00:00:01.00000");

            boolean foreground = intent.getBooleanExtra(getString(R.string.keys_is_foreground), false);
            Log.wtf(TAG, "checking server with foregroung = " + foreground);
            checkWebService(foreground);

        }
    }

    //used to start the service
    public static void startServiceAlarm(Context context, boolean isInForeground) {
        Intent i = new Intent(context, MessagesIntentService.class);
        Log.d(TAG, "in foreground set to : " + isInForeground);
        i.putExtra(context.getString(R.string.keys_is_foreground), isInForeground);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,i,0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int startAfter = isInForeground ? POLL_INTERVAL : POLL_INTERVAL *2;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startAfter
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

    //This method checks the web service for any new messages.
    private boolean checkWebService(boolean isInForeground) {
        boolean isEmptyReply = false;
        //check webservice in background
        Uri checkForNew = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_all_message))
                .appendQueryParameter("username", mUsername)
                .appendQueryParameter("after", mTimestamp)
                .build();

        HttpURLConnection urlConnection = null;

        StringBuilder response = new StringBuilder();
        try{
            URL urlObject = new URL(checkForNew.toString());
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
        if(response.charAt(11) == ']') isEmptyReply = true;
        //if there are new incoming requests then set a notification or notify HomeActivity.
        if (!isEmptyReply) {
            Log.e(TAG, "creating notification in forground" + isInForeground);
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
        This method puts a notification in the notification bar on the device and loads the
        ChatListFragment when pressed.
     */
    private void buildNotification (String s) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_menu_message)
                .setContentTitle("New Message(s)")
                .setContentText("You have a new messages");

        //Create an Intent for the Activity
        Intent notifyIntent = new Intent(this, HomeActivity.class);
        notifyIntent.putExtra("username", mUsername);
        notifyIntent.putExtra(getString(R.string.keys_message_results), s);

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
