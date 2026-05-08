# P2 — Guru WordGame (Java)

## 1. Introdução

Este projeto foi desenvolvido no âmbito da disciplina de **Programação II** da Universidade de Évora. O **Guru** é um jogo de adivinhação de palavras (estilo "Word Connect") implementado em Java, no qual o jogador deve formar palavras válidas a partir de um conjunto de letras fornecidas para avançar de nível.

## 2. Funcionalidades

O sistema oferece uma experiência completa de jogo através da consola, incluindo:

- **Gestão de níveis**: carregamento de níveis a partir do ficheiro de texto `ficheiro_niveis.txt`.
- **Sistema de moedas**: o jogador ganha moedas ao acertar palavras e pode usá-las para obter pistas.
- **Validação com dicionário**: integração com o dicionário `portuguese-large.txt` para validar palavras introduzidas pelo utilizador.
- **Persistência de dados**: possibilidade de salvar o estado do jogo (nível atual e moedas) e retomá-lo mais tarde através do ficheiro `game_state.txt`.
- **Modo criativo**: funcionalidade para criar novos níveis personalizados diretamente na aplicação.

## 3. Decisões de implementação

- **Estruturas de dados**: utilização de `HashSet` para pesquisas rápidas no dicionário e `ArrayList` para a gestão dinâmica das palavras e letras de cada nível.
- **Manipulação de ficheiros**: leitura e escrita de ficheiros com `BufferedReader` e `PrintWriter` para garantir que o progresso não se perde.
- **Lógica de validação**: algoritmo que verifica não só se a palavra existe no dicionário, mas também se pode ser formada com a quantidade de letras disponíveis no nível atual.

## 4. Estrutura do projeto

- `Guru.java`: classe principal contendo a lógica do jogo e a interface de consola.
- `portuguese-large.txt`: dicionário utilizado para validação.
- `ficheiro_niveis.txt`: base de dados dos níveis do jogo.
- `relatorio.pdf`: documentação técnica do trabalho.

## 5. Como executar

### Pré-requisitos

- Java Development Kit (JDK) 8 ou superior instalado.

### Passo a passo

1. Certifique-se de que os ficheiros `.txt` estão na mesma pasta que o ficheiro `Guru.java`.
2. Abra um terminal na pasta do projeto.
3. Compile o código:

    ```bash
    javac Guru.java
    ```

4. Execute o jogo:

    ```bash
    java Guru
    ```

## 6. Autores

- Pedro Jorge
- Diogo Tavares
- João Fitas
