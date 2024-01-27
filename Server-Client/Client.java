import java.io.*;
import java.net.*;
import java.util.Scanner;


public class Client {
    private static final int PORT = 12345;
    // private static final int MAX_FILENAME_SIZE = 256;
    private static final int MAX_BUFFER_SIZE = 1024;

    private static Socket clientSocket;

    private static void handleUpload() {
        // Scanner scanner = new Scanner(System.in);

        try(Scanner scanner = new Scanner(System.in) ) {
            System.out.print("Enter filename to upload: ");
            String filename = scanner.nextLine();

            File file = new File(filename);
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            OutputStream outputStream = clientSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeUTF("upload");
            dataOutputStream.writeUTF(filename);

            byte[] buffer = new byte[MAX_BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = bufferedInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } //finally {scanner.close();}
    }

    private static void handleSkip() {
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeUTF("skip");
            System.out.println("Sending skip");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleViewQueue() {
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            dataOutputStream.writeUTF("view\n");

            InputStream inputStream = clientSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            String queueInfo = dataInputStream.readUTF();
            System.out.println("Current queue:\n" + queueInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleQuit() {
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

    public static void main(String[] args) {
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
                System.out.print("Enter your choice: " + scanner + "\n\n");
                int choice = scanner.nextInt();
                System.out.print("Enter your choice: " + scanner + "\n\n");
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
