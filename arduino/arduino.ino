int ticketsToDispense = 0;
int thresholdHigh = 250;
int thresholdLow = 150;
bool thresholdMet = false;

// Stepper delay controls speed. Lower is faster.
int stepperDelay = 300;

void setup() {
  pinMode(4, OUTPUT); // Direction
  pinMode(5, OUTPUT); // Step
  pinMode(6, OUTPUT); // Enable

  digitalWrite(4, LOW);
  digitalWrite(6, LOW);

  Serial.begin(9600);

  thresholdMet = analogRead(A0) >= thresholdHigh;
  Serial.println(thresholdMet);

  delay(1000);
}

int counter = 0;
int input = 0;
unsigned char incomingByte = 0;

void loop() {
  // Read serial input
  while (Serial.available() > 0) {
    incomingByte = Serial.read();

    // Check if the incoming byte is the delimiter
    if(incomingByte == '.') {
      // The number is finished, add to tickets to dispense
      ticketsToDispense += input;
      Serial.print("Printing ");
      Serial.print(input);
      Serial.print(" tickets\n");
      input = 0;
    }
    else {
      // Add to input
      input = 10 * input + (incomingByte - '0');
    }
  }
  
  if(counter < ticketsToDispense) {
    int value = analogRead(A0);

    if(value >= thresholdHigh && !thresholdMet) {
      thresholdMet = true;
      counter++;
      Serial.print("Ticket " );
      Serial.print(counter);
      Serial.print(" dispensed!\n");
    }
    else if(value <= thresholdLow && thresholdMet) {
      thresholdMet = false;
    }
  
//    if(counter < ticketsToDispense) {
      digitalWrite(5, HIGH);
      delayMicroseconds(stepperDelay);
      digitalWrite(5, LOW);
      delayMicroseconds(stepperDelay);
//    }
  }
}
