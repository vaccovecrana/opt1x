#!/bin/bash

# Script to scan subfolders for .java files and concatenate their contents into out.md

if [ $# -ne 1 ]; then
  echo "Usage: $0 "
  echo "Example: $0 /path/to/project"
  exit 1
fi

input_dir="$1"

if [ ! -d "$input_dir" ]; then
  echo "Error: Directory '$input_dir' does not exist."
  exit 1
fi

if [ ! -r "$input_dir" ]; then
  echo "Error: Directory '$input_dir' is not readable."
  exit 1
fi

output_file="$(dirname "$0")/out.txt"

> "$output_file"

file_count=0

while IFS= read -r src_file; do
  ((file_count++))

  relative_path="${src_file#"$input_dir"}"
  relative_path="${relative_path#/}" # Remove leading slash if present

  echo "# File: $relative_path" >> "$output_file"
  echo '```' >> "$output_file"
  cat "$src_file" >> "$output_file"
  echo '```' >> "$output_file"
  echo "" >> "$output_file"

done < <(find "$input_dir" -type f -name "*.java" -o -name "*.c")

if [ $file_count -eq 0 ]; then
  echo "No source files found in '$input_dir' or its subdirectories."
  echo "# No source files found" > "$output_file"
else
  echo "Found $file_count source files. Concatenated into '$output_file'."
fi