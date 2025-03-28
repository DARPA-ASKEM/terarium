
import os
import json
import boto3
import botocore
import logging
from openai import OpenAI, AzureOpenAI
from src.utils import resize_image


TABLE_EXTRACTION_ENHANCE_PROMPT = """Here is the extracted table in html from the provided image.

The extracted result isn't perfect, but it's a good start. You know that the structure of the table is correct, but the extracted content of the each cells may not be accurate or be missing.

Your job is to verify the extracted table content with the provided image and correct any inaccuracies or missing data. Do not alter the structure, colspan, or rowspan of the table, only replace the cell text value with the correct data. Some cells may span multiple columns or rows which is normal and should be preserved. Also it's normal to have multiple header rows or columns. Do not remove any rows or columns from the table, just correct the cell text values. Number of rows and columns should be preserved.
Ensure that symbols, subscripts, superscripts, and greek characters are preserved, "α" should not be swapped to "a". Make sure symbols, greek characters, and mathematical expressions are preserved and represented correctly.
Return corrected html table with the same structure. The result must starts with <table> tag and ends with </table> tag.

Please respond only with the valid json format, {"table_text": ... }
"""
class LlmToolsInterface:
    """Interface for LLM tools"""

    def name(self) -> str:
        """Get the name of the LLM"""
        pass

    def enhance_table_extraction(self, table_image_uri: str, table_html: str, max_tokens=int) -> dict:
        """Enhance table extraction using an LLM model"""
        pass


class OpenAiTools(LlmToolsInterface):
    """OpenAI LLM tools"""

    GPT_MODEL = "gpt-4o-2024-08-06"

    def __init__(self, api_key=None):
        self.api_key = os.getenv("ASKEM_DOC_AI_API_KEY") if api_key == None else api_key
        if (self.api_key is None):
            raise ValueError("ASKEM_DOC_AI_API_KEY not found in environment variables. Please set 'ASKEM_DOC_AI_API_KEY'.")

    def name(self) -> str:
        return f"OpenAI ({self.GPT_MODEL})"

    def enhance_table_extraction(self, table_image_uri: str, table_html: str, max_tokens=8192) -> dict:
        client = OpenAI(api_key=self.api_key)

        logging.info(f"Enhancing table extraction using {self.GPT_MODEL} model...")
        response = client.chat.completions.create(
            model=self.GPT_MODEL,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image_url",
                            "image_url": {"url": table_image_uri},
                        },
                        {
                            "type": "text",
                            "text": table_html,
                        },
                        {"type": "text", "text": TABLE_EXTRACTION_ENHANCE_PROMPT},
                    ],
                }
            ],
            max_tokens=max_tokens,
            response_format={"type": "json_object"}
        )
        message_content = json.loads(response.choices[0].message.content)
        return message_content




class LlamaTools(LlmToolsInterface):

    GPT_MODEL = "us.meta.llama3-2-90b-instruct-v1:0"

    def __init__(self, bedrock_access_key=None, bedrock_secret_access_key=None):
        self.bedrock_access_key = os.environ.get("BEDROCK_ACCESS_KEY") if bedrock_access_key is None else bedrock_access_key
        self.bedrock_secret_access_key = os.environ.get("BEDROCK_SECRET_ACCESS_KEY") if bedrock_secret_access_key is None else bedrock_secret_access_key

        if self.bedrock_access_key is None:
            raise ValueError("BEDROCK_ACCESS_KEY not found in environment variables or provided as an argument. Please set 'BEDROCK_ACCESS_KEY'.")
        if self.bedrock_secret_access_key is None:
            raise ValueError("BEDROCK_SECRET_ACCESS_KEY not found in environment variables or provided as an argument. Please set 'BEDROCK_SECRET_ACCESS_KEY'.")

        self.boto_config = botocore.config.Config(
            read_timeout=600,
            connect_timeout=480,
            retries={"max_attempts": 0}
        )

    def name(self) -> str:
        return "AWS Llama (Llama 3.2 90B Instruct)"

    def enhance_table_extraction(self, table_image_uri: str, table_html: str, max_tokens=8192) -> dict:

        # Convert the table image to base64 and resize it
        table_image_base64 = table_image_uri.split(",", 1)[-1]  # Take only the Base64 part
        # Llama model only supports images with a maximum size of 1120x1120
        img_base64 = resize_image(table_image_base64, (1120, 1120))

        prompt = f"\nTable HTML: {table_html}\n {TABLE_EXTRACTION_ENHANCE_PROMPT}\n"

        # More details on the prompt template: https://www.llama.com/docs/model-cards-and-prompt-formats/llama3_2/
        formatted_prompt = f"""
        <|begin_of_text|><|start_header_id|>user<|end_header_id|><|image|>
        {prompt}
        <|eot_id|>
        <|start_header_id|>assistant<|end_header_id|>
        """

        client = boto3.client(
            "bedrock-runtime",
            region_name="us-west-2",
            aws_access_key_id = self.bedrock_access_key,
            aws_secret_access_key = self.bedrock_secret_access_key,
            config=self.boto_config
        )

        request = json.dumps({
            "prompt": formatted_prompt,
            "temperature": 0,
            "max_gen_len": max_tokens,
            "images": [img_base64],
        })

        logging.info(f"Enhancing table extraction using {self.GPT_MODEL} model...")
        response = client.invoke_model(modelId=self.GPT_MODEL, body=request)

        # Decode the response body.
        model_response = json.loads(response["body"].read())
        result = json.loads(model_response["generation"])
        return result


class AzureTools(LlmToolsInterface):
    """Azure LLM tools"""

    API_VERSION = "2024-10-21"

    def __init__(self, api_key=None, azure_endpoint=None, model_name=None):
        self.api_key = os.getenv("AZURE_OPENAI_KEY") if api_key is None else api_key
        self.azure_endpoint = os.getenv("AZURE_OPENAI_ENDPOINT") if azure_endpoint is None else azure_endpoint
        self.model_name = os.getenv("AZURE_OPENAI_MODEL") if model_name is None else model_name

        if self.api_key is None:
            raise ValueError("AZURE_OPENAI_KEY not found in environment variables or provided as an argument. Please set 'AZURE_OPENAI_KEY'.")
        if self.azure_endpoint is None:
            raise ValueError("AZURE_OPENAI_ENDPOINT not found in environment variables or provided as an argument. Please set 'AZURE_OPENAI_ENDPOINT'.")
        if self.model_name is None:
            raise ValueError("AZURE_OPENAI_MODEL not found in environment variables or provided as an argument. Please set 'AZURE_OPENAI_MODEL'.")

    def name(self) -> str:
        return f"Azure OpenAI (gpt-4o 2024-11-20)"

    def enhance_table_extraction(self, table_image_uri: str, table_html: str, max_tokens=8192) -> dict:

        client = AzureOpenAI(
            api_key=self.api_key,
            api_version=self.API_VERSION,
            azure_endpoint =self.azure_endpoint
        )
        logging.info(f"Enhancing table extraction using {self.model_name} model...")

        response = client.chat.completions.create(
            model=self.model_name,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image_url",
                            "image_url": {"url": table_image_uri},
                        },
                        {
                            "type": "text",
                            "text": table_html,
                        },
                        {"type": "text", "text": TABLE_EXTRACTION_ENHANCE_PROMPT},
                    ],
                }
            ],
            max_tokens=max_tokens,
            response_format={"type": "json_object"},
        )
        message_content = json.loads(response.choices[0].message.content)
        return message_content



def get_llm_tools(model: str) -> LlmToolsInterface:
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
