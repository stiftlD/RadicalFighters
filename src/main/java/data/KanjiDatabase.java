package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.kanji.Kanji;
import org.apache.commons.io.IOUtils;
import org.sqlite.SQLiteException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KanjiDatabase {

    private String rootDir = System.getProperty("user.dir"); //TODO config file for this
    private String kanjiDBURL = "jdbc:sqlite:" + Path.of(rootDir + "/database/kanji.db").toString();
    private Path kanjiPath = Path.of(rootDir + "/src/main/java/data/kanjis.json");

    //private String kanjidbURL = "jdbc:sqlite:C:/Users/david/projects/RadicalFighters/database/kanji.db";
    //private Path kanjiPath = Path.of("C:\\Users\\david\\projects\\kanji_data\\kanjiapi_full\\kanjis.json");

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

    ;

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
    }

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

        public JsonKanji(String kanji, int grade, int stroke_count, List<String> meanings,
                         List<String> kun_readings, List<String> on_readings, List<String> name_readings,
                         int jlpt, String unicode, String heisig_en) {
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

    // setup the kanji database from kanji.json if it doesn't exist
    public void initialize() throws SQLException {

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
            System.out.println("read kanjiArray");
            System.out.println("read " + kanjiArray.length + " kanji");
            /*for (int i = 0; i < kanjiArray.length; i++){
                JsonKanji kanji = kanjiArray[i];
                System.out.println(kanji.getCharacter() + kanji.getGrade());
            }*/

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Creating data base at: " + kanjiDBURL);
        File f = new File(kanjiDBURL);
        if (!f.exists() && !f.isDirectory()) {
            try (Connection connection = DriverManager.getConnection(kanjiDBURL)) {
                // create kanji table
                try (Statement statement = connection.createStatement()) {
                    statement.execute(createTableSql);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
                System.out.println("Kanji Table created");
                // insert parsed kanji into into  kanjiDB
                Arrays.asList(kanjiArray).stream().forEach(kanji -> {
                            if (kanji.getGrade() < 1 || kanji.getGrade() > 5) return; //TODO just so we have less data for now
                            try (PreparedStatement statement = connection.prepareStatement(insertDataSql)) {

                                statement.setInt(1, generateKanjiID());
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
                            } catch (SQLiteException e) {
                                e.printStackTrace();
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        }

                );


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
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }

                String createViableKanjiView =
                        "CREATE VIEW viable_kanji AS "
                                + "SELECT *, ROW_NUMBER() OVER (ORDER BY Proficiency DESC) as prof_rank FROM kanji "
                                + "WHERE Grade > 0 AND Grade <= " + 3 + " " // TODO update this based on player level
                                + "ORDER BY Proficiency DESC;";

                try (Statement statement = connection.createStatement()) {
                    // create viable Kanji view
                    try {
                        statement.execute(createViableKanjiView);
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                        System.out.println("no new view created");
                    }
                }

                connection.close();
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(kanjiDBURL + " exists.");
        }

        //Setup radical table
        /*JsonRadical radicalArray[] = {};

        // parse json data
        try
        {
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
            for (int i = 0; i < radicalArray.length; i++){
                JsonRadical rad = radicalArray[i];
                System.out.println(rad.getCharacter() + rad.getMeaning());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Setup radical table
        String createRadicalTableSql = "CREATE TABLE IF NOT EXISTS radicals ("
                //+ "ID INTEGER PRIMARY KEY,"
                //+ "Name TEXT,"
                + "ID INTEGER PRIMARY KEY,"
                + "Unicode TEXT,"
                //+ "Boost TEXT,"
                + "Meaning TEXT"
                //+ "Description TEXT"
                + ");";

        String insertRadicalDataSql = "INSERT INTO radicals ("
                + "ID,"
                + "Unicode,"
                + "Meaning"
                + ") VALUES ("
                + "?,"
                + " ?,"
                + "?);";

        String radicaldbURL = "jdbc:sqlite:C:/Users/david/projects/RadicalFighters/database/radicals.db";
        try (Connection connection = DriverManager.getConnection(radicaldbURL)) {
            // create radical table
            try (Statement statement = connection.createStatement()) {
                statement.execute(createRadicalTableSql);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
            System.out.println("Table created");
            // insert parsed radicals into into  radicalDB

            Arrays.asList(radicalArray).stream().forEach(rad -> {
                        try (PreparedStatement statement = connection.prepareStatement(insertRadicalDataSql)) {

                            statement.setInt(1, generateRadID());
                            statement.setString(2, rad.getCharacter());

                            statement.setString(3, rad.getMeaning());

                            statement.executeUpdate();
                        } catch (SQLiteException e) {
                            e.printStackTrace();
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
                    String character = resultSet.getString("Unicode");
                    String meaning = resultSet.getString("Meaning");
                    // ... retrieve other columns as needed

                    System.out.println("Character: " + character);
                    System.out.println("Meaning: " + meaning);
                    // ... print other columns

                    System.out.println("--------------------");
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }*/
    }

    public List<Kanji> getRandomKanjiInProfInterval(int maxGrade, double start, double end) throws SQLException, IllegalArgumentException {
        if (maxGrade < 0 || start < 0.0 || end > 1.0 || start > end)
            throw new IllegalArgumentException("start and end within [0, 1]");

        /*String queryNumViableKanji =
                "SELECT count() FROM kanji WHERE "
                + "Grade > 0 AND Grade <= " + maxGrade
                + ";";*/
        String queryNumViableKanji =
                "SELECT count() from viable_kanji;";

        String selectViableKanjiString =
                "SELECT * FROM kanji "
                        + "WHERE Grade > 0 AND Grade <= " + maxGrade + " "
                        + "ORDER BY Proficiency DESC";

        try (Connection connection = DriverManager.getConnection(kanjiDBURL)) {
            try (Statement statement = connection.createStatement()) {

                ResultSet countResult = statement.executeQuery(queryNumViableKanji);
                int count = countResult.getInt("count()");
                int startIndex = (int) (count * start);
                int stopIndex = (int) (count * end);

                //System.out.println("count: " + count + ", startI: " + startIndex + ", stopI: " + stopIndex);

                double randIndex = Math.random() * (stopIndex - startIndex);
                int resultIndex = startIndex + (int) randIndex;
                System.out.println("random was " + randIndex + ", result is " + resultIndex + " out of " + count);

                String selectKanjiInProfInterval =
                        "SELECT * FROM viable_kanji "
                                + "WHERE prof_rank == " + resultIndex;

                ResultSet resultSet = statement.executeQuery(selectKanjiInProfInterval);
                System.out.println(resultSet);
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
            } catch (SQLiteException e) {
                e.printStackTrace();

                //return null;
            }

            connection.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }


        return null;
    }

    private List<Kanji> dataToKanji(ResultSet resultSet) throws SQLException {
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
                System.out.println(kanji.getCharacter() + kanji.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
