#include <xc.h>
#include "expander.h"

void initExpander() {
    ANSELBbits.ANSB2 = 0;       // Turn off analog for B2
    ANSELBbits.ANSB3 = 0;       // Turn off analog for B3
    
}