from setuptools import setup, find_packages

setup(
    name="equation_extraction_task",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    entry_points={
        "console_scripts": [
            "equation_extraction_task:extract_equations=tasks.extract_equations:main",
        ],
    },
    python_requires=">=3.11",
)
