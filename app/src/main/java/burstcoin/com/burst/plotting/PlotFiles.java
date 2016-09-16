package burstcoin.com.burst.plotting;

import android.util.Log;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by IceBurst on 7/19/2016.
 */

/*
 * Used to maintain all of the Plot Files stored on the system
 */

public class PlotFiles {

    private ArrayList<PlotFile> mPlotFiles;
    private String mPath;
    private String mNumericID;

    static String TAG = "PlotFiles";

    public PlotFiles(String path, String numericID) {
        mPath = path;
        mNumericID = numericID;
        mPlotFiles = new ArrayList<PlotFile>();
        refreshPlotFiles();
    }

    // Returns the number of plots
    public int size() { return mPlotFiles.size(); }

    // Delete the last plot in the list
    public void deletePlot() {
        if (mPlotFiles.size() > 0) {
            String mDeleteFile = "";
            int mHighestNonce = 0;
            for (PlotFile mPF : mPlotFiles) {
                String mWorkingFile = mPF.getFileName();
                String[] mParts = mWorkingFile.split("_");
                int mCheckingNonce = Integer.parseInt(mParts[1]);
                if (mCheckingNonce >= mHighestNonce) {
                    mHighestNonce = mCheckingNonce;
                    mDeleteFile = mWorkingFile;
                    Log.d(TAG,"New Highest Found:" + mWorkingFile);
                }
            }
            File file = new File(mPath+'/'+mDeleteFile);
            Log.d(TAG, "We deleted:"+mPath+'/'+mDeleteFile);
            file.delete();
        }
        refreshPlotFiles();
    }

    // refresh the PlotFiles from external
    public void rescan() {
        refreshPlotFiles();
    }

    public ArrayList<PlotFile> getPlotFiles() {
        return mPlotFiles;
    }
    // Internal refresh the PlotFiles worker
    private void refreshPlotFiles() {
        mPlotFiles = null;
        mPlotFiles = new ArrayList<PlotFile>();
        String workingFileName = "";
        Log.d("Files", "Path: " + mPath);
        File f = new File(mPath);
        File file[] = f.listFiles();
        // Maybe people are trying to open Mining before Plotting and we have no files
        try {
            Log.d("Files", "Size: " + file.length);
            for (int i = 0; i < file.length; i++) {
                workingFileName = file[i].getName();
                Log.d(TAG, "FileName:" + workingFileName);
                if (workingFileName.contains(mNumericID)) {
                    mPlotFiles.add(new PlotFile(workingFileName, file.length)); // Put it on the stack if it starts with numericID
                    Log.d(TAG, "Found Plot:" + workingFileName);
                }
            }
        }  catch (NullPointerException e) {
            Log.e(TAG, "NULL Pointer Caught");
        }
    }

}
