#include <xc.h>
#include "i2c.h"

void i2c_master_setup(void) {
    I2C2BRG = 0x35;                             // I2CBRG = [1/(2*400kHz) - 104ns]*48M - 2
    I2C2CONbits.ON = 1;                         // turn on the I2C2 module
}

// Start a transmission on the I2C bus
void i2c_master_start(void) {
    I2C2CONbits.SEN = 1;                        // send the start bit
    while(I2C2CONbits.SEN) { ; }                // wait for the start bit to be sent
}

void i2c_master_restart(void) {
    I2C2CONbits.RSEN = 1;                       // send a restart
    while(I2C2CONbits.RSEN) { ; }               // wait for the restart to clear
}

void i2c_master_send(unsigned char byte) {      // send a byte to slave
  I2C2TRN = byte;                               // if an address, bit 0 = 0 for write, 1 for read
  while(I2C2STATbits.TRSTAT) { ; }              // wait for the transmission to finish
  if(I2C2STATbits.ACKSTAT) {                    // if this is high, slave has not acknowledged
  }
}

unsigned char i2c_master_recv(void) {           // receive a byte from the slave
    I2C2CONbits.RCEN = 1;                       // start receiving data
    while(!I2C2STATbits.RBF) { ; }              // wait to receive the data
    return I2C2RCV;                             // read and return the data
}

void i2c_master_ack(int val) {                  // sends ACK = 0 (slave should send another byte) or NACK = 1 (no more bytes requested from slave)
    I2C2CONbits.ACKDT = val;                    // store ACK/NACK in ACKDT
    I2C2CONbits.ACKEN = 1;                      // send ACKDT
    while(I2C2CONbits.ACKEN) { ; }              // wait for ACK/NACK to be sent
}

void i2c_master_stop(void) {                    // send a STOP:
  I2C2CONbits.PEN = 1;                          // comm is complete and master relinquishes bus
  while(I2C2CONbits.PEN) { ; }                  // wait for STOP to complete
}

void i2c_write(unsigned char reg, unsigned char val) {
    i2c_master_start();                         // make the start bit
    i2c_master_send((ADDRESS << 1)|0);          // write the address, shifted left by 1, or'ed with a 0 to indicate writing
    i2c_master_send(reg);                       // the register to write to
    i2c_master_send(val);                       // the value to put in the register
    i2c_master_stop();                          // make the stop bit
}

char i2c_read(unsigned char reg) {
    i2c_master_start();                         // make the start bit
    i2c_master_send((ADDRESS << 1)|0);          // write the address, shifted left by 1, or'ed with a 0 to indicate writing
    i2c_master_send(reg);                       // the register to read from
    i2c_master_restart();                       // make the restart bit
    i2c_master_send((ADDRESS << 1)|1);          // write the address, shifted left by 1, or'ed with a 1 to indicate reading
    char r = i2c_master_recv();                 // save the value returned
    i2c_master_ack(1);                          // make the ack so the slave knows we got it
    i2c_master_stop();                          // make the stop bit
    return r;
}

void IMU_init() {
    ANSELBbits.ANSB2 = 0;                       // Turn off analog for B2
    ANSELBbits.ANSB3 = 0;                       // Turn off analog for B3
    i2c_master_setup();                         // Turn on I2C module
    
    i2c_write(0x10, 0b10000010);                // CTRL1_XL, 1.66kHz sample rate, +/-2g, 100Hz filter
    i2c_write(0x11, 0b10001000);                // CTRL2_G, 1.66kHz sample rate, 1000dps
    i2c_write(0x12, 0b00000100);                // Enable incremental multiple reads
    i2c_write(0x0B, 0b00100000);
}

void i2c_read_multiple(unsigned char address, unsigned char reg, unsigned char * data, int length) {
    int i;
    i2c_master_start();                         // make the start bit
    i2c_master_send((address << 1)|0);          // write the address, shifted left by 1, or'ed with a 0 to indicate writing
    i2c_master_send(reg);                       // the register to read from
    i2c_master_restart();                       // make the restart bit
    i2c_master_send((address << 1)|1);          // write the address, shifted left by 1, or'ed with a 1 to indicate reading
    for (i = 0; i < length-1; i++) {
        data[i] = i2c_master_recv();            // save the value returned into data array
        i2c_master_ack(0);                      // Send ack of 0 to keep reading
    }
    data[length-1] = i2c_master_recv();         // Read last value and store into data array
    i2c_master_ack(1);                          // make the ack so the slave knows we got it
    i2c_master_stop();                          // make the stop bit
}

void combine_bytes(unsigned char * raw, signed short * data, int len) {
    int j = 0;
    while (j < len) {                                       // Combine bytes in data array to get IMU info
        signed short high = raw[j+1] << 8;
        signed short low = raw[j];
        data[j/2] = high|low;                               // Shift the high byte left 8 units and OR it with the low byte
        j=j+2;
    }
}