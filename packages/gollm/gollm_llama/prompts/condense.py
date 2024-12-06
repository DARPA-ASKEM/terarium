from typing import List


def format_chunks(chunks: List[str]) -> str:
    """Format a list of chunks into a single string."""
    return "\n---END CHUNK---".join(chunks)


CONDENSE_PROMPT = """You are a helpful agent deployed within a search engine. It is your job to extract relevant information from chunks to answer the user's query. Do your best to attribute each part of your answer to the provided chunks.
 					 Given the following user query: \n\n {query} \n\n strictly use the following chunks to answer the user's query: \n\n {chunks} \n\n Answer: \n\n"""
