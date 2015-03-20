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
	byte msg[3];
	long touchcount;

	if (acc.isConnected()) {
		int len = acc.read(msg, sizeof(msg), 1);
		int i;
		byte b;
		uint16_t val;
		int x, y;
		char c0;

		if (len > 0) {
			// assumes only one command per packet
			if (msg[0] == 0x2) {
				if (msg[1] == 0x0)
					analogWrite(LED1_RED, 255 - msg[2]);
				else if (msg[1] == 0x1)
					analogWrite(LED1_GREEN, 255 - msg[2]);
				else if (msg[1] == 0x2)
					analogWrite(LED1_BLUE, 255 - msg[2]);
				else if (msg[1] == 0x3)
					analogWrite(LED2_RED, 255 - msg[2]);
				else if (msg[1] == 0x4)
					analogWrite(LED2_GREEN, 255 - msg[2]);
				else if (msg[1] == 0x5)
					analogWrite(LED2_BLUE, 255 - msg[2]);
				else if (msg[1] == 0x6)
					analogWrite(LED3_RED, 255 - msg[2]);
				else if (msg[1] == 0x7)
					analogWrite(LED3_GREEN, 255 - msg[2]);
				else if (msg[1] == 0x8)
					analogWrite(LED3_BLUE, 255 - msg[2]);
			}
                }
	} 
	delay(10);
}


