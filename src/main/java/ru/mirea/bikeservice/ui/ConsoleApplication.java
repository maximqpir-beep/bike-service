package ru.mirea.bikeservice.ui;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import ru.mirea.bikeservice.model.*;
import ru.mirea.bikeservice.service.*;
import ru.mirea.bikeservice.exception.*;
import ru.mirea.bikeservice.util.*;
public class ConsoleApplication {
    private final ClientService clients;
    private final ServiceRequestService requests;
    private final StatisticsService statistics;
    private final InputReader input;
    public ConsoleApplication(ClientService clients, ServiceRequestService requests, InputReader input) {
        this.clients=clients; this.requests=requests; this.input=input; this.statistics=new StatisticsService(clients,requests);
    }
    public void run() {
        while (true) {
            try {
                System.out.println("\n=== ВЕЛОСЕРВИС ===\n1. Клиенты\n2. Заявки\n3. Поиск\n4. Фильтрация\n5. Сортировка\n6. Статистика\n7. Экспорт Excel\n8. Вывести обе таблицы\n0. Выход");
                long n=input.number("Выберите действие: ");
                if (n==0) return;
                if (n==1) clientMenu();
                else if (n==2) requestMenu();
                else if (n==3) search();
                else if (n==4) filter();
                else if (n==5) sort();
                else if (n==6) statistics.calculate().forEach((key,value) -> System.out.println(key+": "+value));
                else if (n==7) export();
                else if (n==8) { showClients(clients.all()); showRequests(requests.all()); }
                else System.out.println("Нет такого пункта.");
            } catch (BusinessException | EntityNotFoundException | DatabaseException e) { System.out.println("Ошибка: "+e.getMessage()); }
            catch (IOException e) { System.out.println("Не удалось записать Excel: "+e.getMessage()); }
            catch (NoSuchElementException e) { System.out.println("\nВвод завершён. До свидания!"); return; }
        }
    }
    private void clientMenu() {
        System.out.println("1. Добавить\n2. Все клиенты\n3. По ID\n4. Изменить\n5. Удалить\n0. Назад");
        long n=input.number("Действие: ");
        if (n==0) return;
        if (n==1) { Client c=readClient(null); clients.create(c); System.out.println("Клиент создан, ID="+c.getId()); }
        else if (n==2) showClients(clients.all());
        else if (n==3) showClients(List.of(clients.get(input.number("ID: "))));
        else if (n==4) { long id=input.number("ID: "); showClients(List.of(clients.get(id))); clients.update(readClient(id)); System.out.println("Клиент изменён."); }
        else if (n==5) { long id=input.number("ID: "); clients.get(id); if (confirm()) { clients.delete(id); System.out.println("Клиент удалён."); } }
        else System.out.println("Нет такого пункта.");
    }
    private Client readClient(Long id) {
        return new Client(id,input.text("ФИО: "),input.text("Телефон (10–15 цифр): "),input.text("Email (можно пусто): "));
    }
    private void requestMenu() {
        System.out.println("1. Создать\n2. Все заявки\n3. По ID\n4. Изменить данные\n5. Удалить\n6. Изменить статус\n0. Назад");
        long n=input.number("Действие: ");
        if (n==0) return;
        if (n==1) { ServiceRequest r=readRequest(null); requests.create(r); System.out.println("Заявка создана, ID="+r.getId()); }
        else if (n==2) showRequests(requests.all());
        else if (n==3) showRequests(List.of(requests.get(input.number("ID: "))));
        else if (n==4) { long id=input.number("ID: "); showRequests(List.of(requests.get(id))); requests.update(readRequest(id)); System.out.println("Данные изменены."); }
        else if (n==5) { long id=input.number("ID: "); requests.get(id); if (confirm()) { requests.delete(id); System.out.println("Заявка удалена."); } }
        else if (n==6) { long id=input.number("ID: "); System.out.println("Текущий статус: "+requests.get(id).getStatus()); requests.changeStatus(id,input.choice("Новый статус: ",RequestStatus.class)); System.out.println("Статус изменён."); }
        else System.out.println("Нет такого пункта.");
    }
    private ServiceRequest readRequest(Long id) {
        ServiceRequest r=new ServiceRequest(); r.setId(id);
        r.setClientId(input.number("ID клиента: ")); r.setBikeBrand(input.text("Марка: ")); r.setBikeModel(input.text("Модель (можно пусто): "));
        r.setBikeType(input.choice("Тип велосипеда: ",BikeType.class)); r.setProblemDescription(input.text("Описание неисправности: "));
        r.setEstimatedCost(input.money("Стоимость, руб. (пусто = ещё не определена): ")); return r;
    }
    private boolean confirm() { return input.text("Удалить? Введите да: ").equalsIgnoreCase("да"); }
    private void search() {
        System.out.println("1. По марке\n2. По описанию\n0. Назад"); long n=input.number("Способ: ");
        if (n==1) showRequests(requests.searchBrand(input.text("Часть марки: ")));
        else if (n==2) showRequests(requests.searchProblem(input.text("Часть описания: ")));
        else if (n!=0) System.out.println("Нет такого пункта.");
    }
    private void filter() {
        System.out.println("1. По статусу\n2. По типу велосипеда\n0. Назад"); long n=input.number("Фильтр: ");
        if (n==1) showRequests(requests.filterStatus(input.choice("Статус: ",RequestStatus.class)));
        else if (n==2) showRequests(requests.filterType(input.choice("Тип: ",BikeType.class)));
        else if (n!=0) System.out.println("Нет такого пункта.");
    }
    private void sort() {
        System.out.println("1. По дате (старые сначала)\n2. По стоимости (по возрастанию, неизвестные в конце)\n0. Назад"); long n=input.number("Сортировка: ");
        if (n==1) showRequests(requests.sortDate()); else if (n==2) showRequests(requests.sortCost()); else if (n!=0) System.out.println("Нет такого пункта.");
    }
    private void export() throws IOException {
        Path path=Path.of("exports","bike-service-"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"))+".xlsx");
        new ExcelExporter().export(path,clients.all(),requests.all()); System.out.println("Экспорт создан: "+path.toAbsolutePath());
    }
    private void showClients(List<Client> list) {
        if (list.isEmpty()) System.out.println("Клиенты не найдены.");
        for (Client c:list) System.out.printf("ID=%d | %s | %s | %s%n",c.getId(),c.getFullName(),c.getPhone(),c.getEmail()==null?"—":c.getEmail());
    }
    private void showRequests(List<ServiceRequest> list) {
        if (list.isEmpty()) System.out.println("Заявки не найдены.");
        for (ServiceRequest r:list) System.out.printf("ID=%d | клиент=%d | %s %s | %s | %s | стоимость=%s | создана=%s | завершена=%s%n  %s%n",
            r.getId(),r.getClientId(),r.getBikeBrand(),r.getBikeModel()==null?"":r.getBikeModel(),r.getBikeType(),r.getStatus(),
            r.getEstimatedCost()==null?"не определена":r.getEstimatedCost(),r.getCreatedAt(),r.getCompletedAt()==null?"—":r.getCompletedAt(),r.getProblemDescription());
    }
}
