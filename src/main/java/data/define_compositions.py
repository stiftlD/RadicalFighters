import os, sys, json
import queue
import argparse
import time
from tkinter import BooleanVar, Button, Checkbutton, IntVar, Tk, Label, Entry
from threading import Thread, Event

kanji_data_path = f"{os.getcwd()}/kanjis.json"
radical_data_path = f"{os.getcwd()}/radicals.json"

# this script goes through kanjis and radical json and writes a new file
# with ways of breaking down each kanji into kanji/radical components given by user

input_available = Event() 
relations_data = None
kanji_data = None
radical_data = None

# we will serialize these classes to json data
# basically a heisig relation for a given kanji is a list of possible decompositions which are lists of heisig components which are either kanji themselves or radicals
class HeisigComponent:
    
    def __init__(self, is_radical, character, id=None):
        self.is_radical = is_radical # whether we have to look for the component in kanji or radical data
        self.character = character

    def is_radical(self):
        return self.is_radical is True
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

# A window to display the kanji in large font for the user and take text from user (hopefully that way you can type kanji in)
class HeisigRelationsWindow(Tk):

    def __init__(self, queue, relation=None):
        Tk.__init__(self)
        self.is_radical = BooleanVar()
        self.queue = queue
        self.relation=relation # heisig relation which we are defining
        self.current_composition=None # the composition we are currently defining

        self.title(f"Define Heisig relations")
        
        user_instruction= str(
            """
            If a component of this kanji is a kanji itself, please enter that component.
            If a component of this kanji is a radical, please consult radicals.json and check [Radical] before entering its ID or the character.
            Please try not to add components multiple times within one composition.
            Do not add subcomponents to a composition.
            You can ignore primitive components that don't really have a corresponding kanji.
            """
        )
        self.instruction_label = Label(self, text=user_instruction, font=("Arial", 10))
        self.instruction_label.pack()

        self.label = Label(self, text=self.relation.get_kanji(), font=("Arial", 90))
        self.label.pack()
        
        self.components_label = Label(self, text="", font=("Gothic", 10))
        self.components_label.pack(pady=20)

        self.radical_check = Checkbutton(self, text='Radical',variable=self.is_radical, onvalue=1, offvalue=0, )
        self.radical_check.pack()

        # 2 buttons, to add compositions and to save the relation
        self.add_comp_button = Button(self, text="Add composition", command=self.add_composition)
        self.add_comp_button.pack()

        self.save_relation_button = Button(self, text="Save heisig relation for this kanji", command=self.save_relation)
        self.save_relation_button.pack()

        self.entry = Entry(self, font=("Arial", 30))
        self.entry.pack() 
        # Bind the Enter key press event to the display_character function
        self.entry.bind('<Return>', self.publish_user_input)

    def add_composition(self):
        print("adding composition")
        if self.current_composition is not None:
            print("saving composition")
            # we are currently working on a composition, complete it by adding to relation
            self.relation.add_composition(self.current_composition)
            return
        self.current_composition = HeisigComposition()
        self.display_components()
        return

    def save_relation(self):
        return

    def display_character(self):
        self.label.config(text=self.relation.get_kanji())

    def display_components(self):
        print("comp")
        self.components_label.config(text='Current components: ' +
            '\t'.join([f"{c.get_character()}; Radical : {str(c.is_radical)}" for c in self.current_composition.get_components()])                                           
        )

    # push a dict with user input to the script
    def publish_user_input(self, event=None):
        #print(str(radical_data))
        if self.current_composition is None:
            # no composition we could add this to
            return
        user_input = {}
        user_input['is_radical'] = self.is_radical.get() is True
        entry_text = self.get_input()
        user_input['character'] = entry_text
        print(user_input)
        self.entry.delete(0, len(entry_text))
        
        # use user input to search for component in data
        if not user_input['is_radical']:
            # search for component in kanji_data
            #print(str(kanji_data))
            if not [k for k in kanji_data if k['kanji'] == entry_text]:
                print(f"could not find kanji {entry_text} in kanji data, adding component anyways")
            self.current_composition.add_component(HeisigComponent(False, entry_text))
        else:
            # search for component in radical_data
            id = None
            is_id = str.isnumeric(entry_text) # search by id or character?
            if is_id:
                id = int(entry_text)
                matches =  [r for r in radical_data if r['id'] == int(entry_text)] 
                if not matches:
                    # id was not found
                    print(f"could not find radical by id {entry_text}, adding component anyways")
                else:
                    rad = matches[0]
                    
                    self.current_composition.add_component(HeisigComponent(True, rad['unicode'], id))
            else:
                matches = [r for r in radical_data if r['unicode'] == entry_text]
                if not matches:
                    # character was not found in radical_data
                    print(f"could not find radical by character {entry_text}, adding component anyways")
                else:
                    rad = matches[0]
                    self.current_composition.add_component(HeisigComponent(True, entry_text, rad['id']))

        # show the updated components
        self.display_components()


        #print(f"size in gui: {self.queue.qsize()}")
        #self.queue.put(user_input)
        #input_available.set()
        #print(f"size in gui after update: {self.queue.qsize()}")

    def get_input(self):
        return self.entry.get()
    
    def is_radical(self):
        return self.is_radical.get() is True

#def main():
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

# Create a queue for communication between threads
update_queue = queue.Queue()

# go through kanji and if we do not have its relations already we prompt the user to input one if applicable (no compositions means atomic kanji/radical)
for kanji in kanji_data:
    kanji_char = kanji['kanji']
    heisig_relation = HeisigRelation(kanji_char)
    
    print(str(kanji))
    print("--- Moving on to next kanji ---")
    print(f"{kanji_char}")
    print(f"{str(kanji['meanings'])}")
    print(f"{str(kanji['kun_readings'])}")
    print(f"{str(kanji['on_readings'])}")


    # display kanji in a new window for visibility
    relation_window = HeisigRelationsWindow(update_queue, relation=heisig_relation)
    #def gui_thread():
    #    relation_window.mainloop()
        
    relation_window_thread = Thread(target=relation_window.mainloop)#, args=(update_queue,))
    relation_window_thread.daemon = True
    relation_window_thread.start()
    
    # ask the user to add compositions as long as they see more
    # TODO give the user the option to add compositions, remove compositions, or save relations.
    while True:
        print(f"Current possible decompositions for {kanji_char}:")
        print(str(heisig_relation.compositions))

        s = ""
        while s != "y" and s != "n":
            s = str.lower(input("Do you see another and want to add it? y/n\n"))
        if s == "y":
            # TODO have the user define a new composition and then add it to the dictionary
            print(f"Creating new composition for {kanji_char}")

            composition = HeisigComposition()
            composition.add_component(HeisigComponent(False, 'B'))
            composition.add_component(HeisigComponent(True, 'A'))
            print(str(composition.get_components()))

            # ask the user to add components as long as they see more
            while True:
                user_input = None

                print("displaying")
                relation_window.display_components(composition.get_components())
                print("waiting for input")
                exit_flag = False
                print(f"size in main: {update_queue.qsize()}")
                input_available.wait()
                """
                while True:
                    try:
                        user_input = update_queue.get_nowait()
                        if user_input is not None:
                            exit_flag = True
                    except:
                        time.sleep(1)
                        pass
                print(str(user_input))
                """
                print("exited")
                #print(f"{str(user_input['character'])}")
                #print(f"{str(user_input['is_radical'])}")
                break

        elif s == "n":
            print(f"Definition of heisig relations for {kanji_char} complete.")
            relations_data.append(heisig_relation)
            break

    relation_window.destroy()

    s = input("what do you think?")
    print(s)



#main()