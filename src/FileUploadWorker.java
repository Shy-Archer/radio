import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
public class FileUploadWorker extends SwingWorker<Void, Void> {
    private final String fullPath;
    private final String filename;
    private final Socket clientSocket;

    public FileUploadWorker(String fullPath, String filename, Socket clientSocket) {
        this.fullPath = fullPath;
        this.filename = filename;
        this.clientSocket = clientSocket;
    }

    @Override
    protected Void doInBackground() throws Exception {
        try {

            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            printWriter.write("upload");
            printWriter.flush();
            Thread.sleep(5000);




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
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
                System.out.println(bytesRead);

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

        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
