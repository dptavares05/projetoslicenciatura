# Sistema de Comunicação em Tempo Real (UDP/TCP)

Este projeto foi desenvolvido no âmbito da unidade curricular de Redes de Computadores da Universidade de Évora (ano letivo 2024/2025). O trabalho consiste numa aplicação cliente-servidor de comunicação estruturada para operar em tempo real, efetuando a transmissão de dados letra a letra sem o uso tradicional de um botão de submissão de mensagens.

A arquitetura do sistema assenta no protocolo UDP para assegurar uma comunicação de baixa latência e integra o protocolo TCP para garantir a transferência fiável de ficheiros entre os utilizadores.

## Funcionalidades Implementadas

* **Comunicação em Tempo Real:** A introdução de caracteres é transmitida e processada instantaneamente, permitindo aos utilizadores visualizar a construção das mensagens em tempo real.
* **Autenticação de Utilizadores:** Sistema de registo e controlo de acesso obrigatório, com armazenamento persistente das credenciais no ficheiro `contas.txt`.
* **Canais de Comunicação:** Suporte integrado para o canal global (`[all]`) e para o envio de mensagens privadas diretas entre utilizadores (`[dm]`).
* **Gestão de Grupos:** Permite a criação de grupos de comunicação, adição e remoção de membros, bem como a eliminação da estrutura pelo respetivo administrador. A persistência dos dados é assegurada através do ficheiro `groups.txt`.
* **Transferência de Ficheiros:** Abordagem híbrida em que a oferta do ficheiro é sinalizada via UDP e a subsequente transferência ocorre via TCP, estabelecida numa porta alocada de forma dinâmica (entre 20000 e 21000), assegurando uma ligação direta e segura entre pares.
* **Controlo de Inatividade:** Monitorização contínua por parte do servidor, que procede à desconexão automática de clientes inativos após decorridos 60 segundos (`CLIENT_TIMEOUT`).
* **Fiabilidade e Controlo de Erros:** O sistema quantifica e regista a perda de datagramas através da atribuição de números de sequência aos pacotes UDP, produzindo relatórios periódicos de estado (checkpoints) diretamente no terminal do servidor.

## Como Compilar e Executar

O código fonte encontra-se segmentado em dois ficheiros principais: a componente cliente (`clFINAL.c`) e a componente servidor (`svFINAL.c`). O servidor está configurado, por defeito, para escutar ligações na porta `12345`.

**1. Compilação do Servidor:**
```bash
gcc svFINAL.c -o server
