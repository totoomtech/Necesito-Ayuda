PROJECT NAME:
Necesito Ayuda

OUTPUT DIRECTORY:
C:\totoom\app\Necesito Ayuda

ROLE:
You are a Senior Android Engineer specialized in accessibility,
elderly usability and emergency applications.

This is NOT a demo app.
This is a REAL emergency assistance application.

You MUST strictly follow all mockups located in:
/Mockups

DO NOT redesign UI.
DO NOT improvise layouts.
Visual fidelity is mandatory.

--------------------------------------------------

PRIMARY GOAL

Create a FULL Android application (Kotlin + Android Studio)
ready to build APK.

Target users:
elderly people,
motor difficulties,
cognitive stress situations.

The app must work with MINIMUM interaction.

--------------------------------------------------

MAIN SCREENS

1) HOME SCREEN (Mockup 01)

Layout EXACTLY as image.

Elements:

- BIG button "AYUDA RÁPIDA"
- 3 LARGE CONTACT CARDS (photo + name)
- Cards contain NO icons
- Entire card is clickable
- Bottom buttons:
    - 112
    - MÉDICO
- Location status indicator
- Settings accessible ONLY via LONG PRESS (5 seconds)

--------------------------------------------------

USER ACTION FLOW

Touch on:
- AYUDA RÁPIDA
- Contact Card
- MÉDICO
- 112

→ opens COUNTDOWN OVERLAY

--------------------------------------------------

2) COUNTDOWN OVERLAY (Mockup 02)

Full screen overlay.

Features:

- Big countdown number (3..2..1)
- CANCEL button VERY LARGE
- Vibrate device
- Play confirmation sound
- TTS voice announcement:

Spanish:
"Enviando ayuda a %CONTACT%"

Catalan:
"Enviant ajuda a %CONTACT%"

English:
"Sending help to %CONTACT%"

Countdown times:

Contacts / Médico / Ayuda Rápida → 3 seconds
112 → 5 seconds

--------------------------------------------------

AFTER COUNTDOWN (NO CANCEL)

Execute automatically:

1) Obtain GPS location
2) Send SMS automatically:

Message:
"Necesito ayuda. Mi ubicación:
https://maps.google.com/?q=LAT,LON"

3) Open WhatsApp chat prefilled message
(no automatic send)

4) Start phone call

5) Call Cascade:
Contact 1 → wait
if not answered → Contact 2
if not answered → Contact 3

--------------------------------------------------

3) SETTINGS SCREEN (Mockup 03)

Accessible only by long press.

Configurable:

- Language:
    Spanish
    Catalan
    English

- 3 Contacts:
    name
    phone
    photo (camera or gallery)

- Doctor number

- Toggle:
    Voice announcements ON/OFF

--------------------------------------------------

4) PERMISSIONS SCREEN (Mockup 04)

Guided permission activation:

Required permissions:
- Location
- SMS
- Phone
- Notifications
- Ignore battery optimization

Show checklist status.

--------------------------------------------------

TECH REQUIREMENTS

Language: Kotlin
Architecture: MVVM
Min SDK: 26+
Offline first
NO cloud services

Use Android native:
- TextToSpeech
- SMS Manager
- LocationManager/FusedLocation
- Intent WhatsApp
- Foreground Service

--------------------------------------------------

ACCESSIBILITY REQUIREMENTS

- VERY LARGE touch targets
- High contrast
- Minimal text
- Works without reading instructions
- Voice + vibration feedback

--------------------------------------------------

DELIVERABLES

Generate:

- Complete Android Studio project
- Gradle working
- Buildable APK
- README.md explaining build
- Code comments

Place project inside OUTPUT DIRECTORY.

--------------------------------------------------

ACCEPTANCE CONDITION

App must be usable by an elderly user
without explanation.

If interaction requires thinking,
REDESIGN internally but KEEP visuals.