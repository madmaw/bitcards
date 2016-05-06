# What's This?

Bitcards is an Android app for making and playing card games that would normally require a referee.

Bitcards was made as part of bitjam, a game jam where the objective was to make a game that uses only a single, 1-bit pixel.

# How does it work

The game rules are written in JavaScript and encoded in QR codes printed on each card and on the game sheet. Players
scan a card every time they pick up or draw. Each player has a phone that is networked and tells them when to play or draw.

## Modules

bitcards-android - Android App
bitcards-generator - Desktop app to generate cards and rules sheets
bitcards-common - Common code between Android App and Generator

## Building

Requires Maven 3.2.3 or below (Android plugin doesn't work with later versions)
Requires JDK (maybe 1.6 for certain things like Android signing, seems to be a bug in later versions)
Requires Android SDK (maybe 10, maybe any version, I don't really understand how it works)



