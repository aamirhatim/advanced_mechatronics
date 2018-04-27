#ifndef _I2C_H    /* Guard against multiple inclusion */
#define _I2C_H

void IMU_init();
void i2c_write(unsigned char, unsigned char);
char i2c_read(unsigned char);

#endif
