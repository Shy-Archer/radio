import java.io.File;

import jaco.mp3.player.MP3Player;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.sound.sampled.*;

public class Client implements LineListener{

    boolean isPlaybackCompleted;

    @Override
    public void update(LineEvent event) {
        if (LineEvent.Type.START == event.getType()) {
            System.out.println("Playback started.");
        } else if (LineEvent.Type.STOP == event.getType()) {
            isPlaybackCompleted = true;
            System.out.println("Playback completed.");
        }
    }

    private static final int PORT = 12345;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int BUFFER_SIZE = 4096;

    private static Socket clientSocket;

    private static void handleUpload() throws InterruptedException {
        try {
            String filePath = "/home/czarka/IdeaProjects/radio/sound/";
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            printWriter.write("upload");
            printWriter.flush();
            Thread.sleep(1000);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter filename to upload: ");
            String filename = scanner.nextLine();
//            String filename = "sound/s.mp3"; // Replace with the actual path to your mp3 file
            String fullPath = filePath + filename;

            // Opening file input stream
            FileInputStream fileInputStream = new FileInputStream(fullPath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);


            // Opening output stream to send file to server
            OutputStream outputStream = clientSocket.getOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            // Sending file name to server
            printWriter.write(filename);
            printWriter.flush();
            Thread.sleep(1000);
            String fileDataSignal = "START_FILE_DATA";
            printWriter.write(fileDataSignal);
            printWriter.flush();

            // Sending file content to server
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);

            }
            bufferedOutputStream.flush();
            printWriter.flush();

            // Signal the end of file transmission
            printWriter.write("END_OF_FILE");
            printWriter.flush();


            // Closing streams

            bufferedOutputStream.flush();
            printWriter.flush();
            // clientSocket.shutdownOutput();

            System.out.println("File " + filename + " uploaded successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleSkip() {
        try {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(),true);
            printWriter.write("skip");
            printWriter.flush();
            System.out.println("Sending skip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void handleDownloadAndDelete() throws InterruptedException{
        try {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            printWriter.write("download_and_delete");
            printWriter.flush();

            // Odbieranie nazwy pliku od serwera
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String filename = "temporary.mp3";
            if (filename.equals("NO_FILES")) {
                System.out.println("No files available for download.");
                return;
            }

            // Tworzenie strumienia do zapisu pliku
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            // Odbieranie danych pliku od serwera
            InputStream inputStream = clientSocket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Sprawdzenie, czy w buforze znajduje się sygnał "END_OF_FILE"
                if (containsEndOfFileSignal(buffer, bytesRead)) {
                    System.out.println("Received END_OF_FILE signal. File transmission completed.");
                    bytesRead -= "END_OF_FILE".length();
                    break;
                }

                System.out.println(bytesRead);
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }

            // Zamykanie strumieni
            bufferedOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();

            System.out.println("File downloaded: " + filename);

            // Odtwarzanie pliku dźwiękowego
            playAudio(filename);


            Thread.sleep(5000);

            // Usuwanie pliku na końcu
            File fileToDelete = new File(filename);
            if (fileToDelete.delete()) {
                System.out.println("File deleted: " + filename);
            } else {
                System.out.println("Error deleting file: " + filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean containsEndOfFileSignal(byte[] buffer, int bytesRead) {
        if (bytesRead >= "END_OF_FILE".length()) {
            for (int i = 0; i < "END_OF_FILE".length(); i++) {
                if (buffer[bytesRead - "END_OF_FILE".length() + i] != "END_OF_FILE".charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    private static void playAudio(String filename) {
        try{
//            String bip = filename;
            MP3Player mp3player = new MP3Player(new File(filename));
            mp3player.play();
        } catch (Exception e) {
                e.printStackTrace();
        }
//        MP3Player mp3player = new MP3Player(new File("src/s.mp3"));
//        mp3player.play();
//        Platform.runLater(() -> {
//            try {
//                String bip = filename;
//                Media hit = new Media(new File(bip).toURI().toString());5
//                MediaPlayer mediaPlayer = new MediaPlayer(hit);
//                mediaPlayer.play();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }
    private static void handleViewQueue() {
        try {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(),true);

            printWriter.write("view");
            printWriter.flush();

            BufferedReader intReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            int qlen = intReader.read();
            System.out.println("i:" + qlen);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String queueInfo;
            System.out.println("\nCurrent queue:");

            for(int i=0; i < qlen; i++){
                System.out.println("\nCurrent:");
                queueInfo = bufferedReader.readLine();
                System.out.println(queueInfo);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleQuit() {
        System.out.println("quit");
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeUTF("quit");

            clientSocket.close();
            System.out.println("Disconnected from the server. Exiting...");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        javafx.application.Platform.startup(() -> {});
        Scanner scanner = null;
        try {
            clientSocket = new Socket("127.0.0.1", PORT);

            System.out.println("Connected to the server.");

            while (true) {
                System.out.println("\nOptions:");
                System.out.println("1. Upload a file");
                System.out.println("2. Skip to the next file");
                System.out.println("3. View current queue");
                System.out.println("4. Quit");
                System.out.println("5. Download and play");

                scanner = new Scanner(System.in);

                System.out.print("Enter your choice: \n\n");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume the newline character

                switch (choice) {
                    case 1:

                        handleUpload();

                        break;
                    case 2:
                        handleSkip();
                        break;
                    case 3:
                        handleViewQueue();
                        break;
                    case 4:
                        handleQuit();
                        break;
                    case 5:
                        handleDownloadAndDelete();
//                        try {
//                            playAudio("src/s.mp3");
//                            // Step 1: Load the audio file
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }

                        break;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {if (scanner != null) {scanner.close();}}
    }
}