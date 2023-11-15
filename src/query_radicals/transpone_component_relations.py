import sys
import os
import json

relation_json_path = "./component_relations.json"
output_path = "./output.json"
component_relations = {}

def customRadicalDecoder(dict):
    if dict is None:
        print("None")
        return None
    print(dict)
    return dict['unicode']

component_relations_file =open(relation_json_path, 'r', encoding='utf-8')
component_relations = json.load(component_relations_file)
# input is radical -> kanji, we write it as kanji -> radicals for use in the database
kanji_to_radical_map = {}
for radical in component_relations:
    #print(radical)
    kanjis = component_relations[radical]
    #print(kanjis)
    for kanji in kanjis:
        if not kanji_to_radical_map.get(kanji):
            kanji_to_radical_map[kanji] = [radical]
        else:
            kanji_to_radical_map[kanji].append(radical)
        print(kanji_to_radical_map)
     #   print(kanji_to_radical_map[kanji])
      #  if kanji_to_radical_map[kanji] is None:
     #       kanji_to_radical_map[kanji] = [radical]
    #    else:
   #         kanji_to_radical_map[kanji].append(radical)

#print("done")
#print(kanji_to_radical_map)
output_file = open(output_path, "w")
json.dump(kanji_to_radical_map, output_file)
output_file.close()
print("wrote kanji->radical data to " + output_path)