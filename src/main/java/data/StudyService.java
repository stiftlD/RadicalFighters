package data;

import model.kanji.Kanji;
import org.sqlite.SQLiteException;

import javax.xml.transform.Result;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static data.KanjiDatabase.dataToKanji;

// this class maintains views on study/player performance data for usage by e.g. scheduler and stat-dashboard
public class StudyService {

    private KanjiDatabase kanjiDatabase;

    public StudyService() {
        // Initialize the database connection
        kanjiDatabase = new KanjiDatabase();
        createOverAllPerformanceView();
        createStudyPerformanceView();
        createViableKanjiView();
    }

    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;
        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
        public X getX() {return x;}
        public Y getY() {return y;}
    }

    public List<Kanji> getRandomKanjiInProfInterval(int maxGrade, double start, double end) throws SQLException, IllegalArgumentException {
        if (maxGrade < 0 || start < 0.0 || end > 1.0 || start > end)
            throw new IllegalArgumentException("start and end within [0, 1]");

        String queryNumViableKanji =
                "SELECT count() from viable_kanji;";

        try (Connection connection = SqliteHelper.getConn()) {
            try (Statement statement = connection.createStatement()) {

                ResultSet countResult = statement.executeQuery(queryNumViableKanji);
                int count = countResult.getInt("count()");
                int startIndex = (int) (count * start);
                int stopIndex = (int) (count * end);

                //System.out.println("count: " + count + ", startI: " + startIndex + ", stopI: " + stopIndex);

                double randIndex = Math.random() * (stopIndex - startIndex);
                int resultIndex = startIndex + (int) randIndex;
                //System.out.println("random was " + randIndex + ", result is " + resultIndex + " out of " + count);

                String selectKanjiInProfInterval =
                        "SELECT * FROM viable_kanji "
                                + "WHERE prof_rank == " + resultIndex;

                ResultSet resultSet = statement.executeQuery(selectKanjiInProfInterval);
                //System.out.println(resultSet);
                /*while (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    String kanji = resultSet.getString("Kanji");
                    // ... retrieve other columns as needed

                    System.out.println("ID: " + id);
                    System.out.println("Kanji: " + kanji);
                    System.out.println("row: " + resultSet.getInt("prof_rank"));
                    // ... print other columns

                    System.out.println("--------------------");
                }*/
                List<Kanji> result = dataToKanji(resultSet);
                connection.close();
                return result;
            } catch (SQLException e) { e.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }

        return null;
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
                    Kanji kanji = kanjiDatabase.getKanjiByID(resultSet.getInt("id"));
                    kanji.setProficiency((int) (resultSet.getDouble("success_rate") * 100.0));
                    resultList.add(kanji);
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

    // this view holds all kanji appropriate for the players current grade and ranks them by success rate
    private void createViableKanjiView() {
        try (Connection connection = SqliteHelper.getConn()) {
            String createViableKanjiView =
                    "CREATE VIEW IF NOT EXISTS viable_kanji AS \n" +
                            /*
                            TODO use a derived proficiency value instead of success_rate and then DESC
                            also probably do secondary ordering by #encounters or sth
                             */
                            "SELECT k.*, sp.success_rate, ROW_NUMBER() OVER (ORDER BY success_rate DESC, grade ASC) as prof_rank\n" +
                            "FROM kanji k LEFT OUTER JOIN study_performance sp ON k.id == sp.kanji_id, player_details p\n" +
                            "WHERE k.Grade > 0 AND k.Grade <= p.Grade;";

            try (Statement statement = connection.createStatement()) {
                // create viable Kanji view
                try {
                    statement.execute(createViableKanjiView);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("no new view created");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This view can provide detailed information about
    // the user's study performance for each kanji, including statistics
    // such as average study duration and success rate.
    public void createStudyPerformanceView() {
        String createStudyPerformanceViewSQL =
                "CREATE VIEW IF NOT EXISTS study_performance AS\n" +
                "SELECT kanji_id,\n" +
                "       AVG(datetime('%f', finish_time - start_time, \"unixepoch\")) AS avg_study_duration_seconds,\n" +
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
