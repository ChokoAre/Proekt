package proekt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDialog extends JDialog {
    private JTextField nameField;
    private JTextArea ingredientsArea;
    private JTextArea instructionsArea;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean cancelled = true;
    private Recipe recipe;

    public RecipeDialog(JFrame parent, Recipe existingRecipe) {
        super(parent, "Рецепта", true);
        this.recipe = existingRecipe;
        initializeUI();
        if (existingRecipe != null) {
            populateFields();
        }
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(400, 500));

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Име:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Съставки (разделени със запетая):"));
        ingredientsArea = new JTextArea(5, 20);
        ingredientsArea.setLineWrap(true);
        formPanel.add(new JScrollPane(ingredientsArea));

        formPanel.add(new JLabel("Инструкции:"));
        instructionsArea = new JTextArea(10, 20);
        instructionsArea.setLineWrap(true);
        formPanel.add(new JScrollPane(instructionsArea));

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Запази");
        cancelButton = new JButton("Отказ");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            if (validateFields()) {
                cancelled = false;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            cancelled = true;
            dispose();
        });
    }

    private void populateFields() {
        nameField.setText(recipe.getName());
        ingredientsArea.setText(String.join(", ", recipe.getIngredients()));
        instructionsArea.setText(recipe.getInstructions());
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Моля въведете име на рецепта!");
            return false;
        }
        if (ingredientsArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Моля въведете съставки!");
            return false;
        }
        return true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Recipe getRecipe() {
        String name = nameField.getText().trim();
        String[] ingredientsArray = ingredientsArea.getText().split("\\s*,\\s*");
        List<String> ingredients = new ArrayList<>();
        for (String ingredient : ingredientsArray) {
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        String instructions = instructionsArea.getText().trim();
        return new Recipe(name, ingredients, instructions);
    }
}