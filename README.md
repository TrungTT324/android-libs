# android-libs

Shared Android library modules published to GitHub Packages so they can be
consumed as regular Gradle dependencies from other repos, without needing
those repos to check this one out as source.

## Modules

- `webserver` (`hdisoft.app:webserver`) — raw-socket HTTP server + static
  asset serving (`SimpleHttpServer`, `HttpWebServerService`), originally
  extracted from the `ci-deploy` repo's `:libs:webserver` module. Fully
  self-contained (no dependency on any other hdisoft module).

## Releasing a new version

1. Bump whatever needs bumping, commit, push to `main`.
2. Tag the release with the module-prefixed scheme and push the tag:
   ```bash
   git tag webserver-v1.0.0
   git push origin webserver-v1.0.0
   ```
3. `.github/workflows/publish-webserver.yml` builds and publishes
   `hdisoft.app:webserver:1.0.0` to this repo's GitHub Packages Maven
   registry automatically. Check the Actions tab for the run.

Publishing locally (e.g. to test before tagging) works the same way, given
a GitHub PAT with `write:packages`:

```bash
GITHUB_ACTOR=<your-username> GITHUB_TOKEN=<pat-with-write:packages> \
  ./gradlew :webserver:publish -PlibVersion=1.0.0-local
```

## Consuming `webserver` from another project

GitHub Packages requires authentication to *read* packages too, even for a
public repo — there's no anonymous access. Each consumer needs a GitHub
Personal Access Token with the `read:packages` scope.

**1. Add the GitHub Packages repository** (project-level `settings.gradle.kts`,
inside `dependencyResolutionManagement.repositories`, or the module's own
`build.gradle.kts` `repositories {}` block):

```kotlin
maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/TrungTT324/android-libs")
    credentials {
        username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR") ?: "")
        password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN") ?: "")
    }
}
```

**2. Provide credentials** — never commit them. Either export env vars
before building:

```bash
export GITHUB_ACTOR=<your-username>
export GITHUB_TOKEN=<pat-with-read:packages>
```

or add to your **global** `~/.gradle/gradle.properties` (not the project's):

```properties
gpr.user=<your-username>
gpr.key=<pat-with-read:packages>
```

**3. Add the dependency:**

```kotlin
dependencies {
    implementation("hdisoft.app:webserver:1.0.0")
}
```

## Local development against ci-deploy

`ci-deploy`'s own `settings.gradle.kts` does **not** use the published
package — it points the `:libs:webserver` module coordinate straight at
this repo's checked-out `webserver/` folder via `projectDir`, so editing
source here is picked up immediately by a `ci-deploy` build without any
publish/version-bump round-trip:

```kotlin
include(":libs:webserver")
project(":libs:webserver").projectDir = File(rootDir, "../android_libs/webserver")
```

That requires `android_libs` to be checked out as a sibling directory next
to `ci-deploy`. The GitHub Packages publish flow above is for *other*
projects that don't have this repo checked out locally.
