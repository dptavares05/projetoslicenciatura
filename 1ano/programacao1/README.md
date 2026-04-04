#  Jogo Ouri - Programação 1

Este projeto foi desenvolvido no âmbito da disciplina de Programação 1 da Universidade de Évora , durante o ano letivo de 2023/2024.

##  Sobre o Projeto

Este projeto consiste na implementação do jogo de tabuleiro tradicional "Ouri" através da linguagem C. O Ouri é um jogo de estratégia focado em tentar capturar as pedras do adversário, evitando ao mesmo tempo que as nossas sejam capturadas. O jogo disputa-se num tabuleiro de 14 casas (6 para cada jogador e 2 depósitos para armazenar pedras capturadas). O objetivo principal é alcançar 25 pedras no depósito para vencer.

**Funcionalidades Principais:**
* **Modos de Jogo:** É possível escolher jogar contra outro jogador humano (Player vs Player) ou contra o computador (Player vs Engine).
* **Gravar e Carregar Estado:** O jogo permite gravar o estado atual do tabuleiro num ficheiro `.txt` para continuar mais tarde, bem como carregar um jogo a partir desse ficheiro.
* **Implementação de Regras Complexas:** Inclui a lógica de restrição de movimentos com pilhas de uma única pedra , saltos na distribuição de pilhas grandes (regra das 12)  e mecânicas de reações em cadeia nas capturas.

##  Documentação e Relatório Técnico

Para obter informações detalhadas sobre a arquitetura do código, explicação aprofundada das funções principais e a resolução dos desafios lógicos encontrados (como a prevenção de capturas das próprias peças), por favor consulte o documento `Relatório P1.pdf` incluído na diretoria deste repositório.

##  Como Compilar e Executar

Para testar o projeto localmente, abra o terminal na diretoria onde os ficheiros se encontram e execute os seguintes passos:

**1. Compilar o código fonte:**
```bash
gcc ouri.c -o ouri
```

**2. Executar um jogo novo:**
```bash
./ouri
```

**3. Executar carregando um jogo guardado:**
O programa suporta a leitura de um ficheiro de tabuleiro passado diretamente como argumento na inicialização.
```bash
./ouri nome_do_arquivo.txt
```

##  Autores
Trabalho de grupo realizado por:
* Diogo Tavares 
* Rodrigo Barreto
* Cristiano Cascarrinho