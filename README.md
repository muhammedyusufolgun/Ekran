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
