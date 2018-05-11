function imu_filter()
%IMU Summary of this function goes here
%   Detailed explanation goes here

% Opening COM connection
if ~isempty(instrfind)
    fclose(instrfind);
    delete(instrfind);
end

port = '/dev/ttyACM0';

fprintf('Opening port %s....\n',port);

% settings for opening the serial port. baud rate 230400, hardware flow control
% wait up to 120 seconds for data before timing out
mySerial = serial(port, 'BaudRate', 230400, 'FlowControl', 'hardware','Timeout',120); 
% opens serial connection
fopen(mySerial);
% closes serial port when function exits
clean = onCleanup(@()fclose(mySerial));                                 

% menu for reading data 
has_quit = false;
while ~has_quit
    fprintf('IMU DATA FILTERING\n\n');
    selection = input('Enter "r" to get IMU data, "q" to quit: ', 's');
    
    % send the command to the PIC32
    fprintf(mySerial,'%c\n',selection);
    
    switch selection
        case 'r'
           fprintf("Waiting for samples...\n");
           data = zeros(100,5);
           
           % Read IMU data into MATLAB variable
           for i = 1:100
               data(i,:) = fscanf(mySerial, '%d %d %d %d %d');
           end
           fprintf("Done.\n");
           
           % Plot
           figure();
           hold on;
           plot(data(:,1), data(:,2));
           plot(data(:,1), data(:,3));
           plot(data(:,1), data(:,4));
           plot(data(:,1), data(:,5));
           legend("Raw", "MAV", "FIR", "IIR");
        case 'q'
            fprintf("We're done here.\n\n");
            has_quit = true;
        otherwise
            fprintf('Invalid entry!\n\n');
    end
end

end

