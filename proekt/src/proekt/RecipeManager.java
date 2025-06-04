package proekt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class RecipeManager {
    private MyArrayList<Recipe> recipes = new MyArrayList<>();

    public void addRecipe(Recipe recipe) throws DuplicateRecipeException {
        if (containsRecipe(recipe.getName())) {
            throw new DuplicateRecipeException("Рецепта с това име вече съществува: " + recipe.getName());
        }
        recipes.add(recipe);
    }

    public boolean containsRecipe(String name) {
        for (int i = 0; i < recipes.size(); i++) {
            Recipe r = recipes.get(i);
            if (r.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void saveRecipeToFile(Recipe recipe, Path filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("Recipe: " + recipe.getName() + "\n");
            writer.write("Ingredients: " + String.join(", ", recipe.getIngredients()) + "\n");
            writer.write("Instructions: " + recipe.getInstructions().replace("\n", "\\n") + "\n");
        }
    }

    public void loadRecipesFromDirectory(String directoryPath) throws IOException, InvalidRecipeFormatException {
        recipes.clear();
        
        Path dir = Paths.get(directoryPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return;
        }
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.txt")) {
            for (Path filePath : stream) {
                loadRecipeFromFile(filePath);
            }
        }
    }

    void loadRecipeFromFile(Path filePath) throws IOException, InvalidRecipeFormatException {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String name = null;
            List<String> ingredients = new ArrayList<>();
            StringBuilder instructions = new StringBuilder();
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Recipe: ")) {
                    name = line.substring(8).trim();
                } else if (line.startsWith("Ingredients: ")) {
                    String ingredientsStr = line.substring(13).trim();
                    if (!ingredientsStr.isEmpty()) {
                        String[] ingredientsArray = ingredientsStr.split("\\s*,\\s*");
                        for (String ingredient : ingredientsArray) {
                            ingredients.add(ingredient);
                        }
                    }
                } else if (line.startsWith("Instructions: ")) {
                    instructions.append(line.substring(14).trim());
                }
            }
            
            if (name != null && !ingredients.isEmpty()) {
                String normalizedInstructions = instructions.toString().replace("\\n", "\n");
                Recipe recipe = new Recipe(name, ingredients, normalizedInstructions);
                
                if (!containsRecipe(recipe.getName())) {
                    recipes.add(recipe);
                }
            } else {
                throw new InvalidRecipeFormatException("Невалиден формат на рецепта във файл: " + filePath.getFileName());
            }
        }
    }

    public String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9а-яА-Я\\s]", "")
                   .replaceAll("\\s+", "_");
    }

    public void sortRecipes() {
        quickSort(0, recipes.size() - 1);
    }

    private void quickSort(int low, int high) {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) {
        String pivot = recipes.get(high).getName();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (recipes.get(j).getName().compareToIgnoreCase(pivot) < 0) {
                i++;
                swap(i, j);
            }
        }
        
        swap(i + 1, high);
        return i + 1;
    }

    private void swap(int i, int j) {
        Recipe temp = recipes.get(i);
        recipes.set(i, recipes.get(j));
        recipes.set(j, temp);
    }

    public MyArrayList<Recipe> searchRecipes(String query) {
        MyArrayList<Recipe> results = new MyArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            if (recipe.getName().toLowerCase().contains(lowerQuery)) {
                results.add(recipe);
                continue;
            }
            
            for (String ingredient : recipe.getIngredients()) {
                if (ingredient.toLowerCase().contains(lowerQuery)) {
                    results.add(recipe);
                    break;
                }
            }
        }
        
        return results;
    }

    public void updateRecipe(int index, Recipe newRecipe) throws DuplicateRecipeException {
        if (index < 0 || index >= recipes.size()) {
            throw new IndexOutOfBoundsException("Невалиден индекс");
        }
        
        if (!recipes.get(index).getName().equals(newRecipe.getName())) {
            if (containsRecipe(newRecipe.getName())) {
                throw new DuplicateRecipeException("Рецепта с това име вече съществува: " + newRecipe.getName());
            }
        }
        
        recipes.set(index, newRecipe);
    }

    public MyArrayList<Recipe> getRecipes() { 
        return recipes; 
    }
    
    public Recipe getRecipe(int index) {
        if (index >= 0 && index < recipes.size()) {
            return recipes.get(index);
        }
        return null;
    }
    
    public void deleteRecipe(int index) {
        if (index >= 0 && index < recipes.size()) {
            recipes.remove(index);
        }
    }
}