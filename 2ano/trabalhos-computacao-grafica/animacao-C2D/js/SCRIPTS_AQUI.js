const canvas = document.getElementById("sceneCanvas");
const gc = canvas.getContext("2d");


const boneco = { x: 100, y: 400, size: 50, speed: 2 };
const coracao = { x: 200, y: 450, size: 20, speed: 1.5 };
let direction = 1;

function drawBackground() {
    // Desenhar chão
    gc.fillStyle = "green";
    gc.fillRect(0, canvas.height - 100, canvas.width, 100);
  
    // Sol no canto superior esquerdo
    gc.beginPath();
    gc.arc(50, 50, 90, 0, Math.PI * 2);
    gc.fillStyle = "yellow";
    gc.fill();
  
    // Casas
    
    for (let i = 0; i < 700; i += 170) {
      // "tronco" da casa
      gc.fillStyle = "grey";
      gc.fillRect(i + 10, canvas.height - 200, 100, 100);

      //portas
      gc.fillStyle = "brown";
      gc.fillRect(i + 25, canvas.height - 160, 30, 60);

      //janelas
      gc.fillStyle = "lightblue";
      gc.fillRect(i + 70, canvas.height - 160, 30, 30);
      
  
      // Telhado rosa
      gc.beginPath();
      gc.moveTo(i , canvas.height - 200); // Ponto esquerdo do triângulo
      gc.lineTo(i + 60, canvas.height - 250); // Ponto do topo do triângulo
      gc.lineTo(i + 120, canvas.height - 200); // Ponto direito do triângulo
      gc.closePath();
      gc.fillStyle = "pink";
      gc.fill();
    }

  }
   //função q desenha o modelo da figura
  function drawFigura(x, y) {
    //sub-objetos gráficos da figura  
      // Tronco
      gc.fillStyle = "orange";
      gc.fillRect(x - 15, y - 40, 30, 40);
    
      // Pernas
      gc.fillStyle = "steelblue";
      gc.fillRect(x - 15, y, 30, 40);
    
      // Cabeça
      gc.beginPath();
      gc.arc(x, y - 50, 15, 0, Math.PI * 2);
      gc.fillStyle = "brown";
      gc.fill();
    
      // Braços mas é só uma linha
      gc.fillStyle = "orange";
      gc.fillRect(x - 25, y - 35, 50, 15);;
  }
 
  function drawCoracao(x, y) {
    // Coração (elemento seguidor)
    gc.beginPath();
    gc.moveTo(x, y); // Base do coração
    gc.bezierCurveTo(x - 10, y - 15, x - 20, y, x, y + 15); // Lado esquerdo
    gc.bezierCurveTo(x + 20, y, x + 10, y - 15, x, y); // Lado direito
    gc.fillStyle = "magenta";
    gc.fill();
  }
  
  function update() {
    gc.clearRect(0, 0, canvas.width, canvas.height);
  
    drawBackground();
    drawFigura(boneco.x, boneco.y);
    drawCoracao(coracao.x, coracao.y);
  
    // calculo dos movimentos da figura
    boneco.x += boneco.speed * direction;
    if (boneco.x > canvas.width - 50 || boneco.x < 50) {
      direction *= -1; // Inverter direção
    }
  
    // movimentar o coração consoante a figura
    if (coracao.x < boneco.x) {
      coracao.x += coracao.speed;
    } else if (coracao.x > boneco.x) {
      coracao.x -= coracao.speed;
    }
  
    // deslocar o coração tbm verticalmente para um efeito dinâmico
    coracao.y = boneco.y + Math.sin(boneco.x / 50) * 20;
  
    requestAnimationFrame(update);
  }
  
  // Iniciar a animação
  update();
  