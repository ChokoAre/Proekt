package proekt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame extends JFrame {
    private RecipeManager recipeManager = new RecipeManager();
    private JList<String> recipeList;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JTextArea detailsArea;
    private JTextField searchField;
    private static final String RECIPES_DIR = "recipes";
    private MyArrayList<Recipe> currentDisplayedRecipes = new MyArrayList<>();

    public MainFrame() {
        setTitle("Recipe managment");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel(" Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        add(searchPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        recipeList = new JList<>(listModel);
        JScrollPane listScroll = new JScrollPane(recipeList);
        listScroll.setPreferredSize(new Dimension(200, 0));
        mainPanel.add(listScroll, BorderLayout.WEST);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        mainPanel.add(detailsScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
        add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        addButton.addActionListener(e -> showRecipeDialog(null));
        editButton.addActionListener(e -> editRecipe());
        deleteButton.addActionListener(e -> deleteRecipe());
        saveButton.addActionListener(e -> saveSelectedRecipe());
        loadButton.addActionListener(e -> loadRecipes());
        recipeList.addListSelectionListener(e -> showRecipeDetails());
        searchButton.addActionListener(e -> searchRecipes());
        searchField.addActionListener(e -> searchRecipes());

        autoLoadRecipes();
    }

    private void saveAllRecipesToDirectory() {
        Path dirPath = Paths.get(RECIPES_DIR);
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.txt")) {
                for (Path file : stream) {
                    Files.delete(file);
                }
            }
            
            for (int i = 0; i < recipeManager.getRecipes().size(); i++) {
                Recipe recipe = recipeManager.getRecipe(i);
                String fileName = recipeManager.sanitizeFileName(recipe.getName()) + ".txt";
                Path filePath = dirPath.resolve(fileName);
                recipeManager.saveRecipeToFile(recipe, filePath);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error in saving recipes " + ex.getMessage());
        }
    }

    private void autoLoadRecipes() {
        try {
            recipeManager.loadRecipesFromDirectory(RECIPES_DIR);
            currentDisplayedRecipes = recipeManager.getRecipes();
            refreshRecipeList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error in loading recipes: " + e.getMessage());
        }
    }

    private void saveRecipe(Recipe recipe, Path filePath) {
        try {
            recipeManager.saveRecipeToFile(recipe, filePath);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error when saving recipe: " + ex.getMessage());
        }
    }

    private void refreshRecipeList() {
        listModel.removeAllElements();
        recipeManager.sortRecipes();
        for (int i = 0; i < currentDisplayedRecipes.size(); i++) {
            Recipe r = currentDisplayedRecipes.get(i);
            listModel.addElement(r.getName());
        }
        
        if (listModel.getSize() > 0) {
            recipeList.setSelectedIndex(0);
        }
    }

    private void showRecipeDetails() {
        int index = recipeList.getSelectedIndex();
        if (index != -1 && index < currentDisplayedRecipes.size()) {
            Recipe recipe = currentDisplayedRecipes.get(index);
            if (recipe != null) {
                detailsArea.setText(String.format(
                    "Name: %s\nIngredients: %s\nInstructions: %s",
                    recipe.getName(),
                    String.join(", ", recipe.getIngredients()),
                    recipe.getInstructions()
                ));
            }
        }
    }

    private void deleteRecipe() {
        int index = recipeList.getSelectedIndex();
        if (index != -1 && index < currentDisplayedRecipes.size()) {
            Recipe recipe = currentDisplayedRecipes.get(index);
            
            int mainIndex = -1;
            for (int i = 0; i < recipeManager.getRecipes().size(); i++) {
                if (recipeManager.getRecipe(i).getName().equals(recipe.getName())) {
                    mainIndex = i;
                    break;
                }
            }
            
            if (mainIndex != -1) {
                recipeManager.deleteRecipe(mainIndex);
                
                saveAllRecipesToDirectory();
                
                if (searchField.getText().isEmpty()) {
                    currentDisplayedRecipes = recipeManager.getRecipes();
                } else {
                    searchRecipes();
                }
                refreshRecipeList();
            }
        }
    }

    private void saveSelectedRecipe() {
        int index = recipeList.getSelectedIndex();
        if (index != -1 && index < currentDisplayedRecipes.size()) {
            Recipe recipe = currentDisplayedRecipes.get(index);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save recipe");
            fileChooser.setSelectedFile(new File(recipeManager.sanitizeFileName(recipe.getName()) + ".txt"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                Path filePath = fileChooser.getSelectedFile().toPath();
                saveRecipe(recipe, filePath);
                JOptionPane.showMessageDialog(this, "The recipe was saved successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a recipe for saving");
        }
    }

    private void loadRecipes() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                for (java.io.File file : fileChooser.getSelectedFiles()) {
                    Path filePath = file.toPath();
                    recipeManager.loadRecipeFromFile(filePath);
                }
                
                saveAllRecipesToDirectory();
                
                if (searchField.getText().isEmpty()) {
                    currentDisplayedRecipes = recipeManager.getRecipes();
                } else {
                    searchRecipes();
                }
                refreshRecipeList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Loading error: " + ex.getMessage());
            }
        }
    }

    private void showRecipeDialog(Recipe existingRecipe) {
        RecipeDialog dialog = new RecipeDialog(this, existingRecipe);
        dialog.setVisible(true);
        
        if (!dialog.isCancelled()) {
            try {
                Recipe recipe = dialog.getRecipe();
                
                if (existingRecipe == null) {
                    recipeManager.addRecipe(recipe);
                }
                
                saveAllRecipesToDirectory();
                
                if (searchField.getText().isEmpty()) {
                    currentDisplayedRecipes = recipeManager.getRecipes();
                } else {
                    searchRecipes();
                }
                refreshRecipeList();
            } catch (DuplicateRecipeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void editRecipe() {
        int index = recipeList.getSelectedIndex();
        if (index != -1 && index < currentDisplayedRecipes.size()) {
            Recipe existingRecipe = currentDisplayedRecipes.get(index);
            RecipeDialog dialog = new RecipeDialog(this, existingRecipe);
            dialog.setVisible(true);
            
            if (!dialog.isCancelled()) {
                try {
                    Recipe updatedRecipe = dialog.getRecipe();
                    
                    int mainIndex = -1;
                    for (int i = 0; i < recipeManager.getRecipes().size(); i++) {
                        if (recipeManager.getRecipe(i).getName().equals(existingRecipe.getName())) {
                            mainIndex = i;
                            break;
                        }
                    }
                    
                    if (mainIndex != -1) {
                        recipeManager.updateRecipe(mainIndex, updatedRecipe);
                        
                        saveAllRecipesToDirectory();
                        
                        if (searchField.getText().isEmpty()) {
                            currentDisplayedRecipes = recipeManager.getRecipes();
                        } else {
                            searchRecipes();
                        }
                        refreshRecipeList();
                    }
                } catch (DuplicateRecipeException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                } catch (IndexOutOfBoundsException ex) {
                    JOptionPane.showMessageDialog(this, "Error: Invalid index");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a recipe for editing");
        }
    }

    private void searchRecipes() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            currentDisplayedRecipes = recipeManager.getRecipes();
            refreshRecipeList();
            return;
        }
        
        currentDisplayedRecipes = recipeManager.searchRecipes(query);
        refreshRecipeList();
        
        if (currentDisplayedRecipes.size() > 0) {
            recipeList.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "No found recipes for: " + query);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}