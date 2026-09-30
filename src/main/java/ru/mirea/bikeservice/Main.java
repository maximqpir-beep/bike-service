package ru.mirea.bikeservice;
import java.util.Scanner;
import ru.mirea.bikeservice.repository.*;
import ru.mirea.bikeservice.repository.jdbc.*;
import ru.mirea.bikeservice.service.*;
import ru.mirea.bikeservice.ui.ConsoleApplication;
import ru.mirea.bikeservice.util.*;
public class Main {
    public static void main(String[] args) {
        ConnectionProvider db=DatabaseManager.fromEnvironment();
        ClientRepository clients=new JdbcClientRepository(db);
        ServiceRequestRepository requests=new JdbcServiceRequestRepository(db);
        try (Scanner scanner=new Scanner(System.in)) {
            new ConsoleApplication(new ClientService(clients,requests),new ServiceRequestService(requests,clients),new InputReader(scanner)).run();
        }
    }
}
