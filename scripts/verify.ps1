# Fast, offline verification of everything that does not need a device.
# CI calls this same script with explicit tool paths instead of reimplementing
# the checks; see .github/workflows/verify.yml.
[CmdletBinding()]
param(
    [string]$BuildHome = "$env:LOCALAPPDATA\SpektraLabBuild",
    [string]$JavaHome,
    [string]$AndroidSdkRoot,
    [string]$NdkPath,
    [string]$EngineRoot,
    [switch]$SkipNative,
    [switch]$StrictEngine
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'common.ps1')

$root = Resolve-RepoRoot
$failed = @()
$engineWarnings = 0
function Src([string]$relative) { Join-Path $root $relative }

function Report($name, [bool]$ok) {
    if ($ok) { Write-Host "  PASS  $name" } else { Write-Host "  FAIL  $name"; $script:failed += $name }
}

# The published engine patch is a known blocker. By default these two checks
# are diagnostics; -StrictEngine promotes them to real failures.
function Report-Engine($name, [bool]$ok) {
    if ($ok) { Write-Host "  PASS  $name"; return }
    Write-Host "  WARN  $name (known blocker; see docs/source-build.md)"
    $script:engineWarnings++
    if ($StrictEngine) {
        Write-Host "  FAIL  $name (-StrictEngine)"
        $script:failed += $name
    }
}

# Python: the embedded runtime in BuildHome, or whatever is on PATH.
$py = Join-Path $BuildHome 'python.exe'
if (!(Test-Path $py)) {
    $py = (Get-Command python3 -ErrorAction SilentlyContinue).Source
    if (!$py) { $py = (Get-Command python -ErrorAction SilentlyContinue).Source }
}
if (!$py) { throw 'No Python interpreter found (run scripts/setup-windows.ps1).' }

Write-Host '== locale catalogs =='
& $py (Src 'app/build/check-locales.py')
Report 'check-locales' ($LASTEXITCODE -eq 0)

Write-Host '== version metadata =='
& $py (Src 'app/build/check-version.py')
Report 'check-version' ($LASTEXITCODE -eq 0)

Write-Host '== prebuilt native payload =='
& $py (Src 'app/build/check-prebuilt.py')
Report 'check-prebuilt' ($LASTEXITCODE -eq 0)

Write-Host '== build-tool tests =='
& $py (Src 'app/build/test_build_tools.py')
Report 'test_build_tools' ($LASTEXITCODE -eq 0)
& $py (Src 'app/build/test_checkers.py')
Report 'test_checkers' ($LASTEXITCODE -eq 0)
& $py (Src 'app/build/check-no-silent-catch.py')
Report 'check-no-silent-catch' ($LASTEXITCODE -eq 0)
& $py (Src 'app/build/check-package-rules.py')
Report 'check-package-rules' ($LASTEXITCODE -eq 0)

Write-Host '== Java compilation =='
if (!$JavaHome) { $JavaHome = Get-Jdk17 $BuildHome }
if (!$JavaHome) { throw 'No JDK found (run scripts/setup-windows.ps1 or set JAVA_HOME).' }
$javac = Join-ToolPath -Directory (Join-Path $JavaHome 'bin') -Name 'javac'
$java = Join-ToolPath -Directory (Join-Path $JavaHome 'bin') -Name 'java'
if (!$AndroidSdkRoot) { $AndroidSdkRoot = Join-Path $BuildHome 'sdk' }
$jar = Join-Path $AndroidSdkRoot 'platforms/android-36/android.jar'
if (!(Test-Path $jar)) { throw "Android platform jar missing: $jar" }
$classes = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-verify-classes'
Remove-Item -LiteralPath $classes -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $classes | Out-Null
$sources = Get-ChildItem -LiteralPath (Src 'app/src/main/java') -Recurse -Filter '*.java' |
           ForEach-Object { $_.FullName }
$javacArgs = @('--release', '17', '-encoding', 'UTF-8', '-nowarn', '-classpath', $jar, '-d', $classes) + $sources
& $javac @javacArgs
Report 'javac' ($LASTEXITCODE -eq 0)

Write-Host '== runnable JVM logic tests =='
$logicOut = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-purelogic'
Remove-Item -LiteralPath $logicOut -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $logicOut | Out-Null
$logicSources = @(
    'app/src/main/java/app/spektralab/PixelBudget.java',
    'app/src/main/java/app/spektralab/domain/JsonQuote.java',
    'app/src/main/java/app/spektralab/AppFailure.java',
    'app/src/test/java/app/spektralab/PureLogicTest.java') | ForEach-Object { Src $_ }
& $javac @logicSources -d $logicOut
if ($LASTEXITCODE -eq 0) {
    & $java -cp $logicOut app.spektralab.PureLogicTest
}
Report 'PureLogicTest' ($LASTEXITCODE -eq 0)

Write-Host '== domain use-case tests =='
$domainOut = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-domain'
Remove-Item -LiteralPath $domainOut -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $domainOut | Out-Null
$domainSources = @(
    'app/src/main/java/app/spektralab/domain/EngineApi.java',
    'app/src/main/java/app/spektralab/domain/SessionApi.java',
    'app/src/main/java/app/spektralab/domain/ImportPlanner.java',
    'app/src/main/java/app/spektralab/domain/EditorState.java',
    'app/src/main/java/app/spektralab/domain/RenderLookUseCase.java',
    'app/src/main/java/app/spektralab/domain/RenderPreviewUseCase.java',
    'app/src/main/java/app/spektralab/domain/PhotoRepository.java',
    'app/src/main/java/app/spektralab/domain/AppPreferences.java',
    'app/src/main/java/app/spektralab/domain/JsonFields.java',
    'app/src/main/java/app/spektralab/domain/PixelHash.java',
    'app/src/main/java/app/spektralab/domain/EdgeVerdict.java',
    'app/src/main/java/app/spektralab/domain/ExportPlan.java',
    'app/src/main/java/app/spektralab/domain/ExportSessionPlan.java',
    'app/src/main/java/app/spektralab/domain/ExportController.java',
    'app/src/main/java/app/spektralab/domain/ImportController.java',
    'app/src/main/java/app/spektralab/domain/RenderController.java',
    'app/src/main/java/app/spektralab/domain/JsonQuote.java',
    'app/src/test/java/app/spektralab/testing/FakeSession.java',
    'app/src/test/java/app/spektralab/testing/FakeAppPreferences.java',
    'app/src/test/java/app/spektralab/testing/FakePhotoRepository.java',
    'app/src/test/java/app/spektralab/testing/FakeEngine.java',
    'app/src/test/java/app/spektralab/testing/DomainTest.java') | ForEach-Object { Src $_ }
& $javac @domainSources -d $domainOut
if ($LASTEXITCODE -eq 0) {
    & $java -cp $domainOut app.spektralab.testing.DomainTest
}
Report 'DomainTest' ($LASTEXITCODE -eq 0)

# --- Native C++ checks -----------------------------------------------------
function Get-NdkClang([string]$Ndk) {
    $name = if ($__spkIsWindows) { 'clang++.exe' } else { 'clang++' }
    $hostTag = if ($__spkIsWindows) { 'windows-x86_64' } else { 'linux-x86_64' }
    $prebuilt = Join-Path $Ndk 'toolchains/llvm/prebuilt'
    $preferred = Join-Path $prebuilt "$hostTag/bin/$name"
    if (Test-Path -LiteralPath $preferred) { return $preferred }
    return (Get-ChildItem -LiteralPath $prebuilt -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName "bin/$name" } |
            Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1)
}

if ($SkipNative) {
    Write-Host '== native checks =='
    Write-Host '  SKIP  all native checks (-SkipNative)'
} else {
    $ndk = Get-Ndk -SdkRoot $AndroidSdkRoot -Preferred $NdkPath
    $clang = Get-NdkClang $ndk
    $include = Src 'app/src/main/cpp'
    $viewTest = Src 'app/build/native-tests/view_adjust_test.cpp'
    $metaTest = Src 'app/build/native-tests/export_metadata_test.cpp'
    # jconfig.h is generated by the libjpeg build on Windows; on a CI host the
    # system libjpeg-dev headers are used instead.
    $jpegIncludes = @()
    $jpegSrc = Join-Path $BuildHome 'libjpeg-turbo-3.1.2/src'
    $jpegBuild = Join-Path $BuildHome 'jpeg-android'
    if (Test-Path (Join-Path $jpegSrc 'jpeglib.h')) { $jpegIncludes += "-I$jpegSrc" }
    if (Test-Path (Join-Path $jpegBuild 'jconfig.h')) { $jpegIncludes += "-I$jpegBuild" }
    if (!$jpegIncludes -and !$__spkIsWindows) {
        # CI installs libjpeg-dev. Copy just the public jpeg headers into a
        # private include dir: adding /usr/include to an NDK cross-compile would
        # also shadow the Android sysroot's C library headers.
        $jpegShim = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-jpeg-headers'
        $publicHeaders = @('/usr/include/jpeglib.h', '/usr/include/jmorecfg.h',
                           '/usr/include/jerror.h',
                           '/usr/include/x86_64-linux-gnu/jconfig.h')
        if (Test-Path '/usr/include/jpeglib.h') {
            Remove-Item -LiteralPath $jpegShim -Recurse -Force -ErrorAction SilentlyContinue
            New-Item -ItemType Directory -Path $jpegShim | Out-Null
            foreach ($header in $publicHeaders) {
                if (Test-Path $header) { Copy-Item -LiteralPath $header -Destination $jpegShim }
            }
            $jpegIncludes += "-I$jpegShim"
        }
    }

    Write-Host '== native compile checks (NDK, aarch64) =='
    if (!$clang) {
        Write-Host '  SKIP  clang++ not found in the NDK'
    } else {
        $obj = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-view_adjust_test.o'
        & $clang --target=aarch64-linux-android24 -std=c++20 -O0 "-I$include" -c $viewTest -o $obj
        Report 'view_adjust_test compile' ($LASTEXITCODE -eq 0)
        $metaObj = Join-Path ([IO.Path]::GetTempPath()) 'spk_export_metadata_test.o'
        & $clang --target=aarch64-linux-android24 -std=c++20 -O0 "-I$include" @jpegIncludes -c $metaTest -o $metaObj
        Report 'export_metadata_test compile' ($LASTEXITCODE -eq 0)
    }

    Write-Host '== native run checks (host C++20) =='
    # Both test files are header-only, so a host compiler can link and RUN them.
    # Windows toolchains here are Android-only: without g++/clang++ on PATH the
    # run is explicitly skipped, but the NDK cross-compile checks above still
    # ran. CI (ubuntu) has g++ and always executes these assertions.
    $hostCxx = $env:CXX
    if (!$hostCxx) {
        foreach ($candidate in @('g++', 'clang++', 'c++')) {
            $cmd = Get-Command $candidate -ErrorAction SilentlyContinue |
                   Where-Object { $_.Source -notlike '*ndk*' } | Select-Object -First 1
            if ($cmd) { $hostCxx = $cmd.Source; break }
        }
    }
    if (!$hostCxx) {
        Write-Host '  SKIP  native test run (no host C++ compiler; the NDK compile checks above still ran)'
    } else {
        $viewExe = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-view_adjust_test'
        & $hostCxx -std=c++20 -O0 "-I$include" $viewTest -o $viewExe
        if ($LASTEXITCODE -eq 0) {
            & $viewExe
            Report 'view_adjust_test run' ($LASTEXITCODE -eq 0)
        } else {
            Report 'view_adjust_test run' $false
        }
        $metaExe = Join-Path ([IO.Path]::GetTempPath()) 'spk-export_metadata_test'
        & $hostCxx -std=c++20 -O0 "-I$include" @jpegIncludes $metaTest -o $metaExe
        if ($LASTEXITCODE -eq 0) {
            & $metaExe
            Report 'export_metadata_test run' ($LASTEXITCODE -eq 0)
        } else {
            Report 'export_metadata_test run' $false
        }
    }
}

if ($EngineRoot) {
    Write-Host '== engine patch =='
    & $py (Src 'app/build/check-patch.py') $EngineRoot
    if ($LASTEXITCODE -eq 0) { Write-Host '  PASS  check-patch' }
    else { Report-Engine 'check-patch' $false }

    if (!$SkipNative) {
        Write-Host '== JNI compile against upstream (shimmed missing APIs) =='
        $eng = Join-Path $EngineRoot 'engine'
        $hostTag = if ($__spkIsWindows) { 'windows-x86_64' } else { 'linux-x86_64' }
        $sysroot = Join-Path $ndk "toolchains/llvm/prebuilt/$hostTag/sysroot"
        $shim = Src 'app/build/native-tests/jni-shim.hpp'
        $jpeg = Join-Path $BuildHome 'jpeg-android'
        $jpegSrc = Join-Path $BuildHome 'libjpeg-turbo-3.1.2/src'
        $incs = @("--target=aarch64-none-linux-android24", "--sysroot=$sysroot", '-std=gnu++20',
                  '-fPIC', "-I$(Join-Path $eng 'include')", "-I$(Join-Path $eng 'src')",
                  "-I$(Join-Path $eng 'src/core')", "-I$(Join-Path $eng 'src/pipeline')",
                  "-I$(Join-Path $eng 'src/io')", "-I$(Join-Path $eng 'src/gpu')",
                  "-I$include", "-I$jpeg", "-I$jpegSrc", "-include$shim")
        if ($clang) {
            & $clang @incs -c (Src 'app/src/main/cpp/spektralab_jni.cpp') -o (Join-Path ([IO.Path]::GetTempPath()) 'spk_jni_check.o')
            Report-Engine 'spektralab_jni.cpp compile' ($LASTEXITCODE -eq 0)
        } else {
            Write-Host '  SKIP  spektralab_jni.cpp compile (clang++ not found)'
        }
    }
}

if ($failed.Count) {
    throw ('verify failed: ' + ($failed -join ', '))
}
if ($engineWarnings) {
    Write-Host 'WARN  engine patch/JNI checks are diagnostic by default; use -StrictEngine to enforce them.'
}
Write-Host 'All offline checks passed.'
