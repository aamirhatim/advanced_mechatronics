#include <xc.h>             // processor SFR definitions
#include <sys/attribs.h>    // __ISR macro
#include <stdio.h>          // Inmport sprintf()
#include "i2c.h"            // Import expander chip library
#include "ST7735.h"         // Import LCD library

#define LED LATAbits.LATA4
#define MAX_G 18000.0        // Upper limit for raw acceleration value

// DEVCFG0
#pragma config DEBUG = OFF // no debugging
#pragma config JTAGEN = OFF // no jtag
#pragma config ICESEL = ICS_PGx1 // use PGED1 and PGEC1
#pragma config PWP = OFF // no write protect
#pragma config BWP = OFF // no boot write protect
#pragma config CP = OFF // no code protect

// DEVCFG1
#pragma config FNOSC = PRIPLL // use primary oscillator with pll
#pragma config FSOSCEN = OFF // turn off secondary oscillator
#pragma config IESO = OFF // no switching clocks
#pragma config POSCMOD = HS // high speed crystal mode
#pragma config OSCIOFNC = OFF // disable secondary osc
#pragma config FPBDIV = DIV_1 // divide sysclk freq by 1 for peripheral bus clock
#pragma config FCKSM = CSDCMD // do not enable clock switch
#pragma config WDTPS = PS1 // use slowest wdt
#pragma config WINDIS = OFF // wdt no window mode
#pragma config FWDTEN = OFF // wdt disabled
#pragma config FWDTWINSZ = WINSZ_25 // wdt window at 25%

// DEVCFG2 - get the sysclk clock to 48MHz from the 8MHz crystal
#pragma config FPLLIDIV = DIV_2 // divide input clock to be in range 4-5MHz
#pragma config FPLLMUL = MUL_24 // multiply clock after FPLLIDIV
#pragma config FPLLODIV = DIV_2 // divide clock after FPLLMUL to get 48MHz
#pragma config UPLLIDIV = DIV_2 // divider for the 8MHz input clock, then multiplied by 12 to get 48MHz for USB
#pragma config UPLLEN = ON // USB clock on

// DEVCFG3
#pragma config USERID = 0x5877 // 16bit userid
#pragma config PMDL1WAY = OFF // allow multiple reconfigurations
#pragma config IOL1WAY = OFF // allow multiple reconfigurations
#pragma config FUSBIDIO = ON // USB pins controlled by USB module
#pragma config FVBUSONIO = ON // USB BUSON controlled by USB module

void draw_xBar(int);
void draw_yBar(int);

int main() {

    __builtin_disable_interrupts();

    // set the CP0 CONFIG register to indicate that kseg0 is cacheable (0x3)
    __builtin_mtc0(_CP0_CONFIG, _CP0_CONFIG_SELECT, 0xa4210583);

    // 0 data RAM access wait states
    BMXCONbits.BMXWSDRM = 0x0;

    // enable multi vector interrupts
    INTCONbits.MVEC = 0x1;

    // disable JTAG to get pins back
    DDPCONbits.JTAGEN = 0;
    
    // Enable RA4 as LED output pin
    TRISAbits.TRISA4 = 0;                       // Make RA4 an output pin
    LED = 1;                                    // Set RA4 to high (turn on LED)
    
    // Initialize LCD
    LCD_init();
    
    // Initialize IMU
    IMU_init();
    
    __builtin_enable_interrupts();
    
    int i, p, j, len = 14;
    unsigned char data[len];
    signed short info[7];
    char msg[WIDTH-1];
    
    LCD_clearScreen(RED);
    for (i = 0; i < 100; i++) {
        for (p = 0; p < 3; p++) {
            LCD_drawPixel(14+i, 79+p, BLACK);
            LCD_drawPixel(63+p, 30+i, BLACK);
        }
//        LCD_drawPixel(14+i, 80, BLACK);
//        LCD_drawPixel(64, 30+i, BLACK);
    }
    
    while(1) {
        _CP0_SET_COUNT(0);
        i2c_read_multiple(ADDRESS, OUT_TEMP_L, data, len);      // Get current IMU data
        j = 0;
        while (j < len) {                                       // Combine bytes in data array to get IMU info
            signed short high = data[j+1] << 8;
            signed short low = data[j];
            info[j/2] = high|low;                               // Shift the high byte left 8 units and OR it with the low byte
            j=j+2;
        }
        
        float xscale = info[4]/MAX_G;
        float yscale = info[5]/MAX_G;
        int xbar = -50*xscale;                                  // Scale for 50 pixel bar, flip the sign
        int ybar = 50*yscale;                                   // Scale for 50 pixel bar
        sprintf(msg, "X: %d    ", xbar);
        LCD_drawString(10, 5, msg, BLACK, RED);                 // Print x-acceleration data to screen
        sprintf(msg, "Y: %d    ", ybar);
        LCD_drawString(10, 15, msg, BLACK, RED);                // Print y-acceleration data to screen
        draw_xBar(xbar);                                        // Draw x tilt line
        draw_yBar(ybar);                                        // Draw y tilt line
        LCD_drawPixel(64, 80, WHITE);                           // Draw center point
        
        LED = !LED;                                             // Toggle LED for heartbeat
        while (_CP0_GET_COUNT() <= 1200000) {;}                 // (48M/2)*.05sec => 20Hz cycles
    }
    return 0;
}

void draw_xBar(int x) {
    int i, s;
    if (x < 0) {                                                // Check the sign of the tilt to determine what direction to draw in
        s = -1;
    }
    else {
        s = 1;
    }
    
    for (i = 0; i < x*s; i++) {
            LCD_drawPixel(64+i*s, 80, WHITE);                   // Draw tilt line
        }
        while (i < 49) {
            LCD_drawPixel(64+i*s, 80, BLACK);                   // Fill remainder of the bar with black
            i++;
        }
}

void draw_yBar(int y) {
    int i, s;
    if (y < 0) {
        s = -1;
    }
    else {
        s = 1;
    }
    
    for (i = 0; i < y*s; i++) {
            LCD_drawPixel(64, 80-i*s, WHITE);
        }
        while (i < 49) {
            LCD_drawPixel(64, 80-i*s, BLACK);
            i++;
        }
}