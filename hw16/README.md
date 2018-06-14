# Homework 16: PI Motor Control
A PI Controller was built to control the speed of the motors. Using the encoder values, we are able to do velocity control. The same controller and gains were used for both motors, however each motor had its own error and integral error values. A 20KHz PWM was used to run the motors and a 50Hz interrupt timer was used to set a new duty cycle.

See [`motor_controller.c`](hw16_code/firmware/src/motor_control.c) for the PI control code. Its implementation can be found in [`system_interrupt.c`](hw16_code/firmware/src/system_config/default/system_interrupt.c).
