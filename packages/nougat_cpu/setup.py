from setuptools import setup, find_packages

setup(
    name="nougat_task",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    entry_points={
        "console_scripts": [
            "nougat_task:extraction=tasks.extraction:main",
        ],
    },
    python_requires=">=3.10",
)
