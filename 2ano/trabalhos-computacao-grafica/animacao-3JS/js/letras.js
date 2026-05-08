// setup da cena
let scene = new THREE.Scene();
let camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
let renderer = new THREE.WebGLRenderer();
renderer.setSize(window.innerWidth, window.innerHeight);
document.body.appendChild(renderer.domElement);

// luz
let light = new THREE.PointLight(0xffffff);
light.position.set(50, 10, 0);
scene.add(light);
scene.add(new THREE.AmbientLight(0x404040));


//não consegui fazer com extrusões e segmentos por isso foi com fonte da letra
let fontLoader = new THREE.FontLoader();
fontLoader.load('media/times2.json', (font) => {
    let letters = 'gladiai'.split('');
    let materials = [
        new THREE.MeshPhongMaterial({ color: 0xff6347 }),
        new THREE.MeshPhongMaterial({ color: 0x2e8b57 })
    ];

    let group = new THREE.Group();
        //cada letra tem os mms parametros
    letters.forEach((letter, i) => {
        let geometry = new THREE.TextGeometry(letter, {
            font: font,
            size: 1,
            height: 0.5,
            curveSegments: 12,
            bevelEnabled: true,
            bevelThickness: 0.03,
            bevelSize: 0.03,
            bevelSegments: 5
        });

        let material = materials[i % materials.length];
        let mesh = new THREE.Mesh(geometry, material);

        mesh.position.set(i * 1.5 - (letters.length * 1.5) / 2, 0, 0);
        group.add(mesh);

        // pra cada letra girar com velocidades diferentes temos de meter o math.random a multiplicar pela vel de cada letra 
        mesh.rotationSpeed = Math.random() * 0.01 + 0.01;
    });

    scene.add(group);

    // loop
    function animate() {
        requestAnimationFrame(animate);

        group.children.forEach((child) => {
            child.rotation.y += child.rotationSpeed;
        });

        renderer.render(scene, camera);
    }

    animate();
});

// posição boa pra camara
camera.position.z = 8;


window.addEventListener('resize', () => {
    renderer.setSize(window.innerWidth, window.innerHeight);
    
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
});
