package arm.man.gunmen;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_INITIALIZE = 0;
    private static final int REQUEST_MAP = 1;

    //    @BindView(R.id.text_map)
//    TextView textViewMap;
    @BindView(R.id.layout_map)
    LinearLayout layoutMap;

    @BindView(R.id.text_solutions)
    TextView textViewSolutions;

    @BindView(R.id.text_gunmen)
    TextView textViewGunMen;

    @BindView(R.id.button_browse_solutions)
    Button buttonBrowseSolutions;

    @BindView(R.id.button_calculate)
    Button buttonCalculate;

    @BindView(R.id.button_init)
    Button buttonInit;

    @BindView(R.id.button_cancel)
    Button buttonCancel;

    @BindView(R.id.edit_delay)
    EditText editTextDelay;

    private int mWidth;
    private int mHeight;
    private int[][] mMap;

    private int mMaxSolution;
    private int mMaxGunman;
    private ArrayList<String> mSolutions;

    private MapGenV2 mMapGen;

    private CalculateReceiver mCalculateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        IntentFilter intentFilter = new IntentFilter(CalculateReceiver.ACTION_GUNMEN_PROGRESS);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mCalculateReceiver = new CalculateReceiver();
        registerReceiver(mCalculateReceiver, intentFilter);
    }

    public void initialize(View view) {
        Intent intent = new Intent(this, InitSizeActivity.class);
        startActivityForResult(intent, REQUEST_INITIALIZE);
    }

    public void initMap(View view) {
        Intent intent = new Intent(this, InitMapActivity.class);
        intent.putExtra(InitMapActivity.EXTRA_WIDTH, mWidth);
        intent.putExtra(InitMapActivity.EXTRA_HEIGHT, mHeight);
        startActivityForResult(intent, REQUEST_MAP);
    }

    public void calculate(View view) {
        buttonBrowseSolutions.setVisibility(View.GONE);
        buttonCalculate.setVisibility(View.INVISIBLE);
        buttonInit.setEnabled(false);
        buttonCancel.setVisibility(View.VISIBLE);

        mMapGen = new MapGenV2() {
            @Override
            protected void onProgressUpdate(Progress... values) {
                if (values != null && values.length > 0) {
                    Progress progress = values[values.length - 1];
                    textViewSolutions.setText(getString(R.string.solution_count, progress.solutionCount));
                    textViewGunMen.setText(getString(R.string.gunmen_count, progress.maxGunmen));
                    drawMap(progress.solution);
                }
            }

            @Override
            protected void onPostExecute(Result result) {
                if (result != null) {
                    mMaxSolution = result.maxSolution;
                    mMaxGunman = result.maxGunmen;
                    mSolutions = result.solutions;

                    textViewSolutions.setText(getString(R.string.solution_count, result.maxSolution));
                    textViewGunMen.setText(getString(R.string.gunmen_count, result.maxGunmen));
                    showResult(result.maxSolution, result.maxGunmen);

                    buttonBrowseSolutions.setVisibility(View.VISIBLE);

                    buttonCalculate.setVisibility(View.VISIBLE);
                    buttonInit.setEnabled(true);
                    buttonCancel.setVisibility(View.INVISIBLE);
                }
            }
        };

        mMapGen.setDelay(Integer.valueOf(editTextDelay.getText().toString()));

        mMapGen.execute(mMap);
    }

    public void calculateService(View view) {

    }

    public void cancelCalculate(View view) {
        if (mMapGen != null)
            mMapGen.cancel(true);

        buttonInit.setEnabled(true);
        buttonCalculate.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.INVISIBLE);
    }

    public void showResult(int maxSolution, int maxGunMen) {
        new AlertDialog.Builder(this).setTitle("Result").setMessage(getString(R.string.solution_count, maxSolution) + "\n" + getString(R.string.gunmen_count, maxGunMen)).show();
    }

//    private void drawMap(int[][] map) {
//        String mapString = "";
//        for (int i = 0; i < mHeight; i++) {
//            for (int j = 0; j < mWidth; j++) {
//                int state = map[j][i];
//
//                if (state == BlockType.BLOCK_EMPTY)
//                    mapString += "[   ]";
//                else if (state == BlockType.BLOCK_WALL)
//                    mapString += " # ";
//                else if (state == BlockType.BLOCK_GUNMAN)
//                    mapString += " & ";
//            }
//            mapString += "\n";
//        }
//
//        textViewMap.setText(mapString);
//    }

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
                            checkBox.setEnabled(true);
                        } else if (state == BlockType.BLOCK_WALL) {
                            checkBox.setChecked(true);
                            checkBox.setEnabled(false);
                        } else if (state == BlockType.BLOCK_GUNMAN) {
                            checkBox.setChecked(true);
                            checkBox.setEnabled(true);
                        }
                    }
                }
            }
        }
    }

    private void generateMap() {
        layoutMap.removeAllViews();

        for (int i = 0; i < mHeight; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < mWidth; j++) {
                CheckBox checkBox = new CheckBox(this);
                linearLayout.addView(checkBox);

                checkBox.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }

            layoutMap.addView(linearLayout);
        }
    }

    public void browseSolution(View view) {
        Intent intent = new Intent(this, SolutionBrowserActivity.class);
        intent.putExtra(SolutionBrowserActivity.EXTRA_WIDTH, mWidth);
        intent.putExtra(SolutionBrowserActivity.EXTRA_HEIGHT, mHeight);
        intent.putStringArrayListExtra(SolutionBrowserActivity.EXTRA_SOLUTIONS, mSolutions);

        startActivity(intent);
    }

    private void reset() {
        textViewSolutions.setText(getString(R.string.solution_count, 0));
        textViewGunMen.setText(getString(R.string.gunmen_count, 0));
        buttonBrowseSolutions.setVisibility(View.GONE);
    }

    private String parseMap() {
        String stringMap = "";

        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                stringMap += mMap[j][i];
            }
        }

        return stringMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INITIALIZE && resultCode == RESULT_OK) {
            if (data != null) {
                mWidth = data.getIntExtra(InitSizeActivity.EXTRA_WIDTH, 0);
                mHeight = data.getIntExtra(InitSizeActivity.EXTRA_HEIGHT, 0);

                initMap(null);
            }
        } else if (requestCode == REQUEST_MAP) {
            if (resultCode == RESULT_OK && data != null) {
                String mapString = data.getStringExtra(InitMapActivity.EXTRA_MAP);

                mMap = new int[mWidth][mHeight];

                int index = 0;
                for (int i = 0; i < mHeight; i++) {
                    for (int j = 0; j < mWidth; j++) {
                        String stateString = mapString.substring(index, index + 1);
                        mMap[j][i] = Integer.valueOf(stateString);
                        index++;
                    }
                }

                generateMap();

                drawMap(mMap);

                reset();
            } else {
                mWidth = 0;
                mHeight = 0;
            }
        }
    }
}
