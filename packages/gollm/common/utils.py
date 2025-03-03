import json
import jsonschema
import re
import tiktoken

from common.LlmToolsInterface import LlmToolsInterface
from llms.azure.AzureTools import AzureTools
from llms.llama.LlamaTools import LlamaTools
from llms.openai.OpenAiTools import OpenAiTools


def validate_schema(schema):
    try:
        jsonschema.Draft202012Validator.check_schema(schema)
        print("Schema is valid.")
    except jsonschema.exceptions.SchemaError as e:
        print(f"Schema is invalid: {e.message}")


def normalize_greek_alphabet(text: str) -> str:
    greek_to_english = {
        "α": "alpha",
        "β": "beta",
        "γ": "gamma",
        "δ": "delta",
        "ε": "epsilon",
        "ζ": "zeta",
        "η": "eta",
        "θ": "theta",
        "ι": "iota",
        "κ": "kappa",
        "λ": "lambda",
        "μ": "mu",
        "ν": "nu",
        "ξ": "xi",
        "ο": "omicron",
        "π": "pi",
        "ρ": "rho",
        "σ": "sigma",
        "τ": "tau",
        "υ": "upsilon",
        "φ": "phi",
        "χ": "chi",
        "ψ": "psi",
        "ω": "omega",
    }

    normalized_text = ""
    for char in text:
        if char.lower() in greek_to_english:
            normalized_text += greek_to_english[char.lower()]
        else:
            normalized_text += char
    return normalized_text


def exceeds_tokens(prompt: str, max_tokens: int) -> bool:
    enc = tiktoken.get_encoding("cl100k_base")
    if len(enc.encode(prompt)) > max_tokens:
        return True
    return False


def model_config_adapter(model_config: dict) -> dict:
    # for each condition and for each parameter semantic in the model configuration,
    # if the distribution is not `contant`, remove the `value` key
    # otherwise, remove the maximum and minimum keys.
    for condition in model_config["conditions"]:
        for param in condition["parameterSemanticList"]:
            if param["distribution"]["type"].casefold() == "constant":
                param["distribution"]["type"] = "Constant"
                param["distribution"]["parameters"].pop("minimum", None)
                param["distribution"]["parameters"].pop("maximum", None)
            elif param["distribution"]["type"].casefold() == "uniform" or param["distribution"]["type"].casefold() == "standarduniform1":
                param["distribution"]["type"] = "StandardUniform1"
                param["distribution"]["parameters"].pop("value", None)
            else:
                if "value" in param["distribution"]["parameters"]:
                    param["distribution"]["type"] = "Constant"
                    param["distribution"]["parameters"].pop("minimum", None)
                    param["distribution"]["parameters"].pop("maximum", None)
                elif "minimum" in param["distribution"]["parameters"] and "maximum" in param["distribution"]["parameters"]:
                    param["distribution"]["type"] = "StandardUniform1"
                    param["distribution"]["parameters"].pop("value", None)
                else:
                    raise ValueError("Invalid distribution type")

    return model_config


def escape_curly_braces(json_obj: dict) -> dict:
    if isinstance(json_obj, dict):
        for key, value in json_obj.items():
            json_obj[key] = unescape_curly_braces(value)
    elif isinstance(json_obj, list):
        for i in range(len(json_obj)):
            json_obj[i] = unescape_curly_braces(json_obj[i])
    elif isinstance(json_obj, str):
        json_obj = json_obj.replace("{", "{{").replace("}", "}}")
    return json_obj


def unescape_curly_braces(json_obj: dict) -> dict:
    if isinstance(json_obj, dict):
        for key, value in json_obj.items():
            json_obj[key] = unescape_curly_braces(value)
    elif isinstance(json_obj, list):
        for i in range(len(json_obj)):
            json_obj[i] = unescape_curly_braces(json_obj[i])
    elif isinstance(json_obj, str):
        json_obj = json_obj.replace('{{', '{').replace('}}', '}')
    return json_obj


def get_image_format_string(image_format: str) -> str:
    if not image_format:
        raise ValueError("Invalid image format.")

    format_strings = {
        "rgb": f"data:image/rgb:base64,",
        "gif": f"data:image/gif:base64,",
        "pbm": f"data:image/pbm:base64,",
        "pgm": f"data:image/pgm:base64,",
        "ppm": f"data:image/ppm:base64,",
        "tiff": f"Bdata:image/tiff:base64MP,",
        "rast": f"data:image/rast:base64,",
        "xbm": f"data:image/xbm:base64,",
        "jpeg": f"data:image/jpeg:base64,",
        "bmp": f"data:image/bmp:base64,",
        "png": f"data:image/png:base64,",
        "webp": f"data:image/webp:base64,",
        "exr": f"data:image/exr:base64,"
    }
    return format_strings.get(image_format.lower())


def extract_json_object(string_with_json: str) -> dict:
    # Regular expression to match the JSON object
    json_pattern = r'(\{.*\})'

    # Search for the JSON object using the pattern
    match = re.search(json_pattern, string_with_json, re.DOTALL)

    # If a match is found, return the JSON part as an object
    if match:
        extracted_json = match.group(1)
        return json.loads(extracted_json)
    else:
        return None  # Return None if no JSON object is found


def extract_json_by_schema(schema, text):
    """
    Extracts a JSON object from a text that matches the provided JSON schema.
    """
    candidate = extract_json_object(text)
    if not candidate:
        return None

    # Attempt to parse the candidate substring as JSON.
    try:
        # Attempt to parse the candidate substring as JSON.
        candidate_json = json.loads(candidate)
    except json.JSONDecodeError:
        # If parsing fails, return None.
        return None
    try:
        # Validate the parsed JSON object against the schema.
        jsonschema.validate(instance=candidate_json, schema=schema)
        # If no exception is raised, this candidate matches the schema.
        return candidate_json
    except jsonschema.ValidationError:
        # Candidate did not match the schema; return None.
        return None


def decode_if_bytes(input_data):
    """
    Checks if the input is a byte string. If so, decodes it to a regular string using UTF-8.
    Otherwise, returns the input unchanged.

    Args:
        input_data (str or bytes): The input data to check.

    Returns:
        str: The decoded string if input_data was bytes, otherwise the original string.
    """
    if isinstance(input_data, bytes):
        return input_data.decode('utf-8')
    return input_data


def format_json_to_schema(schema, data):
    """
    Recursively format a JSON object to match a given JSON schema.
    For example, if a value is a string but should be a number per the schema,
    this function will try to convert the string to a number.

    Args:
        schema (dict): A JSON schema specifying expected types.
        data (any): The JSON data (typically a dict) to format.

    Returns:
        any: The formatted data.
    """
    # Helper function to convert a value to an expected type.
    def convert_value(expected_type, value):
        if expected_type in ("number", "integer"):
            if isinstance(value, str):
                try:
                    # Convert to int if expected integer, otherwise to float.
                    return int(value) if expected_type == "integer" else float(value)
                except ValueError:
                    return value
            # You might also want to convert other numeric types (like bool) if needed.
            return value
        elif expected_type == "boolean":
            if isinstance(value, str):
                low = value.strip().lower()
                if low in ("true", "yes", "1"):
                    return True
                elif low in ("false", "no", "0"):
                    return False
                else:
                    return value
            return value
        # For other types like "string", just return the value.
        return value

    # Recursive function that formats the data according to the schema.
    def format_value(schema, value):
        # If the schema defines an object.
        if schema.get("type") == "object":
            if not isinstance(value, dict):
                return value  # If it's not a dict, we can't do much.
            return format_object(schema, value)

        # If the schema defines an array.
        if schema.get("type") == "array":
            if not isinstance(value, list):
                return value  # Or wrap in list if desired.
            item_schema = schema.get("items", {})
            return [format_value(item_schema, item) for item in value]

        # Otherwise, try converting the value.
        expected_type = schema.get("type")
        return convert_value(expected_type, value)

    # Function to format an object (dictionary) based on its schema.
    def format_object(schema, obj):
        properties = schema.get("properties", {})
        formatted_obj = {}
        for key, prop_schema in properties.items():
            if key in obj:
                formatted_obj[key] = format_value(prop_schema, obj[key])
            # Optionally, you can add handling for missing keys or defaults.
        return formatted_obj

    # Start the formatting from the root.
    if schema.get("type") == "object" and isinstance(data, dict):
        return format_object(schema, data)
    else:
        return format_value(schema, data)


def determine_llm(model: str) -> LlmToolsInterface:
    """
    Determine the LLM model based on the provided model name.
    """
    if "llama" in model.lower():
        return LlamaTools()
    elif "openai" in model.lower():
        return OpenAiTools()
    elif "azure" in model.lower():
        return AzureTools()
    else:
        raise ValueError(f"Unknown LLM model: {model}")
