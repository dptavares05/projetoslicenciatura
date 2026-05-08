// inputs_part1.h
#include <stddef.h> 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#ifndef INPUTS_PART1_H
#define INPUTS_PART1_H

// Declaração dos arrays de input. A definição está em inputs_part1.c
extern int inputP1Mem00[];
extern int inputP1Exec00[];
extern int inputP1Mem01[];
extern int inputP1Exec01[];
extern int inputP1Mem02[];
extern int inputP1Exec02[];
extern int inputP1Mem03[];
extern int inputP1Exec03[];
extern int inputP1Mem04[];
extern int inputP1Exec04[];
extern int inputP1Mem05[];
extern int inputP1Exec05[];

// Estrutura para facilitar o acesso aos pares de input
typedef struct {
    int* mem_data;
    size_t mem_size;
    int* exec_data;
    size_t exec_size;
} InputSet;

// Declaração da função que vai selecionar o input
InputSet get_input_set(int index);

#endif // INPUTS_PART1_H