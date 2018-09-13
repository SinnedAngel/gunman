package arm.man.gunmen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SolutionBrowserActivity extends AppCompatActivity {
    public static final String EXTRA_SOLUTIONS = "extra_solutions";
    public static final String EXTRA_WIDTH = "extra_width";
    public static final String EXTRA_HEIGHT = "extra_height";

    private List<String> mSolutions;

    private ArrayAdapter<String> adapter;

    private int mWidth;
    private int mHeight;

    @BindView(R.id.list_solutions)
    ListView listViewSolution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution_browser);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mWidth = extras.getInt(EXTRA_WIDTH);
            mHeight = extras.getInt(EXTRA_HEIGHT);

            mSolutions = extras.getStringArrayList(EXTRA_SOLUTIONS);
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(mSolutions);
        listViewSolution.setAdapter(adapter);

        listViewSolution.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showSolution(position);
            }
        });
    }

    public void showSolution(int position) {
        String solution = adapter.getItem(position);


        Intent intent = new Intent(this, SolutionViewerActivity.class);
        intent.putExtra(SolutionViewerActivity.EXTRA_MAP, solution);
        intent.putExtra(SolutionViewerActivity.EXTRA_WIDTH, mWidth);
        intent.putExtra(SolutionViewerActivity.EXTRA_HEIGHT, mHeight);

        startActivity(intent);
    }
}
