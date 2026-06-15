# Genera iconos Android (GSPCOMS Chat) desde gspchat.png.
# Produce PNG (Android resuelve recursos por nombre, no por extension) y borra los .webp.
# Capas adaptive: foreground (logo en safe-zone ~62%), monochrome (silueta blanca), legacy, playstore.
param(
    [string]$Src = "c:\Projectos ZABCOM\Servers\Server 114\image\gspchat.png",
    [string]$ResDir = "C:\Projectos ZABCOM\Github\element-x-android\appicon\element\src\main\res",
    [string]$PlayStore = "C:\Projectos ZABCOM\Github\element-x-android\appicon\element\src\main\ic_launcher-playstore.png"
)
Add-Type -AssemblyName System.Drawing

$image = [System.Drawing.Image]::FromFile($Src)
Write-Output "Fuente: $($image.Width)x$($image.Height)"

function New-Canvas {
    param([int]$Size, [object]$Bg = $null)
    $bmp = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    if ($null -ne $Bg) { $g.Clear($Bg) } else { $g.Clear([System.Drawing.Color]::Transparent) }
    return @($bmp, $g)
}

# Dibuja $img centrado ocupando $fraction del lienzo
function Draw-Centered {
    param($g, $img, [int]$Size, [double]$Fraction, $attr = $null)
    $target = $Size * $Fraction
    $scale = [Math]::Min($target / $img.Width, $target / $img.Height)
    $w = [int]($img.Width * $scale); $h = [int]($img.Height * $scale)
    $x = [int](($Size - $w) / 2); $y = [int](($Size - $h) / 2)
    if ($null -ne $attr) {
        $rect = New-Object System.Drawing.Rectangle($x, $y, $w, $h)
        $g.DrawImage($img, $rect, 0, 0, $img.Width, $img.Height, [System.Drawing.GraphicsUnit]::Pixel, $attr)
    } else {
        $g.DrawImage($img, $x, $y, $w, $h)
    }
}

function Save-Png { param($bmp, [string]$path)
    $dir = Split-Path $path -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    $ms = New-Object System.IO.MemoryStream
    $bmp.Save($ms, [System.Drawing.Imaging.ImageFormat]::Png)
    [System.IO.File]::WriteAllBytes($path, $ms.ToArray()); $ms.Dispose()
}

# ColorMatrix para silueta blanca conservando alfa (themed/monochrome)
$cm = New-Object System.Drawing.Imaging.ColorMatrix
$cm.Matrix00=0; $cm.Matrix01=0; $cm.Matrix02=0; $cm.Matrix03=0; $cm.Matrix04=0
$cm.Matrix10=0; $cm.Matrix11=0; $cm.Matrix12=0; $cm.Matrix13=0; $cm.Matrix14=0
$cm.Matrix20=0; $cm.Matrix21=0; $cm.Matrix22=0; $cm.Matrix23=0; $cm.Matrix24=0
$cm.Matrix30=0; $cm.Matrix31=0; $cm.Matrix32=0; $cm.Matrix33=1; $cm.Matrix34=0
$cm.Matrix40=1; $cm.Matrix41=1; $cm.Matrix42=1; $cm.Matrix43=0; $cm.Matrix44=1
$whiteAttr = New-Object System.Drawing.Imaging.ImageAttributes
$whiteAttr.SetColorMatrix($cm)

$legacy   = @{ "mdpi"=48; "hdpi"=72; "xhdpi"=96; "xxhdpi"=144; "xxxhdpi"=192 }
$adaptive = @{ "mdpi"=108; "hdpi"=162; "xhdpi"=216; "xxhdpi"=324; "xxxhdpi"=432 }

foreach ($d in $legacy.Keys) {
    $sz = $legacy[$d]
    # legacy ic_launcher + round: logo ~92% transparente
    foreach ($name in @("ic_launcher","ic_launcher_round")) {
        $c = New-Canvas -Size $sz
        Draw-Centered -g $c[1] -img $image -Size $sz -Fraction 0.92
        Save-Png -bmp $c[0] -path (Join-Path $ResDir "mipmap-$d\$name.png")
        $c[1].Dispose(); $c[0].Dispose()
    }
}

foreach ($d in $adaptive.Keys) {
    $sz = $adaptive[$d]
    # foreground: logo en safe-zone ~62%
    $c = New-Canvas -Size $sz
    Draw-Centered -g $c[1] -img $image -Size $sz -Fraction 0.62
    Save-Png -bmp $c[0] -path (Join-Path $ResDir "mipmap-$d\ic_launcher_foreground.png")
    $c[1].Dispose(); $c[0].Dispose()
    # monochrome: silueta blanca ~62%
    $c = New-Canvas -Size $sz
    Draw-Centered -g $c[1] -img $image -Size $sz -Fraction 0.62 -attr $whiteAttr
    Save-Png -bmp $c[0] -path (Join-Path $ResDir "mipmap-$d\ic_launcher_monochrome.png")
    $c[1].Dispose(); $c[0].Dispose()
}

# Play Store 512 sobre blanco
$c = New-Canvas -Size 512 -Bg ([System.Drawing.Color]::White)
Draw-Centered -g $c[1] -img $image -Size 512 -Fraction 0.86
Save-Png -bmp $c[0] -path $PlayStore
$c[1].Dispose(); $c[0].Dispose()

# Borrar los .webp (reemplazados por .png del mismo nombre)
Get-ChildItem $ResDir -Recurse -Filter "ic_launcher*.webp" | ForEach-Object { Remove-Item $_.FullName -Force }

$image.Dispose()
Write-Output "Iconos Android generados (PNG) y .webp eliminados. LISTO"
