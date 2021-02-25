#include<LiquidCrystal.h>//libreria del componente LCD
LiquidCrystal lcd(12 , 11 , 5 , 4 , 3 , 2);//pines que usa la pantalla lcd
int redLed = 7;//led rojo que se usa para cuando detecte gas..
int greenLed = 6;//led verde que se usa para ¡cuando no detecte gas..
int Gas = 9;//pin del sensor de gas ..
String voice;//la orden que recibira la aplicacion de android..
void setup() {
  pinMode(13, OUTPUT);
  Serial.begin(9600);//puerto respectivamente..
  pinMode(Gas , INPUT);
}
void loop() {
  while (Serial.available())//condicion mientras haya conexion..
  {
    delay(10);
    char c = Serial.read();//lee el texto que se envia..
    voice += c;
  }
  if (voice.length() > 0)//largo de la palabra ingresada 
  {
    Serial.println(voice);
    if (voice == "on")//la palabra ingresada es on..
    {
      pinMode(Gas , HIGH);//enciende el gas
        lcd.setCursor(0, 0);
        lcd.print(" Gas Activado ");//imprime en la pantalla LCD
        digitalWrite(7 , HIGH);//el led rojo se enciende
        digitalWrite(6, LOW);//mientras que el verde queda contsante..
      
    }
    if (voice == "off")//apaga el gas respectivamente si la palabra ingresada es off
    {
      lcd.setCursor(0, 0);
      lcd.print(" Gas Desactivado ");//imprime en la pantalla LCD
      digitalWrite(6, HIGH);//enciende el led verde..
      digitalWrite(7 , LOW);//apaga el led rojo..
    }
    voice = "";//queda en escucha ante cualquier actividad relacionada anteriormente..
  }
}
