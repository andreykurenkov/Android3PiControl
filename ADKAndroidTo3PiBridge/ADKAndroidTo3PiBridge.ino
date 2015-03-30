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
	acc.powerOn();
}

void loop()
{
	byte err;
	byte idle;
	static byte count = 0;
	byte singleByte[1];

	if (acc.isConnected()) {
		int len = acc.read(singleByte, 1, 1);

		if (len > 0) {
			acc.write(singleByte,1);
                }
	} 
}


