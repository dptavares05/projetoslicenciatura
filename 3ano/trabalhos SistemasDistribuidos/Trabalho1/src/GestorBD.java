import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestorBD {

    private final String url;
    private final String user;
    private final String pwd;

    public GestorBD(String host, String db, String schema, String user, String pwd) {
        this.url = "jdbc:postgresql://" + host + ":5432/" + db + "?currentSchema=" + schema;
        this.user = user;
        this.pwd = pwd;
    }

    private Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(url, user, pwd);
    }

    // ---- UTILIZADOR ----
    public void registarUtilizador(String nome, String email, String tipo) throws Exception {
        String sql = "INSERT INTO utilizador (nome_completo, email, tipo) VALUES (?, ?, ?::tipo_utilizador_enum)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, email);
            ps.setString(3, tipo);
            ps.executeUpdate();
        }
    }

    public List<String> listarUtilizadoresPorEstadoAdmin(String estadoAdmin) throws Exception {
        String sql = "SELECT id_utilizador, nome_completo, email, tipo, estado_admin, estado_conta " +
                "FROM utilizador " +
                "WHERE estado_admin = ?::estado_admin_enum " +
                "ORDER BY id_utilizador";

        List<String> res = new ArrayList<>();

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estadoAdmin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String linha = rs.getInt("id_utilizador") + " | " +
                            rs.getString("nome_completo") + " | " +
                            rs.getString("email") + " | " +
                            rs.getString("tipo") + " | " +
                            "admin=" + rs.getString("estado_admin") + " | " +
                            "conta=" + rs.getString("estado_conta");
                    res.add(linha);
                }
            }
        }
        return res;
    }

    // ---- ADMIN: aprovar utilizador ----
    public String aprovarUtilizador(int idUtilizador) throws Exception {
        try (Connection con = getConnection()) {
            // Tenta aprovar apenas se ainda nao estiver aprovado
            String sqlUpd = "UPDATE utilizador " +
                    "SET estado_admin = 'APROVADO'::estado_admin_enum, " +
                    "    estado_conta = 'ATIVA'::estado_conta_enum " +
                    "WHERE id_utilizador = ? " +
                    "  AND estado_admin <> 'APROVADO'::estado_admin_enum";
            try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                ps.setInt(1, idUtilizador);
                int n = ps.executeUpdate();
                if (n == 1) {
                    return "Utilizador " + idUtilizador + " aprovado com sucesso.";
                }
            }

            // 0 linhas atualizadas: ver se existe e se já estava aprovado
            String sqlSel = "SELECT estado_admin FROM utilizador WHERE id_utilizador = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
                ps.setInt(1, idUtilizador);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        return "Utilizador " + idUtilizador + " nao existe.";
                    String estado = rs.getString(1);
                    if ("APROVADO".equalsIgnoreCase(estado)) {
                        return "O utilizador " + idUtilizador + " ja estava aprovado.";
                    }
                }
            }
            return "Nao foi possivel aprovar o utilizador " + idUtilizador;
        }
    }

    // ---- RECURSOS / LIVROS ----
    public int criarLivro(String titulo, String autor, int numCopias) throws Exception {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            // ver se ja existe recurso+livro com esse titulo+autor
            String sqlSel = "SELECT r.id_recurso " +
                    "FROM recurso r JOIN livro l ON r.id_recurso = l.id_recurso " +
                    "WHERE l.titulo = ? AND l.autor = ? " +
                    "LIMIT 1";
            Integer idRecursoExistente = null;
            try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
                ps.setString(1, titulo);
                ps.setString(2, autor);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idRecursoExistente = rs.getInt("id_recurso");
                    }
                }
            }

            if (idRecursoExistente != null) {
                // só soma copias
                String sqlUpd = "UPDATE livro SET num_copias = num_copias + ? " +
                        "WHERE id_recurso = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                    ps.setInt(1, numCopias);
                    ps.setInt(2, idRecursoExistente);
                    ps.executeUpdate();
                }
                con.commit();
                return idRecursoExistente;
            }

            // caso nao exista, criar recurso novo + livro
            String sqlRecurso = "INSERT INTO recurso (tipo_recurso, estado_admin, estado) " +
                    "VALUES ('LIVRO'::tipo_recurso_enum, 'NAO_APROVADO'::estado_admin_enum, " +
                    "        'INDISPONIVEL'::estado_recurso_enum) " +
                    "RETURNING id_recurso";

            int idRecursoNovo;
            try (PreparedStatement ps = con.prepareStatement(sqlRecurso);
                    ResultSet rs = ps.executeQuery()) {
                rs.next();
                idRecursoNovo = rs.getInt("id_recurso");
            }

            String sqlLivro = "INSERT INTO livro (id_recurso, titulo, autor, num_copias) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlLivro)) {
                ps.setInt(1, idRecursoNovo);
                ps.setString(2, titulo);
                ps.setString(3, autor);
                ps.setInt(4, numCopias);
                ps.executeUpdate();
            }

            con.commit();
            return idRecursoNovo;

        } catch (Exception e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (Exception ignore) {
                }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public int criarComputador(String modelo) throws Exception {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            String sqlRecurso = "INSERT INTO recurso (tipo_recurso, estado_admin, estado) " +
                    "VALUES ('COMPUTADOR'::tipo_recurso_enum, 'NAO_APROVADO'::estado_admin_enum, " +
                    "        'INDISPONIVEL'::estado_recurso_enum) " +
                    "RETURNING id_recurso";

            int idRecurso;
            try (PreparedStatement ps = con.prepareStatement(sqlRecurso);
                    ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Falha ao criar recurso COMPUTADOR.");
                }
                idRecurso = rs.getInt("id_recurso");
            }

            String sqlComp = "INSERT INTO computador (id_recurso, modelo) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlComp)) {
                ps.setInt(1, idRecurso);
                ps.setString(2, modelo);
                ps.executeUpdate();
            }

            con.commit();
            return idRecurso;
        } catch (Exception e) {
            if (con != null)
                con.rollback();
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public int criarSalaEstudo(String nome, int capacidade) throws Exception {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            String sqlRecurso = "INSERT INTO recurso (tipo_recurso, estado_admin, estado) " +
                    "VALUES ('SALA_ESTUDO'::tipo_recurso_enum, 'NAO_APROVADO'::estado_admin_enum, " +
                    "        'INDISPONIVEL'::estado_recurso_enum) " +
                    "RETURNING id_recurso";

            int idRecurso;
            try (PreparedStatement ps = con.prepareStatement(sqlRecurso);
                    ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Falha ao criar recurso SALA_ESTUDO.");
                }
                idRecurso = rs.getInt("id_recurso");
            }

            String sqlSala = "INSERT INTO sala_estudo (id_recurso, nome, capacidade) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlSala)) {
                ps.setInt(1, idRecurso);
                ps.setString(2, nome);
                ps.setInt(3, capacidade);
                ps.executeUpdate();
            }

            con.commit();
            return idRecurso;
        } catch (Exception e) {
            if (con != null)
                con.rollback();
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    // ---- RECURSOS / Emprestimo ----
    public void criarEmprestimo(int idUtilizador, int idRecurso, int duracaoDias) throws Exception {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            // 1) Validar utilizador
            String sqlUser = "SELECT estado_admin, estado_conta FROM utilizador WHERE id_utilizador = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUser)) {
                ps.setInt(1, idUtilizador);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new Exception("Utilizador inexistente.");
                    String estadoAdmin = rs.getString("estado_admin");
                    String estadoConta = rs.getString("estado_conta");
                    if (!"APROVADO".equals(estadoAdmin))
                        throw new Exception("Utilizador ainda nao foi aprovado.");
                    if (!"ATIVA".equals(estadoConta))
                        throw new Exception("Conta do utilizador nao esta ativa.");
                }
            }

            // 2) Bloquear o recurso
            String sqlRec = "SELECT tipo_recurso, estado_admin, estado FROM recurso WHERE id_recurso = ? FOR UPDATE";
            String tipoRecurso, estadoAdminRec, estadoRec;
            try (PreparedStatement ps = con.prepareStatement(sqlRec)) {
                ps.setInt(1, idRecurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new Exception("Recurso inexistente.");
                    tipoRecurso = rs.getString("tipo_recurso");
                    estadoAdminRec = rs.getString("estado_admin");
                    estadoRec = rs.getString("estado");
                }
            }
            if (!"APROVADO".equals(estadoAdminRec))
                throw new Exception("Recurso ainda nao foi aprovado.");

            if ("LIVRO".equals(tipoRecurso)) {
                // 3A) LIVRO — disponibilidade por cópias (NÃO decrementar num_copias)
                int numCopias;
                // bloqueia linha do livro para evitar corridas
                String sqlNum = "SELECT num_copias FROM livro WHERE id_recurso = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(sqlNum)) {
                    ps.setInt(1, idRecurso);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new Exception("Livro inexistente para este recurso.");
                        numCopias = rs.getInt("num_copias");
                    }
                }

                int emprestimosAtivos;
                String sqlCount = "SELECT COUNT(*) FROM emprestimo WHERE id_recurso = ? AND estado = 'ATIVO'::estado_emprestimo_enum";
                try (PreparedStatement ps = con.prepareStatement(sqlCount)) {
                    ps.setInt(1, idRecurso);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        emprestimosAtivos = rs.getInt(1);
                    }
                }

                if (emprestimosAtivos >= numCopias) {
                    throw new Exception("Nao ha copias disponiveis deste livro.");
                }

                // 4) Criar emprestimo
                String sqlEmp = "INSERT INTO emprestimo (id_utilizador, id_recurso, data_inicio, data_fim, estado) " +
                        "VALUES (?, ?, NOW(), NOW() + (? || ' days')::interval, 'ATIVO'::estado_emprestimo_enum)";
                try (PreparedStatement ps = con.prepareStatement(sqlEmp)) {
                    ps.setInt(1, idUtilizador);
                    ps.setInt(2, idRecurso);
                    ps.setInt(3, duracaoDias);
                    ps.executeUpdate();
                }

                // 5) Atualizar estado do recurso conforme disponibilidade remanescente
                if (emprestimosAtivos + 1 == numCopias) {
                    // ocupou a última cópia
                    String sqlUpd = "UPDATE recurso SET estado = 'INDISPONIVEL'::estado_recurso_enum WHERE id_recurso = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                        ps.setInt(1, idRecurso);
                        ps.executeUpdate();
                    }
                } else {
                    // ainda há cópias: assegura DISPONIVEL
                    if (!"DISPONIVEL".equals(estadoRec)) {
                        String sqlUpd = "UPDATE recurso SET estado = 'DISPONIVEL'::estado_recurso_enum WHERE id_recurso = ?";
                        try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                            ps.setInt(1, idRecurso);
                            ps.executeUpdate();
                        }
                    }
                }

            } else {
                // 3B) COMPUTADOR / SALA_ESTUDO — 1 a 1
                if (!"DISPONIVEL".equals(estadoRec)) {
                    throw new Exception("Recurso nao disponivel para emprestimo.");
                }
                String sqlUpd = "UPDATE recurso SET estado = 'EMPRESTADO'::estado_recurso_enum WHERE id_recurso = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                    ps.setInt(1, idRecurso);
                    ps.executeUpdate();
                }

                // 4) Criar emprestimo
                String sqlEmp = "INSERT INTO emprestimo (id_utilizador, id_recurso, data_inicio, data_fim, estado) " +
                        "VALUES (?, ?, NOW(), NOW() + (? || ' days')::interval, 'ATIVO'::estado_emprestimo_enum)";
                try (PreparedStatement ps = con.prepareStatement(sqlEmp)) {
                    ps.setInt(1, idUtilizador);
                    ps.setInt(2, idRecurso);
                    ps.setInt(3, duracaoDias);
                    ps.executeUpdate();
                }
            }

            con.commit();

        } catch (Exception e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (Exception ignore) {
                }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    public List<String> listarEmprestimosAtivos() throws Exception {
        // 1) Promove emprestimos vencidos para ATRASADO
        final String bumpSql = "UPDATE emprestimo " +
                "SET estado = 'ATRASADO'::estado_emprestimo_enum " +
                "WHERE estado = 'ATIVO'::estado_emprestimo_enum " +
                "  AND NOW() > data_fim";

        // 2) Depois lista apenas os que continuam ATIVOS
        final String sql = "SELECT e.id_emprestimo, e.data_inicio, e.data_fim, e.estado, " +
                "       u.id_utilizador, u.nome_completo, " +
                "       r.id_recurso, r.tipo_recurso, " +
                "       COALESCE(l.titulo, c.modelo, s.nome) AS recurso_nome " +
                "FROM emprestimo e " +
                "JOIN utilizador u ON e.id_utilizador = u.id_utilizador " +
                "JOIN recurso r    ON e.id_recurso = r.id_recurso " +
                "LEFT JOIN livro       l ON r.id_recurso = l.id_recurso " +
                "LEFT JOIN computador  c ON r.id_recurso = c.id_recurso " +
                "LEFT JOIN sala_estudo s ON r.id_recurso = s.id_recurso " +
                "WHERE e.estado = 'ATIVO'::estado_emprestimo_enum or e.estado = 'ATRASADO'::estado_emprestimo_enum " +
                "ORDER BY e.data_inicio DESC";

        List<String> res = new ArrayList<>();

        try (Connection con = getConnection()) {

            // step 1: atualizar estados vencidos
            try (PreparedStatement up = con.prepareStatement(bumpSql)) {
                up.executeUpdate();
            }

            // step 2: listar ativos
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String linha = "# " + rs.getInt("id_emprestimo") +
                            " | User: " + rs.getInt("id_utilizador") +
                            " - " + rs.getString("nome_completo") +
                            " | Recurso: " + rs.getInt("id_recurso") +
                            " (" + rs.getString("tipo_recurso") + ") " +
                            rs.getString("recurso_nome") +
                            " | Inicio: " + rs.getTimestamp("data_inicio") +
                            " | Fim: " + rs.getTimestamp("data_fim") +
                            " | Estado: " + rs.getString("estado");
                    res.add(linha);
                }
            }
        }
        return res;
    }

    public List<String> historicoUtilizador(int idUtilizador) throws Exception {
        String sql = "SELECT e.id_emprestimo, e.data_inicio, e.data_fim, e.estado, " +
                "       r.id_recurso, r.tipo_recurso " +
                "FROM emprestimo e " +
                "JOIN recurso r ON e.id_recurso = r.id_recurso " +
                "WHERE e.id_utilizador = ? " +
                "ORDER BY e.data_inicio DESC";

        List<String> res = new ArrayList<>();

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUtilizador);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String linha = "Emp: " + rs.getInt("id_emprestimo") +
                            " | Recurso: " + rs.getInt("id_recurso") + " (" + rs.getString("tipo_recurso") + ")" +
                            " | Inicio: " + rs.getTimestamp("data_inicio") +
                            " | Fim: " + rs.getTimestamp("data_fim") +
                            " | Estado: " + rs.getString("estado");
                    res.add(linha);
                }
            }
        }
        return res;
    }

    public void devolverEmprestimo(int idEmprestimo) throws Exception {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            // 1) Buscar info do emprestimo
            String sqlSelEmp = "SELECT id_recurso, estado " +
                    "FROM emprestimo WHERE id_emprestimo = ? FOR UPDATE";
            int idRecurso;
            String estadoEmp;

            try (PreparedStatement ps = con.prepareStatement(sqlSelEmp)) {
                ps.setInt(1, idEmprestimo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("Emprestimo nao encontrado.");
                    }
                    idRecurso = rs.getInt("id_recurso");
                    estadoEmp = rs.getString("estado");
                }
            }

            if (!"ATIVO".equals(estadoEmp)) {
                throw new Exception("Só e possivel devolver emprestimos ativos.");
            }

            // 2) Atualizar emprestimo para DEVOLVIDO
            String sqlEmp = "UPDATE emprestimo " +
                    "SET estado = 'DEVOLVIDO'::estado_emprestimo_enum, " +
                    "    data_fim = NOW() " +
                    "WHERE id_emprestimo = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlEmp)) {
                ps.setInt(1, idEmprestimo);
                ps.executeUpdate();
            }

            // 3) Atualizar num_copias para LIVRO
            // 3) Ajustar estado do recurso após devolução
            String tipoRecurso;
            try (PreparedStatement ps = con.prepareStatement("SELECT tipo_recurso FROM recurso WHERE id_recurso = ?")) {
                ps.setInt(1, idRecurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        throw new Exception("Recurso associado ao emprestimo nao existe.");
                    tipoRecurso = rs.getString("tipo_recurso");
                }
            }

            if ("LIVRO".equals(tipoRecurso)) {
                // NÃO alterar num_copias (e fixo). Verifica disponibilidade pela contagem de
                // emprestimos ATIVOS.
                int numCopias;
                try (PreparedStatement ps = con.prepareStatement("SELECT num_copias FROM livro WHERE id_recurso = ?")) {
                    ps.setInt(1, idRecurso);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        numCopias = rs.getInt(1);
                    }
                }
                int ativos;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM emprestimo WHERE id_recurso = ? AND estado = 'ATIVO'::estado_emprestimo_enum")) {
                    ps.setInt(1, idRecurso);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        ativos = rs.getInt(1);
                    }
                }
                // Se agora há menos ativos que cópias, fica DISPONIVEL; senão mantem
                // INDISPONIVEL
                String novo = (ativos < numCopias) ? "DISPONIVEL" : "INDISPONIVEL";
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE recurso SET estado = ?::estado_recurso_enum WHERE id_recurso = ?")) {
                    ps.setString(1, novo);
                    ps.setInt(2, idRecurso);
                    ps.executeUpdate();
                }
            } else if ("COMPUTADOR".equals(tipoRecurso)) {

                // incrementa o número de utilizações
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE computador SET utilizacoes = utilizacoes + 1 WHERE id_recurso = ?")) {
                    ps.setInt(1, idRecurso);
                    ps.executeUpdate();
                }

                // computador volta a ficar disponivel
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE recurso SET estado = 'DISPONIVEL'::estado_recurso_enum WHERE id_recurso = ?")) {
                    ps.setInt(1, idRecurso);
                    ps.executeUpdate();
                }

            } else {
                // COMPUTADOR / SALA: volta a DISPONIVEL
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE recurso SET estado = 'DISPONIVEL'::estado_recurso_enum WHERE id_recurso = ?")) {
                    ps.setInt(1, idRecurso);
                    ps.executeUpdate();
                }
            }

            con.commit();

        } catch (Exception e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (Exception ignore) {
                }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    // ---- ADMIN: listar recursos por estado_admin ----
    public java.util.List<String> listarRecursosPorEstadoAdmin(String estadoAdmin) throws Exception {
        String sql = "SELECT id_recurso, tipo_recurso, estado_admin, estado " +
                "FROM recurso " +
                "WHERE estado_admin = ?::estado_admin_enum " +
                "ORDER BY " +
                "  CASE tipo_recurso " +
                "    WHEN 'LIVRO' THEN 1 " +
                "    WHEN 'COMPUTADOR' THEN 2 " +
                "    WHEN 'SALA_ESTUDO' THEN 3 " +
                "  END, " +
                "  id_recurso";

        java.util.List<String> res = new java.util.ArrayList<>();

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estadoAdmin); // "APROVADO" OU "NAO_APROVADO"

            try (ResultSet rs = ps.executeQuery()) {

                String tipoAtual = null;

                while (rs.next()) {
                    String tipo = rs.getString("tipo_recurso"); // "LIVRO", "COMPUTADOR" ou "SALA_ESTUDO"

                    // Sempre que o tipo mudar, adiciona a "header"
                    if (!tipo.equals(tipoAtual)) {
                        tipoAtual = tipo;
                        res.add(" ");

                        if ("LIVRO".equals(tipo)) {
                            res.add("--Livros--");
                        } else if ("COMPUTADOR".equals(tipo)) {
                            res.add("--Computadores--");
                        } else if ("SALA_ESTUDO".equals(tipo)) {
                            res.add("--Salas de Estudo--");
                        }
                    }

                    // Agora adicionas o recurso em si
                    res.add(
                            "   " + rs.getInt("id_recurso") + " | " +
                                    rs.getString("tipo_recurso") + " | " +
                                    "admin=" + rs.getString("estado_admin") + " | " +
                                    "estado=" + rs.getString("estado"));
                }
            }
        }

        return res;
    }

    // ---- ADMIN: aprovar recurso ----
    public String aprovarRecurso(int idRecurso) throws Exception {
        try (Connection con = getConnection()) {
            // Tenta aprovar apenas se ainda nao estiver aprovado
            String sqlUpd = "UPDATE recurso " +
                    "SET estado_admin = 'APROVADO'::estado_admin_enum, " +
                    "    estado = 'DISPONIVEL'::estado_recurso_enum " +
                    "WHERE id_recurso = ? " +
                    "  AND estado_admin <> 'APROVADO'::estado_admin_enum";
            try (PreparedStatement ps = con.prepareStatement(sqlUpd)) {
                ps.setInt(1, idRecurso);
                int n = ps.executeUpdate();
                if (n == 1) {
                    return "Recurso " + idRecurso + " aprovado com sucesso.";
                }
            }

            // 0 linhas atualizadas: ver se existe e se ja estava aprovado
            String sqlSel = "SELECT estado_admin FROM recurso WHERE id_recurso = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlSel)) {
                ps.setInt(1, idRecurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        return "Recurso " + idRecurso + " nao existe.";
                    String estado = rs.getString(1);
                    if ("APROVADO".equalsIgnoreCase(estado)) {
                        return "O recurso " + idRecurso + " ja esta aprovado.";
                    }
                }
            }
            return "Nao foi possivel aprovar o recurso " + idRecurso;
        }
    }

    // ---- ADMIN: alterar estado operacional do recurso ----
    public void alterarEstadoRecurso(int idRecurso, String novoEstado) throws Exception {
        String sql = "UPDATE recurso SET estado = ?::estado_recurso_enum " +
                "WHERE id_recurso = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, novoEstado);
            ps.setInt(2, idRecurso);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new Exception("Recurso nao encontrado.");
            }
        }
    }

    // ---- ADMIN: alterar nº de copias de um livro ----
    public void alterarNumCopiasLivro(int idRecurso, int numCopias) throws Exception {
        String sql = "UPDATE livro SET num_copias = ? " +
                "WHERE id_recurso = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, numCopias);
            ps.setInt(2, idRecurso);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new Exception("Livro nao encontrado para esse recurso.");
            }
        }
    }

    // ========== LISTAGEM COM FILTROS (GENeRICO) ==========
    public List<String> listarRecursosFiltrado(String tipo, String estadoOp, String estadoAdmin, String texto)
            throws Exception {
        List<String> out = new ArrayList<>();
        if (tipo == null || tipo.isEmpty() || tipo.equalsIgnoreCase("LIVRO")) {
            out.addAll(listarLivrosFiltrado(estadoOp, estadoAdmin, texto));
        }
        if (tipo == null || tipo.isEmpty() || tipo.equalsIgnoreCase("COMPUTADOR")) {
            out.addAll(listarComputadoresFiltrado(estadoOp, estadoAdmin, texto));
        }
        if (tipo == null || tipo.isEmpty() || tipo.equalsIgnoreCase("SALA")) {
            out.addAll(listarSalasFiltrado(estadoOp, estadoAdmin, texto));
        }
        return out;
    }

    private List<String> listarLivrosFiltrado(String estadoOp, String estadoAdmin, String texto) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id_recurso, l.titulo, l.autor, l.num_copias, r.estado_admin, r.estado, " +
                        "       (l.num_copias - COALESCE((SELECT COUNT(*) FROM emprestimo e " +
                        "         WHERE e.id_recurso = r.id_recurso AND e.estado = 'ATIVO'::estado_emprestimo_enum),0)) AS disp "
                        +
                        "FROM recurso r JOIN livro l ON r.id_recurso = l.id_recurso WHERE 1=1 ");
        ArrayList<Object> ps = new ArrayList<>();
        if (estadoOp != null) {
            sql.append(" AND r.estado = ?::estado_recurso_enum ");
            ps.add(estadoOp);
        }
        if (estadoAdmin != null) {
            sql.append(" AND r.estado_admin = ?::estado_admin_enum ");
            ps.add(estadoAdmin);
        }
        if (texto != null) {
            sql.append(" AND (l.titulo ILIKE ? OR l.autor ILIKE ?) ");
            String like = "%" + texto + "%";
            ps.add(like);
            ps.add(like);
        }
        sql.append(" ORDER BY l.titulo ASC ");

        try (Connection con = getConnection(); PreparedStatement st = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++)
                st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) {
                ArrayList<String> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(String.format("#%d | Livro | %s - %s | Copias=%d (Disp=%d) | Estado=%s",
                            rs.getInt("id_recurso"), rs.getString("titulo"), rs.getString("autor"),
                            rs.getInt("num_copias"), rs.getInt("disp"),
                            rs.getString("estado")));
                }
                return out;
            }
        }
    }
    public void alterarUtilizacoesComputador(int idRecurso, int novoValor) throws Exception {
        if (novoValor < 0) throw new Exception("Valor de utilizacoes nao pode ser negativo.");

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            // validar que o recurso existe e é um COMPUTADOR
            String tipo = null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT tipo_recurso FROM recurso WHERE id_recurso = ?")) {
                ps.setInt(1, idRecurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new Exception("Recurso inexistente.");
                    tipo = rs.getString("tipo_recurso");
                }
            }
            if (!"COMPUTADOR".equalsIgnoreCase(tipo)) {
                throw new Exception("Recurso nao e um COMPUTADOR.");
            }

            // atualizar utilizacoes
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE computador SET utilizacoes = ? WHERE id_recurso = ?")) {
                ps.setInt(1, novoValor);
                ps.setInt(2, idRecurso);
                int n = ps.executeUpdate();
                if (n == 0) throw new Exception("Computador nao encontrado na tabela especifica.");
            }

            con.commit();
        } catch (Exception e) {
            throw e;
        }
    }

    private List<String> listarComputadoresFiltrado(String estadoOp, String estadoAdmin, String texto)
            throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id_recurso, c.modelo, c.utilizacoes, r.estado_admin, r.estado " +
                        "FROM recurso r JOIN computador c ON r.id_recurso = c.id_recurso WHERE 1=1 ");
        ArrayList<Object> ps = new ArrayList<>();
        if (estadoOp != null) {
            sql.append(" AND r.estado = ?::estado_recurso_enum ");
            ps.add(estadoOp);
        }
        if (estadoAdmin != null) {
            sql.append(" AND r.estado_admin = ?::estado_admin_enum ");
            ps.add(estadoAdmin);
        }
        if (texto != null) {
            sql.append(" AND (c.modelo ILIKE ?) ");
            ps.add("%" + texto + "%");
        }
        sql.append(" ORDER BY c.modelo ASC ");

        try (Connection con = getConnection(); PreparedStatement st = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++)
                st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) {
                ArrayList<String> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(String.format("#%d | PC | %s | Usos=%d | Estado=%s",
                            rs.getInt("id_recurso"), rs.getString("modelo"),
                            rs.getInt("utilizacoes"), rs.getString("estado")));
                }
                return out;
            }
        }
    }

    private List<String> listarSalasFiltrado(String estadoOp, String estadoAdmin, String texto) throws Exception {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id_recurso, s.nome, s.capacidade, s.localizacao, r.estado_admin, r.estado " +
                        "FROM recurso r JOIN sala_estudo s ON r.id_recurso = s.id_recurso WHERE 1=1 ");
        ArrayList<Object> ps = new ArrayList<>();
        if (estadoOp != null) {
            sql.append(" AND r.estado = ?::estado_recurso_enum ");
            ps.add(estadoOp);
        }
        if (estadoAdmin != null) {
            sql.append(" AND r.estado_admin = ?::estado_admin_enum ");
            ps.add(estadoAdmin);
        }
        if (texto != null) {
            sql.append(" AND s.nome ILIKE ? ");
            ps.add("%" + texto + "%");
        }
        sql.append(" ORDER BY s.nome ASC ");

        try (Connection con = getConnection(); PreparedStatement st = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < ps.size(); i++)
                st.setObject(i + 1, ps.get(i));
            try (ResultSet rs = st.executeQuery()) {
                ArrayList<String> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(String.format("#%d | Sala | %s (Capacidade=%d) | %s | Estado=%s",
                            rs.getInt("id_recurso"), rs.getString("nome"), rs.getInt("capacidade"),
                            rs.getString("localizacao"), rs.getString("estado")));
                }
                return out;
            }
        }
    }

}
