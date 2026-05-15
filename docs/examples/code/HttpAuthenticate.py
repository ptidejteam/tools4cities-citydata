import requests
import json

url = "https://ngci.encs.concordia.ca/citydata/authenticate"

payload = json.dumps({
  "username": "citydata",
  "password": "citydata"
})
headers = {
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=payload)
token = response.text

print(token)