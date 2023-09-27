package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.kanji.Kanji;
import model.radicals.Radical;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.SQLException;

import java.sql.Timestamp;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class KanjiDatabase {

    private String rootDir = System.getProperty("user.dir"); //TODO config file for this
    private String kanjiDBURL = "jdbc:sqlite:" + Path.of(rootDir + "/database/kanji.db").toString();
    private Path kanjiPath = Path.of(rootDir + "/src/main/java/data/kanjis.json");

    int currRadID = 0;

    private int generateRadID() {
        return currRadID++;
    }

    int currKanjiID = 0;

    private int generateKanjiID() {
        return currKanjiID++;
    }

    public KanjiDatabase() {

    }


    // setup the kanji database from kanji.json if it doesn't exist
    // TODO obviously refactor. call different table creation methods and then set the data on them
    public void initialize() throws SQLException {

        try (Connection connection = SqliteHelper.getConn()) {
            createKanjiTable(connection);
            createRadicalTable(connection);
            createKanjiComponentRelationsTable(connection);
            createStudyLogTable(connection);

            connection.close();
        } catch (SQLException e) { e.printStackTrace(); }

        try (Connection connection = SqliteHelper.getConn()) {
            // TODO right now we HAVE to init radicals first
            initializeRadicalTable(connection);
            initializeKanjiTable(connection);

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TODO move some of these specific query methods to studyhelper etc.

    public Kanji getKanjiByID(int kanjiID){
        String selectKanjiWithID =
                "SELECT * FROM kanji " +
                        "WHERE kanji.id == " + kanjiID;

        try (Connection connection = SqliteHelper.getConn()) {
            try (Statement statement = connection.createStatement()) {

                Kanji result = null;
                ResultSet resultSet = statement.executeQuery(selectKanjiWithID);

                if (resultSet == null) return result;
                else {
                    result = dataToKanji(resultSet).get(0);
                }
                connection.close();
                return result;
            } catch (SQLException e) { e.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Radical> getRadicalComponents (int kanjiID) {
        List<Radical> result = new ArrayList<Radical>();

        String selectComponentsOfKanjiSQL =
                "SELECT r.* FROM radicals r, kanji k " +
                        "WHERE k.id == " + kanjiID + " AND EXISTS (" +
                        "SELECT * from component_relations rel " +
                        "WHERE rel.radical_id == r.id AND k.id == rel.kanji_id" +
                        ")";

        try (Connection connection = SqliteHelper.getConn()) {
            try (Statement statement = connection.createStatement()) {

                //System.out.println(selectComponentsOfKanjiSQL);
                ResultSet resultSet = statement.executeQuery(selectComponentsOfKanjiSQL);
                if (resultSet == null) return null;

                result = dataToRadical(resultSet);
                connection.close();
            } catch (SQLException e) { e.printStackTrace(); }
        connection.close();
        } catch (SQLException e) { e.printStackTrace(); }

        return result;
    }

    // return IDs of this kanji's components
    // TODO component data is not complete yet, have to scrape some more
    public List<Integer> getComponentIDs(int kanjiID) {
        List<Integer> result = new ArrayList<Integer>();

        String selectComponentsOfKanjiSQL =
                "SELECT r.id FROM radicals r, kanji k" +
                "WHERE k.id == " + kanjiID + " AND EXISTS (" +
                "SELECT * from component_relations rel" +
                "WHERE rel.radical_id == r.id AND k.id == rel.kanji_id" +
                ")";

        try (Connection connection = SqliteHelper.getConn()) {
            try (Statement statement = connection.createStatement()) {

                ResultSet resultSet = statement.executeQuery(selectComponentsOfKanjiSQL);

                while (resultSet != null && resultSet.next()) {
                    result.add(resultSet.getInt("ID"));
                }
                connection.close();
                return result;
            } catch (SQLException e) { e.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }

        return result;
    }


    private List<Radical> dataToRadical(ResultSet resultSet) {
        List<Radical> result = new ArrayList<Radical>();
        try {
            //System.out.println(resultSet);
            while (resultSet.next()) {
                Radical radical = new Radical(
                        resultSet.getInt("ID"),
                        resultSet.getString("Character"),
                        resultSet.getString("Character"),
                        resultSet.getString("Meaning"),
                        null
                );
                result.add(radical);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private class JsonRadical {
        //@Expose
        private String character;
        //@Expose
        private String meaning;
        //@Expose
        private String image;
        //@Expose
        private int level;

        public JsonRadical(String character, String meaning, String image, int level) {
            this.character = character;
            this.meaning = meaning;
            this.level = level;
            this.image = image;
        }

        public String getMeaning() {
            return meaning;
        }

        public int getLevel() {
            return level;
        }

        public String getImage() {
            return image;
        }

        public String getCharacter() {
            return character;
        }
    };

    private class JsonKanji {
        private String kanji;
        private int grade;
        private int stroke_count;
        private List<String> meanings;
        private List<String> kun_readings;
        private List<String> on_readings;
        private List<String> name_readings;
        private int jlpt;
        private String unicode;
        private String heisig_en;
        private List<String> components;

        public JsonKanji(String kanji, int grade, int stroke_count, List<String> meanings,
                         List<String> kun_readings, List<String> on_readings, List<String> name_readings,
                         int jlpt, String unicode, String heisig_en, List<String> components) {
            this.kanji = kanji;
            this.grade = grade;
            this.stroke_count = stroke_count;
            this.meanings = meanings;
            this.kun_readings = kun_readings;
            this.on_readings = on_readings;
            this.name_readings = name_readings;
            this.jlpt = jlpt;
            this.unicode = unicode;
            this.heisig_en = heisig_en;
            this.components = components;
        }

        public String getCharacter() {
            return this.kanji;
        }

        public int getGrade() {
            return this.grade;
        }

        public int getStrokeCount() {
            return this.stroke_count;
        }

        public List<String> getMeanings() {
            return this.meanings;
        }

        public List<String> getKunReadings() {
            return this.kun_readings;
        }

        public List<String> getOnReadings() {
            return this.on_readings;
        }

        public List<String> getNameReadings() {
            return this.name_readings;
        }

        public List<String> getComponents() { return this.components; }

        public int getJLPT() {
            return this.jlpt;
        }

        public String getUnicode() {
            return this.unicode;
        }

        public String getHeisig() {
            return this.heisig_en;
        }
    }

    private void createKanjiTable(Connection connection) {
        String createTableSql = "CREATE TABLE IF NOT EXISTS kanji ("
                + "ID INTEGER PRIMARY KEY,"
                + "Kanji TEXT,"
                + "Grade INTEGER,"
                + "stroke_count INTEGER,"
                + "meanings TEXT,"
                + "Kun_readings TEXT,"
                + "On_readings TEXT,"
                + "Name_readings TEXT,"
                + "JLPT INTEGER,"
                + "Unicode TEXT,"
                + "Heisig_en TEXT,"
                + "Proficiency INTEGER,"
                + "Encountered BOOLEAN"
                + ");";

        System.out.println("Creating data base at: " + kanjiDBURL);
        File f = new File(kanjiDBURL);
        if (!f.exists() && !f.isDirectory()) {
            // create kanji table
            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableSql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Kanji Table created");

            // small table to track player data, as sqlite doesn't have variables
            // could have stuff like previous experience, study goals etc
            String createPlayerTableSQL =
                    "CREATE TABLE IF NOT EXISTS player_details ("
                    + "ID INTEGER PRIMARY KEY,"
                    + "Grade INTEGER,"
                    + "Name TEXT,"
                    + "Level INTEGER"
                    + ");";

            try (Statement statement = connection.createStatement()) {
                try {
                    statement.execute(createPlayerTableSQL);
                    statement.execute("INSERT INTO player_details VALUES(0, 3, \"TestPlayer\", 1);"); //TODO set these from controller after init
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("table player_details not created");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println(kanjiDBURL + " exists.");
        }
    }

    public void initializeKanjiTable(Connection connection) {
        String insertDataSql = "INSERT INTO kanji ("
                + "ID,"
                + "Kanji,"
                + "Grade,"
                + "stroke_count,"
                + "meanings,"
                + "Kun_readings,"
                + "On_readings,"
                + "Name_readings,"
                + "JLPT,"
                + "Unicode,"
                + "Heisig_en,"
                + "Proficiency,"
                + "Encountered"
                + ") VALUES ("
                + "?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + " ?,"
                + "?);";

        String insertComponentRelationSQL = "INSERT INTO component_relations ("
                + "Kanji_id,"
                + "Radical_id"
                + ") VALUES ("
                + "?, "
                + "?);";

        JsonKanji[] kanjiArray = {};
        //parse json data
        try {
            //Read JSON file
            //Path jsonPath = Path.of("C:\\Users\\david\\projects\\RadicalFighters\\src\\main\\java\\data\\kanji.json");
            Path jsonPath = kanjiPath;
            //System.out.println(jsonPath);
            FileInputStream fis = new FileInputStream(jsonPath.toString());
            String jsonString = IOUtils.toString(fis, "UTF-8");
            //System.out.println(jsonString);
            Gson gson = new GsonBuilder().serializeNulls().create();
            kanjiArray = gson.fromJson(jsonString, JsonKanji[].class);
            //System.out.println("read kanjiArray");
            //System.out.println("read " + kanjiArray.length + " kanji");
            // insert parsed kanji into into  kanjiDB
            Arrays.asList(kanjiArray).stream().forEach(kanji -> {
                        //if (kanji.getGrade() < 1 || kanji.getGrade() > 5) return; //TODO just so we have less data for now

                        int kanjiID = generateKanjiID();

                        // intialize kanji table
                        try (PreparedStatement statement = connection.prepareStatement(insertDataSql)) {

                            statement.setInt(1, kanjiID);
                            statement.setString(2, kanji.getCharacter());

                            // parse and set grades
                                /*String gradesString = kanji.getGrades().stream()
                                        .map(String::valueOf)
                                        .collect(Collectors.joining(", "));
                                statement.setString(3, gradesString);*/
                            statement.setInt(3, kanji.getGrade());

                            statement.setInt(4, kanji.getStrokeCount());

                            //set meanings
                            String meaningString = kanji.getMeanings().stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "));
                            statement.setString(5, meaningString);

                            // set kun readings
                            String kunyomiString = kanji.getKunReadings().stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "));
                            statement.setString(6, kunyomiString);

                            //set on readings
                            String onyomiString = kanji.getOnReadings().stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "));
                            statement.setString(7, onyomiString);

                            //set name readings
                            String nameString = kanji.getNameReadings().stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "));
                            statement.setString(8, nameString);

                            //set JLPT
                            statement.setInt(9, kanji.getJLPT());

                            //set Unicode
                            statement.setString(10, kanji.getUnicode());

                            statement.setString(11, kanji.getHeisig());

                            statement.setInt(12, 5); // initialize proficiency at 5

                            statement.setBoolean(13, false);

                            statement.executeUpdate();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }

                        if (kanji.getComponents() == null) return;
                        // get the ids of the radicals that are components of this kanji
                        // TODO this assumes radical table to exist
                        // TODO also way better ways to do this probably
                        Map<String, Integer> radToIdMap = new HashMap<String, Integer>();
                        kanji.getComponents().stream().forEach( component -> {

                            ResultSet resultSet = null;
                            int radicalID = 0;
                            if (radToIdMap.get(component) == null) {
                                try (Statement statement = connection.createStatement()) {
                                    String query = "SELECT ID FROM radicals "
                                            + "WHERE character == '" + component + "';";

                                    resultSet = statement.executeQuery(query);
                                    if (resultSet != null) {
                                        radicalID = resultSet.getInt("ID");
                                        radToIdMap.put(component, radicalID);
                                    } else System.out.println("Radical not in table");
                                } catch (SQLException throwables) {
                                    throwables.printStackTrace();
                                }
                            } else {
                                    radicalID = radToIdMap.get(component);
                            }

                            //System.out.println("Radical: " + component);
                            //System.out.println("ID: " + radicalID);

                            // ... print other columns

                            //System.out.println("--------------------");

                            // initialize component_relations table
                            // TODO write probably the nicest test yet
                            try (PreparedStatement component_relation_statement = connection.prepareStatement(insertComponentRelationSQL)) {

                                component_relation_statement.setInt(1, kanjiID);
                                component_relation_statement.setInt(2, radicalID);

                                component_relation_statement.executeUpdate();
                            } catch (SQLException e) { e.printStackTrace(); }
                        });

                    }
            );
        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // to test print kanji with grade==1
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM kanji WHERE Grade == 1;";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                String kanji = resultSet.getString("Kanji");
                // ... retrieve other columns as needed

                System.out.println("ID: " + id);
                System.out.println("Kanji: " + kanji);
                // ... print other columns

                System.out.println("--------------------");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void createRadicalTable(Connection connection) {
        String createRadicalTableSql = "CREATE TABLE IF NOT EXISTS radicals ("
                //+ "Name TEXT,"
                + "ID INTEGER PRIMARY KEY,"
                + "Character TEXT,"
                //+ "Boost TEXT,"
                + "Meaning TEXT"
                //+ "Description TEXT"
                + ");";

        // create radical table
        try (Statement statement = connection.createStatement()) {
            statement.execute(createRadicalTableSql);
        } catch (SQLException e) { e.printStackTrace(); }
        System.out.println("Radical Table created");
    }

    public void initializeRadicalTable(Connection connection) throws SQLException {
        //Setup radical table
        JsonRadical radicalArray[] = {};

        // parse json data
        try {
            //Read JSON file
            //Path jsonPath = Path.of("C:\\Users\\david\\projects\\RadicalFighters\\src\\main\\java\\data\\radicals.json");
            Path jsonPath = Path.of("C:\\Users\\david\\projects\\kanji_data\\radicals.json");
            System.out.println(jsonPath);
            FileInputStream fis = new FileInputStream(jsonPath.toString());
            String jsonString = IOUtils.toString(fis, "UTF-8");
            System.out.println(jsonString);
            //System.out.println(jsonString);
            Gson gson = new GsonBuilder().serializeNulls().create();
            radicalArray = gson.fromJson(jsonString, JsonRadical[].class);
            System.out.println("found " + radicalArray.length + " rads");
            for (int i = 0; i < radicalArray.length; i++) {
                JsonRadical rad = radicalArray[i];
                System.out.println(rad.getCharacter() + rad.getMeaning());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO extract these as setters
        String insertRadicalDataSql = "INSERT INTO radicals ("
                + "ID,"
                + "Character,"
                + "Meaning"
                + ") VALUES ("
                + "?,"
                + " ?,"
                + "?);";

        // insert parsed radicals into into  radicalDB
        Arrays.asList(radicalArray).stream().forEach(rad -> {
                    try (PreparedStatement statement = connection.prepareStatement(insertRadicalDataSql)) {

                        statement.setInt(1, generateRadID());
                        statement.setString(2, rad.getCharacter());

                        statement.setString(3, rad.getMeaning());

                        statement.executeUpdate();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
        );

        // to test print all rads
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM radicals;";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String character = resultSet.getString("Character");
                String meaning = resultSet.getString("Meaning");
                // ... retrieve other columns as needed

                System.out.println("Character: " + character);
                System.out.println("Meaning: " + meaning);
                // ... print other columns

                System.out.println("--------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createKanjiComponentRelationsTable(Connection connection) throws SQLException {

        // create kanji component relation table
        //TODO acually just initialize along with kanji since they hold components in json rn

        String createComponentRelationsTableSql = "CREATE TABLE IF NOT EXISTS component_relations ("
                    + "Kanji_id INTEGER,"
                    + "Radical_id INTEGER,"
                    + "FOREIGN KEY (Kanji_id) REFERENCES kanji(ID),"
                    + "FOREIGN KEY (Radical_id) REFERENCES radicals(ID)"
                    + ");";

        // create radical table
        try (Statement statement = connection.createStatement()) {
            statement.execute(createComponentRelationsTableSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Component relations Table created");
    }

    // table where we save the results of completed tasks and we build statistics from
    public void createStudyLogTable(Connection connection) throws SQLException {
        String createStudyLogTableSQL = "CREATE TABLE IF NOT EXISTS study_log ("
                + "kanji_id INTEGER, "
                + "type TEXT, " // probably enum for task type or other proficiency checks
                + "subject TEXT, " // e.g. meaning, reading etc
                + "start_time DATETIME, "
                + "finish_time DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "result BOOLEAN," // whether task was success
                + "FOREIGN KEY (kanji_id) REFERENCES kanji(ID) "
                + ");";

        // create radical table
        try (Statement statement = connection.createStatement()) {
            statement.execute(createStudyLogTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Study Log Table created");
    }

    public void appendStudyLog(int kanji_id, String type, String subject, Timestamp start_time, Timestamp finish_time, boolean result) {
        String insertStudyLogSQL = "INSERT INTO study_log ("
                + "kanji_id,"
                + "type,"
                + "subject,"
                + "start_time,"
                + "finish_time,"
                + "result"
                + ") VALUES ("
                + "?, "
                + "?, "
                + "?, "
                + "?, "
                + "?, "
                + "?);";

        try (Connection connection = SqliteHelper.getConn()) {
            try (PreparedStatement statement = connection.prepareStatement(insertStudyLogSQL)) {

                statement.setInt(1, kanji_id);
                statement.setString(2, type);
                statement.setString(3, subject);
                statement.setTimestamp(4, start_time);
                statement.setTimestamp(5, finish_time);
                statement.setBoolean(6, result);

                statement.executeUpdate();
                connection.close();
            } catch (SQLException e) { e.printStackTrace(); }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void saveStudyLog(String fileName) {
        Path outputPath = Path.of(fileName);
        File outputFile = new File(fileName);
        if (outputFile.exists()) {
            System.out.println("study log already exists at location " + fileName);
            // TODO maybe append new entries or sth instead
            return;
        }
        /*
        try {

            //Path jsonPath = Path.of("C:\\Users\\david\\projects\\RadicalFighters\\src\\main\\java\\data\\radicals.json");
            Path jsonPath = Path.of("C:\\Users\\david\\projects\\kanji_data\\radicals.json");
            System.out.println(jsonPath);
            FileInputStream fis = new FileInputStream(jsonPath.toString());
            String jsonString = IOUtils.toString(fis, "UTF-8");
            System.out.println(jsonString);
            //System.out.println(jsonString);
            Gson gson = new GsonBuilder().serializeNulls().create();
            radicalArray = gson.fromJson(jsonString, JsonRadical[].class);
            System.out.println("found " + radicalArray.length + " rads");
            for (int i = 0; i < radicalArray.length; i++) {
                JsonRadical rad = radicalArray[i];
                System.out.println(rad.getCharacter() + rad.getMeaning());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // get data and write to file
        try (Connection connection = SqliteHelper.getConn()) {
            outputFile.createNewFile();

            FileOutputStream output = new FileOutputStream(outputFile);

            try (Statement statement = connection.createStatement()) {
                String query =
                        "SELECT * FROM study_log ";
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    int id = resultSet.getInt("ID");
                    String kanji = resultSet.getString("Kanji");
                    // ... retrieve other columns as needed

                    System.out.println("ID: " + id);
                    System.out.println("Kanji: " + kanji);
                    // ... print other columns

                    System.out.println("--------------------");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static List<Kanji> dataToKanji(ResultSet resultSet) {
        List<Kanji> result = new ArrayList<Kanji>();
        try {
            while (resultSet.next()) {
                Kanji kanji = new Kanji(
                        resultSet.getInt("ID"),
                        resultSet.getString("Kanji"),
                        resultSet.getInt("Grade"),
                        resultSet.getInt("stroke_count"),
                        List.of(resultSet.getString("Kun_readings")),
                        List.of(resultSet.getString("On_readings")),
                        List.of(resultSet.getString("meanings")),
                        resultSet.getString("Unicode"),
                        resultSet.getInt("Proficiency"),
                        resultSet.getBoolean("encountered")
                );
                result.add(kanji);
                //System.out.println(kanji.getCharacter() + kanji.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
