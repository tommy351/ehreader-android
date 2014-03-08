# E-Hentai Reader for Android

The best E-Hentai Reader for Android ever!

## Features

- ExHentai login
- Gallery download
- Image search

## Download

[![](https://raw.github.com/tommy351/ehreader-android/master/images/qrcode.png)][Download]

## Screenshots

<a href="https://raw.github.com/tommy351/ehreader-android/master/images/screenshot-main.png"><img src="https://raw.github.com/tommy351/ehreader-android/master/images/screenshot-main.png" width="360" height="640"></a>
<a href="https://raw.github.com/tommy351/ehreader-android/master/images/screenshot-gallery.png"><img src="https://raw.github.com/tommy351/ehreader-android/master/images/screenshot-gallery.png" width="360" height="640"></a>
<a href="https://raw.github.com/tommy351/ehreader-android/master/images/screenshot-photo.png"><img src="https://raw.github.com/tommy351/ehreader-android/master/images/screenshot-photo.png" width="360" height="640"></a>

## Development

### Requirements

- [Android SDK](http://developer.android.com/sdk/index.html) (API 19+)
- [Android NDK](http://developer.android.com/tools/sdk/ndk/index.html)
- [Android Studio](http://developer.android.com/sdk/installing/studio.html)

### Installation

Run the following commands and open the project in Android Studio.

``` bash
$ https://github.com/tommy351/ehreader-android.git
$ cd ehreader-android
$ git submodule update --init
$ cd android-stackblur/StackBlur
$ ndk-build
```

### Generating DAO

This app uses [GreenDAO](http://greendao-orm.com/) as ORM. Before you get started or after the DAO is updated, you have to run the gradle task `DaoGenerator:run`.

## License

Apache License 2.0

```
Copyright 2014 Tommy Chen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[Download]: https://github.com/tommy351/ehreader-android/releases/download/0.4.0/ehreader-release.apk
