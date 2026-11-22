# Shared repository / tool discovery for the PowerShell build and test scripts.
# Dot-source it from a script:  . (Join-Path $PSScriptRoot 'common.ps1')
# The captured directory keeps Resolve-RepoRoot correct no matter which script
# dot-sources this file or what the caller's current location is.
$__spkCommonDir = $PSScriptRoot
$__spkIsWindows = if ($null -ne $IsWindows) { $IsWindows } else { $true }

function Resolve-RepoRoot {
    (Resolve-Path (Join-Path $__spkCommonDir '..')).Path
}

function Resolve-BuildHome {
    param([string]$Path = "$env:LOCALAPPDATA\SpektraLabBuild")
    if (Test-Path -LiteralPath $Path) { return (Resolve-Path -LiteralPath $Path).Path }
    return $Path
}

function Join-ToolPath {
    param([string]$Directory, [string]$Name)
    $ext = if ($__spkIsWindows) { '.exe' } else { '' }
    Join-Path $Directory ($Name + $ext)
}

function Get-Jdk17 {
    param([string]$BuildHome)
    $jdk = (Get-ChildItem (Join-Path $BuildHome 'jdk-17*') -Directory -ErrorAction SilentlyContinue |
            Sort-Object Name -Descending | Select-Object -First 1).FullName
    if (!$jdk) { $jdk = $env:JAVA_HOME }
    return $jdk
}

function Get-Ndk {
    param([string]$SdkRoot, [string]$Preferred)
    if ($Preferred) { return $Preferred }
    $ndkRoot = Join-Path $SdkRoot 'ndk'
    $known = Join-Path $ndkRoot '28.2.13676358'
    if (Test-Path -LiteralPath $known) { return $known }
    $newest = Get-ChildItem -LiteralPath $ndkRoot -Directory -ErrorAction SilentlyContinue |
              Sort-Object Name -Descending | Select-Object -First 1
    if ($newest) { return $newest.FullName }
    return $known
}

function Read-Version {
    param([string]$Root)
    Get-Content -LiteralPath (Join-Path $Root 'version.json') -Raw | ConvertFrom-Json
}

function Get-Adb {
    param([string]$BuildHome)
    Join-ToolPath -Directory (Join-Path $BuildHome 'sdk/platform-tools') -Name 'adb'
}
