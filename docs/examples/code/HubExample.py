import requests
import json
import tempfile

from hub.imports.geometry_factory import GeometryFactory
from hub.helpers.dictionaries import Dictionaries
from hub.imports.construction_factory import ConstructionFactory
from hub.imports.usage_factory import UsageFactory
from hub.exports.energy_building_exports_factory import EnergyBuildingsExportsFactory
from hub.imports.weather_factory import WeatherFactory


# Configuration
BASE_URL = "https://ngci.encs.concordia.ca/citydata"
USERNAME = "workshop"  # Replace with your username
PASSWORD = "workshop2026"  # Replace with your password

#Use in case of username and password fetched from the server
def authenticate(base_url, username, password):
    auth_url = f"{base_url}/authenticate"
    response = requests.get(auth_url, auth=(username, password))
    if response.status_code == 200:
        return response.text
    else:
        raise Exception(f"Authentication failed: {response.status_code} - {response.text}")

def fetch_geojson_from_middleware(base_url, token, file_path):
    sync_url = f"{base_url}/apply/sync"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    json_input = {
        "use": "ca.concordia.encs.citydata.producers.GeoJSONProducer",
        "withParams": [
            {"name": "filePath", "value": file_path}
        ],
        "apply": []
    }
    response = requests.post(sync_url, headers=headers, data=json.dumps(json_input))
    if response.status_code == 200:
        return response.json()
    else:
        raise Exception(f"Failed to fetch GeoJSON: {response.status_code} - {response.text}")


# Authenticate and fetch GeoJSON
token = authenticate(BASE_URL, USERNAME, PASSWORD)
raw_response = fetch_geojson_from_middleware(BASE_URL, token, "docs/examples/data/test_one_building.geojson")

# Unwrap the middleware envelope
if isinstance(raw_response, list):
    geojson_data = raw_response[0]
elif "result" in raw_response:
    result = raw_response["result"]
    geojson_data = result[0] if isinstance(result, list) else result
else:
    geojson_data = raw_response

# Write to a temporary file so GeometryFactory can consume it as usual
with tempfile.NamedTemporaryFile(mode='w', suffix='.geojson', delete=False) as tmp_file:
    json.dump(geojson_data, tmp_file)
    geojson_file = tmp_file.name

city = GeometryFactory('geojson',
                    geojson_file,
                    height_field='citygml_me',
                    year_of_construction_field='ANNEE_CONS',
                    function_field='CODE_UTILI',
                    function_to_hub=Dictionaries().montreal_function_to_hub_function).city

ConstructionFactory('nrcan', city).enrich()
UsageFactory('nrcan', city).enrich()
WeatherFactory('epw', city).enrich()

for building in city.buildings:
    print(building.function)
    for internal_zone in building.internal_zones:
        for usage in internal_zone.usages:
            print('Occupancy density in persons per m2:', usage.occupancy.occupancy_density)
            for schedule in usage.occupancy.occupancy_schedules:
                print(schedule.day_types, schedule.values)

# Export to IDF (EnergyPlus)
EnergyBuildingsExportsFactory('idf', city, './outputs').export()