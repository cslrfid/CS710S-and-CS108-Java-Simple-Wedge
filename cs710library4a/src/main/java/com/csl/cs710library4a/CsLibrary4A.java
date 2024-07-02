package com.csl.cs710library4a;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.csl.cs108library4a.Cs108Library4A;

import java.util.ArrayList;
import java.util.List;

public class CsLibrary4A {
    boolean DEBUG = false, DEBUG2 = false;
    String stringVersion = "4.9.2";
    Utility utility;
    Cs710Library4A cs710Library4A;
    com.csl.cs108library4a.Cs108Library4A cs108Library4A;

    public CsLibrary4A(Context context, TextView mLogView) {
        utility = new Utility(context, mLogView);
        cs710Library4A = new Cs710Library4A(context, mLogView);
        cs108Library4A = new Cs108Library4A(context, mLogView);
        stringNOTCONNECT = " is called before Connection !!!";
        dBuV_dBm_constant = cs108Library4A.dBuV_dBm_constant;
        iNO_SUCH_SETTING = cs108Library4A.iNO_SUCH_SETTING;
    } //41

    public String getlibraryVersion() { //called before connection
        if (DEBUG) Log.i("Hello2", "getlibraryVersion");
        if (false) {
            String string710 = cs710Library4A.getlibraryVersion();
            String string108 = cs108Library4A.getlibraryVersion();
            return stringVersion + "." + string710.substring(string710.length()-1, string710.length()) + "." + string108.substring(string108.length()-1, string108.length());
        }
        return stringVersion;
    } //152

    public String byteArrayToString(byte[] packet) { //called before connection
        if (DEBUG) Log.i("Hello2", "byteArrayToString");
        return utility.byteArrayToString(packet);
    } //156

    public void appendToLog(String s) { //called before connection
        if (DEBUG) Log.i("Hello2", "appendToLog");
        utility.appendToLog(s);
    } //160

    public void appendToLogView(String s) {
        if (DEBUG) Log.i("Hello2", "appendToLogView");
        utility.appendToLogView(s);
    } //164

    public boolean isBleScanning() { //called before connection
        if (DEBUG) Log.i("Hello2", "isBleScanning");
        boolean bValue = false, bValue1 = false, bValue7 = false;
        bValue1 = cs108Library4A.isBleScanning();
        bValue7 = cs710Library4A.isBleScanning();
        if (bValue1 && bValue7) bValue = true;
        else if (bValue1 == false && bValue7 == false) { }
        else Log.i("Hello2", "isBleScanning: bVAlue1 = " + bValue1 + ", bValue7 = " + bValue7);
        return bValue;
    } //170

    public boolean scanLeDevice(final boolean enable) { //called before connection
        boolean bValue = false, bValue1 = false, bValue7 = false;
        if (DEBUG) Log.i("Hello2", "scanLeDevice");
        bValue1 = cs108Library4A.scanLeDevice(enable);
        bValue7 = cs710Library4A.scanLeDevice(enable);
        if (bValue1 && bValue7) bValue = true;
        else if (bValue1 == false && bValue7 == false) { }
        else Log.i("Hello2", "scanLeDevice: bValue1 = " + bValue1 + ", bValue7 = " + bValue7);
        return bValue;
    } //177

    public String getBluetoothDeviceAddress() {
        if (DEBUG) Log.i("Hello2", "getBluetoothDeviceAddress");
        if (isCs108Connected()) return cs108Library4A.getBluetoothDeviceAddress();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothDeviceAddress();
        else Log.i("Hello2", "getBluetoothDeviceAddress" + stringNOTCONNECT);
        return null;
    } //233

    int bConnectStatus = 0;
    private boolean isCs108Connected() { return (bConnectStatus == 1); }
    private boolean isCs710Connected() { return (bConnectStatus == 7); }
    public boolean isBleConnected() { //called before connection
        boolean bValue = false;
        if (DEBUG2) Log.i("Hello2", "isBleConnected");
        if (isCs108Connected()) {
            bValue = cs108Library4A.isBleConnected();
            if (bValue == false) bConnectStatus = 0;
        } else if (isCs710Connected()) {
            bValue = cs710Library4A.isBleConnected();
            if (bValue == false) bConnectStatus = 0;
        } else {
            bValue = cs108Library4A.isBleConnected();
            if (bValue) bConnectStatus = 1;
            else {
                bValue = cs710Library4A.isBleConnected();
                if (bValue) bConnectStatus = 7;
                else bConnectStatus = 0;
            }
        }
        return bValue;
    } //238

    int iServiceUuidConnectedBefore = -1;
    public void connect(ReaderDevice readerDevice) { //called before connection
        if (DEBUG || true) Log.i("Hello2", "connect with readerDevice as " + (readerDevice != null ? "valid" : "null") + ", and iServiceUuidConnectedBefore = " + iServiceUuidConnectedBefore);
        int iServiceUuid = -1;
        if (readerDevice == null) iServiceUuid = iServiceUuidConnectedBefore;
        else iServiceUuid = readerDevice.getServiceUUID2p1();
        if (iServiceUuid == 0) {
            com.csl.cs108library4a.ReaderDevice readerDevice1 = null;
            if (readerDevice != null) readerDevice1 = new com.csl.cs108library4a.ReaderDevice(
                    readerDevice.getName(), readerDevice.getAddress(), readerDevice.getSelected(),
                    readerDevice.getDetails(), readerDevice.getCount(), readerDevice.getRssi(),
                    readerDevice.getServiceUUID2p1());
            cs108Library4A.connect(readerDevice1); iServiceUuidConnectedBefore = 0;
        } else if (iServiceUuid == 2) {
            cs710Library4A.connect(readerDevice); iServiceUuidConnectedBefore = 2;
        } else appendToLog("invalid serviceUUID = " + (readerDevice == null ? "null" : readerDevice.getServiceUUID2p1()));
    } //334

    public void disconnect(boolean tempDisconnect) { //called before connection
        if (DEBUG) Log.i("Hello2", "disconnect");
        if (isCs108Connected()) cs108Library4A.disconnect(tempDisconnect);
        else if (isCs710Connected()) cs710Library4A.disconnect(tempDisconnect);
    } //357

    public String checkVersion() {
        if (DEBUG) Log.i("Hello2", "checkVersion");
        if (isCs108Connected()) return cs108Library4A.checkVersion();
        else if (isCs710Connected()) return cs710Library4A.checkVersion();
        else Log.i("Hello2", "checkVersion" + stringNOTCONNECT);
        return null;
    } //392

    public int getRssi() {
        if (DEBUG) Log.i("Hello2", "getRssi");
        if (isCs108Connected()) return cs108Library4A.getRssi();
        else if (isCs710Connected()) return cs710Library4A.getRssi();
        else Log.i("Hello2", "getRssi" + stringNOTCONNECT);
        return -1;
    } //411

    public long getStreamInRate() {
        if (DEBUG) Log.i("Hello2", "getStreamInRate");
        if (isCs108Connected()) return cs108Library4A.getStreamInRate();
        else if (isCs710Connected()) return cs710Library4A.getStreamInRate();
        else Log.i("Hello2", "getStreamInRate" + stringNOTCONNECT);
        return -1;
    } //414

    public long getTagRate() {
        if (DEBUG) Log.i("Hello2", "getTagRate");
        if (isCs108Connected()) return cs108Library4A.getTagRate();
        else if (isCs710Connected()) return cs710Library4A.getTagRate();
        else Log.i("Hello2", "getTagRate" + stringNOTCONNECT);
        return -1;
    } //415

    public boolean getRfidOnStatus() {
        if (DEBUG) Log.i("Hello2", "getRfidOnStatus");
        if (isCs108Connected()) return cs108Library4A.getRfidOnStatus();
        else if (isCs710Connected()) return cs710Library4A.getRfidOnStatus();
        else Log.i("Hello2", "getRfidOnStatus" + stringNOTCONNECT);
        return false;
    } //417

    public boolean isBarcodeFailure() {
        if (DEBUG) Log.i("Hello2", "isBarcodeFailure");
        if (isCs108Connected()) return cs108Library4A.isBarcodeFailure();
        else if (isCs710Connected()) return cs710Library4A.isBarcodeFailure();
        else Log.i("Hello2", "isBarcodeFailure" + stringNOTCONNECT);
        return false;
    } //420

    public boolean isRfidFailure() { //called before connection
        if (DEBUG2) Log.i("Hello2", "isRfidFailure");
        if (isCs108Connected()) return cs108Library4A.isRfidFailure();
        else if (isCs710Connected()) return cs710Library4A.isRfidFailure();
        return false;
    } //421

    public void setSameCheck(boolean sameCheck1) {
        if (DEBUG) Log.i("Hello2", "setSameCheck");
        if (isCs108Connected()) cs108Library4A.setSameCheck(sameCheck1);
        else if (isCs710Connected()) cs710Library4A.setSameCheck(sameCheck1);
        else Log.i("Hello2", "setSameCheck" + stringNOTCONNECT);
    } //423

    public void setReaderDefault() {
        if (DEBUG) Log.i("Hello2", "setReaderDefault");
        if (isCs108Connected()) cs108Library4A.setReaderDefault();
        else if (isCs710Connected()) cs710Library4A.setReaderDefault();
        else Log.i("Hello2", "setReaderDefault" + stringNOTCONNECT);
    } //429

    public void saveSetting2File() {
        if (DEBUG) Log.i("Hello2", "saveSetting2File");
        if (isCs108Connected()) cs108Library4A.saveSetting2File();
        else if (isCs710Connected()) cs710Library4A.saveSetting2File();
        else Log.i("Hello2", "saveSetting2File" + stringNOTCONNECT);
    } //667

    public String getMacVer() {
        if (DEBUG) Log.i("Hello2", "getMacVer");
        if (isCs108Connected()) return cs108Library4A.getMacVer();
        else if (isCs710Connected()) return cs710Library4A.getMacVer();
        else Log.i("Hello2", "getMacVer" + stringNOTCONNECT);
        return null;
    } //738

    public int getPortNumber() {
        if (DEBUG) Log.i("Hello2", "getPortNumber");
        if (isCs108Connected()) return cs108Library4A.getPortNumber();
        else if (isCs710Connected()) return cs710Library4A.getPortNumber();
        else Log.i("Hello2", "getPortNumber" + stringNOTCONNECT);
        return -1;
    } //754

    public int getAntennaSelect() {
        if (DEBUG) Log.i("Hello2", "getAntennaSelect");
        if (isCs108Connected()) return cs108Library4A.getAntennaSelect();
        else if (isCs710Connected()) return cs710Library4A.getAntennaSelect();
        else Log.i("Hello2", "getAntennaSelect" + stringNOTCONNECT);
        return -1;
    } //758

    public boolean setAntennaSelect(int number) {
        if (DEBUG) Log.i("Hello2", "setAntennaSelect");
        if (isCs108Connected()) return cs108Library4A.setAntennaSelect(number);
        else if (isCs710Connected()) return cs710Library4A.setAntennaSelect(number);
        else Log.i("Hello2", "setAntennaSelect" + stringNOTCONNECT);
        return false;
    } //765

    public boolean getAntennaEnable() {
        if (DEBUG) Log.i("Hello2", "getAntennaEnable");
        if (isCs108Connected()) return cs108Library4A.getAntennaEnable();
        else if (isCs710Connected()) return cs710Library4A.getAntennaEnable();
        else Log.i("Hello2", "getAntennaEnable" + stringNOTCONNECT);
        return false;
    } //771

    public boolean setAntennaEnable(boolean enable) {
        if (DEBUG) Log.i("Hello2", "setAntennaEnable");
        if (isCs108Connected()) return cs108Library4A.setAntennaEnable(enable);
        else if (isCs710Connected()) return cs710Library4A.setAntennaEnable(enable);
        else Log.i("Hello2", "setAntennaEnable" + stringNOTCONNECT);
        return false; } //779

    public long getAntennaDwell() {
        if (DEBUG) Log.i("Hello2", "getAntennaDwell");
        if (isCs108Connected()) return cs108Library4A.getAntennaDwell();
        else if (isCs710Connected()) return cs710Library4A.getAntennaDwell();
        else Log.i("Hello2", "getAntennaDwell" + stringNOTCONNECT);
        return -1;
    } //789

    public boolean setAntennaDwell(long antennaDwell) {
        if (DEBUG) Log.i("Hello2", "setAntennaDwell");
        if (isCs108Connected()) return cs108Library4A.setAntennaDwell(antennaDwell);
        else if (isCs710Connected()) return cs710Library4A.setAntennaDwell(antennaDwell);
        else Log.i("Hello2", "setAntennaDwell" + stringNOTCONNECT);
        return false;
    } //796

    public long getPwrlevel() {
        if (DEBUG) Log.i("Hello2", "getPwrlevel");
        if (isCs108Connected()) return cs108Library4A.getPwrlevel();
        else if (isCs710Connected()) return cs710Library4A.getPwrlevel();
        else Log.i("Hello2", "getPwrlevel" + stringNOTCONNECT);
        return -1;
    } //803

    public boolean setPowerLevel(long pwrlevel) {
        if (DEBUG) Log.i("Hello2", "setPowerLevel");
        if (isCs108Connected()) return cs108Library4A.setPowerLevel(pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setPowerLevel(pwrlevel);
        else Log.i("Hello2", "setPowerLevel" + stringNOTCONNECT);
        return false;
    } //811

    public int getQueryTarget() {
        if (DEBUG) Log.i("Hello2", "getQueryTarget");
        if (isCs108Connected()) return cs108Library4A.getQueryTarget();
        else if (isCs710Connected()) return cs710Library4A.getQueryTarget();
        else Log.i("Hello2", "getQueryTarget" + stringNOTCONNECT);
        return -1;
    } //821

    public int getQuerySession() {
        if (DEBUG) Log.i("Hello2", "getQuerySession");
        if (isCs108Connected()) return cs108Library4A.getQuerySession();
        else if (isCs710Connected()) return cs710Library4A.getQuerySession();
        else Log.i("Hello2", "getQuerySession" + stringNOTCONNECT);
        return -1;
    } //829

    public int getQuerySelect() {
        if (DEBUG) Log.i("Hello2", "getQuerySelect");
        if (isCs108Connected()) return cs108Library4A.getQuerySelect();
        else if (isCs710Connected()) return cs710Library4A.getQuerySelect();
        else Log.i("Hello2", "getQuerySelect" + stringNOTCONNECT);
        return -1;
    } //833

    public boolean setTagGroup(int sL, int session, int target1) {
        if (DEBUG) Log.i("Hello2", "setTagGroup");
        if (isCs108Connected()) return cs108Library4A.setTagGroup(sL, session, target1);
        else if (isCs710Connected()) return cs710Library4A.setTagGroup(sL, session, target1);
        else Log.i("Hello2", "setTagGroup" + stringNOTCONNECT);
        return false;
    } //836

    public int getTagFocus() {
        if (DEBUG) Log.i("Hello2", "getTagFocus");
        if (isCs108Connected()) return cs108Library4A.getTagFocus();
        else if (isCs710Connected()) return cs710Library4A.getTagFocus();
        else Log.i("Hello2", "getTagFocus" + stringNOTCONNECT);
        return -1;
    } //842

    public boolean setTagFocus(boolean tagFocusNew) {
        if (DEBUG) Log.i("Hello2", "setTagFocus");
        if (isCs108Connected()) return cs108Library4A.setTagFocus(tagFocusNew);
        else if (isCs710Connected()) return cs710Library4A.setTagFocus(tagFocusNew);
        else Log.i("Hello2", "setTagFocus" + stringNOTCONNECT);
        return false;
    } //849

    public int getFastId() {
        if (DEBUG) Log.i("Hello2", "getFastId");
        if (isCs108Connected()) return cs108Library4A.getFastId();
        else if (isCs710Connected()) return cs710Library4A.getFastId();
        else Log.i("Hello2", "getFastId" + stringNOTCONNECT);
        return -1;
    } //842

    public boolean setFastId(boolean fastIdNew) {
        if (DEBUG) Log.i("Hello2", "setFastId");
        if (isCs108Connected()) return cs108Library4A.setFastId(fastIdNew);
        else if (isCs710Connected()) return cs710Library4A.setFastId(fastIdNew);
        else Log.i("Hello2", "setFastId" + stringNOTCONNECT);
        return false;
    } //849

    public boolean getInvAlgo() {
        if (DEBUG) Log.i("Hello2", "getInvAlgo");
        if (isCs108Connected()) return cs108Library4A.getInvAlgo();
        else if (isCs710Connected()) return cs710Library4A.getInvAlgo();
        else Log.i("Hello2", "getInvAlgo" + stringNOTCONNECT);
        return false;
    } //857

    public boolean setInvAlgo(boolean dynamicAlgo) {
        if (DEBUG) Log.i("Hello2", "setInvAlgo");
        if (isCs108Connected()) return cs108Library4A.setInvAlgo(dynamicAlgo);
        else if (isCs710Connected()) return cs710Library4A.setInvAlgo(dynamicAlgo);
        else Log.i("Hello2", "setInvAlgo" + stringNOTCONNECT);
        return false;
    } //861

    public List<String> getProfileList() {
        if (DEBUG) Log.i("Hello2", "getProfileList");
        if (isCs108Connected()) return cs108Library4A.getProfileList();
        else if (isCs710Connected()) return cs710Library4A.getProfileList();
        else Log.i("Hello2", "getProfileList" + stringNOTCONNECT);
        return null;
    } //900

    public int getCurrentProfile() {
        if (DEBUG) Log.i("Hello2", "getCurrentProfile");
        if (isCs108Connected()) return cs108Library4A.getCurrentProfile();
        else if (isCs710Connected()) return cs710Library4A.getCurrentProfile();
        else Log.i("Hello2", "getCurrentProfile" + stringNOTCONNECT);
        return -1;
    } //905

    public boolean setCurrentLinkProfile(int profile) {
        if (DEBUG) Log.i("Hello2", "setCurrentLinkProfile");
        if (isCs108Connected()) return cs108Library4A.setCurrentLinkProfile(profile);
        else if (isCs710Connected()) return cs710Library4A.setCurrentLinkProfile(profile);
        else Log.i("Hello2", "setCurrentLinkProfile" + stringNOTCONNECT);
        return false;
    } //930

    public void resetEnvironmentalRSSI() {
        if (DEBUG) Log.i("Hello2", "resetEnvironmentalRSSI");
        if (isCs108Connected()) cs108Library4A.resetEnvironmentalRSSI();
        else if (isCs710Connected()) cs710Library4A.resetEnvironmentalRSSI();
        else Log.i("Hello2", "resetEnvironmentalRSSI" + stringNOTCONNECT);
    } //954

    public String getEnvironmentalRSSI() {
        if (DEBUG) Log.i("Hello2", "getEnvironmentalRSSI");
        if (isCs108Connected()) return cs108Library4A.getEnvironmentalRSSI();
        else if (isCs710Connected()) return cs710Library4A.getEnvironmentalRSSI();
        else Log.i("Hello2", "getEnvironmentalRSSI" + stringNOTCONNECT);
        return null;
    } //955

    public int getHighCompression() {
        if (DEBUG) Log.i("Hello2", "getHighCompression");
        if (isCs108Connected()) return cs108Library4A.getHighCompression();
        else if (isCs710Connected()) return cs710Library4A.getHighCompression();
        else Log.i("Hello2", "getHighCompression" + stringNOTCONNECT);
        return -1;
    } //969

    public int getRflnaGain() {
        if (DEBUG) Log.i("Hello2", "getRflnaGain");
        if (isCs108Connected()) return cs108Library4A.getRflnaGain();
        else if (isCs710Connected()) return cs710Library4A.getRflnaGain();
        else Log.i("Hello2", "getRflnaGain" + stringNOTCONNECT);
        return -1;
    } //972

    public int getIflnaGain() {
        if (DEBUG) Log.i("Hello2", "getIflnaGain");
        if (isCs108Connected()) return cs108Library4A.getIflnaGain();
        else if (isCs710Connected()) return cs710Library4A.getIflnaGain();
        else Log.i("Hello2", "getIflnaGain" + stringNOTCONNECT);
        return -1;
    } //973

    public int getAgcGain() {
        if (DEBUG) Log.i("Hello2", "getAgcGain");
        if (isCs108Connected()) return cs108Library4A.getAgcGain();
        else if (isCs710Connected()) return cs710Library4A.getAgcGain();
        else Log.i("Hello2", "getAgcGain" + stringNOTCONNECT);
        return -1;
    } //976

    public boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
        if (DEBUG) Log.i("Hello2", "setRxGain");
        if (isCs108Connected()) return cs108Library4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
        else if (isCs710Connected()) return cs710Library4A.setRxGain(highCompression, rflnagain, iflnagain, agcgain);
        else Log.i("Hello2", "setRxGain" + stringNOTCONNECT);
        return false;
    } //978
    
    public int FreqChnCnt() {
        if (DEBUG) Log.i("Hello2", "FreqChnCnt");
        if (isCs108Connected()) return cs108Library4A.FreqChnCnt();
        else if (isCs710Connected()) return cs710Library4A.FreqChnCnt();
        else Log.i("Hello2", "FreqChnCnt" + stringNOTCONNECT);
        return -1;
    } //1806

    public double getLogicalChannel2PhysicalFreq(int channel) {
        if (DEBUG) Log.i("Hello2", "getLogicalChannel2PhysicalFreq");
        if (isCs108Connected()) return cs108Library4A.getLogicalChannel2PhysicalFreq(channel);
        else if (isCs710Connected()) return cs710Library4A.getLogicalChannel2PhysicalFreq(channel);
        else Log.i("Hello2", "getLogicalChannel2PhysicalFreq" + stringNOTCONNECT);
        return -1;
    } //2003

    public byte getTagDelay() {
        if (DEBUG) Log.i("Hello2", "getTagDelay");
        if (isCs108Connected()) return cs108Library4A.getTagDelay();
        else if (isCs710Connected()) return cs710Library4A.getTagDelay();
        else Log.i("Hello2", "getTagDelay" + stringNOTCONNECT);
        return -1;
    } //2046

    public boolean setTagDelay(byte tagDelay) {
        if (DEBUG) Log.i("Hello2", "setTagDelay");
        if (isCs108Connected()) return cs108Library4A.setTagDelay(tagDelay);
        else if (isCs710Connected()) return cs710Library4A.setTagDelay(tagDelay);
        else Log.i("Hello2", "setTagDelay" + stringNOTCONNECT);
        return false;
    } //2050

    public boolean setIntraPkDelay(byte intraPkDelay) {
        if (DEBUG) Log.i("Hello2", "setIntraPkDelay");
        if (isCs108Connected()) return cs108Library4A.setIntraPkDelay(intraPkDelay);
        else if (isCs710Connected()) return cs710Library4A.setIntraPkDelay(intraPkDelay);
        else Log.i("Hello2", "setIntraPkDelay" + stringNOTCONNECT);
        return false;
    } //2056

    public boolean setDupDelay(byte dupElim) {
        if (DEBUG) Log.i("Hello2", "setDupDelay");
        if (isCs108Connected()) return cs108Library4A.setDupDelay(dupElim);
        else if (isCs710Connected()) return cs710Library4A.setDupDelay(dupElim);
        else Log.i("Hello2", "setDupDelay" + stringNOTCONNECT);
        return false;
    } //2058

    public long getCycleDelay() {
        if (DEBUG) Log.i("Hello2", "getCycleDelay");
        if (isCs108Connected()) return cs108Library4A.getCycleDelay();
        else if (isCs710Connected()) return cs710Library4A.getCycleDelay();
        else Log.i("Hello2", "getCycleDelay" + stringNOTCONNECT);
        return -1;
    } //2060

    public boolean setCycleDelay(long cycleDelay) {
        if (DEBUG) Log.i("Hello2", "setCycleDelay");
        if (isCs108Connected()) return cs108Library4A.setCycleDelay(cycleDelay);
        else if (isCs710Connected()) return cs710Library4A.setCycleDelay(cycleDelay);
        return false;
    } //2064

    public void getAuthenticateReplyLength() {
        if (DEBUG) Log.i("Hello2", "getAuthenticateReplyLength");
        if (isCs108Connected()) cs108Library4A.getAuthenticateReplyLength();
        else if (isCs710Connected()) cs710Library4A.getAuthenticateReplyLength();
        else Log.i("Hello2", "getAuthenticateReplyLength" + stringNOTCONNECT);
    } //2069

    public boolean setTam1Configuration(int keyId, String matchData) {
        if (DEBUG | true) Log.i("Hello2", "setTam1Configuration with KeyId = " + keyId + ", matchData = " + matchData);
        if (isCs108Connected()) return cs108Library4A.setTam1Configuration(keyId, matchData);
        else if (isCs710Connected()) return cs710Library4A.setTam1Configuration(keyId, matchData);
        else Log.i("Hello2", "setTam1Configuration");
        return false;
    } //2072
    public boolean setTam2Configuration(int keyId, String matchData, int profile, int offset, int blockId, int protMode) {
        if (DEBUG) Log.i("Hello2", "setTam2Configuration");
        if (isCs108Connected()) return cs108Library4A.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
        else if (isCs710Connected()) return cs710Library4A.setTam2Configuration(keyId, matchData, profile, offset, blockId, protMode);
        else Log.i("Hello2", "setTam2Configuration");
        return false;
    } //2085

    public int getUntraceableEpcLength() {
        if (DEBUG) Log.i("Hello2", "getUntraceableEpcLength");
        if (isCs108Connected()) return cs108Library4A.getUntraceableEpcLength();
        else if (isCs710Connected()) return cs710Library4A.getUntraceableEpcLength();
        else Log.i("Hello2", "getUntraceableEpcLength" + stringNOTCONNECT);
        return -1;
    } //2124

    public boolean setUntraceable(boolean bHideEpc, int ishowEpcSize, int iHideTid, boolean bHideUser, boolean bHideRange) {
        Log.i("Hello2", "setUntraceable 1");
        return false; } //2127
    public boolean setUntraceable(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
        Log.i("Hello2", "setUntraceable 2");
        return false; } //2130

    public boolean setAuthenticateConfiguration() {
        if (DEBUG) Log.i("Hello2", "setAuthenticateConfiguration");
        if (isCs108Connected()) return cs108Library4A.setAuthenticateConfiguration();
        else if (isCs710Connected()) return cs710Library4A.setAuthenticateConfiguration();
        else Log.i("Hello2", "setAuthenticateConfiguration" + stringNOTCONNECT);
        return false;
    }

    public int getBeepCount() {
        if (DEBUG) Log.i("Hello2", "getBeepCount");
        if (isCs108Connected()) return cs108Library4A.getBeepCount();
        else if (isCs710Connected()) return cs710Library4A.getBeepCount();
        else Log.i("Hello2", "getBeepCount" + stringNOTCONNECT);
        return -1;
    } //2135

    public boolean setBeepCount(int beepCount) {
        if (DEBUG) Log.i("Hello2", "setBeepCount");
        if (isCs108Connected()) return cs108Library4A.setBeepCount(beepCount);
        else if (isCs710Connected()) return cs710Library4A.setBeepCount(beepCount);
        else Log.i("Hello2", "setBeepCount" + stringNOTCONNECT);
        return false;
    } //2138

    public boolean getInventoryBeep() {
        if (DEBUG) Log.i("Hello2", "getInventoryBeep");
        if (isCs108Connected()) return cs108Library4A.getInventoryBeep();
        else if (isCs710Connected()) return cs710Library4A.getInventoryBeep();
        else Log.i("Hello2", "getInventoryBeep" + stringNOTCONNECT);
        return false;
    } //2144

    public boolean setInventoryBeep(boolean inventoryBeep) {
        if (DEBUG) Log.i("Hello2", "setInventoryBeep");
        if (isCs108Connected()) return cs108Library4A.setInventoryBeep(inventoryBeep);
        else if (isCs710Connected()) return cs710Library4A.setInventoryBeep(inventoryBeep);
        else Log.i("Hello2", "setInventoryBeep" + stringNOTCONNECT);
        return false;
    } //2145

    public boolean getInventoryVibrate() {
        if (DEBUG) Log.i("Hello2", "getInventoryVibrate");
        if (isCs108Connected()) return cs108Library4A.getInventoryVibrate();
        else if (isCs710Connected()) return cs710Library4A.getInventoryVibrate();
        else Log.i("Hello2", "getInventoryVibrate" + stringNOTCONNECT);
        return false;
    } //2151

    public boolean setInventoryVibrate(boolean inventoryVibrate) {
        if (DEBUG) Log.i("Hello2", "setInventoryVibrate");
        if (isCs108Connected()) return cs108Library4A.setInventoryVibrate(inventoryVibrate);
        else if (isCs710Connected()) return cs710Library4A.setInventoryVibrate(inventoryVibrate);
        else Log.i("Hello2", "setInventoryVibrate" + stringNOTCONNECT);
        return false;
    } //2152

    public int getVibrateTime() {
        if (DEBUG) Log.i("Hello2", "getVibrateTime");
        if (isCs108Connected()) return cs108Library4A.getVibrateTime();
        else if (isCs710Connected()) return cs710Library4A.getVibrateTime();
        else Log.i("Hello2", "getVibrateTime" + stringNOTCONNECT);
        return -1;
    } //2158

    public boolean setVibrateTime(int vibrateTime) {
        if (DEBUG) Log.i("Hello2", "setVibrateTime");
        if (isCs108Connected()) return cs108Library4A.setVibrateTime(vibrateTime);
        else if (isCs710Connected()) return cs710Library4A.setVibrateTime(vibrateTime);
        else Log.i("Hello2", "setVibrateTime" + stringNOTCONNECT);
        return false;
    } //2161

    public int getVibrateWindow() {
        if (DEBUG) Log.i("Hello2", "getVibrateWindow");
        if (isCs108Connected()) return cs108Library4A.getVibrateWindow();
        else if (isCs710Connected()) return cs710Library4A.getVibrateWindow();
        else Log.i("Hello2", "getVibrateWindow" + stringNOTCONNECT);
        return -1;
    } //2167

    public boolean setVibrateWindow(int vibrateWindow) {
        if (DEBUG) Log.i("Hello2", "setVibrateWindow");
        if (isCs108Connected()) return cs108Library4A.setVibrateWindow(vibrateWindow);
        else if (isCs710Connected()) return cs710Library4A.setVibrateWindow(vibrateWindow);
        else Log.i("Hello2", "setVibrateWindow" + stringNOTCONNECT);
        return false;
    } //2170

    public boolean getSaveFileEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveFileEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveFileEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveFileEnable();
        else Log.i("Hello2", "getSaveFileEnable" + stringNOTCONNECT);
        return false;
    } //2176

    public boolean setSaveFileEnable(boolean saveFileEnable) {
        if (DEBUG) Log.i("Hello2", "setSaveFileEnable");
        if (isCs108Connected()) return cs108Library4A.setSaveFileEnable(saveFileEnable);
        else if (isCs710Connected()) return cs710Library4A.setSaveFileEnable(saveFileEnable);
        else Log.i("Hello2", "setSaveFileEnable" + stringNOTCONNECT);
        return false;
    } //2177

    public boolean getSaveCloudEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveCloudEnable();
        else Log.i("Hello2", "getSaveCloudEnable" + stringNOTCONNECT);
        return false;
    } //2185

    public boolean setSaveCloudEnable(boolean saveCloudEnable) {
        if (DEBUG) Log.i("Hello2", "setSaveCloudEnable");
        if (isCs108Connected()) return cs108Library4A.setSaveCloudEnable(saveCloudEnable);
        else if (isCs710Connected()) return cs710Library4A.setSaveCloudEnable(saveCloudEnable);
        else Log.i("Hello2", "setSaveCloudEnable" + stringNOTCONNECT);
        return false;
    } //2186

    public boolean getSaveNewCloudEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveNewCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveNewCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveNewCloudEnable();
        else Log.i("Hello2", "getSaveNewCloudEnable" + stringNOTCONNECT);
        return false;
    } //2192

    public boolean setSaveNewCloudEnable(boolean saveNewCloudEnable) {
        Log.i("Hello2", "setSaveNewCloudEnable");
        return false; } //2193

    public boolean getSaveAllCloudEnable() {
        if (DEBUG) Log.i("Hello2", "getSaveAllCloudEnable");
        if (isCs108Connected()) return cs108Library4A.getSaveAllCloudEnable();
        else if (isCs710Connected()) return cs710Library4A.getSaveAllCloudEnable();
        else Log.i("Hello2", "getSaveAllCloudEnable" + stringNOTCONNECT);
        return false;
    } //2198

    public boolean setSaveAllCloudEnable(boolean saveAllCloudEnable) {
        Log.i("Hello2", "setSaveAllCloudEnable");
        return false; } //2199

    @Keep public boolean getUserDebugEnable() {
        if (DEBUG) Log.i("Hello2", "getUserDebugEnable");
        if (isCs108Connected()) return cs108Library4A.getUserDebugEnable();
        else if (isCs710Connected()) return cs710Library4A.getUserDebugEnable();
        else Log.i("Hello2", "getUserDebugEnable" + stringNOTCONNECT);
        return false;
    }
    @Keep public boolean setUserDebugEnable(boolean userDebugEnable) {
        if (DEBUG) Log.i("Hello2", "setUserDebugEnable");
        if (isCs108Connected()) return cs108Library4A.setUserDebugEnable(userDebugEnable);
        else if (isCs710Connected()) return cs710Library4A.setUserDebugEnable(userDebugEnable);
        else Log.i("Hello2", "getUserDebugEnable" + stringNOTCONNECT);
        return false;
    }

    public String getServerLocation() {
        if (DEBUG) Log.i("Hello2", "getServerLocation");
        if (isCs108Connected()) return cs108Library4A.getServerLocation();
        else if (isCs710Connected()) return cs710Library4A.getServerLocation();
        else Log.i("Hello2", "getServerLocation" + stringNOTCONNECT);
        return null;
    } //2207

    public boolean setServerLocation(String serverLocation) {
        if (DEBUG) Log.i("Hello2", "setServerLocation");
        if (isCs108Connected()) return cs108Library4A.setServerLocation(serverLocation);
        else if (isCs710Connected()) return cs710Library4A.setServerLocation(serverLocation);
        else Log.i("Hello2", "setServerLocation" + stringNOTCONNECT);
        return false;
    } //2210

    public int getServerTimeout() {
        if (DEBUG) Log.i("Hello2", "getServerTimeout");
        if (isCs108Connected()) return cs108Library4A.getServerTimeout();
        else if (isCs710Connected()) return cs710Library4A.getServerTimeout();
        else Log.i("Hello2", "getServerTimeout" + stringNOTCONNECT);
        return -1;
    } //2216

    public boolean setServerTimeout(int serverTimeout) {
        if (DEBUG) Log.i("Hello2", "setServerTimeout");
        if (isCs108Connected()) return cs108Library4A.setServerTimeout(serverTimeout);
        else if (isCs710Connected()) return cs710Library4A.setServerTimeout(serverTimeout);
        else Log.i("Hello2", "setServerTimeout" + stringNOTCONNECT);
        return false;
    } //2217

    public int getRetryCount() {
        if (DEBUG) Log.i("Hello2", "getRetryCount");
        if (isCs108Connected()) return cs108Library4A.getRetryCount();
        else if (isCs710Connected()) return cs710Library4A.getRetryCount();
        else Log.i("Hello2", "getRetryCount" + stringNOTCONNECT);
        return -1;
    } //2232

    public boolean setRetryCount(int retryCount) {
        if (DEBUG) Log.i("Hello2", "setRetryCount");
        if (isCs108Connected()) return cs108Library4A.setRetryCount(retryCount);
        else if (isCs710Connected()) return cs710Library4A.setRetryCount(retryCount);
        else Log.i("Hello2", "setRetryCount" + stringNOTCONNECT);
        return false;
    } //2240

    public byte getIntraPkDelay() {
        if (DEBUG) Log.i("Hello2", "getIntraPkDelay");
        if (isCs108Connected()) return cs108Library4A.getIntraPkDelay();
        else if (isCs710Connected()) return cs710Library4A.getIntraPkDelay();
        else Log.i("Hello2", "getIntraPkDelay" + stringNOTCONNECT);
        return -1;
    } //2255

    public byte getDupDelay() {
        if (DEBUG) Log.i("Hello2", "getDupDelay");
        if (isCs108Connected()) return cs108Library4A.getDupDelay();
        else if (isCs710Connected()) return cs710Library4A.getDupDelay();
        else Log.i("Hello2", "getDupDelay" + stringNOTCONNECT);
        return -1;
    } //2257

    public int getInvSelectIndex() {
        if (DEBUG) Log.i("Hello2", "getInvSelectIndex");
        if (isCs108Connected()) return cs108Library4A.getInvSelectIndex();
        else if (isCs710Connected()) return cs710Library4A.getInvSelectIndex();
        else Log.i("Hello2", "getInvSelectIndex" + stringNOTCONNECT);
        return -1;
    } //2286

    public boolean getSelectEnable() {
        if (DEBUG) Log.i("Hello2", "getSelectEnable");
        if (isCs108Connected()) return cs108Library4A.getSelectEnable();
        else if (isCs710Connected()) return cs710Library4A.getSelectEnable();
        else Log.i("Hello2", "getSelectEnable" + stringNOTCONNECT);
        return false;
    } //2289

    public int getSelectTarget() {
        if (DEBUG) Log.i("Hello2", "getSelectTarget");
        if (isCs108Connected()) return cs108Library4A.getSelectTarget();
        else if (isCs710Connected()) return cs710Library4A.getSelectTarget();
        else Log.i("Hello2", "getSelectTarget" + stringNOTCONNECT);
        return -1;
    } //2294

    public int getSelectAction() {
        if (DEBUG) Log.i("Hello2", "getSelectAction");
        if (isCs108Connected()) return cs108Library4A.getSelectAction();
        else if (isCs710Connected()) return cs710Library4A.getSelectAction();
        else Log.i("Hello2", "getSelectAction" + stringNOTCONNECT);
        return -1;
    } //2297

    public int getSelectMaskBank() {
        if (DEBUG) Log.i("Hello2", "getSelectMaskBank");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskBank();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskBank();
        else Log.i("Hello2", "getSelectMaskBank" + stringNOTCONNECT);
        return -1;
    } //2300

    public int getSelectMaskOffset() {
        if (DEBUG) Log.i("Hello2", "getSelectMaskOffset");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskOffset();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskOffset();
        else Log.i("Hello2", "getSelectMaskOffset" + stringNOTCONNECT);
        return -1;
    } //2303

    public String getSelectMaskData() {
        if (DEBUG) Log.i("Hello2", "getSelectMaskData");
        if (isCs108Connected()) return cs108Library4A.getSelectMaskData();
        else if (isCs710Connected()) return cs710Library4A.getSelectMaskData();
        else Log.i("Hello2", "getSelectMaskData" + stringNOTCONNECT);
        return null;
    } //2306

    public boolean setInvSelectIndex(int invSelect) {
        if (DEBUG) Log.i("Hello2", "setInvSelectIndex");
        if (isCs108Connected()) return cs108Library4A.setInvSelectIndex(invSelect);
        else if (isCs710Connected()) return cs710Library4A.setInvSelectIndex(invSelect);
        else Log.i("Hello2", "setInvSelectIndex" + stringNOTCONNECT);
        return false; } //2317

    public boolean setSelectCriteriaDisable(int index) {
        if (DEBUG || true) appendToLog("csLibrary4A: setSelectCriteria Disable with index = " + index);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteriaDisable(index);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteriaDisable(index);
        else Log.i("Hello2", "setSelectCriteriaDisable" + stringNOTCONNECT);
        return false;
    } //2351

    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int bank, int offset, String mask, boolean maskbit) {
        appendToLog("csLibrary4A: setSelectCriteria 1 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask + ", maskbit = " + maskbit);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteria(index, enable, target, action, bank, offset, mask, maskbit);
        else Log.i("Hello2", "setSelectCriteria 1" + stringNOTCONNECT);
        return false;
    } //2355

    public boolean setSelectCriteria(int index, boolean enable, int target, int action, int delay, int bank, int offset, String mask) {
        appendToLog("csLibrary4A: setSelectCriteria 2 with index = " + index + ", enable = " + enable + ", target = " + target + ", action = " + action + ", delay = " + delay + ", bank = " + bank + ", offset = " + offset + ", mask = " + mask);
        if (isCs108Connected()) return cs108Library4A.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
        else if (isCs710Connected()) return cs710Library4A.setSelectCriteria(index, enable, target, action, delay, bank, offset, mask);
        else Log.i("Hello2", "setSelectCriteria" + stringNOTCONNECT);
        return false;
    } //2378

    public boolean getRssiFilterEnable() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterEnable");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterEnable();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterEnable();
        else Log.i("Hello2", "getRssiFilterEnable" + stringNOTCONNECT);
        return false;
    } //2452

    public int getRssiFilterType() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterType");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterType();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterType();
        else Log.i("Hello2", "getRssiFilterType" + stringNOTCONNECT);
        return -1;
    } //2458

    public int getRssiFilterOption() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterOption");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterOption();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterOption();
        return -1;
    } //2465

    public boolean setRssiFilterConfig(boolean enable, int rssiFilterType, int rssiFilterOption) {
        if (DEBUG) Log.i("Hello2", "setRssiFilterConfig");
        if (isCs108Connected()) return cs108Library4A.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
        else if (isCs710Connected()) return cs710Library4A.setRssiFilterConfig(enable, rssiFilterType, rssiFilterOption);
        else Log.i("Hello2", "setRssiFilterConfig" + stringNOTCONNECT);
        return false;
    } //2471

    public double getRssiFilterThreshold1() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterThreshold1");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterThreshold1();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterThreshold1();
        else Log.i("Hello2", "getRssiFilterThreshold1" + stringNOTCONNECT);
        return -1;
    } //2477

    public double getRssiFilterThreshold2() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterThreshold2");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterThreshold2();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterThreshold2();
        else Log.i("Hello2", "getRssiFilterThreshold2" + stringNOTCONNECT);
        return -1;
    } //2488

    public boolean setRssiFilterThreshold(double rssiFilterThreshold1, double rssiFilterThreshold2) {
        if (DEBUG) Log.i("Hello2", "setRssiFilterThreshold");
        if (isCs108Connected()) return cs108Library4A.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
        else if (isCs710Connected()) return cs710Library4A.setRssiFilterThreshold(rssiFilterThreshold1, rssiFilterThreshold2);
        else Log.i("Hello2", "setRssiFilterThreshold" + stringNOTCONNECT);
        return false;
    } //2495

    public long getRssiFilterCount() {
        if (DEBUG) Log.i("Hello2", "getRssiFilterCount");
        if (isCs108Connected()) return cs108Library4A.getRssiFilterCount();
        else if (isCs710Connected()) return cs710Library4A.getRssiFilterCount();
        else Log.i("Hello2", "getRssiFilterCount" + stringNOTCONNECT);
        return -1;
    } //2505

    public boolean setRssiFilterCount(long rssiFilterCount) {
        Log.i("Hello2", "setRssiFilterCount");
        return false; } //2508

    public boolean getInvMatchEnable() {
        if (DEBUG) Log.i("Hello2", "getInvMatchEnable");
        if (isCs108Connected()) return cs108Library4A.getInvMatchEnable();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchEnable();
        else Log.i("Hello2", "getInvMatchEnable" + stringNOTCONNECT);
        return false;
    } //2513

    public boolean getInvMatchType() {
        if (DEBUG) Log.i("Hello2", "getInvMatchType");
        if (isCs108Connected()) return cs108Library4A.getInvMatchType();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchType();
        else Log.i("Hello2", "getInvMatchType" + stringNOTCONNECT);
        return false;
    } //2516

    public int getInvMatchOffset() {
        if (DEBUG) Log.i("Hello2", "getInvMatchOffset");
        if (isCs108Connected()) return cs108Library4A.getInvMatchOffset();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchOffset();
        else Log.i("Hello2", "getInvMatchOffset" + stringNOTCONNECT);
        return -1;
    } //2519

    public String getInvMatchData() {
        if (DEBUG) Log.i("Hello2", "getInvMatchData");
        if (isCs108Connected()) return cs108Library4A.getInvMatchData();
        else if (isCs710Connected()) return cs710Library4A.getInvMatchData();
        else Log.i("Hello2", "getInvMatchData" + stringNOTCONNECT);
        return null;
    } //2522

    public boolean setPostMatchCriteria(boolean enable, boolean target, int offset, String mask) {
        if (DEBUG) Log.i("Hello2", "setPostMatchCriteria");
        if (isCs108Connected()) return cs108Library4A.setPostMatchCriteria(enable, target, offset, mask);
        else if (isCs710Connected()) return cs710Library4A.setPostMatchCriteria(enable, target, offset, mask);
        else Log.i("Hello2", "setPostMatchCriteria" + stringNOTCONNECT);
        return false;
    } //2543

    public int mrfidToWriteSize() {
        if (DEBUG2) Log.i("Hello2", "mrfidToWriteSize");
        if (isCs108Connected()) return cs108Library4A.mrfidToWriteSize();
        else if (isCs710Connected()) return cs710Library4A.mrfidToWriteSize();
        else Log.i("Hello2", "mrfidToWriteSize" + stringNOTCONNECT);
        return -1;
    } //2550

    public void mrfidToWritePrint() {
        Log.i("Hello2", "mrfidToWritePrint");
    } //2553

    @Keep
    public enum OperationTypes {
        TAG_RDOEM,
        TAG_INVENTORY_COMPACT, TAG_INVENTORY, TAG_SEARCHING
    }
    public boolean startOperation(OperationTypes operationTypes) {
        if (DEBUG) Log.i("Hello2", "startOperation");
        if (isCs108Connected()) {
            Cs108Library4A.OperationTypes operationTypes1 = null;
            switch (operationTypes) {
                case TAG_RDOEM:
                    operationTypes1 = Cs108Library4A.OperationTypes.TAG_RDOEM;
                    break;
                case TAG_INVENTORY_COMPACT:
                    operationTypes1 = Cs108Library4A.OperationTypes.TAG_INVENTORY_COMPACT;
                    break;
                case TAG_INVENTORY:
                    operationTypes1 = Cs108Library4A.OperationTypes.TAG_INVENTORY;
                    break;
                case TAG_SEARCHING:
                    operationTypes1 = Cs108Library4A.OperationTypes.TAG_SEARCHING;
                    break;
            }
            return cs108Library4A.startOperation(operationTypes1);
        } else if (isCs710Connected()) {
            Cs710Library4A.OperationTypes operationTypes1 = null;
            switch (operationTypes) {
                case TAG_RDOEM:
                    operationTypes1 = Cs710Library4A.OperationTypes.TAG_RDOEM;
                    break;
                case TAG_INVENTORY_COMPACT:
                    operationTypes1 = Cs710Library4A.OperationTypes.TAG_INVENTORY_COMPACT;
                    break;
                case TAG_INVENTORY:
                    operationTypes1 = Cs710Library4A.OperationTypes.TAG_INVENTORY;
                    break;
                case TAG_SEARCHING:
                    operationTypes1 = Cs710Library4A.OperationTypes.TAG_SEARCHING;
                    break;
            }
            return cs710Library4A.startOperation(operationTypes1);
        }
        else Log.i("Hello2", "startOperation" + stringNOTCONNECT);
        return false;
    } //2563

    public boolean abortOperation() {
        if (DEBUG) Log.i("Hello2", "abortOperation");
        if (isCs108Connected()) return cs108Library4A.abortOperation();
        else if (isCs710Connected()) return cs710Library4A.abortOperation();
        else Log.i("Hello2", "abortOperation" + stringNOTCONNECT);
        return false;
    } //2601

    public void restoreAfterTagSelect() {
        if (DEBUG | true) Log.i("Hello2", "restoreAfterTagSelect");
        if (isCs108Connected()) cs108Library4A.restoreAfterTagSelect();
        else if (isCs710Connected()) cs710Library4A.restoreAfterTagSelect();
        else Log.i("Hello2", "restoreAfterTagSelect" + stringNOTCONNECT);
    } //2609

    public boolean setSelectedTagByTID(String strTagId, long pwrlevel) {
        appendToLog("csLibrary4A: setSelectCriteria setSelectedByTID strTagId = " + strTagId + ", pwrlevel = " + pwrlevel);
        if (isCs108Connected()) return cs108Library4A.setSelectedTagByTID(strTagId, pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTagByTID(strTagId, pwrlevel);
        else Log.i("Hello2", "setSelectedTagByTID" + stringNOTCONNECT);
        return false;
    } //2647

    public boolean setSelectedTag(String strTagId, int selectBank, long pwrlevel) {
        if (DEBUG) Log.i("Hello2", "setSelectedTag 1");
        if (isCs108Connected()) return cs108Library4A.setSelectedTag(strTagId, selectBank, pwrlevel);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTag(strTagId, selectBank, pwrlevel);
        else Log.i("Hello2", "setSelectedTag 1" + stringNOTCONNECT);
        return false;
    } //2651

    public boolean setSelectedTag(String selectMask, int selectBank, int selectOffset, long pwrlevel, int qValue, int matchRep) {
        appendToLog("csLibraryA: setSelectCriteria strTagId = " + selectMask + ", selectBank = " + selectBank + ", selectOffset = " + selectOffset + ", pwrlevel = " + pwrlevel + ", qValue = " + qValue + ", matchRep = " + matchRep);
        if (isCs108Connected()) return cs108Library4A.setSelectedTag(selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        else if (isCs710Connected()) return cs710Library4A.setSelectedTag(selectMask, selectBank, selectOffset, pwrlevel, qValue, matchRep);
        else Log.i("Hello2", "setSelectedTag 2" + stringNOTCONNECT);
        return false;
    } //2851

    public boolean setMatchRep(int matchRep) {
        if (DEBUG) Log.i("Hello2", "setMatchRep");
        if (isCs108Connected()) return cs108Library4A.setMatchRep(matchRep);
        else if (isCs710Connected()) return cs710Library4A.setMatchRep(matchRep);
        else Log.i("Hello2", "setMatchRep" + stringNOTCONNECT);
        return false;
    } //2659

    public String[] getCountryList() {
        if (DEBUG) Log.i("Hello2", "getCountryList");
        if (isCs108Connected()) return cs108Library4A.getCountryList();
        else if (isCs710Connected()) return cs710Library4A.getCountryList();
        else Log.i("Hello2", "getCountryList" + stringNOTCONNECT);
        return null;
    } //2950

    public int getCountryNumberInList() {
        if (DEBUG) Log.i("Hello2", "getCountryNumberInList");
        if (isCs108Connected()) return cs108Library4A.getCountryNumberInList();
        else if (isCs710Connected()) return cs710Library4A.getCountryNumberInList();
        else Log.i("Hello2", "getCountryNumberInList" + stringNOTCONNECT);
        return -1;
    } //2949

    public boolean setCountryInList(int countryInList) {
        if (DEBUG || true) Log.i("Hello2", "setCountryInList");
        if (isCs108Connected()) return cs108Library4A.setCountryInList(countryInList);
        else if (isCs710Connected()) return cs710Library4A.setCountryInList(countryInList);
        else Log.i("Hello2", "setCountryInList" + stringNOTCONNECT);
        return false;
    } //3005

    public boolean getChannelHoppingStatus() {
        if (DEBUG) Log.i("Hello2", "getChannelHoppingStatus");
        if (isCs108Connected()) return cs108Library4A.getChannelHoppingStatus();
        else if (isCs710Connected()) return cs710Library4A.getChannelHoppingStatus();
        else Log.i("Hello2", "getChannelHoppingStatus" + stringNOTCONNECT);
        return false;
    } //3046

    public boolean setChannelHoppingStatus(boolean channelOrderHopping) {
        Log.i("Hello2", "setChannelHoppingStatus");
        return false; } //3061

    public String[] getChannelFrequencyList() {
        if (isCs108Connected()) return cs108Library4A.getChannelFrequencyList();
        else if (isCs710Connected()) return cs710Library4A.getChannelFrequencyList();
        else Log.i("Hello2", "getChannelFrequencyList" + stringNOTCONNECT);
        return null;
    } //2950

    public int getChannel() {
        if (DEBUG) Log.i("Hello2", "getChannel");
        if (isCs108Connected()) return cs108Library4A.getChannel();
        else if (isCs710Connected()) return cs710Library4A.getChannel();
        else Log.i("Hello2", "getChannel" + stringNOTCONNECT);
        return -1;
    } //3083

    public boolean setChannel(int channelSelect) {
        if (DEBUG) Log.i("Hello2", "setChannel");
        if (isCs108Connected()) return cs108Library4A.setChannel(channelSelect);
        else if (isCs710Connected()) return cs710Library4A.setChannel(channelSelect);
        else Log.i("Hello2", "setChannel" + stringNOTCONNECT);
        return false;
    } //3094

    public byte getPopulation2Q(int population) {
        if (DEBUG) Log.i("Hello2", "getPopulation2Q");
        if (isCs108Connected()) return cs108Library4A.getPopulation2Q(population);
        else if (isCs710Connected()) return cs710Library4A.getPopulation2Q(population);
        else Log.i("Hello2", "getPopulation2Q" + stringNOTCONNECT);
        return -1;
    } //3339

    public int getPopulation() {
        if (DEBUG) Log.i("Hello2", "getPopulation");
        if (isCs108Connected()) return cs108Library4A.getPopulation();
        else if (isCs710Connected()) return cs710Library4A.getPopulation();
        else Log.i("Hello2", "getPopulation" + stringNOTCONNECT);
        return -1;
    } //3348

    public boolean setPopulation(int population) {
        if (DEBUG) Log.i("Hello2", "setPopulation");
        if (isCs108Connected()) return cs108Library4A.setPopulation(population);
        else if (isCs710Connected()) return cs710Library4A.setPopulation(population);
        else Log.i("Hello2", "setPopulation" + stringNOTCONNECT);
        return false;
    } //3349

    public byte getQValue() {
        if (DEBUG) Log.i("Hello2", "getQValue");
        if (isCs108Connected()) return cs108Library4A.getQValue();
        else if (isCs710Connected()) return cs710Library4A.getQValue();
        else Log.i("Hello2", "getQValue" + stringNOTCONNECT);
        return -1;
    } //3359

    public boolean setQValue(byte byteValue) {
        if (DEBUG) Log.i("Hello2", "setQValue");
        if (isCs108Connected()) return cs108Library4A.setQValue(byteValue);
        else if (isCs710Connected()) return cs710Library4A.setQValue(byteValue);
        else Log.i("Hello2", "setQValue" + stringNOTCONNECT);
        return false;
    } //3362

    public String getBarcodeDate() {
        if (DEBUG) Log.i("Hello2", "getBarcodeDate");
        if (isCs108Connected()) return cs108Library4A.getBarcodeDate();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeDate();
        else Log.i("Hello2", "getBarcodeDate" + stringNOTCONNECT);
        return null;
    } //3375

    public String getRadioSerial() {
        if (DEBUG) Log.i("Hello2", "getRadioSerial");
        if (isCs108Connected()) return cs108Library4A.getRadioSerial();
        else if (isCs710Connected()) return cs710Library4A.getRadioSerial();
        else Log.i("Hello2", "getRadioSerial" + stringNOTCONNECT);
        return null;
    } //3377

    public String getRadioBoardVersion() {
        if (DEBUG) Log.i("Hello2", "getRadioBoardVersion");
        if (isCs108Connected()) return cs108Library4A.getRadioBoardVersion();
        else if (isCs710Connected()) return cs710Library4A.getRadioBoardVersion();
        else Log.i("Hello2", "getRadioBoardVersion" + stringNOTCONNECT);
        return null;
    } //3393

    public boolean getBarcodeOnStatus() {
        if (DEBUG) Log.i("Hello2", "getBarcodeOnStatus");
        if (isCs108Connected()) return cs108Library4A.getBarcodeOnStatus();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeOnStatus();
        else Log.i("Hello2", "getBarcodeOnStatus" + stringNOTCONNECT);
        return false;
    } //3398

    public boolean setBarcodeOn(boolean on) {
        if (DEBUG) Log.i("Hello2", "setBarcodeOn");
        if (isCs108Connected()) return cs108Library4A.setBarcodeOn(on);
        else if (isCs710Connected()) return cs710Library4A.setBarcodeOn(on);
        else Log.i("Hello2", "setBarcodeOn" + stringNOTCONNECT);
        return false;
    } //3412

    public boolean setVibrateOn(int mode) {
        if (DEBUG || true) Log.i("Hello2", "setVibrateOn with mode = " + mode);
        if (isCs108Connected()) return cs108Library4A.setVibrateOn(mode);
        else if (isCs710Connected()) return cs710Library4A.setVibrateOn(mode);
        else Log.i("Hello2", "setVibrateOn" + stringNOTCONNECT);
        return false;
    } //3433

    public boolean barcodeSendCommandTrigger() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandTrigger");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandTrigger();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandTrigger();
        else Log.i("Hello2", "barcodeSendCommandTrigger" + stringNOTCONNECT);
        return false;
    } //3468

    public boolean barcodeSendCommandSetPreSuffix() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandSetPreSuffix");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandSetPreSuffix();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandSetPreSuffix();
        else Log.i("Hello2", "barcodeSendCommandSetPreSuffix" + stringNOTCONNECT);
        return false;
    } //3480

    public boolean barcodeSendCommandResetPreSuffix() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandResetPreSuffix");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandResetPreSuffix();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandResetPreSuffix();
        else Log.i("Hello2", "barcodeSendCommandResetPreSuffix" + stringNOTCONNECT);
        return false;
    } //3503
    public boolean barcodeSendCommandConinuous() {
        if (DEBUG) Log.i("Hello2", "barcodeSendCommandConinuous");
        if (isCs108Connected()) return cs108Library4A.barcodeSendCommandConinuous();
        else if (isCs710Connected()) return cs710Library4A.barcodeSendCommandConinuous();
        else Log.i("Hello2", "barcodeSendCommandConinuous" + stringNOTCONNECT);
        return false;
    } //3522

    public String getBarcodeVersion() {
        if (DEBUG) Log.i("Hello2", "getBarcodeVersion");
        if (isCs108Connected()) return cs108Library4A.getBarcodeVersion();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeVersion();
        else Log.i("Hello2", "getBarcodeVersion" + stringNOTCONNECT);
        return null;
    } //3539

    public String getBarcodeSerial() {
        if (DEBUG) Log.i("Hello2", "getBarcodeSerial");
        if (isCs108Connected()) return cs108Library4A.getBarcodeSerial();
        else if (isCs710Connected()) return cs710Library4A.getBarcodeSerial();
        else Log.i("Hello2", "getBarcodeSerial" + stringNOTCONNECT);
        return null;
    } //3563

    public boolean barcodeInventory(boolean start) {
        if (DEBUG) Log.i("Hello2", "barcodeInventory");
        if (isCs108Connected()) return cs108Library4A.barcodeInventory(start);
        else if (isCs710Connected()) return cs710Library4A.barcodeInventory(start);
        else Log.i("Hello2", "barcodeInventory" + stringNOTCONNECT);
        return false;
    } //3658

    public String getBluetoothICFirmwareVersion() {
        if (DEBUG) Log.i("Hello2", "getBluetoothICFirmwareVersion");
        if (isCs108Connected()) return cs108Library4A.getBluetoothICFirmwareVersion();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothICFirmwareVersion();
        else Log.i("Hello2", "getBluetoothICFirmwareVersion" + stringNOTCONNECT);
        return null;
    } //3683

    public String getBluetoothICFirmwareName() {
        if (DEBUG) Log.i("Hello2", "getBluetoothICFirmwareName");
        if (isCs108Connected()) return cs108Library4A.getBluetoothICFirmwareName();
        else if (isCs710Connected()) return cs710Library4A.getBluetoothICFirmwareName();
        else Log.i("Hello2", "getBluetoothICFirmwareName" + stringNOTCONNECT);
        return null;
    } //3686

    public boolean setBluetoothICFirmwareName(String name) {
        if (DEBUG) Log.i("Hello2", "setBluetoothICFirmwareName");
        if (isCs108Connected()) return cs108Library4A.setBluetoothICFirmwareName(name);
        else if (isCs710Connected()) return cs710Library4A.setBluetoothICFirmwareName(name);
        else Log.i("Hello2", "setBluetoothICFirmwareName" + stringNOTCONNECT);
        return false;
    } //3693

    public boolean forceBTdisconnect() {
        if (DEBUG) Log.i("Hello2", "forceBTdisconnect");
        if (isCs108Connected()) return cs108Library4A.forceBTdisconnect();
        else if (isCs710Connected()) return cs710Library4A.forceBTdisconnect();
        else Log.i("Hello2", "forceBTdisconnect" + stringNOTCONNECT);
        return false;
    } //3696

    public String hostProcessorICGetFirmwareVersion() {
        if (DEBUG) Log.i("Hello2", "hostProcessorICGetFirmwareVersion");
        if (isCs108Connected()) return cs108Library4A.hostProcessorICGetFirmwareVersion();
        else if (isCs710Connected()) return cs710Library4A.hostProcessorICGetFirmwareVersion();
        else Log.i("Hello2", "hostProcessorICGetFirmwareVersion" + stringNOTCONNECT);
        return null;
    } //3699

    public String getHostProcessorICSerialNumber() {
        if (DEBUG) Log.i("Hello2", "getHostProcessorICSerialNumber");
        if (isCs108Connected()) return cs108Library4A.getHostProcessorICSerialNumber();
        else if (isCs710Connected()) return cs710Library4A.getHostProcessorICSerialNumber();
        else Log.i("Hello2", "getHostProcessorICSerialNumber" + stringNOTCONNECT);
        return null;
    } //3702

    public String getHostProcessorICBoardVersion() {
        if (DEBUG) Log.i("Hello2", "getHostProcessorICBoardVersion");
        if (isCs108Connected()) return cs108Library4A.getHostProcessorICBoardVersion();
        else if (isCs710Connected()) return cs710Library4A.getHostProcessorICBoardVersion();
        else Log.i("Hello2", "getHostProcessorICBoardVersion" + stringNOTCONNECT);
        return null;
    } //3709

    public boolean batteryLevelRequest() {
        if (DEBUG) Log.i("Hello2", "batteryLevelRequest");
        if (isCs108Connected()) return cs108Library4A.batteryLevelRequest();
        else if (isCs710Connected()) return cs710Library4A.batteryLevelRequest();
        else Log.i("Hello2", "batteryLevelRequest" + stringNOTCONNECT);
        return false;
    } //3729

    public boolean setAutoBarStartSTop(boolean enable) {
        if (DEBUG) Log.i("Hello2", "setAutoBarStartSTop");
        if (isCs108Connected()) return cs108Library4A.setAutoBarStartSTop(enable);
        else if (isCs710Connected()) return cs710Library4A.setAutoBarStartSTop(enable);
        else Log.i("Hello2", "setAutoBarStartSTop" + stringNOTCONNECT);
        return false;
    } //3763

    public boolean getTriggerReporting() {
        if (DEBUG) Log.i("Hello2", "getTriggerReporting");
        if (isCs108Connected()) cs108Library4A.getTriggerReporting();
        else if (isCs710Connected()) cs710Library4A.getTriggerReporting();
        else Log.i("Hello2", "getTriggerReporting" + stringNOTCONNECT);
        return false;
    } //3780

    public boolean setTriggerReporting(boolean triggerReporting) {
        if (DEBUG) Log.i("Hello2", "setTriggerReporting");
        if (isCs108Connected()) return cs108Library4A.setTriggerReporting(triggerReporting);
        else if (isCs710Connected()) return cs710Library4A.setTriggerReporting(triggerReporting);
        else Log.i("Hello2", "setTriggerReporting" + stringNOTCONNECT);
        return false;
    } //3781

    public int iNO_SUCH_SETTING; //3791
    public short getTriggerReportingCount() { //called before connection
        if (DEBUG) Log.i("Hello2", "getTriggerReportingCount");
        if (isCs108Connected()) return cs108Library4A.getTriggerReportingCount();
        else if (isCs710Connected()) return cs710Library4A.getTriggerReportingCount();
        else Log.i("Hello2", "getTriggerReportingCount" + stringNOTCONNECT);
        return 5;
    } //3793

    public boolean setTriggerReportingCount(short triggerReportingCount) {
        if (DEBUG) Log.i("Hello2", "setTriggerReportingCount");
        if (isCs108Connected()) return cs108Library4A.setTriggerReportingCount(triggerReportingCount);
        else if (isCs710Connected()) return cs710Library4A.setTriggerReportingCount(triggerReportingCount);
        else Log.i("Hello2", "setTriggerReportingCount" + stringNOTCONNECT);
        return false;
    } //3801

    public String getBatteryDisplay(boolean voltageDisplay) {
        if (DEBUG) Log.i("Hello2", "getBatteryDisplay");
        if (isCs108Connected()) return cs108Library4A.getBatteryDisplay(voltageDisplay);
        else if (isCs710Connected()) return cs710Library4A.getBatteryDisplay(voltageDisplay);
        else Log.i("Hello2", "getBatteryDisplay is called befoe connection !!!");
        return null;
    } //3829

    String stringNOTCONNECT;
    public String isBatteryLow() {
        if (DEBUG) Log.i("Hello2", "isBatteryLow");
        if (isCs108Connected()) return cs108Library4A.isBatteryLow();
        else if (isCs710Connected()) return cs710Library4A.isBatteryLow();
        else Log.i("Hello2", "isBatteryLow" + stringNOTCONNECT);
        return null;
    } //3841

    public int getBatteryCount() {
        if (DEBUG2) Log.i("Hello2", "getBatteryCount");
        if (isCs108Connected()) return cs108Library4A.getBatteryCount();
        else if (isCs710Connected()) return cs710Library4A.getBatteryCount();
        else Log.i("Hello2", "getBatteryCount" + stringNOTCONNECT);
        return -1; } //3970

    public boolean getTriggerButtonStatus() {
        if (DEBUG2) Log.i("Hello2", "getTriggerButtonStatus");
        if (isCs108Connected()) return cs108Library4A.getTriggerButtonStatus();
        else if (isCs710Connected()) return cs710Library4A.getTriggerButtonStatus();
        else Log.i("Hello2", "getTriggerButtonStatus" + stringNOTCONNECT);
        return false;
    } //3971

    public int getTriggerCount() {
        if (DEBUG2) Log.i("Hello2", "getTriggerCount");
        if (isCs108Connected()) return cs108Library4A.getTriggerCount();
        else if (isCs710Connected()) return cs710Library4A.getTriggerCount();
        else Log.i("Hello2", "getTriggerCount" + stringNOTCONNECT);
        return -1; } //3972

    public interface NotificationListener { void onChange(); }
    public void setNotificationListener(NotificationListener listener) {
        if (DEBUG) Log.i("Hello2", "setNotificationListener");
        if (isCs108Connected()) {
            cs108Library4A.setNotificationListener(new com.csl.cs108library4a.Cs108Library4A.NotificationListener() {
                @Override
                public void onChange() {
                    listener.onChange();
                }
            });
        } else if (isCs710Connected()) {
            cs710Library4A.setNotificationListener(new Cs710Library4A.NotificationListener() {
                @Override
                public void onChange() {
                    listener.onChange();
                }
            });
        }
        else Log.i("Hello2", "setNotificationListener" + stringNOTCONNECT);
    } //3973

    public int getBatteryDisplaySetting() {
        if (DEBUG) Log.i("Hello2", "getBatteryDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.getBatteryDisplaySetting();
        else if (isCs710Connected()) return cs710Library4A.getBatteryDisplaySetting();
        else Log.i("Hello2", "getBatteryDisplaySetting" + stringNOTCONNECT);
        return -1;
    } //3976

    public boolean setBatteryDisplaySetting(int batteryDisplaySelect) {
        if (DEBUG) Log.i("Hello2", "setBatteryDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.setBatteryDisplaySetting(batteryDisplaySelect);
        else if (isCs710Connected()) return cs710Library4A.setBatteryDisplaySetting(batteryDisplaySelect);
        else Log.i("Hello2", "setBatteryDisplaySetting" + stringNOTCONNECT);
        return false;
    } //3977

    public double dBuV_dBm_constant = -1;
    public int getRssiDisplaySetting() { //called before connection
        if (DEBUG) Log.i("Hello2", "getRssiDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.getRssiDisplaySetting();
        else if (isCs710Connected()) return cs710Library4A.getRssiDisplaySetting();
        return 0;
    } //3985

    public boolean setRssiDisplaySetting(int rssiDisplaySelect) {
        if (DEBUG) Log.i("Hello2", "setRssiDisplaySetting");
        if (isCs108Connected()) return cs108Library4A.setRssiDisplaySetting(rssiDisplaySelect);
        else if (isCs710Connected()) return cs710Library4A.setRssiDisplaySetting(rssiDisplaySelect);
        else Log.i("Hello2", "setRssiDisplaySetting" + stringNOTCONNECT);
        return false;
    } //3986

    public int getVibrateModeSetting() {
        if (DEBUG) Log.i("Hello2", "getVibrateModeSetting");
        if (isCs108Connected()) return cs108Library4A.getVibrateModeSetting();
        else if (isCs710Connected()) return cs710Library4A.getVibrateModeSetting();
        else Log.i("Hello2", "getVibrateModeSetting" + stringNOTCONNECT);
        return -1;
    } //3993

    public boolean setVibrateModeSetting(int vibrateModeSelect) {
        if (DEBUG) Log.i("Hello2", "setVibrateModeSetting");
        if (isCs108Connected()) return cs108Library4A.setVibrateModeSetting(vibrateModeSelect);
        else if (isCs710Connected()) return cs710Library4A.setVibrateModeSetting(vibrateModeSelect);
        else Log.i("Hello2", "setVibrateModeSetting" + stringNOTCONNECT);
        return false;
    } //3994

    public int getSavingFormatSetting() {
        if (DEBUG) Log.i("Hello2", "getSavingFormatSetting");
        if (isCs108Connected()) return cs108Library4A.getSavingFormatSetting();
        else if (isCs710Connected()) return cs710Library4A.getSavingFormatSetting();
        else Log.i("Hello2", "getSavingFormatSetting" + stringNOTCONNECT);
        return -1;
    } //4001

    public boolean setSavingFormatSetting(int savingFormatSelect) {
        if (DEBUG) Log.i("Hello2", "setSavingFormatSetting");
        if (isCs108Connected()) return cs108Library4A.setSavingFormatSetting(savingFormatSelect);
        else if (isCs710Connected()) return cs710Library4A.setSavingFormatSetting(savingFormatSelect);
        else Log.i("Hello2", "setSavingFormatSetting" + stringNOTCONNECT);
        return false;
    } //4002

    public enum CsvColumn {
        RESERVE_BANK,
        EPC_BANK,
        TID_BANK,
        USER_BANK,
        PHASE,
        CHANNEL,
        TIME, TIMEZONE,
        LOCATION, DIRECTION,
        OTHERS
    }
    public int getCsvColumnSelectSetting() {
        if (DEBUG) Log.i("Hello2", "getCsvColumnSelectSetting");
        if (isCs108Connected()) return cs108Library4A.getCsvColumnSelectSetting();
        else if (isCs710Connected()) return cs710Library4A.getCsvColumnSelectSetting();
        else Log.i("Hello2", "getCsvColumnSelectSetting" + stringNOTCONNECT);
        return -1;
    } //4020

    public boolean setCsvColumnSelectSetting(int csvColumnSelect) {
        if (DEBUG) Log.i("Hello2", "setCsvColumnSelectSetting");
        if (isCs108Connected()) return cs108Library4A.setCsvColumnSelectSetting(csvColumnSelect);
        else if (isCs710Connected()) return cs710Library4A.setCsvColumnSelectSetting(csvColumnSelect);
        else Log.i("Hello2", "setCsvColumnSelectSetting" + stringNOTCONNECT);
        return false;
    } //4021

    public static class Cs108ScanData {
        public BluetoothDevice device; String name, address;
        public int rssi;
        public byte[] scanRecord;
        ArrayList<byte[]> decoded_scanRecord;
        public int serviceUUID2p2;

        Cs108ScanData(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
            decoded_scanRecord = new ArrayList<byte[]>();
        }
        Cs108ScanData(String name, String address, int rssi, byte[] scanRecord) {
            this.device = device; this.name = name; this.address = address;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }
        public BluetoothDevice getDevice() { return device; }
        public String getName() {
            return name;
        }
        public String getAddress() {
            return address;
        }
        public byte[] getScanRecord() { return scanRecord; }
    }
    public Cs108ScanData getNewDeviceScanned() { //called before connection
        if (DEBUG2) Log.i("Hello2", "getNewDeviceScanned");
        com.csl.cs108library4a.Cs108Library4A.Cs108ScanData cs108ScanData1;
        Cs710Library4A.Cs108ScanData cs108ScanData7 = cs710Library4A.getNewDeviceScanned();
        Cs108ScanData cs108ScanData = null;
        if (cs108ScanData7 == null) {
            cs108ScanData1 = cs108Library4A.getNewDeviceScanned();
            if (cs108ScanData1 != null) {
                cs108ScanData = new Cs108ScanData(cs108ScanData1.getDevice(), cs108ScanData1.rssi, cs108ScanData1.getScanRecord());
                cs108ScanData.serviceUUID2p2 = cs108ScanData1.serviceUUID2p2;
            }
        } else {
            cs108ScanData = new Cs108ScanData(cs108ScanData7.getDevice(), cs108ScanData7.rssi, cs108ScanData7.getScanRecord());
            cs108ScanData.serviceUUID2p2 = cs108ScanData7.serviceUUID2p2;
        }
        return cs108ScanData;
    } //4026

    public Rx000pkgData onRFIDEvent() {
        if (DEBUG2) Log.i("Hello2", "onRFIDEvent");
        if (isCs108Connected()) {
            Rx000pkgData rx000pkgData = null;
            com.csl.cs108library4a.Cs108Library4A.Rx000pkgData rx000pkgData1 = cs108Library4A.onRFIDEvent();
            if (rx000pkgData1 != null) {
                rx000pkgData = new Rx000pkgData();
                switch (rx000pkgData1.responseType) {
                    case TYPE_18K6C_INVENTORY_COMPACT:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                        break;
                    case TYPE_18K6C_INVENTORY:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                        break;
                    case TYPE_COMMAND_ABORT_RETURN:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN;
                        break;
                    case TYPE_COMMAND_END:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_COMMAND_END;
                        break;
                    case TYPE_18K6C_TAG_ACCESS:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                        break;
                    default:
                        Log.i("Hello2", "onRFIDEvent: responseType = " + rx000pkgData1.responseType.toString());
                }
                rx000pkgData.flags = rx000pkgData1.flags;
                rx000pkgData.dataValues = rx000pkgData1.dataValues;
                rx000pkgData.decodedTime = rx000pkgData1.decodedTime;
                rx000pkgData.decodedRssi = rx000pkgData1.decodedRssi;
                rx000pkgData.decodedPhase = rx000pkgData1.decodedPhase;
                rx000pkgData.decodedChidx = rx000pkgData1.decodedChidx;
                rx000pkgData.decodedPort = rx000pkgData1.decodedPort;
                rx000pkgData.decodedPc = rx000pkgData1.decodedPc;
                rx000pkgData.decodedEpc = rx000pkgData1.decodedEpc;
                rx000pkgData.decodedCrc = rx000pkgData1.decodedCrc;
                rx000pkgData.decodedData1 = rx000pkgData1.decodedData1;
                rx000pkgData.decodedData2 = rx000pkgData1.decodedData2;
                rx000pkgData.decodedResult = rx000pkgData1.decodedResult;
                rx000pkgData.decodedError = rx000pkgData1.decodedError;
            }
            return rx000pkgData;
        }
        else if (isCs710Connected()) {
            Rx000pkgData rx000pkgData = null;
            Cs710Library4A.Rx000pkgData rx000pkgData1 = cs710Library4A.onRFIDEvent();
            if (rx000pkgData1 != null) {
                rx000pkgData = new Rx000pkgData();
                switch (rx000pkgData1.responseType) {
                    case TYPE_18K6C_INVENTORY_COMPACT:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                        break;
                    case TYPE_18K6C_INVENTORY:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                        break;
                    case TYPE_COMMAND_ABORT_RETURN:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_COMMAND_ABORT_RETURN;
                        break;
                    case TYPE_COMMAND_END:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_COMMAND_END;
                        break;
                    case TYPE_18K6C_TAG_ACCESS:
                        rx000pkgData.responseType = HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                        break;
                    default:
                        Log.i("Hello2", "onRFIDEvent: responseType = " + rx000pkgData1.responseType.toString());
                }
                rx000pkgData.flags = rx000pkgData1.flags;
                rx000pkgData.dataValues = rx000pkgData1.dataValues;
                rx000pkgData.decodedTime = rx000pkgData1.decodedTime;
                rx000pkgData.decodedRssi = rx000pkgData1.decodedRssi;
                rx000pkgData.decodedPhase = rx000pkgData1.decodedPhase;
                rx000pkgData.decodedChidx = rx000pkgData1.decodedChidx;
                rx000pkgData.decodedPort = rx000pkgData1.decodedPort;
                rx000pkgData.decodedPc = rx000pkgData1.decodedPc;
                rx000pkgData.decodedEpc = rx000pkgData1.decodedEpc;
                rx000pkgData.decodedCrc = rx000pkgData1.decodedCrc;
                rx000pkgData.decodedData1 = rx000pkgData1.decodedData1;
                rx000pkgData.decodedData2 = rx000pkgData1.decodedData2;
                rx000pkgData.decodedResult = rx000pkgData1.decodedResult;
                rx000pkgData.decodedError = rx000pkgData1.decodedError;
            }
            if (rx000pkgData != null) appendToLog("response0 = " + rx000pkgData.responseType.toString() + ", " + byteArrayToString(rx000pkgData.dataValues));
            return rx000pkgData;
        }
        else Log.i("Hello2", "onRFIDEvent" + stringNOTCONNECT);
        return null;
    } //4034

    public byte[] onNotificationEvent() {
        if (DEBUG2) Log.i("Hello2", "onNotificationEvent");
        if (isCs108Connected()) return cs108Library4A.onNotificationEvent();
        else if (isCs710Connected()) return cs710Library4A.onNotificationEvent();
        else Log.i("Hello2", "onNotificationEvent" + stringNOTCONNECT);
        return null;
    } //4062

    public byte[] onBarcodeEvent() {
        if (DEBUG) Log.i("Hello2", "onBarcodeEvent");
        if (isCs108Connected()) return cs108Library4A.onBarcodeEvent();
        else if (isCs710Connected()) return cs710Library4A.onBarcodeEvent();
        else Log.i("Hello2", "onBarcodeEvent" + stringNOTCONNECT);
        return null;
    } //4073

    public String getModelNumber() {
        if (DEBUG) Log.i("Hello2", "getModelNumber");
        if (isCs108Connected()) return cs108Library4A.getModelNumber();
        else if (isCs710Connected()) return cs710Library4A.getModelNumber();
        else Log.i("Hello2", "getModelNumber" + stringNOTCONNECT);
        return null;
    } //4188

    public boolean setRx000KillPassword(String password) {
        if (DEBUG) Log.i("Hello2", "setRx000KillPassword");
        if (isCs108Connected()) return cs108Library4A.setRx000KillPassword(password);
        else if (isCs710Connected()) return cs710Library4A.setRx000KillPassword(password);
        else Log.i("Hello2", "setRx000KillPassword" + stringNOTCONNECT);
        return false;
    } //4223

    public boolean setRx000AccessPassword(String password) {
        if (DEBUG) Log.i("Hello2", "setRx000AccessPassword");
        if (isCs108Connected()) return cs108Library4A.setRx000AccessPassword(password);
        else if (isCs710Connected()) return cs710Library4A.setRx000AccessPassword(password);
        else Log.i("Hello2", "setRx000AccessPassword" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
        if (DEBUG) Log.i("Hello2", "setAccessRetry");
        if (isCs108Connected()) return cs108Library4A.setAccessRetry(accessVerfiy, accessRetry);
        else if (isCs710Connected()) return cs710Library4A.setAccessRetry(accessVerfiy, accessRetry);
        else Log.i("Hello2", "setAccessRetry" + stringNOTCONNECT);
        return false;
    }

    public boolean setInvModeCompact(boolean invModeCompact) {
        if (DEBUG) Log.i("Hello2", "setInvModeCompact");
        if (isCs108Connected()) return cs108Library4A.setInvModeCompact(invModeCompact);
        else if (isCs710Connected()) return cs710Library4A.setInvModeCompact(invModeCompact);
        else Log.i("Hello2", "setInvModeCompact" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
        if (DEBUG) Log.i("Hello2", "setAccessLockAction");
        if (isCs108Connected()) return cs108Library4A.setAccessLockAction(accessLockAction, accessLockMask);
        else if (isCs710Connected()) return cs710Library4A.setAccessLockAction(accessLockAction, accessLockMask);
        else Log.i("Hello2", "setAccessLockAction" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessBank(int accessBank) {
        if (DEBUG) Log.i("Hello2", "setAccessBank 1");
        if (isCs108Connected()) return cs108Library4A.setAccessBank(accessBank);
        else if (isCs710Connected()) return cs710Library4A.setAccessBank(accessBank);
        else Log.i("Hello2", "setAccessBank 1" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessBank(int accessBank, int accessBank2) {
        if (DEBUG) Log.i("Hello2", "setAccessBank 2");
        if (isCs108Connected()) return cs108Library4A.setAccessBank(accessBank, accessBank2);
        else if (isCs710Connected()) return cs710Library4A.setAccessBank(accessBank, accessBank2);
        else Log.i("Hello2", "setAccessBank 2" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessOffset(int accessOffset) {
        if (DEBUG) Log.i("Hello2", "setAccessOffset 1");
        if (isCs108Connected()) return cs108Library4A.setAccessOffset(accessOffset);
        else if (isCs710Connected()) return cs710Library4A.setAccessOffset(accessOffset);
        else Log.i("Hello2", "setAccessOffset 1" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessOffset(int accessOffset, int accessOffset2) {
        if (DEBUG) Log.i("Hello2", "setAccessOffset 2");
        if (isCs108Connected()) return cs108Library4A.setAccessOffset(accessOffset, accessOffset2);
        else if (isCs710Connected()) return cs710Library4A.setAccessOffset(accessOffset, accessOffset2);
        else Log.i("Hello2", "setAccessOffset 2" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessCount(int accessCount) {
        if (DEBUG) Log.i("Hello2", "setAccessCount 1");
        if (isCs108Connected()) return cs108Library4A.setAccessCount(accessCount);
        else if (isCs710Connected()) return cs710Library4A.setAccessCount(accessCount);
        else Log.i("Hello2", "setAccessCount 1" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessCount(int accessCount, int accessCount2) {
        if (DEBUG) Log.i("Hello2", "setAccessCount 2");
        if (isCs108Connected()) return cs108Library4A.setAccessCount(accessCount, accessCount2);
        else if (isCs710Connected()) return cs710Library4A.setAccessCount(accessCount, accessCount2);
        else Log.i("Hello2", "setAccessCount 2" + stringNOTCONNECT);
        return false;
    }

    public boolean setAccessWriteData(String dataInput) {
        if (DEBUG) Log.i("Hello2", "setAccessWriteData");
        if (isCs108Connected()) return cs108Library4A.setAccessWriteData(dataInput);
        else if (isCs710Connected()) return cs710Library4A.setAccessWriteData(dataInput);
        else Log.i("Hello2", "setAccessWriteData" + stringNOTCONNECT);
        return false;
    }

    public boolean setTagRead(int tagRead) {
        if (DEBUG) Log.i("Hello2", "setTagRead");
        if (isCs108Connected()) return cs108Library4A.setTagRead(tagRead);
        else if (isCs710Connected()) return cs710Library4A.setTagRead(tagRead);
        else Log.i("Hello2", "setTagRead" + stringNOTCONNECT);
        return false;
    } //4235

    public boolean setInvBrandId(boolean invBrandId) {
        if (DEBUG) Log.i("Hello2", "setInvBrandId");
        if (isCs108Connected()) return cs108Library4A.setInvBrandId(invBrandId);
        else if (isCs710Connected()) return cs710Library4A.setInvBrandId(invBrandId);
        else Log.i("Hello2", "setInvBrandId" + stringNOTCONNECT);
        return false;
    } //4243

    public float decodeCtesiusTemperature(String strActData, String strCalData) {
        return utility.decodeCtesiusTemperature(strActData, strCalData);
    } //4278
    public float decodeMicronTemperature(int iTag35, String strActData, String strCalData) {
        appendToLog("iTag35 = " + iTag35 + ", strActData = " + strActData + ", strCalData = " + strCalData);
        float fValue = utility.decodeMicronTemperature(iTag35, strActData, strCalData);
        appendToLog(String.format("fValue = %f", fValue));
        return fValue;
    } //4323
    public float decodeAsygnTemperature(String string) { return utility.decodeAsygnTemperature(string); } //4278

    public enum HostCommands {
        NULL, CMD_WROEM, CMD_RDOEM, CMD_ENGTEST, CMD_MBPRDREG, CMD_MBPWRREG,
        CMD_18K6CINV, CMD_18K6CREAD, CMD_18K6CWRITE, CMD_18K6CLOCK, CMD_18K6CKILL, CMD_SETPWRMGMTCFG, CMD_18K6CAUTHENTICATE,
        CMD_UPDATELINKPROFILE,
        CMD_18K6CBLOCKWRITE,
        CMD_CHANGEEAS, CMD_GETSENSORDATA,
        CMD_READBUFFER, CMD_UNTRACEABLE,
        CMD_FDM_RDMEM, CMD_FDM_WRMEM, CMD_FDM_AUTH, CMD_FDM_GET_TEMPERATURE, CMD_FDM_START_LOGGING, CMD_FDM_STOP_LOGGING,
        CMD_FDM_WRREG, CMD_FDM_RDREG, CMD_FDM_DEEP_SLEEP, CMD_FDM_OPMODE_CHECK, CMD_FDM_INIT_REGFILE, CMD_FDM_LED_CTRL,
        CMD_18K6CINV_SELECT,
        CMD_18K6CINV_COMPACT, CMD_18K6CINV_COMPACT_SELECT,
        CMD_18K6CINV_MB, CMD_18K6CINV_MB_SELECT
    }
    public enum HostCmdResponseTypes {
        NULL,
        TYPE_COMMAND_BEGIN,
        TYPE_COMMAND_END,
        TYPE_18K6C_INVENTORY, TYPE_18K6C_INVENTORY_COMPACT,
        TYPE_18K6C_TAG_ACCESS,
        TYPE_ANTENNA_CYCLE_END,
        TYPE_COMMAND_ACTIVE,
        TYPE_COMMAND_ABORT_RETURN
    }
    public static class Rx000pkgData {
        public HostCmdResponseTypes responseType;
        public int flags;
        public byte[] dataValues;
        public long decodedTime;
        public double decodedRssi;
        public int decodedPhase, decodedChidx, decodedPort;
        public byte[] decodedPc, decodedEpc, decodedCrc, decodedData1, decodedData2;
        public String decodedResult;
        public String decodedError;
    }

    public boolean sendHostRegRequestHST_CMD(HostCommands hostCommand) {
        if (DEBUG | true) Log.i("Hello2", "sendHostRegRequestHST_CMD with hostCommand = " + hostCommand.toString());
        if (isCs108Connected()) {
            com.csl.cs108library4a.Cs108Library4A.HostCommands hostCommands1 = null;
            switch (hostCommand) {
                case CMD_18K6CREAD:
                    hostCommands1 = com.csl.cs108library4a.Cs108Library4A.HostCommands.CMD_18K6CREAD;
                    break;
                case CMD_18K6CWRITE:
                    hostCommands1 = com.csl.cs108library4a.Cs108Library4A.HostCommands.CMD_18K6CWRITE;
                    break;
                case CMD_18K6CLOCK:
                    hostCommands1 = com.csl.cs108library4a.Cs108Library4A.HostCommands.CMD_18K6CLOCK;
                    break;
                case CMD_18K6CKILL:
                    hostCommands1 = com.csl.cs108library4a.Cs108Library4A.HostCommands.CMD_18K6CKILL;
                    break;
                case CMD_18K6CAUTHENTICATE:
                    hostCommands1 = com.csl.cs108library4a.Cs108Library4A.HostCommands.CMD_18K6CAUTHENTICATE;
                    break;
                case CMD_GETSENSORDATA:
                    hostCommands1 = com.csl.cs108library4a.Cs108Library4A.HostCommands.CMD_GETSENSORDATA;
                default:
                    Log.i("Hello2", "sendHostRegRequestHST_CMD: hostCommand = " + hostCommand.toString());
                    break;
            }
            if (hostCommands1 == null) return false;
            else return cs108Library4A.sendHostRegRequestHST_CMD(hostCommands1);
        } else if (isCs710Connected()) {
            Cs710Library4A.HostCommands hostCommands1 = null;
            switch (hostCommand) {
                case CMD_18K6CREAD:
                    hostCommands1 = Cs710Library4A.HostCommands.CMD_18K6CREAD;
                    break;
                case CMD_18K6CWRITE:
                    hostCommands1 = Cs710Library4A.HostCommands.CMD_18K6CWRITE;
                    break;
                case CMD_18K6CLOCK:
                    hostCommands1 = Cs710Library4A.HostCommands.CMD_18K6CLOCK;
                    break;
                case CMD_18K6CKILL:
                    hostCommands1 = Cs710Library4A.HostCommands.CMD_18K6CKILL;
                    break;
                case CMD_18K6CAUTHENTICATE:
                    hostCommands1 = Cs710Library4A.HostCommands.CMD_18K6CAUTHENTICATE;
                    break;
                case CMD_GETSENSORDATA:
                    hostCommands1 = Cs710Library4A.HostCommands.CMD_GETSENSORDATA;
                default:
                    Log.i("Hello2", "sendHostRegRequestHST_CMD: hostCommand = " + hostCommand.toString());
                    break;
            }
            if (hostCommands1 == null) return false;
            return cs710Library4A.sendHostRegRequestHST_CMD(hostCommands1);
        }
        else Log.i("Hello2", "sendHostRegRequestHST_CMD" + stringNOTCONNECT);
        return false;
    } //4237

    public boolean setPwrManagementMode(boolean bLowPowerStandby) { //called before connection
        if (DEBUG) Log.i("Hello2", "setPwrManagementMode");
        if (isCs108Connected()) return cs108Library4A.setPwrManagementMode(bLowPowerStandby);
        else if (isCs710Connected()) return cs710Library4A.setPwrManagementMode(bLowPowerStandby);
        return false;
    } //4241

    public void macWrite(int address, long value) {
        Log.i("Hello2", "macWrite");
    } //4248
    public void set_fdCmdCfg(int value) {
        Log.i("Hello2", "set_fdCmdCfg");
    } //4250
    public void set_fdRegAddr(int addr) {
        Log.i("Hello2", "set_fdRegAddr");
    } //4253
    public void set_fdWrite(int addr, long value) {
        Log.i("Hello2", "set_fdWrite");
    } //4254
    public void set_fdPwd(int value) {
        Log.i("Hello2", "set_fdPwd");
    } //4258
    public void set_fdBlockAddr4GetTemperature(int addr) {
        Log.i("Hello2", "set_fdBlockAddr4GetTemperature");
    } //4259
    public void set_fdReadMem(int addr, long len) {
        Log.i("Hello2", "set_fdReadMem");
    } //4262
    public void set_fdWriteMem(int addr, int len, long value) {
        Log.i("Hello2", "set_fdWriteMem");
    } //4266

    public void setImpinJExtension(boolean tagFocus, boolean fastId) {
        if (DEBUG) Log.i("Hello2", "setImpinJExtension with tagFocus = " + tagFocus + ", fastId = " + fastId);
        if (isCs108Connected()) cs108Library4A.setImpinJExtension(tagFocus, fastId);
        else if (isCs710Connected()) cs710Library4A.setImpinJExtension(tagFocus, fastId);
        else Log.i("Hello2", "setImpinJExtension" + stringNOTCONNECT);
    } //4271

    public String strFloat16toFloat32(String strData) {
        Log.i("Hello2", "strFloat16toFloat32");
        return null; } //4387
    public String str2float16(String strData) {
        Log.i("Hello2", "str2float16");
        return null; } //4393
    public String temperatureC2F(String strValue) {
        Log.i("Hello2", "temperatureC2F");
        return null; } //4424
    public String temperatureF2C(String strValue) {
        Log.i("Hello2", "temperatureF2C");
        return null; } //4436

    public int get98XX() {
        if (DEBUG) Log.i("Hello2", "get98XX");
        if (isCs108Connected()) return cs108Library4A.get98XX();
        else if (isCs710Connected()) return cs710Library4A.get98XX();
        else Log.i("Hello2", "get98XX" + stringNOTCONNECT);
        return -1;
    } //4445

        //Cs108Connector Routine
    public int invalidata, invalidUpdata, validata; //123
    public boolean checkHostProcessorVersion(String version, int majorVersion, int minorVersion, int buildVersion) {
        if (DEBUG) Log.i("Hello2", "checkHostProcessorVersion");
        if (isCs108Connected()) return cs108Library4A.checkHostProcessorVersion(version, majorVersion, minorVersion, buildVersion);
        else if (isCs710Connected()) return cs710Library4A.checkHostProcessorVersion(version, majorVersion, minorVersion, buildVersion);
        else Log.i("Hello2", "checkHostProcessorVersion" + stringNOTCONNECT);
        return false; } //421
/*
    public String getWedgePrefix() {
        if (isCs108Connected()) return cs108Library4A.getWedgePrefix();
        else return cs710Library4A.getWedgePrefix();
    }
    public String getWedgeSuffix() {
        if (isCs108Connected()) return cs108Library4A.getWedgeSuffix();
        else return cs710Library4A.getWedgeSuffix();
    }
    public int getWedgeDelimiter() {
        if (isCs108Connected()) return cs108Library4A.getWedgeDelimiter();
        else return cs710Library4A.getWedgeDelimiter();
    }
    public void setWedgePrefix(String string) {
        if (isCs108Connected()) cs108Library4A.setWedgePrefix(string);
        else cs710Library4A.setWedgePrefix(string);
    }
    public void setWedgeSuffix(String string) {
        if (isCs108Connected()) cs108Library4A.setWedgeSuffix(string);
        else cs710Library4A.setWedgeSuffix(string);
    }
    public void setWedgeDelimiter(int iValue) {
        if (isCs108Connected()) cs108Library4A.setWedgeDelimiter(iValue);
        else cs710Library4A.setWedgeDelimiter(iValue);
    }
*/
}
