#include <Servo.h>

Servo direction;
int volant = 0;
int angleServo = 0;

//partie liée au moteur
const int controlPin1 = 2; 
const int controlPin2 = 3; 
const int enablePin = 5;

int throttle = 0;//throttle
int motorSpeed = 0; // speed of the motor
int motorDir = 1; // current direction of the motor
//fcontion de recup Bluetooth
String input="";

void setup(){
  direction.attach(10);//Link Servo
  pinMode(controlPin1, OUTPUT);//Controle Moteur AV/AR
  pinMode(controlPin2, OUTPUT);//Controle Moteur AV/AR
  pinMode(enablePin, OUTPUT);//Puissance Moteur
  digitalWrite(enablePin, LOW);
  //Bluetooth
  Serial.begin(9600);
  delay(1000);
  Serial.print("AT+NAMEFABLABRC");
  delay(1000);
  Serial.print("AT+PIN1234");
  delay(1000);
  //Serial.print("AT+BAUD7");
}

void loop(){
    delay(20);
    while (Serial.available()) {//stipule que le bazzar recoit des donnée.
      //Assignation des variables de la commande Bluetooth
      input = Serial.readStringUntil('\n');
      volant = getValue(input,';',0).toInt();//donne une valeur entre 0 & 180
      throttle = getValue(input,';',1).toInt();//donne une valeur entre -100 & 100
      
      angleServo = map(volant, -90, 90, 45, 135);
      direction.write(angleServo);
      //Detection Marche AV/AR
      if(throttle <= 0){
        throttle = throttle * -1;
        motorSpeed = map(throttle, 0, 100, 0, 255);
        motorDir = 0;
      }
      else{
        motorSpeed = map(throttle, 0, 100, 0, 255);
        motorDir = 1;
      }

      if (motorDir == 1) {
        digitalWrite(controlPin1, HIGH);
        digitalWrite(controlPin2, LOW);
      } 
      else {
        digitalWrite(controlPin1, LOW);
        digitalWrite(controlPin2, HIGH);
      }
      analogWrite(enablePin, motorSpeed);
    } 
}

//Fonction de split récupération de paquet blutooth
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
