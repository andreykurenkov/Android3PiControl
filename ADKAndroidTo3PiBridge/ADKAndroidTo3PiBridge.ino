#include <Wire.h>
#include <Servo.h>
#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("Google, Inc.",
		     "DemoKit",
		     "DemoKit Arduino Board",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");


void setup()
{
	Serial.begin(115200);
	Serial.print("\r\nStart");
        Serial1.begin(115200);
        delay(100);
	acc.powerOn();
}

void loop()
{
	byte err;
	byte idle;
	static byte count = 0;
	byte msg[3];
	long touchcount;

	if (acc.isConnected()) {
		int len = acc.read(msg, sizeof(msg), 2);
		int i;
		byte b;
		uint16_t val;
		int x, y;
		char c0;
		if (len > 0) {
                    Serial1.write(msg, 2);
                }
	} 
	delay(10);
}

