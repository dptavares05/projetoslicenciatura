document.addEventListener("DOMContentLoaded", function () {
    const lista = document.getElementById("restaurantesLista");
    const estado = document.getElementById("estado-restaurantes");
    const pesquisaInput = document.getElementById("pesquisa-restaurantes");

    let restaurantes = [];

    // --------- XHR GET ---------
    function xhrGET(url, cb) {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", url);
        xhr.onload = () => cb(xhr.status === 200 ? JSON.parse(xhr.responseText) : null);
        xhr.onerror = () => cb(null);
        xhr.send();
    }

    function filtrar() {
        const f = pesquisaInput.value.toLowerCase();
        if (!f) return mostrar(restaurantes);

        const filtrados = restaurantes.filter(r => {
            const nome = r.nome.toLowerCase();
            const morada = (r.localizacao?.morada || "").toLowerCase();
            return nome.includes(f) || morada.includes(f);
        });

        mostrar(filtrados);
    }

    // --------- Mostrar Restaurantes ---------
    function mostrar(listaRest) {
        lista.innerHTML = "";

        if (listaRest.length === 0) {
            estado.textContent = "Nenhum restaurante encontrado.";
            return;
        }

        estado.textContent = "";

        listaRest.forEach(r => {
            const card = document.createElement("article");
            card.className = "restaurante-card";

            const h2 = document.createElement("h2");
            h2.className = "restaurante-nome";
            h2.textContent = `${r.nome} (#${r.restaurante_id})`;

            const p1 = document.createElement("p");
            p1.className = "restaurante-localizacao";
            p1.textContent = "Morada: " + (r.localizacao?.morada || "N/A");

            const p2 = document.createElement("p");
            p2.className = "restaurante-coords";
            p2.textContent = "Código Postal: " + (r.localizacao?.cod_postal || "N/A");

            card.appendChild(h2);
            card.appendChild(p1);
            card.appendChild(p2);

            lista.appendChild(card);
        });
    }

    xhrGET("https://magno.di.uevora.pt/tweb/t1/restaurante/list", data => {
        if (!data || !data.restaurante_set) {
            estado.textContent = "Erro ao carregar restaurantes.";
            return;
        }

        restaurantes = data.restaurante_set;
        mostrar(restaurantes);
    });

    pesquisaInput.addEventListener("input", filtrar);
});
