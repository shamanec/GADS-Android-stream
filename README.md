## GADS-Android-stream
Inspired by DeviceFarmer [minicap](https://github.com/DeviceFarmer/minicap) to create my own solution  

GADS-Android-stream provides a socket interface for streaming realtime(almost) screen capture data out of Android devices. It is being used by [GADS](https://github.com/shamanec/GADS) for remote control of devices.  
Supported Android SDK >= 23.  
Not tested on simulators - might or might not work.  

Project uses MediaProjection and ImageReader APIs to get frames from the device screen and in theory should work for all devices.

**NB** I am not an Android developer. This project is a mix of official Android documentation, StackOverflow, googling and ChatGPT. I do not claim that it is good, it can definitely can be improved, but it serves its intended purpose.  

## Features
* Usable to smooth FPS depending on device - probably reaches 30 FPS on my Huawei P20 Pro  
* Usable but non-zero latency, might be a few frames behind
* Frames are only sent when something changes on the screen, so you might not see a frame on initial connect
* Easy socket interface - directly sends JPEG encoded image data that can be consumed on the other end

## Important
Screenshot dimensions are scaled down by factor of 2 so that they can be compressed faster to achieve higher FPS. This in turn reduces image quality, especially on lower res devices. If libjpeg-turbo is integrated with the project or someone can improve the Bitmap compression speed - contributions are welcome.

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
