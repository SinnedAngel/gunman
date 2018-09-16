package arm.man.gunmen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class CalculateReceiver extends BroadcastReceiver {

    public static final String ACTION_GUNMEN_PROGRESS = "arm.man.gunmen.action.MESSAGE_GUNMEN_PROGRESS";
    public static final String ACTION_GUNMEN_FINISHED = "arm.man.gunmen.action.MESSAGE_GUNMEN_FINISHED";
    public static final String ACTION_GUNMEN_CANCELED = "arm.man.gunmen.action.MESSAGE_GUNMEN_CANCELED";
    public static final String ACTION_GUNMEN_PARITY_FAILED = "arm.man.gunmen.action.MESSAGE_GUNMEN_PARITY_FAILED";

    public static final String EXTRA_MAP = "extra_map";
    public static final String EXTRA_GUNMEN_COUNT = "extra_gunmen_count";
    public static final String EXTRA_SOLUTION_COUNT = "extra_solution_count";
    public static final String EXTRA_SOLUTION_LIST = "extra_solution_list";
    public static final String EXTRA_WIDTH = "extra_width";
    public static final String EXTRA_HEIGHT = "extra_height";

    private Listener mListener;

    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    public CalculateReceiver(){

    }

    public CalculateReceiver(@NonNull Listener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_GUNMEN_PROGRESS.equals(action))
                onProgress(intent);
            else if (ACTION_GUNMEN_FINISHED.equals(action))
                onFinished(intent);
            else if (ACTION_GUNMEN_CANCELED.equals(action))
                onCanceled();
            else if (ACTION_GUNMEN_PARITY_FAILED.equals(action))
                onParityFailed();
        }
    }

    private void onProgress(Intent intent) {
        String mapString = intent.getStringExtra(EXTRA_MAP);
        int solutionCount = intent.getIntExtra(EXTRA_SOLUTION_COUNT, 0);
        int maxGunmen = intent.getIntExtra(EXTRA_GUNMEN_COUNT, 0);
        int width = intent.getIntExtra(EXTRA_WIDTH, 0);
        int height = intent.getIntExtra(EXTRA_HEIGHT, 0);

        int[][] map = parseMap(mapString, width, height);

        if (mListener != null)
            mListener.onProgress(map, solutionCount, maxGunmen);
    }

    private void onFinished(Intent intent) {
        int maxGunmen = intent.getIntExtra(EXTRA_GUNMEN_COUNT, 0);
        int solutionCount = intent.getIntExtra(EXTRA_SOLUTION_COUNT, 0);
        ArrayList<String> solutions = intent.getStringArrayListExtra(EXTRA_SOLUTION_LIST);
        int width = intent.getIntExtra(EXTRA_WIDTH, 0);
        int height = intent.getIntExtra(EXTRA_HEIGHT, 0);

        if (mListener != null)
            mListener.onFinished(maxGunmen, solutionCount, solutions, width, height);
    }

    private void onCanceled() {
        if (mListener != null)
            mListener.onCanceled();
    }

    private void onParityFailed() {
        if (mListener != null)
            mListener.onParityFailed();
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

    public interface Listener {
        void onProgress(int[][] solution, int solutionCount, int maxGunmen);

        void onFinished(int maxGunmen, int maxSolution, ArrayList<String> solutions, int width, int height);

        void onCanceled();

        void onParityFailed();
    }
}
