package gr.academic.city.sdmd.foodnetwork.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import gr.academic.city.sdmd.foodnetwork.service.PushToServerService;

/**
 * Created by trumpets on 4/24/17.
 */
public class TriggerPushToServerBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_TRIGGER = "gr.academic.city.sdmd.foodnetwork.TRIGGER";
    public static final String ACTION_TRIGGER_UPVOTE = "gr.academic.city.sdmd.foodnetwork.TRIGGER_UPVOTE";

    public static final String EXTRA_MEAL_ID = "meal_id";

    public static final String LOG_TAG = "Trigger";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            if(ACTION_TRIGGER.equals(intent.getAction())) {
                PushToServerService.startPushMealsToServer(context);
            }else if(ACTION_TRIGGER_UPVOTE.equals(intent.getAction())){
                PushToServerService.startUpvoteToServer(context, intent.getLongExtra(EXTRA_MEAL_ID, -1));
            }


        }
    }
}
