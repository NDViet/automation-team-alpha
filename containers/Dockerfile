# ──────────────────────────────────────────────────────────────────────────────
# Test runner image for automation-team-alpha
#
# Inherits from ndviet/test-automation-java-common which:
#   • Bundles JDK 17, Maven, and a pre-seeded .m2 repository
#   • Ships /usr/local/bin/test-runner-entrypoint.sh that seeds ~/.m2 on first run
#
# All framework JARs and test sources are baked into the image at build time.
# No GitHub credentials are required at runtime.
#
# Build (credentials required once, at build time):
#   docker build \
#     --build-arg GITHUB_ACTOR=<user> \
#     --build-arg GITHUB_TOKEN=<pat> \
#     -t automation-team-alpha:latest \
#     -f containers/Dockerfile .
#
# Run (all tests — no credentials needed):
#   docker run --rm \
#     -v "$(pwd)/build:/workspace/build" \
#     automation-team-alpha:latest
#
# Run (specific browser):
#   docker run --rm \
#     -v "$(pwd)/build:/workspace/build" \
#     automation-team-alpha:latest \
#     ./gradlew test --no-daemon --offline -Dbrowser=firefox
# ──────────────────────────────────────────────────────────────────────────────
ARG BASE_IMAGE=ndviet/test-automation-java-common:latest
FROM ${BASE_IMAGE}

# ── Build-time credentials (never stored in the final image) ─────────────────
ARG GITHUB_ACTOR=""
ARG GITHUB_TOKEN=""

ENV WORKSPACE_DIR=/workspace
WORKDIR /workspace

# ── Stage 1: copy build files and download all dependencies ──────────────────
# This layer is only rebuilt when build.gradle / settings.gradle change,
# keeping source-only rebuilds fast.
COPY gradle/       gradle/
COPY gradlew       gradlew.bat   build.gradle   settings.gradle   gradle.properties   ./
RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon \
      -Pgpr.user="${GITHUB_ACTOR}" \
      -Pgpr.token="${GITHUB_TOKEN}" \
      --configuration testRuntimeClasspath \
    && echo "[test-runner] All dependencies downloaded and cached"

# ── Stage 2: copy sources and pre-compile test classes ───────────────────────
# Uses the JARs already in ~/.gradle/caches — no network access needed.
COPY src/ src/

RUN ./gradlew testClasses --no-daemon --offline \
    && echo "[test-runner] Test sources compiled"

# ── Default command ───────────────────────────────────────────────────────────
# --offline ensures Gradle never attempts network access at runtime.
# All JARs and compiled classes are already in the image.
CMD ["./gradlew", "test", "--no-daemon", "--offline", "--info"]
