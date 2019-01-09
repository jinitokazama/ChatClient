package group8.tcss450.uw.edu.chatclient.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChatListenManager {

    private static final String TAG = "Chat Listen Manager";

    private final String mURL;
    private final Consumer<JSONObject> mActionToTake;
    private final Consumer<Exception> mActionToTakeOnError;
    private final int mDelay;

    private String mDate;
    private ScheduledThreadPoolExecutor mPool;
    private ScheduledFuture mThread;

    /**
     * Helper class for building ChatListenManagers
     *
     * @author Lloyd Brooks
     */
    public static class Builder {
        //Required Parameters
        private final String mURL;
        private final Consumer<JSONObject> mActionToTake;

        //Optional Parameters
        private int mSleepTime = 500;
        private Consumer<Exception> mActionToTakeOnError = e -> {};
        private String mDate = "1970-01-01 00:00:01.00000";

        /**
         * Constructs a new Builder with a delay of 500 ms.
         *
         * When the Consumer processing the results needs to manipulate any UI elements, this must be
         * performed on the UI Thread. See ListenManager class documentation for more information.
         *
         * @param url the fully-formed url of the web service this task will connect to
         * @param actionToTake the Consumer processing the results
         */
        public Builder(String url, Consumer<JSONObject> actionToTake) {
            mURL = url;
            mActionToTake = actionToTake;
        }

        /**
         * Set the delay amount between calls to the web service. The default delay is 500 ms.
         * @param val the delay amount between calls to the web service
         * @return
         */
        public Builder setDelay(final int val) {
            mSleepTime = val;
            return this;
        }

        /**
         * Set the action to perform during exceptional handling. Note, not ALL possible
         * exceptions are handled by this consumer.
         *
         * @param val the action to perform during exceptional handling
         * @return
         */
        public Builder setExceptionHandler(final Consumer<Exception> val) {
            mActionToTakeOnError = val;
            return this;
        }

        /**
         * Sets the timestamp value for the Builder
         *
         * @param val the timestamp to pass to the  RequestListenManager
         * @return
         */
        public Builder setTimeStamp(final String val) {
            mDate = val;
            return this;
        }

        /**
         * Constructs a ListenManager with the current attributes.
         *
         * @return a ListenManager with the current attributes.
         */
        public ChatListenManager build() {
            return new ChatListenManager(this);
        }

    }

    public ChatListenManager(final Builder builder) {
        mURL = builder.mURL;
        mActionToTake = builder.mActionToTake;
        mDelay = builder.mSleepTime;
        mActionToTakeOnError = builder.mActionToTakeOnError;
        mDate = builder.mDate;
        mPool = new ScheduledThreadPoolExecutor(5);
    }

    /**
     * Starts the worker thread to ask for updates every delay milliseconds.
     */
    public void startListening() {
        mThread = mPool.scheduleAtFixedRate(new ListenForRequests(),
                0,
                mDelay,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stops listening for new messages.
     *
     * @return the most recent timestamp
     */
    public String stopListening() {
        mThread.cancel(true);
        return mDate;
    }

    /**
     * This method hits the end point for connection requests and checks for any new requests.
     */
    private class ListenForRequests implements Runnable {

        @Override
        public void run() {
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;

            //go out and ask for new messages
            response = new StringBuilder();
            try {
                String getURL = mURL;

                URL urlObject = new URL(getURL);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s;

                while ((s = buffer.readLine()) != null) {
                    response.append(s);
                }
                JSONObject messages = new JSONObject(response.toString());

                //here is where we "publish" the message that we received.
                mActionToTake.accept(messages);

                //get and store the last date.
                JSONArray msgs = messages.getJSONArray("chatid");
                if (msgs.length() > 0) {
                    JSONObject mostRecent = msgs.getJSONObject(msgs.length() - 1);
                    String timestamp = mostRecent.get("timestamp").toString();
                    mDate = timestamp;
                    Log.d(TAG, "Updating most recent time in ChatListenManager to : " + timestamp);
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
                mActionToTakeOnError.accept(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }
}
