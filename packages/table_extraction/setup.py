from setuptools import setup, find_packages

setup(
    name="table_extraction_task",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[
        "torch==2.5.1"
    ],
    entry_points={
        "console_scripts": [
            "table_extraction_task:extract_tables=tasks.extract_tables:main",
        ],
    },
    python_requires=">=3.11",
)
