package arm.man.gunmen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InitMapActivity extends AppCompatActivity {
    public static final String EXTRA_WIDTH = "extra_width";
    public static final String EXTRA_HEIGHT = "extra_height";
    public static final String EXTRA_MAP = "extra_map";

    private int mWidth;
    private int mHeight;

    @BindView(R.id.layout_map)
    LinearLayout layoutMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_map);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mWidth = extras.getInt(EXTRA_WIDTH);
            mHeight = extras.getInt(EXTRA_HEIGHT);

            generateMap();

            String mapString = extras.getString(EXTRA_MAP);
            if(mapString!=null){
                int[][] map = parseMap(mapString);

                drawMap(map);
            }
        }
    }

    private void generateMap() {
        for (int i = 0; i < mHeight; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < mWidth; j++) {
                CheckBox checkBox = new CheckBox(this);
                linearLayout.addView(checkBox);
            }

            layoutMap.addView(linearLayout);
        }
    }

    private void drawMap(int[][] map) {
        for (int i = 0; i < mHeight; i++) {

            View row = layoutMap.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout layoutRow = (LinearLayout) row;

                for (int j = 0; j < mWidth; j++) {
                    View col = layoutRow.getChildAt(j);
                    if (col instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) col;
                        int state = map[j][i];

                        if (state == BlockType.BLOCK_EMPTY) {
                            checkBox.setChecked(false);
                        } else if (state == BlockType.BLOCK_WALL) {
                            checkBox.setChecked(true);
                        }
                    }
                }
            }
        }
    }

    public void proccess(View view) {
        String map = parseMap();

        Intent result = new Intent();
        result.putExtra(EXTRA_MAP, map);

        setResult(RESULT_OK, result);
        finish();
    }

    private String parseMap() {
        String stringMap = "";

        for (int i = 0; i < mHeight; i++) {
            View layout = layoutMap.getChildAt(i);
            if (layout instanceof LinearLayout) {
                LinearLayout linearLayout = (LinearLayout) layout;

                for (int j = 0; j < mWidth; j++) {
                    View v = linearLayout.getChildAt(j);

                    if (v instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) v;
                        stringMap += checkBox.isChecked() ? String.valueOf(BlockType.BLOCK_WALL) : String.valueOf(BlockType.BLOCK_EMPTY);
                    }
                }
            }
        }

        return stringMap;
    }

    private int[][] parseMap(String mapString) {
        int[][] map = new int[mWidth][mHeight];

        int index = 0;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                String stateString = mapString.substring(index, index + 1);
                map[j][i] = Integer.valueOf(stateString);
                index++;
            }
        }

        return map;
    }
}
