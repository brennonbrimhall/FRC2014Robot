/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.team20.launchpad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author Driver
 */
public class Vision {

    private boolean connecting = false, redBallDetected = false, blueBallDetected = false,
            horizontal = false, vertical = false, redBallInfoUpdated = false,
            blueBallInfoUpdated = false, horizontalInfoUpdated = false,
            verticalInfoUpdated = false, horizontalInfoUpdatedOnce = false,
            horizontalDetectedFirstUpdate = false, horizontalInfoUpdatedTwice = false,
            horizontalDetectedSecondUpdate = false, hasConnected = false, threadHasStarted = false;
    private int redBallX = 0, redBallY = 0,
            blueBallX = 0, blueBallY = 0,
            horizontalX = 0, horizontalY = 0,
            verticalX = 0, verticalY = 0, mode = -1;
    public final int CAMERA_PROCESSING_TIME = 400,
            DATA_CYCLE_TIME = CAMERA_PROCESSING_TIME + 10;
    private SocketConnection sc;
    private OutputStream os;
    private InputStream is;
    private byte[] buffer = new byte[5];
    Thread st;

    public Vision() {
        createThread();
    }

    /**
     * This MUST be called once so that this class starts writing to the
     * beaglebone
     */
    public void start() {
        if (!threadHasStarted && !hasConnected && !connecting) {
            threadHasStarted = true;
            resetData();
            st.start();
        } else if (threadHasStarted && !hasConnected && !connecting) {
            threadHasStarted = true;
            resetData();
            createThread();
            st.start();
        }
    }

    private void connect() {
        try {
            if (!hasConnected && !connecting) {
                connecting = true;
                sc = (SocketConnection) Connector.open("socket://10.0.20.15:9090");
                sc.setSocketOption(SocketConnection.LINGER, 5);
                os = sc.openOutputStream();
                is = sc.openInputStream();
                hasConnected = true;
                connecting = false;
                System.out.println("Vision connection has been opened successfully");
            }
        } catch (IOException ex) {
            System.out.println("Vision had trouble connecting to beaglebone " + ex.toString());
            connecting = false;
            hasConnected = false;
        }
    }

    public void disconnect() {
        if(sc==null||os==null||is==null){
            System.out.println("vision hasnt been initialized, ending disconnect");
            return;
        }
        try {
            sc.close();
            os.close();
            is.close();
            hasConnected = false;
            System.out.println("Disconnected from the beaglebone successfully");
        } catch (IOException ex) {
            System.out.println("Vision had trouble closing " + ex.toString());
        }

    }

    private void getData(int b) {

        try {
            //sends first byte over to identify what info we want
            os.write(b);//0 horiz 1 vert 2 red 3 blue
            os.flush();
            //sends second byte over that asks for data
           /* os.write(5);//ask for image processing data
            os.flush();
            //System.out.println("written to");
            int n;
            do {//wait until data is sent
                if (!hasConnected) {
                    return;//get out of this method if we somehow lose connection
                }
            } while ((n = is.available()) == 0);
            if (n == 6 && is.read() == mode) {
                is.read(buffer);
            } else if (n > 6) {
                is.skip(is.available());
            }*/
        } catch (IOException e) {
            System.out.println("vision had trouble getting data " + e.toString());
        }

    }

    //mode updaters
    public void lookForHorizontalInfo() {
        mode = 0;

    }

    public void lookForVerticalInfo() {
        mode = 1;
    }

    public void lookForRedBallInfo() {
        mode = 2;
    }

    public void lookForBlueBallInfo() {
        mode = 3;
    }
    public void takeAPicture(){
        mode = 4;
    }

    //processing
    private void processBuffer() {
        if (buffer[0] == -1) {
            return;
        }
        if (buffer == null || buffer.length < 5) {
            System.out.println("buffer is null or has less than 5 pieces of data");
            return;
        }
        switch (mode) {
            case 0:
                //System.out.println("updating vision");
                horizontal = isByteTrue(buffer[0]);
                horizontalX = addBytes(buffer[1], buffer[2]);
                horizontalY = addBytes(buffer[3], buffer[4]);
                horizontalInfoUpdated = true;
                if (horizontalInfoUpdatedOnce && !horizontalInfoUpdatedTwice) {
                    horizontalInfoUpdatedTwice = true;
                    horizontalDetectedSecondUpdate = horizontal;
                }
                if (!horizontalInfoUpdatedOnce) {
                    horizontalDetectedFirstUpdate = horizontal;
                    horizontalInfoUpdatedOnce = true;
                }
                break;
            case 1:
                vertical = isByteTrue(buffer[0]);
                verticalX = addBytes(buffer[1], buffer[2]);
                verticalY = addBytes(buffer[3], buffer[4]);
                verticalInfoUpdated = true;
                break;
            case 2:
                redBallDetected = isByteTrue(buffer[0]);
                redBallX = addBytes(buffer[1], buffer[2]);
                redBallY = addBytes(buffer[3], buffer[4]);
                redBallInfoUpdated = true;
                break;
            case 3:
                blueBallDetected = isByteTrue(buffer[0]);
                blueBallX = addBytes(buffer[1], buffer[2]);
                blueBallY = addBytes(buffer[3], buffer[4]);
                blueBallInfoUpdated = true;
                break;
        }
        resetBuffer();
    }

    private boolean isByteTrue(byte b) {
        return b == 1;
    }

    private short addBytes(byte b, byte c) {
        short ret = (short) (b << 8);
        ret |= c;
        return ret >= 0 ? ret : (short) (ret + 256);
    }

    public void resetData() {
        redBallDetected = false;
        blueBallDetected = false;
        horizontal = false;
        vertical = false;
        redBallInfoUpdated = false;
        blueBallInfoUpdated = false;
        horizontalInfoUpdated = false;
        verticalInfoUpdated = false;
        redBallX = 0;
        redBallY = 0;
        blueBallX = 0;
        blueBallY = 0;
        horizontalX = 0;
        horizontalY = 0;
        verticalX = 0;
        verticalY = 0;
        mode = -1;
        horizontalDetectedFirstUpdate = false;
        horizontalDetectedSecondUpdate = false;
        horizontalInfoUpdated = false;
        horizontalInfoUpdatedOnce = false;
        horizontalInfoUpdatedTwice = false;
        resetBuffer();
    }

    private void resetBuffer() {
        for (int a = 0; a < buffer.length; a++) {
            buffer[a] = -1;
        }
    }

    private void createThread() {
        st = new Thread() {
            public void run() {
                connect();
                long startTime = System.currentTimeMillis();
                long incrementalTime = startTime;
                while (hasConnected) {

                    //falsify all infoUpdated so the class knows that its info is out of date
                    if (System.currentTimeMillis() - incrementalTime > DATA_CYCLE_TIME + 50) {
                        redBallInfoUpdated = false;
                        blueBallInfoUpdated = false;
                        horizontalInfoUpdated = false;
                        verticalInfoUpdated = false;
                        incrementalTime = System.currentTimeMillis();
                    }
                    //get data from the beaglebone
                    //   System.out.println("vision is getting data");
                    if (mode != -1) {
                        getData(mode);
                        // System.out.println("vision is processing data");
                       // processBuffer();
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        System.out.println("Thread was interrupted while sleeping " + ex.toString());
                    }
                }

            }
        };
    }

    //getters
    public int getMode() {
        return mode;
    }

    public boolean hasConnected() {
        return hasConnected;
    }

    public boolean isHorizontalDetectedWithinFirstTwoUpdates() {
        return isHorizontalInfoUpdatedTwice() && (isHorizontalDetectedFirstUpdate() || isHorizontalDetectedSecondUpdate());
    }

    public boolean isHorizontalInfoUpdated() {
        return horizontalInfoUpdated;
    }

    public boolean isVerticalInfoUpdated() {
        return verticalInfoUpdated;
    }

    public boolean isRedBallInfoUpdated() {
        return redBallInfoUpdated;
    }

    public boolean isBlueBallInfoUpdated() {
        return blueBallInfoUpdated;
    }

    public boolean isHorizontalInfoUpdatedOnce() {
        return horizontalInfoUpdatedOnce;
    }

    public boolean isHorizontalDetectedFirstUpdate() {
        return horizontalDetectedFirstUpdate;
    }

    public boolean isHorizontalInfoUpdatedTwice() {
        return horizontalInfoUpdatedTwice;
    }

    public boolean isHorizontalDetectedSecondUpdate() {
        return horizontalDetectedSecondUpdate;
    }

    public boolean isRedBallDetected() {
        return redBallDetected;

    }

    public boolean isBlueBallDetected() {
        return blueBallDetected;
    }

    public boolean isVerticalDetected() {
        return vertical;
    }

    public boolean isHorizontalDetected() {
        return horizontal;
    }

    public int getRedBallX() {
        return redBallX;
    }

    public int getRedBallY() {
        return redBallY;
    }

    public int getBlueBallX() {
        return blueBallX;
    }

    public int getBlueBallY() {
        return blueBallY;
    }

    public int getVerticalX() {
        return verticalX;
    }

    public int getVerticalY() {
        return verticalY;
    }

    public int getHorizontalX() {
        return horizontalX;
    }

    public int getHorizontalY() {
        return horizontalY;
    }

}
