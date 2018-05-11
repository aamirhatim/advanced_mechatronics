#ifndef _FILTERS_H    /* Guard against multiple inclusion */
#define _FILTERS_H

void init_buffer(signed short *, int);
void add_to_buffer(signed short *, int, signed short);
int maf(signed short *, int);
int fir(signed short *, int);
int iir(signed short, signed short);

#endif
