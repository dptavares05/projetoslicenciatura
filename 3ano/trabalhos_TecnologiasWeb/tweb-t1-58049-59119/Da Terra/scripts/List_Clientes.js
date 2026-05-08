document.addEventListener("DOMContentLoaded", function () {
    const estado = document.getElementById("estado-clientes-admin");
    const lista = document.getElementById("clientesLista");
    const pesquisaInput = document.getElementById("pesquisa-clientes-admin");

    let clientes = [];

    // --------- XHR GET ---------
    function xhrGET(url, callback) {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);

        xhr.onload = function () {
            if (xhr.status === 200) {
                try {
                    const data = JSON.parse(xhr.responseText);
                    callback(data);
                } catch (e) {
                    console.error("Erro a fazer parse do JSON:", e);
                    console.error("Resposta:", xhr.responseText);
                    callback(null);
                }
            } else {
                console.error("Erro GET", xhr.status, "em", url);
                callback(null);
            }
        };

        xhr.onerror = function () {
            console.error("Falha de rede em", url);
            callback(null);
        };

        xhr.send();
    }

    // --------- desenhar lista de clientes (em cards) ---------
    function mostrarClientes(listaClientes) {
        lista.innerHTML = "";

        if (!listaClientes || listaClientes.length === 0) {
            estado.textContent = "Nenhum cliente encontrado.";
            return;
        }

        estado.textContent = "";

        // Criar cards para cada cliente
        listaClientes.forEach(cliente => {
            const card = document.createElement("article");
            card.className = "oferta-card";

            const conteudo = document.createElement("div");
            conteudo.className = "oferta-conteudo";

            const titulo = document.createElement("h2");
            titulo.className = "oferta-nome";
            titulo.textContent = `${cliente.nome || "Cliente sem nome"} (ID: ${cliente.cliente_id})`;

            const username = document.createElement("p");
            username.className = "oferta-descricao";
            username.textContent = "Username: " + (cliente.username || "não indicado");

            const dataRegistro = document.createElement("p");
            dataRegistro.style.fontSize = "14px";
            dataRegistro.textContent = "Data de Registro: " + (cliente.data_de_registo || "não indicado");

            conteudo.appendChild(titulo);
            conteudo.appendChild(username);
            conteudo.appendChild(dataRegistro);

            card.appendChild(conteudo);
            lista.appendChild(card);
        });
    }

    // --------- filtro por nome ou ID ---------
    function filtrarClientes() {
        const filtro = pesquisaInput.value.toLowerCase();

        if (!filtro) {
            mostrarClientes(clientes);
            return;
        }

        const filtrados = clientes.filter(cliente => {
            const idStr = String(cliente.cliente_id || "").toLowerCase();
            const nome = (cliente.nome || "").toLowerCase();
            return idStr.includes(filtro) || nome.includes(filtro);
        });

        mostrarClientes(filtrados);
    }

    // --------- carregar clientes ---------
    estado.textContent = "A carregar clientes...";

    xhrGET("https://magno.di.uevora.pt/tweb/t1/admin/cliente/list", function (data) {
        if (!data || !data.cliente_set) {
            console.error("Resposta inesperada de /admin/cliente/list:", data);
            estado.textContent = "Erro ao carregar clientes.";
            return;
        }

        clientes = data.cliente_set;
        mostrarClientes(clientes);
    });

    // evento de pesquisa
    pesquisaInput.addEventListener("input", filtrarClientes);
});
