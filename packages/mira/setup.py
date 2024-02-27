from setuptools import setup, find_packages

setup(
    name="mira",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    entry_points={
        "console_scripts": [
            "mira:sbml_to_petrinet=tasks.sbml_to_petrinet:main",
            "mira:mdl_to_stockflow=tasks.mdl_to_stockflow:main",
            "mira:stella_to_stockflow=tasks.stella_to_stockflow:main"
        ],
    },
    python_requires=">=3.8",
)
