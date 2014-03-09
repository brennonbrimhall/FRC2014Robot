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

    private boolean redBallDetected = false, blueBallDetected = false,
            horizontal = false, vertical = false, redBallInfoUpdated = false,
            blueBallInfoUpdated = false, horizontalInfoUpdated = false,
            verticalInfoUpdated = false, horizontalInfoUpdatedOnce = false,
            horizontalDetectedFirstUpdate = false, horizontalInfoUpdatedTwice = false,
            horizontalDetectedSecondUpdate = false, hasConnected = false, threadHasStarted = false;
    private int redBallX = 0, redBallY = 0,
            blueBallX = 0, blueBallY = 0,
            horizontalX = 0, horizontalY = 0,
            verticalX = 0, verticalY = 0, mode = -1;
    public final int CAMERA_PROCESSING_TIME = 400/*, THREAD_TIMEOUT_TIME = 15000000*/,
            DATA_CYCLE_TIME = CAMERA_PROCESSING_TIME + 10, BALL_CENTERED_PIXELS_X = 160;//TODO: find the pixel range for "centered"
    public final double BALL_CENTERED_TOLERANCE = .08;
    //NetworkTable visionTable = NetworkTable.getTable("Vision");
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
    /* public void startThread() {
     if (!threadHasStarted) {
     threadHasStarted = true;
     //if(!st.isAlive()){
     st.start();
     }
     }*/
    public void startThread() {
        if (threadHasStarted && !hasConnected) {
            System.out.println("spawning a new vision thread");
            resetData();
            createThread();
            st.start();
        } else if (!threadHasStarted) {
            threadHasStarted = true;
            st.start();
        }
    }

    /*public void endThread() {
     if (threadHasStarted&&sc!=null&&is!=null&&os!=null) {
     threadHasStarted = false;
     try {
     sc.close();
     os.close();
     is.close();
     } catch (IOException ex) {
     ex.printStackTrace();
     }
     }
     }*/
    public void disconnect() {

        try {
            if (hasConnected) {
                sc.close();
                os.close();
                is.close();
            }
            hasConnected = false;
        } catch (IOException ex) {
            System.out.println("vision had trouble closing " + ex.toString());
        }

    }

    private void getData(int b) {

        try {
            os.write(b);//0 horiz 1 vert 2 red 3 blue
            os.flush();

            os.write(5);//ask for image processing data
            os.flush();
            //System.out.println("written to");
            int n;
            do {
            } while ((n = is.available()) == 0);
            if (n == 6) {
                if (is.read() == mode) {
                    // System.out.println("reading into buffer");
                    is.read(buffer);
                }
            } else if (n > 6) {
                is.skip(is.available());
            }
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
                //open connection
                try {
                    sc = (SocketConnection) Connector.open("socket://10.0.20.15:9090");
                   

                    sc.setSocketOption(SocketConnection.LINGER, 5);
                    os = sc.openOutputStream();
                    is = sc.openInputStream();
                    hasConnected = true;
                    System.out.println("Vision connection has been opened successfully");
                    //System.out.println("OS and IS have been opened");
                    
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
                            processBuffer();
                        }
                        try {
                            Thread.sleep(250);

                            //time limit on connection
                            /*if (System.currentTimeMillis() - startTime > THREAD_TIMEOUT_TIME) {
                             System.out.println("Vision is closing comms");
                             try {
                             sc.close();
                             os.close();
                             is.close();
                             } catch (IOException ex) {
                             System.out.println("had trouble closing comms " + ex.toString());
                             }
                             break;
                             }*/
                        } catch (InterruptedException ex) {
                            System.out.println("Thread was interrupted while sleeping");
                        }
                    }
                } catch (IOException e) {
                    hasConnected = false;
                    System.out.println("Vision had trouble opening comms " + e.toString());

                }

            }
        };
    }

    /*  public boolean isBallCentered(boolean red) {

     if (red) {
     if (!redBallInfoUpdated) {
     System.out.println("Using outdated red ball info to return centered");
     }
     return (isXValCentered(redBallX));
     } else {
     if (!blueBallInfoUpdated) {
     System.out.println("Using outdated blue ball info to return centered");
     }
     return isXValCentered(blueBallX);
     }
     }

     private boolean isXValCentered(int x) {
     return x * (1 - BALL_CENTERED_TOLERANCE) < BALL_CENTERED_PIXELS_X && x * (1 + BALL_CENTERED_TOLERANCE) > BALL_CENTERED_PIXELS_X;
     }

     public boolean isHorizontalCentered() {
     if (!this.isHorizontalInfoUpdated()) {
     System.out.println("using outdated horizontal info to return centered");
     }
     return isXValCentered(getHorizontalX());
     }

     public boolean isVerticalCentered() {
     if (!this.isVerticalInfoUpdated()) {
     System.out.println("using outdated horizontal info to return centered");
     }
     return isXValCentered(getVerticalX());
     }*/
    //getters
    public int getMode() {
        return mode;
    }

    public boolean hasConnected() {
        return hasConnected;
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
