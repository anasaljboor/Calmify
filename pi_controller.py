from fastapi import FastAPI
from pydantic import BaseModel
import subprocess
from gpiozero import PWMLED

app = FastAPI()

# ---------- AUDIO ----------
AUDIO_FILE = "/home/pi/calm.mp3"

def play_audio():
    subprocess.Popen(["mpg123", AUDIO_FILE])

def stop_audio():
    subprocess.Popen(["pkill", "mpg123"])

# ---------- LIGHT ----------
led = PWMLED(18)  # GPIO18 (pin 12)

# ---------- MODELS ----------
class VolumeReq(BaseModel):
    value: int  # 0-100

class LightReq(BaseModel):
    brightness: float  # 0.0 - 1.0

# ---------- ENDPOINTS ----------
@app.post("/audio/play")
def audio_play():
    play_audio()
    return {"ok": True}

@app.post("/audio/stop")
def audio_stop():
    stop_audio()
    return {"ok": True}

@app.post("/lights")
def set_light(req: LightReq):
    led.value = max(0.0, min(1.0, req.brightness))
    return {"brightness": led.value}
