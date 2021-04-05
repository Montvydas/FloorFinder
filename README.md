# Floor Finder Application

## Intro
I am a Univeristy of Edinburgh Electronics Students and I share my Masters project contents in here. An algorithm was implemented within an Android application, which would combine several sources of data to give the floor level at which the user is. The application requires GPS, a barometer sensor and an Internet connectivity. Full thesis paper is available on [Google Drive](https://drive.google.com/file/d/0B7GF7GPwL7E-VnFJQmtvaElGNVU/view?usp=sharing).

Using the App:
* The first time the application is opened you need to calibrate the ground floor level. Stand somewhere on the ground and hold the phone in a position. Press Calibrate!
* To update readings and your location press inside the red circle. Floor changes should be detected in real time.

APIs used:
* Forecast.io also known as Dark Sky 
* Google Elevation service
* Google Geodecoder class
* SparkFun Data (phant.io)


Features:
* Works in any building anywhere in the world. HOWEVER, due to recent modifications in Dark Sky API it isn't going to be as accuracte but the concept was proven to work.
* Shows received Google Elevation, current pressure and reference pressure.
* Allows to make automatic updates every defined amount of time.
* Allows to adjust the floor numbering convention depending on the country the user is in.

University of Edinburgh
School of Engineering
