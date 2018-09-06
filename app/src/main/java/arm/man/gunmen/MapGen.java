package arm.man.gunmen;

/**
 * Created by ARM on 06/09/18.
 */

public class MapGen {
    public void calculate(int[][] map) {

        for (int i = 0; i < map.length; i++) {
            int[] mapX = map[i];

            for (int j = 0; j < mapX.length; j++) {
                start(map, i, j);
            }
        }
    }

    private void start(int[][] map, int i, int j) {
        input(map, i, j);
    }

    private void input(int[][] map, int i, int j) {
        map[i][j] = 2;
    }

    private boolean checkUp(int[][] map, int i, int j) {
        while (j >= 0) {

        }
    }
}
