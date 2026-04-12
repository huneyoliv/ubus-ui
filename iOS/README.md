# iOS Build

Located in: Xcode/

## Build Requirements
- macOS with Xcode 14+
- iOS 14+ SDK

## Build

\\\ash
cd Xcode
xcodebuild build \
  -scheme Ubus \
  -configuration Release \
  -derivedDataPath build
\\\

## Deployment
Package and submit to App Store via Xcode/TestFlight.

See [web/DEPLOY.md](../web/DEPLOY.md) for more details.
