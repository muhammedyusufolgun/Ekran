# Monitor (Windows 10/11 + Android)
**Durum: deneysel çoğaltma prototipi.** Windows'un yerleşik **Uzat** ve **Yenile/Çoğalt** ekran menüsüyle bütünleşmez. Miracast alıcısı değildir. Windows için ayrı bir gönderici programı gerekir.

## Windows — kurulum gerektirmeyen taşınabilir gönderici
1. Bu depodaki `Monitor-Gonder.ps1` ve `Monitor-Baslat.cmd` dosyalarını **aynı klasöre** indirin.
2. Windows 10/11 Pro bilgisayarda `Monitor-Baslat.cmd` dosyasını çalıştırın. Bu, Windows'un kendi PowerShell 5.1 ve .NET bileşenlerini kullanır; Python, tarayıcı veya üçüncü taraf yazılım kurulumu gerekmez.
3. Windows güvenlik duvarında istenirse **özel ağ** için izin verin. TCP 48621 erişimi ve UDP 48620 yayın keşfi gereklidir. Telefon ve bilgisayar aynı Wi-Fi ağında, istemci yalıtımı kapalı olmalıdır.
4. Android telefonda Monitor.apk'yi açın. Telefon UDP duyurusunu algılayıp bilgisayarın TCP 48621 portuna bağlanmayı dener; başarılıysa Windows'un **birincil ekranı** JPEG kareleriyle eşzamanlı görüntülenir.
5. Göndericiyi durdurmak için Windows komut penceresinde Ctrl+C kullanın.

**Güvenlik:** Bu deneysel protokolde görüntü şifrelenmez ve kimlik doğrulaması yoktur. Yalnızca güvenilen özel ağlarda çalıştırın. PowerShell'i yönetici olarak çalıştırmak gerekmez; kurum politikası komut dosyası çalıştırmayı engelliyorsa politikayı izinsiz aşmayın.

**Sınırlamalar:** Windows + K, gerçek ikinci monitör veya Uzat modu değildir. Birincil ekran çoğaltılır; masaüstü kilitlenmez. Performans ve bağlantı gerçek cihazlarda henüz doğrulanmadı. Önceki `windows_sender.py` ve `Monitor-PC.bat` yöntemleri eski alternatiflerdir ve ek bağımlılıklar gerektirebilir.

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


## Gerçek Windows Miracast alıcısı — platform engeli (2026-11)

**Durum: uygulanmadı / çalıştığı doğrulanmadı.** Wi-Fi Direct taraması, Miracast sink değildir. Windows Win+K ile bulunabilmek için Wi-Fi Display sink (WFD IE) ilanı, P2P grup kurulumu, RTSP kontrolü, RTP/MPEG-TS alımı ve H.264 donanım çözme gerekir. Standart Android APK, WFD ilanı için gereken `CONFIGURE_WIFI_DISPLAY` ayrıcalığını alamaz; Samsung Android 17 tanılamasında izin reddedilmiştir. Bu nedenle normal APK derlemesiyle Win+K çalışır denemez.

Alternatif araştırma: https://github.com/InsertX2k/miracast_second_screen (root/Magisk/LSPosed gerektirir) ve https://github.com/Morishima-Yoru/Mirax (Shizuku/ayrıcalıklı yardımcı, cihaza bağlı). Bu projelerin Galaxy Z Flip7 Android 17 ile uyumluluğu doğrulanmamıştır. Root/bootloader değişiklikleri cihaz güvenliği ve veri kaybı riski taşır. Windows'a yazılım kurmadan çalışma hedefi korunmaktadır; ancak telefon tarafında sistem yetkisi veya üretici desteği gerekebilir.
