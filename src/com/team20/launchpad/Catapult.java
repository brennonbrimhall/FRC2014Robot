/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.team20.launchpad;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author Driver
 */
public class Catapult extends Subsystem {
    
    
    private final int kEngaging = 1;
    private final int kRetracting = 2;
    private final int kIdling1 = 3;
    
    //Shooting full
    private final int kDisengaging = 4;
    private final int kIdling2 = 5;
    private final int kShooting = 6;
    private final int kIdling3 = 7;
    
    //Shooting softly
    private final int kRedundantlyEngaging = 8;
    private final int kIdling4 = 9;
    private final int kShootingSoft = 10;
    private final int kIdling5 = 11;
    
    private int state = 1;
    private int counter;
    
    Talon talon1 = new Talon(7);
    Talon talon2 = new Talon(8);
    
    DoubleSolenoid motors = new DoubleSolenoid(1, 1, 2);
    DoubleSolenoid ratchet = new DoubleSolenoid(1, 3, 4);
    
    DigitalInput bumpSwitch = new DigitalInput(2);
    
    public void update(int cyclesToWait, int cyclesToWaitSoft) {
        if(state == kEngaging){
            //Engage motors and ratchet
            engageMotors();
            engageRatchet();
            
            //Move on to retraction
            counter = 0;
            state++;
        }else if(state == kRetracting){
            //Retract until bump switch is pressed
            counter++;
            if(!isDown() && counter < 100){
                //Keeping motors and ratchet engaged
                engageMotors();
                engageRatchet();
                
                //Driving catapult down
                drive(1.0);
            }else{
                //Move on to disengaging
                state++;
            }
        }else if(state == kIdling1){
            //Keep motors engaged and do nothing
            engageMotors();
            drive(0);
            //Don't automatically move on to shooting
            
        }else if(state == kDisengaging){
            //Disengaging motors
            disengageMotors();
            //Move on to idling
            state++;
            counter = 0;
        }else if (state == kIdling2){
            //Backdriving gearbox slightly
            disengageMotors();
            drive(0);
            if(counter < 10){
                counter++;
            }else{
                state++;
            }
            
        }else if(state == kShooting){
            //Disengaging ratchet and firing
            disengageRatchet();
            disengageMotors();
            drive(0);
            
            //Resetting counter
            counter = 0;
            state++;
        }else if(state == kIdling3){
            //Waiting as the mechanics fire
            counter++;
            drive(0);
            if(counter < cyclesToWait){
                disengageRatchet();
                disengageMotors();
            }else{
                //Resuming enganging sequence
                state = kEngaging;
            }
        }else if(state == kRedundantlyEngaging){
            //Engaging motors
            engageMotors();
            //Move on to idling
            state++;
            counter = 0;
        }else if (state == kIdling4){
            //Backdriving gearbox slightly
            drive(0);
            if(counter < 10){
                counter++;
            }else{
                state++;
            }
        }else if(state == kShootingSoft){
            //Disengaging ratchet and firing
            disengageRatchet();
            engageMotors();
            drive(0);
            
            //Resetting counter
            counter = 0;
            state++;
        }else if(state == kIdling5){
            //Waiting as the mechanics fire
            counter++;
            drive(0);
            if(counter < cyclesToWaitSoft){
                disengageRatchet();
            }else{
                //Resuming enganging sequence
                state = kEngaging;
            }
        }
    }
    
    public void update(){
        update(30, 150);
    }
    
    public void drive(double speed) {
        talon1.set(speed);
        talon2.set(speed);
    }

    public void engageRatchet() {
        ratchet.set(DoubleSolenoid.Value.kForward);
    }

    public void disengageRatchet() {
        ratchet.set(DoubleSolenoid.Value.kReverse);
    }

    public void engageMotors() {
        motors.set(DoubleSolenoid.Value.kForward);
    }

    public void disengageMotors() {
        motors.set(DoubleSolenoid.Value.kReverse);
    }

    public boolean areMotorsEngaged() {
        return motors.get() == DoubleSolenoid.Value.kForward;
    }

    public boolean isRatchetEngaged() {
        return ratchet.get() == DoubleSolenoid.Value.kReverse;
    }
    
    public boolean isDown() {
        return !bumpSwitch.get();
    }
    
    public void shoot(){
        state = kDisengaging;
    }
    
    public void softShoot(){
        state = kRedundantlyEngaging;
    }
    
    public void retract(){
        state = kEngaging;
    }
    
    public boolean isRetracting(){
        return state == kRetracting;
    }
}
