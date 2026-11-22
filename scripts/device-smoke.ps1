# Opt-in device smoke test. Requires an ARM64 device with USB debugging.
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$Apk,
    [string]$BuildHome = "$env:LOCALAPPDATA\SpektraLabBuild",
    [string]$Package = 'app.spektralab',
    [string]$Activity,
    [string]$Serial,
    [switch]$Uninstall
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'device-common.ps1')

Test-DeviceInputs -Apk $Apk
Resolve-Adb -BuildHome $BuildHome -Serial $Serial -AutoDiscover
if (!$Activity) { $Activity = "$Package/.MainActivity" }

Write-Host "Installing $Apk"
Install-TestApk $Apk

Reset-Logcat
Stop-App $Package
Start-App @('-n', $Activity)
Start-Sleep -Seconds 8

$log = Invoke-Adb @('logcat', '-d', '-t', '500')
$crash = $log | Select-String 'FATAL EXCEPTION|native crash|SIGSEGV|SIGABRT|AndroidRuntime'
if ($crash) {
    $crash | ForEach-Object { Write-Host $_.Line }
    throw 'crash detected in logcat'
}
$shots = Join-Path ([IO.Path]::GetTempPath()) 'spektralab-smoke.png'
# PowerShell's `>` redirection mangles binary output; pull the file instead.
Invoke-Adb @('shell', 'screencap', '-p', '/sdcard/spektralab-smoke.png')
Invoke-Adb @('pull', '/sdcard/spektralab-smoke.png', $shots) | Out-Host
Invoke-Adb @('shell', 'rm', '/sdcard/spektralab-smoke.png')
Write-Host "Screenshot: $shots"
Write-Host 'Device smoke test passed.'

if ($Uninstall) { Invoke-Adb @('uninstall', $Package) | Out-Host }
