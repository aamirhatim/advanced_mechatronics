# Homework 16: PI Motor Control
A PI Controller was built to control the speed of the motors. Using the encoder values, we are able to do velocity control. The same controller and gains were used for both motors, however each motor had its own error and integral error values. See [`motor_controller.c`](hw16_code/firmware/src/motor_control.c) for the PI control code.
