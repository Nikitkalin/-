import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TextQuest {

    static class Scenario {
        private String scenarioName;
        private List<String> text;
        private Map<String, String> choices;

        public Scenario(String scenarioName) {
            this.scenarioName = scenarioName;
            this.text = new ArrayList<>();
            this.choices = new HashMap<>();
        }

        private void loadScenario(String branch) {
            try {
                Path scenarioFilePath = Paths.get("scenarios", scenarioName, branch.isEmpty() ? "main.txt" : branch + ".txt");
                if (!Files.exists(scenarioFilePath)) {
                    System.err.println("Ошибка: файл сценария не найден: " + scenarioFilePath.toAbsolutePath());
                    return;
                }

                List<String> content = Files.readAllLines(scenarioFilePath);
                text.clear();
                choices.clear();
                boolean isChoiceSection = false;

                for (String line : content) {
                    line = line.trim();
                    if (line.matches("\\d+\\. .+|end\\..+")) {
                        isChoiceSection = true;
                        String[] choiceParts = line.split(" ", 2);
                        if (choiceParts.length == 2) {
                            String choiceNumber = choiceParts[0].replaceAll("[^0-9a-zA-Z]", "");
                            choices.put(choiceNumber, choiceParts[1]);
                        }
                    } else if (isChoiceSection) {
                        break;
                    } else {
                        text.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка при загрузке сценария: " + e.getMessage());
            }
        }

        public void display() {
            System.out.println(String.join("\n", text));
            for (Map.Entry<String, String> entry : choices.entrySet()) {
                System.out.println(entry.getKey() + ". " + entry.getValue());
            }
        }

        public String getScenarioName() {
            return scenarioName;
        }

        public Map<String, String> getChoices() {
            return choices;
        }
    }

    private static List<Scenario> loadAllScenarios() {
        List<Scenario> scenarios = new ArrayList<>();
        try {
            Files.walk(Paths.get("scenarios"))
                    .filter(Files::isDirectory)
                    .filter(path -> !path.getFileName().toString().equals("scenarios"))
                    .forEach(path -> {
                        String scenarioName = path.getFileName().toString();
                        scenarios.add(new Scenario(scenarioName));
                    });
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке сценариев: " + e.getMessage());
        }
        return scenarios;
    }

    private static void runScenario(Scenario scenario) {
        Scanner scanner = new Scanner(System.in);

        String branch = "";

        while (true) {
            scenario.loadScenario(branch);
            scenario.display();

            System.out.print("Ваш выбор: ");
            String choice = scanner.nextLine().trim();

            if ("end".equalsIgnoreCase(choice)) {
                break;
            }

            if (scenario.getChoices().containsKey(choice)) {
                String nextBranch = branch.isEmpty() ? choice : branch + "_" + choice;
                branch = nextBranch;
            } else {
                System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    public static void main(String[] args) {
        List<Scenario> scenarios = loadAllScenarios();

        if (scenarios.isEmpty()) {
            System.out.println("Нет доступных сценариев.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Выберите сценарий:");
            for (int i = 0; i < scenarios.size(); i++) {
                System.out.println((i + 1) + ". " + scenarios.get(i).getScenarioName());
            }
            System.out.println("0. Выйти");

            System.out.print("Ваш выбор: ");
            int scenarioChoice;
            try {
                scenarioChoice = Integer.parseInt(scanner.nextLine()) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Неверный выбор. Пожалуйста, введите число.");
                continue;
            }

            if (scenarioChoice == -1) {
                System.out.println("Выход...");
                break;
            } else if (scenarioChoice < 0 || scenarioChoice >= scenarios.size()) {
                System.out.println("Неверный выбор сценария.");
            } else {
                runScenario(scenarios.get(scenarioChoice));
            }
        }
    }
}