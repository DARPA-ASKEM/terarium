import json
import os
from common.LlmToolsInterface import LlmToolsInterface
from common.prompts.chart_annotation import build_prompt as build_chart_annotation_prompt
from common.prompts.config_from_dataset import (
    CONFIGURE_FROM_DATASET_PROMPT,
    CONFIGURE_FROM_DATASET_MAPPING_PROMPT,
    CONFIGURE_FROM_DATASET_TIMESERIES_PROMPT,
    CONFIGURE_FROM_DATASET_AMR_PROMPT,
    CONFIGURE_FROM_DATASET_DATASET_PROMPT,
    CONFIGURE_FROM_DATASET_MATRIX_PROMPT
)
from common.prompts.config_from_document import CONFIGURE_FROM_DOCUMENT_PROMPT
from common.prompts.dataset_enrichment import (
    DATASET_ENRICH_PROMPT_WITH_DOCUMENT,
    DATASET_ENRICH_PROMPT_WITHOUT_DOCUMENT
)
from common.prompts.document_question import DOCUMENT_QUESTION_PROMPT
from common.prompts.equations_cleanup import EQUATIONS_CLEANUP_PROMPT
from common.prompts.equations_from_image import EQUATIONS_FROM_IMAGE_PROMPT
from common.prompts.general_query import GENERAL_QUERY_PROMPT
from common.prompts.interventions_from_dataset import INTERVENTIONS_FROM_DATASET_PROMPT
from common.prompts.interventions_from_document import INTERVENTIONS_FROM_DOCUMENT_PROMPT
from common.prompts.latex_style_guide import LATEX_STYLE_GUIDE
from common.prompts.latex_to_sympy import LATEX_TO_SYMPY_PROMPT
from common.prompts.model_card import MODEL_CARD_PROMPT
from common.prompts.model_enrichment import (MODEL_ENRICH_PROMPT_WITH_DOCUMENT, MODEL_ENRICH_PROMPT_WITHOUT_DOCUMENT)
from common.prompts.model_meta_compare import (
    MODEL_METADATA_COMPARE_PROMPT,
    MODEL_METADATA_COMPARE_GOAL_PROMPT,
    MODEL_METADATA_COMPARE_DATA_PROMPT,
    MODEL_METADATA_COMPARE_GOAL_AND_DATA_PROMPT
)
from common.utils import (
    escape_curly_braces,
    unescape_curly_braces
)
from entities import ChartAnnotationType
from openai import AzureOpenAI
from typing import List, Optional


API_VERSION = "2024-10-21"


class AzureTools(LlmToolsInterface):

    def __init__(self, api_key=None, azure_endpoint=None, model_name=None):
        self.api_key = api_key
        self.azure_endpoint = azure_endpoint
        self.model_name = model_name


    def name(self) -> str:
        return f"Azure OpenAI (gpt-4o 2024-11-20)"


    def send_to_llm_with_json_output(self, prompt: str, schema: str, max_tokens=16384) -> dict:
        print("Creating AzureOpenAI client...")
        client = AzureOpenAI(
            api_key=os.getenv("AZURE_OPENAI_KEY") if self.api_key is None else self.api_key,
            api_version=API_VERSION,
            azure_endpoint = os.getenv("AZURE_OPENAI_ENDPOINT") if self.azure_endpoint is None else self.azure_endpoint
        )
        print("Sending request to AzureOpenAI API...")
        output = client.chat.completions.create(
            model=os.getenv("AZURE_OPENAI_MODEL") if self.model_name is None else self.model_name,
            top_p=1,
            frequency_penalty=0,
            presence_penalty=0,
            temperature=0,
            seed=123,
            max_tokens=max_tokens,
            response_format={
                "type": "json_schema",
                "json_schema": {
                    "strict": True,
                    "name": "response_schema",
                    "schema": schema
                }
            },
            messages=[
                {"role": "user", "content": prompt},
            ]
        )
        print("Received response from OpenAI API...")
        output_json = json.loads(output.choices[0].message.content)
        return unescape_curly_braces(output_json)


    def send_to_llm_with_string_output(self, prompt: str, max_tokens=16384) -> str:
        print("Sending request to OpenAI API...")
        client = AzureOpenAI(
            api_key=os.getenv("AZURE_OPENAI_KEY") if self.api_key is None else self.api_key,
            api_version=API_VERSION,
            azure_endpoint = os.getenv("AZURE_OPENAI_ENDPOINT") if self.azure_endpoint is None else self.azure_endpoint
        )
        output = client.chat.completions.create(
            model=os.getenv("AZURE_OPENAI_MODEL") if self.model_name is None else self.model_name,
            top_p=1,
            frequency_penalty=0,
            presence_penalty=0,
            temperature=0,
            seed=123,
            max_tokens=max_tokens,
            messages=[
                {"role": "user", "content": prompt},
            ]
        )
        print("Received response from OpenAI API...")
        return output.choices[0].message.content


    def send_image_to_llm_with_json_output(self, prompt: str, schema: str, image_url: str, max_tokens=16384) -> dict:
        print("Sending request to OpenAI API...")
        client = AzureOpenAI(
            api_key=os.getenv("AZURE_OPENAI_KEY") if self.api_key is None else self.api_key,
            api_version=API_VERSION,
            azure_endpoint = os.getenv("AZURE_OPENAI_ENDPOINT") if self.azure_endpoint is None else self.azure_endpoint
        )
        output = client.chat.completions.create(
            model=os.getenv("AZURE_OPENAI_MODEL") if self.model_name is None else self.model_name,
            top_p=1,
            frequency_penalty=0,
            presence_penalty=0,
            temperature=0,
            seed=123,
            max_tokens=max_tokens,
            response_format={
                "type": "json_schema",
                "json_schema": {
                    "name": "equation_schema",
                    "strict": True,
                    "schema": schema
                }
            },
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {"type": "image_url", "image_url": {"url": image_url}}
                    ]
                },
            ],
        )
        print("Received response from OpenAI API...")
        output_json = json.loads(output.choices[0].message.content)
        return output_json


    def create_enrich_model_prompt(self, amr: str, document: Optional[str], schema=None) -> str:
        if (document is None) or (document == ''):  # If no document is provided
            print("Building prompt to extract model enrichments without a document...")
            return MODEL_ENRICH_PROMPT_WITHOUT_DOCUMENT.format(
                amr=escape_curly_braces(amr)
            )
        else:
            print("Building prompt to extract model enrichments from a document...")
            return MODEL_ENRICH_PROMPT_WITH_DOCUMENT.format(
                amr=escape_curly_braces(amr),
                document=escape_curly_braces(document)
            )


    def create_config_from_dataset_prompt(self, amr: str, dataset: List[str], matrix: str, schema=None) -> str:
        print("Building prompt to extract model configurations from a dataset...")
        dataset_text = os.linesep.join(dataset)
        prompt = (CONFIGURE_FROM_DATASET_PROMPT
                  + CONFIGURE_FROM_DATASET_MAPPING_PROMPT
                  + CONFIGURE_FROM_DATASET_TIMESERIES_PROMPT
                  + CONFIGURE_FROM_DATASET_AMR_PROMPT.format(amr=amr)
                  + CONFIGURE_FROM_DATASET_DATASET_PROMPT.format(data=dataset_text))

        if matrix:
            prompt += CONFIGURE_FROM_DATASET_MATRIX_PROMPT.format(matrix=matrix)

        prompt += "Answer:"
        return prompt


    def create_config_from_document_prompt(self, amr: str, document: str, schema=None) -> str:
        print("Building prompt to extract model configurations from a reasearch paper...")
        return CONFIGURE_FROM_DOCUMENT_PROMPT.format(
            amr=escape_curly_braces(amr),
            document=escape_curly_braces(document)
        )


    def create_enrich_dataset_prompt(self, dataset: str, document: Optional[str], schema=None) -> str:
        if (document is None) or (document == ''):  # If no document is provided
            print("Building prompt to extract dataset enrichments")
            return DATASET_ENRICH_PROMPT_WITHOUT_DOCUMENT.format(dataset=dataset)
        else:
            print("Building prompt to extract dataset enrichments from a research paper...")
            return DATASET_ENRICH_PROMPT_WITH_DOCUMENT.format(
                document=escape_curly_braces(document),
                dataset=dataset
            )


    def create_cleanup_equations_prompt(self, equations: List[str], schema=None) -> str:
        print("Building prompt to reformat equations...")
        return EQUATIONS_CLEANUP_PROMPT.format(
            style_guide=LATEX_STYLE_GUIDE,
            equations="\n".join(equations)
        )


    def create_equations_from_image_prompt(self, image_url: str, schema=None) -> str:
        print("Building prompt to extract equations an image...")
        return EQUATIONS_FROM_IMAGE_PROMPT.format(
            style_guide=LATEX_STYLE_GUIDE
        )


    def create_interventions_from_document_prompt(self, amr: str, document: str, schema=None) -> str:
        print("Building prompt to extract interventions from a research paper...")
        return INTERVENTIONS_FROM_DOCUMENT_PROMPT.format(
            amr=escape_curly_braces(amr),
            document=escape_curly_braces(document)
        )


    def create_interventions_from_dataset_prompt(self, amr: str, dataset: List[str], schema=None) -> str:
        print("Building prompt to extract interventions from a dataset...")
        dataset_text = os.linesep.join(dataset)
        return INTERVENTIONS_FROM_DATASET_PROMPT.format(
            amr=escape_curly_braces(amr),
            dataset=escape_curly_braces(dataset_text)
        )


    def create_model_card_prompt(self, amr: str, document: str, schema=None) -> str:
        print("Building prompt to produce a model card...")
        if not document:
            document = "NO RESEARCH PAPER PROVIDED"

        return MODEL_CARD_PROMPT.format(
            document=escape_curly_braces(document),
            amr=escape_curly_braces(amr)
        )


    def create_compare_models_prompt(self, amrs: List[str], dataset: str, goal: str, schema=None) -> str:
        print("Building prompt to compare models...")
        joined_escaped_amrs = "\n\n------\n\n".join([escape_curly_braces(amr) for amr in amrs])
        prompt = MODEL_METADATA_COMPARE_PROMPT.format(
            amrs=joined_escaped_amrs
        )
        if dataset is not None and dataset != '':
            prompt += MODEL_METADATA_COMPARE_DATA_PROMPT.format(dataset=dataset)
        if goal is not None and goal != '':
            prompt += MODEL_METADATA_COMPARE_GOAL_PROMPT.format(goal=goal)
        if (dataset is not None and dataset != '') and (goal is not None and goal != ''):
            prompt += MODEL_METADATA_COMPARE_GOAL_AND_DATA_PROMPT

        prompt += "Answer:"
        return prompt


    def create_general_query_prompt(self, instruction: str) -> str:
        print("Building general query prompt...")
        return GENERAL_QUERY_PROMPT.format(
            instruction=instruction
        )


    def create_chart_annotation_prompt(self, chartType: ChartAnnotationType, preamble: str, instruction: str, schema=None) -> str:
        print("Building chart annotation prompt...")
        return build_chart_annotation_prompt(chartType, preamble, instruction)


    def create_latex_to_sympy_prompt(self, equations: List[str], schema=None) -> str:
        print("Building prompt to transform latex equations to sympy...")
        return LATEX_TO_SYMPY_PROMPT.format(
            latex_equations="\n".join(equations)
        )

    def create_document_question_prompt(self, document: str, question: str) -> str:
        print("Building prompt to answer a question from a document...")
        return DOCUMENT_QUESTION_PROMPT.format(
            document=document,
            question=question
        )
