#include "ouri.h"

// Função para guardar o tabuleiro
void guardarTabuleiro(int tabuleiro[14], char *nomeArquivo)
{
    FILE *arquivo = fopen(nomeArquivo, "w");
    if (arquivo == NULL)
    {
        printf("Erro ao abrir o arquivo para escrita.\n");
        exit(1);
    }
    fprintf(arquivo, "%d\n", tabuleiro[DEPOSITO_JOGADOR2]);

    for (int i = NUM_TOTAL_CASAS - 1; i >= NUM_CASAS; i--)
    {
        fprintf(arquivo, "%d ", tabuleiro[i]);
    }
    fprintf(arquivo, "\n");

    fprintf(arquivo, "%d\n", tabuleiro[DEPOSITO_JOGADOR1]);

    for (int i = 0; i < NUM_CASAS; i++)
    {
        fprintf(arquivo, "%d ", tabuleiro[i]);
    }
    fprintf(arquivo, "\n");

    fclose(arquivo);
}

// Função para carregar o tabuleiro
int carregarTabuleiro(int tabuleiro[14], char *nomeArquivo)
{
    FILE *arquivo = fopen(nomeArquivo, "r");
    if (arquivo == NULL)
    {
        printf("Erro ao abrir o arquivo para leitura.\n");
        return 0;
    }
    fscanf(arquivo, "%d", &tabuleiro[DEPOSITO_JOGADOR2]);
    for (int i = NUM_TOTAL_CASAS - 1; i >= NUM_CASAS; i--)
    {
        fscanf(arquivo, "%d", &tabuleiro[i]);
    }

    fscanf(arquivo, "%d", &tabuleiro[DEPOSITO_JOGADOR1]);
    for (int i = 0; i < NUM_CASAS; i++)
    {
        fscanf(arquivo, "%d", &tabuleiro[i]);
    }

    fclose(arquivo);
    return 1;
}

// Função para escolher entre player vs player (pvp) e player vs engine (pve)
void escolha(char *pvp)
{
    printf("Quer jogar contra outro Jogador (j) ou contra o Computador (c)? ");
    scanf(" %c", pvp);
}

// Função para verificar se o jogo já acabou (0 se continua e 1 se acabou)
int fim(int tabuleiro[14])
{
    if (tabuleiro[DEPOSITO_JOGADOR2] >= 25)
    {
        printf("O jogador 2 ganhou\n");
        printf("Fim\n");
        return 1;
    }
    else if (tabuleiro[DEPOSITO_JOGADOR1] >= 25)
    {
        printf("O jogador 1 ganhou\n");
        printf("Fim\n");
        return 1;
    }
    else if (tabuleiro[DEPOSITO_JOGADOR1] == 24 && tabuleiro[DEPOSITO_JOGADOR2] == 24)
    {
        printf("O jogo ficou empatado\n");
        printf("Fim\n");
        return 1;
    }
    else
    {
        return 0;
    }
}

// Função para imprimir o tabuleiro atual
void imprimirTabuleiro(int tabuleiro[14])
{
    printf("|---|---|---|---|---|---|---|---|\n|   |%2d |%2d |%2d |%2d |%2d |%2d |   |\n|%2d |-----------------------|%2d |\n|   |%2d |%2d |%2d |%2d |%2d |%2d |   |\n|---|---|---|---|---|---|---|---|\n", tabuleiro[11], tabuleiro[10], tabuleiro[9], tabuleiro[8], tabuleiro[7], tabuleiro[6], tabuleiro[DEPOSITO_JOGADOR2], tabuleiro[DEPOSITO_JOGADOR1], tabuleiro[0], tabuleiro[1], tabuleiro[2], tabuleiro[3], tabuleiro[4], tabuleiro[5]);
}

// Função para fazer as jogadas
void jogada(int tabuleiro[14], int jogadorAtual, int pvp)
{
    int n = 0;
    int pulosConsecutivos[2] = {0};
    int casaJogada = -1; // -1 porque não existe no tabuleiro

    while (fim(tabuleiro) == 0)
    {
        imprimirTabuleiro(tabuleiro);

        // Verificar se o jogador atual têm peças para jogar
        int temPecas = 0;
        for (int i = jogadorAtual * NUM_CASAS; i < jogadorAtual * NUM_CASAS + NUM_CASAS; i++)
        {
            if (tabuleiro[i] > 0)
            {
                temPecas = 1;
                break;
            }
        }

        if (!temPecas)
        {
            jogadorAtual = (jogadorAtual + 1) % 2;
            printf("Jogador %d não tem peças para jogar.O jogador %d é forçado a jogar de maneira a colocar pedras no tabuleiro do adversário.\n Vez do próximo jogador.\n", jogadorAtual, jogadorAtual + 1);

            pulosConsecutivos[jogadorAtual]++;

            // Se um jogador for pulado duas vezes seguidas, o jogo termina
            if (pulosConsecutivos[jogadorAtual] == 2)
            {
                printf("Jogador %d foi pulado duas vezes consecutivas. Jogo encerrado.\n", jogadorAtual);

                tabuleiro[DEPOSITO_JOGADOR1] = tabuleiro[DEPOSITO_JOGADOR1] + tabuleiro[0] + tabuleiro[1] + tabuleiro[2] + tabuleiro[3] + tabuleiro[4] + tabuleiro[5];
                tabuleiro[DEPOSITO_JOGADOR2] = tabuleiro[DEPOSITO_JOGADOR2] + tabuleiro[11] + tabuleiro[10] + tabuleiro[9] + tabuleiro[8] + tabuleiro[7] + tabuleiro[6];

                if (tabuleiro[DEPOSITO_JOGADOR1] > tabuleiro[DEPOSITO_JOGADOR2])
                {
                    printf("O jogador 1 venceu com %d pedras no seu deposito\n", tabuleiro[DEPOSITO_JOGADOR1]);
                    printf("O jogador 2 perdeu com %d pedras no seu deposito\n", tabuleiro[DEPOSITO_JOGADOR2]);
                    exit(0);
                }
                else if (tabuleiro[DEPOSITO_JOGADOR1] < tabuleiro[DEPOSITO_JOGADOR2])
                {
                    printf("O jogador 1 perdeu com %d pedras no seu deposito\n", tabuleiro[DEPOSITO_JOGADOR1]);
                    printf("O jogador 2 venceu com %d pedras no seu deposito\n", tabuleiro[DEPOSITO_JOGADOR2]);
                    exit(0);
                }
            }

            continue;
        }

        // player vs player
        if (pvp == 66)
        {
            if (jogadorAtual == 0)
            {
                printf("Jogador %d, escolha uma casa (de 1 a 6): ", jogadorAtual + 1);
                scanf("%d", &n);
                if (n == 0)
                {
                    char nomeArquivo[50];
                    printf("Digite o nome do arquivo para salvar o tabuleiro (adicione .txt no final): ");
                    scanf("%s", nomeArquivo);
                    guardarTabuleiro(tabuleiro, nomeArquivo);
                    printf("Estado do tabuleiro salvo com sucesso.\n");
                    exit(0);
                }
                n--;

                // Verificar o intervalo das jogadas do jogador 1
                if (n < 0 || n >= NUM_CASAS)
                {
                    printf("Jogada inválida para o jogador %d. Escolha uma casa no seu lado do tabuleiro.\n", jogadorAtual + 1);
                    continue;
                }
            }
            else
            {
                printf("Jogador %d, escolha uma casa (de 1 a 6): ", jogadorAtual + 1);
                scanf("%d", &n);
                if (n == 0)
                {
                    char nomeArquivo[50];
                    printf("Digite o nome do arquivo para salvar o tabuleiro (adicione .txt no final): ");
                    scanf("%s", nomeArquivo);
                    guardarTabuleiro(tabuleiro, nomeArquivo);
                    printf("Estado do tabuleiro salvo com sucesso.\n");
                    exit(0);
                }
                n--;

                // Verificar o intervalo das jogadas do jogador 2
                if (n < 0 || n >= NUM_CASAS)
                {
                    printf("Jogada inválida para o jogador %d. Escolha uma casa no seu lado do tabuleiro.\n", jogadorAtual + 1);
                    continue;
                }

                // Atualizar a jogada para o tabuleiro real
                n = NUM_TOTAL_CASAS - n - 1;
            }
        }

        // player vs engine
        if (pvp == 99)
        {
            if (jogadorAtual == 0)
            {
                printf("Jogador %d, escolha uma casa (de 1 a 6): ", jogadorAtual + 1);
                scanf("%d", &n);
                if (n == 0)
                {
                    char nomeArquivo[50];
                    printf("Digite o nome do arquivo para salvar o tabuleiro (adicione .txt no final): ");
                    scanf("%s", nomeArquivo);
                    guardarTabuleiro(tabuleiro, nomeArquivo);
                    printf("Estado do tabuleiro salvo com sucesso.\n");
                    exit(0);
                }
                n--;

                // Verificar o intervalo das jogadas do jogador 1
                if (n < 0 || n >= NUM_CASAS)
                {
                    printf("Jogada inválida para o jogador %d. Escolha uma casa no seu lado do tabuleiro.\n", jogadorAtual + 1);
                    continue;
                }
            }
            else
            {
                n = rand() % 6;
                printf("Jogador 2 (Bot) escolheu a casa %d.\n", n + 1);

                // Verificar o intervalo das jogadas do jogador 2
                if (n < 0 || n >= NUM_CASAS)
                {
                    printf("Jogador 2 (Bot) escolheu uma casa inválida, irá tentar de novo.\n");
                    continue;
                }

                // Atualizar a jogada para o tabuleiro real
                n = NUM_TOTAL_CASAS - n - 1;
            }
        }

        int pedras = tabuleiro[n];

        // Verificar se existem pedras para jogar na casa escolhida
        if (pedras == 0)
        {
            printf("Escolha uma casa com pedras.\n");
            continue;
        }

        // Verificar a restrição de movimento de uma única pedra
        if (pedras == 1)
        {
            if (jogadorAtual == 0 && (n >= NUM_CASAS || tabuleiro[0] > 1 || tabuleiro[1] > 1 || tabuleiro[2] > 1 || tabuleiro[3] > 1 || tabuleiro[4] > 1 || tabuleiro[5] > 1))
            {
                printf("Não é permitido mover uma pilha de uma única pedra se houverem pilhas maiores no seu lado do tabuleiro.\n");
                continue;
            }
            else if (jogadorAtual == 1 && (n < NUM_CASAS || tabuleiro[6] > 1 || tabuleiro[7] > 1 || tabuleiro[8] > 1 || tabuleiro[9] > 1 || tabuleiro[10] > 1 || tabuleiro[11] > 1))
            {
                printf("Não é permitido mover uma pilha de uma única pedra se houverem pilhas maiores no seu lado do tabuleiro.\n");
                continue;
            }
        }

        tabuleiro[n] = 0;

        // Lógica da regra dos 12
        if (pedras >= 12)
        {
            casaJogada = n;
        }

        while (pedras > 0)
        {
            // Determina a próxima casa para distribuição
            n = (n + 1) % NUM_TOTAL_CASAS;

            // Se a quantidade restante for maior que 1 pula a casa jogada e distribui na próxima
            if (pedras >= 1 && n == casaJogada)
            {
                n = (n + 1) % NUM_TOTAL_CASAS;
            }

            // Distribuir a pedra na casa
            tabuleiro[n]++;
            pedras--;
        }

        // -1 porque não existe no tabuleiro
        casaJogada = -1;

        // Lógica de captura
        if ((tabuleiro[n] == 2 || tabuleiro[n] == 3) && n != DEPOSITO_JOGADOR1 && n != DEPOSITO_JOGADOR2)
        {
            int deposito;
            if (jogadorAtual == 1 && n >= 0 && n < NUM_CASAS)
            {
                deposito = DEPOSITO_JOGADOR2;
                tabuleiro[deposito] += tabuleiro[n];
                tabuleiro[n] = 0;

                // Captura as pedras das casas anteriores apenas se for do lado oposto
                int casa_anterior = (n + NUM_TOTAL_CASAS - 1) % NUM_TOTAL_CASAS;
                if (casa_anterior >= 0 && casa_anterior < NUM_CASAS)
                {
                    while ((tabuleiro[casa_anterior] == 2 || tabuleiro[casa_anterior] == 3) && casa_anterior != n && casa_anterior != 11)
                    {
                        tabuleiro[deposito] += tabuleiro[casa_anterior];
                        tabuleiro[casa_anterior] = 0;
                        casa_anterior = (casa_anterior + NUM_TOTAL_CASAS - 1) % NUM_TOTAL_CASAS;
                    }
                }
            }
            else if (jogadorAtual == 0 && n >= NUM_CASAS && n < NUM_TOTAL_CASAS)
            {
                deposito = DEPOSITO_JOGADOR1;
                tabuleiro[deposito] += tabuleiro[n];
                tabuleiro[n] = 0;

                // Captura as pedras das casas anteriores apenas se for do lado oposto
                int casa_anterior = (n + NUM_TOTAL_CASAS - 1) % NUM_TOTAL_CASAS;
                if (casa_anterior >= NUM_CASAS && casa_anterior < NUM_TOTAL_CASAS)
                {
                    while ((tabuleiro[casa_anterior] == 2 || tabuleiro[casa_anterior] == 3) && casa_anterior != n && casa_anterior != 5)
                    {
                        tabuleiro[deposito] += tabuleiro[casa_anterior];
                        tabuleiro[casa_anterior] = 0;
                        casa_anterior = (casa_anterior + NUM_TOTAL_CASAS - 1) % NUM_TOTAL_CASAS;
                    }
                }
            }
        }

        // Troca para o próximo jogador
        jogadorAtual = (jogadorAtual + 1) % 2;
    }
}

// Função principal do tabuleiro;
int main(int argc, char *argv[])
{
    // Criar a semente (seed) para o bot
    srand(time(NULL));

    // Valores inicias do tabuleiro
    int tabuleiro[14] = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0};
    int jogadorAtual = 0; // Começa com o jogador 1
    char pvp;

    if (argc > 1)
    {
        char nomeArquivo[50];
        sprintf(nomeArquivo, "%s", argv[1]); // Copia o nome do arquivo do argumento

        // Tenta carregar o tabuleiro do arquivo
        if (carregarTabuleiro(tabuleiro, nomeArquivo))
        {
            printf("Tabuleiro carregado com sucesso!\n");
            imprimirTabuleiro(tabuleiro);
        }
        else
        {
            printf("Falha ao carregar o tabuleiro do arquivo. Iniciando com tabuleiro padrão.\n");
        }
    }

    escolha(&pvp);

    if (pvp == 'j' || pvp == 'J')
    {

        printf("Escolheu jogar contra outro jogador.\n");
        printf("- Deve escolher a casa escrevendo o numero da casa.\n- À esquerda e à direita encontam-se os depósitos, sendo o seu o da direita.\n\n");
        while (fim(tabuleiro) == 0)
        {
            jogada(tabuleiro, jogadorAtual, 66);
        }
    }
    else if (pvp == 'c' || pvp == 'C')
    {
        printf("Escolheu jogar contra o computador.\n");
        printf("- Deve escolher a casa escrevendo o numero da casa.\n- À esquerda e à direita encontam-se os depósitos, sendo o seu o da direita.\n\n");
        while (fim(tabuleiro) == 0)
        {
            jogada(tabuleiro, jogadorAtual, 99);
        }
    }
    else
    {
        printf("Escolheu uma opção inválida.\n");
        exit(0);
    }
    imprimirTabuleiro(tabuleiro);
    return 0;
}