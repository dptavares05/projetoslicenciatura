# Brain Tumor Diagnosis Prediction - Machine Learning

## 1. Introdução

Este projeto foi desenvolvido no âmbito da unidade curricular de **Aprendizagem Automática** da Universidade de Évora. O objetivo principal é a construção de modelos preditivos para classificar tumores cerebrais como **Malignos (1)** ou **Benignos (0)**, utilizando atributos demográficos e medidas de textura extraídas de imagens de Ressonância Magnética (ADC).

O projeto foi submetido ao desafio Kaggle "Diagnóstico de Tumores Cerebrais", focando-se na maximização da métrica **F1-Score**.

## 2. O Conjunto de Dados

Os dados apresentam um desafio de agregação, uma vez que a unidade de predição é o **paciente**, mas os dados brutos contêm múltiplas fatias (slices) por indivíduo.

* **Atributos**: Idade, Sexo e 18 medidas de textura de imagens ADC.
* **Agregação**: Implementámos técnicas de agregação (média) para reduzir a variabilidade intra-paciente e transformar o problema numa classificação binária robusta.

## 3. Metodologia e Modelos

Explorámos diversos algoritmos para encontrar o equilíbrio ideal entre complexidade e capacidade de generalização:

* **Modelos Testados**: Naive Bayes, Logistic Regression, SVM, Random Forest e Decision Trees.
* **Seleção de Modelos**: Optámos por modelos com menor propensão ao *overfitting* devido ao tamanho médio do conjunto de dados.
* **Otimização**: Ajuste de hiper-parâmetros para garantir robustez tanto na leaderboard pública como na privada do Kaggle.

## 4. Resultados de Destaque

* **Melhor Desempenho**: O modelo baseado em **Decision Tree** obteve o score mais elevado na parte privada do teste (**0.833**), demonstrando uma excelente capacidade de generalização em dados novos.
* **Conclusão Técnica**: Observámos que modelos mais simples como Regressão Logística e Naive Bayes serviram como excelentes *baselines*, superando modelos complexos em cenários de alta variância.

## 5. Estrutura do Repositório

* `notebook_grupoAA.ipynb`: Notebook principal com todo o pipeline de Data Science (Limpeza, EDA, Treino e Avaliação).
* `notebook_grupoAA.pdf`: Relatório técnico detalhado com a justificação das escolhas algorítmicas e análise de resultados.
* `submission_...csv`: Ficheiros de submissão gerados para o Kaggle.

## 6. Como Correr o Projeto

### Pré-requisitos

* Python 3.8+
* Bibliotecas: `pandas`, `scikit-learn`, `numpy`, `matplotlib`, `seaborn`.

### Execução

1. Clona o repositório.
2. Instala as dependências:

    ```bash
    pip install pandas scikit-learn numpy matplotlib seaborn
    ```

3. Abre o Jupyter Notebook para visualizar a análise:

    ```bash
    jupyter notebook notebook_grupoAA.ipynb
    ```
