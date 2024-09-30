SYSTEM_MESSAGE_PREFIX = """Answer the following questions as best you can. Assume that you are a conversational agent helping a user and any time-based knowledge of yours may be out of date, and should be looked up if you are given access to a tool that will enable you to do so. You have access to the following tools:"""
FORMAT_INSTRUCTIONS = """The way you use the tools is by specifying a json blob.
Specifically, this json should have a `action` key (with the name of the tool to use) and a `action_input` key (with the input to the tool going here).

The only values that should be in the "action" field are: {tool_names}

The $JSON_BLOB should only contain a SINGLE action, do NOT return a list of multiple actions. DO NOT PUT the string 'json' in between the brackets and double quotes. Here is an example of a valid $JSON_BLOB:

```
{{{{
  "action": $TOOL_NAME,
  "action_input": $INPUT
}}}}
```

Use the following format:

Question: the input question you must answer
Thought: you should always think about what to do, each thought corresponds to a single action and observation.
Action:

```
$JSON_BLOB
```
Observation: the result of the action.
Thought: Next thought
Action:
```
$JSON_BLOB
```
Observation: the result of the next action.
... (this Thought/Action/Observation can repeat multiple times.)

Thought: I now know the final answer
Final Answer: the final answer to the original input question.

"""
SYSTEM_MESSAGE_SUFFIX = """Begin! Reminder to always use the exact characters `Final Answer` if you are certain of the final answer."""
HUMAN_MESSAGE = "{input}\n\n{scratchpad}"
ACT_OBS = """
Action:
```
{{
 'action': {tool_name},
 'action_input': {arg}
}}
```
Observation: {tool_name} returned {obs}
"""
