package burstcoin.com.burst;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import java.util.ArrayList;

import burstcoin.com.burst.tools.BurstContext;
import burstcoin.com.burst.tools.WalletTool;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IntProvider {

    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    private GoogleApiClient client;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;
    private NavigationView navigationView;
    private WebView mWebView;
    private DrawerLayout mDrawer;
    private SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    private boolean isAtHome=true;
    private String burstID = "";
    private String numericID = "";
    private String mPassPhrase = "";

    private WalletTool mTheBestWallet;

    public final static String NUMERICID = "burstcoin.com.burst.NUMERICID";
    public final static String PASSPHRASE = "burstcoin.com.burst.PASSPHRASE";
    private final String TAG = "MainActivity";
    final static int PERMISSION_STORAGE = 1;
    final static int PERMISSION_WAKELOCK = 2;

    private int navPath = 0;
    final static int NAV_PLOTTING = 5;
    final static int NAV_MINING = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("burstShared", MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(MainActivity.this);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Load up a Spinner
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Finding Wallet Server");
        progressDialog.show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        //Context.getApplicationInfo().sourceDir;
        //Context.getPackageManager().getInstallerPackageName(packageName);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        // This is where we are putting in the new checks

        mTheBestWallet = BurstContext.getWallet();
        if(mTheBestWallet == null) {
            findBestWallet();
            BurstContext.setWallet(mTheBestWallet);
        }

        String url = "";
        try {
            url = "https://" + mTheBestWallet.getURL() + "/index.html";
        }
        catch (Exception e) { // Force a damn wallet
            WalletTool w  = new WalletTool("wallet1.burstnation.com",8125);
            BurstContext.setWallet(w);
            mTheBestWallet = w;
            url = "https://" + mTheBestWallet.getURL() + "/index.html";
        }
        Log.d(TAG, "Using Wallet:" + url);

        String jsInjection = createBurstJSInjectionPassPhrase();    // This is complete BETA!!!
        loadSite(url, jsInjection);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on position
        switch (menuItem.getItemId()) {

            case R.id.nav_wallet:
                progressDialog.show();
                String jsInjectionWallet = createBurstJSInjectionPassPhrase();
                //loadSite("https://mwallet.burst-team.us:8125/index.html", jsInjectionWallet);
                loadSite("https://"+BurstContext.getWallet().getURL()+"/index.html", jsInjectionWallet);
                isAtHome=true;
                break;
            case R.id.save_passphrase:

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_box_phrase);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setTitle("Save Phrase");

                AppCompatButton buttonSave = (AppCompatButton) dialog.findViewById(R.id.save_phrase);
                AppCompatButton buttonName = (AppCompatButton) dialog.findViewById(R.id.phrase_name);
                AppCompatButton buttonPin = (AppCompatButton) dialog.findViewById(R.id.pin);
                AppCompatButton buttonReenterPin = (AppCompatButton) dialog.findViewById(R.id.reenter_pin);
                ImageView btn_paste = (ImageView)dialog.findViewById(R.id.btn_paste);

                int[] colorList = new int[]{
                        getResources().getColor(R.color.colorAccentPressed),
                        getResources().getColor(R.color.colorAccent)
                };

                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_pressed}, // enabled
                        new int[]{}  // pressed
                };
                ColorStateList csl = new ColorStateList(states, colorList);
                buttonSave.setSupportBackgroundTintList(csl);
                buttonName.setSupportBackgroundTintList(csl);
                buttonPin.setSupportBackgroundTintList(csl);
                buttonReenterPin.setSupportBackgroundTintList(csl);


                final EditText passphrase = (EditText) dialog.findViewById(R.id.edt_passphrase);
                passphrase.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                final EditText namephrase = (EditText) dialog.findViewById(R.id.edt_name);
                final EditText pinphrase = (EditText) dialog.findViewById(R.id.edt_pin);
                final EditText reenter_pin_phrase = (EditText) dialog.findViewById(R.id.edt_reenter_pin);

                btn_paste.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        String pasteData = "";

                        try {
                            // Examines the item on the clipboard. If getText() does not return null, the clip item contains the
// text. Assumes that this application can only handle one item at a time.
                            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

// Gets the clipboard as text.
                            if(item.getText() != null)
                            pasteData = item.getText().toString();

// If the string contains data, then the paste operation is done
                            if (pasteData != null) {

                                passphrase.setText(pasteData);
                                return;

    // The clipboard does not contain text. If it contains a URI, attempts to get data from it
                            } else {
                                Uri pasteUri = item.getUri();

                                // If the URI contains something, try to get text from it
                                if (pasteUri != null) {

                                    // calls a routine to resolve the URI and get data from it. This routine is not
                                    // presented here.
    //                                pasteData = resolveUri(Uri);
                                    return;
                                } else {

                                    // Something is wrong. The MIME type was plain text, but the clipboard does not contain either
                                    // text or a Uri. Report an error.
                                    Log.e("", "Clipboard contains an invalid data type");
                                    return;
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!passphrase.getText().toString().equalsIgnoreCase("")) {
                            dialog.findViewById(R.id.name_layout).setVisibility(View.VISIBLE);
                            namephrase.requestFocus();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter passphrase.", Toast.LENGTH_LONG).show();
                        }
//
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("passphrase",savePhrase.getText().toString());
//                        editor.commit();
                    }
                });

                buttonName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!namephrase.getText().toString().equalsIgnoreCase("")) {
                            dialog.findViewById(R.id.pin_layout).setVisibility(View.VISIBLE);
                            pinphrase.requestFocus();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter name for phrase.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                buttonPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!pinphrase.getText().toString().equalsIgnoreCase("")) {
                            dialog.findViewById(R.id.pin_reenter_layout).setVisibility(View.VISIBLE);
                            reenter_pin_phrase.requestFocus();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter pin for phrase.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                buttonReenterPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!reenter_pin_phrase.getText().toString().equalsIgnoreCase("")) {
                            if (pinphrase.getText().toString().equalsIgnoreCase(reenter_pin_phrase.getText().toString())) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("name", namephrase.getText().toString());
                                contentValues.put("phrase", passphrase.getText().toString());
                                contentValues.put("pin", pinphrase.getText().toString());
                                databaseHandler.insertItem(contentValues, sqLiteDatabase);
                                Toast.makeText(getApplicationContext(), "Your phrase got successfully saved.", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter same pin.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Please re-enter pin.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.load_passphrase:

                Dialog loadPhraseDialog = new Dialog(MainActivity.this);
                loadPhraseDialog.setContentView(R.layout.dialog_box_load_passphrase);
                loadPhraseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                loadPhraseDialog.setTitle("Load Passphrase:");
                RecyclerView recyclerView = (RecyclerView) loadPhraseDialog.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                ArrayList<DataModel> dataModels = databaseHandler.getItems(sqLiteDatabase);
                if (dataModels != null) {
                    if (dataModels.size() > 0) {
                        if (dataModels != null) {
                            RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this,dataModels,loadPhraseDialog, databaseHandler );
                            recyclerView.setAdapter(recyclerViewAdapter);
                        }
                        loadPhraseDialog.show();
                    } else {
                        Toast.makeText(getApplicationContext(), "You haven't added any passphrases yet.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You haven't added any passphrase yet.", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.burst_crowd:
                progressDialog.show();
                mWebView.loadUrl("https://mwallet.burst-team.us:8125/atcrowdfund_mobile.html");
                mWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        progressDialog.cancel();
                    }

                });
                // fragmentClass = ThirdFragment.class;
                isAtHome=false;
                break;

            case R.id.fauet:
                progressDialog.show();
                String url = "https://faucet.burst-team.us";
                if(!burstID.isEmpty()) {
                    String jsInjectionFaucet = "javascript: (function(){document.getElementById('accountId').value='"+burstID+"';})();";
                    loadSite(url, jsInjectionFaucet);
                } else {
                    loadSite(url);
                }
                isAtHome=false;
                // fragmentClass = ThirdFragment.class;
                break;

            case R.id.nav_plotting:
                    if (burstID.isEmpty())
                        Toast.makeText(getApplicationContext(),"Please login so we can obtain your Burst ID",Toast.LENGTH_LONG).show();
                    else if (numericID.isEmpty()){
                        Toast.makeText(getApplicationContext(),"Please login so we can obtain your numeric ID for plotting",Toast.LENGTH_LONG).show();
                    } else {
                        navPath = 5;
                        if (isStoragePermissionGranted() && isPowerManagerPermissionGranted()) {
                            menuNavigator();
                        } else {
                            // We need to ask for permissions
                            requestStoragePermission();
                        }
                    }
                break;

            case R.id.nav_mining:
                if (burstID.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please login so we can obtain your Burst ID",Toast.LENGTH_LONG).show();
                else if (numericID.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please login so we can obtain your numeric ID for plotting",Toast.LENGTH_LONG).show();
                } else {
                    navPath = 6;
                    if (isStoragePermissionGranted() && isPowerManagerPermissionGranted()) {
                        menuNavigator();
                        break;
                    } else {
                        // We need to ask for permissions
                        requestStoragePermission();
                    }
                }
                break;
            case R.id.nav_settings: // User Preferences
                Intent prefIntent = new Intent(this, PrefActivity.class);
                prefIntent.putExtra(NUMERICID, numericID);
                startActivity(prefIntent);

                isAtHome = false;
                break;


            default:

        }
        mDrawer.closeDrawers();
    }

    private void menuNavigator() {
        switch (navPath) {
            case NAV_PLOTTING:
                Intent plotIntent = new Intent(this, PlotterActivity.class);
                plotIntent.putExtra(NUMERICID, numericID);
                startActivity(plotIntent);
                isAtHome = false;
              break;
            case  NAV_MINING:
                Intent miningIntent = new Intent(this, MiningActivity.class);
                miningIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //miningIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                miningIntent.putExtra(NUMERICID, numericID);
                miningIntent.putExtra(PASSPHRASE, mPassPhrase);
                startActivity(miningIntent);
                isAtHome = false;
              break;
        }
    }

    // This is how Data is passed back to main via the IntProvider Class
    @Override
    public void notice(String... args){
        /*
         * args[0] is probably the identified
         * args[1] is probably a status message, SUCCESS or something else, typically an error code
         */
        switch(args[0]) {
            case "GOTBURSTID":
                if (args[1].contains("SUCCESS"))
                    this.burstID = args[2];
                    this.numericID = BurstUtil.getNumericIDFromLocal(burstID, this);
                    if (numericID.equals("")) {
                        BurstUtil fetchNumericID = new BurstUtil(this);
                        fetchNumericID.getNumericIDFromBurstID(burstID, this);
                    }
                break;
            case "GOTNUMERICID":
                if (args[1].contains("SUCCESS"))
                    this.numericID = args[2];
                else
                    this.numericID = "";
                break;
            case "PASSPHRASE":
                    mPassPhrase = args[1];
                break;
        }
        String debug = "";
        for (String s : args)
            debug = debug + " " + s;
        Log.d(TAG, debug);

        //Toast.makeText(getApplicationContext(),"We got a notice back from BurtUtils: "+BurstUtil.getNumericIDFromLocal(burstID, this),Toast.LENGTH_LONG).show();
    }

    public void showToast(View view) {
      //  Toast.makeText(this, "Passphrase saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(isAtHome){
                super.onBackPressed();
            }else{
                isAtHome=true;
                progressDialog.show();
                //mWebView.loadUrl("https://mwallet.burst-team.us:8125/index.html");
                mWebView.loadUrl("https://"+BurstContext.getWallet().getURL()+"/index.html");
                mWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        progressDialog.cancel();
                    }

                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        TextView txtWalletURL = (TextView) findViewById(R.id.textBurstURL);
        txtWalletURL.setText(mTheBestWallet.getURL());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);
        return true;
    }

    // Generic Site Loader Prototype
    private void loadSite(String url){
        loadSite(url, "");
    }

    // Generic Site Loader Final with JSInjection
    private void loadSite(String url, final String jsInjection) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        /*
        ToDo: Enhance this section by SDK Version
        On API Level 19+, use evaluateJavascript().
        On API Level 18 and below, use loadUrl("javascript:"),
            using the same basic syntax used for bookmarklets on desktop browsers.
         */
        mWebView.loadUrl(url);
        mWebView.addJavascriptInterface(new JSInterface(this), "Android");
        mWebView.setWebViewClient(new WebViewClient(){
            @SuppressWarnings("deprecation")
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "ScriptWas:"+jsInjection);
                if (!jsInjection.isEmpty())
                    mWebView.loadUrl(jsInjection);
                //else
                //    mWebView.loadUrl(url);
                progressDialog.cancel();
            }
        });
    }

    // This is brutal, dont mess it up!
    /* Old version pre-passphrase capture
    private String createBurstJSInjection() {
        String jsInjection = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    // DOM Level 4 - Android 4.4 and greater
        jsInjection = "javascript:var options = {subtree: true, childList: true, attributes: true, characterData: true};" +
                       "try { " +
                         "var account = $('#account_id')[0];" +
                         "var observer = new MutationObserver( function(mutations) {" +
                         "mutations.forEach(function (mutation) {" +
                           "Android.getBurstID(account.innerHTML);" +
                         "})" +
                       "});" +
                       "observer.observe(account, options);" +
                       "} catch (err) { Android.getBurstID('error:' + err) }";
        }
        else {
            jsInjection =
                    "javascript:" +
                            "Android.getBurstID('Hello');" +  // <-- This does not work
                    "try { " +
                      "var account = document.getElementById('account_id')[0];"+
                            "Android.getBurstID('Pre 4.4');" +    // <-- This is not ffiring
                      "account.addEventListener('DOMCharacterDataModified', function(e) {" +  // DOMSubtreeModified or DOMAttrModified or DOMCharacterDataModified
                        "Android.getBurstID(document.getElementById('account_id).innerHTML);" +
                      "}, false);" +
                    "} catch (err) { Android.getBurstID('error:' + err) " +
                    "}";
        }
        return jsInjection;
    }
    */

    private String createBurstJSInjectionPassPhrase() {
        String jsInjection = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    // DOM Level 4 - Android 4.4 and greater
            jsInjection = "javascript:var options = {subtree: true, childList: true, attributes: true, characterData: true};" +
                    "try { " +
                    "var account = $('#account_id')[0];" +
                    "var observer = new MutationObserver( function(mutations) {" +
                    "mutations.forEach(function (mutation) {" +
                    "Android.getBurstID(account.innerHTML);" +
                    "})" +
                    "});" +
                    "observer.observe(account, options);" +
                    "var passphrase = document.getElementById('login_password');" +
                    "passphrase.addEventListener('input', function() {" +
                    "Android.getPassPhrase(passphrase.value); });" +
                    "} catch (err) { Android.getBurstID('error:' + err) }";
            // This crashes J/S before we start typing
        }
        else {        // ToDo: DOM Level 3 - MutationEvent (DOES NOT WORK YET)
            jsInjection =
                    "javascript:" +
                            "Android.getBurstID('Hello');" +  // <-- This does not work
                            "try { " +
                            "var account = document.getElementById('account_id')[0];"+
                            "Android.getBurstID('Pre 4.4');" +    // <-- This is not ffiring
                            "account.addEventListener('DOMCharacterDataModified', function(e) {" +  // DOMSubtreeModified or DOMAttrModified or DOMCharacterDataModified
                            "Android.getBurstID(document.getElementById('account_id).innerHTML);" +
                            "}, false);" +
                            "} catch (err) { Android.getBurstID('error:' + err) " +
                            "}";
        }
        return jsInjection;
    }

    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK}, PERMISSION_STORAGE);
        // do I also need to put WAKE_LOCK in here?
        //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WAKE_LOCK},PERMISSION_WAKELOCK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Log.v(TAG, "Permission Denied: STORAGE");
                    // Lets tell them we can't work without this
                    return;
                }            }
            case PERMISSION_WAKELOCK: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // Lets tell them we can't work without this
                    Log.v(TAG, "Permission Denied: WAKELOCK");
                    return;
                }
            }
        }
        menuNavigator();
    }

    private boolean isPowerManagerPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WAKE_LOCK)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"WAKE_LOCK Permission is granted");
                return true;
            } else {
                Log.v(TAG,"WAKE_LOCK Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WAKE_LOCK}, PERMISSION_WAKELOCK);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
    private void findBestWallet() {
        WalletTool mStaticWallets[] = {
                new WalletTool("wallet1.burstnation.com",8125),
                new WalletTool("wallet2.burstnation.com",8125),
                new WalletTool("wallet3.burstnation.com",8125),
                new WalletTool("wallet.burst-team.us")};

        mTheBestWallet = null;
        long h;
        long sp = 999999999;
        h = 0;

        for (WalletTool w : mStaticWallets ) {
            w.GetHeight();
            if (w.Height > h) {
                h = w.Height;
                mTheBestWallet = w;
                Log.d(TAG,"Set New Wallet based on new Height");
            }
            // if (w.Height == h && w.GetSpeed() < sp && w.GetSpeed() != 0) {
            if (w.Height == h && w.GetSpeed() < sp ) {
                sp = w.GetSpeed();
                mTheBestWallet = w;
                Log.d(TAG,"Set New Wallet based on Speed");
            }
            Log.d(TAG, "Checking Wallet " + w.getURL() + " height:" + h + " speed was:" + w.GetSpeed()); // Add URL, add Speed result, we want the lowest speed number
        }
    }
}

