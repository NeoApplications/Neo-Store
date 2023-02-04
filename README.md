# Neo Store <img title="" src="src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" align="left" width="64">

### The modern feature-rich F-Droid client for everyone!

[![GitHub repo stars](https://img.shields.io/github/stars/NeoApplications/Neo-Store?style=flat)](https://github.com/NeoApplications/Neo-Store/stargazers)
[![GitHub License](https://img.shields.io/github/license/NeoApplications/Neo-Store)](https://github.com/NeoApplications/Neo-Store/blob/master/COPYING)
[![GitHub All Releases](https://img.shields.io/github/downloads/NeoApplications/Neo-Store/total.svg)](https://github.com/NeoApplications/Neo-Store/releases/)
[![GitHub release](https://img.shields.io/github/v/release/NeoApplications/Neo-Store?display_name=tag)](https://github.com/NeoApplications/Neo-Store/releases/latest)
[![Small translation badge](https://hosted.weblate.org/widgets/neo-store/-/svg-badge.svg)](https://hosted.weblate.org/engage/neo-store/?utm_source=widget)

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyDroid" width="38%" align="center">](https://android.izzysoft.de/repo/apk/com.machiav3lli.fdroid)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" width="38%" align="center">](https://f-droid.org/packages/com.machiav3lli.fdroid)
[<img src="https://upload.wikimedia.org/wikipedia/commons/8/82/Telegram_logo.svg" alt="Join Telegram Channel" width="13%" align="center">](https://t.me/neo_android_store)

[![Neo Applications Banner](neo_banner.png)](https://github.com/NeoApplications)

## :book: Features

* :fist: UX focused on empowering users
* :airplane: Fast repository syncing
* :fountain: Pre-setup repositories
* :mag: Easy exploration of new apps
* :dart: KISS-Minimalism

## :framed_picture: Screenshots

### :sun_with_face:, :last_quarter_moon_with_face: & :waxing_crescent_moon: Themes

| <img title="" src="metadata/en-US/images/phoneScreenshots/light.png" alt="" width="330" align="center"> | <img title="" src="metadata/en-US/images/phoneScreenshots/dark.png" alt="" width="330" align="center"> | <img title="" src="metadata/en-US/images/phoneScreenshots/amoled.png" alt="" width="330" align="center"> |
|:-------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------:|

### :toolbox: Full app details, latest apps & pre-setup repositories

| <img title="" src="metadata/en-US/images/phoneScreenshots/app.png" alt="" width="330" align="center"> | <img title="" src="metadata/en-US/images/phoneScreenshots/latest.png" alt="" width="330" align="center"> | <img title="" src="metadata/en-US/images/phoneScreenshots/repos.png" alt="" width="330" align="center"> |
|:-----------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------:| -------------------------------------------------------------------------------------------------------:|

## :speech_balloon: Community

You can join either our [Telegram](https://t.me/neo_android_store) or [Matrix](https://matrix.to/#/#neo-store:matrix.org) groups to make suggestions, ask questions, receive news, or just chat.

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

Run `./gradlew assembleRelease` to build a release package or run `./gradlew assembleDebug` if you want to build a debug APK.

All of these packages can be installed using the Android package manager.

## 👁️‍🗨️ Translations [<img align="right" src="https://hosted.weblate.org/widgets/neo-store/-/287x66-white.png" alt="Übersetzungsstatus" />](https://hosted.weblate.org/engage/neo-store/?utm_source=widget)

Help translate Neo Store on [Hosted Weblate](https://hosted.weblate.org/engage/neo-store/). \
[![Translation status](https://hosted.weblate.org/widgets/neo-store/-/multi-auto.svg)](https://hosted.weblate.org/engage/neo-store/?utm_source=widget)

You can always add other languages.

## Special thanks

To [Iamlooker](https://github.com/Iamlooker), who worked with me on maintaining the project, until he had to step away and fork out out of career-related reasons. Best luck!

## :scroll: Copylefted libre license

[GPLv3+](/COPYING). \
Direct adaptation/modification of [Foxy-Droid](https://github.com/kitsunyan/foxy-droid/) \
Copyright © 2022 [Antonios Hazim](https://github.com/machiav3lli) and [contributors](https://github.com/NeoApplications/Neo-Store/graphs/contributors).
