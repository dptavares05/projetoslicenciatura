#include "header_parte1.h"

// --- Definições Globais e Constantes ---

#define MAIN_MEMORY_SIZE 21000 // 21kB
#define PAGE_SIZE 3000         // 3kB
#define NUM_FRAMES (MAIN_MEMORY_SIZE / PAGE_SIZE) // 7 frames

// Enum para o algoritmo de substituição
typedef enum { FIFO, LRU } ReplacementAlgorithm;


// --- Estruturas de Dados ---

// Representa um frame na memória principal
typedef struct {
    int owner_pid;          // ID do processo que ocupa o frame (-1 se livre)
    int owner_page_num;     // Número da página do processo que está aqui
    int fifo_load_time;     // Tempo em que o frame foi carregado (para FIFO)
    int lru_last_access_time; // Tempo em que o frame foi acedido pela última vez (para LRU)
} Frame;

// Representa uma entrada na tabela de páginas de um processo
typedef struct {
    int frame_id;           // Onde a página está na memória principal (-1 se não estiver)
} PageTableEntry;

// Representa um processo
typedef struct {
    int pid;
    int memory_size;
    int num_pages;
    int has_terminated;     // Flag para saber se o processo terminou (ex: SIGSEGV)
    PageTableEntry* page_table;
} Process;



// --- Funções Auxiliares ---

// Procura um frame livre (owner_pid == -1), retorna o de menor índice
int find_free_frame(Frame main_memory[]) {
    for (int i = 0; i < NUM_FRAMES; i++) {
        if (main_memory[i].owner_pid == -1) {
            return i;
        }
    }
    return -1; // Nenhum frame livre
}

// Procura um frame para substituir usando o algoritmo FIFO
int find_victim_frame_fifo(Frame main_memory[]) {
    int victim_frame_id = -1;
    int earliest_time = -1;

    for (int i = 0; i < NUM_FRAMES; i++) {
        if (victim_frame_id == -1 || main_memory[i].fifo_load_time < earliest_time) {
            earliest_time = main_memory[i].fifo_load_time;
            victim_frame_id = i;
        }
    }
    return victim_frame_id;
}

// Procura um frame para substituir usando o algoritmo LRU
int find_victim_frame_lru(Frame main_memory[]) {
    int victim_frame_id = -1;
    int least_recent_time = -1;

    for (int i = 0; i < NUM_FRAMES; i++) {
        if (victim_frame_id == -1 || main_memory[i].lru_last_access_time < least_recent_time) {
            least_recent_time = main_memory[i].lru_last_access_time;
            victim_frame_id = i;
        }
    }
    return victim_frame_id;
}

// Imprime o cabeçalho da tabela de output
void print_header(int num_processes) {
    printf("%-5s", "time inst ");
    for (int i = 0; i < num_processes; i++) {
        char proc_name[20];
        // Use snprintf para segurança
        snprintf(proc_name, sizeof(proc_name), "proc%d", i + 1);
        printf("%-18s", proc_name);
    }
    printf("\n");
}

// Imprime o estado atual da memória
void print_memory_state(int time, int num_processes, Process processes[], char sigsegv_flags[]) {    printf("%-10d", time); // "inst" é igual a "time" na Parte 1

    for (int i = 0; i < num_processes; i++) {
        if (sigsegv_flags[i]) {
            printf("%-18s", "SIGSEGV");
            continue;
        }
        
        if (processes[i].has_terminated) {
            printf("%-18s", "");
            continue;
        }

        char frame_str[100] = "";
        char temp_str[10];
        int first_frame = 1;

        // Itera pelas PÁGINAS do processo para garantir a ordem correta no output
        for (int p = 0; p < processes[i].num_pages; p++) {
            if (processes[i].page_table[p].frame_id != -1) {
                if (!first_frame) {
                    strcat(frame_str, ",");
                }
                sprintf(temp_str, "F%d", processes[i].page_table[p].frame_id);
                strcat(frame_str, temp_str);
                first_frame = 0;
            }
        }
        printf("%-18s", frame_str);
    }
    printf("\n");
}


// --- Função Principal ---

// --- Função Principal ---

int main(int argc, char *argv[]) {
    // 1. Validação do input para 3 argumentos (algoritmo e índice do input)
    if (argc != 3) {
        fprintf(stderr, "Uso: %s <fifo|lru> <input_index>\n", argv[0]);
        fprintf(stderr, "Exemplo: %s lru 2\n", argv[0]);
        return 1;
    }

    // Seleção do algoritmo de substituição
    ReplacementAlgorithm algorithm;
    if (strcmp(argv[1], "fifo") == 0) {
        algorithm = FIFO;
    } else if (strcmp(argv[1], "lru") == 0) {
        algorithm = LRU;
    } else {
        fprintf(stderr, "Algoritmo inválido. Use 'fifo' ou 'lru'.\n");
        return 1;
    }
    
    // Converter o índice do input de string para inteiro
    int input_index = atoi(argv[2]);

    // --- TABELAS DE LOOKUP PARA SELECIONAR O INPUT CORRETO ---
    
    // Tabela de ponteiros para os arrays de memória
    int* mem_inputs[] = {
        inputP1Mem00, inputP1Mem01, inputP1Mem02,
        inputP1Mem03, inputP1Mem04, inputP1Mem05
    };
    // Tabela de ponteiros para os arrays de execução
    int* exec_inputs[] = {
        inputP1Exec00, inputP1Exec01, inputP1Exec02,
        inputP1Exec03, inputP1Exec04, inputP1Exec05
    };

    // Tabela com o NÚMERO DE ELEMENTOS de cada array (contados manualmente)
    const int mem_elements[] = {5, 5, 5, 10, 20, 3};
    const int exec_elements[] = {24, 12, 18, 26, 78, 48};
    
    // Validar se o índice do input fornecido é válido
    int num_available_inputs = sizeof(mem_inputs) / sizeof(mem_inputs[0]);
    if (input_index < 0 || input_index >= num_available_inputs) {
        fprintf(stderr, "Índice de input inválido: %d. Válidos: 0 a %d\n", input_index, num_available_inputs - 1);
        return 1;
    }

    // Selecionar os ponteiros e tamanhos corretos para a simulação atual
    int* current_mem_input = mem_inputs[input_index];
    int* current_exec_input = exec_inputs[input_index];
    int num_processes = mem_elements[input_index];
    int num_instructions = exec_elements[input_index];

    // 2. Inicialização das estruturas de dados
    Frame main_memory[NUM_FRAMES];
    for (int i = 0; i < NUM_FRAMES; i++) {
        main_memory[i].owner_pid = -1;
    }

    Process processes[num_processes];
    for (int i = 0; i < num_processes; i++) {
        processes[i].pid = i + 1;
        processes[i].memory_size = current_mem_input[i]; // Usa o ponteiro para o input de memória
        processes[i].num_pages = (int)ceil((double)processes[i].memory_size / PAGE_SIZE);
        processes[i].has_terminated = 0;
        processes[i].page_table = (PageTableEntry*)malloc(processes[i].num_pages * sizeof(PageTableEntry));
        for (int j = 0; j < processes[i].num_pages; j++) {
            processes[i].page_table[j].frame_id = -1;
        }
    }

    // 3. Loop principal da simulação
    print_header(num_processes);
    int time_counter = 0;
    
    // O loop itera sobre o array de execução (um par de cada vez)
    for (int i = 0; i < num_instructions / 2; i++) {
        // Obter a instrução atual do array de execução selecionado
        int current_pid = current_exec_input[i * 2];
        int address = current_exec_input[i * 2 + 1];

        // Validação do PID para evitar crash se o input tiver um PID inválido
        if (current_pid <= 0 || current_pid > num_processes) {
             print_memory_state(time_counter, num_processes, processes, NULL); // Imprime estado e avança
             time_counter++;
             continue;
        }

        Process *proc = &processes[current_pid - 1];
        char sigsegv_flags[num_processes];
        memset(sigsegv_flags, 0, sizeof(sigsegv_flags));

        // Se o processo já terminou, não faz nada, apenas avança no tempo
        if (proc->has_terminated) {
            print_memory_state(time_counter, num_processes, processes, sigsegv_flags);
            time_counter++;
            continue;
        }

        // Verificar Segmentation Fault
        if (address >= proc->memory_size) {
            sigsegv_flags[current_pid - 1] = 1; // Marcar para imprimir SIGSEGV
            proc->has_terminated = 1;

            // Libertar todos os frames do processo terminado
            for (int f = 0; f < NUM_FRAMES; f++) {
                if (main_memory[f].owner_pid == proc->pid) {
                    main_memory[f].owner_pid = -1;
                }
            }
        } else {
            // Lógica normal de Page Hit / Page Fault
            int required_page = address / PAGE_SIZE;
            int frame_id = proc->page_table[required_page].frame_id;

            if (frame_id != -1) { // Page Hit
                if (algorithm == LRU) {
                    main_memory[frame_id].lru_last_access_time = time_counter;
                }
            } else { // Page Fault
                int target_frame_id = find_free_frame(main_memory);
                if (target_frame_id == -1) { // Precisa de substituição
                    if (algorithm == FIFO) {
                        target_frame_id = find_victim_frame_fifo(main_memory);
                    } else { // LRU
                        target_frame_id = find_victim_frame_lru(main_memory);
                    }
                    // Desalocar a página antiga (vítima)
                    int victim_pid = main_memory[target_frame_id].owner_pid;
                    int victim_page_num = main_memory[target_frame_id].owner_page_num;
                    processes[victim_pid - 1].page_table[victim_page_num].frame_id = -1;
                }
                // Alocar a nova página no frame encontrado
                main_memory[target_frame_id].owner_pid = proc->pid;
                main_memory[target_frame_id].owner_page_num = required_page;
                main_memory[target_frame_id].fifo_load_time = time_counter;
                main_memory[target_frame_id].lru_last_access_time = time_counter;
                proc->page_table[required_page].frame_id = target_frame_id;
            }
        }
        
        // Imprimir o estado da memória DEPOIS de processar a instrução do instante atual
        print_memory_state(time_counter, num_processes, processes, sigsegv_flags);
        time_counter++;
    }

    // 4. Libertar memória alocada dinamicamente
    for (int i = 0; i < num_processes; i++) {
        free(processes[i].page_table);
    }

    return 0;
}

