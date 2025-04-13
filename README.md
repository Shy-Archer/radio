# 🎧 Internet Radio – Client-Server Audio Streaming System

A university project for the **Computer Networks** course. This system allows users to upload, browse, download, and remove MP3 audio files using a custom client-server architecture over TCP.

## 📌 Project Overview

The application consists of:
- A **server** written in **C** that handles audio file queue management and client requests.
- A **client** written in **Java** using **Swing** for the GUI, enabling interaction with the server.

Communication between client and server is handled via **TCP sockets**.

## 🛠️ Technologies Used

- **Programming Languages**: C (server), Java (client)
- **GUI**: Java Swing
- **Networking**: TCP
- **Audio Playback**: JACo MP3 Player library

## 🧩 Features

### Server
- Maintains a queue of audio files (`AudioQueue`)
- Handles multiple client connections with synchronization (mutex semaphore)
- Supported operations:
  - `handleUpload` – receives and queues audio files from client
  - `handleViewQueue` – sends current queue status to client
  - `sendFileFromQueue` – streams a file to client
  - `removeFromQueue` – removes a specific file from the queue
  - `handleQuit` – closes the client connection

### Client
- Connects to the server over TCP
- Provides a GUI to:
  - Upload MP3 files to the server
  - Browse the server-side file queue
  - Download and play MP3 files (using JACo MP3 Player)
  - Remove files from the queue
- Uses polling every second to update queue status

## 📦 How to Run

### Server (C)
1. Compile the server:
   ```bash
   gcc server.c -o server -lpthread
   ```
2. Run the server:
    ```
    ./server
    ```
    
### Client (Java)

1. Open the client project in your Java IDE (e.g. IntelliJ, Eclipse)

2. Make sure the JACo MP3 Player library is added to the project dependencies

3. Run the main client class

## 👨‍💻 Authors

- Łukasz Walicki (151061)

- Jacek Młynarczyk (151747)

## 📄  License

This project was developed for academic and educational purposes.
