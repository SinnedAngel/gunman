package arm.man.gunmen;

import android.os.AsyncTask;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ARM on 06/09/18.
 */

public class MapGenV2 extends AsyncTask<int[][], MapGenV2.Progress, MapGenV2.Result> {

    private static final int STATE_ALLOWED = 0;
    private static final int STATE_NOT_ALLOWED = 1;
    private static final int STATE_MUST = 2;

    private int mMaxX, mMaxY;
    private int[][] mMap;

    private ArrayList<String> mSolutions = new ArrayList<>();
    private int mMaxGunmen = 0;

    private int mDelay = 500;

    public void setDelay(int delay) {
        mDelay = delay;
    }

    private void calculate(int[][] map) {
        mMap = map;

        mMaxX = map.length - 1;
        if (mMaxX > 0)
            mMaxY = map[0].length - 1;

//        for (int i = 0; i <= mMaxY; i++) {
//            for (int j = 0; j <= mMaxX; j++) {
        Position startPosition = new Position(0, 0);
        next(startPosition);
//            }
//        }
    }

//    private void start(Position position) {
//        int state = check(position.x, position.y);
//
//        if (state == STATE_NOT_ALLOWED)
//            next(position);
//        else {
//            input(position, BlockType.BLOCK_GUNMAN);
//            next(position);
//
//            if (state == STATE_ALLOWED) {
//                input(position, BlockType.BLOCK_EMPTY);
//                next(position);
//            }
//        }
//    }

    private void next(Position position) {
        int state = check(position.x, position.y);

        if (state == STATE_NOT_ALLOWED) {
            Position nextPosition = position.next();

            if (nextPosition != null)
                next(nextPosition);
            else
                addSolution();
        } else {
            input(position, BlockType.BLOCK_GUNMAN);

            Position nextPosition = position.next();

            if (nextPosition != null)
                next(nextPosition);
            else
                addSolution();

            if (state == STATE_ALLOWED) {
                input(position, BlockType.BLOCK_EMPTY);

                if (nextPosition != null)
                    next(nextPosition);
                else
                    addSolution();
            }
        }
    }

    private void addSolution() {
        String solution = "";
        int gunmen = 0;
        for (int i = 0; i <= mMaxY; i++) {
            for (int j = 0; j <= mMaxX; j++) {
                solution += String.valueOf(mMap[j][i]);
                if (mMap[j][i] == BlockType.BLOCK_GUNMAN)
                    gunmen++;
            }
        }

        if (mMaxGunmen < gunmen)
            mMaxGunmen = gunmen;

//        long intSolution = Long.valueOf(solution);
        if (!mSolutions.contains(solution) && checkParity())
            mSolutions.add(solution);

        final Progress progress = new Progress();
        progress.solution = mMap.clone();
        progress.solutionCount = mSolutions.size();
        progress.maxGunmen = mMaxGunmen;

        publishProgress(progress);
    }

    private int check(int x, int y) {
        if (mMap[x][y] == BlockType.BLOCK_WALL)
            return STATE_NOT_ALLOWED;
        else {
            final int upState = checkUp(x, y);

            if (upState == STATE_NOT_ALLOWED)
                return upState;
            else {
                final int rightState = checkRight(x, y);

                if (rightState == STATE_NOT_ALLOWED)
                    return rightState;
                else {
                    final int downState = checkDown(x, y);

                    if (downState == STATE_NOT_ALLOWED)
                        return downState;
                    else {
                        final int leftState = checkLeft(x, y);

                        if (leftState == STATE_MUST) {
                            if (upState == STATE_MUST && rightState == STATE_MUST && downState == STATE_MUST)
                                return STATE_MUST;
                            else
                                return STATE_ALLOWED;
                        } else
                            return leftState;
                    }
                }
            }
        }
    }

    private boolean canBeEmptied(int x, int y) {
        if (checkRight(x, y) == STATE_MUST && checkDown(x, y) == STATE_MUST)
            return false;
        return true;
    }

    private int checkDown(int x, int y) {
        int count = 0;
        for (int i = y + 1; i <= mMaxY; i++) {
            if (mMap[x][i] == BlockType.BLOCK_WALL)
                if (count < 1)
                    return STATE_MUST;
                else
                    return STATE_ALLOWED;
            if (mMap[x][i] == BlockType.BLOCK_GUNMAN)
                return STATE_NOT_ALLOWED;

            count++;
        }
        if (count < 1)
            return STATE_MUST;
        else
            return STATE_ALLOWED;
    }

    private int checkLeft(int x, int y) {
        int count = 0;
        for (int i = x - 1; i >= 0; i--) {
            if (mMap[i][y] == BlockType.BLOCK_WALL)
                if (count < 1)
                    return STATE_MUST;
                else
                    return STATE_ALLOWED;
            if (mMap[i][y] == BlockType.BLOCK_GUNMAN)
                return STATE_NOT_ALLOWED;

            count++;
        }
        if (count < 1)
            return STATE_MUST;
        else
            return STATE_ALLOWED;
    }

    private int checkUp(int x, int y) {
        int count = 0;
        for (int i = y - 1; i >= 0; i--) {
            if (mMap[x][i] == BlockType.BLOCK_WALL) {
                if (count < 1)
                    return STATE_MUST;
                else
                    return STATE_ALLOWED;
            }
            if (mMap[x][i] == BlockType.BLOCK_GUNMAN)
                return STATE_NOT_ALLOWED;

            count++;
        }

        if (count < 1)
            return STATE_MUST;
        else
            return STATE_ALLOWED;
    }

    private int checkRight(int x, int y) {
        int count = 0;
        for (int i = x + 1; i <= mMaxX; i++) {
            if (mMap[i][y] == BlockType.BLOCK_WALL)
                if (count < 1)
                    return STATE_MUST;
                else
                    return STATE_ALLOWED;
            if (mMap[i][y] == BlockType.BLOCK_GUNMAN)
                return STATE_NOT_ALLOWED;

            count++;
        }
        if (count < 1)
            return STATE_MUST;
        else
            return STATE_ALLOWED;
    }

    private boolean checkParity() {
        for (int i = 0; i <= mMaxY; i++) {
            for (int j = 0; j <= mMaxX; j++) {
                if (mMap[j][i] == BlockType.BLOCK_EMPTY)
                    if (!checkParity(j, i))
                        return false;
            }
        }
        return true;
    }

    private boolean checkParity(int x, int y) {
        return parityUp(x, y) | parityRight(x, y) | parityDown(x, y) | parityLeft(x, y);
    }

    private boolean parityUp(int x, int y) {
        for (int i = y - 1; i >= 0; i--) {
            if (mMap[x][i] == BlockType.BLOCK_GUNMAN)
                return true;
            else if (mMap[x][i] == BlockType.BLOCK_WALL)
                return false;
        }
        return false;
    }

    private boolean parityRight(int x, int y) {
        for (int i = x + 1; i <= mMaxX; i++) {
            if (mMap[i][y] == BlockType.BLOCK_GUNMAN)
                return true;
            else if (mMap[i][y] == BlockType.BLOCK_WALL)
                return false;
        }
        return false;
    }

    private boolean parityDown(int x, int y) {
        for (int i = y + 1; i <= mMaxY; i++) {
            if (mMap[x][i] == BlockType.BLOCK_GUNMAN)
                return true;
            else if (mMap[x][i] == BlockType.BLOCK_WALL)
                return false;
        }
        return false;
    }

    private boolean parityLeft(int x, int y) {
        for (int i = x - 1; i >= 0; i--) {
            if (mMap[i][y] == BlockType.BLOCK_GUNMAN)
                return true;
            else if (mMap[i][y] == BlockType.BLOCK_WALL)
                return false;
        }
        return false;
    }

    private void input(Position position, int blockType) {
        mMap[position.x][position.y] = blockType;

        final Progress progress = new Progress();
        progress.solution = mMap.clone();
        progress.solutionCount = mSolutions.size();
        progress.maxGunmen = mMaxGunmen;

        publishProgress(progress);

        if (mDelay > 0) {
            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Result doInBackground(int[][]... ints) {
        calculate(ints[0]);

        Result result = new Result();
        result.maxGunmen = mMaxGunmen;
        result.maxSolution = mSolutions.size();
        result.solutions = mSolutions;
        return result;
    }

    public class Position {
        public int x;
        public int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position next() {
            int nextX = x;
            int nextY = y;

            if (x < mMaxX)
                nextX++;
            else if (y < mMaxY) {
                nextX = 0;
                nextY++;
            } else
                return null;

            return new Position(nextX, nextY);
        }
    }

    public class Result {
        public int maxGunmen;
        public int maxSolution;
        public ArrayList<String> solutions;
    }

    public class Progress {
        public int[][] solution;
        public int solutionCount;
        public int maxGunmen;
    }
}
