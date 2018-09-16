package arm.man.gunmen;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    public static final String ACTION_PROGRESS = "action_progress";
    public static final String ACTION_FINISHED = "action_finished";

    // TODO: Rename parameters
    private static final String EXTRA_WIDTH = "extra_width";
    private static final String EXTRA_HEIGHT = "extra_height";
    private static final String EXTRA_MAP = "extra_map";
    private static final String EXTRA_DELAY = "extra_delay";

    private static final String CHANNEL_ID = "GunMen";
    private static final int NOTIFICATION_ID = 1234;

    private MapGenV3 mMapGen;

    private int mWidth, mHeight;

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
    public static void startCalculate(Context context, int width, int height, int[][] map) {
        String mapString = parseMap(map);

        Intent intent = new Intent(context, CalculateIntentService.class);
        intent.setAction(ACTION_CALCULATE);
        intent.putExtra(EXTRA_WIDTH, width);
        intent.putExtra(EXTRA_HEIGHT, height);
        intent.putExtra(EXTRA_MAP, mapString);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CALCULATE.equals(action)) {
                mWidth = intent.getIntExtra(EXTRA_WIDTH, 0);
                mHeight = intent.getIntExtra(EXTRA_HEIGHT, 0);
                final String map = intent.getStringExtra(EXTRA_MAP);
                handleAction(map);
            }
        }
    }

    private int[][] parseMap(String mapString) {
        int[][] map = new int[mWidth][mHeight];

        int index = 0;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {

                map[j][i] = Integer.valueOf(mapString.substring(index, index + 1));

                index++;
            }
        }

        return map;
    }

    private static String parseMap(int[][] map) {
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
    private void handleAction(String mapString) {
        mMapGen = new MapGenV3(this);

        int[][] map = parseMap(mapString);

        mMapGen.calculate(map);

    }

    @Override
    public void onProgress(int[][] solution, int solutionCount, int maxGunmen) {

        final String solutionString = parseMap(solution);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CalculateReceiver.ACTION_GUNMEN_PROGRESS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_MAP, solutionString);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_SOLUTION_COUNT, solutionCount);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_GUNMEN_COUNT, maxGunmen);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_WIDTH, mWidth);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_HEIGHT, mHeight);
        sendBroadcast(broadcastIntent);

        showProgressNotification(solutionString, solutionCount, maxGunmen);
    }

    @Override
    public void onFinished(int maxGunmen, int maxSolution, ArrayList<String> solutions) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CalculateReceiver.ACTION_GUNMEN_FINISHED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_GUNMEN_COUNT, maxGunmen);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_SOLUTION_COUNT, maxSolution);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_SOLUTION_LIST, solutions);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_WIDTH, mWidth);
        broadcastIntent.putExtra(CalculateReceiver.EXTRA_HEIGHT, mHeight);
        sendBroadcast(broadcastIntent);

        showFinishedNotification(solutions, maxSolution, maxGunmen);

        saveSolutions(maxSolution, maxGunmen, solutions);
    }

    @Override
    public void onCanceled() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CalculateReceiver.ACTION_GUNMEN_CANCELED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onParityFailed() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CalculateReceiver.ACTION_GUNMEN_PARITY_FAILED);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }

    private void showProgressNotification(String solution, int solutionCount, int maxGunmen) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_PROGRESS);
        intent.putExtra(CalculateReceiver.EXTRA_MAP, solution);
        intent.putExtra(CalculateReceiver.EXTRA_SOLUTION_COUNT, solutionCount);
        intent.putExtra(CalculateReceiver.EXTRA_GUNMEN_COUNT, maxGunmen);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("GunMen")
                .setContentText("Solution found=" + solutionCount)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void showFinishedNotification(ArrayList<String> solutions, int solutionCount, int maxGunmen) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_FINISHED);
        intent.putStringArrayListExtra(CalculateReceiver.EXTRA_MAP, solutions);
        intent.putExtra(CalculateReceiver.EXTRA_SOLUTION_COUNT, solutionCount);
        intent.putExtra(CalculateReceiver.EXTRA_GUNMEN_COUNT, maxGunmen);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("GunMen")
                .setContentText("Solution found=" + solutionCount)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.notification_channel_name);
        String description = getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void saveSolutions(int solutionCount, int maxGunMen, ArrayList<String> solutions) {
        final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/gunmen/";
        final File directory = new File(path);
        directory.mkdirs();

        File file = new File(path, "gunmen_" + System.currentTimeMillis() + ".txt");
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("solution count = ").append(solutionCount).append("\n")
                    .append("max gunmen = ").append(maxGunMen).append("\n")
                    .append("solutions :");

            for (String solution : solutions) {
                stringBuilder.append("\n");
                stringBuilder.append(solution);
            }

            outputStreamWriter.write(stringBuilder.toString());
        } catch (FileNotFoundException e) {
            Log.e("DUDIDAM", e.getMessage());
        } catch (IOException e) {
            Log.e("DUDIDAM", e.getMessage());
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    Log.e("DUDIDAM", e.getMessage());
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e("DUDIDAM", e.getMessage());
                }
            }
        }
    }
}
