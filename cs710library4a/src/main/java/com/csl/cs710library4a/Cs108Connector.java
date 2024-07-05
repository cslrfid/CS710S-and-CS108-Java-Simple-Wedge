package com.csl.cs710library4a;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

public class Cs108Connector extends BleConnector {
    final boolean appendToLogViewDisable = false;
    final boolean DEBUG = false; final boolean DEBUGTHREAD = false;
    boolean sameCheck = true;

    @Override
    boolean connectBle(ReaderDevice readerDevice) {
        boolean result = false;
        if (DEBUG_CONNECT) appendToLog("ConnectBle(" + readerDevice.getCompass() + ")");
        result = super.connectBle(readerDevice);
        if (result)
            writeDataCount = 0;
        return result;
    }

    @Override
    boolean isBleConnected() { return super.isBleConnected(); }

    @Override
    void disconnect() {
        super.disconnect();
        appendToLog("abcc done");
        mRfidDevice.mRfidToWrite.clear();
        mRfidDevice.mRfidReaderChip.mRx000ToWrite.clear();
    }

    long getStreamInRate() { return super.getStreamInRate(); }

    int writeDataCount; int btSendTimeOut = 0; long btSendTime = 0;
    boolean writeData(byte[] buffer, int timeout) {
        if (mRfidDevice.isInventoring()) {
            appendToLogView("BtData: isInventoring is true when writeData " + byteArrayToString(buffer));
        }
        boolean result = writeBleStreamOut(buffer);
        if (result == false) appendToLog("!!! failure to writeBleStreamOut");
        if (true) {
            btSendTime = System.currentTimeMillis();
            btSendTimeOut = timeout + 60;
            if (isCharacteristicListRead() == false) btSendTimeOut += 3000;
        }
        appendToLog("BtDataOut: done writeBleStreamOut with timeout = " + timeout + ", btSendTimeout = " + btSendTimeOut + ", btSendTime = " + btSendTime);
        return result;
    }

    int[] crc_lookup_table = new int[]{
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
            0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
            0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
            0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
            0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
            0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
            0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
            0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
            0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
            0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
            0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
            0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
            0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
            0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
            0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
            0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
            0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
            0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
            0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
            0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
            0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
            0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
            0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
            0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
            0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
            0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
            0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
            0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
            0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78};

    boolean dataRead = false; int dataReadDisplayCount = 0; boolean mCs108DataReadRequest = false;
    int inventoryLength = 0;
    int iSequenceNumber; boolean bDifferentSequence = false, bFirstSequence = true;
    public int invalidata, invalidUpdata, validata;
    boolean dataInBufferResetting;
    @Override
    void processBleStreamInData() {
        final boolean DEBUG = false;
        int cs108DataReadStartOld = 0;
        int cs108DataReadStart = 0;
        boolean validHeader = false;

        if (dataInBufferResetting) {
            if (DEBUG) appendToLog("RESET.");
            dataInBufferResetting = false;
            /*cs108DataLeft = new byte[CS108DATALEFT_SIZE];*/
            cs108DataLeftOffset = 0;
            mCs108DataRead.clear();
        }
        if (DEBUG) appendToLog("START, cs108DataLeftOffset=" + cs108DataLeftOffset + ", streamInBufferSize=" + getStreamInBufferSize());
        boolean bFirst = true; long lTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - lTime > (getIntervalProcessBleStreamInData()/2)) {
                writeDebug2File("B" + String.valueOf(getIntervalProcessBleStreamInData()) + ", " + System.currentTimeMillis() + ", Timeout");
                if (DEBUG) appendToLogView("processCs108DataIn_TIMEOUT");
                break;
            }

            long streamInOverflowTime = getStreamInOverflowTime();
            int streamInMissing = getStreamInBytesMissing();
            if (streamInMissing != 0) appendToLogView("processCs108DataIn(" + getStreamInTotalCounter() + ", " + getStreamInAddCounter() + "): len=0, getStreamInOverflowTime()=" + streamInOverflowTime + ", MissBytes=" + streamInMissing + ", Offset=" + cs108DataLeftOffset);
            int len = readData(cs108DataLeft, cs108DataLeftOffset, cs108DataLeft.length);
            if (len != 0) {
                byte[] debugData = new byte[len];
                System.arraycopy(cs108DataLeft, cs108DataLeftOffset, debugData, 0, len);
                if (DEBUG) appendToLog("DataIn = " + byteArrayToString(debugData));
            }
            if (len != 0 && bFirst) { bFirst = false; writeDebug2File("B" + String.valueOf(getIntervalProcessBleStreamInData()) + ", " + System.currentTimeMillis()); }
            cs108DataLeftOffset += len;
            if (len == 0) {
                if (zeroLenDisplayed == false) {
                    zeroLenDisplayed = true;
                    if (getStreamInTotalCounter() != getStreamInAddCounter() || getStreamInAddTime() != 0 || cs108DataLeftOffset != 0) {
                        if (DEBUG) appendToLog("processCs108DataIn(" + getStreamInTotalCounter() + "," + getStreamInAddCounter() + "): len=0, getStreamInAddTime()=" + getStreamInAddTime() + ", Offset=" + cs108DataLeftOffset);
                    }
                }
                if (cs108DataLeftOffset == cs108DataLeft.length) {
                    if (DEBUG) appendToLog("cs108DataLeftOffset=" + cs108DataLeftOffset + ", cs108DataLeft=" + byteArrayToString(cs108DataLeft));
                }
                break;
            } else {
                dataRead = true;
                zeroLenDisplayed = false;

                if (DEBUG) appendToLog("cs108DataLeftOffset = " + cs108DataLeftOffset + ", cs108DataReadStart = " + cs108DataReadStart);
                while (cs108DataLeftOffset >= cs108DataReadStart + 8) {
                    validHeader = false;
                    byte[] dataIn = cs108DataLeft;
                    int iPayloadLength = (dataIn[cs108DataReadStart + 2] & 0xFF);
                    if ((dataIn[cs108DataReadStart + 0] == (byte) 0xA7)
                            && (dataIn[cs108DataReadStart + 1] == (byte) 0xB3)
                            && (dataIn[cs108DataReadStart + 3] == (byte) 0xC2
                            || dataIn[cs108DataReadStart + 3] == (byte) 0x6A
                            || dataIn[cs108DataReadStart + 3] == (byte) 0xD9
                            || dataIn[cs108DataReadStart + 3] == (byte) 0xE8
                            || dataIn[cs108DataReadStart + 3] == (byte) 0x5F)
                            //&& ((dataIn[cs108DataReadStart + 4] == (byte) 0x82) || ((dataIn[cs108DataReadStart + 3] == (byte) 0xC2) && (dataIn[cs108DataReadStart + 8] == (byte) 0x81)))
                            && (dataIn[cs108DataReadStart + 5] == (byte) 0x9E)) {
                        if (cs108DataLeftOffset - cs108DataReadStart < (iPayloadLength + 8))
                            break;

                        boolean bcheckChecksum = true;
                        int checksum = ((byte) dataIn[cs108DataReadStart + 6] & 0xFF) * 256 + ((byte) dataIn[cs108DataReadStart + 7] & 0xFF);
                        int checksum2 = 0;
                        if (bcheckChecksum) {
                            for (int i = cs108DataReadStart; i < cs108DataReadStart + 8 + iPayloadLength; i++) {
                                if (i != (cs108DataReadStart + 6) && i != (cs108DataReadStart + 7)) {
                                    int index = (checksum2 ^ ((byte) dataIn[i] & 0x0FF)) & 0x0FF;
                                    int table_value = crc_lookup_table[index];
                                    checksum2 = (checksum2 >> 8) ^ table_value;
                                }
                            }
                            if (DEBUG) appendToLog("checksum = " + String.format("%04X", checksum) + ", checksum2 = " + String.format("%04X", checksum2));
                        }
                        if (bcheckChecksum && checksum != checksum2) {
                            if (iPayloadLength < 0) {
                                if (DEBUG) appendToLog("processCs108DataIn_ERROR, iPayloadLength=" + iPayloadLength + ", cs108DataLeftOffset=" + cs108DataLeftOffset + ", dataIn=" + byteArrayToString(dataIn));
                            }
                            if (true) {
                                byte[] invalidPart = new byte[8 + iPayloadLength];
                                System.arraycopy(dataIn, cs108DataReadStart, invalidPart, 0, invalidPart.length);
                                if (DEBUG) appendToLog("processCs108DataIn_ERROR, INCORRECT RevChecksum=" + Integer.toString(checksum, 16) + ", CalChecksum2=" + Integer.toString(checksum2, 16) + ",data=" + byteArrayToString(invalidPart));
                            }
                        } else {
                            validHeader = true;
                            if (cs108DataReadStart > cs108DataReadStartOld) {
                                if (true) {
                                    byte[] invalidPart = new byte[cs108DataReadStart - cs108DataReadStartOld];
                                    System.arraycopy(dataIn, cs108DataReadStartOld, invalidPart, 0, invalidPart.length);
                                    if (DEBUG) appendToLog("processCs108DataIn_ERROR, before valid data, invalid unused data: " + invalidPart.length + ", " + byteArrayToString(invalidPart));
                                }
                            } else if (cs108DataReadStart < cs108DataReadStartOld)
                                if (DEBUG) appendToLog("processCs108DataIn_ERROR, invalid cs108DataReadStartdata=" + cs108DataReadStart + " < cs108DataReadStartOld=" + cs108DataReadStartOld);
                            cs108DataReadStartOld = cs108DataReadStart;

                            Cs108ReadData cs108ReadData = new Cs108ReadData();
                            byte[] dataValues = new byte[iPayloadLength];
                            System.arraycopy(dataIn, cs108DataReadStart + 8, dataValues, 0, dataValues.length);
                            cs108ReadData.dataValues = dataValues;
                            cs108ReadData.milliseconds = System.currentTimeMillis(); //getStreamInDataMilliSecond(); //
                            if (DEBUG) appendToLog("current:" + System.currentTimeMillis() + ", streamInData:" + getStreamInDataMilliSecond());
                            if (false) {
                                byte[] headerbytes = new byte[8];
                                System.arraycopy(dataIn, cs108DataReadStart, headerbytes, 0, headerbytes.length);
                                if (DEBUG) appendToLog("processCs108DataIn: Got package=" + byteArrayToString(headerbytes) + " " + byteArrayToString(dataValues));
                            }
                            switch (dataIn[cs108DataReadStart + 3]) {
                                case (byte) 0xC2:
                                case (byte) 0x6A:
                                    if (dataIn[cs108DataReadStart + 3] == 0xC2) cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.RFID;
                                    else cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.BARCODE;
                                    if (dataIn[cs108DataReadStart + 8] == (byte) 0x81 || dataIn[cs108DataReadStart + 8] == (byte) 0x91) {
                                        int iSequenceNumber = (int) (dataIn[cs108DataReadStart + 4] & 0xFF);
                                        int itemp = iSequenceNumber;
                                        if (itemp < this.iSequenceNumber) {
                                            itemp += 256;
                                        }
                                        if (false) appendToLog("0 itemp = " + itemp + ", this.iSequenceNumber = " + this.iSequenceNumber);
                                        itemp -= (this.iSequenceNumber + 1);
                                        if (false) appendToLog("1 itemp = " + itemp + ", this.iSequenceNumber = " + this.iSequenceNumber + ", bFirstSequence = " + bFirstSequence);
                                        boolean bRecdOldSequence = true;
                                        if (itemp != 0) {
                                            cs108ReadData.invalidSequence = true;
                                            if (bFirstSequence == false) {
                                                bRecdOldSequence = false;
                                                invalidata += itemp;
                                                String stringSequenceList = "";
                                                for (int i = 0; i < itemp; i++) {
                                                    int iMissedNumber = (iSequenceNumber - i - 1);
                                                    if (iMissedNumber < 0) iMissedNumber += 256;
                                                    stringSequenceList += (i != 0 ? ", " : "") + String.format("%X", iMissedNumber);
                                                }
                                                appendToLogView(String.format("ERROR !!!: invalidata = %d, %X - %X, miss %d: ", invalidata, iSequenceNumber, this.iSequenceNumber, itemp) + stringSequenceList);
                                            }
                                        }
                                        bFirstSequence = false;
                                        if (bRecdOldSequence == false) {
                                            appendToLog("!!! invalid sequence[new = " + iSequenceNumber + ", old = " + this.iSequenceNumber + " for DataIn[Start=" + cs108DataReadStart + "] = " + byteArrayToString(dataIn));
                                        }
                                        this.iSequenceNumber = iSequenceNumber;
                                    }
                                    if (false) appendToLogView("Rin: " + (cs108ReadData.invalidSequence ? "invalid sequence" : "ok") + "," + byteArrayToString(cs108ReadData.dataValues));
                                    validata++;
                                    break;
                                case (byte) 0xD9:
                                    if (DEBUG)
                                        appendToLog("BARTRIGGER NotificationData = " + byteArrayToString(cs108ReadData.dataValues));
                                    cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.NOTIFICATION;
                                    break;
                                case (byte) 0xE8:
                                    cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.SILICON_LAB;
                                    break;
                                case (byte) 0x5F:
                                    cs108ReadData.cs108ConnectedDevices = Cs108ConnectedDevices.BLUETOOTH;
                                    break;
                            }
                            mCs108DataRead.add(cs108ReadData);
                            cs108DataReadStart += ((8 + iPayloadLength));

                            byte[] cs108DataLeftNew = new byte[CS108DATALEFT_SIZE];
                            System.arraycopy(cs108DataLeft, cs108DataReadStart, cs108DataLeftNew, 0, cs108DataLeftOffset - cs108DataReadStart);
                            cs108DataLeft = cs108DataLeftNew;
                            cs108DataLeftOffset -= cs108DataReadStart;
                            cs108DataReadStart = 0;
                            cs108DataReadStart = -1;
                            if (mCs108DataReadRequest == false) {
                                mCs108DataReadRequest = true;
                                appendToLog("ready2Write: start immediate mReadWriteRunnable");
                                mHandler.removeCallbacks(mReadWriteRunnable); mHandler.post(mReadWriteRunnable);
                            }
                        }
                    }
                    if (validHeader && cs108DataReadStart < 0) {
                        cs108DataReadStart = 0;
                        cs108DataReadStartOld = 0;
                    } else {
                        cs108DataReadStart++;
                    }
                }
                if (cs108DataReadStart != 0 && cs108DataLeftOffset >= 8) {
                    if (true) {
                        byte[] invalidPart = new byte[cs108DataReadStart];
                        System.arraycopy(cs108DataLeft, 0, invalidPart, 0, invalidPart.length);
                        byte[] validPart = new byte[cs108DataLeftOffset - cs108DataReadStart];
                        System.arraycopy(cs108DataLeft, cs108DataReadStart, validPart, 0, validPart.length);
                        if (DEBUG) appendToLog("processCs108DataIn_ERROR, ENDLOOP invalid unused data: " + invalidPart.length + ", " + byteArrayToString(invalidPart) + ", with valid data length=" + validPart.length + ", " + byteArrayToString(validPart));
                    }

                    byte[] cs108DataLeftNew = new byte[CS108DATALEFT_SIZE];
                    System.arraycopy(cs108DataLeft, cs108DataReadStart, cs108DataLeftNew, 0, cs108DataLeftOffset - cs108DataReadStart);
                    cs108DataLeft = cs108DataLeftNew;
                    cs108DataLeftOffset -= cs108DataReadStart; cs108DataReadStart = 0;
                }
            }
        }
        if (DEBUG) appendToLog("END, cs108DataLeftOffset=" + cs108DataLeftOffset + ", streamInBufferSize=" + getStreamInBufferSize());
    }

    private int readData(byte[] buffer, int byteOffset, int byteCount) { return readBleSteamIn(buffer, byteOffset, byteCount); }

    class Cs108ConnectorData {
        int mVoltageValue; int getVoltageMv() { return mVoltageValue; }
        int mVoltageCount; int getVoltageCnt() { return mVoltageCount; }
        boolean triggerButtonStatus; boolean getTriggerButtonStatus() { return triggerButtonStatus; }
        int iTriggerCount; int getTriggerCount() { return iTriggerCount; }
        Date timeStamp;
        String getTimeStamp() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            return sdf.format(mCs108ConnectorData.timeStamp);
        }

    }
    Cs108ConnectorData mCs108ConnectorData;

    RfidDevice mRfidDevice;
    BarcodeDevice mBarcodeDevice;
    NotificationDevice mNotificationDevice;
    SiliconLabIcDevice mSiliconLabIcDevice;
    BluetoothConnector mBluetoothConnector;

    private Handler mHandler = new Handler();

    void cs108ConnectorDataInit() {
        mCs108DataRead = new ArrayList<>();
        cs108DataLeft = new byte[CS108DATALEFT_SIZE];
        cs108DataLeftOffset = 0;
        zeroLenDisplayed = false;

        invalidata = 0;
        validata = 0;
        dataInBufferResetting = false;

        writeDataCount = 0;

        mCs108ConnectorData = new Cs108ConnectorData();

        mRfidDevice = new RfidDevice();
        mBarcodeDevice = new BarcodeDevice();
        mNotificationDevice = new NotificationDevice();
        mSiliconLabIcDevice = new SiliconLabIcDevice();
        mBluetoothConnector = new BluetoothConnector(context, mLogView);
        appendToLog("!!! all major classes are initialised");
    }

    enum Cs108ConnectedDevices {
        RFID, BARCODE, NOTIFICATION, SILICON_LAB, BLUETOOTH, OTHER
    }
    class Cs108ReadData {
        Cs108ConnectedDevices cs108ConnectedDevices;
        byte[] dataValues;
        boolean invalidSequence;
        long milliseconds;

    }
    final int CS108DATALEFT_SIZE = 300; //4000;    //100;
    private ArrayList<Cs108ReadData> mCs108DataRead;
    byte[] cs108DataLeft;
    int cs108DataLeftOffset;
    boolean zeroLenDisplayed;

    Context context; TextView mLogView;
    Cs108Connector(Context context, TextView mLogView) {
        super(context, mLogView);

        this.context = context;
        this.mLogView = mLogView;

        cs108ConnectorDataInit();
        mHandler.removeCallbacks(runnableProcessBleStreamInData); mHandler.post(runnableProcessBleStreamInData);
        appendToLog("ready2Write: start immediate mReadWriteRunnable");
        mHandler.removeCallbacks(mReadWriteRunnable); mHandler.post(mReadWriteRunnable);
        mHandler.removeCallbacks(runnableRx000UplinkHandler); mHandler.post(runnableRx000UplinkHandler);
    }
/*
    boolean batteryReportRequest = false; boolean batteryReportOn = false;
    void setBatteryAutoReport(boolean on) {
        appendToLog("setBatteryAutoReport(" + on + ")");
        batteryReportRequest = true; batteryReportOn = on;
    }
    void setBatteryAutoReport() {
        if (batteryReportRequest) {
            batteryReportRequest = false;
            appendToLog("setBatteryAutoReport()");
            boolean retValue = false;
            if (checkHostProcessorVersion(mSiliconLabIcDevice.getSiliconLabIcVersion(), 1, 0, 2)) {
                appendToLog("setBatteryAutoReport(): 111");
                if (batteryReportOn)
                    retValue = mNotificationDevice.mNotificationToWrite.add(NotificationPayloadEvents.NOTIFICATION_AUTO_BATTERY_VOLTAGE);
                else
                    retValue = mNotificationDevice.mNotificationToWrite.add(NotificationPayloadEvents.NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE);
            }
        }
    }*/
    public boolean checkHostProcessorVersion(String version, int majorVersion, int minorVersion, int buildVersion) {
        if (version == null) return false;
        if (version.length() == 0) return false;
        String[] versionPart = version.split("\\.");

        if (versionPart == null) { appendToLog("NULL VersionPart"); return false; }
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

    int rfidPowerOnTimeOut = 0; int barcodePowerOnTimeOut = 0;
    long timeReady; boolean aborting = false, sendFailure = false;
    private final Runnable mReadWriteRunnable = new Runnable() {
        boolean ready2Write = false, DEBUG = false;
        int timer2Write = 0;
        boolean validBuffer;

        @Override
        public void run() {
            if (DEBUGTHREAD) appendToLog("mReadWriteRunnable starts");
            if (timer2Write != 0 || getStreamInBufferSize() != 0 || mRfidDevice.mRfidToRead.size() != 0) {
                validBuffer = true;
                if (DEBUG) appendToLog("mReadWriteRunnable(): START, timer2Write=" + timer2Write + ", streamInBufferSize = " + getStreamInBufferSize() + ", mRfidToRead.size=" + mRfidDevice.mRfidToRead.size() + ", mRx000ToRead.size=" + mRfidDevice.mRfidReaderChip.mRx000ToRead.size());
            } else  validBuffer = false;
            int intervalReadWrite = 250; //50;   //50;    //500;   //500, 100;
            if (rfidPowerOnTimeOut >= intervalReadWrite) {
                rfidPowerOnTimeOut -= intervalReadWrite;
                if (rfidPowerOnTimeOut <= 0) {
                    rfidPowerOnTimeOut = 0;
                }
            }
            if (barcodePowerOnTimeOut >= intervalReadWrite) {
                barcodePowerOnTimeOut -= intervalReadWrite;
                if (barcodePowerOnTimeOut <= 0) {
                    barcodePowerOnTimeOut = 0;
                }
            }
            if (barcodePowerOnTimeOut != 0)
                if (DEBUG) appendToLog("mReadWriteRunnable(): barcodePowerOnTimeOut = " + barcodePowerOnTimeOut);

            long lTime = System.currentTimeMillis();
            if (DEBUGTHREAD) appendToLog("ready2Write: start new mReadWriteRunnable after " + intervalReadWrite + " ms");
            mHandler.postDelayed(mReadWriteRunnable, intervalReadWrite);
            if (mRfidDevice.mRfidReaderChip == null) return;

            boolean bFirst = true;
            mCs108DataReadRequest = false;
            while (mCs108DataRead.size() != 0) {
                if (isBleConnected() == false) {
                    mCs108DataRead.clear();
                } else if (System.currentTimeMillis() - lTime > (intervalRx000UplinkHandler / 2)) {
                        writeDebug2File("C" + String.valueOf(intervalReadWrite) + ", " + System.currentTimeMillis() + ", Timeout");
                        appendToLogView("mReadWriteRunnable: TIMEOUT !!! mCs108DataRead.size() = " + mCs108DataRead.size());
                        break;
                } else {
                    if (bFirst) { bFirst = false; writeDebug2File("C" + String.valueOf(intervalReadWrite) + ", " + System.currentTimeMillis()); }
                    try {
                        Cs108ReadData cs108ReadData = mCs108DataRead.get(0);
                        mCs108DataRead.remove(0);
                        if (DEBUG) appendToLog("mReadWriteRunnable(): mCs108DataRead.dataValues = " + byteArrayToString(cs108ReadData.dataValues));
                        if (mRfidDevice.isMatchRfidToWrite(cs108ReadData)) {
                            if (writeDataCount > 0) writeDataCount--; //ready2Write = true; //btSendTime = 0; aborting = false; appendToLog("mReadWriteRunnable: 1 btSendTime is set to 0 to allow new sending.");
                        } else if (mBarcodeDevice.isMatchBarcodeToWrite(cs108ReadData)) {
                            if (writeDataCount > 0) writeDataCount--; //ready2Write = true; appendToLog("ready2Write is set true after true isMatchBarcodeToWrite ");//btSendTime = 0; appendToLog("mReadWriteRunnable: 2 btSendTime is set to 0 to allow new sending.");
                        } else if (mNotificationDevice.isMatchNotificationToWrite(cs108ReadData)) {
                            if (writeDataCount > 0) writeDataCount--; ready2Write = true; appendToLog("ready2Write is set true after true isMatchNotificationToWrite "); btSendTime = 0; if (DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: matched notification. btSendTime is set to 0 to allow new sending.");
                        } else if (mSiliconLabIcDevice.isMatchSiliconLabIcToWrite(cs108ReadData)) {
                            if (writeDataCount > 0) writeDataCount--; ready2Write = true; appendToLog("ready2Write is set true after true isMatchSiliconLabIcToWrite "); btSendTime = 0; if (DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: matched AtmelIc. btSendTime is set to 0 to allow new sending.");
                        } else if (mBluetoothConnector.mBluetoothIcDevice.isMatchBluetoothIcToWrite(cs108ReadData)) {
                            if (writeDataCount > 0) writeDataCount--; ready2Write = true; appendToLog("ready2Write is set true after true isMatchBluetoothIcToWrite "); btSendTime = 0; if (DEBUG_PKDATA) appendToLog("PKData: mReadWriteRunnable: matched bluetoothIc. btSendTime is set to 0 to allow new sending.");
                        } else if (mRfidDevice.isRfidToRead(cs108ReadData)) { mRfidDevice.rfidValid = true;
                        } else if (mBarcodeDevice.isBarcodeToRead(cs108ReadData)) {
                        } else if (mNotificationDevice.isNotificationToRead(cs108ReadData)) {
                            /* if (mRfidDevice.mRfidToWrite.size() != 0 && mNotificationDevice.mNotificationToRead.size() != 0) {
                                mNotificationDevice.mNotificationToRead.remove(0);
                                mRfidDevice.mRfidToWrite.clear();
                                mSiliconLabIcDevice.mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.RESET);

                                timeReady = System.currentTimeMillis() - 1500;
                                appendToLog("mReadWriteRunnable: endingMessage: changed timeReady");
                            }*/
                        } else appendToLog("mReadWriteRunnable: !!! CANNOT process " + byteArrayToString(cs108ReadData.dataValues) + " with mDataToWriteRemoved = " + mBarcodeDevice.mDataToWriteRemoved);
                        if (mBarcodeDevice.mDataToWriteRemoved)  { mBarcodeDevice.mDataToWriteRemoved = false; ready2Write = true; appendToLog("ready2Write is set true after true mBarcodeDevice.mDataToWriteRemoved "); btSendTime = 0; if (DEBUG_PKDATA) appendToLog("PkData: mReadWriteRunnable: processed barcode. btSendTime is set to 0 to allow new sending."); }
                    } catch (Exception ex) {
                    }
                }
            }

            lTime = System.currentTimeMillis();
            if (mRfidDevice.mRfidToWriteRemoved)  {
                mRfidDevice.mRfidToWriteRemoved = false; ready2Write = true; appendToLog("ready2Write is set true after true mRfidDevice.mRfidToWriteRemoved ");
                btSendTime = (lTime - btSendTimeOut + 60);
                if (DEBUGTHREAD) appendToLog("ready2Write: start new mReadWriteRunnable after " + 60 + " ms");
                mHandler.removeCallbacks(mReadWriteRunnable); mHandler.postDelayed(mReadWriteRunnable, 60 + 2);
                if (DEBUG_PKDATA || true) appendToLog("PkData: mReadWriteRunnable: processed Rfidcode. btSendTime is set to 0 to allow new sending with systime = " + lTime); }
            //int timeout2Ready = 2000; if (aborting || sendFailure) timeout2Ready = 200;
            //if (lTime > timeReady + timeout2Ready) ready2Write = true;
            //if (mBarcodeDevice.mBarcodeToWrite.size() != 0 && DEBUG) appendToLog("mBarcodeToWrite.size = " + mBarcodeDevice.mBarcodeToWrite.size() + ", ready2write = " + ready2Write);
            if (ready2Write == false && lTime - btSendTime > btSendTimeOut) {
                appendToLog("ready2Write is set to true from false with difference = " + (lTime - btSendTime) + ", systime = " + lTime + ", btSendTime = " + btSendTime + ", btSendTimeOut = " + btSendTime);
                ready2Write = true;
            }
            appendToLog("ready2Write = " + ready2Write);
            if (ready2Write) {
                timeReady = System.currentTimeMillis();
                timer2Write = 0;
                if (mRfidDevice.rfidFailure) mRfidDevice.mRfidToWrite.clear();
                if (mBarcodeDevice.barcodeFailure) mBarcodeDevice.mBarcodeToWrite.clear();
                if (mRfidDevice.mRfidReaderChip.mRx000ToWrite.size() != 0 && mRfidDevice.mRfidToWrite.size() == 0) {
                    if (DEBUG) appendToLog("mReadWriteRunnable(): mRx000ToWrite.size=" + mRfidDevice.mRfidReaderChip.mRx000ToWrite.size() + ", mRfidToWrite.size=" + mRfidDevice.mRfidToWrite.size());
                    mRfidDevice.mRfidReaderChip.addRfidToWrite(mRfidDevice.mRfidReaderChip.mRx000ToWrite.get(0));
                }
                boolean bisRfidCommandStop = false, bisRfidCommandExecute = false;
                if (mRfidDevice.mRfidToWrite.size() != 0 && DEBUG) appendToLog("mRfidToWrite = " + mRfidDevice.mRfidToWrite.get(0).rfidPayloadEvent.toString() + "." + byteArrayToString(mRfidDevice.mRfidToWrite.get(0).dataValues) + ", ready2write = " + ready2Write);
                if (mRfidDevice.mRfidToWrite.size() != 0) {
                    Cs108RfidData cs108RfidData = mRfidDevice.mRfidToWrite.get(0);
                    if (cs108RfidData.rfidPayloadEvent == RfidPayloadEvents.RFID_COMMAND) {
                        int ii;
                        if (false) {
                            byte[] byCommandExeccute = new byte[]{0x70, 1, 0, (byte) 0xF0};
                            for (ii = 0; ii < 4; ii++) {
                                if (byCommandExeccute[ii] != cs108RfidData.dataValues[ii]) break;
                            }
                            if (ii == 4) bisRfidCommandExecute = true;
                        }

                        byte[] byCommandStop = new byte[]{(byte) 0x40, 3, 0, 0, 0, 0, 0, 0};
                        for (ii = 0; ii < 4; ii++) {
                            if (byCommandStop[ii] != cs108RfidData.dataValues[ii]) break;
                        }
                        if (ii == 4) bisRfidCommandStop = true;
                        if (DEBUG) appendToLog("mRfidToWrite(0).dataValues = " + byteArrayToString(mRfidDevice.mRfidToWrite.get(0).dataValues) + ", bisRfidCommandExecute = " + bisRfidCommandExecute + ", bisRfidCommandStop = " + bisRfidCommandStop);
                    }
                }
                if (mBarcodeDevice.mBarcodeToWrite.size() != 0 && DEBUG) appendToLog("mBarcodeToWrite.size = " + mBarcodeDevice.mBarcodeToWrite.size() + ", bisRfidCommandStop = " + bisRfidCommandStop);
                if (bisRfidCommandStop) {
                    appendToLog("BtDataOut: going to sendRfidToWrite as bisRfidCommandStop is true");
                    mRfidDevice.sendRfidToWrite();
                    ready2Write = false;    //
                    appendToLog("ready2Write is set false after sendRfidToWrite");
                } else if (mRfidDevice.isInventoring()) {
                    appendToLog("BtDataOut: done sendRfidToWrite with isInventoring is true");
                    boolean bValue1 = mRfidDevice.sendRfidToWrite();
                    if (bValue1) {
                        ready2Write = false;
                        appendToLog("ready2Write is set false after true sendRfidToWrite");
                    }
                } else if (mSiliconLabIcDevice.sendSiliconLabIcToWrite()) { //SiliconLab version afffects Notification operation
                    ready2Write = false;    //
                    appendToLog("ready2Write is set false after true sendSiliconLabIcToWrite");
                } else if (mBluetoothConnector.mBluetoothIcDevice.mBluetoothIcToWrite.size() != 0) {   //Bluetooth version affects Barcode operation
                    if (isBleConnected() == false) mBluetoothConnector.mBluetoothIcDevice.mBluetoothIcToWrite.clear();
                    else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                        byte[] dataOut = mBluetoothConnector.mBluetoothIcDevice.sendBluetoothIcToWrite();
                        boolean retValue = false;
                        if (DEBUG_PKDATA && mBluetoothConnector.mBluetoothIcDevice.sendDataToWriteSent != 0) appendToLog("!!! mBluetoothIcDevice.sendDataToWriteSent = " + mBluetoothConnector.mBluetoothIcDevice.sendDataToWriteSent);
                        if (DEBUG_PKDATA) appendToLog(String.format("PkData: write mBluetoothIcDevice.%s.%s with mBluetoothIcDevice.sendDataToWriteSent = %d",
                                mBluetoothConnector.mBluetoothIcDevice.mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString(),
                                byteArrayToString(mBluetoothConnector.mBluetoothIcDevice.mBluetoothIcToWrite.get(0).dataValues),
                                mBluetoothConnector.mBluetoothIcDevice.sendDataToWriteSent));
                        if (mBluetoothConnector.mBluetoothIcDevice.sendDataToWriteSent != 0) appendToLog("!!! mBluetoothIcDevice.sendDataToWriteSent = " + mBluetoothConnector.mBluetoothIcDevice.sendDataToWriteSent);
                        if (dataOut != null) retValue = writeData(dataOut, 0);
                        if (retValue) {
                            mBluetoothConnector.mBluetoothIcDevice.sendDataToWriteSent++;
                        } else {
                            if (DEBUG) appendToLogView("failure to send " + mBluetoothConnector.mBluetoothIcDevice.mBluetoothIcToWrite.get(0).bluetoothIcPayloadEvent.toString());
                            mBluetoothConnector.mBluetoothIcDevice.mBluetoothIcToWrite.remove(0);
                        }
                    }
                    ready2Write = false;
                    appendToLog("ready2Write is set false after non-zero mBluetoothIcToWrite.size()");
                } else if (mNotificationDevice.sendNotificationToWrite()) {
                    ready2Write = false;
                    appendToLog("ready2Write is set false after true sendNotificationToWrite");
                } else if (mBarcodeDevice.sendBarcodeToWrite()) {
                    ready2Write = false;
                    appendToLog("ready2Write is set false after true sendBarcodeToWrite");
                } else {
                    //appendToLog("BtDataOut: going to sendRfidToWrite 3");
                    boolean bValue1 = mRfidDevice.sendRfidToWrite();
                    if (bValue1) {
                        appendToLog("ready2Write is set false after true sendRfidToWrite");
                        ready2Write = false;
                    }
                }
            }
            if (validBuffer) {
                if (DEBUG)  appendToLog("mReadWriteRunnable: END, timer2Write=" + timer2Write + ", streamInBufferSize = " + getStreamInBufferSize() + ", mRfidToRead.size=" + mRfidDevice.mRfidToRead.size() + ", mRx000ToRead.size=" + mRfidDevice.mRfidReaderChip.mRx000ToRead.size());
            }
            mRfidDevice.mRfidReaderChip.mRx000UplinkHandler();
            if (DEBUGTHREAD) appendToLog("mReadWriteRunnable: mReadWriteRunnable ends");
        }
    };

    int intervalRx000UplinkHandler = 250;
    private final Runnable runnableRx000UplinkHandler = new Runnable() {
        @Override
        public void run() {
//            mRfidDevice.mRx000Device.mRx000UplinkHandler();
            mHandler.postDelayed(runnableRx000UplinkHandler, intervalRx000UplinkHandler);
        }
    };

    private enum RfidPayloadEvents {
        RFID_POWER_ON, RFID_POWER_OFF, RFID_COMMAND,
        RFID_DATA_READ
    }
    class Cs108RfidData {
        boolean waitUplinkResponse = false;
        boolean downlinkResponded = false;

        boolean uplinkResponded = false;
        boolean waitUplink1Response = false;

        RfidPayloadEvents rfidPayloadEvent;
        byte[] dataValues;
        boolean invalidSequence;
        long milliseconds;
    }
    class RfidDevice {
        private boolean onStatus = false; boolean getOnStatus() { return onStatus;}
        ArrayList<Cs108RfidData> mRfidToWrite = new ArrayList<>();
        ArrayList<Cs108RfidData> mRfidToRead = new ArrayList<>();

        boolean inventoring = false;
        boolean isInventoring() { return  inventoring; }
        void setInventoring(boolean enable) { inventoring = enable; debugFileEnable(false); if (true) appendToLog("isInventoring is set as " + inventoring);}

        RfidReaderChip mRfidReaderChip = new RfidReaderChip();

        private boolean arrayTypeSet(byte[] dataBuf, int pos, RfidPayloadEvents event) {
            boolean validEvent = false;
            switch (event) {
                case RFID_POWER_ON:
                    validEvent = true;
                    break;
                case RFID_POWER_OFF:
                    dataBuf[pos] = 1;
                    validEvent = true;
                    break;
                case RFID_COMMAND:
                    dataBuf[pos] = 2;
                    validEvent = true;
                    break;
            }
            return validEvent;
        }

        boolean writeRfid(Cs108RfidData dataIn) {
            byte[] dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xC2, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0x80, 0};
            if (dataIn.rfidPayloadEvent == RfidPayloadEvents.RFID_COMMAND) {
                if (dataIn.dataValues != null) {
                    byte[] dataOut1 = new byte[dataOut.length + dataIn.dataValues.length];
                    System.arraycopy(dataOut, 0, dataOut1, 0, dataOut.length);
                    dataOut1[2] += dataIn.dataValues.length;
                    System.arraycopy(dataIn.dataValues, 0, dataOut1, dataOut.length, dataIn.dataValues.length);
                    dataOut = dataOut1;
                }
            }
            if (arrayTypeSet(dataOut, 9, dataIn.rfidPayloadEvent)) {
                if (false) appendToLogView(byteArrayToString(dataOut));
                if (DEBUG_PKDATA) appendToLog(String.format("PkData: write Rfid.%s.%s with mRfidDevice.sendRfidToWriteSent = %d", dataIn.rfidPayloadEvent.toString(), byteArrayToString(dataIn.dataValues), sendRfidToWriteSent));
                if (sendRfidToWriteSent != 0) appendToLog("!!! mRfidDevice.sendRfidToWriteSent = " + sendRfidToWriteSent);
                boolean bValue1 = writeData(dataOut, (dataIn.waitUplinkResponse ? 500 : 0));
                appendToLog("done writeData with waitUplinkResponse = " + dataIn.waitUplinkResponse);
                return bValue1;
            }
            return false;
        }

        private boolean isMatchRfidToWrite(Cs108ReadData cs108ReadData) {
            boolean match = false, DEBUG = false;
            if (mRfidToWrite.size() != 0 && cs108ReadData.dataValues[0] == (byte)0x80) {
                byte[] dataInCompare = new byte[]{(byte) 0x80, 0};
                if (arrayTypeSet(dataInCompare, 1, mRfidToWrite.get(0).rfidPayloadEvent) && (cs108ReadData.dataValues.length == dataInCompare.length + 1)) {
                    if (match = compareArray(cs108ReadData.dataValues, dataInCompare, dataInCompare.length)) {
                        if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply with payload = " + byteArrayToString(cs108ReadData.dataValues) + " for writeData Rfid." + mRfidToWrite.get(0).rfidPayloadEvent.toString() + "." + byteArrayToString(mRfidToWrite.get(0).dataValues));
                        if (cs108ReadData.dataValues[2] != 0) {
                            if (DEBUG) appendToLog("Rfid.reply data is found with error");
                        } else {
                            if (mRfidToWrite.get(0).rfidPayloadEvent == RfidPayloadEvents.RFID_POWER_ON) {
                                rfidPowerOnTimeOut = 3000;
                                onStatus = true;
                                if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply.PowerOn with result 0 and onStatus = " + onStatus);
                            } else if (mRfidToWrite.get(0).rfidPayloadEvent == RfidPayloadEvents.RFID_POWER_OFF) {
                                onStatus = false;
                                if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply.PowerOff with result 0 and onStatus = " + onStatus);
                            } else if (DEBUG_PKDATA) appendToLog("PkData: matched Rfid.Reply." + mRfidToWrite.get(0).rfidPayloadEvent.toString() + " with result 0");
                            Cs108RfidData cs108RfidData = mRfidToWrite.get(0);
                            if (cs108RfidData.waitUplinkResponse) {
                                cs108RfidData.downlinkResponded = true;
                                mRfidToWrite.set(0, cs108RfidData);
                                if (DEBUG_PKDATA) appendToLog("PkData: mRfidToWrite.downlinkResponsed is set and waiting uplink data");
                                if (false) {
                                    for (int i = 0; i < mRfidReaderChip.mRx000ToRead.size(); i++) {
                                        if (mRfidReaderChip.mRx000ToRead.get(i).responseType == Cs710Library4A.HostCmdResponseTypes.TYPE_COMMAND_END)
                                            if (DEBUG) appendToLog("mRx0000ToRead with COMMAND_END is removed");
                                    }
                                    if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                                }
                                mRfidReaderChip.mRx000ToRead.clear(); if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                                return true;
                            }
                            if (DEBUG) appendToLog("matched Rfid.reply data is found with mRfidToWrite.size=" + mRfidToWrite.size());
                        }
                        mRfidToWrite.remove(0); sendRfidToWriteSent = 0; mRfidToWriteRemoved = true; if (DEBUG) appendToLog("mmRfidToWrite remove 1 with remained write size = " + mRfidToWrite.size());
                        if (DEBUG_PKDATA) appendToLog("PkData: new mRfidToWrite size = " + mRfidToWrite.size());
                        if (false) {
                            for (int i = 0; i < mRfidReaderChip.mRx000ToRead.size(); i++) {
                                if (mRfidReaderChip.mRx000ToRead.get(i).responseType == Cs710Library4A.HostCmdResponseTypes.TYPE_COMMAND_END)
                                    if (DEBUG) appendToLog("mRx0000ToRead with COMMAND_END is removed");
                            }
                            if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                        }
                        mRfidReaderChip.mRx000ToRead.clear(); if (DEBUG) appendToLog("mRx000ToRead.clear !!!");
                    }
                }
            }
            return match;
        }

        private long logTime;
        private int sendRfidToWriteSent = 0; boolean mRfidToWriteRemoved = false;
        boolean rfidFailure = false; boolean rfidValid = false;
        private boolean sendRfidToWrite() {
            boolean DEBUG = false;
            boolean bValue = false;
            if (rfidPowerOnTimeOut != 0) {
                if (DEBUG) appendToLog("rfidPowerOnTimeOut = " + rfidPowerOnTimeOut + ", mRfidToWrite.size() = " + mRfidToWrite.size());
            } else if (rfidFailure == false && mRfidToWrite.size() != 0) {
                if (isBleConnected() == false) {
                    mRfidToWrite.clear();
                } else {
                    appendToLog("BtDataOut: currentTime = " + System.currentTimeMillis() + ", btSendTime = " + btSendTime + ", difference = " + (System.currentTimeMillis() - btSendTime) + ", btSendTimeOut = " + btSendTimeOut);
                    if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                        RfidPayloadEvents rfidPayloadEvents = mRfidToWrite.get(0).rfidPayloadEvent;
                        if (sendRfidToWriteSent >= 4) {
                            mRfidToWrite.remove(0); sendRfidToWriteSent = 0; mRfidToWriteRemoved = true; if (DEBUG) appendToLog("mmRfidToWrite remove 2");
                            if (DEBUG) appendToLog("Removed after sending count-out.");
                            if (true) {
                                appendToLog("Rfdid data transmission failure !!! clear mRfidToWrite buffer !!!");
                                rfidFailure = true;
                                mRfidToWrite.clear();
                            } else if (rfidValid == false) {
                                Toast.makeText(context, "Problem in sending data to Rfid Module. Rfid is disabled.", Toast.LENGTH_SHORT).show();
                                rfidFailure = true;
                            } else {
                                Toast.makeText(context, "Problem in Sending Commands to RFID Module.  Bluetooth Disconnected.  Please Reconnect", Toast.LENGTH_SHORT).show();
                                appendToLog("disconnect d");
                                disconnect();
                            }
                            if (DEBUG) appendToLog("done");
                        } else {
                            boolean retValue = writeRfid(mRfidToWrite.get(0));
                            if (DEBUG || true) appendToLog("BtDataOut: done writeRfid with size = " + mRfidToWrite.size() + ", PayloadEvents = " + rfidPayloadEvents.toString() + ", data=" + byteArrayToString(mRfidToWrite.get(0).dataValues));
                            sendRfidToWriteSent++;
                            if (retValue)   {
                                mRfidToWriteRemoved = false;
                                if (DEBUG) appendToLog("writeRfid() with sendRfidToWriteSent = " + sendRfidToWriteSent);
                                sendFailure = false;
                                bValue = true;
                            } else sendFailure = true;
                        }
                    }
                }
            }
            return bValue;
        }

        private boolean isRfidToRead(Cs108ReadData cs108ReadData) {
            boolean found = false, DEBUG = false;
            if (cs108ReadData.dataValues[0] == (byte) 0x81) {
                if (DEBUG_PKDATA) appendToLog("PkData: found Rfid.Uplink with payload = " + byteArrayToString(cs108ReadData.dataValues));
                Cs108RfidData cs108RfidReadData = new Cs108RfidData();
                byte[] dataValues = new byte[cs108ReadData.dataValues.length - 2];
                System.arraycopy(cs108ReadData.dataValues, 2, dataValues, 0, dataValues.length);
                if (DEBUG_PKDATA) appendToLog("PkData: found Rfid.Uplink.DataRead with payload = " + byteArrayToString(dataValues));
                switch (cs108ReadData.dataValues[1]) {
                    case 0:
                        if (DEBUG) appendToLog("mRfidToWrite.size = " + mRfidToWrite.size());
                        if (mRfidToWrite.size() > 0) {
                            Cs108RfidData cs108RfidData = mRfidToWrite.get(0);
                            if (DEBUG) appendToLog("downlinkResponsed = " + cs108RfidData.downlinkResponded + ", uplinkResponsed = " + cs108RfidData.uplinkResponded);
                            if (cs108RfidData.downlinkResponded || cs108RfidData.uplinkResponded) {
                                boolean matched = false, updatedUplinkResponse = false;
                                if (DEBUG) appendToLog("mRfidToWrite.dataValue = " + byteArrayToString(cs108RfidData.dataValues) + ", dataValues = " + byteArrayToString(dataValues));
                                if (cs108RfidData.dataValues[0] == (byte)0x80
                                        && cs108RfidData.dataValues[1] == (byte)0xB3
                                        && dataValues[0] == 0x51
                                        && dataValues[1] == (byte)0xE2
                                        && cs108RfidData.dataValues[2] == dataValues[2]
                                        && cs108RfidData.dataValues[3] == dataValues[3]
                                        && cs108RfidData.dataValues[4] == dataValues[4]
                                ) {
                                    boolean valid = false;
                                    byte[] commandValue = null;
                                    int iCommandCode = (dataValues[2] & 0xFF) * 256 + (dataValues[3] & 0xFF);
                                    int iLength = dataValues[5] * 256 + dataValues[6];
                                    if (iLength != 0) {
                                        commandValue = new byte[iLength];
                                        System.arraycopy(dataValues, 7, commandValue, 0, commandValue.length);
                                    }
                                    if (DEBUG_PKDATA) appendToLog("PkData: found Rfid.Uplink.DataRead.CommandResponse" + (iLength != 0 ? " with payload = " + byteArrayToString(commandValue) : ""));
                                    if (DEBUG) appendToLog("found iCommandCode = " + String.format("%4X", iCommandCode) + ", iLength = " + iLength + ", commandValue = " + byteArrayToString(commandValue));
                                    if ((iCommandCode == 0x10A1 && iLength == 0)
                                            || (iCommandCode == 0x10A2 && iLength == 0)
                                            || (iCommandCode == 0x10A3 && iLength == 0)
                                            || (iCommandCode == 0x10A4 && iLength == 0)
                                            || (iCommandCode == 0x10A5 && iLength == 0)
                                            || (iCommandCode == 0x10A6 && iLength == 0)
                                            || (iCommandCode == 0x10AE && iLength == 0)
                                            || (iCommandCode == 0x10B1 && iLength == 0)
                                            || (iCommandCode == 0x10B2 && iLength == 0)
                                            || (iCommandCode == 0x10B7 && iLength == 0)
                                            || (iCommandCode == 0x10B8 && iLength == 0)
                                            || (iCommandCode == 0x10B9 && iLength == 0)
                                            || (iCommandCode == 0x1471 && cs108RfidData.dataValues.length == 11)
                                            || (iCommandCode == 0x9A06 && commandValue.length == 1)
                                    ) valid = true;
                                    String strCommandResponseType = null;
                                    if (iCommandCode == 0x10A1) strCommandResponseType = "RfidStartSimpleInventory";
                                    else if (iCommandCode == 0x10A2) strCommandResponseType = "RfidStartCompactInventory";
                                    else if (iCommandCode == 0x10A3) strCommandResponseType = "RfidStartSelectInventory";
                                    else if (iCommandCode == 0x10A4) strCommandResponseType = "RfidStartMBInventory";
                                    else if (iCommandCode == 0x10A5) strCommandResponseType = "RfidStartSelectMBInventory";
                                    else if (iCommandCode == 0x10A6) strCommandResponseType = "RfidStartSelectCompactInventory";
                                    else if (iCommandCode == 0x10AE) strCommandResponseType = "RfidStopOperation";
                                    else if (iCommandCode == 0x10B1) strCommandResponseType = "RfidReadMB";
                                    else if (iCommandCode == 0x10B2) strCommandResponseType = "RfidWriteMB";
                                    else if (iCommandCode == 0x10B7) strCommandResponseType = "RfidLock";
                                    else if (iCommandCode == 0x10B8) strCommandResponseType = "RfidKill";
                                    else if (iCommandCode == 0x10B9) strCommandResponseType = "RfidAuthenticate";
                                    if (valid) {
                                        if (cs108RfidData.waitUplink1Response) {
                                            found = true;
                                            cs108RfidData.uplinkResponded = true; updatedUplinkResponse = true;
                                            mRfidToWrite.set(0, cs108RfidData);
                                            if (DEBUG_PKDATA) appendToLog("PkData: Rfid.Uplink.DataRead.CommandResponse_" + strCommandResponseType + " is processed to set mRfidToWrite.uplinkResponded and wait uplink data 1");
                                        } else {
                                            matched = true;
                                            if (DEBUG) appendToLog(String.format("000 iCommandCode = 0x%X with writeData.dataValues = %s", iCommandCode, byteArrayToString(cs108RfidData.dataValues)));
                                            if (iCommandCode == 0x10A1) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStartSimpleInventory"); }
                                            else if (iCommandCode == 0x10A2) { mRfidDevice.setInventoring(true); if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStartCompactInventory"); }
                                            else if (iCommandCode == 0x10A3) { mRfidDevice.setInventoring(true); if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStartSelectInventory"); }
                                            else if (iCommandCode == 0x10A4) { mRfidDevice.setInventoring(true); if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStartMBInventory"); }
                                            else if (iCommandCode == 0x10A5) { mRfidDevice.setInventoring(true); if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStartSelectMBInventory"); }
                                            else if (iCommandCode == 0x10A6) { mRfidDevice.setInventoring(true); if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStartSelectCompactInventory"); }
                                            else if (iCommandCode == 0x10AE) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidStopOperation"); }
                                            else if (iCommandCode == 0x10B1) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidReadMB"); }
                                            else if (iCommandCode == 0x10B2) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidWriteMB"); }
                                            else if (iCommandCode == 0x10B7) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidLock"); }
                                            else if (iCommandCode == 0x10B8) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidKill"); }
                                            else if (iCommandCode == 0x10B9) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RfidAuthenticate"); }
                                            //else if ((iCommandCode & 0x7F00) == 0x1000) { if (DEBUG_PKDATA) appendToLog("PkData: uplink data is processed as CommandResponse.RFID??? !!!"); }
                                            else if (iCommandCode == 0x1471 || iCommandCode == 0x9A06) {
                                                int iRegAddr = (cs108RfidData.dataValues[8] & 0xFF) * 256 + (cs108RfidData.dataValues[9] & 0xFF);
                                                boolean bprocessed = false;
                                                if (DEBUG) appendToLog(String.format("1 iCommandCode = 0x%X with iRegAddr = 0x%X", iCommandCode, iRegAddr));
                                                if (iCommandCode == 0x9A06) {
                                                    if (DEBUG) appendToLog(String.format("2 CommandCode = 0x%X is processed here", iCommandCode));
                                                    bprocessed = true;
                                                } else if (iRegAddr == 8) {
                                                    if (DEBUG) appendToLog("2 iCommandCode");
                                                    try {
                                                        mRfidDevice.mRfidReaderChip.mRx000Setting.macVer = new String(commandValue, StandardCharsets.UTF_8).trim();
                                                        bprocessed = true;
                                                        if (false)
                                                            appendToLog("macVer = " + mRfidDevice.mRfidReaderChip.mRx000Setting.macVer);
                                                    } catch (Exception e) {
                                                        //throw new RuntimeException(e);
                                                    }
                                                } else if (iRegAddr == 0x28 && commandValue.length >= 3) {
                                                    int iValue = 0;
                                                    for (int i = 0, increment = 1; i < commandValue.length; i++, increment *= 10) {
                                                        iValue = commandValue[commandValue.length - 1 - i] * increment;
                                                    }
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.macVerBuild = iValue;
                                                    bprocessed = true;
                                                    if (false)
                                                        appendToLog("macVerBuild = " + mRfidDevice.mRfidReaderChip.mRx000Setting.macVerBuild);
                                                } else if (iRegAddr == 0x3014) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.countryEnum = commandValue;
                                                    bprocessed = true;
                                                    if (true)
                                                        appendToLog("countryEnum = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.countryEnum));
                                                } else if (iRegAddr == 0x3018) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.frequencyChannelIndex = commandValue;
                                                    bprocessed = true;
                                                    if (true)
                                                        appendToLog("frequencyChannelIndex = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.frequencyChannelIndex));
                                                } else if (iRegAddr >= 0x3030 && iRegAddr < 0x3030 + 16 * 16) {
                                                    int iPort = 0, iOffset = 0, iWidth = 0;
                                                    for (iPort = 0; iPort < 16; iPort++) {
                                                        if (DEBUG)
                                                            appendToLog("antennaPortConfig: iPort = " + iPort + String.format(", iRegAddr = 0x%04X", iRegAddr));
                                                        if (iRegAddr < 0x3030 + (iPort + 1) * 16) break;
                                                    }
                                                    iOffset = iRegAddr - 0x3030 - iPort * 16;
                                                    if (DEBUG)
                                                        appendToLog("antennaPortConfig: iOffset = " + iOffset);
                                                    iWidth = commandValue.length;
                                                    if (DEBUG)
                                                        appendToLog("antennaPortConfig: iWidth = " + iWidth);
                                                    if (iOffset == 0 && iWidth == 16) {
                                                        mRfidDevice.mRfidReaderChip.mRx000Setting.antennaPortConfig[iPort] = commandValue;
                                                        bprocessed = true;
                                                        if (false)
                                                            appendToLog("antennaPortConfig[" + iPort + "] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.antennaPortConfig[iPort]));
                                                    } else
                                                        appendToLog("!!! CANNOT handle with iPort = " + iPort + ", iOffset = " + iOffset + ", iWidth = " + iWidth);
                                                } else if (iRegAddr >= 0x3140 && iRegAddr < 0x3140 + 42 * 7) {
                                                    int index = 0, iOffset = 0, iWidth = 0;
                                                    for (index = 0; index < 7; index++) {
                                                        if (DEBUG)
                                                            appendToLog("selectConfiguration: index = " + index + String.format(", iRegAddr = 0x%04X", iRegAddr));
                                                        if (iRegAddr < 0x3140 + (index + 1) * 42) break;
                                                    }
                                                    iOffset = iRegAddr - 0x3140 - index * 42;
                                                    if (DEBUG)
                                                        appendToLog("selectConfiguration: iOffset = " + iOffset);
                                                    iWidth = commandValue.length;
                                                    if (DEBUG)
                                                        appendToLog("selectConfiguration: iWidth = " + iWidth);
                                                    if (iOffset == 0 && iWidth == 42) {
                                                        mRfidDevice.mRfidReaderChip.mRx000Setting.selectConfiguration[index] = commandValue;
                                                        bprocessed = true;
                                                        if (false)
                                                            appendToLog("selectConfiguration[" + index + "] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.selectConfiguration[index]));
                                                    } else
                                                        appendToLog("!!! CANNOT handle with index = " + index + ", iOffset = " + iOffset + ", iWidth = " + iWidth);
                                                } else if (iRegAddr >= 0x3270 && iRegAddr < 0x3270 + 7 * 3) {
                                                    int index = 0, iOffset = 0, iWidth = 0, iPortStartAddr = 0x3270, iPortSize = 7;
                                                    for (index = 0; index < 3; index++) {
                                                        if (DEBUG)
                                                            appendToLog("multibankReadConfig: index = " + index + String.format(", iRegAddr = 0x%04X", iRegAddr));
                                                        if (iRegAddr < iPortStartAddr + (index + 1) * iPortSize) break;
                                                    }
                                                    iOffset = iRegAddr - iPortStartAddr - index * iPortSize;
                                                    if (DEBUG)
                                                        appendToLog("multibankReadConfig: iOffset = " + iOffset);
                                                    iWidth = commandValue.length;
                                                    if (DEBUG)
                                                        appendToLog("multibankReadConfig: iWidth = " + iWidth);
                                                    if (iOffset == 0 && iWidth == iPortSize) {
                                                        mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[index] = commandValue;
                                                        bprocessed = true;
                                                        if (false)
                                                            appendToLog("multibankReadConfig[" + index + "] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[index]));
                                                    } else
                                                        appendToLog("!!! CANNOT handle with index = " + index + ", iOffset = " + iOffset + ", iWidth = " + iWidth);
                                                } else if (iRegAddr == 0x38A6) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.accessPassword = commandValue;
                                                    bprocessed = true;
                                                    if (false) appendToLog("accessPassword = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.accessPassword));
                                                } else if (iRegAddr == 0x3900) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.dupElimRollWindow = commandValue;
                                                    bprocessed = true;
                                                    if (false) appendToLog("dupElimDelay = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.dupElimRollWindow));
                                                } else if (iRegAddr == 0x3906) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.eventPacketUplnkEnable = commandValue;
                                                    bprocessed = true;
                                                    if (false) appendToLog("eventPacketUplnkEnable = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.eventPacketUplnkEnable));
                                                } else if (iRegAddr == 0x3908) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.intraPacketDelay = commandValue;
                                                    bprocessed = true;
                                                    if (false) appendToLog("intraPacketDelay = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.intraPacketDelay));
                                                } else if (iRegAddr == 0x3948) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.currentPort = commandValue;
                                                    bprocessed = true;
                                                    if (true) appendToLog("currentPort = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.currentPort));
                                                } else if (iRegAddr == 0x5000) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.modelCode = commandValue;
                                                    bprocessed = true;
                                                    if (false)
                                                        appendToLog("modelCode = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.modelCode));
                                                } else if (iRegAddr == 0x5020) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.productSerialNumber = commandValue;
                                                    bprocessed = true;
                                                    if (false)
                                                        appendToLog("productSerialNumber = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.productSerialNumber));
                                                } else if (iRegAddr == 0x5040) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.countryEnumOem = commandValue;
                                                    bprocessed = true;
                                                    if (true)
                                                        appendToLog("countryEnumOem = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.countryEnumOem));
                                                } else if (iRegAddr == 0xEF98) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.countryCodeOem = commandValue;
                                                    bprocessed = true;
                                                    if (false)
                                                        appendToLog("countryCodeOem = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.countryCodeOem));
                                                } else if (iRegAddr == 0xEF9C) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.boardSerialNumber = commandValue;
                                                    bprocessed = true;
                                                    if (true)
                                                        appendToLog("boardSerialNumber = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.boardSerialNumber));
                                                } else if (iRegAddr == 0xEFAC) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.specialcountryCodeOem = commandValue;
                                                    bprocessed = true;
                                                    if (false)
                                                        appendToLog("specialcountryCodeOem = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.specialcountryCodeOem));
                                                } else if (iRegAddr == 0xEFB0) {
                                                    mRfidDevice.mRfidReaderChip.mRx000Setting.freqModifyCode = commandValue;
                                                    bprocessed = true;
                                                    if (false)
                                                        appendToLog("freqModifyCode = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.freqModifyCode));
                                                }
                                                if (bprocessed) {
                                                    if (DEBUG_PKDATA) {
                                                        if (iCommandCode == 0x9A06)
                                                            appendToLog("PkData: Rfid.Uplink.DataRead.CommandResponse_WriteRegister with result " + byteArrayToString(commandValue) + " is processed for register address = " + String.format("0x%X", iRegAddr));
                                                        else if (iCommandCode == 0x1471)
                                                            appendToLog("PkData: Rfid.Uplink.DataRead.CommandResponse_ReadRegister with result " + byteArrayToString(commandValue) + " is processed for register address = " + String.format("0x%X", iRegAddr));
                                                        else {
                                                            appendToLog("PkData: Rfid.Uplink.DataRead.CommandResponse_xxx with result " + byteArrayToString(commandValue) + " is processed");
                                                        }
                                                    }
                                                } else
                                                    appendToLog("!!! Rfid.Uplink.DataRead.CommandResponse_ReadRegister CANNOT be processed for register address = " + String.format("0x%X", iRegAddr));
                                            } else appendToLog(String.format("!!! Rfid.Uplink.DataRead.CommandResponse_%X CANNOT be processed", iCommandCode));
                                        }
                                    } else appendToLog("!!! Rfid.Uplink.DataRead.CommandResponse CANNOT be processed");
                                } else if (dataValues[0] == 0x49
                                        && dataValues[1] == (byte)0xDC
                                        && dataValues[4] == 0
                                ) {
                                    if (DEBUG) appendToLog("Ready 0");
                                    int iLength = dataValues[5] * 256 + dataValues[6];
                                    if (DEBUG) appendToLog("Ready 1 with length = " + iLength);
                                    if (dataValues.length == 7 + iLength) {
                                        byte[] dataValues1 = new byte[iLength];
                                        System.arraycopy(dataValues, 7, dataValues1, 0, dataValues1.length);
                                        int iCommand = (dataValues[2] & 0xFF) * 256 + (dataValues[3] & 0xFF);
                                        if (iCommand == 0x3008) {
                                            mRfidDevice.setInventoring(false);
                                            appendToLogView("isRfidToRead_3008: " + byteArrayToString(dataValues));
                                            if (DEBUG) appendToLog("Ready 2");
                                            if (DEBUG_PKDATA)
                                                appendToLog("PkData: found Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete with payload = " + byteArrayToString(dataValues1));
                                            if (cs108RfidData.dataValues[2] == dataValues1[4]
                                                    && cs108RfidData.dataValues[3] == dataValues1[5]) {
                                                if (DEBUG) appendToLog("Ready 3");

                                                int iStatus = dataValues1[6] * 256 + dataValues1[7];
                                                if (cs108RfidData.uplinkResponded) {
                                                    matched = true;
                                                    if (DEBUG_PKDATA)
                                                        appendToLog("PkData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete is processed with status = " + String.format("%04X", iStatus));
                                                }
                                            } else appendToLog("!!! mismatched command code");
                                        }
                                    }
                                }
                                /*
                                int count = 0;
                                if (mBarcodeToWrite.get(0).dataValues[0] == 0x1b) {
                                    commandType = BarcodeCommendTypes.COMMAND_COMMON;
                                    count = 1;
                                    if (false) appendToLog("uplink data is processed with count = " + count + " for mBarcodeToWrite data = " + byteArrayToString(mBarcodeToWrite.get(0).dataValues));
                                } else if (mBarcodeToWrite.get(0).dataValues[0] == 0x7E) {
                                    matched = true;
                                    commandType = BarcodeCommendTypes.COMMAND_QUERY;
                                    int index = 0;
                                    while (dataValues.length - index >= 5 + 1) {
                                        if (dataValues[index+0] == 2 && dataValues[index+1] == 0 && dataValues[index+4] == 0x34) {
                                            int length = dataValues[index+2] * 256 + dataValues[index+3];
                                            if (dataValues.length - index >= length + 4 + 1) {
                                                matched = true;
                                                if (mBarcodeToWrite.get(0).dataValues[5] == 0x37 && length >= 5) {
                                                    matched = true;
                                                    int prefixLength = dataValues[index+6];
                                                    int suffixLength = 0;
                                                    if (dataValues.length - index >= 5 + 2 + prefixLength + 2 + 1) {
                                                        suffixLength = dataValues[index + 6 + prefixLength + 2];
                                                    }
                                                    if (dataValues.length - index >= 5 + 2 + prefixLength + 2 + suffixLength + 1) {
                                                        bytesBarcodePrefix = null;
                                                        bytesBarcodeSuffix = null;
                                                        if (dataValues[index+5] == 1) {
                                                            bytesBarcodePrefix = new byte[prefixLength];
                                                            System.arraycopy(dataValues, index + 7, bytesBarcodePrefix, 0, bytesBarcodePrefix.length);
                                                        }
                                                        if (dataValues[index + 6 + prefixLength + 1] == 1) {
                                                            bytesBarcodeSuffix = new byte[suffixLength];
                                                            System.arraycopy(dataValues, index + 7 + prefixLength + 2, bytesBarcodeSuffix, 0, bytesBarcodeSuffix.length);
                                                        }
                                                    }
                                                    if (true) appendToLog("uplink data is processed as Barcode Prefix = " + byteArrayToString(bytesBarcodePrefix) + ", Suffix = " + byteArrayToString(bytesBarcodeSuffix));
                                                } else if (mBarcodeToWrite.get(0).dataValues[5] == 0x47 && length > 1) {
                                                    matched = true;
                                                    byte[] byteVersion = new byte[length - 1];
                                                    System.arraycopy(dataValues, index + 5, byteVersion, 0, byteVersion.length);
                                                    String versionNumber;
                                                    try {
                                                        versionNumber = new String(byteVersion, "UTF-8");
                                                    } catch (Exception e) {
                                                        versionNumber = null;
                                                    }
                                                    strVersion = versionNumber;
                                                    if (true) appendToLog("uplink data " + byteArrayToString(byteVersion) + " is processsed as version = " + versionNumber);
                                                } else if (mBarcodeToWrite.get(0).dataValues[5] == 0x48 && length >= 5) {
                                                    if (dataValues[index+5] == mBarcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == mBarcodeToWrite.get(0).dataValues[7]) {
                                                        matched = true; //for ESN, S/N or Date
                                                        byte[] byteSN = new byte[length - 3];
                                                        System.arraycopy(dataValues, index + 7, byteSN, 0, byteSN.length);
                                                        String serialNumber;
                                                        try {
                                                            serialNumber = new String(byteSN, "UTF-8");
                                                            int snLength = Integer.parseInt(serialNumber.substring(0, 2));
                                                            if (snLength + 2 == serialNumber.length()) {
                                                                serialNumber = serialNumber.substring(2);
                                                            } else serialNumber = null;
                                                        } catch (Exception e) {
                                                            serialNumber = null;
                                                        }
                                                        appendToLog("uplink data is processed as Barcode serial number [" + serialNumber + "] for index = " + index);
                                                        if (dataValues[index+6] == (byte)0x32) strESN = serialNumber;
                                                        else if (dataValues[index+6] == (byte)0x33) strSerialNumber = serialNumber;
                                                        else if (dataValues[index+6] == (byte)0x34) strDate = serialNumber;
                                                    }
                                                } else if (mBarcodeToWrite.get(0).dataValues[5] == 0x44 && length >= 3) {
                                                    if (dataValues[index+5] == mBarcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == mBarcodeToWrite.get(0).dataValues[7]) {
                                                        matched = true;
                                                        if (mBarcodeToWrite.get(0).dataValues[6] == 0x30 && mBarcodeToWrite.get(0).dataValues[7] == 0x30  && mBarcodeToWrite.get(0).dataValues[8] == 0x30) {
                                                            bBarcodeTriggerMode = dataValues[7];
                                                            if (dataValues[index + 7] == 0x30) {
                                                                appendToLog("uplink data is processed as Barcode Reading mode TRIGGER");
                                                            } else
                                                                appendToLog("uplink data is processed as Barcode Reading mode " + String.valueOf(dataValues[7]));
                                                        } else appendToLog("uplink data is processed as incorrect response !!!");
                                                    } else if (true) {
                                                        matched = true;
                                                        appendToLog("uplink data is processed as incorrect response !!!");
                                                    }
                                                }
                                                index += (length + 5);
                                            } else break;
                                        } else index++;
                                    }
                                    if (matched) { if (DEBUG) appendToLog("Matched Query response"); }
                                    else appendToLog("uplink data is processed as Mis-matched Query response");
                                } else {
                                    String strData = null;
                                    try {
                                        strData = new String(mBarcodeToWrite.get(0).dataValues, "UTF-8");
                                    } catch (Exception ex) {
                                        strData = "";
                                    }
                                    String findStr = "nls";
                                    int lastIndex = 0;
                                    while (lastIndex != -1) {
                                        lastIndex = strData.indexOf(findStr, lastIndex);
                                        if (lastIndex != -1) {
                                            count++;
                                            lastIndex += findStr.length();
                                        }
                                    }
                                }
                                if (count != 0) {
                                    if (false) appendToLog("dataValues.length = " + dataValues.length + ", okCount = " + iOkCount + ", count = " + count + " for mBarcodeToWrite data = " + byteArrayToString(mBarcodeToWrite.get(0).dataValues));
                                    matched = false; boolean foundOk = false;
                                    for (int k = 0; k < dataValues.length; k++) {
                                        boolean match06 = false;
                                        if (dataValues[k] == 0x06 || dataValues[k] == 0x15) { match06 = true; if (++iOkCount == count) matched = true; }
                                        if (match06 == false) break;
                                        foundOk = true; found = true;
                                    }
                                    if (false) appendToLog("00 matcched = " + matched);
                                    if (matched) appendToLog("uplink data is processed with matched = " + matched + ", OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(mBarcodeToWrite.get(0).dataValues));
                                    else if (foundOk) appendToLog("uplink data is processed with matched = " + matched + ", but OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(mBarcodeToWrite.get(0).dataValues));
                                    else {
                                        mBarcodeDevice.mBarcodeToRead.add(cs108BarcodeData);
                                        appendToLog("uplink data Barcode.DataRead." + byteArrayToString(cs108BarcodeData.dataValues) + " is added to mBarcodeToRead");
                                    }
                                }*/
                                if (matched) {
                                    found = true;
                                    mRfidToWrite.remove(0); sendRfidToWriteSent = 0; mRfidToWriteRemoved = true;
                                    if (DEBUG_PKDATA) appendToLog("PkData: new mRfidToWrite size = " + mRfidToWrite.size());
                                }
                                if (matched || updatedUplinkResponse) break;
                            }
                        }
                        cs108RfidReadData.rfidPayloadEvent = RfidPayloadEvents.RFID_DATA_READ;
                        cs108RfidReadData.dataValues = dataValues;
                        cs108RfidReadData.invalidSequence = cs108ReadData.invalidSequence;
                        cs108RfidReadData.milliseconds = cs108ReadData.milliseconds;
                        mRfidToRead.add(cs108RfidReadData);
                        if (DEBUG_PKDATA) appendToLog("PkData: uplink data Rfid.Uplink.DataRead is uploaded to mRfidToRead");
                        found = true;
                        break;
                    default:
                        invalidUpdata++;
                        appendToLog("!!! found INVALID Rfid.Uplink with payload = " + byteArrayToString(cs108ReadData.dataValues));
                        break;
                }
            }
            return found;
        }
    }

    enum BarcodePayloadEvents {
        BARCODE_NULL,
        BARCODE_POWER_ON, BARCODE_POWER_OFF, BARCODE_SCAN_START, BARCODE_COMMAND, BARCODE_VIBRATE_ON, BARCODE_VIBRATE_OFF,
        BARCODE_DATA_READ, BARCODE_GOOD_READ,
    }
    enum BarcodeCommendTypes {
        COMMAND_COMMON, COMMAND_SETTING, COMMAND_QUERY
    }
    class Cs108BarcodeData {
        boolean waitUplinkResponse = false;
        boolean downlinkResponsed = false;
        BarcodePayloadEvents barcodePayloadEvent;
        byte[] dataValues;
    }
    class BarcodeDevice {
        private boolean onStatus = false; boolean getOnStatus() { return onStatus; }
        private boolean vibrateStatus = false; boolean getVibrateStatus() { return vibrateStatus; }
        private String strVersion, strESN, strSerialNumber, strDate;
        String getVersion() { return strVersion; }
        String getESN() { return strESN; }
        String getSerialNumber() { return strSerialNumber; }
        String getDate() { return strDate; }
        byte[] bytesBarcodePrefix = null;
        byte[] bytesBarcodeSuffix = null;
        byte[] getPrefix() { return bytesBarcodePrefix; }
        byte[] getSuffix() { return bytesBarcodeSuffix; }
        boolean checkPreSuffix(byte[] prefix1, byte[] suffix1) {
            boolean result = false;
            if (prefix1 != null && bytesBarcodePrefix != null && suffix1 != null && bytesBarcodeSuffix != null) {
                result = Arrays.equals(prefix1, bytesBarcodePrefix);
                if (result) result = Arrays.equals(suffix1, bytesBarcodeSuffix);
            }
            return result;
        }

        ArrayList<Cs108BarcodeData> mBarcodeToWrite = new ArrayList<>();
        ArrayList<Cs108BarcodeData> mBarcodeToRead = new ArrayList<>();

        private boolean arrayTypeSet(byte[] dataBuf, int pos, BarcodePayloadEvents event) {
            boolean validEvent = false;
            switch (event) {
                case BARCODE_POWER_ON:
                    validEvent = true;
                    break;
                case BARCODE_POWER_OFF:
                    dataBuf[pos] = 1;
                    validEvent = true;
                    break;
                case BARCODE_SCAN_START:
                    dataBuf[pos] = 2;
                    validEvent = true;
                    break;
                case BARCODE_COMMAND:
                    dataBuf[pos] = 3;
                    validEvent = true;
                    break;
                case BARCODE_VIBRATE_ON:
                    dataBuf[pos] = 4;
                    validEvent = true;
                    break;
                case BARCODE_VIBRATE_OFF:
                    dataBuf[pos] = 5;
                    validEvent = true;
                    break;
            }
            return validEvent;
        }

        private boolean writeBarcode(Cs108BarcodeData data) {
            int datalength = 0;
            if (data.dataValues != null)    datalength = data.dataValues.length;
            byte[] dataOutRef = new byte[] { (byte) 0xA7, (byte) 0xB3, 2, (byte) 0x6A, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0x90, 0};

            byte[] dataOut = new byte[10 + datalength];
            if (datalength != 0)    {
                System.arraycopy(data.dataValues, 0, dataOut, 10, datalength);
                dataOutRef[2] += datalength;
            }
            System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.length);
            if (arrayTypeSet(dataOut, 9, data.barcodePayloadEvent)) {
                if (false) {
                    appendToLog("BarStreamOut: " + byteArrayToString(dataOut));
                    appendToLogView("BOut: " + byteArrayToString(dataOut));
                }
                if (DEBUG_PKDATA) appendToLog(String.format("PkData: write Barcode.%s.%s with mBarcodeDevice.sendDataToWriteSent = %d", data.barcodePayloadEvent.toString(), byteArrayToString(data.dataValues), sendDataToWriteSent));
                if (sendDataToWriteSent != 0) appendToLog("!!! mBarcodeDevice.sendDataToWriteSent = " + sendDataToWriteSent);
                return writeData(dataOut, (data.waitUplinkResponse ? 500 : 0));
            }
            return false;
        }

        private boolean isMatchBarcodeToWrite(Cs108ReadData cs108ReadData) {
            boolean match = false, DEBUG = false;
            if (mBarcodeToWrite.size() != 0 && cs108ReadData.dataValues[0] == (byte)0x90) {
                if (DEBUG) appendToLog("cs108ReadData = " + byteArrayToString(cs108ReadData.dataValues));
                if (DEBUG) appendToLog("tempDisconnect: icsModel = " + mBluetoothConnector.getCsModel() + ", mBarcodeToWrite.size = " + mBarcodeToWrite.size());
                if (mBarcodeToWrite.size() != 0) if (DEBUG) appendToLog("mBarcodeToWrite(0) = " + mBarcodeToWrite.get(0).barcodePayloadEvent.toString() + "," + byteArrayToString(mBarcodeToWrite.get(0).dataValues));
                byte[] dataInCompare = new byte[]{(byte) 0x90, 0};
                if (arrayTypeSet(dataInCompare, 1, mBarcodeToWrite.get(0).barcodePayloadEvent) && (cs108ReadData.dataValues.length == dataInCompare.length + 1)) {
                    if (match = compareArray(cs108ReadData.dataValues, dataInCompare, dataInCompare.length)) {
                        if (DEBUG_PKDATA) appendToLog("PkData: matched Barcode.Reply with payload = " + byteArrayToString(cs108ReadData.dataValues) + " for writeData Barcode." + mBarcodeToWrite.get(0).barcodePayloadEvent.toString());
                        if (cs108ReadData.dataValues[2] != 0) {
                            if (DEBUG) appendToLog("Barcode.reply data is found with error");
                        } else if (mBluetoothConnector.getCsModel() == 108) {
                            if (mBarcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_ON) {
                                barcodePowerOnTimeOut = 1000;
                                if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_ON");
                                onStatus = true;
                                if (DEBUG_PKDATA | cs108ReadData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.PowerOn with result = " + cs108ReadData.dataValues[2] + " and onStatus = " + onStatus);
                            } else if (mBarcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_POWER_OFF) {
                                if (DEBUG) appendToLog("tempDisconnect: BARCODE_POWER_OFF");
                                onStatus = false;
                                if (DEBUG_PKDATA | cs108ReadData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.PowerOff with result = " + cs108ReadData.dataValues[2] + " and onStatus = " + onStatus);
                            } else if (mBarcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_ON) {
                                vibrateStatus = true;
                                if (DEBUG_PKDATA | cs108ReadData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.VibrateOn with result = " + cs108ReadData.dataValues[2] + " and vibrateStatus = " + vibrateStatus);
                            } else if (mBarcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_VIBRATE_OFF) {
                                vibrateStatus = false;
                                if (DEBUG_PKDATA | cs108ReadData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.VibrateOff with result = " + cs108ReadData.dataValues[2] + " and vibrateStatus = " + vibrateStatus);
                            } else if (mBarcodeToWrite.get(0).barcodePayloadEvent == BarcodePayloadEvents.BARCODE_COMMAND) {
                                barcodePowerOnTimeOut = 500;
                                if (DEBUG_PKDATA | cs108ReadData.dataValues[2] != 0) appendToLog("PkData: matched Barcode.Reply.Command with result = " + cs108ReadData.dataValues[2] + " and barcodePowerOnTimeOut = " + barcodePowerOnTimeOut);
                            } else appendToLog("Not matched Barcode.Reply");
                            Cs108BarcodeData cs108BarcodeData = mBarcodeToWrite.get(0);
                            if (cs108BarcodeData.waitUplinkResponse) {
                                cs108BarcodeData.downlinkResponsed = true; iOkCount = 0;
                                mBarcodeToWrite.set(0, cs108BarcodeData);
                                if (DEBUG_PKDATA) appendToLog("PkData: mBarcodeToWrite.downlinkResponsed is set and waiting uplink data");
                                return true;
                            }
                        } else {
                            barcodeFailure = true;
                            appendToLog("Not matched Barcode.Reply");
                        }
                        mBarcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                        if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + mBarcodeToWrite.size());
                    }
                }
            }
            return match;
        }

        private int sendDataToWriteSent = 0; boolean mDataToWriteRemoved = false;
        boolean barcodeFailure = false;
        private boolean sendBarcodeToWrite() {
            boolean DEBUG = false;
            if (barcodePowerOnTimeOut != 0) {
                if (DEBUG) appendToLog("barcodePowerOnTimeOut = " + barcodePowerOnTimeOut + ", mBarcodeToWrite.size() = " + mBarcodeToWrite.size());
                return false;
            }
            if (mBarcodeToWrite.size() != 0) {
                if (DEBUG) appendToLog("mBarcodeToWrite.size = " + mBarcodeToWrite.size());
                if (isBleConnected() == false) {
                    mBarcodeToWrite.clear();
                } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                    BarcodePayloadEvents barcodePayloadEvents = mBarcodeToWrite.get(0).barcodePayloadEvent;
                    if (DEBUG)  appendToLog("barcodePayloadEvents = " + barcodePayloadEvents.toString());
                    boolean isBarcodeData = false;
                    if (barcodePayloadEvents == BarcodePayloadEvents.BARCODE_SCAN_START || barcodePayloadEvents == BarcodePayloadEvents.BARCODE_COMMAND) isBarcodeData = true;
                    if (barcodeFailure && isBarcodeData) {
                        mBarcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                    } else if (sendDataToWriteSent >= 2 && isBarcodeData) {
                        mBarcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                        if (DEBUG) appendToLog("Removed after sending count-out.");
                        if (false) Toast.makeText(context, "Problem in sending data to BarCode Module. Barcode is disabled", Toast.LENGTH_LONG).show();
                        else if (mBluetoothConnector.getCsModel() == 108) Toast.makeText(context, "No barcode present on Reader", Toast.LENGTH_LONG).show();
                        barcodeFailure = true; // disconnect(false);
                    } else {
                        if (DEBUG) appendToLog("size = " + mBarcodeToWrite.size() + ", PayloadEvents = " + mBarcodeToWrite.get(0).barcodePayloadEvent.toString());
                        boolean retValue = writeBarcode(mBarcodeToWrite.get(0));
                        if (retValue) {
                            sendDataToWriteSent++;
                            mDataToWriteRemoved = false;
                        } else {
                            if (DEBUG) appendToLogView("failure to send " + mBarcodeToWrite.get(0).barcodePayloadEvent.toString());
                            mBarcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        byte bBarcodeTriggerMode = (byte)0xff;
        int iOkCount = 0;
        private boolean isBarcodeToRead(Cs108ReadData cs108ReadData) {
            boolean found = false, DEBUG = false;

            if (cs108ReadData.dataValues[0] == (byte) 0x91) {
                if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink with payload = " + byteArrayToString(cs108ReadData.dataValues));
                Cs108BarcodeData cs108BarcodeData = new Cs108BarcodeData();
                switch (cs108ReadData.dataValues[1]) {
                    case 0:
                        cs108BarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_DATA_READ;
                        byte[] dataValues = new byte[cs108ReadData.dataValues.length - 2];
                        System.arraycopy(cs108ReadData.dataValues, 2, dataValues, 0, dataValues.length);
                        if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink.DataRead with payload = " + byteArrayToString(dataValues));
                        BarcodeCommendTypes commandType = null;
                        if (mBarcodeToWrite.size() > 0) {
                            if (mBarcodeToWrite.get(0).downlinkResponsed) {
                                int count = 0; boolean matched = true;
                                if (mBarcodeToWrite.get(0).dataValues[0] == 0x1b) {
                                    commandType = BarcodeCommendTypes.COMMAND_COMMON;
                                    count = 1;
                                    if (DEBUG) appendToLog("0x1b, Common response with  count = " + count);
                                } else if (mBarcodeToWrite.get(0).dataValues[0] == 0x7E) {
                                    if (DEBUG) appendToLog("0x7E, Barcode response with 0x7E mBarcodeToWrite.get(0).dataValues[0] and response data = " + byteArrayToString(dataValues));
                                    matched = true;
                                    commandType = BarcodeCommendTypes.COMMAND_QUERY;
                                    int index = 0;
                                    while (dataValues.length - index >= 5 + 1) {
                                        if (dataValues[index+0] == 2 && dataValues[index+1] == 0 && dataValues[index+4] == 0x34) {
                                            int length = dataValues[index+2] * 256 + dataValues[index+3];
                                            if (dataValues.length - index >= length + 4 + 1) {
                                                matched = true;
                                                byte[] bytes = new byte[length-1];
                                                System.arraycopy(dataValues, index + 5, bytes, 0, bytes.length);
                                                byte[] requestBytes = new byte[mBarcodeToWrite.get(0).dataValues.length - 6];
                                                System.arraycopy(mBarcodeDevice.mBarcodeToWrite.get(0).dataValues, 5, requestBytes, 0, requestBytes.length);
                                                if (DEBUG_PKDATA) appendToLog("PkData: found Barcode.Uplink.DataRead.QueryResponse with payload data1 = " + byteArrayToString(bytes) + " for QueryInput data1 = " + byteArrayToString(requestBytes));
                                                if (mBarcodeToWrite.get(0).dataValues[5] == 0x37 && length >= 5) {
                                                    int prefixLength = dataValues[index+6];
                                                    int suffixLength = 0;
                                                    if (dataValues.length - index >= 5 + 2 + prefixLength + 2 + 1) {
                                                        suffixLength = dataValues[index + 6 + prefixLength + 2];
                                                    }
                                                    if (dataValues.length - index >= 5 + 2 + prefixLength + 2 + suffixLength + 1) {
                                                        bytesBarcodePrefix = null;
                                                        bytesBarcodeSuffix = null;
                                                        if (dataValues[index+5] == 1) {
                                                            bytesBarcodePrefix = new byte[prefixLength];
                                                            System.arraycopy(dataValues, index + 7, bytesBarcodePrefix, 0, bytesBarcodePrefix.length);
                                                        }
                                                        if (dataValues[index + 6 + prefixLength + 1] == 1) {
                                                            bytesBarcodeSuffix = new byte[suffixLength];
                                                            System.arraycopy(dataValues, index + 7 + prefixLength + 2, bytesBarcodeSuffix, 0, bytesBarcodeSuffix.length);
                                                        }
                                                        if (DEBUG) appendToLog("BarStream: BarcodePrefix = " + byteArrayToString(bytesBarcodePrefix) + ", BarcodeSuffix = " + byteArrayToString(bytesBarcodeSuffix));
                                                    }
                                                    if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead.QueryResponse.SelfPrefix_SelfSuffix is processed as Barcode Prefix = " + byteArrayToString(bytesBarcodePrefix) + ", Suffix = " + byteArrayToString(bytesBarcodeSuffix));
                                                } else if (mBarcodeToWrite.get(0).dataValues[5] == 0x47 && length > 1) {
                                                    if (DEBUG) appendToLog("versionNumber is detected with length = " + length);
                                                    byte[] byteVersion = new byte[length - 1];
                                                    System.arraycopy(dataValues, index + 5, byteVersion, 0, byteVersion.length);
                                                    String versionNumber;
                                                    try {
                                                        versionNumber = new String(byteVersion, "UTF-8");
                                                    } catch (Exception e) {
                                                        versionNumber = null;
                                                    }
                                                    strVersion = versionNumber;
                                                    if (DEBUG_PKDATA) appendToLog("PkData: uplink data " + byteArrayToString(byteVersion) + " is processsed as version = " + versionNumber);
                                                } else if (mBarcodeToWrite.get(0).dataValues[5] == 0x48 && length >= 5) {
                                                    if (dataValues[index+5] == mBarcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == mBarcodeToWrite.get(0).dataValues[7]) {
                                                        byte[] byteSN = new byte[length - 3];
                                                        System.arraycopy(dataValues, index + 7, byteSN, 0, byteSN.length);
                                                        String serialNumber;
                                                        try {
                                                            serialNumber = new String(byteSN, "UTF-8");
                                                            int snLength = Integer.parseInt(serialNumber.substring(0, 2));
                                                            if (DEBUG)
                                                                appendToLog("BarStream: serialNumber = " + serialNumber + ", snLength = " + snLength + ", serialNumber.length = " + serialNumber.length());
                                                            if (snLength + 2 == serialNumber.length()) {
                                                                serialNumber = serialNumber.substring(2);
                                                            } else serialNumber = null;
                                                        } catch (Exception e) {
                                                            serialNumber = null;
                                                        }
                                                        if (false) appendToLog("debug index = " + index + ", " + byteArrayToString(dataValues));
                                                        String strResponseType = "";
                                                        if (dataValues[index+6] == (byte)0x32) {
                                                            strESN = serialNumber;
                                                            strResponseType = "EquipmentSerialNumber";
                                                        } else if (dataValues[index+6] == (byte)0x33) {
                                                            strSerialNumber = serialNumber;
                                                            strResponseType = "SerialNumber";
                                                        } else if (dataValues[index+6] == (byte)0x34) {
                                                            strDate = serialNumber;
                                                            strResponseType = "DataCode";
                                                        }
                                                        if (false) appendToLog("strResponseType = " + strResponseType);
                                                        if (DEBUG_PKDATA) appendToLog(String.format("PkData: Barcode.Uplink.DataRead.QueryResponse.%s is processed as %s[%s]", strResponseType, byteArrayToString(byteSN).substring(4), serialNumber));
                                                    } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                                } else if (mBarcodeToWrite.get(0).dataValues[5] == 0x44 && length >= 3) {
                                                    if (dataValues[index+5] == mBarcodeToWrite.get(0).dataValues[6] && dataValues[index+6] == mBarcodeToWrite.get(0).dataValues[7]) {
                                                        if (mBarcodeToWrite.get(0).dataValues[6] == 0x30 && mBarcodeToWrite.get(0).dataValues[7] == 0x30  && mBarcodeToWrite.get(0).dataValues[8] == 0x30) {
                                                            bBarcodeTriggerMode = dataValues[7];
                                                            String strModeType = "";
                                                            if (dataValues[index+7] == 0x30) strModeType = "trigger";
                                                            else if (dataValues[index+7] == 0x31) strModeType = "auto_Scan";
                                                            else if (dataValues[index+7] == 0x32) strModeType = "continue_Scan";
                                                            else if (dataValues[index+7] == 0x33) strModeType = "batch_Scan";
                                                            if (DEBUG_PKDATA) appendToLog(String.format("PkData: Barcode.Uplink.DataRead.QueryResponse.ReadingMode is processed as last 0x%X[%s]", dataValues[index+7], strModeType));
                                                        } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                                    } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                                } else appendToLog("Barcode.Uplink.DataRead.QueryResponse has mis-matched values");
                                                index += (length + 5);
                                            } else break;
                                        } else index++;
                                    }
                                    if (matched) { if (DEBUG) appendToLog("Matched Query response"); }
                                    else { if (DEBUG) appendToLog("Mis-matched Query response"); }
                                } else {
                                    if (DEBUG) appendToLog("BarStream: Barcode response with mBarcodeToWrite.get(0).dataValues[0] =  Others");
                                    String strData = null;
                                    try {
                                        strData = new String(mBarcodeToWrite.get(0).dataValues, "UTF-8");
                                    } catch (Exception ex) {
                                        strData = "";
                                    }
                                    String findStr = "nls";
                                    int lastIndex = 0;
                                    while (lastIndex != -1) {
                                        lastIndex = strData.indexOf(findStr, lastIndex);
                                        if (lastIndex != -1) {
                                            count++;
                                            lastIndex += findStr.length();
                                        }
                                    }
                                    if (DEBUG) appendToLog("Setting strData = " + strData + ", count = " + count);
                                }
                                if (count != 0) {
                                    if (false) appendToLog("dataValues.length = " + dataValues.length + ", okCount = " + iOkCount + ", count = " + count + " for mBarcodeToWrite data = " + byteArrayToString(mBarcodeToWrite.get(0).dataValues));
                                    matched = false; boolean foundOk = false;
                                    for (int k = 0; k < dataValues.length; k++) {
                                        boolean match06 = false;
                                        if (dataValues[k] == 0x06 || dataValues[k] == 0x15) { match06 = true; if (++iOkCount == count) matched = true; }
                                        if (match06 == false) break;
                                        foundOk = true; found = true;
                                    }
                                    if (false) appendToLog("00 matcched = " + matched);
                                    if (matched) { if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead." + byteArrayToString(dataValues) + " is processed with matched = " + matched + ", OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(mBarcodeToWrite.get(0).dataValues)); }
                                    else if (foundOk) { if (DEBUG_PKDATA) appendToLog("PkData: Barcode.Uplink.DataRead." + byteArrayToString(dataValues) + " is processed with matched = " + matched + ", but OkCount = " + iOkCount + ", expected count = " + count + " for " + byteArrayToString(mBarcodeToWrite.get(0).dataValues)); }
                                    else {
                                        mBarcodeDevice.mBarcodeToRead.add(cs108BarcodeData);
                                        if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.DataRead." + byteArrayToString(cs108BarcodeData.dataValues) + " is added to mBarcodeToRead");
                                    }
                                }
                                if (matched) {
                                    found = true;
                                    mBarcodeToWrite.remove(0); sendDataToWriteSent = 0; mDataToWriteRemoved = true;
                                    if (DEBUG_PKDATA) appendToLog("PkData: new mBarcodeToWrite size = " + mBarcodeToWrite.size());
                                }
                                break;
                            }
                        }
                        for (int i=0; false && commandType == null && i < dataValues.length; i++) {
                            if (dataValues[i] == 0x28 || dataValues[i] == 0x29    //  ( )
                            || dataValues[i] == 0x5B || dataValues[i] == 0x5D || dataValues[i] == 0x5C
                            || dataValues[i] == 0x7B || dataValues[i] == 0x7D
                                    ) dataValues[i] = 0x20;
                        }
                        cs108BarcodeData.dataValues = dataValues;
                        mBarcodeDevice.mBarcodeToRead.add(cs108BarcodeData);
                        if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.DataRead." + byteArrayToString(dataValues) + " is added to mBarcodeToRead");
                        found = true;
                        break;
                    case 1:
                        cs108BarcodeData.barcodePayloadEvent = BarcodePayloadEvents.BARCODE_GOOD_READ;
                        cs108BarcodeData.dataValues = null;
                        mBarcodeDevice.mBarcodeToRead.add(cs108BarcodeData);
                        if (DEBUG_PKDATA) appendToLog("PkData: uplink data Barcode.GoodRead is added to mBarcodeToRead");
                        found = true;
                        break;
                }
            }
            return found;
        }
    }

    enum NotificationPayloadEvents {
        NOTIFICATION_GET_BATTERY_VOLTAGE, NOTIFICATION_GET_TRIGGER_STATUS,
        NOTIFICATION_AUTO_BATTERY_VOLTAGE, NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE,
        NOTIFICATION_AUTO_RFIDINV_ABORT, NOTIFICATION_GET_AUTO_RFIDINV_ABORT,
        NOTIFICATION_AUTO_BARINV_STARTSTOP, NOTIFICATION_GET_AUTO_BARINV_STARTSTOP,
        NOTIFICATION_AUTO_TRIGGER_REPORT, NOTIFICATION_STOP_TRIGGER_REPORT,

        NOTIFICATION_BATTERY_FAILED, NOTIFICATION_BATTERY_ERROR,
        NOTIFICATION_TRIGGER_PUSHED, NOTIFICATION_TRIGGER_RELEASED
    }
    class Cs108NotificatiionData {
        NotificationPayloadEvents notificationPayloadEvent;
        byte[] dataValues;
    }

//    public interface NotificationListener { void onChange(); }
    class NotificationDevice {
        Cs710Library4A.NotificationListener listener;
        void setNotificationListener0(Cs710Library4A.NotificationListener listener) { this.listener = listener; }

        //NotificationListener getListener() { return listener; }
        boolean mTriggerStatus;
        boolean getTriggerStatus() { return mTriggerStatus; }
        void setTriggerStatus(boolean mTriggerStatus) {
            if (this.mTriggerStatus != mTriggerStatus) {
                this.mTriggerStatus = mTriggerStatus;
                if (listener != null) listener.onChange();
            }
        }

        boolean mAutoRfidAbortStatus = true, mAutoRfidAbortStatusUpdate = false;
        boolean getAutoRfidAbortStatus() {
            if (mAutoRfidAbortStatusUpdate == false) {
                Cs108NotificatiionData cs108NotificatiionData = new Cs108NotificatiionData();
                cs108NotificatiionData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT;
                mNotificationDevice.mNotificationToWrite.add(cs108NotificatiionData);
                if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_GET_AUTO_RFIDINV_ABORT to mNotificationToWrite with length = " + mNotificationDevice.mNotificationToWrite.size());
            }
            return mAutoRfidAbortStatus;
        }
        void setAutoRfidAbortStatus(boolean mAutoRfidAbortStatus) { this.mAutoRfidAbortStatus = mAutoRfidAbortStatus; mAutoRfidAbortStatusUpdate = true; }

        boolean mAutoBarStartStopStatus = false, mAutoBarStartStopStatusUpdated = false;
        boolean getAutoBarStartStopStatus() {
            if (mAutoBarStartStopStatusUpdated == false) {
                Cs108NotificatiionData cs108NotificatiionData = new Cs108NotificatiionData();
                cs108NotificatiionData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP;
                mNotificationDevice.mNotificationToWrite.add(cs108NotificatiionData);
                if (DEBUG_PKDATA) appendToLog("PkData: add NOTIFICATION_GET_AUTO_BARINV_STARTSTOP to mNotificationToWrite with length = " + mNotificationDevice.mNotificationToWrite.size());
            }
            return mAutoBarStartStopStatus;
        }
        void setAutoBarStartStopStatus(boolean mAutoBarStartStopStatus) { this.mAutoBarStartStopStatus = mAutoBarStartStopStatus; mAutoBarStartStopStatusUpdated = true; }

        ArrayList<Cs108NotificatiionData> mNotificationToWrite = new ArrayList<>();
        ArrayList<Cs108NotificatiionData> mNotificationToRead = new ArrayList<>();

        private boolean arrayTypeSet(byte[] dataBuf, int pos, NotificationPayloadEvents event) {
            boolean validEvent = false;
            switch (event) {
                case NOTIFICATION_GET_BATTERY_VOLTAGE:
                    validEvent = true;
                    break;
                case NOTIFICATION_GET_TRIGGER_STATUS:
                    dataBuf[pos] = 1;
                    validEvent = true;
                    break;
                case NOTIFICATION_AUTO_BATTERY_VOLTAGE:
                    if (false && checkHostProcessorVersion(mSiliconLabIcDevice.getSiliconLabIcVersion(), 0, 0, 2) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 2;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_STOPAUTO_BATTERY_VOLTAGE:
                    if (false && checkHostProcessorVersion(mSiliconLabIcDevice.getSiliconLabIcVersion(), 0, 0, 1) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 3;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_AUTO_RFIDINV_ABORT:
                    if (false && checkHostProcessorVersion(mBluetoothConnector.mBluetoothIcDevice.getBluetoothIcVersion(), 0, 0, 1) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 4;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_GET_AUTO_RFIDINV_ABORT:
                    if (false && checkHostProcessorVersion(mBluetoothConnector.mBluetoothIcDevice.getBluetoothIcVersion(), 1, 0, 13) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 5;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_AUTO_BARINV_STARTSTOP:
                    if (false && checkHostProcessorVersion(mBluetoothConnector.mBluetoothIcDevice.getBluetoothIcVersion(), 1, 0, 14) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 6;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_GET_AUTO_BARINV_STARTSTOP:
                    if (false && checkHostProcessorVersion(mBluetoothConnector.mBluetoothIcDevice.getBluetoothIcVersion(), 1, 0, 14) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 7;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_AUTO_TRIGGER_REPORT:
                    if (false && checkHostProcessorVersion(mSiliconLabIcDevice.getSiliconLabIcVersion(), 1, 0, 16) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 8;
                        validEvent = true;
                    }
                    break;
                case NOTIFICATION_STOP_TRIGGER_REPORT:
                    if (false && checkHostProcessorVersion(mSiliconLabIcDevice.getSiliconLabIcVersion(), 1, 0, 16) == false) {
                        validEvent = false;
                    } else {
                        dataBuf[pos] = 9;
                        validEvent = true;
                    }
                    break;
            }
            return validEvent;
        }

        private boolean writeNotification(Cs108NotificatiionData data) {
            int datalength = 0;
            if (data.dataValues != null)    datalength = data.dataValues.length;
            byte[] dataOutRef = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xD9, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xA0, 0};

            byte[] dataOut = new byte[10 + datalength];
            if (datalength != 0) {
                System.arraycopy(data.dataValues, 0, dataOut, 10, datalength);
                dataOutRef[2] += datalength;
            }
            System.arraycopy(dataOutRef, 0, dataOut, 0, dataOutRef.length);
            if (arrayTypeSet(dataOut, 9, data.notificationPayloadEvent)) {
                if (DEBUG_PKDATA) appendToLog(String.format("PkData: write mNotificationDevice.%s.%s with  mNotificationDevice.sendDataToWriteSent = %d", data.notificationPayloadEvent.toString(), byteArrayToString(data.dataValues), sendDataToWriteSent));
                if (sendDataToWriteSent != 0) appendToLog("!!! mNotificationDevice.sendDataToWriteSent = " + sendDataToWriteSent);
                boolean bValue = writeData(dataOut, 0);
                return bValue;
            }
            return false;
        }

        private boolean isMatchNotificationToWrite(Cs108ReadData cs108ReadData) {
            boolean match = false;
            if (mNotificationToWrite.size() != 0 && cs108ReadData.dataValues[0] == (byte)0xA0) {
                byte[] dataInCompare = new byte[]{(byte) 0xA0, 0};
                if (arrayTypeSet(dataInCompare, 1, mNotificationToWrite.get(0).notificationPayloadEvent) && (cs108ReadData.dataValues.length >= dataInCompare.length + 1)) {
                    if (match = compareArray(cs108ReadData.dataValues, dataInCompare, dataInCompare.length)) {
                        if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply with payload = " + byteArrayToString(cs108ReadData.dataValues) + " for writeData Notification." + mNotificationToWrite.get(0).notificationPayloadEvent.toString());
                        if (mNotificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_BATTERY_VOLTAGE) {
                            if (cs108ReadData.dataValues.length >= dataInCompare.length + 2) {
                                mCs108ConnectorData.mVoltageValue = (cs108ReadData.dataValues[2] & 0xFF) * 256 + (cs108ReadData.dataValues[3] & 0xFF);
                                mCs108ConnectorData.mVoltageCount++;
                            }
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply.GetBatteryVoltage with result = " + String.format("%X", mCs108ConnectorData.mVoltageValue));
                        } else if (mNotificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_TRIGGER_STATUS) {
                            if (cs108ReadData.dataValues[2] != 0) {
                                setTriggerStatus(true); //mTriggerStatus = true;
                                mCs108ConnectorData.triggerButtonStatus = true;
                            } else {
                                setTriggerStatus(false); //mTriggerStatus = false;
                                mCs108ConnectorData.triggerButtonStatus = false;
                            }
                            mCs108ConnectorData.iTriggerCount++;
                            if (DEBUG_PKDATA) appendToLog("PkData: BARTRIGGER: isMatchNotificationToWrite finds trigger = " + getTriggerStatus());
                        } else if (mNotificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_AUTO_RFIDINV_ABORT) {
                            if (cs108ReadData.dataValues[2] != 0) setAutoRfidAbortStatus(true);
                            else setAutoRfidAbortStatus(false);
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply.GetAutoRfidinvAbort with result = " + cs108ReadData.dataValues[2] + " and autoRfidAbortStatus = " + getAutoRfidAbortStatus());
                        } else if (mNotificationToWrite.get(0).notificationPayloadEvent == NotificationPayloadEvents.NOTIFICATION_GET_AUTO_BARINV_STARTSTOP) {
                            if (cs108ReadData.dataValues[2] != 0) setAutoBarStartStopStatus(true);
                            else setAutoBarStartStopStatus(false);
                            if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply.GetAutoBarinvStartstop with result = " + cs108ReadData.dataValues[2] + " and " + getAutoBarStartStopStatus());
                        } else if (DEBUG_PKDATA) appendToLog("PkData: matched Notification.Reply." + mNotificationToWrite.get(0).notificationPayloadEvent.toString() + " with result = " + cs108ReadData.dataValues[2]);
                        mNotificationToWrite.remove(0); sendDataToWriteSent = 0;
                        if (DEBUG_PKDATA) appendToLog("PkData: new mNotificationToWrite size = " + mNotificationToWrite.size());
                    }
                }
            }
            return match;
        }

        private int sendDataToWriteSent = 0;
        private boolean sendNotificationToWrite() {
            boolean DEBUG = false;
            if (mNotificationToWrite.size() != 0) {
                if (DEBUG) appendToLog("size = " + mNotificationToWrite.size());
                if (isBleConnected() == false) {
                    if (DEBUG) appendToLog("link is disconnected");
                    mNotificationToWrite.clear();
                } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                    if (DEBUG) appendToLog("timeout with sendDataToWriteSent = " + sendDataToWriteSent);
                    if (sendDataToWriteSent >= 5) {
                        int oldSize = mNotificationToWrite.size();
                        Cs108NotificatiionData cs108NotificatiionData = mNotificationToWrite.get(0);
                        mNotificationToWrite.remove(0); sendDataToWriteSent = 0;
                        if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mNotificationToWrite.size() = " + mNotificationToWrite.size());
                        if (DEBUG) appendToLog("Removed after sending count-out.");
                        String string = "Problem in sending data to Notification Module. Removed data sending after count-out";
                        if (mBluetoothConnector.userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                        else appendToLogView(string);
                        if (true) Toast.makeText(context, cs108NotificatiionData.notificationPayloadEvent.toString(), Toast.LENGTH_LONG).show();
                    } else {
                        boolean retValue = writeNotification(mNotificationToWrite.get(0));
                        if (DEBUG) appendToLog("writeNotication with return = " + retValue);
                        if (retValue) {
                            sendDataToWriteSent++;
                        } else {
                            if (DEBUG) appendToLog("failure to send " + mNotificationToWrite.get(0).notificationPayloadEvent.toString());
                            mNotificationToWrite.remove(0);
                        }
                    }
                }
                return true;
            }
            return false;
        }

        long timeTriggerRelease;
        private boolean isNotificationToRead(Cs108ReadData cs108ReadData) {
            boolean found = false;
            if (cs108ReadData.dataValues[0] == (byte) 0xA0 && cs108ReadData.dataValues[1] == (byte) 0x00 && cs108ReadData.dataValues.length >= 4) {
                mCs108ConnectorData.mVoltageValue = (cs108ReadData.dataValues[2] & 0xFF) * 256 + (cs108ReadData.dataValues[3] & 0xFF);
                mCs108ConnectorData.mVoltageCount++;
                if (DEBUG_PKDATA) appendToLog("PkData: found Notification.Uplink with payload = " + byteArrayToString(cs108ReadData.dataValues));
                if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.GetCurrentBattteryVoltage is processed as mVoltageValue = " + mCs108ConnectorData.mVoltageValue + " and mVoltageCount = " + mCs108ConnectorData.mVoltageCount);
                found = true;
            } else if (cs108ReadData.dataValues[0] == (byte) 0xA0 && cs108ReadData.dataValues[1] == (byte) 0x01 && cs108ReadData.dataValues.length >= 3) {
                if (cs108ReadData.dataValues[2] == 0) mCs108ConnectorData.triggerButtonStatus = false;
                else mCs108ConnectorData.triggerButtonStatus = true;
                mCs108ConnectorData.iTriggerCount++;
                if (DEBUG_PKDATA) appendToLog("PkData: found Notification.Uplink with payload = " + byteArrayToString(cs108ReadData.dataValues));
                if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.GetCurrentTriggerState is processed as triggerButtonStatus = " + mCs108ConnectorData.triggerButtonStatus + " and iTriggerCount = " + mCs108ConnectorData.iTriggerCount);
                found = true;
            } else if (cs108ReadData.dataValues[0] == (byte) 0xA1) {
                if (DEBUG_PKDATA) appendToLog("PkData: found Notification.Uplink with payload = " + byteArrayToString(cs108ReadData.dataValues));
                Cs108NotificatiionData cs108NotificatiionData = new Cs108NotificatiionData();
                switch (cs108ReadData.dataValues[1]) {
                    case 0:
                        cs108NotificatiionData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_BATTERY_FAILED;
                        cs108NotificatiionData.dataValues = null;
                        if (true) mNotificationDevice.mNotificationToRead.add(cs108NotificatiionData);
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.Reserve is processed");
                        found = true;
                        break;
                    case 1:
                        cs108NotificatiionData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_BATTERY_ERROR;
                        byte[] dataValues = new byte[cs108ReadData.dataValues.length - 2];
                        System.arraycopy(cs108ReadData.dataValues, 2, dataValues, 0, dataValues.length);
                        cs108NotificatiionData.dataValues = dataValues;
                        if (true) mNotificationDevice.mNotificationToRead.add(cs108NotificatiionData);
                        btSendTime = System.currentTimeMillis() - btSendTimeOut + 50;
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.ErrorCode with value = " + byteArrayToString(dataValues) + " is addded to mNotificationToRead");
                        found = true;
                        break;
                    case 2:
                        cs108NotificatiionData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_TRIGGER_PUSHED;
                        cs108NotificatiionData.dataValues = null;
                        setTriggerStatus(true);
                        if (false) mNotificationDevice.mNotificationToRead.add(cs108NotificatiionData);
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.TriggerPushed is processed as trigger = " + getTriggerStatus());
                        found = true;
                        break;
                    case 3:
                        cs108NotificatiionData.notificationPayloadEvent = NotificationPayloadEvents.NOTIFICATION_TRIGGER_RELEASED;
                        cs108NotificatiionData.dataValues = null;
                        setTriggerStatus(false);
                        if (false) mNotificationDevice.mNotificationToRead.add(cs108NotificatiionData);
                        if (DEBUG_PKDATA) appendToLog("PkData: Notification.Uplink.TriggerReleased is processed as trigger = " + getTriggerStatus());
                        found = true;
                        break;
                    default:
                        appendToLog("Notification.Uplink with mis-matched result");
                        break;
                }
            }
            return found;
        }
    }

    enum SiliconLabIcPayloadEvents {
        GET_VERSION, GET_SERIALNUMBER, GET_MODELNAME, RESET
    }
    class Cs108SiliconLabIcReadData {
        SiliconLabIcPayloadEvents siliconLabIcPayloadEvent;
        byte[] dataValues;
    }
    class SiliconLabIcDevice {
        private byte[] mSiliconLabIcVersion = new byte[]{-1, -1, -1};

        String getSiliconLabIcVersion() {
            if (mSiliconLabIcVersion[0] == -1) {
                boolean repeatRequest = false;
                if (mSiliconLabIcToWrite.size() != 0) {
                    if (mSiliconLabIcToWrite.get(mSiliconLabIcToWrite.size() - 1) == SiliconLabIcPayloadEvents.GET_VERSION) {
                        repeatRequest = true;
                    }
                }
                if (repeatRequest == false) {
                    mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.GET_VERSION);
                    if (DEBUG_PKDATA) appendToLog("PkData: add GET_VERSION to mSiliconLabIcWrite with length = " + mSiliconLabIcToWrite.size());
                }
                return "";
            } else {
                if (false) appendToLog("siliconLabIcVersion = " + byteArrayToString(mSiliconLabIcVersion));
                String string = String.valueOf(mSiliconLabIcVersion[0]) + "." + String.valueOf(mSiliconLabIcVersion[1]) + "." + String.valueOf(mSiliconLabIcVersion[2]);
                if (false) appendToLog("siliconLabIcVersion string = " + string);
                return string;
            }
        }

        private byte[] serialNumber = null;
        String getSerialNumber() {
            if (serialNumber == null) {
                boolean repeatRequest = false;
                if (mSiliconLabIcToWrite.size() != 0) {
                    if (mSiliconLabIcToWrite.get(mSiliconLabIcToWrite.size() - 1) == SiliconLabIcPayloadEvents.GET_SERIALNUMBER) {
                        repeatRequest = true;
                    }
                }
                if (repeatRequest == false) {
                    mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.GET_SERIALNUMBER);
                    if (DEBUG_PKDATA) appendToLog("PkData: add GET_SERIALNUMBER to mSiliconLabIcWrite with length = " + mSiliconLabIcToWrite.size());
                }
                return "";
            } else {
                byte[] bytes = new byte[serialNumber.length];
                System.arraycopy(serialNumber, 0, bytes, 0, serialNumber.length);
                if (false) appendToLog("serialNumber = " + byteArrayToString(serialNumber));
                String string = byteArray2DisplayString(serialNumber);
                if (string == null || string.length() == 0) {
                    string = byteArrayToString(bytes);
                    if (string.length() > 16) string = string.substring(0, 16);
                }
                if (false) appendToLog("string = " + string);
                return string;
            }
        }

        private byte[] modelName = null;
        String getModelName() {
            if (false) appendToLog("modelName = " + byteArrayToString(modelName));
            String strValue = null;
            if (modelName == null) {
                boolean repeatRequest = false;
                if (mSiliconLabIcToWrite.size() != 0) {
                    if (mSiliconLabIcToWrite.get(mSiliconLabIcToWrite.size() - 1) == SiliconLabIcPayloadEvents.GET_MODELNAME) {
                        repeatRequest = true;
                    }
                }
                if (repeatRequest == false) {
                    mSiliconLabIcToWrite.add(SiliconLabIcPayloadEvents.GET_MODELNAME);
                    if (false) appendToLog("PkData: add GET_MODELNAME to mSiliconLabIcWrite with length = " + mSiliconLabIcToWrite.size());
                }
            } else {
                strValue = byteArray2DisplayString(modelName);
                if (false) appendToLog("strValue 0 = " + strValue);
                if (strValue == null || strValue.length() == 0) {
                    strValue = byteArrayToString(modelName).substring(0, 5);
                }
            }
            if (false) appendToLog("strValue = " + strValue);
            return strValue;
        }
        ArrayList<SiliconLabIcPayloadEvents> mSiliconLabIcToWrite = new ArrayList<>();

        private boolean arrayTypeSet(byte[] dataBuf, int pos, SiliconLabIcPayloadEvents event) {
            boolean validEvent = false;
            switch (event) {
                case GET_VERSION:
                    validEvent = true;
                    break;
                case GET_SERIALNUMBER:
                    dataBuf[pos] = 4;
                    validEvent = true;
                    break;
                case GET_MODELNAME:
                    dataBuf[pos] = 6;
                    validEvent = true;
                    break;
                case RESET:
                    dataBuf[pos] = 12;
                    validEvent = true;
                    break;
            }
            return validEvent;
        }

        private boolean writeSiliconLabIc(SiliconLabIcPayloadEvents event) {
            byte[] dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 0};
            if (event == SiliconLabIcPayloadEvents.GET_SERIALNUMBER) {
                dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 3, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 4, 0};
            } else if (event == SiliconLabIcPayloadEvents.GET_MODELNAME) {
                dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 6};
            } else if (event == SiliconLabIcPayloadEvents.RESET) {
                dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 12};
            }
            if (DEBUG) appendToLog(byteArrayToString(dataOut));
            if (DEBUG_PKDATA) appendToLog(String.format("PkData: write mSiliconLabIcDevice.%s with mSiliconLabIcDevice.sendDataToWriteSent = %d", event.toString(), sendDataToWriteSent));
            if (sendDataToWriteSent != 0) appendToLog("!!! mSiliconLabIcDevice.sendDataToWriteSent = " + sendDataToWriteSent);
            return writeData(dataOut, 0);
        }

        private boolean isMatchSiliconLabIcToWrite(Cs108ReadData cs108ReadData) {
            boolean match = false;
            if (mSiliconLabIcToWrite.size() != 0 && cs108ReadData.dataValues[0] == (byte)0xB0) {
                byte[] dataInCompare = new byte[]{(byte) 0xB0, 0};
                if (arrayTypeSet(dataInCompare, 1, mSiliconLabIcToWrite.get(0)) && (cs108ReadData.dataValues.length >= dataInCompare.length + 1)) {
                    if (match = compareArray(cs108ReadData.dataValues, dataInCompare, dataInCompare.length)) {
                        if (DEBUG_PKDATA) appendToLog("PkData: matched SiliconLabIc.Reply with payload = " + byteArrayToString(cs108ReadData.dataValues) + " for writeData.SiliconLabIc." + mSiliconLabIcToWrite.get(0).toString());
                        if (mSiliconLabIcToWrite.get(0) == SiliconLabIcPayloadEvents.GET_VERSION) {
                            if (cs108ReadData.dataValues.length >= 2 + mSiliconLabIcVersion.length) {
                                System.arraycopy(cs108ReadData.dataValues, 2, mSiliconLabIcVersion, 0, mSiliconLabIcVersion.length);
                                if (DEBUG_PKDATA) appendToLog("PkData: matched SiliconLabIc.Reply.GetVersion with version = " + byteArrayToString(mSiliconLabIcVersion));
                            }
                        } else if (mSiliconLabIcToWrite.get(0) == SiliconLabIcPayloadEvents.GET_SERIALNUMBER) {
                            int length = cs108ReadData.dataValues.length - 2;
                            serialNumber = new byte[length];
                            System.arraycopy(cs108ReadData.dataValues, 2, serialNumber, 0, length);
                            if (DEBUG_PKDATA) appendToLog("PkData: matched SiliconLabIc.Reply.GetSerialNumber with serialNumber = " + byteArrayToString(serialNumber));
                        } else if (mSiliconLabIcToWrite.get(0) == SiliconLabIcPayloadEvents.GET_MODELNAME) {
                            int length = cs108ReadData.dataValues.length - 2;
                            modelName = new byte[length];
                            System.arraycopy(cs108ReadData.dataValues, 2, modelName, 0, length);
                            if (DEBUG_PKDATA) appendToLog("PkData: matched mSiliconLabIc.GetModelName.reply with modelName = " + byteArrayToString(modelName));
                        } else if (mSiliconLabIcToWrite.get(0) == SiliconLabIcPayloadEvents.RESET) {
                            if (cs108ReadData.dataValues[2] != 0) {
                                appendToLog("Silicon Lab RESET is found with error");
                            } else appendToLog("matched SiliconLab.reply data is found");
                        } else {
                            appendToLog("matched mSiliconLabIc.Other.reply data is found.");
                        }
                        mSiliconLabIcToWrite.remove(0); sendDataToWriteSent = 0;
                        if (DEBUG_PKDATA) appendToLog("PkData: new mSiliconLabIcToWrite size = " + mSiliconLabIcToWrite.size());

                    }
                }
            }
            return match;
        }

        private int sendDataToWriteSent = 0;
        private boolean sendSiliconLabIcToWrite() {
            if (mSiliconLabIcToWrite.size() != 0) {
                if (isBleConnected() == false) {
                    mSiliconLabIcToWrite.clear();
                } else if (System.currentTimeMillis() - btSendTime > btSendTimeOut) {
                    if (sendDataToWriteSent >= 5) {
                        int oldSize = mSiliconLabIcToWrite.size();
                        mSiliconLabIcToWrite.remove(0); sendDataToWriteSent = 0;
                        if (DEBUG) appendToLog("Removed after sending count-out with oldSize = " + oldSize + ", updated mSiliconLabIcToWrite.size() = " + mSiliconLabIcToWrite.size());
                        if (DEBUG) appendToLog("Removed after sending count-out.");
                        String string = "Problem in sending data to SiliconLabIc Module. Removed data sending after count-out";
                        if (mBluetoothConnector.userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                        else appendToLogView(string);
                    } else {
                        if (DEBUG) appendToLog("size = " + mSiliconLabIcToWrite.size());
                        boolean retValue = writeSiliconLabIc(mSiliconLabIcToWrite.get(0));
                        if (retValue) {
                            sendDataToWriteSent++;
                        } else {
                            appendToLogView("failure to send " + mSiliconLabIcToWrite.get(0).toString());
                            mSiliconLabIcToWrite.remove(0);
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    enum ControlCommands {
        NULL,
        CANCEL, SOFTRESET, ABORT, PAUSE, RESUME, GETSERIALNUMBER, RESETTOBOOTLOADER
    }

    enum HostRegRequests {
        MAC_OPERATION,
        //MAC_VER, MAC_LAST_COMMAND_DURATION,
        //HST_CMNDIAGS,
        //HST_MBP_ADDR, HST_MBP_DATA,
        //HST_OEM_ADDR, HST_OEM_DATA,
        HST_ANT_CYCLES, HST_ANT_DESC_SEL, HST_ANT_DESC_CFG, MAC_ANT_DESC_STAT, HST_ANT_DESC_PORTDEF, HST_ANT_DESC_DWELL, HST_ANT_DESC_RFPOWER, HST_ANT_DESC_INV_CNT,
        HST_TAGMSK_DESC_SEL, HST_TAGMSK_DESC_CFG, HST_TAGMSK_BANK, HST_TAGMSK_PTR, HST_TAGMSK_LEN, HST_TAGMSK_0_3,
        HST_QUERY_CFG, HST_INV_CFG, HST_INV_SEL, HST_INV_ALG_PARM_0, HST_INV_ALG_PARM_1, HST_INV_ALG_PARM_2, HST_INV_ALG_PARM_3, HST_INV_RSSI_FILTERING_CONFIG, HST_INV_RSSI_FILTERING_THRESHOLD, HST_INV_RSSI_FILTERING_COUNT, HST_INV_EPC_MATCH_CFG, HST_INV_EPCDAT_0_3,
        HST_TAGACC_DESC_CFG, HST_TAGACC_BANK, HST_TAGACC_PTR, HST_TAGACC_CNT, HST_TAGACC_LOCKCFG, HST_TAGACC_ACCPWD, HST_TAGACC_KILLPWD, HST_TAGWRDAT_SEL, HST_TAGWRDAT_0,
        HST_RFTC_CURRENT_PROFILE,
        HST_RFTC_FRQCH_SEL, HST_RFTC_FRQCH_CFG, HST_RFTC_FRQCH_DESC_PLLDIVMULT, HST_RFTC_FRQCH_DESC_PLLDACCTL, HST_RFTC_FRQCH_CMDSTART,
        HST_AUTHENTICATE_CFG, HST_AUTHENTICATE_MSG, HST_READBUFFER_LEN, HST_UNTRACEABLE_CFG,
        HST_CMD
    }
    class Rx000Setting {
        Rx000Setting(boolean set_default_setting) {
            if (set_default_setting) {
                macVer = mDefault.macVer;
                diagnosticCfg = mDefault.diagnosticCfg;
                oemAddress = mDefault.oemAddress;

                //RFTC block paramters
                currentProfile = mDefault.currentProfile;

                // Antenna block parameters
                antennaCycle = mDefault.antennaCycle;
                antennaFreqAgile = mDefault.antennaFreqAgile;
                antennaSelect = mDefault.antennaSelect;
            }
            antennaSelectedData = new AntennaSelectedData[ANTSELECT_MAX + 1];
            for (int i = 0; i < antennaSelectedData.length; i++) {
                int default_setting_type = 0;
                if (set_default_setting) {
                    if (i == 0) default_setting_type = 1;
                    else if (i >= 1 && i <= 3)  default_setting_type = 2;
                    else if (i >= 4 && i <= 7)  default_setting_type = 3;
                    else if (i >= 8 && i <= 11) default_setting_type = 4;
                    else    default_setting_type = 5;
                }
                antennaSelectedData[i] = new AntennaSelectedData(set_default_setting, default_setting_type);
            }

            //Tag select block parameters
            if (set_default_setting)    invSelectIndex = 0;
            invSelectData = new InvSelectData[INVSELECT_MAX + 1];
            for (int i = 0; i < invSelectData.length; i++) {
                invSelectData[i] = new InvSelectData(set_default_setting);
            }

            if (set_default_setting) {
                //Inventtory block paraameters
                //queryTarget = mDefault.queryTarget;
                //querySession = mDefault.querySession;
                //querySelect = mDefault.querySelect;
                invAlgo = mDefault.invAlgo;
                matchRep = mDefault.matchRep;
                tagSelect = mDefault.tagSelect;
                noInventory = mDefault.noInventory;
                tagDelay = mDefault.tagDelay;
                invModeCompact = mDefault.tagJoin;
                invBrandId = mDefault.brandid;
            }

            if (set_default_setting)    algoSelect = 3;
            algoSelectedData = new AlgoSelectedData[ALGOSELECT_MAX + 1];
            for (int i = 0; i < algoSelectedData.length; i++) {//0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
                int default_setting_type = 0;
                if (set_default_setting) {
                    default_setting_type = i + 1;
                }
                algoSelectedData[i] = new AlgoSelectedData(set_default_setting, default_setting_type);
            }

            if (set_default_setting) {
                rssiFilterType = mDefault.rssiFilterType;
                rssiFilterOption = mDefault.rssiFilterOption;
                rssiFilterThreshold1 = mDefault.rssiFilterThreshold;
                rssiFilterThreshold2 = mDefault.rssiFilterThreshold;
                rssiFilterCount = mDefault.rssiFilterCount;

                matchEnable = mDefault.matchEnable;
                matchType = mDefault.matchType;
                matchLength = mDefault.matchLength;
                matchOffset = mDefault.matchOffset;
                invMatchDataReady = mDefault.invMatchDataReady;

                //Tag access block parameters
                //accessRetry = mDefault.accessRetry;
                //accessBank = mDefault.accessBank; accessBank2 = mDefault.accessBank2;
                //accessOffset = mDefault.accessOffset; accessOffset2 = mDefault.accessOffset2;
                //accessCount = mDefault.accessCount; accessCount2 = mDefault.accessCount2;
                //accessLockAction = mDefault.accessLockAction;
                //accessLockMask = mDefault.accessLockMask;
                //long accessPassword = 0;
                //long killPassword = 0;
                //accessWriteDataSelect = mDefault.accessWriteDataSelect;
                //accWriteDataReady = mDefault.accWriteDataReady;

                authMatchDataReady = mDefault.authMatchDataReady;
            }

            invMatchData0_63 = new byte[4 * 16];
            accWriteData0_63 = new byte[4 * 16 * 2];
            authMatchData0_63 = new byte[4 * 4];
        }

        class Rx000Setting_default {
            String macVer;
            int diagnosticCfg = 0x210;
            int mbpAddress = 0; // ?
            int mbpData = 0; // ?
            int oemAddress = 4; // ?
            int oemData = 0; // ?

            //RFTC block paramters
            int currentProfile = 1;
            int freqChannelSelect = 0;

            // Antenna block parameters
            int antennaCycle = 1;
            int antennaFreqAgile = 0;
            int antennaSelect = 0;

            //Tag select block parameters
            int invSelectIndex = 0;

            //Inventtory block paraameters
            int queryTarget = 0;
            int querySession = 2;
            int querySelect = 1;
            int invAlgo = 3;
            int matchRep = 0;
            int tagSelect = 0;
            int noInventory = 0;
            int tagRead = 0;
            int tagDelay = 0;
            int tagJoin = 0;
            int brandid = 0;
            int algoSelect = 3;

            int rssiFilterType = 0;
            int rssiFilterOption = 0;
            int rssiFilterThreshold = 0;
            long rssiFilterCount = 0;

            int matchEnable = 0;
            int matchType = 0;
            int matchLength = 0;
            int matchOffset = 0;
            byte[] invMatchData0_63; int invMatchDataReady = 0;

            //Tag access block parameters
            int accessRetry = 3;
            int accessBank = 1; int accessBank2 = 0;
            int accessOffset = 2; int accessOffset2 = 0;
            int accessCount = 1; int accessCount2 = 0;
            int accessLockAction = 0;
            int accessLockMask = 0;
            //long accessPassword = 0;
            // long killPassword = 0;
            int accessWriteDataSelect = 0;
            byte[] accWriteData0_63; int accWriteDataReady = 0;

            byte[] authMatchData; int authMatchDataReady = 0;
        }
        Rx000Setting_default mDefault = new Rx000Setting_default();

        boolean readMAC(int address, int length) {
            byte[] msgBuffer = new byte[]{(byte) 0x80, (byte)0xb3, 0x14, 0x71, 0, 0, 4,   1, 0, 8, 0};
            msgBuffer[8] = (byte) ((address >> 8) % 256);
            msgBuffer[9] = (byte) (address % 256);
            msgBuffer[10] = (byte) (length & 0xFF);
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.MAC_OPERATION, false, msgBuffer);
        }
        boolean writeMAC(int address, byte[] bytes, boolean bReady) {
            /*if (address != 0x3031
                    && address != 0x3014
                    && address != 0x3033
                    && address != 0x303E
                    && address != 0x3038
                    && address != 0x3140
            )*/
            if (false && address == 0x3035) {
                appendToLog(String.format("0 writeMAC[address = 0x%X, bytes = %s with antennaPortConfig = %s", address, byteArrayToString(bytes), byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.getAntennaPortConfig(0))));
                //bytes[1] = 0x1E; //(byte)0x86; //orginal 6, new 0x9E
                //bytes[8] = 1; //original 1, new 8
                appendToLog(String.format("0A writeMAC[address = 0x%X, bytes = %s with antennaPortConfig = %s", address, byteArrayToString(bytes), byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.getAntennaPortConfig(0))));
                //return true;
            }
            byte[] header = new byte[] {(byte) 0x80, (byte)0xb3, (byte)0x9A, 6, 0, 0, 4,   1, 0, 8, 0 };
            byte[] msgBuffer = new byte[header.length + bytes.length];
            int iPayloadLength = 4 + bytes.length;
            System.arraycopy(header, 0, msgBuffer, 0, header.length);
            msgBuffer[5] = (byte)((iPayloadLength / 256) & 0xFF);
            msgBuffer[6] = (byte)((iPayloadLength % 256) & 0xFF);
            msgBuffer[8] = (byte) ((address >> 8) & 0xFF);
            msgBuffer[9] = (byte) (address & 0xFF);
            msgBuffer[10] = (byte) (bytes.length & 0XFF);
            System.arraycopy(bytes, 0, msgBuffer, 11, bytes.length);
            if (false) appendToLog(String.format("3 writeMAC with address = 0x%X, msgBuffer = ", address) + byteArrayToString(msgBuffer));
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.MAC_OPERATION, true, msgBuffer);
        }
        boolean readMAC(int address) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 0, 0, 0, 0, 0};
            msgBuffer[2] = (byte) (address % 256);
            msgBuffer[3] = (byte) ((address >> 8) % 256);
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.MAC_OPERATION, false, msgBuffer);
        }
        boolean writeMAC(int address, long value) {
            byte[] msgBuffer = null;
            appendToLog(String.format("3A setTagFocus with address = 0x%X, value = 0x%X ", address, value));
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.MAC_OPERATION, true, msgBuffer);
        }

        String macVer = null; int macVerBuild = -1;
        String getMacVer() {
            if (macVerBuild < 0) readMAC(0x28, 4);
            if (macVer == null) readMAC(8, 0x20);
            if (macVerBuild < 0 || macVer == null) return null;
            String strValue = macVer + " b" + macVerBuild;
            if (false) appendToLog("2 getMacVer = " + strValue);
            return strValue;
        }

        int authenticateConfig = -1;
        boolean setAuthenticateConfig(int authenticateConfig) {
            byte[] data = new byte[3];
            data[0] = (byte) ((authenticateConfig >> 16) & 0xFF);
            data[1] = (byte) ((authenticateConfig >> 8) & 0xFF);
            data[2] = (byte) (authenticateConfig & 0xFF);
            boolean bValue = writeMAC(0x390E, data, true);
            if (bValue) this.authenticateConfig = authenticateConfig;
            //readMAC3(0x390E, 3);
            return bValue;
        }
        boolean setAuthenticateMessage(byte[] authenticateMessage) {
            int length = authenticateMessage.length;
            if (length > 32) length = 32;
            byte[] data = new byte[length];
            System.arraycopy(authenticateMessage, 0, data, 0, length);
            boolean bValue = writeMAC(0x3912, data, true);
            //readMAC3(0x3912, 32);
            return bValue;
        }
        boolean setAuthenticateResponseLen(int authenticateResponseLen) {
            byte[] data = new byte[2];
            data[0] = (byte) ((authenticateResponseLen >> 8) & 0xFF);
            data[1] = (byte) (authenticateResponseLen & 0xFF);
            boolean bValue = writeMAC(0x3944, data, true);
            //readMAC3(0x3944, 2);
            return bValue;
        }

        byte[] currentPort = null;
        byte getCurrentPort() {
            byte byValue = (byte)-1;
            if (currentPort != null && currentPort.length == 1) byValue = currentPort[0];
            else readMAC(0x3948, 1);
            appendToLog("byValue = " + byValue);
            return byValue;
        }
        boolean setCurrentPort(byte currentPortNew) {
            if (currentPortNew >= 0 && currentPort.length == 1 && currentPort[0] == currentPortNew && sameCheck) {
                appendToLog("!!! Skip sending repeated data with currentPortNew = " + currentPortNew);
                return true;
            }
            byte[] bytes = new byte[1];
            bytes[0] = currentPortNew;
            boolean bValue;
            bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3948, bytes, true);
            appendToLog("new currentPort = " + byteArrayToString(bytes) + ", old currentPort = " + byteArrayToString(currentPort));
            if (bValue) currentPort = bytes;
            return true;
        }
        boolean updateCurrentPort() {
            byte currentPortOld = getCurrentPort();
            byte currentPortNew = 0;
            for (int i = 0; i < 16; i++) {
                if (mRfidDevice.mRfidReaderChip.mRx000Setting.antennaPortConfig[i] != null) {
                    if (mRfidDevice.mRfidReaderChip.mRx000Setting.antennaPortConfig[i][0] != 0) {
                        currentPortNew = (byte)(i & 0xFF);
                    }
                }
            }
            appendToLog("currentPortOld = " + currentPortOld + ", currentPortNew = " + currentPortNew);
            boolean bValue = false;
            if (currentPortOld != currentPortNew) bValue = setCurrentPort(currentPortNew);
            return bValue;
        }

        byte[] modelCode;
        String getModelCode() {
            String strValue = null;
            if (modelCode == null) readMAC(0x5000, 32);
            else {
                strValue = byteArray2DisplayString(modelCode);
                if (strValue == null || strValue.length() == 0) strValue = byteArrayToString(modelCode).substring(0, 5);
            }
            return strValue;
        }

        byte[] productSerialNumber;
        String getProductSerialNumber() {
            String strValue = null;
            if (productSerialNumber == null) {
                readMAC(0x5020, 32);
            } else {
                strValue = byteArray2DisplayString(productSerialNumber);
                if (strValue == null || strValue.length() == 0) strValue = byteArrayToString(productSerialNumber).substring(0, 5);
                //string.substring(string.length() - 8, string.length());
            }
            return strValue;
        }

        byte[] countryEnum;
        int getCountryEnum() {
            int iValue = -1;
            if (countryEnum == null) readMAC(0x3014, 2);
            else iValue = byteArrayToInt(countryEnum);
            appendToLog("countryEnum = " + iValue);
            return iValue;
        }
        boolean setCountryEnum(short countryEnum) {
            byte[] data = new byte[2];
            data[0] = 0;
            data[1] = (byte) ((countryEnum) & 0xFF);
            if (this.countryEnum != null &&  compareArray(this.countryEnum, data, data.length) && sameCheck) return true;
            boolean bValue = writeMAC(0x3014, data, true);
            appendToLog("new countryEnum = " + byteArrayToString(data) + ", with bValue = " + bValue);
            if (bValue) this.countryEnum = data;
            return bValue;
        }

        byte[] frequencyChannelIndex;
        int getFrequencyChannelIndex() {
            int iValue = -1;
            if (frequencyChannelIndex == null) readMAC(0x3018, 1);
            else iValue = byteArrayToInt(frequencyChannelIndex);
            appendToLog("frequencyChannelIndex = " + iValue);
            return iValue;
        }
        boolean setFrequencyChannelIndex(byte frequencyChannelIndex) {
            byte[] data = new byte[1];
            data[0] = frequencyChannelIndex;
            if (this.frequencyChannelIndex != null && compareArray(this.frequencyChannelIndex, data, data.length) && sameCheck) return true;
            boolean bValue = writeMAC(0x3018, data, true);
            appendToLog("new frequencyChannelIndex = " + byteArrayToString(data) + ", old frequencyChannelIndex = " + byteArrayToString(this.frequencyChannelIndex) + ", with bValue = " + bValue + ", sameCheck = " + sameCheck);
            if (bValue) this.frequencyChannelIndex = data;
            return bValue;
        }

        byte[] countryEnumOem;
        int getCountryEnumOem() {
            int iValue = -1;
            if (countryEnumOem == null) readMAC(0x5040, 2);
            else iValue = byteArrayToInt(countryEnumOem);
            appendToLog("countryEnumOem = " + iValue);
            return iValue;
        }

        byte[] countryCodeOem;
        int getCountryCodeOem() {
            int iValue = -1;
            if (countryCodeOem == null) readMAC(0xef98, 4);
            else iValue = byteArrayToInt(countryCodeOem);
            return iValue;
        }
        byte[] boardSerialNumber;
        String getBoardSerialNumber() {
            if (boardSerialNumber == null) {
                readMAC(0xef9c, 16);
                return null;
            } else if (boardSerialNumber.length < 13) return null;
            else {
                byte[] retValue = new byte[boardSerialNumber.length];
                System.arraycopy(boardSerialNumber, 0, retValue, 0, boardSerialNumber.length);
                String string = new String(retValue).trim().replaceAll("[^\\x00-\\x7F]", "");
                if (string == null || string.length() == 0) return byteArrayToString(retValue).substring(0, 13);
                if (false) appendToLog("String = " + string + ", length = " + string.length());
                if (retValue.length > 13) {
                    for (int i = 13; i < retValue.length; i++) {
                        if (retValue[i] < 0x30) retValue[i] += 0x30;
                    }
                }
                return (retValue == null ? null : new String(retValue).trim().replaceAll("[^\\x00-\\x7F]", ""));
            }
        }

        byte[] specialcountryCodeOem;
        String getSpecialCountryCodeOem() {
            String strValue = null;
            if (specialcountryCodeOem == null) readMAC(0xefac, 4);
            else {
                strValue = byteArray2DisplayString(specialcountryCodeOem);
                //if (strValue == null || strValue.length() == 0) strValue = byteArrayToString(modelCode).substring(0, 5);
            }
            return strValue;
        }

        byte[] freqModifyCode;
        int getFreqModifyCode() {
            int iValue = -1;
            if (freqModifyCode == null) readMAC(0xefb0, 4);
            else iValue = byteArrayToInt(freqModifyCode);
            return iValue;
        }
        long mac_last_command_duration;
        long getMacLastCommandDuration(boolean request) {
            if (request) {
                if (true) readMAC(9);
                //byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, 0, 0, 0, 0, 0};
                //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.MAC_LAST_COMMAND_DURATION, false, msgBuffer);
            }
            return mac_last_command_duration;
        }

        final int DIAGCFG_INVALID = -1; final int DIAGCFG_MIN = 0; final int DIAGCFG_MAX = 0x3FF;
        int diagnosticCfg = DIAGCFG_INVALID;
        int getDiagnosticConfiguration() {
            if (diagnosticCfg < DIAGCFG_MIN || diagnosticCfg > DIAGCFG_MAX) {
                if (true) readMAC(0x201);
                //byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 2, 0, 0, 0, 0};
                //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_CMNDIAGS, false, msgBuffer);
            }
            return diagnosticCfg;
        }
        boolean setDiagnosticConfiguration(boolean bCommmandActive) {
//            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 2, (byte)0x10, 0, 0, 0};
//            if (bCommmandActive) msgBuffer[5] |= 0x20;
            int diagnosticCfgNew;
            diagnosticCfgNew = 0x10; if (bCommmandActive) diagnosticCfgNew |= 0x20;
            appendToLog("diagnosticCfg = " + diagnosticCfg + ", diagnosticCfgNew = " + diagnosticCfgNew);
            if (diagnosticCfg == diagnosticCfgNew && sameCheck) return true;
            diagnosticCfg = diagnosticCfgNew;
            return writeMAC(0x201, diagnosticCfgNew); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_CMNDIAGS, true, msgBuffer);
        }
        int getAntennaPort() {
            if (false) appendToLog("2 iAntennaPort = " + antennaSelect);
            return antennaSelect;
        }
        byte[][] antennaPortConfig = new byte[16][];
        byte[] getAntennaPortConfig(int iAntennaPort) {
            byte[] bytes = null;
            if (antennaPortConfig[iAntennaPort] == null) {
                if (false) appendToLog("getAntennaPortConfig starts readMAC");
                readMAC(0x3030 + iAntennaPort * 16, 16);
            }
            else {
                bytes = new byte[antennaPortConfig[iAntennaPort].length];
                System.arraycopy(antennaPortConfig[iAntennaPort], 0, bytes, 0, bytes.length);
            }
            if (false) appendToLog("getAntennaPortConfig[" + iAntennaPort + "] = " + byteArrayToString(antennaPortConfig[iAntennaPort]));
            return bytes;
        }

        int impinjExtensionValue = -1;
        int getImpinjExtension() {
            int iValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("2 getImpinjExtension: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2A getImpinjExtension: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = (antennaPortConfig[antennaSelect][5] & 0x06);
                if (DEBUG) appendToLog(String.format("2b getImpinjExtension: iValue = 0x%X", iValue));
            }
            return iValue;
        }

        boolean setImpinjExtension(boolean tagFocus, boolean fastId) {
            boolean bValue = false, DEBUG = false;
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2 setImpinjExtension: tagFocus = " + tagFocus);
                if (DEBUG) appendToLog("2A setImpinjExtension: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                bytes[0] = antennaPortConfig[antennaSelect][5];

                if (tagFocus) bytes[0] |= 0x04;
                else bytes[0] &= ~0x04;
                if (fastId) bytes[0] |= 0x02;
                else bytes[0] &= ~0x02;
                if (DEBUG) appendToLog(String.format("2A1 setImpinjExtension: bytes = 0x%X", bytes[0]));
                boolean bSame = false;
                if (sameCheck | true) {
                    if (antennaPortConfig[antennaSelect][5] == bytes[0]) bSame = true;
                    if (DEBUG) appendToLog("2ab setImpinjExtension: the array is the same = " + bSame);
                }
                //bSame = false; appendToLog("!!! assme bSame is false before 1A writeMAC 0x3035");
                if (bSame) {
                    if (DEBUG_PKDATA) appendToLog(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes),  0x3030 + antennaSelect * 16 + 5));
                    bValue = true;
                } else {
                    appendToLog("test 1");
                    bValue = writeMAC(0x3030 + antennaSelect * 16 + 5, bytes, true);
                    if (bValue) antennaPortConfig[antennaSelect][5] = bytes[0];
                }
                if (DEBUG) appendToLog("2b setImpinjExtension: with updated " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }

        int iSelectPort = 0;
        byte[][] selectConfiguration = new byte[7][];

        byte[] getSelectConfiguration(int iSelectPort) {
            byte[] bytes = null;
            if (selectConfiguration[iSelectPort] == null) {
                if (false) appendToLog("getSelectConfiguration starts readMAC");
                readMAC(0x3140 + iSelectPort * 42, 42);
            }
            else {
                bytes = new byte[selectConfiguration[iSelectPort].length];
                System.arraycopy(selectConfiguration[iSelectPort], 0, bytes, 0, bytes.length);
            }
            if (false) appendToLog("getSelectConfiguration[" + iSelectPort + "] = " + byteArrayToString(selectConfiguration[iSelectPort]));
            return bytes;
        }
        boolean setSelectConfiguration(int index, boolean enable, int bank, int offset, byte[] mask, int target, int action, int delay) {
            boolean bValue = false;
            byte[] bytes = new byte[42];
            if (selectConfiguration[index] != null) System.arraycopy(selectConfiguration[index], 0, bytes, 0, bytes.length);
            bytes[0] = (byte) (enable ? 1 : 0);
            bytes[1] = (byte) (bank & 0xFF);
            bytes[2] = (byte) ((offset >> 24) & 0xFF);
            bytes[3] = (byte) ((offset >> 16) & 0xFF);
            bytes[4] = (byte) ((offset >>  8) & 0xFF);
            bytes[5] = (byte) (offset & 0xFF);
            if (mask != null) {
                int iWdith = mask.length;
                if (iWdith > 32) iWdith = 32;
                System.arraycopy(mask, 0, bytes, 7, iWdith);
                bytes[6] = (byte) ((iWdith * 8) & 0xFF);
            } else bytes[6] = 0;
            bytes[39] = (byte) (target & 0xFF);
            bytes[40] = (byte) (action & 0xFF);
            bytes[41] = (byte) (delay & 0xFF);
            if (false) appendToLog("1A writeMAC 0x3140");
            if (compareArray(selectConfiguration[index], bytes, bytes.length) && sameCheck) {
                appendToLog("!!! Skip sending repeated data " + byteArrayToString(bytes) + " to address 0x3140");
                return true;
            }
            bValue = writeMAC(0x3140 + index * 42, bytes, true);
            if (bValue) selectConfiguration[index] = bytes;
            else appendToLog("!!! Failed to send data " + byteArrayToString(bytes) + " to address 0x3140");
            return bValue;
        }

        byte[][] multibankReadConfig = new byte[3][];
        int getMultibankReadLength(int iSelectPort) {
            int iValue = 0;
            if (multibankReadConfig[iSelectPort] != null) {
                if (multibankReadConfig[iSelectPort][0] != 0) iValue = multibankReadConfig[iSelectPort][6];
            }
            return iValue;
        }
        byte[] getMultibankReadConfig(int iSelectPort) {
            boolean DEBUG = false; int iPortSize = 7;
            byte[] bytes = null;
            if (multibankReadConfig[iSelectPort] == null) {
                if (DEBUG) appendToLog("getMultibankReadConfig starts readMAC");
                readMAC(0x3270 + iSelectPort * iPortSize, iPortSize);
            }
            else {
                bytes = new byte[multibankReadConfig[iSelectPort].length];
                System.arraycopy(multibankReadConfig[iSelectPort], 0, bytes, 0, bytes.length);
            }
            if (DEBUG) appendToLog("getMultibankReadConfig[" + iSelectPort + "] = " + byteArrayToString(multibankReadConfig[iSelectPort]));
            return bytes;
        }
        byte[][] multibankWriteConfig = new byte[3][];
        boolean setMultibankReadConfig(int index, boolean enable, int bank, int offset, int length) {
            boolean bValue = false;
            byte[] bytes = new byte[7];
            bytes[0] = (byte) (enable ? 1 : 0);
            bytes[1] = (byte) (bank & 0xFF);
            bytes[2] = (byte) ((offset >> 24) & 0xFF);
            bytes[3] = (byte) ((offset >> 16) & 0xFF);
            bytes[4] = (byte) ((offset >>  8) & 0xFF);
            bytes[5] = (byte) (offset & 0xFF);
            bytes[6] = (byte) (length & 0xFF);
            bValue = writeMAC(0x3270 + index * 7, bytes, true);
            if (bValue) multibankReadConfig[index] = bytes;
            return bValue;
        }

        boolean setMultibankWriteConfig(int index, boolean enable, int bank, int offset, int length, byte[] data) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("Start with index = " + index + ", enable = " + enable + ", bank = " + bank + ", offset = " + offset + ", length = " + length + ", data = " + byteArrayToString(data));
            byte[] bytes = new byte[7+data.length];
            bytes[0] = (byte) (enable ? 1 : 0);
            bytes[1] = (byte) (bank & 0xFF);
            bytes[2] = (byte) ((offset >> 24) & 0xFF);
            bytes[3] = (byte) ((offset >> 16) & 0xFF);
            bytes[4] = (byte) ((offset >>  8) & 0xFF);
            bytes[5] = (byte) (offset & 0xFF);
            bytes[6] = (byte) (length & 0xFF);
            System.arraycopy(data, 0, bytes, 7, data.length);
            if (DEBUG) appendToLog("bytes = " + byteArrayToString(bytes));
            bValue = writeMAC(0x3290 + index * 519, bytes, true);
            if (DEBUG) appendToLog("After writeMAC, bValue = " + bValue);
            if (bValue) multibankWriteConfig[index] = bytes;
            return bValue;
        }
        int pwrMgmtStatus = -1;
        void getPwrMgmtStatus() {
            appendToLog("pwrMgmtStatus: getPwrMgmtStatus ");
            pwrMgmtStatus = -1; readMAC(0x204);
        }

        final int MBPADDR_INVALID = -1; final int MBPADDR_MIN = 0; final int MBPADDR_MAX = 0x1FFF;
        long mbpAddress = MBPADDR_INVALID;
        boolean setMBPAddress(long mbpAddress) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 4, 0, 0, 0, 0};
            if (mbpAddress < MBPADDR_MIN || mbpAddress > MBPADDR_MAX) return false;
                //mbpAddress = mDefault.mbpAddress;
            if (this.mbpAddress == mbpAddress && sameCheck)  return true;
            //msgBuffer[4] = (byte) (mbpAddress % 256);
            //msgBuffer[5] = (byte) ((mbpAddress >> 8) % 256);
            this.mbpAddress = mbpAddress;
            if (false) appendToLog("Going to writeMAC");
            appendToLog("3 setRxGain");
            return writeMAC(0x400, (int) mbpAddress); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_MBP_ADDR, true, msgBuffer);
        }

        final int MBPDATA_INVALID = -1; final int MBPDATA_MIN = 0; final int MBPDATA_MAX = 0x1FFF;
        long mbpData = MBPDATA_INVALID;
        boolean setMBPData(long mbpData) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 4, 0, 0, 0, 0};
            if (mbpData < MBPADDR_MIN || mbpData > MBPADDR_MAX) return false;
                //mbpData = mDefault.mbpData;
            if (this.mbpData == mbpData && sameCheck)  return true;
            //msgBuffer[4] = (byte) (mbpData % 256);
            //msgBuffer[5] = (byte) ((mbpData >> 8) % 256);
            this.mbpData = mbpData;
            return writeMAC(0x401, (int) mbpData); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_MBP_DATA, true, msgBuffer);
        }

        final int OEMADDR_INVALID = -1; final int OEMADDR_MIN = 0; final int OEMADDR_MAX = 0x1FFF;
        long oemAddress = OEMADDR_INVALID;
        boolean setOEMAddress(long oemAddress) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 5, 0, 0, 0, 0};
            if (oemAddress < OEMADDR_MIN || oemAddress > OEMADDR_MAX) return false;
                //oemAddress = mDefault.oemAddress;
            if (this.oemAddress == oemAddress && sameCheck)  return true;
            //msgBuffer[4] = (byte) (oemAddress % 256);
            //msgBuffer[5] = (byte) ((oemAddress >> 8) % 256);
            this.oemAddress = oemAddress;
            return writeMAC(0x500, (int) oemAddress); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_OEM_ADDR, true, msgBuffer);
        }

        final int OEMDATA_INVALID = -1; final int OEMDATA_MIN = 0; final int OEMDATA_MAX = 0x1FFF;
        long oemData = OEMDATA_INVALID;
        boolean setOEMData(long oemData) {
            //byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 5, 0, 0, 0, 0};
            if (oemData < OEMADDR_MIN || oemData > OEMADDR_MAX) return false;
                //oemData = mDefault.oemData;
            if (this.oemData == oemData && sameCheck)  return true;
            //msgBuffer[4] = (byte) (oemData % 256);
            //msgBuffer[5] = (byte) ((oemData >> 8) % 256);
            this.oemData = oemData;
            return writeMAC(0x501, (int) oemData); //mRfidDevice.mRx000Device.sendHostRegRequest(HostRegRequests.HST_OEM_DATA, true, msgBuffer);
        }

        // Antenna block parameters
        final int ANTCYCLE_INVALID = -1; final int ANTCYCLE_MIN = 0; final int ANTCYCLE_MAX = 0xFFFF;
        int antennaCycle = ANTCYCLE_INVALID;
        int getAntennaCycle() {
            if (antennaCycle < ANTCYCLE_MIN || antennaCycle > ANTCYCLE_MAX) getHST_ANT_CYCLES();
            return antennaCycle;
        }
        boolean setAntennaCycle(int antennaCycle) {
            if (antennaCycle == this.antennaCycle) return true;
            this.antennaCycle = antennaCycle; appendToLog(String.format("!!! Skip setAntennaCycle[0x%X]", antennaCycle));
            return true;
        }
        boolean setAntennaCycle(int antennaCycle, int antennaFreqAgile) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, 7, 0, 0, 0, 0};
            if (antennaCycle < ANTCYCLE_MIN || antennaCycle > ANTCYCLE_MAX) antennaCycle = mDefault.antennaCycle;
            if (antennaFreqAgile < FREQAGILE_MIN || antennaFreqAgile > FREQAGILE_MAX)   antennaFreqAgile = mDefault.antennaFreqAgile;
            if (this.antennaCycle == antennaCycle && this.antennaFreqAgile == antennaFreqAgile  && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaCycle % 256);
            msgBuffer[5] = (byte) ((antennaCycle >> 8) % 256);
            if (antennaFreqAgile != 0) {
                msgBuffer[7] |= 0x01;
            }
            this.antennaCycle = antennaCycle;
            this.antennaFreqAgile = antennaFreqAgile;
            appendToLog("3 Set HST_ANT_CYCLES");
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_CYCLES, true, msgBuffer);
        }

        final int FREQAGILE_INVALID = -1; final int FREQAGILE_MIN = 0; final int FREQAGILE_MAX = 1;
        int antennaFreqAgile = FREQAGILE_INVALID;
        int getAntennaFreqAgile() {
            if (antennaFreqAgile < FREQAGILE_MIN || antennaFreqAgile > FREQAGILE_MAX)
                getHST_ANT_CYCLES();
            return antennaFreqAgile;
        }
        boolean setAntennaFreqAgile(int freqAgile) {
            appendToLog("Set HST_ANT_CYCLES");
            return setAntennaCycle(antennaCycle, freqAgile);
        }

        private boolean getHST_ANT_CYCLES() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 7, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_CYCLES, false, msgBuffer);
        }

        final int ANTSELECT_INVALID = -1; final int ANTSLECT_MIN = 0; final int ANTSELECT_MAX = 15;
        int antennaSelect = ANTSELECT_INVALID;  //default value = 0
        boolean setAntennaSelect(int antennaSelect) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  antennaSelect = mDefault.antennaSelect;
            if (this.antennaSelect == antennaSelect && sameCheck) return true;
            this.antennaSelect = antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect);
            return true;
        }

        AntennaSelectedData[] antennaSelectedData;
        int getAntennaEnable() {
            boolean DEBUG = false;
            int iValue = -1;
            if (antennaPortConfig[antennaSelect] == null) {
                mRfidDevice.mRfidReaderChip.mRx000Setting.getAntennaPortConfig(antennaSelect);
                readMAC(0x3030 + antennaSelect * 16, 16);
                appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            } else {
                if (DEBUG) appendToLog("2 getAntennaEnable");
                if (DEBUG) appendToLog("2A getAntennaEnable: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = antennaPortConfig[antennaSelect][0] & 0xFF;
            }
            if (DEBUG) appendToLog("2 getAntennaEnable: iValue = " + iValue);
            return iValue;
        }
        boolean setAntennaEnable(int antennaEnable) {
            boolean bValue = false, DEBUG = true;

            appendToLog("antennaEnable is " + antennaEnable);
            if (antennaEnable == 0) {
                boolean disableInvalid = true;
                for (int i = 0; i < 16; i++) {
                    appendToLog("i = " + i + ", antennaSelect = " + antennaSelect);
                    if (i != antennaSelect && antennaPortConfig[i] != null) {
                        if (antennaPortConfig[i][0] != 0) disableInvalid = false;
                        appendToLog("i = " + i + ", disableInvalid = " + disableInvalid);
                    }
                }
                appendToLog("disableInvalid is " + disableInvalid);
                if (disableInvalid) return false;
            }

            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2 setAntennaEnable with antennaEnable = " + antennaEnable);
                if (DEBUG) appendToLog("2A setAntennaEnable: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                if (antennaEnable > 0) bytes[0] = 1;
                else bytes[0] = 0;
                if (DEBUG) appendToLog("2b setAntennaEnable: bytes = " + byteArrayToString(bytes));
                bValue = writeMAC(0x3030 + antennaSelect * 16, bytes, true);
                if (bValue) antennaPortConfig[antennaSelect][0] = bytes[0];
                if (DEBUG) appendToLog("2C setAntennaEnable: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            if (DEBUG) appendToLog("2d getAntennaEnable: bValue = " + bValue);
            return bValue;
        }
        boolean setAntennaEnable(int antennaEnable, int antennaInventoryMode, int antennaLocalAlgo, int antennaLocalStartQ,
                                        int antennaProfileMode, int antennaLocalProfile, int antennaFrequencyMode, int antennaLocalFrequency) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        int getAntennaInventoryMode() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaInventoryMode();
            }
        }
        boolean setAntennaInventoryMode(int antennaInventoryMode) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaInventoryMode(antennaInventoryMode);
        }

        int getAntennaLocalAlgo() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalAlgo();
            }
        }
        boolean setAntennaLocalAlgo(int antennaLocalAlgo) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalAlgo(antennaLocalAlgo);
        }

        int getAntennaLocalStartQ() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalStartQ();
            }
        }
        boolean setAntennaLocalStartQ(int antennaLocalStartQ) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalStartQ(antennaLocalStartQ);
        }

        int getAntennaProfileMode() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaProfileMode();
            }
        }
        boolean setAntennaProfileMode(int antennaProfileMode) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaProfileMode(antennaProfileMode);
        }

        int getAntennaLocalProfile() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalProfile();
            }
        }
        boolean setAntennaLocalProfile(int antennaLocalProfile) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalProfile(antennaLocalProfile);
        }

        int getAntennaFrequencyMode() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaFrequencyMode();
            }
        }
        boolean setAntennaFrequencyMode(int antennaFrequencyMode) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaFrequencyMode(antennaFrequencyMode);
        }

        int getAntennaLocalFrequency() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaLocalFrequency();
            }
        }
        boolean setAntennaLocalFrequency(int antennaLocalFrequency) {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX)  { antennaSelect = mDefault.antennaSelect; appendToLog("antennaSelect is set to " + antennaSelect); }
            return antennaSelectedData[antennaSelect].setAntennaLocalFrequency(antennaLocalFrequency);
        }

        int getAntennaStatus() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaStatus();
            }
        }

        int getAntennaDefine() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaDefine();
            }
        }

        long getAntennaDwell() {
            boolean DEBUG = false;
            long lValue = -1;
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2 getAntennaDwell");
                if (DEBUG) appendToLog("2A getAntennaDwell: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                lValue = (antennaPortConfig[antennaSelect][1] & 0xFF) << 8;
                lValue += (antennaPortConfig[antennaSelect][2] & 0xFF);
            }
            if (DEBUG) appendToLog("2C getAntennaDwell: iValue = " + lValue);
            return lValue;
        }

        boolean setAntennaDwell(long antennaDwell) {
            boolean bValue = false, DEBUG = false;
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2 setAntennaDwell: antennaDwell = " + antennaDwell);
                if (DEBUG) appendToLog("2A setAntennaDwell: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[2];
                bytes[0] = (byte)((antennaDwell/256) & 0xFF);
                bytes[1] = (byte)((antennaDwell%256) & 0xFF);
                if (sameCheck | true) {
                    byte[] bytesOld = new byte[2];
                    System.arraycopy(antennaPortConfig[antennaSelect], 1, bytesOld, 0, bytesOld.length);
                    if (DEBUG) appendToLog("2A2 setAntennaDwell: bytesOld = " + byteArrayToString(bytesOld));
                    boolean bValue1 = compareArray(bytes, bytesOld, bytes.length);
                    if (DEBUG) appendToLog("2ab setAntennaDwell: the array is the same = " + bValue1);
                }
                bValue = writeMAC(0x3030 + antennaSelect * 16 + 1, bytes, true);
                if (bValue) System.arraycopy(bytes, 0, antennaPortConfig[antennaSelect], 1, bytes.length);
                if (DEBUG) appendToLog("2b setAntennaDwell: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }

        long getAntennaPower(int portNumber) {
            long lValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("2 getAntennaPower: portNumber = " + portNumber);
            if (portNumber < 0) portNumber = antennaSelect;
            if (antennaPortConfig[portNumber] == null) appendToLog("CANNOT continue as antennaPortConfig[" + portNumber + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2A getAntennaPower: getAntennaPortConfig[" + portNumber + "] = " + byteArrayToString(antennaPortConfig[portNumber]));
                lValue = (antennaPortConfig[portNumber][3] & 0xFF) * 256;
                lValue += (antennaPortConfig[portNumber][4] & 0xFF);
                if (DEBUG) appendToLog(String.format("2b getAntennaPower: lValue = 0x%X", lValue));
                lValue /= 10;
            }
            return lValue;
        }
        boolean setAntennaPower(long antennaPower) {
            boolean bValue = false, DEBUG = false;
            if (antennaPortConfig[antennaSelect] == null) {
                if (DEBUG) appendToLog("4 bValue = " + bValue);
                appendToLog("!!! CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null");
            }
            else if (getAntennaPower(antennaSelect) == antennaPower && sameCheck) {
                if (DEBUG) appendToLog("3 bValue = " + bValue);
                return true;
            }
            else {
                if (DEBUG) appendToLog("2 setAntennaPower: antennaPower = " + antennaPower);
                if (DEBUG) appendToLog("2A setAntennaPower: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                antennaPower *= 10;
                byte[] bytes = new byte[2];
                bytes[0] = (byte)((antennaPower/256) & 0xFF);
                bytes[1] = (byte)((antennaPower%256) & 0xFF);
                if (sameCheck | true) {
                    byte[] bytesOld = new byte[2];
                    System.arraycopy(antennaPortConfig[antennaSelect], 3, bytesOld, 0, bytesOld.length);
                    if (DEBUG) appendToLog("2A2 setAntennaPower: bytesOld = " + byteArrayToString(bytesOld));
                    boolean bValue1 = compareArray(bytes, bytesOld, bytes.length);
                    if (DEBUG) appendToLog("2ab setAntennaPower: the array is the same = " + bValue1);
                }

                bValue = writeMAC(0x3030 + antennaSelect * 16 + 3, bytes, true);
                if (DEBUG) appendToLog("2 bValue = " + bValue);
                if (bValue) System.arraycopy(bytes, 0, antennaPortConfig[antennaSelect], 3, bytes.length);
                if (DEBUG) appendToLog("2b setAntennaPower: with updated " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            if (DEBUG) appendToLog("1 bValue = " + bValue);
            return bValue;
        }

        long antennaInvCount = -1;
        long getAntennaInvCount() {
            if (antennaSelect < ANTSLECT_MIN || antennaSelect > ANTSELECT_MAX) {
                return ANTSELECT_INVALID;
            } else {
                return antennaSelectedData[antennaSelect].getAntennaInvCount();
            }
        }
        boolean setAntennaInvCount(long antennaInvCount) {
            if (antennaInvCount == this.antennaInvCount) return true;
            this.antennaInvCount = antennaInvCount; appendToLog(String.format("!!! Skip setAntennaInvCount[0x%X]", antennaInvCount));
            return true;
        }

        //Tag select block parameters
        final int INVSELECT_INVALID = -1; final int INVSELECT_MIN = 0; final int INVSELECT_MAX = 7;
        int invSelectIndex = INVSELECT_INVALID;
        int getInvSelectIndex() {
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) {
                invSelectIndex = 0; appendToLog("!!! Skip getInvSelectIndex with assumed value = 0");
            }
            return invSelectIndex;
        }
        boolean setInvSelectIndex(int invSelect) {
            if (invSelect < INVSELECT_MIN || invSelect > INVSELECT_MAX) invSelect = mDefault.invSelectIndex;
            if (this.invSelectIndex == invSelect && sameCheck) {
                appendToLog("!!! Skip sending repeated data with invSelect = " + invSelect);
                return true;
            }
            this.invSelectIndex = invSelect;
            appendToLog(String.format("!!! Skip setInvSelectIndex[%d]", invSelect));
            return true;
        }

        InvSelectData[] invSelectData;
        int getSelectEnable() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT getSelectEnable as selectConfiguration[" + invSelectIndex + "] is null");
            else iValue = (byte)(selectConfiguration[invSelectIndex][0] & 0xFF);
            return iValue;
        }
        boolean setSelectEnable(int enable, int selectTarget, int selectAction, int selectDelay) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("Start with enable = " + enable + ", selectTarget = " + selectTarget + ", selectAction = " + selectAction + ", selectDelay = " + selectDelay);
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT setSelectEnable as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                if (DEBUG) appendToLog("Old selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[42];
                if (selectConfiguration[invSelectIndex] != null) System.arraycopy(selectConfiguration[invSelectIndex], 0, bytes, 0, bytes.length);
                bytes[0] = (byte)(enable & 0xFF);
                bytes[39] = (byte)(selectTarget & 0xFF);
                bytes[40] = (byte)(selectAction & 0xFF);
                bytes[41] = (byte)(selectDelay & 0xFF);;
                if (compareArray(selectConfiguration[invSelectIndex], bytes, bytes.length) && sameCheck) {
                    appendToLog("!!! Skip sending repeated data " + byteArrayToString(bytes) + " to address 0x3140");
                    return true;
                }
                bValue = writeMAC(0x3140 + invSelectIndex * 42, bytes, true);
                if (DEBUG) appendToLog("after writeMAC 0x3140, bValue = " + bValue);
                if (bValue) selectConfiguration[invSelectIndex] = bytes;
                else appendToLog("!!! Failed to send data " + byteArrayToString(bytes) + " to address 0x3140");
                if (DEBUG) appendToLog("bytes = " + byteArrayToString(bytes) + ", new selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            if (DEBUG) appendToLog("End with bValue = " + bValue);
            return bValue;
        }

        int getSelectTarget() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT getSelectTarget as selectConfiguration[" + invSelectIndex + "] is null");
            else iValue = (selectConfiguration[invSelectIndex][39] & 0xFF);
            return iValue;
        }

        int getSelectAction() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT getSelectTarget as selectConfiguration[" + invSelectIndex + "] is null");
            else iValue = (selectConfiguration[invSelectIndex][40] & 0xFF);
            return iValue;
        }

        int getSelectMaskBank() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT getSelectMaskBank as selectConfiguration[" + invSelectIndex + "] is null");
            else iValue = (byte)(selectConfiguration[invSelectIndex][1] & 0xFF);
            return iValue;
        }
        boolean setSelectMaskBank(int selectMaskBank) {
            boolean bValue = false, DEBUG = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT setSelectMaskBank[" + selectMaskBank + "] as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                if (DEBUG) appendToLog("Old selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[1];
                bytes[0] = (byte)(selectMaskBank & 0xFF);
                if (false) { appendToLog("!!!! Skip 1A writeMAC 0x3141"); bValue = true; }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 1, bytes, true);
                if (bValue) selectConfiguration[invSelectIndex][1] = bytes[0];
                if (DEBUG) appendToLog("bytes = " + byteArrayToString(bytes) + ", new selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            if (DEBUG) appendToLog("1 setSelectMaskBank with selectMaskBank = " + selectMaskBank + " and bValue = " + bValue);
            return bValue;
        }

        int getSelectMaskOffset() {
            int iValue = -1;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT getSelectMaskBank as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                iValue = ((selectConfiguration[invSelectIndex][2] & 0xFF) << 24);
                iValue += ((selectConfiguration[invSelectIndex][3] & 0xFF) << 16);
                iValue += ((selectConfiguration[invSelectIndex][4] & 0xFF) << 8);
                iValue += (selectConfiguration[invSelectIndex][5] & 0xFF);
            }
            return iValue;
        }
        boolean setSelectMaskOffset(int selectMaskOffset) {
            boolean bValue = false, DEBUG = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT setSelectMaskBank[" + selectMaskOffset + "] as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                if (DEBUG) appendToLog("Old selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[4];
                bytes[0] = (byte)((selectMaskOffset >> 24) & 0xFF);
                bytes[1] = (byte)((selectMaskOffset >> 16) & 0xFF);
                bytes[2] = (byte)((selectMaskOffset >> 8) & 0xFF);
                bytes[3] = (byte)(selectMaskOffset & 0xFF);
                if (false) { appendToLog("!!!! Skip 1A writeMAC 0x3142"); bValue = true; }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 2, bytes, true);
                if (bValue) System.arraycopy(bytes, 0, selectConfiguration[invSelectIndex], 2, bytes.length);
                if (DEBUG) appendToLog("bytes = " + byteArrayToString(bytes) + ", new selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            if (DEBUG) appendToLog("1 setSelectMaskOffset with selectMaskOffset = " + selectMaskOffset + " and bValue = " + bValue);
            return bValue;
        }

        int getSelectMaskLength() {
            int dataIndex = invSelectIndex, iValue = INVSELECT_INVALID; boolean DEBUG = false;
            if (dataIndex >= INVSELECT_MIN && dataIndex <= INVSELECT_MAX) {
                if (DEBUG) appendToLog("selectConfiguration " + dataIndex + " = " + byteArrayToString(selectConfiguration[dataIndex]));
                if (selectConfiguration[dataIndex] != null) {
                    iValue = selectConfiguration[dataIndex][6];
                }
            }
            if (DEBUG) appendToLog("iValue = " + iValue);
            return iValue;
        }
        boolean setSelectMaskLength(int selectMaskLength) {
            boolean bValue = false, DEBUG = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT setSelectMaskLength[" + selectMaskLength + "] as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                if (DEBUG) appendToLog("Old selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[1];
                bytes[0] = (byte)(selectMaskLength & 0xFF);
                if (false) { appendToLog("!!!! Skip 1A writeMAC 0x3146"); bValue = true; }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 6, bytes, true);
                if (bValue) selectConfiguration[invSelectIndex][6] = bytes[0];
                if (DEBUG) appendToLog("bytes = " + byteArrayToString(bytes) + ", new selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            if (DEBUG) appendToLog("1 setSelectMaskOffset with setSelectMaskLength = " + selectMaskLength + " and bValue = " + bValue);
            return bValue;
        }

        String getSelectMaskData() {
            String strValue = null; boolean DEBUG = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT getSelectMaskBank as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                if (selectConfiguration[invSelectIndex] != null && selectConfiguration[invSelectIndex].length > 39) {
                    if (DEBUG || true) appendToLog("selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
                    byte[] bytes = new byte[32];
                    System.arraycopy(selectConfiguration[invSelectIndex], 7, bytes, 0, bytes.length);
                    if (DEBUG || true) appendToLog("bytes = " + byteArrayToString(bytes));
                    for (int i = 0; i < bytes.length; i++) {
                        String string = String.format("%02X", bytes[i]);
                        if (strValue == null) strValue = string;
                        else strValue += string;
                        if (DEBUG) appendToLog("i = " + i + ", strValue = " + strValue);
                    }
                    if (strValue == null) strValue = "";
                }
            }
            return strValue;
        }
        boolean setSelectMaskData(String maskData) {
            boolean bValue = false, DEBUG = false;
            if (invSelectIndex < INVSELECT_MIN || invSelectIndex > INVSELECT_MAX) invSelectIndex = mDefault.invSelectIndex;
            if (selectConfiguration[invSelectIndex] == null) appendToLog("!!! CANNOT setSelectMaskData[" + maskData + "] as selectConfiguration[" + invSelectIndex + "] is null");
            else {
                if (DEBUG) appendToLog("Old selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
                byte[] bytes = new byte[maskData.length() / 2 + maskData.length() % 2];
                for (int i = 0; i < bytes.length; i++) {
                    boolean bSingle = false;
                    if (i * 2 + 2 > maskData.length()) bSingle = true;
                    if (DEBUG) appendToLog("substring i = " + i + " " + maskData.substring(i * 2, (bSingle ? i * 2 +1 : i * 2 + 2)));
                    try {
                        String stringSub = null;
                        if (bSingle) {
                            stringSub = maskData.substring(i * 2, i * 2 + 1) + "0";
                        } else stringSub = maskData.substring(i * 2, i * 2 + 2);
                        int iValue = Integer.parseInt(stringSub, 16);
                        bytes[i] = (byte) (iValue & 0xFF);
                    } catch (Exception ex) {
                        appendToLog("!!! Error in parsing maskdata " + maskData + " when i = " + i);
                    }
                }
                if (false) { appendToLog("!!!! Skip 1A writeMAC 0x3147"); bValue = true; }
                else bValue = writeMAC(0x3140 + invSelectIndex * 42 + 7, bytes, true);
                if (bValue) System.arraycopy(bytes, 0, selectConfiguration[invSelectIndex], 7, bytes.length);
                if (DEBUG) appendToLog("bytes = " + byteArrayToString(bytes) + ", new selectConfiguration " + invSelectIndex + " = " + byteArrayToString(selectConfiguration[invSelectIndex]));
            }
            if (DEBUG) appendToLog("1 setSelectMaskData with maskData = " + maskData + " and bValue = " + bValue);
            return bValue;

        }

        //Inventtory block paraameters
        final int QUERYTARGET_INVALID = -1; final int QUERYTARGET_MIN = 0; final int QUERYTARGET_MAX = 1;
        //int queryTarget = QUERYTARGET_INVALID;
        int getQueryTarget() {
            int iValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("2 getQueryTarget: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null)
                appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG)
                    appendToLog("2A getQueryTarget: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                if (antennaPortConfig[antennaSelect][13] != 0) iValue = 2;
                else if ((antennaPortConfig[antennaSelect][6] & 0x80) != 0) iValue = 1;
                else iValue = 0;
                if (DEBUG) appendToLog(String.format("2b getQueryTarget: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        boolean setQueryTarget(int queryTarget) {
            appendToLog("!!! Skip setQueryTarget");
            return true; //setQueryTarget(queryTarget, querySession, querySelect);
        }
        boolean setQueryTarget(int queryTarget, int querySession, int querySelect) {
            boolean bValue = false, DEBUG = false;
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else { // queryTarget = 0;
                if (DEBUG) appendToLog("2 setQueryConfig: queryTarget = " + queryTarget + ", querySession = " + querySession + ", querySelect = " + querySelect);
                if (DEBUG) appendToLog("2A setQueryConfig: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[9];
                System.arraycopy(antennaPortConfig[antennaSelect], 5, bytes, 0, bytes.length);

                int iValue;
                if (querySession >= 0) {
                    iValue = querySession & 0x03;
                    bytes[1] &= ~0x18; bytes[1] |= (iValue << 3);
                }
                if (querySelect >= 0) {
                    iValue = querySelect & 0x03;
                    bytes[1] &= ~0x60; bytes[1] |= (iValue << 5);
                }
                if (DEBUG) appendToLog("2b setQueryConfig: queryTarget = " + queryTarget);
                if (queryTarget >= 0) {
                    iValue = queryTarget & 0x01;
                    bytes[1] &= ~0x80; bytes[1] |= (iValue << 7);
                    if (queryTarget >= 2) bytes[8] = 1;
                    else bytes[8] = 0;
                }
                if (DEBUG) appendToLog("2C setQueryConfig: bytes = " + byteArrayToString(bytes));
                boolean bSame = false;
                if (sameCheck | true) {
                    byte[] bytesOld = new byte[9];
                    System.arraycopy(antennaPortConfig[antennaSelect], 5, bytesOld, 0, bytes.length);
                    if (DEBUG) appendToLog("2d setQueryConfig: bytesOld = " + byteArrayToString(bytesOld));
                    bSame = compareArray(bytes, bytesOld, bytes.length);
                    if (DEBUG) appendToLog("2E setQueryConfig: the array is the same = " + bSame);
                }
                //bSame = false; appendToLog("!!! assme bSame is false before 1b writeMAC 0x3035");
                if (bSame) {
                    if (DEBUG_PKDATA) appendToLog(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes),  0x3030 + antennaSelect * 16 + 5));
                    bValue = true;
                } else {
                    appendToLog("test 2");
                    bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3030 + antennaSelect * 16 + 5, bytes, true);
                    if (bValue)
                        System.arraycopy(bytes, 0, antennaPortConfig[antennaSelect], 5, bytes.length);
                }
                if (DEBUG) appendToLog("2F setQueryConfig: with updated " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }
        final int QUERYSESSION_INVALID = -1; final int QUERYSESSION_MIN = 0; final int QUERYSESSION_MAX = 3;
        //int querySession = QUERYSESSION_INVALID;
        int getQuerySession() {
            int iValue = -1; boolean DEBUG = true;
            if (DEBUG) appendToLog("2 getQuerySession: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2A getQuerySession: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = (antennaPortConfig[antennaSelect][6] & 0x18) >> 3;
                if (DEBUG) appendToLog(String.format("2b getQuerySession: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        boolean setQuerySession(int querySession) {
            appendToLog("!!! Skip setQuerySession");
            return true; //setQueryTarget(queryTarget, querySession, querySelect);
        }

        final int QUERYSELECT_INVALID = -1; final int QUERYSELECT_MIN = 0; final int QUERYSELECT_MAX = 3;
        //int querySelect = QUERYSELECT_INVALID;
        int getQuerySelect() {
            int iValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("2 getQuerySession: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2A getQuerySession: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = (antennaPortConfig[antennaSelect][6] & 0x60) >> 5;
                if (DEBUG) appendToLog(String.format("2b getQuerySession: iValue = 0x%X", iValue));
            }
            return iValue;

        }
        boolean setQuerySelect(int querySelect) {
            boolean bValue = false, DEBUG = false;
            for (int antennaSelect = 0; antennaSelect < 16; antennaSelect++) {
                if (antennaPortConfig[antennaSelect] == null)
                    appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
                else { // queryTarget = 0;
                    if (DEBUG) appendToLog("2 setQuerySelect: querySelect = " + querySelect);
                    if (DEBUG)
                        appendToLog("2A setQuerySelect: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                    byte[] bytes = new byte[1];
                    bytes[0] = antennaPortConfig[antennaSelect][6];

                    int iValue;
                    if (querySelect >= 0) {
                        iValue = querySelect & 0x03;
                        bytes[0] &= ~0x60;
                        bytes[0] |= (iValue << 5);
                    }
                    if (DEBUG)
                        appendToLog("2C setQueryConfig: bytes = " + byteArrayToString(bytes));
                    boolean bSame = false;
                    if (sameCheck) {
                        if (bytes[0] == antennaPortConfig[antennaSelect][6]) bSame = true;
                        if (DEBUG)
                            appendToLog("2E setQueryConfig: the array is the same = " + bSame);
                    }
                    if (bSame) {
                        if (DEBUG_PKDATA)
                            appendToLog(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes), 0x3030 + antennaSelect * 16 + 5));
                        bValue = true;
                    } else {
                        bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3030 + antennaSelect * 16 + 6, bytes, true);
                        if (bValue) antennaPortConfig[antennaSelect][6] = bytes[0];
                    }
                    if (DEBUG)
                        appendToLog("2F setQueryConfig: with updated " + byteArrayToString(antennaPortConfig[antennaSelect]));
                    if (bValue == false) break;
                }
            }
            return bValue;
        }

        private boolean getHST_QUERY_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_QUERY_CFG, false, msgBuffer);
        }

        final int INVALGO_INVALID = -1; final int INVALGO_MIN = 0; final int INVALGO_MAX = 3;
        int invAlgo = INVALGO_INVALID;
        int getInvAlgo() {
            int iValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("3 getInvAlgo: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("3A getInvAlgo: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = antennaPortConfig[antennaSelect][6];
                if (DEBUG) appendToLog(String.format("3b getInvAlgo: iValue = 0x%X", iValue));
                iValue &= 0x01; //iValue |= 0x01;
                if (DEBUG) appendToLog(String.format("3c getInvAlgo: changed iValue = 0x%X", iValue));
                if (iValue == 0) iValue = 3;
                else iValue = 0;
                if (DEBUG) appendToLog(String.format("3d getInvAlgo: after adjustement, iValue = 0x%X", iValue));
            }
            return iValue;
        }
        boolean setInvAlgo(int invAlgo) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("3 setInvAlgo[" + invAlgo + "]");
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                byte[] bytes = new byte[1];
                bytes[0] = (byte)(antennaPortConfig[antennaSelect][6] & ~0x01);
                if (invAlgo == 0) bytes[0] |= 0x01;
                if (DEBUG) appendToLog("3 setInvAlgo: bytes = " + byteArrayToString(bytes));
                bValue = writeMAC(0x3036 + this.antennaSelect * 16, bytes, true);
                if (bValue) antennaPortConfig[antennaSelect][6] = bytes[0];
            }
            if (DEBUG) appendToLog("3A setInvAlgo with bValue = " + bValue);
            return bValue;
        }

        final int MATCHREP_INVALID = -1; final int MATCHREP_MIN = 0; final int MATCHREP_MAX = 255;
        int matchRep = MATCHREP_INVALID;
        int getMatchRep() {
            if (matchRep < MATCHREP_MIN || matchRep > MATCHREP_MAX) getHST_INV_CFG();
            return matchRep;
        }
        boolean setMatchRep(int matchRep) {
            if (matchRep == this.matchRep) return true;
            if (this.matchRep == matchRep && sameCheck) {
                appendToLog("Skip sending repeated data with matchRep = " + matchRep);
                return true;
            }
            this.matchRep = matchRep; appendToLog(String.format("!!! Skip setMatchRep[0x%X]", matchRep));
            return true;
        }

        final int TAGSELECT_INVALID = -1; final int TAGSELECT_MIN = 0; final int TAGSELECT_MAX = 1;
        int tagSelect = TAGSELECT_INVALID;
        int getTagSelect() {
            if (tagSelect < TAGSELECT_MIN || tagSelect > TAGSELECT_MAX) getHST_INV_CFG();
            return tagSelect;
        }
        boolean setTagSelect(int tagSelect) {
            if (tagSelect == this.tagSelect) return true;
            this.tagSelect = tagSelect; appendToLog(String.format("!!! Skip setTagSelect[%d]", tagSelect));
            return true;
        }

        final int NOINVENTORY_INVALID = -1; final int NOINVENTORY_MIN = 0; final int NOINVENTORY_MAX = 1;
        int noInventory = NOINVENTORY_INVALID;
        int getNoInventory() {
            if (noInventory < NOINVENTORY_MIN || noInventory > NOINVENTORY_MAX) getHST_INV_CFG();
            return noInventory;
        }
        boolean setNoInventory(int noInventory) {
            appendToLog("1b setInvAlgo");
            return setInvAlgo(invAlgo, matchRep, tagSelect, noInventory, tagRead, tagDelay, invModeCompact, invBrandId);
        }

        final int TAGREAD_INVALID = -1; final int TAGREAD_MIN = 0; final int TAGREAD_MAX = 2;
        int tagRead = TAGREAD_INVALID;
        int getTagRead() {
            int iValue = 0;
            if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null)
                appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1] == null)
                appendToLog("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][0] != 0) {
                iValue++;
                if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][0] != 0)
                    iValue++;
            }
            return iValue;
        }
        boolean setTagRead(int tagRead) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("0 setTagRead with tagRead = " + tagRead);
            if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null)
                appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1] == null)
                appendToLog("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                if (DEBUG)
                    appendToLog("0 multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                if ((tagRead == 0 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][0] != 0)
                        || (tagRead != 0 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][0] == 0)) {
                    byte[] bytes = new byte[1];
                    if (tagRead != 0) bytes[0] = 1;
                    else bytes[0] = 0;
                    bValue = writeMAC(0x3270 + 7 * 0, bytes, true);
                    if (bValue)
                        mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][0] = bytes[0];
                    if (DEBUG)
                        appendToLog("0A multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]) + ", with bValue = " + bValue);
                } else bValue = true;
                if (DEBUG)
                    appendToLog("0 multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                if (bValue && ((tagRead < 2 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][0] != 0)
                        || (tagRead >= 2 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][0] == 0))) {
                    byte[] bytes = new byte[1];
                    if (tagRead >= 2) bytes[0] = 1;
                    else bytes[0] = 0;
                    bValue = writeMAC(0x3270 + 7 * 1, bytes, true);
                    if (bValue)
                        mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][0] = bytes[0];
                    if (DEBUG)
                        appendToLog("0A multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }

        final int TAGDELAY_INVALID = -1; final int TAGDELAY_MIN = 0; final int TAGDELAY_MAX = 63;
        int tagDelay = TAGDELAY_INVALID;
        int getTagDelay() {
            if (tagDelay < TAGDELAY_MIN || tagDelay > TAGDELAY_MAX) getHST_INV_CFG();
            return tagDelay;
        }
        boolean setTagDelay(int tagDelay) {
            if (tagDelay == this.tagDelay) return true;
            if (this.tagDelay == tagDelay && sameCheck) {
                appendToLog("!!! Skip sending repeated data with tagDelay = " + tagDelay);
                return true;
            }
            this.tagDelay = tagDelay; appendToLog(String.format("!!! Skip setTagDelay[%d]", tagDelay));
            return true;
        }

        final int DUPELIM_INVALID = -1; final int DUPELIM_MIN = 0; final int DUPELIM_MAX = 63;
        byte[] dupElimRollWindow = null;
        byte getDupElimRollWindow() {
            if (dupElimRollWindow != null && dupElimRollWindow.length == 1) return dupElimRollWindow[0];
            readMAC(0x3900, 1);
            return ((byte)-1);
        }
        boolean setDupElimRollWindow(byte dupElimDelay) {
            if (dupElimRollWindow != null && dupElimRollWindow.length == 1 && dupElimRollWindow[0] == dupElimDelay && sameCheck) {
                appendToLog("!!! Skip sending repeated data with dupElimDelay = " + dupElimDelay);
                return true;
            }
            byte[] bytes = new byte[1];
            bytes[0] = dupElimDelay;
            boolean bValue;
            bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3900, bytes, true);
            if (bValue) dupElimRollWindow = bytes;
            return true;
        }
        Date keepAliveTime;
        Date inventoryRoundEndTime;
        int crcErrorRate;
        int tagRate = -1;
        int getTagRate() {
            int iValue = tagRate;
            tagRate = -1;
            return iValue;
        }
        byte[] eventPacketUplnkEnable = null;
        int getEventPacketUplinkEnable() {
            if (eventPacketUplnkEnable != null && eventPacketUplnkEnable.length == 2) {
                int iValue = ((eventPacketUplnkEnable[0] & 0xFF) << 8) + (eventPacketUplnkEnable[1] & 0xFF);
                appendToLog("eventPacketUplnkEnable iValue = " + iValue);
                return iValue;
            }
            readMAC(0x3906, 2);
            return -1;
        }

        boolean setEventPacketUplinkEnable(byte byteEventPacketUplinkEnable) {
            if (eventPacketUplnkEnable != null && eventPacketUplnkEnable.length == 2 && eventPacketUplnkEnable[1] == byteEventPacketUplinkEnable && sameCheck) {
                appendToLog("!!! Skip sending repeated data with byteEventPacketUplinkEnable = " + byteEventPacketUplinkEnable);
                return true;
            }
            byte[] bytes = new byte[2];
            bytes[1] = byteEventPacketUplinkEnable;
            boolean bValue;
            bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3906, bytes, true);
            if (bValue) eventPacketUplnkEnable = bytes;
            return true;
        }

        byte[] intraPacketDelay = null;
        byte getIntraPacketDelay() {
            if (intraPacketDelay != null && intraPacketDelay.length == 1) return intraPacketDelay[0];
            readMAC(0x3908, 1);
            return ((byte)-1);
        }
        boolean setIntraPacketDelay(byte intraPkDelay) {
            if (intraPkDelay >= 0 && intraPacketDelay.length == 1 && intraPacketDelay[0] == intraPkDelay && sameCheck) {
                appendToLog("!!! Skip sending repeated data with intraPkDelay = " + intraPkDelay);
                return true;
            }
            byte[] bytes = new byte[1];
            bytes[0] = intraPkDelay;
            boolean bValue;
            bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3908, bytes, true);
            if (bValue) intraPacketDelay = bytes;
            return true;
        }
        long cycleDelay = -1;
        long getCycleDelay() {
            return cycleDelay;
        }
        boolean setCycleDelay(long cycleDelay) {
            if (cycleDelay == this.cycleDelay) return true;
            if (this.cycleDelay == cycleDelay && sameCheck) {
                appendToLog("!!! Skip sending repeated data with cycleDelay = " + cycleDelay);
                return true;
            }
            this.cycleDelay = cycleDelay; appendToLog(String.format("!!! Skip setCycleDelay[%d]", cycleDelay));
            return true;
        }

        final int AUTHENTICATE_CFG_INVALID = -1; final int AUTHENTICATE_CFG_MIN = 0; final int AUTHENTICATE_CFG_MAX = 4095;
        boolean authenticateSendReply;
        boolean authenticateIncReplyLength;
        int authenticateLength = AUTHENTICATE_CFG_INVALID;
        int getAuthenticateReplyLength() {
            if (authenticateLength < AUTHENTICATE_CFG_MIN || authenticateLength > AUTHENTICATE_CFG_MAX) getHST_AUTHENTICATE_CFG();
            return authenticateLength;
        }
        private boolean getHST_AUTHENTICATE_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0, (byte) 0x0F, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_CFG, false, msgBuffer);
        }
        boolean setHST_AUTHENTICATE_CFG(boolean sendReply, boolean incReplyLenth, int csi, int length) {
            appendToLog("sendReply = " + sendReply + ", incReplyLenth = " + incReplyLenth + ", length = " + length);
            if (length < 0 || length > 0x3FF) return false;

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0, (byte) 0x0F, 0, 0, 0, 0};
            if (sendReply) msgBuffer[4] |= 0x01; authenticateSendReply = sendReply;
            if (incReplyLenth) msgBuffer[4] |= 0x02; authenticateIncReplyLength = incReplyLenth;
            msgBuffer[4] |= ((csi & 0x3F) << 2);
            msgBuffer[5] |= ((csi >> 6) & 0x03);
            msgBuffer[5] |= ((length & 0x3F) << 2);
            msgBuffer[6] |= ((length & 0xFC0) >> 6); authenticateLength = length;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_CFG, true, msgBuffer);
        }

        byte[] authMatchData0_63; int authMatchDataReady = 0;
        String getAuthMatchData() {
            int length = 96;
            String strValue = "";
            for (int i = 0; i < 3; i++) {
                if (length > 0) {
                    appendToLog("i = " + i + ", authMatchDataReady = " + authMatchDataReady);
                    if ((authMatchDataReady & (0x01 << i)) == 0) {
                        byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, (byte)0x0F, 0, 0, 0, 0};
                        msgBuffer[2] += i;
                        mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_MSG, false, msgBuffer);
                    } else {
                        for (int j = 0; j < 4; j++) {
                            strValue += String.format("%02X", authMatchData0_63[i * 4 + j]);
                        }
                    }
                    length -= 32;
                }
            }
            if (strValue.length() < 16) strValue = null;
            return strValue;
        }
        boolean setAuthMatchData(String matchData) {
            int length = matchData.length();
            for (int i = 0; i < 6; i++) {
                if (length > 0) {
                    length -= 8;

                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, (byte)0x0F, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < 8; j++) {
                        if (i * 8 + j + 1 <= matchData.length()) {
                            String subString = matchData.substring(i * 8 + j, i * 8 + j + 1).toUpperCase();
                            int k = 0;
                            for (k = 0; k < 16; k++) {
                                if (subString.matches(hexString.substring(k, k + 1))) {
                                    break;
                                }
                            }
                            if (k == 16) return false;
                            if ((j / 2) * 2 == j) {
                                msgBuffer[7 - j / 2] |= (byte) (k << 4);
                            } else {
                                msgBuffer[7 - j / 2] |= (byte) (k);
                            }
                        }
                    }
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + i);
                    if (mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_AUTHENTICATE_MSG, true, msgBuffer) == false)
                        return false;
                    else {
                        //authMatchDataReady |= (0x01 << i);
                        System.arraycopy(msgBuffer, 4, authMatchData0_63, i * 4, 4); //appendToLog("Data=" + byteArrayToString(mRx000Setting.invMatchData0_63));
//                        appendToLog("invMatchDataReady=" + Integer.toString(mRx000Setting.invMatchDataReady, 16) + ", message=" + byteArrayToString(msgBuffer));
                    }
                }
            }
            return true;
        }

        final int UNTRACEABLE_CFG_INVALID = -1; final int UNTRACEABLE_CFG_MIN = 0; final int UNTRACEABLE_CFG_MAX = 3;
        int untraceableRange = UNTRACEABLE_CFG_INVALID;
        boolean untraceableUser;
        int untraceableTid = UNTRACEABLE_CFG_INVALID;
        int untraceableEpcLength = UNTRACEABLE_CFG_INVALID;
        boolean untraceableEpc;
        boolean untraceableUXpc;
        int getUntraceableEpcLength() {
            if (untraceableRange < UNTRACEABLE_CFG_MIN || untraceableRange > UNTRACEABLE_CFG_MAX) getHST_UNTRACEABLE_CFG();
            return untraceableEpcLength;
        }
        private boolean getHST_UNTRACEABLE_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, (byte) 0x0F, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG, false, msgBuffer);
        }
        boolean setHST_UNTRACEABLE_CFG(int range, boolean user, int tid, int epcLength, boolean epc, boolean uxpc) {
            appendToLog("range = " + range + ", user = " + user + ", tid = " + tid + ", epc = " + epc + ", epcLength = " + epcLength + ", xcpc = " + uxpc);
            if (range < 0 || range > 3) return false;
            if (tid < 0 || tid > 2) return false;
            if (epcLength < 0 || epcLength > 31) return false;

            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, (byte) 0x0F, 0, 0, 0, 0};
            msgBuffer[4] |= (range); untraceableRange = range;
            if (user) msgBuffer[4] |= 0x04; untraceableUser = user;
            msgBuffer[4] |= (tid << 3); untraceableTid = tid;
            msgBuffer[4] |= ((epcLength & 0x7) << 5);
            msgBuffer[5] |= ((epcLength & 0x18) >> 3); untraceableEpcLength = epcLength;
            if (epc) msgBuffer[5] |= 0x04; untraceableEpc = epc;
            if (uxpc) msgBuffer[5] |= 0x08; untraceableUXpc = uxpc;
            appendToLog("going to do sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG,");
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_UNTRACEABLE_CFG, true, msgBuffer);
        }

        final int TAGJOIN_INVALID = -1; final int TAGJOIN_MIN = 0; final int TAGJOIN_MAX = 1;
        int invModeCompact = TAGJOIN_INVALID;
        boolean getInvModeCompact() {
            if (invModeCompact < TAGDELAY_MIN || invModeCompact > TAGDELAY_MAX) { getHST_INV_CFG(); return false; }
            return (invModeCompact == 1 ? true : false);
        }
        boolean setInvModeCompact(boolean bInvModeCompact) {
            int invModeCompact = (bInvModeCompact ? 1 : 0);
            if (invModeCompact == this.invModeCompact && sameCheck) {
                appendToLog("!!! Skip sending repeated data with bInvModeCompact = " + bInvModeCompact);
                return true;
            }
            this.invModeCompact = invModeCompact; appendToLog(String.format("!!! Skip setInvModeCompact[%s]", (bInvModeCompact ? "true" : "false")));
            return true;
        }

        final int BRAND_INVALID = -1; final int BRANDID_MIN = 0; final int BRANDID_MAX = 1;
        int invBrandId = BRAND_INVALID;
        boolean getInvBrandId() {
            if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) { getHST_INV_CFG(); return false; }
            return (invModeCompact == 1 ? true : false);
        }
        boolean setInvBrandId(boolean invBrandId) {
            if (invBrandId == getInvBrandId()) return true;
            this.invBrandId = (invBrandId ? 1 : 0); appendToLog("!!! Skip setInvBrandId[" + invBrandId + "]");
            return true;
        }

        private boolean getHST_INV_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_CFG, false, msgBuffer);
        }
        boolean setInvAlgo(int invAlgo, int matchRep, int tagSelect, int noInventory, int tagRead, int tagDelay, int invModeCompact, int invBrandId) {
            appendToLog("0 setInvAlgo with invAlgo = " + invAlgo + ", matchRep = " + matchRep + ", tagSelect = " + tagSelect
                    + ", noInventory = " + noInventory + ", tagRead = " + tagRead + ", tagDelay = " + tagDelay + ", invModeCompact = " + invModeCompact + ", invBrandId = " + invBrandId);


            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 9, 0, 0, 0, 0};
            if (invAlgo < INVALGO_MIN || invAlgo > INVALGO_MAX) invAlgo = mDefault.invAlgo;
            if (matchRep < MATCHREP_MIN || matchRep > MATCHREP_MAX) matchRep = mDefault.matchRep;
            if (tagSelect < TAGSELECT_MIN || tagSelect > TAGSELECT_MAX) tagSelect = mDefault.tagSelect;
            if (noInventory < NOINVENTORY_MIN || noInventory > NOINVENTORY_MAX) noInventory = mDefault.noInventory;
            if (tagDelay < TAGDELAY_MIN || tagDelay > TAGDELAY_MAX) tagDelay = mDefault.tagDelay;
            if (invModeCompact < TAGJOIN_MIN || invModeCompact > TAGJOIN_MAX) invModeCompact = mDefault.tagJoin;
            if (invBrandId < BRANDID_MIN || invBrandId > BRANDID_MAX) invBrandId = mDefault.brandid;
            if (tagRead < TAGREAD_MIN || tagRead > TAGREAD_MAX) tagRead = mDefault.tagRead;
            if (DEBUG) appendToLog("Old invAlgo = " + this.invAlgo + ", matchRep = " + this.matchRep + ", tagSelect =" + this.tagSelect + ", noInventory = " + this.noInventory + ", tagRead = " + this.tagRead + ", tagDelay = " + this.tagDelay + ", invModeCompact = " + this.invModeCompact + ", invBrandId = " + this.invBrandId);
            if (DEBUG) appendToLog("New invAlgo = " + invAlgo + ", matchRep = " + matchRep + ", tagSelect =" + tagSelect + ", noInventory = " + noInventory + ", tagRead = " + tagRead + ", tagDelay = " + tagDelay + ", invModeCompact = " + invModeCompact + ", invBrandId = " + invBrandId + ", sameCheck = " + sameCheck);
            if (this.invAlgo == invAlgo && this.matchRep == matchRep && this.tagSelect == tagSelect && this.noInventory == noInventory && this.tagRead == tagRead && this.tagDelay == tagDelay && this.invModeCompact == invModeCompact && this.invBrandId == invBrandId && sameCheck) return true;
            if (DEBUG) appendToLog("There is difference");
            msgBuffer[4] |= invAlgo;
            msgBuffer[4] |= (byte) ((matchRep & 0x03) << 6);
            msgBuffer[5] |= (byte) (matchRep >> 2);
            if (tagSelect != 0) {
                msgBuffer[5] |= 0x40;
            }
            if (noInventory != 0) {
                msgBuffer[5] |= 0x80;
            }
            if ((tagRead & 0x03) != 0) {
                msgBuffer[6] |= (tagRead & 0x03);
            }
            if ((tagDelay & 0x0F) != 0) {
                msgBuffer[6] |= ((tagDelay & 0x0F) << 4);
            }
            if ((tagDelay & 0x30) != 0) {
                msgBuffer[7] |= ((tagDelay & 0x30) >> 4);
            }
            if (invModeCompact == 1) {
                msgBuffer[7] |= 0x04;
            }
            if (invBrandId == 1) {
                msgBuffer[7] |= 0x08;
            }
            this.invAlgo = invAlgo;
            this.matchRep = matchRep;
            this.tagSelect = tagSelect;
            this.noInventory = noInventory;
            this.tagRead = tagRead;
            this.tagDelay = tagDelay;
            this.invModeCompact = invModeCompact;
            this.invBrandId = invBrandId;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_CFG, true, msgBuffer);
        }

        final int ALGOSELECT_INVALID = -1; final int ALGOSELECT_MIN = 0; final int ALGOSELECT_MAX = 3;   //DataSheet says Max=1
        int algoSelect = ALGOSELECT_INVALID;
        int getAlgoSelect() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 9, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_SEL, false, msgBuffer);
            }
            return algoSelect;
        }
        boolean dummyAlgoSelected = false;
        boolean setAlgoSelect(int algoSelect) {
            boolean bValue = false, DEBUG = false;
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else { //algoSelect = 0;
                if (DEBUG) appendToLog("2 setAlgoSelect: algoSelect = " + algoSelect);
                if (DEBUG) appendToLog("2A setAlgoSelect: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                bytes[0] = antennaPortConfig[antennaSelect][6];
                if (algoSelect >= 3) bytes[0] &= ~0x01;
                else bytes[0] |= 0x01;
                if (DEBUG) appendToLog(String.format("2A1 setAlgoSelect: bytes = 0x%X with sameCheck = ", bytes[0]) + sameCheck);
                boolean bSame = false;
                if (sameCheck) {
                    if (DEBUG) appendToLog(String.format("2A2 setAlgoSelect: bytesOld = 0x%X", antennaPortConfig[antennaSelect][6]));
                    if (antennaPortConfig[antennaSelect][6] == bytes[0]) bSame = true;
                    if (DEBUG) appendToLog("2ab setAlgoSelect: the array is the same = " + bSame);
                }
                if (bSame) {
                    if (DEBUG_PKDATA) appendToLog(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes),  0x3030 + antennaSelect * 16 + 6));
                    bValue = true;
                } else {
                    bValue = mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x3030 + antennaSelect * 16 + 6, bytes, true);
                    if (bValue) antennaPortConfig[antennaSelect][6] = bytes[0];
                }
                if (DEBUG) appendToLog("2b setAlgoSelect: with updated array " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }
        AlgoSelectedData[] algoSelectedData;
        int getAlgoStartQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoStartQ(false);
            }
        }
        int getAlgoStartQ() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoStartQ(true);
            }
        }
        boolean setAlgoStartQ(int algoStartQ) {
            boolean bValue = false, DEBUG = false;
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else { //algoStartQ = 6;
                if (DEBUG) appendToLog("2 setAlgoStartQ: algoStartQ = " + algoStartQ);
                if (DEBUG) appendToLog("2A setAlgoStartQ: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] bytes = new byte[1];
                bytes[0] = antennaPortConfig[antennaSelect][8];

                bytes[0] &= ~0x0F; bytes[0] |= (algoStartQ & 0x0F);
                if (DEBUG) appendToLog(String.format("2A1 setAlgoStartQ: bytes = 0x%X with sameCheck = ", bytes[0]) + sameCheck);
                boolean bSame = false;
                if (sameCheck) {
                    if (antennaPortConfig[antennaSelect][8] == bytes[0]) bSame = true;
                    if (DEBUG) appendToLog("2ab setAlgoStartQ: the array is the same = " + bSame);
                }
                if (bSame) {
                    if (DEBUG_PKDATA) appendToLog(String.format("!!! Skip sending repeated data %s in address 0x%X", byteArrayToString(bytes), 0x3030 + antennaSelect * 16 + 8));
                    bValue = true;
                } else {
                    bValue = writeMAC(0x3030 + antennaSelect * 16 + 8, bytes, true);
                    if (bValue) antennaPortConfig[antennaSelect][8] = bytes[0];
                }
                if (DEBUG) appendToLog("2b setAlgoStartQ: with updated " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }
        boolean setAlgoStartQ(int startQ, int algoMaxQ, int algoMinQ, int algoMaxRep, int algoHighThres, int algoLowThres) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoStartQ(startQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        int getAlgoMaxQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMaxQ();
            }
        }
        int getAlgoMaxQ() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMaxQ();
            }
        }
        boolean setAlgoMaxQ(int algoMaxQ) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoMaxQ(algoMaxQ);
        }

        int getAlgoMinQ(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMinQ();
            }
        }
        int getAlgoMinQ() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMinQ();
            }
        }
        boolean setAlgoMinQ(int algoMinQ) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoMinQ(algoMinQ);
        }

        int getAlgoMaxRep() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoMaxRep();
            }
        }
        boolean setAlgoMaxRep(int algoMaxRep) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoMaxRep(algoMaxRep);
        }

        int getAlgoHighThres() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoHighThres();
            }
        }
        boolean setAlgoHighThres(int algoHighThre) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoHighThres(algoHighThre);
        }

        int getAlgoLowThres() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoLowThres();
            }
        }
        boolean setAlgoLowThres(int algoLowThre) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            return algoSelectedData[algoSelect].setAlgoLowThres(algoLowThre);
        }

        final int ALGORETRY_INVALID = -1, ALGORETRY_MIN = 0, ALGORETRY_MAX = 255, ALGORETRY_DEFAULT = 1;
        int algoRetry = ALGORETRY_INVALID;
        int getAlgoRetry(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX) {
                algoRetry = ALGORETRY_DEFAULT; appendToLog(String.format("!!! Skip getAlgoRetry[%d] with assumed value = 0x%X", algoRetry, ALGORETRY_DEFAULT));
            }
            return algoRetry;
        }
        boolean setAlgoRetry(int algoRetry) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            else if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX) algoRetry = ALGORETRY_DEFAULT;
            if (this.algoRetry == algoRetry && sameCheck) {
                appendToLog("!!! Skip sending repeated data with algoRetry = " + algoRetry);
                return true;
            }
            this.algoRetry = algoRetry; appendToLog(String.format("!!! Skip setAlgoRetry[%d]", algoRetry));
            return true;
        }

        int getAlgoAbFlip(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoAbFlip();
            }
        }
        int getAlgoAbFlip() {
            int iValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("3 getAlgoAbFlip: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("3A getAlgoAbFlip: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                iValue = antennaPortConfig[antennaSelect][13];
                if (DEBUG) appendToLog(String.format("3b getAlgoAbFlip: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        boolean setAlgoAbFlip(int algoAbFlip) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("3 setAlgoAbFlip: iAntennaPort = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("3A setAlgoAbFlip: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                byte[] data = new byte[1];
                data[0] = (byte)(algoAbFlip & 0xFF);
                bValue = writeMAC(0x303d + this.antennaSelect * 16, data, true);
                if (bValue) antennaPortConfig[antennaSelect][13] = data[0];
                if (DEBUG) appendToLog("3b setAlgoAbFlip: bValue = " + bValue);
                if (DEBUG) appendToLog("3C setAlgoAbFlip: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
            }
            return bValue;
        }
        boolean setAlgoAbFlip(int algoAbFlip, int algoRunTilZero) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            appendToLog("algoSelect = " + algoSelect + ", algoAbFlip = " + algoAbFlip + ", algoRunTilZero = " + algoRunTilZero);
            return algoSelectedData[algoSelect].setAlgoAbFlip(algoAbFlip, algoRunTilZero);
        }

        int getAlgoRunTilZero(int algoSelect) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoRunTilZero();
            }
        }
        int getAlgoRunTilZero() {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) {
                return ALGOSELECT_INVALID;
            } else {
                return algoSelectedData[algoSelect].getAlgoRunTilZero();
            }
        }
        int algoRunTilZero = -1, ALGORUNTILZERO_MIN = 0, ALGORUNTILZERO_MAX = 1, ALGORUNTILZERO_DEFAULT = 0;
        boolean setAlgoRunTilZero(int algoRunTilZero) {
            if (algoSelect < ALGOSELECT_MIN || algoSelect > ALGOSELECT_MAX) return false;
            else if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX) algoRunTilZero = ALGORUNTILZERO_DEFAULT;
            if (this.algoRunTilZero == algoRunTilZero && sameCheck) {
                appendToLog("!!! Skip sending repeated data with algoRunTilZero = " + algoRunTilZero);
                return true;
            }
            this.algoRunTilZero = algoRunTilZero; appendToLog(String.format("!!! Skip setAlgoRunTilZero[%d]", algoRunTilZero));
            return true;
        }

        int rssiFilterConfig = -1;
        final int RSSIFILTERTYPE_INVALID = -1, RSSIFILTERTYPE_MIN = 0, RSSIFILTERTYPE_MAX = 2;
        int rssiFilterType = RSSIFILTERTYPE_INVALID;
        final int RSSIFILTEROPTION_INVALID = -1, RSSIFILTEROPTION_MIN = 0, RSSIFILTEROPTION_MAX = 4;
        int rssiFilterOption = RSSIFILTEROPTION_INVALID;
        int getRssiFilterType() {
            if (rssiFilterType < 0) getHST_INV_RSSI_FILTERING_CONFIG();
            return rssiFilterType;
        }
        int getRssiFilterOption() {
            if (rssiFilterOption < 0) getHST_INV_RSSI_FILTERING_CONFIG();
            return rssiFilterOption;
        }
        private boolean getHST_INV_RSSI_FILTERING_CONFIG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 7, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_CONFIG, false, msgBuffer);
        }
        boolean setHST_INV_RSSI_FILTERING_CONFIG(int rssiFilterType, int rssiFilterOption) {
            appendToLog("rssiFilterType = " + rssiFilterType + ", rssiFilterOption = " + rssiFilterOption);
            byte[] bytes = new byte[] { 0 };
            if (rssiFilterType > 0) {
                if (rssiFilterOption > 0) bytes[0] = 2;
                else bytes[0] = 1;
            } else bytes[0] = 0;
            boolean bValue = writeMAC(0x390A, bytes, true);
            if (bValue) {
                this.rssiFilterType = rssiFilterType;
                this.rssiFilterOption = rssiFilterOption;
            }
            return bValue;
        }

        final int RSSIFILTERTHRESHOLD_INVALID = -1, RSSIFILTERTHRESHOLD_MIN = 0, RSSIFILTERTHRESHOLD_MAX = 0xFFFF;
        int rssiFilterThreshold1 = RSSIFILTERTHRESHOLD_INVALID;
        int getRssiFilterThreshold1() {
            if (rssiFilterThreshold1 < 0) getHST_INV_RSSI_FILTERING_THRESHOLD();
            return rssiFilterThreshold1;
        }
        int rssiFilterThreshold2 = RSSIFILTERTHRESHOLD_INVALID;
        int getRssiFilterThreshold2() {
            if (rssiFilterThreshold2 < 0) getHST_INV_RSSI_FILTERING_THRESHOLD();
            return rssiFilterThreshold2;
        }
        private boolean getHST_INV_RSSI_FILTERING_THRESHOLD() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 8, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD, false, msgBuffer);
        }
        boolean setHST_INV_RSSI_FILTERING_THRESHOLD(int rssiFilterThreshold1, int rssiFilterThreshold2) {
            byte[] bytes = new byte[2];
            bytes[0] = (byte)(((short)rssiFilterThreshold1 >> 8) & 0xFF);
            bytes[1] = (byte)((short)rssiFilterThreshold1 & 0xFF);
            boolean bValue = writeMAC(0x390C, bytes, true);
            if (bValue) {
                this.rssiFilterThreshold1 = rssiFilterThreshold1;
                this.rssiFilterThreshold2 = rssiFilterThreshold2;
            }
            return bValue;
        }

        final long RSSIFILTERCOUNT_INVALID = -1, RSSIFILTERCOUNT_MIN = 0, RSSIFILTERCOUNT_MAX = 1000000;
        long rssiFilterCount = RSSIFILTERCOUNT_INVALID;
        long getRssiFilterCount() {
            if (rssiFilterCount < 0) getHST_INV_RSSI_FILTERING_COUNT();
            return rssiFilterCount;
        }
        private boolean getHST_INV_RSSI_FILTERING_COUNT() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_THRESHOLD, false, msgBuffer);
        }
        boolean setHST_INV_RSSI_FILTERING_COUNT(long rssiFilterCount) {
            appendToLog("entry: rssiFilterCount = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 9, 9, 0, 0, 0, 0};
            if (rssiFilterCount < RSSIFILTERCOUNT_MIN || rssiFilterCount > RSSIFILTERCOUNT_MAX)
                rssiFilterCount = mDefault.rssiFilterCount;
            appendToLog("rssiFilterCount 1 = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount);
            if (this.rssiFilterCount == rssiFilterCount && sameCheck) return true;
            appendToLog("rssiFilterCount 2 = " + rssiFilterCount + ", this.rssiFilterCount = " + this.rssiFilterCount);
            msgBuffer[4] |= (byte) (rssiFilterCount & 0xFF);
            msgBuffer[5] |= (byte) ((rssiFilterCount >> 8) & 0xFF);
            msgBuffer[6] |= (byte) ((rssiFilterCount >> 16) & 0xFF);
            msgBuffer[7] |= (byte) ((rssiFilterCount >> 24) & 0xFF);
            this.rssiFilterCount = rssiFilterCount;
            appendToLog("entering to sendHostRegRequest: rssiFilterCount = " + rssiFilterCount);
            boolean bValue = mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_RSSI_FILTERING_COUNT, true, msgBuffer);
            appendToLog("after sendHostRegRequest: rssiFilterCount = " + rssiFilterCount);
            return bValue;
        }

        final int MATCHENABLE_INVALID = -1; final int MATCHENABLE_MIN = 0; final int MATCHENABLE_MAX = 1;
        int matchEnable = MATCHENABLE_INVALID;
        int getInvMatchEnable() {
            getHST_INV_EPC_MATCH_CFG();
            return matchEnable;
        }
        boolean setInvMatchEnable(int matchEnable) {
            return setHST_INV_EPC_MATCH_CFG(matchEnable, this.matchType, this.matchLength, this.matchOffset);
        }
        boolean setInvMatchEnable(int matchEnable, int matchType, int matchLength, int matchOffset) {
            return setHST_INV_EPC_MATCH_CFG(matchEnable, matchType, matchLength, matchOffset);
        }

        final int MATCHTYPE_INVALID = -1; final int MATCHTYPE_MIN = 0; final int MATCHTYPE_MAX = 1;
        int matchType = MATCHTYPE_INVALID;
        int getInvMatchType() {
            getHST_INV_EPC_MATCH_CFG();
            return matchType;
        }

        final int MATCHLENGTH_INVALID = 0; final int MATCHLENGTH_MIN = 0; final int MATCHLENGTH_MAX = 496;
        int matchLength = MATCHLENGTH_INVALID;
        int getInvMatchLength() {
            getHST_INV_EPC_MATCH_CFG();
            return matchLength;
        }

        final int MATCHOFFSET_INVALID = -1; final int MATCHOFFSET_MIN = 0; final int MATCHOFFSET_MAX = 496;
        int matchOffset = MATCHOFFSET_INVALID;
        int getInvMatchOffset() {
            getHST_INV_EPC_MATCH_CFG();
            return matchOffset;
        }

        private boolean getHST_INV_EPC_MATCH_CFG() {
            if (matchEnable < MATCHENABLE_MIN || matchEnable > MATCHENABLE_MAX
                    || matchType < MATCHTYPE_MIN || matchType > MATCHTYPE_MAX
                    || matchLength < MATCHLENGTH_MIN || matchLength > MATCHLENGTH_MAX
                    || matchOffset < MATCHOFFSET_MIN || matchOffset > MATCHOFFSET_MAX
                    ) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0x11, 9, 0, 0, 0, 0};
                return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_EPC_MATCH_CFG, false, msgBuffer);
            } else {
                return false;
            }
        }
        private boolean setHST_INV_EPC_MATCH_CFG(int matchEnable, int matchType, int matchLength, int matchOffset) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0x11, 9, 0, 0, 0, 0};
            if (matchEnable < MATCHENABLE_MIN || matchEnable > MATCHENABLE_MAX)
                matchEnable = mDefault.matchEnable;
            if (matchType < MATCHTYPE_MIN || matchType > MATCHTYPE_MAX)
                matchType = mDefault.matchType;
            if (matchLength < MATCHLENGTH_MIN || matchLength > MATCHLENGTH_MAX)
                matchLength = mDefault.matchLength;
            if (matchOffset < MATCHOFFSET_MIN || matchOffset > MATCHOFFSET_MAX)
                matchOffset = mDefault.matchOffset;
            if (this.matchEnable == matchEnable && this.matchType == matchType && this.matchLength == matchLength && this.matchOffset == matchOffset && sameCheck) return true;
            if (matchEnable != 0) {
                msgBuffer[4] |= 0x01;
            }
            if (matchType != 0) {
                msgBuffer[4] |= 0x02;
            }
            msgBuffer[4] |= (byte) ((matchLength % 64) << 2);
            msgBuffer[5] |= (byte) ((matchLength / 64));
            msgBuffer[5] |= (byte) ((matchOffset % 32) << 3);
            msgBuffer[6] |= (byte) (matchOffset / 32);
            this.matchEnable = matchEnable;
            this.matchType = matchType;
            this.matchLength = matchLength;
            this.matchOffset = matchOffset;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_EPC_MATCH_CFG, true, msgBuffer);
        }

        byte[] invMatchData0_63; int invMatchDataReady = 0;
        String getInvMatchData() {
            int length = matchLength;
            String strValue = "";
            for (int i = 0; i < 16; i++) {
                if (length > 0) {
                    if ((invMatchDataReady & (0x01 << i)) == 0) {
                        byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 0x12, 9, 0, 0, 0, 0};
                        msgBuffer[2] += i;
                        mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_EPCDAT_0_3, false, msgBuffer);

                        strValue = null;
                        break;
                    } else {
                        for (int j = 0; j < 4; j++) {
                            strValue += String.format("%02X", invMatchData0_63[i * 4 + j]);
                        }
                    }
                    length -= 32;
                }
            }
            return strValue;
        }
        boolean setInvMatchData(String matchData) {
            int length = matchData.length();
            for (int i = 0; i < 16; i++) {
                if (length > 0) {
                    length -= 8;

                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 0x12, 9, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < 8; j++) {
                        if (i * 8 + j + 1 <= matchData.length()) {
                            String subString = matchData.substring(i * 8 + j, i * 8 + j + 1).toUpperCase();
                            int k = 0;
                            for (k = 0; k < 16; k++) {
                                if (subString.matches(hexString.substring(k, k + 1))) {
                                    break;
                                }
                            }
                            if (k == 16) return false;
                            if ((j / 2) * 2 == j) {
                                msgBuffer[4 + j / 2] |= (byte) (k << 4);
                            } else {
                                msgBuffer[4 + j / 2] |= (byte) (k);
                            }
                        }
                    }
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + i);
                    if (mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_EPCDAT_0_3, true, msgBuffer) == false)
                        return false;
                    else {
                        invMatchDataReady |= (0x01 << i);
                        System.arraycopy(msgBuffer, 4, invMatchData0_63, i * 4, 4); //appendToLog("Data=" + byteArrayToString(mRx000Setting.invMatchData0_63));
//                        appendToLog("invMatchDataReady=" + Integer.toString(mRx000Setting.invMatchDataReady, 16) + ", message=" + byteArrayToString(msgBuffer));
                    }
                }
            }
            return true;
        }

        //Tag access block parameters
        boolean accessVerfiy;
        final int ACCRETRY_INVALID = -1; final int ACCRETRY_MIN = 0; final int ACCRETRY_MAX = 7;
        int accessRetry = ACCRETRY_INVALID;
        int getAccessRetry() {
            if (accessRetry < ACCRETRY_MIN || accessRetry > ACCRETRY_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, (byte) 0x0A, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGACC_DESC_CFG, false, msgBuffer);
            }
            return accessRetry;
        }
        boolean setAccessRetry(boolean accessVerfiy, int accessRetry) {
            if (accessVerfiy == this.accessVerfiy && accessRetry == this.accessRetry) return true;
            this.accessVerfiy = accessVerfiy; this.accessRetry = accessRetry; appendToLog("!!! Skip setAccessRetry[" + accessVerfiy + ", " + accessRetry + "]");
            return true;
        }

        boolean setAccessEnable(int accessEnable, int accessEnable2) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("0 setAccessEnable with accessEnable = " + accessEnable + ", accessEnable2 = " + accessEnable2);
            if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                if (DEBUG) appendToLog("0 multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                if (accessEnable == mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][0] && sameCheck) bValue = true;
                else {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte) (accessEnable & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 0, bytes, true);
                    if (bValue) mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][0] = bytes[0];
                    if (DEBUG) appendToLog("0A multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                }
                if (DEBUG) appendToLog("0 multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                if (accessEnable2 == mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][0] && sameCheck) { }
                else if (bValue) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte) (accessEnable2 & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 1, bytes, true);
                    if (bValue) mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][0] = bytes[0];
                    if (DEBUG) appendToLog("0A multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }
        final int ACCBANK_INVALID = -1; final int ACCBANK_MIN = 0; final int ACCBANK_MAX = 3;
        int accessBank = ACCBANK_INVALID; int accessBank2 = ACCBANK_INVALID;
        int getAccessBank() {
            boolean DEBUG = false; int iValue = -1;
            if (accessBank >= 0 && accessBank <= 3 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else iValue = mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][1];
            return iValue;
        }
        boolean setAccessBank(int accessBank) { return setAccessBank(accessBank, 0); }
        boolean setAccessBank(int accessBank, int accessBank2) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("0 setAccessBank with accessBank = " + accessBank + ", accessBank2 = " + accessBank2);
            if (accessBank >= 0 && accessBank <= 3 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (accessBank2 >= 0 && accessBank2 <= 3 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                if (DEBUG) appendToLog("0 multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                if (accessBank == mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][1] && sameCheck) bValue = true;
                else if (accessBank >= 0 && accessBank <= 3) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessBank & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 0 + 1, bytes, true);
                    if (bValue) mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][1] = bytes[0];
                    if (DEBUG) appendToLog("0A multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                }
                if (DEBUG) appendToLog("0 multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                if (accessBank2 == mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][1] && sameCheck) { }
                else if (bValue && accessBank2 >= 0 && accessBank2 <= 3) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessBank2 & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 1 + 1, bytes, true);
                    if (bValue) mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][1] = bytes[0];
                    if (DEBUG) appendToLog("0A multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }

        final int ACCOFFSET_INVALID = -1; final int ACCOFFSET_MIN = 0; final int ACCOFFSET_MAX = 0xFFFF;
        int accessOffset = ACCOFFSET_INVALID; int accessOffset2 = ACCOFFSET_INVALID;
        int getAccessOffset() {
            boolean DEBUG = false; int iValue = -1;
            if (accessBank >= 0 && accessBank <= 3 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else {
                iValue = (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][2] & 0xFF) << 24;
                iValue |= (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][3] & 0xFF) << 16;
                iValue |= (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][4] & 0xFF) << 8;
                iValue |= (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][5] & 0xFF);
            }
            return iValue;
        }
        boolean setAccessOffset(int accessOffset) {
            //appendToLog("10 setAccessOffset with accessOffset = " + accessOffset);
            return setAccessOffset(accessOffset, 0); }
        boolean setAccessOffset(int accessOffset, int accessOffset2) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("0 setAccessOffset with accessOffset = " + accessOffset + ", accessOffset2 = " + accessOffset2);
            if (accessOffset >= 0 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (accessOffset2 >= 0 && mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                if (DEBUG) appendToLog("0 multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                if (accessOffset >= 0) {
                    byte[] bytes = new byte[4];
                    bytes[0] = (byte)((accessOffset >> 24) & 0xFF);
                    bytes[1] = (byte)((accessOffset >> 16) & 0xFF);
                    bytes[2] = (byte)((accessOffset >> 8) & 0xFF);
                    bytes[3] = (byte)(accessOffset & 0xFF);
                    byte[] bytesOld = new byte[4]; System.arraycopy(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0], 2, bytesOld, 0, bytesOld.length);
                    if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) bValue = true;
                    else {
                        bValue = writeMAC(0x3270 + 7 * 0 + 2, bytes, true);
                        if (bValue) System.arraycopy(bytes, 0, mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0], 2, bytes.length);
                        if (DEBUG) appendToLog("0A multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                    }
                }
                if (DEBUG) appendToLog("0 multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                if (bValue && accessOffset2 >= 0) {
                    byte[] bytes = new byte[4];
                    bytes[0] = (byte)((accessOffset2 >> 24) & 0xFF);
                    bytes[1] = (byte)((accessOffset2 >> 16) & 0xFF);
                    bytes[2] = (byte)((accessOffset2 >> 8) & 0xFF);
                    bytes[3] = (byte)(accessOffset2 & 0xFF);
                    byte[] bytesOld = new byte[4]; System.arraycopy(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1], 2, bytesOld, 0, bytesOld.length);
                    if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) { }
                    else {
                        bValue = writeMAC(0x3270 + 7 * 1 + 2, bytes, true);
                        if (bValue) System.arraycopy(bytes, 0, mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1], 2, bytes.length);
                        if (DEBUG) appendToLog("0A multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                    }
                }
            }
            return bValue;
        }

        final int ACCCOUNT_INVALID = -1; final int ACCCOUNT_MIN = 0; final int ACCCOUNT_MAX = 255;
        int accessCount = ACCCOUNT_INVALID; int accessCount2 = ACCCOUNT_INVALID;
        int getAccessCount() {
            if (accessCount < ACCCOUNT_MIN || accessCount > ACCCOUNT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, (byte) 0x0A, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGACC_CNT, false, msgBuffer);
            }
            return accessCount;
        }
        boolean setAccessCount(int accessCount) {
            setAccessEnable(((accessCount != 0) ? 1 : 0), 0);
            return setAccessCount(accessCount, 0); }
        boolean setAccessCount(int accessCount, int accessCount2) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("0 setAccessCount with accessCount = " + accessCount + ", accessCount2 = " + accessCount2);
            if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[0] is null !!!");
            else if (mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1] == null) appendToLog("!!! CANNOT continue as multibankReadConfig[1] is null !!!");
            else {
                if (DEBUG) appendToLog("0 multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                if (accessCount == mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][6] && sameCheck) bValue = true;
                else {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessCount & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 0 + 6, bytes, true);
                    if (bValue) mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0][6] = bytes[0];
                    if (DEBUG) appendToLog("0A multibankReadConfig[0] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[0]));
                }
                if (DEBUG) appendToLog("0 multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                if (accessCount2 == mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][6] && sameCheck) { }
                else if (bValue) {
                    byte[] bytes = new byte[1];
                    bytes[0] = (byte)(accessCount2 & 0xFF);
                    bValue = writeMAC(0x3270 + 7 * 1 + 6, bytes, true);
                    if (bValue) mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1][6] = bytes[0];
                    if (DEBUG) appendToLog("0A multibankReadConfig[1] = " + byteArrayToString(mRfidDevice.mRfidReaderChip.mRx000Setting.multibankReadConfig[1]));
                }
            }
            return bValue;
        }

        final int ACCLOCKACTION_INVALID = -1; final int ACCLOCKACTION_MIN = 0; final int ACCLOCKACTION_MAX = 0x3FF;
        int accessLockAction = ACCLOCKACTION_INVALID;
        int getAccessLockAction() {
            if (accessLockAction < ACCLOCKACTION_MIN || accessLockAction > ACCLOCKACTION_MAX)
                getHST_TAGACC_LOCKCFG();
            return accessLockAction;
        }
        boolean setAccessLockAction(int accessLockAction) {
            return setAccessLockAction(accessLockAction, accessLockMask);
        }

        final int ACCLOCKMASK_INVALID = -1; final int ACCLOCKMASK_MIN = 0; final int ACCLOCKMASK_MAX = 0x3FF;
        int accessLockMask = ACCLOCKMASK_INVALID;
        int getAccessLockMask() {
            if (accessLockMask < ACCLOCKMASK_MIN || accessLockMask > ACCLOCKMASK_MAX)
                getHST_TAGACC_LOCKCFG();
            return accessLockMask;
        }
        boolean setAccessLockMask(int accessLockMask) {
            return setAccessLockAction(accessLockAction, accessLockMask);
        }

        boolean getHST_TAGACC_LOCKCFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, (byte) 0x0A, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGACC_LOCKCFG, false, msgBuffer);
        }

        byte[] lockMask, lockAction;
        boolean setAccessLockAction(int accessLockAction, int accessLockMask) {
            appendToLog("accessLockAction = " + accessLockAction + ", accessLockMask = " + accessLockMask);
            boolean bValue = false;
            byte[] bytes = new byte[2];
            bytes[0] = (byte) (accessLockMask / 256);
            bytes[1] = (byte) (accessLockMask % 256);
            bValue = writeMAC(0x38AE, bytes, true);
            if (bValue) {
                lockMask = bytes;
                byte[] bytes1 = new byte[2];
                bytes1[0] = (byte) (accessLockAction / 256);
                bytes1[1] = (byte) (accessLockAction % 256);
                bValue = writeMAC(0x38B0, bytes1, true);
                if (bValue) lockAction = bytes;
            }
            return bValue;
        }

        final int ACCPWD_INVALID = 0; final long ACCPWD_MIN = 0; final long ACCPWD_MAX = 0x0FFFFFFFF;
        byte[] accessPassword = null;
        boolean getRx000AccessPassword() {
            return readMAC(0x38A6, 4);
        }
        boolean setRx000AccessPassword(String password) {
            boolean bValue = false, DEBUG = false;
            if (DEBUG) appendToLog("0 setRx000AccessPassword with password = " + password);
            if (accessPassword == null) appendToLog("!!! CANNOT continue as accessPassword is null !!!");
            else {
                if (DEBUG) appendToLog("0 accessPassword = " + byteArrayToString(accessPassword));
                byte[] bytes = new byte[4];
                if (password == null) password = "";
                String hexString = "0123456789ABCDEF";
                for (int j = 0; j < 16; j++) {
                    if (j + 1 <= password.length()) {
                        String subString = password.substring(j, j + 1).toUpperCase();
                        int k = 0;
                        for (k = 0; k < 16; k++) {
                            if (subString.matches(hexString.substring(k, k + 1))) {
                                break;
                            }
                        }
                        if (k == 16) return false;
                        if ((j / 2) * 2 == j) {
                            bytes[j / 2] |= (byte) (k << 4);
                        } else {
                            bytes[j / 2] |= (byte) (k);
                        }
                    }
                }
                byte[] bytesOld = new byte[4];
                System.arraycopy(accessPassword, 0, bytesOld, 0, bytesOld.length);
                if (DEBUG) appendToLog("0 bytes = " + byteArrayToString(bytes));
                if (compareArray(bytes, bytesOld, bytesOld.length) && sameCheck) bValue = true;
                else {
                    bValue = writeMAC(0x38A6, bytes, true);
                    if (bValue) accessPassword = bytes;
                    if (DEBUG)
                        appendToLog("0A accessPassword = " + byteArrayToString(accessPassword));
                }
            }
            return bValue;
        }

        boolean setRx000KillPassword(String password) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 7, (byte) 0x0A, 0, 0, 0, 0};
            String hexString = "0123456789ABCDEF";
            for (int j = 0; j < 16; j++) {
                if (j + 1 <= password.length()) {
                    String subString = password.substring(j, j + 1).toUpperCase();
                    int k = 0;
                    for (k = 0; k < 16; k++) {
                        if (subString.matches(hexString.substring(k, k + 1))) {
                            break;
                        }
                    }
                    if (k == 16) return false;
                    if ((j / 2) * 2 == j) {
                        msgBuffer[7 - j / 2] |= (byte) (k << 4);
                    } else {
                        msgBuffer[7 - j / 2] |= (byte) (k);
                    }
                }
            }
            boolean retValue = mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGACC_KILLPWD, true, msgBuffer);
            if (DEBUG) appendToLog("sendHostRegRequest(): retValue = " + retValue);
            return retValue;
        }

        final int ACCWRITEDATSEL_INVALID = -1; final int ACCWRITEDATSEL_MIN = 0; final int ACCWRITEDATSEL_MAX = 7;
        int accessWriteDataSelect = ACCWRITEDATSEL_INVALID;
        int getAccessWriteDataSelect() {
            if (accessWriteDataSelect < ACCWRITEDATSEL_MIN || accessWriteDataSelect > ACCWRITEDATSEL_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 8, (byte) 0x0A, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_SEL, false, msgBuffer);
            }
            return accessWriteDataSelect;
        }
        boolean setAccessWriteDataSelect(int accessWriteDataSelect) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 8, 0x0A, 0, 0, 0, 0};
            if (accessWriteDataSelect < ACCWRITEDATSEL_MIN || accessWriteDataSelect > ACCWRITEDATSEL_MAX)
                accessWriteDataSelect = mDefault.accessWriteDataSelect;
            if (this.accessWriteDataSelect == accessWriteDataSelect && sameCheck) return true;
            accWriteDataReady = 0;
            msgBuffer[4] = (byte) (accessWriteDataSelect & 0x07);
            this.accessWriteDataSelect = accessWriteDataSelect;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_SEL, true, msgBuffer);
        }

        byte[] accWriteData0_63; int accWriteDataReady = 0;
        String getAccessWriteData() {
            int length = accessCount;
            if (length > 32) {
                length = 32;
            }
            String strValue = "";
            for (int i = 0; i < 32; i++) {
                if (length > 0) {
                    if ((accWriteDataReady & (0x01 << i)) == 0) {
                        byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 9, (byte) 0x0A, 0, 0, 0, 0};
                        msgBuffer[2] += i;
                        mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGWRDAT_0, false, msgBuffer);

                        strValue = null;
                        break;
                    } else {
                        for (int j = 0; j < 4; j++) {
                            strValue += String.format("%02X", accWriteData0_63[i * 4 + j]);
                        }
                    }
                    length -= 2;
                }
            }
            return strValue;
        }
        boolean setAccessWriteData(String dataInput) {
            boolean bVAlue = false, DEBUG = false;
            if (DEBUG) appendToLog("Start with dataInput = " + dataInput);
            dataInput = dataInput.trim();
            int writeBufLength = 16 * 2; //16
            int wrieByteSize = 4;   //8
            int length = dataInput.length();
            if (DEBUG) appendToLog("Check dataInput length = " + length + " with maximum length = " + wrieByteSize * writeBufLength);
            if (length > wrieByteSize * writeBufLength) return false;
            byte[] msgBuffer = new byte[length/2 + (length%2 != 0 ? 1 : 0)];
            for (int i = 0; i < writeBufLength; i++) {
                if (DEBUG) appendToLog("Before processing 4 nibbles, check length = " + length);
                if (length > 0) {
                    length -= wrieByteSize;
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < wrieByteSize; j++) {
                        if (DEBUG) appendToLog("Check dataInput = " + dataInput + ", i = " + i + ", wrieByteSize = " + wrieByteSize + ", j = " + j);
                        if (i * wrieByteSize + j >= dataInput.length()) break;
                        String subString = dataInput.substring(i * wrieByteSize + j, i * wrieByteSize + j + 1).toUpperCase();
                        if (DEBUG) appendToLog("subString = " + subString);
                        int k = 0;
                        for (k = 0; k < 16; k++) {
                            if (DEBUG && false) appendToLog("k = " + k + ", with hexString = " + hexString);
                            if (subString.matches(hexString.substring(k, k + 1))) {
                                break;
                            }
                        }
                        if (k == 16) { appendToLog("!!! Cannot decode the data, with with i= " + i + ", j=" + j + ", subString = " + subString); return false; }
                        if ((j / 2) * 2 == j) {
                            msgBuffer[i * 2 + j / 2] |= (byte) (k << 4);
                        } else {
                            msgBuffer[i * 2 + j / 2] |= (byte) (k);
                        }
                        if (DEBUG) appendToLog("j = " + j + " with updated data : " + byteArrayToString(msgBuffer));
                    }
                    if (DEBUG) appendToLog("complete 4 bytes: " + byteArrayToString(msgBuffer));
                } else break;
            }

            bVAlue = setMultibankWriteConfig(0,true, getAccessBank(), getAccessOffset(), (msgBuffer.length/2 + (msgBuffer.length%2 != 0 ? 1 : 0)), msgBuffer);
            if (DEBUG) appendToLog("after setMultibankWriteConfig, bvalue = " + bVAlue);
            if (bVAlue) {
                //mRfidDevice.mRfidReaderChip.mRx000Setting.accWriteDataReady |= (0x01 << i);
                if (DEBUG) appendToLog("accWriteReady=" + accWriteDataReady);
                for (int k = 0; k < 4; k++) {
                    //accWriteData0_63[i * 4 + k] = msgBuffer[7 - k];
                }
                if (DEBUG) appendToLog("Data=" + byteArrayToString(accWriteData0_63));
            }
            return bVAlue;
        }

        //RFTC block paramters
        final int PROFILE_INVALID = -1; final int PROFILE_MIN = 0; final int PROFILE_MAX = 5;   //profile 4 and 5 are custom profiles.
        int currentProfile = PROFILE_INVALID;
        int iRfidModeSingleByte = -1;
        int getCurrentProfile() {
            int iValue = -1; boolean DEBUG = false;
            if (DEBUG) appendToLog("2 getCurrentProfile: antennaSelect = " + antennaSelect);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else {
                if (DEBUG) appendToLog("2A getCurrentProfile: getAntennaPortConfig[" + antennaSelect + "] = " + byteArrayToString(antennaPortConfig[antennaSelect]));
                if (antennaPortConfig[antennaSelect][14] != 0 && antennaPortConfig[antennaSelect][15] == 0) {
                    iRfidModeSingleByte = 1;
                    iValue = antennaPortConfig[antennaSelect][14];
                } else {
                    iRfidModeSingleByte = 0;
                    iValue = (antennaPortConfig[antennaSelect][14] & 0xFF) << 8;
                    iValue += (antennaPortConfig[antennaSelect][15] & 0xFF);
                }
                if (DEBUG) appendToLog(String.format("2b getCurrentProfile: iValue = 0x%X", iValue));
            }
            return iValue;
        }
        boolean setCurrentProfile(int currentProfile) {
            byte[] data; boolean DEBUG = false, bValue = false;
            if (DEBUG) appendToLog("2 setCurrentProfile: currentProfile = " + currentProfile + ", iRfidModeSingleByte = " + iRfidModeSingleByte);
            if (antennaPortConfig[antennaSelect] == null) appendToLog("CANNOT continue as antennaPortConfig[" + antennaSelect + "] is null !!!");
            else if (getCurrentProfile() == currentProfile && sameCheck) bValue = true;
            else {
                if (iRfidModeSingleByte < 0) getCurrentProfile();
                else {
                    if (iRfidModeSingleByte != 0) {
                        data = new byte[1];
                        data[0] = (byte) currentProfile;
                    } else {
                        data = new byte[2];
                        data[0] = (byte) (currentProfile / 256);
                        data[1] = (byte) (currentProfile & 0xFF);
                    }
                    if (DEBUG)
                        appendToLog("2A setCurrentProfile: data = " + byteArrayToString(data));
                    bValue = writeMAC(0x3030 + this.antennaSelect * 16 + 14, data, true);
                    if (DEBUG)
                        appendToLog("2b setCurrentProfile: after writeMAC, bValue = " + bValue);
                    if (bValue && antennaPortConfig[antennaSelect] != null)
                        System.arraycopy(data, 0, antennaPortConfig[antennaSelect], 14, data.length);
                }
            }
            return bValue;
        }

        final int COUNTRYENUM_INVALID = -1; final int COUNTRYENUM_MIN = 1; final int COUNTRYENUM_MAX = 109;
        final int COUNTRYCODE_INVALID = -1; final int COUNTRYCODE_MIN = 1; final int COUNTRYCODE_MAX = 9;
        int countryCode = COUNTRYCODE_INVALID;   // OemAddress = 0x02
        final int FREQCHANSEL_INVALID = -1; final int FREQCHANSEL_MIN = 0; final int FREQCHANSEL_MAX = 49;
        int freqChannelSelect = FREQCHANSEL_INVALID;

        final int FREQCHANCONFIG_INVALID = -1; final int FREQCHANCONFIG_MIN = 0; final int FREQCHANCONFIG_MAX = 1;
        int freqChannelConfig = FREQCHANCONFIG_INVALID;
        int getFreqChannelConfig() {
            if (freqChannelConfig < FREQCHANCONFIG_MIN || freqChannelConfig > FREQCHANCONFIG_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 0x0C, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_CFG, false, msgBuffer);
            }
            appendToLog("freqChannelConfig = " + freqChannelConfig);
            return freqChannelConfig;
        }

        final int FREQPLLMULTIPLIER_INVALID = -1;
        int freqPllMultiplier = FREQPLLMULTIPLIER_INVALID;
        int getFreqPllMultiplier() {
            if (freqPllMultiplier == FREQPLLMULTIPLIER_INVALID) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 0x0C, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, false, msgBuffer);
            }
            return freqPllMultiplier;
        }
        boolean setFreqPllMultiplier(int freqPllMultiplier) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 0x0C, 0, 0, 0, 0};
            msgBuffer[4] = (byte)(freqPllMultiplier & 0xFF);
            msgBuffer[5] = (byte)((freqPllMultiplier >> 8) & 0xFF);
            msgBuffer[6] = (byte)((freqPllMultiplier >> 16) & 0xFF);
            msgBuffer[7] = (byte)((freqPllMultiplier >> 24) & 0xFF);
            this.freqPllMultiplier = freqPllMultiplier;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, true, msgBuffer);
        }

        final int FREQPLLDAC_INVALID = -1;
        int freqPllDac = FREQPLLDAC_INVALID;
        int getFreqPllDac() {
            if (freqPllDac == FREQPLLDAC_INVALID) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 0x0C, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDACCTL, false, msgBuffer);
            }
            return freqPllDac;
        }

        boolean setFreqChannelOverride(int freqStart) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 8, 0x0C, 0, 0, 0, 0};
            msgBuffer[4] = (byte)(freqStart & 0xFF);
            msgBuffer[5] = (byte)((freqStart >> 8) & 0xFF);
            msgBuffer[6] = (byte)((freqStart >> 16) & 0xFF);
            msgBuffer[7] = (byte)((freqStart >> 24) & 0xFF);
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_CMDSTART, true, msgBuffer);
        }
    }

    class AntennaSelectedData {
        AntennaSelectedData(boolean set_default_setting, int default_setting_type) {
            if (default_setting_type < 0)    default_setting_type = 0;
            if (default_setting_type > 5)    default_setting_type = 5;
            mDefault = new AntennaSelectedData_default(default_setting_type);
            if (false && set_default_setting) {
                antennaEnable = mDefault.antennaEnable;
                antennaInventoryMode = mDefault.antennaInventoryMode;
                antennaLocalAlgo = mDefault.antennaLocalAlgo;
                antennaLocalStartQ = mDefault.antennaLocalStartQ;
                antennaProfileMode = mDefault.antennaProfileMode;
                antennaLocalProfile = mDefault.antennaLocalProfile;
                antennaFrequencyMode = mDefault.antennaFrequencyMode;
                antennaLocalFrequency = mDefault.antennaLocalFrequency;
                antennaStatus = mDefault.antennaStatus;
                antennaDefine = mDefault.antennaDefine;
                antennaDwell = mDefault.antennaDwell;
                antennaPower = mDefault.antennaPower; appendToLog("antennaPower is set to default " + antennaPower);
                antennaInvCount = mDefault.antennaInvCount;
            }
        }

        class AntennaSelectedData_default {
            AntennaSelectedData_default(int set_default_setting) {
                antennaEnable = mDefaultArray.antennaEnable[set_default_setting];
                antennaInventoryMode = mDefaultArray.antennaInventoryMode[set_default_setting];
                antennaLocalAlgo = mDefaultArray.antennaLocalAlgo[set_default_setting];
                antennaLocalStartQ = mDefaultArray.antennaLocalStartQ[set_default_setting];
                antennaProfileMode = mDefaultArray.antennaProfileMode[set_default_setting];
                antennaLocalProfile = mDefaultArray.antennaLocalProfile[set_default_setting];
                antennaFrequencyMode = mDefaultArray.antennaFrequencyMode[set_default_setting];
                antennaLocalFrequency = mDefaultArray.antennaLocalFrequency[set_default_setting];
                antennaStatus = mDefaultArray.antennaStatus[set_default_setting];
                antennaDefine = mDefaultArray.antennaDefine[set_default_setting];
                antennaDwell = mDefaultArray.antennaDwell[set_default_setting];
                antennaPower = mDefaultArray.antennaPower[set_default_setting];
                antennaInvCount = mDefaultArray.antennaInvCount[set_default_setting];
            }

            int antennaEnable;
            int antennaInventoryMode;
            int antennaLocalAlgo;
            int antennaLocalStartQ;
            int antennaProfileMode;
            int antennaLocalProfile;
            int antennaFrequencyMode;
            int antennaLocalFrequency;
            int antennaStatus;
            int antennaDefine;
            long antennaDwell;
            long antennaPower;
            long antennaInvCount;
        }
        AntennaSelectedData_default mDefault;

        private class AntennaSelectedData_defaultArray { //0 for invalid default,    1  for 0,       2 for 1 to 3,       3 for 4 to 7,       4 for 8 to   11,        5 for 12 to 15
            int[] antennaEnable =         { -1, 1, 0, 0, 0, 0 };
            int[] antennaInventoryMode = { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalAlgo =     { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalStartQ =    { -1, 0, 0, 0, 0, 0 };
            int[] antennaProfileMode =    { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalProfile =    { -1, 0, 0, 0, 0, 0 };
            int[] antennaFrequencyMode = { -1, 0, 0, 0, 0, 0 };
            int[] antennaLocalFrequency = { -1, 0, 0, 0, 0, 0 };
            int[] antennaStatus =           { -1, 0, 0, 0, 0, 0 };
            int[] antennaDefine =         { -1, 0, 0, 1, 2, 3 };
            long[] antennaDwell =      { -1, 2000, 2000, 2000, 2000, 2000 };
            long[] antennaPower =       { -1, 300, 0, 0, 0, 0 };
            long[] antennaInvCount =   { -1, 8192, 8192, 8192, 8192, 8192 };
        }
        AntennaSelectedData_defaultArray mDefaultArray = new AntennaSelectedData_defaultArray();

        final int ANTENABLE_INVALID = -1; final int ANTENABLE_MIN = 0; final int ANTENABLE_MAX = 1;
        int antennaEnable = ANTENABLE_INVALID;
        int getAntennaEnable() {
            appendToLog("3 getAntennaEnable");
            if (antennaEnable < ANTENABLE_MIN || antennaEnable > ANTENABLE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaEnable;
        }
        boolean setAntennaEnable(int antennaEnable) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTINVMODE_INVALID = 0; final int ANTINVMODE_MIN = 0; final int ANTINVMODE_MAX = 1;
        int antennaInventoryMode = ANTINVMODE_INVALID;
        int getAntennaInventoryMode() {
            if (antennaInventoryMode < ANTPROFILEMODE_MIN || antennaInventoryMode > ANTPROFILEMODE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaInventoryMode;
        }
        boolean setAntennaInventoryMode(int antennaInventoryMode) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ, antennaProfileMode,
                    antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALALGO_INVALID = 0; final int ANTLOCALALGO_MIN = 0; final int ANTLOCALALGO_MAX = 5;
        int antennaLocalAlgo = ANTLOCALALGO_INVALID;
        int getAntennaLocalAlgo() {
            if (antennaLocalAlgo < ANTLOCALALGO_MIN || antennaLocalAlgo > ANTLOCALALGO_MAX)
                getHST_ANT_DESC_CFG();
            return antennaLocalAlgo;
        }
        boolean setAntennaLocalAlgo(int antennaLocalAlgo) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALSTARTQ_INVALID = 0; final int ANTLOCALSTARTQ_MIN = 0; final int ANTLOCALSTARTQ_MAX = 15;
        int antennaLocalStartQ = ANTLOCALSTARTQ_INVALID;
        int getAntennaLocalStartQ() {
            if (antennaLocalStartQ < ANTLOCALSTARTQ_MIN || antennaLocalStartQ > ANTLOCALSTARTQ_MAX)
                getHST_ANT_DESC_CFG();
            return antennaLocalStartQ;
        }
        boolean setAntennaLocalStartQ(int antennaLocalStartQ) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTPROFILEMODE_INVALID = 0; final int ANTPROFILEMODE_MIN = 0; final int ANTPROFILEMODE_MAX = 1;
        int antennaProfileMode = ANTPROFILEMODE_INVALID;
        int getAntennaProfileMode() {
            if (antennaProfileMode < ANTPROFILEMODE_MIN || antennaProfileMode > ANTPROFILEMODE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaProfileMode;
        }
        boolean setAntennaProfileMode(int antennaProfileMode) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALPROFILE_INVALID = 0; final int ANTLOCALPROFILE_MIN = 0; final int ANTLOCALPROFILE_MAX = 5;
        int antennaLocalProfile = ANTLOCALPROFILE_INVALID;
        int getAntennaLocalProfile() {
            if (antennaLocalProfile < ANTLOCALPROFILE_MIN || antennaLocalProfile > ANTLOCALPROFILE_MIN)
                getHST_ANT_DESC_CFG();
            return antennaLocalProfile;
        }
        boolean setAntennaLocalProfile(int antennaLocalProfile) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTFREQMODE_INVALID = 0; final int ANTFREQMODE_MIN = 0; final int ANTFREQMODE_MAX = 1;
        int antennaFrequencyMode = ANTFREQMODE_INVALID;
        int getAntennaFrequencyMode() {
            if (antennaFrequencyMode < ANTFREQMODE_MIN || antennaFrequencyMode > ANTFREQMODE_MAX)
                getHST_ANT_DESC_CFG();
            return antennaFrequencyMode;
        }
        boolean setAntennaFrequencyMode(int antennaFrequencyMode) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        final int ANTLOCALFREQ_INVALID = 0; final int ANTLOCALFREQ_MIN = 0; final int ANTLOCALFREQ_MAX = 49;
        int antennaLocalFrequency = ANTLOCALFREQ_INVALID;
        int getAntennaLocalFrequency() {
            if (antennaLocalFrequency < ANTLOCALFREQ_MIN || antennaLocalFrequency > ANTLOCALFREQ_MAX)
                getHST_ANT_DESC_CFG();
            return antennaLocalFrequency;
        }
        boolean setAntennaLocalFrequency(int antennaLocalFrequency) {
            return setAntennaEnable(antennaEnable, antennaInventoryMode, antennaLocalAlgo, antennaLocalStartQ,
                    antennaProfileMode, antennaLocalProfile, antennaFrequencyMode, antennaLocalFrequency);
        }

        private boolean getHST_ANT_DESC_CFG() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 7, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_CFG, false, msgBuffer);
        }
        boolean setAntennaEnable(int antennaEnable, int antennaInventoryMode, int antennaLocalAlgo, int antennaLocalStartQ,
                                        int antennaProfileMode, int antennaLocalProfile,
                                        int antennaFrequencyMode, int antennaLocalFrequency) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 7, 0, 0, 0, 0};
            if (antennaEnable < ANTENABLE_MIN || antennaEnable > ANTENABLE_MAX)
                antennaEnable = mDefault.antennaEnable;
            if (antennaInventoryMode < ANTINVMODE_MIN || antennaInventoryMode > ANTINVMODE_MAX)
                antennaInventoryMode = mDefault.antennaInventoryMode;
            if (antennaLocalAlgo < ANTLOCALALGO_MIN || antennaLocalAlgo > ANTLOCALALGO_MAX)
                antennaLocalAlgo = mDefault.antennaLocalAlgo;
            if (antennaLocalStartQ < ANTLOCALSTARTQ_MIN || antennaLocalStartQ > ANTLOCALSTARTQ_MAX)
                antennaLocalStartQ = mDefault.antennaLocalStartQ;
            if (antennaProfileMode < ANTPROFILEMODE_MIN || antennaProfileMode > ANTPROFILEMODE_MAX)
                antennaProfileMode = mDefault.antennaProfileMode;
            if (antennaLocalProfile < ANTLOCALPROFILE_MIN || antennaLocalProfile > ANTLOCALPROFILE_MAX)
                antennaLocalProfile = mDefault.antennaLocalProfile;
            if (antennaFrequencyMode < ANTFREQMODE_MIN || antennaFrequencyMode > ANTFREQMODE_MAX)
                antennaFrequencyMode = mDefault.antennaFrequencyMode;
            if (antennaLocalFrequency < ANTLOCALFREQ_MIN || antennaLocalFrequency > ANTLOCALFREQ_MAX)
                antennaLocalFrequency = mDefault.antennaLocalFrequency;
            if (this.antennaEnable == antennaEnable && this.antennaInventoryMode == antennaInventoryMode && this.antennaLocalAlgo == antennaLocalAlgo
                    && this.antennaLocalStartQ == antennaLocalStartQ && this.antennaProfileMode == antennaProfileMode && this.antennaLocalProfile == antennaLocalProfile
                    && this.antennaFrequencyMode == antennaFrequencyMode && this.antennaLocalFrequency == antennaLocalFrequency
                    && sameCheck)
                return true;
            msgBuffer[4] |= antennaEnable;
            msgBuffer[4] |= (antennaInventoryMode << 1);
            msgBuffer[4] |= (antennaLocalAlgo << 2);
            msgBuffer[4] |= (antennaLocalStartQ << 4);
            msgBuffer[5] |= antennaProfileMode;
            msgBuffer[5] |= (antennaLocalProfile << 1);
            msgBuffer[5] |= (antennaFrequencyMode << 5);
            msgBuffer[5] |= ((antennaLocalFrequency & 0x03) << 6);
            msgBuffer[6] |= (antennaLocalFrequency >> 2);
            this.antennaEnable = antennaEnable;
            this.antennaInventoryMode = antennaInventoryMode;
            this.antennaLocalAlgo = antennaLocalAlgo;
            this.antennaLocalStartQ = antennaLocalStartQ;
            this.antennaProfileMode = antennaProfileMode;
            this.antennaLocalProfile = antennaLocalProfile;
            this.antennaFrequencyMode = antennaFrequencyMode;
            this.antennaLocalFrequency = antennaLocalFrequency;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_CFG, true, msgBuffer);
        }

        final int ANTSTATUS_INVALID = -1; final int ANTSTATUS_MIN = 0; final int ANTSTATUS_MAX = 0xFFFFF;
        int antennaStatus = ANTSTATUS_INVALID;
        int getAntennaStatus() {
            if (antennaStatus < ANTSTATUS_MIN || antennaStatus > ANTSTATUS_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 7, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.MAC_ANT_DESC_STAT, false, msgBuffer);
            }
            return antennaStatus;
        }

        final int ANTDEFINE_INVALID = -1; final int ANTDEFINE_MIN = 0; final int ANTDEFINE_MAX = 3;
        int antennaDefine = ANTDEFINE_INVALID;
        int getAntennaDefine() {
            if (antennaDefine < ANTDEFINE_MIN || antennaDefine > ANTDEFINE_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 7, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_PORTDEF, false, msgBuffer);
            }
            return antennaDefine;
        }

        final long ANTDWELL_INVALID = -1; final long ANTDWELL_MIN = 0; final long ANTDWELL_MAX = 0xFFFF;
        long antennaDwell = ANTDWELL_INVALID;
        long getAntennaDwell() {
            if (antennaDwell < ANTDWELL_MIN || antennaDwell > ANTDWELL_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, 7, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_DWELL, false, msgBuffer);
            }
            return antennaDwell;
        }
        boolean setAntennaDwell(long antennaDwell) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 7, 0, 0, 0, 0};
            if (antennaDwell < ANTDWELL_MIN || antennaDwell > ANTDWELL_MAX)
                antennaDwell = mDefault.antennaDwell;
            if (this.antennaDwell == antennaDwell && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaDwell % 256);
            msgBuffer[5] = (byte) ((antennaDwell >> 8) % 256);
            msgBuffer[6] = (byte) ((antennaDwell >> 16) % 256);
            msgBuffer[7] = (byte) ((antennaDwell >> 24) % 256);
            this.antennaDwell = antennaDwell;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_DWELL, true, msgBuffer);
        }

        final int ANTARGET_INVALID = -1; final int ANTARGET_MIN = 0; final int ANTARGET_MAX = 1;
        int antennaTarget = ANTARGET_INVALID;
        byte[] antennaInventoryRoundControl = null;
        final int ANTOGGLE_INVALID = -1; final int ANTOGGLE_MIN = 0; final int ANTOGGLE_MAX = 100;
        int antennaToggle = ANTOGGLE_INVALID;
        final int ANTRFMODE_INVALID = -1; final int ANTRFMODE_MIN = 1; final int ANTRFMODE_MAX = 15;
        int antennaRfMode = ANTRFMODE_INVALID;

        final long ANTPOWER_INVALID = -1; final long ANTPOWER_MIN = 0; final long ANTPOWER_MAX = 330; //Maximum 330\
        long antennaPower = ANTPOWER_INVALID;   //default value = 300
        long getAntennaPower() {
            if (antennaPower < ANTPOWER_MIN || antennaPower > ANTPOWER_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 6, 7, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_RFPOWER, false, msgBuffer);
            }
            return antennaPower;
        }
        boolean antennaPowerSet = false;
        boolean setAntennaPower(long antennaPower) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 6, 7, 0, 0, 0, 0};
            if (antennaPower < ANTPOWER_MIN || antennaPower > ANTPOWER_MAX)
                antennaPower = mDefault.antennaPower;
            if (this.antennaPower == antennaPower && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaPower % 256);
            msgBuffer[5] = (byte) ((antennaPower >> 8) % 256);
            this.antennaPower = antennaPower;
            antennaPowerSet = true;
            appendToLog("3 setPowerLevel");
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_RFPOWER, true, msgBuffer);
        }

        final long ANTINVCOUNT_INVALID = -1; final long ANTINVCOUNT_MIN = 0; final long ANTINVCOUNT_MAX = 0xFFFFFFFFL;
        long antennaInvCount = ANTINVCOUNT_INVALID;
        long getAntennaInvCount() {
            if (antennaInvCount < ANTINVCOUNT_MIN || antennaInvCount > ANTINVCOUNT_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 7, 7, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_INV_CNT, false, msgBuffer);
            }
            return antennaInvCount;
        }
        boolean setAntennaInvCount(long antennaInvCount) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 7, 7, 0, 0, 0, 0};
            if (antennaInvCount < ANTINVCOUNT_MIN || antennaInvCount > ANTINVCOUNT_MAX)
                antennaInvCount = mDefault.antennaInvCount;
            if (this.antennaInvCount == antennaInvCount && sameCheck) return true;
            msgBuffer[4] = (byte) (antennaInvCount % 256);
            msgBuffer[5] = (byte) ((antennaInvCount >> 8) % 256);
            msgBuffer[6] = (byte) ((antennaInvCount >> 16) % 256);
            msgBuffer[7] = (byte) ((antennaInvCount >> 24) % 256);
            this.antennaInvCount = antennaInvCount;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_ANT_DESC_INV_CNT, true, msgBuffer);
        }
    }

    class InvSelectData {
        InvSelectData(boolean set_default_setting) {
            if (set_default_setting) {
                selectEnable = mDefault.selectEnable;
                selectTarget = mDefault.selectTarget;
                selectAction = mDefault.selectAction;
                selectDelay = mDefault.selectDelay;
                selectMaskBank = mDefault.selectMaskBank;
                selectMaskOffset = mDefault.selectMaskOffset;
                selectMaskLength = mDefault.selectMaskLength;
                selectMaskDataReady = mDefault.selectMaskDataReady;
            }
        }

        private class InvSelectData_default {
            int selectEnable = 0;
            int selectTarget = 0;
            int selectAction = 0;
            int selectDelay = 0;
            int selectMaskBank = 0;
            int selectMaskOffset = 0;
            int selectMaskLength = 0;
            byte[] selectMaskData0_31 = new byte[4 * 8]; byte selectMaskDataReady = 0;
        }
        InvSelectData_default mDefault = new InvSelectData_default();

        final int INVSELENABLE_INVALID = 0; final int INVSELENABLE_MIN = 0; final int INVSELENABLE_MAX = 1;
        int selectEnable = INVSELENABLE_INVALID;
        int getSelectEnable() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectEnable;
        }
        boolean setSelectEnable(int selectEnable) {
            appendToLog("1 setRx000HostReg_HST_TAGMSK_DESC_CFG: selectEnable = " + selectEnable);
            return setRx000HostReg_HST_TAGMSK_DESC_CFG(selectEnable, this.selectTarget, this.selectAction, this.selectDelay);
        }

        final int INVSELTARGET_INVALID = -1; final int INVSELTARGET_MIN = 0; final int INVSELTARGET_MAX = 7;
        int selectTarget = INVSELTARGET_INVALID;
        int getSelectTarget() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectTarget;
        }

        final int INVSELACTION_INVALID = -1; final int INVSELACTION_MIN = 0; final int INVSELACTION_MAX = 7;
        int selectAction = INVSELACTION_INVALID;
        int getSelectAction() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectAction;
        }

        final int INVSELDELAY_INVALID = -1; final int INVSELDELAY_MIN = 0; final int INVSELDELAY_MAX = 255;
        int selectDelay = INVSELDELAY_INVALID;
        int getSelectDelay() {
            getRx000HostReg_HST_TAGMSK_DESC_CFG();
            return selectDelay;
        }

        boolean getRx000HostReg_HST_TAGMSK_DESC_CFG() {
            if (selectEnable < INVSELENABLE_MIN || selectEnable > INVSELENABLE_MAX
                    || selectTarget < INVSELTARGET_MIN || selectTarget > INVSELTARGET_MAX
                    || selectAction < INVSELACTION_MIN || selectAction > INVSELACTION_MAX
                    || selectDelay < INVSELDELAY_MIN || selectDelay > INVSELDELAY_MAX
                    ) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 1, 8, 0, 0, 0, 0};
                return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_DESC_CFG, false, msgBuffer);
            } else {
                return false;
            }
        }
        boolean setRx000HostReg_HST_TAGMSK_DESC_CFG(int selectEnable, int selectTarget, int selectAction, int selectDelay) {
            appendToLog("0 setRx000HostReg_HST_TAGMSK_DESC_CFG: selectEnable = " + selectEnable + ", selectTarget" + selectTarget + ", selectAction = " + selectAction + ", selectDelay = " + selectDelay);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 1, 8, 0, 0, 0, 0};
            if (selectEnable < INVSELENABLE_MIN || selectEnable > INVSELENABLE_MAX)
                selectEnable = mDefault.selectEnable;
            if (selectTarget < INVSELTARGET_MIN || selectTarget > INVSELTARGET_MAX)
                selectTarget = mDefault.selectTarget;
            if (selectAction < INVSELACTION_MIN || selectAction > INVSELACTION_MAX)
                selectAction = mDefault.selectAction;
            int selectDalay0 = selectDelay;
            if (selectDelay < INVSELDELAY_MIN || selectDelay > INVSELDELAY_MAX)
                selectDelay = mDefault.selectDelay;
            if (this.selectEnable == selectEnable && this.selectTarget == selectTarget && this.selectAction == selectAction && this.selectDelay == selectDelay && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectEnable & 0x1);
            msgBuffer[4] |= (byte) ((selectTarget & 0x07) << 1);
            msgBuffer[4] |= (byte) ((selectAction & 0x07) << 4);
            msgBuffer[5] |= (byte) (selectDelay & 0xFF);
            this.selectEnable = selectEnable;
            this.selectTarget = selectTarget;
            this.selectAction = selectAction;
            this.selectDelay = selectDelay;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_DESC_CFG, true, msgBuffer);
        }

        final int INVSELMBANK_INVALID = -1; final int INVSELMBANK_MIN = 0; final int INVSELMBANK_MAX = 3;
        int selectMaskBank = INVSELMBANK_INVALID;
        int getSelectMaskBank() {
            if (selectMaskBank < INVSELMBANK_MIN || selectMaskBank > INVSELMBANK_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 2, 8, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_BANK, false, msgBuffer);
            }
            return selectMaskBank;
        }
        boolean setSelectMaskBank(int selectMaskBank) {
            appendToLog("0 setSelectMaskBank with selectMaskBank = " + selectMaskBank);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 2, 8, 0, 0, 0, 0};
            if (selectMaskBank < INVSELMBANK_MIN || selectMaskBank > INVSELMBANK_MAX)
                selectMaskBank = mDefault.selectMaskBank;
            if (this.selectMaskBank == selectMaskBank && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectMaskBank & 0x3);
            this.selectMaskBank = selectMaskBank;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_BANK, true, msgBuffer);
        }

        final int INVSELMOFFSET_INVALID = -1; final int INVSELMOFFSET_MIN = 0; final int INVSELMOFFSET_MAX = 0xFFFF;
        int selectMaskOffset = INVSELMOFFSET_INVALID;
        int getSelectMaskOffset() {
            if (selectMaskOffset < INVSELMOFFSET_MIN || selectMaskOffset > INVSELMOFFSET_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 8, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_PTR, false, msgBuffer);
            }
            return selectMaskOffset;
        }
        boolean setSelectMaskOffset(int selectMaskOffset) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 8, 0, 0, 0, 0};
            if (selectMaskOffset < INVSELMOFFSET_MIN || selectMaskOffset > INVSELMOFFSET_MAX)
                selectMaskOffset = mDefault.selectMaskOffset;
            if (this.selectMaskOffset == selectMaskOffset && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectMaskOffset & 0xFF);
            msgBuffer[5] |= (byte) ((selectMaskOffset >> 8) & 0xFF);
            this.selectMaskOffset = selectMaskOffset;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_PTR, true, msgBuffer);
        }

        final int INVSELMLENGTH_INVALID = -1; final int INVSELMLENGTH_MIN = 0; final int INVSELMLENGTH_MAX = 255;
        int selectMaskLength = INVSELMLENGTH_INVALID;
        int getSelectMaskLength() {
            appendToLog("getSelectMaskData with selectMaskLength = " + selectMaskLength);
            if (selectMaskLength < INVSELMLENGTH_MIN || selectMaskOffset > INVSELMLENGTH_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 8, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_LEN, false, msgBuffer);
            }
            return selectMaskLength;
        }
        boolean setSelectMaskLength(int selectMaskLength) {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 4, 8, 0, 0, 0, 0};
            if (selectMaskLength < INVSELMLENGTH_MIN) selectMaskLength = INVSELMLENGTH_MIN;
            else if (selectMaskLength > INVSELMLENGTH_MAX) selectMaskLength = INVSELMLENGTH_MAX;
            if (this.selectMaskLength == selectMaskLength && sameCheck) return true;
            msgBuffer[4] |= (byte) (selectMaskLength & 0xFF);
            if (selectMaskLength == INVSELMLENGTH_MAX) msgBuffer[5] = 1;
            this.selectMaskLength = selectMaskLength; if (false) appendToLog("getSelectMaskData with saved selectMaskLength = " + selectMaskLength);
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_PTR, true, msgBuffer);
        }

        byte[] selectMaskData0_31 = new byte[4 * 8]; byte selectMaskDataReady = 0;
        String getRx000SelectMaskData() {
            appendToLog("getSelectMaskData with selectMaskData0_31 = " + byteArrayToString(selectMaskData0_31));
            int length = selectMaskLength;
            String strValue = "";
            if (length < 0) {
                getSelectMaskLength();
            } else {
                for (int i = 0; i < 8; i++) {
                    if (length > 0) {
                        if ((selectMaskDataReady & (0x01 << i)) == 0) {
                            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, 8, 0, 0, 0, 0};
                            msgBuffer[2] += i;
                            mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_0_3, false, msgBuffer);

                            strValue = null;
                            break;
                        } else {
                            for (int j = 0; j < 4; j++) {
                                if (DEBUG) appendToLog("i = " + i + ", j = " + j + ", selectMaskData0_31 = " + selectMaskData0_31[i * 4 + j]);
                                strValue += String.format("%02X", selectMaskData0_31[i * 4 + j]);
                            }
                        }
                        length -= 32;
                    }
                }
            }
            return strValue;
        }
        boolean setRx000SelectMaskData(String maskData) {
            int length = maskData.length();
            for (int i = 0; i < 8; i++) {
                if (length > 0) {
                    length -= 8;

                    byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 8, 0, 0, 0, 0};
                    String hexString = "0123456789ABCDEF";
                    for (int j = 0; j < 8; j++) {
                        if (i * 8 + j + 1 <= maskData.length()) {
                            String subString = maskData.substring(i * 8 + j, i * 8 + j + 1).toUpperCase();
                            int k = 0;
                            for (k = 0; k < 16; k++) {
                                if (subString.matches(hexString.substring(k, k + 1))) {
                                    break;
                                }
                            }
                            if (k == 16) return false;
//                                appendToLog("setSelectMaskData(" + maskData +"): i=" + i + ", j=" + j + ", k=" + k);
                            if ((j / 2) * 2 == j) {
                                msgBuffer[4 + j / 2] |= (byte) (k << 4);
                            } else {
                                msgBuffer[4 + j / 2] |= (byte) (k);
                            }
                        }
                    }
                    msgBuffer[2] = (byte) ((msgBuffer[2] & 0xFF) + i);
                    if (mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_TAGMSK_0_3, true, msgBuffer) == false)
                        return false;
                    else {
                        selectMaskDataReady |= (0x01 << i);
                        if (DEBUG) appendToLog("Old selectMaskData0_31 = " + byteArrayToString(selectMaskData0_31));
                        System.arraycopy(msgBuffer, 4, selectMaskData0_31, i * 4, 4);
                        if (DEBUG) appendToLog("New selectMaskData0_31 = " + byteArrayToString(selectMaskData0_31));
                    }
                }
            }
            return true;
        }
    }

    class AlgoSelectedData {
        AlgoSelectedData(boolean set_default_setting, int default_setting_type) {
            if (default_setting_type < 0) default_setting_type = 0;
            if (default_setting_type > 4) default_setting_type = 4;
            mDefault = new AlgoSelectedData_default(default_setting_type);
            if (set_default_setting) {
                algoStartQ = mDefault.algoStartQ;
                algoMaxQ = mDefault.algoMaxQ;
                algoMinQ = mDefault.algoMinQ;
                algoMaxRep = mDefault.algoMaxRep;
                algoHighThres = mDefault.algoHighThres;
                algoLowThres = mDefault.algoLowThres;
                algoRetry = mDefault.algoRetry;
                algoAbFlip = mDefault.algoAbFlip;
                algoRunTilZero = mDefault.algoRunTilZero;
            }
        }

        class AlgoSelectedData_default {
            AlgoSelectedData_default(int set_default_setting) {
                algoStartQ = mDefaultArray.algoStartQ[set_default_setting];
                algoMaxQ = mDefaultArray.algoMaxQ[set_default_setting];
                algoMinQ = mDefaultArray.algoMinQ[set_default_setting];
                algoMaxRep = mDefaultArray.algoMaxRep[set_default_setting];
                algoHighThres = mDefaultArray.algoHighThres[set_default_setting];
                algoLowThres = mDefaultArray.algoLowThres[set_default_setting];
                algoRetry = mDefaultArray.algoRetry[set_default_setting];
                algoAbFlip = mDefaultArray.algoAbFlip[set_default_setting];
                algoRunTilZero = mDefaultArray.algoRunTilZero[set_default_setting];
            }

            int algoStartQ = -1;
            int algoMaxQ = -1;
            int algoMinQ = -1;
            int algoMaxRep = -1;
            int algoHighThres = -1;
            int algoLowThres = -1;
            int algoRetry = -1;
            int algoAbFlip = -1;
            int algoRunTilZero = -1;
        }
        AlgoSelectedData_default mDefault;

        class AlgoSelectedData_defaultArray { //0 for invalid default,    1 for 0,    2 for 1,     3 for 2,   4 for 3
            int[] algoStartQ =     { -1, 0, 0, 0, 4 };
            int[] algoMaxQ =      { -1, 0, 0, 0, 15 };
            int[] algoMinQ =      { -1, 0, 0, 0, 0 };
            int[] algoMaxRep =    { -1, 0, 0, 0, 4 };
            int[] algoHighThres =  { -1, 0, 5, 5, 5 };
            int[] algoLowThres =  { -1, 0, 3, 3, 3 };
            int[] algoRetry =      { -1, 0, 0, 0, 0 };
            int[] algoAbFlip =     { -1, 0, 1, 1, 1 };
            int[] algoRunTilZero = { -1, 0, 0, 0, 0 };
        }
        AlgoSelectedData_defaultArray mDefaultArray = new AlgoSelectedData_defaultArray();

        final int ALGOSTARTQ_INVALID = -1; final int ALGOSTARTQ_MIN = 0; final int ALGOSTARTQ_MAX = 15;
        int algoStartQ = ALGOSTARTQ_INVALID;
        int getAlgoStartQ(boolean getInvalid) {
            if (getInvalid && (algoStartQ < ALGOSTARTQ_MIN || algoStartQ > ALGOSTARTQ_MAX)) getHST_INV_ALG_PARM_0();
            return algoStartQ;
        }
        boolean setAlgoStartQ(int algoStartQ) {
            appendToLog("1A setAlgoStartQ with algoStartQ = " + algoStartQ);
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMAXQ_INVALID = -1; final int ALGOMAXQ_MIN = 0; final int ALGOMAXQ_MAX = 15;
        int algoMaxQ = ALGOMAXQ_INVALID;
        int getAlgoMaxQ() {
            if (algoMaxQ < ALGOMAXQ_MIN || algoMaxQ > ALGOMAXQ_MAX) getHST_INV_ALG_PARM_0();
            return algoMaxQ;
        }
        boolean setAlgoMaxQ(int algoMaxQ) {
            appendToLog("1b setAlgoStartQ");
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMINQ_INVALID = -1; final int ALGOMINQ_MIN = 0; final int ALGOMINQ_MAX = 15;
        int algoMinQ = ALGOMINQ_INVALID;
        int getAlgoMinQ() {
            if (algoMinQ < ALGOMINQ_MIN || algoMinQ > ALGOMINQ_MAX) getHST_INV_ALG_PARM_0();
            return algoMinQ;
        }
        boolean setAlgoMinQ(int algoMinQ) {
            appendToLog("1C setAlgoStartQ");
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOMAXREP_INVALID = -1; final int ALGOMAXREP_MIN = 0; final int ALGOMAXREP_MAX = 255;
        int algoMaxRep = ALGOMAXREP_INVALID;
        int getAlgoMaxRep() {
            if (algoMaxRep < ALGOMAXREP_MIN || algoMaxRep > ALGOMAXREP_MAX) getHST_INV_ALG_PARM_0();
            return algoMaxRep;
        }
        boolean setAlgoMaxRep(int algoMaxRep) {
            appendToLog("1d setAlgoStartQ");
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOHIGHTHRES_INVALID = -1; final int ALGOHIGHTHRES_MIN = 0; final int ALGOHIGHTHRES_MAX = 15;
        int algoHighThres = ALGOHIGHTHRES_INVALID;
        int getAlgoHighThres() {
            if (algoHighThres < ALGOHIGHTHRES_MIN || algoHighThres > ALGOHIGHTHRES_MAX)
                getHST_INV_ALG_PARM_0();
            return algoHighThres;
        }
        boolean setAlgoHighThres(int algoHighThres) {
            appendToLog("1E setAlgoStartQ");
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        final int ALGOLOWTHRES_INVALID = -1; final int ALGOLOWTHRES_MIN = 0; final int ALGOLOWTHRES_MAX = 15;
        int algoLowThres = ALGOLOWTHRES_INVALID;
        int getAlgoLowThres() {
            if (algoLowThres < ALGOLOWTHRES_MIN || algoLowThres > ALGOLOWTHRES_MAX)
                getHST_INV_ALG_PARM_0();
            return algoLowThres;
        }
        boolean setAlgoLowThres(int algoLowThres) {
            appendToLog("1F setAlgoStartQ");
            return setAlgoStartQ(algoStartQ, algoMaxQ, algoMinQ, algoMaxRep, algoHighThres, algoLowThres);
        }

        private boolean getHST_INV_ALG_PARM_0() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 3, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_0, false, msgBuffer);
        }
        boolean setAlgoStartQ(int startQ, int algoMaxQ, int algoMinQ, int algoMaxRep, int algoHighThres, int algoLowThres) {
            appendToLog("0 setAlgoStartQ with startQ = " + startQ);
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 9, 0, 0, 0, 0};
            if (startQ < ALGOSTARTQ_MIN || startQ > ALGOSTARTQ_MAX) startQ = mDefault.algoStartQ;
            if (algoMaxQ < ALGOMAXQ_MIN || algoMaxQ > ALGOMAXQ_MAX) algoMaxQ = mDefault.algoMaxQ;
            if (algoMinQ < ALGOMINQ_MIN || algoMinQ > ALGOMINQ_MAX) algoMinQ = mDefault.algoMinQ;
            if (algoMaxRep < ALGOMAXREP_MIN || algoMaxRep > ALGOMAXREP_MAX)
                algoMaxRep = mDefault.algoMaxRep;
            if (algoHighThres < ALGOHIGHTHRES_MIN || algoHighThres > ALGOHIGHTHRES_MAX)
                algoHighThres = mDefault.algoHighThres;
            if (algoLowThres < ALGOLOWTHRES_MIN || algoLowThres > ALGOLOWTHRES_MAX)
                algoLowThres = mDefault.algoLowThres;
            if (false && this.algoStartQ == startQ && this.algoMaxQ == algoMaxQ && this.algoMinQ == algoMinQ
                    && this.algoMaxRep == algoMaxRep && this.algoHighThres == algoHighThres && this.algoLowThres == algoLowThres
                    && sameCheck)
                return true;
            msgBuffer[4] |= (byte) (startQ & 0x0F);
            msgBuffer[4] |= (byte) ((algoMaxQ & 0x0F) << 4);
            msgBuffer[5] |= (byte) (algoMinQ & 0x0F);
            msgBuffer[5] |= (byte) ((algoMaxRep & 0xF) << 4);
            msgBuffer[6] |= (byte) ((algoMaxRep & 0xF0) >> 4);
            msgBuffer[6] |= (byte) ((algoHighThres & 0x0F) << 4);
            msgBuffer[7] |= (byte) (algoLowThres & 0x0F);
            this.algoStartQ = startQ;
            this.algoMaxQ = algoMaxQ;
            this.algoMinQ = algoMinQ;
            this.algoMaxRep = algoMaxRep;
            this.algoHighThres = algoHighThres;
            this.algoLowThres = algoLowThres;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_0, true, msgBuffer);
        }

        final int ALGORETRY_INVALID = -1; final int ALGORETRY_MIN = 0; final int ALGORETRY_MAX = 255;
        int algoRetry = ALGORETRY_INVALID;
        int getAlgoRetry() {
            if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX) {
                byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 4, 9, 0, 0, 0, 0};
                mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_1, false, msgBuffer);
            }
            return algoRetry;
        }
        boolean setAlgoRetry(int algoRetry) {
            appendToLog("2 setAlgoRetry[" + algoRetry + "]");
            if (algoRetry < ALGORETRY_MIN || algoRetry > ALGORETRY_MAX)
                algoRetry = mDefault.algoRetry;
            if (false && this.algoRetry == algoRetry && sameCheck) return true;
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 4, 9, 0, 0, 0, 0};
            msgBuffer[4] = (byte) algoRetry;
            this.algoRetry = algoRetry;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_1, true, msgBuffer);
        }

        final int ALGOABFLIP_INVALID = -1; final int ALGOABFLIP_MIN = 0; final int ALGOABFLIP_MAX = 1;
        int algoAbFlip = ALGOABFLIP_INVALID;
        int getAlgoAbFlip() {
            if (algoAbFlip < ALGOABFLIP_MIN || algoAbFlip > ALGOABFLIP_MAX) getHST_INV_ALG_PARM_2();
            return algoAbFlip;
        }
        boolean setAlgoAbFlip(int algoAbFlip) {
            return setAlgoAbFlip(algoAbFlip, algoRunTilZero);
        }

        final int ALGORUNTILZERO_INVALID = -1; final int ALGORUNTILZERO_MIN = 0; final int ALGORUNTILZERO_MAX = 1;
        int algoRunTilZero = ALGORUNTILZERO_INVALID;
        int getAlgoRunTilZero() {
            if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX) getHST_INV_ALG_PARM_2();
            return algoRunTilZero;
        }
        boolean setAlgoRunTilZero(int algoRunTilZero) {
            return setAlgoAbFlip(algoAbFlip, algoRunTilZero);
        }

        private boolean getHST_INV_ALG_PARM_2() {
            byte[] msgBuffer = new byte[]{(byte) 0x70, 0, 5, 9, 0, 0, 0, 0};
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_2, false, msgBuffer);
        }
        boolean setAlgoAbFlip(int algoAbFlip, int algoRunTilZero) {
            if (algoAbFlip < ALGOABFLIP_MIN || algoAbFlip > ALGOABFLIP_MAX)
                algoAbFlip = mDefault.algoAbFlip;
            if (algoRunTilZero < ALGORUNTILZERO_MIN || algoRunTilZero > ALGORUNTILZERO_MAX)
                algoRunTilZero = mDefault.algoRunTilZero;
            if (false) appendToLog("this.algoAbFlip  = " + this.algoAbFlip + ", algoAbFlip = " + algoAbFlip + ", this.algoRunTilZero = " + this.algoRunTilZero + ", algoRunTilZero = " + algoRunTilZero);
            if (false && this.algoAbFlip == algoAbFlip && this.algoRunTilZero == algoRunTilZero && sameCheck) return true;
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 5, 9, 0, 0, 0, 0};
            if (algoAbFlip != 0) {
                msgBuffer[4] |= 0x01;
            }
            if (algoRunTilZero != 0) {
                msgBuffer[4] |= 0x02;
            }
            this.algoAbFlip = algoAbFlip;
            this.algoRunTilZero = algoRunTilZero;
            return mRfidDevice.mRfidReaderChip.sendHostRegRequest(HostRegRequests.HST_INV_ALG_PARM_2, true, msgBuffer);
        }
    }

    class Rx000EngSetting {
        int narrowRSSI = -1, wideRSSI = -1;
        int getwideRSSI() {
            if (wideRSSI < 0) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x100, 0x05); //sub-command: 0x05, Arg0: reserved
                mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x101,  3 + 0x20000); //Arg1: 15-0: number of RSSI sample
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_ENGTEST);
            } else appendToLog("Hello123: wideRSSI = " + wideRSSI);
            return wideRSSI;
        }
        int getnarrowRSSI() {
            if (narrowRSSI < 0) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x100, 0x05); //sub-command: 0x05, Arg0: reserved
                mRfidDevice.mRfidReaderChip.mRx000Setting.writeMAC(0x101,  3 + 0x20000); //Arg1: 15-0: number of RSSI sample
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_ENGTEST);
            } else appendToLog("Hello123: narrowRSSI = " + wideRSSI);
            return wideRSSI;
        }
        public void resetRSSI() {
            narrowRSSI = -1; wideRSSI = -1;
        }
    }

    class Rx000MbpSetting {
        final int RXGAIN_INVALID = -1; final int RXGAIN_MIN = 0; final int RXGAIN_MAX = 0x1FF;
        int rxGain = RXGAIN_INVALID;
        int getHighCompression() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPAddress(0x450); appendToLog("70010004: getHighCompression");
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_MBPRDREG);
            } else iRetValue = (rxGain >> 8);
            return iRetValue;
        }
        int getRflnaGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPAddress(0x450); appendToLog("70010004: getRflnaGain");
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_MBPRDREG);
            } else iRetValue = ((rxGain & 0xC0) >> 6);
            return iRetValue;
        }
        int getIflnaGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPAddress(0x450); appendToLog("70010004: getIflnaGain");
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_MBPRDREG);
            } else iRetValue = ((rxGain & 0x38) >> 3);
            return iRetValue;
        }
        int getAgcGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPAddress(0x450); appendToLog("70010004: getAgcGain");
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_MBPRDREG);
            } else iRetValue = (rxGain & 0x07);
            return iRetValue;
        }
        int getRxGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPAddress(0x450);
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_MBPRDREG);
            } else iRetValue = rxGain;
            return iRetValue;
        }
        boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
            int rxGain_new = ((highCompression & 0x01) << 8) | ((rflnagain & 0x3) << 6) | ((iflnagain & 0x7) << 3) | (agcgain & 0x7);
            return setRxGain(rxGain_new);
        }
        boolean setRxGain(int rxGain_new) {
            boolean bRetValue = true;
            if ((rxGain_new != rxGain) || (sameCheck == false)) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                appendToLog("2 setRxGain");
                bRetValue = mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPAddress(0x450); if (false) appendToLog("70010004: setRxGain");
                if (bRetValue != false) bRetValue = mRfidDevice.mRfidReaderChip.mRx000Setting.setMBPData(rxGain_new);
                if (bRetValue != false) bRetValue = mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_MBPWRREG);
                if (bRetValue != false) rxGain = rxGain_new;
            }
            return bRetValue;
        }
    }

    class Rx000OemSetting {
        final int COUNTRYCODE_INVALID = -1; final int COUNTRYCODE_MIN = 1; final int COUNTRYCODE_MAX = 9;
        int countryCode = COUNTRYCODE_INVALID;   // OemAddress = 0x02
        int getCountryCode() {
            if (countryCode < COUNTRYCODE_MIN || countryCode > COUNTRYCODE_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(2);
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_RDOEM);
            }
            return countryCode;
        }

        final int SERIALCODE_INVALID = -1;
        byte[] serialNumber = new byte[] { SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0 };
        String getSerialNumber() {
            boolean invalid = false;
            int length = serialNumber.length / 4;
            if (serialNumber.length % 4 != 0)   length++;
            for (int i = 0; i < length; i++) {
                if (serialNumber[4 * i] == SERIALCODE_INVALID) {    // OemAddress = 0x04 - 7
                    invalid = true;
                    mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                    mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(0x04 + i);
                    mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_RDOEM);
                }
            }
            if (invalid)    return null;
            appendToLog("retValue = " + byteArrayToString(serialNumber));
            byte[] retValue = new byte[serialNumber.length];
            for (int i = 0; i < retValue.length; i++) {
                int j = (i/4)*4 + 3 - i%4;
                if (j >= serialNumber.length)   retValue[i] = serialNumber[i];
                else    retValue[i] = serialNumber[j];
                if (retValue[i] == 0) retValue[i] = 0x30;
            }
            appendToLog("retValue = " + byteArrayToString(retValue) + ", String = " + new String(retValue));
            return new String(retValue);
        }

        final int PRODUCT_SERIALCODE_INVALID = -1;
        byte[] productserialNumber = new byte[] { SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0, SERIALCODE_INVALID, 0, 0, 0 };
        String getProductSerialNumber() {
            boolean invalid = false;
            int length = productserialNumber.length / 4;
            if (productserialNumber.length % 4 != 0)   length++;
            for (int i = 0; i < length; i++) {
                if (productserialNumber[4 * i] == PRODUCT_SERIALCODE_INVALID) {    // OemAddress = 0x04 - 7
                    invalid = true;
                    appendToLog(i + " start setOEMAddress");
                    mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                    mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(0x08 + i);
                    mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_RDOEM);
                }
            }
            if (invalid)    return null;
            appendToLog("retValue = " + byteArrayToString(productserialNumber));
            byte[] retValue = new byte[productserialNumber.length];
            for (int i = 0; i < retValue.length; i++) {
                int j = (i/4)*4 + 3 - i%4;
                if (j >= productserialNumber.length)   retValue[i] = productserialNumber[i];
                else    retValue[i] = productserialNumber[j];
                if (retValue[i] == 0) retValue[i] = 0x30;
            }
            appendToLog("retValue = " + byteArrayToString(retValue) + ", String = " + new String(retValue));
            return new String(retValue);
        }

        final int VERSIONCODE_INVALID = -1; final int VERSIONCODE_MIN = 1; final int VERSIONCODE_MAX = 9;
        int versionCode = VERSIONCODE_INVALID;   // OemAddress = 0x02
        int getVersionCode() {
            if (versionCode < VERSIONCODE_MIN || versionCode > VERSIONCODE_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(0x0B);
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_RDOEM);
            }
            return versionCode;
        }

        String spcialCountryVersion = null;
        String getSpecialCountryVersion() {
            if (spcialCountryVersion == null) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(0x8E);
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_RDOEM);
                return "";
            }
            return spcialCountryVersion.replaceAll("[^A-Za-z0-9]", "");
        }

        final int FREQMODIFYCODE_INVALID = -1; final int FREQMODIFYCODE_MIN = 0; final int FREQMODIFYCODE_MAX = 0xAA;
        int freqModifyCode = FREQMODIFYCODE_INVALID;   // OemAddress = 0x8A
        int getFreqModifyCode() {
            if (freqModifyCode < FREQMODIFYCODE_MIN || freqModifyCode > FREQMODIFYCODE_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(0x8F);
                mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_RDOEM);
            }
            return freqModifyCode;
        }

        void writeOEM(int address, int value) {
            mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
            mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMAddress(address);
            mRfidDevice.mRfidReaderChip.mRx000Setting.setOEMData(value);
            mRfidDevice.mRfidReaderChip.sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands.CMD_WROEM);
        }
    }

    boolean bFirmware_reset_before = false;
    final int RFID_READING_BUFFERSIZE = 600; //1024;
    class RfidReaderChip {
        byte[] mRfidToReading = new byte[RFID_READING_BUFFERSIZE];
        int mRfidToReadingOffset = 0;
        ArrayList<Cs108RfidData> mRx000ToWrite = new ArrayList<>();

        Rx000Setting mRx000Setting = new Rx000Setting(true);
        Rx000EngSetting mRx000EngSetting = new Rx000EngSetting();
        Rx000MbpSetting mRx000MbpSetting = new Rx000MbpSetting();
        Rx000OemSetting mRx000OemSetting = new Rx000OemSetting();

        ArrayList<Cs710Library4A.Rx000pkgData> mRx000ToRead = new ArrayList<>();
        private boolean clearTempDataIn_request = false;
        boolean commandOperating;

        double decodeNarrowBandRSSI(byte byteRSSI) {
            byte mantissa = byteRSSI;
            mantissa &= 0x07;
            byte exponent = byteRSSI;
            exponent >>= 3;
            double dValue = 20 * log10(pow(2, exponent) * (1 + (mantissa / pow(2, 3))));
            if (false) appendToLog("byteRSSI = " + String.format("%X", byteRSSI) + ", mantissa = " + mantissa + ", exponent = " + exponent + "dValue = " + dValue);
            return dValue;
        }
        int encodeNarrowBandRSSI(double dRSSI) {
            double dValue = dRSSI / 20;
            dValue = pow(10, dValue);
            int exponent = 0;
            if (false) appendToLog("exponent = " + exponent + ", dValue = " + dValue);
            while ((dValue + 0.062) >= 2) {
                dValue /= 2; exponent++;
                if (false) appendToLog("exponent = " + exponent + ", dValue = " + dValue);
            }
            dValue--;
            int mantissa = (int)((dValue * 8) + 0.5);
            while (mantissa >= 8) {
                mantissa -= 8; exponent++;
            }
            int iValue = ((exponent & 0x1F) << 3) | (mantissa & 0x7);
            if (false) appendToLog("dRssi = " + dRSSI + ", exponent = " + exponent + ", mantissa = " + mantissa + ", iValue = " + String.format("%X", iValue));
            return iValue;
        }

        long firmware_ontime_ms = 0; long date_time_ms = 0; boolean bRx000ToReading = false;
        int getBytes2EpcLength(byte[] bytes) {
            int iValue = ((bytes[0] & 0xFF) >> 3) * 2;
            if (false) appendToLog("bytes = " + byteArrayToString(bytes) + ", iValue = " + iValue);
            return iValue;
        }
        void mRx000UplinkHandler() {
            boolean DEBUG = false;
            if (bRx000ToReading) return;
            bRx000ToReading = true;
            int startIndex = 0, startIndexOld = 0, startIndexNew = 0;
            boolean packageFound = false;
            int packageType = 0;
            long lTime = System.currentTimeMillis();
            if (mRfidDevice.mRfidToRead.size() != 0) { if (DEBUGTHREAD) appendToLog("mRx000UplinkHandler(): START with mRfidToRead size = " + mRfidDevice.mRfidToRead.size() + ", mRx000ToRead size = " + mRx000ToRead.size()); }
            else if (DEBUGTHREAD) appendToLog("START AAA with mRx000ToRead size = " + mRx000ToRead.size());
            if (false && mRx000ToRead.size() != 0) appendToLog("START AAA with mRx000ToRead size = " + mRx000ToRead.size());
            boolean bFirst = true;
            while (mRfidDevice.mRfidToRead.size() != 0) {
                if (DEBUG) appendToLog("Looping with mRfidToRead.size = " + mRfidDevice.mRfidToRead.size() + " with bleConnected = " + isBleConnected());
                if (isBleConnected() == false) {
                    mRfidDevice.mRfidToRead.clear();
                    appendToLog("BLE DISCONNECTED !!! mRfidToRead.size() = " + mRfidDevice.mRfidToRead.size());
                } else if (System.currentTimeMillis() - lTime > (intervalRx000UplinkHandler/2)) {
                    writeDebug2File("D" + String.valueOf(intervalRx000UplinkHandler) + ", " + System.currentTimeMillis() + ", Timeout");
                    appendToLogView("TIMEOUT !!! mRfidToRead.size() = " + mRfidDevice.mRfidToRead.size());
                    break;
                } else {
                    if (DEBUG) appendToLog("Check bFirst = " + bFirst);
                    if (bFirst) { bFirst = false; writeDebug2File("D" + String.valueOf(intervalRx000UplinkHandler) + ", " + System.currentTimeMillis()); }
                    byte[] dataIn = mRfidDevice.mRfidToRead.get(0).dataValues;
                    long tagMilliSeconds = mRfidDevice.mRfidToRead.get(0).milliseconds;
                    boolean invalidSequence = mRfidDevice.mRfidToRead.get(0).invalidSequence;
                    if (DEBUG_APDATA) appendToLog("ApData: found mRfidToRead data with invalidSequence= " + invalidSequence + ", bytes= " + byteArrayToString(dataIn));
                    mRfidDevice.mRfidToRead.remove(0);

                    if (DEBUG) appendToLog("Check buffer size: data.length = " + dataIn.length+ ", mRfidToReading.length = " + mRfidToReading.length + ", mRfidToReadingOffset = " + mRfidToReadingOffset);
                    if (dataIn.length >= mRfidToReading.length - mRfidToReadingOffset) {
                        if (mRfidToReadingOffset != 0) {
                            byte[] unhandledBytes = new byte[mRfidToReadingOffset];
                            System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                            appendToLogView("!!! ERROR insufficient buffer, mRfidToReadingOffset=" + mRfidToReadingOffset + ", dataIn.length=" + dataIn.length + ", clear mRfidToReading: " + byteArrayToString(unhandledBytes));
                            byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                            mRfidToReading = mRfidToReadingNew;
                            mRfidToReadingOffset = 0;
                            invalidUpdata++;
                        }
                        if (dataIn.length >= mRfidToReading.length - mRfidToReadingOffset) {
                            appendToLogView("!!! ERROR insufficient buffer, mRfidToReading.length=" + mRfidToReading.length + ", dataIn.length=" + dataIn.length + ", clear mRfidToReading: " + byteArrayToString(dataIn));
                            invalidata++;
                            break;
                        }
                    }

                    if (DEBUG) appendToLog("Check invalidSequence = " + invalidSequence + " with mRfidToReadingOffset = " + mRfidToReadingOffset);
                    if (mRfidToReadingOffset != 0 && invalidSequence) {
                        byte[] unhandledBytes = new byte[mRfidToReadingOffset];
                        System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                        if (true) appendToLog("!!! ERROR invalidSequence with nonzero mRfidToReadingOffset=" + mRfidToReadingOffset + ", throw invalid unused data=" + unhandledBytes.length + ", " + byteArrayToString(unhandledBytes));
                        mRfidToReadingOffset = 0;
                        startIndex = 0;
                        startIndexNew = 0;
                    }

                    System.arraycopy(dataIn, 0, mRfidToReading, mRfidToReadingOffset, dataIn.length);
                    mRfidToReadingOffset += dataIn.length;

                    int iPayloadSizeMin = 7; //boolean bprinted = false;
                    while (mRfidToReadingOffset - startIndex >= iPayloadSizeMin) {
                        //if (bprinted == false) { bprinted = true; appendToLog(byteArrayToString(mRfidToReading)); }
                        int packageLengthRead = (mRfidToReading[startIndex + 5] & 0xFF) * 256 + (mRfidToReading[startIndex + 6] & 0xFF);
                        int expectedLength = 7 + (mRfidToReading[startIndex + 5] & 0xFF) * 256 + (mRfidToReading[startIndex + 6] & 0xFF);
                        if (DEBUG) appendToLog("Looping with startIndex = " + startIndex + ", mRfidToReadingOffset = " + mRfidToReadingOffset + ", iPayloadSizeMin = " + iPayloadSizeMin + ", expectedLength = " + expectedLength);
                        if (true) {
                            if (mRfidToReading[startIndex + 0] == 0x49
                                    && mRfidToReading[startIndex + 1] == (byte) 0xdc
                                    && (mRfidToReadingOffset - startIndex >= expectedLength) && (expectedLength > 7)
                            ) {
                                byte[] header = new byte[7], payload = new byte[expectedLength - 7];
                                System.arraycopy(mRfidToReading, startIndex, header, 0, header.length);
                                System.arraycopy(mRfidToReading, startIndex + 7, payload, 0, payload.length);
                                int iUplinkPackageType = (mRfidToReading[startIndex + 2] & 0xFF) * 256 + (mRfidToReading[startIndex + 3] & 0xFF);
                                if (DEBUG_APDATA) appendToLog(String.format("ApData: found Rfid.Uplink.DataRead.UplinkPackage_%04X with payload = ", iUplinkPackageType) + byteArrayToString(payload));
                                Cs710Library4A.Rx000pkgData dataA = new Cs710Library4A.Rx000pkgData();
                                dataA.dataValues = new byte[expectedLength - 7];
                                System.arraycopy(mRfidToReading, startIndex + 7, dataA.dataValues, 0, dataA.dataValues.length);
                                if (iUplinkPackageType == 0x3001 || iUplinkPackageType == 0x3003) {
                                    dataA.responseType = Cs710Library4A.HostCmdResponseTypes.TYPE_18K6C_INVENTORY;
                                    //mRfidDevice.setInventoring(true);
                                    if (DEBUG) appendToLog("Check UplinkPackage_Event_csl_tag_read_epc_only_new data length = " + dataA.dataValues.length);
                                    if ((iUplinkPackageType == 0x3001 && dataA.dataValues.length < 17)
                                    || (iUplinkPackageType == 0x3003 && dataA.dataValues.length < 18)) {
                                        appendToLog("!!! UplinkPackage_Event_csl_tag_read_epc_only_new data length has length equal or less than 15");
                                        dataA.decodedError = "Received UplinkPackage_Event_csl_tag_read_epc_only_new with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                    } else {
                                        dataA.decodedTime = System.currentTimeMillis();
                                        dataA.decodedRssi = get2BytesOfRssi(dataA.dataValues, 4);
                                        if (DEBUG) appendToLog("decoded decodedRssi = " + dataA.decodedRssi);
                                        dataA.decodedPhase = (dataA.dataValues[6] & 0xFF) * 256 + (dataA.dataValues[7] & 0xFF);
                                        if (DEBUG) appendToLog("decoded decodedPhase = " + dataA.decodedPhase);
                                        dataA.decodedPort = (dataA.dataValues[10] & 0xFF);
                                        if (DEBUG) appendToLog("decoded decodedPort = " + dataA.decodedPort);
                                        dataA.decodedChidx = 1; //(dataA.dataValues[13] & 0xFF) * 256 + (dataA.dataValues[14] & 0xFF);
                                        if (DEBUG) appendToLog("decoded decodedChidx = " + dataA.decodedChidx);
                                        dataA.decodedPc = new byte[2]; System.arraycopy(dataA.dataValues, 15, dataA.decodedPc, 0, dataA.decodedPc.length);
                                        if (DEBUG) appendToLog("decoded decodedPc = " + byteArrayToString(dataA.decodedPc));
                                        if (iUplinkPackageType == 0x3001) {
                                            dataA.decodedEpc = new byte[dataA.dataValues.length - 17];
                                            System.arraycopy(dataA.dataValues, 17, dataA.decodedEpc, 0, dataA.decodedEpc.length);
                                        } else {
                                            int iEpcLength = getBytes2EpcLength(dataA.decodedPc);
                                            if (DEBUG) appendToLog("dataA.dataValues.length = " + dataA.dataValues.length + ", iEpcLength = " + iEpcLength + " for data " + byteArrayToString(dataA.dataValues));
                                            if (dataA.dataValues.length - 18 > iEpcLength) {
                                                dataA.decodedEpc = new byte[dataA.dataValues.length - 18];
                                                System.arraycopy(dataA.dataValues, 17, dataA.decodedEpc, 0, iEpcLength);
                                                System.arraycopy(dataA.dataValues, iEpcLength + 18, dataA.decodedEpc, iEpcLength, dataA.dataValues.length - iEpcLength - 18);
                                                if (DEBUG) appendToLog("decodedEpc = " + byteArrayToString(dataA.decodedEpc));

                                                int iMbDataLength = dataA.dataValues.length - 18 - iEpcLength;
                                                int iDataIndex = 0, iDataOffset = 0;
                                                for (int i = 0; i < 3; i++) {
                                                    int iValue = mRfidDevice.mRfidReaderChip.mRx000Setting.getMultibankReadLength(i);
                                                    if (DEBUG) appendToLog("i = " + i + ", getMultibankReadLength = " + iValue);
                                                    if (iValue != 0) {
                                                        int iBankLength = iValue * 2;
                                                        if (DEBUG) appendToLog("Check iDataIndex = " + iDataIndex + ", iDataOffset = " + iDataOffset + ", iBankLength = " + iBankLength + ", iMbDataLength = " + iMbDataLength);
                                                        if (iDataOffset + iBankLength > iMbDataLength) appendToLog("!!! iBankLength " + iBankLength + " is too long for iDataOffset " + iDataOffset + ", iMbDataLength = " + iMbDataLength);
                                                        else {
                                                            if (iDataIndex == 0) {
                                                                dataA.decodedData1 = new byte[iBankLength];
                                                                System.arraycopy(dataA.dataValues, iEpcLength + 18 + iDataOffset, dataA.decodedData1, 0, dataA.decodedData1.length);
                                                                if (DEBUG) appendToLog("decodedData1 = " + byteArrayToString(dataA.decodedData1));
                                                                iDataIndex++; iDataOffset += iBankLength;
                                                            } else if (iDataIndex == 1) {
                                                                dataA.decodedData2 = new byte[iBankLength];
                                                                System.arraycopy(dataA.dataValues, iEpcLength + 18 + iDataOffset, dataA.decodedData2, 0, dataA.decodedData2.length);
                                                                if (DEBUG) appendToLog("decodedData2 = " + byteArrayToString(dataA.decodedData2));
                                                                iDataIndex++; iDataOffset += iBankLength;
                                                            } else appendToLog("!!! CANNOT handle the third multibank data");
                                                        }
                                                    }
                                                }
                                                if (iDataOffset != iMbDataLength) appendToLog("!!! Some unhandled data as iDataOffset = " + iDataOffset + " for iMbDataLength = " + iMbDataLength);
                                                else if (DEBUG) appendToLog("iDataOffset = iMbDataLength = " + iMbDataLength);
                                            } else appendToLog("!!! iEpcLength " + iEpcLength + " is too long for the data " + byteArrayToString(dataA.dataValues));
                                        }
                                        mRx000ToRead.add(dataA);
                                        if (DEBUG) appendToLog("3001/3003 dataA.responseType = " + dataA.responseType.toString());
                                        if (DEBUG) appendToLog("decoded decodedEpc = " + byteArrayToString(dataA.decodedEpc) + " with mRx000ToRead.size = " + mRx000ToRead.size());
                                        if (DEBUG_APDATA) appendToLog("ApData: uplink data UplinkPackage_Event_csl_tag_read_epc_only_new tag with Epc = " + byteArrayToString(dataA.decodedEpc) + " is uploaded to mRx000ToRead with mRx000ToRead.size = " + mRx000ToRead.size());
                                        if (DEBUG_APDATA) appendToLog("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_tag_read_epc_only_new has been processed");
                                    }
                                } else if (iUplinkPackageType == 0x3006) {
                                    dataA.responseType = Cs710Library4A.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                                    //mRfidDevice.setInventoring(true);
                                    if (DEBUG) appendToLog("Check UplinkPackage_Event_csl_tag_read_compact data length = " + dataA.dataValues.length);
                                    if (dataA.dataValues.length < 10) {
                                        appendToLog("!!! UplinkPackage_Event_csl_tag_read_compact data length has length equal or less than 6");
                                        dataA.decodedError = "Received Event_csl_tag_read_compact with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                    } else {
                                        int index = 0;
                                        byte[] dataHeader = new byte[6]; System.arraycopy(dataA.dataValues, 0, dataHeader, 0, dataHeader.length);
                                        byte[] dataValuesFull = new byte[dataA.dataValues.length - 6]; System.arraycopy(dataA.dataValues, 6, dataValuesFull, 0, dataValuesFull.length);
                                        if (DEBUG_APDATA) appendToLog("ApData: found Rfid.Uplink.DataRead.UplinkPackage_Event_csl_tag_read_compact with payload header = " + byteArrayToString(dataHeader) + ", dataValuesFull = " + byteArrayToString(dataValuesFull));
                                        while (index < dataValuesFull.length) { //change from while
                                            if (DEBUG) appendToLog("Looping with index = " + index + ", dataValuesFull.length = " + dataValuesFull.length);
                                            dataA.decodedTime = System.currentTimeMillis();
                                            if (dataValuesFull.length >= index + 2) {
                                                dataA.decodedPc = new byte[2];
                                                System.arraycopy(dataValuesFull, index, dataA.decodedPc, 0, dataA.decodedPc.length);
                                                index += 2;
                                            } else break;

                                            int epcLength = getBytes2EpcLength(dataA.decodedPc); //((dataA.decodedPc[0] & 0xFF) >> 3) * 2;
                                            if (DEBUG) appendToLog("decoded decodedPc = " + byteArrayToString(dataA.decodedPc) + " with epclength = " + epcLength);
                                            if (dataValuesFull.length >= index + epcLength) {
                                                dataA.decodedEpc = new byte[epcLength];
                                                System.arraycopy(dataValuesFull, index, dataA.decodedEpc, 0, epcLength);
                                                index += epcLength;
                                            } else break;

                                            if (DEBUG) appendToLog("decoded decodedEpc = " + byteArrayToString(dataA.decodedEpc));
                                            if (dataValuesFull.length >= index + 2) {
                                                dataA.decodedRssi = get2BytesOfRssi(dataValuesFull, index);
                                                if (DEBUG) appendToLog("decoded decodedRssi = " + dataA.decodedRssi);
                                                index += 2;
                                            } else break;

                                            mRx000ToRead.add(dataA);
                                            if (DEBUG) appendToLog("3006 dataA.responseType = " + dataA.responseType.toString());
                                            if (DEBUG_APDATA) appendToLog("ApData: uplink data UplinkPackage_Event_csl_tag_read_compact tag with Epc = " + byteArrayToString(dataA.decodedEpc) + " is uploaded to mRx000ToRead with mRx000ToRead.size = " + mRx000ToRead.size());

                                            dataA = new Cs710Library4A.Rx000pkgData();
                                            dataA.responseType = Cs710Library4A.HostCmdResponseTypes.TYPE_18K6C_INVENTORY_COMPACT;
                                        }
                                        if (DEBUG) appendToLog("Exit while loop with index = " + index + ", dataValuesFull.length = " + dataValuesFull.length);
                                        if (index != dataValuesFull.length) {
                                            byte[] bytesUnhandled = new byte[dataValuesFull.length - index];
                                            System.arraycopy(dataValuesFull, index, bytesUnhandled, 0, bytesUnhandled.length);
                                            appendToLog("!!! unhandled data: " + byteArrayToString(bytesUnhandled));
                                        }
                                        if (DEBUG_APDATA) appendToLog("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_tag_read_compact has been processed");
                                    }
                                } else if (iUplinkPackageType == 0x3007) {
                                    DEBUG = true;
                                    if (DEBUG) appendToLog("Check UplinkPackage_Event_csl_miscellaneous_event data length = " + dataA.dataValues.length);
                                    int iCommand = (dataA.dataValues[4] & 0xFF) * 256 + (dataA.dataValues[5] & 0xFF);
                                    if (dataA.dataValues.length < 6 || (iCommand >= 3 && dataA.dataValues.length < 8)) {
                                        appendToLog("!!! UplinkPackage_Event_csl_miscellaneous_event data length has length equal or less than 8");
                                    } else {
                                        switch (iCommand) {
                                            case 1:
                                                mRx000Setting.keepAliveTime = new Date();
                                                break;
                                            case 2:
                                                mRx000Setting.inventoryRoundEndTime = new Date();
                                                break;
                                            case 3:
                                                mRx000Setting.crcErrorRate = ((dataA.dataValues[6] & 0xFF) << 8) + (dataA.dataValues[7] & 0xFF);
                                                break;
                                            case 4:
                                                mRx000Setting.tagRate = ((dataA.dataValues[6] & 0xFF) << 8) + (dataA.dataValues[7] & 0xFF);
                                                break;
                                            default:
                                                appendToLog("!!! iCommand cannot be recognised for the uplink data " + byteArrayToString(dataA.dataValues));
                                                break;
                                        }
                                        if (DEBUG_PKDATA) appendToLog("PkData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_miscellaneous_event has been processed");
                                    }
                                } else if (iUplinkPackageType == 0x3008) {
                                    dataA.responseType = Cs710Library4A.HostCmdResponseTypes.TYPE_COMMAND_END;
                                    mRfidDevice.setInventoring(false);
                                    appendToLogView("mRx000UplinkHandler_3008: " + byteArrayToString(dataA.dataValues));
                                    if (DEBUG) appendToLog("Check UplinkPackage_Event_csl_operation_complete data length = " + dataA.dataValues.length);
                                    if (dataA.dataValues.length < 8) {
                                        appendToLog("!!! UplinkPackage_Event_csl_operation_complete data length has length equal or less than 8");
                                        dataA.decodedError = "Received Event_csl_operation_complete with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                    } else {
                                        int iCommand = (dataA.dataValues[4] & 0xFF) * 256 + (dataA.dataValues[5] & 0xFF);
                                        int iStatus = (dataA.dataValues[6] & 0xFF) * 256 + (dataA.dataValues[7] & 0xFF);
                                        if (DEBUG_APDATA) appendToLog("ApData: found Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete");
                                        if (DEBUG) appendToLog("Check iStatus = " + iStatus);
                                        switch (iStatus) {
                                            case 0:
                                                dataA.decodedError = null;
                                                break;
                                            case 1:
                                                dataA.decodedError = "Tag cache table buffer is overflowed";
                                                break;
                                            case 2:
                                                dataA.decodedError = "Wrong register address";
                                                break;
                                            case 3:
                                                dataA.decodedError = "Register length too large";
                                                break;
                                            case 4:
                                                dataA.decodedError = "E710 not powered up";
                                                break;
                                            case 5:
                                                dataA.decodedError = "Invalid parameter";
                                                break;
                                            case 6:
                                                dataA.decodedError = "Event fifo full";
                                                break;
                                            case 7:
                                                dataA.decodedError = "TX not ramped up";
                                                break;
                                            case 8:
                                                dataA.decodedError = "Register read only";
                                                break;
                                            case 9:
                                                dataA.decodedError = "Failed to halt";
                                            case 10:
                                                dataA.decodedError = "PLL not locked";
                                                break;
                                            case 11:
                                                dataA.decodedError = "Power control target failed";
                                                break;
                                            case 12:
                                                dataA.decodedError = "Radio power not enabled";
                                                break;
                                            case 13:
                                                dataA.decodedError = "E710 command error";
                                                break;
                                            case 14:
                                                dataA.decodedError = "E710 Op timeout";
                                                break;
                                            case 15:
                                                dataA.decodedError = "E710 Aggregate error";
                                                break;
                                            case 0xFFF:
                                                dataA.decodedError = "Other error";
                                                break;
                                            default:
                                                dataA.decodedError = "Unknown error";
                                                appendToLog("!!! CANNOT handle status type with " + byteArrayToString(header) + "." + byteArrayToString(payload));
                                                break;
                                        }
                                        mRx000ToRead.add(dataA);
                                        if (DEBUG) appendToLog("3008 dataA.responseType = " + dataA.responseType.toString());
                                        if (DEBUG_APDATA) appendToLog("ApData: uplink data UplinkPackage_Event_csl_operation_complete with decodedError = " + dataA.decodedError + " is uploaded to mRx000ToRead with mRx000ToRead.size = " + mRx000ToRead.size());
                                        if (DEBUG_APDATA) appendToLog("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_operation_complete has been processed");
                                    }
                                } else if (iUplinkPackageType == 0x3009) {
                                    dataA.responseType = Cs710Library4A.HostCmdResponseTypes.TYPE_18K6C_TAG_ACCESS;
                                    if (DEBUG) appendToLog("Check UplinkPackage_Event_csl_access_complete data length = " + dataA.dataValues.length);
                                    if (dataA.dataValues.length < 12) {
                                        appendToLog("!!! UplinkPackage_Event_csl_access_complete data length has length equal or less than 12");
                                        dataA.decodedError = "Received Event_csl_access_complete with length = " + String.valueOf(dataA.dataValues.length) + ", data = " + byteArrayToString(dataA.dataValues);
                                    } else {
                                        int iCommand = (dataA.dataValues[4] & 0xFF) * 256 + (dataA.dataValues[5] & 0xFF);
                                        int iTagError = dataA.dataValues[6];
                                        int iMacError = dataA.dataValues[7];
                                        int iWriteCount = dataA.dataValues[8] * 256 + dataA.dataValues[9];
                                        byte[] bytesResponse = null, bytesHeader = new byte[12];
                                        System.arraycopy(dataA.dataValues, 0, bytesHeader, 0, bytesHeader.length);
                                        if (dataA.dataValues.length > 12) {
                                            bytesResponse = new byte[dataA.dataValues.length - 12];
                                            System.arraycopy(dataA.dataValues, 12, bytesResponse, 0, bytesResponse.length);
                                            if (DEBUG)
                                                appendToLog("bytesResponse = " + byteArrayToString(bytesResponse));
                                        }
                                        String string = null;
                                        switch (iTagError) {
                                            case 0x00:
                                                string = "Other error";
                                                break;
                                            case 0x01:
                                                string = "Not supported";
                                                break;
                                            case 0x02:
                                                string = "Insufficient privileges";
                                                break;
                                            case 0x03:
                                                string = "Memory overrun";
                                                break;
                                            case 0x04:
                                                string = "Memory locked";
                                                break;
                                            case 0x05:
                                                string = "Crypto suite error";
                                                break;
                                            case 0x06:
                                                string = "Command not encapsulated";
                                                break;
                                            case 0x07:
                                                string = "ResponseBuffer overflow";
                                                break;
                                            case 0x08:
                                                string = "Security timeout";
                                                break;
                                            case 0x0B:
                                                string = "Insufficient power";
                                                break;
                                            case 0x0F:
                                                string = "Non-specific error";
                                                break;
                                            case 0x10:
                                                //string = "No error";
                                                break;
                                            default:
                                                string = "OTHER errors";
                                                break;
                                        }
                                        if (string != null)
                                            dataA.decodedError = "Tag Error: " + string;
                                        string = null;
                                        switch (iMacError) {
                                            case 0x00:
                                                //string = "No error";
                                                break;
                                            case 0x01:
                                                string = "No tag reply";
                                                break;
                                            case 0x02:
                                                string = "Invalid password";
                                                break;
                                            case 0x03:
                                                string = "Failed to send command";
                                                break;
                                            case 0x04:
                                                string = "No access reply";
                                                break;
                                            default:
                                                string = "OTHER errors";
                                                break;
                                        }
                                        if (string != null) {
                                            if (dataA.decodedError == null)
                                                dataA.decodedError = "Mac Error: " + string;
                                            else dataA.decodedError += (", Mac Error: " + string);
                                        }
                                        if (iCommand == 0xC3 && iWriteCount == 0) {
                                            string = "Write Error: nothing is written";
                                            if (dataA.decodedError == null)
                                                dataA.decodedError = string;
                                            else dataA.decodedError += (", " + string);
                                            appendToLog(String.format("rx000pkgData: Command 0x%X with mRfidToWrite.size = %s", iCommand, mRfidDevice.mRfidToWrite.size()));
                                        }
                                        if (DEBUG)
                                            appendToLog("decodedError2 = " + dataA.decodedError);
                                        appendToLog("bytesResponse is " + (bytesResponse == null ? "null" : byteArrayToString(bytesResponse)));
                                        if (bytesResponse != null && bytesResponse.length != 0) {
                                            for (int i = 0; i < bytesResponse.length; i++) {
                                                string = String.format("%02X", (byte) ((bytesResponse[i] & 0xFF)));
                                                if (dataA.decodedResult == null)
                                                    dataA.decodedResult = string;
                                                else dataA.decodedResult += string;
                                            }
                                        } else dataA.decodedResult = "";
                                        if (DEBUG || true)
                                            appendToLog("decodedResult = " + dataA.decodedResult);
                                    }
                                    mRx000ToRead.add(dataA);
                                    if (DEBUG) appendToLog("3009 dataA.responseType = " + dataA.responseType.toString());
                                    if (DEBUG_APDATA) appendToLog("ApData: uplink data UplinkPackage_Event_csl_access_complete tag with data = " + byteArrayToString(dataA.dataValues) + " is uploaded to mRx000ToRead with mRx000ToRead.size = " + mRx000ToRead.size());
                                    if (DEBUG_APDATA) appendToLog("ApData: Rfid.Uplink.DataRead.UplinkPackage_Event_csl_access_complete has been processed");
                                }
                                else appendToLog(String.format("!!! CANNOT handle UplinkPackageType 0x%X", iUplinkPackageType) +  " with uplink data "  + byteArrayToString(header) + "." + byteArrayToString(payload));
                                packageFound = true;
                                packageType = 4;
                                startIndexNew = startIndex + expectedLength;
                            }
                        }

                        if (packageFound) {
                            packageFound = false;
                            if (DEBUG) appendToLog("Found package with packageType = " + packageType + ", Check startIndex = " + startIndex + " with startIndexNew = " + startIndexNew + ", mRfidToReadingOffset = " + mRfidToReadingOffset);
                            if (DEBUG && startIndex != 0) {
                                byte[] unhandledBytes = new byte[startIndex];
                                System.arraycopy(mRfidToReading, 0, unhandledBytes, 0, unhandledBytes.length);
                                appendToLog("!!! packageFound with invalid unused data: " + unhandledBytes.length + ", " + byteArrayToString(unhandledBytes));
                                invalidUpdata++;
                            }
                            if (DEBUG) {
                                byte[] usedBytes = new byte[startIndexNew - startIndex];
                                System.arraycopy(mRfidToReading, startIndex, usedBytes, 0, usedBytes.length);
                                if (DEBUG) appendToLog("used data = " + usedBytes.length + ", " + byteArrayToString(usedBytes));
                            }
                            byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                            System.arraycopy(mRfidToReading, startIndexNew, mRfidToReadingNew, 0, mRfidToReadingOffset - startIndexNew);
                            mRfidToReading = mRfidToReadingNew;
                            mRfidToReadingOffset -= startIndexNew;
                            startIndex = 0;
                            startIndexNew = 0;
                            startIndexOld = 0;
                            if (DEBUG) appendToLog("Check new mRfidToReadingOffset = " + mRfidToReadingOffset + " with startIndex and startIndexNew = 0");
                            if (DEBUG && mRfidToReadingOffset != 0) {
                                byte[] remainedBytes = new byte[mRfidToReadingOffset];
                                System.arraycopy(mRfidToReading, 0, remainedBytes, 0, remainedBytes.length);
                                appendToLog("!!! moved with remained bytes=" + byteArrayToString(remainedBytes));
                            }
                        } else {
                            startIndex++;
                        }
                    }
                    if (DEBUG) appendToLog("Exit while loop with startIndex = " + startIndex + ", mRfidToReadingOffset = " + mRfidToReadingOffset + ", iPayloadSizeMin = " + iPayloadSizeMin);
                    if (startIndex != 0 && mRfidToReadingOffset != 0) {
                        //appendToLog("exit while(-8) loop with startIndex = " + startIndex + ( startIndex == 0 ? "" : "(NON-ZERO)" ) + ", mRfidToReadingOffset=" + mRfidToReadingOffset);
                        if (startIndex > mRfidToReadingOffset) appendToLog("!!! ERROR. startIndex = " + startIndex + " is greater than mRfidToReadingOffset = " + mRfidToReadingOffset);
                        else {
                            byte[] unhandled = new byte[startIndex];
                            System.arraycopy(mRfidToReading, 0, unhandled, 0, unhandled.length);
                            appendToLog("!!! Unhandled data: " + byteArrayToString(unhandled));
                            byte[] mRfidToReadingNew = new byte[RFID_READING_BUFFERSIZE];
                            System.arraycopy(mRfidToReading, startIndex, mRfidToReadingNew, 0, mRfidToReadingOffset - startIndex);
                            mRfidToReading = mRfidToReadingNew;
                            mRfidToReadingOffset = mRfidToReadingOffset - startIndex;
                            startIndex = 0;
                            startIndexNew = 0;
                            invalidUpdata++;
                        }
                    }
                }
            }
            if (DEBUG & bFirst == false) appendToLog("Exit while loop with mRfidToRead.size = " + mRfidDevice.mRfidToRead.size());
            /*if (DEBUG) appendToLog("mRfidToReadingOffset = " + mRfidToReadingOffset + ", startIndexNew = " + startIndexNew);
            if (mRfidToReadingOffset == startIndexNew && mRfidToReadingOffset != 0) {
                byte[] unusedData = new byte[mRfidToReadingOffset];
                System.arraycopy(mRfidToReading, 0, unusedData, 0, unusedData.length);
                appendToLog("Ending with invaid unused data: " + mRfidToReadingOffset + ", " + byteArrayToString(unusedData));
                mRfidToReading = new byte[RFID_READING_BUFFERSIZE];
                mRfidToReadingOffset = 0;
            }*/
            bRx000ToReading = false;
            if (DEBUGTHREAD) {
                if (mRx000ToRead.size() != 0) appendToLog("mRx000UplinkHandler(): END with mRx000ToRead size = " + mRx000ToRead.size());
            }
        }
        boolean turnOn(boolean onStatus) {
            Cs108RfidData cs108RfidData = new Cs108RfidData();
            if (onStatus) {
                cs108RfidData.rfidPayloadEvent = RfidPayloadEvents.RFID_POWER_ON;
                cs108RfidData.waitUplinkResponse = false;
                clearTempDataIn_request = true;
                addRfidToWrite(cs108RfidData);
                return true;
            } else if (onStatus == false) {
                cs108RfidData.rfidPayloadEvent = RfidPayloadEvents.RFID_POWER_OFF;
                cs108RfidData.waitUplinkResponse = false;
                clearTempDataIn_request = true;
                addRfidToWrite(cs108RfidData);
                return true;
            } else {
                return false;
            }
        }

        boolean sendControlCommand(ControlCommands controlCommands) {
            byte[] msgBuffer = new byte[]{(byte) 0x40, 6, 0, 0, 0, 0, 0, 0};
            boolean needResponse = false;
            if (isBleConnected() == false) return false;
            switch (controlCommands) {
                default:
                    msgBuffer = null;
                case CANCEL:
                    msgBuffer[1] = 1;
                    commandOperating = false;
                    break;
                case SOFTRESET:
                    msgBuffer[1] = 2;
                    needResponse = true;
                    break;
                case ABORT:
                    msgBuffer[1] = 3;
                    needResponse = true;
                    commandOperating = false;
                    break;
                case PAUSE:
                    msgBuffer[1] = 4;
                    break;
                case RESUME:
                    msgBuffer[1] = 5;
                    break;
                case GETSERIALNUMBER:
                    msgBuffer = new byte[]{(byte) 0xC0, 0x06, 0, 0, 0, 0, 0, 0};
                    needResponse = true;
                    break;
                case RESETTOBOOTLOADER:
                    msgBuffer[1] = 7;
                    needResponse = true;
                    break;
            }

            if (msgBuffer == null) {
                if (DEBUG) appendToLog("Invalid control commands");
                return false;
            } else {
                clearTempDataIn_request = true;

                Cs108RfidData cs108RfidData = new Cs108RfidData();
                cs108RfidData.rfidPayloadEvent = Cs108Connector.RfidPayloadEvents.RFID_COMMAND;
                cs108RfidData.dataValues = msgBuffer;
                if (needResponse) {
//                    if (DEBUG) appendToLog("sendControlCommand() adds to mRx000ToWrite");
                    cs108RfidData.waitUplinkResponse = needResponse;
                    addRfidToWrite(cs108RfidData);
//                    mRx000ToWrite.add(cs108RfidData);
                } else {
//                    if (DEBUG) appendToLog("sendControlCommand() adds to mRfidToWrite");
                    cs108RfidData.waitUplinkResponse = needResponse;
                    addRfidToWrite(cs108RfidData);
                }
                if (controlCommands == Cs108Connector.ControlCommands.ABORT) aborting = true;
                return true;
            }
        }

        boolean sendHostRegRequestHST_RFTC_FRQCH_DESC_PLLDIVMULT(int freqChannel) {
            long fccFreqTable[] = new long[]{
                    0x00180E4F, //915.75 MHz
                    0x00180E4D, //915.25 MHz
                    0x00180E1D, //903.25 MHz
                    0x00180E7B, //926.75 MHz
                    0x00180E79, //926.25 MHz
                    0x00180E21, //904.25 MHz
                    0x00180E7D, //927.25 MHz
                    0x00180E61, //920.25 MHz
                    0x00180E5D, //919.25 MHz
                    0x00180E35, //909.25 MHz
                    0x00180E5B, //918.75 MHz
                    0x00180E57, //917.75 MHz
                    0x00180E25, //905.25 MHz
                    0x00180E23, //904.75 MHz
                    0x00180E75, //925.25 MHz
                    0x00180E67, //921.75 MHz
                    0x00180E4B, //914.75 MHz
                    0x00180E2B, //906.75 MHz
                    0x00180E47, //913.75 MHz
                    0x00180E69, //922.25 MHz
                    0x00180E3D, //911.25 MHz
                    0x00180E3F, //911.75 MHz
                    0x00180E1F, //903.75 MHz
                    0x00180E33, //908.75 MHz
                    0x00180E27, //905.75 MHz
                    0x00180E41, //912.25 MHz
                    0x00180E29, //906.25 MHz
                    0x00180E55, //917.25 MHz
                    0x00180E49, //914.25 MHz
                    0x00180E2D, //907.25 MHz
                    0x00180E59, //918.25 MHz
                    0x00180E51, //916.25 MHz
                    0x00180E39, //910.25 MHz
                    0x00180E3B, //910.75 MHz
                    0x00180E2F, //907.75 MHz
                    0x00180E73, //924.75 MHz
                    0x00180E37, //909.75 MHz
                    0x00180E5F, //919.75 MHz
                    0x00180E53, //916.75 MHz
                    0x00180E45, //913.25 MHz
                    0x00180E6F, //923.75 MHz
                    0x00180E31, //908.25 MHz
                    0x00180E77, //925.75 MHz
                    0x00180E43, //912.75 MHz
                    0x00180E71, //924.25 MHz
                    0x00180E65, //921.25 MHz
                    0x00180E63, //920.75 MHz
                    0x00180E6B, //922.75 MHz
                    0x00180E1B, //902.75 MHz
                    0x00180E6D, //923.25 MHz
            };
            byte[] msgBuffer = new byte[]{(byte) 0x70, 1, 3, 0x0C, 0, 0, 0, 0};
            if (freqChannel >= 50) {
                freqChannel = 49;
            }
            long freqData = fccFreqTable[freqChannel];
            msgBuffer[4] = (byte) (freqData % 256);
            msgBuffer[5] = (byte) ((freqData >> 8) % 256);
            msgBuffer[6] = (byte) ((freqData >> 16) % 256);
            msgBuffer[7] = (byte) ((freqData >> 24) % 256);
            return sendHostRegRequest(HostRegRequests.HST_RFTC_FRQCH_DESC_PLLDIVMULT, true, msgBuffer);
        }

        boolean bLowPowerStandby = false;
        boolean setPwrManagementMode(boolean bLowPowerStandby) {
            if (isBleConnected() == false) return false;
            if (this.bLowPowerStandby == bLowPowerStandby) return true;
            this.bLowPowerStandby = bLowPowerStandby; appendToLog("!!! Skip setPwrManagementMode[" + bLowPowerStandby + "] with this.blowPowerStandby = " + this.bLowPowerStandby);
            return true;
        }

        int wideRSSI = -1;
        int getwideRSSI() {
            if (wideRSSI < 0) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                wideRSSI = 0; appendToLog("!!! Skip getwideRSSI with assumed value = 0");
            }
            return wideRSSI;
        }

        final int RXGAIN_INVALID = -1, RXGAIN_MIN = 0, RXGAIN_MAX = 0x1FF, RXGAIN_DEFAULT = 0x104;
        int rxGain = RXGAIN_INVALID;
        int getHighCompression() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                rxGain = RXGAIN_DEFAULT; appendToLog(String.format("!!! Skip getHighCompression with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
            } else iRetValue = (rxGain >> 8);
            return iRetValue;
        }
        int getRflnaGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                rxGain = RXGAIN_DEFAULT; appendToLog(String.format("!!! Skip getRflnaGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
            } else iRetValue = ((rxGain & 0xC0) >> 6);
            return iRetValue;
        }
        int getIflnaGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                rxGain = RXGAIN_DEFAULT; appendToLog(String.format("!!! Skip getIflnaGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
            } else iRetValue = ((rxGain & 0x38) >> 3);
            return iRetValue;
        }
        int getAgcGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                rxGain = RXGAIN_DEFAULT; appendToLog(String.format("!!! Skip getAgcGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
            } else iRetValue = (rxGain & 0x07);
            return iRetValue;
        }
        int getRxGain() {
            int iRetValue = -1;
            if (rxGain < RXGAIN_MIN || rxGain > RXGAIN_MAX) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                rxGain = RXGAIN_DEFAULT; appendToLog(String.format("!!! Skip getRxGain with assumed rxGain = 0x%X", RXGAIN_DEFAULT));
            } else iRetValue = rxGain;
            return iRetValue;
        }
        boolean setRxGain(int highCompression, int rflnagain, int iflnagain, int agcgain) {
            int rxGain_new = ((highCompression & 0x01) << 8) | ((rflnagain & 0x3) << 6) | ((iflnagain & 0x7) << 3) | (agcgain & 0x7);
            return setRxGain(rxGain_new);
        }
        boolean setRxGain(int rxGain_new) {
            boolean bRetValue = true;
            if ((rxGain_new != rxGain) || (sameCheck == false)) {
                mRfidDevice.mRfidReaderChip.setPwrManagementMode(false);
                rxGain = rxGain_new; appendToLog(String.format("!!! Skip setRxGain[0x%X]", rxGain_new));
            }
            return bRetValue;
        }

        boolean sendHostRegRequestHST_CMD(Cs710Library4A.HostCommands hostCommand) {
            appendToLog("!!! hostCommand = " + hostCommand.toString());
            long hostCommandData = -1;
            switch (hostCommand) {
                case CMD_18K6CINV:
                    hostCommandData = 0xA1;
                    if (mRfidDevice.mRfidReaderChip.mRx000Setting.getQuerySelect() > 1 /*&& mRfidDevice.mRfidReaderChip.mRx000Setting.getImpinjExtension() == 0*/) hostCommandData = 0xA3;
                    break;
                case CMD_18K6CINV_COMPACT:
                    hostCommandData = 0xA2;
                    if (mRfidDevice.mRfidReaderChip.mRx000Setting.getQuerySelect() > 1 /*&& mRfidDevice.mRfidReaderChip.mRx000Setting.getImpinjExtension() == 0*/) hostCommandData = 0xA6;
                    break;
                case CMD_18K6CINV_MB:
                    hostCommandData = 0xA4;
                    appendToLog("getQuerySelect = " + mRfidDevice.mRfidReaderChip.mRx000Setting.getQuerySelect() + ", getImpinjExtension = " + mRfidDevice.mRfidReaderChip.mRx000Setting.getImpinjExtension());
                    if (mRfidDevice.mRfidReaderChip.mRx000Setting.getQuerySelect() > 1 /*&& mRfidDevice.mRfidReaderChip.mRx000Setting.getImpinjExtension() == 0*/) hostCommandData = 0xA5;
                    break;
                case NULL:
                    hostCommandData = 0xAE;
                    break;
                case CMD_18K6CREAD:
                    hostCommandData = 0xB1;
                    break;
                case CMD_18K6CWRITE:
                    hostCommandData = 0xB2;
                    break;
                case CMD_18K6CLOCK:
                    hostCommandData = 0xB7;
                    break;
                case CMD_18K6CKILL:
                    hostCommandData = 0xB8;
                    break;
                case CMD_18K6CAUTHENTICATE:
                    hostCommandData = 0xB9;
                    break;

                case CMD_WROEM:
                    hostCommandData = 0x02;
                    break;
                case CMD_RDOEM:
                    hostCommandData = 0x03;
                    break;
                case CMD_ENGTEST:
                    hostCommandData = 0x04;
                    break;
                case CMD_MBPRDREG:
                    hostCommandData = 0x05;
                    break;
                case CMD_MBPWRREG:
                    hostCommandData = 0x06;
                    break;
                case CMD_SETPWRMGMTCFG:
                    hostCommandData = 0x14;
                    break;
                case CMD_UPDATELINKPROFILE:
                    hostCommandData = 0x19;
                    break;
                case CMD_18K6CBLOCKWRITE:
                    hostCommandData = 0x1F;
                    break;
                case CMD_CHANGEEAS:
                    hostCommandData = 0x26;
                    break;
                case CMD_GETSENSORDATA:
                    hostCommandData = 0x3b;
                    break;
                case CMD_READBUFFER:
                    hostCommandData = 0x51;
                    break;
                case CMD_UNTRACEABLE:
                    hostCommandData = 0x52;
                    break;
                case CMD_FDM_RDMEM:
                    hostCommandData = 0x53; break;
                case CMD_FDM_WRMEM:
                    hostCommandData = 0x54; break;
                case CMD_FDM_AUTH:
                    hostCommandData = 0x55; break;
                case CMD_FDM_GET_TEMPERATURE:
                    hostCommandData = 0x56; break;
                case CMD_FDM_START_LOGGING:
                    hostCommandData = 0x57; break;
                case CMD_FDM_STOP_LOGGING:
                    hostCommandData = 0x58; break;
                case CMD_FDM_WRREG:
                    hostCommandData = 0x59; break;
                case CMD_FDM_RDREG:
                    hostCommandData = 0x5A; break;
                case CMD_FDM_DEEP_SLEEP:
                    hostCommandData = 0x5B; break;
                case CMD_FDM_OPMODE_CHECK:
                    hostCommandData = 0x5C; break;
                case CMD_FDM_INIT_REGFILE:
                    hostCommandData = 0x5d; break;
                case CMD_FDM_LED_CTRL:
                    hostCommandData = 0x5e; break;
                default:
                    appendToLog("!!! CANNOT handle with hostCommand = " + hostCommand.toString());
            }
            if (hostCommandData == -1) {
                return false;
            } else {
                commandOperating = true;
                byte[] msgBuffer = new byte[]{(byte)0x80, (byte)0xb3, (byte)0x10, (byte)0xA1, 0, 0, 0};
                msgBuffer[3] = (byte) (hostCommandData % 256);
                return sendHostRegRequest(HostRegRequests.HST_CMD, true, msgBuffer);
            }
        }

        ArrayList<byte[]> macAccessHistory = new ArrayList<>();
        boolean bifMacAccessHistoryData(byte[] msgBuffer) {
            if (sameCheck == false) return false;
            if (msgBuffer.length != 8) return false;
            if (msgBuffer[0] != (byte)0x70 || msgBuffer[1] != 1) return false;
            if (msgBuffer[2] == 0 && msgBuffer[3] == (byte)0xF0) return false;
            return true;
        }
        int findMacAccessHistory(byte[] msgBuffer) {
            int i = -1;
            for (i = 0; i < macAccessHistory.size(); i++) {
//                appendToLog("macAccessHistory(" + i + ")=" + byteArrayToString(macAccessHistory.get(i)));
                if (Arrays.equals(macAccessHistory.get(i), msgBuffer)) break;
            }
            if (i == macAccessHistory.size()) i = -1;
            if (i >= 0) appendToLog("macAccessHistory: returnValue = " + i + ", msgBuffer=" + byteArrayToString(msgBuffer));
            return i;
        }
        void addMacAccessHistory(byte[] msgBuffer) {
            byte[] msgBuffer4 = Arrays.copyOf(msgBuffer, 4);
            for (int i = 0; i < macAccessHistory.size(); i++) {
                byte[] macAccessHistory4 = Arrays.copyOf(macAccessHistory.get(i), 4);
                if (Arrays.equals(msgBuffer4, macAccessHistory4)) {
                    appendToLog("macAccessHistory: deleted old record=" + byteArrayToString(macAccessHistory4));
                    macAccessHistory.remove(i);
                    break;
                }
            }
            appendToLog("macAccessHistory: added msgbuffer=" + byteArrayToString(msgBuffer));
            macAccessHistory.add(msgBuffer);
        }

        byte downlinkSequenceNumber = 0;
        boolean sendHostRegRequest(HostRegRequests hostRegRequests, boolean writeOperation, byte[] msgBuffer) {
            boolean needResponse = false;
            boolean validRequest = false;

            if (hostRegRequests == HostRegRequests.HST_ANT_DESC_DWELL) appendToLog("setAntennaDwell 4");
            boolean bSkip = false;
            if ( (hostRegRequests != HostRegRequests.HST_CMD && hostRegRequests != HostRegRequests.MAC_OPERATION)
                    || (hostRegRequests == HostRegRequests.HST_CMD
                    && msgBuffer[3] != (byte)0xA1
                    && msgBuffer[3] != (byte)0xA2
                    && msgBuffer[3] != (byte)0xA3
                    && msgBuffer[3] != (byte)0xA4
                    && msgBuffer[3] != (byte)0xA5
                    && msgBuffer[3] != (byte)0xA6
                    && msgBuffer[3] != (byte)0xAE
                    && msgBuffer[3] != (byte)0xB1
                    && msgBuffer[3] != (byte)0xB2
                    && msgBuffer[3] != (byte)0xB7
                    && msgBuffer[3] != (byte)0xB8
                    && msgBuffer[3] != (byte)0xB9
            ) || (hostRegRequests == HostRegRequests.MAC_OPERATION && writeOperation && msgBuffer[0] != (byte)0x80)
            ) bSkip = true;
            if (bSkip) {
                appendToLog("!!! Skip sendingRegRequest with " + hostRegRequests.toString() + ", writeOperation = " + writeOperation + "." + byteArrayToString(msgBuffer));
                return true;
            }
            if (isBleConnected() == false) {
                appendToLog("!!! Skip sending as bleConnected is false");
                return false;
            }
            if (false) addMacAccessHistory(msgBuffer);
            switch (hostRegRequests) {
                case MAC_OPERATION:
                case HST_ANT_CYCLES:
                case HST_ANT_DESC_SEL:
                case HST_ANT_DESC_CFG:
                case MAC_ANT_DESC_STAT:
                case HST_ANT_DESC_PORTDEF:
                case HST_ANT_DESC_DWELL:
                case HST_ANT_DESC_RFPOWER:
                case HST_ANT_DESC_INV_CNT:
                    validRequest = true;
                    break;
                case HST_TAGMSK_DESC_SEL:
                case HST_TAGMSK_DESC_CFG:
                case HST_TAGMSK_BANK:
                case HST_TAGMSK_PTR:
                case HST_TAGMSK_LEN:
                case HST_TAGMSK_0_3:
                    validRequest = true;
                    break;
                case HST_QUERY_CFG:
                case HST_INV_CFG:
                case HST_INV_SEL:
                case HST_INV_ALG_PARM_0:
                case HST_INV_ALG_PARM_1:
                case HST_INV_ALG_PARM_2:
                case HST_INV_ALG_PARM_3:
                case HST_INV_RSSI_FILTERING_CONFIG:
                case HST_INV_RSSI_FILTERING_THRESHOLD:
                case HST_INV_RSSI_FILTERING_COUNT:
                case HST_INV_EPC_MATCH_CFG:
                case HST_INV_EPCDAT_0_3:
                    validRequest = true;
                    break;
                case HST_TAGACC_DESC_CFG:
                case HST_TAGACC_BANK:
                case HST_TAGACC_PTR:
                case HST_TAGACC_CNT:
                case HST_TAGACC_LOCKCFG:
                case HST_TAGACC_ACCPWD:
                case HST_TAGACC_KILLPWD:
                case HST_TAGWRDAT_SEL:
                case HST_TAGWRDAT_0:
                    validRequest = true;
                    break;
                case HST_RFTC_CURRENT_PROFILE:
                case HST_RFTC_FRQCH_SEL:
                case HST_RFTC_FRQCH_CFG:
                case HST_RFTC_FRQCH_DESC_PLLDIVMULT:
                case HST_RFTC_FRQCH_DESC_PLLDACCTL:
                case HST_RFTC_FRQCH_CMDSTART:
                    validRequest = true;
                    break;
                case HST_AUTHENTICATE_CFG:
                case HST_AUTHENTICATE_MSG:
                case HST_READBUFFER_LEN:
                case HST_UNTRACEABLE_CFG:
                    validRequest = true;
                    break;
                case HST_CMD:
                    validRequest = true;
                    needResponse = true;
                    break;
            }

            if (msgBuffer == null || validRequest == false) {
                appendToLog("invalid request for msgbuffer = " + (msgBuffer == null ? "NULL" : "Valid") + ", validRequst = " + validRequest);
                return false;
            } else {
                Cs108RfidData cs108RfidData = new Cs108RfidData();
                cs108RfidData.rfidPayloadEvent = Cs108Connector.RfidPayloadEvents.RFID_COMMAND;
                cs108RfidData.dataValues = msgBuffer;
                cs108RfidData.waitUplinkResponse = true; //(needResponse || writeOperation == false);
                if (msgBuffer[0] == (byte)0x80
                        && msgBuffer[1] == (byte)0xB3
                ) {
                    cs108RfidData.dataValues[4] = downlinkSequenceNumber++;
                    if (msgBuffer[2] == 0x10
                            && msgBuffer[3] != (byte)0xA1
                            && msgBuffer[3] != (byte)0xA2
                            && msgBuffer[3] != (byte)0xA3
                            && msgBuffer[3] != (byte)0xA4
                            && msgBuffer[3] != (byte)0xA5
                            && msgBuffer[3] != (byte)0xA6
                            && msgBuffer[3] != (byte)0xB1
                            && msgBuffer[3] != (byte)0xB2
                            && msgBuffer[3] != (byte)0xB7
                            && msgBuffer[3] != (byte)0xB8
                            && msgBuffer[3] != (byte)0xB9
                    ) cs108RfidData.waitUplink1Response = true;
                }
                addRfidToWrite(cs108RfidData);
                return true;
            }
        }

        void addRfidToWrite(Cs108RfidData cs108RfidData) {
            boolean repeatRequest = false;
            if (false && cs108RfidData.rfidPayloadEvent == RfidPayloadEvents.RFID_COMMAND) {
                appendToLog("!!! Skip " + cs108RfidData.rfidPayloadEvent.toString() + "." + byteArrayToString(cs108RfidData.dataValues));
                return;
            }
            if (mRfidDevice.mRfidToWrite.size() != 0 && sameCheck) {
                Cs108RfidData cs108RfidData1 = mRfidDevice.mRfidToWrite.get(mRfidDevice.mRfidToWrite.size() - 1);
                if (cs108RfidData.rfidPayloadEvent == cs108RfidData1.rfidPayloadEvent) {
                    if (cs108RfidData.dataValues == null && cs108RfidData1.dataValues == null) {
                        repeatRequest = true;
                    } else if (cs108RfidData.dataValues != null && cs108RfidData1.dataValues != null) {
                        if (cs108RfidData.dataValues.length == cs108RfidData1.dataValues.length) {
                            if (compareArray(cs108RfidData.dataValues, cs108RfidData1.dataValues, cs108RfidData.dataValues.length)) {
                                repeatRequest = true;
                            }
                        }
                    }
                }
            }
            if (repeatRequest == false) {
                mRfidDevice.mRfidToWrite.add(cs108RfidData);
                if (DEBUG_PKDATA) appendToLog("PkData: add " + cs108RfidData.rfidPayloadEvent + (cs108RfidData.dataValues != null ? "." : "") + byteArrayToString(cs108RfidData.dataValues)
                        + (cs108RfidData.waitUplinkResponse ? " waitUplinkResponse" : "") + (cs108RfidData.waitUplink1Response ? " waitUplink1Response" : "")
                        + " to mRfidToWrite with length = " + mRfidDevice.mRfidToWrite.size());
            } else if (DEBUG_PKDATA) appendToLog("!!! Skip repeated sending " + cs108RfidData.rfidPayloadEvent + (cs108RfidData.dataValues != null ? "." : "") + byteArrayToString(cs108RfidData.dataValues));
        }
    }
}

