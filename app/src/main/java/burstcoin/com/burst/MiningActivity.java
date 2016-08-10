package burstcoin.com.burst;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import burstcoin.com.burst.mining.MiningService;

public class MiningActivity extends AppCompatActivity {

    //ivate Button
    private TextView mTxtMiningHolder;
    private MiningService mMiningService;
    private String mNumericID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mining);

        mNumericID = getIntent().getStringExtra(MainActivity.NUMERICID);
        mTxtMiningHolder =  (TextView) findViewById(R.id.txtMiningHolder);

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
        });
        */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // This is just to try crap out
        mMiningService = new MiningService();
    }

}
