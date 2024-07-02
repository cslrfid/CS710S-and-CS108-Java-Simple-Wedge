package com.csl.cs710library4a;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static androidx.core.app.ActivityCompat.requestPermissions;

class Utility {
    final boolean DEBUG_PKDATA = false, DEBUG_APDATA = false;
    private Context mContext; private TextView mLogView;
    Utility(Context context, TextView mLogView) {
        mContext = context;
        this.mLogView = mLogView;
    }

    private static long mReferenceTimeMs;
    void setReferenceTimeMs() {
        mReferenceTimeMs = System.currentTimeMillis();
    }
    long getReferencedCurrentTimeMs() { return System.currentTimeMillis() - mReferenceTimeMs; }

    boolean compareByteArray(byte[] array1, byte[] array2, int length) {
        int i = 0;
        if (array1 == null) return false;
        if (array2 == null) return false;
        if (array1.length < length || array2.length < length) {
            return false;
        }
        for (; i < length; i++) {
            if (array1[i] != array2[i]) {
                break;
            }
        }
        return (i == length);
    }

    String byteArray2DisplayString(byte[] byteData) {
        if (false) appendToLog("String0 = " + byteArrayToString(byteData));
        String str = "";
        try {
            str = new String(byteData, "UTF-8");
            str = str.replaceAll("[^\\x00-\\x7F]", "");
            str = str.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (false) appendToLog("String1 = " + str);
        return str;
    }
    String byteArrayToString(byte[] packet) {
        if (packet == null) return "";
        StringBuilder sb = new StringBuilder(packet.length * 2);
        for (byte b : packet) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    int byteArrayToInt(byte[] bytes) {
        int iValue = 0;
        int length = bytes.length;
        if (bytes.length > 4) length = 4;
        for (int i = 0; i < length; i++) {
            iValue = (iValue << 8) + (bytes[i] & 0xFF);
        }
        return iValue;
    }
    private static Handler mHandler = new Handler();
    void appendToLogRunnable(final String s) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                appendToLog(s);
            }
        });
    }
    String appendToLog(String s) {
        String TAG = "";
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        boolean foundMe = false;
        for(int i=0; i<stacktrace.length; i++) {
            StackTraceElement e = stacktrace[i];
            String methodName = e.getMethodName();
            if (methodName.contains("appendToLog")) {
                foundMe = true;
            } else if (foundMe) {
                if (!methodName.startsWith("access$")) {
                    //TAG = String.format(Locale.US, "%s.%s", e.getClassName(), methodName);
                    TAG = String.format(Locale.US, "%s", methodName);
                    break;
                }
            }
        }
        Log.i(TAG + ".Hello", s);
        String string = "\n" + getReferencedCurrentTimeMs() + "." + s;
        return (string);
    }

    void appendToLogView(String s) {
        appendToLog(s);
        String string = "\n" + getReferencedCurrentTimeMs() + "." + s;
        if (Looper.myLooper() == Looper.getMainLooper() && mLogView != null && string != null)   mLogView.append(string);
    }

    private static File fileDebug; private static boolean enableFileDebug = false;
    void debugFileSetup() {
        boolean writeExtPermission = true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                writeExtPermission = false;
                appendToLog("requestPermissions WRITE_EXTERNAL_STORAGE 1");
                requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                if (false) Toast.makeText(mContext, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String errorDisplay = null;
        if (writeExtPermission == false) {
            errorDisplay = "denied WRITE_EXTERNAL_STORAGE Permission !!!";
        } else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false) errorDisplay = "Error in mouting external storage !!!";
        else {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/cs108Java");
            if (path.exists() == false) path.mkdirs();
            if (path.exists() == false) errorDisplay = "Error in making directory !!!";
            else {
                String dateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
                String fileName = "cs108JavaDebug_" + dateTime + ".txt";
                fileDebug = new File(path, fileName);
                if (fileDebug == null) errorDisplay = "Error in making directory !!!";
            }
        }
        if (errorDisplay != null) appendToLog("Error in saving file with " + errorDisplay);
    }
    void debugFileClose() {
        if (fileDebug != null) {
            try {
                MediaScannerConnection.scanFile(mContext, new String[]{fileDebug.getAbsolutePath()}, null, null);
            } catch (Exception ex) {
            }
        }
    }
    void debugFileEnable(boolean enable) { enableFileDebug = enable; }
    void writeDebug2File(String stringDebug) {
        if (fileDebug != null && enableFileDebug) {
            try {
                FileOutputStream outputStreamDebug = new FileOutputStream(fileDebug, true);
                PrintWriter printWriterDebug = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(outputStreamDebug), "UTF-8"));
                printWriterDebug.println(stringDebug);
                printWriterDebug.flush(); printWriterDebug.close();
                outputStreamDebug.close();
            } catch (Exception ex) {
            }
        }
    }

    String getlast3digitVersion(String str) {
        if (str != null) {
            int len = str.length();
            if (len > 3) {
                String strOut = "";
                if (str.substring(len-3, len-2).matches("0")) strOut = str.substring(len-2, len-1);
                else strOut = str.substring(len-3, len-1);
                strOut += "." + str.substring(len-1, len);
                return strOut;
            }
        }
        return null;
    }


    boolean isVersionGreaterEqual(String version, int majorVersion, int minorVersion, int buildVersion) {
        if (version == null) return false;
        if (version.length() == 0) return false;
        String[] versionPart = version.split("[ .,-]+");

        if (versionPart == null) return false;
        try {
            int value = Integer.valueOf(versionPart[0]);
            if (value < majorVersion) return false;
            if (value > majorVersion) return true;

            if (versionPart.length < 2) return true;
            value = Integer.valueOf(versionPart[1]);
            if (value < minorVersion) return false;
            if (value > minorVersion) return true;

            if (versionPart.length < 3) return true;
            value = Integer.valueOf(versionPart[2]);
            if (value < buildVersion) return false;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    double get2BytesOfRssi(byte[] bytes, int index) {
        int iValue = (bytes[index] & 0xFF) * 256 + (bytes[index + 1] & 0xFF);
        if ((iValue & 0x8000) != 0) iValue ^= ~0xFFFF;
        double dValue = iValue;
        return dValue/100;
    }

    float fTemperature_old = -500;
    public float decodeCtesiusTemperature(String strActData, String strCalData) {
        float fTemperature = -500; boolean invalid = false;
        appendToLog("Hello9: strActData = " + strActData + ", strCalData = " + strCalData);
        if (strActData.length() != 8 || strCalData.length() != 8) {
            if (strActData.length() != 8) appendToLogView("Warning: Invalid length of sensing data = " + strActData);
            else appendToLogView("Warning: Invalid length of calibration data = " + strCalData);
            invalid = true;
        }
        else if ((strActData.substring(0, 1).matches("F") && strActData.substring(4, 5).matches("F")) == false) {
            appendToLogView("Warning: Not F header of sensing data = " + strActData);
            invalid = true;
        }
        else {
            String strTemp = strActData.substring(4,8);
            int iTemp = Integer.parseInt(strTemp, 16);
            int iChecksum = 0;
            for (int i=0; i<5; i++, iTemp >>= 3) {
                iChecksum ^= (iTemp & 0x7);
            }
            if (iChecksum != 0) {
                appendToLogView("Warning: Invalid checksum(" + String.valueOf(iChecksum) + ") for strActData = " + strActData);
                invalid = true;
            }
        }
        if (true || invalid == false) {
            int iDelta1 = Integer.parseInt(strCalData.substring(0,4), 16);
            if ((iDelta1 & 0x8000) != 0) { iDelta1 ^= 0xFFFF; iDelta1++; iDelta1 *= -1; }
            appendToLog(String.format("iDelta1 = %d", iDelta1));
            int iVersion = Integer.parseInt(strCalData.substring(4,5), 16);
            appendToLog("Hello9: " + String.format("iDelta1 = %X, iVersion = %X", iDelta1, iVersion));
            float fDelta2 = ((float) iDelta1) / 100 - 101;
            String strTemp = strActData.substring(1,4) + strActData.substring(5,8);
            int iTemp = Integer.parseInt(strTemp, 16);
            int iD1 = ((iTemp & 0xF80000) >> 19);
            int iD2 = ((iTemp & 0x7FFF8) >> 3);
            if (iVersion == 0 || iVersion == 1) fTemperature = (float) (11984.47 / (21.25 + iD1 + iD2 / 2752 + fDelta2) - 301.57);
            else if (iVersion == 2) {
                fTemperature = (float) (11109.6 / (24 + (iD2 + iDelta1)/375.3) - 290);
                if (fTemperature >= 125) fTemperature = (float) (fTemperature * 1.2 - 25);
            } else appendToLogView("Warning: Invalid version " + String.valueOf(iVersion));
            if (invalid) appendToLogView(String.format("Temperature = %f", fTemperature));
        }
        if (fTemperature != -1) fTemperature_old = fTemperature;
        return fTemperature;
    }
    public float decodeMicronTemperature(int iTag35, String strActData, String strCalData) {
        float fTemperature = -1;
        if (strActData == null || strCalData == null) {
        } else if (strActData.length() != 4 || strCalData.length() != 16) {
        } else if (strActData.matches("0000")) {
            fTemperature = fTemperature_old;
        } else if (iTag35 == 3) {
            int calCode1, calTemp1, calCode2, calTemp2;
            int crc = Integer.parseInt(strCalData.substring(0, 4), 16);
            calCode1 = Integer.parseInt(strCalData.substring(4, 7), 16);
            calTemp1 = Integer.parseInt(strCalData.substring(7, 10), 16);
            calTemp1 >>= 1;
            calCode2 = Integer.parseInt(strCalData.substring(9, 13), 16);
            calCode2 >>= 1;
            calCode2 &= 0xFFF;
            calTemp2 = Integer.parseInt(strCalData.substring(12, 16), 16);
            calTemp2 >>= 2;
            calTemp2 &= 0x7FF;

            fTemperature = Integer.parseInt(strActData, 16);
            fTemperature = ((float) calTemp2 - (float) calTemp1) * (fTemperature - (float) calCode1);
            fTemperature /= ((float) (calCode2) - (float) calCode1);
            fTemperature += (float) calTemp1;
            fTemperature -= 800;
            fTemperature /= 10;
        } else if (iTag35 == 5) {
            int iTemp;
            float calCode2 = Integer.parseInt(strCalData.substring(0, 4), 16); calCode2 /= 16;
            iTemp = Integer.parseInt(strCalData.substring(4, 8), 16); iTemp &= 0x7FF; float calTemp2 = iTemp; calTemp2 -= 600; calTemp2 /= 10;
            float calCode1 = Integer.parseInt(strCalData.substring(8, 12), 16); calCode1 /= 16;
            iTemp = Integer.parseInt(strCalData.substring(12, 16), 16); iTemp &= 0x7FF; float calTemp1 = iTemp; calTemp1 -= 600; calTemp1 /= 10;

            fTemperature = Integer.parseInt(strActData, 16);
            fTemperature -= calCode1;
            fTemperature *= (calTemp2 - calTemp1);
            fTemperature /= (calCode2 - calCode1);
            fTemperature += calTemp1;
        }
        if (fTemperature != -1) fTemperature_old = fTemperature;
        return fTemperature;
    }
    float decodeAsygnTemperature(String string) {
        String stringUser5 = string.substring(20, 24); int iUser5 = Integer.valueOf(stringUser5, 16);
        String stringUser6 = string.substring(24, 28); int iUser6 = Integer.valueOf(stringUser6, 16);
        String stringUser1 = string.substring(4, 8); int iUser1 = Integer.valueOf(stringUser1, 16);
        switch (iUser1 & 0xC000) {
            case 0xc000:
                iUser1 &= 0x1FFF; iUser1 /= 8;
                break;
            case 0x8000:
                iUser1 &= 0xFFF; iUser1 /= 4;
                break;
            case 0x4000:
                iUser1 &= 0x7FF; iUser1 /= 2;
                break;
            default:
                iUser1 &= 0x3FF;
                break;
        }
        float temperature = -1;
        appendToLog("input string " + string + ", user1 = " + stringUser1 + ", user5 = " + stringUser5 + ", user6 = " + stringUser6);
        //iUser1 = 495; iUser6 = 3811;
        appendToLog("iUser1 = " + iUser1 + ", iUser5 = " + iUser5 + ", iUser6 = " + iUser6);
        if (iUser5 == 3000) {
            float calibOffset = (float) 3860.27 - (float) iUser6;
            appendToLog("calibOffset = " + calibOffset);
            float acqTempCorrected = (float) iUser1 + calibOffset / 8;
            appendToLog("acqTempCorrected = " + acqTempCorrected);
            temperature = (float) 0.3378 * acqTempCorrected - (float) 133;
            appendToLog("temperature = " + temperature);
        } else if (iUser5 == 1835) {
            float expAcqTemp = (float) 398.54 - (float) iUser5 / (float) 100;
            appendToLog("expAcqTemp = " + expAcqTemp);
            expAcqTemp /= (float) 0.669162;
            appendToLog("expAcqTemp = " + expAcqTemp);
            float calibOffset = ((float) 8 * expAcqTemp) - (float) iUser6;
            float acqTempCorrected = (float) iUser1 + calibOffset;
            acqTempCorrected /= 8;
            temperature = (float) -0.669162 * acqTempCorrected;
            temperature += 398.54;
            appendToLog("expAcqTemp = " + expAcqTemp + ". calibOffset = " + calibOffset + ", acqTempCorrected = " + acqTempCorrected + ", temperature = " + temperature);
        }
        return temperature;
    } //4278
}
