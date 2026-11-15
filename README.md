# Monitor (Windows 10/11 + Android)
**Durum: deneysel çoğaltma prototipi.** Windows'un yerleşik **Uzat** ve **Yenile/Çoğalt** ekran menüsüyle bütünleşmez. Miracast alıcısı değildir. Windows için ayrı bir gönderici programı gerekir.

## Windows
1. Python 3 yükleyin.
2. `pip install pillow mss`
3. `python windows_sender.py` çalıştırın.
4. Windows Güvenlik Duvarı'nda özel ağlar için TCP 48621 ve UDP 48620 trafiğine izin verin.
5. Telefonları aynı Wi-Fi ağına bağlayıp **Monitor** uygulamasını açın.

Windows programı, birinci monitörü JPEG kareleri halinde aynı ağdaki bütün telefonlara yayınlar. Yaklaşık 10 FPS, en fazla 1280x720; gecikme ağ koşullarına bağlıdır. Aynı Wi-Fi ağında olmak tek başına yeterli olmayabilir: istemci yalıtımı, VPN ve güvenlik duvarı bağlantıyı engelleyebilir.

## Android
Yatay beyaz bekleme ekranı, çift dokununca 5 saniyelik dönüş, bağlantı kopunca bekleme ekranı, üç saniye içinde iki aşamalı kapatma.

## Sınırlamalar
- Gerçek ikinci/üçüncü Windows ekranı ve Windows Ayarlar > Ekran > Uzat özelliği **henüz uygulanmadı**. Bunun için IddCx sanal ekran sürücüsü, Windows görüntü akışı ve cihaz başına ekran yönlendirme gerekiyor.
- Bu sürümün görüntü akışı şifrelenmez ve eşleşme istemez. Yalnızca güvenilen yerel ağlarda kullanın.
- İlk sürüm görüntüyü ekrana esnetir; en-boy oranını korumaz.
- Çok cihazlı eşzamanlılık ve gerçek cihaz davranışı henüz test edilmedi.
- GitHub Actions iş akışı `Monitor.apk` adlı debug APK üretmeyi hedefler; Actions sayfasından durumunu kontrol edin.

## Windows + K hakkında
Windows + K, Miracast alıcılarını keşfeder. Bu uygulama Miracast (Wi-Fi Direct, RTSP ve ilgili medya protokolleri) alıcısı değildir; bu yüzden Windows + K listesinde görünmez. Mevcut prototipte Windows üzerinde `windows_sender.py` çalıştırılması gerekir. Windows + K desteği için ayrı bir Miracast alıcı altyapısı ve cihaz/işletim sistemi uyumluluğu geliştirilip test edilmelidir. APK'nın yeniden derlenmesi tek başına bunu sağlamaz.

## Miracast alıcı dönüşümü — doğrulama kapıları

Windows bilgisayarda `netsh wlan show drivers` çıktısı `Wireless Display Supported: Yes (Graphics Driver: Yes, Wi-Fi Driver: Yes)` olarak doğrulandı. Bu yalnızca Windows'un Miracast gönderici desteğini doğrular.

**Kapı 1 — Android sistem yetkisi:** Android 17'de `CONFIGURE_WIFI_DISPLAY` izni `signature|knownSigner`, `CONTROL_WIFI_DISPLAY` izni `signature` düzeyindedir. Normal debug APK bu izinleri kendi kendine kazanamaz. Uygulamadaki Bağlantı Tanılama ekranı birinci iznin durumunu gösterir. Sistem izinleri verilmeden Wi-Fi Direct eşleşmesi Miracast alıcı ilanı yerine geçmez.

**Kapı 2 — Miracast sink keşfi:** Windows + K içinde cihazın gerçek bir Wi-Fi Display sink olarak görünmesi, yalnızca standart `discoverPeers()` çağrısı ile sağlanmaz. Bu kapı cihaz üzerinde doğrulanmadı.

**Kapı 3 — Oturum ve medya:** RTSP kontrol oturumu, RTP/MPEG-TS veya uyumlu RTP aktarımı, H.264 donanımsal çözme ve bağlantı kopma yönetimi uygulanıp uçtan uca test edilmelidir. Mevcut JPEG/TCP protokolü Miracast değildir.

**Kapı 4 — Kabul testi:** Windows + K keşfi, bağlantı, çoğaltma, genişletme, gecikme ve yeniden bağlanma gerçek Windows 10/11 + Galaxy Z Flip7 cihazlarında ayrı ayrı doğrulanmalıdır. Hiçbiri henüz başarılı olarak raporlanmamıştır.

**Kısıt:** Windows'a yazılım kurulmayacak. Normal APK ile sistem izinleri sağlanamıyorsa üretici desteği veya işletim sistemi düzeyinde değişiklik olmadan Windows + K hedefi sağlanamayabilir. APK'nın başarıyla derlenmesi Miracast uyumluluğu anlamına gelmez.
