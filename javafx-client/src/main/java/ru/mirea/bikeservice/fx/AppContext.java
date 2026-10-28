package ru.mirea.bikeservice.fx;

import ru.mirea.bikeservice.fx.dao.ClientDao;
import ru.mirea.bikeservice.fx.dao.ServiceRequestDao;
import ru.mirea.bikeservice.fx.dao.jdbc.JdbcClientDao;
import ru.mirea.bikeservice.fx.dao.jdbc.JdbcServiceRequestDao;
import ru.mirea.bikeservice.fx.database.DatabaseConfig;
import ru.mirea.bikeservice.fx.database.DatabaseManager;
import ru.mirea.bikeservice.fx.service.ClientService;
import ru.mirea.bikeservice.fx.service.ServiceRequestService;
import ru.mirea.bikeservice.fx.service.StatisticsService;

public final class AppContext {
    private static final AppContext INSTANCE = new AppContext();

    private final DatabaseManager database;
    private final ClientService clients;
    private final ServiceRequestService requests;
    private final StatisticsService statistics;

    private AppContext() {
        database = new DatabaseManager(DatabaseConfig.load());
        ClientDao clientDao = new JdbcClientDao(database);
        ServiceRequestDao requestDao = new JdbcServiceRequestDao(database);
        clients = new ClientService(clientDao);
        requests = new ServiceRequestService(requestDao, clientDao);
        statistics = new StatisticsService();
    }

    public static AppContext get() { return INSTANCE; }
    public DatabaseManager database() { return database; }
    public ClientService clients() { return clients; }
    public ServiceRequestService requests() { return requests; }
    public StatisticsService statistics() { return statistics; }
}
