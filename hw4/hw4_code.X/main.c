#include <xc.h>             // processor SFR definitions
#include <sys/attribs.h>    // __ISR macro
#include <math.h>           // Import math library
#include "spi.h"            // Include SPI library

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

    // do your TRIS and LAT commands here
    TRISAbits.TRISA4 = 0;               // Make RA4 an output pin
    LATAbits.LATA4 = 1;                 // Set RA4 to high (turn on LED)
    TRISBbits.TRISB4 = 1;               // Make RB4 an input pin
    
    // Initialize pins for the DAC (SPI interface)
    init_SPI();
    __builtin_enable_interrupts();

    while(1) {
	// use _CP0_SET_COUNT(0) and _CP0_GET_COUNT() to test the PIC timing
	// remember the core timer runs at half the sysclk
//        while (PORTBbits.RB4 == 0) {
//            LATAbits.LATA4 = 0;                 // Turn off LED while button is pushed
//        }
        
        _CP0_SET_COUNT(0);                      // Set core timer to 0
        
        LATAbits.LATA4 = 1;                     // Turn on LED
        CS = 0;
        SPI1_IO(0b01111111);
        SPI1_IO(0b11110000);
        CS = 1;
        while (_CP0_GET_COUNT() <= 6000000) {   // (48M/2)*.25sec
            ;                                   // Do nothing
        }
        
        _CP0_SET_COUNT(0);                      // Set core timer to 0
        
        LATAbits.LATA4 = 0;                     // Turn off LED
        CS = 0;
        SPI1_IO(0b01111111);
        SPI1_IO(0b10000000);
        CS = 1;
        while (_CP0_GET_COUNT() <= 6000000) {   // (48M/2)*.25sec
            ;                                   // Do nothing
        }
        
        
    
    }
}