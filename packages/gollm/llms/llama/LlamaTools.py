import boto3
import botocore
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
from common.prompts.model_enrichment import (MODEL_ENRICH_PROMPT_WITH_DOCUMENT, MODEL_ENRICH_PROMPT_WITHOUT_DOCUMENT)
from common.prompts.model_meta_compare import (
    MODEL_METADATA_COMPARE_PROMPT,
    MODEL_METADATA_COMPARE_GOAL_PROMPT,
    MODEL_METADATA_COMPARE_DATA_PROMPT,
    MODEL_METADATA_COMPARE_GOAL_AND_DATA_PROMPT
)
from common.utils import (
    decode_if_bytes,
    escape_curly_braces,
    extract_json_object,
    format_json_to_schema,
    unescape_curly_braces
)
from entities import ChartAnnotationType
from llms.llama.prompts.llama_prompts import LLAMA_START_PROMPT, LLAMA_RETURN_INSTRUCTIONS, LLAMA_END_PROMPT
from typing import List, Optional


GPT_MODEL = "us.meta.llama3-2-90b-instruct-v1:0"


class LlamaTools(LlmToolsInterface):

    def __init__(self, bedrock_access_key=None, bedrock_secret_access_key=None):
        self.bedrock_access_key = bedrock_access_key
        self.bedrock_secret_access_key = bedrock_secret_access_key


    def name(self) -> str:
        return "AWS Llama (Llama 3.2 90B Instruct)"


    def send_to_llm_with_json_output(self, prompt: str, schema: str, max_tokens=8192) -> dict:
        print("Creating AWS Bedrock (Llama) client...")

        config = botocore.config.Config(
            read_timeout=600,
            connect_timeout=480,
            retries={"max_attempts": 0}
        )

        client = boto3.client(
            "bedrock-runtime",
            region_name="us-west-2",
            aws_access_key_id = os.environ.get("BEDROCK_ACCESS_KEY") if self.bedrock_access_key is None else self.bedrock_access_key,
            aws_secret_access_key = os.environ.get("BEDROCK_SECRET_ACCESS_KEY") if self.bedrock_secret_access_key is None else self.bedrock_secret_access_key,
            config=config
        )

        request = json.dumps({
            "prompt": prompt,
            "max_gen_len": max_tokens,
            "temperature": 0,
            "top_p": 1,
        })

        print("Sending request to AWS Bedrock (Llama)...")
        response = client.invoke_model(
            modelId=GPT_MODEL,
            body=request,
            contentType="application/json"
        )

        print("Received response from AWS Bedrock (Llama)...")
        raw_response = response["body"].read();
        string_response = decode_if_bytes(raw_response)

        try:
            print("Trying to parse response as JSON...")
            model_response = json.loads(string_response)
        except json.JSONDecodeError:
            print("Parsing response as JSON failed, trying to extract JSON object...")
            model_response = extract_json_object(string_response)

        try:
            response_json = json.loads(model_response["generation"])
        except json.JSONDecodeError:
            print("Parsing response as JSON failed, trying to extract JSON object...")
            response_json = extract_json_object(model_response["generation"])

        return format_json_to_schema(schema, unescape_curly_braces(response_json))


    def send_to_llm_with_string_output(self, prompt: str, max_tokens=8192) -> str:
        print("Creating AWS Bedrock (Llama) client...")

        config = botocore.config.Config(
            read_timeout=600,
            connect_timeout=480,
            retries={"max_attempts": 0}
        )

        client = boto3.client(
            "bedrock-runtime",
            region_name="us-west-2",
            aws_access_key_id = os.environ.get("BEDROCK_ACCESS_KEY") if self.bedrock_access_key is None else self.bedrock_access_key,
            aws_secret_access_key = os.environ.get("BEDROCK_SECRET_ACCESS_KEY") if self.bedrock_secret_access_key is None else self.bedrock_secret_access_key,
            config=config
        )

        request = json.dumps({
            "prompt": prompt,
            "max_gen_len": max_tokens,
            "temperature": 0,
            "top_p": 1,
        })

        print("Sending request to AWS Bedrock (Llama)...")
        response = client.invoke_model(
            modelId=GPT_MODEL,
            body=request,
            contentType="application/json"
        )

        print("Received response from AWS Bedrock (Llama)...")
        raw_response = response["body"].read();
        string_response = decode_if_bytes(raw_response)

        try:
            print("Trying to parse response as JSON...")
            model_response = json.loads(string_response)
        except json.JSONDecodeError:
            print("Parsing response as JSON failed, trying to extract JSON object...")
            model_response = extract_json_object(string_response)

        return model_response["generation"]


    def send_image_to_llm_with_json_output(self, prompt: str, schema: str, image_url: str, max_tokens=8192) -> dict:
        print("Creating AWS Bedrock (Llama) client...")

        config = botocore.config.Config(
            read_timeout=600,
            connect_timeout=480,
            retries={"max_attempts": 0}
        )

        client = boto3.client(
            "bedrock-runtime",
            region_name="us-west-2",
            aws_access_key_id = os.environ.get("BEDROCK_ACCESS_KEY") if self.bedrock_access_key is None else self.bedrock_access_key,
            aws_secret_access_key = os.environ.get("BEDROCK_SECRET_ACCESS_KEY") if self.bedrock_secret_access_key is None else self.bedrock_secret_access_key,
            config=config
        )

        request = json.dumps({
            "prompt": prompt,
            "max_gen_len": max_tokens,
            "temperature": 0,
            "top_p": 1,
        })

        print("Sending request to AWS Bedrock (Llama)...")
        response = client.invoke_model(
            modelId=GPT_MODEL,
            body=request,
            contentType="application/json"
        )

        print("Received response from AWS Bedrock (Llama)...")
        raw_response = response["body"].read();
        string_response = decode_if_bytes(raw_response)

        try:
            print("Trying to parse response as JSON...")
            model_response = json.loads(string_response)
        except json.JSONDecodeError:
            print("Parsing response as JSON failed, trying to extract JSON object...")
            model_response = extract_json_object(string_response)

        try:
            response_json = json.loads(model_response["generation"])
        except json.JSONDecodeError:
            print("Parsing response as JSON failed, trying to extract JSON object...")
            response_json = extract_json_object(model_response["generation"])

        return format_json_to_schema(schema, unescape_curly_braces(response_json))


    def create_enrich_model_prompt(self, amr: str, document: str, schema: str) -> str:
        print("Building prompt to extract model enrichments from a document...")
        prompt = LLAMA_START_PROMPT

        if (document is None) or (document == ''):  # If no document is provided
            print("Building prompt to extract model enrichments without a document...")
            prompt += MODEL_ENRICH_PROMPT_WITHOUT_DOCUMENT.format(
                amr=escape_curly_braces(amr)
            )
        else:
            print("Building prompt to extract model enrichments from a document...")
            prompt += MODEL_ENRICH_PROMPT_WITH_DOCUMENT.format(
                amr=escape_curly_braces(amr),
                document=escape_curly_braces(document)
            )

        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_config_from_dataset_prompt(self, amr: str, dataset: List[str], matrix: str, schema: str) -> str:
        print("Building prompt to extract model configurations from a dataset...")
        dataset_text = os.linesep.join(dataset)

        prompt = LLAMA_START_PROMPT
        prompt += (CONFIGURE_FROM_DATASET_PROMPT
                  + CONFIGURE_FROM_DATASET_MAPPING_PROMPT
                  + CONFIGURE_FROM_DATASET_TIMESERIES_PROMPT
                  + CONFIGURE_FROM_DATASET_AMR_PROMPT.format(amr=amr)
                  + CONFIGURE_FROM_DATASET_DATASET_PROMPT.format(data=dataset_text))

        if matrix:
            prompt += CONFIGURE_FROM_DATASET_MATRIX_PROMPT.format(matrix=matrix)

        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_config_from_document_prompt(self, amr: str, document: str, schema: str) -> str:
        print("Building prompt to extract model configurations from a reasearch paper...")
        prompt = LLAMA_START_PROMPT
        prompt += CONFIGURE_FROM_DOCUMENT_PROMPT.format(
            amr=escape_curly_braces(amr),
            document=escape_curly_braces(document)
        )
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_enrich_dataset_prompt(self, dataset: str, document: Optional[str], schema: str) -> str:
        prompt = LLAMA_START_PROMPT

        if (document is None) or (document == ''):  # If no document is provided
            print("Building prompt to extract dataset enrichments")
            prompt += DATASET_ENRICH_PROMPT_WITHOUT_DOCUMENT.format(dataset=dataset)
        else:
            print("Building prompt to extract dataset enrichments from a research paper...")
            prompt += DATASET_ENRICH_PROMPT_WITH_DOCUMENT.format(
                document=escape_curly_braces(document),
                dataset=dataset
            )

        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_cleanup_equations_prompt(self, equations: List[str], schema: str) -> str:
        print("Building prompt to reformat equations...")
        prompt = LLAMA_START_PROMPT
        prompt += EQUATIONS_CLEANUP_PROMPT.format(
            style_guide=LATEX_STYLE_GUIDE,
            equations="\n".join(equations)
        )
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_equations_from_image_prompt(self, image_url: str, schema: str) -> str:
        print("Building prompt to extract equations an image...")
        prompt = LLAMA_START_PROMPT
        prompt += EQUATIONS_FROM_IMAGE_PROMPT.format(
            style_guide=LATEX_STYLE_GUIDE
        )
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_interventions_from_document_prompt(self, amr: str, document: str, schema: str) -> str:
        print("Building prompt to extract interventions from a reasearch paper...")
        prompt = LLAMA_START_PROMPT
        prompt += INTERVENTIONS_FROM_DOCUMENT_PROMPT.format(
            amr=escape_curly_braces(amr),
            document=escape_curly_braces(document)
        )
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_interventions_from_dataset_prompt(self, amr: str, dataset: List[str], schema: str) -> str:
        print("Building prompt to extract interventions from a dataset...")
        dataset_text = os.linesep.join(dataset)
        prompt = LLAMA_START_PROMPT
        prompt += INTERVENTIONS_FROM_DATASET_PROMPT.format(
            amr=escape_curly_braces(amr),
            dataset=escape_curly_braces(dataset_text)
        )
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_compare_models_prompt(self, amrs: List[str], dataset: str, goal: str, schema: str) -> str:
        print("Building prompt to compare models...")
        joined_escaped_amrs = "\n\n------\n\n".join([escape_curly_braces(amr) for amr in amrs])

        prompt = LLAMA_START_PROMPT
        prompt += MODEL_METADATA_COMPARE_PROMPT.format(
            amrs=joined_escaped_amrs
        )
        if dataset is not None and dataset != '':
            prompt += MODEL_METADATA_COMPARE_DATA_PROMPT.format(dataset=dataset)
        if goal is not None and goal != '':
            prompt += MODEL_METADATA_COMPARE_GOAL_PROMPT.format(goal=goal)
        if (dataset is not None and dataset != '') and (goal is not None and goal != ''):
            prompt += MODEL_METADATA_COMPARE_GOAL_AND_DATA_PROMPT

        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_general_query_prompt(self, instruction: str) -> str:
        print("Building general query prompt...")
        prompt = LLAMA_START_PROMPT
        prompt += GENERAL_QUERY_PROMPT.format(
            instruction=instruction
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_chart_annotation_prompt(self, chartType: ChartAnnotationType, preamble: str, instruction: str, schema: str) -> str:
        print("Building chart annotation prompt...")
        prompt = LLAMA_START_PROMPT
        prompt += build_chart_annotation_prompt(chartType, preamble, instruction)
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT


    def create_latex_to_sympy_prompt(self, equations: List[str], schema: str) -> str:
        print("Building prompt to transform latex equations to sympy...")
        prompt = LLAMA_START_PROMPT
        prompt += LATEX_TO_SYMPY_PROMPT.format(
            latex_equations="\n".join(equations)
        )
        prompt += LLAMA_RETURN_INSTRUCTIONS.format(
            schema=schema
        )
        prompt += LLAMA_END_PROMPT
        return prompt


    def create_document_question_prompt(self, document: str, question: str) -> str:
        print("Building prompt to answer a question from a document...")
        prompt = LLAMA_START_PROMPT
        prompt += DOCUMENT_QUESTION_PROMPT.format(
            document=document,
            question=question
        )
        prompt += LLAMA_END_PROMPT
        return prompt
