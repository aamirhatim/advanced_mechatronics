#ifndef _SPI_H    /* Guard against multiple inclusion */
#define _SPI_H

#define CS LATAbits.LATA0           // Chip select pin

void init_SPI();
char SPI1_IO(char);

#endif
