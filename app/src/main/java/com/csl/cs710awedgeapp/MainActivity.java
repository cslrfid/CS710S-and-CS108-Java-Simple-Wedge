package com.csl.cs710awedgeapp;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csl.cs710awedgeapp.DrawerListContent.DrawerPositions;
import com.csl.cs710awedgeapp.fragments.*;
import com.csl.cs710library4a.CsLibrary4A;
import com.csl.cslibrary4a.ReaderDevice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    final boolean DEBUG = false; final String TAG = "Hello";
    public static boolean activityActive = false;
    public static DrawerPositions drawerPositionsDefault = DrawerPositions.DIRECTWEDGE;

    //Tag to identify the currently displayed fragment
    Fragment fragment = null;
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";

    public static TextView mLogView;
    private CharSequence mTitle;

    public static Context mContext;
    public static CsLibrary4A csLibrary4A;
    public static SharedObjects sharedObjects;
    public static SensorConnector mSensorConnector;
    public static ReaderDevice tagSelected;

    public static String mDid; public static int selectHold; public static int selectFor;
    public static class Config {
        public String configPassword, configPower, config0, config1, config2, config3;
    };
    public static Config config  = new Config();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            if (savedInstanceState == null) Log.i(TAG, "MainActivity.onCreate: NULL savedInstanceState");
            else Log.i(TAG, "MainActivity.onCreate: VALID savedInstanceState");
        }

        setContentView(R.layout.activity_main);

        mTitle = getTitle();

        mLogView = (TextView) findViewById(R.id.log_view);

        mContext = this;
        sharedObjects = new SharedObjects(mContext);
        csLibrary4A = new CsLibrary4A(mContext, mLogView);
        mSensorConnector = new SensorConnector(mContext);

        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) selectItem(drawerPositionsDefault);
        if (true) Log.i(TAG, "MainActivity.onCreate.onCreate: END");
        loadWedgeSettingFile();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MainActivity.csLibrary4A.connect(null);
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityActive = true; wedged = false;
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onResume()");
    }

    @Override
    protected void onPause() {
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onPause()");
        activityActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) csLibrary4A.appendToLog("MainActivity.onDestroy()");
        if (true) { csLibrary4A.disconnect(true); }
        //csLibrary4A = null;
        super.onDestroy();
    }

    private void selectItem(DrawerPositions position) {
        if (DEBUG) Log.i(TAG, "MainActivity.selectItem: position = " + position);
        switch (position) {
            case ABOUT:
                fragment = new AboutFragment();
                break;
            case DIRECTWEDGE:
                fragment = new DirectWedgeFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (position == drawerPositionsDefault) {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Don't add the transaction to back stack since we are navigating to the first fragment
            //being displayed and adding the same to the backstack will result in redundancy
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        } else {
            //Pop the back stack since we want to maintain only one level of the back stack
            //Add the transaction to the back stack since we want the state to be preserved in the back stack
            //fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public static boolean permissionRequesting;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("permissionRequesting: requestCode = " + requestCode + ", permissions is " + (permissions == null ? "null" : "valid") + ", grantResults is " + (grantResults == null ? "null" : "valid") );
        if (DEBUG) MainActivity.csLibrary4A.appendToLog("permissionRequesting: permissions[" + permissions.length + "] = " + (permissions != null && permissions.length > 0 ? permissions[0] : ""));
        if (grantResults != null && grantResults.length != 0) {
            boolean bNegative = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] < 0) bNegative = true;
                if (DEBUG) csLibrary4A.appendToLog("permissionRequesting: grantResults[" + i + "] = " + grantResults[i] );
            }
            if (bNegative) {
                Toast toast = Toast.makeText(this, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT);
                if (false) toast.setGravity(Gravity.TOP | Gravity.RIGHT, 100, 200);
                toast.show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionRequesting = false;
    }

    public void privacyClicked(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("https://www.convergence.com.hk/apps-privacy-policy"));
        startActivity(intent);
    }

    public void aboutClicked(View view) { selectItem(DrawerPositions.ABOUT); }
    public static boolean wedged = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) csLibrary4A.appendToLog("onNewIntent !!! intent.getAction = " + intent.getAction());
        readFromIntent(intent);
    }
    private void readFromIntent(Intent intent) {
        if (DEBUG) csLibrary4A.appendToLog("onNewIntent !!! readFromIntent entry");
        String action = intent.getAction();
    }

    public static String fileName = "csReaderA_SimpleWedge";
    public static String wedgePrefix = null, wedgeSuffix = null;
    public static int wedgeDelimiter = 0x0a, wedgePower = 300;
    void loadWedgeSettingFile() {
        File path = this.getFilesDir();
        File file = new File(path, fileName);
        boolean bNeedDefault = true, DEBUG = false;
        MainActivity.csLibrary4A.appendToLog(fileName + "file.exists = " + file.exists());
        if (file.exists()) {
            int length = (int) file.length();
            byte[] bytes = new byte[length];
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(instream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (true) csLibrary4A.appendToLog("Data read = " + line);
                        String[] dataArray = line.split(",");
                        if (dataArray.length == 2) {
                            if (dataArray[0].matches("wedgePower")) {
                                wedgePower = Integer.valueOf(dataArray[1]);
                            } else if (dataArray[0].matches("wedgePrefix")) {
                                wedgePrefix = dataArray[1];
                            } else if (dataArray[0].matches("wedgeSuffix")) {
                                wedgeSuffix = dataArray[1];
                            } else if (dataArray[0].matches("wedgeDelimiter")) {
                                wedgeDelimiter = Integer.valueOf(dataArray[1]);
                            }
                        }
                    }
                }
                instream.close();
            } catch (Exception ex) {
                //
            }
        }
    }
}
