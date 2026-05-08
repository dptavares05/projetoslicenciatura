(* ===================================================================================
   TRABALHO P3: Agente de Batalha Naval (OCaml) Nº's : 58049-59119
   =================================================================================== *)

(* Inicializa o gerador de números aleatórios (apenas para o comando 'random') *)
let () = Random.self_init ()

(* --- Tipos de Dados --- *)
type coord = int * int

type tipo_barco =
  | PortaAvioes | Destroyer | Fragata | Torpedeiro | Submarino

type orientacao = Horizontal | Vertical | Rot90 | Rot180 | Rot270

type celula_defesa =
  | Mar | Navio of tipo_barco | Atingido of tipo_barco | ErroAdversario

type celula_ataque =
  | Desconhecido | Agua | Fogo | Afundado      

type info_barco = {
  id: tipo_barco;
  coords: coord list;
  mutable vidas: int;
}

(* --- Variáveis Globais --- *)
let tamanho = ref 8 
let tabuleiro_defesa = ref (Array.make_matrix 8 8 Mar)
let tabuleiro_ataque = ref (Array.make_matrix 8 8 Desconhecido)
let minha_frota = ref []
let pilha_alvos = ref [] 

(* --- Auxiliares Input/Output --- *)

let ler_linha_segura () =
  try String.trim (read_line ()) with End_of_file -> exit 0

let string_de_barco b =
  match b with
  | PortaAvioes -> "porta-avioes"
  | Destroyer -> "destroyer"
  | Fragata -> "fragata"
  | Torpedeiro -> "torpedeiro"
  | Submarino -> "submarino"

let tipo_de_string s =
  match String.lowercase_ascii s with
  | "porta-avioes" -> Some PortaAvioes
  | "destroyer" -> Some Destroyer
  | "fragata" -> Some Fragata
  | "torpedeiro" -> Some Torpedeiro
  | "submarino" -> Some Submarino
  | _ -> None

(* --- Tabuleiro e Geometria --- *)

let reset_tabuleiros n =
  tamanho := n;
  tabuleiro_defesa := Array.make_matrix n n Mar;
  tabuleiro_ataque := Array.make_matrix n n Desconhecido;
  pilha_alvos := [];
  minha_frota := [];
  ()

(* Função auxiliar para gerar coordenadas (usada no RANDOM) *)
let obter_coords_barco (l, c) tipo rot =
  match tipo with
  | Submarino -> [(l,c)]
  | Torpedeiro -> 
      if rot mod 2 = 0 then [(l,c); (l, c+1)] else [(l,c); (l+1, c)]
  | Fragata -> 
      if rot mod 2 = 0 then [(l,c); (l, c+1); (l, c+2)] else [(l,c); (l+1, c); (l+2, c)]
  | Destroyer -> 
      if rot mod 2 = 0 then [(l,c); (l, c+1); (l, c+2); (l, c+3)] else [(l,c); (l+1, c); (l+2, c); (l+3, c)]
  | PortaAvioes -> 
      match rot mod 4 with
      | 0 -> [(l,c); (l, c-1); (l, c+1); (l+1, c); (l+2, c)] (* T baixo *)
      | 1 -> [(l,c); (l-1, c); (l+1, c); (l, c-1); (l, c-2)] (* T esquerda *)
      | 2 -> [(l,c); (l, c-1); (l, c+1); (l-1, c); (l-2, c)] (* T cima *)
      | _ -> [(l,c); (l-1, c); (l+1, c); (l, c+1); (l, c+2)] (* T direita *)

(* Verifica se cabe e se não toca em mais nenhum barco que esteja por perto *)
let posicao_valida coords =
  let n = !tamanho in
  let dentro = List.for_all (fun (l, c) -> l >= 0 && l < n && c >= 0 && c < n) coords in
  if not dentro then false
  else
    let livre (l, c) =
      let checar_vizinho dl dc =
        let nl, nc = l + dl, c + dc in
        if nl >= 0 && nl < n && nc >= 0 && nc < n then
          match (!tabuleiro_defesa).(nl).(nc) with
          | Mar -> true
          | _ -> false
        else true
      in
      List.for_all (fun dl -> List.for_all (fun dc -> checar_vizinho dl dc) [-1;0;1]) [-1;0;1]
    in
    List.for_all livre coords

let colocar_barco tipo coords =
  List.iter (fun (l, c) -> (!tabuleiro_defesa).(l).(c) <- Navio tipo) coords;
  let novo = { id = tipo; coords = coords; vidas = List.length coords } in
  minha_frota := novo :: !minha_frota

(* --- Lógica Random --- *)

exception TabuleiroImpossivel

let rec tentar_colocar_com_limite tipo tentativas_restantes =
  if tentativas_restantes <= 0 then raise TabuleiroImpossivel
  else
    let n = !tamanho in
    let l = Random.int n in
    let c = Random.int n in
    let rot = Random.int 4 in
    let coords = obter_coords_barco (l, c) tipo rot in
    if posicao_valida coords then colocar_barco tipo coords
    else tentar_colocar_com_limite tipo (tentativas_restantes - 1)

let rec setup_random () =
  try
    reset_tabuleiros !tamanho;
    let frota = [PortaAvioes; Destroyer; Fragata; Fragata; Torpedeiro; Torpedeiro; Torpedeiro; Submarino] in
    List.iter (fun b -> tentar_colocar_com_limite b 1000) frota;
    () 
  with TabuleiroImpossivel -> setup_random ()

(* --- Tabuleiro_Defesa --- *)

let processar_tiro_recebido l c =
  let n = !tamanho in
  if l < 0 || l >= n || c < 0 || c >= n then
    print_endline "água"
  else
    match (!tabuleiro_defesa).(l).(c) with
    | Mar | ErroAdversario ->
        (!tabuleiro_defesa).(l).(c) <- ErroAdversario;
        print_endline "água"
    | Atingido _ -> print_endline "água" 
    | Navio tipo ->
        (!tabuleiro_defesa).(l).(c) <- Atingido tipo;
        let rec atualizar frota =
          match frota with
          | [] -> []
          | b :: resto ->
              if List.mem (l, c) b.coords then (
                b.vidas <- b.vidas - 1;
                if b.vidas = 0 then (
                  let vivos = List.exists (fun x -> x.vidas > 0) !minha_frota in
                  if not vivos then (print_endline "perdi"; exit 0)
                  else Printf.printf "afundado %s\n" (string_de_barco tipo)
                ) else Printf.printf "tiro %s\n" (string_de_barco tipo);
                b :: resto
              ) else b :: (atualizar resto)
        in
        minha_frota := atualizar !minha_frota

(* --- Ataque Agente --- *)

(* IA: Adiciona vizinhos à pilha de alvos *)
let adicionar_alvos_inteligentes (l, c) =
  let n = !tamanho in
  let tab = !tabuleiro_ataque in
  let eh_fogo cx cy = cx >= 0 && cx < n && cy >= 0 && cy < n && tab.(cx).(cy) = Fogo in
  let norte, sul = eh_fogo (l-1) c, eh_fogo (l+1) c in
  let oeste, este = eh_fogo l (c-1), eh_fogo l (c+1) in
  let novos = 
    if norte || sul then [(l-1, c); (l+1, c)] 
    else if oeste || este then [(l, c-1); (l, c+1)] 
    else [(l-1, c); (l+1, c); (l, c-1); (l, c+1)] 
  in
  let validos = List.filter (fun (x, y) ->
    x >= 0 && x < n && y >= 0 && y < n && tab.(x).(y) = Desconhecido
  ) novos in
  pilha_alvos := validos @ !pilha_alvos

(* Pinta água à volta de barcos afundados *)
let processar_afundanco_total l_origem c_origem =
  let n = !tamanho in
  let tab = !tabuleiro_ataque in
  let q = Queue.create () in
  tab.(l_origem).(c_origem) <- Afundado;
  Queue.add (l_origem, c_origem) q;
  let processado = Array.make_matrix n n false in
  processado.(l_origem).(c_origem) <- true;
  while not (Queue.is_empty q) do
    let (l, c) = Queue.take q in
    for dl = -1 to 1 do
      for dc = -1 to 1 do
        if dl <> 0 || dc <> 0 then (
          let nl, nc = l + dl, c + dc in
          if nl >= 0 && nl < n && nc >= 0 && nc < n then (
            match tab.(nl).(nc) with
            | Fogo -> 
                if not processado.(nl).(nc) then (
                   tab.(nl).(nc) <- Afundado;
                   processado.(nl).(nc) <- true;
                   Queue.add (nl, nc) q
                )
            | Desconhecido -> tab.(nl).(nc) <- Agua
            | _ -> ()
          )
        )
      done
    done
  done

(* Recuperar Focos Esquecidos no caso de acertar outro barco no meio de ataque a um barco *)
let reativar_focos_perdidos () =
  let n = !tamanho in
  let encontrou = ref false in
  for i = 0 to n - 1 do
    for j = 0 to n - 1 do
      if (!tabuleiro_ataque).(i).(j) = Fogo then (
        adicionar_alvos_inteligentes (i, j);
        encontrou := true
      )
    done
  done;
  if not !encontrou then pilha_alvos := []

(* ESTRATÉGIA DE TIRO *)
let realizar_ataque () =
  let n = !tamanho in
  
  let rec obter_alvo () =
    match !pilha_alvos with
    | (l, c) :: resto ->
        pilha_alvos := resto;
        if (!tabuleiro_ataque).(l).(c) = Desconhecido then (l, c) else obter_alvo ()
    | [] ->
        (* Estratégia Xadrez (Paridade) *)
        let alvo = ref None in
        
        (* 1. Pretas (soma par) *)
        for i = 0 to n - 1 do
          for j = 0 to n - 1 do
             if !alvo = None && (!tabuleiro_ataque).(i).(j) = Desconhecido then
               if (i + j) mod 2 = 0 then alvo := Some (i, j)
          done
        done;

        (* 2. Brancas (se necessário) *)
        if !alvo = None then (
          for i = 0 to n - 1 do
            for j = 0 to n - 1 do
               if !alvo = None && (!tabuleiro_ataque).(i).(j) = Desconhecido then
                 alvo := Some (i, j)
            done
          done
        );

        match !alvo with
        | Some (l, c) -> (l, c)
        | None -> (0, 0)
  in

  let (l, c) = obter_alvo () in
  Printf.printf "tiro %d %d\n" l c;
  flush stdout;
  
  let resp = ler_linha_segura () in
  if resp = "água" || resp = "agua" then (!tabuleiro_ataque).(l).(c) <- Agua
  else if resp = "perdi" then exit 0
  else if String.length resp >= 4 && String.sub resp 0 4 = "tiro" then (
    (!tabuleiro_ataque).(l).(c) <- Fogo; adicionar_alvos_inteligentes (l, c)
  ) else if String.length resp >= 8 && String.sub resp 0 8 = "afundado" then (
    processar_afundanco_total l c;
    pilha_alvos := [];
    reativar_focos_perdidos ()
  )

(* --- Loop Principal e Comandos --- *)

let rec loop_jogo vez_minha =
  if vez_minha then (realizar_ataque (); loop_jogo false)
  else (
    let linha = ler_linha_segura () in
    match String.split_on_char ' ' linha with
    | ["tiro"; ls; cs] -> 
        (try 
           let l = int_of_string ls in
           let c = int_of_string cs in
           processar_tiro_recebido l c; 
           flush stdout; 
           loop_jogo true
         with Failure _ -> loop_jogo false)
    | _ -> loop_jogo false
  )

(* --- MAIN --- *)

let () =
  let rec config () =
    try
      let linha = ler_linha_segura () in
      let tokens = List.filter (fun s -> s <> "") (String.split_on_char ' ' linha) in
      match tokens with
      | ["init"; n] -> 
          (try reset_tabuleiros (int_of_string n) with Failure _ -> ()); 
          config ()

      (* Comando Random *)
      | ["random"] -> 
          setup_random (); 
          config ()
      
      | "barco" :: nome_str :: coords_str ->
          (match tipo_de_string nome_str with
           | Some tipo ->
               (try
                 let nums = List.map int_of_string coords_str in
                 let rec agrupar_pares lista =
                   match lista with
                   | l :: c :: resto -> (l, c) :: agrupar_pares resto
                   | [] -> []
                   | _ -> [] 
                 in
                 let coords = agrupar_pares nums in
                 colocar_barco tipo coords
               with Failure _ -> ());
               config ()
           | None -> config () 
          )

      | ["vou"; "eu"] -> loop_jogo true  
      | ["vai"; "tu"] -> loop_jogo false 
      | _ -> config ()
    with _ -> exit 0
  in
  config ()