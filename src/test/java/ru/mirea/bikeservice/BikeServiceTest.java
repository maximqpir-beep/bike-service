package ru.mirea.bikeservice;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.bikeservice.model.*;
import ru.mirea.bikeservice.repository.*;
import ru.mirea.bikeservice.repository.jdbc.*;
import ru.mirea.bikeservice.service.*;
import ru.mirea.bikeservice.exception.*;
import ru.mirea.bikeservice.util.*;
import ru.mirea.bikeservice.ui.ConsoleApplication;

class BikeServiceTest {
    private ConnectionProvider db;
    private ClientRepository clientRepo;
    private ServiceRequestRepository requestRepo;
    private ClientService clients;
    private ServiceRequestService requests;
    private String schema;
    private String pgUrl;
    @TempDir Path temp;

    @BeforeEach void setup() throws Exception {
        pgUrl=System.getenv("TEST_DB_URL");
        if (pgUrl == null) {
            String url="jdbc:h2:mem:"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
            db=() -> DriverManager.getConnection(url,"sa","");
        } else {
            schema="test_"+UUID.randomUUID().toString().replace("-","");
            try (Connection c=pgConnection(); Statement s=c.createStatement()) { s.execute("CREATE SCHEMA "+schema); }
            db=() -> { Connection c=pgConnection(); c.setSchema(schema); return c; };
        }
        executeScript("sql/01_schema.sql");
        clientRepo=new JdbcClientRepository(db); requestRepo=new JdbcServiceRequestRepository(db);
        clients=new ClientService(clientRepo,requestRepo); requests=new ServiceRequestService(requestRepo,clientRepo);
    }
    private Connection pgConnection() throws SQLException {
        return DriverManager.getConnection(pgUrl,System.getenv().getOrDefault("TEST_DB_USER","postgres"),System.getenv().getOrDefault("TEST_DB_PASSWORD",""));
    }
    @AfterEach void cleanup() throws Exception {
        if (schema != null) try (Connection c=pgConnection(); Statement s=c.createStatement()) { s.execute("DROP SCHEMA "+schema+" CASCADE"); }
        else if (db != null) try (Connection c=db.getConnection(); Statement s=c.createStatement()) { s.execute("SHUTDOWN"); }
    }
    private void executeScript(String file) throws Exception {
        try (Connection c=db.getConnection(); Statement s=c.createStatement()) {
            for (String sql:Files.readString(Path.of(file)).split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    private Client client() { return clients.create(new Client(null,"Иван Петров","+7 (900) 000-00-01","ivan@example.com")); }
    private ServiceRequest draft(long clientId) {
        ServiceRequest r=new ServiceRequest(); r.setClientId(clientId); r.setBikeBrand("Trek"); r.setBikeModel("Marlin");
        r.setBikeType(BikeType.MOUNTAIN); r.setProblemDescription("Скрип тормозов"); r.setEstimatedCost(new BigDecimal("1500.50")); return r;
    }
    private ServiceRequest create() { return requests.create(draft(client().getId())); }
    private void start(long id) { requests.changeStatus(id,RequestStatus.ACCEPTED); requests.changeStatus(id,RequestStatus.IN_PROGRESS); }

    @Test void clientCrudAndNormalization() {
        Client c=client(); assertEquals("+79000000001",clients.get(c.getId()).getPhone());
        c.setFullName("Пётр Иванов"); clients.update(c); assertEquals("Пётр Иванов",clients.get(c.getId()).getFullName());
        clients.delete(c.getId()); assertThrows(EntityNotFoundException.class,() -> clients.get(c.getId()));
    }
    @Test void requestCrudPersistsAcrossRepositoryInstances() {
        ServiceRequest r=create(); assertEquals(RequestStatus.CREATED,r.getStatus());
        r.setProblemDescription("Замена цепи"); requests.update(r);
        assertEquals("Замена цепи",new JdbcServiceRequestRepository(db).findById(r.getId()).orElseThrow().getProblemDescription());
        requests.delete(r.getId()); assertThrows(EntityNotFoundException.class,() -> requests.get(r.getId()));
    }
    @Test void missingClientRejected() { assertThrows(BusinessException.class,() -> requests.create(draft(999))); }
    @Test void mandatoryFieldsRejected() {
        ServiceRequest r=draft(client().getId()); r.setBikeBrand("  "); assertThrows(BusinessException.class,() -> requests.create(r));
        r.setBikeBrand("Trek"); r.setProblemDescription(""); assertThrows(BusinessException.class,() -> requests.create(r));
        r.setProblemDescription("Ремонт"); r.setBikeType(null); assertThrows(BusinessException.class,() -> requests.create(r));
    }
    @Test void invalidCostsRejected() {
        ServiceRequest r=draft(client().getId());
        for (String cost:List.of("-1","100000000","0.001")) { r.setEstimatedCost(new BigDecimal(cost)); assertThrows(BusinessException.class,() -> requests.create(r)); }
    }
    @Test void duplicatesRejectedAndBlankEmailAllowed() {
        client(); assertThrows(BusinessException.class,() -> clients.create(new Client(null,"Другой","+79000000001",null)));
        assertThrows(BusinessException.class,() -> clients.create(new Client(null,"Другой","+79000000002","IVAN@example.com")));
        Client c=clients.create(new Client(null,"Анна","+79000000003"," ")); assertNull(clients.get(c.getId()).getEmail());
    }
    @Test void clientWithRequestsCannotBeDeleted() { ServiceRequest r=create(); assertThrows(BusinessException.class,() -> clients.delete(r.getClientId())); }
    @Test void transitionsAndCompletionDatePersist() {
        ServiceRequest r=create(); assertThrows(BusinessException.class,() -> requests.changeStatus(r.getId(),RequestStatus.COMPLETED));
        start(r.getId()); requests.changeStatus(r.getId(),RequestStatus.WAITING_FOR_PARTS); requests.changeStatus(r.getId(),RequestStatus.IN_PROGRESS);
        requests.changeStatus(r.getId(),RequestStatus.COMPLETED);
        assertNotNull(requests.get(r.getId()).getCompletedAt());
        assertThrows(BusinessException.class,() -> requests.changeStatus(r.getId(),RequestStatus.IN_PROGRESS));
        assertThrows(BusinessException.class,() -> requests.update(requests.get(r.getId())));
    }
    @Test void completionRequiresKnownCost() {
        ServiceRequest r=create(); r.setEstimatedCost(null); requests.update(r); start(r.getId());
        assertThrows(BusinessException.class,() -> requests.changeStatus(r.getId(),RequestStatus.COMPLETED));
    }
    @Test void ordinaryUpdateCannotBypassStatusRules() {
        ServiceRequest r=create(); r.setStatus(RequestStatus.COMPLETED); requests.update(r);
        assertEquals(RequestStatus.CREATED,requests.get(r.getId()).getStatus()); assertNull(requests.get(r.getId()).getCompletedAt());
    }
    @Test void cancelledRequestIsImmutable() {
        ServiceRequest r=create(); requests.changeStatus(r.getId(),RequestStatus.CANCELLED);
        for (RequestStatus s:RequestStatus.values()) assertThrows(BusinessException.class,() -> requests.changeStatus(r.getId(),s));
        assertThrows(BusinessException.class,() -> requests.update(r));
    }
    @Test void initialDataSearchFilterSortAndStatistics() throws Exception {
        executeScript("sql/02_seed.sql");
        assertEquals(5,clients.all().size()); assertEquals(12,requests.all().size());
        assertEquals(2,requests.searchBrand("tReK").size()); assertEquals(2,requests.searchProblem("СКРИП").size());
        assertEquals(3,requests.filterStatus(RequestStatus.COMPLETED).size()); assertEquals(1,requests.filterType(BikeType.BMX).size());
        var sorted=requests.sortCost(); assertEquals(new BigDecimal("800.00"),sorted.get(0).getEstimatedCost()); assertNull(sorted.get(11).getEstimatedCost());
        assertTrue(requests.sortDate().get(0).getCreatedAt().isBefore(requests.sortDate().get(11).getCreatedAt()));
        var stats=new StatisticsService(clients,requests).calculate(); assertEquals("7500.00",stats.get("Стоимость завершённых работ, руб."));
        assertEquals("2640.00",stats.get("Средняя указанная стоимость, руб."));
    }
    @Test void emptyStatisticsWork() { assertEquals("нет данных",new StatisticsService(clients,requests).calculate().get("Средняя указанная стоимость, руб.")); }
    @Test void databaseConstraintsProtectDirectWrites() throws Exception {
        Client c=client();
        assertThrows(DatabaseException.class,() -> clientRepo.save(new Client(null,"Дубль",c.getPhone(),null)));
        ServiceRequest r=draft(999); r.setStatus(RequestStatus.CREATED); r.setCreatedAt(java.time.LocalDateTime.now());
        assertThrows(DatabaseException.class,() -> requestRepo.save(r));
        r.setClientId(c.getId()); r.setEstimatedCost(new BigDecimal("-1")); assertThrows(DatabaseException.class,() -> requestRepo.save(r));
    }
    @Test void quotedUserTextCannotChangeSql() {
        Client c=client(); ServiceRequest r=draft(c.getId()); r.setBikeBrand("'; DROP TABLE clients; --"); requests.create(r);
        assertEquals(1,clients.all().size()); assertEquals(r.getBikeBrand(),requests.get(r.getId()).getBikeBrand());
    }
    @Test void exportContainsBothSheetsTypedMoneyAndDates() throws Exception {
        ServiceRequest r=create(); start(r.getId()); requests.changeStatus(r.getId(),RequestStatus.COMPLETED);
        Path output=temp.resolve("export.xlsx"); new ExcelExporter().export(output,clients.all(),requests.all());
        try (var in=Files.newInputStream(output); var wb=new XSSFWorkbook(in)) {
            assertEquals(2,wb.getNumberOfSheets()); var row=wb.getSheet("Заявки").getRow(1);
            assertEquals(1500.50,row.getCell(9).getNumericCellValue()); assertNotNull(row.getCell(11).getLocalDateTimeCellValue());
            assertEquals("Иван Петров",row.getCell(2).getStringCellValue());
        }
        assertThrows(FileAlreadyExistsException.class,() -> new ExcelExporter().export(output,clients.all(),requests.all()));
    }
    @Test void inputRecoversFromInvalidNumbersAndEnums() {
        InputReader in=new InputReader(new Scanner("abc\n17\nwrong\n12,50\n99\n1\n"));
        assertEquals(17,in.number("")); assertEquals(new BigDecimal("12.50"),in.money("")); assertEquals(BikeType.ROAD,in.choice("",BikeType.class));
    }
    @Test void consoleSurvivesConnectionFailureAndBadInput() {
        ConnectionProvider broken=() -> { throw new SQLException("offline","08001"); };
        ClientRepository cr=new JdbcClientRepository(broken); ServiceRequestRepository rr=new JdbcServiceRequestRepository(broken);
        ByteArrayOutputStream output=new ByteArrayOutputStream(); PrintStream old=System.out;
        try {
            System.setOut(new PrintStream(output));
            new ConsoleApplication(new ClientService(cr,rr),new ServiceRequestService(rr,cr),new InputReader(new Scanner("abc\n8\n0\n"))).run();
        } finally { System.setOut(old); }
        assertTrue(output.toString().contains("Нет соединения")); assertTrue(output.toString().contains("введите целое число"));
    }
}
