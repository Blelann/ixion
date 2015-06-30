#include <Servo.h>

Servo servoMoteur;
int angle=0;
int power=0;
String input="";
int state = 0;

void setup() {
  servoMoteur.attach(9);
  Serial.begin(9600);
}

void loop() {
  while (Serial.available()) {
    input = Serial.readStringUntil('\n');
    angle = getValue(input,';',0).toInt();
    power = getValue(input,';',1).toInt();
    // servoMoteur.write(angle);
  }
}

String getValue(String data, char separator, int index)
{
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length()-1;

  for(int i=0; i<=maxIndex && found<=index; i++){
    if(data.charAt(i)==separator || i==maxIndex){
        found++;
        strIndex[0] = strIndex[1]+1;
        strIndex[1] = (i == maxIndex) ? i+1 : i;
    }
  }
  return found>index ? data.substring(strIndex[0], strIndex[1]) : "";
}


