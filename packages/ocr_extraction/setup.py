from setuptools import setup, find_packages

setup(
    name="ocr_extraction_task",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    entry_points={
        "console_scripts": [
            "ocr_extraction_task:ocr_extraction=tasks.ocr_extraction:main",
        ],
    },
    python_requires=">=3.11",
)
