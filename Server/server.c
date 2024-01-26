#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

#define PORT 12345
#define MAX_FILENAME_SIZE 256
#define MAX_BUFFER_SIZE 1024
#define MAX_QUEUE_SIZE 10

typedef struct {
    char filename[MAX_FILENAME_SIZE];
} AudioFile;

typedef struct {
    AudioFile queue[MAX_QUEUE_SIZE];
    int front, rear;
} AudioQueue;

void initQueue(AudioQueue* audioQueue) {
    audioQueue->front = audioQueue->rear = -1;
}

int isQueueEmpty(AudioQueue* audioQueue) {
    return audioQueue->front == -1;
}

int isQueueFull(AudioQueue* audioQueue) {
    return (audioQueue->rear + 1) % MAX_QUEUE_SIZE == audioQueue->front;
}

void enqueue(AudioQueue* audioQueue, const char* filename) {
    if (isQueueFull(audioQueue)) {
        printf("Queue is full. Cannot enqueue.\n\n");
        return;
    }

    if (isQueueEmpty(audioQueue)) {
        audioQueue->front = audioQueue->rear = 0;
    } else {
        audioQueue->rear = (audioQueue->rear + 1) % MAX_QUEUE_SIZE;
    }

    strcpy(audioQueue->queue[audioQueue->rear].filename, filename);
}

void dequeue(AudioQueue* audioQueue) {
    printf("Enter dequeue \n");
    if (isQueueEmpty(audioQueue)) {
        printf("Queue is empty. Cannot dequeue.\n");
        return;
    }

    printf("Dequeued: %s\n", audioQueue->queue[audioQueue->front].filename);

    if (audioQueue->front == audioQueue->rear) {
        initQueue(audioQueue);
    } else {
        audioQueue->front = (audioQueue->front + 1) % MAX_QUEUE_SIZE;
    }
}

void printQueue(AudioQueue* audioQueue) {
    if (isQueueEmpty(audioQueue)) {
        printf("Queue is empty.\n\n");
        return;
    }

    printf("Current queue:\n");
    int i = audioQueue->front;
    do {
        printf("%s\n\n", audioQueue->queue[i].filename);
        i = (i + 1) % MAX_QUEUE_SIZE;
    } while (i != (audioQueue->rear + 1) % MAX_QUEUE_SIZE);
}



void handleUpload(int clientSocket, AudioQueue* audioQueue) {
    char filename[MAX_FILENAME_SIZE];
    recv(clientSocket, filename, sizeof(filename), 0);

    enqueue(audioQueue, filename);
    printf("File %s uploaded to the queue.\n", filename);
    bzero(filename,256);
}

void handleSkip(AudioQueue* audioQueue) {
    printf("Enter skip \n");
    dequeue(audioQueue);
}

void handleViewQueue(int clientSocket, AudioQueue* audioQueue) {
    printf("Enter view \n");
    printQueue(audioQueue);

    // Send the queue information back to the client
    char queueInfo[MAX_BUFFER_SIZE];
    memset(queueInfo, 0, sizeof(queueInfo));

    int i = audioQueue->front;
    do {
        strcat(queueInfo, audioQueue->queue[i].filename);
        strcat(queueInfo, "\n");
        i = (i + 1) % MAX_QUEUE_SIZE;
    } while (i != (audioQueue->rear + 1) % MAX_QUEUE_SIZE);

    send(clientSocket, queueInfo, sizeof(queueInfo), 0);
}


void handleClientRequest(int clientSocket, AudioQueue* audioQueue) {
    while(1){

        if (clientSocket < 0) {
            printf("Connection closed \n");
            break;
        }

        printf("Enter wait for request \n");
        char buffer[MAX_BUFFER_SIZE];
        bzero(buffer,1024);
        recv(clientSocket, buffer, sizeof(buffer), 0);

        if (strcmp(buffer, "upload") == 0) {
            handleUpload(clientSocket, audioQueue);
        } else if (strcmp(buffer, "skip") == 0) {
            handleSkip(audioQueue);
        } else if (strcmp(buffer, "view") == 0) {
            handleViewQueue(clientSocket, audioQueue);
        } else if (strcmp(buffer, "quit") == 0) {
            close(clientSocket);
            clientSocket = -1;
            printf("Client disconnected \n");
        } else {
            printf("Unknown command received.\n");
        }
    }
}

int main() {
    int serverSocket, clientSocket;
    struct sockaddr_in serverAddr, clientAddr;
    socklen_t addrLen = sizeof(clientAddr);

    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket < 0) {
        perror("Socket creation error");
        exit(EXIT_FAILURE);
    }

    memset(&serverAddr, 0, sizeof(serverAddr));
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    if (bind(serverSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) < 0) {
        perror("Binding error");
        exit(EXIT_FAILURE);
    }

    if (listen(serverSocket, 1) < 0) {
        perror("Listening error");
        exit(EXIT_FAILURE);
    }

    printf("Server listening on port %d...\n", PORT);

    AudioQueue audioQueue;
    initQueue(&audioQueue);

    while (1) {
        printf("Before accept\n");
        clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &addrLen);
        if (clientSocket < 0) {
            perror("Acceptance error");
            exit(EXIT_FAILURE);
        } else {
            printf("Client connected\n");
        }

        handleClientRequest(clientSocket, &audioQueue);

    }

    close(serverSocket);

    return 0;
}
