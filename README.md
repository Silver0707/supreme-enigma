# supreme-enigma

A library that provides utilities for running privileged shell commands via [Shizuku](https://shizuku.rikkuness.net/).

## Installation

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    def shizuku_version = "13.1.5"
    implementation "dev.rikka.shizuku:api:$shizuku_version"
}
```

## Usage

### Running a privileged command

Once you have obtained a Shizuku binder (e.g. via `Shizuku.onBinderReceived()`), use `Run.run()` to execute a shell command with elevated privileges:

```java
import rikka.shizuku.kk.Run;

// binder obtained from Shizuku.onBinderReceived()
try {
    Run.Result result = Run.run(binder, new String[]{"id"});
    if (result.isSuccess()) {
        // exit code 0 — command succeeded
    } else {
        int code = result.getExitCode();
    }
} catch (RemoteException e) {
    // handle IPC failure
}
```

### `Run.Result`

| Method | Description |
|---|---|
| `getExitCode()` | Returns the exit code of the command, or `-1` if unavailable. |
| `isSuccess()` | Returns `true` if the command exited with code `0`. |
