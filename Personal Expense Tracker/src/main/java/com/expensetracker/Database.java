package com.expensetracker;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class Database {
 private static final String DB_URL;
 private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

 static {
  String path = System.getProperty("user.home") + File.separator + "AuraExpenseAI";
  new File(path).mkdirs();
  DB_URL = "jdbc:sqlite:" + path + File.separator + "aura_tracker.db";
  createTable();
 }

 public static Connection connect() throws SQLException {
  return DriverManager.getConnection(DB_URL);
 }

 public static void initialize() {
  String userTable = "CREATE TABLE IF NOT EXISTS users (" +
          "username TEXT PRIMARY KEY, " +
          "email text, " +
          "password TEXT, " +
          "security_answer TEXT)";
  try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
   stmt.execute(userTable);
   System.out.println("Database initialized successfully.");
  } catch (SQLException e) {
   e.printStackTrace();
  }
 }

 private static void createTable() {
  String sql = "CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT, amount REAL, description TEXT, date TEXT, currency TEXT)";
  try (Connection c = connect(); Statement s = c.createStatement()) {
   s.execute(sql);
  } catch (SQLException e) { e.printStackTrace(); }
 }

 public static void insert(Expense e) {
  String sql = "INSERT INTO expenses(category, amount, description, date, currency) VALUES(?,?,?,?,?)";
  try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
   p.setString(1, e.getCategory());
   p.setDouble(2, e.getAmount());
   p.setString(3, e.getDescription());
   p.setString(4, e.getDate().format(FMT));
   p.setString(5, e.getCurrency());
   p.executeUpdate();
  } catch (SQLException ex) { ex.printStackTrace(); }
 }

 public static void update(Expense e) {
  String sql = "UPDATE expenses SET category = ?, amount = ?, description = ?, date = ?, currency = ? WHERE id = ?";
  try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
   p.setString(1, e.getCategory());
   p.setDouble(2, e.getAmount());
   p.setString(3, e.getDescription());
   p.setString(4, e.getDate().format(FMT));
   p.setString(5, e.getCurrency());
   p.setInt(6, e.getId());
   p.executeUpdate();
  } catch (SQLException ex) { ex.printStackTrace(); }
 }

 public static void delete(int id) {
  String sql = "DELETE FROM expenses WHERE id = ?";
  try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
   p.setInt(1, id);
   p.executeUpdate();
  } catch (SQLException e) { e.printStackTrace(); }
 }

 public static List<Expense> getAll() {
  List<Expense> list = new ArrayList<>();
  try (Connection c = connect(); Statement s = c.createStatement();
       ResultSet r = s.executeQuery("SELECT * FROM expenses ORDER BY date DESC")) {
   while (r.next()) {
    list.add(new Expense(r.getInt("id"), r.getString("category"), r.getDouble("amount"),
            r.getString("description"), LocalDateTime.parse(r.getString("date"), FMT), r.getString("currency")));
   }
  } catch (SQLException e) { e.printStackTrace(); }
  return list;
 }

 public static List<Expense> getFiltered(LocalDate start, LocalDate end) {
  List<Expense> list = new ArrayList<>();
  String sql = "SELECT * FROM expenses WHERE date >= ? AND date <= ? ORDER BY date DESC";
  try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
   p.setString(1, start.atStartOfDay().format(FMT));
   p.setString(2, end.atTime(23, 59, 59).format(FMT));
   ResultSet r = p.executeQuery();
   while (r.next()) {
    list.add(new Expense(r.getInt("id"), r.getString("category"), r.getDouble("amount"),
            r.getString("description"), LocalDateTime.parse(r.getString("date"), FMT), r.getString("currency")));
   }
  } catch (SQLException e) { e.printStackTrace(); }
  return list;
 }
 public static List<Expense> getFilteredByCategory(String category) {
  List<Expense> list = new ArrayList<>();
  // SQL query to filter by category and sort by newest first
  String sql = "SELECT * FROM expenses WHERE category = ? ORDER BY date DESC";
  try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
   p.setString(1, category);
   ResultSet r = p.executeQuery();
   while (r.next()) {
    list.add(new Expense(
            r.getInt("id"),
            r.getString("category"),
            r.getDouble("amount"),
            r.getString("description"),
            LocalDateTime.parse(r.getString("date"), FMT),
            r.getString("currency")
    ));
   }
  } catch (SQLException e) {
   e.printStackTrace();
  }
  return list;
 }

 public static void exportToCSV() {
  List<Expense> data = Database.getAll(); // Pulls all records from Database
  File file = new File("expense_report.csv");

  try (PrintWriter writer = new PrintWriter(file)) {

   writer.println("ID,Category,Amount,Description,Date,Currency");


   for (Expense e : data) {
    writer.printf("%d,%s,%.2f,%s,%s,%s%n",
            e.getId(), e.getCategory(), e.getAmount(),
            e.getDescription(), e.getDate().toString(), e.getCurrency());
   }
   System.out.println("Data exported to:" + file.getAbsolutePath());
  } catch (FileNotFoundException e) {
   e.printStackTrace();
  }
 }
 public static void exportUsersToCSV() {
  String sql = "SELECT username,email, password,security_answer FROM users";
  File file = new File("C:/Users/NIKSHITHA/Downloads/ExpenseTracker_WindowsBundle_Helper/users_list.csv");

  try (Connection conn = connect();
       Statement stmt = conn.createStatement();
       ResultSet rs = stmt.executeQuery(sql);
       PrintWriter writer = new PrintWriter(file)) {


   writer.println("Username,Email,Password,SecurityAnswer");


   while (rs.next()) {
    writer.printf("%s,%s,%s,%s%n",
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("security_answer"));
   }

   System.out.println("User list exported to: " + file.getAbsolutePath());
  }catch (Exception e) {
   System.err.println("EXPORT FAILED!");
   e.printStackTrace(); // This will tell you why it's missing
  }
 }
}