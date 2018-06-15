#include "motor_control.h"
#include <xc.h>
#include "app.h"

#define MAX_SPEED 50

void init_motors() {
    RPA0Rbits.RPA0R = 0b0101;   // A0 to OC1 (motor 1)
    RPB13Rbits.RPB13R = 0b0101; // B13 to OC4 (motor 2)
    
    // Init timer
    T2CONbits.TCKPS = 0;        // Timer2 prescaler N=1 (1:1)
    PR2 = 2399;                 // 48000000 Hz / 20000 Hz / 1 - 1 = 2399 (20kHz PWM from 48MHz clock with 1:1 prescaler)
    TMR2 = 0;                   // initial TMR2 count is 0
    
    // Set up motor 1
    OC1CONbits.OCM = 0b110;     // PWM mode without fault pin; other OCxCON bits are defaults
    OC1RS = 0;                  // duty cycle
    OC1R = 0;                   // initialize before turning OC1 on; afterward it is read-only
    
    // Set up motor 2
    OC4CONbits.OCM = 0b110;     // PWM mode without fault pin; other OCxCON bits are defaults
    OC4RS = 0;                  // duty cycle
    OC4R = 0;                   // initialize before turning OC4 on; afterward it is read-only
    
    T2CONbits.ON = 1;           // turn on Timer2
    OC1CONbits.ON = 1;          // turn on OC1
    OC4CONbits.ON = 1;          // turn on OC4
}

void init_encoders() {
    T5CKRbits.T5CKR = 0b0100;   // B9 is read by T5CK (encoder 1)
    T3CKRbits.T3CKR = 0b0100;   // B8 is read by T3CK (encoder 2)
    
    // Motor 1 encoder
    T5CONbits.TCS = 1;          // count external pulses
    PR5 = 0xFFFF;               // enable counting to max value of 2^16 - 1
    TMR5 = 0;                   // set the timer count to zero
    T5CONbits.ON = 1;           // turn Timer on and start counting
    
    // Motor 2 encoder
    T3CONbits.TCS = 1;          // count external pulses
    PR3 = 0xFFFF;               // enable counting to max value of 2^16 - 1
    TMR3 = 0;                   // set the timer count to zero
    T3CONbits.ON = 1;           // turn Timer on and start counting
}

void init_controller() {
    T4CONbits.TCKPS = 0b101;    // Timer4 prescaler N=32
    PR4 = 29999;                // 48000000 Hz / 50 Hz / 4 - 1 = 29999 (50Hz from 48MHz clock with 32:1 prescaler)
    TMR4 = 0;                   // initial TMR4 count is 0
    T4CONbits.ON = 1;
    IPC4bits.T4IP = 4;          // priority for Timer 4 
    IFS0bits.T4IF = 0;          // clear interrupt flag for Timer4
    IEC0bits.T4IE = 1;          // enable interrupt for Timer4
}

int read_encoder1() {
    return TMR5;
}

int read_encoder2() {
    return TMR3;
}

void reset_encoders() {
    TMR5 = 0;
    TMR3 = 0;
}

float pi_control(int reference, int actual, int motor_num) {
    int err;
    float u;
    
    err = reference - actual;
    if (ki*(eint[motor_num-1] + err) <= 1.0) {
        eint[motor_num-1] += err;
    }
    
    u = (float) (kp*err + ki*eint[motor_num-1]);
    
    if (u > 100.0) {
        u = 100.0;
    }
    else if (u < 0.0) {
        u = 0.0;
    }
        
    return u/100.0;
}