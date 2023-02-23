FROM python:3.10
RUN useradd -m jupyter
EXPOSE 8888
WORKDIR /jupyter

# Prepare for building js output
RUN apt update && apt install -y npm
RUN npm install -g typescript
COPY --chown=1000:1000 ./package.json /jupyter/package.json
RUN npm install


# Install Julia
RUN wget https://julialang-s3.julialang.org/bin/linux/x64/1.8/julia-1.8.5-linux-x86_64.tar.gz
RUN tar -xzf julia-1.8.5-linux-x86_64.tar.gz && mv julia-1.8.5 /opt/julia && \
    ln -s /opt/julia/bin/julia /usr/local/bin/julia && rm julia-1.8.5-linux-x86_64.tar.gz

# Add Julia to Jupyter
USER 1000
RUN julia -e 'using Pkg; Pkg.add("IJulia");'

# Install Python requirements
USER root
RUN pip install jupyterlab jupyterlab_server pandas matplotlib xarray numpy

# Switch to non-root user
USER 1000

# Install Julia requirements
RUN julia -e ' \
    packages = [ \
        "Catlab", "AlgebraicPetri", "DataSets", "EasyModelAnalysis", "XLSX", "Plots", "Downloads", \
        "DataFrames", "ModelingToolkit", "Symbolics", \
    ]; \
    using Pkg; \
    for p in packages; Pkg.add(p); \
    end;'

# Copy src code over
COPY --chown=1000:1000 . /jupyter

# Build Notebook javascript bundle
RUN npm run build

CMD ["python", "main.py", "--ip", "0.0.0.0"]