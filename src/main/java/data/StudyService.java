package data;

import model.kanji.Kanji;
import org.sqlite.SQLiteException;

import javax.xml.transform.Result;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// this class maintains views on study/player performance data for usage by e.g. scheduler and stat-dashboard
public class StudyService {

    private KanjiDatabase kanjiDatabase;

    public StudyService() {
        // Initialize the database connection
        kanjiDatabase = new KanjiDatabase();
        createOverAllPerformanceView();
        createStudyPerformanceView();
    }

    public List<Kanji> getKanjiRankedByProficiency() {
        List<Kanji> resultList = new ArrayList<Kanji>();

        // This SQL query retrieves a list of kanji along with their proficiency statistics,
        // sorted by success rate and study duration.
        String selectAllKnownKanjisStudyPerformanceSQL =
                "SELECT k.id, k.kanji,\n" +
                "sp.avg_study_duration_seconds, sp.success_rate\n" +
                "FROM study_performance sp\n" +
                "JOIN kanji k ON sp.kanji_id = k.id\n" +
                "ORDER BY sp.success_rate DESC, sp.avg_study_duration_seconds ASC;\n";

        try (Connection connection = SqliteHelper.getConn()) {

            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(selectAllKnownKanjisStudyPerformanceSQL);
                if (resultSet == null) return resultList;
                // turn our query results into kanji objects to be used within the model
                // TODO we should do this conversion in bulk, also right now we do not keep the statistics data
                while (resultSet.next()) {
                    resultList.add(kanjiDatabase.getKanjiByID(resultSet.getInt("id")));
                }
            } catch (SQLException e) { e.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }

        return resultList;
    }


    // create player progress view as
    // how many times each kanji has been studied,
    // how many times it was successfully learned,
    // and how many times it was not successfully learned
    private void createOverAllPerformanceView() {
        String createOverallProgressViewSQL =
                "CREATE VIEW IF NOT EXISTS overall_progress AS "
                + "SELECT kanji_id, "
                + "COUNT(*) AS total_attempts, "
                + "SUM(result) AS successful_attempts, "
                + "(COUNT(*) - SUM(result)) AS unsuccessful_attempts "
                + "FROM study_log "
                + "GROUP BY kanji_id;";

        try (Connection connection = SqliteHelper.getConn()) {

            try (Statement statement = connection.createStatement()) {
                statement.execute(createOverallProgressViewSQL);
            } catch (SQLException e) { e.printStackTrace(); }
            finally {
                if (connection != null) {
                    try {
                        connection.close(); // <-- This is important
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // This view can provide detailed information about
    // the user's study performance for each kanji, including statistics
    // such as average study duration and success rate.
    public void createStudyPerformanceView() {
        String createStudyPerformanceViewSQL =
                "CREATE VIEW IF NOT EXISTS study_performance AS\n" +
                "SELECT kanji_id,\n" +
                "       AVG(strftime('%s', finish_time) - strftime('%s', start_time)) AS avg_study_duration_seconds,\n" +
                "       (SUM(result) * 1.0 / COUNT(*)) AS success_rate\n" +
                "FROM study_log\n" +
                "GROUP BY kanji_id;\n";

        try (Connection connection = SqliteHelper.getConn()) {

            try (Statement statement = connection.createStatement()) {
                statement.execute(createStudyPerformanceViewSQL);
            } catch (SQLException e) { e.printStackTrace(); } finally {
                if (connection != null) {
                    try {
                        connection.close(); // <-- This is important
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

    }

    // Other methods for filtering, custom views, and additional statistics can be added here
    private class StudyViewBuilder {
        private String taskType;
        private String kanjiSubject;
        private String viewName;
        private List<String> selections;

        public StudyViewBuilder withViewName(String viewName) {
            this.viewName = viewName;
            return this;
        }

        public StudyViewBuilder withTaskType(String taskType) {
            this.taskType = taskType;
            return this;
        }

        public StudyViewBuilder withKanjiSubject(String kanjiSubject) {
            this.kanjiSubject = kanjiSubject;
            return this;
        }

        public StudyViewBuilder withSelections(List<String> selections) {
            this.selections = selections;
            return this;
        }

        public void execute() {
            String sql = buildSQLQuery();

            try (Connection connection = SqliteHelper.getConn();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                // Execute the prepared statement to create the view in the database
                preparedStatement.execute();
                connection.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        private String buildSQLQuery() {
            StringBuilder query = new StringBuilder("CREATE VIEW ");
            query.append(viewName).append(" AS SELECT kanji_id");

            if (selections != null && !selections.isEmpty()) {
                query.append(", ").append(String.join(", ", selections));
            }

            query.append(" FROM study_log WHERE 1=1");

            if (taskType != null) {
                query.append(" AND type = ?");
            }

            if (kanjiSubject != null) {
                query.append(" AND subject = ?");
            }

            return query.toString();
        }

    }

}
