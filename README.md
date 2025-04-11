# 🎧 Radio Internetowe – projekt klient-serwer

Projekt zrealizowany w ramach kursu **Sieci Komputerowe**, umożliwiający przesyłanie, przeglądanie, pobieranie i usuwanie plików dźwiękowych za pomocą architektury klient-serwer.

## 📌 Opis projektu

System składa się z:
- **Serwera** napisanego w języku **C**, obsługującego kolejkę plików audio,
- **Klienta** napisanego w języku **Java** z wykorzystaniem **Swing**, umożliwiającego komunikację z serwerem przez **TCP**.

## 🛠️ Technologie

- **Języki programowania**: C (serwer), Java (klient)
- **GUI**: Java Swing
- **Protokoły**: TCP
- **Biblioteka audio**: JACo MP3 Player (do odtwarzania dźwięku)

## 🧩 Funkcjonalności

### Serwer
- Utrzymuje kolejkę plików dźwiękowych (`AudioQueue`)
- Obsługuje połączenia od wielu klientów z synchronizacją przez semafory
- Obsługiwane żądania:
  - `handleUpload` – dodanie pliku do kolejki
  - `handleViewQueue` – przesyłanie listy plików
  - `sendFileFromQueue` – przesyłanie pliku do klienta
  - `removeFromQueue` – usunięcie pliku z kolejki
  - `handleQuit` – zamknięcie połączenia

### Klient
- Nawiązuje połączenie z serwerem przez gniazdo TCP
- Obsługuje:
  - Wysyłanie plików (`handleUpload`)
  - Przeglądanie kolejki (`handleViewQueue`)
  - Pobieranie i odtwarzanie pliku (`handleDownloadAndDelete`)
  - Usuwanie pliku (`handleRemoveSong`)
- Odtwarza pliki MP3 dzięki bibliotece JACo MP3 Player
- Polling co sekundę sprawdza stan kolejki

## 📦 Jak uruchomić?

### Serwer (C)
1. Skompiluj serwer:
   ```bash
   gcc server.c -o server -lpthread
   ```
2. Uruchom:
    ```
    ./server
    ```
### Klient (Java)

1. Otwórz projekt w IDE (np. IntelliJ, Eclipse)

2. Upewnij się, że biblioteka JACo MP3 Player jest dołączona

3. Uruchom klasę główną klienta

## 👨‍💻 Autorzy

- Łukasz Walicki (151061)

- Jacek Młynarczyk (151747)

## 📄 Licencja

Projekt edukacyjny – brak formalnej licencji.
