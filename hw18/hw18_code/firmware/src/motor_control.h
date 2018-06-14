#ifndef _MOTOR_CONTROL_H    /* Guard against multiple inclusion */
#define _MOTOR_CONTROL_H

static int enc_ref[2] = {15, 15};           // Init reference speed for motors (counts/sec)

static float kp = 10.0, ki = 0.1;           // Init Kp and Ki gains
static volatile int eint[2] = {0, 0};       // Init integral error for both motors

void init_motors();
void init_encoders();
void init_controller();
int read_encoder1();
int read_encoder2();
void reset_encoders();
float pi_control(int, int, int);

#endif