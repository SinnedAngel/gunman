package arm.man.gunmen;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ARM on 06/09/18.
 */

public class MapGen extends AsyncTask<int[][], int[][], MapGen.Result> {

//    private static final int DIRECTION_NONE = 0;
//    private static final int DIRECTION_NORTH = 1;
//    private static final int DIRECTION_NORTH_EAST = 2;
//    private static final int DIRECTION_EAST = 3;
//    private static final int DIRECTION_SOUTH_EAST = 4;
//    private static final int DIRECTION_SOUTH = 5;
//    private static final int DIRECTION_SOUTH_WEST = 6;
//    private static final int DIRECTION_WEST = 7;
//    private static final int DIRECTION_NORTH_WEST = 8;

    private int mMaxX, mMaxY;
    private int[][] mMap;

    private List<Integer> mSolutions = new ArrayList<>();
    private int mMaxGunmen = 0;

    public void calculate(int[][] map) {
        mMap = map;

        mMaxX = map.length;
        if (mMaxX > 0)
            mMaxY = map[0].length;

        for (int i = 0; i < map.length; i++) {
            int[] mapX = map[i];

            for (int j = 0; j < mapX.length; j++) {
                start(i, j);
            }
        }
    }

    private void start(int x, int y) {
        input(x, y);
        next(x, y, null);
    }

    private void next(int x, int y, Direction direction) {
        if (y > 0 && direction.y > -1)
            nextUp(x, y, direction);
        if (x <= mMaxX && direction.x > -1)
            nextRight(x, y, direction);
        if (y <= mMaxY && direction.y < 1)
            nextDown(x, y, direction);
        if (x > 0 && direction.x < 1)
            nextLeft(x, y, direction);

        if (x == mMaxX && y == mMaxY)
            addSolution();
    }

    private void addSolution() {
        String solution = "";
        int gunmen = 0;
        for (int i = 0; i < mMaxX; i++) {
            for (int j = 0; j < mMaxY; j++) {
                solution += String.valueOf(mMap[i][j]);
                if (mMap[i][j] == 2)
                    gunmen++;
            }
        }

        if (mMaxGunmen < gunmen)
            mMaxGunmen = gunmen;

        int intSolution = Integer.valueOf(solution);
        if (!mSolutions.contains(intSolution))
            mSolutions.add(intSolution);
    }

    private void nextUp(int x, int y, Direction direction) {
        int newY = y--;

        if (direction == null)
            direction = new Direction();

        direction.y = -1;
        if (checkDown(x, newY))
            input(x, newY);

        next(x, y, direction);
    }

    private void nextRight(int x, int y, Direction direction) {
        int newX = x++;

        if (direction == null)
            direction = new Direction();

        direction.x = 1;
        if (checkLeft(newX, y))
            input(newX, y);

        next(x, y, direction);
    }

    private void nextDown(int x, int y, Direction direction) {
        int newY = y++;

        if (direction == null)
            direction = new Direction();

        direction.y = 1;
        if (checkUp(x, newY))
            input(x, newY);

        next(x, y, direction);
    }

    private void nextLeft(int x, int y, Direction direction) {
        int newX = x--;

        if (direction == null)
            direction = new Direction();

        direction.x = -1;
        if (checkRight(newX, y))
            input(newX, y);

        next(x, y, direction);
    }

    private boolean checkDown(int x, int y) {
        for (int i = y; i < mMaxY; i++) {
            if (mMap[x][i] == 1)
                return true;
            if (mMap[x][i] == 2)
                return false;
        }
        return true;
    }

    private boolean checkLeft(int x, int y) {
        for (int i = x; i >= 0; i--) {
            if (mMap[x][i] == 1)
                return true;
            if (mMap[x][i] == 2)
                return false;
        }
        return true;
    }

    private boolean checkUp(int x, int y) {
        for (int i = y; i >= 0; i--) {
            if (mMap[x][i] == 1)
                return true;
            if (mMap[x][i] == 2)
                return false;
        }
        return true;
    }

    private boolean checkRight(int x, int y) {
        for (int i = x; i < mMaxX; i++) {
            if (mMap[x][i] == 1)
                return true;
            if (mMap[x][i] == 2)
                return false;
        }
        return true;
    }

    private void input(int x, int y) {
        mMap[x][y] = 2;
        publishProgress(mMap);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Result doInBackground(int[][]... ints) {
        calculate(ints[0]);

        Result result = new Result();
        result.maxGunmen = mMaxGunmen;
        result.maxSolution = mSolutions.size();
        return result;
    }

    public class Direction {
        public int x = 0;
        public int y = 0;
    }

    public class Result {
        public int maxGunmen;
        public int maxSolution;
    }
}
