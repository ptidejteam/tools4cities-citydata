import requests
from requests.auth import HTTPBasicAuth
import xml.etree.ElementTree as ET
import pandas as pd
import json

# ==========================
# USER CONFIGURATION
# ==========================
# Configuration
BASE_URL = "https://ngci.encs.concordia.ca/citydata"
USERNAME = username  # Production username created by CITYdata team
PASSWORD = password  # Production password created by CITYdata team
# BASE_URL = "http://localhost:8080/citydata"
# USERNAME = username  # Local username created with the script (cf. READMe)
# PASSWORD = password  # Local password created with the script (cf. READMe)
           


# Authentication
def authenticate(base_url, username, password):
    auth_url = f"{base_url}/authenticate"
    response = requests.get(auth_url, auth=(username, password))
    if response.status_code == 200:
        return response.text
    else:
        raise Exception(f"Authentication failed: {response.status_code} - {response.text}")

def print_result(response):
    if isinstance(response, list) and len(response) > 0:
        response = response[0]

    raw_data = response.get("result", "")
    raw_data = raw_data.strip("[]")

    parts =raw_data.split(", ")

    print("\nResults:\n")
    for i in range(0, len(parts), 3):
        chunk = parts[i:i+3]
        if len(chunk) == 3:
            address, date, value = chunk
            print(f"Address: {address}")
            print(f"Date: {date}")
            print(f"Value: {value}")
            print("-" * 30)


    
# Function to fetch data
def fetch_consumption(base_url, token):
    sync_url = f"{base_url}/apply/sync"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }

    # json_input = {
    #     "use": "ca.concordia.encs.citydata.producers.CalibrationProducer",
    #     "withParams": [
    #         { "name": "filePath", "value": "src/main/resources/Data/OMHM_natural_gas.csv" }
    #     ],
    #     "apply": [
    #         {
    #             "name": "ca.concordia.encs.citydata.operations.CSVFilterOperation",
    #             "withParams": [
    #                 { "name": "addressFilter", "value": "H1W3J9" },
    #                 { "name": "dateFilter",    "value": "2022-04-30" }
    #             ]
    #         }
    #     ]
    # }

    json_input = {
        "use": "ca.concordia.encs.citydata.producers.CalibrationProducer",
        "withParams": [
            { "name": "filePath", "value": "src/test/resources/OMHM_electricity.csv" }
        ],
        "apply": [
            {
                "name": "ca.concordia.encs.citydata.operations.CSVFilterOperation",
                "withParams": [
                    { "name": "addressFilter", "value": "H1Z 0B3" },
                    { "name": "dateFilter",    "value": "2022-02-28" }
                ]
            }
        ]
    }

    response = requests.post(sync_url, headers=headers, data=json.dumps(json_input))

    if response.status_code == 200:
        return response.json()
    else:
        raise Exception(f"Request failed: {response.status_code} - {response.text}")



# First, authenticate against CITYdata and then print fetched data
token = authenticate(BASE_URL, USERNAME, PASSWORD)
print("Authenticated with CITYdata")

response = fetch_consumption(BASE_URL, token)

print("\nRAW DATA:\n", json.dumps(response, indent=2))

print_result(response)
