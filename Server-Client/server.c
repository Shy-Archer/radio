#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <signal.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <semaphore.h>

#define PORT 12345
#define MAX_FILENAME_SIZE 256
#define MAX_BUFFER_SIZE 1024
#define MAX_QUEUE_SIZE 10
#define SHM_KEY 5678

typedef struct {
    char filename[MAX_FILENAME_SIZE];

} AudioFile;

typedef struct {
    AudioFile queue[MAX_QUEUE_SIZE];
    int front, rear;
    sem_t mutex;  // Semaphore for mutual exclusion
} AudioQueue;

void initQueue(AudioQueue* audioQueue) {
    audioQueue->front = audioQueue->rear = -1;
    sem_init(&audioQueue->mutex, 1, 1);  // Initialize the semaphore with value 1
}

int isQueueEmpty(AudioQueue* audioQueue) {
    return audioQueue->front == -1;
}

int isQueueFull(AudioQueue* audioQueue) {
    return (audioQueue->rear + 1) % MAX_QUEUE_SIZE == audioQueue->front;
}

void enqueue(AudioQueue* audioQueue, const char* filename) {
    sem_wait(&audioQueue->mutex);  // Wait for the semaphore
    if (isQueueFull(audioQueue)) {
        printf("Queue is full. Cannot enqueue.\n\n");
        sem_post(&audioQueue->mutex);  // Release the semaphore
        return;
    }

    if (isQueueEmpty(audioQueue)) {
        audioQueue->front = audioQueue->rear = 0;
    } else {
        audioQueue->rear = (audioQueue->rear + 1) % MAX_QUEUE_SIZE;
    }

    strcpy(audioQueue->queue[audioQueue->rear].filename, filename);
    printf("Leave enqueue :%d\n", audioQueue->rear);
    sem_post(&audioQueue->mutex);  // Release the semaphore
}

void dequeue(AudioQueue* audioQueue) {
    sem_wait(&audioQueue->mutex);  // Wait for the semaphore
    printf("Enter dequeue \n");
    if (isQueueEmpty(audioQueue)) {
        printf("Queue is empty. Cannot dequeue.\n\n");
        sem_post(&audioQueue->mutex);  // Release the semaphore
        return;
    }

    printf("Dequeued: %s\n", audioQueue->queue[audioQueue->front].filename);

    // Move the dequeued element to the end of the queue
    if (audioQueue->front != audioQueue->rear) {
        audioQueue->rear = (audioQueue->rear + 1) % MAX_QUEUE_SIZE;
        strcpy(audioQueue->queue[audioQueue->rear].filename, audioQueue->queue[audioQueue->front].filename);
    }

    if (audioQueue->front == audioQueue->rear) {
        initQueue(audioQueue);
    } else {
        audioQueue->front = (audioQueue->front + 1) % MAX_QUEUE_SIZE;
    }

    sem_post(&audioQueue->mutex);  // Release the semaphore
}


void printQueue(AudioQueue* audioQueue) {
    sem_wait(&audioQueue->mutex);  // Wait for the semaphore
    if (isQueueEmpty(audioQueue)) {
        printf("Queue is empty.\n\n");
        sem_post(&audioQueue->mutex);  // Release the semaphore
        return;
    }

    printf("Current queue:\n");
    int i = audioQueue->front;
    do {
        printf("%s\n", audioQueue->queue[i].filename);
        i = (i + 1) % MAX_QUEUE_SIZE;
    } while (i != (audioQueue->rear + 1) % MAX_QUEUE_SIZE);

    sem_post(&audioQueue->mutex);  // Release the semaphore
}

void handleUpload_old(int clientSocket, AudioQueue* audioQueue) {
    char filename[MAX_FILENAME_SIZE];
    bzero(filename, sizeof(filename));
    recv(clientSocket, filename, sizeof(filename), 0);
    printf("%s \n", filename);
    enqueue(audioQueue, filename);
    printf("File %s uploaded to the queue.\n\n", filename);
    bzero(filename, sizeof(filename));
    printf("Enter skip \n");
}

// ... (wcześniejszy kod serwera pozostaje niezmieniony)

void handleUpload(int clientSocket, AudioQueue* audioQueue) {
    char filename[MAX_FILENAME_SIZE];
    bzero(filename, sizeof(filename));

    // Odbieranie nazwy pliku od klienta
    if (recv(clientSocket, filename, sizeof(filename), 0) <= 0) {
        perror("Error receiving filename from client");
        return;
    }

    printf("Received filename: %s\n", filename);

    char filepath[MAX_FILENAME_SIZE + 50];  // Dodatkowe miejsce na ścieżkę
    snprintf(filepath, sizeof(filepath), "/home/czarka/IdeaProjects/radio/Server-Client/server_queue/%s", filename);

    // Odbieranie zawartości pliku od klienta
    FILE* file = fopen(filepath, "wb");
    if (file == NULL) {
        perror("Error opening file for writing");
        return;
    }

    const char* fileDataSignal = "START_FILE_DATA";
    if (send(clientSocket, fileDataSignal, strlen(fileDataSignal), 0) < 0) {
        perror("Error sending file data signal");
        fclose(file);
        return;
    }

    size_t totalBytesReceived = 0;
    char buffer[MAX_BUFFER_SIZE];
    char* endOfFileSignal = "END_OF_FILE";
    size_t endOfFileSignalLength = strlen(endOfFileSignal);

    while (1) {
        ssize_t bytesRead = recv(clientSocket, buffer, sizeof(buffer), 0);

        if (bytesRead <= 0) {
            // Błąd lub zakończenie połączenia przez klienta
            break;
        }

        // Sprawdzenie, czy na końcu bufora jest sygnał "END_OF_FILE"
        if (bytesRead >= endOfFileSignalLength &&
            memcmp(buffer + bytesRead - endOfFileSignalLength, endOfFileSignal, endOfFileSignalLength) == 0) {
            printf("Received END_OF_FILE signal. Transmission completed.\n");
            // Usunięcie sygnału "END_OF_FILE" z danych zapisywanych do pliku
            totalBytesReceived -= endOfFileSignalLength;
            break;
        }

        // Zapisywanie danych do pliku
        fwrite(buffer, 1, bytesRead, file);
        totalBytesReceived += bytesRead;
        printf("Total bytes received: %ld\n", totalBytesReceived);
        if (totalBytesReceived >= 53000000) {
            // Limit wielkości pliku osiągnięty
            break;
        }
    }
            
    
    fclose(file);
    bzero(buffer, sizeof(buffer));
    printf("Leave saving file\n");


    

    // Dodawanie pliku do kolejki
    enqueue(audioQueue, filename);

    printf("File %s uploaded to the queue.\n\n", filename);

    int num = 0;
    send(clientSocket, &num, sizeof(num), 0);
}



void handleSkip(AudioQueue* audioQueue) {
    printf("Enter skip \n");
    dequeue(audioQueue);
}

void handleViewQueue(int clientSocket, AudioQueue* audioQueue) {
    printf("Enter view \n");
    printQueue(audioQueue);

    // Przygotowanie informacji o kolejce
    char queueInfo[MAX_BUFFER_SIZE];
    bzero(queueInfo, sizeof(queueInfo));
    int i = audioQueue->front;
    do {
        strcat(queueInfo, audioQueue->queue[i].filename);
        strcat(queueInfo, "\n");
        i = (i + 1) % MAX_QUEUE_SIZE;
    } while (i != (audioQueue->rear + 1) % MAX_QUEUE_SIZE);
    printf("i:%d, rear:%d, front:%d\n", i, audioQueue->rear + 1, audioQueue->front);
    int qlen = audioQueue->rear + 1 - audioQueue->front;
    printf("qlen:%d, %lu, %lu\n", qlen, sizeof(qlen), sizeof(queueInfo));
    send(clientSocket, &qlen, sizeof(qlen), 0);
    sleep(1);
    // Wysłanie informacji o kolejce do klienta
    send(clientSocket, queueInfo, sizeof(queueInfo), 0);
    // Opcjonalnie: Wysłanie pustej linii, aby oznaczyć koniec informacji
    send(clientSocket, "\n", 1, 0);
    i = 0;
    qlen = 0;
    bzero(queueInfo, sizeof(queueInfo));
}
void sendFileFromQueue(int clientSocket, AudioQueue* audioQueue) {
    char filename[MAX_FILENAME_SIZE];
    bzero(filename, sizeof(filename));

    // Sprawdzenie, czy kolejka jest pusta
    if (isQueueEmpty(audioQueue)) {
        const char* noFilesSignal = "NO_FILES";
        send(clientSocket, noFilesSignal, strlen(noFilesSignal), 0);
        return;
    }

    // Pobranie nazwy pliku z kolejki
    strcpy(filename, audioQueue->queue[audioQueue->front].filename);

    // Przygotowanie ścieżki do pliku
    char filepath[MAX_FILENAME_SIZE + 50];  // Dodatkowe miejsce na ścieżkę
    snprintf(filepath, sizeof(filepath), "/home/czarka/IdeaProjects/radio/Server-Client/server_queue/%s", filename);

    // Otwarcie pliku do odczytu
    FILE* file = fopen(filepath, "rb");
    if (file == NULL) {
        perror("Error opening file for reading");
        return;
    }

    // Wysłanie nazwy pliku do klienta
    send(clientSocket, filename, sizeof(filename), 0);

    // Wysłanie sygnału "START_FILE_DATA"
    const char* fileDataSignal = "START_FILE_DATA";
    send(clientSocket, fileDataSignal, strlen(fileDataSignal), 0);

    // Wysyłanie zawartości pliku do klienta
    char buffer[MAX_BUFFER_SIZE];
    size_t bytesRead;
    while ((bytesRead = fread(buffer, 1, sizeof(buffer), file)) > 0) {
        send(clientSocket, buffer, bytesRead, 0);
    }

    // Wysłanie sygnału "END_OF_FILE" jako znacznika zakończenia przesyłania pliku
    const char* endOfFileSignal = "END_OF_FILE";
    send(clientSocket, endOfFileSignal, strlen(endOfFileSignal), 0);

    fclose(file);
    printf("File %s sent to the client.\n", filename);

    // Usunięcie pliku z kolejki po wysłaniu
    dequeue(audioQueue);
}

void handleClientRequest(int clientSocket, AudioQueue* audioQueue) {
    while (1) {
        if (clientSocket < 0) {
            printf("Connection closed \n\n");
            break;
        }

        printf("Enter wait for request \n");
        char buffer[MAX_BUFFER_SIZE];
        bzero(buffer, sizeof(buffer));
        // recv(clientSocket, buffer, sizeof(buffer), 0);

        // Obsługa sygnału SIGPIPE
        signal(SIGPIPE, SIG_IGN);

        ssize_t bytesRead = recv(clientSocket, buffer, sizeof(buffer), 0);
        if (bytesRead <= 0) {
            // Rozłączono klienta, bytesRead <= 0 oznacza błąd lub zamknięcie gniazda przez klient
            printf("Client disconnected \n\n");
            close(clientSocket);
            break;
        }
        buffer[bytesRead] = '\0';
        printf("Buffer: %s\n", buffer);
        if (strcmp(buffer, "upload") == 0) {
            handleUpload(clientSocket, audioQueue);
        } else if (strcmp(buffer, "skip") == 0) {
            handleSkip(audioQueue);
        } else if (strncmp(buffer, "view", 4) == 0) {
            handleViewQueue(clientSocket, audioQueue);
        } else if (strcmp(buffer, "quit") == 0) {
            close(clientSocket);
            clientSocket = -1;
            printf("Client disconnected \n\n");
            break;  // Exit the loop when the client disconnects

        } 
        else if (strcmp(buffer, "download_and_delete") == 0) {
            sendFileFromQueue(clientSocket, audioQueue);}
        else {
            printf("Unknown command received.\n\n");
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

    // Ustawienie opcji SO_REUSEADDR
    int enable = 1;
    if (setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int)) < 0) {
        perror("Setsockopt error");
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

    if (listen(serverSocket, 10) < 0) {  // Increased backlog to allow multiple pending connections
        perror("Listening error");
        exit(EXIT_FAILURE);
    }

    printf("Server listening on port %d...\n", PORT);

    // Tworzenie i przyłączanie pamięci współdzielonej
    int shmid = shmget(SHM_KEY, sizeof(AudioQueue), IPC_CREAT | 0666);
    if (shmid < 0) {
        perror("shmget error");
        exit(EXIT_FAILURE);
    }

    AudioQueue* sharedQueue = (AudioQueue*)shmat(shmid, NULL, 0);
    if (sharedQueue == (AudioQueue*)-1) {
        perror("shmat error");
        exit(EXIT_FAILURE);
    }

    initQueue(sharedQueue);

    while (1) {
        printf("Before accept\n");
        clientSocket = accept(serverSocket, (struct sockaddr*)&clientAddr, &addrLen);
        if (clientSocket < 0) {
            perror("Acceptance error");
            continue;  // Continue to the next iteration to handle other clients
        } else {
            printf("Client connected\n");
        }

        pid_t pid = fork();  // Create a new process for each client

        if (pid < 0) {
            perror("Forking error");
            exit(EXIT_FAILURE);
        } else if (pid == 0) {  // Child process
            close(serverSocket);  // Close the server socket in the child process
            handleClientRequest(clientSocket, sharedQueue);
            exit(EXIT_SUCCESS);  // Exit the child process
        } else {  // Parent process
            close(clientSocket);  // Close the client socket in the parent process
            waitpid(-1, NULL, WNOHANG);  // Non-blocking wait for any terminated child process
        }
    }

    // Usunięcie pamięci współdzielonej
    shmdt(sharedQueue);
    shmctl(shmid, IPC_RMID, NULL);

    close(serverSocket);

    return 0;
}
