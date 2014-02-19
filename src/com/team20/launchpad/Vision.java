/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.team20.launchpad;

import com.sun.squawk.platform.windows.natives.Socket;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
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

    boolean redBallDetected, blueBallDetected, horizontal, vertical;
    int redBallX, redBallY,
            blueBallX, blueBallY,
            horizontalX, horizontalY,
            verticalX, verticalY;
    int mode;
    //NetworkTable visionTable = NetworkTable.getTable("Vision");
    SocketConnection sc;
    OutputStream os;
    InputStream is;
    byte[] buffer = new byte[5];

    public Vision() {

        try {
            sc = (SocketConnection) Connector.open("socket://10.0.20.15:9090");
            sc.setSocketOption(SocketConnection.LINGER, 5);
            os = sc.openOutputStream();
            is = sc.openInputStream();
        } catch (Exception e) {
            System.out.println("Exception 1 " + e.toString());
        }
    }

    public boolean isByteTrue(byte b) {
        return b == 1;
    }

    public short addBytes(byte b, byte c) {
        short ret =(short) (b << 8);
        ret |= c;
        return (short)(256+ret);
    }

    private void getData(int b) {

        try {

            //is.skip(is.available());
            os.write(b);//0 horiz 1 vert 2 red 3 blue
            os.flush();
            Thread.sleep(500);//wait for processing
            os.write(5);//ask for image processing data
            os.flush();

            int n;
            do {
            } while ((n = is.available()) == 0);
            if (n == 6) {
                if (is.read() == mode) {
                    is.read(buffer);
                }
            } else if (n > 6) {
                is.skip(is.available());
            }
        } catch (Exception e) {
            System.out.println("I just blew up");
        }
    }

    private void processBuffer() {
        if (buffer == null || buffer.length < 5) {
            return;
        }
        if (mode == 0) {
            horizontal = isByteTrue(buffer[0]);
            horizontalX = addBytes(buffer[1], buffer[2]);
            horizontalY = addBytes(buffer[3], buffer[4]);
        } else if (mode == 1) {
            vertical = isByteTrue(buffer[0]);
            verticalX = addBytes(buffer[1], buffer[2]);
            verticalY = addBytes(buffer[3], buffer[4]);

        } else if (mode == 2) {
            redBallDetected = isByteTrue(buffer[0]);
            redBallX = addBytes(buffer[1], buffer[2]);
            redBallY = addBytes(buffer[3], buffer[4]);

        } else if (mode == 3) {
            blueBallDetected = isByteTrue(buffer[0]);
            blueBallX = addBytes(buffer[1], buffer[2]);
            blueBallY = addBytes(buffer[3], buffer[4]);

        }
    }

    private void getHorizontalInfo() {
        mode = 0;
        getData(mode);
        processBuffer();

    }

    private void getVerticalInfo() {
        mode = 1;
        getData(mode);
        processBuffer();
    }

    private void getRedBallInfo() {
        mode = 2;
        getData(mode);
        processBuffer();
    }

    private void getBlueBallInfo() {
        mode = 3;
        getData(mode);
        processBuffer();
    }

    public boolean getRedBallDetected() {
        getRedBallInfo();
        return redBallDetected;

    }

    public boolean getBlueBallDetected() {
        getBlueBallInfo();
        return blueBallDetected;
    }

    public boolean getVerticalDetected() {
        getVerticalInfo();
        return vertical;
    }

    public boolean getHorizontalDetected() {
        getHorizontalInfo();
        return horizontal;
    }

    public int getRedBallX() {
        getRedBallInfo();
        return redBallX;
    }

    public int getRedBallY() {
        getRedBallInfo();
        return redBallY;
    }

    public int getBlueBallX() {
        getBlueBallInfo();
        return blueBallX;
    }

    public int getBlueBallY() {
        getBlueBallInfo();
        return blueBallY;
    }

    public int getVerticalX() {
        getVerticalInfo();
        return verticalX;
    }

    public int getVerticalY() {
        getVerticalInfo();
        return verticalY;
    }

    public int getHorizontalX() {
        getHorizontalInfo();
        return horizontalX;
    }

    public int getHorizontalY() {
        getHorizontalInfo();
        return horizontalY;
    }
}
