# Base Image -- We just install 
FROM python:3.7-alpine as base

# Make some dummy directory that's doing the installation.
RUN mkdir /install
WORKDIR /install

# Copy the Requirements.
COPY requirements.txt ./

# Add the build-deps, no special libraries required.
RUN apk add --update --no-cache --virtual .build-deps g++ gcc

# Run install. Copy from /install
RUN pip install --no-cache --prefix="/install" -r requirements.txt

# -----------------------------------------------------

# Working Image -- We copy from Base.

FROM python:3.7-alpine
RUN mkdir -p /app

# We'll make sure that we got an app directory we're working from.
# Rather than putting everything at root.

WORKDIR /app

# Copy the Compiled Files over, we don't have to run pip install
COPY --from=base /install /usr/local

# Copy our Source Code.
COPY . /app

# And finally, run our bot
CMD ["python", "-m" "ZyenyoBot"]