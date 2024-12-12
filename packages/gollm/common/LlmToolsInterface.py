from typing import List


class LlmToolsInterface:

    def send_to_llm(self, prompt: str, schema: str, max_tokens: int) -> dict:
        """Send a request to an LLM API"""
        pass


    def send_image_to_llm(self, prompt: str, schema: str, image_url: str, max_tokens: int) -> dict:
        """Send a request to an LLM API with an image"""
        pass


    def create_enrich_model_prompt(self, amr: str, document: str, schema: str) -> str:
        """Create a prompt to enrich a model's metadata from a document"""
        pass


    def create_config_from_dataset_prompt(self, amr: str, dataset: List[str], matrix: str, schema: str) -> str:
        """Create a prompt to extract model configurations from a dataset"""
        pass

    def create_config_from_document_prompt(self, amr: str, document: str, schema: str) -> str:
        """Create a prompt to extract model configurations from a document"""
        pass

    def create_enrich_dataset_prompt(self, dataset: str, document: str, schema: str) -> str:
        """Create a prompt to enrich a dataset's metadata from a document"""
        pass

    def create_cleanup_equations_prompt(self, equations: List[str], schema: str) -> str:
        """Create a prompt that formats latex equations into a format usable by Terarium"""
        pass

    def create_equations_from_image_prompt(self, image_url: str, schema: str) -> str:
        """Create equations from image prompt"""
        pass

    def create_interventions_from_document_prompt(self, amr: str, document: str, schema: str) -> str:
        """Create a prompt to extract interventions from a document"""
        pass

    def create_model_card_prompt(self, amr: str, document: str, schema: str) -> str:
        """Create a prompt that produces a model card"""
        pass

    def create_compare_models_prompt(self, amrs: List[str], goal: str, schema: str) -> str:
        """Create a prompt that compares models"""
        pass
