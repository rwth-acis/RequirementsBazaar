package de.rwth.dbis.acis.bazaar.service.customChangeSets;

import liquibase.Scope;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.RawSqlStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddDefaultTags implements CustomTaskChange {
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;

    @Override
    public void execute(Database database) throws CustomChangeException {
        try {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            String sql = "SELECT * FROM project";
            SqlStatement sqlStatement = new RawSqlStatement(sql);

            List<Map<String, ?>> results = executor.queryForList(sqlStatement);
            List<Integer> projectIds = new ArrayList<>();
            for (Map<String, ?> result : results) {
                int projectId = ((Integer) result.get("id"));
                projectIds.add(projectId);
            }

            for (int id : projectIds) {
                InsertStatement insertStatementMust = getInsertStatement(id, "Must", "#991629");
                InsertStatement insertStatementCould = getInsertStatement(id, "Could", "#72a16f");
                InsertStatement insertStatementShould = getInsertStatement(id, "Should", "#FFD966");
                executor.execute(insertStatementShould);
                executor.execute(insertStatementMust);
                executor.execute(insertStatementCould);
            }

        } catch (DatabaseException e) {
            throw new CustomChangeException(e);
        }
    }

    private static InsertStatement getInsertStatement(int id, String name, String colour) {
        return new InsertStatement("reqbaz", "public", "tag")
                .addColumnValue("project_id", id)
                .addColumnValue("name", name)
                .addColumnValue("colour", colour);
    }

    @Override
    public String getConfirmationMessage() {
        return "Tags added";
    }

    @Override
    public void setUp() throws SetupException {
        ;
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        ;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
