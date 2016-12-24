package burstcoin.com.burst;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class PrefActivity extends AppCompatActivity {

    private CheckBox mBoxSoundEffect;
    private Button mBtnDone;
    private Button mBtnSetPool;
    private Spinner mSpinnerPools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBtnDone = (Button) findViewById(R.id.btnDone);
        mBoxSoundEffect = (CheckBox) findViewById(R.id.cbSound);
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
