import requests
import json

# Configuration
BASE_URL = "http://localhost:8084" # Replace with actual server address
USERNAME = "Your_Username"  # Replace with actual username
PASSWORD = "Your_Password"  # Replace with actual password

def authenticate(base_url, username, password):

    print("Step 1: Authenticating...")
    
    auth_url = f"{base_url}/authenticate"
    
    # Using Basic Authentication
    response = requests.get(
        auth_url,
        auth=(username, password)
    )
    
    if response.status_code == 200:
        token = response.text
        print(f"Authentication successful!")
        print(f"Token: {token[:50]}...")  # Print first 50 chars
        return token
    else:
        print(f"Authentication failed: {response.status_code}")
        print(f"Response: {response.text}")
        return None

def apply_sync_operation(base_url, token, json_input):

    print("\nStep 3: Executing sync operation...")
    
    sync_url = f"{base_url}/apply/sync"
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    response = requests.post(
        sync_url,
        headers=headers,
        data=json.dumps(json_input)
    )
    
    if response.status_code == 200:
        print("Operation completed successfully!")
        return response.text
    else:
        print(f"Operation failed: {response.status_code}")
        print(f"Response: {response.text}")
        return None

def main():

    print("=" * 60)
    print("Energy Consumption Data Processing Example")
    print("=" * 60)
    
    # Authenticate and get token
    token = authenticate(BASE_URL, USERNAME, PASSWORD)
    
    if not token:
        print("\nAuthentication failed. Exiting.")
        return
    
    # Define JSON input
    print("\nStep 2: Token copied for next request")
    print("\nStep 4: Preparing JSON input...")
    
    json_input = {
        "use": "ca.concordia.encs.citydata.producers.EnergyConsumptionProducer",
        "withParams": [
            {
                "name": "city",
                "value": "montreal"
            },
            {
                "name": "startDatetime",
                "value": "2021-09-01 00:00:00"
            },
            {
                "name": "endDatetime",
                "value": "2021-09-01 23:59:00"
            },
            {
                "name": "clientId",
                "value": 1
            }
        ],
        "apply": [
            {
                "name": "ca.concordia.encs.citydata.operations.JsonArrayAverageOperation",
                "withParams": [
                    {
                        "name": "keyName",
                        "value": "consumptionKwh"
                    },
                    {
                        "name": "roundingMethod",
                        "value": "none"
                    }
                ]
            }
        ]
    }
    
    print("JSON input prepared")
    print(f"Producer: {json_input['use']}")
    print(f"City: {json_input['withParams'][0]['value']}")
    print(f"Date Range: {json_input['withParams'][1]['value']} to {json_input['withParams'][2]['value']}")
    
    # Execute operation and print result
    result = apply_sync_operation(BASE_URL, token, json_input)
    
    if result:
        print("\n" + "=" * 60)
        print("Step 5: Result")
        print("=" * 60)
        print(result)
        print("=" * 60)
    else:
        print("\nOperation failed. Please check the error messages above.")

if __name__ == "__main__":
    main()