# Image Denoising em Assembly RISC-V

## 1. Introdução

Este projeto foi desenvolvido para a unidade curricular de **Arquitetura de Computadores I** da Universidade de Évora. O objetivo principal é a implementação de algoritmos de processamento de imagem em linguagem **Assembly RISC-V** para a remoção de ruído em imagens monocromáticas (tons de cinzento).

O programa processa ficheiros de imagem no formato `.gray`, aplicando filtros espaciais para suavizar imperfeições e restaurar a qualidade visual.

## 2. Filtros Implementados

Foram implementadas duas técnicas clássicas de filtragem:

### A. Filtro da Média (`filtro-media`)

* **Funcionamento**: Para cada píxel, calcula a média aritmética das intensidades de uma vizinhança 3x3.
* **Objetivo**: Suavizar a imagem, reduzindo variações bruscas de intensidade.
* **Tratamento de Bordas**: Implementa verificação de limites para evitar acessos de memória inválidos fora da matriz da imagem.

### B. Filtro da Mediana (`filtro-mediana`)

* **Funcionamento**: Ordena os valores da vizinhança 3x3 e substitui o píxel central pelo valor mediano.
* **Algoritmo Auxiliar**: Utiliza o **Insertion Sort** para a ordenação dos 9 bytes da vizinhança.
* **Vantagem**: Extremamente eficaz na remoção de ruído do tipo "sal e pimenta" sem desfocar excessivamente as arestas da imagem.

## 3. Especificações Técnicas

* **Arquitetura**: RISC-V (32-bit).
* **Simulador**: RARS (RISC-V Assembler and Runtime Simulator).
* **Gestão de Memória**: Utilização de buffers estáticos para armazenamento da imagem original e das imagens de saída (`buffer_Saida` e `buffer_Saida2`).
* **I/O de Ficheiros**: Uso de *syscalls* para abertura, leitura e escrita de ficheiros binários.

## 4. Estrutura do Repositório

* `filtro.asm`: Código-fonte principal em Assembly contendo toda a lógica de processamento e filtros.
* `relatório.pdf`: Documentação técnica detalhada com análise de performance e comparação visual dos resultados.
* `cat_noisy.gray`: Ficheiro de entrada (imagem original com ruído).
* `enunciado.pdf`: Descrição dos requisitos e objetivos do trabalho.

## 5. Como Executar

1. Descarregue o simulador [RARS](https://github.com/TheThirdOne/rars).
2. Certifique-se de que o ficheiro `cat_noisy.gray` está na mesma pasta que o código.
3. Abra o `filtro.asm` no RARS.
4. Monte (Assemble) e execute o programa.
5. O programa irá gerar dois ficheiros:
    * `cat_noisy2.gray` (Resultado do Filtro da Média)
    * `cat_noisy3.gray` (Resultado do Filtro da Mediana)
