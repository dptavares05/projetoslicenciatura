# OppNetChat - Simulação de Redes Oportunistas

Este projeto foi desenvolvido no âmbito da unidade curricular de Sistemas Móveis e Ambientes (SMA) da Universidade de Évora. Consiste num protótipo (mock-up funcional) de uma aplicação móvel para Android, desenhado para simular a interface e a gestão de estados de um sistema de troca de mensagens em redes oportunistas (ambientes descentralizados sem infraestrutura de Internet).

## Funcionalidades Simuladas

* **Arquitetura de Navegação:** Implementação de uma prova de conceito visual que demonstra como um utilizador interagiria com uma rede intermitente.
* **Gestão Visual de Estados do Nó:** A aplicação simula a transição do dispositivo entre dois papéis operacionais distintos na rede, através de ecrãs e interfaces dedicadas:
  * **Modo Seed (Semente):** Simulação do ecrã de nó de origem, com interface gráfica para a criação e injeção da mensagem inicial na rede.
  * **Modo Helper (Auxiliar):** Simulação do ecrã de nó retransmissor. Representa visualmente a receção de mensagens de nós vizinhos e a gestão de um *buffer* temporário de retenção antes do reencaminhamento teórico.

## Tecnologias e Ferramentas

* **Linguagem de Programação:** Kotlin
* **Plataforma:** Android SDK
* **Ambiente de Desenvolvimento:** Android Studio
* **Foco do Projeto:** UI/UX, Gestão de Ciclo de Vida de Activities/Fragments e simulação de estados.

## Como Compilar e Testar

1. Clone este repositório para a sua máquina local.
2. Abra o projeto utilizando o Android Studio.
3. Aguarde que o Gradle sincronize as dependências.
4. Compile a aplicação selecionando `Build > Make Project`.
5. Execute a aplicação num emulador ou dispositivo físico selecionando `Run > Run 'app'`.

*Nota: Este projeto serve como uma demonstração de interface gráfica e gestão de ecrãs (Mock-up). A transferência física de dados entre dispositivos via P2P/Bluetooth Low Energy não está ativada nesta versão de demonstração.*

## Autoria

* Diogo Tavares (Nº 58049)