#ifndef _SPI_H    /* Guard against multiple inclusion */
#define _SPI_H

#define CS LATAbits.LATA0           // Chip select pin
#define CONFIGA 0b01110000          // Config bits for DACa
#define CONFIGB 0b11110000          // Config bits for DACb

void init_SPI();
void DAC_write(char, short int);

#endif
