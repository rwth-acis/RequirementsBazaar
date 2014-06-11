package de.rwth.dbis.acis.bazaar.dal;

import de.rwth.dbis.acis.bazaar.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ProjectsRecord;
import de.rwth.dbis.acis.bazaar.dal.repositories.ProjectRepository;
import de.rwth.dbis.acis.bazaar.dal.repositories.ProjectRepositoryImpl;
import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Projects.PROJECTS;
import static org.junit.Assert.*;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.BeforeClass;
import org.junit.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
* @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
* @since 6/9/2014
*/
public class RepositoryTest {


    private static ProjectRepository repo;


    public static final int PROJECT_ID = 1;
    public static final String PROJECT_DESC = "blabla";
    public static final String PROJECT_NAME = "projRec1";

    private static final Integer PROJECT_ID2 = 2;
    private static final String PROJECT_DESC2 = "Lorem ipsum";
    private static final String PROJECT_NAME2 = "testProject2";
    private static MockedProjectDataProvider mockedProjectDataProvider;

    @BeforeClass
    public static void setUp(){
        mockedProjectDataProvider = new MockedProjectDataProvider();
        Connection connection = new MockConnection(mockedProjectDataProvider);
        DSLContext context = DSL.using(connection, SQLDialect.MYSQL);
        repo = new ProjectRepositoryImpl(context);
    }

    @Test
    public void testFindById() throws Exception {

        Project project = repo.findById(PROJECT_ID);

        assertEquals((long)PROJECT_ID,(long)project.getId());
        assertEquals(PROJECT_DESC,project.getDescription());
        assertEquals(PROJECT_NAME,project.getName());
    }

    @Test
    public void testFindAll() throws Exception {
        List<Project> projects = repo.findAll();

        assertNotNull(projects);
        assertEquals(2,projects.size());
        Project project1 = projects.get(0);

        assertEquals((long)PROJECT_ID,(long)project1.getId());
        assertEquals(PROJECT_DESC,project1.getDescription());
        assertEquals(PROJECT_NAME,project1.getName());

        Project project2 = projects.get(1);

        assertEquals((long)PROJECT_ID2,(long)project2.getId());
        assertEquals(PROJECT_DESC2,project2.getDescription());
        assertEquals(PROJECT_NAME2,project2.getName());
    }

//    @Test
//    public void testDeleteAddFirst() throws Exception {
//        repo.delete(PROJECT_ID);
//        assertEquals((long)PROJECT_ID,(long)mockedProjectDataProvider.getDeletedIds().get(0));
//
//
//
//        Project project = repo.findById(PROJECT_ID);
//        assertNull(project);
//
//        mockedProjectDataProvider.getDeletedIds().clear();
//    }


    public static class MockedProjectDataProvider implements MockDataProvider {
        private List<ProjectsRecord> records;

        public List<Integer> getDeletedIds() {
            return deletedIds;
        }

        private List<Integer> deletedIds = new ArrayList<Integer>();
        private final DSLContext create;

        public MockedProjectDataProvider(){
            create = DSL.using(SQLDialect.MYSQL);

            ProjectsRecord record1 = create.newRecord(PROJECTS);
            record1.setValue(PROJECTS.ID, PROJECT_ID);
            record1.setValue(PROJECTS.DESCRIPTION, PROJECT_DESC);
            record1.setValue(PROJECTS.NAME, PROJECT_NAME);

            ProjectsRecord record2 = create.newRecord(PROJECTS);
            record2.setValue(PROJECTS.ID, PROJECT_ID2);
            record2.setValue(PROJECTS.DESCRIPTION, PROJECT_DESC2);
            record2.setValue(PROJECTS.NAME, PROJECT_NAME2);

            records = new ArrayList<ProjectsRecord>();
            records.add(record1);
            records.add(record2);
        }

        @Override
        public MockResult[] execute(MockExecuteContext ctx) throws SQLException {

            MockResult[] mock = new MockResult[1];

            String sql = ctx.sql();


            // DROP IS NOT SUPPORTED
            if (sql.toUpperCase().startsWith("DROP")) {
                throw new SQLException("Statement not supported: " + sql);
            }
            else if (sql.toUpperCase().startsWith("DELETE")){
                Integer id = (Integer) ctx.bindings()[0];
                deletedIds.add(id);

            }
            else if (sql.toUpperCase().startsWith("INSERT")){

            }
            // IF SELECT + WHERE, so FindById and we return 1 record
            else if (sql.toUpperCase().startsWith("SELECT") && sql.toUpperCase().contains("WHERE"))
            {
                Result<ProjectsRecord> result = create.newResult(PROJECTS);
                Integer id = (Integer) ctx.bindings()[0];
                if(!deletedIds.contains(id))
                    result.add(records.get(id-1));
                mock[0] = new MockResult(1, result);
            }
            // IF SELECT ONLY: FindAll so we return more records
            else if (sql.toUpperCase().startsWith("SELECT")) {
                Result<ProjectsRecord> result = create.newResult(PROJECTS);
                result.add(records.get(0));
                result.add(records.get(1));
                mock[0] = new MockResult(1,result);
            }
            return mock;
        }
    }
}

