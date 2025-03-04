from common.LlmToolsInterface import LlmToolsInterface
from llms.azure.AzureTools import AzureTools
from llms.llama.LlamaTools import LlamaTools
from llms.openai.OpenAiTools import OpenAiTools

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
