package ru.mirea.bikeservice.util;

import java.math.BigDecimal;
import java.util.*;
public class InputReader {
    private final Scanner scanner;
    public InputReader(Scanner scanner) { this.scanner = scanner; }
    public String text(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) throw new NoSuchElementException("Ввод завершён");
        return scanner.nextLine().trim();
    }
    public long number(String prompt) {
        while (true) { try { return Long.parseLong(text(prompt)); } catch (NumberFormatException e) { System.out.println("Ошибка: введите целое число."); } }
    }
    public BigDecimal money(String prompt) {
        while (true) {
            String value = text(prompt);
            if (value.isEmpty()) return null;
            try { return new BigDecimal(value.replace(',', '.')); }
            catch (NumberFormatException e) { System.out.println("Ошибка: введите стоимость числом, например 1500.50."); }
        }
    }
    public <E extends Enum<E>> E choice(String prompt, Class<E> type) {
        E[] values = type.getEnumConstants();
        for (int i=0;i<values.length;i++) System.out.println((i+1) + ". " + values[i]);
        while (true) { long n = number(prompt); if (n >= 1 && n <= values.length) return values[(int)n-1]; System.out.println("Нет такого варианта."); }
    }
}
