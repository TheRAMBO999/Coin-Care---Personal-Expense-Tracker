package com.expensetracker;

import java.time.LocalDateTime;

public class Expense {
 private int id;
 private String category, description, currency;
 private double amount;
 private LocalDateTime date;

 public Expense(int id, String category, double amount, String description, LocalDateTime date, String currency) {
  this.id = id;
  this.category = category;
  this.amount = amount;
  this.description = description;
  this.date = date;
  this.currency = currency;
 }

 public int getId() { return id; }
 public String getCategory() { return category; }
 public double getAmount() { return amount; }
 public String getDescription() { return description; }
 public LocalDateTime getDate() { return date; }
 public String getCurrency() { return (currency == null || currency.isEmpty()) ? "$" : currency; }
}