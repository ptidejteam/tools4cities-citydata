import requests
from requests.auth import HTTPBasicAuth
#from lxml import etree
import xml.etree.ElementTree as ET
import pandas as pd
import json

# ==========================
# USER CONFIGURATION
# ==========================
# Configuration
BASE_URL = "https://ngci.encs.concordia.ca/citydata"
USERNAME = "workshop"  # Production username
PASSWORD = "workshop2026"  # Production password
#BASE_URL = "http://localhost:8080/citydata"
#USERNAME = "temp"  # Local username
#PASSWORD = "temp"  # Local password
 
METERS = {
    "electricity": "299278275",
    "natural_gas": "279802875"
}            

# Authentication
def authenticate(base_url, username, password):
    auth_url = f"{base_url}/authenticate"
    response = requests.get(auth_url, auth=(username, password))
    if response.status_code == 200:
        return response.text
    else:
        raise Exception(f"Authentication failed: {response.status_code} - {response.text}")


# Function to fetch data
def fetch_consumption(base_url, token, meter_id):
    """
    Calls PortfolioManagerProducer via CITYdata.
    CITYdata manages PM credentials and connection internally.
    Only meterId is needed from the client.
    """
    sync_url = f"{base_url}/apply/sync"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    json_input = {
        "use": "ca.concordia.encs.citydata.producers.PortfolioManagerProducer",
        "withParams": [
            {"name": "meterId", "value": meter_id}
        ],
        "apply": []
    }
    response = requests.post(sync_url, headers=headers, data=json.dumps(json_input))
    if response.status_code == 200:
        return response.json()
    else:
        raise Exception(f"Failed to fetch meter {meter_id}: {response.status_code} - {response.text}")


# Raw result
def unwrap(raw_response):
    if isinstance(raw_response, list):
        return raw_response[0]
    elif "result" in raw_response:
        result = raw_response["result"]
        return result[0] if isinstance(result, list) else result
    return raw_response

# Processing data using an operation
def xml_to_flat_json(label, meter_id, xml_string):
    """Parse XML and return a list of flat JSON-ready dicts."""
    root = ET.fromstring(xml_string)
    records = []
    for entry in root.findall("meterConsumption"):
        records.append({
            "type":      label,
            "meterId":   meter_id,
            "startDate": entry.findtext("startDate"),
            "endDate":   entry.findtext("endDate"),
            "usage":     str(entry.findtext("usage"))
        })
    return records

def save_to_json(flat_records, filename="consumption.json"):
    with open(filename, "w") as f:
        json.dump(flat_records, f, indent=2)
    print(f"Saved {len(flat_records)} records to {filename}")


token = authenticate(BASE_URL, USERNAME, PASSWORD)
print("Authenticated with CITYdata")

# Fetch and process each meter sequentially - printing as a Json output
all_records = []  # flat list of all records across all meters

for label, meter_id in METERS.items():
    print(f"Fetching {label} consumption (meter {meter_id})...")

    raw_response = fetch_consumption(BASE_URL, token, meter_id)
    data = unwrap(raw_response)

    if isinstance(data, dict) and "xml" in data:
        records = xml_to_flat_json(label, meter_id, data["xml"])
        all_records.extend(records)

print(json.dumps(all_records, indent=2))
save_to_json(all_records)
