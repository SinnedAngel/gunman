package arm.man.gunmen;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements CalculateReceiver.Listener {
    private static final int REQUEST_INITIALIZE = 0;
    private static final int REQUEST_MAP = 1;

    private static final int STATUS_INIT = 0;
    private static final int STATUS_READY = 1;
    private static final int STATUS_CALCULATING = 2;
    private static final int STATUS_CANCEL = 3;
    private static final int STATUS_FINISHED = 4;

    private static final String STATE_WIDTH = "state_width";
    private static final String STATE_HEIGHT = "state_height";
    private static final String STATE_STATUS = "state_status";

    private static final String PREF_MAX_GUNMEN = "pref_max_gunmen";
    private static final String PREF_MAX_SOLUTIONS = "pref_max_solutions";
    private static final String PREF_SOLUTION_LIST = "pref_solution_list";

    private static final String PREF_DELAY = "pref_delay";
    private static final String PREF_WIDTH = "pref_width";
    private static final String PREF_HEIGHT = "pref_height";
    private static final String PREF_MAP = "pref_map";

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

    @BindView(R.id.button_init_size)
    Button buttonInitSize;

    @BindView(R.id.button_cancel)
    Button buttonCancel;

    @BindView(R.id.edit_delay)
    EditText editTextDelay;

    @BindView(R.id.button_init_map)
    Button buttonInitMap;

    @BindView(R.id.text_parity_fail)
    TextView textViewParityFail;

    @BindView(R.id.button_pause)
    Button buttonPause;

    private int mWidth;
    private int mHeight;
    private int[][] mMap;

    private int mParityFailCount;

    private int mMaxSolution;
    private int mMaxGunman;
    private ArrayList<String> mSolutions;

    private MapGenV2 mMapGen;

    private int mStatus = STATUS_INIT;

    private CalculateReceiver mCalculateReceiver;

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void justPermission() {

    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationale(final PermissionRequest request) {
        Snackbar.make(findViewById(android.R.id.content), "Allow write external storage", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", v -> request.proceed()).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        MainActivityPermissionsDispatcher.justPermissionWithPermissionCheck(this);

        final SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        ThreadState.delay = sharedPreferences.getInt(PREF_DELAY, 500);
        editTextDelay.setText(String.valueOf(ThreadState.delay));

        if (savedInstanceState != null) {
            mWidth = savedInstanceState.getInt(STATE_WIDTH);
            mHeight = savedInstanceState.getInt(STATE_HEIGHT);

            int status = savedInstanceState.getInt(STATE_STATUS);
            setStatus(status);

        } else {
            mWidth = sharedPreferences.getInt(PREF_WIDTH, 0);
            mHeight = sharedPreferences.getInt(PREF_HEIGHT, 0);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String action = getIntent().getAction();

            mMaxSolution = extras.getInt(CalculateReceiver.EXTRA_SOLUTION_COUNT);
            mMaxGunman = extras.getInt(CalculateReceiver.EXTRA_GUNMEN_COUNT);

            if (CalculateIntentService.ACTION_PROGRESS.equals(action)) {
                String mapString = extras.getString(CalculateReceiver.EXTRA_MAP);
                if (mapString != null)
                    parseMap(mapString);

                setStatus(STATUS_CALCULATING);
            } else if (CalculateIntentService.ACTION_FINISHED.equals(action)) {
                mSolutions = extras.getStringArrayList(CalculateReceiver.EXTRA_SOLUTION_LIST);

                setStatus(STATUS_FINISHED);
            }

            refresh();
        }

        if (mWidth > 0 && mHeight > 0) {
            buttonInitMap.setVisibility(View.VISIBLE);

            generateMap();
        }

        if (sharedPreferences.contains(PREF_MAP)) {
            String mapString = sharedPreferences.getString(PREF_MAP, "");

            parseMap(mapString);

            drawMap(mMap);
        }

        if (sharedPreferences.contains(PREF_SOLUTION_LIST)) {
            mMaxGunman = sharedPreferences.getInt(PREF_MAX_GUNMEN, 0);
            mMaxSolution = sharedPreferences.getInt(PREF_MAX_SOLUTIONS, 0);
            Set<String> solutions = sharedPreferences.getStringSet(PREF_SOLUTION_LIST, null);
            if (solutions != null) {
                mSolutions = new ArrayList<>(solutions);
            }
        }

        IntentFilter intentFilter = new IntentFilter(CalculateReceiver.ACTION_GUNMEN_PROGRESS);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction(CalculateReceiver.ACTION_GUNMEN_FINISHED);
        intentFilter.addAction(CalculateReceiver.ACTION_GUNMEN_CANCELED);
        intentFilter.addAction(CalculateReceiver.ACTION_GUNMEN_PARITY_FAILED);

        mCalculateReceiver = new CalculateReceiver(this);
        registerReceiver(mCalculateReceiver, intentFilter);
    }

    public void setStatus(int status) {

        mStatus = status;

        if (status == STATUS_INIT) {
            buttonInitSize.setEnabled(true);
            buttonCalculate.setVisibility(View.VISIBLE);
            buttonCalculate.setEnabled(false);
            buttonCancel.setVisibility(View.INVISIBLE);
            buttonBrowseSolutions.setVisibility(View.GONE);
            buttonInitMap.setVisibility(View.VISIBLE);
            buttonInitMap.setEnabled(true);
            buttonPause.setEnabled(false);
        } else if (status == STATUS_READY) {
            buttonInitSize.setEnabled(true);
            buttonCalculate.setVisibility(View.VISIBLE);
            buttonCalculate.setEnabled(true);
            buttonCancel.setVisibility(View.INVISIBLE);
            buttonBrowseSolutions.setVisibility(View.GONE);
            buttonInitMap.setVisibility(View.VISIBLE);
            buttonInitMap.setEnabled(true);
            buttonPause.setEnabled(false);
        } else if (status == STATUS_CALCULATING) {
            buttonBrowseSolutions.setVisibility(View.GONE);
            buttonCalculate.setVisibility(View.INVISIBLE);
            buttonInitSize.setEnabled(false);
            buttonCancel.setVisibility(View.VISIBLE);
            buttonCalculate.setEnabled(true);
            buttonInitMap.setVisibility(View.VISIBLE);
            buttonInitMap.setEnabled(false);
            buttonPause.setEnabled(true);
        } else if (status == STATUS_CANCEL) {
            buttonInitSize.setEnabled(true);
            buttonCalculate.setEnabled(true);
            buttonCalculate.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.INVISIBLE);
            buttonBrowseSolutions.setVisibility(View.GONE);
            buttonInitMap.setVisibility(View.VISIBLE);
            buttonInitMap.setEnabled(true);
            buttonPause.setEnabled(false);
        } else if (status == STATUS_FINISHED) {
            buttonInitSize.setEnabled(true);
            buttonCalculate.setEnabled(true);
            buttonCalculate.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.INVISIBLE);
            buttonBrowseSolutions.setVisibility(View.VISIBLE);
            buttonInitMap.setVisibility(View.VISIBLE);
            buttonInitMap.setEnabled(true);
            buttonPause.setEnabled(false);
        }
    }

    public void initSize(View view) {
        Intent intent = new Intent(this, InitSizeActivity.class);

        if (mWidth > 0 && mHeight > 0) {
            intent.putExtra(InitSizeActivity.EXTRA_WIDTH, mWidth);
            intent.putExtra(InitSizeActivity.EXTRA_HEIGHT, mHeight);
        }

        startActivityForResult(intent, REQUEST_INITIALIZE);
    }

    public void initMap(View view) {
        Intent intent = new Intent(this, InitMapActivity.class);
        intent.putExtra(InitMapActivity.EXTRA_WIDTH, mWidth);
        intent.putExtra(InitMapActivity.EXTRA_HEIGHT, mHeight);
        if (mMap != null) {
            String mapString = parseMap();
            intent.putExtra(InitMapActivity.EXTRA_MAP, mapString);
        }
        startActivityForResult(intent, REQUEST_MAP);
    }

    public void calculate(View view) {
        setStatus(STATUS_CALCULATING);

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

                    setStatus(STATUS_FINISHED);
                }
            }
        };

        mMapGen.setDelay(Integer.valueOf(editTextDelay.getText().toString()));

        mMapGen.execute(mMap);
    }

    public void calculateService(View view) {
        mParityFailCount = 0;

        setStatus(STATUS_CALCULATING);

        saveDelayPreferences();

        CalculateIntentService.startCalculate(this, mWidth, mHeight, mMap);
    }

    private void saveDelayPreferences() {
        ThreadState.delay = Integer.valueOf(editTextDelay.getText().toString());

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_DELAY, ThreadState.delay);
        editor.commit();
    }

    private void saveSizePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PREF_WIDTH, mWidth);
        editor.putInt(PREF_HEIGHT, mHeight);
        editor.commit();
    }

    private void saveMapPreferences() {
        String mapString = parseMap();

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_MAP, mapString);
        editor.commit();
    }

    private void saveResult(boolean clear) {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (clear) {
            editor.remove(PREF_MAX_GUNMEN);
            editor.remove(PREF_MAX_SOLUTIONS);
            editor.remove(PREF_SOLUTION_LIST);
        } else {
            editor.putInt(PREF_MAX_GUNMEN, mMaxGunman);
            editor.putInt(PREF_MAX_SOLUTIONS, mMaxSolution);

            Set<String> solutions = new HashSet<>();
            solutions.addAll(mSolutions);
            editor.putStringSet(PREF_SOLUTION_LIST, solutions);
        }

        editor.commit();
    }

    public void cancelCalculate(View view) {
        if (mMapGen != null)
            mMapGen.cancel(true);

        setStatus(STATUS_CANCEL);
    }

    public void cancelCalculateService(View view) {
//        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
//
//        Iterator<ActivityManager.RunningAppProcessInfo> iter = runningAppProcesses.iterator();
//
//        while (iter.hasNext()) {
//            ActivityManager.RunningAppProcessInfo next = iter.next();
//
//            String pricessName = getPackageName();//+ ":CalculateIntentService";
//
//            if (next.processName.equals(pricessName)) {
//                try {
//                    Process.killProcess(next.pid);
//                } catch (Exception e) {
//                    Log.e("DUDIDAM", e.getMessage());
//                }
//                break;
//            }
//        }
//
//        setStatus(STATUS_CANCEL);
        ThreadState.isRequestCancel = true;
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
        mParityFailCount = 0;

        textViewSolutions.setText(getString(R.string.solution_count, 0));
        textViewGunMen.setText(getString(R.string.gunmen_count, 0));
        buttonBrowseSolutions.setVisibility(View.GONE);
        textViewParityFail.setText(getString(R.string.parity_fail, mParityFailCount));
    }

    private String parseMap() {
        String stringMap = "";

        if (mMap != null) {
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    stringMap += mMap[j][i];
                }
            }
        }

        return stringMap;
    }

    public void pauseProccess(View view) {
        ThreadState.isPaused = !ThreadState.isPaused;

        if (ThreadState.isPaused)
            buttonPause.setText(getString(R.string.cont));
        else
            buttonPause.setText(getString(R.string.pause));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INITIALIZE && resultCode == RESULT_OK) {
            if (data != null) {
                mWidth = data.getIntExtra(InitSizeActivity.EXTRA_WIDTH, 0);
                mHeight = data.getIntExtra(InitSizeActivity.EXTRA_HEIGHT, 0);

                mMap = null;
                saveMapPreferences();

                saveSizePreferences();

                initMap(null);

                setStatus(STATUS_INIT);

                buttonInitMap.setVisibility(View.VISIBLE);

                generateMap();
            }
        } else if (requestCode == REQUEST_MAP) {
            if (resultCode == RESULT_OK && data != null) {
                String mapString = data.getStringExtra(InitMapActivity.EXTRA_MAP);

                parseMap(mapString);

                drawMap(mMap);

                reset();

                saveMapPreferences();

                setStatus(STATUS_READY);
            }
        }
    }

    private void parseMap(String mapString) {
        mMap = new int[mWidth][mHeight];

        int index = 0;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                String stateString = mapString.substring(index, index + 1);
                mMap[j][i] = Integer.valueOf(stateString);
                index++;
            }
        }
    }

    @Override
    public void onProgress(int[][] solution, int solutionCount, int maxGunmen) {
        mMap = solution;
        mMaxSolution = solutionCount;
        mMaxGunman = maxGunmen;
        refresh();
    }

    @Override
    public void onFinished(int maxGunmen, int maxSolution, ArrayList<String> solutions, int width, int height) {
        mMaxSolution = maxSolution;
        mMaxGunman = maxGunmen;
        mSolutions = solutions;

        mWidth = width;
        mHeight = height;

        refresh();
        showResult(maxSolution, maxGunmen);

        setStatus(STATUS_FINISHED);
    }

    @Override
    public void onCanceled() {
        setStatus(STATUS_CANCEL);

        ThreadState.isRequestCancel = false;
    }

    @Override
    public void onParityFailed() {
        mParityFailCount++;

        refresh();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_WIDTH, mWidth);
        outState.putInt(STATE_HEIGHT, mHeight);
        outState.putInt(STATE_STATUS, mStatus);
    }

    private void refresh() {
        textViewSolutions.setText(getString(R.string.solution_count, mMaxSolution));
        textViewGunMen.setText(getString(R.string.gunmen_count, mMaxGunman));

        if (mMap != null)
            drawMap(mMap);

        textViewParityFail.setText(getString(R.string.parity_fail, mParityFailCount));
    }

    public void setDelay(View view) {
        saveDelayPreferences();
    }

    public void addDelay(View view) {
        ThreadState.delay += 10;
        editTextDelay.setText(String.valueOf(ThreadState.delay));
        saveDelayPreferences();
    }

    public void reduceDelay(View view) {
        ThreadState.delay -= 10;
        if (ThreadState.delay < 0)
            ThreadState.delay = 0;
        editTextDelay.setText(String.valueOf(ThreadState.delay));
        saveDelayPreferences();
    }
}
