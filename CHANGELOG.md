# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
## [0.4.2] - 2020-04-17
### Added
- `Clap` sound event for `human-interaction` service

### Changed
- Preprocessing routine for the audio input data

### Removed
- `Gunshot_explosion` sound event for `emergency` service

## [0.4.1] - 2020-04-13
### Changed
- The package file name
  - 'sense-sdk-release.aar' -> 'sense-sdk-<version>.aar'

### Fixed
- The device identifier

## [0.4.0] - 2020-03-04
### Changed
- Supported model
  - 'event' -> 'emergency' and 'human-interaction'

## [0.3.0] - 2020-01-17
### Added
- More sound event detection by the event model (`14` new classes, `34` total classes)
  - 'Bicycle_belli', 'Birds', 'Burping', 'Cat_meow', 'Clap', 'Crowd_applause', 'Crowd_scream'
  - 'Explosion', 'Finger_snap', 'Keyboard_mouse', 'Mosquito', 'Sigh', 'Whisper', 'Wind_noise'
- `config.properties` configuration file
- Prebuilt library headers
- Custom Tensorflow as built-in

### Changed
- Melspectrogram routine to native C++ from JNI
- Melspectrogram parameter as configurable
- Assets hierarchy
- The model file decryption routine to C++ from JAVA
- Authorization routine as JNI from JAVA

### Fixed
- The HTTPS request issue in JNI
- Wrong SDK name

### Removed
- Tensorflow package dependency

## [0.2.0] - 2019-11-13
### Added
- New event model which is improved performance about 4% in the internal test set.
- pause() function for audio stream prediction.
- resume() function for audio stream prediction.

### Changed
- Added REST call for SDK authentication parameters - 'version' and 'sdk_type'

## [0.1.0] - 2019-09-06
### Added
- First release
