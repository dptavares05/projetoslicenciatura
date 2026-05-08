const arena = document.getElementById("arena");
const scoreDisplay = document.getElementById("score");

const arenaWidth =250;
const arenaHeight = 300;
const minRadius = 20;
const maxRadius = 50;
let score = 0;
let gameInterval;


const model = {
    robot: {
        x: 0,
        y: 0,
        w: 40,
        h: 40,
        vx: 4, 
        vy: 4, 
        dx: 0,
        dy: 0,
    },
    islands: [],
    phase: 0,
};

const foot = document.createElementNS("http://www.w3.org/2000/svg", "image");
foot.setAttribute("width", model.robot.w);
foot.setAttribute("height", model.robot.h);
foot.setAttribute("href", "media/bus.png");
arena.appendChild(foot);


function updateFoot() {
    const robot = model.robot;

    robot.x += robot.dx * robot.vx;
    robot.y += robot.dy * robot.vy;

    // Limites da arena
    robot.x = Math.max(0, Math.min(robot.x, arenaWidth - robot.w));
    robot.y = Math.max(0, Math.min(robot.y, arenaHeight - robot.h));

    foot.setAttribute("x", robot.x);
    foot.setAttribute("y", robot.y);
}

const min_islands = 5;

function createIslands() {
    while (model.islands.length < min_islands) {
        const island = {
            x: Math.random() * (arenaWidth - maxRadius * 2) + maxRadius,
            y: Math.random() * (arenaHeight - maxRadius * 2) + maxRadius,
            radius: Math.random() * (maxRadius - minRadius) + minRadius,
            growing: true,
            age: 0,
        };

        const islandElem = document.createElementNS("http://www.w3.org/2000/svg", "circle");
        islandElem.setAttribute("cx", island.x);
        islandElem.setAttribute("cy", island.y);
        islandElem.setAttribute("r", island.radius);
        islandElem.setAttribute("fill", "#FFD700");
        arena.insertBefore(islandElem, foot);
        model.islands.push({ ...island, element: islandElem });
    }
    // Posiciona o pé sobre a primeira ilha
    const firstIsland = model.islands[0];
    model.robot.x = firstIsland.x - model.robot.w / 2;
    model.robot.y = firstIsland.y - model.robot.h / 2;
    updateFoot();
}



function updateIslands() {
    model.islands.forEach(island => {
        island.age++;

       
        if (island.growing) {
            island.radius += 2;
            if (island.radius >= maxRadius) island.growing = false;
        } else {
            island.radius -= 2;
            if (island.radius <= minRadius) island.growing = true;
        }

        // Substituir ilha logo dps do 300º ciclo
        if (island.age >= 300) {
            island.x = Math.random() * (arenaWidth - maxRadius * 2) + maxRadius;
            island.y = Math.random() * (arenaHeight - maxRadius * 2) + maxRadius;
            island.radius = Math.random() * (maxRadius - minRadius) + minRadius;
            island.age = 0;
        }

        // atualiza a ilha
        island.element.setAttribute("cx", island.x);
        island.element.setAttribute("cy", island.y);
        island.element.setAttribute("r", island.radius);
    });
}

// Ver se o jogador perdeu
function checkCollision() {
    const robot = model.robot;

    const isOnIsland = model.islands.some(island => {
        const distance = Math.hypot(robot.x + robot.w / 2 - island.x, robot.y + robot.h / 2 - island.y);
        return distance <= island.radius;
    });

    if (!isOnIsland) {
        clearInterval(gameInterval);
        alert(`Game Over! Score: ${score}`);
    }
}


function gameLoop() {
    score++;
    scoreDisplay.textContent = `Pontuação: ${score}`;
    updateFoot();
    updateIslands();
    checkCollision();
}

// input wasd
document.addEventListener("keydown", e => {
    switch (e.key.toLowerCase()) {
        case "w": model.robot.dy = -1; break;
        case "s": model.robot.dy = 1; break;
        case "a": model.robot.dx = -1; break;
        case "d": model.robot.dx = 1; break;
    }
});

document.addEventListener("keyup", e => {
    switch (e.key.toLowerCase()) {
        case "w":
        case "s": model.robot.dy = 0; break;
        case "a":
        case "d": model.robot.dx = 0; break;
    }
});

// Inicializar a gameplay sinistra
function startGame() {
    createIslands();
    updateFoot();
    gameInterval = setInterval(gameLoop, 50);
}

startGame();
