document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("form");

    form.addEventListener("submit", async function (event) {
        event.preventDefault(); // Impede o comportamento normal (navegar para o servidor)

        const formData = new URLSearchParams();
        // Adiciona os campos do formulário ao formData
        formData.append("nome", document.getElementById("nome").value);
        formData.append("restaurante_id", document.getElementById("restaurante_id").value);
        formData.append("descricao", document.getElementById("descricao").value);
        formData.append("unidades", document.getElementById("unidades").value);
        formData.append("foto", document.getElementById("foto").value);

        // Envia a requisição POST para o servidor
        try {
            const response = await fetch("https://magno.di.uevora.pt/tweb/t1/oferta/insert", {
                method: "POST",
                body: formData
            });

            const data = await response.json();

            if (data.status === "ok") {
                alert("Oferta inserida com sucesso! ID da oferta: " + data.oferta_id);

                form.reset();
            } else {
                alert("Erro ao inserir oferta: " + JSON.stringify(data));
            }

        } catch (error) {
            console.error(error);
            alert("Ocorreu um erro ao comunicar com o servidor.");
        }
    });
});
