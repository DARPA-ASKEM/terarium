from setuptools import setup, find_packages

setup(
    name="MiraC",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[

    ],
    entry_points={
        "console_scripts": [
            "mirac:sbml_to_petrinet=tasks.sbml_to_petrinet:main",
            "mirac:mdl_to_stockflow=tasks.mdl_to_stockflow:main",
            "mirac:stella_to_stockflow=tasks.stella_to_stockflow:main"
        ],
    },
    python_requires=">=3.8",
)
