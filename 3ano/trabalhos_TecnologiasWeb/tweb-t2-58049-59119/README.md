# Student Housing Manager - Plataforma de Arrendamento Universitário

## 1. Introdução

Este projeto foi desenvolvido para a unidade curricular de **Tecnologias Web** da Universidade de Évora. Trata-se de uma aplicação Web Full-Stack desenhada para simular a gestão de uma associação de estudantes que facilita o arrendamento de quartos. A plataforma liga senhorios (ofertas) e estudantes (procuras), garantindo a persistência e segurança dos dados.

## 2. Arquitetura do Sistema

A aplicação segue o padrão **MVC (Model-View-Controller)**, garantindo uma separação clara de responsabilidades:

* **Backend**: Implementado em **Java** com a framework **Spring Boot**.
* **Persistência**: Base de dados relacional **PostgreSQL**.
* **Frontend**: Páginas dinâmicas utilizando **Thymeleaf**, estilizadas com **CSS3 (Flexbox)** para garantir responsividade.
* **Segurança**: Implementação de **Spring Security** para autenticação e encriptação de passwords.

## 3. Áreas de Acesso e Funcionalidades

A plataforma organiza-se em três níveis de permissão:

### Área Pública

* **Dashboard**: Visualização dos anúncios mais recentes (ofertas e procuras).
* **Pesquisa Avançada**: Filtros por zona, tipo de anúncio e pesquisa de texto livre.

### Área Privada (Utilizadores Registados)

* **Gestão de Anúncios**: Criação, edição e remoção de ofertas de quartos ou pedidos de procura.
* **Sistema de Mensagens**: Caixa de mensagens privada interna para comunicação direta entre senhorios e estudantes.
* **Perfil**: Gestão de dados de conta e preferências.

### Área de Administração

* **Controlo de Utilizadores**: Aprovação e moderação de novas contas.
* **Moderação de Conteúdo**: Validação e gestão de todos os anúncios submetidos na plataforma.

## 4. Decisões Técnicas de Implementação

* **Spring Data JPA**: Utilização de consultas personalizadas (`@Query`) e métodos de repositório avançados para filtros de pesquisa simultâneos.
* **Segurança**: Proteção contra vulnerabilidades comuns (como *Broken Object Level Authentication*) através de regras de acesso granulares.
* **UX/UI**: Design limpo e otimizado para dispositivos móveis e desktop, sem recurso a frameworks pesadas (como Bootstrap), mantendo o código leve e personalizado.

## 5. Estrutura do Repositório

* `/src/main/java`: Código fonte Java (Controllers, Services, Models, Repositories).
* `/src/main/resources/templates`: Vistas dinâmicas em Thymeleaf.
* `/src/main/resources/static`: Ficheiros estáticos (CSS, imagens, JavaScript).
* `relatorio.pdf`: Documentação técnica detalhada do projeto.

## 6. Como Executar

1. **Base de Dados**: Certifique-se de que tem o PostgreSQL instalado e uma base de dados criada.
2. **Configuração**: Ajuste as credenciais da BD no ficheiro `application.properties`.
3. **Execução**:

    ```bash
    mvn spring-boot:run
    ```

4. **Acesso**: Aceda a `http://localhost:8080` no seu navegador.
