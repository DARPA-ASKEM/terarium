import json
import jsonschema
import regex as re
import tiktoken


def remove_references(text: str) -> str:
    """
    Removes reference sections from a scientific paper.
    """
    pattern = r"References\n([\s\S]*?)(?:\n\n|\Z)"
    new_text = re.sub(pattern, "", text)
    return new_text.strip()


def parse_param_initials(amr: dict):
    try:
        ode = amr['semantics']['ode']
    except KeyError:
        raise KeyError("ODE semantics not found in AMR, please provide a valide AMR with structure semantics.ode")

    assert 'parameters' in ode, "No parameters found in ODE semantics, please provide a valid AMR with structure semtnatics.ode.parameters"
    assert 'initials' in ode, "No initials found in ODE semantics, please provide a valid AMR with structure semantics.ode.initials"

    params = ode['parameters']

    assert all(['id' in p.keys() for p in params]), "All parameters must have an 'id' key"

    param_ids = [p['id'] for p in params if p is not None and p.get('id')]

    initials = ode['initials']

    assert all(['target' in i.keys() for i in initials]), "All initials must have an 'id' key"

    initial_ids = [i['target'] for i in initials if i is not None and i.get('target')]

    return {'initial_names': initial_ids, 'param_names': param_ids}


def parse_json_from_markdown(text):
    print("Stripping markdown...")
    json_pattern = r"```json\s*(\{.*?\})\s*```"
    match = re.search(json_pattern, text, re.DOTALL)
    if match:
        return match.group(1)
    else:
        print(f"No markdown found in text: {text}")
        return text


def extract_json(text: str) -> dict:
    corrected_text = text.replace("{{", "{").replace("}}", "}")
    try:
        json_obj = json.loads(corrected_text)
        return json_obj
    except json.JSONDecodeError as e:
        raise ValueError(f"Error decoding JSON: {e}\nfrom text {text}")


def validate_schema(schema):
    try:
        jsonschema.Draft202012Validator.check_schema(schema)
        print("Schema is valid.")
    except jsonschema.exceptions.SchemaError as e:
        print(f"Schema is invalid: {e.message}")


def postprocess_oai_json(output: str) -> dict:
    output = "{" + parse_json_from_markdown(
        output
    )  # curly bracket is used in all prompts to denote start of json.
    return extract_json(output)


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


# Adapter function which converts the model config dict to HMI expected format.
def model_config_adapter(model_config: dict) -> dict:
    # for each condition and for each parameter semantic in the model configuration,
    # if the distribution is not `contant`, remove the `value` key
    # otherwise, remove the maximum and minimum keys.
    for condition in model_config["conditions"]:
        print(condition)
        for param in condition["parameterSemanticList"]:
            if param["distribution"]["type"].casefold() == "constant":
                param["distribution"]["type"] = "Constant"
                param["distribution"]["parameters"].pop("minimum", None)
                param["distribution"]["parameters"].pop("maximum", None)
            elif param["distribution"]["type"].casefold() == "uniform":
                param["distribution"]["type"] = "Uniform"
                param["distribution"]["parameters"].pop("value", None)
            else:
                if "value" in param["distribution"]["parameters"]:
                    param["distribution"]["type"] = "Constant"
                    param["distribution"]["parameters"].pop("minimum", None)
                    param["distribution"]["parameters"].pop("maximum", None)
                elif "minimum" in param["distribution"]["parameters"] and "maximum" in param["distribution"]["parameters"]:
                    param["distribution"]["type"] = "Uniform"
                    param["distribution"]["parameters"].pop("value", None)
                else:
                    raise ValueError("Invalid distribution type")

    return model_config
