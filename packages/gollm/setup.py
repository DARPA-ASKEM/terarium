from setuptools import setup, find_packages

setup(
    name="gollm_task",
    version="0.1.0",
    packages=find_packages(),
    install_requires=[],
    entry_points={
        "console_scripts": [
            "gollm_task:configure_model=tasks.configure_model:main",
            "gollm_task:model_card=tasks.model_card:main",
            "gollm_task:embedding=tasks.embedding:main",
            "gollm_task:compare_models=tasks.compare_models:main",
            "gollm_task:dataset_configure=tasks.dataset_configure:main",
            "gollm_task:generate_summary=tasks.generate_summary:main",
            "gollm_task:generate_response=tasks.generate_response:main",
        ],
    },
    python_requires=">=3.11",
)
