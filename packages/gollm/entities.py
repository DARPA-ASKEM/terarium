from datetime import datetime
from pydantic import BaseModel, root_validator
from typing import List, Callable, Type, Optional
from enum import StrEnum


class GollmModel(BaseModel):
    llm: Optional[str] = None


class GeneralQueryModel(GollmModel):
    instruction: str


class ModelAndDocument(GollmModel):
    amr: str  # expects AMR in a stringified JSON object
    document: Optional[str] = None


class ModelIntrospection(GollmModel):
    ode: str
    parameters: str
    question: str


class ModelAndDataset(GollmModel):
    amr: str  # expects AMR in a stringified JSON object
    dataset: List[str]
    matrix: Optional[str] = None


class DatasetStatistics(GollmModel):
    datasetUrl: str  # expects a URL of a CSV file


class DatasetCardModel(GollmModel):
    dataset: str  # expects a stringified JSON object
    document: Optional[str] = None


class ModelCompareModel(GollmModel):
    amrs: List[str]  # expects AMRs to be a stringified JSON object
    goal: str = None


class DocumentQuestionModel(GollmModel):
    document: str  # expects a stringified JSON object
    question: str


class EquationsModel(GollmModel):
    equations: List[str]


class EquationsFromImage(GollmModel):
    image: str  # expects a base64 encoded image


class ChartAnnotationType(StrEnum):
    FORECAST_CHART = 'FORECAST_CHART',
    QUANTILE_FORECAST_CHART = 'QUANTILE_FORECAST_CHART',


class ChartAnnotationModel(GollmModel):
    preamble: str
    instruction: str
    chartType: ChartAnnotationType = ChartAnnotationType.FORECAST_CHART


class EmbeddingModel(GollmModel):
    text: List[str]
    embedding_model: str

    @root_validator(pre=False, skip_on_failure=True)
    def check_embedding_model(cls, values):
        embedding_model = values.get("embedding_model")
        if embedding_model != "text-embedding-ada-002":
            raise ValueError(
                'Invalid embedding model, must be "text-embedding-ada-002"'
            )
        return values


class Message(GollmModel):
    message_type: str
    message_content: str
    message_id: int = None
    timestamp: datetime = None

    @root_validator(pre=True)
    def set_timestamp_id(cls, values):
        timestamp = values.get("timestamp")
        if timestamp:
            values["timestamp"] = datetime.fromtimestamp(timestamp)
        else:
            values["timestamp"] = datetime.now()
        return values

    @root_validator(pre=True)
    def set_message_id(cls, values):
        timestamp = values.get("timestamp")
        if timestamp:
            values["message_id"] = int(timestamp.timestamp())
        return values


class Action(GollmModel):
    message_id: int
    action_blob: dict


class ChatSession:
    # create session_id from datetime now
    session_id = int(datetime.now().timestamp())

    def __init__(self, system_context: str):
        self.system_context = system_context
        self.conversation_history = []

    def add_message(self, message: Message):
        """
        Add a message to the conversation history.
        """
        self.conversation_history.append(message)

    def get_history(self):
        """
        Get the conversation history.
        :return: List of tuples containing message type and content
        """
        return self.conversation_history


class Tool:
    def __init__(
        self, name: str, args: List, description: str, func: Callable, input_type: Type
    ):
        self.name = name
        self.args = args
        self.description = description
        self.func = func
        self.input_type = input_type

    def __call__(self, *args, **kwargs):
        return self.func(*args, **kwargs)


class Toolset:
    """
    A class for testing the toolset. Use as context manager to add tools.
    Example usage:

    with Toolset() as named_toolset:
                                                            named_toolset.add_tool("ask_a_human", ["human_instructions"], "Asks the end user for their input. Useful if there are no existing tools to solve your task. You can rely on the user to search the web, provide personal details, and generally provide you with up-to-date information.", _ask_a_human, str)
            named_toolset.add_tool("get_date", ["date_format"], "Returns the current date.", _get_date, str)
            named_toolset.add_tool("read_csv", ["file_path", "**kwargs"], "Reads a CSV file into a pandas DataFrame.", _read_csv, str)
    """

    def __init__(self):
        self.tools = []

    @property
    def TOOLS(self):
        return {tool.name: tool for tool in self.tools}

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        pass

    def get_tool_names(self):
        """
        Returns a string of tool names.
        """
        return "\n".join([tool.name for tool in self.tools])

    def get_tool_code(self):
        """
        Returns a string of tool arguments. Used for zero shot ReAct
        """
        return "\n".join([inspect.getsource(tool.func) for tool in self.tools])

    def run_tool(self, tool_name: str, tool_args):
        """
        Runs a tool.
        """
        tool = self.TOOLS[tool_name]
        return tool(tool.input_type(tool_args))

    def add_tool(
        self,
        name: str,
        args: List[str],
        description: str,
        func: Callable,
        input_type: Type,
    ):
        """
        Adds a tool to the toolset.
        """
        self.tools.append(Tool(name, args, description, func, input_type))
