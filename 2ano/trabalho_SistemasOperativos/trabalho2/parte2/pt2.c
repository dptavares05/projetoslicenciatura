#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "queue.h"
#include "header_p2.h"

// --- Constantes ---
#define MAIN_MEMORY_SIZE 21000// Tamanho total da memória principal
#define PAGE_SIZE 3000// Tamanho de cada página
#define NUM_FRAMES (MAIN_MEMORY_SIZE / PAGE_SIZE)// Número total de quadros na memória
#define MAX_PROCESSOS 20// Número máximo de processos
#define MAX_PROGRAMAS_PER_INPUT 20// Número máximo de programas por entrada
#define MAX_INS_PER_PROG 20// Número máximo de instruções por programa
#define MAX_INSTANTES 100// Número máximo de instâncias
#define QUANTUM 3

// --- Estruturas ---
typedef enum//estados possiveis de um processo
{
    NEW,
    READY,
    RUNNING,
    BLOCKED,
    EXIT
} State; 

typedef enum//erros possiveis 
{
    NO_ERROR,
    SIGSEGV,
    SIGILL,
    SIGEOF
} ErrorType;

typedef struct{// Entrada da tabela de páginas, que mapeia uma página para um quadro na memória
    int frame_id;
} PageTableEntry;
typedef struct{                 // Estrutura que representa um quadro na memória principal, que contém :
    int owner_pid;              //o PID do processo dono
    int owner_page_num;        //o número da página
    int lru_last_access_time;   //o tempo do último acesso LRU
} Frame;
typedef struct{// Estrutura que representa um processo em execução que contem:

    int id;// ID do processo
    int *instr;// Instruções do processo
    int pc;// program counter
    int tamanho_programa;
    State estado;
    int tempo_no_estado;
    int bloqueado_restante;//instantes restantes em estado bloqueado
    int quantum_usado;
    int memory_size;// Tamanho total da memória do processo
    int num_pages;// Número de páginas do processo
    PageTableEntry *page_table;
    ErrorType last_error;
} Processo;
typedef struct{// Estrutura que representa os dados de entrada, que contem:
    void *data;// Ponteiro para os dados de entrada (matriz de inteiros)
    int rows;// Número de linhas
    int cols;// Número de colunas
} InputData;

// --- Variáveis Globais ---
Processo *processos[MAX_PROCESSOS] = {NULL};// Array de ponteiros para processos, inicializado com NULL
// Cada índice do array representa um processo, onde o índice 0 é o processo 1
// e assim por diante, até o índice 19 que representa o processo 20.
Frame main_memory[NUM_FRAMES];// Memória principal, que é um array de frames
// Cada frame é uma estrutura que contém informações sobre o processo dono, o número da página e o tempo do último acesso LRU.
int id_proximo_processo = 1;
int total_processos_ativos = 0;
InputData current_input;

// --- Funções de Memória ---
void inicializar_memoria()
{
    for (int i = 0; i < NUM_FRAMES; i++)
    {
        main_memory[i].owner_pid = -1;// Inicializa o PID do dono como -1 (nenhum processo)
        main_memory[i].lru_last_access_time = -1;// Inicializa o tempo do último acesso LRU como -1 (nunca acessado)
    }
}
void libertar_memoria_processo(int pid)// Limpar os quadros de memória ocupados por um processo específico
{
    for (int i = 0; i < NUM_FRAMES; i++)
    {
        if (main_memory[i].owner_pid == pid)
        {
            main_memory[i].owner_pid = -1;
            main_memory[i].lru_last_access_time = -1;
        }
    }
}
int encontrar_frame_livre()// Encontra um quadro livre na memória principal
{
    for (int i = 0; i < NUM_FRAMES; i++)
        if (main_memory[i].owner_pid == -1)// Se o PID do dono for -1, significa que o quadro está livre
            return i;
    return -1;
}
int encontrar_vitima_lru()// Encontra uma vítima usando o algoritmo LRU (Least Recently Used)
// Este algoritmo escolhe o quadro que foi acessado há mais tempo, ou seja, o menos recentemente usado.
// Se houver um empate, escolhe o de menor índice.
{
    int victim_frame_id = -1;// Inicializa o ID do quadro da vítima como -1
    int least_recent_time = __INT_MAX__;// Inicializa o tempo do último acesso LRU como o maior valor possível
    for (int i = 0; i < NUM_FRAMES; i++)// Percorre todos os quadros na memória principal
    {
        if (main_memory[i].lru_last_access_time != -1 && (victim_frame_id == -1 || main_memory[i].lru_last_access_time < least_recent_time))
        {// Se o quadro foi acessado e é o primeiro encontrado ou tem um tempo de acesso menor que o atual
            least_recent_time = main_memory[i].lru_last_access_time;// Atualiza o tempo do último acesso LRU
            victim_frame_id = i;// Atualiza o ID do quadro da vítima
        }
        else if (main_memory[i].lru_last_access_time == least_recent_time && i < victim_frame_id)// Se o tempo de acesso é igual ao 
        {
            victim_frame_id = i;// Atualiza o ID do quadro da vítima para o de menor índice
        }
    }
    return victim_frame_id;
}
int tratar_acesso_memoria(Processo *p, int address, int time, int page_to_keep_in_mem)// Trata o acesso à memória de um processo
{
    if (address < 0 || address >= p->memory_size)// Verifica se o endereço está dentro dos limites do processo
    {
        p->last_error = SIGSEGV;// Se o endereço for inválido dá um segmentation fault
        return -1;
    }
    int required_page = address / PAGE_SIZE;
    if (p->page_table[required_page].frame_id != -1)// Se a página está na memória
    {
        main_memory[p->page_table[required_page].frame_id].lru_last_access_time = time;// Atualiza o tempo do último acesso LRU
        return 0;
    }
    int target_frame_id = encontrar_frame_livre();// Tenta encontrar um quadro livre na memória principal
    if (target_frame_id == -1)// Se não encontrar um quadro livre, precisa escolher uma vítima
    {
        target_frame_id = encontrar_vitima_lru();
        if (target_frame_id == -1)// Se não encontrar uma vítima, retorna erro
            return -1; // No victim found
        if (main_memory[target_frame_id].owner_page_num == page_to_keep_in_mem && main_memory[target_frame_id].owner_pid == p->id)
        // Se a página que está sendo mantida na memória é a mesma da vítima, não precisa fazer nada
            return 0;
        int victim_pid = main_memory[target_frame_id].owner_pid;// Obtém o PID do processo dono da vítima
        int victim_page = main_memory[target_frame_id].owner_page_num;// Obtém o número da página da vítima
        for (int i = 0; i < MAX_PROCESSOS; i++)
        {
            if (processos[i] && processos[i]->id == victim_pid)// Se o processo existe e o ID bate
            {
                processos[i]->page_table[victim_page].frame_id = -1;// Marca a página da vítima como não alocada
                break;
            }
        }
    }
    main_memory[target_frame_id].owner_pid = p->id;// Define o PID do dono do quadro como o do processo atual
    main_memory[target_frame_id].owner_page_num = required_page;// Define o número da página do dono do quadro como o da página requerida
    main_memory[target_frame_id].lru_last_access_time = time;// Atualiza o tempo do último acesso LRU
    p->page_table[required_page].frame_id = target_frame_id;// Atualiza a tabela de páginas do processo com o ID do quadro alocado
    return 0;
}

// --- Funções de Processo ---
void destruir_processo(Processo *p)// Libera a memória alocada para um processo
{
    if (!p)
        return;
    libertar_memoria_processo(p->id);
    free(p->instr);
    if (p->page_table != NULL)
        free(p->page_table);
    free(p);
}
Processo *criar_processo(int id_programa, int time)// Cria um novo processo com base no programa especificado
{
    if (id_proximo_processo > MAX_PROCESSOS)// Verifica se o número máximo de processos foi atingido
        return NULL;
    Processo *p = malloc(sizeof(Processo));// Alocar memória para o novo processo
    p->id = id_proximo_processo;// Atribui o ID do processo
    p->pc = 0;
    p->estado = NEW;
    p->tempo_no_estado = (time == 0) ? -1 : 0;// Define o tempo no estado como -1 se for o primeiro processo, ou 0 caso contrário
    p->bloqueado_restante = 0;
    p->quantum_usado = 0;
    p->last_error = NO_ERROR;
    int (*input_matrix)[current_input.cols] = current_input.data;// Obtém a matriz de entrada atual
    p->memory_size = input_matrix[0][id_programa - 1];
    p->num_pages = (p->memory_size > 0) ? (int)ceil((double)p->memory_size / PAGE_SIZE) : 0;// Calcula o número de páginas necessárias para o processo
    if (p->num_pages > 0)// Se o processo tem páginas, aloca memória para a tabela de páginas
    {
        p->page_table = malloc(p->num_pages * sizeof(PageTableEntry));
        for (int i = 0; i < p->num_pages; i++)// Inicializa a tabela de páginas
        {
            p->page_table[i].frame_id = -1;
        }
    }
    else
    {
        p->page_table = NULL;
    }
    p->instr = malloc(MAX_INS_PER_PROG * sizeof(int));
    p->tamanho_programa = 0;
    for (int i = 1; i < current_input.rows; i++)// Copia as instruções do programa para o processo
    {
        int instrucao = input_matrix[i][id_programa - 1];
        p->instr[p->tamanho_programa++] = instrucao;
        if (instrucao == 0)
            break;
    }
    processos[id_proximo_processo - 1] = p;
    id_proximo_processo++;
    total_processos_ativos++;
    return p;
}
void executar_instrucao(Processo *p, Queue *new_q, int time)
{
    if (p->pc >= p->tamanho_programa)// Verifica se o contador de programa está fora dos limites do programa
    {
        p->last_error = SIGEOF;// Se sim, define o erro como SIGEOF (fim de arquivo)
        p->estado = EXIT;// Muda o estado do processo para EXIT
        p->tempo_no_estado = 0;// Reseta o tempo no estado
        return;
    }

    int ins = p->instr[p->pc];
    if (ins >= 1000000000)//SWAP/MEMCPY
    {
        int addr1 = (ins / 100000) % 100000 - 10000;
        int addr2 = ins % 100000;
        if (tratar_acesso_memoria(p, addr1, time, addr2 / PAGE_SIZE) != 0 || tratar_acesso_memoria(p, addr2, time, addr1 / PAGE_SIZE) != 0)
        {
            p->estado = EXIT;
            p->tempo_no_estado = 0;
            return;
        }
        p->pc++;
    }
    else if (ins >= 1000 && ins <= 15999)//LOAD/STORE
    {
        if (tratar_acesso_memoria(p, ins - 1000, time, -1) != 0)
        {
            p->estado = EXIT;
            p->tempo_no_estado = 0;
            return;
        }
        p->pc++;
    }
    else if (ins >= 1 && ins <= 100)//JUMPF
    {
        int new_pc = p->pc + ins;
        if (new_pc >= p->tamanho_programa)
        {
            p->last_error = SIGILL;
            p->estado = EXIT;
            p->tempo_no_estado = 0;
        }
        else
        {
            p->pc = new_pc;
        }
    }
    else if (ins >= 101 && ins <= 199)//jumpb
    {
        int new_pc = p->pc - (ins % 100);
        if (new_pc < 0)
        {
            p->last_error = SIGILL;
            p->estado = EXIT;
            p->tempo_no_estado = 0;
        }
        else
        {
            p->pc = new_pc;
        }
    }
    else if (ins >= 201 && ins <= 299)//exec
    {
        Processo *np = criar_processo(ins % 100, time);
        if (np)
            enqueue(new_q, np);
        p->pc++;
        np->tempo_no_estado = 0;
    }
    else if (ins < 0)// I/O
    {
        p->estado = BLOCKED;
        p->tempo_no_estado = 0;
        p->bloqueado_restante = -ins - 1;
        p->pc++;
    }
    else if (ins == 0)//halt
    {
        p->estado = EXIT;
        p->tempo_no_estado = 0;
        p->pc++;
    }
    else
    {
        p->pc++;
    }
}

// --- Funções de Impressão ---
void formatar_frames_string(Processo *p, char *buffer)// Formata a string de frames ocupados por um processo
{
    buffer[0] = '\0';
    if (p->page_table == NULL)// Se o processo não tem páginas, retorna uma string vazia
        return;
    char temp[10];
    int first = 1;
    for (int i = 0; i < p->num_pages; i++)// Percorre a tabela de páginas do processo
    {
        if (p->page_table[i].frame_id != -1)// Se a página está alocada em um quadro
        {
            if (!first)// Se não for o primeiro quadro, adiciona uma vírgula
                strcat(buffer, ",");
            sprintf(temp, "F%d", p->page_table[i].frame_id);// Formata o quadro como "F<id>"
            strcat(buffer, temp);// Adiciona o quadro à string de frames
            first = 0;
        }
    }
}
void imprimir_estado(int time)
// Imprime o estado de todos os processos no formato especifico no output dado pelo professor, incluindo o tempo atual
{
    for (int i = 0; i < MAX_PROCESSOS; i++)
    {
        Processo *p = processos[i];
        char output[120] = "";
        if (!p)
        {
            printf("%-26s", "");
            continue;
        }
        const char *s = "";
        if (p->estado == EXIT)
        {
            if (p->tempo_no_estado > 0)// só dá print a exit no processo no proximo instante
            {
                s = "EXIT";
            }
            else
            {
                switch (p->last_error)// print nos erros possiveis
                {
                case SIGSEGV:
                    s = "SIGSEGV";
                    break;
                case SIGILL:
                    s = "SIGILL";
                    break;
                case SIGEOF:
                    s = "SIGEOF";
                    break;
                default:
                    s = "EXIT";
                    break;
                }
            }
        }
        else
        {
            switch (p->estado)
            {
            case NEW:
                s = "NEW";
                break;
            case READY:
                s = "READY";
                break;
            case RUNNING:
                s = "RUN";
                break;
            case BLOCKED:
            if (p->tempo_no_estado == 0){// só dá print a blocked no processo no proximo instante depois de ser bloqueado por um I/O
                s = "RUN";
            
            }else {
                s = "BLOCKED";
                break;
            }
            }
        }
        if (p->estado == NEW)// se o estado é new não precisa de frames
        {
            snprintf(output, sizeof(output), "%s", s);//
        }
        else// se o estado é diferente de new, precisa de dar print tambem aos frames e no formato certo
        {
            char frames_str[100];
            formatar_frames_string(p, frames_str);
            snprintf(output, sizeof(output), "%s [%s]", s, frames_str);
        }
        printf("%-26s", output);
    }
    printf("\n");
}
void imprimir_cabecalho()
{
    printf("%-10s", "time inst");
    for (int i = 1; i <= MAX_PROCESSOS; i++)
    {
        printf("proc%-22d", i);
    }
    printf("\n");
}

// --- Main ---
int main(int argc, char *argv[])
{
    if (argc != 2)// Verifica se o número de argumentos é correto
    {
        printf("Uso: %s <input_test_number>\n", argv[0]);//mensagem de erro pra corrigir o input
        return 1;
    }
    int test_num = atoi(argv[1]);// Converte o argumento para um número inteiro
    InputData lookup_table[] = {// Tabela de entrada que associa cada número de teste a um conjunto de dados
        {(void *)input00, 8, 20}, {(void *)input01, 6, 20}, {(void *)input02, 5, 20}, {(void *)input03, 6, 20}, {(void *)input04, 6, 20}, {(void *)input05, 6, 20}, {(void *)input06, 5, 20}, {(void *)input07, 12, 20}, {(void *)input08, 12, 20}, {(void *)input09, 12, 20}, {(void *)input10, 12, 20}, {(void *)input11, 12, 20}};// Define a tabela de entrada com os dados de cada teste
    current_input = lookup_table[test_num];// Define os dados de entrada atuais com base no número do teste

    Queue *new_q = createQueue(), *ready_q = createQueue(), *blocked_q = createQueue();
    Processo *running = NULL;
    inicializar_memoria();

    Processo *p1 = criar_processo(1, 0);
    if (p1)
        enqueue(new_q, p1);

    // Imprime o cabeçalho fora do loop
    imprimir_cabecalho();

    for (int time = 1; time <= MAX_INSTANTES; time++)
    {
        if (total_processos_ativos == 0 && isEmpty(new_q))
            break;


        // 1. ATUALIZAÇÃO DE ESTADOS ANTES DA EXECUÇÃO

        // NEW para READY
        int new_q_size = new_q->size;
        for (int i = 0; i < new_q_size; i++)
        {
            Processo *p = dequeue(new_q);
            p->tempo_no_estado++;
            if (p->tempo_no_estado >= 2)
            {
                p->estado = READY;
                enqueue(ready_q, p);
            }
            else
            {
                enqueue(new_q, p);
            }
        }

        // BLOCKED para READY
        int blocked_q_size = blocked_q->size;
        for (int i = 0; i < blocked_q_size; i++)
        {
            Processo *p = dequeue(blocked_q);
            p->bloqueado_restante--;
            if (p->bloqueado_restante < 0)
            {
                p->estado = READY;
                enqueue(ready_q, p);
            }
            else
            {
                enqueue(blocked_q, p);
            }
        }

        // Lidar com o processo que estava a correr na iteração anterior (pré-escalonamento)
        if (running)
        {
            if (running->estado == RUNNING && running->quantum_usado >= QUANTUM)// Se o processo está em execução e usou todo o seu quantum
            {
                running->estado = READY;
                enqueue(ready_q, running);
                running = NULL;
            }
            else if (running->estado == BLOCKED)
            {
                enqueue(blocked_q, running);
                running = NULL;
            }
            else if (running->estado == EXIT)
            {
                running = NULL;
            }
        }

        // 2. ESCALONAMENTO
        // Escalonar novo processo se a CPU estiver livre
        if (!running && !isEmpty(ready_q))
        {
            running = dequeue(ready_q);
            running->estado = RUNNING;
            running->quantum_usado = 0;
        }

        // 3. EXECUÇÃO DA INSTRUÇÃO
        int current_instruction = 0; // Guardar a instrução para imprimir
        if (running)
        {
            current_instruction = running->instr[running->pc];
            executar_instrucao(running, new_q, time);
            running->quantum_usado++;
        }

        // 4. IMPRESSÃO DO ESTADO FINAL DO TICK
        printf("%-10d", time);
        imprimir_estado(time);

        // 5. LIMPEZA DE PROCESSOS TERMINADOS
        for (int i = 0; i < MAX_PROCESSOS; i++)
        {
            if (processos[i]  && processos[i]->estado != NEW){
                processos[i]->tempo_no_estado++;
            }
            if (processos[i] && processos[i]->estado == EXIT)
            {

                if (processos[i]->tempo_no_estado >= 4)
                {
                    destruir_processo(processos[i]);
                    processos[i] = NULL;
                    total_processos_ativos--;
                }
            }
        }
    }// --- FIM DO LOOP DE EXECUÇÃO ---


    //limpar as filas e destruir os processos restantes
    deleteQueue(new_q);
    deleteQueue(ready_q);
    deleteQueue(blocked_q);
    for (int i = 0; i < MAX_PROCESSOS; ++i)
        if (processos[i])
            destruir_processo(processos[i]);
    return 0;
}