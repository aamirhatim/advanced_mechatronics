#include "motor_control.h"
#include <xc.h>

void init_motors() {
    RPA0Rbits.RPA0R = 0b0101;   // A0 to OC1
    RPB13Rbits.RPB13R = 0b0101; // B13 to OC4
    
    T2CONbits.TCKPS = 0;        // Timer2 prescaler N=1 (1:1)
    PR2 = 2399;                 // 48000000 Hz / 20000 Hz / 1 - 1 = 2399 (20kHz PWM from 48MHz clock with 1:1 prescaler)
    TMR2 = 0;                   // initial TMR2 count is 0
    OC1CONbits.OCM = 0b110;     // PWM mode without fault pin; other OCxCON bits are defaults
    OC1RS = 0;                  // duty cycle
    OC1R = 0;                   // initialize before turning OC1 on; afterward it is read-only
    OC4CONbits.OCM = 0b110;     // PWM mode without fault pin; other OCxCON bits are defaults
    OC4RS = 0;                  // duty cycle
    OC4R = 0;                   // initialize before turning OC4 on; afterward it is read-only
    T2CONbits.ON = 1;           // turn on Timer2
    OC1CONbits.ON = 1;          // turn on OC1
    OC4CONbits.ON = 1;          // turn on OC4
}

void init_encoders() {
    T5CKRbits.T5CKR = 0b0100;   // B9 is read by T5CK
    T3CKRbits.T3CKR = 0b0100;   // B8 is read by T3CK
    
    T5CONbits.TCS = 1;          // count external pulses
    PR5 = 0xFFFF;               // enable counting to max value of 2^16 - 1
    TMR5 = 0;                   // set the timer count to zero
    T5CONbits.ON = 1;           // turn Timer on and start counting
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