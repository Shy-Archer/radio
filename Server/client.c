#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

#define PORT 12345
#define MAX_FILENAME_SIZE 256
#define MAX_BUFFER_SIZE 1024

int clientSocket;


void handleUpload(int socket) {
    char filename[MAX_FILENAME_SIZE];
    printf("Enter filename to upload: ");
    scanf("%s", filename);

    FILE *file = fopen(filename, "rb");
    if (file == NULL) {
        perror("File opening error");
        return;
    }

    send(socket, "upload", sizeof("upload"), 0);
    send(socket, filename, sizeof(filename), 0);

    char buffer[MAX_BUFFER_SIZE];
    size_t bytesRead;
    while ((bytesRead = fread(buffer, 1, sizeof(buffer), file)) > 0) {
        send(socket, buffer, bytesRead, 0);
    }

    fclose(file);
}

void handleSkip(int socket) {
    send(socket, "skip", sizeof("skip"), 0);
    printf("Sending skip \n ");

}

void handleViewQueue(int socket) {
    send(socket, "view", sizeof("view"), 0);

    char queueInfo[MAX_BUFFER_SIZE];
    memset(queueInfo, 0, sizeof(queueInfo));

    recv(socket, queueInfo, sizeof(queueInfo), 0);

    printf("Current queue:\n%s", queueInfo);
}

void handleQuit(int socket) {
    send(socket, "quit", sizeof("quit"), 0);

    close(clientSocket);
    printf("Disconnected from the server. Exiting...\n");
}

int main() {
    struct sockaddr_in serverAddr;

    clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket < 0) {
        perror("Socket creation error");
        exit(EXIT_FAILURE);
    }

    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(PORT);
    serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");

    if (connect(clientSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        perror("Connection error");
        exit(EXIT_FAILURE);
    }

    printf("Connected to the server.\n");

    while (1) {
        printf("Options:\n");
        printf("1. Upload a file\n");
        printf("2. Skip to the next file\n");
        printf("3. View current queue\n");
        printf("4. Quit\n");

        int choice;
        printf("Enter your choice: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                handleUpload(clientSocket);
                break;
            case 2:
                handleSkip(clientSocket);
                break;
            case 3:
                handleViewQueue(clientSocket);
                break;
            case 4:
                handleQuit(clientSocket);
                exit(EXIT_SUCCESS);
            default:
                printf("Invalid choice. Try again.\n");
        }
    }

    return 0;
}
