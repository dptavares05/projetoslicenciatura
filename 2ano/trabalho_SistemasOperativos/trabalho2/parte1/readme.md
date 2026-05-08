## Parte 1 – Simulador de Gestão de Memória
Implementa um simulador de gestão de memória com paginação, utilizando os algoritmos de substituição FIFO e LRU para tratar page faults durante acessos de múltiplos processos.

## Pré-requisitos
- Compilador `gcc` instalado.
- Sistema operacional Linux/Unix (ou ambiente compatível com shell).

## Compilação
- Para compilar o projeto, execute o seguinte comando:

`make compile`

## Execução
Execute o simulador com um dos inputs de teste (0 a 5) usando os seguintes comandos:
`make lru-0`   # Executa o teste 0 e gera lru00.out
`make lru-1`   # Executa o teste 1 e gera lru01.out
.
.
`make lru-5`   # Executa o teste 5 e gera lru05.out
`make fifo-0`   # Executa o teste 0 e gera fifo00.out
.
.
`make fifo-5`   # Executa o teste 5 e gera fifo05.out
## Limpar Arquivos Gerados
-Para remover o executável e os arquivos de saída:

`make clean`

## Comandos Disponíveis
- compile	Compila o código fonte e gera o executável.
- runX	Executa o teste X (0 a 11) e gera o output.
- clean	Remove o executável e os arquivos de saída.

## Exemplo de Uso
`make compile`   # Compila o projeto
`make lru-0`     # Executa o teste 0 em lru
`make fifo-0`    # Executa o teste 0 em fifo
`make clean`     # Remove arquivos gerados