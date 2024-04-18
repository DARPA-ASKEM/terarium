from setuptools import setup, find_packages

setup(
    name="funman_task",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    entry_points={
        "console_scripts": [
            "funamn_task:amr_to_mmt=tasks.validate_modelconfig:main",
        ],
    },
    python_requires=">=3.8",
)
