import json
import signal
import sys
import concurrent.futures
import argparse
import os
import time
import threading
from typing import Callable


READ_CHUNK_SIZE = 1024*1024


class SelfDestructThread:
    def __init__(self, self_destruct_timeout_seconds):
        self.self_destruct_timeout_seconds = self_destruct_timeout_seconds
        self.stop_destruct_event = threading.Event()
        self.thread = threading.Thread(target=self._kill_after_timeout)

    def start(self):
        self.thread.start()

    def stop(self):
        self.stop_destruct_event.set()
        self.thread.join()

    def _kill_after_timeout(self):
        self.stop_destruct_event.wait(self.self_destruct_timeout_seconds)
        if not self.stop_destruct_event.is_set():
            # if for whatever reason this process is still around, do our best to self destruct
            os.kill(
                os.getpid(), signal.SIGKILL
            )  # Send the SIGTERM signal to the current process


class TaskRunnerInterface:
    def __init__(self, description: str):
        parser = argparse.ArgumentParser(description=description)
        parser.add_argument(
            "--id", type=str, required=True, help="The id of the request"
        )
        parser.add_argument(
            "--input", type=str, required=False, help="The name of the input pipe"
        )
        parser.add_argument(
            "--input_pipe", type=str, required=False, help="The name of the input pipe"
        )
        parser.add_argument(
            "--output_pipe",
            type=str,
            required=False,
            help="The name of the output pipe",
        )
        parser.add_argument(
            "--progress_pipe",
            type=str,
            required=False,
            help="The name of the progress pipe",
        )
        parser.add_argument(
            "--self_destruct_timeout_seconds",
            type=int,
            default=60 * 60 * 24,
            required=False,
            help="Process self destruct timeout in seconds",
        )
        args = parser.parse_args()
        self.id = args.id
        self.input = args.input
        self.input_pipe = args.input_pipe
        self.output_pipe = args.output_pipe
        self.progress_pipe = args.progress_pipe
        self.has_written_output = False

        if self.input is None and self.input_pipe is None:
            raise ValueError("Either `input` or `input_pipe` must be specified")

        # Start the self destruct timer
        self.self_destructor = SelfDestructThread(args.self_destruct_timeout_seconds)
        self.self_destructor.start()

    def log(self, msg: str):
        print(msg, flush=True)

    def shutdown(self):
        self.self_destructor.stop()

    def read_input_bytes_with_timeout(self, timeout_seconds: int = 30) -> bytes:
        def read_input() -> bytes:
            self.log("Reading input from input pipe")
            chunks = []
            with open(self.input_pipe, "rb") as f:
                while True:
                    chunk = f.read(READ_CHUNK_SIZE)
                    if chunk == b"":
                        break
                    chunks.append(chunk)
            return b"".join(chunks)

        if self.input is not None:
            self.log("Reading input from input argument")
            return self.input

        with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
            future = executor.submit(read_input)
            try:
                return future.result(timeout=timeout_seconds)
            except concurrent.futures.TimeoutError:
                raise TimeoutError("Reading from input pipe timed out")

    def read_input_str_with_timeout(self, timeout_seconds: int = 30) -> str:
        return self.read_input_str_with_timeout(timeout_seconds).decode("utf-8")

    def read_input_dict_with_timeout(self, timeout_seconds: int = 30) -> dict:
        return json.loads(self.read_input_str_with_timeout(timeout_seconds))

    def write_progress_str_with_timeout(self, progress: str, timeout_seconds: int = 30):
        def write_progress(progress_pipe: str, progress: str):
            bs = progress.encode("utf-8")
            with open(progress_pipe, 'wb') as f_out:
                f_out.write(bs)
                return

        # if no progress pipe is specified, just print the progress to stdout
        if self.progress_pipe is None:
            self.log("Writing progress to stdout")
            print(progress)
            return

        with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
            future = executor.submit(write_progress, self.progress_pipe, progress)
            try:
                return future.result(timeout=timeout_seconds)
            except concurrent.futures.TimeoutError:
                print('Writing to progress pipe {} timed out'.format(self.progress_pipe), flush=True)
                raise TimeoutError('Writing to progress pipe timed out')

    def write_progress_dict_with_timeout(self, progress: dict, timeout_seconds: int):
        return self.write_progress_str_with_timeout(json.dumps(progress), timeout_seconds)

    def write_output_str_with_timeout(self, output: str, timeout_seconds: int = 30):
        def write_output(output: str):
            self.log("Writing output to output pipe")
            bs = output.encode("utf-8")
            with open(self.output_pipe, "wb") as f_out:
                f_out.write(bs)
                return

        # output should only be written once
        if self.has_written_output:
            raise ValueError("Output has already been written")

        self.has_written_output = True

        # if no output pipe is specified, just print the output to stdout
        if self.output_pipe is None:
            self.log("Writing output to stdout")
            print(output)
            return

        # signal to the taskrunner that it should stop consuming progress
        self.write_progress_dict_with_timeout({'done':True}, timeout_seconds)

        # otherwise use the output pipe
        with concurrent.futures.ThreadPoolExecutor(max_workers=1) as executor:
            future = executor.submit(write_output, output)
            try:
                return future.result(timeout=timeout_seconds)
            except concurrent.futures.TimeoutError:
                raise TimeoutError("Writing to output pipe timed out")

    def write_output_dict_with_timeout(self, output: str, timeout_seconds: int = 30):
        return self.write_output_str_with_timeout(json.dumps(output), timeout_seconds)

    def on_cancellation(self, func: Callable):
        def signal_handler(sig, frame):
            func()
            sys.exit(1)

        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)
