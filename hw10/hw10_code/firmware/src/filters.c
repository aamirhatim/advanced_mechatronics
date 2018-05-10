void init_buffer(signed short * buffer) {
    int buff_size = sizeof(buffer)/sizeof(buffer[0]);
    int i;
    for (i = 0; i < buff_size; i++) {
        buffer[i] = 0;
    }
}

void add_to_buffer(signed short * buffer, signed short value) {
    int buff_size = sizeof(buffer)/sizeof(buffer[0]);
    int i;
    for (i = buff_size-1; i > 0; i--) {
        buffer[i] = buffer[i-1];
    }
    buffer[0] = value;
}

int average(signed short * buffer) {
    int buff_size = sizeof(buffer)/sizeof(buffer[0]);
    int i, avg = 0;
    for (i = 0; i < buff_size; i++) {
        avg += buffer[i];
    }
    return avg;
}