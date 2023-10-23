import os, sys, json
import argparse

kanji_data_path = f"{os.getcwd()}/kanjis.json"
radical_data_path = f"{os.getcwd()}/radicals.json"

# this script goes through kanjis and radical json and writes a new file
# with ways of breaking down each kanji into kanji/radical components given by user

parser = argparse.ArgumentParser(
                    prog='define_compositions',
                    description='define components of kanji')

parser.add_argument('filename', help="file to write to")

args = parser.parse_args()

heisig_composition_data_path = f"{os.getcwd()}/{args.filename}"
print(heisig_composition_data_path)

if not os.path.exists(kanji_data_path):
    print(f"no such file as {kanji_data_path}")

#if not os.path.exists(heisig_composition_data_path):
#    print(f"no such file as {heisig_composition_data_path}")

compositions_data = None
kanji_data = None
radical_data = None

with open(heisig_composition_data_path, "r") as compositions_file:
    compositions_data = compositions_file.read()
with open(kanji_data_path, "r") as kanji_file:
    kanji_data = kanji_file.read()
with open(radical_data_path, "r") as radical_file:
    radical_data = radical_file.read()
