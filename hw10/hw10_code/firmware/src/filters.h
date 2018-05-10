#ifndef _FILTERS_H    /* Guard against multiple inclusion */
#define _FILTERS_H

void init_buffer(signed short *);
void add_to_buffer(signed short *, signed short);
int average(signed short *);

#endif
