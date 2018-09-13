package arm.man.gunmen;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SolutionViewerActivity extends AppCompatActivity {
    public static final String EXTRA_WIDTH = "extra_width";
    public static final String EXTRA_HEIGHT = "extra_height";
    public static final String EXTRA_MAP = "extra_map";

    private int mWidth;
    private int mHeight;

    private int[][] mMap;

    @BindView(R.id.layout_map)
    LinearLayout layoutMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution_viewer);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mWidth = extras.getInt(EXTRA_WIDTH);
            mHeight = extras.getInt(EXTRA_HEIGHT);

            String mapString = extras.getString(EXTRA_MAP);
            mMap = parseMap(mapString);
        }

        generateMap();
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

    private void generateMap() {
        for (int i = 0; i < mHeight; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < mWidth; j++) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                linearLayout.addView(checkBox);

                if (mMap[j][i] == BlockType.BLOCK_GUNMAN) {
                    checkBox.setChecked(true);
                } else if (mMap[j][i] == BlockType.BLOCK_WALL) {
                    checkBox.setChecked(true);
                    checkBox.setEnabled(false);
                }
            }

            layoutMap.addView(linearLayout);
        }
    }
}
