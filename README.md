# CSC750P1
Author: Rajat Shah (rshah6)
## Overview
A healthcare application can store private healthcare data of user, such as name, address, and
medical history. In general, a user would want to hold such Protected Health Information (PHI),
as this information is termed by the Health Insurance Portability and Accountability Act (HIPAA),
private. Therefore, ordinarily, a mobile phone should not share that information.

However, during a medical emergency, a user may wish to share that information with others.
For example, an unconscious user may wish to let it known to bystanders whether he or she
suffers from a condition such as diabetes or epilepsy, and what his or her blood type, blood
pressure, and heart rate are. However, the user cannot be giving instructions when the user is
unconscious. Hence, it is up to the user’s mobile phone to figure out if the user is unconscious
Your task in this assignment is to build an adaptive healthcare application that addresses this
scenario, and reveal user’s private health data when necessary.

## Installation Steps & Requirements
The project was developed in latest version of Android Studio on Mac.

Minimum requirements:
- JDK version: jdk1.8.0_91.jdk
- Gradle version: 2.14.1

Dependencies and External Libraries:
- Google Play Services: 9.0.2
- Android Kitat: 4.4

## Implemented Approach
Following Conditions should be simultaneously met to generate a notification with PHI:

- User is **falling**: The Accelerometer Sensor of Android provides acceleration of the phone in each direction in 3D space. When the verticle (Y-axis acceleration) of the user be comes in the range of 9-10m/s^2, it is considered as falling. Ideally it should be 9.8m/s^2, but generating test case for this is non-trivial.
- The time needed to drive to the **nearest hospital** is greater than 5 minutes: Using the Google Maps API and Location service in Google Play Services, the users current location is determined. The distance to Hospital is then caculated and if it less than a threshold of 6000meters (or 6KM), it is considered to be reachable in 5 minutes.
- The **barometric pressure** reading is less than 29.5(==0.9989847 Bar) inches of mercury: Using the Pressure Sensor API in Android, the pressure is caculated in Bar unit. At normal circumstances it is close to 1Bar (==29.53 inches of mercury). In order to be practically tested, the upper threshold for determining  
- The **light** is less than 400 lx: The Light Sensor API in Android provides the light exposure value of the device.

## Sample Screenshots
<p align="center">
  <img src="https://media.giphy.com/media/26ufcVxET21XK5HUI/giphy.gif" style="margin-right:30px" />
  <img src="https://media.giphy.com/media/26ufdf0kAOt3Gniko/giphy.gif" />
</p>
