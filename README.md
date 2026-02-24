# Necesito Ayuda - Android Emergency App

Real emergency assistance application for elderly people.

## Build Instructions
1. Open this folder in **Android Studio (Hedgehog or later)**.
2. Wait for Gradle sync to complete.
3. Build APK via `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. Install on device.

## Permission Setup
Upon first launch, the app will guide you to the Permissions screen. 
**CRITICAL**: Ensure all items in the checklist are "Activado".
- **Battery Optimization**: Use the button to find "Necesito Ayuda" and set to "Don't optimize" or "Unrestricted".

## Quick Start
- **Emergency**: Press "AYUDA RÁPIDA" or any Contact Card.
- **Cancel**: Press "CANCELAR" during the 3s countdown.
- **Settings**: Perform a **5-second long press** on the cog icon at the bottom right.

## Technical Notes
- Uses `FusedLocationProvider` for high-accuracy GPS.
- Sequential calling attempts to call Contact 1, then 2, then 3 if no answer (timeout based).
- SMS is sent automatically to all registered contacts.
