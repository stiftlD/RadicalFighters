import os, sys, json
import argparse
from tkinter import Tk, Label
from threading import Thread

kanji_data_path = f"{os.getcwd()}/kanjis.json"
radical_data_path = f"{os.getcwd()}/radicals.json"

# this script goes through kanjis and radical json and writes a new file
# with ways of breaking down each kanji into kanji/radical components given by user

def main():
    parser = argparse.ArgumentParser(
                        prog='define_compositions',
                        description='define components of kanji')

    parser.add_argument('filename', help="file to write to", default="heisig_relations.json", nargs='?') # filename is optional

    args = parser.parse_args()

    heisig_relation_data_path = f"{os.getcwd()}/{args.filename}"
    print(heisig_relation_data_path)

    if not os.path.exists(heisig_relation_data_path):
        print(f"no such file as {heisig_relation_data_path}, creating anew")
        json.dump([], open(heisig_relation_data_path, 'w'))

    #if not os.path.exists(heisig_composition_data_path):
    #    print(f"no such file as {heisig_composition_data_path}")

    relations_data = None
    kanji_data = None
    radical_data = None

    print("reading data")
    with open(heisig_relation_data_path, "r") as compositions_file:
        relations_data = json.load(compositions_file)
        #print("relation data: " + str(relations_data))
    with open(kanji_data_path, "r", encoding='utf-8') as kanji_file:
        kanji_data = json.load(kanji_file)
        # discard kanji we already have relations for
        kanji_data = [kanji for kanji in kanji_data if not [r for r in relations_data if r['kanji'] == kanji['kanji']]]

        # for now we only use the kanji from jlpt 5,4,3
        kanji_data = [kanji for kanji in kanji_data if kanji['jlpt'] is not None and kanji['jlpt'] > 2]
        #print("kanji data:" + str(kanji_data))
    with open(radical_data_path, "r") as radical_file:
        radical_data = json.load(radical_file)
        #print("radical data:" + str(radical_data))

    # go through kanji and if we do not have its relations already we prompt the user to input one if applicable (no compositions means atomic kanji/radical)
    for kanji in kanji_data:
        kanji_char = kanji['kanji']
        heisig_relation = HeisigRelation(kanji_char)
        
        print(str(kanji))
        print("--- Moving on to next kanji ---")
        print(f"{kanji_char}")
        print(f"{str(kanji['kun_readings'])}")
        print(f"{str(kanji['on_readings'])}")

        # display kanji in a new window for visibility
        kanji_window_thread = Thread(target=display_character, args=(kanji_char,))
        kanji_window_thread.daemon = True  # This allows the program to exit even if the GUI thread is still running
        kanji_window_thread.start()

        # ask the user to add compositions as long as they see more
        while True:
            print(f"Current possible decompositions for {kanji_char}:")
            print(str(heisig_relation.compositions))

            s = ""
            while str.lower(s) != "y" and str.lower(s) != "n":
                s = input("Do you see another and want to add it? y/n\n")
            break


        s = input("what do you think?")
        print(s)

# we will serialize these classes to json data
# basically a heisig relation for a given kanji is a list of possible decompositions which are lists of heisig components which are either kanji themselves or radicals
class HeisigComponent:
    
    def __init__(self, is_radical, character):
        self.is_readical = is_radical # whether we have to look for the component in kanji or radical data
        self.character = character

    def is_radical(self):
        return self.is_radical
    def get_character(self):
        return self.character
    
class HeisigComposition:

    def __init__(self):
        self.components = [] # the heisig components involved in this composition

    def get_components(self):
        return self.components
    
    def add_component(self, comp):
        self.components.append(comp)


class HeisigRelation:

    def __init__(self, kanji, kanji_id = None):
        self.kanji = kanji # the kanji character
        self.kanji_id = kanji_id # id if we use one
        self.compositions = [] # list of possible heisig compositions

    def get_kanji(self):
        return self.kanji
    def get_kanji_id(self):
        return self.kanji_id
    def get_compositions(self):
        return self.compositions
    
    def add_composition(self, comp):
        self.compositions.append(comp)


# open a window and write the kanji in large font for the user
def display_character(character):
    window = Tk()
    label = Label(window, text=character, font=("Arial", 90))
    label.pack()
    window.mainloop()

main()