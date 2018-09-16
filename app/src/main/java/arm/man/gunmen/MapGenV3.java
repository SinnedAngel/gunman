package arm.man.gunmen;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by ARM on 06/09/18.
 */

public class MapGenV3 {

    private static final int STATE_ALLOWED = 0;
    private static final int STATE_NOT_ALLOWED = 1;
    private static final int STATE_MUST = 2;

    private int mMaxX, mMaxY;
    private int[][] mMap;

    private ArrayList<String> mSolutions = new ArrayList<>();
    private int mMaxGunmen = 0;

    private Listener mListener;

    public MapGenV3(@NonNull Listener listener) {
        mListener = listener;
    }

    public void calculate(@NonNull int[][] map) {
        mMap = map;

        mMaxX = map.length - 1;
        if (mMaxX > 0)
            mMaxY = map[0].length - 1;

        Position startPosition = new Position(0, 0);
        next(startPosition);

        if (mListener != null) {
            if (ThreadState.isRequestCancel)
                mListener.onCanceled();
            else
                mListener.onFinished(mMaxGunmen, mSolutions.size(), mSolutions);
        }
    }

    private void next(Position position) {

        if (ThreadState.isRequestCancel)
            return;

        while (ThreadState.isPaused) {

        }

        int state = checkBack(position.x, position.y);

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

//            if (state == STATE_ALLOWED) {
            input(position, BlockType.BLOCK_EMPTY);

            if (canBeEmptied(position.x, position.y)) {
                if (nextPosition != null)
                    next(nextPosition);
                else
                    addSolution();
            }
//            }
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

        if (!mSolutions.contains(solution) && checkParity())
            mSolutions.add(solution);

        if (mListener != null)
            mListener.onProgress(mMap, mSolutions.size(), mMaxGunmen);
    }

//    private int check(int x, int y) {
//        if (mMap[x][y] == BlockType.BLOCK_WALL)
//            return STATE_NOT_ALLOWED;
//        else {
//            final int upState = checkUp(x, y);
//
//            if (upState == STATE_NOT_ALLOWED)
//                return upState;
//            else {
//                final int rightState = checkRight(x, y);
//
//                if (rightState == STATE_NOT_ALLOWED)
//                    return rightState;
//                else {
//                    final int downState = checkDown(x, y);
//
//                    if (downState == STATE_NOT_ALLOWED)
//                        return downState;
//                    else {
//                        final int leftState = checkLeft(x, y);
//
//                        if ((leftState == STATE_MUST && rightState == STATE_MUST) || (upState == STATE_MUST && downState == STATE_MUST))
//                            return STATE_MUST;
//                        else
//                            return leftState;
//                    }
//                }
//            }
//        }
//    }

    private int checkBack(int x, int y) {
        if (mMap[x][y] == BlockType.BLOCK_WALL)
            return STATE_NOT_ALLOWED;
        else {
            final int upState = checkUp(x, y);

            if (upState == STATE_NOT_ALLOWED)
                return upState;
            else {
                final int leftState = checkLeft(x, y);

                if (leftState == STATE_NOT_ALLOWED)
                    return leftState;
                else {
                    return STATE_ALLOWED;
                }
            }
        }
    }

    private boolean canBeEmptied(int x, int y) {
        if (canBeEmptiedRight(x, y))
            return true;
        else if (canBeEmptiedDown(x, y)) //&& isEdgeHorizontal(x, y)) {
            return true;
//        }
        return false;

//        return (canBeEmptiedRight(x, y) || isEdgeHorizontal(x, y)) && canBeEmptiedDown(x, y);

//        if (x >= mMaxX) {
//            if (mMap[x - 1][y] == BlockType.BLOCK_WALL)
//                return canBeEmptiedDown(x, y);
//            else
//                return false;
//        }
//
//        int count = 0;
//        for (int i = x + 1; i <= mMaxX; i++) {
//            if (mMap[i][y] == BlockType.BLOCK_WALL && count == 0) {
//                if (x == 0 || mMap[x - 1][y] == BlockType.BLOCK_WALL)
//                    return canBeEmptiedDown(x, y);
//                else
//                    return false;
//            } else if (checkUp(i, y) != STATE_NOT_ALLOWED)
//                return true;
//
//            count++;
//        }
//        return false;
    }

    private boolean canBeEmptiedRight(int x, int y) {
//        if (x < mMaxX && mMap[x + 1][y] == BlockType.BLOCK_EMPTY)
//            return true;
//        return false;

        if (x >= mMaxX)
            return false;

        int count = 0;
        for (int i = x + 1; i <= mMaxX; i++) {
            if (mMap[i][y] == BlockType.BLOCK_WALL && count == 0)
                return false;
            else if (checkUp(i, y) != STATE_NOT_ALLOWED)
                return true;

            count++;
        }
        return false;
    }

//    private boolean canBeEmptiedDown(int x, int y) {
//        if (y >= mMaxY)
//            return false;
//
//        int count = 0;
//        for (int i = y + 1; i <= mMaxY; i++) {
//            if (mMap[x][i] == BlockType.BLOCK_WALL && count == 0)
//                return false;
//            else if (checkLeft(x, i) != STATE_NOT_ALLOWED)
//                return true;
//
//            count++;
//        }
//
//        return false;
//    }

//    private boolean canBeEmptiedDown(int x, int y) {
//        if (y < mMaxY && mMap[x][y + 1] == BlockType.BLOCK_EMPTY)
//            return true;
//        return false;
//    }

    private boolean canBeEmptiedDown(int x, int y) {
        if (y < mMaxY && mMap[x][y + 1] == BlockType.BLOCK_EMPTY)
            return true;
        return false;
    }

    private boolean isEdgeHorizontal(int x, int y) {
        if (mMaxX == 0 ||
                (x == 0 && mMap[x + 1][y] == BlockType.BLOCK_WALL) ||
                (x == mMaxX && mMap[x - 1][y] == BlockType.BLOCK_WALL) ||
                (mMap[x - 1][y] == BlockType.BLOCK_WALL && mMap[x + 1][y] == BlockType.BLOCK_WALL))
            return true;
        return false;
    }

//    private boolean canBeEmptied(int x, int y) {
//        if (checkRight(x, y) == STATE_MUST && checkDown(x, y) == STATE_MUST)
//            return false;
//        return true;
//    }

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
                    if (!checkParity(j, i)) {
                        if (mListener != null)
                            mListener.onParityFailed();

                        return false;
                    }
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

        if (mListener != null)
            mListener.onProgress(mMap, mSolutions.size(), mMaxGunmen);

        if (ThreadState.delay > 0) {
            try {
                Thread.sleep(ThreadState.delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    public interface Listener {
        void onProgress(int[][] solution, int solutionCount, int maxGunmen);

        void onFinished(int maxGunmen, int maxSolution, ArrayList<String> solutions);

        void onCanceled();

        void onParityFailed();
    }
}
