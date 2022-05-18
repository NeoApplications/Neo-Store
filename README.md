# Neo Store <img title="" src="src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" align="left" width="64">

### A quick material F-Droid client

[![Github repo stars](https://img.shields.io/github/stars/NeoApplications/Neo-Store?style=flat)](https://github.com/NeoApplications/Neo-Store/stargazers)
[![Github License](https://img.shields.io/github/license/NeoApplications/Neo-Store)](https://github.com/NeoApplications/Neo-Store/blob/master/COPYING)
[![Github All Releases](https://img.shields.io/github/downloads/NeoApplications/Neo-Store/total.svg)](https://github.com/NeoApplications/Neo-Store/releases/)
[![Github release](https://img.shields.io/github/v/release/NeoApplications/Neo-Store?display_name=tag)](https://github.com/NeoApplications/Neo-Store/releases/latest)
[![Small translation badge](https://hosted.weblate.org/widgets/droidify/-/localization/svg-badge.svg)](https://hosted.weblate.org/engage/droidify/?utm_source=widget)

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyDroid" width="38%" align="center">](https://android.izzysoft.de/repo/apk/com.looker.droidify)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" width="38%" align="center">](https://f-droid.org/packages/com.looker.droidify)
[<img src="https://upload.wikimedia.org/wikipedia/commons/8/82/Telegram_logo.svg" alt="Join Telegram Channel" width="13%" align="center">](https://t.me/neo_android_store)

![](neo_banner.png)

## :book: Features

* :art: Material F-Droid style
* :dart: No cards or inappropriate animations
* :airplane: Fast repository syncing
* :wrench: Standard Android components and minimal dependencies

## :framed_picture: Screenshots

### :sun_with_face: Light theme

| <img src="metadata/en-US/images/phoneScreenshots/home-light.png" width="500" align="center"/> | <img src="metadata/en-US/images/phoneScreenshots/app-light.png" width="500" align="center"/> |
|:---------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------:|

### :last_quarter_moon_with_face: Dark theme

| <img src="metadata/en-US/images/phoneScreenshots/home-dark.png" width="500" align="center"/> | <img src="metadata/en-US/images/phoneScreenshots/app-dark.png" width="500" align="center"/> |
|:--------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------:|

### :waxing_crescent_moon: Amoled theme

| <img src="metadata/en-US/images/phoneScreenshots/home-amoled.png" width="500" align="center"/> | <img src="metadata/en-US/images/phoneScreenshots/app-amoled.png" width="500" align="center"/> |
|:----------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------:|

## :building_construction: Building from source

Specify your Android SDK path either using the `ANDROID_HOME` environment variable, \
or by filling out the `sdk.dir` property in `local.properties`.

### :pen: Signing

Can be done automatically using `keystore.properties` as follows:

```properties
store.file=/path/to/keystore
store.password=key-store-password
key.alias=key-alias
key.password=key-password
```

### :hammer: Building

Run `./gradlew assembleRelease` to build release package or run `./gradlew assembleDebug` if you want to build debug apk.

All of these packages can be installed using the Android package manager

## :eye_speech_bubble: Translations [<img align="right" src="https://hosted.weblate.org/widgets/droidify/-/287x66-white.png" alt="Translation status" />](https://hosted.weblate.org/engage/droidify/?utm_source=widget)

Help us translate Neo Store on [Hosted Weblate](https://hosted.weblate.org/engage/droidify/). \
[![Translation status](https://hosted.weblate.org/widgets/droidify/-/multi-auto.svg)](https://hosted.weblate.org/engage/droidify/?utm_source=widget)

### You can always add other languages

## :scroll: License

Licensed under GPLv3+. \
Direct Adaptation/Modification of [Foxy-Droid](https://github.com/kitsunyan/foxy-droid/) \
Copyright © 2020–2022 [Iamlooker](https://github.com/Iamlooker) and [contributors](https://github.com/NeoApplications/Neo-Store/graphs/contributors).
