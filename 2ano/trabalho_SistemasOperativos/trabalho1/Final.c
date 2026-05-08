#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "queue.h"

#define MAX_PROCESSOS 20
#define MAX_INSTANTES 100
#define MAX_PROGRAMAS 20
#define MAX_INS 11

// Declaração dos arrays de entrada
extern int input00[5][20];
extern int input01[5][20];
extern int input02[4][20];
extern int input03[5][20];
extern int input04[11][20];
extern int input05[11][20];

typedef enum
{
    NEW,
    READY,
    RUNNING,
    RUN_IO,
    RUN_HALT,
    BLOCKED,
    EXIT
} State;

typedef struct
{
    int id;
    int *instr;
    int pc;
    State estado;
    int tempo_no_estado;
    int bloqueado_restante;
    int quantum_usado;
    int tamanho;
} Processo;

Processo *processos[MAX_PROCESSOS] = {NULL};
int id_atual = 1, total_processos = 0;

void initialize_programs(int input_number, int program_id, int *program, int *program_length)
{
    int (*input)[20] = NULL;
    int rows = 0;

    switch (input_number)
    {
    case 0:
        input = input00;
        rows = 5;
        break;
    case 1:
        input = input01;
        rows = 5;
        break;
    case 2:
        input = input02;
        rows = 4;
        break;
    case 3:
        input = input03;
        rows = 5;
        break;
    case 4:
        input = input04;
        rows = 11;
        break;
    case 5:
        input = input05;
        rows = 11;
        break;
    default:
        fprintf(stderr, "Erro: Número de entrada inválido.\n");
        exit(1);
    }

    for (int i = 0; i < MAX_INS; i++)
    {
        if (i < rows && program_id < MAX_PROGRAMAS)
        {
            program[i] = input[i][program_id];
        }
        else
        {
            program[i] = 0;
        }
        if (program[i] == 0 && i > 0)
        {
            *program_length = i;
            return;
        }
    }
    *program_length = MAX_INS;
}

Processo *criar_processo(int id, int *instr, int tam)
{
    Processo *p = malloc(sizeof(Processo));
    p->id = id;
    p->instr = malloc(tam * sizeof(int));
    memcpy(p->instr, instr, tam * sizeof(int));
    p->pc = 0;
    p->estado = NEW;
    p->tempo_no_estado = 0;
    p->bloqueado_restante = 0;
    p->quantum_usado = 0;
    p->tamanho = tam;
    return p;
}

void destruir_processo(Processo *p)
{
    free(p->instr);
    free(p);
}

void executar_instrucao(Processo *p, Queue *new_q, int prog[][MAX_PROGRAMAS])
{
    int ins = p->instr[p->pc];

    if (ins == 0)
    { // HALT
        p->estado = RUN_HALT; 
        p->tempo_no_estado = 0;
        p->pc++;
    }
    else if (ins < 0)
    { // I/O
        p->estado = RUN_IO; // Primeiro vai para RUN_IO
        p->bloqueado_restante = -ins;
        p->tempo_no_estado = 0;
        p->pc++;
    }
    else if (ins >= 101 && ins <= 199)
    { // JUMP
        int n = ins % 100;
        p->pc = (p->pc - n >= 0 ? p->pc - n : 0);
    }
    else if (ins >= 201 && ins <= 299)
    { // EXEC
        int pid_prog = ins % 100;
        if (total_processos < MAX_PROCESSOS && id_atual <= MAX_PROCESSOS)
        {
            if (pid_prog >= 1 && pid_prog <= MAX_PROGRAMAS)
            {
                int instr_programa[MAX_PROGRAMAS];
                int tam = 0;

                for (int row = 0; row < MAX_PROGRAMAS; row++)
                {
                    instr_programa[tam++] = prog[row][pid_prog - 1];
                    if (prog[row][pid_prog - 1] == 0)
                        break;
                }

                Processo *np = criar_processo(id_atual, instr_programa, tam);
                processos[id_atual - 1] = np;
                enqueue(new_q, np);
                total_processos++;
                id_atual++;
            }
        }
        p->pc++;
    }
    else
    { // instrução “outros positivos”
        p->pc++;
    }
}

void imprimir_estado(int t)
{
    printf("|%5d |", t);
    for (int i = 0; i < MAX_PROCESSOS; i++)
    {
        Processo *p = processos[i];
        const char *s = "        ";
        if (p)
        {
            switch (p->estado)
            {
            case NEW:
                s = "  NEW   ";
                break;
            case READY:
                s = " READY  ";
                break;
            case RUNNING:
                s = "  RUN   ";
                break;
            case RUN_IO:
                s = "  RUN   ";
                break;
            case RUN_HALT:
                s = "  RUN   ";
                break;
            case BLOCKED:
                s = " BLOCKED";
                break;
            case EXIT:
                s = "  EXIT  ";
                break;
            default:
                s = "        ";
            }
        }
        printf("%s|", s);
    }
    printf("\n");
}

int main(int argc, char *argv[])
{
    if (argc < 2)
    {
        printf("Uso: %s <input_number>\n", argv[0]);
        return 1;
    }

    int input_number = atoi(argv[1]);

    // Inicializa os programas com base no número de entrada
    int programas[MAX_INS][MAX_PROGRAMAS] = {0};

    switch (input_number)
    {
    case 0:
        memcpy(programas, input00, sizeof(input00));
        break;
    case 1:
        memcpy(programas, input01, sizeof(input01));
        break;
    case 2:
        memcpy(programas, input02, sizeof(input02));
        break;
    case 3:
        memcpy(programas, input03, sizeof(input03));
        break;
    case 4:
        memcpy(programas, input04, sizeof(input04));
        break;
    case 5:
        memcpy(programas, input05, sizeof(input05));
        break;
    default:
        fprintf(stderr, "Erro: Número de entrada inválido.\n");
        return 1;
    }

    Queue *new_q = createQueue();
    Queue *ready_q = createQueue();
    Queue *blocked_q = createQueue();
    Queue *exit_q = createQueue();
    Processo *running = NULL;

    // Cria processo inicial
    int instr_programa0[MAX_INS];
    int tam0 = 0;
    initialize_programs(input_number, 0, instr_programa0, &tam0);

    Processo *p1 = criar_processo(id_atual++, instr_programa0, tam0);
    processos[0] = p1;
    enqueue(new_q, p1);
    total_processos = 1;

    // Cabeçalho
    printf("| Time |");
    for (int i = 1; i <= MAX_PROCESSOS; i++)
        printf(" proc%02d |", i);
    printf("\n|------|");
    for (int i = 0; i < MAX_PROCESSOS; i++)
        printf("--------|");
    printf("\n");

    for (int tempo = 1; tempo <= MAX_INSTANTES; tempo++)
    {
        // 1) Atualiza BLOCKED, RUN_IO e RUN_HALT
        for (int i = 0; i < blocked_q->size; i++)
        {
            Processo *p = dequeue(blocked_q);
            p->bloqueado_restante--;
            if (p->bloqueado_restante <= 0)
            {
                p->estado = READY;
                enqueue(ready_q, p);
            }
            else
            {
                enqueue(blocked_q, p);
            }
        }

        for (int i = 0; i < MAX_PROCESSOS; i++)
        {
            Processo *p = processos[i];
            if (p && p->estado == RUN_IO)
            {
                // Transição de RUN_IO para BLOCKED no próximo instante
                p->estado = BLOCKED;
                enqueue(blocked_q, p);
            }
            if (p && p->estado == RUN_HALT)
            {
                // Incrementa o tempo no estado RUN_HALT
                p->tempo_no_estado++;
                if (p->tempo_no_estado >= 1)
                {
                    // Transição de RUN_HALT para EXIT
                    p->estado = EXIT;
                    enqueue(exit_q, p);
                }
            }
            }

        // 2) Atualiza NEW
        for (int i = 0; i < new_q->size; i++)
        {
            Processo *p = dequeue(new_q);
            if (tempo != 1)
            {
                p->tempo_no_estado++;
            }

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

        // 3) Atualiza EXIT
        for (int i = 0; i < exit_q->size; i++)
        {
            Processo *p = dequeue(exit_q);
            p->tempo_no_estado++;
            if (p->tempo_no_estado > 2) // Permite que o EXIT seja exibido por um ciclo
            {
                processos[p->id - 1] = NULL;
                destruir_processo(p);
                total_processos--;
            }
            else
            {
                enqueue(exit_q, p); // Reinsere na fila para exibição no próximo ciclo
            }
        }

        // 4) Escalonamento RR
        if (!running && !isEmpty(ready_q))
        {
            running = dequeue(ready_q);
            running->estado = RUNNING;
            running->quantum_usado = 0;
        }

        // 5) Executa
        if (running)
        {
            executar_instrucao(running, new_q, programas);
            running->quantum_usado++;
            if (running->estado != RUNNING || running->quantum_usado >= 3)
            {
                if (running->estado == RUNNING)
                {
                    running->estado = READY;
                    enqueue(ready_q, running);
                }
                else if (running->estado == EXIT)
                {
                    enqueue(exit_q, running);
                }
                running = NULL;
            }
        }

        imprimir_estado(tempo);
        if (total_processos == 0)
            break;
    }

    deleteQueue(new_q);
    deleteQueue(ready_q);
    deleteQueue(blocked_q);
    deleteQueue(exit_q);
    return 0;
}
