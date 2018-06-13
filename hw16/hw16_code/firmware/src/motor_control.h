#ifndef _MOTOR_CONTROL_H    /* Guard against multiple inclusion */
#define _MOTOR_CONTROL_H

int vel1_ref = 0;               // Init reference speed for motor 1
int vel2_ref = 0;               // Init reference speed for motor 2
int prev1 = 0;                  // Initialize previous encoder value
int prev2 = 0;                  // Initialize previous encoder value
static volatile int eint1 = 0;
static volatile int eint2 = 0;

void init_motors();
void init_encoders();
void init_controller();
int read_encoder1();
int read_encoder2();

#endif