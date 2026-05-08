import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        // Leitura da entrada
        String[] primeira_linha = r.readLine().split(" ");
        int N = Integer.parseInt(primeira_linha[0]); // O Nº de numeros diferentes disponiveis para carregar sonhos
        int[] ni = new int[N]; // array para guardar os numeros disponiveis para guardar sonhos

        int D = Integer.parseInt(primeira_linha[1]); // O Nº de sonhos encomendados
        int[] dj = new int[D]; // array para guardar os sonhos encomendados

        String[] NIs = r.readLine().split(" ");// lê os numeros disponiveis

        for (int i = 0; i < N; i++) {
            ni[i] = Integer.parseInt(NIs[i]);// Carrega os numeros disponiveis
        }
        Arrays.sort(ni);// Ordena os numeros disponiveis
        int maxNi = ni[N - 1]; // Maior numero disponivel

        for (int j = 0; j < D; j++) {
            dj[j] = Integer.parseInt(r.readLine()); // Carrega os tamanhos dos sonhos
        }

        // Chama a funcao recursiva e imprime o resultado
        System.out.println(M(ni, dj, maxNi));
    }

    // Funcao que calcula o desperdi­cio mÃinimo
    public static long M(int[] ni, int[] dj, int maxNi) {

        long[] dp = new long[dj.length + 1];
        Arrays.fill(dp, Long.MAX_VALUE);
        dp[0] = 0; // Caso base: 0 sonhos = 0 desperdÃ­cio

        for (int i = 1; i <= dj.length; i++) { // Para cada quantidade de sonhos de 1 ate D, calcula o minWaste.
            long sum = 0;
            // Verifica todos os grupos possi­veis terminando em i-1
            for (int j = i - 1; j >= 0; j--) {
                sum += dj[j]; // Soma dos sonhos de j ate i-1
                if (sum > maxNi)
                    break; // Otimizacao: interrompe se exceder
                // Encontra o menor numero que caiba na soma
                int pos = Arrays.binarySearch(ni, (int) sum);
                if (pos < 0)
                    pos = -pos - 1; // Ajuste para Índice válido
                if (pos >= ni.length)
                    continue; // Ignora se não houver número

                // Calcula o desperdício e atualiza a DP
                long waste = ni[pos] - sum;
                dp[i] = Math.min(dp[i], dp[j] + waste);
            }
        }
        return dp[dj.length]; // Resultado para todos os sonhos
    }
}