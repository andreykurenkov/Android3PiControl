/*
 * PID3piLineFollower - demo code for the Pololu 3pi Robot
 * 
 * This code will follow a black line on a white background, using a
 * PID-based algorithm.
 *
 * http://www.pololu.com/docs/0J21
 * http://www.pololu.com
 * http://forum.pololu.com
 *
 */

// The following libraries will be needed by this demo
#include <Pololu3pi.h>
#include <PololuQTRSensors.h>
#include <OrangutanMotors.h>
#include <OrangutanAnalog.h>
#include <OrangutanLEDs.h>
#include <OrangutanLCD.h>
#include <OrangutanPushbuttons.h>
#include <OrangutanBuzzer.h>

Pololu3pi robot;


// This include file allows data to be stored in program space.  The
// ATmega168 has 16k of program space compared to 1k of RAM, so large
// pieces of static data should be stored in program space.
#include <avr/pgmspace.h>

// Introductory messages.  The "PROGMEM" identifier causes the data to
// go into program space.
const char welcome_line1[] PROGMEM = " Pololu";
const char welcome_line2[] PROGMEM = "3\xf7 Robot";
const char demo_name_line1[] PROGMEM = "Android";
const char demo_name_line2[] PROGMEM = "Control";

// A couple of simple tunes, stored in program space.
const char welcome[] PROGMEM = ">g32>>c32";
const char go[] PROGMEM = "L16 cdegreg4";

// Data for generating the characters used in load_custom_characters
// and display_readings.  By reading levels[] starting at various
// offsets, we can generate all of the 7 extra characters needed for a
// bargraph.  This is also stored in program space.
const char levels[] PROGMEM = {
  0b00000,
  0b00000,
  0b00000,
  0b00000,
  0b00000,
  0b00000,
  0b00000,
  0b11111,
  0b11111,
  0b11111,
  0b11111,
  0b11111,
  0b11111,
  0b11111
};

// This function loads custom characters into the LCD.  Up to 8
// characters can be loaded; we use them for 7 levels of a bar graph.
void load_custom_characters()
{
  OrangutanLCD::loadCustomCharacter(levels + 0, 0); // no offset, e.g. one bar
  OrangutanLCD::loadCustomCharacter(levels + 1, 1); // two bars
  OrangutanLCD::loadCustomCharacter(levels + 2, 2); // etc...
  OrangutanLCD::loadCustomCharacter(levels + 3, 3);
  OrangutanLCD::loadCustomCharacter(levels + 4, 4);
  OrangutanLCD::loadCustomCharacter(levels + 5, 5);
  OrangutanLCD::loadCustomCharacter(levels + 6, 6);
  OrangutanLCD::clear(); // the LCD must be cleared for the characters to take effect
}

// Initializes the 3pi, displays a welcome message.
void setup()
{
  unsigned int counter; // used as a simple timer

  // This must be called at the beginning of 3pi code, to set up the
  // sensors.  We use a value of 2000 for the timeout, which
  // corresponds to 2000*0.4 us = 0.8 ms on our 20 MHz processor.
  robot.init(2000);

  load_custom_characters(); // load the custom characters

  // Play welcome music and display a message
  OrangutanLCD::printFromProgramSpace(welcome_line1);
  OrangutanLCD::gotoXY(0, 1);
  OrangutanLCD::printFromProgramSpace(welcome_line2);
  OrangutanBuzzer::playFromProgramSpace(welcome);
  delay(1000);

  OrangutanLCD::clear();
  OrangutanLCD::printFromProgramSpace(demo_name_line1);
  OrangutanLCD::gotoXY(0, 1);
  OrangutanLCD::printFromProgramSpace(demo_name_line2);
  delay(1000);

  // Display battery voltage and wait for button press
  while (!OrangutanPushbuttons::isPressed(BUTTON_B))
  {
    int bat = OrangutanAnalog::readBatteryMillivolts();

    OrangutanLCD::clear();
    OrangutanLCD::print(bat);
    OrangutanLCD::print("mV");
    OrangutanLCD::gotoXY(0, 1);
    OrangutanLCD::print("Press B");

    delay(100);
  }

  // Always wait for the button to be released so that 3pi doesn't
  // start moving until your hand is away from it.
  OrangutanPushbuttons::waitForRelease(BUTTON_B);
  delay(1000);

  OrangutanLCD::clear();

  OrangutanLCD::print("Go!");	
}

// The main function.  This function is repeatedly called by
// the Arduino framework.
void loop()
{
    OrangutanMotors::setSpeeds(0,0);
}
