.data
NomeFicheiro:        .asciz "cat_noisy.gray"  # nome do arquivo a ser lido
buffer:          .space 239600            # buffer para os dados da imagem
buffer_Saida:   .space 239600            # buffer para armazenar a imagem de saida (filtro média)
buffer_Saida2:  .space 239600            # buffer para armazenar a imagem de saida (filtro mediana)
nomeFicheiro_Saida: .asciz "cat_noisy2.gray"     #Imagem com filtro da média
nomeFicheiro_Saida2: .asciz "cat_noisy3.gray"   #Imagem com filtro da mediana
comprimento:     .word 400                # comprimento da imagem 
largura:         .word 599                # largura da imagem 
array_vizinhanca:.space 9                 # array para armazenar valores da vizinhança (9 bytes, pois há uma matriz 3x3)

.text
.globl main

main:
    # abre o arquivo
    li a7, 1024                  # syscall number para abrir arquivo
    la a0, NomeFicheiro              # endereço do nome do arquivo
    li a1, 0                     # load immediate a 0 para abrir em modo leitura
    ecall
    mv s0, a0                    # retorna o file descriptor em a0 e guarda-se este valor no s0 para podermos ler depois

    # lê os dados do arquivo
    li a7, 63                    # syscall number para ler do arquivo
    mv a0, s0                    # descritor de arquivo
    la a1, buffer                # buffer onde armazenar os dados
    li a2, 239600                # número de bytes para ler
    ecall

    # fecha o arquivo
    li a7, 57                    # syscall para fechar arquivo
    mv a0, s0                    # descritor de arquivo
    ecall

    #criar variáveis importantes para o filtro da média
    li t0, 0                     # t0 = x (coluna)
    li t1, 0                     # t1 = y (linha)

######################################################
# Funcao: filtro_media
# Descricao: Esta função aplica o filtro de média completo aos valores guardados no buffer, 
#                 somando os valores de intensidade dos pixeis em volta do pixel escolhido(inclusivé)
#                 e fazendo a media destes valores após o qual aplica o valor médio da intensidade 
#		   ao pixel do meio.A função faz isto com todos os valores do ficheiro aberto e depois
#                guarda-o no ficheiro de output(nomeFicheiro_Saida)
#
#  Argumentos:
#
# buffer- reque que o buffer tenha os dados do ficheiro .gray onde queremos aplicar o filtro
#
#  Retorna:
#
# buffer_Saida- buffer com os valores da média já aplicados
# 
######################################################
filtro_media:
    # Inicializar variáveis
    li t0, 0                     # t0 = x (coluna)
    li t1, 0                     # t1 = y (linha)

for1:
    lw t2, comprimento           # carregar altura
    bge t1, t2, escrever    # if y >= comprimento, prepare to write
    li t0, 0                     # resetar x

for2:
    lw t2, largura               # carregar largura
    bge t0, t2, prox_linha         # if x >= largura, prox_linha

    # Inicializar soma e contador
    li s1, 0                     # s1 = soma dos valores dos pixels
    li s2, 0                     # s2 = contador de pixels
    li s3, 1                     # valor a 1 para comparar o i e o j
    li t3, -1                    # i = -1

for3:
    bgt t3, s3, prox_pixel       # if i > s3, prox_pixel
    li t4, -1                    # j = -1

for4:
    bgt t4, s3, proximo_i           # if j > s3, proximo_i

    add t5, t0, t4               # nx = x + j
    add t6, t1, t3               # ny = y + i

    # Verificar se nx e ny estão dentro dos limites da imagem
    blt t5, zero, continuar_loop_interior # if nx < 0, continuar
    blt t6, zero, continuar_loop_interior # if ny < 0, continuar
    lw t2, largura
    bge t5, t2, continuar_loop_interior   # if nx >= largura, continuar
    lw t2, comprimento
    bge t6, t2, continuar_loop_interior   # if ny >= comprimento, continuar

    # Calcular Índice linear e somar o valor do pixel
    lw t2, largura
    mul t2, t6, t2               # ny * largura
    add t2, t2, t5               # ny * largura + nx
    la s4, buffer                # endereço do buffer de entrada
    add s4, s4, t2
    lbu t2, 0(s4)
    add s1, s1, t2               # soma += valor do pixel
    addi s2, s2, 1               # contador++

continuar_loop_interior:
    addi t4, t4, 1               # incrementar j
    j for4

proximo_i:
    addi t3, t3, 1               # incrementar i
    j for3

prox_pixel:
    # Calcular a média
    div s1, s1, s2               # soma / contador
    mv t2, s1                    # resultado da divisão

    # Armazenar o valor no buffer de saida
    lw t5, largura
    mul t5, t1, t5               # y * largura
    add t5, t5, t0               # y * largura + x
    la t6, buffer_Saida
    add t6, t6, t5
    sb t2, 0(t6)

    addi t0, t0, 1               # incrementar x
    j for2

prox_linha:
    addi t1, t1, 1               # incrementar y
    j for1

escrever:
    # Abrir o arquivo de saida
    li a7, 1024                  # syscall para abrir arquivo
    la a0, nomeFicheiro_Saida       # endereço do nome do arquivo
    li a1, 1                     # modo de escrita (1 para escrita)
    ecall
    mv s0, a0                    # armazenar o descritor de arquivo em s0

    # Escrever os dados no arquivo de saida
    li a7, 64                    # syscall para escrever no arquivo
    mv a0, s0                    # descritor de arquivo
    la a1, buffer_Saida         # endereço do buffer de saida
    li a2, 239600                # número de bytes para escrever
    ecall

    # Fechar o arquivo de saida
    li a7, 57                    # syscall para fechar arquivo
    mv a0, s0                    # descritor de arquivo
    ecall

# Resetar os valores antes de entrar no filtro_mediana
li t0,0
li t1,0
li t2,0
li t3,0
li t4,0
li t5,0
li t6,0
li s0,0
li s1,0
li s2,0
li s3,0

######################################################
# Funcao: filtro_mediana
# Descricao: Esta função pega nos valores da vizinhança de um pixel e escolhe a mediana 
#                 da intensidade dos pixeis em volta do escolhido,inclusivé, e depois aplica este
#                  valor ao pixel do centro.
#                
#               
#                
#  Argumentos:
#
# buffer- reque que o buffer tenha os dados do ficheiro .gray onde queremos aplicar o filtro
#
#  Retorna:
#
# buffer_Saida- buffer com os valores da mediana já aplicados
######################################################
filtro_mediana:
    li t1, 0                     # resetar y

for1_mediana:
    lw t2, comprimento           # carregar altura
    bge t1, t2, escrever2   # if y >= comprimento, prepare to write
    li t0, 0                                   # resetar x

for2_mediana:
    lw t2, largura               # carregar largura
    bge t0, t2, prox_linha_mediana # if x >= largura, prox_linha

    # Inicializar array de valores
    li s5, 0                     # s5 = número de valores no array
    li s3, 1                     # valor a 1 para comparar o i e o j
    li t3, -1                    # i = -1

for3_mediana:
    bgt t3, s3, processar_mediana   # if i > s3, processar a mediana
    li t4, -1                    # j = -1

for4_mediana:
    bgt t4, s3, próximo_i_mediana   # if j > s3, proximo_i

    add t5, t0, t4               # nx = x + j
    add t6, t1, t3               # ny = y + i

    # Verificar se nx e ny estão dentro dos limites da imagem
    blt t5, zero, Continuar_Loop_Interior_mediana # if nx < 0, continuar
    blt t6, zero, Continuar_Loop_Interior_mediana # if ny < 0, continuar
    lw t2, largura
    bge t5, t2, Continuar_Loop_Interior_mediana   # if nx >= largura, continuar
    lw t2, comprimento
    bge t6, t2, Continuar_Loop_Interior_mediana   # if ny >= comprimento, continuar

    # Calcular Índice linear e adicionar o valor ao array
    lw t2, largura
    mul t2, t6, t2               # ny * largura
    add t2, t2, t5               # ny * largura + nx
    la s4, buffer                # endereço do buffer de entrada
    add s4, s4, t2
    lbu t2, 0(s4)
    la s4, array_vizinhanca      # resetar o ponteiro do array de vizinhança
    add s4, s4, s5                       # array_vizinhanca[s5]
    sb t2, 0(s4)                          # armazenar o valor no array
    addi s5, s5, 1                       # contador++

Continuar_Loop_Interior_mediana:
    addi t4, t4, 1               # incrementar j
    j for4_mediana

próximo_i_mediana:
    addi t3, t3, 1               # incrementar i
    j for3_mediana

processar_mediana:
    la s4, array_vizinhanca
    li t3, 1

insertion_sort:
    beq t3, s5, procurar_Mediana       # se t3 >= s5, array está ordenado

    la s4, array_vizinhanca
    add s4, s4, t3                # array_vizinhanca[t3]
    lbu t6, 0(s4)                 # t6 = array_vizinhanca[t3]
    addi t4, t3, -1               # t4 = t3 - 1

comparar_elementos:
    blt t4, zero, inserir_elementos  # if t4 < 0, inserir elemento
    la s4, array_vizinhanca
    add s4, s4, t4                # array_vizinhanca[t4]
    lbu s6, 0(s4)                 # s6 = array_vizinhanca[t4]

    bge s6, t6, inserir_elementos    # if s6 >= t6, inserir elemento
    la s4, array_vizinhanca
    add s4, s4, t4
    addi s4, s4, 1                # array_vizinhanca[t4+1]
    sb s6, 0(s4)                  # array_vizinhanca[t4+1] = s6
    addi t4, t4, -1               # t4--

    j comparar_elementos            # continuar comparando

inserir_elementos:
    la s4, array_vizinhanca
    add s4, s4, t4
    addi s4, s4, 1                # array_vizinhanca[t4+1]
    sb t6, 0(s4)                  # array_vizinhanca[t4+1] = t6
    addi t3, t3, 1                # incrementar t3
    j insertion_sort              # continuar insertion sort

procurar_Mediana:
    # Achar o valor mediano
    la s4, array_vizinhanca
    addi s4, s4, 4                # array_vizinhanca[4] (elemento mediano)
    lbu t2, 0(s4)

    # Armazenar o valor no buffer de saida
    lw t5, largura
    mul t5, t1, t5               # y * largura
    add t5, t5, t0               # y * largura + x
    la t6, buffer_Saida2
    add t6, t6, t5
    sb t2, 0(t6)

    addi t0, t0, 1               # incrementar x
    j for2_mediana

prox_linha_mediana:
    addi t1, t1, 1               # incrementar y
    j for1_mediana

escrever2:
    # Abrir o arquivo de saida
    li a7, 1024                  # syscall para abrir arquivo
    la a0, nomeFicheiro_Saida2      # endereço do nome do arquivo
    li a1, 1                     # modo de escrita (1 para escrita)
    ecall
    mv s0, a0                    # armazenar o descritor de arquivo em s0

    # Escrever os dados no arquivo de saida
    li a7, 64                    # syscall para escrever no arquivo
    mv a0, s0                    # descritor de arquivo
    la a1, buffer_Saida2        # endereço do buffer de saida
    li a2, 239600                # número de bytes para escrever
    ecall

    # fechar o arquivo de saida
    li a7, 57                    # syscall para fechar arquivo
    mv a0, s0                    # descritor de arquivo
    ecall

    # finalizar
    li a7, 93                    # syscall para sair
    li a0, 0
    ecall
