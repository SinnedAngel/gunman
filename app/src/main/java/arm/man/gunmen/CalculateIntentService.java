package arm.man.gunmen;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CalculateIntentService extends IntentService implements MapGenV3.Listener {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_CALCULATE = "arm.man.gunmen.action.CALCULATE";

    // TODO: Rename parameters
    private static final String EXTRA_WIDTH = "extra_width";
    private static final String EXTRA_HEIGHT = "extra_height";
    private static final String EXTRA_MAP = "extra_map";
    private static final String EXTRA_DELAY = "extra_delay";

    private MapGenV3 mMapGen;

    public CalculateIntentService() {
        super("CalculateIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startCalculate(Context context, int width, int height, String map, int delay) {
        Intent intent = new Intent(context, CalculateIntentService.class);
        intent.setAction(ACTION_CALCULATE);
        intent.putExtra(EXTRA_WIDTH, width);
        intent.putExtra(EXTRA_HEIGHT, height);
        intent.putExtra(EXTRA_MAP, map);
        intent.putExtra(EXTRA_DELAY, delay);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CALCULATE.equals(action)) {
                final int width = intent.getIntExtra(EXTRA_WIDTH, 0);
                final int height = intent.getIntExtra(EXTRA_HEIGHT, 0);
                final String map = intent.getStringExtra(EXTRA_MAP);
                final int delay = intent.getIntExtra(EXTRA_DELAY, 0);
                handleAction(width, height, map, delay);
            }
        }
    }

    private int[][] parseMap(String mapString, int width, int height) {
        int[][] map = new int[width][height];

        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                map[j][i] = Integer.valueOf(mapString.substring(index, index + 1));

                index++;
            }
        }

        return map;
    }

    private String parseMap(int[][] map) {
        String stringMap = "";

        int width = map.length;
        int height = map[0].length;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                stringMap += map[j][i];
            }
        }

        return stringMap;
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleAction(int width, int height, String mapString, int delay) {
        mMapGen = new MapGenV3(this);
        mMapGen.setDelay(delay);

        int[][] map = parseMap(mapString, width, height);

        mMapGen.calculate(map);

    }

    @Override
    public void onProgress(int[][] solution, int solutionCount, int maxGunmen) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CalculateReceiver.ACTION_GUNMEN_PROGRESS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_MAP, parseMap(solution));
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_SOLUTION_COUNT, solutionCount);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_GUNMEN_COUNT, maxGunmen);
    }

    @Override
    public void onFinished(int maxGunmen, int maxSolution, ArrayList<String> solutions) {

    }
}
