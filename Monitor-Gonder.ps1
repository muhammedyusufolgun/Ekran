# Monitor portable sender - Windows PowerShell 5.1, no installation required.
# Run on a trusted private Wi-Fi network only. Unencrypted JPEG stream.
param([int]$Port=48621,[int]$Fps=8,[int]$Width=1280)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
$listener=[System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any,$Port)
$udp=[System.Net.Sockets.UdpClient]::new()
$udp.EnableBroadcast=$true
$announce=[System.Text.Encoding]::UTF8.GetBytes("KABLOSUZ_EKRAN_V1:$Port")
$broadcast=[System.Net.IPEndPoint]::new([System.Net.IPAddress]::Broadcast,48620)
$listener.Start()
Write-Host "Monitor gonderici hazir. TCP $Port / UDP 48620. Telefon Monitor.apk acik olmali."
Write-Host "Yalnizca guvenilen ozel agda kullanin. Durdurmak icin Ctrl+C."
$lastAnnounce=[DateTime]::MinValue
try {
 while($true){
  if(([DateTime]::UtcNow-$lastAnnounce).TotalSeconds -ge 2){
   try{$null=$udp.Send($announce,$announce.Length,$broadcast)}catch{Write-Warning $_.Exception.Message}
   $lastAnnounce=[DateTime]::UtcNow
  }
  if(-not $listener.Pending()){Start-Sleep -Milliseconds 150;continue}
  $client=$listener.AcceptTcpClient()
  Write-Host "Telefon baglandi: $($client.Client.RemoteEndPoint)"
  try{
   $client.NoDelay=$true
   $network=$client.GetStream()
   $writer=[System.IO.BinaryWriter]::new($network)
   while($client.Connected){
    $bounds=[System.Windows.Forms.Screen]::PrimaryScreen.Bounds
    $scale=[Math]::Min(1.0,[double]$Width/$bounds.Width)
    $w=[Math]::Max(1,[int]($bounds.Width*$scale))
    $h=[Math]::Max(1,[int]($bounds.Height*$scale))
    $source=[System.Drawing.Bitmap]::new($bounds.Width,$bounds.Height)
    $graphics=[System.Drawing.Graphics]::FromImage($source)
    $target=$null;$g2=$null;$memory=$null
    try{
     $graphics.CopyFromScreen($bounds.Left,$bounds.Top,0,0,$bounds.Size)
     $target=[System.Drawing.Bitmap]::new($w,$h)
     $g2=[System.Drawing.Graphics]::FromImage($target)
     $g2.InterpolationMode=[System.Drawing.Drawing2D.InterpolationMode]::HighQualityBilinear
     $g2.DrawImage($source,0,0,$w,$h)
     $memory=[System.IO.MemoryStream]::new()
     $target.Save($memory,[System.Drawing.Imaging.ImageFormat]::Jpeg)
     $bytes=$memory.ToArray()
     $n=[System.Net.IPAddress]::HostToNetworkOrder([int]$bytes.Length)
     $writer.Write($n)
     $writer.Write($bytes)
     $writer.Flush()
    }finally{
     if($memory){$memory.Dispose()};if($g2){$g2.Dispose()};if($target){$target.Dispose()}
     $graphics.Dispose();$source.Dispose()
    }
    Start-Sleep -Milliseconds ([Math]::Max(50,[int](1000/[Math]::Max(1,$Fps))))
   }
  }catch{Write-Warning "Baglanti kapandi: $($_.Exception.Message)"}
  finally{$client.Close()}
 }
}finally{$udp.Dispose();$listener.Stop()}
