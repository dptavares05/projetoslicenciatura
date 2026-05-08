package uevora.sd.admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class AdminCLI {

    // URL do Servidor
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("##################################################");
        System.out.println("#      SISTEMA DE MONITORIZACAO AMBIENTAL        #");
        System.out.println("#             CLIENTE DE ADMINISTRACAO           #");
        System.out.println("##################################################");

        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Gestao de Dispositivos");
            System.out.println("2. Consulta de Metricas");
            System.out.println("3. Estatisticas do Sistema");
            System.out.println("0. Sair");
            System.out.print("Selecione uma opcao > ");

            String opt = scanner.nextLine();

            switch (opt) {
                case "1" -> menuGestaoDispositivos();
                case "2" -> menuConsultaMetricas();
                case "3" -> menuEstatisticas();
                case "0" -> {
                    System.out.println("A encerrar sistema...");
                    return;
                }
                default -> System.out.println("[X] Opcao invalida!");
            }
        }
    }

    // =========================================================================
    // 1. MENU GESTAO DE DISPOSITIVOS
    // =========================================================================
    private static void menuGestaoDispositivos() {
        System.out.println("\n--- GESTAO DE DISPOSITIVOS ---");
        System.out.println("1. Listar todos os dispositivos");
        System.out.println("2. Adicionar novo dispositivo");
        System.out.println("3. Atualizar dispositivo existente");
        System.out.println("4. Remover dispositivo");
        System.out.println("5. Visualizar detalhes");
        System.out.println("0. Voltar ao Menu Principal");
        System.out.print("Opcao > ");

        String opt = scanner.nextLine();

        switch (opt) {
            case "1" -> listarDispositivos();
            case "2" -> adicionarDispositivo();
            case "3" -> atualizarDispositivo();
            case "4" -> removerDispositivo();
            case "5" -> visualizarDetalhes();
            case "0" -> {} // Volta para tras
            default -> System.out.println("[X] Opcao invalida!");
        }
    }

    private static void listarDispositivos() {
        try {
            String json = sendRequest(BASE_URL + "/devices", "GET", null);
            if (json == null) return;
            JsonArray lista = gson.fromJson(json, JsonArray.class);

            System.out.println("\n>>> LISTA DE DISPOSITIVOS REGISTADOS");
            imprimirTabelaDispositivos(lista);

        } catch (Exception e) {
            System.out.println("[X] Erro ao listar dispositivos.");
        }
    }

    private static void adicionarDispositivo() {
        System.out.println("\n>>> ADICIONAR NOVO DISPOSITIVO");
        try {
            JsonObject json = new JsonObject();
            
            System.out.print("ID Unico (ex: sensor-sala-01): ");
            json.addProperty("id", scanner.nextLine());

            System.out.print("Protocolo (MQTT, GRPC, REST): ");
            json.addProperty("protocol", scanner.nextLine());

            System.out.print("Sala: ");
            json.addProperty("room", scanner.nextLine());

            System.out.print("Departamento: ");
            json.addProperty("department", scanner.nextLine());

            System.out.print("Piso: ");
            json.addProperty("floor", scanner.nextLine());

            System.out.print("Edificio: ");
            json.addProperty("building", scanner.nextLine());

            json.addProperty("active", true); // Por defeito, cria-se ativo

            sendRequest(BASE_URL + "/devices", "POST", json.toString());
            System.out.println("[OK] Dispositivo adicionado com sucesso!");

        } catch (Exception e) {
            System.out.println("[X] Erro ao criar dispositivo.");
        }
    }

    private static void atualizarDispositivo() {
        System.out.println("\n>>> ATUALIZAR DISPOSITIVO");
        System.out.print("Introduza o ID do dispositivo a editar: ");
        String id = scanner.nextLine();

        try {
            // 1. Primeiro vamos buscar os dados atuais
            String currentJson = sendRequest(BASE_URL + "/devices/" + id, "GET", null);
            if (currentJson == null) {
                System.out.println("[X] Dispositivo nao encontrado.");
                return;
            }
            JsonObject device = gson.fromJson(currentJson, JsonObject.class);

            // 2. Pedimos os novos dados (Enter para manter)
            System.out.println("(Pressione ENTER para manter o valor atual)");

            String input;

            // Sala
            System.out.print("Nova Sala [" + getStr(device, "room") + "]: ");
            input = scanner.nextLine();
            if (!input.isBlank()) device.addProperty("room", input);

            // Departamento
            System.out.print("Novo Departamento [" + getStr(device, "department") + "]: ");
            input = scanner.nextLine();
            if (!input.isBlank()) device.addProperty("department", input);

            // Piso
            System.out.print("Novo Piso [" + getStr(device, "floor") + "]: ");
            input = scanner.nextLine();
            if (!input.isBlank()) device.addProperty("floor", input);

            // Edificio
            System.out.print("Novo Edificio [" + getStr(device, "building") + "]: ");
            input = scanner.nextLine();
            if (!input.isBlank()) device.addProperty("building", input);

            // Protocolo
            System.out.print("Novo Protocolo [" + getStr(device, "protocol") + "]: ");
            input = scanner.nextLine();
            if (!input.isBlank()) device.addProperty("protocol", input);

            // Estado
            boolean ativoAtual = device.get("active").getAsBoolean();
            System.out.print("Ativo? (true/false) [" + ativoAtual + "]: ");
            input = scanner.nextLine();
            if (!input.isBlank()) device.addProperty("active", Boolean.parseBoolean(input));

            // 3. Enviamos a atualizacao
            sendRequest(BASE_URL + "/devices/" + id, "PUT", device.toString());
            System.out.println("[OK] Dispositivo atualizado com sucesso!");

        } catch (Exception e) {
            System.out.println("[X] Erro ao atualizar.");
        }
    }

    private static void removerDispositivo() {
        System.out.print("\nID do dispositivo a remover: ");
        String id = scanner.nextLine();
        try {
            sendRequest(BASE_URL + "/devices/" + id, "DELETE", null);
            System.out.println("[OK] Dispositivo removido da Base de Dados.");
        } catch (Exception e) {
            System.out.println("[X] Erro ao remover (verifique se o ID existe).");
        }
    }

    private static void visualizarDetalhes() {
        System.out.print("\nID do dispositivo para detalhes: ");
        String id = scanner.nextLine();
        try {
            String json = sendRequest(BASE_URL + "/devices/" + id, "GET", null);
            if (json == null) {
                System.out.println("[X] Nao encontrado.");
                return;
            }
            JsonObject o = gson.fromJson(json, JsonObject.class);

            System.out.println("\n====== DETALHES DO DISPOSITIVO ======");
            System.out.println("ID:           " + getStr(o, "id"));
            System.out.println("Protocolo:    " + getStr(o, "protocol"));
            System.out.println("Status:       " + (o.get("active").getAsBoolean() ? "ATIVO" : "INATIVO"));
            System.out.println("-------------------------------------");
            System.out.println("Localizacao:");
            System.out.println("  Sala:       " + getStr(o, "room"));
            System.out.println("  Dept:       " + getStr(o, "department"));
            System.out.println("  Piso:       " + getStr(o, "floor"));
            System.out.println("  Edificio:   " + getStr(o, "building"));
            System.out.println("=====================================");

        } catch (Exception e) {
            System.out.println("[X] Erro ao obter detalhes.");
        }
    }

    // =========================================================================
    // 2. MENU CONSULTA DE METRICAS (Cascata)
    // =========================================================================
    private static void menuConsultaMetricas() {
        System.out.println("\n--- CONSULTA DE METRICAS ---");
        System.out.println("Preencha o campo pelo qual quer filtrar.");
        System.out.println("Pressione ENTER para saltar um campo.");
        
        String nivel = null;
        String id = null;

        // 1. Tentar filtrar por Sala
        System.out.print("Filtrar por Sala? (Nome da sala): ");
        String inputSala = scanner.nextLine();
        if (!inputSala.isBlank()) {
            nivel = "sala";
            id = inputSala;
        }

        // 2. Se nao escolheu Sala, tentar Dept
        if (nivel == null) {
            System.out.print("Filtrar por Departamento? (Nome do dept): ");
            String inputDept = scanner.nextLine();
            if (!inputDept.isBlank()) {
                nivel = "departamento";
                id = inputDept;
            }
        }

        // 3. Se nao escolheu Dept, tentar Piso
        if (nivel == null) {
            System.out.print("Filtrar por Piso? (Nome do piso): ");
            String inputPiso = scanner.nextLine();
            if (!inputPiso.isBlank()) {
                nivel = "piso";
                id = inputPiso;
            }
        }

        // 4. Se nao escolheu Piso, tentar Edificio
        if (nivel == null) {
            System.out.print("Filtrar por Edificio? (Nome do edificio): ");
            String inputEd = scanner.nextLine();
            if (!inputEd.isBlank()) {
                nivel = "edificio";
                id = inputEd;
            }
        }

        if (nivel == null) {
            System.out.println("[!] Nenhum filtro selecionado. A voltar...");
            return;
        }

        // 5. Intervalo de Datas
        System.out.println("\n--- Intervalo de Datas (Opcional: YYYY-MM-DD) ---");
        System.out.print("Data Inicio [Enter = ultimas 24h]: ");
        String from = scanner.nextLine();
        
        System.out.print("Data Fim    [Enter = agora]:       ");
        String to = scanner.nextLine();

        // 6. Fazer a Query
        try {
            System.out.println("\n[...] A consultar dados...");
            String encodedId = id.replace(" ", "%20");
            String url = String.format("%s/metrics/average?level=%s&id=%s", BASE_URL, nivel, encodedId);

            if (!from.isBlank()) url += "&from=" + from + "T00:00:00";
            if (!to.isBlank())   url += "&to=" + to + "T23:59:59";

            String json = sendRequest(url, "GET", null);
            JsonObject res = gson.fromJson(json, JsonObject.class);

            if (res.has("message")) {
                System.out.println("[AVISO] " + getStr(res, "message"));
            } else {
                // Formato Tabular
                System.out.println("\n>>> RESULTADO DA PESQUISA");
                System.out.println("-----------------------------------------------------------------------");
                System.out.printf("| %-20s | %-15s | %-10s | %-10s | %-6s |%n", "LOCAL", "TIPO FILTRO", "TEMP(C)", "HUM(%)", "N.SENS");
                System.out.println("-----------------------------------------------------------------------");
                System.out.printf("| %-20s | %-15s | %-10.2f | %-10.2f | %-6d |%n",
                        getStr(res, "id"),
                        getStr(res, "level").toUpperCase(),
                        res.get("averageTemperature").getAsDouble(),
                        res.get("averageHumidity").getAsDouble(),
                        res.get("sensorsAnalyzed").getAsInt());
                System.out.println("-----------------------------------------------------------------------");
            }

        } catch (Exception e) {
            System.out.println("[X] Erro ao consultar metricas.");
        }
    }

    // =========================================================================
    // 3. MENU ESTATISTICAS DO SISTEMA
    // =========================================================================
    private static void menuEstatisticas() {
        try {
            
            // 1. Estatisticas de Dispositivos
            String jsonDev = sendRequest(BASE_URL + "/devices", "GET", null);
            JsonArray listaDev = gson.fromJson(jsonDev, JsonArray.class);
            
            int totalDev = listaDev.size();
            long ativos = 0;
            long mqtt = 0, grpc = 0, rest = 0;

            for(JsonElement e : listaDev) {
                JsonObject o = e.getAsJsonObject();
                if(o.get("active").getAsBoolean()) ativos++;
                
                String proto = getStr(o, "protocol").toUpperCase();
                if(proto.contains("MQTT")) mqtt++;
                else if(proto.contains("GRPC")) grpc++;
                else if(proto.contains("REST")) rest++;
            }

            // 2. Estatisticas de Metricas (Media Global)
            // Pedimos TODAS as metricas para calcular a media global
            String jsonMet = sendRequest(BASE_URL + "/metrics", "GET", null);
            JsonArray listaMet = gson.fromJson(jsonMet, JsonArray.class);
            
            double somaTemp = 0;
            double somaHum = 0;
            int totalReadings = listaMet.size();

            for(JsonElement e : listaMet) {
                JsonObject o = e.getAsJsonObject();
                somaTemp += o.get("temperature").getAsDouble();
                somaHum += o.get("humidity").getAsDouble();
            }

            double mediaTempGlobal = totalReadings > 0 ? somaTemp / totalReadings : 0;
            double mediaHumGlobal = totalReadings > 0 ? somaHum / totalReadings : 0;


            // MOSTRAR RELATORIO
            System.out.println("\n==========================================");
            System.out.println("         ESTATISTICAS GERAIS              ");
            System.out.println("==========================================");
            System.out.println("DISPOSITIVOS:");
            System.out.printf("  Total Registados:  %d%n", totalDev);
            System.out.printf("  Ativos:            %d%n", ativos);
            System.out.printf("  Inativos:          %d%n", (totalDev - ativos));
            System.out.println("  ----------------");
            System.out.printf("  Via MQTT:          %d%n", mqtt);
            System.out.printf("  Via gRPC:          %d%n", grpc);
            System.out.printf("  Via REST:          %d%n", rest);
            System.out.println("==========================================");
            System.out.println("METRICAS (HISTORICO):");
            System.out.printf("  Total Leituras:    %d%n", totalReadings);
            System.out.printf("  Temp. Media Global: %.2f C%n", mediaTempGlobal);
            System.out.printf("  Hum. Media Global:  %.2f %%%n", mediaHumGlobal);
            System.out.println("==========================================");

        } catch (Exception e) {
            System.out.println("[X] Erro ao calcular estatisticas. O servidor esta ligado?");
        }
    }

    // =========================================================================
    // UTILITARIOS
    // =========================================================================
    
    // Funcao para desenhar tabela de dispositivos
    private static void imprimirTabelaDispositivos(JsonArray lista) {
        String format = "| %-25s | %-6s | %-12s | %-15s | %-8s | %-12s | %-8s |%n";
        
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.printf(format, "ID", "PROTO", "SALA", "DEPT", "PISO", "EDIFICIO", "ESTADO");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        
        for (JsonElement e : lista) {
            JsonObject o = e.getAsJsonObject();
            String estado = o.get("active").getAsBoolean() ? "ATIVO" : "INATIVO";
            
            System.out.printf(format,
                    getStr(o, "id"),
                    getStr(o, "protocol"),
                    getStr(o, "room"),
                    getStr(o, "department"),
                    getStr(o, "floor"),
                    getStr(o, "building"),
                    estado);
        }
        System.out.println("------------------------------------------------------------------------------------------------------------");
    }

    private static String sendRequest(String url, String method, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        switch (method) {
            case "POST" -> builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            case "PUT" -> builder.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
            case "DELETE" -> builder.DELETE();
            case "GET" -> builder.GET();
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) return response.body();
        return null;
    }

    private static String getStr(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "N/A";
    }
}