document.addEventListener("DOMContentLoaded", () => {
    const estado = document.getElementById("estado-restaurantes-admin");
    const tabela = document.getElementById("tabela-restaurantes-admin");
    const pesquisa = document.getElementById("pesquisa-restaurantes-admin");

    let restaurantes = [];

    // --------- XHR GET ---------
    function xhrGET(url, cb) {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", url);
        xhr.onload = () => cb(xhr.status === 200 ? JSON.parse(xhr.responseText) : null);
        xhr.onerror = () => cb(null);
        xhr.send();
    }

    // --------- Mostrar Restaurantes ---------
    function desenhar(lista) {
        tabela.innerHTML = "";

        if (!lista || lista.length === 0) {
            estado.textContent = "Nenhum restaurante encontrado.";
            return;
        }

        estado.textContent = "";

        lista.forEach(r => {
            const tr = document.createElement("tr");

            const loc = r.localizacao || {};
            const lat = loc.lat ?? "";
            const lng = loc.long ?? loc.lng ?? "";

            tr.innerHTML = `
                <td>${r.restaurante_id}</td>
                <td>${r.nome}</td>
                <td>${loc.morada || ""}</td>
                <td>${loc.cod_postal || ""}</td>
                <td>${lat}</td>
                <td>${lng}</td>
                <td>${r.proprietario}</td>
                <td>${r.data_de_registo}</td>
            `;

            tabela.appendChild(tr);
        });
    }

    function filtrar() {
        const f = pesquisa.value.toLowerCase();
        if (!f) return desenhar(restaurantes);

        const filtrados = restaurantes.filter(r => {
            const nome = (r.nome || "").toLowerCase();
            const morada = (r.localizacao?.morada || "").toLowerCase();
            return nome.includes(f) || morada.includes(f);
        });

        desenhar(filtrados);
    }

    xhrGET("https://magno.di.uevora.pt/tweb/t1/admin/restaurante/list", data => {
        if (!data || !data.restaurante_set) {
            estado.textContent = "Erro ao carregar.";
            return;
        }

        restaurantes = data.restaurante_set;
        desenhar(restaurantes);
    });

    pesquisa.addEventListener("input", filtrar);
});
