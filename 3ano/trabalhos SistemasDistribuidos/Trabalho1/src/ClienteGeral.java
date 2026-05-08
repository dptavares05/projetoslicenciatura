import java.rmi.Naming;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class ClienteGeral {

    public static void main(String[] args) {
        try {
            // Lê configuração RMI
            Properties p = Config.get();
            String rmiHost = p.getProperty("rmi.host");
            int rmiPort = Integer.parseInt(p.getProperty("rmi.port", "1099"));

            IGestorGeral gestor = (IGestorGeral) Naming.lookup("rmi://" + rmiHost + ":" + rmiPort + "/GestorGeral");

            Scanner sc = new Scanner(System.in);
            int opcao = -1;

            do {
                System.out.println();
                System.out.println("<===========>");
                System.out.println("CLIENTE GERAL");
                System.out.println("<===========>");
                System.out.println("1 - Registrar Utilizador");
                System.out.println("2 - Listar Recursos");
                System.out.println("3 - Listar Emprestimos Ativos");
                System.out.println("4 - O meu Historico de emprestimos");
                System.out.println("5 - Pedir Emprestimo");
                System.out.println("6 - Devolver");
                System.out.println("0 - Sair");
                System.out.print("Opcao: ");

                String linha = sc.nextLine();
                if (linha.isEmpty()) {
                    opcao = -1;
                    continue;
                }
                opcao = Integer.parseInt(linha);

                switch (opcao) {
                    // 1 - Registrar Utilizador
                    case 1: {
                        System.out.print("Nome completo: ");
                        String nome = sc.nextLine();
                        System.out.print("Email: ");
                        String email = sc.nextLine();
                        System.out.print("Tipo (ADM/USER): ");
                        String tipo = sc.nextLine().toUpperCase();

                        try {
                            gestor.registarUtilizador(nome, email, tipo);
                            System.out.println("Utilizador registrado com sucesso.");
                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                        break;
                    }

                    // 2 - Listar Recursos (com filtros)
                    case 2: {
                        // 1) Estado operacional
                        System.out.println("\n===== FILTRAR POR ESTADO =====");
                        System.out.println("1 - Disponiveis");
                        System.out.println("2 - Indisponiveis (EMPRESTADO/MANUTENCAO/INDISPONIVEL)");
                        System.out.println("0 - Sem filtro");
                        System.out.print("Opcao: ");
                        String sEstado = sc.nextLine().trim();
                        String fEstado = "";
                        boolean indisponivelAgregado = false;
                        if ("1".equals(sEstado)) {
                            fEstado = "DISPONIVEL";
                        } else if ("2".equals(sEstado)) {
                            indisponivelAgregado = true; // vamos buscar varios estados
                        }

                        // 2) Tipo
                        System.out.println("\n===== TIPO =====");
                        System.out.println("1 - Livros");
                        System.out.println("2 - Computadores");
                        System.out.println("3 - Salas de Estudo");
                        System.out.println("0 - Todos");
                        System.out.print("Opcao: ");
                        String sTipo = sc.nextLine().trim();
                        String fTipo = switch (sTipo) {
                            case "1" -> "LIVRO";
                            case "2" -> "COMPUTADOR";
                            case "3" -> "SALA";
                            default -> "";
                        };

                        // 3) Texto (opcional) e Estado Admin (default: APROVADO)
                        System.out.print("Texto de pesquisa(ENTER para ignorar): ");
                        String fTxt = sc.nextLine().trim();
                        System.out.print("Estado administrativo [APROVADO/NAO_APROVADO]: ");
                        String fAdmin = sc.nextLine().trim().toUpperCase();
                        if (fAdmin.isBlank())
                            fAdmin = "APROVADO";

                        try {
                            java.util.ArrayList<String> linhas = new java.util.ArrayList<>();
                            if (indisponivelAgregado) {
                                for (String st : new String[] { "EMPRESTADO", "INDISPONIVEL", "MANUTENCAO" }) {
                                    linhas.addAll(gestor.listarRecursosFiltrado(fTipo, st, fAdmin, fTxt));
                                }
                            } else {
                                linhas.addAll(gestor.listarRecursosFiltrado(fTipo, fEstado, fAdmin, fTxt));
                            }

                            if (linhas.isEmpty())
                                System.out.println("Sem resultados.");
                            else
                                linhas.forEach(System.out::println);

                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                        break;
                    }

                    // 3 - Listar Empréstimos Ativos
                    case 3: {
                        try {
                            List<String> emps = gestor.listarEmprestimosAtivos();
                            System.out.println("\n-- Emprestimos Ativos --");
                            if (emps.isEmpty()) {
                                System.out.println("Nenhum emprestimo ativo.");
                            } else {
                                for (String s : emps)
                                    System.out.println(s);
                            }
                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                        break;
                    }

                    // 4 - O meu histórico de empréstimos (histórico de um utilizador)
                    case 4: {
                        try {
                            System.out.print("Informe o seu ID de utilizador: ");
                            int idUser = Integer.parseInt(sc.nextLine());
                            List<String> hist = gestor.historicoUtilizador(idUser);
                            System.out.println("\n-- Histórico do Utilizador " + idUser + " --");
                            if (hist.isEmpty()) {
                                System.out.println("Sem emprestimos registrados.");
                            } else {
                                for (String s : hist)
                                    System.out.println(s);
                            }
                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                        break;
                    }

                    // 5 - Pedir Emprestimo
                    case 5: {
                        try {
                            System.out.print("ID do utilizador: ");
                            int idUser = Integer.parseInt(sc.nextLine());
                            System.out.print("ID do recurso: ");
                            int idRecurso = Integer.parseInt(sc.nextLine());
                            System.out.print("Duracao (dias): ");
                            int dias = Integer.parseInt(sc.nextLine());

                            gestor.criarEmprestimo(idUser, idRecurso, dias);
                            System.out.println("Emprestimo criado com sucesso.");
                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                        break;
                    }

                    // 6 - Devolver
                    case 6: {
                        try {
                            System.out.print("ID do emprestimo a devolver: ");
                            int idEmp = Integer.parseInt(sc.nextLine());
                            gestor.devolverEmprestimo(idEmp);
                            System.out.println("Emprestimo devolvido com sucesso.");
                        } catch (Exception e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                        break;
                    }

                    case 0:
                        System.out.println("A sair...");
                        break;

                    default:
                        System.out.println("Opcao invalida.");
                }

            } while (opcao != 0);

            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro no ClienteGeral.");
        }
    }

}
