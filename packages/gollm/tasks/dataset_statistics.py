import sys
from entities import DatasetStatistics
from utils.statistics import analyze_csv

from taskrunner import TaskRunnerInterface

def cleanup():
    pass

def main():
    global taskrunner
    exit_code = 0

    try:
        taskrunner = TaskRunnerInterface(description="Dataset Statistics CLI")
        taskrunner.on_cancellation(cleanup)

        input_dict = taskrunner.read_input_dict_with_timeout()
        inputs = DatasetStatistics(**input_dict)

        taskrunner.log("Creating statistics from input")
        response = analyze_csv(dataset=inputs.dataset)
        taskrunner.log("Statistics from input created")

        taskrunner.write_output_dict_with_timeout({"response": response})

    except Exception as e:
        sys.stderr.write(f"Error: {str(e)}\n")
        sys.stderr.flush()
        exit_code = 1

    taskrunner.log("Shutting down")
    taskrunner.shutdown()
    sys.exit(exit_code)

if __name__ == "__main__":
    main()
