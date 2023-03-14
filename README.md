## GADS-Android-stream
Inspired by DeviceFarmer [minicap](https://github.com/DeviceFarmer/minicap) to create my own solution  

GADS-Android-stream provides a socket interface for streaming realtime(almost) screen capture data out of Android devices. It is being used by [GADS](https://github.com/shamanec/GADS) for remote control of devices.  
Supported Android SDK >= 23.  
Should be working on simulators as well - tested on Pixel 4 API 29

Project uses MediaProjection and ImageReader APIs to get frames from the device screen and in theory should work for all devices.

**NB** I am not an Android developer. This project is a mix of official Android documentation, StackOverflow, googling and ChatGPT. I do not claim that it is good, it probably can be improved, but it serves its intended purpose.  

## Features
* Reasonable quality - ImageReader dimensions are scaled down by factor of 2 and then the Bitmaps are compressed to JPEG which reduces quality even further, despite that the quality is good on higher resolution devices and acceptable on lower resolution devices  
* Usable to smooth fps - seems to be quite acceptable even on my Sony Xperia XA from 2016
* Non-zero latency, might be a few frames behind, but almost non-existent when running locally
* Frames are only sent when something changes on the screen, so you might not see a frame on initial connect
* Easy socket interface - directly sends JPEG encoded image data that can be consumed on the other end

## Starting the JPEG stream
* Get the apk file from the releases or build the apk yourself
* Execute `adb install -r appname.apk`  
* Give recording permissions with `adb shell appops set com.shamanec.stream PROJECT_MEDIA allow`  
* Start the stream app with `adb shell am start -n com.shamanec.stream/com.shamanec.stream.ScreenCaptureActivity`  
* To hide the transparent activity tap Home on the device or run `adb shell input keyevent KEYCODE_HOME`  
* Forward the stream socket to the PC with `adb forward tcp:YOUR_PORT tcp:1991` or directly connect to it using the IP address of the device itself  

## Experimental instrumentation
There is experimental control for devices through Android Instrumentation. It is implemented with an endlessly running UI test that starts a Websocket connection. On structured JSON message received by the Websocket, the Instrumentation can tap, swipe and type text on the device really fast.  
* Start the instrumentation after installing GADS-Stream with `adb shell am instrument -w -e debug false com.shamanec.stream.test`  
* Forward the Websocket connection to the host computer with `adb forward tcp:YOUR_PORT tcp:1992`  
* Connect to the Websocket through Postman for example on `ws://localhost:YOUR_PORT`  
* Send a JSON as message:  
  * {"action":"typeText", "text": "text_to_type"}  
  * {"action":"swipe", "startX": int, "startY": int, "endX":int, "endY": int}  
  * {"action":"tap", "x": int, "y": int}  
  * {"action":"executeShellCommand", "shellCommand":"shell command string"}
