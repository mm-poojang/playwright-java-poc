# playwright-java-poc

## MDO login test credentials

`MdoLoginTest` does **not** store a password in the repo. Provide it at runtime in either of these ways:

### Option A — environment variable (recommended)

**PowerShell** (same window you use for Maven):

```powershell
$env:MDO_LOGIN_PASSWORD = "your-actual-password"
mvn test -Dtest=MdoLoginTest
```

**Windows Command Prompt**:

```bat
set MDO_LOGIN_PASSWORD=your-actual-password
mvn test -Dtest=MdoLoginTest
```

Set `MDO_LOGIN_PASSWORD` in **Windows User environment variables** if you want it available in every terminal and in your IDE.

### Option B — JVM system property

```powershell
mvn test -Dtest=MdoLoginTest "-Dmdo.login.password=your-actual-password"
```

The `-D` value can appear in shell history and process listings; prefer the env var when you can.

### IDE (Cursor / VS Code)

Use **Settings → Terminal → Integrated: Env** or your **launch configuration** / **Java Test Runner** setup to define `MDO_LOGIN_PASSWORD`, or add VM options `-Dmdo.login.password=…` for the run configuration that executes tests.
