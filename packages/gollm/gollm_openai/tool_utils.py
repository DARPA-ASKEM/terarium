import base64
import imghdr
import json
import os
from gollm_openai.prompts.amr_enrichment import ENRICH_PROMPT
from gollm_openai.prompts.condense import CONDENSE_PROMPT, format_chunks
from gollm_openai.prompts.config_from_dataset import (
    CONFIGURE_FROM_DATASET_PROMPT,
    CONFIGURE_FROM_DATASET_DATASET_PROMPT,
    CONFIGURE_FROM_DATASET_MAPPING_PROMPT,
    CONFIGURE_FROM_DATASET_TIMESERIES_PROMPT,
    CONFIGURE_FROM_DATASET_AMR_PROMPT,
    CONFIGURE_FROM_DATASET_MATRIX_PROMPT
)
from gollm_openai.prompts.config_from_document import CONFIGURE_FROM_DOCUMENT_PROMPT
from gollm_openai.prompts.dataset_enrichment import DATASET_ENRICH_PROMPT
from gollm_openai.prompts.equations_cleanup import EQUATIONS_CLEANUP_PROMPT
from gollm_openai.prompts.equations_from_image import EQUATIONS_FROM_IMAGE_PROMPT
from gollm_openai.prompts.general_instruction import GENERAL_INSTRUCTION_PROMPT
from gollm_openai.prompts.interventions_from_document import INTERVENTIONS_FROM_DOCUMENT_PROMPT
from gollm_openai.prompts.latex_style_guide import LATEX_STYLE_GUIDE
from gollm_openai.prompts.model_card import INSTRUCTIONS
from gollm_openai.prompts.model_meta_compare import (
    MODEL_METADATA_COMPARE_PROMPT,
    MODEL_METADATA_COMPARE_GOAL_PROMPT
)
from openai import OpenAI
from openai.types.chat.completion_create_params import ResponseFormat
from typing import List
from utils import (
    exceeds_tokens,
    model_config_adapter,
    normalize_greek_alphabet,
    validate_schema
)

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
GPT_MODEL = "gpt-4o-2024-08-06"


def escape_curly_braces(text: str):
    # Escape curly braces
    return text.replace("{", "{{").replace("}", "}}")


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


def equations_cleanup(equations: List[str]) -> dict:
    print("Reformatting equations...")

    print("Uploading and validating equations schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'equations.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to reformat equations...")
    prompt = EQUATIONS_CLEANUP_PROMPT.format(
        style_guide=LATEX_STYLE_GUIDE,
        equations="\n".join(equations)
    )

    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        temperature=0,
        seed=123,
        max_tokens=1024,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "equations",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ]
    )
    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    return output_json


def equations_from_image(image: str) -> dict:
    print("Translating equations from image...")

    print("Validating and encoding image...")
    base64_image_str = get_image_format_string(
        imghdr.what(None, h=base64.b64decode(image))
    )

    print("Uploading and validating equations schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'equations.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to extract equations an image...")
    prompt = EQUATIONS_FROM_IMAGE_PROMPT.format(
        style_guide=LATEX_STYLE_GUIDE
    )

    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        temperature=0,
        seed=234,
        max_tokens=1024,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "equations",
                "schema": response_schema
            }
        },
        messages=[
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": prompt},
                    {"type": "image_url", "image_url": {"url": f"{base64_image_str}{image}"}}
                ]
            },
        ],
    )
    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    print("There are", len(output_json['equations']), "equations identified from the image.")

    return output_json


def interventions_from_document(research_paper: str, amr: str) -> dict:
    print("Extracting and formatting research paper...")
    research_paper = normalize_greek_alphabet(research_paper)

    print("Uploading and validating intervention policy schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'intervention_policy.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to extract model configurations from a reasearch paper...")
    prompt = INTERVENTIONS_FROM_DOCUMENT_PROMPT.format(
        amr=escape_curly_braces(amr),
        research_paper=escape_curly_braces(research_paper)
    )

    print("Sending request to OpenAI API...")
    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        frequency_penalty=0,
        max_tokens=4000,
        presence_penalty=0,
        seed=905,
        temperature=0,
        top_p=1,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "intervention_policy",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ]
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    print("There are ", len(output_json["interventionPolicies"]), "intervention policies identified from the text.")

    return unescape_curly_braces(output_json)


def model_config_from_document(research_paper: str, amr: str) -> dict:
    print("Extracting and formatting research paper...")
    research_paper = normalize_greek_alphabet(research_paper)

    print("Uploading and validating model configuration schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'configuration.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to extract model configurations from a reasearch paper...")
    prompt = CONFIGURE_FROM_DOCUMENT_PROMPT.format(
        amr=escape_curly_braces(amr),
        research_paper=escape_curly_braces(research_paper)
    )

    print("Sending request to OpenAI API...")
    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        frequency_penalty=0,
        max_tokens=4000,
        presence_penalty=0,
        seed=123,
        temperature=0,
        top_p=1,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "model_configurations",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ]
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    print("There are ", len(output_json["conditions"]), "conditions identified from the text.")

    return unescape_curly_braces(model_config_adapter(output_json))


def amr_enrichment_chain(amr: str, research_paper: str) -> dict:
    print("Extracting and formatting research paper...")
    research_paper = normalize_greek_alphabet(research_paper)

    print("Uploading and validating model enrichment schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'amr_enrichment.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to extract model enrichments from a research paper...")
    prompt = ENRICH_PROMPT.format(
        amr=escape_curly_braces(amr),
        research_paper=escape_curly_braces(research_paper)
    )

    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        max_tokens=16000,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        seed=123,
        temperature=0,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "model_configurations",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ],
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    return unescape_curly_braces(output_json)


def dataset_enrichment_chain(research_paper: str, dataset: str) -> dict:
    print("Extracting and formatting research paper...")
    research_paper = normalize_greek_alphabet(research_paper)

    print("Uploading and validating dataset enrichment schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'dataset_enrichment.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to extract dataset enrichments from a research paper...")
    prompt = DATASET_ENRICH_PROMPT.format(
        research_paper=escape_curly_braces(research_paper),
        dataset=dataset
    )

    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        max_tokens=16000,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        seed=123,
        temperature=0,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "dataset_enrichment",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ],
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    return unescape_curly_braces(output_json)


def model_card_chain(amr: str, research_paper: str = None) -> dict:
    print("Creating model card...")
    assert amr, "An AMR model must be provided."
    if not research_paper:
        research_paper = "NO RESEARCH PAPER PROVIDED"

    print("Uploading and validating model card schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'model_card.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    prompt = INSTRUCTIONS.format(
        research_paper=escape_curly_braces(research_paper),
        amr=escape_curly_braces(amr)
    )

    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        temperature=0,
        frequency_penalty=0,
        max_tokens=16000,
        presence_penalty=0,
        seed=365,
        top_p=1,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "model_card",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ],
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    return unescape_curly_braces(output_json)


def condense_chain(query: str, chunks: List[str], max_tokens: int = 16385) -> str:
    print("Condensing chunks for query: {}".format(query[:100]))
    prompt = CONDENSE_PROMPT.format(query=query, chunks=format_chunks(chunks))
    if exceeds_tokens(prompt, max_tokens):
        raise ValueError(
            "Prompt exceeds max tokens. Reduce number of chunks by reducing K in KNN search."
        )
    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        temperature=0,
        seed=123,
        max_tokens=1024,
        messages=[
            {"role": "user", "content": prompt},
        ],
    )
    return output.choices[0].message.content


def generate_response(instruction: str, response_format: ResponseFormat | None = None) -> str:
    prompt = GENERAL_INSTRUCTION_PROMPT.format(instruction=instruction)
    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        temperature=0,
        seed=123,
        max_tokens=2048,
        response_format=response_format,
        messages=[
            {"role": "user", "content": prompt},
        ],
    )
    return output.choices[0].message.content


def bulk_embedding_chain(texts: List[str]) -> List:
    print("Creating embeddings for texts...")
    client = OpenAI()
    response = client.embeddings.create(
        model="text-embedding-ada-002",
        input=texts
    )

    embeddings = []
    for item in response.data:
        embeddings.append(item.embedding)

    return embeddings


def embedding_chain(text: str) -> List:
    print("Creating embeddings for text: {}".format(text[:100]))
    client = OpenAI()
    output = client.embeddings.create(model="text-embedding-ada-002", input=text)
    return output.data[0].embedding


def model_config_from_dataset(amr: str, dataset: List[str], matrix: str) -> dict:
    print("Extracting datasets...")
    dataset_text = os.linesep.join(dataset)

    print("Uploading and validating model configuration schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'configuration.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    print("Building prompt to extract model configurations from a dataset...")
    prompt = (CONFIGURE_FROM_DATASET_PROMPT
              + CONFIGURE_FROM_DATASET_MAPPING_PROMPT
              + CONFIGURE_FROM_DATASET_TIMESERIES_PROMPT
              + CONFIGURE_FROM_DATASET_AMR_PROMPT.format(amr=amr)
              + CONFIGURE_FROM_DATASET_DATASET_PROMPT.format(data=dataset_text))

    if matrix:
        prompt += CONFIGURE_FROM_DATASET_MATRIX_PROMPT.format(matrix=matrix)

    prompt += "Answer:"

    print("Sending request to OpenAI API...")
    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        frequency_penalty=0,
        max_tokens=10000,
        presence_penalty=0,
        seed=123,
        temperature=0,
        top_p=1,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "model_configurations",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ],
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    print("There are ", len(output_json["conditions"]), "conditions identified from the datasets.")

    return unescape_curly_braces(model_config_adapter(output_json))


def compare_models(amrs: List[str], goal: str) -> dict:

    print("Comparing models...")

    print("Building prompt to compare models...")
    joined_escaped_amrs = "\n\n------\n\n".join([escape_curly_braces(amr) for amr in amrs])
    prompt = MODEL_METADATA_COMPARE_PROMPT.format(
        amrs=joined_escaped_amrs
    )
    if goal is not None and goal != '':
        prompt += MODEL_METADATA_COMPARE_GOAL_PROMPT.format(goal=goal)
    prompt += "Answer:"

    print(prompt)

    print("Uploading and validating compare models schema...")
    config_path = os.path.join(SCRIPT_DIR, 'schemas', 'compare_models.json')
    with open(config_path, 'r') as config_file:
        response_schema = json.load(config_file)
    validate_schema(response_schema)

    client = OpenAI()
    output = client.chat.completions.create(
        model=GPT_MODEL,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        seed=123,
        temperature=0,
        max_tokens=16000,
        response_format={
            "type": "json_schema",
            "json_schema": {
                "name": "compare_models",
                "schema": response_schema
            }
        },
        messages=[
            {"role": "user", "content": prompt},
        ]
    )

    print("Received response from OpenAI API. Formatting response to work with HMI...")
    output_json = json.loads(output.choices[0].message.content)

    return unescape_curly_braces(output_json)
