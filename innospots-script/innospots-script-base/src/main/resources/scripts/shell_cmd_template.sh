##!/usr/bin/env bash

CRT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd )
input_params="$1"
IFS=',' read -r -a pairs <<< "$input_params"
for pair in "${pairs[@]}"; do
    IFS='=' read -r key value <<< "$pair"
    eval "$key=\"$value\""
done

