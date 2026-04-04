# 🚦 Controlador de Semáforos - Sistemas Digitais

Este projeto foi desenvolvido no âmbito da disciplina de Sistemas Digitais da Universidade de Évora, durante o ano letivo de 2023/2024. 

##  Sobre o Projeto

O projeto consiste no desenho e implementação de um controlador digital para um sistema de semáforos interligados (um semáforo para carros e outro para peões). O circuito foi totalmente projetado e simulado utilizando o software **Logisim**, recorrendo a flip-flops e portas lógicas baseadas em mapas de Karnaugh.

### Semáforo de Carros
* **Entradas:** Botão de peões / Sensor de velocidade (`bp+sv`), e o sinal de vermelho dos peões (`vrp`).
* **Saídas:** Verde Carros (`vc`), Amarelo Carros (`ac`), e Vermelho Carros (`vrc`).
* **Lógica:** O estado normal é verde para os carros. A transição para amarelo e depois vermelho apenas ocorre se o botão dos peões for premido (ou o sensor de velocidade for ativado) E o semáforo dos peões já estiver no estado vermelho.

###  Semáforo de Peões
* **Entradas:** Sinal de vermelho dos carros (`vrc`).
* **Saídas:** Verde Peões (`vp`), Amarelo Peões (`ap` - intermitente/aviso), e Vermelho Peões (`vrp`).
* **Lógica:** O estado normal é vermelho para os peões. A transição para verde apenas é autorizada quando o sinal dos carros reporta o estado vermelho (`vrc=1`). Adicionalmente, inclui lógica para controlar um display de 7 segmentos que faz a contagem decrescente do tempo de passadeira.

##  Tecnologias e Componentes Utilizados

* **Software de Simulação:** Logisim (versão 2.7.1)
* **Flip-Flops:** Edge-Triggered D-Type Flip-Flops (escolhidos para o controlo de estado das máquinas de Moore/Mealy).
* **Portas Lógicas:** AND, OR e NOT.
* **Outros:** Display de 7 Segmentos e LEDs (Verde, Amarelo, Vermelho).
* **Otimização:** As expressões lógicas que alimentam o circuito foram todas otimizadas com recurso a Mapas de Karnaugh (K-Maps).

##  Documentação e Relatório Técnico

Para detalhes profundos sobre a máquina de estados, consulte o documento `sd.pdf` incluído neste repositório. O relatório contém:
* Os Modelos ASM (Algorithmic State Machine) para ambos os controladores.
* As Tabelas de Transição de Estado.
* A resolução completa de todos os Mapas de Karnaugh para as variáveis de estado (`d0`, `d1`, `d2`, `d3`), saídas dos semáforos e entradas do display de 7 segmentos (`a` a `g`).

##  Como Simular o Circuito

1.  Faça o download do [Logisim](http://www.cburch.com/logisim/), com o Java instalado.
2.  Abra o Logisim.
3.  Vá a `File` -> `Open` e selecione o ficheiro `circuito (2).circ` presente nesta pasta.
4.  No painel esquerdo, expanda a vista e dê duplo clique em `main` para ver a vista geral do projeto.
5.  Para simular o funcionamento temporal:
    * Vá ao menu `Simulate` e certifique-se de que a opção `Simulation Enabled` está ativa.
    * Ainda no menu `Simulate`, ative a opção `Ticks Enabled` ou ajuste a `Tick Frequency` para ver a contagem do display e as transições de cor a acontecerem automaticamente no tempo.
    * Use a ferramenta `Poke Tool` (ícone da mão) para alterar o estado do pino `bpsv` (simulando a pressão do botão por um peão).
