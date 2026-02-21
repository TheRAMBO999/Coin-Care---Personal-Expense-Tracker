package com.expensetracker;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRProcessor {

 private final ITesseract tesseract = new Tesseract();

 public OCRProcessor() {
  tesseract.setLanguage("eng");
  tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
 }

 public String readText(File image) throws TesseractException {
  return tesseract.doOCR(image);
 }

 public String extractAmount(String text) {
  Matcher m = Pattern.compile("(\\d+[\\.,]\\d{2})").matcher(text);
  if (m.find()) return m.group(1).replace(",", ".");
  return "";
 }

 public String detectCategory(String text) {
  text = text.toLowerCase();
  if (text.contains("food") || text.contains("restaurant")) return "Food";
  if (text.contains("fuel") || text.contains("petrol")) return "Transport";
  if (text.contains("market") || text.contains("grocery")) return "Groceries";
  return "Misc";
 }

 public String extractDescription(String text) {
  return text.split("\\n")[0];
 }
}
