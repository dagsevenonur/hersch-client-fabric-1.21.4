HerschClient

HerschClient, Minecraft Fabric altyapısı üzerine inşa edilmiş, performans ve kullanıcı deneyimini merkezine alan modern bir client projesidir. Projenin temel amacı, oyuncuya gereksiz karmaşa yaratmadan; tamamen özelleştirilebilir, hafif ve sürdürülebilir bir client deneyimi sunmaktır.

Client mimarisi baştan sona modüler olacak şekilde tasarlanmıştır. HUD bileşenleri (FPS, CPS, koordinat, yön, keystrokes vb.) ve modüller (örneğin AutoSprint) birbirinden bağımsızdır. Bu sayede hem yeni özellik eklemek hem de mevcut sistemleri genişletmek oldukça kolaydır.

Teknik Genel Bakış

Fabric (1.21.x) tabanlıdır

Java 21 kullanır

HUD sistemi tamamen widget tabanlıdır

Her widget kendi ayarlarını (scale, arka plan, opacity vb.) taşır

Konumlandırma ve aktiflik durumu ayrı ayrı yönetilir

Ayarlar ve durumlar JSON tabanlı config sistemi ile kaydedilir

Oyuncu oyunu kapatıp açtığında tüm tercihler otomatik yüklenir

Modern, kart tabanlı bir Mod Ayarları arayüzü bulunur

Lunar / Badlion tarzı sade ve okunabilir tasarım

Widget’lar ikonlu kartlar halinde yönetilir

Module sistemi (Movement, Visual, HUD, Misc kategorileri)

Enable / disable durumu kalıcıdır

Client başlatıldığında otomatik uygulanır

HUD & Özelleştirme

HerschClient’in en güçlü yönlerinden biri HUD sistemidir. Oyuncu:

HUD elemanlarını serbestçe taşıyabilir

Her widget için özel ayarlara girebilir

Gereksiz hiçbir şeyle uğraşmadan sadece istediğini aktif edebilir

Şu anda client içerisinde FPS, CPS, koordinat, yön, ping, saat, armor durumu, potion efektleri, keystrokes ve benzeri temel HUD bileşenleri yer almaktadır. Sistem, ileride eklenecek yeni widget’lar için de hazırdır.

Felsefe

HerschClient “her şeyi yapan dev bir client” olmayı değil,
kontrolün oyuncuda olduğu, sade ama güçlü bir yapı sunmayı hedefler.

Gereksiz efekt yok

Zorunlu mod yok

Kullanıcının istemediği hiçbir şey çalışmaz

Bu da client’ı hem günlük kullanım hem de uzun vadeli geliştirme için ideal hale getirir.