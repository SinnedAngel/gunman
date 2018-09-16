package arm.man.gunmen;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InitSizeActivity extends AppCompatActivity {
    public static final String EXTRA_WIDTH = "extra_width";
    public static final String EXTRA_HEIGHT = "extra_height";

    @BindView(R.id.edit_width)
    EditText editTextWidth;

    @BindView(R.id.edit_height)
    EditText editTextHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_size);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int width = extras.getInt(EXTRA_WIDTH);
            int height = extras.getInt(EXTRA_HEIGHT);

            editTextWidth.setText(String.valueOf(width));
            editTextHeight.setText(String.valueOf(height));
        }
    }

    public void done(View view) {
        Intent result = new Intent();

        int width = 0;
        int height = 0;

        width = Integer.valueOf(editTextWidth.getText().toString());
        height = Integer.valueOf(editTextHeight.getText().toString());

        result.putExtra(EXTRA_WIDTH, width);
        result.putExtra(EXTRA_HEIGHT, height);

        setResult(RESULT_OK, result);
        finish();
    }
}
