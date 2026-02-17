Installation and Setup Guide (Windows & macOS)
This guide provides step-by-step instructions to set up the Calmify ecosystem, including the FastAPI backend, the Android mobile application, and the Raspberry Pi controller.

1. Backend API Setup (Python & FastAPI)
The backend acts as the secure gateway between the mobile app and the Firebase Realtime Database.


Prerequisites
Python 3.10+ installed on your system.

A valid Firebase project with Realtime Database and Authentication enabled.

Steps for Windows
Open PowerShell or Command Prompt.

Navigate to the /Backend folder: cd path\to\Calmify\Backend.

Create a virtual environment: python -m venv venv.

Activate the environment: .\venv\Scripts\activate.

Install dependencies: pip install fastapi uvicorn pyrebase4 pydantic[email].

Start the server: uvicorn main:app --host 0.0.0.0 --port 8000.

Steps for macOS
Open Terminal.

Navigate to the /Backend folder: cd path/to/Calmify/Backend.

Create a virtual environment: python3 -m venv venv.

Activate the environment: source venv/bin/activate.

Install dependencies: pip3 install fastapi uvicorn pyrebase4 pydantic[email].

Start the server: uvicorn main:app --host 0.0.0.0 --port 8000.

2. Mobile Application Setup (Android Studio)
The mobile app serves as the Cross-Device Interaction hub, processing biometric data and orchestrating interventions.

Steps (Windows & macOS)
Download and install Android Studio Hedgehog (or newer).

Open Android Studio and select "Open". Navigate to the /Mobile_App folder.

Ensure your build.gradle file is using Android Gradle Plugin (AGP) 9.0.0.

Sync the project with Gradle files.

Configuration: Update the baseUrl in AppGraph.kt to match your local machine's IP address (e.g., http://192.168.0.102:8000).

Run the application on a physical device or an emulator with Bluetooth support.

3. IoT Controller Setup (Raspberry Pi)
The pi_controller.py manages local environmental responses, such as lighting and audio.

Steps (Linux/Raspbian)
Ensure Python 3 is installed on your Raspberry Pi.

Install the required hardware libraries: pip3 install fastapi uvicorn gpiozero.

Install the audio player: sudo apt-get install mpg123.

Place calm.mp3 in the /home/pi/ directory.

Connect an LED to GPIO 18 (Pin 12) for PWM dimming.

Run the controller: uvicorn pi_controller:app --host 0.0.0.0 --port 8001.

4. Verification
Health Check: Navigate to http://localhost:8000/health in your browser. You should see {"ok": true}.


Database: Log in via the mobile app and verify that a new entry appears in your Firebase Realtime Database under the users/ node.

5. Physical Sensor Integration (Polar H10)
While the current evaluation uses a Digital Twin (VM) simulation for stability, the system is fully engineered for physical deployment with the Polar H10 chest strap.
+2

To Enable Physical Sensing:

Hardware: Ensure a Polar H10 sensor is powered on and within Bluetooth range of the Android device.


Software Toggle: In the mobile application, navigate to the runtime settings and set the sourceMode to SourceMode.BLE_ONLY.


Automatic Discovery: The AppGraph will trigger the BleHrSource to scan for the Heart Rate Service (UUID 0x180D) and automatically subscribe to the RR-interval characteristic (UUID 0x2A37).

Scientific Validation:
The system's detection algorithms are calibrated based on the 99.6% accuracy rating of the Polar H10 for RR-interval detection, as validated in the Gilgen-Ammann et al. (2019) study (PubMed: 31004219). Transitioning to this hardware will provide research-grade stress metrics with minimal configuration changes