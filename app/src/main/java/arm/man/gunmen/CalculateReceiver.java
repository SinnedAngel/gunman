package arm.man.gunmen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CalculateReceiver extends BroadcastReceiver {

    public static final String ACTION_GUNMEN_PROGRESS = "arm.man.gunmen.action.MESSAGE_GUNMEN_PROGRESS";
    public static final String ACTION_GUNMEN_FINISHED = "arm.man.gunmen.action.MESSAGE_GUNMEN_FINISHED";

    public static final String EXTRA_MAP = "extra_map";
    public static final String EXTRA_GUNMEN_COUNT = "extra_gunmen_count";
    public static final String EXTRA_SOLUTION_COUNT = "extra_solution_count";
    public static final String EXTRA_SOLUTION_LIST = "extra_solution_list";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
