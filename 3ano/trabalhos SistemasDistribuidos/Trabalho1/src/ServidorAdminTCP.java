import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServidorAdminTCP implements Runnable {

    private final GestorBD gestorBD;
    private final int port;

    public ServidorAdminTCP(GestorBD gestorBD, int port) {
        this.gestorBD = gestorBD;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket s = serverSocket.accept();
                new Thread(new AdminClientHandler(s, gestorBD)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro no ServidorAdminTCP.");
        }
    }

    // Handler por ligaçao
    private static class AdminClientHandler implements Runnable {
        private final Socket socket;
        private final GestorBD gestorBD;

        AdminClientHandler(Socket socket, GestorBD gestorBD) {
            this.socket = socket;
            this.gestorBD = gestorBD;
        }

        @Override
        public void run() {
            System.out.println("Admin ligado: " + socket.getRemoteSocketAddress());
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty())
                        continue;

                    String[] parts = line.split("\\s+");
                    String cmd = parts[0].toUpperCase();

                    try {
                        switch (cmd) {
                            case "APROVAR": {
                                int id = Integer.parseInt(parts[1]);
                                String msg = gestorBD.aprovarRecurso(id); // devolve msg pronta
                                out.println(msg);
                                break;
                            }

                            case "SET_ESTADO": {
                                int id = Integer.parseInt(parts[1]);
                                String estado = parts[2].toUpperCase(); // DISPPONIVEL, MANUTENCAO, ...
                                gestorBD.alterarEstadoRecurso(id, estado);
                                out.println("Estado do recurso " + id + " atualizado para " + estado + ".");
                                break;
                            }

                            case "SET_COPIAS": {
                                int id = Integer.parseInt(parts[1]);
                                int n = Integer.parseInt(parts[2]);
                                gestorBD.alterarNumCopiasLivro(id, n);
                                out.println("Livro " + id + " agora tem " + n + " copias.");
                                break;
                            }

                            case "SET_UTILIZACOES": {
                                if (parts.length < 3) {
                                    out.println("Uso: SET_UTILIZACOES <idRecursoComputador> <novoValor>");
                                } else {
                                    int id = Integer.parseInt(parts[1]);
                                    int novo = Integer.parseInt(parts[2]);
                                    gestorBD.alterarUtilizacoesComputador(id, novo);   // <— chama o GestorBD
                                    out.println("Computador " + id + " agora tem utilizacoes = " + novo + ".");
                                }
                                break;
                            }

                            case "LIST_USERS_APROVADOS": {
                                List<String> lista = gestorBD.listarUtilizadoresPorEstadoAdmin("APROVADO");
                                if (lista.isEmpty())
                                    out.println("Nao ha utilizadores aprovados.");
                                else
                                    for (String s : lista)
                                        out.println(s);
                                break;
                            }

                            case "LIST_USERS_PENDENTES": {
                                List<String> lista = gestorBD.listarUtilizadoresPorEstadoAdmin("NAO_APROVADO");
                                if (lista.isEmpty())
                                    out.println("Nao ha utilizadores pendentes.");
                                else
                                    for (String s : lista)
                                        out.println(s);
                                break;
                            }

                            case "APROVAR_USER": {
                                int id = Integer.parseInt(parts[1]);
                                String msg = gestorBD.aprovarUtilizador(id);
                                out.println(msg);
                                break;
                            }

                            case "HISTORICO_USER": {
                                int id = Integer.parseInt(parts[1]);
                                List<String> hist = gestorBD.historicoUtilizador(id);
                                if (hist.isEmpty())
                                    out.println("Sem historico para esse utilizador.");
                                else
                                    for (String s : hist)
                                        out.println(s);
                                break;
                            }

                            case "LIST_EMPRESTIMOS_ATIVOS": {
                                List<String> lista = gestorBD.listarEmprestimosAtivos();
                                if (lista.isEmpty())
                                    out.println("Nao ha emprestimos ativos.");
                                else
                                    for (String s : lista)
                                        out.println(s);
                                break;
                            }

                            case "REGISTAR_LIVRO": {
                                String titulo = parts[1].replace('_', ' ');
                                String autor = parts[2].replace('_', ' ');
                                int numCopias = Integer.parseInt(parts[3]);
                                int id = gestorBD.criarLivro(titulo, autor, numCopias);
                                out.println("Livro registrado com id_recurso = " + id);
                                break;
                            }

                            case "REGISTAR_COMPUTADOR": {
                                String modelo = parts[1].replace('_', ' ');
                                int id = gestorBD.criarComputador(modelo);
                                out.println("Computador registrado com id_recurso = " + id);
                                break;
                            }

                            case "REGISTAR_SALA_ESTUDO": {
                                String nome = parts[1].replace('_', ' ');
                                int capacidade = Integer.parseInt(parts[2]);
                                int id = gestorBD.criarSalaEstudo(nome, capacidade);
                                out.println("Sala de estudo registrada com id_recurso = " + id);
                                break;
                            }
                            case "LIST_RECURSOS_NAO_APROVADOS": {
                                List<String> lista = gestorBD.listarRecursosPorEstadoAdmin("NAO_APROVADO");
                                if (lista.isEmpty())
                                    out.println("Sem recursos NAO aprovados.");
                                else
                                    for (String s : lista)
                                        out.println(s);
                                break;
                            }

                            case "LIST_RECURSOS_APROVADOS": {
                                List<String> lista = gestorBD.listarRecursosPorEstadoAdmin("APROVADO");
                                if (lista.isEmpty())
                                    out.println("Sem recursos aprovados.");
                                else
                                    for (String s : lista)
                                        out.println(s);
                                break;
                            }

                            case "QUIT":
                                out.println("A terminar ligaçao admin.");
                                return;

                            default:
                                out.println("Erro :(");
                        }
                    } catch (Exception e) {
                        out.println("ERRO: " + e.getMessage());
                    }
                }
            } catch (

            IOException e) {
                System.err.println("Ligacao admin terminada com erro.");
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
