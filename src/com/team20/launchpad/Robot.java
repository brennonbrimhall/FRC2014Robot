
/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package com.team20.launchpad;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.buttons.JoystickButton;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    //Driver joystick and driver buttons
    LogitechGamepadController driverGamepad;
    JoystickButton yButton, xButton, bButton, aButton, rightTrigger, leftTrigger;
    //Operator joystick and operator buttons
    LogitechDualActionController operatorController;
    JoystickButton button1, button2, button3, button4, button5, button6, button7, button8, button9, button10;
    boolean previousDPadUp = false, previousDPadLeft = false, previousDPadRight = false, previousDPadDown = false;
    boolean allPanelsBlooming = false;
    Drivetrain drivetrain;
    Collector collector;
    Compressor compressor;
    Relay relay;
    Catapult catapult;
    CatcherPanel rightPanel, leftPanel, backPanel;
    Vision vision = new Vision();
    //Boolean to store hot or not
    boolean hot = false;
    //Auto
    int autonomousMode = 1;
    final int kCloseOneBall = 1, kFarOneBall = 2, kFarOneBallDriveBack = 3,
            kTwoBall = 4, kHotOneBall = 5, kHotTwoBall = 6, kHotTwoBallWithTurn = 7, kBackupAuto = 9;
    ;
    final int kMobility = -2, kStop = -1, kBlooming = 0, kProcessing = 1, kShooting = 2,
            kIdling = 3, kDrivingForward = 4, kSettlingBall = 5, kTurning = 6, kShooting2 = 7,
            kIdling2 = 8, kTurning2 = 9;
    int state = kBlooming, counter = 0;
    long previousSystemTime = System.currentTimeMillis();
    long autoStartTime = 0, disconnectStartTime = 0;
    boolean firstBallShot = false, secondBallShot = false;

    public void robotInit() {
        //Initializing and starting compressor
        compressor = new Compressor(1, 1);
        compressor.start();

        //Initializing and starting up subsystems
        drivetrain = new Drivetrain();
        collector = new Collector();
        catapult = new Catapult();

        //Initializing and setting up catcher panels
        rightPanel = new CatcherPanel(new DoubleSolenoid(2, 1, 2));
        backPanel = new CatcherPanel(new DoubleSolenoid(2, 3, 4));
        leftPanel = new CatcherPanel(new DoubleSolenoid(2, 5, 6));

        //Initializing driver joystick
        driverGamepad = new LogitechGamepadController(1);
        yButton = driverGamepad.getYButton();
        xButton = driverGamepad.getXButton();
        bButton = driverGamepad.getBButton();
        aButton = driverGamepad.getAButton();
        rightTrigger = driverGamepad.getRightBumper();
        leftTrigger = driverGamepad.getLeftBumper();

        //Initializing operator joystick
        operatorController = new LogitechDualActionController(2);
        button1 = operatorController.getButton(1);
        button2 = operatorController.getButton(2);
        button3 = operatorController.getButton(3);
        button4 = operatorController.getButton(4);
        button5 = operatorController.getButton(5);
        button6 = operatorController.getButton(6);
        button7 = operatorController.getButton(7);
        button8 = operatorController.getButton(8);
        button9 = operatorController.getButton(9);
        button10 = operatorController.getButton(10);

        //Initializing comms for Beaglebone
        //vision.start();
    }

    public void init() {
        collector.bloom();
        drivetrain.resetEncoders();
    }

    public void periodic() {
        //Update subsystems
        collector.update();
        if (catapult.isRetracting()) {
            compressor.stop();
        } else {
            compressor.start();
        }

        //Calculate method call cycle time
        /*
         * long currentTime = System.currentTimeMillis(); long delta =
         * currentTime - previousSystemTime; System.out.println(delta+"ms per
         * cycle."); previousSystemTime = currentTime;
         */
        //System.out.println("Left:\t"+drivetrain.getLeftDistance());
    }

    public void autonomousInit() {
        init();
        //vision.resetData();
        //vision.start();
        //vision.takeAPicture();
        firstBallShot = false;
        secondBallShot = false;
        //autoStartTime = System.currentTimeMillis();
        counter = 0;
        if (DriverStation.getInstance().getDigitalIn(1)) {
            autonomousMode = 1;
        } else if (DriverStation.getInstance().getDigitalIn(2)) {
            autonomousMode = 2;
        } else if (DriverStation.getInstance().getDigitalIn(3)) {
            autonomousMode = 3;
        } else if (DriverStation.getInstance().getDigitalIn(4)) {
            autonomousMode = 4;
        } else if (DriverStation.getInstance().getDigitalIn(5)) {
            autonomousMode = 5;
        } else if (DriverStation.getInstance().getDigitalIn(6)) {
            autonomousMode = 6;
        } else if (DriverStation.getInstance().getDigitalIn(7)) {
            autonomousMode = 7;
        } else if (DriverStation.getInstance().getDigitalIn(8)) {
            autonomousMode = 8;
        }
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        counter++;
        periodic();
        switch (autonomousMode) {
            case kCloseOneBall:
                closeOneBallPeriodic();
                break;
            case kFarOneBall:
                farOneBallPeriodic();
                break;
            case kFarOneBallDriveBack:
                farOneBallDriveBackPeriodic();
                break;
            case kTwoBall:
                twoBallPeriodic();
                break;
            case kHotOneBall:
                hotOneBallPeriodic();
                break;
            /*
             * case kHotTwoBall: hotTwoBallPeriodicNoTurn(); break; case
             * kHotTwoBallWithTurn: hotTwoBallPeriodicWithTurn(); break; case
             * kBackupAuto: System.out.println("vision not connected, running
             * backupAuto");
                break;
             */
            default:
                break;
        }
    }

    public void closeOneBallPeriodic() {
        catapult.update();
        drivetrain.lowGear();
        if (counter < 20) {
            //Waiting for panels and collector to come out
        } else if (counter < 170) {
            if (drivetrain.getLeftDistance() > -300) {
                drivetrain.arcadeDrive(-1, 0);
            } else {
                drivetrain.arcadeDrive(0, 0);
            }

        } else if (counter < 180) {
            catapult.shoot();
            drivetrain.arcadeDrive(0, 0);
        } else {
            //Catapult retracts automatically
            drivetrain.arcadeDrive(0, 0);
        }
    }

    public void farOneBallPeriodic() {
        catapult.update(30, 30);
        drivetrain.lowGear();
        if (counter < 40) {
            //Wait for stuff to open
            backPanel.bloom();
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 50) {
            catapult.shoot();
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 100) {
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 230) {
            if (drivetrain.getLeftDistance() > -700) {
                drivetrain.arcadeDrive(-1, 0);
                collector.drive();
            } else {
                drivetrain.arcadeDrive(0, 0);
            }
        } else {
            drivetrain.arcadeDrive(0, 0);
            collector.stop();
        }
    }

    public void farOneBallDriveBackPeriodic() {
        catapult.update(30, 30);
        drivetrain.lowGear();
        if (counter < 40) {
            //Wait for stuff to open
            backPanel.bloom();
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 50) {
            catapult.shoot();
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 100) {
            //Waiting for shoot sequence
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 250) {
            backPanel.wilt();
            if (drivetrain.getLeftDistance() > -700) {
                drivetrain.arcadeDrive(-1, 0);
                collector.drive();
            } else {
                drivetrain.arcadeDrive(0, 0);
            }
        } else {
            if (drivetrain.getLeftDistance() < -50) {
                drivetrain.arcadeDrive(1, 0);
            } else {
                drivetrain.arcadeDrive(0, 0);
                //backPanel.wilt();
                collector.stop();
                //collector.wilt();
            }
        }
    }

    public void twoBallPeriodic() {
        //System.out.println(counter);
        catapult.update(20, 30);
        drivetrain.lowGear();
        if (counter < 40) {
            //Wait for stuff to open
            backPanel.bloom();
            collector.bloom();
        } else if (counter < 100) {
            if (drivetrain.getRightDistance() < 200) {
                drivetrain.arcadeDrive(-1, 0);
            } else {
                drivetrain.arcadeDrive(0, 0);
            }
        } else if (counter < 110) {
            drivetrain.arcadeDrive(0, 0);
            catapult.shoot();
        } else if (counter < 150) {
            //Staying still while catapult shoots
            drivetrain.arcadeDrive(0, 0);
        } else if (counter < 300) {
            if (drivetrain.getRightDistance() < 850) {
                collector.drive();
                drivetrain.arcadeDrive(-1, 0);
            } else {
                collector.drive();
                drivetrain.arcadeDrive(0, 0);
            }
        } else if (counter < 350) {
            collector.wilt();
            drivetrain.arcadeDrive(0, 0);
            collector.drive();
        } else if (counter < 360) {
            collector.bloom();
            drivetrain.arcadeDrive(0, 0);
            collector.drive();
        } else if (counter < 370) {
            drivetrain.arcadeDrive(0, 0);
            catapult.shoot();
            collector.drive();
        }
    }

    public void hotOneBallPeriodic() {

        catapult.update(30, 30);
        drivetrain.lowGear();
        //System.out.println("hot one ball");
        switch (state) {
            case kBlooming://starting state
                vision.lookForHorizontalInfo();//sets the vision to look for the horizontal info
                if (counter < 40) {
                    //Wait for stuff to open
                    leftPanel.wilt();
                    rightPanel.wilt();
                    backPanel.bloom();
                    collector.bloom();
                    collector.drive();
                } else {
                    //System.out.println("going to processing");
                    state = kProcessing;//wait for image processing
                    counter = 0;
                }
                break;
            case kProcessing:
                //System.out.println("processing");
                if (vision.isHorizontalDetectedWithinFirstTwoUpdates()) {
                    //System.out.println("\n**************horizontal detected, shooting\n");
                    state = kShooting;
                    drivetrain.arcadeDrive(0, 0);
                } else if ((System.currentTimeMillis() - autoStartTime) > 4750) {
                    //if we never get any data back, this will still run
                    //System.out.println("\n**************horizontal not detected, shooting\n");
                    state = kShooting;
                    drivetrain.arcadeDrive(0, 0);
                } else if (counter > 30 && drivetrain.getRightDistance() < 900
                           && (System.currentTimeMillis() - autoStartTime < 4000)) {
                    //counter is here to ensure that we wait long enough to get data back from vision
                    //  System.out.println("driving forward");
                    state = kDrivingForward;//drives forward
                } else {
                    drivetrain.arcadeDrive(0, 0);
                }
                break;
            case kShooting:
                firstBallShot = true;
                catapult.shoot();
                drivetrain.arcadeDrive(0, 0);
                counter = 0;
                state = kIdling;
                //System.out.println("*****SHOOTING");
                break;
            case kIdling://waits for the shooter to finish
                if (counter < 60) {//TODO: test how long idling needs to go on
                    drivetrain.arcadeDrive(0, 0);
                } else if (firstBallShot && drivetrain.getRightDistance() >= 880) {
                    counter = 0;
                    drivetrain.arcadeDrive(0, 0);
                    //System.out.println("stopping from kIdling");
                    state = kStop;//if we already shot a ball and drove forward, end auto
                } else {
                    drivetrain.arcadeDrive(0, 0);
                    state = kDrivingForward;//drives forward
                    //System.out.println("driving forward from kIdling");
                    counter = 0;
                }
                break;
            case kDrivingForward://drives forward
                if (counter > 140) {//timeout
                    state = kProcessing;
                    counter = 0;
                    drivetrain.arcadeDrive(0, 0);
                } else if (drivetrain.getRightDistance() < 900) {
                    drivetrain.arcadeDrive(-1, 0);//drive forwards
                } else if (!firstBallShot) {
                    //System.out.println("processing from kDrivingForward");
                    drivetrain.arcadeDrive(0, 0);
                    state = kProcessing;//go back to processing and shoot the ball if its in the 2nd half
                } else {
                    drivetrain.arcadeDrive(0, 0);
                    //state = kMobility;
                    // System.out.println("stopping from kDrivingForward");
                    counter = 0;
                    state = kStop;//stop the robot
                }
                break;
            case kMobility:
                //can be used in place of kStop if we don't always get mobility points
                if (counter < 50) {
                    drivetrain.arcadeDrive(-1, 0);//drive forward
                } else {
                    state = kStop;//stops the robot
                    counter = 0;
                }
                break;
            case kStop://stops the robot
                drivetrain.arcadeDrive(0, 0);
                break;

        }

    }

    /**
     * shoots the ball in the hot goal by waiting until the goal in front of us
     * is hot, shooting, then it collects the ball, settles the ball, then
     * shoots the ball. Pretty much hotOneBallPeriodic but shoots the 2nd ball
     */
    /*
     * public void hotTwoBallPeriodicNoTurn() { catapult.update(30, 30);
     * drivetrain.lowGear(); //System.out.println("hot two ball no turn");
     * switch (state) { case kBlooming://starting state
     * vision.lookForHorizontalInfo();//sets the vision to look for the
     * horizontal info if (counter < 40) { //Wait for stuff to open
     * leftPanel.wilt(); rightPanel.wilt(); backPanel.bloom();
     * collector.bloom(); collector.stop(); } else { state = kProcessing;//wait
     * for image processing counter = 0; } break; case kProcessing:
     * //System.out.println("processing"); if ((System.currentTimeMillis() -
     * autoStartTime) > 2000 &&
     * vision.isHorizontalDetectedWithinFirstTwoUpdates()) {
     * drivetrain.arcadeDrive(0, 0);
     * System.out.println("\n***********************horizontal detected,
     * shooting"); state = kShooting; } else if ((System.currentTimeMillis() -
     * autoStartTime) > 4750) { drivetrain.arcadeDrive(0, 0);
     * System.out.println("\n***********************horizontal not detected,
     * shooting"); state = kShooting; } else { drivetrain.arcadeDrive(0, 0); }
     *
     * break; case kShooting: firstBallShot = true; catapult.shoot();
     * drivetrain.arcadeDrive(0, 0); counter = 0;
     * System.out.println("******SHOOTING"); if (!secondBallShot) { state =
     * kIdling;//idle the bot after shooting } else { //state = kMobility;
     * counter = 0; state = kStop;//stop the robot, 2nd ball has been shot TODO:
     * change to kMobility after testing everything } break; case
     * kIdling://waits for the shooter to finish if (counter < 60) {//TODO: test
     * how long idling needs to go on drivetrain.arcadeDrive(0, 0); } else {
     * counter = 0; drivetrain.arcadeDrive(0, 0); state =
     * kDrivingForward;//drives forward } break; case kDrivingForward://drives
     * forward if(counter > 90){ counter = 0; state = kSettlingBall;
     * drivetrain.arcadeDrive(0, 0); } else if (drivetrain.getRightDistance() <
     * 900) { collector.drive(); backPanel.wilt(); drivetrain.arcadeDrive(-1,
     * 0);//drive forwards } else { collector.drive(); backPanel.wilt(); counter
     * = 0; drivetrain.arcadeDrive(0, 0); state = kSettlingBall; } break; case
     * kSettlingBall: //settle ball if (counter < 60) { collector.wilt();
     * backPanel.wilt(); } else if (counter < 100) { collector.bloom();
     * collector.stop(); } else { counter = 0; state = kIdling2; } break; case
     * kIdling2: if (catapult.isDown()) { state = kShooting;//shoot the 2nd ball
     * secondBallShot = true; } break; case kMobility: if (counter < 50) {
     * drivetrain.arcadeDrive(-1, 0);//drive forward } else { state =
     * kStop;//stops the robot counter = 0; } break; case kStop://stops the
     * robot drivetrain.arcadeDrive(0, 0); break; } }
     *
     * /**
     * shoots the ball into the hot goal by (if the goal is hot within first
     * half): Shooting the first ball into the goal in front of us, gets the
     * ball, turns left to the next hot goal, waits until the second
     * half,settles, and shoots the second ball. Or (if the goal is hot within
     * second half): waits for the second half, shoots the ball, gets the ball,
     * settles, and shoots the second ball.
     */
    /*
     * public void hotTwoBallPeriodicWithTurn() { catapult.update(30, 30);
     * drivetrain.lowGear(); //System.out.println("hot two ball no turn");
     * switch (state) { case kBlooming://starting state
     * vision.lookForHorizontalInfo();//sets the vision to look for the
     * horizontal info if (counter < 40) { //Wait for stuff to open
     * leftPanel.wilt(); rightPanel.wilt(); backPanel.bloom();
     * collector.bloom(); collector.stop(); } else { state = kProcessing;//wait
     * for image processing counter = 0; } break; case kProcessing:
     * //System.out.println("processing"); if ((System.currentTimeMillis() -
     * autoStartTime) > 2000 &&
     * vision.isHorizontalDetectedWithinFirstTwoUpdates()) {
     * drivetrain.arcadeDrive(0, 0);
     * System.out.println("\n***********************horizontal detected,
     * shooting"); state = kShooting; } else if ((System.currentTimeMillis() -
     * autoStartTime) > 4250) { drivetrain.arcadeDrive(0, 0);
     * System.out.println("\n***********************horizontal not detected,
     * shooting"); state = kShooting; } else { drivetrain.arcadeDrive(0, 0); }
     *
     * break; case kShooting: firstBallShot = true; catapult.shoot();
     * drivetrain.arcadeDrive(0, 0); counter = 0;
     * System.out.println("******SHOOTING"); if (!secondBallShot) { state =
     * kIdling;//idle the bot after shooting } else { //state = kMobility;
     * counter = 0; state = kStop;//stop the robot, 2nd ball has been shot TODO:
     * change to kMobility after testing everything } break; case
     * kIdling://waits for the shooter to finish if (counter < 60) {//TODO: test
     * how long idling needs to go on drivetrain.arcadeDrive(0, 0); } else {
     * drivetrain.arcadeDrive(0, 0); state = kDrivingForward;//drives forward
     * counter = 0; } break; case kDrivingForward://drives forward and turns if
     * necessary if(counter > 90){ counter = 0; state = kSettlingBall;
     * drivetrain.arcadeDrive(0, 0); } else if (drivetrain.getRightDistance() <
     * 900) { collector.drive(); backPanel.wilt(); drivetrain.arcadeDrive(-1,
     * 0);//drive forwards } else if
     * (vision.isHorizontalDetectedWithinFirstTwoUpdates() &&
     * drivetrain.getRightDistance() < 1100) {//TODO: tune how long we turn
     * //turn left for second hot ball if your first hot ball was within first
     * half collector.drive(); backPanel.wilt(); drivetrain.arcadeDrive(0,
     * 1);//turn left counter = 0; } else if
     * (vision.isHorizontalDetectedWithinFirstTwoUpdates() &&
     * drivetrain.getRightDistance() >= 1080 && System.currentTimeMillis() -
     * autoStartTime > 4250) { //waits until you are within the second half of
     * auto to shoot second ball collector.drive(); backPanel.wilt();
     * drivetrain.arcadeDrive(0, 0); state = kSettlingBall; } else if
     * (!vision.isHorizontalDetectedWithinFirstTwoUpdates()) { //if the goal was
     * hot in the second half, move on collector.drive(); backPanel.wilt();
     * drivetrain.arcadeDrive(0, 0); state = kSettlingBall; } else { //stop the
     * drivetrain if no other conditions are met drivetrain.arcadeDrive(0, 0); }
     * break; case kSettlingBall: //settle ball if (counter < 60) { //brings
     * collector and backpanel up collector.wilt(); backPanel.wilt();
     * drivetrain.arcadeDrive(0, 0); } else if (counter < 100) { //brings
     * collector down, stops collector collector.bloom(); collector.stop();
     * drivetrain.arcadeDrive(0, 0); } else { counter = 0;
     * drivetrain.arcadeDrive(0, 0); state = kIdling2;//move onto idling }
     * break; case kIdling2: if (catapult.isDown()) {//ensures full power on
     * catapult secondBallShot = true; state = kShooting;//shoot the 2nd ball }
     * break; case kMobility: if (counter < 50) { drivetrain.arcadeDrive(-1,
     * 0);//drive forward } else { state = kStop;//stops the robot counter = 0;
     * } break; case kStop://stops the robot drivetrain.arcadeDrive(0, 0);
     * break; }
    }
     */
    public void teleopInit() {
        init();
        leftPanel.bloom();
        backPanel.bloom();
        rightPanel.bloom();
        drivetrain.highGear();

        //Killing Beaglebone
        //vision.disconnect();
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        periodic();
        catapult.update();
        // System.out.println(drivetrain.getRightDistance());
        //catapult.drive(operatorController.getLeftY());

        //Drive the robot
        drivetrain.arcadeDrive(driverGamepad.getLeftY(), driverGamepad.getAnalogTriggers());

        if (rightTrigger.get()) {
            drivetrain.highGear();
        } else if (leftTrigger.get()) {
            drivetrain.lowGear();
        }

        //Set catapult state to shoot
        if (button5.get() && button7.get()) {
            if (collector.isBloomed()) {
                catapult.shoot();
            } else {
                catapult.engageMotors();
                catapult.softShoot();
                collector.backdrive();
                //collector.bloom();
            }
        } else if (button10.get() || yButton.get()) {
            System.out.println("Button for soft shooting engaged.  Calling method.");
            catapult.softShoot();
            collector.backdrive();
        }
        //catapult.disengageRatchet();

        if (button9.get()) {
            catapult.retract();
        }

        //Setting collector position
        if (button6.get()) {
            collector.wilt();
        } else if (button8.get()) {
            collector.bloom();
        }

        //Setting collector state
        if (button4.get()) {
            collector.drive();
        } else if (button2.get()) {
            collector.backdrive();
        } else if (button3.get() || button1.get()) {
            collector.stop();
        }

        //Catcher panels
        if (operatorController.dPadUp() && !previousDPadUp) {
            if (allPanelsBlooming) {
                leftPanel.bloom();
                backPanel.bloom();
                rightPanel.bloom();
                allPanelsBlooming = false;
            } else {
                leftPanel.wilt();
                backPanel.wilt();
                rightPanel.wilt();
                allPanelsBlooming = true;
            }
        } else if (operatorController.dPadDown() && !previousDPadDown) {
            if (backPanel.isBloomed()) {
                backPanel.wilt();
            } else {
                backPanel.bloom();
            }
        } else if (operatorController.dPadLeft() && !previousDPadLeft) {
            if (leftPanel.isBloomed()) {
                leftPanel.wilt();
            } else {
                leftPanel.bloom();
            }
        } else if (operatorController.dPadRight() && !previousDPadRight) {
            if (rightPanel.isBloomed()) {
                rightPanel.wilt();
            } else {
                rightPanel.bloom();
            }
        }

        //Updating previous DPad states for toggle
        previousDPadUp = operatorController.dPadUp();
        previousDPadDown = operatorController.dPadDown();
        previousDPadLeft = operatorController.dPadLeft();
        previousDPadRight = operatorController.dPadRight();
    }

    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
        drivetrain.arcadeDrive(0, 0);
        compressor.start();

        collector.wilt();
        leftPanel.wilt();
        rightPanel.wilt();
        backPanel.wilt();

        collector.stop();
        catapult.update();

        drivetrain.lowGear();

        if (button9.get()) {
            catapult.retract();
        }

        vision.lookForHorizontalInfo();
        if (vision.isHorizontalInfoUpdated()) {
            System.out.println(vision.getHorizontalX());
            System.out.println(vision.getHorizontalY());
            System.out.println(vision.isHorizontalDetected());
        }

        /*if (button10.get()) {
            vision.disconnect();
            disconnectStartTime = System.currentTimeMillis();
        }*/
    }

    public void disabledInit() {
    }

    public void disabledPeriodic() {
        if (System.currentTimeMillis() - disconnectStartTime > 20000) {
            //vision.start();
        }
        if (button1.get()&&button2.get()&&button3.get()&&button4.get()&&button10.get()) {
            //vision.disconnect();
            //disconnectStartTime = System.currentTimeMillis();
        }
        //System.out.println("right" + drivetrain.getRightDistance());
        //System.out.println("left" + drivetrain.getLeftDistance());
    }
}
