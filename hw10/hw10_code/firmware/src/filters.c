void init_buffer(signed short * buffer, int size) {
    int i;
    for (i = 0; i < size; i++) {
        buffer[i] = 0;
    }
}

void add_to_buffer(signed short * buffer, int size, signed short value) {
    int i;
    for (i = size-1; i > 0; i--) {
        buffer[i] = buffer[i-1];
    }
    buffer[0] = value;
}

int maf(signed short * buffer, int size) {
    int i, avg = 0;
    for (i = 0; i < size; i++) {
        avg += buffer[i];
    }
    return avg/size;
}

int fir(signed short * buffer, int size) {
    int weights[9] = {144, 439, 1202, 2025, 2380, 2025, 1202, 439, 144};
    int i;
    int avg = 0;
    for (i = 0; i < size; i++) {
        avg += buffer[i] * weights[i];
    }
    return avg/10000;
}

int iir(signed short old, signed short new) {
    int weights[] = {70, 30};
    int val = old*weights[0] + new*weights[1];
    return val/100;
}