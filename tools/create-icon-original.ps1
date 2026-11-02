Add-Type -AssemblyName System.Drawing
$bitmap=[System.Drawing.Bitmap]::new(400,400)
$g=[System.Drawing.Graphics]::FromImage($bitmap)
$g.SmoothingMode=[System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.ColorTranslator]::FromHtml('#12101d'))
function Brush($hex){[System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($hex))}
function Polygon($hex,$coords){
    $points=[System.Drawing.PointF[]]@()
    for($i=0;$i -lt $coords.Count;$i+=2){$points += [System.Drawing.PointF]::new($coords[$i],$coords[$i+1])}
    $b=Brush $hex
    $g.FillPolygon($b,$points)
    $b.Dispose()
}
$pen=[System.Drawing.Pen]::new([System.Drawing.ColorTranslator]::FromHtml('#38304f'),3)
$g.DrawEllipse($pen,30,30,340,340)
$pen.Dispose()
# Two halves of one warrior, with the shadow side dissolving outward.
Polygon '#6e5b9b' @(200,55,268,83,291,164,267,230,234,256,200,271)
Polygon '#b9b3ce' @(200,55,132,83,109,164,133,230,166,256,200,271)
Polygon '#e3dfed' @(200,70,146,93,126,155,147,144,159,106,200,88)
Polygon '#252032' @(200,113,152,130,137,163,150,189,180,198,200,229,220,198,250,189,263,163,248,130)
Polygon '#080610' @(200,113,248,130,263,163,250,189,220,198,200,229)
Polygon '#eeeaf8' @(141,155,183,163,183,176,148,170)
Polygon '#cf9dff' @(217,163,259,155,252,170,217,176)
Polygon '#b9b3ce' @(165,243,200,272,200,350,78,350,96,290)
Polygon '#4e3d70' @(200,272,234,243,304,290,322,350,200,350)
Polygon '#ddd7eb' @(111,290,156,261,173,285,143,300,100,325)
Polygon '#171020' @(200,292,219,281,238,350,200,350)
Polygon '#8771b4' @(278,102,305,94,298,119,280,133)
Polygon '#6e5b9b' @(303,154,327,143,339,168,313,176)
Polygon '#a68bcf' @(290,204,310,191,328,210,307,230)
Polygon '#4e3d70' @(333,250,351,252,344,275,326,269)
$bitmap.Save((Join-Path (Get-Location) 'outputs/halfshade-icon-400.png'),[System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose();$bitmap.Dispose()
