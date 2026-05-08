#include <stdio.h>      // Para printf, scanf
#include <stdlib.h>     // Para malloc, free
#include <string.h>     // Para strcpy, strncpy
#include <unistd.h>     // Para close, read, write
#include <arpa/inet.h>  // Para socket, connect, sendto, recvfrom
#include <sys/socket.h> // Para socket, bind, listen, accept
#include <pthread.h>    // Para threads
#include <termios.h>    // Para terminal em modo raw
#include <stdint.h>     // Para tipos inteiros fixos
#include <sys/ioctl.h>  // Para obter o tamanho do terminal
#include <signal.h>     // Para tratamento de sinais
#include <sys/time.h>   // Para manipulação de tempo
#include <sys/select.h> // Para select

void process_sendfile_command(const char *cmd);
void *file_send_server(void *arg);

#define SERVER_IP "127.0.0.1"
#define PORT 12345
#define BUFFER_SIZE 1024
#define MAX_GROUPS 20
#define MAX_DMS 10

// NOVOS TIPOS DE MENSAGEM
#define MSG_AUTH 10
#define MSG_AUTH_OK 11
#define MSG_AUTH_FAIL 12
#define MSG_CREATE 13
#define MSG_CREATE_OK 14
#define MSG_DM 15
#define MSG_ALL 16
#define MSG_GROUPS_OK 17
#define MSG_GROUPS_FAIL 18
#define MSG_GROUPS_CREATE 19
#define MSG_GROUPS_ADD 20
#define MSG_GROUPS_REMOVE 21
#define MSG_GROUPS_DELETE 22
#define MSG_GROUPS_CHECK 23
#define MSG_FILE_OFFER 24

typedef struct
{
    uint8_t type;
    char from[50];
    char data[BUFFER_SIZE];
    char to[20];
    char group[20];
} Message;

struct
{
    char user[50];
    char buffer[BUFFER_SIZE];
} dm_buffers[MAX_DMS]; // até 10 DMs live

struct
{
    char name[50];
    char buffer[BUFFER_SIZE];
} group_buffers[MAX_GROUPS]; // até 20 grupos live



int sockfd;
struct sockaddr_in servaddr;

char username[50];
char password[50];

int running = 1;
int num_dms = 0;           // Número de DMs ativas
int num_group_buffers = 0; // Número de buffers de grupo ativos

char current_channel[10] = "all"; // "all" ou "dm"
char current_group[50] = "";      // nome do grupo atual
char dm_target[50] = "";          // username do alvo DM


// Guarda o estado dos chats recebidos
char input_buffer[BUFFER_SIZE]; // buffer do input do usuário
char all_buffer[BUFFER_SIZE] = ""; // buffer para chat all
char group_buffer[BUFFER_SIZE] = ""; // buffer para chat de grupo
char notification_buffer[BUFFER_SIZE] = ""; // buffer para notificações
int buffer_index = 0; // Índice do buffer de entrada


// cabeçalho para funções de manipulação de terminal
void redraw_prompt();
void redraw_chat_live(void);

// Limpa as linhas de entrada do terminal
void clear_input_lines_len(int prev_len)
{
    struct winsize w;
    ioctl(STDOUT_FILENO, TIOCGWINSZ, &w);
    int width = w.ws_col > 0 ? w.ws_col : 80;
    int len = prev_len + 2; // +2 para "> "
    int lines = (len + width - 1) / width;

    for (int i = 0; i < lines; i++)
    {
        printf("\r\033[K");
        if (i < lines - 1)
            printf("\033[A");
    }
    printf("\r");
}

void process_command(const char *cmd)
{
    if (strncmp(cmd, "/sendfile", 9) == 0)
    {
        process_sendfile_command(cmd);
        return;
    }
    if (strncmp(cmd, "/all", 4) == 0)
    {
        strcpy(current_channel, "all");
        dm_target[0] = '\0';
        memset(input_buffer, 0, BUFFER_SIZE); // Limpa buffer ao trocar de canal
        buffer_index = 0;
        printf("\n* Mudou para o chat [all]\n");
    }
    else if (strncmp(cmd, "/dm ", 4) == 0)
    {
        const char *target = cmd + 4;
        if (strlen(target) > 0 && strcmp(target, username) != 0)
        {
            strcpy(current_channel, "dm");
            strncpy(dm_target, target, sizeof(dm_target) - 1);
            dm_target[sizeof(dm_target) - 1] = '\0';
            memset(input_buffer, 0, BUFFER_SIZE); // Limpa buffer ao trocar de canal
            buffer_index = 0;
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Mudou para DM com [%s]\n", dm_target);
        }
        else
        {
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Não pode enviar DM para si mesmo!\n");
        }
    }
    else if (strncmp(cmd, "/group create ", 14) == 0)
    {
        // Cria grupo
        Message msg = {.type = MSG_GROUPS_CREATE};
        strcpy(msg.from, username);
        strcpy(msg.group, cmd + 14); // Nome do grupo
        msg.data[0] = '\0';
        sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Pedido para criar grupo enviado\n");
    }
    else if (strncmp(cmd, "/group add ", 11) == 0)
    {
        // Adiciona membro ao grupo atual
        if (strlen(current_group) == 0)
        {
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Selecione primeiro um grupo com /group (nome)\n");
            return;
        }
        Message msg = {.type = MSG_GROUPS_ADD};
        strcpy(msg.from, username);
        strcpy(msg.group, current_group);
        strcpy(msg.to, cmd + 11);
        msg.data[0] = '\0';
        sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Pedido para adicionar membro enviado\n");
    }
    else if (strncmp(cmd, "/group remove ", 14) == 0)
    {
        // Remove membro do grupo atual
        if (strlen(current_group) == 0)
        {
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Selecione primeiro um grupo com /group (nome)\n");
            return;
        }
        Message msg = {.type = MSG_GROUPS_REMOVE};
        strcpy(msg.from, username);
        strcpy(msg.group, current_group);
        strcpy(msg.to, cmd + 14);
        msg.data[0] = '\0';
        sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
    }
    else if (strncmp(cmd, "/group delete", 13) == 0)
    {
        // Apaga grupo
        const char *group_name = NULL;
        if (strlen(cmd) > 14) // "/group delete " + nome
            group_name = cmd + 14;
        else if (strlen(current_group) > 0)
            group_name = current_group;
        else
            group_name = NULL;

        if (group_name && strlen(group_name) > 0)
        {
            Message msg = {.type = MSG_GROUPS_DELETE};
            strcpy(msg.from, username);
            strcpy(msg.group, group_name);
            msg.data[0] = '\0';
            sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Pedido para apagar grupo enviado\n");
        }
        else
        {
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Nenhum grupo selecionado para apagar!\n");
        }
    }
    else if (strncmp(cmd, "/group ", 7) == 0)
    {
        // Seleciona grupo para chat
        const char *gname = cmd + 7;
        if (strlen(gname) > 0)
        {
            // Envia pedido de verificação ao servidor
            Message msg = {.type = MSG_GROUPS_CHECK};
            strcpy(msg.from, username);
            strcpy(msg.group, gname);
            msg.data[0] = '\0';
            sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
            snprintf(notification_buffer, sizeof(notification_buffer), "\n* Pedido para entrar no grupo [%s] enviado. Aguarde confirmação...\n", gname);
        }
    }
    redraw_prompt();
    redraw_chat_live();
}

void set_terminal_mode(int enable_raw)
{
    static struct termios oldt;
    if (enable_raw)
    {
        tcgetattr(STDIN_FILENO, &oldt);
        struct termios newt = oldt;
        newt.c_lflag &= ~(ICANON | ECHO);
        tcsetattr(STDIN_FILENO, TCSANOW, &newt);
    }
    else
    {
        tcsetattr(STDIN_FILENO, TCSANOW, &oldt);
    }
}

int autenticar_ou_criar()
{
    int escolha;
    char op[10];
    while (1)
    {
        printf("1 - Criar conta\n2 - Entrar\nEscolha: ");
        fgets(op, sizeof(op), stdin);
        escolha = atoi(op);
        if (escolha == 1 || escolha == 2)
            break;
    }
    printf("Nome de utilizador: ");
    fgets(username, sizeof(username), stdin);
    username[strcspn(username, "\n")] = '\0';
    printf("Palavra-passe: ");
    fgets(password, sizeof(password), stdin);
    password[strcspn(password, "\n")] = '\0';

    Message msg;
    if (escolha == 1)
    {
        msg.type = MSG_CREATE;
    }
    else
    {
        msg.type = MSG_AUTH;
    }
    strcpy(msg.from, username);
    strcpy(msg.data, password);

    sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));

    // Espera resposta
    Message resp;
    recvfrom(sockfd, &resp, sizeof(Message), 0, NULL, NULL);

    if (escolha == 1)
    {
        if (resp.type == MSG_CREATE_OK)
        {
            printf("Conta criada com sucesso!\n");
            return 1;
        }
        else
        {
            printf("Erro ao criar conta (usuario já existe)\n");
            return 0;
        }
    }
    else
    {
        if (resp.type == MSG_AUTH_OK)
        {
            printf("Login bem-sucedido!\n");
            return 1;
        }
        else
        {
            printf("Login falhou! Verifique username/palavra-passe.\n");
            return 0;
        }
    }
}

void send_farewell()
{
    Message leave_msg = {.type = 3};
    strcpy(leave_msg.from, username);
    sendto(sockfd, &leave_msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
}

void handle_sigint(int sig)
{
    running = 0;
    send_farewell();
    set_terminal_mode(0);
    close(sockfd);
    exit(0);
}

void redraw_prompt()
{
    if (strcmp(current_channel, "all") == 0)
        printf("\033[1;34m[all] %s: %s\033[0m", username, input_buffer);
    else if (strcmp(current_channel, "dm") == 0)
        printf("\033[1;34m[dm] %s: %s\033[0m", username, input_buffer);
    else if (strcmp(current_channel, "group") == 0)
        printf("\033[1;34m[group:%s] %s: %s\033[0m", current_group, username, input_buffer);
    fflush(stdout);
}

// Atualiza o terminal mostrando o estado dos chats
void redraw_chat_live()
{
    printf("\033[H\033[J"); // Limpa o terminal

    if (notification_buffer[0] != '\0')
        printf("\033[1;33m%s\033[0m\n", notification_buffer); // Amarelo

    // [all] em verde
    printf("\033[1;32m[all]\033[0m\n%s\n", all_buffer);

    // [dm] em azul
    for (int i = 0; i < num_dms; i++)
    {
        printf("\033[1;34m[dm] %s:\033[0m\n%s\n", dm_buffers[i].user, dm_buffers[i].buffer);
    }

    // [group] em magenta
    if (strcmp(current_channel, "group") == 0)
        printf("\033[1;35m[group:%s]\033[0m\n%s", current_group, group_buffer);

    // Prompt colorido conforme o canal
    if (strcmp(current_channel, "all") == 0)
        printf("\033[1;32m[all] %s: \033[0m%s", username, input_buffer);
    else if (strcmp(current_channel, "dm") == 0)
        printf("\033[1;34m[dm] %s: \033[0m%s", username, input_buffer);
    else if (strcmp(current_channel, "group") == 0)
        printf("\033[1;35m[group:%s] %s: \033[0m%s", current_group, username, input_buffer);

    fflush(stdout);
}

int find_group_buffer(const char *group_name)
{
    for (int i = 0; i < num_group_buffers; i++)
    {
        if (strcmp(group_buffers[i].name, group_name) == 0)
            return i;
    }
    if (num_group_buffers < MAX_GROUPS)
    {
        strncpy(group_buffers[num_group_buffers].name, group_name, 49);
        group_buffers[num_group_buffers].name[49] = '\0';
        group_buffers[num_group_buffers].buffer[0] = '\0';
        return num_group_buffers++;
    }
    return -1;
}

// Função para receber ficheiro por TCP
void file_receive_client(const char *ip, int port, const char *filename, size_t filesize)
{
    int sock = 0;
    struct sockaddr_in serv_addr;
    char buffer[1024];
    FILE *fp = fopen(filename, "wb");
    if (!fp)
    {
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Erro ao criar ficheiro para receber\n");
        return;
    }
    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Erro ao criar socket TCP\n");
        fclose(fp);
        return;
    }
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(port);
    inet_pton(AF_INET, ip, &serv_addr.sin_addr);
    if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
    {
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Erro ao conectar ao servidor de ficheiros\n");
        close(sock);
        fclose(fp);
        return;
    }
    size_t received = 0;
    while (received < filesize)
    {
        ssize_t n = recv(sock, buffer, sizeof(buffer), 0);
        if (n <= 0)
            break;
        fwrite(buffer, 1, n, fp);
        received += n;
    }
    snprintf(notification_buffer, sizeof(notification_buffer), "\n* Ficheiro '%s' recebido (%zu bytes)\n", filename, received);
    close(sock);
    fclose(fp);
}

// Comando para enviar ficheiro
void process_sendfile_command(const char *cmd)
{
    char filename[256], target[50] = "all";
    if (sscanf(cmd, "/sendfile %255s %49s", filename, target) < 1)
    {
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Use: /sendfile <ficheiro> [destino]\n");
        return;
    }
    FILE *fp = fopen(filename, "rb");
    if (!fp)
    {
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Ficheiro não encontrado\n");
        return;
    }
    fseek(fp, 0, SEEK_END);
    size_t filesize = ftell(fp);
    fclose(fp);
    // Escolhe porta aleatória entre 20000-21000
    int port = 20000 + rand() % 1000;
    // Envia aviso UDP
    Message msg = {.type = MSG_FILE_OFFER};
    strcpy(msg.from, username);
    strcpy(msg.to, target);
    snprintf(msg.data, sizeof(msg.data), "%s|%d|%zu", filename, port, filesize);
    sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
    // Inicia thread servidor TCP
    pthread_t t;
    struct file_send_args
    {
        char filename[256];
        int port;
    } *args = malloc(sizeof(struct file_send_args));
    strncpy(args->filename, filename, 255);
    args->port = port;
    pthread_create(&t, NULL, file_send_server, args);
    pthread_detach(t);
    snprintf(notification_buffer, sizeof(notification_buffer), "\n* Oferta de ficheiro enviada para %s\n", target);
}

void *file_send_server(void *arg)
{
    struct file_send_args
    {
        char filename[256];
        int port;
    } *args = arg;
    int server_fd, new_socket;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);
    char buffer[1024];
    FILE *fp = fopen(args->filename, "rb");
    if (!fp)
    {
        snprintf(notification_buffer, sizeof(notification_buffer), "\n* Erro ao abrir ficheiro para envio\n");
        free(args);
        return NULL;
    }
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0)
    {
        perror("socket failed");
        fclose(fp);
        free(args);
        return NULL;
    }
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(args->port);
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0)
    {
        perror("bind failed");
        close(server_fd);
        fclose(fp);
        free(args);
        return NULL;
    }
    listen(server_fd, 5);
    snprintf(notification_buffer, sizeof(notification_buffer), "\n* Servidor de ficheiros ativo na porta %d. A aguardar ligações...\n", args->port);
    time_t start = time(NULL);
    while (time(NULL) - start < 30)
    { // 30 segundos para aceitar ligações
        fd_set fds;
        struct timeval tv = {1, 0};
        FD_ZERO(&fds);
        FD_SET(server_fd, &fds);
        int rv = select(server_fd + 1, &fds, NULL, NULL, &tv);
        if (rv > 0 && FD_ISSET(server_fd, &fds))
        {
            new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t *)&addrlen);
            if (new_socket >= 0)
            {
                fseek(fp, 0, SEEK_SET);
                size_t nread;
                while ((nread = fread(buffer, 1, sizeof(buffer), fp)) > 0)
                {
                    send(new_socket, buffer, nread, 0);
                }
                close(new_socket);
                snprintf(notification_buffer, sizeof(notification_buffer), "\n* Ficheiro enviado a um destinatário\n");
            }
        }
    }
    close(server_fd);
    fclose(fp);
    snprintf(notification_buffer, sizeof(notification_buffer), "\n* Servidor de ficheiros fechado\n");
    free(args);
    return NULL;
}

void *receiver(void *arg)
{
    Message msg;
    while (running)
    {
        int n = recvfrom(sockfd, &msg, sizeof(Message), 0, NULL, NULL);
        if (n > 0)
        {
            if (msg.type == MSG_FILE_OFFER)
            {
                // msg.data: filename|port|filesize
                char filename[256];
                int port = 0;
                size_t filesize = 0;
                sscanf(msg.data, "%255[^|]|%d|%zu", filename, &port, &filesize);
                printf("\n* %s quer enviar-lhe o ficheiro '%s' (%zu bytes). Aceitar? (s/n):\033[0m", msg.from, filename, filesize);
                fflush(stdout);
                char resp = getchar();
                if (resp == 's' || resp == 'S')
                {
                    file_receive_client(SERVER_IP, port, filename, filesize);
                }
                else
                {
                    printf("\n\033[1;33m* Transferência recusada.\033[0m\n");
                }
                while (getchar() != '\n')
                    ;
            }
            else if (msg.type == MSG_GROUPS_OK)
            {
                if (strlen(msg.data) > 0)
                {
                    strcpy(current_channel, "group");
                    strncpy(current_group, msg.data, sizeof(current_group) - 1);
                    current_group[sizeof(current_group) - 1] = '\0';
                    memset(input_buffer, 0, BUFFER_SIZE);
                    buffer_index = 0;
                    snprintf(notification_buffer, sizeof(notification_buffer), "* Mudou para o grupo [%s]", current_group);
                    // Atualize o buffer do grupo atual
                    int gi = find_group_buffer(current_group);
                    if (gi >= 0)
                    {
                        strncpy(group_buffer, group_buffers[gi].buffer, BUFFER_SIZE - 1);
                        group_buffer[BUFFER_SIZE - 1] = '\0';
                    }
                    else
                    {
                        group_buffer[0] = '\0';
                    }
                }
                else
                {
                    snprintf(notification_buffer, sizeof(notification_buffer), "* Operação de grupo realizada com sucesso!");
                }
            }
            else if (msg.type == MSG_GROUPS_FAIL)
            {
                if (strlen(msg.data) > 0)
                {
                    snprintf(notification_buffer, sizeof(notification_buffer), "* Erro na operação de grupo!");
                }
                else
                {
                    snprintf(notification_buffer, sizeof(notification_buffer), "* Grupo não existe ou você não é membro!");
                }
            }
            else if (msg.type == MSG_ALL && strlen(msg.group) > 0)
            {
                int gi = find_group_buffer(msg.group);
                if (gi >= 0)
                {
                    strncpy(group_buffers[gi].buffer, msg.data, BUFFER_SIZE - 1);
                    group_buffers[gi].buffer[BUFFER_SIZE - 1] = '\0';
                    // Atualize o chat live se estiver neste grupo
                    if (strcmp(current_channel, "group") == 0 && strcmp(current_group, msg.group) == 0)
                    {
                        strncpy(group_buffer, msg.data, BUFFER_SIZE - 1);
                        group_buffer[BUFFER_SIZE - 1] = '\0';
                        redraw_chat_live();
                    }
                }
            }
            else if (msg.type == MSG_ALL)
            {
                strncpy(all_buffer, msg.data, BUFFER_SIZE - 1);
                all_buffer[BUFFER_SIZE - 1] = '\0';
            }
            else if (msg.type == MSG_DM)
            {
                // Procura DM pelo remetente
                int found = 0;
                for (int i = 0; i < num_dms; i++)
                {
                    if (strcmp(dm_buffers[i].user, msg.from) == 0)
                    {
                        strncpy(dm_buffers[i].buffer, msg.data, BUFFER_SIZE - 1);
                        dm_buffers[i].buffer[BUFFER_SIZE - 1] = '\0';
                        found = 1;
                        break;
                    }
                }
                if (!found && num_dms < 10)
                {
                    strncpy(dm_buffers[num_dms].user, msg.from, 49);
                    strncpy(dm_buffers[num_dms].buffer, msg.data, BUFFER_SIZE - 1);
                    dm_buffers[num_dms].buffer[BUFFER_SIZE - 1] = '\0';
                    num_dms++;
                }
            }
            else if (msg.type == 1)
            {
                snprintf(notification_buffer, sizeof(notification_buffer), "* %s entrou no servidor", msg.from);
            }
            else if (msg.type == 3)
            {
                snprintf(notification_buffer, sizeof(notification_buffer), "* %s saiu da sala", msg.from);
            }
            else if (msg.type == MSG_AUTH_FAIL && strlen(msg.data) > 0)
            {
                printf("\n* Erro: %s\n", msg.data);
            }
            else if (msg.type == MSG_GROUPS_DELETE)
            {
                // Grupo foi apagado, volta ao chat all
                if (strcmp(current_channel, "group") == 0 && strcmp(current_group, msg.group) == 0)
                {
                    strcpy(current_channel, "all");
                    current_group[0] = '\0';
                    snprintf(notification_buffer, sizeof(notification_buffer), "* O grupo [%s] foi apagado. Voltou ao chat [all].", msg.group);
                    memset(input_buffer, 0, BUFFER_SIZE);
                    buffer_index = 0;
                }
            }
            else if (msg.type == MSG_GROUPS_REMOVE)
            {
                // Foi removido do grupo, volta ao chat all
                if (strcmp(current_channel, "group") == 0 && strcmp(current_group, msg.group) == 0)
                {
                    strcpy(current_channel, "all");
                    current_group[0] = '\0';
                    snprintf(notification_buffer, sizeof(notification_buffer), "* Foi removido do grupo [%s]. Voltou ao chat [all].", msg.group);
                    memset(input_buffer, 0, BUFFER_SIZE);
                    buffer_index = 0;
                }
            }
            redraw_chat_live();
        }
    }
    return NULL;
}

int main()
{
    sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(PORT);
    inet_pton(AF_INET, SERVER_IP, &servaddr.sin_addr);

    // NOVO: Autenticação/criação de conta
    while (!autenticar_ou_criar())
        ;

    Message join_msg = {.type = 1};
    strcpy(join_msg.from, username);
    sendto(sockfd, &join_msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));

    pthread_t thread;
    pthread_create(&thread, NULL, receiver, NULL);

    set_terminal_mode(1);
    printf("> ");
    fflush(stdout);

    signal(SIGINT, handle_sigint);

    while (running)
    {
        char c = getchar();
        int prev_len = strlen(input_buffer);

        if (c == '\n' || c == '\r')
        {
            if (buffer_index > 0 && input_buffer[0] == '/')
            {
                // Limpa notificação ao executar comando
                if (notification_buffer[0] != '\0')
                    notification_buffer[0] = '\0';

                process_command(input_buffer);
                clear_input_lines_len(prev_len);
                memset(input_buffer, 0, BUFFER_SIZE);
                buffer_index = 0;
                redraw_prompt();
            }
            continue;
        }
        else if (c == 127 || c == 8)
        { // Backspace/Delete
            if (buffer_index > 0)
            {
                input_buffer[--buffer_index] = '\0';
            }
        }
        else if (c >= 32 && c < 127 && buffer_index < BUFFER_SIZE - 1)
        { // Caracteres imprimíveis
            input_buffer[buffer_index++] = c;
            input_buffer[buffer_index] = '\0';
        }

        clear_input_lines_len(prev_len);
        redraw_chat_live();

        // Envia o buffer live (exceto se for comando ou DM para si mesmo)
        if (input_buffer[0] != '/')
        {
            Message msg;
            strcpy(msg.from, username);
            strcpy(msg.data, input_buffer);
            if (strcmp(current_channel, "all") == 0)
            {
                msg.type = MSG_ALL;
                msg.group[0] = '\0';
                sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
            }
            else if (strcmp(current_channel, "dm") == 0 && strlen(dm_target) > 0)
            {
                msg.type = MSG_DM;
                strncpy(msg.to, dm_target, sizeof(msg.to) - 1);
                msg.to[sizeof(msg.to) - 1] = '\0';
                sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
            }
            else if (strcmp(current_channel, "group") == 0 && strlen(current_group) > 0)
            {
                msg.type = MSG_ALL;
                strncpy(msg.group, current_group, sizeof(msg.group) - 1);
                msg.group[sizeof(msg.group) - 1] = '\0';
                sendto(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&servaddr, sizeof(servaddr));
            }
        }
    }

    // Envia mensagem de saída
    send_farewell();

    set_terminal_mode(0);
    close(sockfd);
    pthread_join(thread, NULL);
    return 0;
}