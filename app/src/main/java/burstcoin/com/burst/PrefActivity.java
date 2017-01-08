package burstcoin.com.burst;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class PrefActivity extends AppCompatActivity {

    private CheckBox mBoxSoundEffect;
    private EditText mTxtPoolTimer;
    private Button mBtnDone;
    private Button mBtnSetPool;
    private Spinner mSpinnerPools;

    private int mPoolTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences settings = getSharedPreferences("MINING", 0);

        mBtnDone = (Button) findViewById(R.id.btnDone);

        mBoxSoundEffect = (CheckBox) findViewById(R.id.cbSound);
        boolean mCHACHING = settings.getBoolean("CHACHING", false);
        mBoxSoundEffect.setChecked(mCHACHING);
        mBoxSoundEffect.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is where we toggle and write it to the preferences
                SharedPreferences settings = getSharedPreferences("MINING", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("CHACHING", mBoxSoundEffect.isPressed());

                // Commit the edits!
                editor.commit();
            }
        });

        mTxtPoolTimer = (EditText)findViewById(R.id.txtSecondsPoll);
        mPoolTimer = settings.getInt("POLLTIMER", 3);
        mTxtPoolTimer.setText(Integer.toString(mPoolTimer));
        mTxtPoolTimer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                SharedPreferences settings = getSharedPreferences("MINING", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("POLLTIMER", Integer.parseInt(charSequence.toString()));
                editor.commit();
                } catch (NumberFormatException e) {
                    // Don't care this is maybe empty or something else thats NaN
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //mBtnSetPool = (Button) findViewById(R.id.btnSetPool);

        // Setup the Close Function
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Populate the Drop Down of sites
        //mSpinnerPools = (Spinner) findViewById(R.id.spinnerPool);
        //populatePoolList();
    }

    private void setPrivateValue (String name) {

    }

    private void populatePoolList() {

    }

}
