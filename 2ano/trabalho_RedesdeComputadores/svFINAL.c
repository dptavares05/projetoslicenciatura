#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <time.h>
#include <stdint.h> // For uint8_t
#include <stdbool.h>

#define PORT 12345
#define BUFFER_SIZE 1024
#define CONTAS_FILE "contas.txt"
#define GROUPS_FILE "groups.txt"
#define CLIENT_TIMEOUT 60 // segundos para timeout
#define MAX_CLIENTS 100
#define MAX_GROUPS 50
#define MAX_GROUP_MEMBERS 20

// tipos de mensagem para autenticação
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
// struct para o cliente
typedef struct
{
    char username[50];          // Nome do cliente
    struct sockaddr_in addr;    // Endereço do cliente
    time_t last_activity;       // Última atividade do cliente
    char text_buffer[BUFFER_SIZE]; // Buffer para armazenar texto recebido
} Client;

typedef struct
{
    uint8_t type;           // 1: char, 2: comando (/)
    char from[50];          // Nome do cliente
    char data[BUFFER_SIZE]; // Dados da mensagem
    char to[20];            // Destinatário
    char group[20];         // Nome do grupo
} Message;

typedef struct
{
    char name[50];                         // Nome do grupo
    char owner[50];                        // Dono do grupo
    char members[MAX_GROUP_MEMBERS][50];   // Membros do grupo
    int num_members;                       // Número de membros no grupo
    char member_buffers[MAX_GROUP_MEMBERS][BUFFER_SIZE]; // texto atual de cada membro numa matriz
} Group;

Message msg;                // Mensagem para comunicação
Client clients[MAX_CLIENTS];// Lista de clientes conectados
int num_clients = 0;        // Número de clientes conectados ao servidor
int sockfd;                 // Corrected sockfd_t to int

char all_buffers[MAX_CLIENTS][BUFFER_SIZE];             // Texto de cada cliente no [all]
char dm_buffers[MAX_CLIENTS][MAX_CLIENTS][BUFFER_SIZE]; // Texto de cada DM: [remetente][destinatario]

// Estrutura para armazenar grupos
Group groups[MAX_GROUPS];
int num_groups = 0;

// Contadores de mensagens recebidas e perdidas por cliente
int msgs_from_client[MAX_CLIENTS] = {0};
int lost_msgs_from_client[MAX_CLIENTS] = {0};
int last_seq_from_client[MAX_CLIENTS] = {-1}; // -1 para indicar que ainda não recebeu nenhuma

// Função para verificar se o ficheiro existe
bool file_exists(const char *filename)
{
    FILE *f = fopen(filename, "r");
    if (f)
    {
        fclose(f);
        return true;
    }
    return false;
}

// Função para criar uma conta
bool criar_conta(const char *username, const char *password)
{
    if (!file_exists(CONTAS_FILE))
    {
        FILE *f = fopen(CONTAS_FILE, "w");
        if (!f)
            return false;
        fclose(f);
    }
    // Verifica se já existe
    FILE *f = fopen(CONTAS_FILE, "r");
    char u[50], p[50];
    while (fscanf(f, "%49s %49s", u, p) == 2)
    {
        if (strcmp(u, username) == 0)
        {
            fclose(f);
            return false; // Já existe
        }
    }
    fclose(f);
    // Adiciona nova conta
    f = fopen(CONTAS_FILE, "a");
    if (!f)
        return false;
    fprintf(f, "%s %s\n", username, password);
    fclose(f);
    return true;
}

// Função para autenticar um usuário
bool autenticar(const char *username, const char *password)
{
    if (!file_exists(CONTAS_FILE))
        return false;
    FILE *f = fopen(CONTAS_FILE, "r");
    char u[50], p[50];
    while (fscanf(f, "%49s %49s", u, p) == 2)
    {
        if (strcmp(u, username) == 0 && strcmp(p, password) == 0)
        {
            fclose(f);
            return true;
        }
    }
    fclose(f);
    return false;
}
// Função para enviar mensagem a todos os clientes, exceto o remetente
void send_to_all(Message *msg, struct sockaddr_in exclude_addr)
{
    for (int i = 0; i < num_clients; i++)
    {
        if (memcmp(&clients[i].addr, &exclude_addr, sizeof(exclude_addr)) != 0)
        {
            sendto(sockfd, msg, sizeof(Message), 0,
                   (struct sockaddr *)&clients[i].addr, sizeof(clients[i].addr));
        }
    }
}


// Função para enviar mensagem a um usuário específico
void display_all_clients()
{
    printf("\033[H\033[J"); // Limpa a tela
    printf("--- ESTADO ATUAL DOS CLIENTES ---\n");
    for (int i = 0; i < num_clients; i++)
    {
        printf("%s: %s\n", clients[i].username, clients[i].text_buffer);
    }
    fflush(stdout);
}

// Função para atualizar o buffer do cliente
void update_client_text(const char *username, const char *new_text)
{
    for (int i = 0; i < num_clients; i++)
    {
        if (strcmp(clients[i].username, username) == 0)
        {
            strncpy(clients[i].text_buffer, new_text, BUFFER_SIZE - 1);
            clients[i].text_buffer[BUFFER_SIZE - 1] = '\0';
            break;
        }
    }
}
// Função para remover clientes inativos
void remove_inactive_clients()
{
    time_t now = time(NULL);
    for (int i = 0; i < num_clients;)
    {
        if (difftime(now, clients[i].last_activity) > CLIENT_TIMEOUT)
        {
            printf("\n[INFO] Cliente inativo removido: %s\n", clients[i].username);

            Message notify_msg = {.type = 3};
            strcpy(notify_msg.from, clients[i].username);
            send_to_all(&notify_msg, clients[i].addr);

            memmove(&clients[i], &clients[num_clients - 1], sizeof(Client));
            num_clients--;
            display_all_clients();
            // Não incrementa i, pois o cliente atual foi substituído
        }
        else
        {
            i++;
        }
    }
}

int find_client_index(const char *username)
{
    for (int i = 0; i < num_clients; i++)
    {
        if (strcmp(clients[i].username, username) == 0)
            return i;
    }
    return -1;
}

// --- Funções de grupos ---

// Carrega grupos do ficheiro
void load_groups()
{
    FILE *f = fopen(GROUPS_FILE, "r");
    if (!f)
    {
        // Cria ficheiro se não existir
        f = fopen(GROUPS_FILE, "w");
        if (f)
            fclose(f);
        return;
    }
    char line[2048];
    num_groups = 0;
    while (fgets(line, sizeof(line), f))
    {
        if (num_groups >= MAX_GROUPS)
            break;
        char *token = strtok(line, ",\n");
        if (!token)
            continue;
        strncpy(groups[num_groups].name, token, 49);
        token = strtok(NULL, ",\n");
        if (!token)
            continue;
        strncpy(groups[num_groups].owner, token, 49);
        groups[num_groups].num_members = 0;
        for (int i = 0; i < MAX_GROUP_MEMBERS; i++)
            groups[num_groups].member_buffers[i][0] = '\0';
        while ((token = strtok(NULL, ",\n")) && groups[num_groups].num_members < MAX_GROUP_MEMBERS)
        {
            strncpy(groups[num_groups].members[groups[num_groups].num_members], token, 49);
            groups[num_groups].member_buffers[groups[num_groups].num_members][0] = '\0';
            groups[num_groups].num_members++;
        }
        num_groups++;
    }
    fclose(f);
}

// Guarda grupos no ficheiro
void save_groups()
{
    FILE *f = fopen(GROUPS_FILE, "w");
    if (!f)
        return;
    for (int i = 0; i < num_groups; i++)
    {
        fprintf(f, "%s,%s", groups[i].name, groups[i].owner);
        for (int j = 0; j < groups[i].num_members; j++)
        {
            fprintf(f, ",%s", groups[i].members[j]);
        }
        fprintf(f, "\n");
    }
    fclose(f);
}

// Procura grupo pelo nome
int find_group_index(const char *group_name)
{
    for (int i = 0; i < num_groups; i++)
    {
        if (strcmp(groups[i].name, group_name) == 0)
            return i;
    }
    return -1;
}

// Verifica se username é membro do grupo
int is_group_member(Group *g, const char *username)
{
    for (int i = 0; i < g->num_members; i++)
    {
        if (strcmp(g->members[i], username) == 0)
            return 1;
    }
    return 0;
}

// Adiciona membro ao grupo
int add_group_member(Group *g, const char *username)
{
    if (g->num_members >= MAX_GROUP_MEMBERS)
        return 0;
    if (is_group_member(g, username))
        return 0;
    strncpy(g->members[g->num_members++], username, 49);
    return 1;
}

// Remove membro do grupo
int remove_group_member(Group *g, const char *username)
{
    for (int i = 0; i < g->num_members; i++)
    {
        if (strcmp(g->members[i], username) == 0)
        {
            memmove(&g->members[i], &g->members[i + 1], (g->num_members - i - 1) * 50);
            g->num_members--;
            return 1;
        }
    }
    return 0;
}

// Atualiza o texto de um membro num grupo
void update_group_member_text(Group *g, const char *username, const char *new_text)
{
    for (int i = 0; i < g->num_members; i++)
    {
        if (strcmp(g->members[i], username) == 0)
        {
            strncpy(g->member_buffers[i], new_text, BUFFER_SIZE - 1);
            g->member_buffers[i][BUFFER_SIZE - 1] = '\0';
            break;
        }
    }
}

// Monta o estado "live" do grupo (todos os membros e respetivo texto)
void montar_estado_grupo(Group *g, char *buffer, size_t buffer_size, const char *exclude_username)
{
    buffer[0] = '\0';
    for (int i = 0; i < g->num_members; i++)
    {
        if (exclude_username && strcmp(g->members[i], exclude_username) == 0)
            continue;
        char linha[BUFFER_SIZE + 64];
        snprintf(linha, sizeof(linha), "%s: %s\n", g->members[i], g->member_buffers[i]);
        strncat(buffer, linha, buffer_size - strlen(buffer) - 1);
    }
}

// ---------- Função principal do servidor ----------
// Envia mensagem a todos os clientes, exceto o remetente    
int main()
{
    sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    struct sockaddr_in servaddr = {
        .sin_family = AF_INET,
        .sin_port = htons(PORT),
        .sin_addr.s_addr = INADDR_ANY};

    bind(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));

    printf("Servidor iniciado na porta %d\n\n", PORT);

    fd_set readfds;
    struct timeval tv;

    load_groups();

    time_t last_checkpoint = time(NULL);
    const int CHECKPOINT_INTERVAL = 10; // segundos

    while (1)
    {
        FD_ZERO(&readfds);
        FD_SET(sockfd, &readfds);
        tv.tv_sec = 1; // Checa a cada 1 segundo
        tv.tv_usec = 0;

        int activity = select(sockfd + 1, &readfds, NULL, NULL, &tv);

        if (activity > 0 && FD_ISSET(sockfd, &readfds))
        {
            Message msg;
            struct sockaddr_in cliaddr;
            socklen_t len = sizeof(cliaddr);

            int n = recvfrom(sockfd, &msg, sizeof(Message), 0, (struct sockaddr *)&cliaddr, &len);
            if (n <= 0)
                continue;

            int client_idx = -1;
            for (int i = 0; i < num_clients; i++)
            {
                if (memcmp(&clients[i].addr, &cliaddr, sizeof(cliaddr)) == 0)
                {
                    clients[i].last_activity = time(NULL);
                    client_idx = i;
                    break;
                }
            }
            // Se for mensagem de cliente já conhecido, processa sequência
            if (client_idx >= 0) {
                // Extrai número de sequência do início de msg.data (formato "SEQ:resto")
                int seq = -1;
                sscanf(msg.data, "%d:", &seq);
                if (seq >= 0) {
                    if (last_seq_from_client[client_idx] != -1 && seq != last_seq_from_client[client_idx] + 1) {
                        lost_msgs_from_client[client_idx] += (seq - last_seq_from_client[client_idx] - 1);
                    }
                    last_seq_from_client[client_idx] = seq;
                }
                msgs_from_client[client_idx]++;
            }

            // NOVO: Autenticação e criação de conta
            if (msg.type == MSG_AUTH)
            {
                char *username = msg.from;
                char *password = msg.data;
                Message resp = {.type = MSG_AUTH_FAIL};
                if (autenticar(username, password))
                {
                    resp.type = MSG_AUTH_OK;
                }
                sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                continue;
            }
            if (msg.type == MSG_CREATE)
            {
                char *username = msg.from;
                char *password = msg.data;
                if (criar_conta(username, password))
                {
                    Message resp = {.type = MSG_CREATE_OK};
                    sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                }
                
                continue;
            }

            if (msg.type == 1) // Join
            {
                int exists = 0;
                for (int i = 0; i < num_clients; i++)
                {
                    if (strcmp(clients[i].username, msg.from) == 0)
                    {
                        exists = 1;
                        break;
                    }
                }
                if (!exists)
                {
                    strcpy(clients[num_clients].username, msg.from);
                    memcpy(&clients[num_clients].addr, &cliaddr, sizeof(cliaddr));
                    clients[num_clients].last_activity = time(NULL);
                    clients[num_clients].text_buffer[0] = '\0';
                    num_clients++;

                    printf("\n[INFO] Novo cliente conectado: %s\n", msg.from);
                }

                Message notify_msg = {.type = 1};
                strcpy(notify_msg.from, msg.from);
                send_to_all(&notify_msg, cliaddr);

                display_all_clients();
            }

            else if (msg.type == 3) // Leave
            {
                for (int i = 0; i < num_clients; i++)
                {
                    if (strcmp(clients[i].username, msg.from) == 0)
                    {
                        memmove(&clients[i], &clients[num_clients - 1], sizeof(Client));
                        num_clients--;
                        break;
                    }
                }
                Message notify_msg = {.type = 3};
                strcpy(notify_msg.from, msg.from);
                send_to_all(&notify_msg, cliaddr);

                printf("\n[INFO] Cliente desconectado: %s\n", msg.from);
                display_all_clients();
            }
            else if (msg.type == MSG_ALL && strlen(msg.group) == 0)
            {
                int idx_from = find_client_index(msg.from);
                if (idx_from >= 0)
                {
                    strncpy(all_buffers[idx_from], msg.data, BUFFER_SIZE - 1);
                    all_buffers[idx_from][BUFFER_SIZE - 1] = '\0';
                    // Atualiza também o text_buffer do cliente
                    update_client_text(msg.from, msg.data);
                }
                // Para cada cliente, monta o estado de todos os outros no [all]
                for (int i = 0; i < num_clients; i++)
                {
                    if (memcmp(&clients[i].addr, &cliaddr, sizeof(cliaddr)) != 0)
                    {
                        Message estado_msg = msg;
                        // Monta o estado de todos os outros clientes (exceto o destinatário)
                        estado_msg.data[0] = '\0';
                        for (int j = 0; j < num_clients; j++)
                        {
                            if (i != j)
                            {
                                char linha[BUFFER_SIZE + 64];
                                snprintf(linha, sizeof(linha), "%s: %s\n", clients[j].username, all_buffers[j]);
                                strncat(estado_msg.data, linha, BUFFER_SIZE - strlen(estado_msg.data) - 1);
                            }
                        }
                        sendto(sockfd, &estado_msg, sizeof(Message), 0,
                               (struct sockaddr *)&clients[i].addr, sizeof(clients[i].addr));
                    }
                }
                // Mostra o estado atualizado no terminal do servidor
                display_all_clients();
            }
            else if (msg.type == MSG_DM)
            {
                int idx_from = find_client_index(msg.from);
                int idx_to = find_client_index(msg.to);
                if (idx_from >= 0 && idx_to >= 0)
                {
                    // Atualiza buffer DM do remetente para o destinatário
                    strncpy(dm_buffers[idx_from][idx_to], msg.data, BUFFER_SIZE - 1);
                    dm_buffers[idx_from][idx_to][BUFFER_SIZE - 1] = '\0';

                    // Envia o estado live do DM do remetente para o destinatário
                    Message dm_msg = msg;
                    snprintf(dm_msg.data, BUFFER_SIZE, "%s", dm_buffers[idx_from][idx_to]);
                    sendto(sockfd, &dm_msg, sizeof(Message), 0,
                           (struct sockaddr *)&clients[idx_to].addr, sizeof(clients[idx_to].addr));
                }
            }
            // --- Comandos de grupo ---
            if (msg.type == MSG_GROUPS_CREATE)
            {
                if (find_group_index(msg.group) >= 0)
                {
                    Message resp = {.type = MSG_GROUPS_FAIL};
                    strncpy(resp.data, msg.group, sizeof(resp.data) - 1);
                    strncpy(resp.group, msg.group, sizeof(resp.group) - 1); // Notificação consistente
                    sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                    continue;
                }
                // Cria novo grupo
                Group *g = &groups[num_groups++];
                strncpy(g->name, msg.group, 49);
                strncpy(g->owner, msg.from, 49);
                g->num_members = 1;
                strncpy(g->members[0], msg.from, 49);
                g->member_buffers[0][0] = '\0';
                save_groups();
                Message resp = {.type = MSG_GROUPS_OK};
                strncpy(resp.data, msg.group, sizeof(resp.data) - 1);
                strncpy(resp.group, msg.group, sizeof(resp.group) - 1); // Notificação consistente
                sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                continue;
            }
            if (msg.type == MSG_GROUPS_ADD)
            {
                int gi = find_group_index(msg.group);
                if (gi >= 0 && strcmp(groups[gi].owner, msg.from) == 0)
                {
                    if (add_group_member(&groups[gi], msg.to))
                    {
                        save_groups();
                        Message resp = {.type = MSG_GROUPS_OK};
                        strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                        sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                    }
                    else
                    {
                        Message resp = {.type = MSG_GROUPS_FAIL};
                        strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                        sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                    }
                }
                else
                {
                    Message resp = {.type = MSG_GROUPS_FAIL};
                    strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                    sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                }
                continue;
            }
            if (msg.type == MSG_GROUPS_REMOVE)
            {
                int gi = find_group_index(msg.group);
                if (gi >= 0 && strcmp(groups[gi].owner, msg.from) == 0)
                {
                    if (remove_group_member(&groups[gi], msg.to))
                    {
                        save_groups();
                        Message resp = {.type = MSG_GROUPS_OK};
                        strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                        sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                        // NOVO: Notifica o utilizador removido
                        int ci = find_client_index(msg.to);
                        if (ci >= 0)
                        {
                            Message notify = {.type = MSG_GROUPS_REMOVE};
                            strcpy(notify.group, groups[gi].name);
                            strcpy(notify.data, "Foi removido do grupo. Voltou ao chat all.");
                            sendto(sockfd, &notify, sizeof(Message), 0,
                                   (struct sockaddr *)&clients[ci].addr, sizeof(clients[ci].addr));
                        }
                    }
                    else
                    {
                        Message resp = {.type = MSG_GROUPS_FAIL};
                        strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                        sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                    }
                }
                else
                {
                    Message resp = {.type = MSG_GROUPS_FAIL};
                    strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                    sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                }
                continue;
            }
            if (msg.type == MSG_GROUPS_DELETE)
            {
                int gi = find_group_index(msg.group);
                if (gi >= 0 && strcmp(groups[gi].owner, msg.from) == 0)
                {
                    // Antes de remover, notifica todos os membros ativos
                    for (int mi = 0; mi < groups[gi].num_members; mi++)
                    {
                        int ci = find_client_index(groups[gi].members[mi]);
                        if (ci >= 0)
                        {
                            Message notify = {.type = MSG_GROUPS_DELETE};
                            strcpy(notify.group, groups[gi].name);
                            strcpy(notify.data, "Grupo removido. Voltou ao chat all.");
                            sendto(sockfd, &notify, sizeof(Message), 0,
                                   (struct sockaddr *)&clients[ci].addr, sizeof(clients[ci].addr));
                        }
                    }
                    memmove(&groups[gi], &groups[gi + 1], (num_groups - gi - 1) * sizeof(Group));
                    num_groups--;
                    save_groups();
                    Message resp = {.type = MSG_GROUPS_OK};
                    strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                    sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                    continue;
                }
                else
                {
                    Message resp = {.type = MSG_GROUPS_FAIL};
                    strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                    sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                }
                continue;
            }
            if (msg.type == MSG_GROUPS_CHECK)
            {
                int gi = find_group_index(msg.group);
                Message resp = {.type = MSG_GROUPS_FAIL};
                if (gi >= 0 && is_group_member(&groups[gi], msg.from))
                {
                    resp.type = MSG_GROUPS_OK;
                    strncpy(resp.data, msg.group, sizeof(resp.data) - 1);
                    strncpy(resp.group, msg.group, sizeof(resp.group) - 1);
                }
                sendto(sockfd, &resp, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                continue;
            }
            // --- Fim comandos de grupo ---

            // --- Mensagens para grupo ---
            if (msg.type == MSG_ALL && strlen(msg.group) > 0)
            {
                int gi = find_group_index(msg.group);
                if (gi >= 0 && is_group_member(&groups[gi], msg.from))
                {
                    // Atualiza o buffer do membro no grupo
                    update_group_member_text(&groups[gi], msg.from, msg.data);

                    // Para cada membro do grupo, exceto o remetente, envia o estado live
                    for (int mi = 0; mi < groups[gi].num_members; mi++)
                    {
                        if (strcmp(groups[gi].members[mi], msg.from) != 0)
                        {
                            int ci = find_client_index(groups[gi].members[mi]);
                            if (ci >= 0)
                            {
                                Message estado_msg = msg;
                                // Monta o estado do grupo (todos os membros e respetivo texto)
                                montar_estado_grupo(&groups[gi], estado_msg.data, sizeof(estado_msg.data), groups[gi].members[mi]);
                                strncpy(estado_msg.group, groups[gi].name, sizeof(estado_msg.group) - 1);
                                estado_msg.group[sizeof(estado_msg.group) - 1] = '\0';
                                sendto(sockfd, &estado_msg, sizeof(Message), 0,
                                       (struct sockaddr *)&clients[ci].addr, sizeof(clients[ci].addr));
                            }
                        }
                    }
                }
                else
                {
                    // Não é membro, envia erro
                    Message err = {.type = MSG_GROUPS_FAIL};
                    snprintf(err.data, BUFFER_SIZE, "Você não está neste grupo");
                    sendto(sockfd, &err, sizeof(Message), 0, (struct sockaddr *)&cliaddr, sizeof(cliaddr));
                }
                continue;
            }
            // --- Fim mensagens para grupo ---

            // --- Encaminhamento de ficheiros ---
            if (msg.type == MSG_FILE_OFFER) {
                // Se for para all
                if (strcmp(msg.to, "all") == 0) {
                    for (int i = 0; i < num_clients; i++) {
                        if (strcmp(clients[i].username, msg.from) != 0) {
                            sendto(sockfd, &msg, sizeof(Message), 0,
                                   (struct sockaddr *)&clients[i].addr, sizeof(clients[i].addr));
                        }
                    }
                } else if (msg.to[0] != '\0') {
                    // Se for para um utilizador
                    for (int i = 0; i < num_clients; i++) {
                        if (strcmp(clients[i].username, msg.to) == 0 && strcmp(clients[i].username, msg.from) != 0) {
                            sendto(sockfd, &msg, sizeof(Message), 0,
                                   (struct sockaddr *)&clients[i].addr, sizeof(clients[i].addr));
                        }
                    }
                } else if (msg.group[0] != '\0') {
                    // Se for para um grupo
                    int gi = -1;
                    for (int g = 0; g < MAX_GROUPS; g++) {
                        if (strcmp(groups[g].name, msg.group) == 0) { gi = g; break; }
                    }
                    if (gi >= 0) {
                        for (int mi = 0; mi < groups[gi].num_members; mi++) {
                            if (strcmp(groups[gi].members[mi], msg.from) != 0) {
                                for (int ci = 0; ci < num_clients; ci++) {
                                    if (strcmp(clients[ci].username, groups[gi].members[mi]) == 0) {
                                        sendto(sockfd, &msg, sizeof(Message), 0,
                                               (struct sockaddr *)&clients[ci].addr, sizeof(clients[ci].addr));
                                    }
                                }
                            }
                        }
                    }
                }
                continue;
            }
        }

        // Após processar mensagens, verifica clientes inativos
        remove_inactive_clients();

        // Periodicamente imprime checkpoint (estado atual dos clientes)
        time_t now = time(NULL);
        if (now - last_checkpoint >= CHECKPOINT_INTERVAL) {
            printf("\033[H\033[J"); // Limpa o terminal
            printf("----------CHECKPOINT-----------\n");
            for (int i = 0; i < num_clients; i++) {
                printf("Cliente: %s | Mensagens recebidas: %d | Perdidas: %d\n",
                    clients[i].username, msgs_from_client[i], lost_msgs_from_client[i]);
            }
            printf("----------------------------------\n");
            last_checkpoint = now;
        }
    }
    close(sockfd);
    return 0;
}