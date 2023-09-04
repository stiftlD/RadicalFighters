import requests
from urllib.parse import urlencode, quote
import re
import subprocess
import json
import pandas as pd
import numpy as np


//TODO apparently we are writing every radical twice :)

"""
headers = {}
with open('headers.txt', 'r') as file:
    for line in file:
        line = line.strip('\t')
        if ':' in line:
            key, value = line.split(':', 1)
            headers[key.strip()] = value.strip()

#print(headers)


cookies = {}
with open('cookies.txt', 'r') as file:
    i = 0
    for line in file:
        print(str(i))
        i += 1
        #print(line)
        #line = line.strip()
        print(line)
        #if line.startswith("# ") and not line.startswith("#H"):#or line.startswith("Cookie"):
        #    continue
        #print(line)
        parts = line.split()
        #print("parts:\n", parts)
        #parts = line.split(r'\s+')
        #parts = [s for s in line if s not in (re.findall(r'[\s+]+', line))]
        #print(parts)
        cookies[parts[5]] = parts[6]

print(cookies)


"""
#headers = {'User-Agent': 'curl/7.68.0'}  # Replace with the User-Agent used by curl

# Define the URL and parameters for the request

base_url = 'https://www.kanshudo.com/searchcg'

#url = 'https://www.kanshudo.com/kanji/'

def customRadicalDecoder(dict):
    if dict is None or not 'unicode' in dict:
        return None
    return dict['unicode']

#radicalfile =open("C:\\Users\\David Stiftl\\projects\\RadicalFighters\\src\\radicals.json", 'r')
##radicals = json.load(radicalfile, object_hook = customRadicalDecoder)
radicalfile =open("C:\\Users\\David Stiftl\\projects\\query_radicals\\radicals.txt", 'r', encoding='utf-8')
radicals = radicalfile.readlines() #json.load(radicalfile, object_hook = customRadicalDecoder)



responsefile =open("C:\\Users\\David Stiftl\\projects\\query_radicals\\response.html", 'w', encoding='utf-8')



component_relations = {}

params = {}
for radical in radicals:
    params['q'] = radical

    # Manually encode the parameters
    encoded_params = urlencode(params, safe='')
    # {k: quote(v, safe='') for k, v in params.items()}, safe='')



    # Combine the base URL and encoded parameters

    url = f'{base_url}?{encoded_params}'

    print(url)



    kanjis = []

    # Define the curl command with necessary options and URL
    curl_command = ['curl', "--insecure", '-X', 'GET', url]


    # Execute the curl command and capture the output
    process = subprocess.Popen(curl_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    # Read the response in a streaming manner
    while True:
        # Read a line from the output
        line = process.stdout.readline()
        #print(line)
        #print(line.decode())
        if not line:
            # Check if the stream has ended
            if process.poll() is not None:
                break
        else:
            # Process the response line by line
            response_line = line.decode().strip()
            parts = re.findall(r"this\);.*<", response_line)
            if len(parts) > 0:
                #print(parts)
                for s in parts:
                    kanjis.append(parts[0][:-1][-1:])
                    #kanjis.append(parts[0])
            # Add filtering or data extraction logic here
            #print(response_line)
            responsefile.write(url)
            responsefile.write(response_line)

    url = url[0:-3]

    print(url)

    # Define the curl command with necessary options and URL
    curl_command = ['curl', "--insecure", '-X', 'GET', url]

     # Execute the curl command and capture the output
    process = subprocess.Popen(curl_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    

    # Read the response in a streaming manner
    while True:
        # Read a line from the output
        line = process.stdout.readline()
        #print(line)
        #print(line.decode())
        if not line:
            # Check if the stream has ended
            if process.poll() is not None:
                break
        else:
            # Process the response line by line
            response_line = line.decode().strip()
            parts = re.findall(r"this\);.*<", response_line)
            if len(parts) > 0:
                #print(parts)
                for s in parts:
                    kanjis.append(parts[0][:-1][-1:])

                    

                    

                    #kanjis.append(parts[0])

            # Add filtering or data extraction logic here
            #print(response_line)
            responsefile.write(url)
            responsefile.write(response_line)

    # Wait for the process to finish
    process.wait()

    component_relations[''.join(radical.split())] = kanjis

    component_relations_file =open("C:\\Users\\David Stiftl\\projects\\query_radicals\\component_relations.json", 'w', encoding='utf-8')

    json.dump(component_relations, component_relations_file)

    component_relations_file.close()
    
    component_relations_file =open("C:\\Users\\David Stiftl\\projects\\query_radicals\\component_relations.json", 'r', encoding='utf-8')


    print(component_relations_file.read())
    component_relations_file.close()

    print(kanjis)

    component_relations[radical] = kanjis

    # Get the return code of the curl command
    return_code = process.returncode
    if return_code != 0:
        # Handle any errors or failures
        stderr_output = process.stderr.read().strip()
        print(f'Error occurred: {stderr_output}')



