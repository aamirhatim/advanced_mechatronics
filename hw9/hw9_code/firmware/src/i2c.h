#ifndef _I2C_H    /* Guard against multiple inclusion */
#define _I2C_H

#define ADDRESS 0b1101010
#define OUT_TEMP_L 0x20

void IMU_init();
void i2c_write(unsigned char, unsigned char);
char i2c_read(unsigned char);
void i2c_read_multiple(unsigned char, unsigned char, unsigned char *, int);

#endif
