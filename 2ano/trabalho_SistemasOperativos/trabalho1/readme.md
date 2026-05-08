# Simulador de Processos de Sistemas Operativos
Este projeto é um simulador de processos para Sistemas Operativos que implementa um modelo de 5 estados (NEW, READY, RUNNING, BLOCKED, EXIT). O simulador segue as especificações dadas e utiliza filas FIFO e o algoritmo Round Robin para escalonamento com quantum = 3.

## Pré-requisitos
- Compilador `gcc` instalado.
- Sistema operacional Linux/Unix (ou ambiente compatível com shell).

## Compilação
- Para compilar o projeto, execute o seguinte comando:

`make compile`

## Execução
Execute o simulador com um dos inputs de teste (0 a 5) usando os seguintes comandos:
`make run0`   # Executa o teste 0 e gera output00.out
`make run1`   # Executa o teste 1 e gera output01.out
`make run2`   # Executa o teste 2 e gera output02.out
`make run3`   # Executa o teste 3 e gera output03.out
`make run4`   # Executa o teste 4 e gera output04.out
`make run5`   # Executa o teste 5 e gera output05.out

## Limpar Arquivos Gerados
-Para remover o executável e os arquivos de saída:

`make clean`

## Comandos Disponíveis
- compile	Compila o código fonte e gera o executável.
- runX	Executa o teste X (0 a 5) e gera o output.
- clean	Remove o executável e os arquivos de saída.

## Exemplo de Uso
`make compile`   # Compila o projeto
`make run0`      # Executa o teste 0
`make clean`     # Remove arquivos gerados