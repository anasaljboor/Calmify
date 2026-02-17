import re
import time
from typing import Any

from fastapi import FastAPI, HTTPException, Header
from pydantic import BaseModel, EmailStr
import pyrebase


# -----------------------------
# Firebase config (ENV or inline)
# -----------------------------
firebase_config = {
    "apiKey": "AIzaSyCeCk5S_h057qYjPwG26U0q7yIv7jpSA5c",
    "authDomain": "calmify-c8cac.firebaseapp.com",
    "projectId": "calmify-c8cac",
    "storageBucket": "calmify-c8cac.firebasestorage.app",
    "messagingSenderId": "810376594591",
    "appId": "1:810376594591:web:94b8c67634695527f00eaa",
    "databaseURL": "https://calmify-c8cac-default-rtdb.europe-west1.firebasedatabase.app",
}

missing = [k for k, v in firebase_config.items() if k in ("apiKey", "projectId") and not v]
if missing:
    raise RuntimeError(f"Missing Firebase config env vars: {missing}")

firebase = pyrebase.initialize_app(firebase_config)
pb_auth = firebase.auth()
pb_db = firebase.database() if firebase_config.get("databaseURL") else None

app = FastAPI(title="Calmify VM API (Pyrebase)", version="1.3.0")


# -----------------------------
# Models
# -----------------------------
class AuthRequest(BaseModel):
    email: EmailStr
    password: str
    username: str | None = None  # used for signup


class AuthResponse(BaseModel):
    userId: str
    accessToken: str


class UserSettings(BaseModel):
    stressThreshold: int | None = None
    displayName: str | None = None


class ProfileUpdate(BaseModel):
    username: str | None = None
    displayName: str | None = None


# Simulation models (match Kotlin)
class SimSample(BaseModel):
    tsMs: int
    hrBpm: int | None = None
    rmssd: float | None = None
    stress0to100: int | None = None


class SimulationRequest(BaseModel):
    startedAtMs: int
    endedAtMs: int
    samples: list[SimSample] = []
    summary: dict[str, float] = {}


# -----------------------------
# Helpers
# -----------------------------
def require_token(authorization: str | None):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing Bearer token")
    return authorization.replace("Bearer ", "").strip()


def normalize_username(u: str) -> str:
    u = u.strip().lower()
    u = re.sub(r"[^a-z0-9._-]", "", u)
    return u


def _pretty_pyrebase_error(e: Exception) -> str:
    s = str(e)
    for k in ["EMAIL_EXISTS", "EMAIL_NOT_FOUND", "INVALID_PASSWORD", "USER_DISABLED", "WEAK_PASSWORD"]:
        if k in s:
            return k
    return "Auth/DB error"


def must_have_db():
    if pb_db is None:
        raise HTTPException(status_code=400, detail="Realtime Database not configured (missing databaseURL)")


def get_uid_from_token(token: str) -> str:
    info = pb_auth.get_account_info(token)
    users = info.get("users", [])
    if not users:
        raise HTTPException(status_code=401, detail="Invalid token")
    return users[0]["localId"]


def now_ms() -> int:
    return int(time.time() * 1000)


# -----------------------------
# Chart aggregation (server-side)
# -----------------------------
def _bucket_daily_by_hour(samples: list[dict]) -> list[int]:
    """
    Returns 24 values (0..23) = average stress for that hour.
    Uses server local time. Missing hour -> 0.
    """
    buckets: list[list[int]] = [[] for _ in range(24)]

    for s in samples:
        ts = s.get("tsMs")
        v = s.get("stress0to100")
        if ts is None or v is None:
            continue
        try:
            hour = time.localtime(float(ts) / 1000.0).tm_hour
            hour = max(0, min(23, int(hour)))
            buckets[hour].append(int(v))
        except Exception:
            continue

    out: list[int] = []
    for arr in buckets:
        out.append(int(sum(arr) / len(arr)) if arr else 0)
    return out


def _bucket_weekly_by_day(samples: list[dict]) -> list[int]:
    """
    Returns 7 values (oldest -> newest) = average stress per day
    for the last 7 days relative to NOW (server local time).
    Missing day -> 0.
    """
    buckets: list[list[int]] = [[] for _ in range(7)]
    now_day = int(time.time() // 86400)  # days since epoch

    for s in samples:
        ts = s.get("tsMs")
        v = s.get("stress0to100")
        if ts is None or v is None:
            continue
        try:
            day = int((float(ts) / 1000.0) // 86400)
            diff = now_day - day  # 0 today ... 6 six days ago
            if 0 <= diff <= 6:
                idx = 6 - diff  # oldest -> newest
                buckets[idx].append(int(v))
        except Exception:
            continue

    out: list[int] = []
    for arr in buckets:
        out.append(int(sum(arr) / len(arr)) if arr else 0)
    return out


# -----------------------------
# Health
# -----------------------------
@app.get("/health")
def health():
    return {"ok": True}


# -----------------------------
# Auth endpoints
# -----------------------------
@app.post("/signup", response_model=AuthResponse)
def signup(req: AuthRequest):
    must_have_db()

    if not req.username or not req.username.strip():
        raise HTTPException(status_code=400, detail="USERNAME_REQUIRED")

    raw_username = req.username.strip()
    uname_norm = normalize_username(raw_username)

    if len(uname_norm) < 3:
        raise HTTPException(status_code=400, detail="INVALID_USERNAME")

    # check username availability BEFORE creating auth user
    try:
        existing = pb_db.child("usernames").child(uname_norm).get().val()
        if existing:
            raise HTTPException(status_code=409, detail="USERNAME_TAKEN")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))

    try:
        user = pb_auth.create_user_with_email_and_password(req.email, req.password)
        uid = user["localId"]
        token = user["idToken"]

        pb_db.child("users").child(uid).update(
            {
                "username": raw_username,
                "usernameNorm": uname_norm,
                "email": str(req.email),
                "createdAtMs": now_ms(),
            },
            token,
        )

        pb_db.child("usernames").child(uname_norm).set(uid, token)

        return AuthResponse(userId=uid, accessToken=token)

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


@app.post("/login", response_model=AuthResponse)
def login(req: AuthRequest):
    try:
        user = pb_auth.sign_in_with_email_and_password(req.email, req.password)
        return AuthResponse(userId=user["localId"], accessToken=user["idToken"])
    except Exception as e:
        raise HTTPException(status_code=401, detail=_pretty_pyrebase_error(e))


# -----------------------------
# /me -> returns username from RTDB
# -----------------------------
@app.get("/me")
def me(authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)

    try:
        info = pb_auth.get_account_info(token)
        users = info.get("users", [])
        if not users:
            raise HTTPException(status_code=401, detail="Invalid token")

        u = users[0]
        uid = u.get("localId")
        email = u.get("email")

        profile = pb_db.child("users").child(uid).get(token).val() or {}
        return {
            "userId": uid,
            "email": email,
            "username": profile.get("username"),
            "displayName": profile.get("displayName"),
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=401, detail=_pretty_pyrebase_error(e))


# -----------------------------
# Profile endpoints
# -----------------------------
@app.get("/profile")
def get_profile(authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)
    try:
        uid = get_uid_from_token(token)
        profile = pb_db.child("users").child(uid).get(token).val() or {}
        return {"userId": uid, "profile": profile}
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


@app.put("/profile")
def put_profile(payload: ProfileUpdate, authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)
        updates: dict[str, Any] = payload.model_dump(exclude_none=True)

        if "username" in updates:
            raw_username = updates["username"].strip()
            uname_norm = normalize_username(raw_username)
            if len(uname_norm) < 3:
                raise HTTPException(status_code=400, detail="INVALID_USERNAME")

            existing = pb_db.child("usernames").child(uname_norm).get(token).val()
            if existing and existing != uid:
                raise HTTPException(status_code=409, detail="USERNAME_TAKEN")

            old = pb_db.child("users").child(uid).child("usernameNorm").get(token).val()
            if old and old != uname_norm:
                pb_db.child("usernames").child(old).remove(token)

            pb_db.child("usernames").child(uname_norm).set(uid, token)

            updates["username"] = raw_username
            updates["usernameNorm"] = uname_norm

        pb_db.child("users").child(uid).update(updates, token)
        return {"ok": True}

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


# -----------------------------
# Settings endpoints
# /users/{uid}/settings
# -----------------------------
@app.put("/settings")
def put_settings(payload: UserSettings, authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)
        pb_db.child("users").child(uid).child("settings").update(
            payload.model_dump(exclude_none=True),
            token,
        )
        return {"ok": True}
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


@app.get("/settings")
def get_settings(authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)
        data = pb_db.child("users").child(uid).child("settings").get(token).val()
        return {"userId": uid, "settings": data}
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


# -----------------------------
# Simulation endpoints
# Store:
#   /users/{uid}/simulations/{simId} -> full payload
#   /users/{uid}/latestSimulation   -> small summary
# -----------------------------
@app.post("/simulation")
def post_simulation(payload: SimulationRequest, authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)

        sim_id = str(now_ms())  # timestamp id

        sim_obj = {
            "id": sim_id,
            "startedAtMs": payload.startedAtMs,
            "endedAtMs": payload.endedAtMs,
            "summary": payload.summary or {},
            "samples": [s.model_dump() for s in payload.samples] if payload.samples else [],
        }

        pb_db.child("users").child(uid).child("simulations").child(sim_id).set(sim_obj, token)

        latest_obj = {
            "id": sim_id,
            "startedAtMs": payload.startedAtMs,
            "endedAtMs": payload.endedAtMs,
            "summary": payload.summary or {},
        }
        pb_db.child("users").child(uid).child("latestSimulation").set(latest_obj, token)

        return {"ok": True, "id": sim_id}

    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


@app.get("/simulation/latest")
def latest_simulation(authorization: str | None = Header(default=None)):
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)
        latest = pb_db.child("users").child(uid).child("latestSimulation").get(token).val()
        return {"userId": uid, "latest": latest}
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


# -----------------------------
# NEW: Chart endpoints
# -----------------------------
@app.get("/simulation/latest/charts")
def latest_simulation_charts(authorization: str | None = Header(default=None)):
    """
    Returns:
      dailyStressByHour: 24 ints
      weeklyStressByDay: 7 ints (oldest -> newest)
    Computed from latest simulation samples.
    """
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)

        latest = pb_db.child("users").child(uid).child("latestSimulation").get(token).val()
        if not latest or not latest.get("id"):
            return {
                "userId": uid,
                "simId": None,
                "dailyStressByHour": [0] * 24,
                "weeklyStressByDay": [0] * 7,
            }

        sim_id = str(latest["id"])
        sim = pb_db.child("users").child(uid).child("simulations").child(sim_id).get(token).val() or {}
        samples = sim.get("samples") or []

        daily = _bucket_daily_by_hour(samples)
        weekly = _bucket_weekly_by_day(samples)

        return {
            "userId": uid,
            "simId": sim_id,
            "dailyStressByHour": daily,
            "weeklyStressByDay": weekly,
        }

    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))


@app.get("/simulation/{sim_id}/charts")
def simulation_charts(sim_id: str, authorization: str | None = Header(default=None)):
    """
    Same chart computation but for a specific sim id.
    """
    must_have_db()
    token = require_token(authorization)

    try:
        uid = get_uid_from_token(token)

        sim = pb_db.child("users").child(uid).child("simulations").child(sim_id).get(token).val()
        if not sim:
            raise HTTPException(status_code=404, detail="SIM_NOT_FOUND")

        samples = sim.get("samples") or []

        daily = _bucket_daily_by_hour(samples)
        weekly = _bucket_weekly_by_day(samples)

        return {
            "userId": uid,
            "simId": sim_id,
            "dailyStressByHour": daily,
            "weeklyStressByDay": weekly,
        }

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=_pretty_pyrebase_error(e))