# Web (wasmJs)

Automated deployment via GitHub Actions.

## Files

- \Dockerfile\ - Multi-stage build (Gradle → Nginx)
- \
ginx.conf\ - SPA routing + WASM MIME types
- \.github/workflows/deploy-web.yml\ - CI/CD pipeline
- \	est-build.sh/bat\ - Local testing

## Quick Start

\\\ash
# Build locally
./test-build.sh

# Then open http://localhost:8080
\\\

## Deployment

Push to \main\ branch - GitHub Actions automatically:
1. Builds Docker image (multi-arch)
2. Pushes to ghcr.io
3. Deploys to VM via SSH

See root [DEPLOY.md](../DEPLOY.md) for configuration.
