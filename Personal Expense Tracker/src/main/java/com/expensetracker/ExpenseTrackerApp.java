package com.expensetracker;

import javafx.collections.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpenseTrackerApp {
 @FXML private VBox homePane, statsPane, historyPane;
 @FXML private Pane backgroundLogo;
 @FXML private TextField categoryField, amountField, descriptionField;
 @FXML private DatePicker datePicker;
 @FXML private ImageView navLogo;
 @FXML private ComboBox<String> categoryFilter, currencySelector;
 @FXML private PieChart reportChart;
 @FXML private Label reportTitle,totalIncomeLabel, totalExpenseLabel, balanceLabel;
 @FXML private TableView<Expense> expenseTable;
 @FXML private TableColumn<Expense, String> catCol;
 @FXML private TableColumn<Expense, String> amtCol;
 @FXML private TableColumn<Expense, LocalDateTime> dateCol;
 @FXML private TableColumn<Expense, Void> deleteCol;
 @FXML private TableColumn<Expense, Void> editCol;
 @FXML private ComboBox<String> typeSelector;
 @FXML private ComboBox<String> categorySelector;
 @FXML private TableColumn<Expense, String> descCol;
 @FXML private TextField converterInput;
 @FXML private Label converterResult;
 @FXML private ComboBox<String> converterFrom;
 @FXML private ComboBox<String> converterTarget;

 private Expense selectedExpense = null;
 private final DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
 private String currentSymbol = "$";
 private final OCRProcessor ocr = new OCRProcessor();
 private final Map<String, Double> rates= Map.of("INR", 83.0, "EUR", 0.92, "GBP", 0.79, "USD", 1.0);


 @FXML
 public void initialize() {
  ObservableList<String> categories = FXCollections.observableArrayList("Food", "Transport", "Groceries", "Misc", "Salary", "Debt", "Apparels");
  categoryFilter.setItems(categories);
  categorySelector.setItems(categories);

  currencySelector.setItems(FXCollections.observableArrayList("$ (USD)", "₹ (INR)", "€ (EUR)"));
  currencySelector.setValue("$ (USD)");
  typeSelector.setItems(FXCollections.observableArrayList("Income", "Expense"));
  typeSelector.setValue("Expense");
  ObservableList<String> options = FXCollections.observableArrayList("USD", "INR", "EUR", "GBP");
  converterFrom.setItems(options);
  converterTarget.setItems(options);
  converterTarget.setValue("INR");

  //Currency Conversion form
  converterInput.textProperty().addListener((obs, oldVal, newVal) -> calculateConversion());
  converterFrom.valueProperty().addListener((obs, old, newVal) -> calculateConversion());
  converterTarget.valueProperty().addListener((obs, oldVal, newVal) -> calculateConversion());
  catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
  amtCol.setCellValueFactory(cellData -> {
   Expense e = cellData.getValue();
   // This combines the stored symbol (like $) with the numerical amount
   String formattedValue = e.getCurrency() + " " + String.format("%.2f", e.getAmount());
   return new javafx.beans.property.SimpleStringProperty(formattedValue);
  });
  dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
  descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

  // Formatting Date column to show time
  dateCol.setCellFactory(col -> new TableCell<>() {
   @Override protected void updateItem(LocalDateTime item, boolean empty) {
    super.updateItem(item, empty);
    setText(empty || item == null ? null : displayFmt.format(item));
   }
  });
  setupEditColumn();
  setupDeleteColumn();
  datePicker.setValue(LocalDate.now());
  hideAllPanes();

 }
 private void calculateConversion() {
  String text = converterInput.getText();
  if (text == null || text.isEmpty()) {
   converterResult.setText("0.00");
   return;
  }

  try {
   double amount = Double.parseDouble(text);
   double fromRate = rates.get(converterFrom.getValue());
   double toRate = rates.get(converterTarget.getValue());


   double result = (amount / fromRate) * toRate;

   converterResult.setText(String.format("%.2f", result));
  } catch (NumberFormatException e) {
   converterResult.setText("0.00");
  }
 }
 private void hideAllPanes() {
  homePane.setVisible(false);
  statsPane.setVisible(false);
  historyPane.setVisible(false);
 }
 @FXML private void showHome() { switchPage(homePane); }
 @FXML private void showStats() { switchPage(statsPane); dailyReport(); }
 @FXML private void showHistory() { switchPage(historyPane); refreshData(); }
 private void switchPage(VBox target) {
  hideAllPanes();
  target.setVisible(true);
 }
 @FXML
 private void handleExport() {
  Database.exportToCSV();
 }
 private void refreshData() {
  List<Expense> data = Database.getAll();
  expenseTable.setItems(FXCollections.observableArrayList(data));
  updateSummary(data);
 }
 private void updateSummary(List<Expense> data) {
  double income = data.stream().filter(e -> e.getDescription().contains("[INCOME]")).mapToDouble(Expense::getAmount).sum();
  double expenses = data.stream().filter(e -> !e.getDescription().contains("[INCOME]")).mapToDouble(Expense::getAmount).sum();

  totalIncomeLabel.setText(String.format("%.2f", income));
  totalExpenseLabel.setText(String.format("%.2f", expenses));
  balanceLabel.setText(String.format("%.2f", income - expenses));
 }

 private void setupEditColumn() {
  editCol.setCellFactory(param -> new TableCell<>() {
   private final Button btn = new Button("Edit");
   {
    btn.setStyle("-fx-background-color: #58a6ff; -fx-text-fill: white; -fx-cursor: hand;");
    btn.setOnAction(event -> {
     selectedExpense = getTableView().getItems().get(getIndex());
     // Load data back into input fields for editing

     amountField.setText(String.valueOf(selectedExpense.getAmount()));
     descriptionField.setText(selectedExpense.getDescription());
     datePicker.setValue(selectedExpense.getDate().toLocalDate());
     showHome(); // Switch to the add/edit pane
    });
   }
   @Override protected void updateItem(Void item, boolean empty) {
    super.updateItem(item, empty);
    setGraphic(empty ? null : btn);
   }
  });
 }

 private void setupDeleteColumn() {
  deleteCol.setCellFactory(param -> new TableCell<>() {
   private final Button btn = new Button("Delete");
   {
    btn.setStyle("-fx-background-color: #f85149; -fx-text-fill: white; -fx-cursor: hand;");
    btn.setOnAction(event -> {
     Expense e = getTableView().getItems().get(getIndex());
     Database.delete(e.getId());
     refreshTable();
    });
   }
   @Override protected void updateItem(Void item, boolean empty) {
    super.updateItem(item, empty);
    setGraphic(empty ? null : btn);
   }
  });
 }
 @FXML
 private void goHome() {
  hideAllPanes(); // Resets the UI to show only the logo and top bar
 }
 @FXML
 private void addExpense() {
  try {
   // Read from ComboBox instead of TextField
   String category = categorySelector.getValue();
   String amountText = amountField.getText();
   String desc = descriptionField.getText();
   LocalDateTime timestamp = datePicker.getValue().atTime(LocalTime.now());

   // Validation
   if (category == null || amountText.isEmpty()) {
    System.out.println("Missing Category or Amount");
    return;
   }

   if ("Income".equals(typeSelector.getValue())) {
    desc += " [INCOME]";
   }

   // Database Insertion with Symbol
   Database.insert(new Expense(-1, category,
           Double.parseDouble(amountText), desc, timestamp, currentSymbol));

   clearFields();
   refreshData(); // Refresh table and summary
   showHistory();
  } catch (Exception e) {
   System.err.println("Save Failed: " + e.getMessage());
   e.printStackTrace();
  }
 }

 private void clearFields() {

  amountField.clear();
  descriptionField.clear();
  datePicker.setValue(LocalDate.now());
  selectedExpense = null;
 }




 private void refreshTable() { expenseTable.setItems(FXCollections.observableArrayList(Database.getAll())); }



 @FXML private void monthlyReport() { updateChart("Monthly Report", LocalDate.now().minusMonths(1)); }
 @FXML private void weeklyReport() { updateChart("Weekly Report", LocalDate.now().minusWeeks(1)); }
 @FXML private void dailyReport() { updateChart("Daily Report", LocalDate.now()); }

 private void updateChart(String title, LocalDate start) {
  reportTitle.setText(title);
  List<Expense> list = Database.getFiltered(start, LocalDate.now());
  Map<String, Double> data = Database.getFiltered(start, LocalDate.now()).stream()
          .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));
  reportChart.getData().clear();
  data.forEach((k, v) -> reportChart.getData().add(new PieChart.Data(k, v)));
 }

 @FXML
 private void handleCategoryFilter() {
  String selected = categoryFilter.getValue();
  if (selected == null || selected.isEmpty()) {
   refreshTable();
   return;
  }

  // Fetch filtered data from the database
  List<Expense> data = Database.getFilteredByCategory(selected);
  double total = data.stream().mapToDouble(Expense::getAmount).sum();

  // Update Chart for the specific category
  reportChart.getData().clear();
  if (total > 0) {
   reportChart.getData().add(new PieChart.Data(selected + " (" + total + ")", total));
  }

  reportTitle.setText("Filter: " + selected);
  expenseTable.setItems(FXCollections.observableArrayList(data)); // Show only filtered rows
 }

 @FXML
 private void handleCurrencyChange() {
  // Extract symbol (e.g., "$" from "$ (USD)")
  currentSymbol = currencySelector.getValue().split(" ")[0];

  // Refresh the table to apply the new symbol to the amount column
  refreshTable();

  // If stats are visible, refresh the chart labels too
  if (historyPane.isVisible()) {
   refreshData();
  }
 }

 @FXML
 private void scanReceipt() {
  FileChooser fileChooser = new FileChooser();
  fileChooser.setTitle("Select Receipt Image");
  fileChooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
  );

  File selectedFile = fileChooser.showOpenDialog(homePane.getScene().getWindow());

  if (selectedFile != null) {
   Tesseract tesseract = new Tesseract();
   // IMPORTANT: Set the correct path to your 'tessdata' folder
   tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
   tesseract.setLanguage("eng");

   try {
    // 1. Perform OCR on the image
    String result = tesseract.doOCR(selectedFile);
    System.out.println("OCR Raw Output: " + result);

    // 2. Extract the TOTAL using Regex
    // This pattern looks for 'TOTAL' followed by a number with 2 decimal places
    // It allows for characters like €, $, or OCR artifacts in between
    Pattern totalPattern = Pattern.compile("TOTAL.*?(\\d+[.,]\\d{2})", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher matcher = totalPattern.matcher(result);

    if (matcher.find()) {
     // Group 1 is the actual number (e.g., 7.35)
     String foundAmount = matcher.group(1).replace(",", ".");
     amountField.setText(foundAmount);
     descriptionField.setText("Scanned Total: " + foundAmount);
    } else {
     // Fallback: If 'TOTAL' keyword is missed, look for the LAST price in the text
     Pattern pricePattern = Pattern.compile("(\\d+[.,]\\d{2})");
     Matcher priceMatcher = pricePattern.matcher(result);
     String lastPrice = "";
     while (priceMatcher.find()) {
      lastPrice = priceMatcher.group(1).replace(",", ".");
     }

     if (!lastPrice.isEmpty()) {
      amountField.setText(lastPrice);
      descriptionField.setText("Extracted Last Price Found");
     } else {
      descriptionField.setText("Scan failed to find amount.");
     }
    }

   } catch (TesseractException e) {
    System.err.println("OCR Error: " + e.getMessage());
    descriptionField.setText("Error reading image.");
   }
  }
 }
 @FXML
 private void handleLogout() {
  try {
   // 1. Load the Authentication (Login) UI
   FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
   Parent root = loader.load();

   // 2. Get the current Stage
   Stage stage = (Stage) expenseTable.getScene().getWindow(); // Use any FX element ID here

   // 3. Switch Scene
   Scene scene = new Scene(root);
   scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

   stage.setTitle("CoinCare - Login");
   stage.setScene(scene);
   stage.show();

   System.out.println("User logged out successfully.");
  } catch (IOException e) {
   e.printStackTrace();
  }
 }
}