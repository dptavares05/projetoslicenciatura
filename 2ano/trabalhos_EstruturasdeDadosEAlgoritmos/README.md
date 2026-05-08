# Estruturas de Dados e Algoritmos II (EDA 2) - Projetos Académicos

Este repositório contém a coleção de trabalhos práticos realizados na unidade curricular de **EDA 2** da Universidade de Évora. Os projetos focam-se na resolução de problemas complexos utilizando algoritmos avançados e estruturas de dados otimizadas.

---

## 📂 Organização do Repositório

O repositório está dividido em três pastas principais, cada uma contendo a implementação em Java, o enunciado oficial e o relatório técnico explicativo de um problema complexo de Estruduras de Dados.

### 1. [Trabalho 1] The Dream Factory - Programação Dinâmica

**O Problema:** Minimizar o desperdício de capacidade ao empacotar "sonhos" de tamanhos variados em recipientes numéricos, respeitando obrigatoriamente a ordem de chegada.

* **Conceitos:** Programação Dinâmica (Bottom-up), Pesquisa Binária.
* **Complexidade:** Otimizado para processar até 100.000 sonhos de forma eficiente.

### 2. [Trabalho 2] Palm Island Neighbours - Diâmetro de Árvores

**O Problema:** Calcular a maior distância mínima (diâmetro) entre dois habitantes numa ilha cuja topologia é um grafo acíclico (árvore).

* **Conceitos:** Grafos, Procura em Largura (BFS).
* **Solução:** Implementação de duas BFS consecutivas para determinar o diâmetro da árvore em tempo linear $O(V+E)$.

### 3. [Trabalho 3] Card Exchange - Redes de Fluxo

**O Problema:** Determinar se é possível realizar uma troca circular de cartas entre todos os participantes de um festival, garantindo que todos recebem uma carta que cobiçam.

* **Conceitos:** Redes de Fluxo, Algoritmo de Edmonds-Karp (Fluxo Máximo).
* **Modelação:** Criação de uma rede com fonte e dreno onde o fluxo máximo igual ao número de participantes indica uma solução válida.

---

## Tecnologias e Ferramentas

* **Linguagem:** Java 17+
* **Algoritmos:** Programação Dinâmica, BFS, Edmonds-Karp.
* **Gestão:** Os projetos foram testados contra conjuntos de dados de larga escala para garantir eficiência temporal e espacial.

## Como Executar

Cada pasta contém a sua própria classe `Main`. Para testar qualquer um dos trabalhos via terminal:

1. Navegue até à pasta desejada: `cd Trabalho1`
2. Compile o código: `javac Main.java`
3. Execute o programa (recomenda-se redirecionar um ficheiro de texto para o input):

    ```bash
    java Main < input.txt
    ```
