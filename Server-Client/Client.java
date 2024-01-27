import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client {
    private static final int PORT = 12345;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int BUFFER_SIZE = 4096;

    private static Socket clientSocket;

    private static void handleUpload() throws InterruptedException {
        try {
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            printWriter.write("upload");
            printWriter.flush();
            Thread.sleep(1000);
            System.out.print("Enter filename to upload: ");
            String filename = "sound.mp3"; // Replace with the actual path to your mp3 file

           
            // Opening file input stream
            FileInputStream fileInputStream = new FileInputStream(filename);
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
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {if (scanner != null) {scanner.close();}}
    }
}
