package de.rwth.dbis.acis.bazaar.service.helpers;

import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacadeImpl;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Tag;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import org.apache.commons.dbcp2.BasicDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.junit.After;
import org.junit.Before;

import javax.sql.DataSource;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class SetupData {
    public DALFacade facade;
    public DALFacadeImpl dalImpl;
    public DSLContext jooq;
    public User initUser;
    public Project testProject;
    public Requirement testRequirement;
    // las2peer id of eve (defined in the testing components of las2peer)
    public String eveId = "799dea0f00e126dc3493f362bddbddbc55bdfbb918fce3b12f68e1340a8ea7de7aaaa8a7af900b6ee7f849a524b18649d4ae80cb406959568f405a487f085ac7";

    private static DataSource setupDataSource(String dbUrl, String dbUserName, String dbPassword) {
        BasicDataSource dataSource = new BasicDataSource();
        // Deprecated according to jooq
        // dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(dbUrl + "?useSSL=false&serverTimezone=UTC");
        dataSource.setUsername(dbUserName);
        dataSource.setPassword(dbPassword);
        dataSource.setValidationQuery("SELECT 1;");
        dataSource.setTestOnBorrow(true); // test each connection when borrowing from the pool with the validation query
        dataSource.setMaxConnLifetimeMillis(1000 * 60 * 60); // max connection life time 1h. mysql drops connection after 8h.
        return dataSource;
    }

    @Before
    public void setUp() throws Exception {
        String url = "jdbc:mysql://localhost:3306/reqbaz";

        DataSource dataSource = setupDataSource(url, "root", "12345678");

        dalImpl = new DALFacadeImpl(dataSource, SQLDialect.MYSQL);
        facade = dalImpl;
        jooq = dalImpl.getDslContext();

        Locale locale = new Locale("en", "us");
        Localization.getInstance().setResourceBundle(ResourceBundle.getBundle("i18n.Translation", locale));

        try {
            initUser = facade.getUserById(facade.getUserIdByLAS2PeerId(eveId));
        } catch (Exception e) {
            initUser = User.builder()
                    .eMail("test@test.hu")
                    .firstName("Elek")
                    .lastName("Test")
                    .userName("TestElek")
                    .las2peerId(eveId)
                    .build();

            facade.createUser(initUser);
            initUser = facade.getUserById(facade.getUserIdByLAS2PeerId(eveId));
            facade.addUserToRole(initUser.getId(), "SystemAdmin", null);
        }
        try {
            testProject = facade.getProjectById(0, initUser.getId());
            testRequirement = facade.getRequirementById(0, initUser.getId());
        } catch (Exception e) {
            testProject = Project.builder().name("ProjectTest").description("ProjDesc").leader(initUser).visibility(true).build();
            testProject = facade.createProject(testProject, initUser.getId());


            facade.createTag(Tag.builder()
                    .name("Bug")
                    .colour("#FF0000")
                    .projectId(testProject.getId())
                    .build());

            testRequirement = Requirement.builder()
                    .name("Test")
                    .description("Test")
                    .categories(List.of(testProject.getDefaultCategoryId()))
                    .projectId(testProject.getId())
                    .creator(initUser)
                    .build();
            testRequirement = facade.createRequirement(testRequirement, initUser.getId());
        }
    }

    @After
    public void tearDown() {
        jooq.delete(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT).where(de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project.PROJECT.ID.eq(testProject.getId())).execute();
    }
}
