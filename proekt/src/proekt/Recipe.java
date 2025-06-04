package proekt;

public class Recipe {
    private String name;
    private java.util.List<String> ingredients;
    private String instructions;

    public Recipe(String name, java.util.List<String> ingredients, String instructions) {
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getName() {
        return name;
    }

    public java.util.List<String> getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }
}