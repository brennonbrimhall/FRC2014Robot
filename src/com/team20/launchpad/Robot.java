/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.team20.launchpad;


import edu.wpi.first.wpilibj.Compressor;
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
    boolean allPanelsBlooming = true;
    
    Drivetrain drivetrain;
    Collector collector;
    Compressor compressor;
    Relay relay;
    Catapult catapult;
    CatcherPanel rightPanel, leftPanel, backPanel;
    
    //Auto
    int autonomousMode = 1;
    final int kOneBall = 1, kTwoBall = 2;
    
    int counter = 0;
    
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
    }
    
    public void init(){
        collector.bloom();
        leftPanel.bloom();
        backPanel.bloom();
        rightPanel.bloom();
        drivetrain.resetEncoders();
    }
    
    public void periodic(){
        //Update subsystems
        collector.update();
        catapult.update();
        compressor.start();
        System.out.println("L:\t"+drivetrain.getLeftDistance());
        System.out.println("R:\t"+drivetrain.getRightDistance());
    }
    
    public void autonomousInit(){
        init();
        counter = 0;
        if(DriverStation.getInstance().getDigitalIn(1)){
            autonomousMode = 1;
        }else if(DriverStation.getInstance().getDigitalIn(2)){
            autonomousMode = 2;
        }
    }
    
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        counter++;
        periodic();
        switch(autonomousMode){
            case kOneBall:
                oneBallPeriodic();
                break;
            case kTwoBall:
                twoBallPeriodic();
                break;
            default:
                break;
        }
    }
    
    public void oneBallPeriodic(){
        drivetrain.lowGear();
        if(counter < 20){
            //Waiting for panels and collector to come out
        
        }else if(counter < 250){
            if(drivetrain.getLeftDistance() < 1100){
                drivetrain.arcadeDrive(-.5, 0);
            }else{
                //System.out.println("Counter:"+counter);
                drivetrain.arcadeDrive(0, 0);
            }
            
        }else if(counter < 260){
            catapult.shoot();
            drivetrain.arcadeDrive(0, 0);
        }else{
            //Catapult retracts automatically
            drivetrain.arcadeDrive(0, 0);
        }
    }
    
    public void twoBallPeriodic(){
        
    }
    
    public void teleopInit(){
        init();
    }
    
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        periodic();
        
        //catapult.drive(operatorController.getLeftY());
        
        //Drive the robot
        drivetrain.arcadeDrive(driverGamepad.getLeftY(), driverGamepad.getAnalogTriggers());
        
        if (rightTrigger.get()) {
            drivetrain.highGear();
        } else if (leftTrigger.get()) {
            drivetrain.lowGear();
        }
        
        //Set catapult state to shoot
        if (button5.get()) {
            if(collector.isBloomed()){
                catapult.shoot();
            }else {
                catapult.engageMotors();
                catapult.shoot();
                
                //collector.bloom();
            }
            //catapult.disengageRatchet();
        }else if(button7.get()){
            catapult.engageMotors();
        }
        
        if(button9.get()){
            catapult.engage();
        }else if(button10.get()){
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
            if(allPanelsBlooming){
                leftPanel.bloom();
                backPanel.bloom();
                rightPanel.bloom();
                allPanelsBlooming = false;
            }else{
                leftPanel.wilt();
                backPanel.wilt();
                rightPanel.wilt();
                allPanelsBlooming = true;
            }
        } else if (operatorController.dPadDown() && !previousDPadDown) {
            if(backPanel.isBloomed()){
                backPanel.wilt();
            }else{
                backPanel.bloom();
            }
        } else if (operatorController.dPadLeft() && !previousDPadLeft) {
            if(leftPanel.isBloomed()){
                leftPanel.wilt();
            }else{
                leftPanel.bloom();
            }
        } else if (operatorController.dPadRight() && !previousDPadRight) {
            if(rightPanel.isBloomed()){
                rightPanel.wilt();
            }else{
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
        compressor.start();
        
        collector.wilt();
        leftPanel.wilt();
        rightPanel.wilt();
        backPanel.wilt();
        
        collector.stop();
        catapult.update();
        
        drivetrain.lowGear();
    }
}