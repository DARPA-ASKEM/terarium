import traceback

from ipykernel.kernelbase import Kernel
from chatty.controller import Controller
from archytas.react import ReActAgent

import logging
logger = logging.getLogger(__name__)

class PythonLLMKernel(Kernel):
    implementation = "askem-chatty-py"
    implementation_version = "0.1"
    banner = "Chatty ASKEM"

    language_info = {
        "mimetype": "text/plain",
        "name": "text",
        "file_extension": ".txt",
    }


    def setup_instance(self, *args, **kwargs):
        # Init LLM agent
        self.agent = ReActAgent(tools=[Controller()])
        return super().setup_instance(*args, **kwargs)


    def do_execute(self, code, silent, store_history=True, user_expressions=None, allow_stdin=False, *, cell_id=None):
        # Send "code" to LLM Agent. The "code" is actually the LLM query
        try:
            result = self.agent.react(code)
        except Exception as err:
            error_text = f"""LLM Error: 
{err}

{traceback.format_exc()}
"""
            stream_content = {"name": "stderr", "text": error_text}
            self.send_response(self.iopub_socket, "stream", stream_content)
            return {
                "status": "error",
                "execution_count": self.execution_count,
                "payload": [],
                'user_expressions': {},
            }
        
        if not silent:
            stream_content = {"name": "stdout", "text": f"{result}"}
            self.send_response(self.iopub_socket, "stream", stream_content)

        return {
            "status": "ok",
            "execution_count": self.execution_count,
            "payload": [],
            'user_expressions': {},
        }


    def send_response(self, stream, msg_or_type, content=None, ident=None, buffers=None, track=False, header=None, metadata=None, channel="shell"):
        logger.warn("sending response")
        # Parse response as needed
        return super().send_response(stream, msg_or_type, content, ident, buffers, track, header, metadata, channel)


if __name__ == '__main__':
    from ipykernel.kernelapp import IPKernelApp
    IPKernelApp.launch_instance(kernel_class=PythonLLMKernel)
