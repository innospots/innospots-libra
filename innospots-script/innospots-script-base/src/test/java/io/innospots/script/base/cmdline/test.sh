##!/usr/bin/env bash

CRT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd )

# input param：xx=yy,ss=dd,oo=aa
input_params="$1"

# parse input param
IFS=',' read -r -a pairs <<< "$input_params"

# set variables to current shell environment
for pair in "${pairs[@]}"; do
    IFS='=' read -r key value <<< "$pair"
    eval "$key=\"$value\""
done

echo "$CRT_DIR"
# output variables
echo "xx: $xx"
echo "ss: $ss"
echo "oo: $oo"