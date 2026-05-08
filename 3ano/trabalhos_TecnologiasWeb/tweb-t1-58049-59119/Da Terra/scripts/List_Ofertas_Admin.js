document.addEventListener("DOMContentLoaded", function () {
    const lista = document.getElementById("ofertasLista");
    const estado = document.getElementById("estado-carregamento");
    const pesquisaInput = document.getElementById("pesquisa-ofertas");

    let ofertas = [];
    let restaurantesPorId = {};
    let restaurantesEmPedido = {};

    // --------- XHR GET ---------
    function xhrGET(url, callback) {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);

        xhr.onload = function () {
            if (xhr.status === 200) {
                callback(JSON.parse(xhr.responseText));
            } else {
                console.error("Erro GET:", xhr.status, url);
                callback(null);
            }
        };

        xhr.onerror = function () {
            console.error("Falha de rede em:", url);
            callback(null);
        };

        xhr.send();
    }

    function xhrPOST(url, body, callback) {
        const xhr = new XMLHttpRequest();
        xhr.open("POST", url, true);

        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        xhr.onload = function () {
            if (xhr.status === 200) callback(JSON.parse(xhr.responseText));
            else callback(null);
        };

        xhr.onerror = function () {
            console.error("Erro na ligação (POST)");
            callback(null);
        };

        xhr.send(body);
    }

    // --------- obter nome do restaurante ---------
    function atualizarTitulo(tituloEl, oferta) {
        const restId = oferta.restaurante_id;

        // se já temos guardado
        if (restaurantesPorId[restId]) {
            tituloEl.textContent = `${oferta.nome}, Restaurante ${restaurantesPorId[restId]}`;
            return;
        }

        tituloEl.textContent = oferta.nome;

        // evitar pedidos duplicados
        if (restaurantesEmPedido[restId]) return;
        restaurantesEmPedido[restId] = true;

        xhrGET(`https://magno.di.uevora.pt/tweb/t1/restaurante/get/${restId}`, function (data) {
            if (!data || !data.result) return;

            const nomeRest = data.result.nome || ("Restaurante #" + restId);
            restaurantesPorId[restId] = nomeRest;

            // atualizar todos os títulos no ecrã
            document.querySelectorAll(`[data-restaurante-id="${restId}"]`)
                .forEach(el => {
                    const nomeOferta = el.getAttribute("data-oferta-nome");
                    el.textContent = `${nomeOferta}, Restaurante ${nomeRest}`;
                });
        });
    }

    // --------- filtro(por Nome Oferta e Nome Restaurante) ---------
    function filtrar() {
        const filtro = pesquisaInput.value.toLowerCase();

        if (!filtro) {
            mostrar(ofertas);
            return;
        }

        const filtradas = ofertas.filter(o => {
            const nomeOferta = o.nome.toLowerCase();
            const restNome = (restaurantesPorId[o.restaurante_id] || "").toLowerCase();
            return nomeOferta.includes(filtro) || restNome.includes(filtro);
        });

        mostrar(filtradas);
    }

    // --------- Mostrar Ofertas ---------
    function mostrar(listaOfertas) {
        lista.innerHTML = "";

        if (listaOfertas.length === 0) {
            estado.textContent = "Nenhuma oferta encontrada.";
            return;
        }

        estado.textContent = "";

        listaOfertas.forEach(oferta => {
            const article = document.createElement("article");
            article.className = "oferta-card";

            const img = document.createElement("img");
            img.className = "oferta-imagem";
            img.src = oferta.foto || "images/default.png";

            const conteudo = document.createElement("div");
            conteudo.className = "oferta-conteudo";

            const titulo = document.createElement("h2");
            titulo.setAttribute("data-restaurante-id", oferta.restaurante_id);
            titulo.setAttribute("data-oferta-nome", oferta.nome);

            atualizarTitulo(titulo, oferta);

            const desc = document.createElement("p");
            desc.className = "oferta-descricao";
            desc.textContent = oferta.descricao;

            const unidadesP = document.createElement("p");
            unidadesP.textContent = "Unidades Disponíveis: " + oferta.unidades;

            const acoes = document.createElement("div");
            acoes.className = "oferta-acoes";

            const qtd = document.createElement("input");
            qtd.type = "number";
            qtd.min = 1;
            qtd.max = oferta.unidades;
            qtd.value = 1;

            const botao = document.createElement("button");
            botao.className = "btn primary";
            botao.textContent = "Reservar";

            botao.onclick = () => {
                const unidades = parseInt(qtd.value);

                if (isNaN(unidades) || unidades <= 0) {
                    alert("Quantidade inválida!");
                    return;
                }

                const body = `oferta_id=${oferta.oferta_id}&unidades=${unidades}&cliente_id=1`;

                xhrPOST("https://magno.di.uevora.pt/tweb/t1/oferta/reserve", body, function (resp) {
                    if (resp && resp.status === "ok")
                        alert("Oferta reservada com sucesso!");
                    else
                        alert("Erro ao reservar!");
                });
            };

            acoes.appendChild(qtd);
            acoes.appendChild(botao);

            conteudo.appendChild(titulo);
            conteudo.appendChild(desc);
            conteudo.appendChild(unidadesP);
            conteudo.appendChild(acoes);

            article.appendChild(img);
            article.appendChild(conteudo);
            lista.appendChild(article);
        });
    }

    // --------- carregar ofertas ---------
    xhrGET("https://magno.di.uevora.pt/tweb/t1/admin/oferta/list", function (data) {
        if (!data || !data.oferta_set) {
            estado.textContent = "Erro ao carregar ofertas.";
            return;
        }

        ofertas = data.oferta_set;
        mostrar(ofertas);
    });

    pesquisaInput.addEventListener("input", filtrar);
});
