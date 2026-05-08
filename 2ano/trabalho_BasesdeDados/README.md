# Projeto: Sistema de Gestão de Membros e Receitas de Doces (SQL)

## 1. Introdução

Este projeto foi desenvolvido no âmbito da unidade curricular de **Bases de Dados** da Universidade de Évora. O objetivo principal consistiu na modelação e implementação de uma base de dados relacional capaz de gerir informações sobre membros, as suas relações de amizade, a criação e confeção de doces, e a gestão detalhada de ingredientes e custos associados.

## 2. Estrutura da Base de Dados

A base de dados foi desenhada para suportar um ecossistema social de partilha de culinária. As principais entidades e relações incluem:

* **Membros**: Utilizadores da plataforma com dados de nascimento e país de origem.
* **Amizades**: Relação recursiva que permite conectar membros entre si.
* **Doces e Ingredientes**: Catálogo de receitas com descrição, género e cálculo dinâmico de custos com base na quantidade e preço unitário dos ingredientes.
* **Confeções (Tabela 'Fez')**: Registo histórico de quando um membro confeciona um doce, incluindo avaliações de aspeto, sabor e tempo despendido.

## 3. Tecnologias Utilizadas

* **SQL (Standard Query Language)**: Para definição de dados (DDL) e manipulação de dados (DML).
* **Álgebra Relacional**: Utilizada na fase de planeamento para otimização das consultas complexas.

## 4. Funcionalidades e Consultas Implementadas

O projeto resolve uma série de problemas de negócio através de SQL avançado, incluindo:

* **Gestão de Custos**: Cálculo do custo total de um doce somando todos os seus ingredientes.
* **Análise de Desempenho**: Identificação dos membros que criaram mais doces ou que têm a melhor média de aspeto/sabor nas suas confeções.
* **Filtros Complexos**: Consultas para encontrar membros que nunca usaram certos ingredientes (ex: Baunilha) ou que partilham todos os amigos de um utilizador específico.

## 5. Estrutura do Repositório

* `Criação da Base de Dados.txt`: Script SQL contendo os comandos `CREATE TABLE` e as definições de chaves primárias e estrangeiras.
* `Resposta as perguntas em SQl.txt`: Script contendo as resoluções de exercícios práticos, incluindo `INSERT INTO` e consultas `SELECT` complexas (Joins, Subqueries, Agregações).
* `Relatório Base de Dados.pdf`: Documentação completa com as soluções em Álgebra Relacional e explicações teóricas.

## 6. Como Utilizar

1. **Criação**: Execute o conteúdo do ficheiro `Criação da Base de Dados.txt` num motor de base de dados compatível com SQL (como MySQL, PostgreSQL ou SQLite).
2. **Povoamento**: Utilize os comandos de `INSERT` presentes no ficheiro de respostas para carregar os dados de teste.
3. **Consultas**: Execute as queries do ficheiro de respostas para verificar o funcionamento da lógica de negócio.
