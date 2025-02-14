from entities import ChartAnnotationType

CHART_ANNOTATION_PROMPT_BASE = """
You are an agent who is an expert in Vega-Lite chart specs. Provide a Vega-Lite layer JSON object for the annotation that can be added to an existing chart spec to satisfy the provided user request.

- The Vega-Lite schema version you must use is https://vega.github.io/schema/vega-lite/v5.json.
- Assume that you don’t know the exact data points from the data.
- You must give me the single layer object that renders all the necessary drawing objects, including multiple layers within the top layer object if needed.
- When adding a label, also draw a constant line perpendicular to the axis to which you are adding the label.

Here are some examples of user requests and the corresponding JSON objects that you should provide for a chart spec:
--- EXAMPLES START ---
{examples}
--- EXAMPLES END ---

{preamble}

Give me the layer object described by the json schema found at https://vega.github.io/schema/vega-lite/v5.json to be added to the existing chart spec based on the following user instruction.
Do not respond in full sentences; only create a JSON object that satisfies the JSON schema specified in https://vega.github.io/schema/vega-lite/v5.json. Make sure you follow the JSON standard for the data types and format.

--- INSTRUCTION START ---

{instruction}

--- INSTRUCTION END ---
"""

FORECAST_CHART_ANNOTATION_EXAMPLES = """
Assuming you are adding the annotations to the following chart spec,
---- Example Chart Spec Start -----
{{
    "data": {{ "url": "data/samples.csv" }},
    "transform": [
        {{ "fold": ["price", "cost", "tax", "profit"], "as": ["variableField", "valueField"] }}
    ],
    "layer": [
        {{
            "mark": "line",
            "encoding": {{
                "x": {{ "field": "date", "type": "quantitative", "axis": {{ "title": "Day" }} }},
                "y": {{ "field": "valueField", "type": "quantitative", "axis": {{ "title": "Dollars" }} }}
            }}
        }}
    ]
}}
---- Example Chart Spec End -----
Here are some example requests and the answers:

Request:
At day 200, add a label 'important'
Answer:
{{
  "description": "At day 200, add a label 'important'",
  "layer": [
    {{
      "mark": {{
        "type": "rule",
        "strokeDash": [4, 4]
      }},
      "encoding": {{
        "x": {{ "datum": 200, "axis": {{ "title": ""}} }}
      }}
    }},
    {{
      "mark": {{
        "type": "text",
        "align": "left",
        "dx": 5,
        "dy": -5
      }},
      "encoding": {{
        "x": {{ "datum": 200, "axis": {{ "title": ""}} }}
        "text": {{"value": "important"}}
      }}
    }}
  ]
}}

Request:
Add a label 'expensive' at price 20
Answer:
{{
  "description": "Add a label 'expensive' at price 20",
  "layer": [
    {{
      "mark": {{
        "type": "rule",
        "strokeDash": [4, 4]
      }},
      "encoding": {{
        "y": {{ "datum": 20, "axis": {{ "title": ""}} }}
      }}
    }},
    {{
      "mark": {{
        "type": "text",
        "align": "left",
        "dx": 5,
        "dy": -5
      }},
      "encoding": {{
        "y": {{ "datum": 20, "axis": {{ "title": ""}} }},
        "text": {{"value": "expensive"}}
      }}
    }}
  ]
}}

Request:
Add a vertical line for the day where the price exceeds 100.
Answer:
{{
  "description": "Add a vertical line for the day where the price exceeds 100.",
  "transform": [
    {{"filter": "datum.valueField > 100"}},
    {{"aggregate": [{{"op": "min", "field": "date", "as": "min_date"}}]}}
  ],
  "layer": [
    {{
      "mark": {{
        "type": "rule",
        "strokeDash": [4, 4]
      }},
      "encoding": {{
        "x": {{"field": "min_date", "type": "quantitative", "axis": {{ "title": ""}}}}
      }}
    }},
    {{
      "mark": {{
        "type": "text",
        "align": "left",
        "dx": 5,
        "dy": -10
      }},
      "encoding": {{
        "x": {{"field": "min_date", "type": "quantitative", "axis": {{ "title": ""}}}},
        "text": {{"value": "Price > 100"}}
      }}
    }}
  ]
}}

Request:
Add a vertical line at the day where the price reaches its peak value.
Answer:
{{
  "description": "Add a vertical line at the day where the price reaches its peak value.",
  "transform": [
    {{"filter": "datum.variableField == 'price'"}},
    {{
      "joinaggregate": [{{
      "op": "max",
      "field": "valueField",
      "as": "max_price"
      }}]
    }},
    {{"filter": "datum.valueField >= datum.max_price"}}
  ],
  "layer": [
    {{
      "mark": {{
        "type": "rule",
        "strokeDash": [4, 4]
      }},
      "encoding": {{
        "x": {{"field": "date", "type": "quantitative", "axis": {{ "title": ""}}}}
      }}
    }},
    {{
      "mark": {{
        "type": "text",
        "align": "left",
        "dx": 5,
        "dy": -10
      }},
      "encoding": {{
        "x": {{"field": "date", "type": "quantitative", "axis": {{ "title": ""}}}},
        "text": {{"value": "Max Price"}}
      }}
    }}
  ]
}}
"""

QUANTILE_FORECAST_CHART_ANNOTATION_EXAMPLES="""
Assuming you are adding the annotations to the following chart spec for quantile confidence intervals,
---- Example Chart Spec Start -----
{{
    "data": {{ "url": "data/samples.csv" }},
    "layer": [
        {{
            "mark": "errorband",
            "encoding": {{
                "x": {{ "field": "x", "type": "quantitative", "axis": {{ "title": "Day" }} }},
                "y": {{ "field": "upper", "type": "quantitative", "axis": {{ "title": "Dollars" }} }}
                "y2": {{ "field": "lower", "type": "quantitative", "axis": {{ "title": "Dollars" }} }}
                "color": {{ "field": "variable", "type": "nominal" }}
                "opacity": {{ "field": "quantile", "type": "quantitative" }}
            }}
        }}
    ]
}}
---- Example Chart Spec End -----
And here are some information about the data for the example chart spec:
- data is records with following columns: x, upper, lower, variable, quantile
- When not instructed otherwise, assume that quantile is 0.5 (median) and upper and lower are the same value.
- Possible values for quantile are from 0.5 to 0.99
- Possible values for variable are price, cost, tax, profit

Here are some example requests and the answers:

Request:
At day 200, add a label 'important'
Answer:
{{
  "description": "At day 200, add a label 'important'",
  "layer": [
    {{
      "mark": {{
        "type": "rule",
        "strokeDash": [4, 4]
      }},
      "encoding": {{
        "x": {{ "datum": 200, "axis": {{ "title": ""}} }}
      }}
    }},
    {{
      "mark": {{
        "type": "text",
        "align": "left",
        "dx": 5,
        "dy": -5
      }},
      "encoding": {{
        "x": {{ "datum": 200, "axis": {{ "title": ""}} }}
        "text": {{"value": "important"}}
      }}
    }}
  ]
}}

Request:
Add a label 'expensive' at price 20
Answer:
{{
  "description": "Add a label 'expensive' at price 20",
  "layer": [
    {{
      "mark": {{
        "type": "rule",
        "strokeDash": [4, 4]
      }},
      "encoding": {{
        "y": {{ "datum": 20, "axis": {{ "title": ""}} }}
      }}
    }},
    {{
      "mark": {{
        "type": "text",
        "align": "left",
        "dx": 5,
        "dy": -5
      }},
      "encoding": {{
        "y": {{ "datum": 20, "axis": {{ "title": ""}} }},
        "text": {{"value": "expensive"}}
      }}
    }}
  ]
}}
"""

def build_prompt(chartType: ChartAnnotationType, preamble: str, instruction: str) -> str:
    examples = {
        ChartAnnotationType.FORECAST_CHART: FORECAST_CHART_ANNOTATION_EXAMPLES,
        ChartAnnotationType.QUANTILE_FORECAST_CHART: QUANTILE_FORECAST_CHART_ANNOTATION_EXAMPLES
    }[chartType]
    prompt = CHART_ANNOTATION_PROMPT_BASE.format(examples=examples, preamble=preamble, instruction=instruction)
    print(prompt)
    return prompt
