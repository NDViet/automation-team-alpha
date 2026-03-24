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
#     ./gradlew test --no-daemon -Dbrowser=firefox
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

# ── Freeze SNAPSHOT cache TTL so Gradle never goes to the network at runtime ──
# Gradle refuses to serve SNAPSHOT (changing) modules from its file cache in
# --offline mode. Instead of --offline we extend the cache TTL to ~100 years
# via an init script that is automatically applied by Gradle on every run.
# The init script lives in the Gradle user home inside the image; it is never
# applied outside the container.
RUN mkdir -p "$(gradle --no-daemon -q properties 2>/dev/null | grep '^gradle.user.home:' | awk '{print $2}' || echo "$HOME/.gradle")/init.d" \
    && GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}" \
    && mkdir -p "${GRADLE_HOME}/init.d" \
    && cat > "${GRADLE_HOME}/init.d/freeze-snapshot-cache.gradle" <<'EOF'
// Prevent Gradle from re-checking SNAPSHOT (changing) dependencies at runtime.
// All JARs were baked in at image build time; no network access is needed.
allprojects {
    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor 999999, 'hours'
            cacheDynamicVersionsFor 999999, 'hours'
        }
    }
}
EOF

# ── Stage 2: copy sources and pre-compile test classes ───────────────────────
# Credentials are still available here because GITHUB_ACTOR/TOKEN are build
# ARGs; they are NOT written to any layer env, so the final image is clean.
COPY src/ src/

RUN ./gradlew testClasses --no-daemon \
      -Pgpr.user="${GITHUB_ACTOR}" \
      -Pgpr.token="${GITHUB_TOKEN}" \
    && echo "[test-runner] Test sources compiled"

# ── Default command ───────────────────────────────────────────────────────────
# The init script above prevents Gradle from hitting the network for SNAPSHOTs.
# No --offline flag needed; Gradle uses the baked-in cache transparently.
CMD ["./gradlew", "test", "--no-daemon", "--info"]
