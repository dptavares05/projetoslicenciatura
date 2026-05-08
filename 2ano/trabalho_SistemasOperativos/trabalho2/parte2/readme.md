# Parte 2 - Simulador de Escalonamento com Memória Virtual
Integra o simulador de memória com um escalonador de processos Round-Robin, simulando um sistema operativo com múltiplos estados, acessos à memória virtual e tratamento de erros.

## Pré-requisitos
- Compilador `gcc` instalado.
- Sistema operacional Linux/Unix (ou ambiente compatível com shell).

## Compilação
- Para compilar o projeto, execute o seguinte comando:

`make compile`

## Execução
Execute o simulador com um dos inputs de teste (0 a 5) usando os seguintes comandos:
`make run0`   # Executa o teste 0 e gera output2T00.out
`make run1`   # Executa o teste 1 e gera output2T01.out
.
.
.
`make run11`   # Executa o teste 5 e gera output2T11.out

## Limpar Arquivos Gerados
-Para remover o executável e os arquivos de saída:

`make clean`

## Comandos Disponíveis
- compile	Compila o código fonte e gera o executável.
- runX	Executa o teste X (0 a 11) e gera o output.
- clean	Remove o executável e os arquivos de saída.

## Exemplo de Uso
`make compile`   # Compila o projeto
`make run0`      # Executa o teste 0
`make clean`     # Remove arquivos gerados