# Da Terra - Plataforma Anti-Desperdício Alimentar

## 1. Introdução

O **Da Terra** é uma aplicação web desenvolvida para a unidade curricular de **Tecnologias Web** da Universidade de Évora. A plataforma tem como objetivo mitigar o desperdício alimentar em Évora, permitindo que restaurantes disponibilizem refeições excedentes a preços reduzidos para os clientes locais.

## 2. Funcionalidades Principal

A aplicação está dividida em três perfis de utilização:

* **Área do Cliente**:
  * Visualização de ofertas do dia com filtros de pesquisa por nome/restaurante.
  * Reserva de refeições em tempo real.
  * Listagem de restaurantes aderentes.
* **Área do Restaurante**:
  * Interface para inserção de novas ofertas (refeições, descrição, unidades e fotos).
* **Área de Administração**:
  * Listagem e pesquisa detalhada de clientes registados.
  * Gestão e visualização de todos os restaurantes e ofertas na plataforma com dados técnicos (coordenadas, proprietários, etc.).

## 3. Detalhes Técnicos

* **Frontend**: HTML5, CSS3 (Flexbox e design responsivo).
* **Lógica**: JavaScript (Vanilla JS) com comunicação assíncrona.
* **Comunicação com Servidor**:
  * Utilização de `XMLHttpRequest` (XHR) e `fetch` para pedidos GET e POST.
  * Integração com uma API REST externa para persistência de dados.
* **Otimização**: Implementação de um sistema de cache simples no lado do cliente para reduzir pedidos repetidos ao servidor.

## 4. Estrutura de Ficheiros

* `main.html`: Página de entrada e apresentação.
* `cliente.html` / `admin.html`: Dashboards de acesso por perfil.
* `Insert_Oferta.html` & `.js`: Lógica de criação de novas refeições.
* `List_*.js`: Scripts responsáveis pelo consumo da API e renderização dinâmica dos "cards" de informação.
* `stylesheet.css`: Estilização global e regras de responsividade.

## 5. Como Executar

Sendo uma aplicação *client-side*, basta abrir o ficheiro `main.html` num navegador web moderno.
> **Nota**: Para que a listagem de ofertas funcione corretamente, é necessária uma ligação ativa à internet para comunicar com o servidor da Universidade de Évora.
