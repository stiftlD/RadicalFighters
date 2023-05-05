package model.kanji;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KanjiDex {
   /* private List<model.kanji.Kanji> kanjiList;

    public model.kanji.KanjiDex() {
        //Gson gson = new GsonBuilder().registerTypeAdapter(model.kanji.Kanji.class, new CustomKanjiDecoder()).create();

        try (FileReader reader = new FileReader("kanji.json")) {
            //model.kanji.Kanji[] kanjiArray = gson.fromJson(reader, model.kanji.Kanji[].class);
            kanjiList = new ArrayList<>(List.of(kanjiArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public model.kanji.Kanji getRandomKanji() {
        Random rand = new Random();
        return kanjiList.get(rand.nextInt(kanjiList.size()));
    }

    public List<model.kanji.Kanji> getKanji() {
        return kanjiList;
    }*/
}
