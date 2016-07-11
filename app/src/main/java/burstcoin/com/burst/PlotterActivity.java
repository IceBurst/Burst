package burstcoin.com.burst;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.TreeMap;

public class PlotterActivity extends AppCompatActivity {

    private Plotter plotter;

    private TreeMap<String, String> mMiningPools = null;
    private TextView mTxtDriveInfo;
    private TextView mTxtTest;
    private TextView mTxtDriveMessage;
    private Button mBtnDone;

    private String numericID;
    private double mTotalSpace = 0;
    private double mFreeSpace = 0;
    private final static String TAG = "PlotterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotter);
        numericID = getIntent().getStringExtra(MainActivity.NUMERICID);

        // Enable the Done button
        mBtnDone = (Button) findViewById(R.id.btnDone);
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        // Lets update the Screen with current System/Plot Information
        mTxtDriveInfo =  (TextView) findViewById(R.id.txtDriveInfo);
        updateDriveInfo();

        mTxtDriveMessage =  (TextView) findViewById(R.id.txtDriveMessage);
        updateDriveMessage();

        // below here is all pilot code that is a work in progress
        mTxtTest = (TextView) findViewById(R.id.txtTestHolder);
        String mFreeMem = Long.toString(new BurstUtil().getFreeMemoryInMB(this));
        String mTotalMem = Long.toString(new BurstUtil().getTotalMemoryInMB(this));
        mTxtTest.setText("Free Memory "+mFreeMem+"MB of "+mTotalMem+"MB");
        updateCurrentPlotInfo();

        // This might be best in the Mining Activity
        loadMiningPools();

    }

    // We should pass in the Text box to update
    private void updateDriveInfo() {
        mTotalSpace = BurstUtil.getTotalSpaceInGB();
        String mStringTotalSpace = Double.toString(mTotalSpace);
        mFreeSpace =  BurstUtil.getFreeSpaceInGB();
        String mStringFreeSpace = Double.toString(mFreeSpace);
        mTxtDriveInfo.setText(mStringFreeSpace+"GB Free of "+ mStringTotalSpace+"GB Total");
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.barDriveUseage);
        // Just Trying this out
        mProgressBar.setMax((int)8);
        mProgressBar.setProgress((int)2);
        //android:max
        //android:progress
    }
    // Best Memory sizes for plotting are things that break nicely into 1GB, powers of 2.
    // Try to target for 512mb of RAM, less is OK

    private void updateDriveMessage() {
        if (mFreeSpace < 1)
            mTxtDriveMessage.setText("Not Enough Space to Plot");
        else
            mTxtDriveMessage.setText("");
    }

    private void updateCurrentPlotInfo() {

    }

    // This should probably be in the Mining section so we can check/Set reward assignment
    // We can also later show the user which pool they are tied to if we know about it
    private void loadMiningPools( ){
        // Later this should get data from an External Source
        mMiningPools = null;
        mMiningPools = new TreeMap<String, String>();
        mMiningPools.put("pool.burst-team.us","BURST-32TT-TSAC-HTKW-CC26C");
        // Add more pools just like above

        /*
        Pool.Burstcoin.de / pool.Burstcoin.uk
        Recipient: BURST-GHTV-7ZP3-DY4B-FPBFA
        URL: http://pool.burstcoin.de (Port 8080 )
        */
    }

    // Things to Implement in the future
    //Plotter p = new Plotter(this);
    //p.plot1GB();

}
