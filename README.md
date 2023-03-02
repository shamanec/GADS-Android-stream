## GADS-Android-stream
Inspired by DeviceFarmer [minicap](https://github.com/DeviceFarmer/minicap) to create my own solution  

GADS-Android-stream provides a socket interface for streaming realtime(almost) screen capture data out of Android devices. It is being used by GADS for remote control of devices.  
Supported Android SDK >= 23.  
Not tested on simulators - might or might not work.  

Project uses MediaProjection and ImageReader APIs to get frames from the device screen and in theory should work for all devices.

**NB** I am not an Android developer. This project is a mix of official Android documentation, StackOverflow, googling and ChatGPT. I do not claim that it is good, it can definitely can be improved, but it serves its intended purpose.  

## Features
* Usable to smooth FPS depending on device  
* Usable but non-zero latency, might be a few frames behind
* Frames are only sent when something changes on the screen, so you might not see a frame on initial connect
* Easy socket interface - directly sends JPEG encoded image data that can be consumed on the other end

## Important
Screenshot dimensions are scaled down by factor of 2 so that they can be compressed faster to achieve higher FPS. This in turn reduces image quality, especially on lower res devices. If libjpeg-turbo is integrated with the project or someone can improve the Bitmap compression speed - contributions are welcome.
