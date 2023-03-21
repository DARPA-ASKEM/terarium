
const output = {
    init: `
import pandas as pd
import numpy as np
import xarray as xr

DATA_SERVICE_URL=f"http://{os.environ.get('data-service-host', 'localhost')}:{os.environ.get('data-service-port', '3020')}'

def fetch_dataframe(dataset_id):
    import os
    import requests




    `,
    read_csv: `
df = pd.read_csv({filename})
    `,
}
    


export default output;