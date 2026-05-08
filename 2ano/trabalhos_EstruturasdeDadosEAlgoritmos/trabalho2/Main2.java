import java.io.*;
import java.util.*;

 public class Main2 {


    public static void main(String[] args) throws Exception{
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        // LÃª o nÂº de habitantes e cria a ilha
        int n = Integer.parseInt(input.readLine());
        Ilha Ilha = new Ilha(n);

        for(int i = 0 ; i<n-1; i++)// LÃª as conexões entre eles
        {
            String[] lines = input.readLine().split(" ");   
            int habitante1 =  Integer.parseInt(lines[0]);
            int habitante2 =  Integer.parseInt(lines[1]);

            Ilha.addConections(habitante1 , habitante2);//criar as conexões dadas no input
        }
        System.out.println(Ilha.maiorCaminho());
    }

    static class Edge {// classe edge para definir arcos
        private final int destination;

        public Edge(int destination) {//construtor edge com destino do arco como argumento
            this.destination = destination;
        }
    }

    static class Vertex {

        enum Color{// estado do vertice
            white,  //não descoberto
            grey,   //descoberto mas nÃ£o processado
            black   //processado
            };

        Color cor;
        int d;//distância ao vertice atual (comprimento do caminho)
        Integer p;//antecessor do nÃ³ no caminho a partir do vértice atual
        
        public Vertex()//construtor
        {
            this.cor = Color.white;
            this.d = Integer.MAX_VALUE;
            this.p = null;
        }
    }

    public static class Ilha {
        private final int habitantes;
        List<List<Edge>> lista;
        Vertex[] vertices;

        public Ilha(int habitantes) // construtor da ilha com o n.º de habitantes como argumento
        {
            this.habitantes = habitantes;
            this.lista = new ArrayList<>();
            this.vertices = new Vertex[habitantes +1];
            for (int i = 0; i <= habitantes; i++) {
                lista.add(new ArrayList<>());
                vertices[i] = new Vertex(); 
            }
        }

        public void addConections(int habitante1, int habitante2) {//metodo para adicionar conexões para ambos os habitantes de maneira  a não ter  orientação
            lista.get(habitante1).add(new Edge(habitante2));
            lista.get(habitante2).add(new Edge(habitante1));
        }

        public int maiorCaminho() {
            // primeiro encontra o nÃ³ mais distante do primeiro nÃ³
            int noMaisDistante = bfs(1)[0];

            // depois encontra o nó mais distante a esse nó i
            
            int distanciaMaxima = bfs(noMaisDistante)[1];
            return distanciaMaxima;
        }

    // algoritmo BFS que retorna o nÃ³ mais distante e sua distância máxima a partir do nÃ³ inicial
        private int[] bfs(int inicio) {
            // Inicializa os vértices
            for (int i = 1; i <= habitantes; i++) {
                vertices[i].cor = Vertex.Color.white;
                vertices[i].d = Integer.MAX_VALUE;
                vertices[i].p = null;
            }

            // Configura o nÃ³ inicial
            vertices[inicio].cor = Vertex.Color.grey;
            vertices[inicio].d = 0;
            vertices[inicio].p = null;

            Queue<Integer> fila = new LinkedList<>();// fila para armazenar os nÃ³s a serem processados
            fila.add(inicio);

            int noMaisDistante = inicio;
            int distanciaMaxima = 0;

            while (!fila.isEmpty()) 
            {
            int atual = fila.poll();// remove o nÃ³ da fila
            
            // Processa todos os vizinhos do nÃ³ atual
            for (Edge vizinho : lista.get(atual)) 
            {
                int prox = vizinho.destination;
                // Se o vizinho não foi visitado, atualiza suas informacoes
                if (vertices[prox].cor == Vertex.Color.white) 
                {
                    vertices[prox].cor = Vertex.Color.grey;
                    vertices[prox].d = vertices[atual].d + 1;
                    vertices[prox].p = atual;
                    fila.add(prox);

                    // Atualiza o nó mais distante e a distância máxima
                    if (vertices[prox].d > distanciaMaxima) {
                        distanciaMaxima = vertices[prox].d;
                        noMaisDistante = prox;
                        }
                    }
                }
            vertices[atual].cor = Vertex.Color.black;
            }

            // Retorna o nó mais distante e a distância máxima
            return new int[]{noMaisDistante, distanciaMaxima};
        }
    }
}