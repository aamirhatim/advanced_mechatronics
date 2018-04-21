#include <xc.h>                      // processor SFR definitions
#include "spi.h"

// Read/Write over SPI
char SPI1_IO(char cmd) {
    SPI1BUF = cmd;                  // Add cmd to buffer
    while(!SPI1STATbits.SPIRBF) {;} // Wait to receive byte
    return SPI1BUF;                 // Return buffer
}

// Initialize pins for the DAC (SPI interface)
void init_SPI() {
SDI1Rbits.SDI1R = 0b0100;           // Set RPB8 to SDI1 (not connected to DAC)
TRISAbits.TRISA1 = 0;               // Make RPA1 an output pin
RPA1Rbits.RPA1R = 0b0011;           // Set RPA1 to SDO1 (pin 3 -> SDI pin 5 on DAC)
TRISAbits.TRISA0 = 0;               // Set A0 as an output pin (CS)
SS1Rbits.SS1R = 0b0000;             // Set RPA0 to SS1 (pin 2 -> CS pin 3 on DAC)
CS = 1;

SPI1CON = 0;                        // Turn off SPI module and reset
SPI1BUF;                            // Clear Rx buffer by reading from it
SPI1BRG = 0x1;                      // Set baud rate to 10MHz (SPI1BRG = (80M/(2*desired))-1)
SPI1STATbits.SPIROV = 0;            // Clear overflow bit
SPI1CONbits.CKE = 1;                // Data changes when clock goes from high to low
SPI1CONbits.MSTEN = 1;              // Master operation
SPI1CONbits.ON = 1;                 // Enable SPI1

CS = 0;
SPI1_IO(0x01);
SPI1_IO(0x41);
CS = 1;
}

// Create data sequence to send to DAC
void SPI_write(char config_bits, short int data) {
    char first = data >> 6;         // Get first four bits of input
    char bit1 = config_bits | first;
    char bit2 = data << 2;        // Get remaining 6 bits of input
    
    CS = 0;
    SPI1_IO(bit1);
    SPI1_IO(bit2);
    CS = 1;
}