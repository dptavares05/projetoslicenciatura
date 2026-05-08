import java.io.*;
import java.util.*;

public class Guru{
    private static List<List<String>> allWords = new ArrayList<>();
    private static List<String> remainingWords = new ArrayList<>();
    private static List<String> guessedWords = new ArrayList<>();
    private static List<List<String>> allLetters = new ArrayList<>();
    private static List<Integer> wordSizes = new ArrayList<>();
    private static int coins = 0;
    private static int currentLevel = 1;
    private static String filePath = "ficheiro_niveis.txt";///home/diogo/Desktop/p2/1trabalho/VF/out/production/VF/ficheiro_niveis.txt
    private static String saveFilePath = "game_state.txt";
    private static String dictionaryPath = "portuguese-large.txt";
    private static Set<String> validWords = new HashSet<>();
    private static Set<String> extraValidWords = new HashSet<>();


    public static void main(String[] args) {
        loadDictionary(dictionaryPath);

        Scanner userScanner = new Scanner(System.in);
        System.out.println("Bem vindo ao Guru2P2!");
        System.out.println("Opções: \n1. Começar um novo jogo\n2. Carregar um jogo\n3. Criar um novo jogo");
        int choice = userScanner.nextInt();
        userScanner.nextLine();

        switch (choice) {
            case 1:
            coins = 0;
            remainingWords.clear();
            wordSizes.clear();
            allLetters.clear();
            guessedWords.clear();
            currentLevel = 1;
            playGame(filePath, saveFilePath);
            break;
            case 2:
            loadGameState(saveFilePath, filePath);
            playGame(filePath, saveFilePath);
            break;
            case 3:
                createNewGame();
                break;
            default:
                System.out.println("Opção inválida! A sair ...");
        }
    }

    private static void playGame(String filePath, String saveFilePath) {
        try {
            Scanner fileScanner = new Scanner(new File(filePath));//ler ficheiro (ficheiro_níveis)

            while (fileScanner.hasNextLine()) {
                String firstLine = fileScanner.nextLine();

                if (firstLine.isEmpty()) {
                    continue;
                }

                String[] letters = firstLine.split("\\s+");
                allLetters.add(List.of(letters));

                List<String> levelWords = new ArrayList<>();
                while (fileScanner.hasNextLine()) {
                    String word = fileScanner.nextLine();

                    if (word.isEmpty()) {
                        break;
                    }

                    levelWords.add(word);
                    wordSizes.add(word.length());
                }

                allWords.add(levelWords);
            }

            fileScanner.close();

            updateRemainingWords();

            Scanner userScanner = new Scanner(System.in);

            while (true) {
                if (currentLevel - 1 >= allWords.size()) {
                    System.out.println("Acabou! Não há mais niveis.");
                    break;
                }

                List<String> levelLetters = allLetters.get(currentLevel - 1);

                System.out.println("\nNivel " + currentLevel + " começou!");
                System.out.println("Letras deste nivel: " + String.join(" ", levelLetters));
                System.out.println("Tenta advinhar as palavras!");

                while (!remainingWords.isEmpty()) {
                    updateRemainingWords();

                    System.out.print("\nPalavras em falta pelos seus tamanhos: ");
                    for (String word : remainingWords) {
                        System.out.print(word.length() + " ");
                    }
                    System.out.println("\nCoins: " + coins);
                    System.out.print("Escreve uma das palavras acima, ou escreve 'pista' para ter uma pista, ou 'sair' pra salvar e sair: ");
                    String userGuess = userScanner.nextLine().toUpperCase();

                    if (userGuess.equalsIgnoreCase("sair")) {
                        saveGameState(saveFilePath, filePath);
                        System.out.println("Jogo salvo. A sair...");
                        return;
                    } else if (userGuess.equalsIgnoreCase("pista")) {
                        if (coins >= 100) {
                            System.out.print("Escreve o índice da palavra: ");
                            int wordIndex = Integer.parseInt(userScanner.nextLine());
                            System.out.print("Escreve o índice da letra: ");
                            int letterIndex = Integer.parseInt(userScanner.nextLine());
                            String hint = clues(wordIndex, letterIndex, remainingWords);//pista

                            System.out.println(hint);
                            coins -= 100;

                        } else {
                            System.out.println("Sem moedas suficientes para uma pista! precisas de pelo menos 100 moedas.");
                        }
                    } else if (remainingWords.contains(userGuess)) {
                        remainingWords.remove(userGuess);
                        guessedWords.add(userGuess);
                        coins += 10;
                        System.out.println("Acertaste! Ganhaste 10 moedas.");
                    } else if (validWords.contains(userGuess) && !extraValidWords.contains(userGuess) && !guessedWords.contains(userGuess)) {
                        extraValidWords.add(userGuess);
                        coins += 10;
                        System.out.println("Palavra valida, porém não faz parte deste nivel! Ganhaste 10 moedas.");
                    } else {
                        System.out.println("Palavra errada ou escreveste-a anteriormente. Tente novamente.!");
                    }
                }

                System.out.println("\nParabéns! Completaste o nível  " + currentLevel + ".\n");
                currentLevel++;
                guessedWords.clear();
                updateRemainingWords();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro não encontrado!");
        }
    }

    private static void updateRemainingWords() {
        remainingWords.clear();

        if (currentLevel - 1 < allWords.size()) {
            List<String> levelWords = allWords.get(currentLevel - 1);

            for (String word : levelWords) {
                if (!guessedWords.contains(word)) {
                    remainingWords.add(word);
                }
            }
        } else {
            System.out.println("O nível " + currentLevel + " não existe.");
        }

        for (String word : guessedWords) {
            remainingWords.remove(word);
        }
    }

    private static String clues(int wordIndex, int letterIndex, List<String> wordsToBeGuessed) {
        if (wordIndex < 0 || wordIndex >= wordsToBeGuessed.size()) {
            return "Índice da palavra errado.";
        }

        String word = wordsToBeGuessed.get(wordIndex);

        if (letterIndex < 0 || letterIndex >= word.length()) {
            return "Índice da palavra errado.";
        }

        char letter = word.charAt(letterIndex);
        return "A letra da palavra que procuras é: " + letter;
    }

    private static void saveGameState(String saveFilePath, String originalGameFilePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFilePath));
             BufferedReader reader = new BufferedReader(new FileReader(originalGameFilePath))) {

            writer.write("Moedas: " + coins);
            writer.newLine();
            writer.write("Nível: " + currentLevel);
            writer.newLine();

            String line;
            int levelIndex = 0;
            boolean levelStarted = false;

            while ((line = reader.readLine()) != null) {
                line = line;

                if (line.isEmpty()) {
                    if (levelStarted) {
                        writer.newLine();
                        levelStarted = false;
                    }
                    continue;
                }

                if (levelIndex >= currentLevel - 1) {
                    if (!levelStarted) {
                        levelStarted = true;
                    }

                    writer.write(line);
                    writer.newLine();

                    while ((line = reader.readLine()) != null) {
                        line = line;
                        if (line.isEmpty()) {
                            break;
                        }

                        if (!guessedWords.contains(line)) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                } else {
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    }
                }

                levelIndex++;
            }

            System.out.println("Jogo Salvo com sucesso!");

        } catch (IOException e) {
            System.out.println("Erro ao salvar o jogo");
            e.printStackTrace();
        }
    }

    private static void createNewGame() {//parte 2/4.2 pra gerar novos jogos
        Scanner scanner = new Scanner(System.in);
        System.out.println("Escreve o numero de níveis que queres gerar: ");
        int numberOfLevels = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Escreve o nome do ficheiro onde queres guardar o jogo: ");
        String fileName = scanner.nextLine();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < numberOfLevels; i++) {
                System.out.println("A gera o nivel " + (i + 1) + "...");
                List<String> letters = generateRandomLetters();
                List<String> words = findValidWords(letters);

                if (words.isEmpty()) {
                    System.out.println("Não foram achadas palavras validas para o nivel " + (i + 1) + ". A Saltar...");
                    i--; // Retry this level
                    continue;
                }

                // Write level to the file
                writer.write(String.join(" ", letters));
                writer.newLine();

                // Select some words for the level
                Collections.shuffle(words);
                for (int j = 0; j < Math.min(words.size(), 5); j++) {
                    writer.write(words.get(j));
                    writer.newLine();
                }

                writer.newLine(); // Separate levels with a blank line
            }

            System.out.println("Novo jogo salvo e guardado em " + fileName);

        } catch (IOException e) {
            System.out.println("Erro a escrever o ficheiro: " + e.getMessage());
        }
    }

    private static List<String> generateRandomLetters() {
        Random random = new Random();
        String vowels = "AEIOU";
        String consonants = "BCDFGHJKLMNPQRSTVWXYZ";

        List<String> letters = new ArrayList<>();

        // Ensure at least one vowel
        letters.add(String.valueOf(vowels.charAt(random.nextInt(vowels.length()))));

        // Fill the rest with random letters (mix of vowels and consonants)
        for (int i = 0; i < 6; i++) { // Total 7 letters
            if (random.nextBoolean()) {
                letters.add(String.valueOf(vowels.charAt(random.nextInt(vowels.length()))));
            } else {
                letters.add(String.valueOf(consonants.charAt(random.nextInt(consonants.length()))));
            }
        }

        Collections.shuffle(letters);
        return letters;
    }

    private static List<String> findValidWords(List<String> letters) {
        List<String> foundWords = new ArrayList<>();
        String regex = String.join("", letters);

        for (String word : validWords) {
            if (isWordValid(word, letters)) {
                foundWords.add(word);
            }
        }

        return foundWords;
    }

    private static boolean isWordValid(String word, List<String> letters) {
        Map<Character, Integer> letterCount = new HashMap<>();

        for (String letter : letters) {
            letterCount.put(letter.charAt(0), letterCount.getOrDefault(letter.charAt(0), 0) + 1);
        }

        for (char c : word.toCharArray()) {
            if (!letterCount.containsKey(c) || letterCount.get(c) == 0) {
                return false;
            }
            letterCount.put(c, letterCount.get(c) - 1);
        }

        return true;
    }

    private static void loadDictionary(String dictionaryPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(dictionaryPath))) {
            String word;

            while ((word = reader.readLine()) != null) {
                validWords.add(word.toUpperCase());
            }
        } catch (IOException e) {
            System.out.println("Erro a carregar o dicionário");
            e.printStackTrace();
        }
    }

    private static void loadGameState(String saveFilePath, String originalGameFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFilePath))) {
            coins = Integer.parseInt(reader.readLine().split(": ")[1]);
            currentLevel = Integer.parseInt(reader.readLine().split(": ")[1]);
            System.out.println("Jogo carregado com sucesso!");
        } catch (IOException e) {
            System.out.println("Erro a carregar o jogo. A começar um novo jogo.");
            e.printStackTrace();
            coins = 10;
            currentLevel = 1;
        }
    }
}
