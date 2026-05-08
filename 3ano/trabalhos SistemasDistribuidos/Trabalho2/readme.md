# 🌍 Sistema de Monitorização Ambiental (IoT Distribuído)

**Universidade de Évora - Sistemas Distribuídos**

Este projeto implementa uma solução robusta para a monitorização de temperatura e humidade em tempo real. O sistema destaca-se pela sua arquitetura híbrida e agnóstica ao protocolo, permitindo a ingestão simultânea de dados via **MQTT**, **gRPC** e **REST**, centralizando o processamento e a persistência num servidor Spring Boot.

---

## Arquitetura do Sistema

O sistema segue uma arquitetura distribuída composta pelos seguintes módulos:

1. **Sensores (Clientes):** Três tipos de simuladores Java que geram dados sintéticos.
    * **Sensor MQTT:** Simula dispositivos de baixo consumo (bateria/jardim).
    * **Sensor gRPC:** Simula gateways de alta performance (laboratórios/industrial).
    * **Sensor REST:** Simula dispositivos genéricos com capacidade HTTP.
2. **Infraestrutura (Docker):**
    * **PostgreSQL:** Base de dados relacional para persistência de dispositivos e métricas.
    * **Message Broker (ActiveMQ/Mosquitto):** Para gestão de mensagens MQTT.
3. **Servidor Central (Backend):** Aplicação Spring Boot que orquestra a ingestão de dados, validação de negócio centralizada e exposição de API.
4. **Admin CLI:** Cliente de consola para gestão de dispositivos e visualização de relatórios.

---

## Tecnologias Utilizadas

* **Linguagem:** Java 17+
* **Framework:** Spring Boot 3.x (Web, Data JPA, Integration)
* **Protocolos:**
  * **gRPC:** Google Protocol Buffers (`.proto`)
  * **MQTT:** Eclipse Paho Client
  * **REST:** Java `HttpClient` & Spring Web MVC
* **Base de Dados:** PostgreSQL
* **DevOps:** Docker & Docker Compose, Maven

---

## Pré-requisitos

* **Java JDK 17** ou superior.
* **Maven** instalado.
* **Docker** e **Docker Compose** a correr.

---

## Instalação e Execução

Siga a ordem abaixo para garantir que todos os componentes comunicam corretamente.

### 1. Iniciar a Infraestrutura (Docker)

Na raiz do projeto (onde está o `docker-compose.yml`), execute:

```bash
docker-compose up -d
```

*Isto iniciará a Base de Dados PostgreSQL e o Broker MQTT.*

### 2. Iniciar o Servidor

Navegue até à pasta do servidor e execute:

```bash
cd server
mvn spring-boot:run
```

*O servidor ficará à escuta nas portas: 8080 (REST), 9090 (gRPC) e 1883 (ligação ao Broker).*

### 3. Configurar Dispositivos (Admin CLI)

**Importante:** O servidor rejeita dados de sensores desconhecidos por segurança. Utilize o CLI para registar os IDs primeiro.

```bash
cd client-admin
mvn exec:java
```

1. No menu, escolha **"1. Gestão de Dispositivos"**.
2. Escolha **"2. Adicionar novo dispositivo"**.
3. Registe os IDs que pretende usar nos sensores (ex: `sensor-jardim`, `sensor-lab`, `sensor-sala`).

### 4. Correr os Sensores

Abra novos terminais para cada sensor que deseja simular.

**Sensor REST:**

```bash
cd client-rest
mvn exec:java
# Introduza o ID registado quando pedido
```

**Sensor MQTT:**

```bash
cd client-mqtt
mvn exec:java
# Introduza o ID registado quando pedido
```

**Sensor gRPC:**

```bash
cd client-grpc
mvn exec:java
# Introduza o ID registado quando pedido
```

---

## Funcionalidades

### Gestão Centralizada (Admin CLI)

* **CRUD Completo:** Registar, Listar, Atualizar e Remover sensores.
* **Tabelas Formatadas:** Visualização clara do estado dos dispositivos, protocolo e localização.

### Monitorização e Estatísticas

* **Médias Agregadas:** Consulta de temperatura/humidade média filtrada por **Sala**, **Piso**, **Departamento** ou **Edifício**.
* **Relatório Global:** Visualização em tempo real de todos os dispositivos ativos e suas médias de leituras.
* **Logs do Sistema:** Contagem de dispositivos por protocolo e leituras totais.

### Simulação Realista

* **Dados Sintéticos:** Os sensores geram variações graduais de temperatura (15-30ºC) e humidade (30-80%).
* **Segurança:** Sistema de "Whitelisting" (apenas IDs registados na BD podem enviar dados).
* **Resiliência:** Tratamento de erros caso o servidor esteja offline.

---

## Decisões de Arquitetura

1. **Agnosticismo de Protocolo:**
    A camada de negócio (`MetricsProcessor`) é independente do protocolo de entrada. O código que valida e grava na base de dados é único, garantindo consistência e facilidade de manutenção.

2. **Escolha de Protocolos:**
    * **MQTT** para a "borda" (sensores a bateria) devido ao baixo *overhead*.
    * **gRPC** para gateways concentradores devido à performance binária.
    * **REST** para gestão e integração web pela sua universalidade.

3. **Dados Agregados vs. Raw:**
    O servidor armazena todos os dados brutos (`raw`), mas a API expõe endpoints otimizados para cálculo de médias (`average`), reduzindo o tráfego de rede para o cliente de administração.

---
