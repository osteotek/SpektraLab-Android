# Shared adb scaffold for the opt-in device scripts.
# Dot-source from device-smoke.ps1 / device-golden.ps1:
#   . (Join-Path $PSScriptRoot 'device-common.ps1')
. (Join-Path $PSScriptRoot 'common.ps1')

$script:SpkAdb = $null
$script:SpkAdbSerialArgs = @()

function Resolve-Adb {
    param([string]$BuildHome, [string]$Serial, [switch]$AutoDiscover)
    $adb = Get-Adb $BuildHome
    if (!(Test-Path $adb)) { throw "adb not found: $adb (run scripts/setup-windows.ps1)" }
    & $adb start-server | Out-Null
    $script:SpkAdb = $adb
    if ($AutoDiscover -and !$Serial) {
        if (!(& $adb devices | Select-String 'device$')) { throw 'No Android device connected.' }
        $script:SpkAdbSerialArgs = @()
    } else {
        $script:SpkAdbSerialArgs = if ($Serial) { @('-s', $Serial) } else { @() }
    }
    return $adb
}

function Test-DeviceInputs {
    param([string]$Apk, [string]$Serial, [switch]$RequireSerial)
    if (!(Test-Path $Apk)) { throw "APK not found: $Apk" }
    if ($RequireSerial -and !$Serial) { throw 'A device -Serial is required (see adb devices).' }
}

function Invoke-Adb {
    param([string[]]$Arguments)
    $all = @($script:SpkAdbSerialArgs) + @($Arguments)
    & $script:SpkAdb @all
}

function Install-TestApk {
    param([string]$Apk)
    Invoke-Adb @('install', '-r', $Apk) | Out-Host
    if ($LASTEXITCODE -ne 0) { throw 'adb install failed' }
}

function Reset-Logcat {
    Invoke-Adb @('logcat', '-c') | Out-Null
}

function Stop-App {
    param([string]$Package)
    Invoke-Adb @('shell', 'am', 'force-stop', $Package)
}

function Start-App {
    param([string[]]$Arguments, [string]$ErrorMessage = 'activity launch failed')
    Invoke-Adb (@('shell', 'am', 'start') + @($Arguments)) | Out-Host
    if ($LASTEXITCODE -ne 0) { throw $ErrorMessage }
}
