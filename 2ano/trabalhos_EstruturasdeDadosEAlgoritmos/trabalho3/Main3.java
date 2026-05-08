import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String[] lines = input.readLine().split(" ");

        int N = Integer.parseInt(lines[0]);
        int M = Integer.parseInt(lines[1]);

        FlowNetwork rede = new FlowNetwork(2*N +2);// numero de nós é N para as cartas dos participantes + N para os participantes + 2 para a fonte e dreno
        int fonte = 0;
        int dreno = 2 * N + 1;


        int[] cartas = new int[N];
        int[] participantes = new int[N];
        for (int i = 0; i < N; i++) {
            cartas[i] = i + 1;
            participantes[i] = N + i + 1 ;
        }


        for (int i = 0; i < N; i++) {
        // arcos da fonte para as cartas
            rede.addEdge(new FlowEdge(fonte, cartas[i], 1));
        // arcos dos participantes para o dreno
            rede.addEdge(new FlowEdge(participantes[i], dreno, 1));
        }

        // ler declaraÃ§Ãµes do input
        for (int i = 0; i < M; i++) {
            lines = input.readLine().split(" ");
            int A = Integer.parseInt(lines[0]);
            int B = Integer.parseInt(lines[1]);
            rede.addEdge(new FlowEdge(cartas[B], participantes[A], 1));
        }


        // max através do algoritmo de Edmonds-Karp 
        int maxFlow = edmondsKarp(rede, fonte, dreno);

        // se o fluxo máximo for igual ao número de cartas, é possível distribuir todas as cartas
        System.out.println(maxFlow == N ? "YES" : "NO");
    }

    private static int edmondsKarp(FlowNetwork network, int source, int sink) {
        int maxFlow = 0;
        FlowEdge[] edgeTo = new FlowEdge[network.V()]; // achar arestas do caminho

        while (hasAugmentingPath(network, source, sink, edgeTo)) {
            // (menor capacidade residual no caminho)
            int bottleneck = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = edgeTo[v].other(v)) {
                bottleneck = Math.min(bottleneck, edgeTo[v].residualCapacityTo(v));
            }

            // atualizar fluxo
            for (int v = sink; v != source; v = edgeTo[v].other(v)) {
                edgeTo[v].addResidualFlowTo(v, bottleneck);
            }

            maxFlow += bottleneck;
        }

        return maxFlow;
    }

    // BFS
    private static boolean hasAugmentingPath(FlowNetwork network, int source, int sink, FlowEdge[] edgeTo) {
        boolean[] visited = new boolean[network.V()];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (FlowEdge e : network.adj(u)) {
                int v = e.other(u);
                if (e.residualCapacityTo(v) > 0 && !visited[v]) {
                    edgeTo[v] = e;
                    visited[v] = true;
                    if (v == sink) return true; // caminho encontrado
                    queue.add(v);
                }
            }
        }

        return false; // nenhum caminho encontrado
    }

    public static class FlowEdge {
        private final int v, w;
        private final int capacity;
        private int flow;

        public FlowEdge(int v, int w, int capacity) {
            this.v = v;
            this.w = w;
            this.capacity = capacity;
            this.flow = 0;
        }

        public int from() { return v; }
        public int to() { return w; }

        // nÃ³ oposto da aresta
        public int other(int vertex) {
            if (vertex == v) return w;
            else if (vertex == w) return v;
            else throw new IllegalArgumentException();
        }

        public int residualCapacityTo(int vertex) {
            if (vertex == w) return capacity - flow; // direção direta
            else if (vertex == v) return flow;       // direcao reversa
            else throw new IllegalArgumentException();
        }


        public void addResidualFlowTo(int vertex, int delta) {
            if (vertex == w) flow += delta;  // aumenta fluxo direto
            else if (vertex == v) flow -= delta; // reduz fluxo reverso
            else throw new IllegalArgumentException();
        }
    }

    public static class FlowNetwork {
        private final int V;
        private List<FlowEdge>[] adj;

        @SuppressWarnings("unchecked")
        public FlowNetwork(int V) {
            this.V = V;
            adj = (List<FlowEdge>[]) new List[V];
            for (int v = 0; v < V; v++) {
                adj[v] = new ArrayList<>();
            }
        }

        public void addEdge(FlowEdge e) {
            int v = e.from();
            int w = e.to();
            adj[v].add(e);
            adj[w].add(e);
        }

        public Iterable<FlowEdge> adj(int v) {
            return adj[v];
        }

        public int V() {
            return V;
        }
    }
}